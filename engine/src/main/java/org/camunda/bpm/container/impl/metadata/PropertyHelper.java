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
package org.camunda.bpm.container.impl.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.util.ReflectUtil;

/**
 * 
 * @author Daniel Meyer
 *
 */
public class PropertyHelper {

  public static boolean getBooleanProperty(Map<String, String> properties, String name, boolean defaultValue) {
    String value = properties.get(name);
    if(value == null) {
      return defaultValue;
    } else {
      return Boolean.parseBoolean(value);
    }
  }

  /**
   * Converts a value to the type of the given field.
   * @param value
   * @param field
   * @return
   */
  public static Object convertToFieldType(String value, Field field) {
    Object propertyValue;
    Class<?> expectedPropertyClass = field.getType();
    if (expectedPropertyClass.isAssignableFrom(int.class)) {
      propertyValue = Integer.parseInt(value);
    } else if (expectedPropertyClass.isAssignableFrom(boolean.class)) {
      propertyValue = Boolean.parseBoolean(value);
    } else {
      propertyValue = value;
    }
    return propertyValue;
  }
  
  public static void applyProperty(Object configuration, String key, String stringValue) {
    Class<?> configurationClass = configuration.getClass();
    Field propertyField = ReflectUtil.getField(key, configurationClass);
    
    if (propertyField == null) {
      throw new ProcessEngineException("Cannot set property " + key + " on configuration of class " + configurationClass);
    }
    
    Object value = PropertyHelper.convertToFieldType(stringValue, propertyField);
    Method setter = ReflectUtil.getSetter(key, configurationClass, propertyField.getType());
    if(setter != null) {
      try {
        setter.invoke(configuration, value);
      } catch (Exception e) {
        throw new ProcessEngineException("Could not set value for property '"+key, e);
      }
    } else {
      throw new ProcessEngineException("Could not find setter for property '"+key);
    }
  }
  
  /**
   * Sets an objects fields via reflection from String values.
   * Depending on the field's type the respective values are converted to int or boolean.
   * 
   * @param configuration
   * @param properties
   * @throws ProcessEngineException if a property is supplied that matches no field or
   * if the field's type is not String, nor int, nor boolean.
   */
  public static void applyProperties(Object configuration, Map<String, String> properties) {
    for (Map.Entry<String, String> property : properties.entrySet()) {
      applyProperty(configuration, property.getKey(), property.getValue());
    }
  }

}
