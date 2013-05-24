package org.camunda.bpm.engine.rest.dto.runtime;

import org.camunda.bpm.engine.runtime.Execution;

public class ExecutionDto {

  private String id;
  private String processInstanceId;
  private boolean ended;
  
  public static ExecutionDto fromExecution(Execution execution) {
    ExecutionDto dto = new ExecutionDto();
    dto.id = execution.getId();
    dto.processInstanceId = execution.getProcessInstanceId();
    dto.ended = execution.isEnded();
    return dto;
  }

  public String getId() {
    return id;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public boolean isEnded() {
    return ended;
  }
}
