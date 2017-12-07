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
package org.camunda.bpm.engine;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.BatchQuery;
import org.camunda.bpm.engine.batch.BatchStatisticsQuery;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.management.ActivityStatisticsQuery;
import org.camunda.bpm.engine.management.DeploymentStatisticsQuery;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.management.MetricsQuery;
import org.camunda.bpm.engine.management.ProcessDefinitionStatisticsQuery;
import org.camunda.bpm.engine.management.TableMetaData;
import org.camunda.bpm.engine.management.TablePage;
import org.camunda.bpm.engine.management.TablePageQuery;
import org.camunda.bpm.engine.management.UpdateJobDefinitionSuspensionStateBuilder;
import org.camunda.bpm.engine.management.UpdateJobDefinitionSuspensionStateSelectBuilder;
import org.camunda.bpm.engine.management.UpdateJobSuspensionStateBuilder;
import org.camunda.bpm.engine.management.UpdateJobSuspensionStateSelectBuilder;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.Task;



/**
 * Service for admin and maintenance operations on the process engine.
 *
 * These operations will typically not be used in a workflow driven application,
 * but are used in for example the operational console.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Falko Menge
 * @author Thorben Lindhauer
 */
public interface ManagementService {

  /**
   * Activate a deployment for a given ProcessApplication. The effect of this
   * method is twofold:
   * <ol>
   *   <li>The process engine will execute atomic operations within the context of
   *       that ProcessApplication</li>
   *   <li>The job executor will start acquiring jobs from that deployment</li>
   * </ol>
   *
   * @param deploymentId
   *          the Id of the deployment to activate
   * @param reference
   *          the reference to the process application
   * @return a new {@link ProcessApplicationRegistration}
   *
   * @throws AuthorizationException
   *          If the user is not a member of the group {@link Groups#CAMUNDA_ADMIN}.
   */
  ProcessApplicationRegistration registerProcessApplication(String deploymentId, ProcessApplicationReference reference);

  /**
   * Deactivate a deployment for a given ProcessApplication. This removes the association
   * between the process engine and the process application and optionally removes the associated
   * process definitions from the cache.
   *
   * @param deploymentId
   *          the Id of the deployment to deactivate
   * @param removeProcessDefinitionsFromCache
   *          indicates whether the process definitions should be removed from the deployment cache
   *
   * @throws AuthorizationException
   *          If the user is not a member of the group {@link Groups#CAMUNDA_ADMIN}.
   */
  void unregisterProcessApplication(String deploymentId, boolean removeProcessDefinitionsFromCache);

  /**
   * Deactivate a deployment for a given ProcessApplication. This removes the association
   * between the process engine and the process application and optionally removes the associated
   * process definitions from the cache.
   *
   * @param deploymentIds
   *          the Ids of the deployments to deactivate
   * @param removeProcessDefinitionsFromCache
   *          indicates whether the process definitions should be removed from the deployment cache
   *
   * @throws AuthorizationException
   *          If the user is not a member of the group {@link Groups#CAMUNDA_ADMIN}.
   */
  void unregisterProcessApplication(Set<String> deploymentIds, boolean removeProcessDefinitionsFromCache);

  /**
   * @return the name of the process application that is currently registered for
   *         the given deployment or 'null' if no process application is
   *         currently registered.
   *
   * @throws AuthorizationException
   *          If the user is not a member of the group {@link Groups#CAMUNDA_ADMIN}.
   */
  String getProcessApplicationForDeployment(String deploymentId);

  /**
   * Get the mapping containing {table name, row count} entries of the database schema.
   *
   * @throws AuthorizationException
   *          If the user is not a member of the group {@link Groups#CAMUNDA_ADMIN}.
   */
  Map<String, Long> getTableCount();

  /**
   * Gets the table name (including any configured prefix) for an entity like {@link Task},
   * {@link Execution} or the like.
   *
   * @throws AuthorizationException
   *          If the user is not a member of the group {@link Groups#CAMUNDA_ADMIN}.
   */
  String getTableName(Class<?> entityClass);

  /**
   * Gets the metadata (column names, column types, etc.) of a certain table.
   * Returns null when no table exists with the given name.
   *
   * @throws AuthorizationException
   *          If the user is not a member of the group {@link Groups#CAMUNDA_ADMIN}.
   */
  TableMetaData getTableMetaData(String tableName);

  /**
   * Creates a {@link TablePageQuery} that can be used to fetch {@link TablePage}
   * containing specific sections of table row data.
   *
   * @throws AuthorizationException
   *          If the user is not a member of the group {@link Groups#CAMUNDA_ADMIN}.
   */
  TablePageQuery createTablePageQuery();

  /**
   * Returns a new JobQuery implementation, that can be used
   * to dynamically query the jobs.
   */
  JobQuery createJobQuery();

  /**
   * Returns a new {@link JobDefinitionQuery} implementation, that can be used
   * to dynamically query the job definitions.
   */
  JobDefinitionQuery createJobDefinitionQuery();

  /**
   * Forced synchronous execution of a job (eg. for administration or testing)
   * The job will be executed, even if the process definition and/or the process instance
   * is in suspended state.
   *
   * @param jobId id of the job to execute, cannot be null.
   *
   * @throws ProcessEngineException
   *          When there is no job with the given id.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void executeJob(String jobId);

  /**
   * Delete the job with the provided id.
   *
   * @param jobId id of the job to execute, cannot be null.
   *
   * @throws ProcessEngineException
   *          When there is no job with the given id.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void deleteJob(String jobId);

  /**
   * <p>Activates the {@link JobDefinition} with the given id immediately.</p>
   *
   * <p>
   * <strong>Note:</strong> All {@link Job}s of the provided job definition
   * will be <strong>not</strong> activated.
   * </p>
   *
   * <p>Note: for more complex activate commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          If the job definition id is equal null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @see #activateJobById(String)
   * @see #activateJobByJobDefinitionId(String)
   */
  void activateJobDefinitionById(String jobDefinitionId);

  /**
   * <p>Activates all {@link JobDefinition}s of the provided process definition id immediately.</p>
   *
   * <p>
   * <strong>Note:</strong> All {@link Job}s of the provided job definition
   * will be <strong>not</strong> activated.
   * </p>
   *
   * <p>Note: for more complex activate commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          If the process definition id is equal null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @see #activateJobByProcessDefinitionId(String)
   */
  void activateJobDefinitionByProcessDefinitionId(String processDefinitionId);

  /**
   * <p>Activates all {@link JobDefinition}s of the provided process definition key immediately.</p>
   *
   * <p>
   * <strong>Note:</strong> All {@link Job}s of the provided job definition
   * will be <strong>not</strong> activated.
   * </p>
   *
   * <p>Note: for more complex activate commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          If the process definition key is equal null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @see #activateJobByProcessDefinitionKey(String)
   */
  void activateJobDefinitionByProcessDefinitionKey(String processDefinitionKey);

  /**
   * <p>Activates the {@link JobDefinition} with the given id immediately.</p>
   *
   * <p>Note: for more complex activate commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * @param activateJobs If true, all the {@link Job}s of the provided job definition
   *                     will be activated too.
   *
   * @throws ProcessEngineException
   *          If the job definition id is equal null.
   * @throws AuthorizationException thrown if the current user does not possess
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *
   *   If <code>activateJobs</code> is <code>true</code>, the user must further possess one of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *     <li>{@link Permissions#UPDATE} on any {@link Resources#PROCESS_INSTANCE}</li>
   *   </ul>
   *
   * @see #activateJobById(String)
   * @see #activateJobByJobDefinitionId(String)
   */
  void activateJobDefinitionById(String jobDefinitionId, boolean activateJobs);

  /**
   * <p>Activates all {@link JobDefinition}s of the provided process definition id immediately.</p>
   *
   * <p>Note: for more complex activate commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * @param activateJobs If true, all the {@link Job}s of the provided job definition
   *                     will be activated too.
   *
   * @throws ProcessEngineException
   *          If the process definition id is equal null.
   * @throws AuthorizationException thrown if the current user does not possess
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *
   *   If <code>activateJobs</code> is <code>true</code>, the user must further possess one of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *     <li>{@link Permissions#UPDATE} on any {@link Resources#PROCESS_INSTANCE}</li>
   *   </ul>
   *
   * @see #activateJobByProcessDefinitionId(String)
   */
  void activateJobDefinitionByProcessDefinitionId(String processDefinitionId, boolean activateJobs);

  /**
   * <p>Activates all {@link JobDefinition}s of the provided process definition key immediately.</p>
   *
   * <p>Note: for more complex activate commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * @param activateJobs If true, all the {@link Job}s of the provided job definition
   *                     will be activated too.
   *
   * @throws ProcessEngineException
   *          If the process definition key is equal null.
   * @throws AuthorizationException thrown if the current user does not possess
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *
   *   If <code>activateJobs</code> is <code>true</code>, the user must further possess one of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *     <li>{@link Permissions#UPDATE} on any {@link Resources#PROCESS_INSTANCE}</li>
   *   </ul>
   *
   * @see #activateJobByProcessDefinitionKey(String)
   */
  void activateJobDefinitionByProcessDefinitionKey(String processDefinitionKey, boolean activateJobs);

  /**
   * Activates the {@link JobDefinition} with the given id.
   *
   * <p>Note: for more complex activate commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * @param activateJobs If true, all the {@link Job}s of the provided job definition
   *                     will be activated too.
   *
   * @param activationDate The date on which the job definition will be activated. If null, the
   *                       job definition is activated immediately.
   *                       Note: The {@link JobExecutor} needs to be active to use this!
   *
   * @throws ProcessEngineException
   *          If the job definition id is equal null.
   * @throws AuthorizationException thrown if the current user does not possess
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *
   *   If <code>activateJobs</code> is <code>true</code>, the user must further possess one of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *     <li>{@link Permissions#UPDATE} on any {@link Resources#PROCESS_INSTANCE}</li>
   *   </ul>
   *
   * @see #activateJobById(String)
   * @see #activateJobByJobDefinitionId(String)
   */
  void activateJobDefinitionById(String jobDefinitionId, boolean activateJobs, Date activationDate);

  /**
   * <p>Activates all {@link JobDefinition}s of the provided process definition id.</p>
   *
   * <p>Note: for more complex activate commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * @param activateJobs If true, all the {@link Job}s of the provided job definition
   *                     will be activated too.
   *
   * @param activationDate The date on which the job definition will be activated. If null, the
   *                       job definition is activated immediately.
   *                       Note: The {@link JobExecutor} needs to be active to use this!
   *
   * @throws ProcessEngineException
   *          If the process definition id is equal null.
   * @throws AuthorizationException thrown if the current user does not possess
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *
   *   If <code>activateJobs</code> is <code>true</code>, the user must further possess one of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *     <li>{@link Permissions#UPDATE} on any {@link Resources#PROCESS_INSTANCE}</li>
   *   </ul>
   *
   * @see #activateJobByProcessDefinitionId(String)
   */
  void activateJobDefinitionByProcessDefinitionId(String processDefinitionId, boolean activateJobs, Date activationDate);

  /**
   * <p>Activates all {@link JobDefinition}s of the provided process definition key.</p>
   *
   * <p>Note: for more complex activate commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * @param activateJobs If true, all the {@link Job}s of the provided job definition
   *                     will be activated too.
   *
   * @param activationDate The date on which the job definition will be activated. If null, the
   *                       job definition is activated immediately.
   *                       Note: The {@link JobExecutor} needs to be active to use this!
   *
   * @throws ProcessEngineException
   *          If the process definition key is equal null.
   * @throws AuthorizationException thrown if the current user does not possess
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *
   *   If <code>activateJobs</code> is <code>true</code>, the user must further possess one of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *     <li>{@link Permissions#UPDATE} on any {@link Resources#PROCESS_INSTANCE}</li>
   *   </ul>
   *
   * @see #activateJobByProcessDefinitionKey(String)
   */
  void activateJobDefinitionByProcessDefinitionKey(String processDefinitionKey, boolean activateJobs, Date activationDate);

  /**
   * <p>Suspends the {@link JobDefinition} with the given id immediately.</p>
   *
   * <p>Note: for more complex suspend commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * <p>
   * <strong>Note:</strong> All {@link Job}s of the provided job definition
   * will be <strong>not</strong> suspended.
   * </p>
   *
   * @throws ProcessEngineException
   *          If no such job definition can be found.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @see #suspendJobById(String)
   * @see #suspendJobByJobDefinitionId(String)
   */
  void suspendJobDefinitionById(String jobDefinitionId);

  /**
   * <p>Suspends all {@link JobDefinition} of the provided process definition id immediately.</p>
   *
   * <p>Note: for more complex suspend commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * <p>
   * <strong>Note:</strong> All {@link Job}s of the provided job definition
   * will be <strong>not</strong> suspended.
   * </p>
   *
   * @throws ProcessEngineException
   *          If the process definition id is equal null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @see #suspendJobByProcessDefinitionId(String)
   */
  void suspendJobDefinitionByProcessDefinitionId(String processDefinitionId);

  /**
   * <p>Suspends all {@link JobDefinition} of the provided process definition key immediately.</p>
   *
   * <p>Note: for more complex suspend commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * <p>
   * <strong>Note:</strong> All {@link Job}s of the provided job definition
   * will be <strong>not</strong> suspended.
   * </p>
   *
   * @throws ProcessEngineException
   *          If the process definition key is equal null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @see #suspendJobByProcessDefinitionKey(String)
   */
  void suspendJobDefinitionByProcessDefinitionKey(String processDefinitionKey);

  /**
   * Suspends the {@link JobDefinition} with the given id immediately.
   *
   * <p>Note: for more complex suspend commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * @param suspendJobs If true, all the {@link Job}s of the provided job definition
   *                     will be suspended too.
   *
   * @throws ProcessEngineException
   *          If the job definition id is equal null.
   * @throws AuthorizationException thrown if the current user does not possess
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *
   *   If <code>suspendJobs</code> is <code>true</code>, the user must further possess one of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *     <li>{@link Permissions#UPDATE} on any {@link Resources#PROCESS_INSTANCE}</li>
   *   </ul>
   *
   * @see #suspendJobById(String)
   * @see #suspendJobByJobDefinitionId(String)
   */
  void suspendJobDefinitionById(String jobDefinitionId, boolean suspendJobs);

  /**
   * Suspends all {@link JobDefinition}s of the provided process definition id immediately.
   *
   * <p>Note: for more complex suspend commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * @param suspendJobs If true, all the {@link Job}s of the provided job definition
   *                     will be suspended too.
   *
   * @throws ProcessEngineException
   *          If the process definition id is equal null.
   * @throws AuthorizationException thrown if the current user does not possess
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *
   *   If <code>suspendJobs</code> is <code>true</code>, the user must further possess one of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *     <li>{@link Permissions#UPDATE} on any {@link Resources#PROCESS_INSTANCE}</li>
   *   </ul>
   *
   * @see #suspendJobByProcessDefinitionId(String)
   */
  void suspendJobDefinitionByProcessDefinitionId(String processDefinitionId, boolean suspendJobs);

  /**
   * Suspends all {@link JobDefinition}s of the provided process definition key immediately.
   *
   * <p>Note: for more complex suspend commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * @param suspendJobs If true, all the {@link Job}s of the provided job definition
   *                     will be suspended too.
   *
   * @throws ProcessEngineException
   *          If the process definition key is equal null.
   * @throws AuthorizationException thrown if the current user does not possess
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *
   *   If <code>suspendJobs</code> is <code>true</code>, the user must further possess one of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *     <li>{@link Permissions#UPDATE} on any {@link Resources#PROCESS_INSTANCE}</li>
   *   </ul>
   *
   * @see #suspendJobByProcessDefinitionKey(String)
   */
  void suspendJobDefinitionByProcessDefinitionKey(String processDefinitionKey, boolean suspendJobs);

  /**
   * Suspends the {@link JobDefinition} with the given id.
   *
   * <p>Note: for more complex suspend commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * @param suspendJobs If true, all the {@link Job}s of the provided job definition
   *                     will be suspended too.
   *
   * @param suspensionDate The date on which the job definition will be suspended. If null, the
   *                       job definition is suspended immediately.
   *                       Note: The {@link JobExecutor} needs to be active to use this!
   *
   * @throws ProcessEngineException
   *          If the job definition id is equal null.
   * @throws AuthorizationException thrown if the current user does not possess
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *
   *   If <code>suspendJobs</code> is <code>true</code>, the user must further possess one of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *     <li>{@link Permissions#UPDATE} on any {@link Resources#PROCESS_INSTANCE}</li>
   *   </ul>
   *
   * @see #suspendJobById(String)
   * @see #suspendJobByJobDefinitionId(String)
   */
  void suspendJobDefinitionById(String jobDefinitionId, boolean suspendJobs, Date suspensionDate);

  /**
   * Suspends all {@link JobDefinition}s of the provided process definition id.
   *
   * <p>Note: for more complex suspend commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * @param suspendJobs If true, all the {@link Job}s of the provided job definition
   *                     will be suspended too.
   *
   * @param suspensionDate The date on which the job definition will be suspended. If null, the
   *                       job definition is suspended immediately.
   *                       Note: The {@link JobExecutor} needs to be active to use this!
   *
   * @throws ProcessEngineException
   *          If the process definition id is equal null.
   * @throws AuthorizationException thrown if the current user does not possess
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *
   *   If <code>suspendJobs</code> is <code>true</code>, the user must further possess one of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *     <li>{@link Permissions#UPDATE} on any {@link Resources#PROCESS_INSTANCE}</li>
   *   </ul>
   *
   * @see #suspendJobByProcessDefinitionId(String)
   */
  void suspendJobDefinitionByProcessDefinitionId(String processDefinitionId, boolean suspendJobs, Date suspensionDate);

  /**
   * Suspends all {@link JobDefinition}s of the provided process definition key.
   *
   * <p>Note: for more complex suspend commands use {@link #updateJobDefinitionSuspensionState()}.</p>
   *
   * @param suspendJobs If true, all the {@link Job}s of the provided job definition
   *                     will be suspended too.
   *
   * @param suspensionDate The date on which the job definition will be suspended. If null, the
   *                       job definition is suspended immediately.
   *                       Note: The {@link JobExecutor} needs to be active to use this!
   *
   * @throws ProcessEngineException
   *          If the process definition key is equal null.
   * @throws AuthorizationException thrown if the current user does not possess
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *
   *   If <code>suspendJobs</code> is <code>true</code>, the user must further possess one of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *     <li>{@link Permissions#UPDATE} on any {@link Resources#PROCESS_INSTANCE}</li>
   *   </ul>
   *
   * @see #suspendJobByProcessDefinitionKey(String)
   */
  void suspendJobDefinitionByProcessDefinitionKey(String processDefinitionKey, boolean suspendJobs, Date suspensionDate);

  /**
   * <p>Activates the {@link Job} with the given id.</p>
   *
   * <p>Note: for more complex activate commands use {@link #updateJobSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          If the job id is equal null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void activateJobById(String jobId);

  /**
   * <p>Activates all {@link Job}s of the provided job definition id.</p>
   *
   * <p>Note: for more complex activate commands use {@link #updateJobSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          If the job definition id is equal null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void activateJobByJobDefinitionId(String jobDefinitionId);

  /**
   * <p>Activates all {@link Job}s of the provided process instance id.</p>
   *
   * <p>Note: for more complex activate commands use {@link #updateJobSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          If the process instance id is equal null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void activateJobByProcessInstanceId(String processInstanceId);

  /**
   * <p>Activates all {@link Job}s of the provided process definition id.</p>
   *
   * <p>Note: for more complex activate commands use {@link #updateJobSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          If the process definition id is equal null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void activateJobByProcessDefinitionId(String processDefinitionId);

  /**
   * <p>Activates {@link Job}s of the provided process definition key.</p>
   *
   * <p>Note: for more complex activate commands use {@link #updateJobSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          If the process definition key is equal null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void activateJobByProcessDefinitionKey(String processDefinitionKey);

  /**
   * <p>Suspends the {@link Job} with the given id.</p>
   *
   * <p>Note: for more complex suspend commands use {@link #updateJobSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          If the job id is equal null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void suspendJobById(String jobId);

  /**
   * <p>Suspends all {@link Job}s of the provided job definition id.</p>
   *
   * <p>Note: for more complex suspend commands use {@link #updateJobSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          If the job definition id is equal null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void suspendJobByJobDefinitionId(String jobDefinitionId);

  /**
   * <p>Suspends all {@link Job}s of the provided process instance id.</p>
   *
   * <p>Note: for more complex suspend commands use {@link #updateJobSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          If the process instance id is equal null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void suspendJobByProcessInstanceId(String processInstanceId);

  /**
   * <p>Suspends all {@link Job}s of the provided process definition id.</p>
   *
   * <p>Note: for more complex suspend commands use {@link #updateJobSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          If the process definition id is equal null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void suspendJobByProcessDefinitionId(String processDefinitionId);

  /**
   * <p>Suspends {@link Job}s of the provided process definition key.</p>
   *
   * <p>Note: for more complex suspend commands use {@link #updateJobSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          If the process definition key is equal null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void suspendJobByProcessDefinitionKey(String processDefinitionKey);

  /**
   * Activate or suspend jobs using a fluent builder. Specify the jobs by
   * calling one of the <i>by</i> methods, like <i>byJobId</i>. To update the
   * suspension state call {@link UpdateJobSuspensionStateBuilder#activate()} or
   * {@link UpdateJobSuspensionStateBuilder#suspend()}.
   *
   * @return the builder to update the suspension state
   */
  UpdateJobSuspensionStateSelectBuilder updateJobSuspensionState();

  /**
   * Activate or suspend job definitions using a fluent builder. Specify the job
   * definitions by calling one of the <i>by</i> methods, like
   * <i>byJobDefinitionId</i>. To update the suspension state call
   * {@link UpdateJobDefinitionSuspensionStateBuilder#activate()} or
   * {@link UpdateJobDefinitionSuspensionStateBuilder#suspend()}.
   *
   * @return the builder to update the suspension state
   */
  UpdateJobDefinitionSuspensionStateSelectBuilder updateJobDefinitionSuspensionState();

  /**
   * Sets the number of retries that a job has left.
   *
   * Whenever the JobExecutor fails to execute a job, this value is decremented.
   * When it hits zero, the job is supposed to be dead and not retried again.
   * In that case, this method can be used to increase the number of retries.
   *
   * @param jobId id of the job to modify, cannot be null.
   * @param retries number of retries.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void setJobRetries(String jobId, int retries);

  /**
   * Sets the number of retries that jobs have left.
   *
   * Whenever the JobExecutor fails to execute a job, this value is decremented.
   * When it hits zero, the job is supposed to be dead and not retried again.
   * In that case, this method can be used to increase the number of retries.
   *
   * @param jobIds ids of the jobs to modify, cannot be null.
   * @param retries number of retries.
   *
   * @throws BadUserRequestException if jobIds is null
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void setJobRetries(List<String> jobIds, int retries);

  /**
   * Sets the number of retries that jobs have left asynchronously.
   *
   * Whenever the JobExecutor fails to execute a job, this value is decremented.
   * When it hits zero, the job is supposed to be dead and not retried again.
   * In that case, this method can be used to increase the number of retries.
   *
   * @param jobIds ids of the jobs to modify, cannot be null.
   * @param retries number of retries.
   *
   * @throws BadUserRequestException if jobIds is null
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} permission on {@link Resources#BATCH}.
   */
  Batch setJobRetriesAsync(List<String> jobIds, int retries);

  /**
   * Sets the number of retries that jobs have left asynchronously.
   *
   * Whenever the JobExecutor fails to execute a job, this value is decremented.
   * When it hits zero, the job is supposed to be dead and not retried again.
   * In that case, this method can be used to increase the number of retries.
   *
   * @param jobQuery query that identifies which jobs should be modified, cannot be null.
   * @param retries number of retries.
   *
   * @throws BadUserRequestException if jobQuery is null
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} permission on {@link Resources#BATCH}.
   */
  Batch setJobRetriesAsync(JobQuery jobQuery, int retries);

  /**
   * Sets the number of retries that jobs have left asynchronously.
   *
   * Whenever the JobExecutor fails to execute a job, this value is decremented.
   * When it hits zero, the job is supposed to be dead and not retried again.
   * In that case, this method can be used to increase the number of retries.
   *
   * Either jobIds or jobQuery has to be provided. If both are provided resulting list
   * of affected jobs will contain jobs matching query as well as jobs defined in the list.
   *
   * @param jobIds ids of the jobs to modify.
   * @param jobQuery query that identifies which jobs should be modified.
   * @param retries number of retries.
   *
   * @throws BadUserRequestException if neither jobIds, nor jobQuery is provided or result in empty list
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} permission on {@link Resources#BATCH}.
   */
  Batch setJobRetriesAsync(List<String> jobIds, JobQuery jobQuery, int retries);

  /**
   * Sets the number of retries that jobs have left asynchronously.
   *
   * Whenever the JobExecutor fails to execute a job, this value is decremented.
   * When it hits zero, the job is supposed to be dead and not retried again.
   * In that case, this method can be used to increase the number of retries.
   *
   * Either jobIds or jobQuery has to be provided. If both are provided resulting list
   * of affected jobs will contain jobs matching query as well as jobs defined in the list.
   *
   * @param processInstanceIds ids of the process instances that for which jobs retries will be set
   * @param query query that identifies process instances with jobs that have to be modified
   * @param retries number of retries.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} permission on {@link Resources#BATCH}.
   */
  Batch setJobRetriesAsync (List<String> processInstanceIds, ProcessInstanceQuery query, int retries);

  /**
   * <p>
   * Set the number of retries of all <strong>failed</strong> {@link Job jobs}
   * of the provided job definition id.
   * </p>
   *
   * <p>
   * Whenever the JobExecutor fails to execute a job, this value is decremented.
   * When it hits zero, the job is supposed to be <strong>failed</strong> and
   * not retried again. In that case, this method can be used to increase the
   * number of retries.
   * </p>
   *
   * <p>
   * {@link Incident Incidents} of the involved failed {@link Job jobs} will not
   * be resolved using this method! When the execution of a job was successful
   * the corresponding incident will be resolved.
   * </p>
   *
   * @param jobDefinitionId id of the job definition, cannot be null.
   * @param retries number of retries.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void setJobRetriesByJobDefinitionId(String jobDefinitionId, int retries);

  /**
   * Sets a new due date for the provided id.
   * When newDuedate is null, the job is executed with the next
   * job executor run.
   *
   * @param jobId id of job to modify, cannot be null.
   * @param newDuedate new date for job execution
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void setJobDuedate(String jobId, Date newDuedate);

  /**
   * Sets a new priority for the job with the provided id.
   *
   * @param jobId the id of the job to modify, must not be null
   * @param priority the job's new priority
   *
   * @throws AuthorizationException thrown if the current user does not possess any of the following permissions
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *
   * @since 7.4
   */
  void setJobPriority(String jobId, long priority);

  /**
   * <p>Sets an explicit priority for jobs of the given job definition.
   * Jobs created after invoking this method receive the given priority.
   * This setting overrides any setting specified in the BPMN 2.0 XML.</p>
   *
   * <p>The overriding priority can be cleared by using the method
   * {@link #clearOverridingJobPriorityForJobDefinition(String)}.</p>
   *
   * @param jobDefinitionId the id of the job definition to set the priority for
   * @param priority the priority to set;
   *
   * @throws AuthorizationException thrown if the current user does not possess any of the following permissions
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *
   * @since 7.4
   */
  void setOverridingJobPriorityForJobDefinition(String jobDefinitionId, long priority);

  /**
   * <p>Sets an explicit default priority for jobs of the given job definition.
   * Jobs created after invoking this method receive the given priority.
   * This setting overrides any setting specified in the BPMN 2.0 XML.</p>
   *
   * <p>If <code>cascade</code> is true, priorities of already existing jobs
   * are updated accordingly.</p>
   *
   * <p>The overriding priority can be cleared by using the method
   * {@link #clearOverridingJobPriorityForJobDefinition(String)}.</p>
   *
   * @param jobDefinitionId the id of the job definition to set the priority for
   * @param priority the priority to set
   * @param cascade if true, priorities of existing jobs of the given definition are changed as well
   *
   * @throws AuthorizationException thrown if the current user does not possess
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *
   *   If cascade is <code>true</code>, the user must further possess one of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *
   * @since 7.4
   */
  void setOverridingJobPriorityForJobDefinition(String jobDefinitionId, long priority, boolean cascade);

  /**
   * <p>Clears the job definition's overriding job priority if set. After invoking this method,
   * new jobs of the given definition receive the priority as specified in the BPMN 2.0 XML
   * or the global default priority.</p>
   *
   * <p>Existing job instance priorities remain unchanged.</p>
   *
   * @param jobDefinitionId the id of the job definition for which to clear the overriding priority
   *
   * @throws AuthorizationException thrown if the current user does not possess any of the following permissions
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *
   * @since 7.4
   */
  void clearOverridingJobPriorityForJobDefinition(String jobDefinitionId);

  /**
   * Returns the full stacktrace of the exception that occurs when the job
   * with the given id was last executed. Returns null when the job has no
   * exception stacktrace.
   *
   * @param jobId id of the job, cannot be null.
   *
   * @throws ProcessEngineException
   *          When no job exists with the given id.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  String getJobExceptionStacktrace(String jobId);

  /** get the list of properties. */
  Map<String, String> getProperties();

  /** Set the value for a property.
   *
   *  @param name the name of the property.
   *  @param value the new value for the property.
   */
  void setProperty(String name, String value);

  /**
   * Deletes a property by name. If the property does not exist, the request is ignored.
   *
   * @param name the name of the property to delete
   */
  void deleteProperty(String name);

  /** programmatic schema update on a given connection returning feedback about what happened
   *
   *  Note: will always return an empty string
   *
   * @throws AuthorizationException
   *          If the user is not a member of the group {@link Groups#CAMUNDA_ADMIN}.
   */
  String databaseSchemaUpgrade(Connection connection, String catalog, String schema);

  /**
   * Query for the number of process instances aggregated by process definitions.
   */
  ProcessDefinitionStatisticsQuery createProcessDefinitionStatisticsQuery();

  /**
   * Query for the number of process instances aggregated by deployments.
   */
  DeploymentStatisticsQuery createDeploymentStatisticsQuery();

  /**
   * Query for the number of activity instances aggregated by activities of a single process definition.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  ActivityStatisticsQuery createActivityStatisticsQuery(String processDefinitionId);

  /**
   * Get the deployments that are registered the engine's job executor.
   * This set is only relevant, if the engine configuration property <code>jobExecutorDeploymentAware</code> is set.
   *
   * @throws AuthorizationException
   *          If the user is not a member of the group {@link Groups#CAMUNDA_ADMIN}.
   */
  Set<String> getRegisteredDeployments();

  /**
   * Register a deployment for the engine's job executor.
   * This is required, if the engine configuration property <code>jobExecutorDeploymentAware</code> is set.
   * If set to false, the job executor will execute any job.
   *
   * @throws AuthorizationException
   *          If the user is not a member of the group {@link Groups#CAMUNDA_ADMIN}.
   */
  void registerDeploymentForJobExecutor(String deploymentId);

  /**
   * Unregister a deployment for the engine's job executor.
   * If the engine configuration property <code>jobExecutorDeploymentAware</code> is set,
   * jobs for the given deployment will no longer get acquired.
   *
   * @throws AuthorizationException
   *          If the user is not a member of the group {@link Groups#CAMUNDA_ADMIN}.
   */
  void unregisterDeploymentForJobExecutor(String deploymentId);

  /**
   * Get the configured history level for the process engine.
   *
   * @return the history level
   *
   * @throws AuthorizationException
   *          If the user is not a member of the group {@link Groups#CAMUNDA_ADMIN}.
   */
  int getHistoryLevel();

  /**
   * @return a new metrics Query.
   * @since 7.3
   */
  MetricsQuery createMetricsQuery();

  /**
   * Deletes all metrics events which are older than the specified timestamp.
   * If the timestamp is null, all metrics will be deleted
   *
   * @param timestamp or null
   * @since 7.3
   */
  void deleteMetrics(Date timestamp);

  /**
   * Deletes all metrics events which are older than the specified timestamp
   * and reported by the given reporter. If a parameter is null, all metric events
   * are matched in that regard.
   *
   * @param timestamp or null
   * @param reporter or null
   * @since 7.4
   */
  void deleteMetrics(Date timestamp, String reporter);

  /**
   * Forces this engine to commit its pending collected metrics to the database.
   *
   * @throws ProcessEngineException if metrics reporting is disabled or the db metrics
   * reporter is deactivated
   */
  void reportDbMetricsNow();

  /**
   * Creates a query to search for {@link org.camunda.bpm.engine.batch.Batch} instances.
   *
   * @since 7.5
   */
  BatchQuery createBatchQuery();

  /**
   * <p>
   *   Suspends the {@link Batch} with the given id immediately.
   * </p>
   *
   * <p>
   *   <strong>Note:</strong> All {@link JobDefinition}s and {@link Job}s
   *   related to the provided batch will be suspended.
   * </p>
   *
   * @throws BadUserRequestException
   *          If no such batch can be found.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#BATCH}.
   *
   * @since 7.5
   */
  void suspendBatchById(String batchId);

  /**
   * <p>
   *   Activates the {@link Batch} with the given id immediately.
   * </p>
   *
   * <p>
   *   <strong>Note:</strong> All {@link JobDefinition}s and {@link Job}s
   *   related to the provided batch will be activated.
   * </p>
   *
   * @throws BadUserRequestException
   *          If no such batch can be found.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#BATCH}.
   *
   * @since 7.5
   */
  void activateBatchById(String batchId);

  /**
   * Deletes a batch instance and the corresponding job definitions.
   *
   * If cascade is set to true the historic batch instances and the
   * historic jobs logs are also removed.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE} permission on {@link Resources#BATCH}
   *
   * @since 7.5
   */
  void deleteBatch(String batchId, boolean cascade);

  /**
   * Query for the statistics of the batch execution jobs of a batch.
   *
   * @since 7.5
   */
  BatchStatisticsQuery createBatchStatisticsQuery();

}
