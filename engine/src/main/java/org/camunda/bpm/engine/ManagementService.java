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
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.engine.management.ActivityStatisticsQuery;
import org.camunda.bpm.engine.management.DeploymentStatisticsQuery;
import org.camunda.bpm.engine.management.ProcessDefinitionStatisticsQuery;
import org.camunda.bpm.engine.management.TableMetaData;
import org.camunda.bpm.engine.management.TablePage;
import org.camunda.bpm.engine.management.TablePageQuery;
import org.camunda.bpm.engine.runtime.JobQuery;



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
   * @return true if the registration was cleared
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
   * @return true if the registration was cleared
   */
  void unregisterProcessApplication(Set<String> deploymentIds, boolean removeProcessDefinitionsFromCache);

  /**
   * @return the name of the process application that is currently registered for
   *         the given deployment or 'null' if no process application is
   *         currently registered.
   */
  String getProcessApplicationForDeployment(String deploymentId);

  /**
   * Get the mapping containing {table name, row count} entries of the
   * Activiti database schema.
   */
  Map<String, Long> getTableCount();

  /**
   * Gets the table name (including any configured prefix) for an Activiti entity like Task, Execution or the like.
   */
  String getTableName(Class<?> activitiEntityClass);

  /**
   * Gets the metadata (column names, column types, etc.) of a certain table.
   * Returns null when no table exists with the given name.
   */
  TableMetaData getTableMetaData(String tableName);

  /**
   * Creates a {@link TablePageQuery} that can be used to fetch {@link TablePage}
   * containing specific sections of table row data.
   */
  TablePageQuery createTablePageQuery();

  /**
   * Returns a new JobQuery implementation, that can be used
   * to dynamically query the jobs.
   */
  JobQuery createJobQuery();

  /**
   * Forced synchronous execution of a job (eg. for administation or testing)
   * The job will be executed, even if the process definition and/or the process instance
   * is in suspended state.
   *
   * @param jobId id of the job to execute, cannot be null.
   * @throws ProcessEngineException when there is no job with the given id.
   */
  void executeJob(String jobId);

  /**
   * Delete the job with the provided id.
   * @param jobId id of the job to execute, cannot be null.
   * @throws ProcessEngineException when there is no job with the given id.
   */
  void deleteJob(String jobId);

  /**
   * Sets the number of retries that a job has left.
   *
   * Whenever the JobExecutor fails to execute a job, this value is decremented.
   * When it hits zero, the job is supposed to be dead and not retried again.
   * In that case, this method can be used to increase the number of retries.
   * @param jobId id of the job to modify, cannot be null.
   * @param retries number of retries.
   */
  void setJobRetries(String jobId, int retries);

  /**
   * Sets a new due date for the provided id.
   * When newDuedate is null, the job is executed with the next
   * job executor run.
   * @param jobId id of job to modify, cannot be null.
   * @param newDuedate new date for job execution
   */
  void setJobDuedate(String jobId, Date newDuedate);

  /**
   * Returns the full stacktrace of the exception that occurs when the job
   * with the given id was last executed. Returns null when the job has no
   * exception stacktrace.
   * @param jobId id of the job, cannot be null.
   * @throws ProcessEngineException when no job exists with the given id.
   */
  String getJobExceptionStacktrace(String jobId);

  /** get the list of properties. */
  Map<String, String> getProperties();

  /** programmatic schema update on a given connection returning feedback about what happened */
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
   */
  ActivityStatisticsQuery createActivityStatisticsQuery(String processDefinitionId);

  /**
   * Get the deployments that are registered the engine's job executor.
   * This set is only relevant, if the engine configuration property <code>jobExecutorDeploymentAware</code> is set.
   */
  Set<String> getRegisteredDeployments();

  /**
   * Register a deployment for the engine's job executor.
   * This is required, if the engine configuration property <code>jobExecutorDeploymentAware</code> is set.
   * If set to false, the job executor will execute any job.
   */
  void registerDeploymentForJobExecutor(String deploymentId);

  /**
   * Unregister a deployment for the engine's job executor.
   * If the engine configuration property <code>jobExecutorDeploymentAware</code> is set,
   * jobs for the given deployment will no longer get acquired.
   */
  void unregisterDeploymentForJobExecutor(String deploymentId);

}
