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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;


/**
 * @author Tom Baeyens
 */
public class HistoricProcessInstanceManager extends AbstractHistoricManager {

  public HistoricProcessInstanceEntity findHistoricProcessInstance(String processInstanceId) {
    if (isHistoryEnabled()) {
      return (HistoricProcessInstanceEntity) getDbSqlSession().selectById(HistoricProcessInstanceEntity.class, processInstanceId);
    }
    return null;
  }

  public HistoricProcessInstanceEventEntity findHistoricProcessInstanceEvent(String eventId) {
    if (historyLevel>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      return (HistoricProcessInstanceEventEntity) getDbSqlSession().selectById(HistoricProcessInstanceEventEntity.class, eventId);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public void deleteHistoricProcessInstanceByProcessDefinitionId(String processDefinitionId) {
    if (isHistoryEnabled()) {
      List<String> historicProcessInstanceIds = getDbSqlSession()
        .selectList("selectHistoricProcessInstanceIdsByProcessDefinitionId", processDefinitionId);

      for (String historicProcessInstanceId: historicProcessInstanceIds) {
        deleteHistoricProcessInstanceById(historicProcessInstanceId);
      }
    }
  }

  public void deleteHistoricProcessInstanceById(String historicProcessInstanceId) {
    if (isHistoryEnabled()) {
      CommandContext commandContext = Context.getCommandContext();

      commandContext
        .getHistoricDetailManager()
        .deleteHistoricDetailsByProcessInstanceId(historicProcessInstanceId);

      commandContext
        .getHistoricVariableInstanceManager()
        .deleteHistoricVariableInstanceByProcessInstanceId(historicProcessInstanceId);

      commandContext
        .getHistoricActivityInstanceManager()
        .deleteHistoricActivityInstancesByProcessInstanceId(historicProcessInstanceId);

      commandContext
        .getHistoricTaskInstanceManager()
        .deleteHistoricTaskInstancesByProcessInstanceId(historicProcessInstanceId);

      commandContext
          .getOperationLogManager()
          .deleteOperationLogEntriesByProcessInstanceId(historicProcessInstanceId);

      commandContext
          .getHistoricIncidentManager()
          .deleteHistoricIncidentsByProcessInstanceId(historicProcessInstanceId);

      commandContext.getDbSqlSession().delete(HistoricProcessInstanceEntity.class, "deleteHistoricProcessInstance", historicProcessInstanceId);

    }
  }

  public long findHistoricProcessInstanceCountByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery) {
    if (isHistoryEnabled()) {
      return (Long) getDbSqlSession().selectOne("selectHistoricProcessInstanceCountByQueryCriteria", historicProcessInstanceQuery);
    }
    return 0;
  }

  @SuppressWarnings("unchecked")
  public List<HistoricProcessInstance> findHistoricProcessInstancesByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery, Page page) {
    if (isHistoryEnabled()) {
      return getDbSqlSession().selectList("selectHistoricProcessInstancesByQueryCriteria", historicProcessInstanceQuery, page);
    }
    return Collections.EMPTY_LIST;
  }

  @SuppressWarnings("unchecked")
  public List<HistoricProcessInstance> findHistoricProcessInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectHistoricProcessInstanceByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findHistoricProcessInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectHistoricProcessInstanceCountByNativeQuery", parameterMap);
  }
}
