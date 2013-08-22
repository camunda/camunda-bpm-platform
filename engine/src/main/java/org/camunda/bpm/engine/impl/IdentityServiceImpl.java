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
package org.camunda.bpm.engine.impl;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.Picture;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.cmd.CheckPassword;
import org.camunda.bpm.engine.impl.cmd.CreateGroupCmd;
import org.camunda.bpm.engine.impl.cmd.CreateGroupQueryCmd;
import org.camunda.bpm.engine.impl.cmd.CreateMembershipCmd;
import org.camunda.bpm.engine.impl.cmd.CreateUserCmd;
import org.camunda.bpm.engine.impl.cmd.CreateUserQueryCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteGroupCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteMembershipCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteUserCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteUserInfoCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteUserPictureCmd;
import org.camunda.bpm.engine.impl.cmd.GetUserAccountCmd;
import org.camunda.bpm.engine.impl.cmd.GetUserInfoCmd;
import org.camunda.bpm.engine.impl.cmd.GetUserInfoKeysCmd;
import org.camunda.bpm.engine.impl.cmd.GetUserPictureCmd;
import org.camunda.bpm.engine.impl.cmd.IsIdentityServiceReadOnlyCmd;
import org.camunda.bpm.engine.impl.cmd.SaveGroupCmd;
import org.camunda.bpm.engine.impl.cmd.SaveUserCmd;
import org.camunda.bpm.engine.impl.cmd.SetUserInfoCmd;
import org.camunda.bpm.engine.impl.cmd.SetUserPictureCmd;
import org.camunda.bpm.engine.impl.identity.Account;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import org.camunda.bpm.engine.impl.persistence.entity.IdentityInfoEntity;


/**
 * @author Tom Baeyens
 */
public class IdentityServiceImpl extends ServiceImpl implements IdentityService {

  /** thread local holding the current authentication */
  private ThreadLocal<Authentication> currentAuthentication = new ThreadLocal<Authentication>();

  public boolean isReadOnly() {
    return commandExecutor.execute(new IsIdentityServiceReadOnlyCmd());
  }

  public Group newGroup(String groupId) {
    return commandExecutor.execute(new CreateGroupCmd(groupId));
  }

  public User newUser(String userId) {
    return commandExecutor.execute(new CreateUserCmd(userId));
  }

  public void saveGroup(Group group) {
    commandExecutor.execute(new SaveGroupCmd((GroupEntity) group));
  }

  public void saveUser(User user) {
    commandExecutor.execute(new SaveUserCmd(user));
  }

  public UserQuery createUserQuery() {
    return commandExecutor.execute(new CreateUserQueryCmd());
  }

  public GroupQuery createGroupQuery() {
    return commandExecutor.execute(new CreateGroupQueryCmd());
  }

  public void createMembership(String userId, String groupId) {
    commandExecutor.execute(new CreateMembershipCmd(userId, groupId));
  }

  public void deleteGroup(String groupId) {
    commandExecutor.execute(new DeleteGroupCmd(groupId));
  }

  public void deleteMembership(String userId, String groupId) {
    commandExecutor.execute(new DeleteMembershipCmd(userId, groupId));
  }

  public boolean checkPassword(String userId, String password) {
    return commandExecutor.execute(new CheckPassword(userId, password));
  }

  public void deleteUser(String userId) {
    commandExecutor.execute(new DeleteUserCmd(userId));
  }

  public void setUserPicture(String userId, Picture picture) {
    commandExecutor.execute(new SetUserPictureCmd(userId, picture));
  }

  public Picture getUserPicture(String userId) {
    return commandExecutor.execute(new GetUserPictureCmd(userId));
  }

  public void deleteUserPicture(String userId) {
    commandExecutor.execute(new DeleteUserPictureCmd(userId));
  }

  public void setAuthenticatedUserId(String authenticatedUserId) {
    currentAuthentication.set(new Authentication(authenticatedUserId, null));
  }

  public void setAuthentication(Authentication auth) {
    if(auth == null) {
      clearAuthentication();
    } else {
      currentAuthentication.set(auth);
    }
  }

  public void setAuthentication(String userId, List<String> groups) {
    currentAuthentication.set(new Authentication(userId, groups));
  }

  public void clearAuthentication() {
    currentAuthentication.remove();
  }

  public Authentication getCurrentAuthentication() {
    return currentAuthentication.get();
  }

  public String getUserInfo(String userId, String key) {
    return commandExecutor.execute(new GetUserInfoCmd(userId, key));
  }

  public List<String> getUserInfoKeys(String userId) {
    return commandExecutor.execute(new GetUserInfoKeysCmd(userId, IdentityInfoEntity.TYPE_USERINFO));
  }

  public List<String> getUserAccountNames(String userId) {
    return commandExecutor.execute(new GetUserInfoKeysCmd(userId, IdentityInfoEntity.TYPE_USERACCOUNT));
  }

  public void setUserInfo(String userId, String key, String value) {
    commandExecutor.execute(new SetUserInfoCmd(userId, key, value));
  }

  public void deleteUserInfo(String userId, String key) {
    commandExecutor.execute(new DeleteUserInfoCmd(userId, key));
  }

  public void deleteUserAccount(String userId, String accountName) {
    commandExecutor.execute(new DeleteUserInfoCmd(userId, accountName));
  }

  public Account getUserAccount(String userId, String userPassword, String accountName) {
    return commandExecutor.execute(new GetUserAccountCmd(userId, userPassword, accountName));
  }

  public void setUserAccount(String userId, String userPassword, String accountName, String accountUsername, String accountPassword, Map<String, String> accountDetails) {
    commandExecutor.execute(new SetUserInfoCmd(userId, userPassword, accountName, accountUsername, accountPassword, accountDetails));
  }
}
