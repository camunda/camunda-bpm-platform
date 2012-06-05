package com.camunda.fox.platform.tasklist;

public class UnassignedTasksLink extends TaskNavigationLink {

  public UnassignedTasksLink(String label, boolean active) {
    super(label, active);
  }

  @Override
  public String toString() {
    return "UnassignedTasksLink [label=" + label + ", active=" + active + "]";
  }
  
}
