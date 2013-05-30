package org.camunda.bpm.engine.rest.dto.message;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.rest.dto.runtime.VariableValueDto;
import org.camunda.bpm.engine.rest.util.DtoUtil;

public class MessageDto {

  private String messageName;
  
  private List<VariableValueDto> variables;

  public String getMessageName() {
    return messageName;
  }

  public void setMessageName(String messageName) {
    this.messageName = messageName;
  }

  public List<VariableValueDto> getVariables() {
    return variables;
  }

  public void setVariables(List<VariableValueDto> variables) {
    this.variables = variables;
  }
  
  public Map<String, Object> variablesToMap() {
    return DtoUtil.toMap(variables);
  }
}
