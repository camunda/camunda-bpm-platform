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
    configureQuery(query);
    return (Long) getDbEntityManager().selectOne("selectHistoricIdentityLinkCountByQueryCriteria", query);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricIdentityLinkLog> findHistoricIdentityLinkLogByQueryCriteria(HistoricIdentityLinkLogQueryImpl query, Page page) {
    configureQuery(query);
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

  public void deleteHistoricIdentityLinksLogByTaskProcessInstanceIds(List<String> processInstanceIds) {
    getDbEntityManager().deletePreserveOrder(HistoricIdentityLinkLogEntity.class, "deleteHistoricIdentityLinksByTaskProcessInstanceIds", processInstanceIds);
  }

  public void deleteHistoricIdentityLinksLogByTaskCaseInstanceIds(List<String> caseInstanceIds) {
    getDbEntityManager().deletePreserveOrder(HistoricIdentityLinkLogEntity.class, "deleteHistoricIdentityLinksByTaskCaseInstanceIds", caseInstanceIds);
  }

  protected void configureQuery(HistoricIdentityLinkLogQueryImpl query) {
    getAuthorizationManager().configureHistoricIdentityLinkQuery(query);
    getTenantManager().configureQuery(query);
  }

}
