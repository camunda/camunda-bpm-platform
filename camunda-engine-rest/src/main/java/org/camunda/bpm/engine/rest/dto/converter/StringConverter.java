package org.camunda.bpm.engine.rest.dto.converter;

public class StringConverter implements StringToTypeConverter<String> {

  @Override
  public String convertToType(String value) {
    return value;
  }

}
