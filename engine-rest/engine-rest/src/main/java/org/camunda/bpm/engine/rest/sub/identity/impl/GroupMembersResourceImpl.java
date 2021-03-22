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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.rest.GroupRestService;
import org.camunda.bpm.engine.rest.dto.ResourceOptionsDto;
import org.camunda.bpm.engine.rest.sub.identity.GroupMembersResource;
import org.camunda.bpm.engine.rest.util.PathUtil;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;

/**
 * @author Daniel Meyer
 *
 */
public class GroupMembersResourceImpl extends AbstractIdentityResource implements GroupMembersResource {

  public GroupMembersResourceImpl(String processEngineName, String resourceId, String rootResourcePath, ObjectMapper objectMapper) {
    super(processEngineName, Resources.GROUP_MEMBERSHIP, resourceId, objectMapper);
    this.relativeRootResourcePath = rootResourcePath;
  }

  public void createGroupMember(String userId) {
    ensureNotReadOnly();

    userId = PathUtil.decodePathParam(userId);
    identityService.createMembership(userId, resourceId);
  }

  public void deleteGroupMember(String userId) {
    ensureNotReadOnly();

    userId = PathUtil.decodePathParam(userId);
    identityService.deleteMembership(userId, resourceId);
  }

  public ResourceOptionsDto availableOperations(UriInfo context) {

    ResourceOptionsDto dto = new ResourceOptionsDto();

    URI uri = context.getBaseUriBuilder()
        .path(relativeRootResourcePath)
        .path(GroupRestService.PATH)
        .path(resourceId)
        .path(PATH)
        .build();

    if (!identityService.isReadOnly() && isAuthorized(DELETE)) {
      dto.addReflexiveLink(uri, HttpMethod.DELETE, "delete");
    }
    if (!identityService.isReadOnly() && isAuthorized(CREATE)) {
      dto.addReflexiveLink(uri, HttpMethod.PUT, "create");
    }

    return dto;

  }

}
