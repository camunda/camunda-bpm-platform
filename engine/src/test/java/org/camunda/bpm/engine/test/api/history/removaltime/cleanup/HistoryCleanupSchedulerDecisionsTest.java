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
package org.camunda.bpm.engine.test.api.history.removaltime.cleanup;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.apache.commons.lang3.time.DateUtils.addSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandlerConfiguration.START_DELAY;

import java.util.Date;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Tassilo Weidner
 */
public class HistoryCleanupSchedulerDecisionsTest extends AbstractHistoryCleanupSchedulerTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
      configure(configuration, HistoryEventTypes.DMN_DECISION_EVALUATE));
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;

  @Before
  public void init() {
    engineConfiguration = engineRule.getProcessEngineConfiguration();
    initEngineConfiguration(engineConfiguration);

    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();

    runtimeService = engineRule.getRuntimeService();
  }

  protected final String CALLING_PROCESS_CALLS_DMN_KEY = "callingProcessCallsDmn";
  protected final BpmnModelInstance CALLING_PROCESS_CALLS_DMN = Bpmn.createExecutableProcess(CALLING_PROCESS_CALLS_DMN_KEY)
    .camundaHistoryTimeToLive(5)
    .startEvent()
      .businessRuleTask()
        .camundaDecisionRef("dish-decision")
        .multiInstance()
          .sequential()
          .cardinality("5")
        .multiInstanceDone()
    .endEvent().done();

  protected final Date END_DATE = new Date(1363608000000L);

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldScheduleToNowByDecisionInputs() {
    // given
    testRule.deploy(CALLING_PROCESS_CALLS_DMN);

    ClockUtil.setCurrentTime(END_DATE);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_CALLS_DMN_KEY,
      Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend"));

    engineConfiguration.setHistoryCleanupBatchSize(20);
    engineConfiguration.initHistoryCleanup();

    Date removalTime = addDays(END_DATE, 5);
    ClockUtil.setCurrentTime(removalTime);

    // when
    runHistoryCleanup();

    Job job = historyService.findHistoryCleanupJobs().get(0);

    // then
    assertThat(job.getDuedate()).isEqualTo(removalTime);
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldScheduleToLaterByDecisionInputs() {
    // given
    testRule.deploy(CALLING_PROCESS_CALLS_DMN);

    ClockUtil.setCurrentTime(END_DATE);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_CALLS_DMN_KEY,
      Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend"));

    engineConfiguration.setHistoryCleanupBatchSize(21);
    engineConfiguration.initHistoryCleanup();

    Date removalTime = addDays(END_DATE, 5);
    ClockUtil.setCurrentTime(removalTime);

    // when
    runHistoryCleanup();

    Job job = historyService.findHistoryCleanupJobs().get(0);

    // then
    assertThat(job.getDuedate()).isEqualTo(addSeconds(removalTime, START_DELAY));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/history/removaltime/cleanup/decisonWithThreeOutputs.dmn11.xml"
  })
  public void shouldScheduleToNowByDecisionOutputs() {
    // given
    testRule.deploy(CALLING_PROCESS_CALLS_DMN);

    ClockUtil.setCurrentTime(END_DATE);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_CALLS_DMN_KEY,
      Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend"));

    engineConfiguration.setHistoryCleanupBatchSize(25);
    engineConfiguration.initHistoryCleanup();

    Date removalTime = addDays(END_DATE, 5);
    ClockUtil.setCurrentTime(removalTime);

    // when
    runHistoryCleanup();

    Job job = historyService.findHistoryCleanupJobs().get(0);

    // then
    assertThat(job.getDuedate()).isEqualTo(removalTime);
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/history/removaltime/cleanup/decisonWithThreeOutputs.dmn11.xml"
  })
  public void shouldScheduleToLaterByDecisionOutputs() {
    // given
    testRule.deploy(CALLING_PROCESS_CALLS_DMN);

    ClockUtil.setCurrentTime(END_DATE);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_CALLS_DMN_KEY,
      Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend"));

    engineConfiguration.setHistoryCleanupBatchSize(26);
    engineConfiguration.initHistoryCleanup();

    Date removalTime = addDays(END_DATE, 5);
    ClockUtil.setCurrentTime(removalTime);

    // when
    runHistoryCleanup();

    Job job = historyService.findHistoryCleanupJobs().get(0);

    // then
    assertThat(job.getDuedate()).isEqualTo(addSeconds(removalTime, START_DELAY));
  }

}
