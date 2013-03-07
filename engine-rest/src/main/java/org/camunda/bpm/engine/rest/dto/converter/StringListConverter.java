package org.camunda.bpm.engine.rest.dto.converter;

import java.util.Arrays;
import java.util.List;

public class StringListConverter implements StringToTypeConverter<List<String>> {

  @Override
  public List<String> convertQueryParameterToType(String value) {
    return Arrays.asList(value.split(","));
  }
}
