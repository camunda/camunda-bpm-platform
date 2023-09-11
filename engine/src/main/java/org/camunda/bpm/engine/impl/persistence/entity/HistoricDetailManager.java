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
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.impl.HistoricDetailQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.history.event.HistoricDetailEventEntity;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;

/**
 * @author Tom Baeyens
 */
public class HistoricDetailManager extends AbstractHistoricManager {

  public void deleteHistoricDetailsByProcessInstanceIds(List<String> historicProcessInstanceIds) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processInstanceIds", historicProcessInstanceIds);
    deleteHistoricDetails(parameters);
  }

  public void deleteHistoricDetailsByTaskProcessInstanceIds(List<String> historicProcessInstanceIds) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("taskProcessInstanceIds", historicProcessInstanceIds);
    deleteHistoricDetails(parameters);
  }

  public void deleteHistoricDetailsByCaseInstanceIds(List<String> historicCaseInstanceIds) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("caseInstanceIds", historicCaseInstanceIds);
    deleteHistoricDetails(parameters);
  }

  public void deleteHistoricDetailsByTaskCaseInstanceIds(List<String> historicCaseInstanceIds) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("taskCaseInstanceIds", historicCaseInstanceIds);
    deleteHistoricDetails(parameters);
  }

  public void deleteHistoricDetailsByVariableInstanceId(String historicVariableInstanceId) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("variableInstanceId", historicVariableInstanceId);
    deleteHistoricDetails(parameters);
  }

  public void deleteHistoricDetails(Map<String, Object> parameters) {
    getDbEntityManager().deletePreserveOrder(ByteArrayEntity.class, "deleteHistoricDetailByteArraysByIds", parameters);
    getDbEntityManager().deletePreserveOrder(HistoricDetailEventEntity.class, "deleteHistoricDetailsByIds", parameters);
  }


  public long findHistoricDetailCountByQueryCriteria(HistoricDetailQueryImpl historicVariableUpdateQuery) {
    configureQuery(historicVariableUpdateQuery);
    return (Long) getDbEntityManager().selectOne("selectHistoricDetailCountByQueryCriteria", historicVariableUpdateQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricDetail> findHistoricDetailsByQueryCriteria(HistoricDetailQueryImpl historicVariableUpdateQuery, Page page) {
    configureQuery(historicVariableUpdateQuery);
    return getDbEntityManager().selectList("selectHistoricDetailsByQueryCriteria", historicVariableUpdateQuery, page);
  }

  public void deleteHistoricDetailsByTaskId(String taskId) {
    if (isHistoryEnabled()) {
      // delete entries in DB
      List<HistoricDetail> historicDetails = findHistoricDetailsByTaskId(taskId);

      for (HistoricDetail historicDetail : historicDetails) {
        ((HistoricDetailEventEntity) historicDetail).delete();
      }

      //delete entries in Cache
      List<HistoricDetailEventEntity> cachedHistoricDetails = getDbEntityManager().getCachedEntitiesByType(HistoricDetailEventEntity.class);
      for (HistoricDetailEventEntity historicDetail : cachedHistoricDetails) {
        // make sure we only delete the right ones (as we cannot make a proper query in the cache)
        if (taskId.equals(historicDetail.getTaskId())) {
          historicDetail.delete();
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public List<HistoricDetail> findHistoricDetailsByTaskId(String taskId) {
    return getDbEntityManager().selectList("selectHistoricDetailsByTaskId", taskId);
  }

  protected void configureQuery(HistoricDetailQueryImpl query) {
    getAuthorizationManager().configureHistoricDetailQuery(query);
    getTenantManager().configureQuery(query);
  }

  public DbOperation addRemovalTimeToDetailsByRootProcessInstanceId(String rootProcessInstanceId, Date removalTime, Integer batchSize) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("rootProcessInstanceId", rootProcessInstanceId);
    parameters.put("removalTime", removalTime);
    parameters.put("maxResults", batchSize);

    return getDbEntityManager()
      .updatePreserveOrder(HistoricDetailEventEntity.class, "updateHistoricDetailsByRootProcessInstanceId", parameters);
  }

  public DbOperation addRemovalTimeToDetailsByProcessInstanceId(String processInstanceId, Date removalTime, Integer batchSize) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("removalTime", removalTime);
    parameters.put("maxResults", batchSize);

    return getDbEntityManager()
      .updatePreserveOrder(HistoricDetailEventEntity.class, "updateHistoricDetailsByProcessInstanceId", parameters);
  }

  public DbOperation deleteHistoricDetailsByRemovalTime(Date removalTime, int minuteFrom, int minuteTo, int batchSize) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("removalTime", removalTime);
    if (minuteTo - minuteFrom + 1 < 60) {
      parameters.put("minuteFrom", minuteFrom);
      parameters.put("minuteTo", minuteTo);
    }
    parameters.put("batchSize", batchSize);

    return getDbEntityManager()
      .deletePreserveOrder(HistoricDetailEventEntity.class, "deleteHistoricDetailsByRemovalTime",
        new ListQueryParameterObject(parameters, 0, batchSize));
  }

}
