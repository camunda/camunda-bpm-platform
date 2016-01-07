/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.history.HistoricActivityStatisticsQuery;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstanceQuery;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricCaseInstanceQuery;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricIncidentQuery;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricJobLogQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstanceReport;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricActivityInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricCaseActivityInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricCaseInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricTaskInstanceQuery;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;

/**
 * Service exposing information about ongoing and past process instances.  This is different
 * from the runtime information in the sense that this runtime information only contains
 * the actual runtime state at any given moment and it is optimized for runtime
 * process execution performance.  The history information is optimized for easy
 * querying and remains permanent in the persistent storage.
 *
 * @author Christian Stettler
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface HistoryService {

  /** Creates a new programmatic query to search for {@link HistoricProcessInstance}s. */
  HistoricProcessInstanceQuery createHistoricProcessInstanceQuery();

  /** Creates a new programmatic query to search for {@link HistoricActivityInstance}s. */
  HistoricActivityInstanceQuery createHistoricActivityInstanceQuery();

  /**
   * Query for the number of historic activity instances aggregated by activities of a single process definition.
   */
  HistoricActivityStatisticsQuery createHistoricActivityStatisticsQuery(String processDefinitionId);

  /** Creates a new programmatic query to search for {@link HistoricTaskInstance}s. */
  HistoricTaskInstanceQuery createHistoricTaskInstanceQuery();

  /** Creates a new programmatic query to search for {@link HistoricDetail}s. */
  HistoricDetailQuery createHistoricDetailQuery();

  /** Creates a new programmatic query to search for {@link HistoricVariableInstance}s. */
  HistoricVariableInstanceQuery createHistoricVariableInstanceQuery();

  /** Creates a new programmatic query to search for {@link UserOperationLogEntry} instances. */
  UserOperationLogQuery createUserOperationLogQuery();

  /** Creates a new programmatic query to search for {@link HistoricIncident historic incidents}. */
  HistoricIncidentQuery createHistoricIncidentQuery();

  /** Creates a new programmatic query to search for {@link HistoricCaseInstance}s. */
  HistoricCaseInstanceQuery createHistoricCaseInstanceQuery();

  /** Creates a new programmatic query to search for {@link HistoricCaseActivityInstance}s. */
  HistoricCaseActivityInstanceQuery createHistoricCaseActivityInstanceQuery();

  /**
   * Creates a new programmatic query to search for {@link HistoricDecisionInstance}s.
   *
   * If the user has no {@link Permissions#READ_HISTORY} permission on {@link Resources#DECISION_DEFINITION}
   * then the result of the query is empty.
   */
  HistoricDecisionInstanceQuery createHistoricDecisionInstanceQuery();

  /**
   * Deletes historic task instance.  This might be useful for tasks that are
   * {@link TaskService#newTask() dynamically created} and then {@link TaskService#complete(String) completed}.
   * If the historic task instance doesn't exist, no exception is thrown and the
   * method returns normal.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE_HISTORY} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void deleteHistoricTaskInstance(String taskId);

  /**
   * Deletes historic process instance. All historic activities, historic task and
   * historic details (variable updates, form properties) are deleted as well.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE_HISTORY} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void deleteHistoricProcessInstance(String processInstanceId);

  /**
   * Deletes a user operation log entry. Does not cascade to any related entities.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE_HISTORY} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void deleteUserOperationLogEntry(String entryId);

  /**
   * Deletes historic case instance. All historic case activities, historic task and
   * historic details are deleted as well.
   */
  void deleteHistoricCaseInstance(String caseInstanceId);

  /**
   * Deletes historic decision instances of a decision definition. All historic
   * decision inputs and outputs are deleted as well.
   *
   * @param decisionDefinitionId
   *          the id of the decision definition
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE_HISTORY} permission on {@link Resources#DECISION_DEFINITION}.
   */
  void deleteHistoricDecisionInstance(String decisionDefinitionId);

  /**
   * creates a native query to search for {@link HistoricProcessInstance}s via SQL
   */
  NativeHistoricProcessInstanceQuery createNativeHistoricProcessInstanceQuery();

  /**
   * creates a native query to search for {@link HistoricTaskInstance}s via SQL
   */
  NativeHistoricTaskInstanceQuery createNativeHistoricTaskInstanceQuery();

  /**
   * creates a native query to search for {@link HistoricActivityInstance}s via SQL
   */
  NativeHistoricActivityInstanceQuery createNativeHistoricActivityInstanceQuery();

  /**
   * creates a native query to search for {@link HistoricCaseInstance}s via SQL
   */
  NativeHistoricCaseInstanceQuery createNativeHistoricCaseInstanceQuery();

  /**
   * creates a native query to search for {@link HistoricCaseActivityInstance}s via SQL
   */
  NativeHistoricCaseActivityInstanceQuery createNativeHistoricCaseActivityInstanceQuery();

  /**
   * creates a native query to search for {@link HistoricDecisionInstance}s via SQL
   */
  NativeHistoricDecisionInstanceQuery createNativeHistoricDecisionInstanceQuery();

  /**
   * Creates a new programmatic query to search for {@link HistoricJobLog historic job logs}.
   *
   * @since 7.3
   */
  HistoricJobLogQuery createHistoricJobLogQuery();

  /**
   * Returns the full stacktrace of the exception that occurs when the
   * historic job log with the given id was last executed. Returns null
   * when the historic job log has no exception stacktrace.
   *
   * @param historicJobLogId id of the historic job log, cannot be null.
   * @throws ProcessEngineException when no historic job log exists with the given id.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ_HISTORY} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 7.3
   */
  String getHistoricJobLogExceptionStacktrace(String historicJobLogId);

  /**
   * Creates a new programmatic query to create a historic process instance report.
   *
   * @since 7.5
   */
  HistoricProcessInstanceReport createHistoricProcessInstanceReport();

}
