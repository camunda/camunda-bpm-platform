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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.CleanableHistoricCaseInstanceReportResult;
import org.camunda.bpm.engine.impl.CleanableHistoricCaseInstanceReportImpl;
import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.HistoricCaseInstanceQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.QueryPropertyImpl;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.history.event.HistoricCaseInstanceEventEntity;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;
import org.camunda.bpm.engine.impl.util.ClockUtil;


/**
 * @author Sebastian Menski
 */
public class HistoricCaseInstanceManager extends AbstractHistoricManager {

  public HistoricCaseInstanceEntity findHistoricCaseInstance(String caseInstanceId) {
    if (isHistoryEnabled()) {
      return getDbEntityManager().selectById(HistoricCaseInstanceEntity.class, caseInstanceId);
    }
    return null;
  }

  public HistoricCaseInstanceEventEntity findHistoricCaseInstanceEvent(String eventId) {
    if (isHistoryEnabled()) {
      return getDbEntityManager().selectById(HistoricCaseInstanceEventEntity.class, eventId);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public void deleteHistoricCaseInstanceByCaseDefinitionId(String caseDefinitionId) {
    if (isHistoryEnabled()) {
      List<String> historicCaseInstanceIds = getDbEntityManager()
        .selectList("selectHistoricCaseInstanceIdsByCaseDefinitionId", caseDefinitionId);

      if (historicCaseInstanceIds != null && !historicCaseInstanceIds.isEmpty()) {
        deleteHistoricCaseInstancesByIds(historicCaseInstanceIds);
      }
    }
  }

  public void deleteHistoricCaseInstancesByIds(List<String> historicCaseInstanceIds) {
    if (isHistoryEnabled()) {
      getHistoricDetailManager().deleteHistoricDetailsByCaseInstanceIds(historicCaseInstanceIds);

      getHistoricVariableInstanceManager().deleteHistoricVariableInstancesByCaseInstanceIds(historicCaseInstanceIds);

      getHistoricCaseActivityInstanceManager().deleteHistoricCaseActivityInstancesByCaseInstanceIds(historicCaseInstanceIds);

      getHistoricTaskInstanceManager().deleteHistoricTaskInstancesByCaseInstanceIds(historicCaseInstanceIds);

      getDbEntityManager().delete(HistoricCaseInstanceEntity.class, "deleteHistoricCaseInstancesByIds", historicCaseInstanceIds);
    }
  }

  public long findHistoricCaseInstanceCountByQueryCriteria(HistoricCaseInstanceQueryImpl historicCaseInstanceQuery) {
    if (isHistoryEnabled()) {
      configureHistoricCaseInstanceQuery(historicCaseInstanceQuery);
      return (Long) getDbEntityManager().selectOne("selectHistoricCaseInstanceCountByQueryCriteria", historicCaseInstanceQuery);
    }
    return 0;
  }

  @SuppressWarnings("unchecked")
  public List<HistoricCaseInstance> findHistoricCaseInstancesByQueryCriteria(HistoricCaseInstanceQueryImpl historicCaseInstanceQuery, Page page) {
    if (isHistoryEnabled()) {
      configureHistoricCaseInstanceQuery(historicCaseInstanceQuery);
      return getDbEntityManager().selectList("selectHistoricCaseInstancesByQueryCriteria", historicCaseInstanceQuery, page);
    }
    return Collections.EMPTY_LIST;
  }

  @SuppressWarnings("unchecked")
  public List<HistoricCaseInstance> findHistoricCaseInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbEntityManager().selectListWithRawParameter("selectHistoricCaseInstanceByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findHistoricCaseInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbEntityManager().selectOne("selectHistoricCaseInstanceCountByNativeQuery", parameterMap);
  }

  protected void configureHistoricCaseInstanceQuery(HistoricCaseInstanceQueryImpl query) {
    getTenantManager().configureQuery(query);
  }

  @SuppressWarnings("unchecked")
  public List<String> findHistoricCaseInstanceIdsForCleanup(int batchSize, int minuteFrom, int minuteTo) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("currentTimestamp", ClockUtil.getCurrentTime());
    if (minuteTo - minuteFrom + 1 < 60) {
      parameters.put("minuteFrom", minuteFrom);
      parameters.put("minuteTo", minuteTo);
    }
    ListQueryParameterObject parameterObject = new ListQueryParameterObject(parameters, 0, batchSize);
    return getDbEntityManager().selectList("selectHistoricCaseInstanceIdsForCleanup", parameterObject);
  }

  @SuppressWarnings("unchecked")
  public List<CleanableHistoricCaseInstanceReportResult> findCleanableHistoricCaseInstancesReportByCriteria(CleanableHistoricCaseInstanceReportImpl query, Page page) {
    query.setCurrentTimestamp(ClockUtil.getCurrentTime());
    getTenantManager().configureQuery(query);
    return getDbEntityManager().selectList("selectFinishedCaseInstancesReportEntities", query, page);
  }

  public long findCleanableHistoricCaseInstancesReportCountByCriteria(CleanableHistoricCaseInstanceReportImpl query) {
    query.setCurrentTimestamp(ClockUtil.getCurrentTime());
    getTenantManager().configureQuery(query);
    return (Long) getDbEntityManager().selectOne("selectFinishedCaseInstancesReportEntitiesCount", query);
  }

}
