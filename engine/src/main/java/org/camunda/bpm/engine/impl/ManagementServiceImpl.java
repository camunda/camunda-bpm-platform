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
package org.camunda.bpm.engine.impl;

import java.sql.Connection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.BatchQuery;
import org.camunda.bpm.engine.batch.BatchStatisticsQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.batch.BatchQueryImpl;
import org.camunda.bpm.engine.impl.batch.BatchStatisticsQueryImpl;
import org.camunda.bpm.engine.impl.batch.DeleteBatchCmd;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.ActivateBatchCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteJobCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteLicenseKeyCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteMetricsCmd;
import org.camunda.bpm.engine.impl.cmd.DeletePropertyCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteTaskMetricsCmd;
import org.camunda.bpm.engine.impl.cmd.GetHistoryLevelCmd;
import org.camunda.bpm.engine.impl.cmd.GetJobExceptionStacktraceCmd;
import org.camunda.bpm.engine.impl.cmd.GetLicenseKeyCmd;
import org.camunda.bpm.engine.impl.cmd.GetProcessApplicationForDeploymentCmd;
import org.camunda.bpm.engine.impl.cmd.GetPropertiesCmd;
import org.camunda.bpm.engine.impl.cmd.GetTableCountCmd;
import org.camunda.bpm.engine.impl.cmd.GetTableMetaDataCmd;
import org.camunda.bpm.engine.impl.cmd.GetTableNameCmd;
import org.camunda.bpm.engine.impl.cmd.GetTelemetryDataCmd;
import org.camunda.bpm.engine.impl.cmd.GetUniqueTaskWorkerCountCmd;
import org.camunda.bpm.engine.impl.cmd.IsTelemetryEnabledCmd;
import org.camunda.bpm.engine.impl.cmd.PurgeDatabaseAndCacheCmd;
import org.camunda.bpm.engine.impl.cmd.RecalculateJobDuedateCmd;
import org.camunda.bpm.engine.impl.cmd.RegisterDeploymentCmd;
import org.camunda.bpm.engine.impl.cmd.RegisterProcessApplicationCmd;
import org.camunda.bpm.engine.impl.cmd.ReportDbMetricsCmd;
import org.camunda.bpm.engine.impl.cmd.SetJobDefinitionPriorityCmd;
import org.camunda.bpm.engine.impl.cmd.SetJobDuedateCmd;
import org.camunda.bpm.engine.impl.cmd.SetJobPriorityCmd;
import org.camunda.bpm.engine.impl.cmd.SetLicenseKeyCmd;
import org.camunda.bpm.engine.impl.cmd.SetPropertyCmd;
import org.camunda.bpm.engine.impl.cmd.SuspendBatchCmd;
import org.camunda.bpm.engine.impl.cmd.TelemetryConfigureCmd;
import org.camunda.bpm.engine.impl.cmd.UnregisterDeploymentCmd;
import org.camunda.bpm.engine.impl.cmd.UnregisterProcessApplicationCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSession;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobHelper;
import org.camunda.bpm.engine.impl.management.PurgeReport;
import org.camunda.bpm.engine.impl.management.SetJobRetriesBuilderImpl;
import org.camunda.bpm.engine.impl.management.SetJobRetriesByJobsAsyncBuilderImpl;
import org.camunda.bpm.engine.impl.management.SetJobRetriesByProcessAsyncBuilderImpl;
import org.camunda.bpm.engine.impl.management.UpdateJobDefinitionSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.management.UpdateJobSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.metrics.MetricsQueryImpl;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.LicenseKeyDataImpl;
import org.camunda.bpm.engine.management.ActivityStatisticsQuery;
import org.camunda.bpm.engine.management.DeploymentStatisticsQuery;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.management.MetricsQuery;
import org.camunda.bpm.engine.management.ProcessDefinitionStatisticsQuery;
import org.camunda.bpm.engine.management.SchemaLogQuery;
import org.camunda.bpm.engine.management.SetJobRetriesByJobsAsyncBuilder;
import org.camunda.bpm.engine.management.SetJobRetriesByProcessAsyncBuilder;
import org.camunda.bpm.engine.management.TableMetaData;
import org.camunda.bpm.engine.management.TablePageQuery;
import org.camunda.bpm.engine.management.UpdateJobDefinitionSuspensionStateSelectBuilder;
import org.camunda.bpm.engine.management.UpdateJobSuspensionStateSelectBuilder;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.telemetry.TelemetryData;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Falko Menge
 * @author Saeid Mizaei
 * @author Askar AKhmerov
 */
public class ManagementServiceImpl extends ServiceImpl implements ManagementService {

  protected ProcessEngineConfiguration processEngineConfiguration;

  public ManagementServiceImpl(ProcessEngineConfiguration processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
  }

  public ProcessApplicationRegistration registerProcessApplication(String deploymentId, ProcessApplicationReference reference) {
    return commandExecutor.execute(new RegisterProcessApplicationCmd(deploymentId, reference));
  }

  public void unregisterProcessApplication(String deploymentId, boolean removeProcessesFromCache) {
    commandExecutor.execute(new UnregisterProcessApplicationCmd(deploymentId, removeProcessesFromCache));
  }

  public void unregisterProcessApplication(Set<String> deploymentIds, boolean removeProcessesFromCache) {
    commandExecutor.execute(new UnregisterProcessApplicationCmd(deploymentIds, removeProcessesFromCache));
  }

  public String getProcessApplicationForDeployment(String deploymentId) {
    return commandExecutor.execute(new GetProcessApplicationForDeploymentCmd(deploymentId));
  }

  public Map<String, Long> getTableCount() {
    return commandExecutor.execute(new GetTableCountCmd());
  }

  public String getTableName(Class<?> activitiEntityClass) {
    return commandExecutor.execute(new GetTableNameCmd(activitiEntityClass));
  }

  public TableMetaData getTableMetaData(String tableName) {
    return commandExecutor.execute(new GetTableMetaDataCmd(tableName));
  }

  public void executeJob(String jobId) {
    ExecuteJobHelper.executeJob(jobId, commandExecutor);
  }

  public void deleteJob(String jobId) {
    commandExecutor.execute(new DeleteJobCmd(jobId));
  }

  public void setJobRetries(String jobId, int retries) {
    setJobRetries(retries).jobId(jobId).execute();
  }

  public void setJobRetries(List<String> jobIds, int retries) {
    setJobRetries(retries).jobIds(jobIds).execute();
  }

  public void setJobRetriesByJobDefinitionId(String jobDefinitionId, int retries) {
    setJobRetries(retries).jobDefinitionId(jobDefinitionId).execute();
  }

  @Override
  public SetJobRetriesBuilderImpl setJobRetries(int retries) {
    return new SetJobRetriesBuilderImpl(commandExecutor, retries);
  }

  @Override
  public Batch setJobRetriesAsync(List<String> jobIds, int retries) {
    return setJobRetriesByJobsAsync(retries)
        .jobIds(jobIds)
        .executeAsync();
  }

  @Override
  public Batch setJobRetriesAsync(JobQuery jobQuery, int retries) {
    return setJobRetriesByJobsAsync(retries)
        .jobQuery(jobQuery)
        .executeAsync();
  }

  @Override
  public Batch setJobRetriesAsync(List<String> jobIds, JobQuery jobQuery, int retries) {
    return setJobRetriesByJobsAsync(retries)
        .jobIds(jobIds)
        .jobQuery(jobQuery)
        .executeAsync();
  }

  @Override
  public Batch setJobRetriesAsync(List<String> processInstanceIds, ProcessInstanceQuery query, int retries) {
    return setJobRetriesByProcessAsync(retries)
        .processInstanceIds(processInstanceIds)
        .processInstanceQuery(query)
        .executeAsync();
  }

  @Override
  public Batch setJobRetriesAsync(List<String> processInstanceIds, ProcessInstanceQuery query,
                                  HistoricProcessInstanceQuery historicProcessInstanceQuery,
                                  int retries) {
    return setJobRetriesByProcessAsync(retries)
        .processInstanceIds(processInstanceIds)
        .processInstanceQuery(query)
        .historicProcessInstanceQuery(historicProcessInstanceQuery)
        .executeAsync();
  }

  @Override
  public SetJobRetriesByJobsAsyncBuilder setJobRetriesByJobsAsync(int retries) {
    return new SetJobRetriesByJobsAsyncBuilderImpl(commandExecutor, retries);
  }

  @Override
  public SetJobRetriesByProcessAsyncBuilder setJobRetriesByProcessAsync(int retries) {
    return new SetJobRetriesByProcessAsyncBuilderImpl(commandExecutor, retries);
  }

  public void setJobDuedate(String jobId, Date newDuedate) {
    setJobDuedate(jobId, newDuedate, false);
  }

  public void setJobDuedate(String jobId, Date newDuedate, boolean cascade) {
    commandExecutor.execute(new SetJobDuedateCmd(jobId, newDuedate, cascade));
  }

  public void recalculateJobDuedate(String jobId, boolean creationDateBased) {
    commandExecutor.execute(new RecalculateJobDuedateCmd(jobId, creationDateBased));
  }

  public void setJobPriority(String jobId, long priority) {
    commandExecutor.execute(new SetJobPriorityCmd(jobId, priority));
  }

  public TablePageQuery createTablePageQuery() {
    return new TablePageQueryImpl(commandExecutor);
  }

  public JobQuery createJobQuery() {
    return new JobQueryImpl(commandExecutor);
  }

  public JobDefinitionQuery createJobDefinitionQuery() {
    return new JobDefinitionQueryImpl(commandExecutor);
  }

  public String getJobExceptionStacktrace(String jobId) {
    return commandExecutor.execute(new GetJobExceptionStacktraceCmd(jobId));
  }

  public Map<String, String> getProperties() {
    return commandExecutor.execute(new GetPropertiesCmd());
  }

  public void setProperty(String name, String value) {
    commandExecutor.execute(new SetPropertyCmd(name, value));
  }

  public void deleteProperty(String name) {
    commandExecutor.execute(new DeletePropertyCmd(name));
  }

  public void setLicenseKey(String licenseKey) {
    commandExecutor.execute(new SetLicenseKeyCmd(licenseKey));
  }

  public String getLicenseKey() {
    return commandExecutor.execute(new GetLicenseKeyCmd());
  }

  public void deleteLicenseKey() {
    commandExecutor.execute(new DeleteLicenseKeyCmd(true));
  }

  public String databaseSchemaUpgrade(Connection connection, String catalog, String schema) {
    return commandExecutor.execute(new DbSchemaUpgradeCmd(connection, catalog, schema));
  }

  /**
   * Purges the database and the deployment cache.
   */
  public PurgeReport purge() {
    return commandExecutor.execute(new PurgeDatabaseAndCacheCmd());
  }


  public ProcessDefinitionStatisticsQuery createProcessDefinitionStatisticsQuery() {
    return new ProcessDefinitionStatisticsQueryImpl(commandExecutor);
  }

  public ActivityStatisticsQuery createActivityStatisticsQuery(String processDefinitionId) {
    return new ActivityStatisticsQueryImpl(processDefinitionId, commandExecutor);
  }

  public DeploymentStatisticsQuery createDeploymentStatisticsQuery() {
    return new DeploymentStatisticsQueryImpl(commandExecutor);
  }

  public Set<String> getRegisteredDeployments() {
    return commandExecutor.execute(new GetRegisteredDeploymentsCmd());
  }

  public void registerDeploymentForJobExecutor(final String deploymentId) {
    commandExecutor.execute(new RegisterDeploymentCmd(deploymentId));
  }

  public void unregisterDeploymentForJobExecutor(final String deploymentId) {
    commandExecutor.execute(new UnregisterDeploymentCmd(deploymentId));
  }


  public void activateJobDefinitionById(String jobDefinitionId) {
    updateJobDefinitionSuspensionState()
        .byJobDefinitionId(jobDefinitionId)
        .activate();
  }

  public void activateJobDefinitionById(String jobDefinitionId, boolean activateJobs) {
    updateJobDefinitionSuspensionState()
        .byJobDefinitionId(jobDefinitionId)
        .includeJobs(activateJobs)
        .activate();
  }

  public void activateJobDefinitionById(String jobDefinitionId, boolean activateJobs, Date activationDate) {
    updateJobDefinitionSuspensionState()
        .byJobDefinitionId(jobDefinitionId)
        .includeJobs(activateJobs)
        .executionDate(activationDate)
        .activate();
  }

  public void suspendJobDefinitionById(String jobDefinitionId) {
    updateJobDefinitionSuspensionState()
        .byJobDefinitionId(jobDefinitionId)
        .suspend();
  }

  public void suspendJobDefinitionById(String jobDefinitionId, boolean suspendJobs) {
    updateJobDefinitionSuspensionState()
        .byJobDefinitionId(jobDefinitionId)
        .includeJobs(suspendJobs)
        .suspend();
  }

  public void suspendJobDefinitionById(String jobDefinitionId, boolean suspendJobs, Date suspensionDate) {
    updateJobDefinitionSuspensionState()
        .byJobDefinitionId(jobDefinitionId)
        .includeJobs(suspendJobs)
        .executionDate(suspensionDate)
        .suspend();
  }

  public void activateJobDefinitionByProcessDefinitionId(String processDefinitionId) {
    updateJobDefinitionSuspensionState()
        .byProcessDefinitionId(processDefinitionId)
        .activate();
  }

  public void activateJobDefinitionByProcessDefinitionId(String processDefinitionId, boolean activateJobs) {
    updateJobDefinitionSuspensionState()
        .byProcessDefinitionId(processDefinitionId)
        .includeJobs(activateJobs)
        .activate();
  }

  public void activateJobDefinitionByProcessDefinitionId(String processDefinitionId, boolean activateJobs, Date activationDate) {
    updateJobDefinitionSuspensionState()
        .byProcessDefinitionId(processDefinitionId)
        .includeJobs(activateJobs)
        .executionDate(activationDate)
        .activate();
  }

  public void suspendJobDefinitionByProcessDefinitionId(String processDefinitionId) {
    updateJobDefinitionSuspensionState()
        .byProcessDefinitionId(processDefinitionId)
        .suspend();
  }

  public void suspendJobDefinitionByProcessDefinitionId(String processDefinitionId, boolean suspendJobs) {
    updateJobDefinitionSuspensionState()
        .byProcessDefinitionId(processDefinitionId)
        .includeJobs(suspendJobs)
        .suspend();
  }

  public void suspendJobDefinitionByProcessDefinitionId(String processDefinitionId, boolean suspendJobs, Date suspensionDate) {
    updateJobDefinitionSuspensionState()
        .byProcessDefinitionId(processDefinitionId)
        .includeJobs(suspendJobs)
        .executionDate(suspensionDate)
        .suspend();
  }

  public void activateJobDefinitionByProcessDefinitionKey(String processDefinitionKey) {
    updateJobDefinitionSuspensionState()
        .byProcessDefinitionKey(processDefinitionKey)
        .activate();
  }

  public void activateJobDefinitionByProcessDefinitionKey(String processDefinitionKey, boolean activateJobs) {
    updateJobDefinitionSuspensionState()
        .byProcessDefinitionKey(processDefinitionKey)
        .includeJobs(activateJobs)
        .activate();
  }

  public void activateJobDefinitionByProcessDefinitionKey(String processDefinitionKey, boolean activateJobs, Date activationDate) {
    updateJobDefinitionSuspensionState()
        .byProcessDefinitionKey(processDefinitionKey)
        .includeJobs(activateJobs)
        .executionDate(activationDate)
        .activate();
  }

  public void suspendJobDefinitionByProcessDefinitionKey(String processDefinitionKey) {
    updateJobDefinitionSuspensionState()
        .byProcessDefinitionKey(processDefinitionKey)
        .suspend();
  }

  public void suspendJobDefinitionByProcessDefinitionKey(String processDefinitionKey, boolean suspendJobs) {
    updateJobDefinitionSuspensionState()
        .byProcessDefinitionKey(processDefinitionKey)
        .includeJobs(suspendJobs)
        .suspend();
  }

  public void suspendJobDefinitionByProcessDefinitionKey(String processDefinitionKey, boolean suspendJobs, Date suspensionDate) {
    updateJobDefinitionSuspensionState()
        .byProcessDefinitionKey(processDefinitionKey)
        .includeJobs(suspendJobs)
        .executionDate(suspensionDate)
        .suspend();
  }

  public UpdateJobDefinitionSuspensionStateSelectBuilder updateJobDefinitionSuspensionState() {
    return new UpdateJobDefinitionSuspensionStateBuilderImpl(commandExecutor);
  }

  public void activateJobById(String jobId) {
    updateJobSuspensionState()
        .byJobId(jobId)
        .activate();
  }

  public void activateJobByProcessInstanceId(String processInstanceId) {
    updateJobSuspensionState()
        .byProcessInstanceId(processInstanceId)
        .activate();
  }

  public void activateJobByJobDefinitionId(String jobDefinitionId) {
    updateJobSuspensionState()
        .byJobDefinitionId(jobDefinitionId)
        .activate();
  }

  public void activateJobByProcessDefinitionId(String processDefinitionId) {
    updateJobSuspensionState()
        .byProcessDefinitionId(processDefinitionId)
        .activate();
  }

  public void activateJobByProcessDefinitionKey(String processDefinitionKey) {
    updateJobSuspensionState()
        .byProcessDefinitionKey(processDefinitionKey)
        .activate();
  }

  public void suspendJobById(String jobId) {
    updateJobSuspensionState()
        .byJobId(jobId)
        .suspend();
  }

  public void suspendJobByJobDefinitionId(String jobDefinitionId) {
    updateJobSuspensionState()
        .byJobDefinitionId(jobDefinitionId)
        .suspend();
  }

  public void suspendJobByProcessInstanceId(String processInstanceId) {
    updateJobSuspensionState()
        .byProcessInstanceId(processInstanceId)
        .suspend();
  }

  public void suspendJobByProcessDefinitionId(String processDefinitionId) {
    updateJobSuspensionState()
        .byProcessDefinitionId(processDefinitionId)
        .suspend();
  }

  public void suspendJobByProcessDefinitionKey(String processDefinitionKey) {
    updateJobSuspensionState()
        .byProcessDefinitionKey(processDefinitionKey)
        .suspend();
  }

  public UpdateJobSuspensionStateSelectBuilder updateJobSuspensionState() {
    return new UpdateJobSuspensionStateBuilderImpl(commandExecutor);
  }

  public int getHistoryLevel() {
    return commandExecutor.execute(new GetHistoryLevelCmd());
  }

  public MetricsQuery createMetricsQuery() {
    return new MetricsQueryImpl(commandExecutor);
  }

  public void deleteMetrics(Date timestamp) {
    commandExecutor.execute(new DeleteMetricsCmd(timestamp, null));
  }

  public void deleteMetrics(Date timestamp, String reporter) {
    commandExecutor.execute(new DeleteMetricsCmd(timestamp, reporter));

  }

  public void reportDbMetricsNow() {
    commandExecutor.execute(new ReportDbMetricsCmd());
  }

  @Override
  public long getUniqueTaskWorkerCount(Date startTime, Date endTime) {
    return commandExecutor.execute(new GetUniqueTaskWorkerCountCmd(startTime, endTime));
  }

  @Override
  public void deleteTaskMetrics(Date timestamp) {
    commandExecutor.execute(new DeleteTaskMetricsCmd(timestamp));
  }

  public void setOverridingJobPriorityForJobDefinition(String jobDefinitionId, long priority) {
    commandExecutor.execute(new SetJobDefinitionPriorityCmd(jobDefinitionId, priority, false));
  }

  public void setOverridingJobPriorityForJobDefinition(String jobDefinitionId, long priority, boolean cascade) {
    commandExecutor.execute(new SetJobDefinitionPriorityCmd(jobDefinitionId, priority, true));
  }

  public void clearOverridingJobPriorityForJobDefinition(String jobDefinitionId) {
    commandExecutor.execute(new SetJobDefinitionPriorityCmd(jobDefinitionId, null, false));
  }

  public BatchQuery createBatchQuery() {
    return new BatchQueryImpl(commandExecutor);
  }

  public void deleteBatch(String batchId, boolean cascade) {
    commandExecutor.execute(new DeleteBatchCmd(batchId, cascade));
  }

  public void suspendBatchById(String batchId) {
    commandExecutor.execute(new SuspendBatchCmd(batchId));
  }

  public void activateBatchById(String batchId) {
    commandExecutor.execute(new ActivateBatchCmd(batchId));
  }

  public BatchStatisticsQuery createBatchStatisticsQuery() {
    return new BatchStatisticsQueryImpl(commandExecutor);
  }

  public SchemaLogQuery createSchemaLogQuery() {
    return new SchemaLogQueryImpl(commandExecutor);
  }

  public void toggleTelemetry(boolean enabled) {
    commandExecutor.execute(new TelemetryConfigureCmd(enabled));
  }

  public Boolean isTelemetryEnabled() {
    return commandExecutor.execute(new IsTelemetryEnabledCmd());
  }

  @Override
  public TelemetryData getTelemetryData() {
    return commandExecutor.execute(new GetTelemetryDataCmd());
  }

  /**
   * Adds the web application name to the telemetry data of the engine.
   *
   * @param webapp
   *          the web application that is used with the engine
   * @return whether the web application was successfully added or not
   */
  public boolean addWebappToTelemetry(String webapp) {
    TelemetryRegistry telemetryRegistry = processEngineConfiguration.getTelemetryRegistry();
    if (telemetryRegistry != null) {
      telemetryRegistry.addWebapp(webapp);
      return true;
    }
    return false;
  }

  /**
   * Adds the application server information to the telemetry data of the engine.
   *
   * @param appServerInfo
   *          a String containing information about the application server
   */
  public void addApplicationServerInfoToTelemetry(String appServerInfo) {
    TelemetryRegistry telemetryRegistry = processEngineConfiguration.getTelemetryRegistry();
    if (telemetryRegistry != null) {
      telemetryRegistry.setApplicationServer(appServerInfo);
    }
  }

  /**
   * Sets license key information to the telemetry data of the engine.
   *
   * @param licenseKeyData
   *          a data object containing various pieces of information
   *          about the installed license
   */
  public void setLicenseKeyForTelemetry(LicenseKeyDataImpl licenseKeyData) {
    TelemetryRegistry telemetryRegistry = processEngineConfiguration.getTelemetryRegistry();
    if (telemetryRegistry != null) {
      telemetryRegistry.setLicenseKey(licenseKeyData);
    }
  }

  public LicenseKeyDataImpl getLicenseKeyFromTelemetry() {
    TelemetryRegistry telemetryRegistry = processEngineConfiguration.getTelemetryRegistry();
    if (telemetryRegistry != null) {
      return telemetryRegistry.getLicenseKey();
    }
    return null;
  }

  public void clearTelemetryData() {
    TelemetryRegistry telemetryRegistry = processEngineConfiguration.getTelemetryRegistry();
    if (telemetryRegistry != null) {
      telemetryRegistry.clear();
    }
    MetricsRegistry metricsRegistry = ((ProcessEngineConfigurationImpl) processEngineConfiguration).getMetricsRegistry();
    if(metricsRegistry != null) {
      metricsRegistry.clearTelemetryMetrics();
    }
    deleteMetrics(null);
  }

  protected class DbSchemaUpgradeCmd implements Command<String> {

    protected Connection connection;
    protected String catalog;
    protected String schema;

    public DbSchemaUpgradeCmd(Connection connection, String catalog, String schema) {
      this.connection = connection;
      this.catalog = catalog;
      this.schema = schema;
    }

    @Override
    public String execute(CommandContext commandContext) {
      commandContext.getAuthorizationManager().checkCamundaAdmin();
      DbSqlSessionFactory dbSqlSessionFactory = (DbSqlSessionFactory) commandContext.getSessionFactories().get(DbSqlSession.class);
      DbSqlSession dbSqlSession = dbSqlSessionFactory.openSession(connection, catalog, schema);
      commandContext.getSessions().put(DbSqlSession.class, dbSqlSession);
      dbSqlSession.dbSchemaUpdate();

      return "";
    }

  }

  protected class GetRegisteredDeploymentsCmd implements Command<Set<String>> {

    @Override
    public Set<String> execute(CommandContext commandContext) {
      commandContext.getAuthorizationManager().checkCamundaAdminOrPermission(CommandChecker::checkReadRegisteredDeployments);
      Set<String> registeredDeployments = Context.getProcessEngineConfiguration().getRegisteredDeployments();
      return new HashSet<>(registeredDeployments);
    }
  }

}
