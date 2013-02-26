package org.activiti.engine.impl;

import java.util.List;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.management.ActivityStatisticsQuery;
import org.activiti.engine.management.ActivityStatistics;

public class ActivityStatisticsQueryImpl extends
    AbstractQuery<ActivityStatisticsQuery, ActivityStatistics> implements ActivityStatisticsQuery{

  private static final long serialVersionUID = 1L;
  private boolean includeFailedJobs = false;
  private String processDefinitionId;
  
  public ActivityStatisticsQueryImpl(String processDefinitionId, CommandExecutor executor) {
    super(executor);
    this.processDefinitionId = processDefinitionId;
  }
  
  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
//  return 
//      commandContext
//        .getRuntimeStatisticsManager()
//        .getRuntimeStatisticsGroupedByProcessDefinitionVersion();
  return 0;
  }

  @Override
  public List<ActivityStatistics> executeList(
      CommandContext commandContext, Page page) {
    checkQueryOk();
    return 
      commandContext
        .getStatisticsManager()
        .getStatisticsGroupedByActivity(this, page);
  }

  @Override
  public ActivityStatisticsQuery includeFailedJobs() {
    includeFailedJobs = true;
    return this;
  }

  public boolean isFailedJobsToInclude() {
    return includeFailedJobs;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
}
