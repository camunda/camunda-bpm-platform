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
package org.camunda.bpm.engine;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.NativeUserQuery;
import org.camunda.bpm.engine.identity.Picture;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.identity.Account;
import org.camunda.bpm.engine.impl.identity.Authentication;


/**
 * Service to manage {@link User}s and {@link Group}s.
 *
 * @author Tom Baeyens
 * @author Daniel Meyer
 *
 */
public interface IdentityService {

  /**
   * <p>Allows to inquire whether this identity service implementation provides
   * read-only access to the user repository, false otherwise.</p>
   *
   * Read only identity service implementations do not support the following methods:
   * <ul>
   * <li> {@link #newUser(String)} </li>
   * <li> {@link #saveUser(User)} </li>
   * <li> {@link #deleteUser(String)} </li>
   *
   * <li> {@link #newGroup(String)} </li>
   * <li> {@link #saveGroup(Group)} </li>
   * <li> {@link #deleteGroup(String)} </li>
   *
   * <li> {@link #newTenant(String)} </li>
   * <li> {@link #saveTenant(Tenant)} </li>
   * <li> {@link #deleteTenant(String)} </li>
   *
   * <li> {@link #createMembership(String, String)} </li>
   * <li> {@link #deleteMembership(String, String)} </li>
   *
   * <li> {@link #createTenantUserMembership(String, String)} </li>
   * <li> {@link #createTenantGroupMembership(String, String)} </li>
   * <li> {@link #deleteTenantUserMembership(String, String)} </li>
   * <li> {@link #deleteTenantGroupMembership(String, String)} </li>
   * </ul>
   *
   * <p>If these methods are invoked on a read-only identity service implementation,
   * the invocation will throw an {@link UnsupportedOperationException}.</p>
   *
   * @return true if this identity service implementation provides read-only
   *         access to the user repository, false otherwise.
   */
  public boolean isReadOnly();

  /**
   * Creates a new user. The user is transient and must be saved using
   * {@link #saveUser(User)}.
   * @param userId id for the new user, cannot be null.
   * @throws UnsupportedOperationException if identity service implementation is read only. See {@link #isReadOnly()}
   * @throws AuthorizationException if the user has no {@link Permissions#CREATE} permissions on {@link Resources#USER}.
   */
  User newUser(String userId);

  /**
   * Saves the user. If the user already existed, the user is updated.
   * @param user user to save, cannot be null.
   * @throws RuntimeException when a user with the same name already exists.
   * @throws UnsupportedOperationException if identity service implementation is read only. See {@link #isReadOnly()}
   * @throws AuthorizationException if the user has no {@link Permissions#UPDATE} permissions on {@link Resources#USER} (update existing user)
   * or if user has no {@link Permissions#CREATE} permissions on {@link Resources#USER} (save new user).
   */
  void saveUser(User user);

  /**
   * Creates a {@link UserQuery} that allows to programmatically query the users.
   */
  UserQuery createUserQuery();

  /**
   * @param userId id of user to delete, cannot be null. When an id is passed
   * for an unexisting user, this operation is ignored.
   * @throws UnsupportedOperationException if identity service implementation is read only. See {@link #isReadOnly()}
   * @throws AuthorizationException if the user has no {@link Permissions#DELETE} permissions on {@link Resources#USER}.
   */
  void deleteUser(String userId);

  void unlockUser(String userId);

  /**
   * Creates a new group. The group is transient and must be saved using
   * {@link #saveGroup(Group)}.
   * @param groupId id for the new group, cannot be null.
   * @throws UnsupportedOperationException if identity service implementation is read only. See {@link #isReadOnly()}
   * @throws AuthorizationException if the user has no {@link Permissions#CREATE} permissions on {@link Resources#GROUP}.
   */
  Group newGroup(String groupId);

  /**
   * Creates a {@link NativeUserQuery} that allows to select users with native queries.
   * @return NativeUserQuery
   */
  NativeUserQuery createNativeUserQuery();

  /**
   * Creates a {@link GroupQuery} thats allows to programmatically query the groups.
   */
  GroupQuery createGroupQuery();

  /**
   * Saves the group. If the group already existed, the group is updated.
   * @param group group to save. Cannot be null.
   * @throws RuntimeException when a group with the same name already exists.
   * @throws UnsupportedOperationException if identity service implementation is read only. See {@link #isReadOnly()}
   * @throws AuthorizationException if the user has no {@link Permissions#UPDATE} permissions on {@link Resources#GROUP} (update existing group)
   * or if user has no {@link Permissions#CREATE} permissions on {@link Resources#GROUP} (save new group).
   */
  void saveGroup(Group group);

  /**
   * Deletes the group. When no group exists with the given id, this operation
   * is ignored.
   * @param groupId id of the group that should be deleted, cannot be null.
   * @throws UnsupportedOperationException if identity service implementation is read only. See {@link #isReadOnly()}
   * @throws AuthorizationException if the user has no {@link Permissions#DELETE} permissions on {@link Resources#GROUP}.
   */
  void deleteGroup(String groupId);

  /**
   * @param userId the userId, cannot be null.
   * @param groupId the groupId, cannot be null.
   * @throws RuntimeException when the given user or group doesn't exist or when the user
   * is already member of the group.
   * @throws UnsupportedOperationException if identity service implementation is read only. See {@link #isReadOnly()}
   * @throws AuthorizationException if the user has no {@link Permissions#CREATE} permissions on {@link Resources#GROUP_MEMBERSHIP}.
   */
  void createMembership(String userId, String groupId);

  /**
   * Delete the membership of the user in the group. When the group or user don't exist
   * or when the user is not a member of the group, this operation is ignored.
   * @param userId the user's id, cannot be null.
   * @param groupId the group's id, cannot be null.
   * @throws UnsupportedOperationException if identity service implementation is read only. See {@link #isReadOnly()}
   * @throws AuthorizationException if the user has no {@link Permissions#DELETE} permissions on {@link Resources#GROUP_MEMBERSHIP}.
   */
  void deleteMembership(String userId, String groupId);

  /**
   * Creates a new tenant. The tenant is transient and must be saved using
   * {@link #saveTenant(Tenant)}.
   *
   * @param tenantId
   *          id for the new tenant, cannot be <code>null</code>.
   * @throws UnsupportedOperationException
   *           if identity service implementation is read only. See
   *           {@link #isReadOnly()}
   * @throws AuthorizationException
   *           if the user has no {@link Permissions#CREATE} permissions on
   *           {@link Resources#TENANT}.
   */
  Tenant newTenant(String tenantId);

  /**
   * Creates a {@link TenantQuery} thats allows to programmatically query the
   * tenants.
   */
  TenantQuery createTenantQuery();

  /**
   * Saves the tenant. If the tenant already existed, it is updated.
   *
   * @param tenant
   *          the tenant to save. Cannot be <code>null</code>.
   * @throws RuntimeException
   *           when a tenant with the same name already exists.
   * @throws UnsupportedOperationException
   *           if identity service implementation is read only. See
   *           {@link #isReadOnly()}
   * @throws AuthorizationException
   *           if the user has no {@link Permissions#UPDATE} permissions on
   *           {@link Resources#TENANT} (update existing tenant) or if user has
   *           no {@link Permissions#CREATE} permissions on
   *           {@link Resources#TENANT} (save new tenant).
   */
  void saveTenant(Tenant tenant);

  /**
   * Deletes the tenant. When no tenant exists with the given id, this operation
   * is ignored.
   *
   * @param tenantId
   *          id of the tenant that should be deleted, cannot be
   *          <code>null</code>.
   * @throws UnsupportedOperationException
   *           if identity service implementation is read only. See
   *           {@link #isReadOnly()}
   * @throws AuthorizationException
   *           if the user has no {@link Permissions#DELETE} permissions on
   *           {@link Resources#TENANT}.
   */
  void deleteTenant(String tenantId);

  /**
   * Creates a new membership between the given user and tenant.
   *
   * @param tenantId
   *          the id of the tenant, cannot be null.
   * @param userId
   *          the id of the user, cannot be null.
   * @throws RuntimeException
   *           when the given tenant or user doesn't exist or the user is
   *           already a member of this tenant.
   * @throws UnsupportedOperationException
   *           if identity service implementation is read only. See
   *           {@link #isReadOnly()}
   * @throws AuthorizationException
   *           if the user has no {@link Permissions#CREATE} permissions on
   *           {@link Resources#TENANT_MEMBERSHIP}.
   */
  void createTenantUserMembership(String tenantId, String userId);

  /**
   * Creates a new membership between the given group and tenant.
   *
   * @param tenantId
   *          the id of the tenant, cannot be null.
   * @param groupId
   *          the id of the group, cannot be null.
   * @throws RuntimeException
   *           when the given tenant or group doesn't exist or when the group
   *           is already a member of this tenant.
   * @throws UnsupportedOperationException
   *           if identity service implementation is read only. See
   *           {@link #isReadOnly()}
   * @throws AuthorizationException
   *           if the user has no {@link Permissions#CREATE} permissions on
   *           {@link Resources#TENANT_MEMBERSHIP}.
   */
  void createTenantGroupMembership(String tenantId, String groupId);

  /**
   * Deletes the membership between the given user and tenant. The operation is
   * ignored when the given user, tenant or membership don't exist.
   *
   * @param tenantId
   *          the id of the tenant, cannot be null.
   * @param userId
   *          the id of the user, cannot be null.
   * @throws UnsupportedOperationException
   *           if identity service implementation is read only. See
   *           {@link #isReadOnly()}
   * @throws AuthorizationException
   *           if the user has no {@link Permissions#DELETE} permissions on
   *           {@link Resources#TENANT_MEMBERSHIP}.
   */
  void deleteTenantUserMembership(String tenantId, String userId);

  /**
   * Deletes the membership between the given group and tenant. The operation is
   * ignored when the given group, tenant or membership don't exist.
   *
   * @param tenantId
   *          the id of the tenant, cannot be null.
   * @param groupId
   *          the id of the group, cannot be null.
   * @throws UnsupportedOperationException
   *           if identity service implementation is read only. See
   *           {@link #isReadOnly()}
   * @throws AuthorizationException
   *           if the user has no {@link Permissions#DELETE} permissions on
   *           {@link Resources#TENANT_MEMBERSHIP}.
   */
  void deleteTenantGroupMembership(String tenantId, String groupId);

  /**
   * Checks if the password is valid for the given user. Arguments userId
   * and password are nullsafe.
   */
  boolean checkPassword(String userId, String password);

  /**
   * Passes the authenticated user id for this thread.
   * All service method (from any service) invocations done by the same
   * thread will have access to this authenticatedUserId. Should be followed by
   * a call to {@link #clearAuthentication()} once the interaction is terminated.
   *
   * @param authenticatedUserId the id of the current user.
   */
  void setAuthenticatedUserId(String authenticatedUserId);

  /**
   * Passes the authenticated user id and groupIds for this thread.
   * All service method (from any service) invocations done by the same
   * thread will have access to this authentication. Should be followed by
   * a call to {@link #clearAuthentication()} once the interaction is terminated.
   *
   *  @param authenticatedUserId the id of the current user.
   *  @param groups the groups of the current user.
   */
  void setAuthentication(String userId, List<String> groups);

  /**
   * Passes the authenticated user id, group ids and tenant ids for this thread.
   * All service method (from any service) invocations done by the same
   * thread will have access to this authentication. Should be followed by
   * a call to {@link #clearAuthentication()} once the interaction is terminated.
   *
   *  @param userId the id of the current user.
   *  @param groups the groups of the current user.
   *  @param tenantIds the tenants of the current user.
   */
  void setAuthentication(String userId, List<String> groups, List<String> tenantIds);

  /**
   * @param currentAuthentication
   */
  public void setAuthentication(Authentication currentAuthentication);

  /**
   * @return the current authentication for this process engine.
   */
  Authentication getCurrentAuthentication();

  /** Allows clearing the current authentication. Does not throw exception if
   * no authentication exists.
   * */
  void clearAuthentication();

  /** Sets the picture for a given user.
   * @throws ProcessEngineException if the user doesn't exist.
   * @param picture can be null to delete the picture. */
  void setUserPicture(String userId, Picture picture);

  /** Retrieves the picture for a given user.
   * @throws ProcessEngineException if the user doesn't exist.
   * @returns null if the user doesn't have a picture. */
  Picture getUserPicture(String userId);

  /** Deletes the picture for a given user. If the user does not have a picture or if the user doesn't exists the call is ignored.
   * @throws ProcessEngineException if the user doesn't exist. */
  void deleteUserPicture(String userId);

  /** Generic extensibility key-value pairs associated with a user */
  void setUserInfo(String userId, String key, String value);

  /** Generic extensibility key-value pairs associated with a user */
  String getUserInfo(String userId, String key);

  /** Generic extensibility keys associated with a user */
  List<String> getUserInfoKeys(String userId);

  /** Delete an entry of the generic extensibility key-value pairs associated with a user */
  void deleteUserInfo(String userId, String key);

  /** Store account information for a remote system */
  @Deprecated
  void setUserAccount(String userId, String userPassword, String accountName, String accountUsername, String accountPassword, Map<String, String> accountDetails);

  /** Get account names associated with the given user */
  @Deprecated
  List<String> getUserAccountNames(String userId);

  /** Get account information associated with a user */
  @Deprecated
  Account getUserAccount(String userId, String userPassword, String accountName);

  /** Delete an entry of the generic extensibility key-value pairs associated with a user */
  @Deprecated
  void deleteUserAccount(String userId, String accountName);

}
