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
package org.camunda.bpm.engine.impl.db;

import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;


/**
 * @author Roman Smirnov
 *
 */
public class PermissionCheck {

  /** the permission to check for */
  protected Permission permission;
  protected int perms;

  /** the type of the resource to check permissions for */
  protected Resource resource;
  protected int resourceType;

  /** the id of the resource to check permission for */
  protected String resourceId;

  /** query parameter for resource Id. Is injected as RAW parameter into the query */
  protected String resourceIdQueryParam;

  protected Long authorizationNotFoundReturnValue = null;

  public PermissionCheck() {
  }

  public Permission getPermission() {
    return permission;
  }

  public void setPermission(Permission permission) {
    this.permission = permission;
    if (permission != null) {
      perms = permission.getValue();
    }
  }

  public int getPerms() {
    return perms;
  }

  public Resource getResource() {
    return resource;
  }

  public void setResource(Resource resource) {
    this.resource = resource;

    if (resource != null) {
      resourceType = resource.resourceType();
    }
  }

  public int getResourceType() {
    return resourceType;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getResourceIdQueryParam() {
    return resourceIdQueryParam;
  }

  public void setResourceIdQueryParam(String resourceIdQueryParam) {
    this.resourceIdQueryParam = resourceIdQueryParam;
  }

  public Long getAuthorizationNotFoundReturnValue() {
    return authorizationNotFoundReturnValue;
  }

  public void setAuthorizationNotFoundReturnValue(Long authorizationNotFoundReturnValue) {
    this.authorizationNotFoundReturnValue = authorizationNotFoundReturnValue;
  }
}
