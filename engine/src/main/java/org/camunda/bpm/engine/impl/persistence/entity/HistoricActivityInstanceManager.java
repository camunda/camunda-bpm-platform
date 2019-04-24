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

import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.impl.HistoricActivityInstanceQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;


/**
 * @author Tom Baeyens
 */
public class HistoricActivityInstanceManager extends AbstractHistoricManager {

  public void deleteHistoricActivityInstancesByProcessInstanceIds(List<String> historicProcessInstanceIds) {
    getDbEntityManager().deletePreserveOrder(HistoricActivityInstanceEntity.class, "deleteHistoricActivityInstancesByProcessInstanceIds", historicProcessInstanceIds);
  }

  public void insertHistoricActivityInstance(HistoricActivityInstanceEntity historicActivityInstance) {
    getDbEntityManager().insert(historicActivityInstance);
  }

  public HistoricActivityInstanceEntity findHistoricActivityInstance(String activityId, String processInstanceId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("activityId", activityId);
    parameters.put("processInstanceId", processInstanceId);

    return (HistoricActivityInstanceEntity) getDbEntityManager().selectOne("selectHistoricActivityInstance", parameters);
  }

  public long findHistoricActivityInstanceCountByQueryCriteria(HistoricActivityInstanceQueryImpl historicActivityInstanceQuery) {
    configureQuery(historicActivityInstanceQuery);
    return (Long) getDbEntityManager().selectOne("selectHistoricActivityInstanceCountByQueryCriteria", historicActivityInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricActivityInstance> findHistoricActivityInstancesByQueryCriteria(HistoricActivityInstanceQueryImpl historicActivityInstanceQuery, Page page) {
    configureQuery(historicActivityInstanceQuery);
    return getDbEntityManager().selectList("selectHistoricActivityInstancesByQueryCriteria", historicActivityInstanceQuery, page);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricActivityInstance> findHistoricActivityInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbEntityManager().selectListWithRawParameter("selectHistoricActivityInstanceByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findHistoricActivityInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbEntityManager().selectOne("selectHistoricActivityInstanceCountByNativeQuery", parameterMap);
  }

  protected void configureQuery(HistoricActivityInstanceQueryImpl query) {
    getAuthorizationManager().configureHistoricActivityInstanceQuery(query);
    getTenantManager().configureQuery(query);
  }

  public void addRemovalTimeToActivityInstancesByRootProcessInstanceId(String rootProcessInstanceId, Date removalTime) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("rootProcessInstanceId", rootProcessInstanceId);
    parameters.put("removalTime", removalTime);

    getDbEntityManager()
      .updatePreserveOrder(HistoricActivityInstanceEventEntity.class, "updateHistoricActivityInstancesByRootProcessInstanceId", parameters);
  }

  public void addRemovalTimeToActivityInstancesByProcessInstanceId(String processInstanceId, Date removalTime) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("removalTime", removalTime);

    getDbEntityManager()
      .updatePreserveOrder(HistoricActivityInstanceEventEntity.class, "updateHistoricActivityInstancesByProcessInstanceId", parameters);
  }

  public DbOperation deleteHistoricActivityInstancesByRemovalTime(Date removalTime, int minuteFrom, int minuteTo, int batchSize) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("removalTime", removalTime);
    if (minuteTo - minuteFrom + 1 < 60) {
      parameters.put("minuteFrom", minuteFrom);
      parameters.put("minuteTo", minuteTo);
    }
    parameters.put("batchSize", batchSize);

    return getDbEntityManager()
      .deletePreserveOrder(HistoricActivityInstanceEntity.class, "deleteHistoricActivityInstancesByRemovalTime",
        new ListQueryParameterObject(parameters, 0, batchSize));
  }

}
