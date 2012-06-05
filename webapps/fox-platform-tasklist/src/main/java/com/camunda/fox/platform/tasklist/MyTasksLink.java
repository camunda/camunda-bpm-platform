package com.camunda.fox.platform.tasklist;

public class MyTasksLink extends TaskNavigationLink {

  public MyTasksLink(String label, boolean active) {
    super(label, active);
  }

  @Override
  public String toString() {
    return "MyTasksLink [label=" + label + ", active=" + active + "]";
  }

}
