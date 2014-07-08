package org.camunda.spin.impl.json.tree;

import java.util.HashMap;
import java.util.Map;

public class JsonJacksonTreeConfiguration implements JsonJacksonTreeConfigurable<JsonJacksonTreeConfiguration> {

  public static final String ALLOW_NUMERIC_LEADING_ZEROS = "allowNumericLeadingZeros";
  
  protected Map<String, Object> configuration;

  public JsonJacksonTreeConfiguration() {
    this.configuration = new HashMap<String, Object>();
  }
  
  public JsonJacksonTreeConfiguration(Map<String, Object> configuration) {
    this.configuration = configuration;
  }
  
  public JsonJacksonTreeConfiguration config(String key, Object value) {
    configuration.put(key, value);
    return this;
  }

  public JsonJacksonTreeConfiguration config(Map<String, Object> config) {
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

  public JsonJacksonTreeConfiguration allowNumericLeadingZeros(Boolean value) {
    configuration.put(ALLOW_NUMERIC_LEADING_ZEROS, value);
    return this;
  }

  public Map<String, Object> getConfiguration() {
    return configuration;
  }
  
  
}
