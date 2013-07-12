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
package org.camunda.bpm.engine.rest.dto.identity;

import org.camunda.bpm.engine.identity.User;

/**
 * @author Daniel Meyer
 * 
 */
public class UserDto {

  protected UserProfileDto profile;

  protected UserCredentialsDto credentials;
  
  // transformers //////////////////////////////////
  
  public static UserDto fromUser(User user, boolean isIncludeCredentials) {
    UserDto userDto = new UserDto();
    userDto.setProfile(UserProfileDto.fromUser(user));
    if(isIncludeCredentials) {
      userDto.setCredentials(UserCredentialsDto.fromUser(user));
    }
    return userDto;
  }

  // getters / setters /////////////////////////////
  
  public UserProfileDto getProfile() {
    return profile;
  }

  public void setProfile(UserProfileDto profile) {
    this.profile = profile;
  }

  public UserCredentialsDto getCredentials() {
    return credentials;
  }

  public void setCredentials(UserCredentialsDto credentials) {
    this.credentials = credentials;
  }

}
