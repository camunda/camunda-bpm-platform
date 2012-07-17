package com.camunda.fox.tasklist;

import com.camunda.fox.tasklist.api.TaskNavigationLink;

public class GroupTasksLink extends TaskNavigationLink {

  protected String groupId;

  public GroupTasksLink(String label, long count, String groupId, boolean active) {
    super(label, count, active);
    this.groupId = groupId;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  @Override
  public String toString() {
    return "GroupTasksLink [groupId=" + groupId + ", label=" + label + ", active=" + active + "]";
  }

}
