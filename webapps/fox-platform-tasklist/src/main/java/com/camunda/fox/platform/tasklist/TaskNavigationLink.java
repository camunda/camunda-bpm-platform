package com.camunda.fox.platform.tasklist;

public abstract class TaskNavigationLink {

  protected String label;
  protected boolean active;

  public TaskNavigationLink(String label, boolean active) {
    super();
    this.label = label;
    this.active = active;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

}
