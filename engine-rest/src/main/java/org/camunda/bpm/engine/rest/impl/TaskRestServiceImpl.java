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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.FormData;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.FormDto;
import org.camunda.bpm.engine.rest.dto.task.GroupDto;
import org.camunda.bpm.engine.rest.dto.task.GroupInfoDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.rest.dto.task.UserDto;
import org.camunda.bpm.engine.rest.dto.task.UserIdDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
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
  public List<TaskDto> getTasks(TaskQueryDto queryDto, Integer firstResult, Integer maxResults) {

    TaskService taskService = getProcessEngine().getTaskService();

    TaskQuery query;

    try {
      query = queryDto.toQuery(taskService);
    } catch (InvalidRequestException e) {
      throw new WebApplicationException(Status.BAD_REQUEST.getStatusCode());
    }

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
  public List<TaskDto> queryTasks(TaskQueryDto query, Integer firstResult,
      Integer maxResults) {
    return getTasks(query, firstResult, maxResults);
  }

  @Override
  public void claim(String taskId, UserIdDto dto) {
    TaskService taskService = getProcessEngine().getTaskService();

    taskService.claim(taskId, dto.getUserId());
  }

  @Override
  public void unclaim(@PathParam("id") String taskId) {
    getProcessEngine().getTaskService().setAssignee(taskId, null);
  }

  @Override
  public void complete(String taskId, CompleteTaskDto dto) {
    TaskService taskService = getProcessEngine().getTaskService();

    taskService.complete(taskId, dto.getVariables());
  }

  @Override
  public void delegate(@PathParam("id") String taskId, UserIdDto delegatedUser) {
    getProcessEngine().getTaskService().delegateTask(taskId, delegatedUser.getUserId());
  }

  @Override
  public GroupInfoDto getGroupInfo(String userId) {
    if (userId == null) {
      throw new WebApplicationException(Status.BAD_REQUEST.getStatusCode());
    }
    
    TaskService taskService = getProcessEngine().getTaskService();
    IdentityService identityService = getProcessEngine().getIdentityService();

    Map<String, Long> groupCounts = new HashMap<String, Long>();

    GroupQuery query = identityService.createGroupQuery();
    List<Group> userGroups = query.groupMember(userId).orderByGroupName().asc().list();

    Set<UserDto> allGroupUsers = new HashSet<UserDto>();
    List<GroupDto> allGroups = new ArrayList<GroupDto>();

    for (Group group : userGroups) {
      long groupTaskCount = taskService.createTaskQuery().taskCandidateGroup(group.getId()).count();
      groupCounts.put(group.getId(), groupTaskCount);
      List<User> groupUsers = identityService.createUserQuery().memberOfGroup(group.getId()).list();
      for (User user: groupUsers) {
        if (!user.getId().equals(userId)) {
          allGroupUsers.add(new UserDto(user.getId(), user.getFirstName(), user.getLastName()));
        }
      }
      allGroups.add(new GroupDto(group.getId(), group.getName()));
    }

    return new GroupInfoDto(groupCounts, allGroups, allGroupUsers);
  }

  @Override
  public CountResultDto getTasksCount(TaskQueryDto queryDto) {
    TaskService taskService = getProcessEngine().getTaskService();

    TaskQuery query;
    try {
      query = queryDto.toQuery(taskService);
    } catch (InvalidRequestException e) {
      throw new WebApplicationException(Status.BAD_REQUEST.getStatusCode());
    }

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

  @Override
  public CountResultDto queryTasksCount(TaskQueryDto queryDto) {
    return getTasksCount(queryDto);
  }

  @Override
  public TaskDto getTask(String id) {
    Task task = getTaskById(id);
    if (task == null) {
      throw new WebApplicationException(Status.BAD_REQUEST.getStatusCode());
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
      throw new WebApplicationException(Status.BAD_REQUEST.getStatusCode());
    }
    
    return FormDto.fromFormData(formData);
  }

  @Override
  public void resolve(String taskId, CompleteTaskDto dto) {
    TaskService taskService = getProcessEngine().getTaskService();
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();

    // FIXME: atomicity of operation

    Task task = getTaskById(taskId);
    String executionId = task.getExecutionId();

    runtimeService.setVariables(executionId, dto.getVariables());
    taskService.resolveTask(taskId);
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
