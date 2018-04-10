package org.camunda.bpm.spring.boot.starter.property;

import java.util.HashMap;
import java.util.Map;

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

public class GenericProperties {

  private Map<String, Object> properties = new HashMap<>();
  private boolean ignoreUnknownFields;

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  public boolean isIgnoreUnknownFields() {
    return ignoreUnknownFields;
  }

  public void setIgnoreUnknownFields(boolean ignoreUnknownFields) {
    this.ignoreUnknownFields = ignoreUnknownFields;
  }

  @Override
  public String toString() {
    return joinOn(this.getClass())
      .add("properties=" + properties)
      .add("ignoreUnknownFields=" + ignoreUnknownFields)
      .toString();
  }

}
