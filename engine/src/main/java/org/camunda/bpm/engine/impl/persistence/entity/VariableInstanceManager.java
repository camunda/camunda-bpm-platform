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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.VariableInstanceQueryImpl;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.runtime.VariableInstance;


/**
 * @author Tom Baeyens
 */
public class VariableInstanceManager extends AbstractManager {

  public List<VariableInstanceEntity> findVariableInstancesByTaskId(String taskId) {
    return findVariableInstancesByTaskIdAndVariableNames(taskId, null);
  }

  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByTaskIdAndVariableNames(String taskId, Collection<String> variableNames) {
    Map<String, Object> parameter = new HashMap<String, Object>();
    parameter.put("taskId", taskId);
    parameter.put("variableNames", variableNames);
    return getDbEntityManager().selectList("selectVariablesByTaskId", parameter);
  }

  public List<VariableInstanceEntity> findVariableInstancesByExecutionId(String executionId) {
    return findVariableInstancesByExecutionIdAndVariableNames(executionId, null);
  }

  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByExecutionIdAndVariableNames(String executionId, Collection<String> variableNames) {
    Map<String, Object> parameter = new HashMap<String, Object>();
    parameter.put("executionId", executionId);
    parameter.put("variableNames", variableNames);
    return getDbEntityManager().selectList("selectVariablesByExecutionId", parameter);
  }

  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByProcessInstanceId(String processInstanceId) {
    return getDbEntityManager().selectList("selectVariablesByProcessInstanceId", processInstanceId);
  }

  public List<VariableInstanceEntity> findVariableInstancesByCaseExecutionId(String caseExecutionId) {
    return findVariableInstancesByCaseExecutionIdAndVariableNames(caseExecutionId, null);
  }

  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByCaseExecutionIdAndVariableNames(String caseExecutionId, Collection<String> variableNames) {
    Map<String, Object> parameter = new HashMap<String, Object>();
    parameter.put("caseExecutionId", caseExecutionId);
    parameter.put("variableNames", variableNames);
    return getDbEntityManager().selectList("selectVariablesByCaseExecutionId", parameter);
  }

  public void deleteVariableInstanceByTask(TaskEntity task) {
    List<VariableInstanceEntity> variableInstances = task.variableStore.getVariables();
    for (VariableInstanceEntity variableInstance: variableInstances) {
      variableInstance.delete();
    }
  }

  public long findVariableInstanceCountByQueryCriteria(VariableInstanceQueryImpl variableInstanceQuery) {
    configureQuery(variableInstanceQuery);
    return (Long) getDbEntityManager().selectOne("selectVariableInstanceCountByQueryCriteria", variableInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<VariableInstance> findVariableInstanceByQueryCriteria(VariableInstanceQueryImpl variableInstanceQuery, Page page) {
    configureQuery(variableInstanceQuery);
    return getDbEntityManager().selectList("selectVariableInstanceByQueryCriteria", variableInstanceQuery, page);
  }

  protected void configureQuery(VariableInstanceQueryImpl query) {
    getAuthorizationManager().configureVariableInstanceQuery(query);
    getTenantManager().configureQuery(query);
  }

}
