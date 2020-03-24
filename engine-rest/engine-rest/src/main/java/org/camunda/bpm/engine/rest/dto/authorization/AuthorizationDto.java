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
package org.camunda.bpm.engine.rest.dto.authorization;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.PermissionConverter;

/**
 * @author Daniel Meyer
 *
 */
public class AuthorizationDto {

  protected String id;
  protected Integer type;
  protected String[] permissions;
  protected String userId;
  protected String groupId;
  protected Integer resourceType;
  protected String resourceId;
  protected Date removalTime;
  protected String rootProcessInstanceId;

  // transformers ///////////////////////////////////////

  public static AuthorizationDto fromAuthorization(Authorization dbAuthorization, ProcessEngineConfiguration engineConfiguration) {
    AuthorizationDto authorizationDto = new AuthorizationDto();

    authorizationDto.setId(dbAuthorization.getId());
    authorizationDto.setType(dbAuthorization.getAuthorizationType());

    Permission[] dbPermissions = getPermissions(dbAuthorization, engineConfiguration);
    authorizationDto.setPermissions(PermissionConverter.getNamesForPermissions(dbAuthorization, dbPermissions));

    authorizationDto.setUserId(dbAuthorization.getUserId());
    authorizationDto.setGroupId(dbAuthorization.getGroupId());
    authorizationDto.setResourceType(dbAuthorization.getResourceType());
    authorizationDto.setResourceId(dbAuthorization.getResourceId());
    authorizationDto.setRemovalTime(dbAuthorization.getRemovalTime());
    authorizationDto.setRootProcessInstanceId(dbAuthorization.getRootProcessInstanceId());

    return authorizationDto;
  }

  public static void update(AuthorizationDto dto, Authorization dbAuthorization, ProcessEngineConfiguration engineConfiguration) {

    dbAuthorization.setGroupId(dto.getGroupId());
    dbAuthorization.setUserId(dto.getUserId());
    dbAuthorization.setResourceId(dto.getResourceId());

    // update optional fields

    if(dto.getResourceType() != null) {
      dbAuthorization.setResourceType(dto.getResourceType());
    }

    if(dto.getPermissions() != null) {
      dbAuthorization.setPermissions(PermissionConverter.getPermissionsForNames(dto.getPermissions(), dto.getResourceType(), engineConfiguration));
    }

  }

  public static List<AuthorizationDto> fromAuthorizationList(List<Authorization> resultList, ProcessEngineConfiguration engineConfiguration) {
    ArrayList<AuthorizationDto> result = new ArrayList<AuthorizationDto>();

    for (Authorization authorization : resultList) {
      result.add(fromAuthorization(authorization, engineConfiguration));
    }

    return result;
  }

  //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public int getType() {
    return type;
  }
  public void setType(int type) {
    this.type = type;
  }
  public String[] getPermissions() {
    return permissions;
  }
  public void setPermissions(String[] permissions) {
    this.permissions = permissions;
  }
  public String getUserId() {
    return userId;
  }
  public void setUserId(String userId) {
    this.userId = userId;
  }
  public String getGroupId() {
    return groupId;
  }
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }
  public Integer getResourceType() {
    return resourceType;
  }
  public void setResourceType(Integer resourceType) {
    this.resourceType = resourceType;
  }
  public String getResourceId() {
    return resourceId;
  }
  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }
  public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
  }
  public void setRootProcessInstanceId(String rootProcessInstanceId) {
    this.rootProcessInstanceId = rootProcessInstanceId;
  }
  public Date getRemovalTime() {
    return removalTime;
  }
  public void setRemovalTime(Date removalTime) {
    this.removalTime = removalTime;
  }

  private static Permission[] getPermissions(Authorization dbAuthorization, ProcessEngineConfiguration engineConfiguration) {
    int givenResourceType = dbAuthorization.getResourceType();
    Permission[] permissionsByResourceType = ((ProcessEngineConfigurationImpl) engineConfiguration).getPermissionProvider().getPermissionsForResource(givenResourceType);
    return dbAuthorization.getPermissions(permissionsByResourceType);
  }

}
