/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.impl.HistoricIncidentQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoricIncidentEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricIncidentManager extends AbstractHistoricManager {

  public long findHistoricIncidentCountByQueryCriteria(HistoricIncidentQueryImpl query) {
    configureQuery(query);
    return (Long) getDbEntityManager().selectOne("selectHistoricIncidentCountByQueryCriteria", query);
  }

  public HistoricIncidentEntity findHistoricIncidentById(String id) {
    return (HistoricIncidentEntity) getDbEntityManager().selectOne("selectHistoricIncidentById", id);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricIncident> findHistoricIncidentByQueryCriteria(HistoricIncidentQueryImpl query, Page page) {
    configureQuery(query);
    return getDbEntityManager().selectList("selectHistoricIncidentByQueryCriteria", query, page);
  }

  public DbOperation addRemovalTimeToIncidentsByRootProcessInstanceId(String rootProcessInstanceId, Date removalTime, Integer batchSize) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("rootProcessInstanceId", rootProcessInstanceId);
    parameters.put("removalTime", removalTime);
    parameters.put("maxResults", batchSize);

    return getDbEntityManager()
      .updatePreserveOrder(HistoricIncidentEventEntity.class, "updateHistoricIncidentsByRootProcessInstanceId", parameters);
  }

  public DbOperation addRemovalTimeToIncidentsByProcessInstanceId(String processInstanceId, Date removalTime, Integer batchSize) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("removalTime", removalTime);
    parameters.put("maxResults", batchSize);

    return getDbEntityManager()
      .updatePreserveOrder(HistoricIncidentEventEntity.class, "updateHistoricIncidentsByProcessInstanceId", parameters);
  }

  public void deleteHistoricIncidentsByProcessInstanceIds(List<String> processInstanceIds) {
    getDbEntityManager().deletePreserveOrder(HistoricIncidentEntity.class, "deleteHistoricIncidentsByProcessInstanceIds", processInstanceIds);
  }

  public void deleteHistoricIncidentsByProcessDefinitionId(String processDefinitionId) {
    if (isHistoryEventProduced()) {
      getDbEntityManager().delete(HistoricIncidentEntity.class, "deleteHistoricIncidentsByProcessDefinitionId", processDefinitionId);
    }
  }

  public void deleteHistoricIncidentsByJobDefinitionId(String jobDefinitionId) {
    if (isHistoryEventProduced()) {
      getDbEntityManager().delete(HistoricIncidentEntity.class, "deleteHistoricIncidentsByJobDefinitionId", jobDefinitionId);
    }
  }

  public void deleteHistoricIncidentsByBatchId(List<String> historicBatchIds) {
    if (isHistoryEventProduced()) {
      getDbEntityManager().delete(HistoricIncidentEntity.class, "deleteHistoricIncidentsByBatchIds", historicBatchIds);
    }
  }

  protected void configureQuery(HistoricIncidentQueryImpl query) {
    getAuthorizationManager().configureHistoricIncidentQuery(query);
    getTenantManager().configureQuery(query);
  }

  protected boolean isHistoryEventProduced() {
    HistoryLevel historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    return historyLevel.isHistoryEventProduced(HistoryEventTypes.INCIDENT_CREATE, null) ||
           historyLevel.isHistoryEventProduced(HistoryEventTypes.INCIDENT_DELETE, null) ||
           historyLevel.isHistoryEventProduced(HistoryEventTypes.INCIDENT_MIGRATE, null) ||
           historyLevel.isHistoryEventProduced(HistoryEventTypes.INCIDENT_RESOLVE, null);
  }

  public DbOperation deleteHistoricIncidentsByRemovalTime(Date removalTime, int minuteFrom, int minuteTo, int batchSize) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("removalTime", removalTime);
    if (minuteTo - minuteFrom + 1 < 60) {
      parameters.put("minuteFrom", minuteFrom);
      parameters.put("minuteTo", minuteTo);
    }
    parameters.put("batchSize", batchSize);

    return getDbEntityManager()
      .deletePreserveOrder(HistoricIncidentEntity.class, "deleteHistoricIncidentsByRemovalTime",
        new ListQueryParameterObject(parameters, 0, batchSize));
  }

  public void addRemovalTimeToHistoricIncidentsByBatchId(String batchId, Date removalTime) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("batchId", batchId);
    parameters.put("removalTime", removalTime);

    getDbEntityManager()
      .updatePreserveOrder(HistoricIncidentEntity.class, "updateHistoricIncidentsByBatchId", parameters);
  }

}
