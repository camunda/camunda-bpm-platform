package org.activiti.engine.impl.persistence.entity;

import java.util.List;

import org.activiti.engine.impl.ActivityRuntimeStatisticsQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.ProcessDefinitionRuntimeStatisticsQueryImpl;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.management.ActivityStatisticsResult;
import org.activiti.engine.management.ProcessDefinitionStatisticsResult;

public class RuntimeStatisticsManager extends AbstractManager {

  @SuppressWarnings("unchecked")
  public List<ProcessDefinitionStatisticsResult> getRuntimeStatisticsGroupedByProcessDefinitionVersion(
      ProcessDefinitionRuntimeStatisticsQueryImpl query, Page page) {
    return getDbSqlSession().selectList("selectProcessDefinitionRuntimeStatistics", query, page);
  }
  
  @SuppressWarnings("unchecked")
  public List<ActivityStatisticsResult> getRuntimeStatisticsGroupedByActivity(ActivityRuntimeStatisticsQueryImpl query, Page page) {
    return getDbSqlSession().selectList("selectActivityRuntimeStatistics", query, page);
  }
}
