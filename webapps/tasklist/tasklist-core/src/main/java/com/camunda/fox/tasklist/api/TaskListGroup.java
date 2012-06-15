package com.camunda.fox.tasklist.api;

public class TaskListGroup {

  private String groupId;
  private String groupName;

  public TaskListGroup(String groupId, String groupName) {
    super();
    this.groupId = groupId;
    this.groupName = groupName;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

}
