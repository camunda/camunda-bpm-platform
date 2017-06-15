/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;


import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.HistoricFinishedProcessInstanceReportResult;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * @author Tom Baeyens
 */
public class HistoricProcessInstanceManager extends AbstractHistoricManager {

  public HistoricProcessInstanceEntity findHistoricProcessInstance(String processInstanceId) {
    if (isHistoryEnabled()) {
      return getDbEntityManager().selectById(HistoricProcessInstanceEntity.class, processInstanceId);
    }
    return null;
  }

  public HistoricProcessInstanceEventEntity findHistoricProcessInstanceEvent(String eventId) {
    if (isHistoryEnabled()) {
      return getDbEntityManager().selectById(HistoricProcessInstanceEventEntity.class, eventId);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public void deleteHistoricProcessInstanceByProcessDefinitionId(String processDefinitionId) {
    if (isHistoryEnabled()) {
      List<String> historicProcessInstanceIds = getDbEntityManager()
        .selectList("selectHistoricProcessInstanceIdsByProcessDefinitionId", processDefinitionId);

      for (String historicProcessInstanceId: historicProcessInstanceIds) {
        deleteHistoricProcessInstanceByIds(Arrays.asList(historicProcessInstanceId));
      }
    }
  }

  public void deleteHistoricProcessInstanceByIds(List<String> processInstanceIds) {
    CommandContext commandContext = Context.getCommandContext();

    commandContext.getHistoricDetailManager().deleteHistoricDetailsByProcessInstanceIds(processInstanceIds);
    commandContext.getHistoricVariableInstanceManager().deleteHistoricVariableInstanceByProcessInstanceIds(processInstanceIds);
    commandContext.getCommentManager().deleteCommentsByProcessInstanceIds(processInstanceIds);
    commandContext.getAttachmentManager().deleteAttachmentsByProcessInstanceIds(processInstanceIds);
    commandContext.getHistoricTaskInstanceManager().deleteHistoricTaskInstancesByProcessInstanceIds(processInstanceIds, false);
    commandContext.getHistoricActivityInstanceManager().deleteHistoricActivityInstancesByProcessInstanceIds(processInstanceIds);
    commandContext.getHistoricIncidentManager().deleteHistoricIncidentsByProcessInstanceIds(processInstanceIds);
    commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByProcessInstanceIds(processInstanceIds);
    commandContext.getHistoricExternalTaskLogManager().deleteHistoricExternalTaskLogsByProcessInstanceIds(processInstanceIds);

    commandContext.getDbEntityManager().deletePreserveOrder(HistoricProcessInstanceEntity.class, "deleteHistoricProcessInstances", processInstanceIds);
  }

  public long findHistoricProcessInstanceCountByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery) {
    if (isHistoryEnabled()) {
      configureQuery(historicProcessInstanceQuery);
      return (Long) getDbEntityManager().selectOne("selectHistoricProcessInstanceCountByQueryCriteria", historicProcessInstanceQuery);
    }
    return 0;
  }

  @SuppressWarnings("unchecked")
  public List<HistoricProcessInstance> findHistoricProcessInstancesByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery, Page page) {
    if (isHistoryEnabled()) {
      configureQuery(historicProcessInstanceQuery);
      return getDbEntityManager().selectList("selectHistoricProcessInstancesByQueryCriteria", historicProcessInstanceQuery, page);
    }
    return Collections.EMPTY_LIST;
  }

  @SuppressWarnings("unchecked")
  public List<HistoricProcessInstance> findHistoricProcessInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbEntityManager().selectListWithRawParameter("selectHistoricProcessInstanceByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findHistoricProcessInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbEntityManager().selectOne("selectHistoricProcessInstanceCountByNativeQuery", parameterMap);
  }

  protected void configureQuery(HistoricProcessInstanceQueryImpl query) {
    getAuthorizationManager().configureHistoricProcessInstanceQuery(query);
    getTenantManager().configureQuery(query);
  }

  public List<String> findHistoricProcessInstanceIdsForCleanup(Integer batchSize) {
    ListQueryParameterObject parameterObject = new ListQueryParameterObject();
    parameterObject.setParameter(ClockUtil.getCurrentTime());
    parameterObject.setFirstResult(0);
    parameterObject.setMaxResults(batchSize);
    return (List<String>) getDbEntityManager().selectList("selectHistoricProcessInstanceIdsForCleanup", parameterObject);
  }

  public Long findHistoricProcessInstanceIdsForCleanupCount() {
    ListQueryParameterObject parameterObject = new ListQueryParameterObject();
    parameterObject.setParameter(ClockUtil.getCurrentTime());
    return (Long) getDbEntityManager().selectOne("selectHistoricProcessInstanceIdsForCleanupCount", parameterObject);
  }

  public List<HistoricFinishedProcessInstanceReportResult> findFinishedProcessInstancesReport() {
    ListQueryParameterObject parameterObject = new ListQueryParameterObject();
    parameterObject.setParameter(ClockUtil.getCurrentTime());
    getAuthorizationManager().configureQuery(parameterObject, Resources.PROCESS_DEFINITION, "PD.KEY_", Permissions.READ, Permissions.READ_HISTORY);
    getTenantManager().configureQuery(parameterObject);
    return (List<HistoricFinishedProcessInstanceReportResult>) getDbEntityManager().selectList("selectFinishedProcessInstancesReportEntities", parameterObject);
  }

  public List<String> findHistoricProcessInstanceIds(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery) {
    configureQuery(historicProcessInstanceQuery);
    return (List<String>) getDbEntityManager().selectList("selectHistoricProcessInstanceIdsByQueryCriteria", historicProcessInstanceQuery);
  }
}
