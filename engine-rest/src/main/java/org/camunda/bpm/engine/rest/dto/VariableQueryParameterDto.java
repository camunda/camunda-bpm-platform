package org.camunda.bpm.engine.rest.dto;

public class VariableQueryParameterDto {

  public static final String EQUALS_OPERATOR_NAME = "eq";
  public static final String NOT_EQUALS_OPERATOR_NAME = "neq";
  public static final String GREATER_THAN_OPERATOR_NAME = "gt";
  public static final String GREATER_THAN_OR_EQUALS_OPERATOR_NAME = "gteq";
  public static final String LESS_THAN_OPERATOR_NAME = "lt";
  public static final String LESS_THAN_OR_EQUALS_OPERATOR_NAME = "lteq";
  public static final String LIKE_OPERATOR_NAME = "like";
  
  private String name;
  private String operator;
  private Object value;
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getOperator() {
    return operator;
  }
  public void setOperator(String operator) {
    this.operator = operator;
  }
  public Object getValue() {
    return value;
  }
  public void setValue(Object value) {
    this.value = value;
  }
}
