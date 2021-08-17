/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.metadata;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.camunda.bpm.container.impl.ContainerIntegrationLogger;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.util.ReflectUtil;

/**
 *
 * @author Daniel Meyer
 *
 */
public class PropertyHelper {

  protected final static ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

  public static final String KEBAB_CASE = "-";
  public static final String SNAKE_CASE = "_";
  public static final String CAMEL_CASE = "";

  /**
   * Regex for Ant-style property placeholders
   */
  private static final Pattern PROPERTY_TEMPLATE = Pattern.compile("([^\\$]*)\\$\\{(.+?)\\}([^\\$]*)");

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
  public static Object convertToClass(String value, Class<?> clazz) {
    Object propertyValue;
    if (clazz.isAssignableFrom(int.class)) {
      propertyValue = Integer.parseInt(value);
    }
    else if (clazz.isAssignableFrom(long.class)) {
      propertyValue = Long.parseLong(value);
    }
    else if (clazz.isAssignableFrom(float.class)) {
      propertyValue = Float.parseFloat(value);
    }
    else if (clazz.isAssignableFrom(boolean.class)) {
      propertyValue = Boolean.parseBoolean(value);
    } else {
      propertyValue = value;
    }
    return propertyValue;
  }

  public static void applyProperty(Object configuration, String key, String stringValue) {
    Class<?> configurationClass = configuration.getClass();

    Method setter = ReflectUtil.getSingleSetter(key, configurationClass);

    if(setter != null) {
      try {
        Class<?> parameterClass = setter.getParameterTypes()[0];
        Object value = PropertyHelper.convertToClass(stringValue, parameterClass);

        setter.invoke(configuration, value);
      }
      catch (Exception e) {
        throw LOG.cannotSetValueForProperty(key, configurationClass.getCanonicalName(), e);
      }
    }
    else {
      throw LOG.cannotFindSetterForProperty(key, configurationClass.getCanonicalName());
    }
  }

  /**
   * Sets an objects fields via reflection from String values.
   * Depending on the field's type the respective values are converted to int or boolean.
   * This method allows to specify a property naming strategy, i.e., if a property is written in
   * <code>camelCase</code>, <code>kebab-case</code>, or <code>snake_case</code>.
   *
   * @param configuration
   * @param properties
   * @param namingStrategy can be either {@link PropertyHelper#KEBAB_CASE}, {@link PropertyHelper#SNAKE_CASE}, or {@link PropertyHelper#CAMEL_CASE}.
   * @throws ProcessEngineException if a property is supplied that matches no field or
   * if the field's type is not String, nor int, nor boolean.
   */
  public static void applyProperties(Object configuration, Map<String, String> properties, String namingStrategy) {
    for (Map.Entry<String, String> property : properties.entrySet()) {
      String key = property.getKey();
      if (!CAMEL_CASE.equals(namingStrategy)) {
        key = convertToCamelCase(key, namingStrategy);
      }
      applyProperty(configuration, key, property.getValue());
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
    applyProperties(configuration, properties, CAMEL_CASE);
  }


  /**
   * Replaces Ant-style property references if the corresponding keys exist in the provided {@link Properties}.
   *
   * @param props contains possible replacements
   * @param original may contain Ant-style templates
   * @return the original string with replaced properties or the unchanged original string if no placeholder found.
   */
  public static String resolveProperty(Properties props, String original) {
    Matcher matcher = PROPERTY_TEMPLATE.matcher(original);
    StringBuilder buffer = new StringBuilder();
    boolean found = false;
    while(matcher.find()) {
      found = true;
      String propertyName = matcher.group(2).trim();
      buffer.append(matcher.group(1))
        .append(props.containsKey(propertyName) ? props.getProperty(propertyName) : "")
        .append(matcher.group(3));
    }
    return found ? buffer.toString() : original;
  }

  protected static String convertToCamelCase(String value, String token) {
    while(value.contains(token)) {
      value = value
          .replaceFirst(token + "[a-z]",
                        String.valueOf(Character.toUpperCase(value.charAt(value.indexOf(token) + 1))));
    }
    return value;
  }

}
