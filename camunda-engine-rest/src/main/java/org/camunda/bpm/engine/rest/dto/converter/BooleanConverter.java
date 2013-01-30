package org.camunda.bpm.engine.rest.dto.converter;

public class BooleanConverter implements StringToTypeConverter<Boolean> {

  @Override
  public Boolean convertToType(String value) {
    return Boolean.valueOf(value);
  }

}
