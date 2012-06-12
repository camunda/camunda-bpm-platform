package com.camunda.fox.tasklist.identity.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;

import com.camunda.fox.tasklist.identity.TasklistIdentityService;
import com.camunda.fox.tasklist.identity.User;

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
  public List<String> getGroupsByUserId(String userId) {
    List<String> groupIds = new ArrayList<String>();
    List<Group> groups = identityService.createGroupQuery().groupMember(userId).list();
    for (Group group : groups) {
      groupIds.add(group.getId());
    }
    return groupIds;
  }

  @Override
  public List<User> getColleaguesByUserId(String userId) {
    // always return all Activiti demo users as colleagues
    // even if we logged on with some other user
    ArrayList<User> colleagues = new ArrayList<User>();
    if (!userId.equals("kermit")) {
      colleagues.add(new User("kermit", "Kermit", "The Frog"));
    }
    if (!userId.equals("gonzo")) {
      colleagues.add(new User("gonzo", "Gonzo", "The Great"));
    }
    if (!userId.equals("fozzie")) {
      colleagues.add(new User("fozzie", "Fozzie", "Bear"));
    } 
    return colleagues;
  }

}
