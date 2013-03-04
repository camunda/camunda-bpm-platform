package org.camunda.bpm.engine.rest.dto;

public abstract class StatisticsResultDto {

  protected String id;
  protected Integer instances;
  protected Integer failedJobs;
  
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
