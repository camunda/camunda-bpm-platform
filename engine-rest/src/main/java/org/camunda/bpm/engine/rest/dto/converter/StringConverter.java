package org.camunda.bpm.engine.rest.dto.converter;

public class StringConverter implements StringToTypeConverter<String> {

  @Override
  public String convertQueryParameterToType(String value) {
    return value;
  }
}
