package org.camunda.bpm.engine.rest.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Common methods for tests to build request bodys 
 * @author Thorben Lindhauer
 *
 */
public class RequestBodyUtil {

  /**
   * Creates a map that is translated by rest-assured to the variable format that is commonly expected by the REST API
   * @param variableName
   * @param variableValue
   * @return
   */
  public static Map<String, Object> createVariableJsonObject(String variableName, Object variableValue) {
    Map<String, Object> variable = new HashMap<String, Object>();
    
    if (variableName != null) {
      variable.put("name", variableName);
    }
    if (variableValue != null) {
      variable.put("value", variableValue);
    }
    
    return variable;
  }
}
