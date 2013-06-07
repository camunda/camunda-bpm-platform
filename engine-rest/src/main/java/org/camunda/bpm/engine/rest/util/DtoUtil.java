package org.camunda.bpm.engine.rest.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.engine.rest.dto.runtime.VariableValueDto;

public class DtoUtil {

  /**
   * Returns null, if variables is null. Else transforms variables into a map
   * @param variables
   * @return
   */
  public static Map<String, Object> toMap(Map<String, VariableValueDto> variables) {
    if (variables == null) {
      return null;
    }
    
    Map<String, Object> variablesMap = new HashMap<String, Object>();
    for (Entry<String, VariableValueDto> variable : variables.entrySet()) {
      variablesMap.put(variable.getKey(), variable.getValue().getValue());
    }
    return variablesMap;
  }
}
