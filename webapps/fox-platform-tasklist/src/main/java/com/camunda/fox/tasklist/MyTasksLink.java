package com.camunda.fox.tasklist;

public class MyTasksLink extends TaskNavigationLink {

  public MyTasksLink(String label, long count, boolean active) {
    super(label, count, active);
  }

  @Override
  public String toString() {
    return "MyTasksLink [label=" + label + ", active=" + active + "]";
  }

}
