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
package org.camunda.bpm.engine.test.api.runtime.migration.history;

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;

import java.util.Arrays;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.AsyncProcessModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class MigrationHistoricIncidentTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected ManagementService managementService;
  protected RepositoryService repositoryService;

  @Before
  public void initServices() {
    historyService = rule.getHistoryService();
    runtimeService = rule.getRuntimeService();
    managementService = rule.getManagementService();
    repositoryService = rule.getRepositoryService();
  }

  @Test
  public void testMigrateHistoricIncident() {
    // given
    ProcessDefinition sourceProcess = testHelper.deployAndGetDefinition(AsyncProcessModels.ASYNC_BEFORE_USER_TASK_PROCESS);
    ProcessDefinition targetProcess = testHelper.deployAndGetDefinition(modify(AsyncProcessModels.ASYNC_BEFORE_USER_TASK_PROCESS)
      .changeElementId(ProcessModels.PROCESS_KEY, "new" + ProcessModels.PROCESS_KEY)
      .changeElementId("userTask", "newUserTask"));

    JobDefinition targetJobDefinition =
        managementService
          .createJobDefinitionQuery()
          .processDefinitionId(targetProcess.getId())
          .singleResult();

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcess.getId(), targetProcess.getId())
      .mapActivities("userTask", "newUserTask")
      .build();

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceProcess.getId());

    Job job = managementService.createJobQuery().singleResult();
    managementService.setJobRetries(job.getId(), 0);

    HistoricIncident incidentBeforeMigration = historyService.createHistoricIncidentQuery().singleResult();

    // when
    runtimeService.newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    // then
    HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().singleResult();
    Assert.assertNotNull(historicIncident);

    Assert.assertEquals("newUserTask", historicIncident.getActivityId());
    Assert.assertEquals(targetJobDefinition.getId(), historicIncident.getJobDefinitionId());
    Assert.assertEquals(targetProcess.getId(), historicIncident.getProcessDefinitionId());
    Assert.assertEquals(targetProcess.getKey(), historicIncident.getProcessDefinitionKey());
    Assert.assertEquals(processInstance.getId(), historicIncident.getExecutionId());

    // and other properties have not changed
    Assert.assertEquals(incidentBeforeMigration.getCreateTime(), historicIncident.getCreateTime());
    Assert.assertEquals(incidentBeforeMigration.getProcessInstanceId(), historicIncident.getProcessInstanceId());

  }

  @Test
  public void testMigrateHistoricIncidentAddScope() {
    // given
    ProcessDefinition sourceProcess = testHelper.deployAndGetDefinition(AsyncProcessModels.ASYNC_BEFORE_USER_TASK_PROCESS);
    ProcessDefinition targetProcess = testHelper.deployAndGetDefinition(AsyncProcessModels.ASYNC_BEFORE_SUBPROCESS_USER_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcess.getId(), targetProcess.getId())
      .mapActivities("userTask", "userTask")
      .build();

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceProcess.getId());

    Job job = managementService.createJobQuery().singleResult();
    managementService.setJobRetries(job.getId(), 0);

    // when
    runtimeService.newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    // then
    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstance.getId());

    HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().singleResult();
    Assert.assertNotNull(historicIncident);
    Assert.assertEquals(
        activityInstance.getTransitionInstances("userTask")[0].getExecutionId(),
        historicIncident.getExecutionId());
  }
}
