package org.camunda.bpm.engine.rest.dto.externaltask;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.AbstractSearchQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

public class GetTopicNamesForExternalTasksDto extends AbstractSearchQueryDto {

  protected Boolean withLockedTasks;
  protected Boolean withUnlockedTasks;
  protected Boolean withRetriesLeft;

  public GetTopicNamesForExternalTasksDto(){};

  public GetTopicNamesForExternalTasksDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters){
    super(objectMapper, queryParameters);
  }

  public Boolean isWithLockedTasks() {
    return withLockedTasks;
  }

  @CamundaQueryParam(value="withLockedTasks", converter = BooleanConverter.class)
  public void setWithLockedTasks(Boolean withLockedTasks) {
    this.withLockedTasks = withLockedTasks;
  }

  public Boolean isWithUnlockedTasks() {
    return withUnlockedTasks;
  }

  @CamundaQueryParam(value="withUnlockedTasks", converter = BooleanConverter.class)
  public void setWithUnlockedTasks(Boolean withUnlockedTasks) {
    this.withUnlockedTasks = withUnlockedTasks;
  }

  public Boolean isWithRetriesLeft() {
    return withRetriesLeft;
  }

  @CamundaQueryParam(value="withRetriesLeft", converter = BooleanConverter.class)
  public void setWithRetriesLeft(Boolean withRetriesLeft) {
    this.withRetriesLeft = withRetriesLeft;
  }

  public List<String> execute(ProcessEngine processEngine){
    if(withLockedTasks == null){
      withLockedTasks = false;
    }
    if(withUnlockedTasks == null){
      withUnlockedTasks = false;
    }
    if(withRetriesLeft == null){
      withRetriesLeft = false;
    }
    return processEngine.getExternalTaskService().getTopicNames(withLockedTasks, withUnlockedTasks, withRetriesLeft);
  }
}
