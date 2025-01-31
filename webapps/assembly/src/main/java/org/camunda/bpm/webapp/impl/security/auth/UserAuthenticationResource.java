/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.webapp.impl.security.auth;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.webapp.impl.WebappLogger;
import org.camunda.bpm.webapp.impl.util.ProcessEngineUtil;
import org.camunda.bpm.webapp.impl.util.ServletContextUtil;

/**
 * Jax-Rs resource allowing users to authenticate with username and password</p>
 *
 * @author Daniel Meyer
 *
 */
@Path(UserAuthenticationResource.PATH)
public class UserAuthenticationResource {

  protected final static WebappLogger LOGGER = WebappLogger.INSTANCE;

  public static final String PATH = "/auth/user";

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

    final ProcessEngine processEngine = ProcessEngineUtil.lookupProcessEngine(engineName);
    if(processEngine == null) {
      throw LOGGER.invalidRequestEngineNotFoundForName(engineName);
    }

    // make sure authentication is executed without authentication :)
    processEngine.getIdentityService().clearAuthentication();

    // check password / username
    boolean isPasswordValid = processEngine.getIdentityService().checkPassword(username, password);

    if (!isPasswordValid) {
      if(isWebappsAuthenticationLoggingEnabled(processEngine)) {
        LOGGER.infoWebappFailedLogin(username, "bad credentials");
      }
      return unauthorized();
    }

    UserAuthentication authentication = AuthenticationUtil.createAuthentication(processEngine, username);
    if (authentication == null) {
      if(isWebappsAuthenticationLoggingEnabled(processEngine)) {
        LOGGER.infoWebappFailedLogin(username, "not authorized");
      }
      return unauthorized();
    }

    ServletContext servletContext = request.getServletContext();
    Date cacheValidationTime = ServletContextUtil.getAuthCacheValidationTime(servletContext);
    if (cacheValidationTime != null) {
      LOGGER.traceCacheValidationTime(cacheValidationTime);
      authentication.setCacheValidationTime(cacheValidationTime);
    }

    Set<String> authorizedApps = authentication.getAuthorizedApps();

    if (!authorizedApps.contains(appName)) {
      if(isWebappsAuthenticationLoggingEnabled(processEngine)) {
        LOGGER.infoWebappFailedLogin(username, "not authorized");
      }
      return forbidden();
    }

    if (request != null) {
      AuthenticationUtil.revalidateSession(request, authentication);
    }

    if(isWebappsAuthenticationLoggingEnabled(processEngine)) {
      LOGGER.infoWebappSuccessfulLogin(username);
    }
    return Response.ok(AuthenticationDto.fromAuthentication(authentication)).build();
  }

  protected List<String> getGroupsOfUser(ProcessEngine engine, String userId) {
    List<Group> groups = engine.getIdentityService().createGroupQuery()
      .groupMember(userId)
      .list();

    List<String> groupIds = new ArrayList<>();
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

    List<String> tenantIds = new ArrayList<>();
    for(Tenant tenant : tenants) {
      tenantIds.add(tenant.getId());
    }
    return tenantIds;
  }

  @POST
  @Path("/{processEngineName}/logout")
  public Response doLogout(@PathParam("processEngineName") String engineName) {
    final Authentications authentications = Authentications.getCurrent();

    ProcessEngine processEngine = ProcessEngineUtil.lookupProcessEngine(engineName);
    if(processEngine == null) {
      throw LOGGER.invalidRequestEngineNotFoundForName(engineName);
    }

    // remove authentication for process engine
    if(authentications != null) {
      UserAuthentication removedAuthentication = authentications.removeByEngineName(engineName);

      if(isWebappsAuthenticationLoggingEnabled(processEngine) && removedAuthentication != null) {
        LOGGER.infoWebappLogout(removedAuthentication.getName());
      }
    }

    return Response.ok().build();
  }

  protected Response unauthorized() {
    return Response.status(Status.UNAUTHORIZED).build();
  }

  protected Response forbidden() {
    return Response.status(Status.FORBIDDEN).build();
  }

  protected Response notFound() {
    return Response.status(Status.NOT_FOUND).build();
  }

  private boolean isWebappsAuthenticationLoggingEnabled(ProcessEngine processEngine) {
    return ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).isWebappsAuthenticationLoggingEnabled();
  }
}
