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
package org.camunda.bpm.engine.rest.util;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.variable.type.SerializableValueType;

/**
 * <p>Builds maps that fulfill the camunda variable json format.</p>
 * <p>
 * For example, if VariablesBuilder.variable("aKey", "aValue").variable("anotherKey", "anotherValue", "String").getVariables()
 * a map is returned that is supposed to be mapped to JSON by rest-assured as follows:
 * </p>
 * <code>
 * {
 *    "aKey" : {"value" : "aValue"},
 *    "anotherKey" : {"value" : "anotherValue", "type" : "String"}
 * }
 * </code>
 *
 * @author Thorben Lindhauer
 *
 */
public class VariablesBuilder {

  private Map<String, Object> variables;

  private VariablesBuilder() {
    variables = new HashMap<String, Object>();
  }

  public static VariablesBuilder create() {
    VariablesBuilder builder = new VariablesBuilder();
    return builder;
  }

  public VariablesBuilder variable(String name, Object value, String type) {
    Map<String, Object> variableValue = getVariableValueMap(value, type);
    variables.put(name, variableValue);
    return this;
  }

  public VariablesBuilder variable(String name, Object value, String type, boolean local) {
    Map<String, Object> variableValue = getVariableValueMap(value, type, local);
    variables.put(name, variableValue);
    return this;
  }

  public VariablesBuilder variable(String name, Object value) {
    return variable(name, value, null);
  }

  public VariablesBuilder variable(String name, Object value, boolean local) {
    return variable(name, value, null, local);
  }

  public VariablesBuilder variable(String name, String type, Object value, String serializationFormat, String objectType) {
    Map<String, Object> variableValue = getObjectValueMap(value, type, serializationFormat, objectType);
    variables.put(name, variableValue);
    return this;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  public static Map<String, Object> getVariableValueMap(Object value) {
    return getVariableValueMap(value, null);
  }

  public static Map<String, Object> getVariableValueMap(Object value, boolean local) {
    return getVariableValueMap(value, null, local);
  }

  public static Map<String, Object> getVariableValueMap(Object value, String type) {
    return getVariableValueMap(value, type, false);
  }

  public static Map<String, Object> getVariableValueMap(Object value, String type, boolean local) {
    Map<String, Object> variable = new HashMap<String, Object>();
    if (value != null) {
      variable.put("value", value);
    }
    if (type != null) {
      variable.put("type", type);
    }

    variable.put("local", local);

    return variable;
  }

  public static Map<String, Object> getObjectValueMap(Object value, String variableType, String serializationFormat, String objectTypeName) {
    Map<String, Object> serializedVariable = new HashMap<String, Object>();

    if (value != null) {
      serializedVariable.put("value", value);
    }

    if (variableType != null) {
      serializedVariable.put("type", variableType);
    }

    Map<String, Object> typeInfo = new HashMap<String, Object>();
    typeInfo.put(SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, serializationFormat);
    typeInfo.put(SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, objectTypeName);

    serializedVariable.put("valueInfo", typeInfo);

    return serializedVariable;
  }

  public VariablesBuilder variableTransient(String name, String value, String type) {
    Map<String, Object> valueMap = getVariableValueMap(value, type);
    Map<String, Object> valueInfo = new HashMap<String, Object>();
    valueInfo.put("transient", true);
    valueMap.put("valueInfo", valueInfo);
    variables.put(name, valueMap);
    return this;
  }

}
