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
package org.camunda.bpm.webapp.impl.security.auth;

/**
 * @author Daniel Meyer
 *
 */
public class AuthenticationDto {
  
  protected String userId;
  protected boolean isCockpitAuthorized;
  protected boolean isTasklistAuthorized;
  
  // transformer ///////////////////////
  
  public static AuthenticationDto fromAuthentication(UserAuthentication auth) {
    AuthenticationDto dto = new AuthenticationDto();
    
    dto.setUserId(auth.getIdentityId());
    dto.setCockpitAuthorized(auth.isCockpitAuthorized());
    dto.setTasklistAuthorized(auth.isTasklistAuthorized());
    
    return dto;
  }
  
  // getter / setters /////////////////
  
  public String getUserId() {
    return userId;
  }
  public void setUserId(String userId) {
    this.userId = userId;
  }
  public boolean isCockpitAuthorized() {
    return isCockpitAuthorized;
  }
  public void setCockpitAuthorized(boolean isCockpitAuthorized) {
    this.isCockpitAuthorized = isCockpitAuthorized;
  }
  public boolean isTasklistAuthorized() {
    return isTasklistAuthorized;
  }
  public void setTasklistAuthorized(boolean isTasklistAuthorized) {
    this.isTasklistAuthorized = isTasklistAuthorized;
  }
  
}
