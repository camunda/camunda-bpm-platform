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
package org.camunda.bpm.engine.impl.identity.db;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.identity.WritableIdentityProvider;
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MembershipEntity;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;

/**
 * <p>{@link WritableIdentityProvider} implementation backed by a
 * database. This implementation is used for the built-in user management.</p>
 *
 * @author Daniel Meyer
 *
 */
public class DbIdentityServiceProvider extends DbReadOnlyIdentityServiceProvider implements WritableIdentityProvider {

  // users ////////////////////////////////////////////////////////

  public UserEntity createNewUser(String userId) {
    checkAuthorization(Permissions.CREATE, Resources.USER, null);
    return new UserEntity(userId);
  }

  public User saveUser(User user) {
    UserEntity userEntity = (UserEntity) user;

    // encrypt password
    userEntity.encryptPassword();

    if(userEntity.getRevision() == 0) {
      checkAuthorization(Permissions.CREATE, Resources.USER, null);
      getDbEntityManager().insert(userEntity);
      createDefaultAuthorizations(userEntity);
    } else {
      checkAuthorization(Permissions.UPDATE, Resources.USER, user.getId());
      getDbEntityManager().merge(userEntity);
    }

    return userEntity;
  }

  public void deleteUser(String userId) {
    checkAuthorization(Permissions.DELETE, Resources.USER, userId);
    UserEntity user = findUserById(userId);
    if(user != null) {
      deleteMembershipsByUserId(userId);
      deleteAuthorizations(Resources.USER, userId);
      getDbEntityManager().delete(user);
    }
  }

  // groups ////////////////////////////////////////////////////////

  public GroupEntity createNewGroup(String groupId) {
    checkAuthorization(Permissions.CREATE, Resources.GROUP, null);
    return new GroupEntity(groupId);
  }

  public GroupEntity saveGroup(Group group) {
    GroupEntity groupEntity = (GroupEntity) group;
    if(groupEntity.getRevision() == 0) {
      checkAuthorization(Permissions.CREATE, Resources.GROUP, null);
      getDbEntityManager().insert(groupEntity);
      createDefaultAuthorizations(group);
    } else {
      checkAuthorization(Permissions.UPDATE, Resources.GROUP, group.getId());
      getDbEntityManager().merge(groupEntity);
    }
    return groupEntity;
  }

  public void deleteGroup(String groupId) {
    checkAuthorization(Permissions.DELETE, Resources.GROUP, groupId);
    GroupEntity group = findGroupById(groupId);
    if(group != null) {
      deleteMembershipsByGroupId(groupId);
      deleteAuthorizations(Resources.GROUP, groupId);
      getDbEntityManager().delete(group);
    }
  }

  // membership //////////////////////////////////////////////////////

  public void createMembership(String userId, String groupId) {
    checkAuthorization(Permissions.CREATE, Resources.GROUP_MEMBERSHIP, groupId);
    UserEntity user = findUserById(userId);
    GroupEntity group = findGroupById(groupId);
    MembershipEntity membership = new MembershipEntity();
    membership.setUser(user);
    membership.setGroup(group);
    getDbEntityManager().insert(membership);
    createDefaultMembershipAuthorizations(userId, groupId);
  }

  public void deleteMembership(String userId, String groupId) {
    checkAuthorization(Permissions.DELETE, Resources.GROUP_MEMBERSHIP, groupId);
    deleteAuthorizations(Resources.GROUP_MEMBERSHIP, groupId);

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    getDbEntityManager().delete(MembershipEntity.class, "deleteMembership", parameters);
  }

  protected void deleteMembershipsByUserId(String userId) {
    getDbEntityManager().delete(MembershipEntity.class, "deleteMembershipsByUserId", userId);
  }

  protected void deleteMembershipsByGroupId(String groupId) {
    getDbEntityManager().delete(MembershipEntity.class, "deleteMembershipsByGroupId", groupId);
  }

  // authorizations ////////////////////////////////////////////////////////////

  protected void createDefaultAuthorizations(UserEntity userEntity) {
    if(Context.getProcessEngineConfiguration().isAuthorizationEnabled()) {
      saveDefaultAuthorizations(getResourceAuthorizationProvider().newUser(userEntity));
    }
  }

  protected void createDefaultAuthorizations(Group group) {
    if(isAuthorizationEnabled()) {
      saveDefaultAuthorizations(getResourceAuthorizationProvider().newGroup(group));
    }
  }

  protected void createDefaultMembershipAuthorizations(String userId, String groupId) {
    if(isAuthorizationEnabled()) {
      saveDefaultAuthorizations(getResourceAuthorizationProvider().groupMembershipCreated(groupId, userId));
    }
  }

}
