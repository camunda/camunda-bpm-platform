package com.camunda.fox.tasklist;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.FormService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.application.ProcessApplicationInfo;

import com.camunda.fox.tasklist.api.TaskListIdentity;
import com.camunda.fox.tasklist.api.TaskNavigationLink;
import com.camunda.fox.tasklist.api.TasklistIdentityService;
import com.camunda.fox.tasklist.api.TasklistUser;
import com.camunda.fox.tasklist.event.TaskNavigationLinkSelectedEvent;

@ViewScoped
@Named
public class TaskList implements Serializable {

  private static final long serialVersionUID = 1L;
  private final static Logger log = Logger.getLogger(TaskList.class.getCanonicalName());
  private static final String TASK_LIST_OUTCOME = "taskList.jsf";
  private static final String PARAM_NAME_TASKFORM_URL_SUFFIX = "fox.taskForm.url.suffix";

  @Inject
  private TaskService taskService;

  @Inject
  private RepositoryService repositoryService;
  
  @Inject
  private ManagementService managementService;

  @Inject
  private FormService formService;

  @Inject
  private ProcessEngine processEngine;

  @Inject
  private TaskListIdentity currentIdentity;

  @Inject
  private TasklistIdentityService identityService;

  private List<Task> tasks;

  private List<Task> myTasks;
  private List<Task> unassignedTasks;
  private Map<String, List<Task>> groupTasksMap;
  private Map<String, List<Task>> colleaguesTasksMap;

  private boolean personalTasks = true;
  
  private Map<Task, String> delegateToColleague = new HashMap<Task, String>();

  @PostConstruct
  protected void init() {
    log.finest("initializing " + this.getClass().getSimpleName() + " (" + this + ")");
    tasks = getMyTasks();
  }

  public List<TasklistUser> getColleages() {
    return identityService.getColleaguesByUserId(currentIdentity.getCurrentUser().getUsername());
  }

  public List<Task> getTasks() {
    return tasks;
  }

  
  public boolean isPersonalTasks() {
    return personalTasks;
  }
  
  public List<Task> getMyTasks() {
    if (myTasks == null) {
      myTasks = getList(taskService.createTaskQuery().taskAssignee(currentIdentity.getCurrentUser().getUsername()));
    }
    return myTasks;
  }

  public ProcessDefinition getProcessDefinition(String processDefinitionId) {
    // TODO: For performance improvements we could introduce our own DTO which
    // queries the process definition together with the tasks immediately
    // see
    // https://app.camunda.com/confluence/display/foxUserGuide/Performance+Tuning+with+custom+Queries
    return repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
  }

  private List<Task> getList(TaskQuery taskQuery) {
    return taskQuery.orderByTaskCreateTime().desc().list();
  }

  public List<Task> getUnassignedTasks() {
    if (unassignedTasks == null) {
      unassignedTasks = getList(taskService.createTaskQuery().taskCandidateUser(currentIdentity.getCurrentUser().getUsername()));
    }
    return unassignedTasks;
  }

  public List<Task> getGroupTasks(String groupId) {
    if (groupTasksMap == null) {
      groupTasksMap = new HashMap<String, List<Task>>();
    }
    if (groupTasksMap.get(groupId) == null) {
      groupTasksMap.put(groupId, getList(taskService.createTaskQuery().taskCandidateGroup(groupId)));
    }
    return groupTasksMap.get(groupId);
  }

  public List<Task> getCoolleaguesTasks(String colleagueId) {
    if (colleaguesTasksMap == null) {
      colleaguesTasksMap = new HashMap<String, List<Task>>();
    }
    if (colleaguesTasksMap.get(colleagueId) == null) {
      colleaguesTasksMap.put(colleagueId, getList(taskService.createTaskQuery().taskAssignee(colleagueId)));
    }
    return colleaguesTasksMap.get(colleagueId);
  }

  public String getTaskFormUrl(Task task) {
    TaskFormData taskFormData = formService.getTaskFormData(task.getId());
    if (taskFormData == null || taskFormData.getFormKey() == null) {
      return null;
    } else {
      
      ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
      
      return getFormUrl(processDefinition, taskFormData.getFormKey(), "taskId=" + task.getId());
    }
  }

  public String getStartFormUrl(ProcessDefinition processDefinition) {
    StartFormData startFormData = null;
    try {
      String key = processDefinition.getKey();
      ProcessDefinition latestProcessDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey(key)
        .latestVersion()
        .singleResult();
      startFormData = formService.getStartFormData(latestProcessDefinition.getId());
    } catch (Exception ex) {
      // TODO: Improve to be able to query start forms without causing an
      // exception which is logged
      return null;
    }
    if (startFormData == null || startFormData.getFormKey() == null) {
      return null;
    } else {
      return getFormUrl(processDefinition, startFormData.getFormKey(), "processDefinitionKey=" + processDefinition.getKey());
    }
  }

  private String getFormUrl(ProcessDefinition processDefinition, String formKey, String urlParameters) {
    try {
            
      // get the name of the process application that made the deployment
      String processApplicationName = managementService.getProcessApplicationForDeployment(processDefinition.getDeploymentId());
      
      if(processApplicationName == null) {
        // no a process application deployment
        return null;
      } else {
        ProcessApplicationService processApplicationService = BpmPlatform.getProcessApplicationService();
        ProcessApplicationInfo processApplicationInfo = processApplicationService.getProcessApplicationInfo(processApplicationName);
      
        String contextPath = processApplicationInfo.getProperties().get(ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH);
        
        if(contextPath == null) {
          // this process application does not publish its context path
          return null;
          
        } else {
          String callbackUrl = getRequestURL();
          return "../.." + contextPath + "/" + getTaskFormKeyWithSuffix(formKey) + "?" + urlParameters + "&callbackUrl=" + callbackUrl;
        }
      }
    } catch (Exception ex) {
      log.log(Level.INFO, "Could not resolve context path for process definition " + processDefinition);
      log.log(Level.FINER, "Could not resolve context path for process definition " + processDefinition, ex);
      return null;
    }

  }

  private String getTaskFormKeyWithSuffix(String formKey) {
    String taskFormUrlSuffix = FacesContext.getCurrentInstance().getExternalContext().getInitParameter(PARAM_NAME_TASKFORM_URL_SUFFIX);
    
    if (taskFormUrlSuffix!=null) {
      if (!formKey.endsWith(taskFormUrlSuffix)) { 
        // if parameter is set, and the formKey does not include the suffix already: add it
        return formKey + taskFormUrlSuffix;
      }
    }
    // else
    return formKey;
  }

  public String delegate(Task task) {
    if (delegateToColleague.get(task)!=null && delegateToColleague.get(task).length() > 0) {
      taskService.setAssignee(task.getId(), delegateToColleague.get(task));
    }
    return TASK_LIST_OUTCOME;
  }

  public boolean isPersonalTask(Task task) {
    String assignee = task.getAssignee();
    if (assignee == null) {
      return false;
    }
    return assignee.equals(currentIdentity.getCurrentUser().getUsername());
  }

  public String claimTask(Task task) {
    taskService.delegateTask(task.getId(), currentIdentity.getCurrentUser().getUsername());
    return TASK_LIST_OUTCOME;
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
      personalTasks = true;
      tasks = getMyTasks();
    } else if (link instanceof UnassignedTasksLink) {
      personalTasks = false;
      tasks = getUnassignedTasks();
    } else if (link instanceof GroupTasksLink) {
      personalTasks = false;
      tasks = getGroupTasks(((GroupTasksLink) link).getGroupId());
    } else if (link instanceof ColleaguesTasksLink) {
      personalTasks = false;
      tasks = getCoolleaguesTasks(((ColleaguesTasksLink) link).getColleagueId());
    }
  }

  public Map<Task, String> getDelegateToColleague() {
    return delegateToColleague;
  }



}
