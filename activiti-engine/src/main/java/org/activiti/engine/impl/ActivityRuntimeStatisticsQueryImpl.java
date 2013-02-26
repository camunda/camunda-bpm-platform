package org.activiti.engine.impl;

import java.util.List;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.management.ActivityRuntimeStatisticsQuery;
import org.activiti.engine.management.ActivityStatisticsResult;

public class ActivityRuntimeStatisticsQueryImpl extends
    AbstractQuery<ActivityRuntimeStatisticsQuery, ActivityStatisticsResult> implements ActivityRuntimeStatisticsQuery{

  private static final long serialVersionUID = 1L;
  private boolean includeFailedJobs = false;
  private String processDefinitionId;
  
  public ActivityRuntimeStatisticsQueryImpl(String processDefinitionId, CommandExecutor executor) {
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
  public List<ActivityStatisticsResult> executeList(
      CommandContext commandContext, Page page) {
    checkQueryOk();
    return 
      commandContext
        .getRuntimeStatisticsManager()
        .getRuntimeStatisticsGroupedByActivity(this, page);
  }

  @Override
  public ActivityRuntimeStatisticsQuery includeFailedJobs() {
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
