package org.camunda.bpm.spring.boot.starter.property;

import java.util.HashMap;
import java.util.Map;

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

public class GenericProperties {

  private Map<String, Object> properties = new HashMap<>();

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  @Override
  public String toString() {
    return joinOn(this.getClass())
      .add("properties=" + properties)
      .toString();
  }

}
