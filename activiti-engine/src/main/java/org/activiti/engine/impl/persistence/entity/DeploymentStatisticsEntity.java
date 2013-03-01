package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.management.DeploymentStatistics;

public class DeploymentStatisticsEntity extends DeploymentEntity implements DeploymentStatistics {

  private static final long serialVersionUID = 1L;

  protected int instances;
  protected int failedJobs;
  
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
