package org.camunda.bpm.engine.rest.dto.converter;

public class IntegerConverter implements StringToTypeConverter<Integer> {

  @Override
  public Integer convertToType(String value) {
    return new Integer(value);
  }

}
