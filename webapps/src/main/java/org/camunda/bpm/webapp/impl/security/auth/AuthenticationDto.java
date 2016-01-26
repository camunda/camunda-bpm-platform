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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Meyer
 * @author nico.rehwaldt
 */
public class AuthenticationDto {

  protected String userId;

  protected List<String> authorizedApps;

  // transformer ///////////////////////

  public static AuthenticationDto fromAuthentication(Authentication authentication) {
    AuthenticationDto dto = new AuthenticationDto();

    dto.setUserId(authentication.getIdentityId());

    if (authentication instanceof UserAuthentication) {
      dto.setAuthorizedApps(new ArrayList<String>(((UserAuthentication) authentication).getAuthorizedApps()));
    }

    return dto;
  }

  // getter / setters /////////////////

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setAuthorizedApps(List<String> authorizedApps) {
    this.authorizedApps = authorizedApps;
  }

  public List<String> getAuthorizedApps() {
    return authorizedApps;
  }
}
