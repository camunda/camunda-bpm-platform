package org.activiti.engine.impl;

import java.util.List;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.management.ProcessDefinitionStatisticsQuery;
import org.activiti.engine.management.ProcessDefinitionStatistics;

public class ProcessDefinitionStatisticsQueryImpl extends AbstractQuery<ProcessDefinitionStatisticsQuery, ProcessDefinitionStatistics>  
  implements ProcessDefinitionStatisticsQuery {

  private static final long serialVersionUID = 1L;
  private boolean includeFailedJobs = false;

  public ProcessDefinitionStatisticsQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
//    return 
//        commandContext
//          .getRuntimeStatisticsManager()
//          .getRuntimeStatisticsGroupedByProcessDefinitionVersion();
    return 0;
  }

  @Override
  public List<ProcessDefinitionStatistics> executeList(CommandContext commandContext,
      Page page) {
    checkQueryOk();
    return 
      commandContext
        .getStatisticsManager()
        .getStatisticsGroupedByProcessDefinitionVersion(this, page);
  }

  public ProcessDefinitionStatisticsQuery includeFailedJobs() {
    includeFailedJobs = true;
    return this;
  }

  public boolean isFailedJobsToInclude() {
    return includeFailedJobs;
  }
}
