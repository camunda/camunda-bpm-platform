package com.camunda.fox.platform.tasklist;

public class GroupTasksLink extends TaskNavigationLink {

  protected String groupId;

  public GroupTasksLink(String label, String groupId, boolean active) {
    super(label, active);
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
