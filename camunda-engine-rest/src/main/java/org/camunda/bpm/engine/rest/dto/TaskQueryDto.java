package org.camunda.bpm.engine.rest.dto;

import java.lang.reflect.InvocationTargetException;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.TaskQuery;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;

public class TaskQueryDto extends SortableParameterizedQueryDto {

  private String name;
  
  @CamundaQueryParam("name")
  public void setName(String name) {
    this.name = name;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    // TODO Auto-generated method stub
    return false;
  }
  
  public TaskQuery toQuery(TaskService taskService) {
    TaskQuery query = taskService.createTaskQuery();
    
    if (name != null) {
      query.taskName(name);
    }
    
    return query;
  }

  @Override
  public void setPropertyFromParameterPair(String key, String value) {
    try {
      setValueBasedOnAnnotation(key, value);
    } catch (IllegalArgumentException e) {
      throw new InvalidRequestException("Cannot set parameter.");
    } catch (IllegalAccessException e) {
      throw new RestException("Cannot set parameter.");
    } catch (InvocationTargetException e) {
      throw new InvalidRequestException(e.getTargetException().getMessage());
    }
  }
}
