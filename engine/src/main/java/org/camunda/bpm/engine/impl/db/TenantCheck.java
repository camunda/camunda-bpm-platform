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

package org.camunda.bpm.engine.impl.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TenantCheck implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * If <code>true</code> then the process engine performs tenant checks to
   * ensure that the query only access data that belongs to one of the
   * authenticated tenant ids.
   */
  protected boolean isTenantCheckEnabled = true;

  /** the ids of the authenticated tenants */
  protected List<String> authTenantIds = new ArrayList<String>();

  public boolean isTenantCheckEnabled() {
    return isTenantCheckEnabled;
  }

  /** is used by myBatis */
  public boolean getIsTenantCheckEnabled() {
    return isTenantCheckEnabled;
  }

  public void setTenantCheckEnabled(boolean isTenantCheckEnabled) {
    this.isTenantCheckEnabled = isTenantCheckEnabled;
  }

  public List<String> getAuthTenantIds() {
    return authTenantIds;
  }

  public void setAuthTenantIds(List<String> tenantIds) {
    this.authTenantIds = tenantIds;
  }

}
