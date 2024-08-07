package org.camunda.bpm.engine.variable.value.builder;

public interface Executionaware {
  public String getExecutionId();
  public void setExecutionId(String executionId);
}
