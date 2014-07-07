package org.camunda.spin.spi;

import java.util.Map;

public interface Configurable<R extends Configurable<R>> {

  R config(String key, Object value);
  
  R config(Map<String, Object> config);
  
  Object getValue(String key);
  
  Object getValue(String key, Object defaultValue);
  
  Map<String, Object> getConfiguration();
  
}
