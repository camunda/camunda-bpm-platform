package com.camunda.fox.tasklist;

import com.camunda.fox.tasklist.api.TaskNavigationLink;

public class UnassignedTasksLink extends TaskNavigationLink {

  public UnassignedTasksLink(String label, long count, boolean active) {
    super(label, count, active);
  }
  
}
