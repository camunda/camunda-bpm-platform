package org.camunda.bpm.engine.rest.dto.task;

import java.util.Map;

public class CompleteTaskDto {

  private Map<String, Object> variables;

  public Map<String, Object> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, Object> variables) {
    this.variables = variables;
  }
}
