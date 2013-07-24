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
 * <p>Exception thrown by the process engine in case a user tries to 
 * interact with a resource in an unauthorized way.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class AuthorizationException extends ProcessEngineException {

  private static final long serialVersionUID = 1L;
  
  protected final String resourceType;
  protected final String permissionName;
  protected final String userId;
  protected final String resourceId;
  
  public AuthorizationException(String userId, String permissionName, String resourceType, String resourceId) {
    super(
        "The user with id '"+userId+ 
        "' does not have '"+permissionName+"' permission " +
        "on resource '" + (resourceId != null ? (resourceId+"' of type '") : "" ) + resourceType+"' .");
    this.resourceType = resourceType;
    this.permissionName = permissionName;    
    this.userId = userId;
    this.resourceId = resourceId;
  }

  public String getResourceType() {
    return resourceType;
  }

  public String getViolatedPermissionName() {
    return permissionName;
  }

  public String getUserId() {
    return userId;
  }

  public String getResourceId() {
    return resourceId;
  }

}
