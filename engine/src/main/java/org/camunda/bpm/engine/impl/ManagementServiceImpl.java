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
import org.camunda.bpm.engine.impl.cmd.ActivateDeploymentForApplicationCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteJobCmd;
import org.camunda.bpm.engine.impl.cmd.ExecuteJobsCmd;
import org.camunda.bpm.engine.impl.cmd.GetJobExceptionStacktraceCmd;
import org.camunda.bpm.engine.impl.cmd.GetProcessApplicationForDeployment;
import org.camunda.bpm.engine.impl.cmd.GetPropertiesCmd;
import org.camunda.bpm.engine.impl.cmd.GetTableCountCmd;
import org.camunda.bpm.engine.impl.cmd.GetTableMetaDataCmd;
import org.camunda.bpm.engine.impl.cmd.GetTableNameCmd;
import org.camunda.bpm.engine.impl.cmd.RegisterDeploymentCmd;
import org.camunda.bpm.engine.impl.cmd.SetJobRetriesCmd;
import org.camunda.bpm.engine.impl.cmd.UnregisterDeploymentCmd;
import org.camunda.bpm.engine.impl.cmd.UnregisterProcessApplication;
import org.camunda.bpm.engine.impl.cmd.SetJobDuedateCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.db.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.management.ActivityStatisticsQuery;
import org.camunda.bpm.engine.management.DeploymentStatisticsQuery;
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
    return commandExecutor.execute(new ActivateDeploymentForApplicationCmd(deploymentId, reference));
  }
  
  public boolean unregisterProcessApplication(String deploymentId, boolean removeProcessesFromCache) {
    return commandExecutor.execute(new UnregisterProcessApplication(deploymentId, removeProcessesFromCache));        
  }
  
  public String getProcessApplicationForDeployment(String deploymentId) {
    return commandExecutor.execute(new GetProcessApplicationForDeployment(deploymentId));
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
    commandExecutor.execute(new SetJobRetriesCmd(jobId, retries));
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

  public String getJobExceptionStacktrace(String jobId) {
    return commandExecutor.execute(new GetJobExceptionStacktraceCmd(jobId));
  }

  public Map<String, String> getProperties() {
    return commandExecutor.execute(new GetPropertiesCmd());
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
        synchronized (registeredDeployments) {
          return new HashSet<String>(registeredDeployments);
        }
      }
    });
  }

  public void registerDeploymentForJobExecutor(final String deploymentId) {
    commandExecutor.execute(new RegisterDeploymentCmd(deploymentId));
  }

  @Override
  public void unregisterDeploymentForJobExecutor(final String deploymentId) {
    commandExecutor.execute(new UnregisterDeploymentCmd(deploymentId));
  }

}
