package org.activiti.engine.impl;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.management.ActivityStatistics;
import org.activiti.engine.management.ActivityStatisticsQuery;

public class ActivityStatisticsQueryImpl extends
    AbstractQuery<ActivityStatisticsQuery, ActivityStatistics> implements ActivityStatisticsQuery{

  private static final long serialVersionUID = 1L;
  private boolean includeFailedJobs = false;
  private String processDefinitionId;
  
  public ActivityStatisticsQueryImpl(String processDefinitionId, CommandExecutor executor) {
    super(executor);
    this.processDefinitionId = processDefinitionId;
  }
  
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return 
      commandContext
        .getStatisticsManager()
        .getStatisticsCountGroupedByActivity(this);
  }

  public List<ActivityStatistics> executeList(
      CommandContext commandContext, Page page) {
    checkQueryOk();
    return 
      commandContext
        .getStatisticsManager()
        .getStatisticsGroupedByActivity(this, page);
  }
  
  protected void checkQueryOk() {
    super.checkQueryOk();
    if (processDefinitionId == null) {
      throw new ActivitiException("No valid process definition id supplied.");
    }
  }

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
