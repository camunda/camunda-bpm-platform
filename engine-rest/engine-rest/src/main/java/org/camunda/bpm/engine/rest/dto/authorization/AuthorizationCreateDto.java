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

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.impl.util.PermissionConverter;

/**
 * @author Daniel Meyer
 *
 */
public class AuthorizationCreateDto {

  protected Integer type;
  protected String[] permissions;  
  protected String userId;
  protected String groupId;
  protected Integer resourceType;
  protected String resourceId;
  
  // transformers ///////////////////////////////////////
    
  public static void update(AuthorizationCreateDto dto, Authorization dbAuthorization, ProcessEngineConfiguration engineConfiguration) {
    
    dbAuthorization.setGroupId(dto.getGroupId());
    dbAuthorization.setUserId(dto.getUserId());
    dbAuthorization.setResourceType(dto.getResourceType());
    dbAuthorization.setResourceId(dto.getResourceId());
    dbAuthorization.setPermissions(PermissionConverter.getPermissionsForNames(dto.getPermissions(), dto.getResourceType(), engineConfiguration));
    
  }
    
  //////////////////////////////////////////////////////
  
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
}
