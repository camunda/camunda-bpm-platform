package org.camunda.bpm.engine.impl.runtime;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.runtime.SerializedVariableValue;

public class SerializedVariableValueImpl implements SerializedVariableValue {

  protected Object value;
  protected Map<String, Object> config = new HashMap<String, Object>();

  public Object getValue() {
    return value;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public void setConfigValue(String key, Object value) {
    this.config.put(key, value);
  }



}
