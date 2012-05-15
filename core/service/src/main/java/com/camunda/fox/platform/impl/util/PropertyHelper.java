package com.camunda.fox.platform.impl.util;

import java.util.Map;

/**
 * 
 * @author Daniel Meyer
 */
public class PropertyHelper {
  
  public static <T> T getProperty(Map<String, Object> configMap, String propertyName, T defaultValue) {
    if(configMap.containsKey(propertyName)) {
      return (T) configMap.get(propertyName);
    } else {
      return defaultValue;
    }
  }

}
