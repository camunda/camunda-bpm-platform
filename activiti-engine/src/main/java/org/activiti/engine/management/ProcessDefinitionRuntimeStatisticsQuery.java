package org.activiti.engine.management;

import org.activiti.engine.query.Query;

public interface ProcessDefinitionRuntimeStatisticsQuery extends Query<ProcessDefinitionRuntimeStatisticsQuery, ProcessDefinitionStatisticsResult> {

  ProcessDefinitionRuntimeStatisticsQuery includeFailedJobs();
}
