package com.camunda.fox.tasklist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.engine.TaskService;

import com.camunda.fox.tasklist.api.TaskListGroup;
import com.camunda.fox.tasklist.api.TaskListIdentity;
import com.camunda.fox.tasklist.api.TaskNavigationLink;
import com.camunda.fox.tasklist.api.TasklistIdentityService;
import com.camunda.fox.tasklist.api.TasklistUser;
import com.camunda.fox.tasklist.event.SignOutEvent;
import com.camunda.fox.tasklist.event.TaskNavigationLinkSelectedEvent;

@Named
@ViewScoped
public class TaskNavigation implements Serializable {

  private static final Logger log = Logger.getLogger(TaskNavigation.class.getCanonicalName());

  private static final long serialVersionUID = 1L;

  @Inject
  private TaskListIdentity currentIdentity;

  @Inject
  private TaskService taskService;

  @Inject
  private TasklistIdentityService tasklistIdentityService;

  @Inject
  private Event<TaskNavigationLinkSelectedEvent> taskNavigationLinkSelectedEvent;

  private MyTasksLink myTasksLink;
  private UnassignedTasksLink unassignedTasksLink;
  private List<GroupTasksLink> groupTasksLinks;
  private List<ColleaguesTasksLink> colleaguesTasksLinks;

  private TaskNavigationLink selected;

  @PostConstruct
  protected void init() {
    log.finest("initializing " + this.getClass().getSimpleName() + " (" + this + ")");
    selected = getMyTasksLink();
    selected.setActive(true);
  }

  public MyTasksLink getMyTasksLink() {
    if (myTasksLink == null) {
      long personalTasksCount = taskService.createTaskQuery().taskAssignee(currentIdentity.getCurrentUser().getUsername()).count();
      myTasksLink = new MyTasksLink("My Tasks (" + personalTasksCount + ")", personalTasksCount, false);
    }
    return myTasksLink;
  }

  public UnassignedTasksLink getUnassignedTasksLink() {
    if (unassignedTasksLink == null) {
      long unassignedTasksCount = taskService.createTaskQuery().taskCandidateUser(currentIdentity.getCurrentUser().getUsername()).count();
      unassignedTasksLink = new UnassignedTasksLink("Unassigned Tasks (" + unassignedTasksCount + ")", unassignedTasksCount, false);
    }
    return unassignedTasksLink;
  }

  public List<GroupTasksLink> getGroupTasksLinks() {
    if (groupTasksLinks == null) {
      groupTasksLinks = new ArrayList<GroupTasksLink>();
      List<TaskListGroup> groups = tasklistIdentityService.getGroupsByUserId(currentIdentity.getCurrentUser().getUsername());
      for (TaskListGroup taskListGroup : groups) {
        long groupTasksCount = taskService.createTaskQuery().taskCandidateGroup(taskListGroup.getGroupId()).count();
        GroupTasksLink gourpLink = new GroupTasksLink(taskListGroup.getGroupName() + " (" + groupTasksCount + ")", groupTasksCount, taskListGroup.getGroupId(), false);
        groupTasksLinks.add(gourpLink);
      }
    }
    return groupTasksLinks;
  }

  public List<ColleaguesTasksLink> getColleaguesTasksLinks() {
    if (colleaguesTasksLinks == null) {
      colleaguesTasksLinks = new ArrayList<ColleaguesTasksLink>();
      List<TasklistUser> colleagues = tasklistIdentityService.getColleaguesByUserId(currentIdentity.getCurrentUser().getUsername());
      for (TasklistUser colleague : colleagues) {
        long colleagueTasksCount = taskService.createTaskQuery().taskAssignee(colleague.getUsername()).count();
        ColleaguesTasksLink colleaguesLink = new ColleaguesTasksLink(colleague.getFirstname() + " " + colleague.getLastname() + " (" + colleagueTasksCount + ")", colleagueTasksCount, colleague.getUsername(), false);
        colleaguesTasksLinks.add(colleaguesLink);
      }
    }
    return colleaguesTasksLinks;
  }

  public void selectViaEvent(@Observes TaskNavigationLinkSelectedEvent taskNavigationLinkSelectedEvent) {
    if (!taskNavigationLinkSelectedEvent.getLink().equals(selected)) {
      if (selected != null) {
        selected.setActive(false);
      }
      selected = taskNavigationLinkSelectedEvent.getLink();
      selected.setActive(true);
    }
  }

  public void reset(@Observes SignOutEvent signOutEvent) {
    myTasksLink = null;
    unassignedTasksLink = null;
    groupTasksLinks = null;
  }

  public void select(TaskNavigationLink link) {
    log.finest("Menu entry " + link + " was selected, firing TaskNavigationLinkSelectedEvent.");
    taskNavigationLinkSelectedEvent.fire(new TaskNavigationLinkSelectedEvent(link));
  }

}
