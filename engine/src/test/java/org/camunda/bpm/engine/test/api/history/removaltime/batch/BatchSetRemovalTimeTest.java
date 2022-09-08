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
package org.camunda.bpm.engine.test.api.history.removaltime.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_FULL;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_END;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_NONE;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_START;
import static org.camunda.bpm.engine.test.api.history.removaltime.batch.helper.BatchSetRemovalTimeRule.addDays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.batch.history.HistoricBatchQuery;
import org.camunda.bpm.engine.history.CleanableHistoricBatchReportResult;
import org.camunda.bpm.engine.history.CleanableHistoricDecisionInstanceReportResult;
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReportResult;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.SetRemovalTimeSelectModeForHistoricBatchesBuilder;
import org.camunda.bpm.engine.history.SetRemovalTimeSelectModeForHistoricDecisionInstancesBuilder;
import org.camunda.bpm.engine.history.SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder;
import org.camunda.bpm.engine.history.SetRemovalTimeToHistoricBatchesBuilder;
import org.camunda.bpm.engine.history.SetRemovalTimeToHistoricDecisionInstancesBuilder;
import org.camunda.bpm.engine.history.SetRemovalTimeToHistoricProcessInstancesBuilder;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.history.removaltime.batch.helper.BatchSetRemovalTimeRule;
import org.camunda.bpm.engine.test.api.runtime.BatchHelper;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Tassilo Weidner
 */

@RequiredHistoryLevel(HISTORY_FULL)
public class BatchSetRemovalTimeTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule engineTestRule = new ProcessEngineTestRule(engineRule);
  protected BatchSetRemovalTimeRule testRule = new BatchSetRemovalTimeRule(engineRule, engineTestRule);
  protected BatchHelper helper = new BatchHelper(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(engineTestRule).around(testRule);

  protected final Date CURRENT_DATE = testRule.CURRENT_DATE;
  protected final Date REMOVAL_TIME = testRule.REMOVAL_TIME;

  protected RuntimeService runtimeService;
  private DecisionService decisionService;
  protected HistoryService historyService;
  protected ManagementService managementService;

  @Before
  public void assignServices() {
    runtimeService = engineRule.getRuntimeService();
    decisionService = engineRule.getDecisionService();
    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();
  }

  @After
  public void tearDown() {
    ClockUtil.reset();
    managementService.createBatchQuery().list().forEach(b -> managementService.deleteBatch(b.getId(), true));
    historyService.createHistoricBatchQuery().list().forEach(b -> historyService.deleteHistoricBatch(b.getId()));
    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJobByBatchType(new HashMap<>());
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldNotSetRemovalTime_DmnDisabled() {
    // given
    testRule.getProcessEngineConfiguration()
      .setDmnEnabled(false);

    testRule.process().ruleTask("dish-decision").deploy().startWithVariables(
      Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend")
    );

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNull();
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldNotSetRemovalTimeInHierarchy_DmnDisabled() {
    // given
    testRule.getProcessEngineConfiguration()
      .setDmnEnabled(false);

    testRule.process()
      .call()
        .passVars("temperature", "dayType")
      .ruleTask("dish-decision")
      .deploy()
      .startWithVariables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      );

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNull();
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldNotSetRemovalTimeForStandaloneDecision_DmnDisabled() {
    // given
    testRule.getProcessEngineConfiguration()
      .setDmnEnabled(false);

    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNull();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNull();
  }

  @Test
  public void shouldCreateDeploymentAwareBatchJobs_ProcessInstances() {
    // given
    testRule.getProcessEngineConfiguration()
      .setInvocationsPerBatchJob(2);

    testRule.process().userTask().deploy().start();
    testRule.process().userTask().deploy().start();

    List<String> deploymentIds = engineRule.getRepositoryService().createDeploymentQuery()
        .list().stream()
        .map(org.camunda.bpm.engine.repository.Deployment::getId)
        .collect(Collectors.toList());

    // when
    Batch batch = historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(historyService.createHistoricProcessInstanceQuery())
        .executeAsync();
    testRule.executeSeedJobs(batch);

    // then
    List<Job> executionJobs = testRule.getExecutionJobs(batch);
    assertThat(executionJobs).hasSize(2);
    assertThat(executionJobs.get(0).getDeploymentId()).isIn(deploymentIds);
    assertThat(executionJobs.get(1).getDeploymentId()).isIn(deploymentIds);
    assertThat(executionJobs.get(0).getDeploymentId()).isNotEqualTo(executionJobs.get(1).getDeploymentId());
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldCreateDeploymentAwareBatchJobs_StandaloneDecision() {
    // given
    testRule.getProcessEngineConfiguration()
      .setInvocationsPerBatchJob(3);

    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    // ... and a second DMN deployment and its evaluation
    engineTestRule.deploy("org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml");
    decisionService.evaluateDecisionByKey("dish-decision")
    .variables(
      Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend")
    ).evaluate();

    List<String> deploymentIds = engineRule.getRepositoryService().createDeploymentQuery()
        .list().stream()
        .map(org.camunda.bpm.engine.repository.Deployment::getId)
        .collect(Collectors.toList());

    // when
    Batch batch = historyService.setRemovalTimeToHistoricDecisionInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(historyService.createHistoricDecisionInstanceQuery())
        .executeAsync();
    testRule.executeSeedJobs(batch);

    // then
    List<Job> executionJobs = testRule.getExecutionJobs(batch);
    assertThat(executionJobs).hasSize(2);
    assertThat(executionJobs.get(0).getDeploymentId()).isIn(deploymentIds);
    assertThat(executionJobs.get(1).getDeploymentId()).isIn(deploymentIds);
    assertThat(executionJobs.get(0).getDeploymentId()).isNotEqualTo(executionJobs.get(1).getDeploymentId());
  }

  @Test
  public void shouldSetRemovalTime_MultipleInvocationsPerBatchJob() {
    // given
    testRule.getProcessEngineConfiguration()
      .setInvocationsPerBatchJob(2);

    testRule.process().userTask().deploy().start();
    testRule.process().userTask().deploy().start();

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // assume
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isEqualTo(REMOVAL_TIME);
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTimeForStandaloneDecision_MultipleInvocationsPerBatchJob() {
    // given
    testRule.getProcessEngineConfiguration()
      .setInvocationsPerBatchJob(2);

    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // assume
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNull();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isEqualTo(REMOVAL_TIME);
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isEqualTo(REMOVAL_TIME);
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTimeForBatch_MultipleInvocationsPerBatchJob() {
    // given
    testRule.getProcessEngineConfiguration()
      .setInvocationsPerBatchJob(2);

    String processInstanceIdOne = testRule.process().userTask().deploy().start();
    Batch batchOne = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceIdOne), "");

    String processInstanceIdTwo = testRule.process().userTask().deploy().start();
    Batch batchTwo = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceIdTwo), "");

    List<HistoricBatch> historicBatches = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .list();

    // assume
    assertThat(historicBatches.get(0).getRemovalTime()).isNull();
    assertThat(historicBatches.get(1).getRemovalTime()).isNull();

    HistoricBatchQuery query = historyService.createHistoricBatchQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    historicBatches = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .list();

    // then
    assertThat(historicBatches.get(0).getRemovalTime()).isEqualTo(REMOVAL_TIME);
    assertThat(historicBatches.get(1).getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTime_SingleInvocationPerBatchJob() {
    // given
    testRule.getProcessEngineConfiguration()
      .setInvocationsPerBatchJob(1);

    testRule.process().userTask().deploy().start();
    testRule.process().userTask().deploy().start();

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // assume
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isEqualTo(REMOVAL_TIME);
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTimeForStandaloneDecision_SingleInvocationPerBatchJob() {
    // given
    testRule.getProcessEngineConfiguration()
      .setInvocationsPerBatchJob(1);

    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // assume
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNull();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isEqualTo(REMOVAL_TIME);
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isEqualTo(REMOVAL_TIME);
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTimeForBatch_SingleInvocationPerBatchJob() {
    // given
    testRule.getProcessEngineConfiguration()
      .setInvocationsPerBatchJob(1);

    String processInstanceIdOne = testRule.process().userTask().deploy().start();
    Batch batchOne = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceIdOne), "");

    String processInstanceIdTwo = testRule.process().userTask().deploy().start();
    Batch batchTwo = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceIdTwo), "");

    List<HistoricBatch> historicBatches = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .list();

    // assume
    assertThat(historicBatches.get(0).getRemovalTime()).isNull();
    assertThat(historicBatches.get(1).getRemovalTime()).isNull();

    HistoricBatchQuery query = historyService.createHistoricBatchQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    historicBatches = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .list();

    // then
    assertThat(historicBatches.get(0).getRemovalTime()).isEqualTo(REMOVAL_TIME);
    assertThat(historicBatches.get(1).getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldNotSetRemovalTime_BaseTimeNone() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_NONE)
      .initHistoryRemovalTime();

    testRule.process().ttl(5).serviceTask().deploy().start();

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // assume
    assertThat(historicProcessInstance.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // then
    assertThat(historicProcessInstance.getRemovalTime()).isNull();
  }

  @Test
  public void shouldClearRemovalTime_BaseTimeNone() {
    // given
    testRule.process().ttl(5).serviceTask().deploy().start();

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // assume
    assertThat(historicProcessInstance.getRemovalTime()).isNotNull();

    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_NONE)
      .initHistoryRemovalTime();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // then
    assertThat(historicProcessInstance.getRemovalTime()).isNull();
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldNotSetRemovalTimeForStandaloneDecision_BaseTimeNone() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_NONE)
      .initHistoryRemovalTime();

    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // assume
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNull();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNull();
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldClearRemovalTimeForStandaloneDecision_BaseTimeNone() {
    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    // given
    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // assume
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNotNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNotNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNotNull();

    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_NONE)
      .initHistoryRemovalTime();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNull();
  }

  @Test
  public void shouldNotSetRemovalTimeInHierarchy_BaseTimeNone() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_NONE)
      .initHistoryRemovalTime();

    testRule.process().ttl(5).call().serviceTask().deploy().start();

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // assume
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isNull();
  }

  @Test
  public void shouldClearRemovalTimeInHierarchy_BaseTimeNone() {
    // given
    testRule.process().ttl(5).call().serviceTask().deploy().start();

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // assume
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNotNull();
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isNotNull();

    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_NONE)
      .initHistoryRemovalTime();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isNull();
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldNotSetRemovalTimeForStandaloneDecisionInHierarchy_BaseTimeNone() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_NONE)
      .initHistoryRemovalTime();

    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .singleResult();

    // assume
    assertThat(historicDecisionInstance.getRemovalTime()).isNull();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().rootDecisionInstancesOnly();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .singleResult();

    // then
    assertThat(historicDecisionInstance.getRemovalTime()).isNull();
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldClearRemovalTimeForStandaloneDecisionInHierarchy_BaseTimeNone() {
    // given
    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .singleResult();

    // assume
    assertThat(historicDecisionInstance.getRemovalTime()).isNotNull();

    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_NONE)
      .initHistoryRemovalTime();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().rootDecisionInstancesOnly();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .singleResult();

    // then
    assertThat(historicDecisionInstance.getRemovalTime()).isNull();
  }

  @Test
  public void shouldNotSetRemovalTimeForBatch_BaseTimeNone() {
    // given
    ProcessEngineConfigurationImpl configuration = testRule.getProcessEngineConfiguration();
    configuration.setHistoryCleanupStrategy("endTimeBased");
    configuration.setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_NONE);

    configuration.setBatchOperationHistoryTimeToLive("P5D");
    configuration.initHistoryCleanup();

    String processInstanceIdOne = testRule.process().serviceTask().deploy().start();
    Batch batchOne = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceIdOne), "");
    testRule.syncExec(batchOne);

    String processInstanceIdTwo = testRule.process().serviceTask().deploy().start();
    Batch batchTwo = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceIdTwo), "");
    testRule.syncExec(batchTwo);

    List<HistoricBatch> historicBatches = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .list();

    // assume
    assertThat(historicBatches.get(0).getRemovalTime()).isNull();
    assertThat(historicBatches.get(1).getRemovalTime()).isNull();

    HistoricBatchQuery query = historyService.createHistoricBatchQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicBatches = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .list();

    // then
    assertThat(historicBatches.get(0).getRemovalTime()).isNull();
    assertThat(historicBatches.get(1).getRemovalTime()).isNull();
  }

  @Test
  public void shouldClearRemovalTimeForBatch_BaseTimeNone() {
    // given
    ProcessEngineConfigurationImpl configuration = testRule.getProcessEngineConfiguration();

    configuration.setBatchOperationHistoryTimeToLive("P5D");
    configuration.initHistoryCleanup();

    String processInstanceIdOne = testRule.process().serviceTask().deploy().start();
    Batch batchOne = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceIdOne), "");
    testRule.syncExec(batchOne);

    String processInstanceIdTwo = testRule.process().serviceTask().deploy().start();
    Batch batchTwo = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceIdTwo), "");
    testRule.syncExec(batchTwo);

    List<HistoricBatch> historicBatches = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .list();

    // assume
    assertThat(historicBatches.get(0).getRemovalTime()).isNotNull();
    assertThat(historicBatches.get(1).getRemovalTime()).isNotNull();

    configuration.setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_NONE);

    HistoricBatchQuery query = historyService.createHistoricBatchQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicBatches = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .list();

    // then
    assertThat(historicBatches.get(0).getRemovalTime()).isNull();
    assertThat(historicBatches.get(1).getRemovalTime()).isNull();
  }

  @Test
  public void shouldSetRemovalTime_BaseTimeStart() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_START)
      .initHistoryRemovalTime();

    testRule.process().userTask().deploy().start();

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // assume
    assertThat(historicProcessInstance.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("process", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // then
    assertThat(historicProcessInstance.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTimeForStandaloneDecision_BaseTimeStart() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_START)
      .initHistoryRemovalTime();

    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .singleResult();

    // assume
    assertThat(historicDecisionInstance.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .singleResult();

    // then
    assertThat(historicDecisionInstance.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTimeForBatch_BaseTimeStart() {
    // given
    String processInstanceId = testRule.process().serviceTask().deploy().start();
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceId), "");

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .singleResult();

    // assume
    assertThat(historicBatch.getRemovalTime()).isNull();

    ProcessEngineConfigurationImpl configuration = testRule.getProcessEngineConfiguration();
    configuration.setBatchOperationHistoryTimeToLive("P5D");
    configuration.initHistoryCleanup();

    HistoricBatchQuery query = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION);

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicBatch = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .singleResult();

    // then
    assertThat(historicBatch.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTimeInHierarchy_BaseTimeStart() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_START)
      .initHistoryRemovalTime();

    testRule.process().call().userTask().deploy().start();

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // assume
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTimeInHierarchyForStandaloneDecision_BaseTimeStart() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_START)
      .initHistoryRemovalTime();

    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .singleResult();

    // assume
    assertThat(historicDecisionInstance.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .singleResult();

    // then
    assertThat(historicDecisionInstance.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldNotSetRemovalTime_BaseTimeEnd() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END)
      .initHistoryRemovalTime();

    testRule.process().ttl(5).userTask().deploy().start();

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // assume
    assertThat(historicProcessInstance.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // then
    assertThat(historicProcessInstance.getRemovalTime()).isNull();
  }

  @Test
  public void shouldClearRemovalTime_BaseTimeEnd() {
    // given
    testRule.process().ttl(5).userTask().deploy().start();

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // assume
    assertThat(historicProcessInstance.getRemovalTime()).isNotNull();

    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END)
      .initHistoryRemovalTime();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // then
    assertThat(historicProcessInstance.getRemovalTime()).isNull();
  }

  @Test
  public void shouldNotSetRemovalTimeForBatch_BaseTimeEnd() {
    // given
    ProcessEngineConfigurationImpl configuration = testRule.getProcessEngineConfiguration();

    configuration
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END);

    String processInstanceId = testRule.process().serviceTask().deploy().start();
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceId), "");

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .singleResult();

    // assume
    assertThat(historicBatch.getRemovalTime()).isNull();

    configuration.setBatchOperationHistoryTimeToLive("P5D");
    configuration.initHistoryCleanup();

    HistoricBatchQuery query = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION);

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicBatch = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .singleResult();

    // then
    assertThat(historicBatch.getRemovalTime()).isNull();
  }

  @Test
  public void shouldClearRemovalTimeForBatch_BaseTimeEnd() {
    // given
    ProcessEngineConfigurationImpl configuration = testRule.getProcessEngineConfiguration();
    configuration.setBatchOperationHistoryTimeToLive("P5D");
    configuration.initHistoryCleanup();

    String processInstanceId = testRule.process().serviceTask().deploy().start();
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceId), "");

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .singleResult();

    // assume
    assertThat(historicBatch.getRemovalTime()).isNotNull();

    configuration.setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END);

    HistoricBatchQuery query = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION);

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicBatch = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .singleResult();

    // then
    assertThat(historicBatch.getRemovalTime()).isNull();
  }

  @Test
  public void shouldNotSetRemovalTimeInHierarchy_BaseTimeEnd() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END)
      .initHistoryRemovalTime();

    testRule.process().call().ttl(5).userTask().deploy().start();

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // assume
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isNull();
  }

  @Test
  public void shouldClearRemovalTimeInHierarchy_BaseTimeEnd() {
    // given
    testRule.process().call().ttl(5).userTask().deploy().start();

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // assume
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNotNull();
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isNotNull();

    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END)
      .initHistoryRemovalTime();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isNull();
  }

  @Test
  public void shouldSetRemovalTime_BaseTimeEnd() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END)
      .initHistoryRemovalTime();

    testRule.process().serviceTask().deploy().start();

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // assume
    assertThat(historicProcessInstance.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("process", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // then
    assertThat(historicProcessInstance.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTimeForStandaloneDecision_BaseTimeEnd() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END)
      .initHistoryRemovalTime();

    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .singleResult();

    // assume
    assertThat(historicDecisionInstance.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .singleResult();

    // then
    assertThat(historicDecisionInstance.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTimeForBatch_BaseTimeEnd() {
    // given
    ProcessEngineConfigurationImpl configuration = testRule.getProcessEngineConfiguration();

    configuration
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END);

    String processInstanceId = testRule.process().serviceTask().deploy().start();
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceId), "");

    ClockUtil.setCurrentTime(addDays(CURRENT_DATE, 1));

    testRule.syncExec(batch);

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .singleResult();

    // assume
    assertThat(historicBatch.getRemovalTime()).isNull();

    configuration.setBatchOperationHistoryTimeToLive("P5D");
    configuration.initHistoryCleanup();

    HistoricBatchQuery query = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION);

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicBatch = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .singleResult();

    // then
    assertThat(historicBatch.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5+1));
  }

  @Test
  public void shouldSetRemovalTimeInHierarchy_BaseTimeEnd() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END)
      .initHistoryRemovalTime();

    testRule.process().call().serviceTask().deploy().start();

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // assume
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTimeInHierarchyForStandaloneDecision_BaseTimeEnd() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END)
      .initHistoryRemovalTime();

    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .singleResult();

    // assume
    assertThat(historicDecisionInstance.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().rootDecisionInstancesOnly();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .singleResult();

    // then
    assertThat(historicDecisionInstance.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_Null() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_START)
      .initHistoryRemovalTime();

    testRule.process().ttl(5).userTask().deploy().start();

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // assume
    assertThat(historicProcessInstance.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .clearedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // then
    assertThat(historicProcessInstance.getRemovalTime()).isNull();
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTimeForStandaloneDecision_Null() {
    // given
    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .singleResult();

    // assume
    assertThat(historicDecisionInstance.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .clearedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .singleResult();

    // then
    assertThat(historicDecisionInstance.getRemovalTime()).isEqualTo(null);
  }

  @Test
  public void shouldSetRemovalTimeForBatch_Null() {
    // given
    ProcessEngineConfigurationImpl configuration = testRule.getProcessEngineConfiguration();

    configuration.setBatchOperationHistoryTimeToLive("P5D");
    configuration.initHistoryCleanup();

    String processInstanceId = testRule.process().serviceTask().deploy().start();
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceId), "");

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .singleResult();

    // assume
    assertThat(historicBatch.getRemovalTime()).isNotNull();

    HistoricBatchQuery query = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION);

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .clearedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    historicBatch = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .singleResult();

    // then
    assertThat(historicBatch.getRemovalTime()).isNull();
  }

  @Test
  public void shouldSetRemovalTimeInHierarchy_Null() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_START)
      .initHistoryRemovalTime();

    testRule.process().call().ttl(5).userTask().deploy().start();

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // assume
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .clearedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isNull();
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTimeInHierarchyForStandaloneDecision_Null() {
    // given
    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .singleResult();

    // assume
    assertThat(historicDecisionInstance.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().rootDecisionInstancesOnly();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .clearedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .singleResult();

    // then
    assertThat(historicDecisionInstance.getRemovalTime()).isEqualTo(null);
  }

  @Test
  public void shouldSetRemovalTime_Absolute() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_START)
      .initHistoryRemovalTime();

    testRule.process().ttl(5).userTask().deploy().start();

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // assume
    assertThat(historicProcessInstance.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // then
    assertThat(historicProcessInstance.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTimeForStandaloneDecision_Absolute() {
    // given
    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .singleResult();

    // assume
    assertThat(historicDecisionInstance.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .singleResult();

    // then
    assertThat(historicDecisionInstance.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTimeForBatch_Absolute() {
    // given
    String processInstanceId = testRule.process().serviceTask().deploy().start();
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceId), "");

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .singleResult();

    // assume
    assertThat(historicBatch.getRemovalTime()).isNull();

    HistoricBatchQuery query = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION);

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    historicBatch = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .singleResult();

    // then
    assertThat(historicBatch.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTimeInHierarchy_Absolute() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_START)
      .initHistoryRemovalTime();

    testRule.process().call().ttl(5).userTask().deploy().start();

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // assume
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isEqualTo(REMOVAL_TIME);
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTimeInHierarchyForStandaloneDecision_Absolute() {
    // given
    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .singleResult();

    // assume
    assertThat(historicDecisionInstance.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().rootDecisionInstancesOnly();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .singleResult();

    // then
    assertThat(historicDecisionInstance.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTimeInHierarchy_ByChildInstance() {
    // given
    String rootProcessInstance = testRule.process().call().ttl(5).userTask().deploy().start();

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // assume
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
      .superProcessInstanceId(rootProcessInstance);

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isEqualTo(REMOVAL_TIME);
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTimeInHierarchyForStandaloneDecision_ByChildInstance() {
    // given
    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .singleResult();

    // assume
    assertThat(historicDecisionInstance.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery()
      .decisionInstanceId(historicDecisionInstance.getId());

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_ByIds() {
    // given
    testRule.process().call().userTask().deploy().start();

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // assume
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive(5, "process", "rootProcess");

    List<String> ids = new ArrayList<>();
    for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
      ids.add(historicProcessInstance.getId());
    }

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byIds(ids.toArray(new String[0]))
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldThrowBadUserRequestException_NotExistingIds() {
    // given

    // when/then
    assertThatThrownBy(() -> historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byIds("aNotExistingId", "anotherNotExistingId")
        .executeAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("historicProcessInstances is empty");
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTimeForStandaloneDecision_ByIds() {
    // given
    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // assume
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLiveDmn(5, "dish-decision", "season", "guestCount");

    List<String> ids = new ArrayList<>();
    for (HistoricDecisionInstance historicDecisionInstance : historicDecisionInstances) {
      ids.add(historicDecisionInstance.getId());
    }

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byIds(ids.toArray(new String[0]))
        .executeAsync()
    );

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldThrowBadUserRequestExceptionForStandaloneDecision_NotExistingIds() {
    // given

    // when/then
    assertThatThrownBy(() -> historyService.setRemovalTimeToHistoricDecisionInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byIds("aNotExistingId", "anotherNotExistingId")
        .executeAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("historicDecisionInstances is empty");
  }

  @Test
  public void shouldSetRemovalTimeForBatch_ByIds() {
    // given
    String processInstanceId = testRule.process().serviceTask().deploy().start();

    Batch batchOne = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceId), "");
    Batch batchTwo = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceId), "");

    List<HistoricBatch> historicBatches = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .list();

    // assume
    assertThat(historicBatches.get(0).getRemovalTime()).isNull();
    assertThat(historicBatches.get(1).getRemovalTime()).isNull();

    List<String> ids = new ArrayList<>();
    for (HistoricBatch historicBatch : historicBatches) {
      ids.add(historicBatch.getId());
    }

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byIds(ids.toArray(new String[0]))
        .executeAsync()
    );

    historicBatches = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .list();

    // then
    assertThat(historicBatches.get(0).getRemovalTime()).isEqualTo(REMOVAL_TIME);
    assertThat(historicBatches.get(1).getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldThrowBadUserRequestExceptionForBatch_NotExistingIds() {
    // given

    // when/then
    assertThatThrownBy(() -> historyService.setRemovalTimeToHistoricBatches()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byIds("aNotExistingId", "anotherNotExistingId")
        .executeAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("historicBatches is empty");
  }

  @Test
  public void shouldThrowBadUserRequestException() {
    // given
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when/then
    assertThatThrownBy(() -> historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("historicProcessInstances is empty");
  }

  @Test
  public void shouldThrowBadUserRequestExceptionForStandaloneDecision() {
    // given
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // when/then
    assertThatThrownBy(() -> historyService.setRemovalTimeToHistoricDecisionInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("historicDecisionInstances is empty");
  }

  @Test
  public void shouldThrowBadUserRequestExceptionForBatch() {
    // given
    HistoricBatchQuery query = historyService.createHistoricBatchQuery();

    // when/then
    assertThatThrownBy(() -> historyService.setRemovalTimeToHistoricBatches()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("historicBatches is empty");
  }

  @Test
  public void shouldProduceHistory() {
    // given
    testRule.process().serviceTask().deploy().start();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();

    // then
    assertThat(historicBatch.getType()).isEqualTo("process-set-removal-time");
    assertThat(historicBatch.getStartTime()).isEqualTo(CURRENT_DATE);
    assertThat(historicBatch.getEndTime()).isEqualTo(CURRENT_DATE);
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldProduceHistoryForStandaloneDecision() {
    // given
    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();

    // then
    assertThat(historicBatch.getType()).isEqualTo("decision-set-removal-time");
    assertThat(historicBatch.getStartTime()).isEqualTo(CURRENT_DATE);
    assertThat(historicBatch.getEndTime()).isEqualTo(CURRENT_DATE);
  }

  @Test
  public void shouldProduceHistoryForBatch() {
    // given
    String processInstanceId = testRule.process().serviceTask().deploy().start();
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceId), "");

    testRule.syncExec(batch);

    HistoricBatchQuery query = historyService.createHistoricBatchQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .calculatedRemovalTime()
        .byQuery(query)
        .executeAsync()
    );

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery()
      .type("batch-set-removal-time")
      .singleResult();

    // then
    assertThat(historicBatch.getStartTime()).isEqualTo(CURRENT_DATE);
    assertThat(historicBatch.getEndTime()).isEqualTo(CURRENT_DATE);
  }

  @Test
  public void shouldThrowExceptionIfNoRemovalTimeSettingDefined()
  {
    // given
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    SetRemovalTimeToHistoricProcessInstancesBuilder batchBuilder = historyService.setRemovalTimeToHistoricProcessInstances()
      .byQuery(query);

    // when/then
    assertThatThrownBy(() -> batchBuilder.executeAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("removalTime is null");
  }

  @Test
  public void shouldThrowExceptionIfNoRemovalTimeSettingDefinedForStandaloneDecision() {
    // given
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    SetRemovalTimeToHistoricDecisionInstancesBuilder batchBuilder = historyService.setRemovalTimeToHistoricDecisionInstances()
      .byQuery(query);

    // when/then
    assertThatThrownBy(() -> batchBuilder.executeAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("removalTime is null");
  }

  @Test
  public void shouldThrowExceptionIfNoRemovalTimeSettingDefinedForBatch() {
    // given
    HistoricBatchQuery query = historyService.createHistoricBatchQuery();

    SetRemovalTimeToHistoricBatchesBuilder batchBuilder = historyService.setRemovalTimeToHistoricBatches()
      .byQuery(query);

    // when/then
    assertThatThrownBy(() -> batchBuilder.executeAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("removalTime is null");
  }

  @Test
  public void shouldThrowExceptionIfNoQueryAndNoIdsDefined()
  {
    // given
    SetRemovalTimeToHistoricProcessInstancesBuilder batchBuilder = historyService.setRemovalTimeToHistoricProcessInstances()
      .absoluteRemovalTime(new Date());

    // when/then
    assertThatThrownBy(() -> batchBuilder.executeAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("Neither query nor ids provided.");
  }

  @Test
  public void shouldThrowExceptionIfNoQueryAndNoIdsDefinedForStandaloneDecision()
  {
    // given
    SetRemovalTimeToHistoricDecisionInstancesBuilder batchBuilder = historyService.setRemovalTimeToHistoricDecisionInstances()
      .absoluteRemovalTime(new Date());

    // when/then
    assertThatThrownBy(() -> batchBuilder.executeAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("Neither query nor ids provided.");
  }

  @Test
  public void shouldThrowExceptionIfNoQueryAndNoIdsDefinedForBatch()
  {
    // given
    SetRemovalTimeToHistoricBatchesBuilder batchBuilder = historyService.setRemovalTimeToHistoricBatches()
      .absoluteRemovalTime(new Date());

    // when/then
    assertThatThrownBy(() -> batchBuilder.executeAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("Neither query nor ids provided.");
  }

  @Test
  public void shouldSetRemovalTime_BothQueryAndIdsDefined() {
    // given
    String rootProcessInstanceId = testRule.process().call().userTask().deploy().start();

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // assume
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive(5, "rootProcess", "process");

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
      .superProcessInstanceId(rootProcessInstanceId);

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .byIds(rootProcessInstanceId)
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTimeForStandaloneDecision_BothQueryAndIdsDefined() {
    // given
    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKeyIn("season", "dish-decision")
      .list();

    // assume
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLiveDmn( 5, "dish-decision", "season");

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("dish-decision");

    String id = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .singleResult()
      .getId();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .byIds(id)
        .executeAsync()
    );

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKeyIn("season", "dish-decision")
      .list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTimeForBatch_BothQueryAndIdsDefined() {
    // given
    String processInstanceId = testRule.process().serviceTask().deploy().start();
    Batch batchOne = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceId), "");
    Batch batchTwo = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceId), "");

    List<HistoricBatch> historicBatches = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .list();

    // assume
    assertThat(historicBatches.get(0).getRemovalTime()).isNull();
    assertThat(historicBatches.get(1).getRemovalTime()).isNull();

    ProcessEngineConfigurationImpl configuration = testRule.getProcessEngineConfiguration();
    configuration.setBatchOperationHistoryTimeToLive("P5D");
    configuration.initHistoryCleanup();

    HistoricBatchQuery query = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .batchId(batchOne.getId());

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .calculatedRemovalTime()
        .byQuery(query)
        .byIds(batchTwo.getId())
        .executeAsync()
    );

    historicBatches = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .list();

    // then
    assertThat(historicBatches.get(0).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
    assertThat(historicBatches.get(1).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_ExistingAndNotExistingId() {
    // given
    String processInstanceId = testRule.process().userTask().deploy().start();

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // assume
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive(5, "process");

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byIds("notExistingId", processInstanceId)
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTimeForStandaloneDecision_ExistingAndNotExistingId() {
    // given
    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKeyIn("season")
      .singleResult();

    // assume
    assertThat(historicDecisionInstance.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLiveDmn( 5, "season");

    String id = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .singleResult()
      .getId();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byIds("notExistingId", id)
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKeyIn("season")
      .singleResult();

    // then
    assertThat(historicDecisionInstance.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTimeForBatch_ExistingAndNotExistingId() {
    // given
    String processInstanceId = testRule.process().serviceTask().deploy().start();
    Batch batchOne = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceId), "");

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .singleResult();

    // assume
    assertThat(historicBatch.getRemovalTime()).isNull();

    ProcessEngineConfigurationImpl configuration = testRule.getProcessEngineConfiguration();
    configuration.setBatchOperationHistoryTimeToLive("P5D");
    configuration.initHistoryCleanup();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .calculatedRemovalTime()
        .byIds("notExistingId", batchOne.getId())
        .executeAsync()
    );

    historicBatch = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION)
      .singleResult();

    // then
    assertThat(historicBatch.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void ThrowBadUserRequestException_SelectMultipleModes_ModeCleared() {
    // given
    SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder builder = historyService.setRemovalTimeToHistoricProcessInstances();
    builder.calculatedRemovalTime();

    // when/then
    assertThatThrownBy(() -> builder.clearedRemovalTime())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("The removal time modes are mutually exclusive: mode is not null");
  }

  @Test
  public void ThrowBadUserRequestException_SelectMultipleModes_ModeAbsolute() {
    // given
    SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder builder = historyService.setRemovalTimeToHistoricProcessInstances();
    builder.calculatedRemovalTime();

    // when/then
    assertThatThrownBy(() -> builder.absoluteRemovalTime(new Date()))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("The removal time modes are mutually exclusive: mode is not null");
  }

  @Test
  public void ThrowBadUserRequestExceptionForStandaloneDecision_SelectMultipleModes_ModeCleared() {
    // given
    SetRemovalTimeSelectModeForHistoricDecisionInstancesBuilder builder = historyService.setRemovalTimeToHistoricDecisionInstances();
    builder.calculatedRemovalTime();

    // when/then
    assertThatThrownBy(() -> builder.clearedRemovalTime())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("The removal time modes are mutually exclusive: mode is not null");
  }

  @Test
  public void ThrowBadUserRequestExceptionForStandaloneDecision_SelectMultipleModes_ModeAbsolute() {
    // given
    SetRemovalTimeSelectModeForHistoricDecisionInstancesBuilder builder = historyService.setRemovalTimeToHistoricDecisionInstances();
    builder.calculatedRemovalTime();

    // when/then
    assertThatThrownBy(() -> builder.absoluteRemovalTime(new Date()))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("The removal time modes are mutually exclusive: mode is not null");
  }

  @Test
  public void ThrowBadUserRequestExceptionForBatch_SelectMultipleModes_ModeCleared() {
    // given
    SetRemovalTimeSelectModeForHistoricBatchesBuilder builder = historyService.setRemovalTimeToHistoricBatches();
    builder.calculatedRemovalTime();

    // when/then
    assertThatThrownBy(() -> builder.clearedRemovalTime())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("The removal time modes are mutually exclusive: mode is not null");
  }

  @Test
  public void ThrowBadUserRequestExceptionForBatch_SelectMultipleModes_ModeAbsolute() {
    // given
    SetRemovalTimeSelectModeForHistoricBatchesBuilder builder = historyService.setRemovalTimeToHistoricBatches();
    builder.calculatedRemovalTime();

    // when/then
    assertThatThrownBy(() -> builder.absoluteRemovalTime(new Date()))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("The removal time modes are mutually exclusive: mode is not null");
  }

  @Test
  public void shouldSeeCleanableButNotFinishedProcessInstanceInReport() {
    // given
    String processInstanceId = testRule.process().userTask().deploy().start();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(CURRENT_DATE)
        .byIds(processInstanceId)
        .executeAsync()
    );

    CleanableHistoricProcessInstanceReportResult report = historyService.createCleanableHistoricProcessInstanceReport().singleResult();

    // then
    assertThat(report.getFinishedProcessInstanceCount()).isEqualTo(0);
    assertThat(report.getCleanableProcessInstanceCount()).isEqualTo(1);
    assertThat(report.getHistoryTimeToLive()).isNull();
  }

  @Test
  public void shouldSeeCleanableAndFinishedProcessInstanceInReport() {
    // given
    String processInstanceId = testRule.process().serviceTask().deploy().start();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(CURRENT_DATE)
        .byIds(processInstanceId)
        .executeAsync()
    );

    CleanableHistoricProcessInstanceReportResult report = historyService.createCleanableHistoricProcessInstanceReport().singleResult();

    // then
    assertThat(report.getFinishedProcessInstanceCount()).isEqualTo(1);
    assertThat(report.getCleanableProcessInstanceCount()).isEqualTo(1);
    assertThat(report.getHistoryTimeToLive()).isNull();
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSeeCleanableAndFinishedDecisionInstanceInReport() {
    // given
    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("dish-decision");

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .absoluteRemovalTime(CURRENT_DATE)
        .byQuery(query)
        .executeAsync()
    );

    CleanableHistoricDecisionInstanceReportResult report = historyService.createCleanableHistoricDecisionInstanceReport()
      .decisionDefinitionKeyIn("dish-decision")
      .singleResult();

    // then
    assertThat(report.getFinishedDecisionInstanceCount()).isEqualTo(1);
    assertThat(report.getCleanableDecisionInstanceCount()).isEqualTo(1);
    assertThat(report.getHistoryTimeToLive()).isNull();
  }

  @Test
  public void shouldSeeCleanableButNotFinishedBatchInReport() {
    // given
    String processInstanceId = testRule.process().serviceTask().deploy().start();
    Batch batchOne = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceId), "");

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .absoluteRemovalTime(CURRENT_DATE)
        .byIds(batchOne.getId())
        .executeAsync()
    );

    testRule.clearDatabase();

    CleanableHistoricBatchReportResult report = historyService.createCleanableHistoricBatchReport().singleResult();

    // then
    assertThat(report.getFinishedBatchesCount()).isEqualTo(0);
    assertThat(report.getCleanableBatchesCount()).isEqualTo(1);
    assertThat(report.getHistoryTimeToLive()).isNull();
  }

  @Test
  public void shouldSeeCleanableAndFinishedBatchInReport() {
    // given
    String processInstanceId = testRule.process().serviceTask().deploy().start();
    Batch batchOne = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceId), "");

    testRule.syncExec(batchOne, false);

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .absoluteRemovalTime(CURRENT_DATE)
        .byIds(batchOne.getId())
        .executeAsync()
    );

    testRule.clearDatabase();

    CleanableHistoricBatchReportResult report = historyService.createCleanableHistoricBatchReport().singleResult();

    // then
    assertThat(report.getFinishedBatchesCount()).isEqualTo(1);
    assertThat(report.getCleanableBatchesCount()).isEqualTo(1);
    assertThat(report.getHistoryTimeToLive()).isNull();
  }

  @Test
  public void shouldSetInvocationsPerBatchTypeForProcesses() {
    // given
    engineRule.getProcessEngineConfiguration()
        .getInvocationsPerBatchJobByBatchType()
        .put(Batch.TYPE_PROCESS_SET_REMOVAL_TIME, 42);

    testRule.process().serviceTask().deploy().start();

    // when
    Batch batch = historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(CURRENT_DATE)
        .byQuery(historyService.createHistoricProcessInstanceQuery())
        .executeAsync();

    // then
    assertThat(batch.getInvocationsPerBatchJob()).isEqualTo(42);
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetInvocationsPerBatchTypeForDecisions() {
    // given
    engineRule.getProcessEngineConfiguration()
        .getInvocationsPerBatchJobByBatchType()
        .put(Batch.TYPE_DECISION_SET_REMOVAL_TIME, 42);

    decisionService.evaluateDecisionByKey("dish-decision")
        .variables(
            Variables.createVariables()
                .putValue("temperature", 32)
                .putValue("dayType", "Weekend")
        ).evaluate();

    // when
    Batch batch = historyService.setRemovalTimeToHistoricDecisionInstances()
        .absoluteRemovalTime(CURRENT_DATE)
        .byQuery(historyService.createHistoricDecisionInstanceQuery())
        .executeAsync();

    // then
    assertThat(batch.getInvocationsPerBatchJob()).isEqualTo(42);
  }

  @Test
  public void shouldSetInvocationsPerBatchTypeForBatches() {
    // given
    engineRule.getProcessEngineConfiguration()
        .getInvocationsPerBatchJobByBatchType()
        .put(Batch.TYPE_BATCH_SET_REMOVAL_TIME, 42);

    String processInstanceId = testRule.process().serviceTask().deploy().start();
    Batch batchOne = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceId), "");

    testRule.syncExec(batchOne, false);

    // when
    Batch batchTwo = historyService.setRemovalTimeToHistoricBatches()
        .absoluteRemovalTime(CURRENT_DATE)
        .byQuery(historyService.createHistoricBatchQuery())
        .executeAsync();

    // then
    assertThat(batchTwo.getInvocationsPerBatchJob()).isEqualTo(42);
  }

  @Test
  public void shouldSetExecutionStartTimeInBatchAndHistoryForBatches() {
    // given
    ClockUtil.setCurrentTime(CURRENT_DATE);
    String processInstanceId = testRule.process().serviceTask().deploy().start();
    Batch batchDelete = historyService.deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstanceId), "");
    testRule.syncExec(batchDelete, false);
    Batch batch = historyService.setRemovalTimeToHistoricBatches()
        .absoluteRemovalTime(CURRENT_DATE)
        .byQuery(historyService.createHistoricBatchQuery())
        .executeAsync();
    helper.executeSeedJob(batch);
    List<Job> executionJobs = helper.getExecutionJobs(batch, Batch.TYPE_BATCH_SET_REMOVAL_TIME);
    historyService.deleteHistoricBatch(batchDelete.getId());

    // when
    helper.executeJob(executionJobs.get(0));

    // then
    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();
    batch = managementService.createBatchQuery().singleResult();

    assertThat(batch.getExecutionStartTime()).isEqualToIgnoringMillis(CURRENT_DATE);
    assertThat(historicBatch.getExecutionStartTime()).isEqualToIgnoringMillis(CURRENT_DATE);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml")
  public void shouldSetExecutionStartTimeInBatchAndHistoryForDecisions() {
    // given
    ClockUtil.setCurrentTime(CURRENT_DATE);
    decisionService.evaluateDecisionByKey("dish-decision")
        .variables(
            Variables.createVariables()
                .putValue("temperature", 32)
                .putValue("dayType", "Weekend")
        ).evaluate();
    Batch batch = historyService.setRemovalTimeToHistoricDecisionInstances()
        .absoluteRemovalTime(CURRENT_DATE)
        .byQuery(historyService.createHistoricDecisionInstanceQuery())
        .executeAsync();
    helper.executeSeedJob(batch);
    List<Job> executionJobs = helper.getExecutionJobs(batch, Batch.TYPE_DECISION_SET_REMOVAL_TIME);

    // when
    helper.executeJob(executionJobs.get(0));

    // then
    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();
    batch = managementService.createBatchQuery().singleResult();

    assertThat(batch.getExecutionStartTime()).isEqualToIgnoringMillis(CURRENT_DATE);
    assertThat(historicBatch.getExecutionStartTime()).isEqualToIgnoringMillis(CURRENT_DATE);
  }

}
