package org.camunda.bpm.engine.rest.dto.runtime;

/**
 * @author: drobisch
 */
public class VariableValueDto {
  String name;
  Object value;
  String type;

  public VariableValueDto(String name, Object value, String type) {
    this.name = name;
    this.value = value;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }

  public String getType() {
    return type;
  }
}
