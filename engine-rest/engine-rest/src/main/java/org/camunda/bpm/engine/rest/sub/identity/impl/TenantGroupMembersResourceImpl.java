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
package org.camunda.bpm.engine.rest.sub.identity.impl;

import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;

import java.net.URI;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.rest.TenantRestService;
import org.camunda.bpm.engine.rest.dto.ResourceOptionsDto;
import org.camunda.bpm.engine.rest.sub.identity.TenantGroupMembersResource;
import org.camunda.bpm.engine.rest.util.PathUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TenantGroupMembersResourceImpl extends AbstractIdentityResource implements TenantGroupMembersResource {

  public TenantGroupMembersResourceImpl(String processEngineName, String resourceId, String rootResourcePath, ObjectMapper objectMapper) {
    super(processEngineName, Resources.TENANT_MEMBERSHIP, resourceId, objectMapper);
    this.relativeRootResourcePath = rootResourcePath;
  }

  public void createMembership(String groupId) {
    ensureNotReadOnly();

    groupId = PathUtil.decodePathParam(groupId);
    identityService.createTenantGroupMembership(resourceId, groupId);
  }

  public void deleteMembership(String groupId) {
    ensureNotReadOnly();

    groupId = PathUtil.decodePathParam(groupId);
    identityService.deleteTenantGroupMembership(resourceId, groupId);
  }

  public ResourceOptionsDto availableOperations(UriInfo context) {
    ResourceOptionsDto dto = new ResourceOptionsDto();

    URI uri = context.getBaseUriBuilder()
        .path(relativeRootResourcePath)
        .path(TenantRestService.PATH)
        .path(resourceId)
        .path(TenantGroupMembersResource.PATH)
        .build();

    dto.addReflexiveLink(uri, HttpMethod.GET, "self");

    if (!identityService.isReadOnly() && isAuthorized(DELETE)) {
      dto.addReflexiveLink(uri, HttpMethod.DELETE, "delete");
    }
    if (!identityService.isReadOnly() && isAuthorized(CREATE)) {
      dto.addReflexiveLink(uri, HttpMethod.PUT, "create");
    }

    return dto;
  }

}
