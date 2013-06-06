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

import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.FormData;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.FormDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.UserIdDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.task.TaskResource;
import org.camunda.bpm.engine.rest.util.DtoUtil;
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

    Map<String, Object> variables = DtoUtil.toMap(dto.getVariables());
    taskService.complete(taskId, variables);
  }

  @Override
  public void delegate(UserIdDto delegatedUser) {
    engine.getTaskService().delegateTask(taskId, delegatedUser.getUserId());
  }

  @Override
  public TaskDto getTask() {
    Task task = getTaskById(taskId);
    if (task == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "No task id supplied");
    }
    
    return TaskDto.fromTask(task);
  }

  @Override
  public FormDto getForm() {
    FormService formService = engine.getFormService();

    FormData formData;
    try {
      formData = formService.getTaskFormData(taskId);
    } catch (ProcessEngineException e) {
      throw new RestException(Status.BAD_REQUEST, e, "Cannot get form for task " + taskId);
    }
    
    return FormDto.fromFormData(formData);
  }

  @Override
  public void resolve(CompleteTaskDto dto) {
    TaskService taskService = engine.getTaskService();
    Map<String, Object> variables = DtoUtil.toMap(dto.getVariables());
    taskService.resolveTask(taskId, variables);
  }
  

  /**
   * Returns the task with the given id
   *
   * @param id
   * @return
   */
  private Task getTaskById(String id) {
    return engine.getTaskService().createTaskQuery().taskId(id).singleResult();
  }
}
