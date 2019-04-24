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
package org.camunda.bpm.engine.rest.dto;

import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;

/**
 * <p>Dto for {@link AuthorizationException}</p>
 *
 * <p>The exception contains a list of Missing authorizations. The List is a
 * disjunction i.e. a user should have any of the authorization for the engine
 * to continue the execution beyond the point where it failed.</p>
 *
 * @author Daniel Meyer
 *
 */
public class AuthorizationExceptionDto extends ExceptionDto {
  
  protected String userId;
  protected String resourceName;
  protected String resourceId;
  protected String permissionName;
  protected List<MissingAuthorizationDto> missingAuthorizations;
  
  // transformer /////////////////////////////
  
  public static AuthorizationExceptionDto fromException(AuthorizationException e) {
    AuthorizationExceptionDto dto = new AuthorizationExceptionDto();
    
    dto.setMessage(e.getMessage());
    dto.setType(AuthorizationException.class.getSimpleName());
    
    dto.setUserId(e.getUserId());
    dto.setMissingAuthorizations(MissingAuthorizationDto.fromInfo(e.getMissingAuthorizations()));
    dto.setPermissionName(e.getViolatedPermissionName());
    dto.setResourceId(e.getResourceId());
    dto.setResourceName(e.getResourceType());
    
    return dto;
  }
  
  // getter / setters ////////////////////////
  /**
   * @return the name of the resource if there
   * is only one {@link MissingAuthorizationDto}, {@code null} otherwise
   *
   * @deprecated Use {@link #getMissingAuthorizations()} to get the name of the resource
   * of the {@link MissingAuthorizationDto}(s). This method will be removed in future version.
   */
  @Deprecated
  public String getResourceName() {
    return resourceName;
  }

  /**
   * @deprecated Use {@link #setMissingAuthorizations(List)}} to set the
   * the {@link MissingAuthorizationDto}(s). This method will be removed in future version.
   */
  @Deprecated
  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  /**
   * @return the id of the resource if there
   * is only one {@link MissingAuthorizationDto}, {@code null} otherwise
   *
   * @deprecated Use {@link #getMissingAuthorizations()} to get the id of the resource
   * of the {@link MissingAuthorizationDto}(s). This method will be removed in future version.
   */
  @Deprecated
  public String getResourceId() {
    return resourceId;
  }

  /**
   * @deprecated Use {@link #setMissingAuthorizations(List)}} to set the
   * the {@link MissingAuthorizationDto}(s). This method will be removed in future version.
   */
  @Deprecated
  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  /**
   * @return the name of the violated permission if there
   * is only one {@link MissingAuthorizationDto}, {@code null} otherwise
   *
   * @deprecated Use {@link #getMissingAuthorizations()} to get the name of the violated permission
   * of the {@link MissingAuthorizationDto}(s). This method will be removed in future version.
   */
  @Deprecated
  public String getPermissionName() {
    return permissionName;
  }

  /**
   * @deprecated Use {@link #setMissingAuthorizations(List)}} to set the
   * the {@link MissingAuthorizationDto}(s). This method will be removed in future version.
   */
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
  /**
   * @return Disjunctive list of {@link MissingAuthorizationDto} from
   * which a user needs to have at least one for the authorization to pass
   */
  public List<MissingAuthorizationDto> getMissingAuthorizations() {
    return missingAuthorizations;
  }
  public void setMissingAuthorizations(List<MissingAuthorizationDto> info) {
    this.missingAuthorizations = info;
  }  
}
