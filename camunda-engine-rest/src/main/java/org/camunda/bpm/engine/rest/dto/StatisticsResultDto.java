package org.camunda.bpm.engine.rest.dto;

public class StatisticsResultDto {

  private String id;
  private int instances;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public int getInstances() {
    return instances;
  }
  public void setInstances(int instances) {
    this.instances = instances;
  }
}
