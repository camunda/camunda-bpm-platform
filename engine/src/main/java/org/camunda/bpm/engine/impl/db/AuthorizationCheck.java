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

import org.camunda.bpm.engine.authorization.Permissions;

/**
 * <p>Input for the authorization check alogrithm</p>
 *
 * @author Daniel Meyer
 *
 */
public class AuthorizationCheck implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * If true authorization check is performed. This switch is
   * useful when implementing a query which may perform an authorization check
   * only under certain circumstances.
   */
  protected boolean isAuthorizationCheckEnabled = false;

  /** the id of the user to check permissions for */
  protected String authUserId;

  /** the ids of the groups to check permissions for */
  protected List<String> authGroupIds = new ArrayList<String>();

  /** the default permissions to use if no matching authorization
   * can be found.*/
  protected int authDefaultPerm = Permissions.ALL.getValue();

  protected CompositePermissionCheck permissionChecks = new CompositePermissionCheck();

  // getters / setters /////////////////////////////////////////

  public boolean isAuthorizationCheckEnabled() {
    return isAuthorizationCheckEnabled;
  }

  public void setAuthorizationCheckEnabled(boolean isAuthorizationCheckPerformed) {
    this.isAuthorizationCheckEnabled = isAuthorizationCheckPerformed;
  }

  public String getAuthUserId() {
    return authUserId;
  }

  public void setAuthUserId(String authUserId) {
    this.authUserId = authUserId;
  }

  public List<String> getAuthGroupIds() {
    return authGroupIds;
  }

  public void setAuthGroupIds(List<String> authGroupIds) {
    this.authGroupIds = authGroupIds;
  }

  public int getAuthDefaultPerm() {
    return authDefaultPerm;
  }

  public void setAuthDefaultPerm(int authDefaultPerm) {
    this.authDefaultPerm = authDefaultPerm;
  }

  // authorization check parameters

  public CompositePermissionCheck getPermissionChecks() {
    return permissionChecks;
  }

  public void setAtomicPermissionChecks(List<PermissionCheck> permissionChecks) {
    this.permissionChecks.setAtomicChecks(permissionChecks);
  }

  public void addAtomicPermissionCheck(PermissionCheck permissionCheck) {
    permissionChecks.addAtomicCheck(permissionCheck);
  }

  public void setPermissionChecks(CompositePermissionCheck permissionChecks) {
    this.permissionChecks = permissionChecks;
  }
}
