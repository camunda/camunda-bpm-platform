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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureOnlyOneNotNull;

import java.util.List;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.impl.HistoricDetailQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.history.event.HistoricDetailEventEntity;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;


/**
 * @author Tom Baeyens
 */
public class HistoricDetailManager extends AbstractHistoricManager {

  public void deleteHistoricDetailsByProcessInstanceId(String historicProcessInstanceId) {
    deleteHistoricDetailsByProcessCaseInstanceId(historicProcessInstanceId, null);
  }

  public void deleteHistoricDetailsByCaseInstanceId(String historicCaseInstanceId) {
    deleteHistoricDetailsByProcessCaseInstanceId(null, historicCaseInstanceId);
  }

  public void deleteHistoricDetailsByProcessCaseInstanceId(String historicProcessInstanceId, String historicCaseInstanceId) {
    ensureOnlyOneNotNull("Only the process instance or case instance id should be set", historicProcessInstanceId, historicCaseInstanceId);
    if (isHistoryEnabled()) {

      // delete entries in DB
      List<HistoricDetail> historicDetails;
      if (historicProcessInstanceId != null) {
        historicDetails = findHistoricDetailsByProcessInstanceId(historicProcessInstanceId);
      }
      else {
        historicDetails = findHistoricDetailsByCaseInstanceId(historicCaseInstanceId);
      }

      for (HistoricDetail historicDetail : historicDetails) {
        ((HistoricDetailEventEntity) historicDetail).delete();
      }

      //delete entries in Cache
      List<HistoricDetailEventEntity> cachedHistoricDetails = getDbEntityManager().getCachedEntitiesByType(HistoricDetailEventEntity.class);
      for (HistoricDetailEventEntity historicDetail : cachedHistoricDetails) {
        // make sure we only delete the right ones (as we cannot make a proper query in the cache)
        if ((historicProcessInstanceId != null && historicProcessInstanceId.equals(historicDetail.getProcessInstanceId()))
            || (historicCaseInstanceId != null && historicCaseInstanceId.equals(historicDetail.getCaseInstanceId()))) {
          historicDetail.delete();
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public List<HistoricDetail> findHistoricDetailsByProcessInstanceId(String processInstanceId) {
    return getDbEntityManager().selectList("selectHistoricDetailsByProcessInstanceId", processInstanceId);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricDetail> findHistoricDetailsByCaseInstanceId(String caseInstanceId) {
    return getDbEntityManager().selectList("selectHistoricDetailsByCaseInstanceId", caseInstanceId);
  }

  public long findHistoricDetailCountByQueryCriteria(HistoricDetailQueryImpl historicVariableUpdateQuery) {
    return (Long) getDbEntityManager().selectOne("selectHistoricDetailCountByQueryCriteria", historicVariableUpdateQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricDetail> findHistoricDetailsByQueryCriteria(HistoricDetailQueryImpl historicVariableUpdateQuery, Page page) {
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
}
