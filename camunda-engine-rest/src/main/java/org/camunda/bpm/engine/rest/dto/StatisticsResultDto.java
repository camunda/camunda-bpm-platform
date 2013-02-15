package org.camunda.bpm.engine.rest.dto;

public class StatisticsResultDto {

  private String id;
  private Integer instances;
  private Integer failedJobs;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public Integer getInstances() {
    return instances;
  }
  public void setInstances(Integer instances) {
    this.instances = instances;
  }
  public Integer getFailedJobs() {
    return failedJobs;
  }
  public void setFailedJobs(Integer failedJobs) {
    this.failedJobs = failedJobs;
  }
  
}
