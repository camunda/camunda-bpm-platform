package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.rest.IdentityRestService;
import org.camunda.bpm.engine.rest.dto.task.GroupDto;
import org.camunda.bpm.engine.rest.dto.task.GroupInfoDto;
import org.camunda.bpm.engine.rest.dto.task.UserDto;

public class IdentityRestServiceImpl extends AbstractRestProcessEngineAware implements IdentityRestService {

  @Override
  public GroupInfoDto getGroupInfo(String userId) {
    if (userId == null) {
      throw new WebApplicationException(Status.BAD_REQUEST.getStatusCode());
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

}
