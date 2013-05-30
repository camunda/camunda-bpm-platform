package org.camunda.bpm.engine.rest.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.rest.dto.runtime.VariableValueDto;

public class DtoUtil {

  /**
   * Returns null, if variables is null. Else transforms variables into a map
   * @param variables
   * @return
   */
  public static Map<String, Object> toMap(List<VariableValueDto> variables) {
    if (variables == null) {
      return null;
    }
    
    Map<String, Object> variablesMap = new HashMap<String, Object>();
    for (VariableValueDto variable : variables) {
      variablesMap.put(variable.getName(), variable.getValue());
    }
    return variablesMap;
  }
}
