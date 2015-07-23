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
package org.camunda.bpm.engine.rest.dto;

import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;

/**
 * <p>Dto for {@link AuthorizationException}</p>
 * 
 * @author Daniel Meyer
 *
 */
public class AuthorizationExceptionDto extends ExceptionDto {
  
  protected String userId;
  protected String resourceName;
  protected String resourceId;
  protected String permissionName;
  protected List<MissingAuthorizationDto> info;
  
  // transformer /////////////////////////////
  
  public static AuthorizationExceptionDto fromException(AuthorizationException e) {
    AuthorizationExceptionDto dto = new AuthorizationExceptionDto();
    
    dto.setMessage(e.getMessage());
    dto.setType(AuthorizationException.class.getSimpleName());
    
    dto.setUserId(e.getUserId());
    dto.setInfo(MissingAuthorizationDto.fromInfo(e.getInfo()));
    dto.setPermissionName(e.getViolatedPermissionName());
    dto.setResourceId(e.getResourceId());
    dto.setResourceName(e.getResourceType());
    
    return dto;
  }
  
  // getter / setters ////////////////////////
  @Deprecated
  public String getResourceName() {
    return resourceName;
  }
  @Deprecated
  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }
  @Deprecated
  public String getResourceId() {
    return resourceId;
  }
  @Deprecated
  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }
  @Deprecated
  public String getPermissionName() {
    return permissionName;
  }
  @Deprecated
  public void setPermissionName(String permissionName) {
    this.permissionName = permissionName;
  }
  public String getUserId() {
    return userId;
  }
  public void setUserId(String userId) {
    this.userId = userId;
  }
  public List<MissingAuthorizationDto> getInfo() {
    return info;
  }
  public void setInfo(List<MissingAuthorizationDto> info) {
    this.info = info;
  }  
}
