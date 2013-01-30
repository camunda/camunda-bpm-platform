package org.camunda.bpm.engine.rest.dto.converter;

public class BooleanConverter implements StringToTypeConverter<Boolean> {

  @Override
  public Boolean convertQueryParameterToType(String value) {
    return Boolean.valueOf(value);
  }

  @Override
  public Boolean convertFromJsonToType(String value) {
    return Boolean.valueOf(value);
  }

}
