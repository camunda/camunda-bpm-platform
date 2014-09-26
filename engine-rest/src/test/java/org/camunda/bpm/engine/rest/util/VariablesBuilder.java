package org.camunda.bpm.engine.rest.util;

import java.util.HashMap;
import java.util.Map;

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

  public VariablesBuilder variableSerialized(String name, String jsonValue, Map<String, Object> serializationConfig) {
    Map<String, Object> variableValueMap = getVariableValueMap(jsonValue, "Object");
    variableValueMap.put("serializationConfig", serializationConfig);
    variables.put(name, variableValueMap);
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

  public static Map<String, Object> getSerializedValueMap(Object value, String variableType, Map<String, Object> configuration) {
    Map<String, Object> serializedVariable = new HashMap<String, Object>();

    if (value != null) {
      serializedVariable.put("value", value);
    }

    if (variableType != null) {
      serializedVariable.put("variableType", variableType);
    }

    if (configuration != null) {
      serializedVariable.put("serializationConfig", configuration);
    }

    return serializedVariable;
  }

}
