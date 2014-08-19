/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.engine.delegate.SerializedVariableTypes;
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
      Object originalValue = variable.getValue().getValue();
      Object concreteValue = toType(type, originalValue);
      variablesMap.put(variable.getKey(), concreteValue);
    }
    return variablesMap;
  }

  public static Object toType(String type, Object value) throws ParseException {

    if (type != null && !type.equals("") && value != null) {
      // boolean
      if (type.equalsIgnoreCase(SerializedVariableTypes.Boolean.getName())) {
        return Boolean.valueOf(value.toString());
      }

      // string
      if (type.equalsIgnoreCase(SerializedVariableTypes.String.getName())) {
        return String.valueOf(value);
      }

      // integer
      if (type.equalsIgnoreCase(SerializedVariableTypes.Integer.getName())) {
        return Integer.valueOf(value.toString());
      }

      // short
      if (type.equalsIgnoreCase(SerializedVariableTypes.Short.getName())) {
        return Short.valueOf(value.toString());
      }

      // long
      if (type.equalsIgnoreCase(SerializedVariableTypes.Long.getName())) {
        return Long.valueOf(value.toString());
      }

      // double
      if (type.equalsIgnoreCase(SerializedVariableTypes.Double.getName())) {
        return Double.valueOf(value.toString());
      }

      // date
      if (type.equalsIgnoreCase(SerializedVariableTypes.Date.getName())) {
        SimpleDateFormat pattern = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = pattern.parse(String.valueOf(value));
        return date;
      }

      // passed a non supported type
      throw new IllegalArgumentException("The variable type '" + type + "' is not supported.");
    }

    // no type specified or value equals null then simply return the value
    return value;
  }

  public static Date toDate(Object value) throws ParseException {
    String stringValue = String.valueOf(value);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    return dateFormat.parse(stringValue);
  }
}
