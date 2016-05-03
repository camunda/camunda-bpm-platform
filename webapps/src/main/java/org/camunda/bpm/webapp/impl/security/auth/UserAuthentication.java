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

import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.authorization.Authorization;


/**
 * <p>An authentication for a user</p>
 *
 * @author Daniel Meyer
 * @author nico.rehwaldt
 */
public class UserAuthentication extends Authentication {

  private static final long serialVersionUID = 1L;

  protected List<String> groupIds;

  protected List<String> tenantIds;

  protected Set<String> authorizedApps;

  public UserAuthentication(String userId, String processEngineName) {
    super(userId, processEngineName);
  }

  public List<String> getGroupIds() {
    return groupIds;
  }

  public boolean isAuthorizedForApp(String app) {
    return authorizedApps.contains(Authorization.ANY) || authorizedApps.contains(app);
  }

  public Set<String> getAuthorizedApps() {
    return authorizedApps;
  }

  public List<String> getTenantIds() {
    return tenantIds;
  }

  public void setTenantIds(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  public void setGroupIds(List<String> groupIds) {
    this.groupIds = groupIds;
  }

  public void setAuthorizedApps(Set<String> authorizedApps) {
    this.authorizedApps = authorizedApps;
  }
}
