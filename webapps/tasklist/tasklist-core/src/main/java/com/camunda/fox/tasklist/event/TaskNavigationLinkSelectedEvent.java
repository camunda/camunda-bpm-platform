package com.camunda.fox.tasklist.event;

import com.camunda.fox.tasklist.api.TaskNavigationLink;

public class TaskNavigationLinkSelectedEvent {

  private TaskNavigationLink link;

  public TaskNavigationLinkSelectedEvent(TaskNavigationLink link) {
    this.link = link;
  }

  public TaskNavigationLink getLink() {
    return link;
  }

  public void setLink(TaskNavigationLink link) {
    this.link = link;
  }

}
