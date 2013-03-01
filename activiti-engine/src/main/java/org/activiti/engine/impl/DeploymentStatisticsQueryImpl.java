package org.activiti.engine.impl;

import java.util.List;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.management.DeploymentStatistics;
import org.activiti.engine.management.DeploymentStatisticsQuery;

public class DeploymentStatisticsQueryImpl extends AbstractQuery<DeploymentStatisticsQuery, DeploymentStatistics>
implements DeploymentStatisticsQuery {

  protected static final long serialVersionUID = 1L;
  protected boolean includeFailedJobs = false;
  
  public DeploymentStatisticsQueryImpl(CommandExecutor executor) {
    super(executor);
  }

  @Override
  public DeploymentStatisticsQuery includeFailedJobs() {
    includeFailedJobs = true;
    return this;
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return 
        commandContext
          .getStatisticsManager()
          .getStatisticsCountGroupedByDeployment(this);
  }

  @Override
  public List<DeploymentStatistics> executeList(CommandContext commandContext,
      Page page) {
    checkQueryOk();
    return 
      commandContext
        .getStatisticsManager()
        .getStatisticsGroupedByDeployment(this, page);
  }
  
  public boolean isFailedJobsToInclude() {
    return includeFailedJobs;
  }
  

}
