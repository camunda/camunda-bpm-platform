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

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.identity.WritableIdentityProvider;
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
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
    return new UserEntity(userId);
  }
  
  public User saveUser(User user) {
    UserEntity userEntity = (UserEntity) user;
    
    // encrypt password
    userEntity.setPassword(encryptPassword(userEntity.getPassword()));
    
    if(userEntity.getRevision() == 0) {
      getDbSqlSession().insert(userEntity);
    } else {
      getDbSqlSession().update(userEntity);
    }

    return userEntity;
  }
  
  public void deleteUser(String userId) {
    UserEntity user = findUserById(userId);
    if(user != null) {
      deleteMembershipsByUserId(userId);
      getDbSqlSession().delete(user);
    }   
  }
  
  // groups ////////////////////////////////////////////////////////

  public GroupEntity createNewGroup(String groupId) {
    return new GroupEntity(groupId);
  }

  public GroupEntity saveGroup(Group group) {
    GroupEntity groupEntity = (GroupEntity) group;
    if(groupEntity.getRevision() == 0) {
      getDbSqlSession().insert(groupEntity);
    } else {
      getDbSqlSession().update(groupEntity);
    }
    return groupEntity;
  }

  public void deleteGroup(String groupId) {
    GroupEntity group = findGroupById(groupId);
    if(group != null) {
      deleteMembershipsByGroupId(groupId);
      getDbSqlSession().delete(group);
    }   
  }
  
  // membership //////////////////////////////////////////////////////
  
  public void createMembership(String userId, String groupId) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    getDbSqlSession().getSqlSession().insert("insertMembership", parameters);
  }

  public void deleteMembership(String userId, String groupId) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    getDbSqlSession().delete("deleteMembership", parameters);
  }
  
  public void deleteMembershipsByUserId(String userId) {
    getDbSqlSession().delete("deleteMembershipsByUserId", userId);
  }
  
  public void deleteMembershipsByGroupId(String groupId) {
    getDbSqlSession().delete("deleteMembershipsByGroupId", groupId);
  }

}
