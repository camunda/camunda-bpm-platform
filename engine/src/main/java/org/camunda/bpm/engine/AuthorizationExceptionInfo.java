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
package org.camunda.bpm.engine;

/**
 * @author Filip Hrisafov
 * 
 */
public class AuthorizationExceptionInfo {

  private String permissionName;
  private String resourceType;
  protected String resourceId;

  protected AuthorizationExceptionInfo(Builder builder) {
    this.permissionName = builder.permissionName;
    this.resourceType = builder.resourceType;
    this.resourceId = builder.resourceId;
  }

  public String getViolatedPermissionName() {
    return permissionName;
  }

  public String getResourceType() {
    return resourceType;
  }

  public String getResourceId() {
    return resourceId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String permissionName;
    private String resourceType;
    private String resourceId;

    public Builder permission(String permission) {
      this.permissionName = permission;
      return this;
    }

    public Builder resource(String resource) {
      this.resourceType = resource;
      return this;
    }

    public Builder resourceId(String resourceId) {
      this.resourceId = resourceId;
      return this;
    }

    public AuthorizationExceptionInfo build() {
      return new AuthorizationExceptionInfo(this);
    }
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
        + "[permissionName=" + permissionName
        + ", resourceType=" + resourceType
        + ", resourceId=" + resourceId
        + "]";
  }
}
