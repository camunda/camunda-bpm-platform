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
package org.camunda.bpm.engine.impl.identity;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.interceptor.Session;

/**
 * <p>SPI Interface for identity service implementations which offer
 * read / write access to the user database.</p>
 *
 * @author Daniel Meyer
 *
 */
public interface WritableIdentityProvider extends Session {

  // users /////////////////////////////////////////////////

  /**
   * <p>Returns a new (transient) {@link User} object. The Object is not
   * yet persistent and must be saved using the {@link #saveUser(User)}
   * method.</p>
   *
   * <p>NOTE: the implementation does not validate the uniqueness of the userId
   * parameter at this time.</p>
   *
   * @param userId
   * @return an non-persistent user object.
   */
  public User createNewUser(String userId);

  /**
   * Allows saving or updates a {@link User} object
   *
   * @param user a User object.
   * @return the User object.
   * @throws IdentityProviderException in case an internal error occurs
   */
  public User saveUser(User user);

  /**
   * Allows deleting a persistent {@link User} object.
   *
   * @param UserId the id of the User object to delete.
   * @throws IdentityProviderException in case an internal error occurs
   */
  public void deleteUser(String userId);

  /**
   * Allows unlocking a {@link User} object.
   * @param userId the id of the User object to delete.
   * @throws AuthorizationException if the user is not CAMUNDA_ADMIN
   */
  public void unlockUser(String userId);

  // groups /////////////////////////////////////////////////

  /**
   * <p>Returns a new (transient) {@link Group} object. The Object is not
   * yet persistent and must be saved using the {@link #saveGroup(Group)}
   * method.</p>
   *
   * <p>NOTE: the implementation does not validate the uniqueness of the groupId
   * parameter at this time.</p>
   *
   * @param groupId
   * @return an non-persistent group object.
   */
  public Group createNewGroup(String groupId);

  /**
   * Allows saving a {@link Group} object which is not yet persistent.
   *
   * @param group a group object.
   * @return the persistent group object.
   * @throws IdentityProviderException in case an internal error occurs
   */
  public Group saveGroup(Group group);

  /**
   * Allows deleting a persistent {@link Group} object.
   *
   * @param groupId the id of the group object to delete.   *
   * @throws IdentityProviderException in case an internal error occurs
   */
  public void deleteGroup(String groupId);

  /**
   * <p>
   * Returns a new (transient) {@link Tenant} object. The Object is not yet
   * persistent and must be saved using the {@link #saveTenant(Tenant)} method.
   * </p>
   *
   * <p>
   * NOTE: the implementation does not validate the uniqueness of the tenantId
   * parameter at this time.
   * </p>
   *
   * @param tenantId
   *          the id of the new tenant
   * @return an non-persistent tenant object.
   */
  public Tenant createNewTenant(String tenantId);

  /**
   * Allows saving a {@link Tenant} object which is not yet persistent.
   *
   * @param tenant
   *          the tenant object to save.
   * @return the persistent tenant object.
   * @throws IdentityProviderException
   *           in case an internal error occurs
   */
  public Tenant saveTenant(Tenant tenant);

  /**
   * Allows deleting a persistent {@link Tenant} object.
   *
   * @param tenantId
   *          the id of the tenant object to delete. *
   * @throws IdentityProviderException
   *           in case an internal error occurs
   */
  public void deleteTenant(String tenantId);

  // Membership ///////////////////////////////////////////////

  /**
   * Creates a membership relation between a user and a group. If the user is already part of that group,
   * IdentityProviderException is thrown.
   *
   * @param userId the id of the user
   * @param groupId id of the group
   * @throws IdentityProviderException
   */
  public void createMembership(String userId, String groupId);

  /**
   * Deletes a membership relation between a user and a group.
   *
   * @param userId the id of the user
   * @param groupId id of the group
   * @throws IdentityProviderException
   */
  public void deleteMembership(String userId, String groupId);

  /**
   * Creates a membership relation between a tenant and a user.
   *
   * @param tenantId
   *          the id of the tenant
   * @param userId
   *          the id of the user
   */
  public void createTenantUserMembership(String tenantId, String userId);

  /**
   * Creates a membership relation between a tenant and a group.
   *
   * @param tenantId
   *          the id of the tenant
   * @param groupId
   *          the id of the group
   */
  public void createTenantGroupMembership(String tenantId, String groupId);

  /**
   * Deletes a membership relation between a tenant and a user.
   *
   * @param tenantId
   *          the id of the tenant
   * @param userId
   *          the id of the user
   */
  public void deleteTenantUserMembership(String tenantId, String userId);

  /**
   * Deletes a membership relation between a tenant and a group.
   *
   * @param tenantId
   *          the id of the tenant
   * @param groupId
   *          the id of the group
   */
  public void deleteTenantGroupMembership(String tenantId, String groupId);

}
