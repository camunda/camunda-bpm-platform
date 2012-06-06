package com.camunda.fox.platform.tasklist;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.event.Observes;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.task.Task;

import com.camunda.fox.client.impl.ProcessArchiveSupport;
import com.camunda.fox.platform.api.ProcessArchiveService;
import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.tasklist.event.TaskNavigationLinkSelectedEvent;

@ViewScoped
@Named
public class TaskList implements Serializable {

  private final static Logger log = Logger.getLogger(TaskList.class.getSimpleName());
  
  private static final long serialVersionUID = 1L;

  @Inject
  private TaskService taskService;

  @Inject
  private FormService formService;

  @EJB(lookup = ProcessArchiveSupport.PROCESS_ARCHIVE_SERVICE_NAME)
  private ProcessArchiveService processArchiveService;

  @Inject
  private ProcessEngine processEngine;

  @Inject
  Identity identity;

  private List<Task> tasks;

  private List<Task> myTasks;
  private List<Task> unassignedTasks;
  private Map<String, List<Task>> groupTasksMap;
  private Map<String, List<Task>> colleaguesTasksMap;

  @PostConstruct
  protected void init() {
    
    log.info("initializing " + this.getClass().getSimpleName() + " (" + this + ")");
    
    tasks = getMyTasks();
  }
  
  public List<Task> getTasks() {
    return tasks;
  }

  public List<Task> getMyTasks() {
    if (myTasks == null) {
      myTasks = taskService.createTaskQuery().taskAssignee(identity.getCurrentUser().getUsername()).list();
    }
    return myTasks;
  }

  public List<Task> getUnassignedTasks() {
    if (unassignedTasks == null) {
      unassignedTasks = taskService.createTaskQuery().taskCandidateUser(identity.getCurrentUser().getUsername()).list();
    }
    return unassignedTasks;
  }

  public List<Task> getGroupTasks(String groupId) {
    if(groupTasksMap == null) {
      groupTasksMap = new HashMap<String, List<Task>>();
    }
    if(groupTasksMap.get(groupId) == null) {
      groupTasksMap.put(groupId, taskService.createTaskQuery().taskCandidateGroup(groupId).list());
    }
    return groupTasksMap.get(groupId);
  }
  
  public List<Task> getCoolleaguesTasks(String colleagueId) {
    if(colleaguesTasksMap == null) {
      colleaguesTasksMap = new HashMap<String, List<Task>>();
    }
    if(colleaguesTasksMap.get(colleagueId) == null) {
      colleaguesTasksMap.put(colleagueId, taskService.createTaskQuery().taskAssignee(colleagueId).list());
    }
    return colleaguesTasksMap.get(colleagueId);
  }

  public String getTaskFormUrl(Task task) {
    String formKey, taskFormUrl = "";
    TaskFormData taskFormData = formService.getTaskFormData(task.getId());
    if (taskFormData == null || taskFormData.getFormKey() == null) {
      return null;
    }
    formKey = taskFormData.getFormKey();
    ProcessArchive processArchive = processArchiveService.getProcessArchiveByProcessDefinitionId(task.getProcessDefinitionId(), processEngine.getName());
    String contextPath = (String) processArchive.getProperties().get(ProcessArchive.PROP_SERVLET_CONTEXT_PATH);
    String callbackUrl = getRequestURL();
    taskFormUrl = "../.." + contextPath + "/" + formKey + ".jsf?taskId=" + task.getId() + "&callbackUrl=" + callbackUrl;
    return taskFormUrl;
  }

  public boolean isPersonalTask(Task task) {
    return task.getAssignee().equals(identity.getCurrentUser().getUsername());
  }
  
  public void claimTask(Task task) {
    log.info("trying to claim task " + task.getName() + " for user " + identity.getCurrentUser().getUsername());
    
    System.out.println("claim task");
    
    taskService.claim(task.getId(), identity.getCurrentUser().getUsername());
  }
  
  private String getRequestURL() {
    Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
    if (request instanceof HttpServletRequest) {
      return ((HttpServletRequest) request).getRequestURL().toString();
    } else {
      return "";
    }
  }
  
  public void linkSelected(@Observes TaskNavigationLinkSelectedEvent taskNavigationLinkSelectedEvent) {
    TaskNavigationLink link = taskNavigationLinkSelectedEvent.getLink();
    if (link instanceof MyTasksLink) {
      tasks = getMyTasks();
    } else if (link instanceof UnassignedTasksLink) {
      tasks = getUnassignedTasks();
    } else if (link instanceof GroupTasksLink) {
      tasks = getGroupTasks(((GroupTasksLink) link).getGroupId());
    } else if (link instanceof ColleaguesTasksLink) {
      tasks = getCoolleaguesTasks(((ColleaguesTasksLink)link).getColleagueId());
    }
  }

}
