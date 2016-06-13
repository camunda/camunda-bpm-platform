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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.hal.Hal;
import org.camunda.bpm.engine.rest.hal.task.HalTaskList;
import org.camunda.bpm.engine.rest.sub.task.TaskReportResource;
import org.camunda.bpm.engine.rest.sub.task.TaskResource;
import org.camunda.bpm.engine.rest.sub.task.impl.TaskReportResourceImpl;
import org.camunda.bpm.engine.rest.sub.task.impl.TaskResourceImpl;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TaskRestServiceImpl extends AbstractRestProcessEngineAware implements TaskRestService {

  public static final List<Variant> VARIANTS = Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE, Hal.APPLICATION_HAL_JSON_TYPE).add().build();

  public TaskRestServiceImpl(String engineName, final ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  public Object getTasks(Request request, UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    Variant variant = request.selectVariant(VARIANTS);
    if (variant != null) {
      if (MediaType.APPLICATION_JSON_TYPE.equals(variant.getMediaType())) {
        return getJsonTasks(uriInfo, firstResult, maxResults);
      }
      else if (Hal.APPLICATION_HAL_JSON_TYPE.equals(variant.getMediaType())) {
        return getHalTasks(uriInfo, firstResult, maxResults);
      }
    }
    throw new InvalidRequestException(Response.Status.NOT_ACCEPTABLE, "No acceptable content-type found");
  }

  public List<TaskDto> getJsonTasks(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    TaskQueryDto queryDto = new TaskQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryTasks(queryDto, firstResult, maxResults);
  }

  public HalTaskList getHalTasks(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    TaskQueryDto queryDto = new TaskQueryDto(getObjectMapper(), uriInfo.getQueryParameters());

    ProcessEngine engine = getProcessEngine();
    TaskQuery query = queryDto.toQuery(engine);

    // get list of tasks
    List<Task> matchingTasks = executeTaskQuery(firstResult, maxResults, query);

    // get total count
    long count = query.count();

    return HalTaskList.generate(matchingTasks, count, engine);
  }

  @Override
  public List<TaskDto> queryTasks(TaskQueryDto queryDto, Integer firstResult,
      Integer maxResults) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
    TaskQuery query = queryDto.toQuery(engine);

    List<Task> matchingTasks = executeTaskQuery(firstResult, maxResults, query);

    List<TaskDto> tasks = new ArrayList<TaskDto>();
    for (Task task : matchingTasks) {
      TaskDto returnTask = TaskDto.fromEntity(task);
      tasks.add(returnTask);
    }

    return tasks;
  }

  protected List<Task> executeTaskQuery(Integer firstResult, Integer maxResults, TaskQuery query) {

    // enable initialization of form key:
    query.initializeFormKeys();

    List<Task> matchingTasks;
    if (firstResult != null || maxResults != null) {
      matchingTasks = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingTasks = query.list();
    }
    return matchingTasks;
  }

  protected List<Task> executePaginatedQuery(TaskQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  @Override
  public CountResultDto getTasksCount(UriInfo uriInfo) {
    TaskQueryDto queryDto = new TaskQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    return queryTasksCount(queryDto);
  }

  @Override
  public CountResultDto queryTasksCount(TaskQueryDto queryDto) {
    ProcessEngine engine = getProcessEngine();
    queryDto.setObjectMapper(getObjectMapper());
    TaskQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

  @Override
  public TaskResource getTask(String id) {
    return new TaskResourceImpl(getProcessEngine(), id, relativeRootResourcePath, getObjectMapper());
  }

  public void createTask(TaskDto taskDto) {
    ProcessEngine engine = getProcessEngine();
    TaskService taskService = engine.getTaskService();

    Task newTask = taskService.newTask(taskDto.getId());
    taskDto.updateTask(newTask);

    try {
      taskService.saveTask(newTask);

    } catch (NotValidException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "Could not save task: " + e.getMessage());
    }

  }

  @Override
  public TaskReportResource getTaskReportResource() {
    return new TaskReportResourceImpl(getProcessEngine());
  }
}
