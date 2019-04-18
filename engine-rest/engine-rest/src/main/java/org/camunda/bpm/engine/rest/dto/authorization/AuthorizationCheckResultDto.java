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

import org.camunda.bpm.engine.rest.util.ResourceUtil;

/**
 * @author Daniel Meyer
 *
 */
public class AuthorizationCheckResultDto {
  
  protected String permissionName;
  protected String resourceName;
  protected String resourceId;
  
  protected Boolean isAuthorized;
  
  public AuthorizationCheckResultDto() {
  }
  
  public AuthorizationCheckResultDto(boolean userAuthorized, String permissionName, ResourceUtil resource, String resourceId) {
    isAuthorized = userAuthorized;
    this.permissionName = permissionName;
    resourceName = resource.resourceName();
    this.resourceId = resourceId;
  }
  
  public String getPermissionName() {
    return permissionName;
  }
  public void setPermissionName(String permissionName) {
    this.permissionName = permissionName;
  }
      
  public Boolean isAuthorized() {
    return isAuthorized;
  }

  public void setAuthorized(Boolean isAuthorized) {
    this.isAuthorized = isAuthorized;
  }

  public String getResourceName() {
    return resourceName;
  }
  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }
  
  public String getResourceId() {
    return resourceId;
  }
  
  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }
}
