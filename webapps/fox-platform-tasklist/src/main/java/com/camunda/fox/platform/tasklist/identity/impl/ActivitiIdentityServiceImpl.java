package com.camunda.fox.platform.tasklist.identity.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;

import com.camunda.fox.platform.tasklist.identity.TasklistIdentityService;
import com.camunda.fox.platform.tasklist.identity.User;

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
    User kermit = new User("kermit", "Kermit", "The Frog");
    User gonzo = new User("gonzo", "Gonzo", "The Great");
    User fozzie = new User("fozzie", "Fozzie", "Bear");
    if (userId.equals("kermit")) {
      return Arrays.asList(new User[] { gonzo, fozzie });
    } else if (userId.equals("gonzo")) {
      return Arrays.asList(new User[] { kermit, fozzie });
    } else if (userId.equals("fozzie")) {
      return Arrays.asList(new User[] { gonzo, kermit });
    } else {
      return new ArrayList<User>();
    }
  }

}
