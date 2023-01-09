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
package org.camunda.bpm.engine.rest.sub.authorization.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.rest.AuthorizationRestService;
import org.camunda.bpm.engine.rest.dto.ResourceOptionsDto;
import org.camunda.bpm.engine.rest.dto.authorization.AuthorizationDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.impl.AbstractAuthorizedRestResource;
import org.camunda.bpm.engine.rest.sub.authorization.AuthorizationResource;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.AUTHORIZATION;

/**
 *
 * @author Daniel Meyer
 *
 */
public class AuthorizationResourceImpl extends AbstractAuthorizedRestResource implements AuthorizationResource {

  protected final AuthorizationService authorizationService;
  protected String relativeRootResourcePath;

  public AuthorizationResourceImpl(String processEngineName, String resourceId, String relativeRootResourcePath, ObjectMapper objectMapper) {
    super(processEngineName, AUTHORIZATION, resourceId, objectMapper);
    this.relativeRootResourcePath = relativeRootResourcePath;
    authorizationService = getProcessEngine().getAuthorizationService();
  }

  public AuthorizationDto getAuthorization(UriInfo context) {

    Authorization dbAuthorization = getDbAuthorization();

    return AuthorizationDto.fromAuthorization(dbAuthorization, getProcessEngine().getProcessEngineConfiguration());

  }

  public void deleteAuthorization() {
    Authorization dbAuthorization = getDbAuthorization();
    authorizationService.deleteAuthorization(dbAuthorization.getId());
  }

  public void updateAuthorization(AuthorizationDto dto) {
    // get db auth
    Authorization dbAuthorization = getDbAuthorization();
    // copy values from dto
    AuthorizationDto.update(dto, dbAuthorization, getProcessEngine().getProcessEngineConfiguration());
    // save
    authorizationService.saveAuthorization(dbAuthorization);
  }

  public ResourceOptionsDto availableOperations(UriInfo context) {

    ResourceOptionsDto dto = new ResourceOptionsDto();

    URI uri = context.getBaseUriBuilder()
        .path(relativeRootResourcePath)
        .path(AuthorizationRestService.PATH)
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

  // utils //////////////////////////////////////////////////

  protected Authorization getDbAuthorization() {
    Authorization dbAuthorization = authorizationService.createAuthorizationQuery()
      .authorizationId(resourceId)
      .singleResult();

    if (dbAuthorization == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Authorization with id " + resourceId + " does not exist.");

    } else {
      return dbAuthorization;

    }
  }

}
