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
package org.camunda.bpm.admin.impl.web;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;

import java.util.Iterator;
import java.util.ServiceLoader;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Providers;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.rest.dto.identity.UserDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.impl.UserRestServiceImpl;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;
import org.camunda.bpm.engine.rest.util.ProvidersUtil;
import org.camunda.bpm.webapp.impl.security.SecurityActions;
import org.camunda.bpm.webapp.impl.security.SecurityActions.SecurityAction;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>Jax RS resource allowing to perform the setup steps.</p>
 *
 * <p>All methods of this class must throw Status.FORBIDDEN exception if
 * setup actions are unavailable.</p>
 *
 * @author Daniel Meyer
 *
 */
@Path("/setup/{engine}")
public class SetupResource {

  @Context
  protected Providers providers;

  @Path("/user/create")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void createInitialUser(final @PathParam("engine") String processEngineName, final UserDto user) {

    final ProcessEngine processEngine = lookupProcessEngine(processEngineName);
    if(processEngine == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Process Engine '"+processEngineName+"' does not exist.");
    }

    SecurityActions.runWithoutAuthentication(new SecurityAction<Void>() {
      public Void execute() {
        createInitialUserInternal(processEngineName, user, processEngine);
        return null;
      }
    }, processEngine);

  }

  protected void createInitialUserInternal(String processEngineName, UserDto user, ProcessEngine processEngine) {

    ObjectMapper objectMapper = getObjectMapper();

    // make sure we can process this request at this time
    ensureSetupAvailable(processEngine);

    // reuse logic from rest api implementation
    UserRestServiceImpl userRestServiceImpl = new UserRestServiceImpl(processEngineName, objectMapper);
    userRestServiceImpl.createUser(user);

    // crate the camunda admin group
    ensureCamundaAdminGroupExists(processEngine);

    // create group membership (add new user to admin group)
    processEngine.getIdentityService()
      .createMembership(user.getProfile().getId(), Groups.CAMUNDA_ADMIN);
  }

  protected ObjectMapper getObjectMapper() {
    if(providers != null) {
      return ProvidersUtil
        .resolveFromContext(providers, ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE, this.getClass());
    }
    else {
      return null;
    }
  }

  protected void ensureCamundaAdminGroupExists(ProcessEngine processEngine) {

    final IdentityService identityService = processEngine.getIdentityService();
    final AuthorizationService authorizationService = processEngine.getAuthorizationService();

    // create group
    if(identityService.createGroupQuery().groupId(Groups.CAMUNDA_ADMIN).count() == 0) {
      Group camundaAdminGroup = identityService.newGroup(Groups.CAMUNDA_ADMIN);
      camundaAdminGroup.setName("camunda BPM Administrators");
      camundaAdminGroup.setType(Groups.GROUP_TYPE_SYSTEM);
      identityService.saveGroup(camundaAdminGroup);
    }

    // create ADMIN authorizations on all built-in resources
    for (Resource resource : Resources.values()) {
      if(authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_ADMIN).resourceType(resource).resourceId(ANY).count() == 0) {
        AuthorizationEntity userAdminAuth = new AuthorizationEntity(AUTH_TYPE_GRANT);
        userAdminAuth.setGroupId(Groups.CAMUNDA_ADMIN);
        userAdminAuth.setResource(resource);
        userAdminAuth.setResourceId(ANY);
        userAdminAuth.addPermission(ALL);
        authorizationService.saveAuthorization(userAdminAuth);
      }
    }

  }

  protected void ensureSetupAvailable(ProcessEngine processEngine) {
    if (processEngine.getIdentityService().isReadOnly()
        || (processEngine.getIdentityService().createUserQuery().memberOfGroup(Groups.CAMUNDA_ADMIN).count() > 0)) {

      throw new InvalidRequestException(Status.FORBIDDEN, "Setup action not available");
    }
  }

  protected ProcessEngine lookupProcessEngine(String engineName) {

    ServiceLoader<ProcessEngineProvider> serviceLoader = ServiceLoader.load(ProcessEngineProvider.class);
    Iterator<ProcessEngineProvider> iterator = serviceLoader.iterator();

    if (iterator.hasNext()) {
      ProcessEngineProvider provider = iterator.next();
      return provider.getProcessEngine(engineName);

    } else {
      throw new RestException(Status.BAD_REQUEST, "Could not find an implementation of the "+ProcessEngineProvider.class+"- SPI");

    }

  }


}
