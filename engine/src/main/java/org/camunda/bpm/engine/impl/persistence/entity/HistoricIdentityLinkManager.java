package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.List;

import org.camunda.bpm.engine.history.HistoricIdentityLink;
import org.camunda.bpm.engine.impl.HistoricIdentityLinkQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;

/**
 * @author Deivarayan Azhagappan
 *
 */
public class HistoricIdentityLinkManager extends AbstractHistoricManager {

  public long findHistoricIdentityLinkCountByQueryCriteria(HistoricIdentityLinkQueryImpl query) {
    getAuthorizationManager().configureHistoricIdentityLinkQuery(query);
    return (Long) getDbEntityManager().selectOne("selectHistoricIdentityLinkCountByQueryCriteria", query);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricIdentityLink> findHistoricIdentityLinkByQueryCriteria(HistoricIdentityLinkQueryImpl query, Page page) {
    getAuthorizationManager().configureHistoricIdentityLinkQuery(query);
    return getDbEntityManager().selectList("selectHistoricIdentityLinkByQueryCriteria", query, page);
  }

  public void deleteHistoricIdentityLinksByProcessDefinitionId(String processDefId) {
    if (isHistoryLevelFullEnabled()) {
      getDbEntityManager().delete(HistoricIdentityLinkEntity.class, "deleteHistoricIdentityLinksByProcessDefinitionId", processDefId);
    }
  }

  public void deleteHistoricIdentityLinksByTaskId(String taskId) {
    if (isHistoryLevelFullEnabled()) {
      getDbEntityManager().delete(HistoricIdentityLinkEntity.class, "deleteHistoricIdentityLinksByTaskId", taskId);
    }
  }
}
