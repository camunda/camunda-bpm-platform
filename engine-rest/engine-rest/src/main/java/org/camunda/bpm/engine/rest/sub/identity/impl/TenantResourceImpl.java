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
package org.camunda.bpm.engine.rest.sub.identity.impl;

import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.TENANT;

import java.net.URI;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.rest.TenantRestService;
import org.camunda.bpm.engine.rest.dto.ResourceOptionsDto;
import org.camunda.bpm.engine.rest.dto.identity.TenantDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.identity.TenantGroupMembersResource;
import org.camunda.bpm.engine.rest.sub.identity.TenantResource;
import org.camunda.bpm.engine.rest.sub.identity.TenantUserMembersResource;

import com.fasterxml.jackson.databind.ObjectMapper;;

public class TenantResourceImpl extends AbstractIdentityResource implements TenantResource {

  private String rootResourcePath;

  public TenantResourceImpl(String processEngineName, String tenantId, String rootResourcePath, ObjectMapper objectMapper) {
    super(processEngineName, TENANT, tenantId, objectMapper);
    this.rootResourcePath = rootResourcePath;
  }

  public TenantDto getTenant(UriInfo context) {

    Tenant tenant = findTenantObject();
    if(tenant == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Tenant with id " + resourceId + " does not exist");
    }

    TenantDto dto = TenantDto.fromTenant(tenant);
    return dto;
  }

  public void updateTenant(TenantDto tenantDto) {
    ensureNotReadOnly();

    Tenant tenant = findTenantObject();
    if(tenant == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Tenant with id " + resourceId + " does not exist");
    }

    tenantDto.update(tenant);

    identityService.saveTenant(tenant);
  }

  public void deleteTenant() {
    ensureNotReadOnly();

    identityService.deleteTenant(resourceId);
  }

  public ResourceOptionsDto availableOperations(UriInfo context) {
    ResourceOptionsDto dto = new ResourceOptionsDto();

    // add links if operations are authorized
    URI uri = context.getBaseUriBuilder()
        .path(rootResourcePath)
        .path(TenantRestService.PATH)
        .path(resourceId)
        .build();

    dto.addReflexiveLink(uri, HttpMethod.GET, "self");

    if(!identityService.isReadOnly() && isAuthorized(DELETE)) {
      dto.addReflexiveLink(uri, HttpMethod.DELETE, "delete");
    }
    if(!identityService.isReadOnly() && isAuthorized(UPDATE)) {
      dto.addReflexiveLink(uri, HttpMethod.PUT, "update");
    }
    return dto;
  }

  public TenantUserMembersResource getTenantUserMembersResource() {
    return new TenantUserMembersResourceImpl(getProcessEngine().getName(), resourceId, rootResourcePath, getObjectMapper());
  }

  public TenantGroupMembersResource getTenantGroupMembersResource() {
    return new TenantGroupMembersResourceImpl(getProcessEngine().getName(), resourceId, rootResourcePath, getObjectMapper());
  }

  protected Tenant findTenantObject() {
    try {
      return identityService.createTenantQuery()
          .tenantId(resourceId)
          .singleResult();

    } catch(ProcessEngineException e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR,
          "Exception while performing tenant query: " + e.getMessage());
    }
  }

}
