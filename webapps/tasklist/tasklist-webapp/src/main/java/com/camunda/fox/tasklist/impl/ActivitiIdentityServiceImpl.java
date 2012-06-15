package com.camunda.fox.tasklist.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;

import com.camunda.fox.tasklist.api.TaskListGroup;
import com.camunda.fox.tasklist.api.TasklistIdentityService;
import com.camunda.fox.tasklist.api.TasklistUser;


@Named
@ApplicationScoped
public class ActivitiIdentityServiceImpl implements TasklistIdentityService, Serializable {

  private static final long serialVersionUID = 1L;
  
  @Inject
  private IdentityService identityService;

  @Override
  public void authenticateUser(String userId, String password) {
    // always authenticate
  }
  
  @Override
  public List<TaskListGroup> getGroupsByUserId(String userId) {
    List<TaskListGroup> taskListGroups = new ArrayList<TaskListGroup>();
    List<Group> groups = identityService.createGroupQuery().groupMember(userId).list();
    for (Group group : groups) {
      taskListGroups.add(new TaskListGroup(group.getId(), group.getName()));
    }
    return taskListGroups;
  }

  @Override
  public List<TasklistUser> getColleaguesByUserId(String userId) {
    // always return all Activiti demo users as colleagues
    // even if we logged on with some other user
    ArrayList<TasklistUser> colleagues = new ArrayList<TasklistUser>();
    if (!userId.equals("kermit")) {
      colleagues.add(new TasklistUser("kermit", "Kermit", "The Frog"));
    }
    if (!userId.equals("gonzo")) {
      colleagues.add(new TasklistUser("gonzo", "Gonzo", "The Great"));
    }
    if (!userId.equals("fozzie")) {
      colleagues.add(new TasklistUser("fozzie", "Fozzie", "Bear"));
    } 
    return colleagues;
  }

}
