package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.List;

import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.impl.HistoricIdentityLinkLogQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;

/**
 * @author Deivarayan Azhagappan
 *
 */
public class HistoricIdentityLinkLogManager extends AbstractHistoricManager {

  public long findHistoricIdentityLinkLogCountByQueryCriteria(HistoricIdentityLinkLogQueryImpl query) {
    getAuthorizationManager().configureHistoricIdentityLinkQuery(query);
    return (Long) getDbEntityManager().selectOne("selectHistoricIdentityLinkCountByQueryCriteria", query);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricIdentityLinkLog> findHistoricIdentityLinkLogByQueryCriteria(HistoricIdentityLinkLogQueryImpl query, Page page) {
    getAuthorizationManager().configureHistoricIdentityLinkQuery(query);
    return getDbEntityManager().selectList("selectHistoricIdentityLinkByQueryCriteria", query, page);
  }

  public void deleteHistoricIdentityLinksLogByProcessDefinitionId(String processDefId) {
    if (isHistoryLevelFullEnabled()) {
      getDbEntityManager().delete(HistoricIdentityLinkLogEntity.class, "deleteHistoricIdentityLinksByProcessDefinitionId", processDefId);
    }
  }

  public void deleteHistoricIdentityLinksLogByTaskId(String taskId) {
    if (isHistoryLevelFullEnabled()) {
      getDbEntityManager().delete(HistoricIdentityLinkLogEntity.class, "deleteHistoricIdentityLinksByTaskId", taskId);
    }
  }
}
