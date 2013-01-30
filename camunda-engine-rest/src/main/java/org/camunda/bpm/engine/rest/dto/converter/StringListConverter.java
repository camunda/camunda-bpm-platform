package org.camunda.bpm.engine.rest.dto.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.activiti.engine.impl.util.json.JSONArray;

public class StringListConverter implements StringToTypeConverter<List<String>> {

  @Override
  public List<String> convertQueryParameterToType(String value) {
    return Arrays.asList(value.split(","));
  }

  @Override
  public List<String> convertFromJsonToType(String value) {
    JSONArray jsonArray = new JSONArray(value);
    
    List<String> list = new ArrayList<String>();
    for (int i = 0; i < jsonArray.length(); i++) {
      list.add(jsonArray.getString(i));
    }
    return list;
  }

}
