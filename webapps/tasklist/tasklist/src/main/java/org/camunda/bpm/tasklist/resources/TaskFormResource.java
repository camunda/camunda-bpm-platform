package org.camunda.bpm.tasklist.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.spi.impl.AbstractProcessEngineAware;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.tasklist.dto.FormDto;

/**
 * @author drobisch
 * @author nico.rehwaldt
 */
@Path("forms")
public class TaskFormResource extends AbstractProcessEngineAware {

  @GET
  @Path("task/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public FormDto getTaskForm(@PathParam("id") String taskId) {

    TaskFormData formData = processEngine.getFormService().getTaskFormData(taskId);
    Task task = processEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();

    String formKey = formData != null ? formData.getFormKey() : null;

    return new FormDto(formKey, getApplicationPath(task.getProcessDefinitionId()));
  }

  @GET
  @Path("process-definition/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public FormDto getStartForm(@PathParam("id") String processDefinitionId) {

    StartFormData formData = processEngine.getFormService().getStartFormData(processDefinitionId);

    String formKey = formData != null ? formData.getFormKey() : null;

    return new FormDto(formKey, getApplicationPath(processDefinitionId));
  }

  private String getApplicationPath(String processDefinitionId) {

    ProcessDefinition processDefinition = processEngine.getRepositoryService().getProcessDefinition(processDefinitionId);

    // get the name of the process application that made the deployment
    String processApplicationName = processEngine.getManagementService().getProcessApplicationForDeployment(processDefinition.getDeploymentId());

    if (processApplicationName == null) {
      // no a process application deployment
      return null;
    } else {
      ProcessApplicationService processApplicationService = BpmPlatform.getProcessApplicationService();
      ProcessApplicationInfo processApplicationInfo = processApplicationService.getProcessApplicationInfo(processApplicationName);

      String contextPath = processApplicationInfo.getProperties().get(ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH);

      return contextPath;
    }
  }

}
