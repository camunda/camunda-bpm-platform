package com.camunda.fox.tasklist;

public abstract class TaskNavigationLink {

  protected String label;
  protected boolean active;
  private long count;

  public TaskNavigationLink(String label, long count, boolean active) {
    this.label = label + "(" + count + ")";
    this.count = count;
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

  public long getCount() {
    return count;
  }

  @Override
  public String toString() {
    return this.getClass().getName() + " [label=" + label + ", active=" + active + ", count=" + count + "]";
  }

}
