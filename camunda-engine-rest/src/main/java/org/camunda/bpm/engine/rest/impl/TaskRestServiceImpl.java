package org.camunda.bpm.engine.rest.impl;

import java.util.*;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.task.*;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

public class TaskRestServiceImpl extends AbstractEngineService implements TaskRestService {

  @Override
  public List<TaskDto> getTasks(TaskQueryDto queryDto,
      Integer firstResult, Integer maxResults) {
    TaskService taskService = processEngine.getTaskService();

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
    TaskService taskService = processEngine.getTaskService();

    try {
      taskService.claim(taskId, dto.getUserId());
    } catch (ActivitiException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void unclaim(@PathParam("id") String taskId, UserIdDto dto) {
    processEngine.getTaskService().setAssignee(taskId, null);
  }

  @Override
  public void complete(String taskId, CompleteTaskDto dto) {
    TaskService taskService = processEngine.getTaskService();

    try {
      taskService.complete(taskId, dto.getVariables());
    } catch (ActivitiException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void delegate(@PathParam("id") String taskId, UserIdDto delegatedUser) {
    processEngine.getTaskService().delegateTask(taskId, delegatedUser.getUserId());
  }

  @Override
  public GroupInfoDto getGroupInfo(String userId) {
    TaskService taskService = processEngine.getTaskService();
    IdentityService identityService = processEngine.getIdentityService();

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
    TaskService taskService = processEngine.getTaskService();

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
    TaskService taskService = processEngine.getTaskService();
    
    Task task = taskService.createTaskQuery().taskId(id).singleResult();
    return TaskDto.fromTask(task);
  }


}
