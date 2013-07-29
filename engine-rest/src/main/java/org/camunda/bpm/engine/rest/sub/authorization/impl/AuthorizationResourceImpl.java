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
package org.camunda.bpm.engine.rest.sub.authorization.impl;

import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.AUTHORIZATION;

import java.net.URI;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.rest.AuthorizationRestService;
import org.camunda.bpm.engine.rest.dto.authorization.AuthorizationDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.authorization.AuthorizationResource;
import org.camunda.bpm.engine.rest.sub.impl.AbstractAuthorizedRestResource;

/**
 * 
 * @author Daniel Meyer
 * 
 */
public class AuthorizationResourceImpl extends AbstractAuthorizedRestResource implements AuthorizationResource {

  protected final AuthorizationService authorizationService;
  protected String relativeRootResourcePath;

  public AuthorizationResourceImpl(ProcessEngine processEngine, String resourceId, String relativeRootResourcePath) {
    super(processEngine, AUTHORIZATION, resourceId);
    this.relativeRootResourcePath = relativeRootResourcePath;
    authorizationService = processEngine.getAuthorizationService();
  }

  public AuthorizationDto getAuthorization(UriInfo context) {

    Authorization dbAuthorization = getDbAuthorization();

    AuthorizationDto dto = AuthorizationDto.fromAuthorization(dbAuthorization);
    
    // add links if operations are authorized
    URI uri = context.getBaseUriBuilder()
        .path(relativeRootResourcePath)
        .path(AuthorizationRestService.class)
        .path(resourceId)
        .build();
    
    dto.addReflexiveLink(uri, HttpMethod.GET, "self");
    
    if (isAuthorized(DELETE)) {
      dto.addReflexiveLink(uri, HttpMethod.DELETE, "delete");
    }
    if (isAuthorized(UPDATE)) {
      dto.addReflexiveLink(uri, HttpMethod.PUT, "update");
    }

    return dto;
  }

  public void deleteAuthorization() {
    Authorization dbAuthorization = getDbAuthorization();
    authorizationService.deleteAuthorization(dbAuthorization.getId());
  }

  public void updateAuthorization(AuthorizationDto dto) {
    // get db auth
    Authorization dbAuthorization = getDbAuthorization();
    // copy values from dto
    AuthorizationDto.update(dto, dbAuthorization);
    // save
    authorizationService.saveAuthorization(dbAuthorization);
  }

  // utils //////////////////////////////////////////////////

  protected Authorization getDbAuthorization() {
    Authorization dbAuthorization = authorizationService.createAuthorizationQuery()
      .authorizationId(resourceId)
      .singleResult();

    if (dbAuthorization == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Authorization with id " + resourceId + " does not exist.");

    } else {
      return dbAuthorization;

    }
  }

}
