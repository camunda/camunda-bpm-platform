package org.activiti.engine.impl.persistence.entity;

import java.util.List;

import org.activiti.engine.impl.ActivityStatisticsQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.ProcessDefinitionStatisticsQueryImpl;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.management.ActivityStatistics;
import org.activiti.engine.management.ProcessDefinitionStatistics;

public class StatisticsManager extends AbstractManager {

  @SuppressWarnings("unchecked")
  public List<ProcessDefinitionStatistics> getStatisticsGroupedByProcessDefinitionVersion(
      ProcessDefinitionStatisticsQueryImpl query, Page page) {
    return getDbSqlSession().selectList("selectProcessDefinitionStatistics", query, page);
  }
  
  public long getStatisticsCountGroupedByProcessDefinitionVersion(ProcessDefinitionStatisticsQueryImpl query) {
    return (Long) getDbSqlSession().selectOne("selectProcessDefinitionStatisticsCount", query);
  }
  
  @SuppressWarnings("unchecked")
  public List<ActivityStatistics> getStatisticsGroupedByActivity(ActivityStatisticsQueryImpl query, Page page) {
    return getDbSqlSession().selectList("selectActivityStatistics", query, page);
  }
  
  public long getStatisticsCountGroupedByActivity(ActivityStatisticsQueryImpl query) {
    return (Long) getDbSqlSession().selectOne("selectActivityStatisticsCount", query);
  }
}
