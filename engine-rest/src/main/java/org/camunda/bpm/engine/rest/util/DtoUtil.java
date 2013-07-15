package org.camunda.bpm.engine.rest.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.engine.impl.variable.BooleanType;
import org.camunda.bpm.engine.impl.variable.DateType;
import org.camunda.bpm.engine.impl.variable.DoubleType;
import org.camunda.bpm.engine.impl.variable.IntegerType;
import org.camunda.bpm.engine.impl.variable.LongType;
import org.camunda.bpm.engine.impl.variable.ShortType;
import org.camunda.bpm.engine.impl.variable.StringType;
import org.camunda.bpm.engine.rest.dto.runtime.VariableValueDto;

public class DtoUtil {

  /**
   * Returns null, if variables is null. Else transforms variables into a map
   * @param variables
   * @return
   */
  public static Map<String, Object> toMap(Map<String, VariableValueDto> variables) throws ParseException {
    if (variables == null) {
      return null;
    }
    
    Map<String, Object> variablesMap = new HashMap<String, Object>();
    for (Entry<String, VariableValueDto> variable : variables.entrySet()) {
      String type = variable.getValue().getType();
      Object value = variable.getValue().getValue();
      
      if (type != null && !type.equals("") && value != null) {
        // boolean
        if (type.equalsIgnoreCase(BooleanType.TYPE_NAME)) {
          variablesMap.put(variable.getKey(), Boolean.valueOf(value.toString()));
          continue;
        }
          
        // string
        if (type.equalsIgnoreCase(StringType.TYPE_NAME)) {
          variablesMap.put(variable.getKey(), String.valueOf(value));
          continue;
        }
          
        // integer
        if (type.equalsIgnoreCase(IntegerType.TYPE_NAME)) {
          variablesMap.put(variable.getKey(), Integer.valueOf(value.toString()));
          continue;
        }
          
        // short
        if (type.equalsIgnoreCase(ShortType.TYPE_NAME)) {
          variablesMap.put(variable.getKey(), Short.valueOf(value.toString()));
          continue;
        }
        
        // long
        if (type.equalsIgnoreCase(LongType.TYPE_NAME)) {
          variablesMap.put(variable.getKey(), Long.valueOf(value.toString()));
          continue;
        }
  
        // double
        if (type.equalsIgnoreCase(DoubleType.TYPE_NAME)) {
          variablesMap.put(variable.getKey(), Double.valueOf(value.toString()));
          continue;
        }
        
        // date
        if (type.equalsIgnoreCase(DateType.TYPE_NAME)) {
          SimpleDateFormat pattern = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
          Date date = pattern.parse(String.valueOf(value));
          variablesMap.put(variable.getKey(), date);
          continue;
        }
        
        // passed a non supported type
        throw new IllegalArgumentException("The variable type '" + type + "' is not supported.");   
      }
      
      // no type specified or value equals null then simply add the variable
      variablesMap.put(variable.getKey(), value);
    }
    return variablesMap;
  }
}
