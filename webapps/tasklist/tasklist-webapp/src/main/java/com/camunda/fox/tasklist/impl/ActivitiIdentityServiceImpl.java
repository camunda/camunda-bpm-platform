package com.camunda.fox.tasklist.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;

import com.camunda.fox.tasklist.api.TaskListGroup;
import com.camunda.fox.tasklist.api.TasklistIdentityService;
import com.camunda.fox.tasklist.api.TasklistUser;

@Named
@ApplicationScoped
public class ActivitiIdentityServiceImpl implements TasklistIdentityService, Serializable {

  private static final long serialVersionUID = 1L;

  private final static Logger log = Logger.getLogger(ActivitiIdentityServiceImpl.class.getCanonicalName());

  @Inject
  private IdentityService identityService;

  @Override
  public void authenticateUser(String userId, String password) {
    // don't check user id and password, default implementation should allow
    // everybody to sign in
  }

  @Override
  public List<TaskListGroup> getGroupsByUserId(String userId) {
    List<TaskListGroup> taskListGroups = new ArrayList<TaskListGroup>();
    if (identityModulePresentAndUserExists(userId)) {
      List<Group> groups = identityService.createGroupQuery().groupMember(userId).list();
      for (Group group : groups) {
        taskListGroups.add(new TaskListGroup(group.getId(), group.getName()));
      }
    } else {
      // return a default list of groups
      taskListGroups.add(new TaskListGroup("management", "Management"));
      taskListGroups.add(new TaskListGroup("sales", "Sales"));
      taskListGroups.add(new TaskListGroup("accounting", "Accounting"));
      taskListGroups.add(new TaskListGroup("back-office", "Back Office"));
    }
    return taskListGroups;
  }

  @Override
  public List<TasklistUser> getColleaguesByUserId(String userId) {
    ArrayList<TasklistUser> colleagues = new ArrayList<TasklistUser>();
    if (identityModulePresentAndUserExists(userId)) {
      List<User> users = identityService.createUserQuery().list();
      for (User user : users) {
        if (!user.getId().equals(userId)) {
          colleagues.add(new TasklistUser(user.getId(), user.getFirstName(), user.getLastName()));
        }
      }
    } else {
      if (!userId.equals("kermit")) {
        colleagues.add(new TasklistUser("kermit", "Kermit", "The Frog"));
      }
      if (!userId.equals("fozzie")) {
        colleagues.add(new TasklistUser("fozzie", "Fozzie", "Bear"));
      }
      if (!userId.equals("gonzo")) {
        colleagues.add(new TasklistUser("gonzo", "Gonzo", "The Great"));
      }
    }
    return colleagues;
  }

  private boolean identityModulePresentAndUserExists(String userId) {
    try {
      if (identityService.createUserQuery().userId(userId).singleResult() != null) {
        return true;
      }
      return false;
    } catch (Exception e) {
      log.fine("Identity service not present");
      return false;
    }
  }
}
