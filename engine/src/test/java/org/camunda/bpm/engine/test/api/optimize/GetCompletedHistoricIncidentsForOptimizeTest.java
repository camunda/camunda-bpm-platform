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
package org.camunda.bpm.engine.test.api.optimize;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.OptimizeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.FailingDelegate;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;


@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class GetCompletedHistoricIncidentsForOptimizeTest {

  private static final String PROCESS_DEFINITION_KEY = "oneFailingServiceTaskProcess";
  private static final BpmnModelInstance FAILING_SERVICE_TASK_MODEL =
    Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent("start")
      .serviceTask("task")
        .camundaAsyncBefore()
        .camundaClass(FailingDelegate.class.getName())
      .endEvent("end")
      .done();

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testHelper);

  private OptimizeService optimizeService;
  private RuntimeService runtimeService;
  private ManagementService managementService;

  @Before
  public void init() {
    ProcessEngineConfigurationImpl config =
      engineRule.getProcessEngineConfiguration();
    optimizeService = config.getOptimizeService();
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
  }

  @After
  public void cleanUp() {
    ClockUtil.reset();
  }

  @Test
  public void getCompletedHistoricIncidents() {
     // given
    testHelper.deploy(FAILING_SERVICE_TASK_MODEL);
    final ProcessInstance processInstance = startProcessInstanceAndFailWithIncident();
    retryAndSucceed(processInstance);
    startProcessInstanceAndFailWithIncident();

    // when
    List<HistoricIncidentEntity> completedIncidents =
      optimizeService.getCompletedHistoricIncidents(pastDate(), null, 10);

    // then
    assertThat(completedIncidents.size(), is(1));
    assertThatInstanceHasAllImportantInformation(completedIncidents.get(0));
  }

  @Test
  public void fishedAfterParameterWorks() {
     // given
    testHelper.deploy(FAILING_SERVICE_TASK_MODEL);
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    final ProcessInstance processInstance = startProcessInstanceAndFailWithIncident();
    retryAndSucceed(processInstance);
    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    final ProcessInstance processInstance2 = startProcessInstanceAndFailWithIncident();
    retryAndSucceed(processInstance2);

    // when
    List<HistoricIncidentEntity> completedIncidents =
      optimizeService.getCompletedHistoricIncidents(now, null, 10);

    // then
    assertThat(completedIncidents.size(), is(1));
    assertThat(completedIncidents.get(0).getProcessInstanceId(), is(processInstance2.getId()));
  }

  @Test
  public void fishedAtParameterWorks() {
     // given
    testHelper.deploy(FAILING_SERVICE_TASK_MODEL);
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    final ProcessInstance processInstance = startProcessInstanceAndFailWithIncident();
    retryAndSucceed(processInstance);
    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    final ProcessInstance processInstance2 = startProcessInstanceAndFailWithIncident();
    retryAndSucceed(processInstance2);

    // when
    List<HistoricIncidentEntity> completedIncidents =
      optimizeService.getCompletedHistoricIncidents(null, now, 10);

    // then
    assertThat(completedIncidents.size(), is(1));
    assertThat(completedIncidents.get(0).getProcessInstanceId(), is(processInstance.getId()));
  }

  @Test
  public void fishedAfterAndFinishedAtParameterWorks() {
     // given
    testHelper.deploy(FAILING_SERVICE_TASK_MODEL);
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    final ProcessInstance processInstance = startProcessInstanceAndFailWithIncident();
    retryAndSucceed(processInstance);
    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    final ProcessInstance processInstance2 = startProcessInstanceAndFailWithIncident();
    retryAndSucceed(processInstance2);

    // when
    List<HistoricIncidentEntity> completedIncidents =
      optimizeService.getCompletedHistoricIncidents(now, now, 10);

    // then
    assertThat(completedIncidents.size(), is(0));
  }

  @Test
  public void maxResultsParameterWorks() {
     // given
    testHelper.deploy(FAILING_SERVICE_TASK_MODEL);
    for (int i = 0; i < 5; i++) {
      final ProcessInstance processInstance = startProcessInstanceAndFailWithIncident();
      retryAndSucceed(processInstance);
    }

    // when
    List<HistoricIncidentEntity> completedIncidents =
      optimizeService.getCompletedHistoricIncidents(pastDate(), null, 3);

    // then
    assertThat(completedIncidents.size(), is(3));
  }

  @Test
  public void resultIsSortedByEndTime() {
     // given
    testHelper.deploy(FAILING_SERVICE_TASK_MODEL);
    Date now = new Date();
    Date nowPlus1Second = new Date(now.getTime() + 1000L);
    ClockUtil.setCurrentTime(nowPlus1Second);
    ProcessInstance processInstance1 = startProcessInstanceAndFailWithIncident();
    retryAndSucceed(processInstance1);

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    final ProcessInstance processInstance2 = startProcessInstanceAndFailWithIncident();
    retryAndSucceed(processInstance2);

    Date nowPlus4Seconds = new Date(nowPlus2Seconds.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus4Seconds);
    final ProcessInstance processInstance3 = startProcessInstanceAndFailWithIncident();
    retryAndSucceed(processInstance3);

    // when
    List<HistoricIncidentEntity> completedIncidents =
      optimizeService.getCompletedHistoricIncidents(now, null, 10);

    // then
    assertThat(completedIncidents.size(), is(3));
    assertThat(completedIncidents.get(0).getProcessInstanceId(), is(processInstance1.getId()));
    assertThat(completedIncidents.get(1).getProcessInstanceId(), is(processInstance2.getId()));
    assertThat(completedIncidents.get(2).getProcessInstanceId(), is(processInstance3.getId()));
  }

  private Date pastDate() {
    return new Date(2L);
  }

  private void assertThatInstanceHasAllImportantInformation(HistoricIncidentEntity historicIncidentEntity) {
    assertThat(historicIncidentEntity, notNullValue());
    assertThat(historicIncidentEntity.getId(), notNullValue());
    assertThat(historicIncidentEntity.getProcessDefinitionKey(), is(PROCESS_DEFINITION_KEY));
    assertThat(historicIncidentEntity.getProcessDefinitionVersion(), nullValue());
    assertThat(historicIncidentEntity.getProcessDefinitionId(), notNullValue());
    assertThat(historicIncidentEntity.getCreateTime(), notNullValue());
    assertThat(historicIncidentEntity.getEndTime(), notNullValue());
  }

  private void retryAndSucceed(final ProcessInstance processInstance) {
    runtimeService.setVariable(processInstance.getId(), "fail", false);
    String jobId = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult().getId();
    managementService.setJobRetries(jobId, 1);
  }

  private ProcessInstance startProcessInstanceAndFailWithIncident() {
    final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
    String jobId = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult().getId();
    managementService.setJobRetries(jobId, 0); // creates incident
    return processInstance;
  }

}
