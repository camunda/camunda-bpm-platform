package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.List;

/**
 * @author: drobisch
 */
public class VariableListDto {
  List<VariableValueDto> variables;

  public VariableListDto(List<VariableValueDto> variables) {
    this.variables = variables;
  }

  public List<VariableValueDto> getVariables() {
    return variables;
  }
}
