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
package org.camunda.bpm.engine.test.standalone.history;

import static org.camunda.bpm.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_CLEANUP_STRATEGY_END_TIME_BASED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.api.runtime.FailingDelegate;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.batch.BatchMigrationHelper;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CustomHistoryLevelIncidentTest {

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
      new Object[]{ Arrays.asList(HistoryEventTypes.INCIDENT_CREATE) },
      new Object[]{ Arrays.asList(HistoryEventTypes.INCIDENT_CREATE, HistoryEventTypes.INCIDENT_RESOLVE) },
      new Object[]{ Arrays.asList(HistoryEventTypes.INCIDENT_DELETE, HistoryEventTypes.INCIDENT_CREATE, HistoryEventTypes.INCIDENT_MIGRATE, HistoryEventTypes.INCIDENT_RESOLVE) }
    });
  }

  @Parameter(0)
  public static List<HistoryEventTypes> eventTypes;

  static CustomHistoryLevelIncident customHistoryLevelIncident = new CustomHistoryLevelIncident(eventTypes);

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration -> {
    configuration.setJdbcUrl("jdbc:h2:mem:" + CustomHistoryLevelIncident.class.getSimpleName());
    List<HistoryLevel> levels = new ArrayList<>();
    levels.add(customHistoryLevelIncident);
    configuration.setCustomHistoryLevels(levels);
    configuration.setHistory("aCustomHistoryLevelIncident");
    configuration.setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_CREATE_DROP);
  });
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected MigrationTestRule migrationRule = new MigrationTestRule(engineRule);
  protected BatchMigrationHelper migrationHelper = new BatchMigrationHelper(engineRule, migrationRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule).around(migrationRule);

  protected HistoryService historyService;
  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected RepositoryService repositoryService;
  protected TaskService taskService;
  protected ProcessEngineConfigurationImpl configuration;

  DeploymentWithDefinitions deployment;

  public static String PROCESS_DEFINITION_KEY = "oneFailingServiceTaskProcess";
  public static BpmnModelInstance FAILING_SERVICE_TASK_MODEL  = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
    .startEvent("start")
    .serviceTask("task")
      .camundaAsyncBefore()
      .camundaClass(FailingDelegate.class.getName())
    .endEvent("end")
    .done();

  @Before
  public void setUp() throws Exception {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();
    repositoryService = engineRule.getRepositoryService();
    taskService = engineRule.getTaskService();
    configuration = engineRule.getProcessEngineConfiguration();

    customHistoryLevelIncident.setEventTypes(eventTypes);
    configuration.setHistoryCleanupStrategy(HISTORY_CLEANUP_STRATEGY_END_TIME_BASED);
  }

  @After
  public void tearDown() throws Exception {
    customHistoryLevelIncident.setEventTypes(null);
    if (deployment != null) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
    migrationHelper.removeAllRunningAndHistoricBatches();

    configuration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        List<Job> jobs = managementService.createJobQuery().list();
        for (Job job : jobs) {
          commandContext.getJobManager().deleteJob((JobEntity) job);
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(job.getId());
        }

        List<HistoricIncident> historicIncidents = historyService.createHistoricIncidentQuery().list();
        for (HistoricIncident historicIncident : historicIncidents) {
          commandContext.getDbEntityManager().delete((HistoricIncidentEntity) historicIncident);
        }

        commandContext.getMeterLogManager().deleteAll();

        return null;
      }
    });
  }

  @Test
  public void testDeleteHistoricIncidentByProcDefId() {
    // given
    deployment = repositoryService.createDeployment().addModelInstance("process.bpmn", FAILING_SERVICE_TASK_MODEL).deployWithResult();
    String processDefinitionId = deployment.getDeployedProcessDefinitions().get(0).getId();

    runtimeService.startProcessInstanceById(processDefinitionId);
    executeAvailableJobs();


    if (eventTypes != null) {
      HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().singleResult();
      assertNotNull(historicIncident);
    }

    // when
    repositoryService.deleteProcessDefinitions()
      .byKey(PROCESS_DEFINITION_KEY)
      .cascade()
      .delete();

    // then
    List<HistoricIncident> incidents = historyService.createHistoricIncidentQuery().list();
    assertEquals(0, incidents.size());
  }

  @Test
  public void testDeleteHistoricIncidentByBatchId() {
    // given
    initBatchOperationHistoryTimeToLive();
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), -11));

    BatchEntity batch = (BatchEntity) createFailingMigrationBatch();

    migrationHelper.completeSeedJobs(batch);

    List<Job> list = managementService.createJobQuery().list();
    for (Job job : list) {
      if (((JobEntity) job).getJobHandlerType().equals("instance-migration")) {
        managementService.setJobRetries(job.getId(), 1);
      }
    }
    migrationHelper.executeJobs(batch);

    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), -10));
    managementService.deleteBatch(batch.getId(), false);
    ClockUtil.setCurrentTime(new Date());

    // assume
    if (eventTypes != null) {
      HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().singleResult();
      assertNotNull(historicIncident);
    }

    // when
    historyService.cleanUpHistoryAsync(true);
    for (Job job : historyService.findHistoryCleanupJobs()) {
      managementService.executeJob(job.getId());
    }

    // then
    List<HistoricIncident> incidents = historyService.createHistoricIncidentQuery().list();
    assertEquals(0, incidents.size());
  }

  @Test
  public void testDeleteHistoricIncidentByJobDefinitionId() {
    // given
    BatchEntity batch = (BatchEntity) createFailingMigrationBatch();

    migrationHelper.completeSeedJobs(batch);

    List<Job> list = managementService.createJobQuery().list();
    for (Job job : list) {
      if (((JobEntity) job).getJobHandlerType().equals("instance-migration")) {
        managementService.setJobRetries(job.getId(), 1);
      }
    }
    migrationHelper.executeJobs(batch);

    // assume
    if (eventTypes != null) {
      HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().singleResult();
      assertNotNull(historicIncident);
    }

    // when
    managementService.deleteBatch(batch.getId(), true);

    // then
    List<HistoricIncident> incidents = historyService.createHistoricIncidentQuery().list();
    assertEquals(0, incidents.size());
  }

  protected void executeAvailableJobs() {
    List<Job> jobs = managementService.createJobQuery().withRetriesLeft().list();

    if (jobs.isEmpty()) {
      return;
    }

    for (Job job : jobs) {
      try {
        managementService.executeJob(job.getId());
      } catch (Exception e) {}
    }

    executeAvailableJobs();
  }

  protected void initBatchOperationHistoryTimeToLive() {
    configuration.setBatchOperationHistoryTimeToLive("P0D");
    configuration.initHistoryCleanup();
  }

  protected Batch createFailingMigrationBatch() {
    BpmnModelInstance instance = createModelInstance();

    ProcessDefinition sourceProcessDefinition = migrationRule.deployAndGetDefinition(instance);
    ProcessDefinition targetProcessDefinition = migrationRule.deployAndGetDefinition(instance);

    MigrationPlan migrationPlan = runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());

    Batch batch = runtimeService.newMigration(migrationPlan).processInstanceIds(Arrays.asList(processInstance.getId(), "unknownId")).executeAsync();
    return batch;
  }

  protected BpmnModelInstance createModelInstance() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess("process")
        .startEvent("start")
        .userTask("userTask1")
        .endEvent("end")
        .done();
    return instance;
  }
}
