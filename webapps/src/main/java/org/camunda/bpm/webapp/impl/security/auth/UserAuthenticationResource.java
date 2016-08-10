/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.webapp.impl.security.auth;

import static org.camunda.bpm.engine.authorization.Permissions.ACCESS;
import static org.camunda.bpm.engine.authorization.Resources.APPLICATION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

/**
 * Jax-Rs resource allowing users to authenticate with username and password</p>
 *
 * @author Daniel Meyer
 *
 */
@Path(UserAuthenticationResource.PATH)
public class UserAuthenticationResource {

  public static final String PATH = "/auth/user";

  private static final String[] APPS = new String[] { "cockpit", "tasklist", "admin"};

  @Context
  protected HttpServletRequest request;

  @GET
  @Path("/{processEngineName}")
  public Response getAuthenticatedUser(@PathParam("processEngineName") String engineName) {
    Authentications allAuthentications = Authentications.getCurrent();

    if (allAuthentications == null) {
      return notFound();
    }

    Authentication engineAuth = allAuthentications.getAuthenticationForProcessEngine(engineName);

    if (engineAuth == null) {
      return notFound();
    } else {
      return Response.ok(AuthenticationDto.fromAuthentication(engineAuth)).build();
    }
  }

  @POST
  @Path("/{processEngineName}/login/{appName}")
  public Response doLogin(
      @PathParam("processEngineName") String engineName,
      @PathParam("appName") String appName,
      @FormParam("username") String username,
      @FormParam("password") String password) {

    final ProcessEngine processEngine = lookupProcessEngine(engineName);
    if(processEngine == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Process engine with name "+engineName+" does not exist");
    }

    // make sure authentication is executed without authentication :)
    processEngine.getIdentityService().clearAuthentication();

    // check password / username
    boolean isPasswordValid = processEngine.getIdentityService().checkPassword(username, password);

    if (!isPasswordValid) {
      return unauthorized();

    } else {

      List<String> groupIds = getGroupsOfUser(processEngine, username);
      List<String> tenantIds = getTenantsOfUser(processEngine, username);

      // check user's app authorizations
      AuthorizationService authorizationService = processEngine.getAuthorizationService();

      HashSet<String> authorizedApps = new HashSet<String>();
      authorizedApps.add("welcome");

      if (processEngine.getProcessEngineConfiguration().isAuthorizationEnabled()) {
        for (String application: APPS) {
          if (isAuthorizedForApp(authorizationService, username, groupIds, application)) {
            authorizedApps.add(application);
          }
        }

      } else {
        Collections.addAll(authorizedApps, APPS);
      }

      if (!authorizedApps.contains(appName)) {
        return forbidden();
      }

      final Authentications authentications = Authentications.getCurrent();

      // create new authentication
      UserAuthentication newAuthentication = new UserAuthentication(username, engineName);
      newAuthentication.setGroupIds(groupIds);
      newAuthentication.setTenantIds(tenantIds);
      newAuthentication.setAuthorizedApps(authorizedApps);
      authentications.addAuthentication(newAuthentication);

      // send reponse including updated cookie
      return Response.ok(AuthenticationDto.fromAuthentication(newAuthentication)).build();
    }
  }

  protected List<String> getGroupsOfUser(ProcessEngine engine, String userId) {
    List<Group> groups = engine.getIdentityService().createGroupQuery()
      .groupMember(userId)
      .list();

    List<String> groupIds = new ArrayList<String>();
    for (Group group : groups) {
      groupIds.add(group.getId());
    }
    return groupIds;
  }

  protected List<String> getTenantsOfUser(ProcessEngine engine, String userId) {
    List<Tenant> tenants = engine.getIdentityService().createTenantQuery()
      .userMember(userId)
      .includingGroupsOfUser(true)
      .list();

    List<String> tenantIds = new ArrayList<String>();
    for(Tenant tenant : tenants) {
      tenantIds.add(tenant.getId());
    }
    return tenantIds;
  }

  @POST
  @Path("/{processEngineName}/logout")
  public Response doLogout(@PathParam("processEngineName") String engineName) {
    final Authentications authentications = Authentications.getCurrent();

    // remove authentication for process engine
    authentications.removeAuthenticationForProcessEngine(engineName);

    return Response.ok().build();
  }

  protected ProcessEngine lookupProcessEngine(String engineName) {

    ServiceLoader<ProcessEngineProvider> serviceLoader = ServiceLoader.load(ProcessEngineProvider.class);
    Iterator<ProcessEngineProvider> iterator = serviceLoader.iterator();

    if(iterator.hasNext()) {
      ProcessEngineProvider provider = iterator.next();
      return provider.getProcessEngine(engineName);

    } else {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, "Could not find an implementation of the "+ProcessEngineProvider.class+"- SPI");

    }

  }

  private Response unauthorized() {
    return Response.status(Status.UNAUTHORIZED).build();
  }

  private Response forbidden() {
    return Response.status(Status.FORBIDDEN).build();
  }

  protected boolean isAuthorizedForApp(AuthorizationService authorizationService, String username, List<String> groupIds, String application) {
    return authorizationService.isUserAuthorized(username, groupIds, ACCESS, APPLICATION, application);
  }

  private Response notFound() {
    return Response.status(Status.NOT_FOUND).build();
  }
}
