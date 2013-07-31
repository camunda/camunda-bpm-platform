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
package org.camunda.bpm.engine.rest.util;

import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.rest.dto.converter.PermissionConverter;

/**
 * @author Daniel Meyer
 *
 */
public class AuthorizationUtil implements Permission, Resource {

  protected String resourceName;
  protected int resourceType;
  protected String permissionName;
  protected int permissionValue;
  
  public AuthorizationUtil(String resourceName, int resourceType, String permissionName) {
    this.resourceName = resourceName;
    this.resourceType = resourceType;
    this.permissionName = permissionName;
    this.permissionValue = PermissionConverter.getPermissionForName(permissionName).getValue();
  }

  public String resourceName() {
    return resourceName;
  }

  public int resourceType() {
    return resourceType;
  }

  public String getName() {
    return permissionName;
  }

  public int getValue() {
    return permissionValue;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((permissionName == null) ? 0 : permissionName.hashCode());
    result = prime * result + permissionValue;
    result = prime * result + ((resourceName == null) ? 0 : resourceName.hashCode());
    result = prime * result + resourceType;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AuthorizationUtil other = (AuthorizationUtil) obj;
    if (permissionName == null) {
      if (other.permissionName != null)
        return false;
    } else if (!permissionName.equals(other.permissionName))
      return false;
    if (permissionValue != other.permissionValue)
      return false;
    if (resourceName == null) {
      if (other.resourceName != null)
        return false;
    } else if (!resourceName.equals(other.resourceName))
      return false;
    if (resourceType != other.resourceType)
      return false;
    return true;
  }
  
  
}
