package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.management.ActivityStatisticsResult;

public class ActivityStatisticsResultImpl implements ActivityStatisticsResult {

  private String id;
  private int instances;
  private int failedJobs;
  
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
  public int getFailedJobs() {
    return failedJobs;
  }
  public void setFailedJobs(int failedJobs) {
    this.failedJobs = failedJobs;
  }
}
