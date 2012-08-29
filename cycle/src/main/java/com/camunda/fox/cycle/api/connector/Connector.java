package com.camunda.fox.cycle.api.connector;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class Connector {
  
  private Map<String, Object> configurationValues;
  
  private String connectorId;
  private String name;
  
  public abstract List<ConnectorNode> getChildren(ConnectorNode parent);
  
  public abstract InputStream getContent(ConnectorNode node);

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * The ConnectorID corresponds to the defining connectors in the spring context, each connector has a singleton instance
   * from which we get the class for our new session instances. We are using the name of this defining bean as the connector id.
   * This is not meant to be shown to the user!
   * 
   * @return
   */
  public String getConnectorId() {
    return connectorId;
  }

  public void setConnectorId(String connectorId) {
    this.connectorId = connectorId;
  }
  
  public void login(String userName, String password) {
  }
  
  public void addConfigValue(String key, Object value) {
    if (this.configurationValues == null) {
      this.configurationValues = new HashMap<String, Object>();
    }
    this.configurationValues.put(key, value);
  }
  
  public Object getConfigValue(String key) {
    return this.configurationValues.get(key);
  }

  @SuppressWarnings("unchecked")
  protected <T> T getConfigValue(String key, Class<T> castTo) {
    Object value = this.configurationValues.get(key);
    if (value == null) {
      return null;
    }
    if (value instanceof String) {
      if (String.class.equals(castTo)) {
        return (T) value;
      }
      if (Boolean.class.equals(castTo)) {
        return (T) Boolean.valueOf((String) value);
      }
      if (Integer.class.equals(castTo)) {
        return (T) Integer.valueOf((String) value);
      }
      if (Float.class.equals(castTo)) {
        return (T) Float.valueOf((String) value);
      }
      if (Long.class.equals(castTo)) {
        return (T) Long.valueOf((String) value);
      }
      throw new RuntimeException("Cannot cast connector configuration value of type 'String' for key '" + key + "' to class '" + castTo);
    } else {
      return (T) value;
    }
  }
  
  public Map<String, Object> getConfigurationValues() {
    return this.configurationValues;
  }
  
  public void setConfigurationValues(Map<String, Object> configurationValues) {
    this.configurationValues = configurationValues;
  }
}
