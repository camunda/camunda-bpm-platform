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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.cfg.auth.ResourceAuthorizationProvider;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.task.Task;


/**
 * @author Tom Baeyens
 */
public class TaskManager extends AbstractManager {

  public void insertTask(TaskEntity task) {
    getDbEntityManager().insert(task);
    createDefaultAuthorizations(task);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void deleteTasksByProcessInstanceId(String processInstanceId, String deleteReason, boolean cascade, boolean skipCustomListeners) {
    List<TaskEntity> tasks = (List) getDbEntityManager()
      .createTaskQuery()
      .processInstanceId(processInstanceId)
      .list();

    String reason = (deleteReason == null || deleteReason.length() == 0) ? TaskEntity.DELETE_REASON_DELETED : deleteReason;

    for (TaskEntity task: tasks) {
      task.delete(reason, cascade, skipCustomListeners);
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void deleteTasksByCaseInstanceId(String caseInstanceId, String deleteReason, boolean cascade) {
    List<TaskEntity> tasks = (List) getDbEntityManager()
        .createTaskQuery()
        .caseInstanceId(caseInstanceId)
        .list();

      String reason = (deleteReason == null || deleteReason.length() == 0) ? TaskEntity.DELETE_REASON_DELETED : deleteReason;

      for (TaskEntity task: tasks) {
        task.delete(reason, cascade, false);
      }
  }

  public void deleteTask(TaskEntity task, String deleteReason, boolean cascade, boolean skipCustomListeners) {
    if (!task.isDeleted()) {
      task.setDeleted(true);

      CommandContext commandContext = Context.getCommandContext();
      String taskId = task.getId();

      List<Task> subTasks = findTasksByParentTaskId(taskId);
      for (Task subTask: subTasks) {
        ((TaskEntity) subTask).delete(deleteReason, cascade, skipCustomListeners);
      }

      task.deleteIdentityLinks();

      commandContext
        .getVariableInstanceManager()
        .deleteVariableInstanceByTask(task);

      if (cascade) {
        commandContext
          .getHistoricTaskInstanceManager()
          .deleteHistoricTaskInstanceById(taskId);
      } else {
        commandContext
          .getHistoricTaskInstanceManager()
          .markTaskInstanceEnded(taskId, deleteReason);
      }

      deleteAuthorizations(Resources.TASK, taskId);
      getDbEntityManager().delete(task);
    }
  }


  public TaskEntity findTaskById(String id) {
    ensureNotNull("Invalid task id", "id", id);
    return getDbEntityManager().selectById(TaskEntity.class, id);
  }

  @SuppressWarnings("unchecked")
  public List<TaskEntity> findTasksByExecutionId(String executionId) {
    return getDbEntityManager().selectList("selectTasksByExecutionId", executionId);
  }

  public TaskEntity findTaskByCaseExecutionId(String caseExecutionId) {
    return (TaskEntity) getDbEntityManager().selectOne("selectTaskByCaseExecutionId", caseExecutionId);
  }

  @SuppressWarnings("unchecked")
  public List<TaskEntity> findTasksByProcessInstanceId(String processInstanceId) {
    return getDbEntityManager().selectList("selectTasksByProcessInstanceId", processInstanceId);
  }


  @Deprecated
  public List<Task> findTasksByQueryCriteria(TaskQueryImpl taskQuery, Page page) {
    taskQuery.setFirstResult(page.getFirstResult());
    taskQuery.setMaxResults(page.getMaxResults());
    return findTasksByQueryCriteria(taskQuery);
  }

  @SuppressWarnings("unchecked")
  public List<Task> findTasksByQueryCriteria(TaskQueryImpl taskQuery) {
    configureQuery(taskQuery);
    return getDbEntityManager().selectList("selectTaskByQueryCriteria", taskQuery);
  }

  public long findTaskCountByQueryCriteria(TaskQueryImpl taskQuery) {
    configureQuery(taskQuery);
    return (Long) getDbEntityManager().selectOne("selectTaskCountByQueryCriteria", taskQuery);
  }

  @SuppressWarnings("unchecked")
  public List<Task> findTasksByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbEntityManager().selectListWithRawParameter("selectTaskByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findTaskCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbEntityManager().selectOne("selectTaskCountByNativeQuery", parameterMap);
  }

  @SuppressWarnings("unchecked")
  public List<Task> findTasksByParentTaskId(String parentTaskId) {
    return getDbEntityManager().selectList("selectTasksByParentTaskId", parentTaskId);
  }

  public void updateTaskSuspensionStateByProcessDefinitionId(String processDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionId", processDefinitionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(TaskEntity.class, "updateTaskSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateTaskSuspensionStateByProcessInstanceId(String processInstanceId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(TaskEntity.class, "updateTaskSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateTaskSuspensionStateByProcessDefinitionKey(String processDefinitionKey, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("isProcessDefinitionTenantIdSet", false);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(TaskEntity.class, "updateTaskSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateTaskSuspensionStateByProcessDefinitionKeyAndTenantId(String processDefinitionKey, String processDefinitionTenantId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("isProcessDefinitionTenantIdSet", true);
    parameters.put("processDefinitionTenantId", processDefinitionTenantId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(TaskEntity.class, "updateTaskSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateTaskSuspensionStateByCaseExecutionId(String caseExecutionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("caseExecutionId", caseExecutionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(TaskEntity.class, "updateTaskSuspensionStateByParameters", configureParameterizedQuery(parameters));

  }

  // helper ///////////////////////////////////////////////////////////

  protected void createDefaultAuthorizations(TaskEntity task) {
    if(isAuthorizationEnabled()) {
      ResourceAuthorizationProvider provider = getResourceAuthorizationProvider();
      AuthorizationEntity[] authorizations = provider.newTask(task);
      saveDefaultAuthorizations(authorizations);
    }
  }

  protected void configureQuery(TaskQueryImpl query) {
    getAuthorizationManager().configureTaskQuery(query);
    getTenantManager().configureQuery(query);
  }

  protected ListQueryParameterObject configureParameterizedQuery(Object parameter) {
    return getTenantManager().configureQuery(parameter);
  }

}
