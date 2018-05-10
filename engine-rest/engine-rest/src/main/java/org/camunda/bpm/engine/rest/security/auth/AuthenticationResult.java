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
package org.camunda.bpm.engine.rest.security.auth;

import java.util.List;

public class AuthenticationResult {

  protected boolean isAuthenticated;

  protected String authenticatedUser;
  protected List<String> groups;
  protected List<String> tenants;

  public AuthenticationResult(String authenticatedUser, boolean isAuthenticated) {
    this.authenticatedUser = authenticatedUser;
    this.isAuthenticated = isAuthenticated;
  }

  public String getAuthenticatedUser() {
    return authenticatedUser;
  }

  public void setAuthenticatedUser(String authenticatedUser) {
    this.authenticatedUser = authenticatedUser;
  }

  public boolean isAuthenticated() {
    return isAuthenticated;
  }

  public void setAuthenticated(boolean isAuthenticated) {
    this.isAuthenticated = isAuthenticated;
  }

  public List<String> getGroups() {
    return groups;
  }

  public void setGroups(List<String> groups) {
    this.groups = groups;
  }

  public List<String> getTenants() {
    return tenants;
  }

  public void setTenants(List<String> tenants) {
    this.tenants = tenants;
  }

  public static AuthenticationResult successful(String userId) {
    return new AuthenticationResult(userId, true);
  }

  public static AuthenticationResult unsuccessful() {
    return new AuthenticationResult(null, false);
  }

  public static AuthenticationResult unsuccessful(String userId) {
    return new AuthenticationResult(userId, false);
  }
}
