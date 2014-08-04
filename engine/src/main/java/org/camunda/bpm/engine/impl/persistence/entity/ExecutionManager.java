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

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.AbstractVariableQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;


/**
 * @author Tom Baeyens
 */
public class ExecutionManager extends AbstractManager {

  public void insertExecution(ExecutionEntity execution) {
    getDbSqlSession().insert(execution);
  }

  @SuppressWarnings("unchecked")
  public void deleteProcessInstancesByProcessDefinition(String processDefinitionId, String deleteReason, boolean cascade, boolean skipCustomListeners) {
    List<String> processInstanceIds = getDbSqlSession()
      .selectList("selectProcessInstanceIdsByProcessDefinitionId", processDefinitionId);

    for (String processInstanceId: processInstanceIds) {
      deleteProcessInstance(processInstanceId, deleteReason, cascade, skipCustomListeners);
    }

    if (cascade) {
      Context
        .getCommandContext()
        .getHistoricProcessInstanceManager()
        .deleteHistoricProcessInstanceByProcessDefinitionId(processDefinitionId);
    }
  }

  public void deleteProcessInstance(String processInstanceId, String deleteReason) {
    deleteProcessInstance(processInstanceId, deleteReason, false, false);
  }

  public void deleteProcessInstance(String processInstanceId, String deleteReason, boolean cascade, boolean skipCustomListeners) {
    ExecutionEntity execution = findExecutionById(processInstanceId);

    if(execution == null) {
      throw new BadUserRequestException("No process instance found for id '" + processInstanceId + "'");
    }

    CommandContext commandContext = Context.getCommandContext();
    commandContext
      .getTaskManager()
      .deleteTasksByProcessInstanceId(processInstanceId, deleteReason, cascade);

    // delete the execution BEFORE we delete the history, otherwise we will produce orphan HistoricVariableInstance instances
    execution.deleteCascade(deleteReason, skipCustomListeners);

    if (cascade) {
      commandContext
      .getHistoricProcessInstanceManager()
      .deleteHistoricProcessInstanceById(processInstanceId);
    }
  }

  public ExecutionEntity findSubProcessInstanceBySuperExecutionId(String superExecutionId) {
    return (ExecutionEntity) getDbSqlSession().selectOne("selectSubProcessInstanceBySuperExecutionId", superExecutionId);
  }

  public ExecutionEntity findSubProcessInstanceBySuperCaseExecutionId(String superCaseExecutionId) {
    return (ExecutionEntity) getDbSqlSession().selectOne("selectSubProcessInstanceBySuperCaseExecutionId", superCaseExecutionId);
  }

  @SuppressWarnings("unchecked")
  public List<ExecutionEntity> findChildExecutionsByParentExecutionId(String parentExecutionId) {
    return getDbSqlSession().selectList("selectExecutionsByParentExecutionId", parentExecutionId);
  }

  @SuppressWarnings("unchecked")
  public List<ExecutionEntity> findChildExecutionsByProcessInstanceId(String processInstanceId) {
    return getDbSqlSession().selectList("selectExecutionsByProcessInstanceId", processInstanceId);
  }

  public ExecutionEntity findExecutionById(String executionId) {
    return (ExecutionEntity) getDbSqlSession().selectById(ExecutionEntity.class, executionId);
  }

  public long findExecutionCountByQueryCriteria(AbstractVariableQueryImpl executionQuery) {
    return (Long) getDbSqlSession().selectOne("selectExecutionCountByQueryCriteria", executionQuery);
  }

  @SuppressWarnings("unchecked")
  public List<ExecutionEntity> findExecutionsByQueryCriteria(AbstractVariableQueryImpl executionQuery, Page page) {
    return getDbSqlSession().selectList("selectExecutionsByQueryCriteria", executionQuery, page);
  }

  public long findProcessInstanceCountByQueryCriteria(AbstractVariableQueryImpl executionQuery) {
    return (Long) getDbSqlSession().selectOne("selectProcessInstanceCountByQueryCriteria", executionQuery);
  }

  @SuppressWarnings("unchecked")
  public List<ProcessInstance> findProcessInstanceByQueryCriteria(AbstractVariableQueryImpl executionQuery, Page page) {
    return getDbSqlSession().selectList("selectProcessInstanceByQueryCriteria", executionQuery, page);
  }

  @SuppressWarnings("unchecked")
  public List<ExecutionEntity> findEventScopeExecutionsByActivityId(String activityRef, String parentExecutionId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("activityId", activityRef);
    parameters.put("parentExecutionId", parentExecutionId);
    return getDbSqlSession().selectList("selectExecutionsByParentExecutionId", parameters);
  }

  @SuppressWarnings("unchecked")
  public List<Execution> findExecutionsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectExecutionByNativeQuery", parameterMap, firstResult, maxResults);
  }

  @SuppressWarnings("unchecked")
  public List<ProcessInstance> findProcessInstanceByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectExecutionByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findExecutionCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectExecutionCountByNativeQuery", parameterMap);
  }

  public void updateExecutionSuspensionStateByProcessDefinitionId(String processDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionId", processDefinitionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbSqlSession().update(ExecutionEntity.class, "updateExecutionSuspensionStateByParameters", parameters);
  }

  public void updateExecutionSuspensionStateByProcessInstanceId(String processInstanceId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbSqlSession().update(ExecutionEntity.class, "updateExecutionSuspensionStateByParameters", parameters);
  }

  public void updateExecutionSuspensionStateByProcessDefinitionKey(String processDefinitionKey, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbSqlSession().update(ExecutionEntity.class, "updateExecutionSuspensionStateByParameters", parameters);
  }

}
