package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.Map;

public class StartProcessInstanceDto {

  private Map<String, Object> variables;

  public Map<String, Object> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, Object> variables) {
    this.variables = variables;
  }
}
