package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.dto.TaskDto;
import org.camunda.bpm.engine.rest.dto.TaskQueryDto;
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

}
