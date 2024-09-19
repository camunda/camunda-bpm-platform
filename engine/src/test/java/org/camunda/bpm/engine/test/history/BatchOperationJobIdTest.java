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
package org.camunda.bpm.engine.test.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.batch.BatchMonitorJobHandler;
import org.camunda.bpm.engine.impl.batch.BatchSeedJobHandler;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.history.removaltime.batch.helper.BatchSetRemovalTimeRule;
import org.camunda.bpm.engine.test.util.BatchRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class BatchOperationJobIdTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  protected BatchRule batchRule = new BatchRule(engineRule, testRule);
  protected BatchSetRemovalTimeRule batchRemovalTimeRule = new BatchSetRemovalTimeRule(engineRule, testRule);

  @Rule
  public RuleChain ruleChain = RuleChain
      .outerRule(engineRule)
      .around(testRule)
      .around(batchRule)
      .around(batchRemovalTimeRule);

  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected ManagementService managementService;
  protected DecisionService decisionService;
  protected ExternalTaskService externalTaskService;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();
    decisionService = engineRule.getDecisionService();
    externalTaskService = engineRule.getExternalTaskService();
  }

  private BpmnModelInstance getUserTaskProcess() {
    return Bpmn.createExecutableProcess("process")
        .startEvent()
        .userTask("task1")
        .endEvent()
        .done();
  }

  private BpmnModelInstance getTwoUserTasksProcess() {
    return Bpmn.createExecutableProcess("process")
        .startEvent()
        .userTask("task1")
        .userTask("task2")
        .endEvent()
        .done();
  }

  private BpmnModelInstance getTimerProcess() {
    return Bpmn.createExecutableProcess("process")
        .startEvent()
        .timerWithDuration("PT5H")
        .endEvent()
        .done();
  }

  @Test
  public void shouldSetBatchIdOnJobAndJobLog_SetHistoricBatchRemovalTime() {
    // given
    testRule.deploy(getUserTaskProcess());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // create historic Batch
    Batch setVariablesBatch = runtimeService.setVariablesAsync(List.of(processInstance.getId()),
        Variables.createVariables().putValue("foo", "bar"));
    batchRule.syncExec(setVariablesBatch);

    // set historic batch removal time
    Batch setRemovalTimeBatch = historyService.setRemovalTimeToHistoricBatches()
        .absoluteRemovalTime(batchRemovalTimeRule.REMOVAL_TIME)
        .byQuery(historyService.createHistoricBatchQuery())
        .executeAsync();

    // when
    batchRule.syncExec(setRemovalTimeBatch);

    // then
    /*
     * there is no way to filter for the seed and monitor jobs of only the set historic batch removal time batch
     * as a workaround: check that both seed and monitor jobs have the two batch ids (from set variables and set batch
     * removal time batches) attached.
     */

    assertThat(setRemovalTimeBatch.getId()).isNotNull();
    List<HistoricJobLog> batchSeedJobLogs = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchSeedJobHandler.TYPE)
        .list();
    assertThat(batchSeedJobLogs).extracting("batchId")
        .containsExactlyInAnyOrder(setVariablesBatch.getId(), setRemovalTimeBatch.getId());
    List<HistoricJobLog> batchMonitorJobLogs = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchMonitorJobHandler.TYPE)
        .list();
    assertThat(batchSeedJobLogs).extracting("batchId")
        .containsExactlyInAnyOrder(setVariablesBatch.getId(), setRemovalTimeBatch.getId());
    HistoricJobLog batchExecutionJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(Batch.TYPE_BATCH_SET_REMOVAL_TIME)
        .singleResult();
    assertThat(batchExecutionJobLog.getBatchId()).isEqualTo(setRemovalTimeBatch.getId());
  }

  @Test
  public void shouldSetBatchIdOnJobAndJobLog_SetVariables() {
    // given
    testRule.deploy(getUserTaskProcess());
    ProcessInstance process = runtimeService.startProcessInstanceByKey("process");

    Batch batch = runtimeService.setVariablesAsync(List.of(process.getId()), Variables.createVariables().putValue("foo", "bar"));

    // when
    batchRule.syncExec(batch);

    // then
    assertThat(batch.getId()).isNotNull();
    HistoricJobLog batchSeedJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchSeedJobHandler.TYPE)
        .singleResult();
    assertThat(batchSeedJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchMonitorJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchMonitorJobHandler.TYPE)
        .singleResult();
    assertThat(batchMonitorJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchExecutionJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(Batch.TYPE_SET_VARIABLES)
        .singleResult();
    assertThat(batchExecutionJobLog.getBatchId()).isEqualTo(batch.getId());
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetBatchIdOnJobAndJobLog_DecisionSetRemovalTime() {
    // given
    decisionService.evaluateDecisionByKey("dish-decision")
        .variables(
          Variables.createVariables()
            .putValue("temperature", 32)
            .putValue("dayType", "Weekend")
        ).evaluate();

    Batch batch = historyService.setRemovalTimeToHistoricDecisionInstances()
        .absoluteRemovalTime(batchRemovalTimeRule.REMOVAL_TIME)
        .byQuery(historyService.createHistoricDecisionInstanceQuery())
        .executeAsync();

    // when
    batchRule.syncExec(batch);

    // then
    assertThat(batch.getId()).isNotNull();
    HistoricJobLog batchSeedJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchSeedJobHandler.TYPE)
        .singleResult();
    assertThat(batchSeedJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchMonitorJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchMonitorJobHandler.TYPE)
        .singleResult();
    assertThat(batchMonitorJobLog.getBatchId()).isEqualTo(batch.getId());
    List<HistoricJobLog> batchExecutionJobLogs = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(Batch.TYPE_DECISION_SET_REMOVAL_TIME)
        .list();
    assertThat(batchExecutionJobLogs).hasSize(3);
    assertThat(batchExecutionJobLogs).extracting("batchId").containsOnly(batch.getId());
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetBatchIdOnJobAndJobLog_DeleteHistoricDecisionInstances() {
    // given
    decisionService.evaluateDecisionByKey("dish-decision")
        .variables(
            Variables.createVariables()
                .putValue("temperature", 32)
                .putValue("dayType", "Weekend")
        ).evaluate();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery()
        .decisionDefinitionKey("dish-decision");

    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(query, null);

    // when
    batchRule.syncExec(batch);

    // then
    assertThat(batch.getId()).isNotNull();
    HistoricJobLog batchSeedJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchSeedJobHandler.TYPE)
        .singleResult();
    assertThat(batchSeedJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchMonitorJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchMonitorJobHandler.TYPE)
        .singleResult();
    assertThat(batchMonitorJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchExecutionJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(Batch.TYPE_HISTORIC_DECISION_INSTANCE_DELETION)
        .singleResult();
    assertThat(batchExecutionJobLog.getBatchId()).isEqualTo(batch.getId());
  }

  @Test
  public void shouldSetBatchIdOnJobAndJobLog_DeleteHistoricProcessInstances() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess("process")
        .startEvent()
        .endEvent()
        .done());
    ProcessInstance process = runtimeService.startProcessInstanceByKey("process");

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
        .processDefinitionKey("process");

    Batch batch = historyService.deleteHistoricProcessInstancesAsync(query, null);

    // when
    batchRule.syncExec(batch);

    // then
    assertThat(batch.getId()).isNotNull();
    HistoricJobLog batchSeedJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchSeedJobHandler.TYPE)
        .singleResult();
    assertThat(batchSeedJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchMonitorJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchMonitorJobHandler.TYPE)
        .singleResult();
    assertThat(batchMonitorJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchExecutionJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
        .singleResult();
    assertThat(batchExecutionJobLog.getBatchId()).isEqualTo(batch.getId());
  }

  @Test
  public void shouldSetBatchIdOnJobAndJobLog_DeleteProcessInstances() {
    // given
    testRule.deploy(getUserTaskProcess());
    ProcessInstance process = runtimeService.startProcessInstanceByKey("process");

    Batch batch = runtimeService.deleteProcessInstancesAsync(List.of(process.getId()), null);

    // when
    batchRule.syncExec(batch);

    // then
    assertThat(batch.getId()).isNotNull();
    HistoricJobLog batchSeedJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchSeedJobHandler.TYPE)
        .singleResult();
    assertThat(batchSeedJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchMonitorJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchMonitorJobHandler.TYPE)
        .singleResult();
    assertThat(batchMonitorJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchExecutionJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(Batch.TYPE_PROCESS_INSTANCE_DELETION)
        .singleResult();
    assertThat(batchExecutionJobLog.getBatchId()).isEqualTo(batch.getId());
  }

  @Test
  public void shouldSetBatchIdOnJobAndJobLog_MessageCorrelation() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess("process")
        .startEvent()
        .intermediateCatchEvent().message("message")
        .userTask()
        .endEvent()
        .done());
    ProcessInstance process = runtimeService.startProcessInstanceByKey("process");

    Batch batch = runtimeService.createMessageCorrelationAsync("message")
        .processInstanceIds(List.of(process.getId()))
        .correlateAllAsync();

    // when
    batchRule.syncExec(batch);

    // then
    assertThat(batch.getId()).isNotNull();
    HistoricJobLog batchSeedJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchSeedJobHandler.TYPE)
        .singleResult();
    assertThat(batchSeedJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchMonitorJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchMonitorJobHandler.TYPE)
        .singleResult();
    assertThat(batchMonitorJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchExecutionJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(Batch.TYPE_CORRELATE_MESSAGE)
        .singleResult();
    assertThat(batchExecutionJobLog.getBatchId()).isEqualTo(batch.getId());
  }

  @Test
  public void shouldSetBatchIdOnJobAndJobLog_Migration() {
    // given
    ProcessDefinition sourceProcessDefinition = testRule.deployAndGetDefinition(getUserTaskProcess());
    ProcessInstance process = runtimeService.startProcessInstanceByKey("process");
    ProcessDefinition targetProcessDefinition = testRule.deployAndGetDefinition(getTwoUserTasksProcess());

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapEqualActivities()
        .build();

    Batch batch = runtimeService.newMigration(migrationPlan).processInstanceIds(List.of(process.getId())).executeAsync();

    // when
    batchRule.syncExec(batch);

    // then
    assertThat(batch.getId()).isNotNull();
    HistoricJobLog batchSeedJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchSeedJobHandler.TYPE)
        .singleResult();
    assertThat(batchSeedJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchMonitorJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchMonitorJobHandler.TYPE)
        .singleResult();
    assertThat(batchMonitorJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchExecutionJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(Batch.TYPE_PROCESS_INSTANCE_MIGRATION)
        .singleResult();
    assertThat(batchExecutionJobLog.getBatchId()).isEqualTo(batch.getId());
  }

  @Test
  public void shouldSetBatchIdOnJobAndJobLog_Modification() {
    // given
    testRule.deploy(getTwoUserTasksProcess());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    // when
    Batch batch = runtimeService.createProcessInstanceModification(processInstance.getId())
        .cancelActivityInstance(tree.getActivityInstances("task1")[0].getId())
        .startBeforeActivity("task2")
        .executeAsync();

    // when
    batchRule.syncExec(batch);

    // then
    assertThat(batch.getId()).isNotNull();
    HistoricJobLog batchSeedJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchSeedJobHandler.TYPE)
        .singleResult();
    assertThat(batchSeedJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchMonitorJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchMonitorJobHandler.TYPE)
        .singleResult();
    assertThat(batchMonitorJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchExecutionJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(Batch.TYPE_PROCESS_INSTANCE_MODIFICATION)
        .singleResult();
    assertThat(batchExecutionJobLog.getBatchId()).isEqualTo(batch.getId());
  }

  @Test
  public void shouldSetBatchIdOnJobAndJobLog_ProcessSetRemovalTime() {
    // given
    testRule.deploy(getTwoUserTasksProcess());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Batch batch = historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(batchRemovalTimeRule.REMOVAL_TIME)
        .byIds(processInstance.getId())
        .executeAsync();

    // when
    batchRule.syncExec(batch);

    // then
    assertThat(batch.getId()).isNotNull();
    HistoricJobLog batchSeedJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchSeedJobHandler.TYPE)
        .singleResult();
    assertThat(batchSeedJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchMonitorJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchMonitorJobHandler.TYPE)
        .singleResult();
    assertThat(batchMonitorJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchExecutionJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(Batch.TYPE_PROCESS_SET_REMOVAL_TIME)
        .singleResult();
    assertThat(batchExecutionJobLog.getBatchId()).isEqualTo(batch.getId());
  }

  @Test
  public void shouldSetBatchIdOnJobAndJobLog_RestartProcessInstance() {
    // given
    testRule.deploy(getUserTaskProcess());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    Batch batch = runtimeService.restartProcessInstances(processInstance.getProcessDefinitionId())
        .processInstanceIds(processInstance.getId())
        .startBeforeActivity("task1")
        .executeAsync();

    // when
    batchRule.syncExec(batch);

    // then
    assertThat(batch.getId()).isNotNull();
    HistoricJobLog batchSeedJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchSeedJobHandler.TYPE)
        .singleResult();
    assertThat(batchSeedJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchMonitorJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchMonitorJobHandler.TYPE)
        .singleResult();
    assertThat(batchMonitorJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchExecutionJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(Batch.TYPE_PROCESS_INSTANCE_RESTART)
        .singleResult();
    assertThat(batchExecutionJobLog.getBatchId()).isEqualTo(batch.getId());
  }

  @Test
  public void shouldSetBatchIdOnJobAndJobLog_SetExternalTaskRetries() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess("process")
        .startEvent()
        .serviceTask().camundaExternalTask("topic")
        .endEvent()
        .done());
    runtimeService.startProcessInstanceByKey("process");

    ExternalTask externalTask = externalTaskService.createExternalTaskQuery().singleResult();

    Batch batch = externalTaskService.setRetriesAsync(List.of(externalTask.getId()), null, 5);

    // when
    batchRule.syncExec(batch);

    // then
    assertThat(batch.getId()).isNotNull();
    HistoricJobLog batchSeedJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchSeedJobHandler.TYPE)
        .singleResult();
    assertThat(batchSeedJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchMonitorJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchMonitorJobHandler.TYPE)
        .singleResult();
    assertThat(batchMonitorJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchExecutionJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(Batch.TYPE_SET_EXTERNAL_TASK_RETRIES)
        .singleResult();
    assertThat(batchExecutionJobLog.getBatchId()).isEqualTo(batch.getId());
  }

  @Test
  public void shouldSetBatchIdOnJobAndJobLog_SetJobRetries() {
    // given
    testRule.deploy(getTimerProcess());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Job timerJob = managementService.createJobQuery().singleResult();

    Batch batch = managementService.setJobRetriesAsync(List.of(timerJob.getId()), 5);

    // when
    batchRule.syncExec(batch);

    // then
    assertThat(batch.getId()).isNotNull();
    HistoricJobLog batchSeedJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchSeedJobHandler.TYPE)
        .singleResult();
    assertThat(batchSeedJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchMonitorJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchMonitorJobHandler.TYPE)
        .singleResult();
    assertThat(batchMonitorJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchExecutionJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(Batch.TYPE_SET_JOB_RETRIES)
        .singleResult();
    assertThat(batchExecutionJobLog.getBatchId()).isEqualTo(batch.getId());
  }

  @Test
  public void shouldSetBatchIdOnJobAndJobLog_UpdateProcessInstancesSuspendState() {
    // given
    testRule.deploy(getUserTaskProcess());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Batch batch = runtimeService.updateProcessInstanceSuspensionState()
        .byProcessInstanceIds(List.of(processInstance.getId()))
        .suspendAsync();

    // when
    batchRule.syncExec(batch);

    // then
    assertThat(batch.getId()).isNotNull();
    HistoricJobLog batchSeedJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchSeedJobHandler.TYPE)
        .singleResult();
    assertThat(batchSeedJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchMonitorJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(BatchMonitorJobHandler.TYPE)
        .singleResult();
    assertThat(batchMonitorJobLog.getBatchId()).isEqualTo(batch.getId());
    HistoricJobLog batchExecutionJobLog = historyService.createHistoricJobLogQuery()
        .successLog()
        .jobDefinitionType(Batch.TYPE_PROCESS_INSTANCE_UPDATE_SUSPENSION_STATE)
        .singleResult();
    assertThat(batchExecutionJobLog.getBatchId()).isEqualTo(batch.getId());
  }
}