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
package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.FormData;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.FormDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.rest.dto.task.UserIdDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;

public class TaskRestServiceImpl extends AbstractRestProcessEngineAware implements TaskRestService {

  public TaskRestServiceImpl() {
    super();
  }
  
  public TaskRestServiceImpl(String engineName) {
    super(engineName);
  }

  @Override
  public List<TaskDto> getTasks(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    TaskQueryDto queryDto = new TaskQueryDto(uriInfo.getQueryParameters());
    return queryTasks(queryDto, firstResult, maxResults);
  }

  @Override
  public List<TaskDto> queryTasks(TaskQueryDto queryDto, Integer firstResult,
      Integer maxResults) {
    ProcessEngine engine = getProcessEngine();
    TaskQuery query = queryDto.toQuery(engine);

    List<Task> matchingTasks;
    if (firstResult != null || maxResults != null) {
      matchingTasks = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingTasks = query.list();
    }

    List<TaskDto> tasks = new ArrayList<TaskDto>();
    for (Task task : matchingTasks) {
      TaskDto returnTask = TaskDto.fromTask(task);
      tasks.add(returnTask);
    }

    return tasks;
  }

  private List<Task> executePaginatedQuery(TaskQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }
  
  @Override
  public void claim(String taskId, UserIdDto dto) {
    TaskService taskService = getProcessEngine().getTaskService();

    taskService.claim(taskId, dto.getUserId());
  }

  @Override
  public void unclaim(String taskId) {
    getProcessEngine().getTaskService().setAssignee(taskId, null);
  }

  @Override
  public void complete(String taskId, CompleteTaskDto dto) {
    TaskService taskService = getProcessEngine().getTaskService();

    taskService.complete(taskId, dto.getVariables());
  }

  @Override
  public void delegate(String taskId, UserIdDto delegatedUser) {
    getProcessEngine().getTaskService().delegateTask(taskId, delegatedUser.getUserId());
  }

  @Override
  public CountResultDto getTasksCount(UriInfo uriInfo) {
    TaskQueryDto queryDto = new TaskQueryDto(uriInfo.getQueryParameters());
    return queryTasksCount(queryDto);
  }

  @Override
  public CountResultDto queryTasksCount(TaskQueryDto queryDto) {
    ProcessEngine engine = getProcessEngine();
    TaskQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

  @Override
  public TaskDto getTask(String id) {
    Task task = getTaskById(id);
    if (task == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "No task id supplied");
    }
    
    return TaskDto.fromTask(task);
  }

  @Override
  public FormDto getForm(String id) {
    FormService formService = getProcessEngine().getFormService();

    FormData formData;
    try {
      formData = formService.getTaskFormData(id);
    } catch (ProcessEngineException e) {
      throw new RestException(Status.BAD_REQUEST, e, "Cannot get form for task " + id);
    }
    
    return FormDto.fromFormData(formData);
  }

  @Override
  public void resolve(String taskId, CompleteTaskDto dto) {
    TaskService taskService = getProcessEngine().getTaskService();
    taskService.resolveTask(taskId, dto.getVariables());
  }

  /**
   * Returns the task with the given id
   *
   * @param id
   * @return
   */
  private Task getTaskById(String id) {
    return getProcessEngine().getTaskService().createTaskQuery().taskId(id).singleResult();
  }
}
