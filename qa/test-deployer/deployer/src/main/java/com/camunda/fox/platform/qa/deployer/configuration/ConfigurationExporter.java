/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.platform.qa.deployer.configuration;

import com.camunda.fox.platform.qa.deployer.exception.ConfigurationException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 
 * @author nico.rehwaldt@camunda.com
 */
public class ConfigurationExporter<T> {

  private final T configuration;
  private final String propertyPrefix;

  public ConfigurationExporter(T configuration, String propertyPrefix) {
    this.configuration = configuration;
    this.propertyPrefix = propertyPrefix;
  }

  public void toProperties(final OutputStream output) {
    try {
      serializeFieldsToProperties(output);
    } catch (Exception e) {
      throw new ConfigurationException("Unable to serialize persistence configuration to property file.", e);
    } finally {
      if (output != null) {
        try {
          output.close();
        } catch (IOException e) {
          throw new ConfigurationException("Unable to close stream after serialization of persistence configuration to property file.", e);
        }
      }
    }
  }

  private void serializeFieldsToProperties(final OutputStream output)
          throws IOException, IllegalArgumentException, IllegalAccessException {
    
    final Map<String, String> fieldsWithValues = extractFieldsWithValues(configuration, propertyPrefix);
    
    for (Entry<String, String> entry : fieldsWithValues.entrySet()) {
      output.write(serializeAsProperty(entry).getBytes());
    }
  }

  private String serializeAsProperty(Entry<String, String> entry) {
    String serializedAsProperty;
    final StringBuilder sb = new StringBuilder();
    sb.append(entry.getKey()).append("=").append(entry.getValue()).append('\n');
    serializedAsProperty = sb.toString();
    return serializedAsProperty;
  }

  private Map<String, String> extractFieldsWithValues(Object container, String prefix) throws IllegalArgumentException, IllegalAccessException {
    if (prefix != null && !prefix.isEmpty()) {
      if (!prefix.endsWith(".")) {
        prefix = prefix + ".";
      }
    } else {
      prefix = "";
    }
    
    final Map<String, String> extractedValues = new HashMap<String, String>();
    List<Field> fields = SecurityActions.getAccessibleFields(container.getClass());
    
    for (Field field : fields) {
      Object object = field.get(container);
      String key = prefix + field.getName();

      if (object != null) {
        if (object instanceof List) {
          Map<String, String> extractedSubValues = extractFieldsWithValuesFromCollection((List<?>) object, key);
          extractedValues.putAll(extractedSubValues);
        } else {
          extractedValues.put(key, object.toString());
        }
      }
    }
    
    return extractedValues;
  }

  private Map<String, String> extractFieldsWithValuesFromCollection(List<?> collection, String prefix) throws IllegalArgumentException, IllegalAccessException {
    HashMap<String, String> extractedValues = new HashMap<String, String>();
    
    StringBuilder sb = new StringBuilder();
    
    int index = 0;
    for (Object o: collection) {
      // Sub prefixes for collection members will be like [prefix.][index]
      String subPrefix = (prefix.isEmpty() ? prefix : (prefix + ".")) + index;
      
      extractedValues.putAll(extractFieldsWithValues(o, subPrefix));
      sb.append(sb.length() > 0 ? "," : "").append(index);
      index++;
    }
    
    extractedValues.put(prefix, sb.toString());
    
    return extractedValues;
  }
}
