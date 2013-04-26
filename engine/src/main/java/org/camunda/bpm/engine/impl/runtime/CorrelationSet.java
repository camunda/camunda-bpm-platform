package org.camunda.bpm.engine.impl.runtime;

import java.util.Map;

public class CorrelationSet {

  private final String businessKey;
  private final Map<String, Object> correlationKeys;
  
  public CorrelationSet(String businessKey, Map<String, Object> correlationKeys) {
    this.businessKey = businessKey;
    this.correlationKeys = correlationKeys;
  }
  
  public String getBusinessKey() {
    return businessKey;
  }
  public Map<String, Object> getCorrelationKeys() {
    return correlationKeys;
  }
}
