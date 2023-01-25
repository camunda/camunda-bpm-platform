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
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.rest.GroupRestService;
import org.camunda.bpm.engine.rest.dto.ResourceOptionsDto;
import org.camunda.bpm.engine.rest.dto.identity.GroupDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.identity.GroupMembersResource;
import org.camunda.bpm.engine.rest.sub.identity.GroupResource;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.GROUP;

/**
 * @author Daniel Meyer
 *
 */
public class GroupResourceImpl extends AbstractIdentityResource implements GroupResource {

  private String rootResourcePath;

  public GroupResourceImpl(String processEngineName, String groupId, String rootResourcePath, ObjectMapper objectMapper) {
    super(processEngineName, GROUP, groupId, objectMapper);
    this.rootResourcePath = rootResourcePath;
  }

  public GroupDto getGroup(UriInfo context) {

    Group dbGroup = findGroupObject();
    if(dbGroup == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Group with id " + resourceId + " does not exist");
    }

    GroupDto group = GroupDto.fromGroup(dbGroup);

    return group;
  }

  public ResourceOptionsDto availableOperations(UriInfo context) {

    ResourceOptionsDto dto = new ResourceOptionsDto();

    // add links if operations are authorized
    URI uri = context.getBaseUriBuilder()
        .path(rootResourcePath)
        .path(GroupRestService.PATH)
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


  public void updateGroup(GroupDto group) {
    ensureNotReadOnly();

    Group dbGroup = findGroupObject();
    if(dbGroup == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Group with id " + resourceId + " does not exist");
    }

    group.update(dbGroup);

    identityService.saveGroup(dbGroup);
  }


  public void deleteGroup() {
    ensureNotReadOnly();
    identityService.deleteGroup(resourceId);
  }

  public GroupMembersResource getGroupMembersResource() {
    return new GroupMembersResourceImpl(getProcessEngine().getName(), resourceId, rootResourcePath, getObjectMapper());
  }

  protected Group findGroupObject() {
    try {
      return identityService.createGroupQuery()
          .groupId(resourceId)
          .singleResult();
    } catch(ProcessEngineException e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, "Exception while performing group query: "+e.getMessage());
    }
  }


}
