package org.camunda.bpm.application.impl.deployment.metadata;

import java.util.Map;

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

}
