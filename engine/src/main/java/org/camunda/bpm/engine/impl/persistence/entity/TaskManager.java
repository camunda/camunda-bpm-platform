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
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.task.Task;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Tom Baeyens
 */
public class TaskManager extends AbstractManager {

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void deleteTasksByProcessInstanceId(String processInstanceId, String deleteReason, boolean cascade) {
    List<TaskEntity> tasks = (List) getDbSqlSession()
      .createTaskQuery()
      .processInstanceId(processInstanceId)
      .list();

    String reason = (deleteReason == null || deleteReason.length() == 0) ? TaskEntity.DELETE_REASON_DELETED : deleteReason;

    for (TaskEntity task: tasks) {
      task.delete(reason, cascade);
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void deleteTasksByCaseInstanceId(String caseInstanceId, String deleteReason, boolean cascade) {
    List<TaskEntity> tasks = (List) getDbSqlSession()
        .createTaskQuery()
        .caseInstanceId(caseInstanceId)
        .list();

      String reason = (deleteReason == null || deleteReason.length() == 0) ? TaskEntity.DELETE_REASON_DELETED : deleteReason;

      for (TaskEntity task: tasks) {
        task.delete(reason, cascade);
      }
  }

  public void deleteTask(TaskEntity task, String deleteReason, boolean cascade) {
    if (!task.isDeleted()) {
      task.setDeleted(true);

      CommandContext commandContext = Context.getCommandContext();
      String taskId = task.getId();

      List<Task> subTasks = findTasksByParentTaskId(taskId);
      for (Task subTask: subTasks) {
        ((TaskEntity) subTask).delete(deleteReason, cascade);
      }

      commandContext
        .getIdentityLinkManager()
        .deleteIdentityLinksByTaskId(taskId);

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
        if (TaskEntity.DELETE_REASON_COMPLETED.equals(deleteReason)) {
          task.createHistoricTaskDetails(UserOperationLogEntry.OPERATION_TYPE_COMPLETE);
        } else {
          task.createHistoricTaskDetails(UserOperationLogEntry.OPERATION_TYPE_DELETE);
        }
      }

      getDbSqlSession().delete(task);
    }
  }


  public TaskEntity findTaskById(String id) {
    ensureNotNull("Invalid task id", "id", id);
    return (TaskEntity) getDbSqlSession().selectById(TaskEntity.class, id);
  }

  @SuppressWarnings("unchecked")
  public List<TaskEntity> findTasksByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectTasksByExecutionId", executionId);
  }

  public TaskEntity findTaskByCaseExecutionId(String caseExecutionId) {
    return (TaskEntity) getDbSqlSession().selectOne("selectTaskByCaseExecutionId", caseExecutionId);
  }

  @SuppressWarnings("unchecked")
  public List<TaskEntity> findTasksByProcessInstanceId(String processInstanceId) {
    return getDbSqlSession().selectList("selectTasksByProcessInstanceId", processInstanceId);
  }


  @Deprecated
  public List<Task> findTasksByQueryCriteria(TaskQueryImpl taskQuery, Page page) {
    taskQuery.setFirstResult(page.getFirstResult());
    taskQuery.setMaxResults(page.getMaxResults());
    return findTasksByQueryCriteria(taskQuery);
  }

  @SuppressWarnings("unchecked")
  public List<Task> findTasksByQueryCriteria(TaskQueryImpl taskQuery) {
    final String query = "selectTaskByQueryCriteria";
    return getDbSqlSession().selectList(query, taskQuery);
  }

  public long findTaskCountByQueryCriteria(TaskQueryImpl taskQuery) {
    return (Long) getDbSqlSession().selectOne("selectTaskCountByQueryCriteria", taskQuery);
  }

  @SuppressWarnings("unchecked")
  public List<Task> findTasksByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectTaskByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findTaskCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectTaskCountByNativeQuery", parameterMap);
  }

  @SuppressWarnings("unchecked")
  public List<Task> findTasksByParentTaskId(String parentTaskId) {
    return getDbSqlSession().selectList("selectTasksByParentTaskId", parentTaskId);
  }

  public void updateTaskSuspensionStateByProcessDefinitionId(String processDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionId", processDefinitionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbSqlSession().update(TaskEntity.class, "updateTaskSuspensionStateByParameters", parameters);
  }

  public void updateTaskSuspensionStateByProcessInstanceId(String processInstanceId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbSqlSession().update(TaskEntity.class, "updateTaskSuspensionStateByParameters", parameters);
  }

  public void updateTaskSuspensionStateByProcessDefinitionKey(String processDefinitionKey, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbSqlSession().update(TaskEntity.class, "updateTaskSuspensionStateByParameters", parameters);
  }

  public void updateTaskSuspensionStateByCaseExecutionId(String caseExecutionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("caseExecutionId", caseExecutionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbSqlSession().update(TaskEntity.class, "updateTaskSuspensionStateByParameters", parameters);

  }

}
