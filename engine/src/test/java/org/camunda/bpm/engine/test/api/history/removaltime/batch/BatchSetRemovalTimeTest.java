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

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.history.removaltime.batch.helper.BatchSetRemovalTimeRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_FULL;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_END;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_NONE;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_START;

/**
 * @author Tassilo Weidner
 */

@RequiredHistoryLevel(HISTORY_FULL)
public class BatchSetRemovalTimeTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule engineTestRule = new ProcessEngineTestRule(engineRule);
  protected BatchSetRemovalTimeRule testRule = new BatchSetRemovalTimeRule(engineRule, engineTestRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(engineTestRule).around(testRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  protected final Date CURRENT_DATE = testRule.CURRENT_DATE;
  protected final Date REMOVAL_TIME = testRule.REMOVAL_TIME;

  protected RuntimeService runtimeService;
  protected HistoryService historyService;

  @Before
  public void assignServices() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
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
      historyService.setRemovalTimeToHistoricProcessInstancesAsync()
        .byQuery(query)
        .removalTime(REMOVAL_TIME)
        .executeAsync()
    );

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNull();
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
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstancesAsync()
        .byQuery(query)
        .removalTime(REMOVAL_TIME)
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isEqualTo(REMOVAL_TIME);
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isEqualTo(REMOVAL_TIME);
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
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstancesAsync()
        .byQuery(query)
        .removalTime(REMOVAL_TIME)
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isEqualTo(REMOVAL_TIME);
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldNotSetRemovalTime_BaseTimeNone() {
    // given
    testRule.getProcessEngineConfiguration()
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_NONE)
      .initHistoryRemovalTime();

    testRule.process().serviceTask().deploy().start();

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // assume
    assertThat(historicProcessInstance.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstancesAsync()
        .byQuery(query)
        .executeAsync()
    );

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // then
    assertThat(historicProcessInstance.getRemovalTime()).isNull();
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

    testRule.updateHistoryTimeToLive(5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstancesAsync()
        .byQuery(query)
        .executeAsync()
    );

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // then
    assertThat(historicProcessInstance.getRemovalTime()).isEqualTo(testRule.addDays(CURRENT_DATE, 5));
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
      historyService.setRemovalTimeToHistoricProcessInstancesAsync()
        .byQuery(query)
        .executeAsync()
    );

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // then
    assertThat(historicProcessInstance.getRemovalTime()).isNull();
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

    testRule.updateHistoryTimeToLive(5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstancesAsync()
        .byQuery(query)
        .executeAsync()
    );

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // then
    assertThat(historicProcessInstance.getRemovalTime()).isEqualTo(testRule.addDays(CURRENT_DATE, 5));
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
    assertThat(historicProcessInstance.getRemovalTime()).isEqualTo(testRule.addDays(CURRENT_DATE, 5));

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstancesAsync()
        .byQuery(query)
        .removalTime(null)
        .executeAsync()
    );

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // then
    assertThat(historicProcessInstance.getRemovalTime()).isNull();
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
    assertThat(historicProcessInstance.getRemovalTime()).isEqualTo(testRule.addDays(CURRENT_DATE, 5));

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstancesAsync()
        .byQuery(query)
        .removalTime(REMOVAL_TIME)
        .executeAsync()
    );

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // then
    assertThat(historicProcessInstance.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldThrowBadUserRequestException() {
    // given

    // then
    thrown.expect(BadUserRequestException.class);
    thrown.expectMessage("historicProcessInstances is empty");

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    historyService.setRemovalTimeToHistoricProcessInstancesAsync()
      .byQuery(query)
      .removalTime(REMOVAL_TIME)
      .executeAsync();
  }

  @Test
  public void shouldProduceHistory() {
    // given
    testRule.process().serviceTask().deploy().start();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstancesAsync()
        .byQuery(query)
        .executeAsync()
    );

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();

    // then
    assertThat(historicBatch.getType()).isEqualTo("process-set-removal-time");
    assertThat(historicBatch.getStartTime()).isEqualTo(CURRENT_DATE);
    assertThat(historicBatch.getEndTime()).isEqualTo(CURRENT_DATE);
  }

}
