package org.activiti.engine.management;

import org.activiti.engine.repository.Deployment;

public interface DeploymentStatistics extends Deployment {

  /**
   * The number of all process instances of the process definitions contained in this deployment.
   */
  int getInstances();
  
  /**
   * The number of all failed jobs of process instances of definitions contained in this deployment.
   */
  int getFailedJobs();
}
