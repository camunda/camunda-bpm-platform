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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.impl.HistoricDetailQueryImpl;
import org.camunda.bpm.engine.impl.Page;
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

}
