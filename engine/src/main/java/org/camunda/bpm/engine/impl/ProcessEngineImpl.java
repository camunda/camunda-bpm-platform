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

import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.TransactionContextFactory;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.metrics.reporter.DbMetricsReporter;

/**
 * @author Tom Baeyens
 */
public class ProcessEngineImpl implements ProcessEngine {

  public static final ReentrantLock LOCK_MONITOR = new ReentrantLock(false);
  public static final Condition IS_EXTERNAL_TASK_AVAILABLE = LOCK_MONITOR.newCondition();

  private final static ProcessEngineLogger LOG = ProcessEngineLogger.INSTANCE;

  protected String name;

  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected HistoryService historicDataService;
  protected IdentityService identityService;
  protected TaskService taskService;
  protected FormService formService;
  protected ManagementService managementService;
  protected AuthorizationService authorizationService;
  protected CaseService caseService;
  protected FilterService filterService;
  protected ExternalTaskService externalTaskService;
  protected DecisionService decisionService;

  protected String databaseSchemaUpdate;
  protected JobExecutor jobExecutor;
  protected CommandExecutor commandExecutor;
  protected CommandExecutor commandExecutorSchemaOperations;
  protected Map<Class<?>, SessionFactory> sessionFactories;
  protected ExpressionManager expressionManager;
  protected HistoryLevel historyLevel;
  protected TransactionContextFactory transactionContextFactory;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  public ProcessEngineImpl(ProcessEngineConfigurationImpl processEngineConfiguration) {

    this.processEngineConfiguration = processEngineConfiguration;
    this.name = processEngineConfiguration.getProcessEngineName();

    this.repositoryService = processEngineConfiguration.getRepositoryService();
    this.runtimeService = processEngineConfiguration.getRuntimeService();
    this.historicDataService = processEngineConfiguration.getHistoryService();
    this.identityService = processEngineConfiguration.getIdentityService();
    this.taskService = processEngineConfiguration.getTaskService();
    this.formService = processEngineConfiguration.getFormService();
    this.managementService = processEngineConfiguration.getManagementService();
    this.authorizationService = processEngineConfiguration.getAuthorizationService();
    this.caseService = processEngineConfiguration.getCaseService();
    this.filterService = processEngineConfiguration.getFilterService();
    this.externalTaskService = processEngineConfiguration.getExternalTaskService();
    this.decisionService = processEngineConfiguration.getDecisionService();

    this.databaseSchemaUpdate = processEngineConfiguration.getDatabaseSchemaUpdate();
    this.jobExecutor = processEngineConfiguration.getJobExecutor();
    this.commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutorSchemaOperations = processEngineConfiguration.getCommandExecutorSchemaOperations();
    this.sessionFactories = processEngineConfiguration.getSessionFactories();
    this.historyLevel = processEngineConfiguration.getHistoryLevel();
    this.transactionContextFactory = processEngineConfiguration.getTransactionContextFactory();

    executeSchemaOperations();

    if (name == null) {
      LOG.processEngineCreated(ProcessEngines.NAME_DEFAULT);
    } else {
      LOG.processEngineCreated(name);
    }

    ProcessEngines.registerProcessEngine(this);

    if ((jobExecutor != null)) {
      // register process engine with Job Executor
      jobExecutor.registerProcessEngine(this);
    }

    if (processEngineConfiguration.isMetricsEnabled()) {
      String reporterId = processEngineConfiguration.getMetricsReporterIdProvider().provideId(this);
      DbMetricsReporter dbMetricsReporter = processEngineConfiguration.getDbMetricsReporter();
      dbMetricsReporter.setReporterId(reporterId);

      if(processEngineConfiguration.isDbMetricsReporterActivate()) {
        dbMetricsReporter.start();
      }
    }
  }

  protected void executeSchemaOperations() {
    commandExecutorSchemaOperations.execute(processEngineConfiguration.getSchemaOperationsCommand());
    commandExecutorSchemaOperations.execute(processEngineConfiguration.getHistoryLevelCommand());

    try {
      commandExecutorSchemaOperations.execute(processEngineConfiguration.getProcessEngineBootstrapCommand());
    } catch (OptimisticLockingException ole) {
      LOG.historyCleanupJobReconfigurationFailure(ole);
    }
  }

  @Override
  public void close() {

    ProcessEngines.unregister(this);

    if(processEngineConfiguration.isMetricsEnabled()) {
      processEngineConfiguration.getDbMetricsReporter().stop();
    }

    if ((jobExecutor != null)) {
      // unregister process engine with Job Executor
      jobExecutor.unregisterProcessEngine(this);
    }

    commandExecutorSchemaOperations.execute(new SchemaOperationProcessEngineClose());

    processEngineConfiguration.close();

    LOG.processEngineClosed(name);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  @Override
  public IdentityService getIdentityService() {
    return identityService;
  }

  @Override
  public ManagementService getManagementService() {
    return managementService;
  }

  @Override
  public TaskService getTaskService() {
    return taskService;
  }

  @Override
  public HistoryService getHistoryService() {
    return historicDataService;
  }

  @Override
  public RuntimeService getRuntimeService() {
    return runtimeService;
  }

  @Override
  public RepositoryService getRepositoryService() {
    return repositoryService;
  }

  @Override
  public FormService getFormService() {
    return formService;
  }

  @Override
  public AuthorizationService getAuthorizationService() {
    return authorizationService;
  }

  @Override
  public CaseService getCaseService() {
    return caseService;
  }

  @Override
  public FilterService getFilterService() {
    return filterService;
  }

  @Override
  public ExternalTaskService getExternalTaskService() {
    return externalTaskService;
  }

  @Override
  public DecisionService getDecisionService() {
    return decisionService;
  }

}
