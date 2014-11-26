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
package org.camunda.bpm.engine.authorization;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;

/**
 * <p>An {@link Authorization} assigns a set of {@link Permission Permissions}
 * to an identity to interact with a given {@link Resource}.</p>
 * <p>EXAMPLES:
 * <ul>
 *   <li>User 'jonny' is authorized to start new instances of the 'invoice' process</li>
 *   <li>Group 'marketing' is not authorized to cancel process instances.</li>
 *   <li>Group 'marketing' is not allowed to use the tasklist application.</li>
 *   <li>Nobody is allowed to edit process variables in the cockpit application,
 *   except the distinct user 'admin'.</li>
 * </ul>
 *
 * <h2>Identities</h2>
 * <p>camunda BPM distinguished two types of identities: <em>users</em> and
 * <em>groups</em>. Authorizations can either range over all users
 * (userId = {@link #ANY}), an individual {@link User} or a {@link Group} of users.</p>
 *
 * <h2>Permissions</h2>
 * <p>A {@link Permission} defines the way an identity is allowed to interact
 * with a certain resource. Examples of permissions are {@link Permissions#CREATE CREATE},
 * {@link Permissions#READ READ}, {@link Permissions#UPDATE UPDATE},
 * {@link Permissions#DELETE DELETE}, ... See {@link Permissions} for a set of
 * built-in permissions.</p>
 *
 * <p>A single authorization object may assign multiple permissions to a single user
 * and resource:</p>
 * <pre>
 * authorization.addPermission(Permissions.READ);
 * authorization.addPermission(Permissions.WRITE);
 * authorization.addPermission(Permissions.DELETE);
 * </pre>
 * <p>On top of the built-in permissions, camunda BPM allows using custom
 * permission types.</p>
 *
 * <h2>Resources</h2>
 * <p>Resources are the entities the user interacts with. Examples of resources are
 * {@link Resources#GROUP GROUPS}, {@link Resources#USER USERS},
 * process-definitions, process-instances, tasks ... See {@link Resources} for a set
 * of built-in resource. The camunda BPM framework supports custom resources.</p>
 *
 * <h2>Authorization Type</h2>
 * <p>There are three types of authorizations:
 * <ul>
 *   <li><strong>Global Authorizations</strong> ({@link #AUTH_TYPE_GLOBAL}) range over
 *   all users and groups (userId = {@link #ANY}) and are usually used for fixing the
 *   "base" permission for a resource.</li>
 *   <li><strong>Grant Authorizations</strong> ({@link #AUTH_TYPE_GRANT}) range over
 *   users and groups and grant a set of permissions. Grant authorizations are commonly
 *   used for adding permissions to a user or group that the global authorization revokes.</li>
 *   <li><strong>Revoke Authorizations</strong> ({@link #AUTH_TYPE_REVOKE}) range over
 *   users and groups and revoke a set of permissions. Revoke authorizations are commonly
 *   used for revoking permissions to a user or group the the global authorization grants.</li>
 * </ul>
 * </p>
 *
 * <h2>Authorization Precedence</h2>
 * <p>Authorizations may range over all users, an individual user or a group of users or .
 * They may apply to an individual resource instance or all instances of the same type
 * (resourceId = {@link #ANY}). The precedence is as follows:
 * <ol>
 *  <li>An authorization applying to an individual resource instance preceds over an authorization
 *  applying to all instances of the same resource type.</li>
 *  <li>An authorization for an individual user preceds over an authorization for a group.</li>
 *  <li>A Group authorization preced over a {@link #AUTH_TYPE_GLOBAL GLOBAL} authorization.</li>
 *  <li>A Group {@link #AUTH_TYPE_REVOKE REVOKE} authorization preced over a Group
 *  {@link #AUTH_TYPE_GRANT GRANT} authorization.</li>
 * </ol>
 * </p>
 *
 * @author Daniel Meyer
 * @since 7.0
 *
 */
public interface Authorization {

  /**
   * A Global Authorization ranges over all users and groups (userId = {@link #ANY}) and are
   * usually used for fixing the "base" permission for a resource.
   */
  public static final int AUTH_TYPE_GLOBAL = 0;

  /**
   * A Grant Authorization ranges over a users or a group and grants a set of permissions.
   * Grant authorizations are commonly used for adding permissions to a user or group that
   * the global authorization revokes.
   */
  public static final int AUTH_TYPE_GRANT = 1;

  /**
   * A Revoke Authorization ranges over a user or a group and revokes a set of permissions.
   * Revoke authorizations are commonly used for revoking permissions to a user or group the
   * the global authorization grants.
   */
  public static final int AUTH_TYPE_REVOKE = 2;

  /** The identifier used for relating to all users or all resourceIds.
   *  Cannot be used for groups.*/
  public static final String ANY = "*";

  /** allows granting a permission. Out-of-the-box constants can be found in {@link Permissions}.
   * */
  public void addPermission(Permission permission);

  /** allows removing a permission. Out-of-the-box constants can be found in {@link Permissions}.
   * */
  public void removePermission(Permission permission);

  /**
   * Allows checking whether this authorization grants a specific permission.
   *
   * @param perm the permission to check for
   * @throws IllegalStateException if this {@link Authorization} is of type {@link #AUTH_TYPE_REVOKE}
   */
  public boolean isPermissionGranted(Permission permission);

  /**
   * Allows checking whether this authorization revokes a specific permission.
   *
   * @param perm the permission to check for
   * @throws IllegalStateException if this {@link Authorization} is of type {@link #AUTH_TYPE_GRANT}
   */
  public boolean isPermissionRevoked(Permission permission);

  /**
   * Allows checking whether this authorization grants every single permission.
   *
   * @return true if every single permission is granted otherwise false
   * @throws IllegalStateException if this {@link Authorization} is of type {@link #AUTH_TYPE_REVOKE}
   */
  boolean isEveryPermissionGranted();

  /**
   * Allows checking whether this authorization revokes every single permission.
   *
   * @return true if every single permission is revoked otherwise false
   * @throws IllegalStateException if this {@link Authorization} is of type {@link #AUTH_TYPE_GRANT}
   */
  boolean isEveryPermissionRevoked();

  /**
   * Allows checking whether this authorization grants / revokes a set of permissions.
   * Usually the set of built-in permissions is used: {@link Permissions#values()}
   *
   * The return value of this method depends on the type of the authorization:
   * <ul>
   *  <li>For {@link #AUTH_TYPE_GLOBAL}: all permissions in the parameter list granted by this authorization are returned. </li>
   *  <li>For {@link #AUTH_TYPE_GRANT}: all permissions in the parameter list granted by this authorization are returned. </li>
   *  <li>For {@link #AUTH_TYPE_REVOKE}: all permissions in the parameter list revoked by this authorization are returned. </li>
   * </ul>
   *
   * @param an array of permissions to check for.
   * @return Returns the set of {@link Permission Permissions} provided by this {@link Authorization}.
   *  */
  public Permission[] getPermissions(Permission[] permissions);

  /**
   * Sets the permissions to the provided value. Replaces all permissions.
   *
   * The effect of this method depends on the type of this authorization:
   * <ul>
   *  <li>For {@link #AUTH_TYPE_GLOBAL}: all provided permissions are granted.</li>
   *  <li>For {@link #AUTH_TYPE_GRANT}: all provided permissions are granted.</li>
   *  <li>For {@link #AUTH_TYPE_REVOKE}: all provided permissions are revoked.</li>
   * </ul>
   *
   *  @param a set of permissions.
   * */
  public void setPermissions(Permission[] permissions);


  /** @return the ID of the {@link Authorization} object */
  public String getId();

  /**
   * set the id of the resource
   */
  public void setResourceId(String resourceId);

  /**
   * @return the id of the resource
   */
  public String getResourceId();

  /**
   * sets the type of the resource
   */
  public void setResourceType(int resourceTypeId);

  /**
   * sets the type of the resource
   */
  public void setResource(Resource resource);

  /**
   * @return the type of the resource
   */
  public int getResourceType();

  /**
   * set the id of the user this authorization is created for
   */
  public void setUserId(String userId);

  /**
   * @return the id of the user this authorization is created for
   */
  public String getUserId();

  /**
   * set the id of the group this authorization is created for
   */
  public void setGroupId(String groupId);

  /**
   * @return the id of the group this authorization is created for
   */
  public String getGroupId();

  /**
   * The type og the authorization. Legal values:
   * <ul>
   * <li>{@link #AUTH_TYPE_GLOBAL}</li>
   * <li>{@link #AUTH_TYPE_GRANT}</li>
   * <li>{@link #AUTH_TYPE_REVOKE}</li>
   * </ul>
   *
   * @return the type of the authorization.
   *
   */
  public int getAuthorizationType();

}
