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
package org.camunda.bpm.engine;

import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.HistoricProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.HistoricTaskPermissions;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.authorization.UserOperationLogCategoryPermissions;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatchQuery;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.history.HistoricActivityStatisticsQuery;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstanceQuery;
import org.camunda.bpm.engine.history.HistoricCaseActivityStatisticsQuery;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricCaseInstanceQuery;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.history.HistoricExternalTaskLogQuery;
import org.camunda.bpm.engine.history.CleanableHistoricBatchReport;
import org.camunda.bpm.engine.history.CleanableHistoricCaseInstanceReport;
import org.camunda.bpm.engine.history.CleanableHistoricDecisionInstanceReport;
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReport;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLogQuery;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricIncidentQuery;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricJobLogQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstanceReport;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.history.HistoricTaskInstanceReport;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricActivityInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricCaseActivityInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricCaseInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricTaskInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricVariableInstanceQuery;
import org.camunda.bpm.engine.history.SetRemovalTimeSelectModeForHistoricBatchesBuilder;
import org.camunda.bpm.engine.history.SetRemovalTimeSelectModeForHistoricDecisionInstancesBuilder;
import org.camunda.bpm.engine.history.SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder;
import org.camunda.bpm.engine.history.SetRemovalTimeToHistoricBatchesBuilder;
import org.camunda.bpm.engine.history.SetRemovalTimeToHistoricDecisionInstancesBuilder;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceStatisticsQuery;
import org.camunda.bpm.engine.history.SetRemovalTimeToHistoricProcessInstancesBuilder;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;

import java.util.Date;
import java.util.List;

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

  /**
   * <p>Creates a new programmatic query to search for {@link HistoricProcessInstance}s.
   *
   * <p>The result of the query is empty in the following cases:
   * <ul>
   *   <li>The user has no {@link Permissions#READ_HISTORY} permission on
   *   {@link Resources#PROCESS_DEFINITION} OR
   *   <li>The user has no {@link HistoricProcessInstancePermissions#READ} permission on
   *       {@link Resources#HISTORIC_PROCESS_INSTANCE} ({@code enableHistoricInstancePermissions} in
   *       {@link ProcessEngineConfigurationImpl} must be set to {@code true})
   * */
  HistoricProcessInstanceQuery createHistoricProcessInstanceQuery();

  /**
   * <p>Creates a new programmatic query to search for {@link HistoricActivityInstance}s.
   *
   * <p>The result of the query is empty in the following cases:
   * <ul>
   *   <li>The user has no {@link Permissions#READ_HISTORY} permission on
   *   {@link Resources#PROCESS_DEFINITION} OR
   *   <li>The user has no {@link HistoricProcessInstancePermissions#READ} permission on
   *       {@link Resources#HISTORIC_PROCESS_INSTANCE} ({@code enableHistoricInstancePermissions} in
   *       {@link ProcessEngineConfigurationImpl} must be set to {@code true})
   *
   * */
  HistoricActivityInstanceQuery createHistoricActivityInstanceQuery();

  /**
   * <p>Query for the number of historic activity instances aggregated by activities of a single
   * process definition.
   *
   * <p>The result of the query is empty when the user has no {@link Permissions#READ_HISTORY}
   * permission on {@link Resources#PROCESS_DEFINITION}
   */
  HistoricActivityStatisticsQuery createHistoricActivityStatisticsQuery(String processDefinitionId);

  /**
   * Query for the number of historic case activity instances aggregated by case activities of a single case definition.
   */
  HistoricCaseActivityStatisticsQuery createHistoricCaseActivityStatisticsQuery(String caseDefinitionId);

  /**
   * <p>Creates a new programmatic query to search for {@link HistoricTaskInstance}s.
   *
   * <p>The result of the query is empty in the following cases:
   * <ul>
   *   <li>The user has no {@link Permissions#READ_HISTORY} permission on
   *   {@link Resources#PROCESS_DEFINITION} OR
   *   <li>The user has no {@link HistoricProcessInstancePermissions#READ} permission on
   *       {@link Resources#HISTORIC_PROCESS_INSTANCE} ({@code enableHistoricInstancePermissions} in
   *       {@link ProcessEngineConfigurationImpl} must be set to {@code true}) OR
   *   <li>The user has no {@link HistoricTaskPermissions#READ} permission on
   *       {@link Resources#HISTORIC_TASK} ({@code enableHistoricInstancePermissions} in
   *       {@link ProcessEngineConfigurationImpl} must be set to {@code true})
   * */
  HistoricTaskInstanceQuery createHistoricTaskInstanceQuery();

  /**
   * <p>Creates a new programmatic query to search for {@link HistoricDetail}s.
   *
   * <p>The result of the query is empty in the following cases:
   * <ul>
   *   <li>The user has no {@link Permissions#READ_HISTORY} permission on
   *       {@link Resources#PROCESS_DEFINITION} OR
   *   <li>The user has no {@link HistoricTaskPermissions#READ} permission on
   *       {@link Resources#HISTORIC_TASK} ({@code enableHistoricInstancePermissions} in
   *       {@link ProcessEngineConfigurationImpl} must be set to {@code true}) OR
   *   <li>The user has no {@link HistoricProcessInstancePermissions#READ} permission on
   *       {@link Resources#HISTORIC_PROCESS_INSTANCE} ({@code enableHistoricInstancePermissions} in
   *       {@link ProcessEngineConfigurationImpl} must be set to {@code true}) OR
   *   <li>The user has no {@link ProcessDefinitionPermissions#READ_HISTORY_VARIABLE} permission on
   *       {@link Resources#PROCESS_DEFINITION}
   *       ({@link ProcessEngineConfigurationImpl#enforceSpecificVariablePermission} must be set to
   *       {@code true}) OR
   *   <li>The user has no {@link HistoricTaskPermissions#READ_VARIABLE} permission on
   *       {@link Resources#HISTORIC_TASK} ({@code enforceSpecificVariablePermission} and
   *       {@code enableHistoricInstancePermissions}
   *       in {@link ProcessEngineConfigurationImpl} must be set to {@code true})
   * */
  HistoricDetailQuery createHistoricDetailQuery();

  /**
   * <p>Creates a new programmatic query to search for {@link HistoricVariableInstance}s.
   *
   * <p>The result of the query is empty in the following cases:
   * <ul>
   *   <li>The user has no {@link Permissions#READ_HISTORY} permission on
   *       {@link Resources#PROCESS_DEFINITION} OR
   *   <li>The user has no {@link HistoricTaskPermissions#READ} permission on
   *       {@link Resources#HISTORIC_TASK} ({@code enableHistoricInstancePermissions} in
   *       {@link ProcessEngineConfigurationImpl} must be set to {@code true}) OR
   *   <li>The user has no {@link HistoricProcessInstancePermissions#READ} permission on
   *       {@link Resources#HISTORIC_PROCESS_INSTANCE} ({@code enableHistoricInstancePermissions} in
   *       {@link ProcessEngineConfigurationImpl} must be set to {@code true}) OR
   *   <li>The user has no {@link ProcessDefinitionPermissions#READ_HISTORY_VARIABLE} permission on
   *       {@link Resources#PROCESS_DEFINITION}
   *       ({@link ProcessEngineConfigurationImpl#enforceSpecificVariablePermission} must be set to
   *       {@code true}) OR
   *   <li>The user has no {@link HistoricTaskPermissions#READ_VARIABLE} permission on
   *       {@link Resources#HISTORIC_TASK} ({@code enforceSpecificVariablePermission} and
   *       {@code enableHistoricInstancePermissions}
   *       in {@link ProcessEngineConfigurationImpl} must be set to {@code true})
   * */
  HistoricVariableInstanceQuery createHistoricVariableInstanceQuery();

  /** <p>Creates a new programmatic query to search for {@link UserOperationLogEntry} instances.
   *
   * <p>The result of the query is empty in the following cases:
   * <ul>
   *   <li>The user has no {@link Permissions#READ_HISTORY} permission on
   *   {@link Resources#PROCESS_DEFINITION} OR
   *   <li>The user has no {@link HistoricProcessInstancePermissions#READ} permission on
   *       {@link Resources#HISTORIC_PROCESS_INSTANCE} ({@code enableHistoricInstancePermissions} in
   *       {@link ProcessEngineConfigurationImpl} must be set to {@code true}) OR
   *   <li>The user has no {@link HistoricTaskPermissions#READ} permission on
   *       {@link Resources#HISTORIC_TASK} ({@code enableHistoricInstancePermissions} in
   *       {@link ProcessEngineConfigurationImpl} must be set to {@code true})
   *
   * */
  UserOperationLogQuery createUserOperationLogQuery();

  /**
   * <p>Creates a new programmatic query to search for {@link HistoricIncident historic incidents}.
   *
   * <p>The result of the query is empty in the following cases:
   * <ul>
   *   <li>The user has no {@link Permissions#READ_HISTORY} permission on
   *   {@link Resources#PROCESS_DEFINITION} OR
   *   <li>The user has no {@link HistoricProcessInstancePermissions#READ} permission on
   *       {@link Resources#HISTORIC_PROCESS_INSTANCE} ({@code enableHistoricInstancePermissions} in
   *       {@link ProcessEngineConfigurationImpl} must be set to {@code true})
   * */
  HistoricIncidentQuery createHistoricIncidentQuery();

  /**
   * <p>Creates a new programmatic query to search for
   * {@link HistoricIdentityLinkLog historic identity links}.
   *
   * <p>The result of the query is empty in the following cases:
   * <ul>
   *   <li>The user has no {@link Permissions#READ_HISTORY} permission on
   *   {@link Resources#PROCESS_DEFINITION} OR
   *   <li>The user has no {@link HistoricTaskPermissions#READ} permission on
   *       {@link Resources#HISTORIC_TASK} ({@code enableHistoricInstancePermissions} in
   *       {@link ProcessEngineConfigurationImpl} must be set to {@code true})
   * */
  HistoricIdentityLinkLogQuery createHistoricIdentityLinkLogQuery();

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
   * Deletes historic process instance. All historic activities, historic task and
   * historic details (variable updates, form properties) are deleted as well.
   * Does not fail if a process instance was not found.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE_HISTORY} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void deleteHistoricProcessInstanceIfExists(String processInstanceId);

  /**
   * Deletes historic process instances. All historic activities, historic task and
   * historic details (variable updates, form properties) are deleted as well.
   *
   * @throws BadUserRequestException
   *          when no process instances are found with the given ids or ids are null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE_HISTORY} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void deleteHistoricProcessInstances(List<String> processInstanceIds);

  /**
   * Deletes historic process instances. All historic activities, historic task and
   * historic details (variable updates, form properties) are deleted as well. Does not
   * fail if a process instance was not found.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE_HISTORY} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void deleteHistoricProcessInstancesIfExists(List<String> processInstanceIds);

  /**
   * Deletes historic process instances and all related historic data in bulk manner. DELETE SQL statement will be created for each entity type. They will have list
   * of given process instance ids in IN clause. Therefore, DB limitation for number of values in IN clause must be taken into account.
   *
   * @param processInstanceIds list of process instance ids for removal
   *
   * @throws BadUserRequestException
   *          when no process instances are found with the given ids or ids are null or when some of the process instances are not finished yet
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE_HISTORY} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void deleteHistoricProcessInstancesBulk(List<String> processInstanceIds);

  /**
   * Schedules history cleanup job at batch window start time. The job will delete historic data for
   * finished process, decision and case instances, and batch operations taking into account {@link ProcessDefinition#getHistoryTimeToLive()},
   * {@link DecisionDefinition#getHistoryTimeToLive()}, {@link CaseDefinition#getHistoryTimeToLive()}, {@link ProcessEngineConfigurationImpl#getBatchOperationHistoryTimeToLive()}
   * and {@link ProcessEngineConfigurationImpl#getBatchOperationsForHistoryCleanup()} values.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE_HISTORY} permission on {@link Resources#PROCESS_DEFINITION}
   * @return history cleanup job. NB! As of v. 7.9.0, method does not guarantee to return a job. Use {@link #findHistoryCleanupJobs()} instead.
   */
  Job cleanUpHistoryAsync();

  /**
   * Schedules history cleanup job at batch window start time. The job will delete historic data for
   * finished process, decision and case instances, and batch operations taking into account {@link ProcessDefinition#getHistoryTimeToLive()},
   * {@link DecisionDefinition#getHistoryTimeToLive()}, {@link CaseDefinition#getHistoryTimeToLive()}, {@link ProcessEngineConfigurationImpl#getBatchOperationHistoryTimeToLive()}
   * and {@link ProcessEngineConfigurationImpl#getBatchOperationsForHistoryCleanup()} values.
   *
   * @param immediatelyDue must be true if cleanup must be scheduled at once, otherwise is will be scheduled according to configured batch window
   * @throws AuthorizationException
   *      If the user has no {@link Permissions#DELETE_HISTORY} permission on {@link Resources#PROCESS_DEFINITION}
   * @return history cleanup job. Job id can be used to check job logs, incident etc.
   *
   */
  Job cleanUpHistoryAsync(boolean immediatelyDue);

  /**
   * Finds history cleanup job, if present.
   * @deprecated As of v. 7.9.0, because there can be more than one history cleanup job at once, use {@link #findHistoryCleanupJobs} instead.
   * @return history cleanup job entity
   */
  @Deprecated
  Job findHistoryCleanupJob();

  /**
   * Finds history cleanup jobs if present.
   * @return history cleanup job entities
   */
  List<Job> findHistoryCleanupJobs();

  /**
   * Deletes historic process instances asynchronously. All historic activities, historic task and
   * historic details (variable updates, form properties) are deleted as well.
   *
   * @throws BadUserRequestException
   *          when no process instances is found with the given ids or ids are null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} or
   *          {@link BatchPermissions#CREATE_BATCH_DELETE_FINISHED_PROCESS_INSTANCES} permission on {@link Resources#BATCH}.
   */
  Batch deleteHistoricProcessInstancesAsync(List<String> processInstanceIds, String deleteReason);

  /**
   * Deletes historic process instances asynchronously based on query. All historic activities, historic task and
   * historic details (variable updates, form properties) are deleted as well.
   *
   * @throws BadUserRequestException
   *          when no process instances is found with the given ids or ids are null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} or
   *          {@link BatchPermissions#CREATE_BATCH_DELETE_FINISHED_PROCESS_INSTANCES} permission on {@link Resources#BATCH}.
   */
  Batch deleteHistoricProcessInstancesAsync(HistoricProcessInstanceQuery query, String deleteReason);

  /**
   * Deletes historic process instances asynchronously based on query and a list of process instances. Query result and
   * list of ids will be merged.
   * All historic activities, historic task and historic details (variable updates, form properties) are deleted as well.
   *
   * @throws BadUserRequestException
   *          when no process instances is found with the given ids or ids are null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} or
   *          {@link BatchPermissions#CREATE_BATCH_DELETE_FINISHED_PROCESS_INSTANCES} permission on {@link Resources#BATCH}.
   */
  Batch deleteHistoricProcessInstancesAsync(List<String> processInstanceIds, HistoricProcessInstanceQuery query, String deleteReason);

  /**
   * Deletes a user operation log entry. Does not cascade to any related entities.
   *
   * @throws AuthorizationException
   *           For entries related to process definition keys: If the user has
   *           neither {@link Permissions#DELETE_HISTORY} permission on
   *           {@link Resources#PROCESS_DEFINITION} nor
   *           {@link UserOperationLogCategoryPermissions#DELETE} permission on
   *           {@link Resources#OPERATION_LOG_CATEGORY}. For entries not related
   *           to process definition keys: If the user has no
   *           {@link UserOperationLogCategoryPermissions#DELETE} permission on
   *           {@link Resources#OPERATION_LOG_CATEGORY}.
   */
  void deleteUserOperationLogEntry(String entryId);

  /**
   * Deletes historic case instance. All historic case activities, historic task and
   * historic details are deleted as well.
   */
  void deleteHistoricCaseInstance(String caseInstanceId);

  /**
   * Deletes historic case instances and all related historic data in bulk manner. DELETE SQL statement will be created for each entity type. They will have list
   * of given case instance ids in IN clause. Therefore, DB limitation for number of values in IN clause must be taken into account.
   *
   * @param caseInstanceIds list of case instance ids for removal
   */
  void deleteHistoricCaseInstancesBulk(List<String> caseInstanceIds);

  /**
   * Deletes historic decision instances of a decision definition. All historic
   * decision inputs and outputs are deleted as well.
   *
   * @deprecated Note that this method name is not expressive enough, because it is also possible to delete the historic
   * decision instance by the instance id. Therefore use {@link #deleteHistoricDecisionInstanceByDefinitionId} instead
   * to delete the historic decision instance by the definition id.
   *
   * @param decisionDefinitionId
   *          the id of the decision definition
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE_HISTORY} permission on {@link Resources#DECISION_DEFINITION}.
   */
  @Deprecated
  void deleteHistoricDecisionInstance(String decisionDefinitionId);

  /**
   * Deletes decision instances and all related historic data in bulk manner. DELETE SQL statement will be created for each entity type. They will have list
   * of given decision instance ids in IN clause. Therefore, DB limitation for number of values in IN clause must be taken into account.
   *
   * @param decisionInstanceIds list of decision instance ids for removal.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE_HISTORY} permission on {@link Resources#DECISION_DEFINITION}.
   */
  void deleteHistoricDecisionInstancesBulk(List<String> decisionInstanceIds);

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
  void deleteHistoricDecisionInstanceByDefinitionId(String decisionDefinitionId);


  /**
   * Deletes historic decision instances by its id. All historic
   * decision inputs and outputs are deleted as well.
   *
   * @param historicDecisionInstanceId
   *          the id of the historic decision instance
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE_HISTORY} permission on {@link Resources#DECISION_DEFINITION}.
   */
  void deleteHistoricDecisionInstanceByInstanceId(String historicDecisionInstanceId);

  /**
   * Deletes historic decision instances asynchronously based on a list of decision instances.
   *
   * @throws BadUserRequestException
   *          when no decision instances are found with the given ids or ids are null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} or
   *          {@link BatchPermissions#CREATE_BATCH_DELETE_DECISION_INSTANCES} permission on {@link Resources#BATCH}.
   */
  Batch deleteHistoricDecisionInstancesAsync(List<String> decisionInstanceIds, String deleteReason);

  /**
   * Deletes historic decision instances asynchronously based on query of decision instances.
   *
   * @throws BadUserRequestException
   *          when no decision instances are found with the given ids or ids are null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} or
   *          {@link BatchPermissions#CREATE_BATCH_DELETE_DECISION_INSTANCES} permission on {@link Resources#BATCH}.
   */
  Batch deleteHistoricDecisionInstancesAsync(HistoricDecisionInstanceQuery query, String deleteReason);

  /**
   * Deletes historic decision instances asynchronously based on query and a list of decision instances, whereby query result and
   * list of ids will be merged.
   *
   * @throws BadUserRequestException
   *          when no decision instances are found with the given ids or ids are null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} or
   *          {@link BatchPermissions#CREATE_BATCH_DELETE_DECISION_INSTANCES} permission on {@link Resources#BATCH}.
   */
  Batch deleteHistoricDecisionInstancesAsync(List<String> decisionInstanceIds, HistoricDecisionInstanceQuery query, String deleteReason);

  /**
   * Deletes a historic variable instance by its id. All related historic
   * details (variable updates, form properties) are deleted as well.
   *
   * @param variableInstanceId
   *          the id of the variable instance
   * @throws BadUserRequestException
   *           when the historic variable instance is not found by the given id
   *           or if id is null
   * @throws AuthorizationException
   *           If the variable instance has a process definition key and
   *           the user has no {@link Permissions#DELETE_HISTORY} permission on
   *           {@link Resources#PROCESS_DEFINITION}.
   */
  void deleteHistoricVariableInstance(String variableInstanceId);

  /**
   * Deletes all historic variables and historic details (variable updates, form properties) of a process instance.
   *
   * @param processInstanceId
   *          the id of the process instance
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE_HISTORY} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void deleteHistoricVariableInstancesByProcessInstanceId(String processInstanceId);

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
   * creates a native query to search for {@link HistoricVariableInstance}s via SQL
   */
  NativeHistoricVariableInstanceQuery createNativeHistoricVariableInstanceQuery();

  /**
   * <p>Creates a new programmatic query to search for {@link HistoricJobLog historic job logs}.
   *
   * <p>The result of the query is empty in the following cases:
   * <ul>
   *   <li>The user has no {@link Permissions#READ_HISTORY} permission on
   *   {@link Resources#PROCESS_DEFINITION} OR
   *   <li>The user has no {@link HistoricProcessInstancePermissions#READ} permission on
   *       {@link Resources#HISTORIC_PROCESS_INSTANCE} ({@code enableHistoricInstancePermissions} in
   *       {@link ProcessEngineConfigurationImpl} must be set to {@code true})
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

  /**
   * <p>Creates a new programmatic query to create a historic task instance report.
   *
   * <p>Subsequent builder methods throw {@link AuthorizationException} when the user has no
   * {@link Permissions#READ_HISTORY} permission on any {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 7.6
   */
  HistoricTaskInstanceReport createHistoricTaskInstanceReport();

  /**
   * Creates a new programmatic query to create a cleanable historic process instance report.
   *
   * @since 7.8
   */
  CleanableHistoricProcessInstanceReport createCleanableHistoricProcessInstanceReport();

  /**
   * Creates a new programmatic query to create a cleanable historic decision instance report.
   *
   * @since 7.8
   */
  CleanableHistoricDecisionInstanceReport createCleanableHistoricDecisionInstanceReport();

  /**
   * Creates a new programmatic query to create a cleanable historic case instance report.
   *
   * @since 7.8
   */
  CleanableHistoricCaseInstanceReport createCleanableHistoricCaseInstanceReport();

  /**
   * Creates a new programmatic query to create a cleanable historic batch report.
   *
   * @since 7.8
   */
  CleanableHistoricBatchReport createCleanableHistoricBatchReport();

  /**
   * Creates a query to search for {@link org.camunda.bpm.engine.batch.history.HistoricBatch} instances.
   *
   * @since 7.5
   */
  HistoricBatchQuery createHistoricBatchQuery();

  /**
   * Deletes a historic batch instance. All corresponding historic job logs are deleted as well;
   *
   * @since 7.5
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE} permission on {@link Resources#BATCH}
   */
  void deleteHistoricBatch(String id);


  /**
   * Query for the statistics of DRD evaluation.
   *
   * @param decisionRequirementsDefinitionId - id of decision requirement definition
   * @since 7.6
   */
  HistoricDecisionInstanceStatisticsQuery createHistoricDecisionInstanceStatisticsQuery(String decisionRequirementsDefinitionId);

  /**
   * <p>Creates a new programmatic query to search for
   * {@link HistoricExternalTaskLog historic external task logs}.
   *
   * <p>The result of the query is empty in the following cases:
   * <ul>
   *   <li>The user has no {@link Permissions#READ_HISTORY} permission on
   *   {@link Resources#PROCESS_DEFINITION} OR
   *   <li>The user has no {@link HistoricProcessInstancePermissions#READ} permission on
   *       {@link Resources#HISTORIC_PROCESS_INSTANCE} ({@code enableHistoricInstancePermissions} in
   *       {@link ProcessEngineConfigurationImpl} must be set to {@code true})
   *
   * @since 7.7
   */
  HistoricExternalTaskLogQuery createHistoricExternalTaskLogQuery();

  /**
   * Returns the full error details that occurs when the
   * historic external task log with the given id was last executed. Returns null
   * when the historic external task log contains no error details.
   *
   * @param historicExternalTaskLogId id of the historic external task log, cannot be null.
   * @throws ProcessEngineException when no historic external task log exists with the given id.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ_HISTORY} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 7.7
   */
  String getHistoricExternalTaskLogErrorDetails(String historicExternalTaskLogId);

  /**
   * <p>Set a removal time to historic process instances and
   * all associated historic entities using a fluent builder.
   *
   * <p>Historic process instances can be specified by passing a query to
   * {@link SetRemovalTimeToHistoricProcessInstancesBuilder#byQuery(HistoricProcessInstanceQuery)}.
   *
   * <p>An absolute time can be specified via
   * {@link SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder#absoluteRemovalTime(Date)}.
   * Pass {@code null} to clear the removal time.
   *
   * <p>As an alternative, the removal time can also be calculated via
   * {@link SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder#calculatedRemovalTime()}
   * based on the configured time to live values.
   *
   * <p>To additionally take those historic process instances into account that are part of
   * a hierarchy, enable the flag
   * {@link SetRemovalTimeToHistoricProcessInstancesBuilder#hierarchical()}
   *
   * <p>To create the batch and complete the configuration chain, call
   * {@link SetRemovalTimeToHistoricProcessInstancesBuilder#executeAsync()}.
   *
   * @since 7.11
   */
  SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder setRemovalTimeToHistoricProcessInstances();

  /**
   * <p>Set a removal time to historic decision instances and
   * all associated historic entities using a fluent builder.
   *
   * <p>Historic decision instances can be specified by passing a query to
   * {@link SetRemovalTimeToHistoricDecisionInstancesBuilder#byQuery(HistoricDecisionInstanceQuery)}.
   *
   * <p>An absolute time can be specified via
   * {@link SetRemovalTimeSelectModeForHistoricDecisionInstancesBuilder#absoluteRemovalTime(Date)}.
   * Pass {@code null} to clear the removal time.
   *
   * <p>As an alternative, the removal time can also be calculated via
   * {@link SetRemovalTimeSelectModeForHistoricDecisionInstancesBuilder#calculatedRemovalTime()}
   * based on the configured time to live values.
   *
   * <p>To additionally take those historic decision instances into account that are part of
   * a hierarchy, enable the flag
   * {@link SetRemovalTimeToHistoricProcessInstancesBuilder#hierarchical()}
   *
   * <p>To create the batch and complete the configuration chain, call
   * {@link SetRemovalTimeToHistoricDecisionInstancesBuilder#executeAsync()}.
   *
   * @since 7.11
   */
  SetRemovalTimeSelectModeForHistoricDecisionInstancesBuilder setRemovalTimeToHistoricDecisionInstances();

  /**
   * <p>Set a removal time to historic batches and all
   * associated historic entities using a fluent builder.
   *
   * <p>Historic batches can be specified by passing a query to
   * {@link SetRemovalTimeToHistoricBatchesBuilder#byQuery(HistoricBatchQuery)}.
   *
   * <p>An absolute time can be specified via
   * {@link SetRemovalTimeSelectModeForHistoricBatchesBuilder#absoluteRemovalTime(Date)}.
   * Pass {@code null} to clear the removal time.
   *
   * <p>As an alternative, the removal time can also be calculated via
   * {@link SetRemovalTimeSelectModeForHistoricBatchesBuilder#calculatedRemovalTime()}
   * based on the configured time to live values.
   *
   * <p>To create the batch and complete the configuration chain, call
   * {@link SetRemovalTimeToHistoricBatchesBuilder#executeAsync()}.
   *
   * @since 7.11
   */
  SetRemovalTimeSelectModeForHistoricBatchesBuilder setRemovalTimeToHistoricBatches();

  /**
   * <p>Set an annotation to user operation log entries.</p>
   *
   * @throws NotValidException when operation id is {@code null}
   * @throws BadUserRequestException when no user operation could be found
   * @throws AuthorizationException
   * <ul>
   *   <li>
   *     when no {@link ProcessDefinitionPermissions#UPDATE_HISTORY} permission
   *     is granted on {@link Resources#PROCESS_DEFINITION}</li>
   *   <li>
   *     or when no {@link UserOperationLogCategoryPermissions#UPDATE} permission
   *     is granted on {@link Resources#OPERATION_LOG_CATEGORY}
   *   </li>
   * </ul>
   *
   * @param operationId of the user operation log entries that are updated
   * @param annotation that is set to the user operation log entries
   *
   * @since 7.12
   */
  void setAnnotationForOperationLogById(String operationId, String annotation);

  /**
   * <p>Clear the annotation for user operation log entries.</p>
   *
   * @throws NotValidException when operation id is {@code null}
   * @throws BadUserRequestException when no user operation could be found
   * @throws AuthorizationException
   * <ul>
   *   <li>
   *     when no {@link ProcessDefinitionPermissions#UPDATE_HISTORY} permission
   *     is granted on {@link Resources#PROCESS_DEFINITION}</li>
   *   <li>
   *     or when no {@link UserOperationLogCategoryPermissions#UPDATE} permission
   *     is granted on {@link Resources#OPERATION_LOG_CATEGORY}
   *   </li>
   * </ul>
   *
   * @param operationId of the user operation log entries that are updated
   *
   * @since 7.12
   */
  void clearAnnotationForOperationLogById(String operationId);

}
