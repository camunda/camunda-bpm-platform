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

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.HistoricVariableInstanceQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;


/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricVariableInstanceManager extends AbstractHistoricManager {

  public void deleteHistoricVariableInstanceByProcessInstanceId(String historicProcessInstanceId) {
    deleteHistoricVariableInstancesByProcessCaseInstanceId(historicProcessInstanceId, null);
  }

  public void deleteHistoricVariableInstanceByCaseInstanceId(String historicCaseInstanceId) {
    deleteHistoricVariableInstancesByProcessCaseInstanceId(null, historicCaseInstanceId);
  }

  protected void deleteHistoricVariableInstancesByProcessCaseInstanceId(String historicProcessInstanceId, String historicCaseInstanceId) {
    ensureOnlyOneNotNull("Only the process instance or case instance id should be set", historicProcessInstanceId, historicCaseInstanceId);
    if (isHistoryEnabled()) {

      // delete entries in DB
      List<HistoricVariableInstance> historicVariableInstances;
      if (historicProcessInstanceId != null) {
        historicVariableInstances = findHistoricVariableInstancesByProcessInstanceId(historicProcessInstanceId);
      }
      else {
        historicVariableInstances = findHistoricVariableInstancesByCaseInstanceId(historicCaseInstanceId);
      }

      for (HistoricVariableInstance historicVariableInstance : historicVariableInstances) {
        ((HistoricVariableInstanceEntity) historicVariableInstance).delete();
      }

      // delete entries in Cache
      List <HistoricVariableInstanceEntity> cachedHistoricVariableInstances = getDbEntityManager().getCachedEntitiesByType(HistoricVariableInstanceEntity.class);
      for (HistoricVariableInstanceEntity historicVariableInstance : cachedHistoricVariableInstances) {
        // make sure we only delete the right ones (as we cannot make a proper query in the cache)
        if ((historicProcessInstanceId != null && historicProcessInstanceId.equals(historicVariableInstance.getProcessInstanceId()))
            || (historicCaseInstanceId != null && historicCaseInstanceId.equals(historicVariableInstance.getCaseInstanceId()))) {
          historicVariableInstance.delete();
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public List<HistoricVariableInstance> findHistoricVariableInstancesByProcessInstanceId(String processInstanceId) {
    return getDbEntityManager().selectList("selectHistoricVariablesByProcessInstanceId", processInstanceId);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricVariableInstance> findHistoricVariableInstancesByCaseInstanceId(String caseInstanceId) {
    return getDbEntityManager().selectList("selectHistoricVariablesByCaseInstanceId", caseInstanceId);
  }

  public long findHistoricVariableInstanceCountByQueryCriteria(HistoricVariableInstanceQueryImpl historicProcessVariableQuery) {
    return (Long) getDbEntityManager().selectOne("selectHistoricVariableInstanceCountByQueryCriteria", historicProcessVariableQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricVariableInstance> findHistoricVariableInstancesByQueryCriteria(HistoricVariableInstanceQueryImpl historicProcessVariableQuery, Page page) {
    return getDbEntityManager().selectList("selectHistoricVariableInstanceByQueryCriteria", historicProcessVariableQuery, page);
  }

  public HistoricVariableInstanceEntity findHistoricVariableInstanceByVariableInstanceId(String variableInstanceId) {
    return (HistoricVariableInstanceEntity) getDbEntityManager().selectOne("selectHistoricVariableInstanceByVariableInstanceId", variableInstanceId);
  }

  public void deleteHistoricVariableInstancesByTaskId(String taskId) {
    if (isHistoryEnabled()) {
      HistoricVariableInstanceQuery historicProcessVariableQuery = new HistoricVariableInstanceQueryImpl().taskIdIn(taskId);
      List<HistoricVariableInstance> historicProcessVariables = historicProcessVariableQuery.list();
      for(HistoricVariableInstance historicProcessVariable : historicProcessVariables) {
        ((HistoricVariableInstanceEntity) historicProcessVariable).delete();
      }
    }
  }
}
