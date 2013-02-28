package org.activiti.engine.management;

import org.activiti.engine.query.Query;

public interface ProcessDefinitionStatisticsQuery extends Query<ProcessDefinitionStatisticsQuery, ProcessDefinitionStatistics> {

  ProcessDefinitionStatisticsQuery includeFailedJobs();
}
