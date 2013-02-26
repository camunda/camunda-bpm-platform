package org.activiti.engine.impl;

import java.util.List;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.management.ProcessDefinitionRuntimeStatisticsQuery;
import org.activiti.engine.management.ProcessDefinitionStatisticsResult;

public class ProcessDefinitionRuntimeStatisticsQueryImpl extends AbstractQuery<ProcessDefinitionRuntimeStatisticsQuery, ProcessDefinitionStatisticsResult>  
  implements ProcessDefinitionRuntimeStatisticsQuery {

  private static final long serialVersionUID = 1L;
  private boolean includeFailedJobs = false;

  public ProcessDefinitionRuntimeStatisticsQueryImpl(CommandExecutor commandExecutor) {
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
  public List<ProcessDefinitionStatisticsResult> executeList(CommandContext commandContext,
      Page page) {
    checkQueryOk();
    return 
      commandContext
        .getRuntimeStatisticsManager()
        .getRuntimeStatisticsGroupedByProcessDefinitionVersion(this, page);
  }

  public ProcessDefinitionRuntimeStatisticsQuery includeFailedJobs() {
    includeFailedJobs = true;
    return this;
  }

  public boolean isFailedJobsToInclude() {
    return includeFailedJobs;
  }
}
