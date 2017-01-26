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
package org.camunda.bpm.engine.rest.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.rest.IdentityRestService;
import org.camunda.bpm.engine.rest.dto.identity.BasicUserCredentialsDto;
import org.camunda.bpm.engine.rest.dto.task.GroupDto;
import org.camunda.bpm.engine.rest.dto.task.GroupInfoDto;
import org.camunda.bpm.engine.rest.dto.task.UserDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IdentityRestServiceImpl extends AbstractRestProcessEngineAware implements IdentityRestService {

  public IdentityRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public GroupInfoDto getGroupInfo(String userId) {
    if (userId == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "No user id was supplied");
    }

    IdentityService identityService = getProcessEngine().getIdentityService();

    GroupQuery query = identityService.createGroupQuery();
    List<Group> userGroups = query.groupMember(userId).orderByGroupName().asc().list();

    Set<UserDto> allGroupUsers = new HashSet<UserDto>();
    List<GroupDto> allGroups = new ArrayList<GroupDto>();

    for (Group group : userGroups) {
      List<User> groupUsers = identityService.createUserQuery().memberOfGroup(group.getId()).list();
      for (User user: groupUsers) {
        if (!user.getId().equals(userId)) {
          allGroupUsers.add(new UserDto(user.getId(), user.getFirstName(), user.getLastName()));
        }
      }
      allGroups.add(new GroupDto(group.getId(), group.getName()));
    }

    return new GroupInfoDto(allGroups, allGroupUsers);
  }

  @Override
  public AuthenticationResult verifyUser(BasicUserCredentialsDto credentialsDto) {
    if (credentialsDto.getUsername() == null || credentialsDto.getPassword() == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Username and password are required");
    }
    IdentityService identityService = getProcessEngine().getIdentityService();
    boolean valid = identityService.checkPassword(credentialsDto.getUsername(),credentialsDto.getPassword());
    if (valid) {
      return AuthenticationResult.successful(credentialsDto.getUsername());
    } else {
      return AuthenticationResult.unsuccessful(credentialsDto.getUsername());
    }
  }

}
