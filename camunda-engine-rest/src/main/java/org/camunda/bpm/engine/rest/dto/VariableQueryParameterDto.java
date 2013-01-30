package org.camunda.bpm.engine.rest.dto;

import org.activiti.engine.impl.QueryOperator;

public class VariableQueryParameterDto {

  private String variableKey;
  private QueryOperator operator;
  private Object variableValue;
  
  public String getVariableKey() {
    return variableKey;
  }
  public void setVariableKey(String variableKey) {
    this.variableKey = variableKey;
  }
  public QueryOperator getOperator() {
    return operator;
  }
  public void setOperator(QueryOperator operator) {
    this.operator = operator;
  }
  public Object getVariableValue() {
    return variableValue;
  }
  public void setVariableValue(Object variableValue) {
    this.variableValue = variableValue;
  }
}
