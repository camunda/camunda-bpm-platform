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
package org.camunda.bpm.engine.impl;

import java.sql.Connection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.impl.cmd.ActivateJobCmd;
import org.camunda.bpm.engine.impl.cmd.ActivateJobDefinitionCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteJobCmd;
import org.camunda.bpm.engine.impl.cmd.ExecuteJobsCmd;
import org.camunda.bpm.engine.impl.cmd.GetJobExceptionStacktraceCmd;
import org.camunda.bpm.engine.impl.cmd.GetProcessApplicationForDeploymentCmd;
import org.camunda.bpm.engine.impl.cmd.GetPropertiesCmd;
import org.camunda.bpm.engine.impl.cmd.GetTableCountCmd;
import org.camunda.bpm.engine.impl.cmd.GetTableMetaDataCmd;
import org.camunda.bpm.engine.impl.cmd.GetTableNameCmd;
import org.camunda.bpm.engine.impl.cmd.RegisterDeploymentCmd;
import org.camunda.bpm.engine.impl.cmd.RegisterProcessApplicationCmd;
import org.camunda.bpm.engine.impl.cmd.SetJobDuedateCmd;
import org.camunda.bpm.engine.impl.cmd.SetJobRetriesCmd;
import org.camunda.bpm.engine.impl.cmd.SetPropertyCmd;
import org.camunda.bpm.engine.impl.cmd.SuspendJobCmd;
import org.camunda.bpm.engine.impl.cmd.SuspendJobDefinitionCmd;
import org.camunda.bpm.engine.impl.cmd.UnregisterDeploymentCmd;
import org.camunda.bpm.engine.impl.cmd.UnregisterProcessApplicationCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.db.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.management.ActivityStatisticsQuery;
import org.camunda.bpm.engine.management.DeploymentStatisticsQuery;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.management.ProcessDefinitionStatisticsQuery;
import org.camunda.bpm.engine.management.TableMetaData;
import org.camunda.bpm.engine.management.TablePageQuery;
import org.camunda.bpm.engine.runtime.JobQuery;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Falko Menge
 * @author Saeid Mizaei
 */
public class ManagementServiceImpl extends ServiceImpl implements ManagementService {

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
    commandExecutor.execute(new ExecuteJobsCmd(jobId));
  }

  public void deleteJob(String jobId) {
    commandExecutor.execute(new DeleteJobCmd(jobId));
  }

  public void setJobRetries(String jobId, int retries) {
    commandExecutor.execute(new SetJobRetriesCmd(jobId, null, retries));
  }

  public void setJobRetriesByJobDefinitionId(String jobDefinitionId, int retries) {
    commandExecutor.execute(new SetJobRetriesCmd(null, jobDefinitionId, retries));
  }

  public void setJobDuedate(String jobId, Date newDuedate) {
    commandExecutor.execute(new SetJobDuedateCmd(jobId, newDuedate));
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

  public String databaseSchemaUpgrade(final Connection connection, final String catalog, final String schema) {
    return commandExecutor.execute(new Command<String>(){
      public String execute(CommandContext commandContext) {
        DbSqlSessionFactory dbSqlSessionFactory = (DbSqlSessionFactory) commandContext.getSessionFactories().get(DbSqlSession.class);
        DbSqlSession dbSqlSession = new DbSqlSession(dbSqlSessionFactory, connection, catalog, schema);
        commandContext.getSessions().put(DbSqlSession.class, dbSqlSession);
        return dbSqlSession.dbSchemaUpdate();
      }
    });
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
    return commandExecutor.execute(new Command<Set<String>>() {
      public Set<String> execute(CommandContext commandContext) {
        Set<String> registeredDeployments = Context.getProcessEngineConfiguration().getRegisteredDeployments();
        return new HashSet<String>(registeredDeployments);
      }
    });
  }

  public void registerDeploymentForJobExecutor(final String deploymentId) {
    commandExecutor.execute(new RegisterDeploymentCmd(deploymentId));
  }

  public void unregisterDeploymentForJobExecutor(final String deploymentId) {
    commandExecutor.execute(new UnregisterDeploymentCmd(deploymentId));
  }


  public void activateJobDefinitionById(String jobDefinitionId) {
    activateJobDefinitionById(jobDefinitionId, false);
  }

  public void activateJobDefinitionById(String jobDefinitionId, boolean activateJobs) {
    activateJobDefinitionById(jobDefinitionId, activateJobs, null);
  }

  public void activateJobDefinitionById(String jobDefinitionId, boolean activateJobs, Date activationDate) {
    commandExecutor.execute(new ActivateJobDefinitionCmd(jobDefinitionId, null, null, activateJobs, activationDate));
  }

  public void suspendJobDefinitionById(String jobDefinitionId) {
    suspendJobDefinitionById(jobDefinitionId, false);
  }

  public void suspendJobDefinitionById(String jobDefinitionId, boolean suspendJobs) {
    suspendJobDefinitionById(jobDefinitionId, suspendJobs, null);
  }

  public void suspendJobDefinitionById(String jobDefinitionId, boolean suspendJobs, Date suspensionDate) {
    commandExecutor.execute(new SuspendJobDefinitionCmd(jobDefinitionId, null, null, suspendJobs, suspensionDate));
  }


  public void activateJobDefinitionByProcessDefinitionId(String processDefinitionId) {
    activateJobDefinitionByProcessDefinitionId(processDefinitionId, false);
  }

  public void activateJobDefinitionByProcessDefinitionId(String processDefinitionId, boolean activateJobs) {
    activateJobDefinitionByProcessDefinitionId(processDefinitionId, activateJobs, null);
  }

  public void activateJobDefinitionByProcessDefinitionId(String processDefinitionId, boolean activateJobs, Date activationDate) {
    commandExecutor.execute(new ActivateJobDefinitionCmd(null, processDefinitionId, null, activateJobs, activationDate));
  }

  public void suspendJobDefinitionByProcessDefinitionId(String processDefinitionId) {
    suspendJobDefinitionByProcessDefinitionId(processDefinitionId, false);
  }

  public void suspendJobDefinitionByProcessDefinitionId(String processDefinitionId, boolean suspendJobs) {
    suspendJobDefinitionByProcessDefinitionId(processDefinitionId, suspendJobs, null);
  }
  public void suspendJobDefinitionByProcessDefinitionId(String processDefinitionId, boolean suspendJobs, Date suspensionDate) {
    commandExecutor.execute(new SuspendJobDefinitionCmd(null, processDefinitionId, null, suspendJobs, suspensionDate));
  }

  public void activateJobDefinitionByProcessDefinitionKey(String processDefinitionKey) {
    activateJobDefinitionByProcessDefinitionKey(processDefinitionKey, false);
  }

  public void activateJobDefinitionByProcessDefinitionKey(String processDefinitionKey, boolean activateJobs) {
    activateJobDefinitionByProcessDefinitionKey(processDefinitionKey, activateJobs, null);
  }

  public void activateJobDefinitionByProcessDefinitionKey(String processDefinitionKey, boolean activateJobs, Date activationDate) {
    commandExecutor.execute(new ActivateJobDefinitionCmd(null, null, processDefinitionKey, activateJobs, activationDate));
  }

  public void suspendJobDefinitionByProcessDefinitionKey(String processDefinitionKey) {
    suspendJobDefinitionByProcessDefinitionKey(processDefinitionKey, false);
  }

  public void suspendJobDefinitionByProcessDefinitionKey(String processDefinitionKey, boolean suspendJobs) {
    suspendJobDefinitionByProcessDefinitionKey(processDefinitionKey, suspendJobs, null);
  }

  public void suspendJobDefinitionByProcessDefinitionKey(String processDefinitionKey, boolean suspendJobs, Date suspensionDate) {
    commandExecutor.execute(new SuspendJobDefinitionCmd(null, null, processDefinitionKey, suspendJobs, suspensionDate));
  }

  public void activateJobById(String jobId) {
    commandExecutor.execute(new ActivateJobCmd(jobId, null, null, null, null));
  }

  public void activateJobByProcessInstanceId(String processInstanceId) {
    commandExecutor.execute(new ActivateJobCmd(null, null, processInstanceId, null, null));
  }

  public void activateJobByJobDefinitionId(String jobDefinitionId) {
    commandExecutor.execute(new ActivateJobCmd(null, jobDefinitionId, null, null, null));
  }

  public void activateJobByProcessDefinitionId(String processDefinitionId) {
    commandExecutor.execute(new ActivateJobCmd(null, null, null, processDefinitionId, null));
  }

  public void activateJobByProcessDefinitionKey(String processDefinitionKey) {
    commandExecutor.execute(new ActivateJobCmd(null, null, null, null, processDefinitionKey));
  }

  public void suspendJobById(String jobId) {
    commandExecutor.execute(new SuspendJobCmd(jobId, null, null, null, null));
  }

  public void suspendJobByJobDefinitionId(String jobDefinitionId) {
    commandExecutor.execute(new SuspendJobCmd(null, jobDefinitionId, null, null, null));
  }

  public void suspendJobByProcessInstanceId(String processInstanceId) {
    commandExecutor.execute(new SuspendJobCmd(null, null, processInstanceId, null, null));
  }

  public void suspendJobByProcessDefinitionId(String processDefinitionId) {
    commandExecutor.execute(new SuspendJobCmd(null, null, null, processDefinitionId, null));
  }

  public void suspendJobByProcessDefinitionKey(String processDefinitionKey) {
    commandExecutor.execute(new SuspendJobCmd(null, null, null, null, processDefinitionKey));
  }

}
