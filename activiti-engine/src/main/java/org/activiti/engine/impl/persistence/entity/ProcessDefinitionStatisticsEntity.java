package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.management.ProcessDefinitionStatisticsResult;

public class ProcessDefinitionStatisticsEntity extends ProcessDefinitionEntity implements ProcessDefinitionStatisticsResult {

  private static final long serialVersionUID = 1L;
  private int instances;
  private int failedJobs;
  
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
