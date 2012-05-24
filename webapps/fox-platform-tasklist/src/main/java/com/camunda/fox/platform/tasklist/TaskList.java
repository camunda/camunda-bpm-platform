package com.camunda.fox.platform.tasklist;

import java.io.Serializable;
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

  public void update() {
    // do nothing here, since a refreh trigger a reload of the list anyway
  }

  public List<Task> getList() {
    return taskService.createTaskQuery().list();
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

    taskFormUrl = "../" + contextPath + "/" + formKey + ".jsf?taskId=" + task.getId();

    return taskFormUrl;
  }
}
