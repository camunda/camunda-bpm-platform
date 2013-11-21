/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.sub.task.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.FormData;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.FormDto;
import org.camunda.bpm.engine.rest.dto.task.IdentityLinkDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.UserIdDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.task.TaskResource;
import org.camunda.bpm.engine.rest.util.ApplicationContextPathUtil;
import org.camunda.bpm.engine.rest.util.DtoUtil;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.Task;

public class TaskResourceImpl implements TaskResource {

  private ProcessEngine engine;
  private String taskId;

  public TaskResourceImpl(ProcessEngine engine, String taskId) {
    this.engine = engine;
    this.taskId = taskId;
  }

  @Override
  public void claim(UserIdDto dto) {
    TaskService taskService = engine.getTaskService();

    taskService.claim(taskId, dto.getUserId());
  }

  @Override
  public void unclaim() {
    engine.getTaskService().setAssignee(taskId, null);
  }

  @Override
  public void complete(CompleteTaskDto dto) {
    TaskService taskService = engine.getTaskService();

    try {
      Map<String, Object> variables = DtoUtil.toMap(dto.getVariables());
      taskService.complete(taskId, variables);

    } catch (NumberFormatException e) {
      String errorMessage = String.format("Cannot complete task %s due to number format exception: %s", taskId, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, e, errorMessage);

    } catch (ParseException e) {
      String errorMessage = String.format("Cannot complete task %s due to parse exception: %s", taskId, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, e, errorMessage);

    } catch (IllegalArgumentException e) {
      String errorMessage = String.format("Cannot complete task %s: %s", taskId, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, errorMessage);

    } catch (ProcessEngineException e) {
      String errorMessage = String.format("Cannot complete task %s: %s", taskId, e.getMessage());
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, errorMessage);
    }
  }

  public void submit(CompleteTaskDto dto) {
    FormService formService = engine.getFormService();

    try {
      Map<String, Object> variables = DtoUtil.toMap(dto.getVariables());
      formService.submitTaskForm(taskId, variables);

    } catch (NumberFormatException e) {
      String errorMessage = String.format("Cannot submit task form %s due to number format exception: %s", taskId, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, e, errorMessage);

    } catch (ParseException e) {
      String errorMessage = String.format("Cannot submit task form %s due to parse exception: %s", taskId, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, e, errorMessage);

    } catch (IllegalArgumentException e) {
      String errorMessage = String.format("Cannot submit task form %s: %s", taskId, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, errorMessage);

    } catch (ProcessEngineException e) {
      String errorMessage = String.format("Cannot submit task form %s: %s", taskId, e.getMessage());
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, errorMessage);
    }

  }

  @Override
  public void delegate(UserIdDto delegatedUser) {
    engine.getTaskService().delegateTask(taskId, delegatedUser.getUserId());
  }

  @Override
  public TaskDto getTask() {
    Task task = getTaskById(taskId);
    if (task == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "No matching task with id " + taskId);
    }

    return TaskDto.fromTask(task);
  }

  @Override
  public FormDto getForm() {
    FormService formService = engine.getFormService();
    Task task = getTaskById(taskId);
    FormData formData;
    try {
      formData = formService.getTaskFormData(taskId);
    } catch (ProcessEngineException e) {
      throw new RestException(Status.BAD_REQUEST, e, "Cannot get form for task " + taskId);
    }

    FormDto dto = FormDto.fromFormData(formData);
    if(dto.getKey() == null || dto.getKey().isEmpty()) {
      if(formData.getFormFields() != null && !formData.getFormFields().isEmpty()) {
        dto.setKey("embedded:engine://engine/:engine/task/"+taskId+"/rendered-form");
      }
    }
    String processDefinitionId = task.getProcessDefinitionId();
    if (processDefinitionId != null) {
      dto.setContextPath(ApplicationContextPathUtil.getApplicationPath(engine, task.getProcessDefinitionId()));
    }

    return dto;
  }

  public String getRenderedForm() {
    FormService formService = engine.getFormService();
    Object renderedTaskForm = formService.getRenderedTaskForm(taskId);
    if(renderedTaskForm != null) {
      return renderedTaskForm.toString();
    }
    throw new InvalidRequestException(Status.NOT_FOUND, "No matching rendered form for task with the id " + taskId + " found.");
  }

  @Override
  public void resolve(CompleteTaskDto dto) {
    TaskService taskService = engine.getTaskService();

    try {
      Map<String, Object> variables = DtoUtil.toMap(dto.getVariables());
      taskService.resolveTask(taskId, variables);

    } catch (NumberFormatException e) {
      String errorMessage = String.format("Cannot resolve task %s due to number format exception: %s", taskId, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, e, errorMessage);

    } catch (ParseException e) {
      String errorMessage = String.format("Cannot resolve task %s due to parse exception: %s", taskId, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, e, errorMessage);

    } catch (IllegalArgumentException e) {
      String errorMessage = String.format("Cannot resolve task %s: %s", taskId, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, errorMessage);
    }

  }


  /**
   * Returns the task with the given id
   *
   * @param id
   * @return
   */
  protected Task getTaskById(String id) {
    return engine.getTaskService().createTaskQuery().taskId(id).singleResult();
  }

  public void setAssignee(UserIdDto dto) {
    TaskService taskService = engine.getTaskService();
    taskService.setAssignee(taskId, dto.getUserId());
  }

  @Override
  public List<IdentityLinkDto> getIdentityLinks(String type) {
    TaskService taskService = engine.getTaskService();
    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);

    List<IdentityLinkDto> result = new ArrayList<IdentityLinkDto>();
    for (IdentityLink link : identityLinks) {
      if (type == null || type.equals(link.getType())) {
        result.add(IdentityLinkDto.fromIdentityLink(link));
      }
    }

    return result;
  }

  @Override
  public void addIdentityLink(IdentityLinkDto identityLink) {
    TaskService taskService = engine.getTaskService();

    identityLink.validate();

    if (identityLink.getUserId() != null) {
      taskService.addUserIdentityLink(taskId, identityLink.getUserId(), identityLink.getType());
    } else if (identityLink.getGroupId() != null) {
      taskService.addGroupIdentityLink(taskId, identityLink.getGroupId(), identityLink.getType());
    }

  }

  @Override
  public void deleteIdentityLink(IdentityLinkDto identityLink) {
    TaskService taskService = engine.getTaskService();

    identityLink.validate();

    if (identityLink.getUserId() != null) {
      taskService.deleteUserIdentityLink(taskId, identityLink.getUserId(), identityLink.getType());
    } else if (identityLink.getGroupId() != null) {
      taskService.deleteGroupIdentityLink(taskId, identityLink.getGroupId(), identityLink.getType());
    }

  }

}
