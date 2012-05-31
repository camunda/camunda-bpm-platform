package com.camunda.fox.platform.tasklist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.task.Task;

import com.camunda.fox.client.impl.ProcessArchiveSupport;
import com.camunda.fox.platform.api.ProcessArchiveService;
import com.camunda.fox.platform.spi.ProcessArchive;

@SessionScoped
@Named
public class TaskList implements Serializable {

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
  
  private List<Task> tasks = new ArrayList<Task>();
  
  public void update() {
  }

  public List<Task> getList() {
    return tasks;
  }

  @Deprecated
  public int countMyTasks() {
    return (int) taskService.createTaskQuery().taskAssignee(identity.getCurrentUser().getUsername()).count();
  }

  public List<Task> getMyTasks() {
    return taskService.createTaskQuery().taskAssignee(identity.getCurrentUser().getUsername()).list();
  }

  @Deprecated
  public int countUnassignedTasks() {
    return (int)taskService.createTaskQuery().taskCandidateUser(identity.getCurrentUser().getUsername()).count();
  }
  
  public List<Task> getUnassignedTasks() {
    return taskService.createTaskQuery().taskCandidateUser(identity.getCurrentUser().getUsername()).list();
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

    taskFormUrl = "../../" + contextPath + "/" + formKey + ".jsf?taskId=" + task.getId();

    return taskFormUrl;
  }

  public void setTaskCategory() {
//    if(taskCategory.equalsIgnoreCase("mytasks")) {
//      this.tasks = getMyTasks();
//    } else if (taskCategory.equalsIgnoreCase("unassigned")) {
//      this.tasks = getUnassignedTasks();
//    }
    
    tasks = getMyTasks();
    
  }
}
