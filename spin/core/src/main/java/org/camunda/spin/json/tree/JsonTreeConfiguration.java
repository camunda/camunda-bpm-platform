package org.camunda.spin.json.tree;

import java.util.HashMap;
import java.util.Map;

public class JsonTreeConfiguration implements JsonTreeConfigurable<JsonTreeConfiguration> {

  public static final String ALLOW_NUMERIC_LEADING_ZEROS = "allowNumericLeadingZeros";
  
  protected Map<String, Object> configuration;

  public JsonTreeConfiguration() {
    this.configuration = new HashMap<String, Object>();
  }
  
  public JsonTreeConfiguration(Map<String, Object> configuration) {
    this.configuration = configuration;
  }
  
  public JsonTreeConfiguration config(String key, Object value) {
    configuration.put(key, value);
    return this;
  }

  public JsonTreeConfiguration config(Map<String, Object> config) {
    configuration.putAll(config);
    return this;
  }

  public Object getValue(String key) {
    return configuration.get(key);
  }

  public Object getValue(String key, Object defaultValue) {
    if (configuration.get(key) == null) {
      return defaultValue;
    }
    
    return configuration.get(key);
  }

  public Boolean allowsNumericLeadingZeros() {
    return (Boolean) getValue(ALLOW_NUMERIC_LEADING_ZEROS, Boolean.FALSE);
  }

  public JsonTreeConfiguration allowNumericLeadingZeros(Boolean value) {
    configuration.put(ALLOW_NUMERIC_LEADING_ZEROS, value);
    return this;
  }

  public Map<String, Object> getConfiguration() {
    return configuration;
  }
  
  
}
