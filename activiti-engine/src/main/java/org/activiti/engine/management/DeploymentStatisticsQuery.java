package org.activiti.engine.management;

import org.activiti.engine.query.Query;

public interface DeploymentStatisticsQuery extends Query<DeploymentStatisticsQuery, DeploymentStatistics> {

  /**
   * Include an aggregation of failed jobs in the result.
   */
  DeploymentStatisticsQuery includeFailedJobs();
}
