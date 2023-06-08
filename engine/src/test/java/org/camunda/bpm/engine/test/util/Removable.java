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

package org.camunda.bpm.engine.test.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.TimerSuspendProcessDefinitionHandler;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class modeling the corresponding entity removal of Domain Classes.
 */
public final class Removable {

  private static final Logger LOG = LoggerFactory.getLogger(Removable.class);

  private final ProcessEngine engine;
  private final Map<Class<?>, ThrowingRunnable> mappings;

  /**
   * New Domain Classes & the deletion of their respective Entities goes here.
   */
  private Removable(ProcessEngine engine) {
    Map<Class<?>, ThrowingRunnable> mappings = new HashMap<>();

    mappings.put(Task.class, this::removeAllTasks);
    mappings.put(ProcessInstance.class, this::removeAllProcessInstances);
    mappings.put(Deployment.class, this::removeAllDeployments);

    mappings.put(Incident.class, this::removeAllIncidents);
    mappings.put(HistoricIncident.class, this::removeAllHistoricIncidents);

    mappings.put(HistoricProcessInstance.class, this::removeAllHistoricProcessInstances);
    mappings.put(HistoricDecisionInstance.class, this::removeAllHistoricDecisionInstances);
    mappings.put(HistoricCaseInstance.class, this::removeAllHistoricCaseInstances);

    mappings.put(HistoryCleanupJobDeclaration.class, this::removeHistoryCleanupJobRelatedEntries);

    // Add here new mappings with [class - associated remove method]

    this.engine = engine;
    this.mappings = mappings;
  }

  /**
   * Static Creation method.
   *
   * @param rule the process engine test rule, non-null.
   * @return the {@link Removable}
   */
  public static Removable of(ProcessEngineTestRule rule) {
    Objects.requireNonNull(rule);
    Objects.requireNonNull(rule.processEngineRule);

    return of(rule.processEngineRule.getProcessEngine());
  }

  public static Removable of(ProcessEngine engine) {
    return new Removable(engine);
  }

  /**
   * Removes the associated mapped entities from the db for the given class.
   *
   * @param clazz the given class to delete associated entities for
   * @throws EntityRemoveException in case anything fails during the process of deletion
   */
  public void remove(Class<?> clazz) throws EntityRemoveException {
    Objects.requireNonNull(clazz, "remove does not accept null arguments");

    ThrowingRunnable runnable = mappings.get(clazz);

    if (runnable == null) {
      throw new UnsupportedOperationException("class " + clazz.getName() + " is not supported yet for Removal");
    }

    if (!isInitialized()) {
      throw new EntityRemoveException("Removable is not initialized");
    }

    try {
      runnable.execute();
    } catch (Exception e) {
      throw new EntityRemoveException(e);
    }
  }

  /**
   * Removes the associated mapped entities from the db for the given classes.
   *
   * @param classes the given classes to delete associated entities for
   * @throws EntityRemoveException in case anything fails during the process of deletion for any of the classes
   */
  public void remove(Class<?>[] classes) throws EntityRemoveException {
    Objects.requireNonNull(classes, "remove does not accept null arguments");

    for (Class<?> clazz : classes) {
      remove(clazz);
    }
  }

  /**
   * Removes associated mapped entities for all known classes.
   *
   * @throws EntityRemoveException in case anything fails during the process of deletion for any of the classes
   */
  public void removeAll() throws EntityRemoveException {
    try {
      for (Map.Entry<Class<?>, ThrowingRunnable> entry : mappings.entrySet()) {
        ThrowingRunnable runnable = entry.getValue();

        runnable.execute();
      }
    } catch (Exception e) {
      throw new EntityRemoveException(e);
    }
  }

  private void removeHistoryCleanupJobRelatedEntries() {
    HistoryService historyService = engine.getHistoryService();
    ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration();

    config.getCommandExecutorTxRequired().execute((Command<Void>) context -> {

      List<Job> jobs = historyService.findHistoryCleanupJobs();

      for (Job job : jobs) {
        context.getJobManager().deleteJob((JobEntity) job);
        context.getHistoricJobLogManager().deleteHistoricJobLogByJobId(job.getId());
      }

      final List<HistoricJobLog> list = historyService.createHistoricJobLogQuery().list();
      for (HistoricJobLog jobLog : list) {
        context.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobLog.getJobId());
      }

      List<HistoricIncident> historicIncidents = historyService.createHistoricIncidentQuery().list();
      for (HistoricIncident historicIncident : historicIncidents) {
        context.getDbEntityManager().delete((HistoricIncidentEntity) historicIncident);
      }

      context.getMeterLogManager().deleteAll();
      return null;
    });
  }

  private void removeAllHistoricProcessInstances() {
    HistoryService historyService = engine.getHistoryService();
    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
      historyService.deleteHistoricProcessInstance(historicProcessInstance.getId());
    }
  }

  private void removeAllHistoricDecisionInstances() {
    HistoryService historyService = engine.getHistoryService();
    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
        .list();
    for (HistoricDecisionInstance historicDecisionInstance : historicDecisionInstances) {
      historyService.deleteHistoricDecisionInstanceByInstanceId(historicDecisionInstance.getId());
    }
  }

  private void removeAllHistoricCaseInstances() {
    HistoryService historyService = engine.getHistoryService();
    List<HistoricCaseInstance> historicCaseInstances = historyService.createHistoricCaseInstanceQuery().list();
    for (HistoricCaseInstance historicCaseInstance : historicCaseInstances) {
      historyService.deleteHistoricCaseInstance(historicCaseInstance.getId());
    }
  }

  private void removeAllTasks() {
    try {
      TaskService taskService = engine.getTaskService();
      List<Task> tasks = taskService.createTaskQuery().list();
      for (Task task : tasks) {
        LOG.debug("deleteTask with taskId: {}", task.getId());
        taskService.deleteTask(task.getId(), true);
      }
    } catch (Exception e) {
      throw new EntityRemoveException(e);
    }
  }

  private void removeAllDeployments() {
    RepositoryService repositoryService = engine.getRepositoryService();
    for (Deployment deployment : engine.getRepositoryService().createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  private void removeAllProcessInstances() {
    try {
      RuntimeService runtimeService = engine.getRuntimeService();
      for (ProcessInstance processInstance : runtimeService.createProcessInstanceQuery().list()) {
        runtimeService.deleteProcessInstance(processInstance.getId(), "test ended", true);
      }
    } catch (Exception e) {
      throw new EntityRemoveException(e);
    }
  }

  private void removeAllHistoricIncidents() {
    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration();
    CommandExecutor commandExecutor = engineConfiguration.getCommandExecutorTxRequired();

    commandExecutor.execute(commandContext -> {
      HistoryLevel historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();

      if (historyLevel.equals(HistoryLevel.HISTORY_LEVEL_FULL)) {
        commandContext.getHistoricJobLogManager()
            .deleteHistoricJobLogsByHandlerType(TimerSuspendProcessDefinitionHandler.TYPE);

        List<HistoricIncident> incidents = Context.getProcessEngineConfiguration()
            .getHistoryService()
            .createHistoricIncidentQuery()
            .list();

        for (HistoricIncident incident : incidents) {
          commandContext.getHistoricIncidentManager().delete((HistoricIncidentEntity) incident);
        }
      }
      return null;
    });
  }

  private void removeAllIncidents() {
    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration();
    CommandExecutor commandExecutor = engineConfiguration.getCommandExecutorTxRequired();

    commandExecutor.execute(commandContext -> {
      HistoryLevel historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();

      if (historyLevel.equals(HistoryLevel.HISTORY_LEVEL_FULL)) {
        commandContext.getHistoricJobLogManager()
            .deleteHistoricJobLogsByHandlerType(TimerSuspendProcessDefinitionHandler.TYPE);

        List<Incident> incidents = Context.getProcessEngineConfiguration()
            .getRuntimeService()
            .createIncidentQuery()
            .list();

        for (Incident incident : incidents) {
          commandContext.getIncidentManager().delete((IncidentEntity) incident);
        }
      }
      return null;
    });
  }

  public boolean isInitialized() {
    return engine != null;
  }

}

/**
 * Exception thrown if the mapped entities to be deleted for a class fail to be removed.
 */
class EntityRemoveException extends RuntimeException {
  public EntityRemoveException(Exception e) {
    super(e);
  }

  public EntityRemoveException(String message) {
    super(message);
  }
}

/**
 * Functional interface used locally to pass functions that can throw exceptions as arguments.
 */
@FunctionalInterface
interface ThrowingRunnable {
  void execute() throws Exception;
}