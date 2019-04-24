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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.impl.HistoricCaseActivityInstanceQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;


/**
 * @author Sebastian Menski
 */
public class HistoricCaseActivityInstanceManager extends AbstractHistoricManager {

  public void deleteHistoricCaseActivityInstancesByCaseInstanceIds(List<String> historicCaseInstanceIds) {
    if (isHistoryEnabled()) {
      getDbEntityManager().delete(HistoricCaseActivityInstanceEntity.class, "deleteHistoricCaseActivityInstancesByCaseInstanceIds", historicCaseInstanceIds);
    }
  }

  public void insertHistoricCaseActivityInstance(HistoricCaseActivityInstanceEntity historicCaseActivityInstance) {
    getDbEntityManager().insert(historicCaseActivityInstance);
  }

  public HistoricCaseActivityInstanceEntity findHistoricCaseActivityInstance(String caseActivityId, String caseInstanceId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("caseActivityId", caseActivityId);
    parameters.put("caseInstanceId", caseInstanceId);

    return (HistoricCaseActivityInstanceEntity) getDbEntityManager().selectOne("selectHistoricCaseActivityInstance", parameters);
  }

  public long findHistoricCaseActivityInstanceCountByQueryCriteria(HistoricCaseActivityInstanceQueryImpl historicCaseActivityInstanceQuery) {
    configureHistoricCaseActivityInstanceQuery(historicCaseActivityInstanceQuery);
    return (Long) getDbEntityManager().selectOne("selectHistoricCaseActivityInstanceCountByQueryCriteria", historicCaseActivityInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricCaseActivityInstance> findHistoricCaseActivityInstancesByQueryCriteria(HistoricCaseActivityInstanceQueryImpl historicCaseActivityInstanceQuery, Page page) {
    configureHistoricCaseActivityInstanceQuery(historicCaseActivityInstanceQuery);
    return getDbEntityManager().selectList("selectHistoricCaseActivityInstancesByQueryCriteria", historicCaseActivityInstanceQuery, page);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricCaseActivityInstance> findHistoricCaseActivityInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbEntityManager().selectListWithRawParameter("selectHistoricCaseActivityInstanceByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findHistoricCaseActivityInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbEntityManager().selectOne("selectHistoricCaseActivityInstanceCountByNativeQuery", parameterMap);
  }

  protected void configureHistoricCaseActivityInstanceQuery(HistoricCaseActivityInstanceQueryImpl query) {
    getTenantManager().configureQuery(query);
  }
}
