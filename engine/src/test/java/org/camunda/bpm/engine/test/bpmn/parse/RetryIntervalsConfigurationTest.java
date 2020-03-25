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
package org.camunda.bpm.engine.test.bpmn.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.api.AbstractAsyncOperationsTest;
import org.camunda.bpm.engine.test.bpmn.executionlistener.RecorderExecutionListener;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class RetryIntervalsConfigurationTest extends AbstractAsyncOperationsTest {

  private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  private static final String PROCESS_ID = "process";
  private static final String FAILING_CLASS = "this.class.does.not.Exist";

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      configuration.setFailedJobRetryTimeCycle("PT5M,PT20M, PT3M");
      configuration.setEnableExceptionsAfterUnhandledBpmnError(true);
      return configuration;
    }
  };

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(testRule);

  @Before
  public void setUp() {
    initDefaults(engineRule);
  }

  @Test
  public void testRetryGlobalConfiguration() throws ParseException {
    // given global retry conf. ("PT5M,PT20M, PT3M")
    BpmnModelInstance bpmnModelInstance = prepareProcessFailingServiceTask();
    testRule.deploy(bpmnModelInstance);

    ClockUtil.setCurrentTime(SIMPLE_DATE_FORMAT.parse("2017-01-01T09:55:00"));

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS_ID);

    Date currentTime = SIMPLE_DATE_FORMAT.parse("2017-01-01T10:00:00");
    ClockUtil.setCurrentTime(currentTime);

    String processInstanceId = pi.getProcessInstanceId();

    int jobRetries = executeJob(processInstanceId);
    assertEquals(3, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 5);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    jobRetries = executeJob(processInstanceId);
    assertEquals(2, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 20);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    jobRetries = executeJob(processInstanceId);
    assertEquals(1, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 3);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    jobRetries = executeJob(processInstanceId);
    assertEquals(0, jobRetries);
  }

  @Test
  public void testRetryGlobalConfigurationWithExecutionListener() throws ParseException {
    // given
    engineRule.getProcessEngineConfiguration().setFailedJobRetryTimeCycle("PT5M");

    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
    .startEvent()
    .serviceTask()
      .camundaClass(FAILING_CLASS)
      .camundaAsyncBefore()
      .camundaExecutionListenerClass(RecorderExecutionListener.EVENTNAME_START, RecorderExecutionListener.class.getName())
    .endEvent()
    .done();
    testRule.deploy(bpmnModelInstance);

    ClockUtil.setCurrentTime(SIMPLE_DATE_FORMAT.parse("2017-01-01T09:55:00"));

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS_ID);

    Date currentTime = SIMPLE_DATE_FORMAT.parse("2017-01-01T10:00:00");
    ClockUtil.setCurrentTime(currentTime);

    String processInstanceId = pi.getProcessInstanceId();

    int jobRetries = executeJob(processInstanceId);
    assertEquals(1, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 5);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    jobRetries = executeJob(processInstanceId);
    assertEquals(0, jobRetries);
  }

  @Test
  public void testRetryMixConfiguration() throws ParseException {
    // given
    BpmnModelInstance bpmnModelInstance = prepareProcessFailingServiceTaskWithRetryCycle("R3/PT1M");

    testRule.deploy(bpmnModelInstance);

    ClockUtil.setCurrentTime(SIMPLE_DATE_FORMAT.parse("2017-01-01T09:55:00"));

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS_ID);
    assertNotNull(pi);

    Date currentTime = SIMPLE_DATE_FORMAT.parse("2017-01-01T10:00:00");
    ClockUtil.setCurrentTime(currentTime);

    String processInstanceId = pi.getProcessInstanceId();

    int jobRetries;

    for (int i = 0; i < 3; i++) {
      jobRetries = executeJob(processInstanceId);
      assertEquals(2 - i, jobRetries);
      currentTime = DateUtils.addMinutes(currentTime, 1);
      assertDueDateTime(currentTime);
      ClockUtil.setCurrentTime(currentTime);
    }
  }

  @Test
  public void testRetryIntervals() throws ParseException {
    // given
    BpmnModelInstance bpmnModelInstance = prepareProcessFailingServiceTaskWithRetryCycle("PT3M, PT10M,PT8M");
    testRule.deploy(bpmnModelInstance);

    ClockUtil.setCurrentTime(SIMPLE_DATE_FORMAT.parse("2017-01-01T09:55:00"));

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS_ID);
    assertNotNull(pi);

    Date currentTime = SIMPLE_DATE_FORMAT.parse("2017-01-01T10:00:00");
    ClockUtil.setCurrentTime(currentTime);

    String processInstanceId = pi.getProcessInstanceId();

    int jobRetries = executeJob(processInstanceId);
    assertEquals(3, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 3);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    jobRetries = executeJob(processInstanceId);
    assertEquals(2, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 10);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    jobRetries = executeJob(processInstanceId);
    assertEquals(1, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 8);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    jobRetries = executeJob(processInstanceId);
    assertEquals(0, jobRetries);
  }

  @Test
  public void testSingleRetryInterval() throws ParseException {
    // given
    BpmnModelInstance bpmnModelInstance = prepareProcessFailingServiceTaskWithRetryCycle("PT8M ");
    testRule.deploy(bpmnModelInstance);

    ClockUtil.setCurrentTime(SIMPLE_DATE_FORMAT.parse("2017-01-01T09:55:00"));

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS_ID);
    assertNotNull(pi);

    Date currentTime = SIMPLE_DATE_FORMAT.parse("2017-01-01T10:00:00");
    ClockUtil.setCurrentTime(currentTime);

    String processInstanceId = pi.getProcessInstanceId();

    int jobRetries = executeJob(processInstanceId);
    assertEquals(1, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 8);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    jobRetries = executeJob(processInstanceId);
    assertEquals(0, jobRetries);
  }

  @Test
  public void testRetryWithVarList() {
    // given
    BpmnModelInstance bpmnModelInstance = prepareProcessFailingServiceTaskWithRetryCycle("${var}");
    testRule.deploy(bpmnModelInstance);

    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("var", "PT1M,PT2M,PT3M,PT4M,PT5M,PT6M,PT7M,PT8M"));

    Job job = managementService.createJobQuery().singleResult();

    // when job fails
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    // then
    job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(8, job.getRetries());
  }

  @Test
  public void testIntervalsAfterUpdateRetries() throws ParseException {
    // given
    BpmnModelInstance bpmnModelInstance = prepareProcessFailingServiceTaskWithRetryCycle("PT3M, PT10M,PT8M");
    testRule.deploy(bpmnModelInstance);

    ClockUtil.setCurrentTime(SIMPLE_DATE_FORMAT.parse("2017-01-01T09:55:00"));

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS_ID);
    assertNotNull(pi);

    Date currentTime = SIMPLE_DATE_FORMAT.parse("2017-01-01T10:00:00");
    ClockUtil.setCurrentTime(currentTime);

    String processInstanceId = pi.getProcessInstanceId();

    int jobRetries = executeJob(processInstanceId);
    assertEquals(3, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 3);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    Job job = managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();
    managementService.setJobRetries(Arrays.asList(job.getId()), 5);

    jobRetries = executeJob(processInstanceId);
    assertEquals(4, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 3);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    jobRetries = executeJob(processInstanceId);
    assertEquals(3, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 3);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    jobRetries = executeJob(processInstanceId);
    assertEquals(2, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 10);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    jobRetries = executeJob(processInstanceId);
    assertEquals(1, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 8);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    jobRetries = executeJob(processInstanceId);
    assertEquals(0, jobRetries);
  }

  @Test
  public void testMixConfigurationWithinOneProcess() throws ParseException {
    // given
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .serviceTask("Task1")
          .camundaClass(ServiceTaskDelegate.class.getName())
          .camundaAsyncBefore()
        .serviceTask("Task2")
          .camundaClass(FAILING_CLASS)
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle("PT3M, PT10M,PT8M")
        .endEvent()
        .done();
    testRule.deploy(bpmnModelInstance);

    ClockUtil.setCurrentTime(SIMPLE_DATE_FORMAT.parse("2017-01-01T09:55:00"));

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS_ID);
    assertNotNull(pi);

    Date currentTime = SIMPLE_DATE_FORMAT.parse("2017-01-01T10:00:00");
    ClockUtil.setCurrentTime(currentTime);

    String processInstanceId = pi.getProcessInstanceId();

    // try to execute the first service task without success
    int jobRetries = executeJob(processInstanceId);
    assertEquals(3, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 5);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    ServiceTaskDelegate.firstAttempt = false;

    // finish the first service task
    jobRetries = executeJob(processInstanceId);
    assertEquals(3, jobRetries);

    // try to execute the second service task without success
    jobRetries = executeJob(processInstanceId);
    assertEquals(3, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 3);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

  }

  @Test
  public void testlocalConfigurationWithNestedChangingExpression() throws ParseException {
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .serviceTask()
          .camundaClass("foo")
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle("${var}")
        .endEvent()
        .done();

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    Date startDate = simpleDateFormat.parse("2017-01-01T09:55:00");
    ClockUtil.setCurrentTime(startDate);

    testRule.deploy(bpmnModelInstance);

    VariableMap params = Variables.createVariables();
    params.putValue("var", "${nestedVar1},PT15M,${nestedVar3}");
    params.putValue("nestedVar", "PT13M");
    params.putValue("nestedVar1", "PT5M");
    params.putValue("nestedVar3", "PT25M");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process", params);

    ClockUtil.setCurrentTime(SIMPLE_DATE_FORMAT.parse("2017-01-01T09:55:00"));

    assertNotNull(pi);

    Date currentTime = SIMPLE_DATE_FORMAT.parse("2017-01-01T10:00:00");
    ClockUtil.setCurrentTime(currentTime);

    String processInstanceId = pi.getProcessInstanceId();

    int jobRetries = executeJob(processInstanceId);
    assertEquals(3, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 5);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    jobRetries = executeJob(processInstanceId);
    assertEquals(2, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 15);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    runtimeService.setVariable(pi.getProcessInstanceId(), "var", "${nestedVar}");

    jobRetries = executeJob(processInstanceId);
    assertEquals(1, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 13);
    assertDueDateTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    jobRetries = executeJob(processInstanceId);
    assertEquals(0, jobRetries);
  }

  private int executeJob(String processInstanceId) {
    Job job = fetchJob(processInstanceId);

    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    job = fetchJob(processInstanceId);

    return job.getRetries();
  }

  private void assertDueDateTime(Date expectedDate) throws ParseException {
    Date dueDateTime = managementService.createJobQuery().singleResult().getDuedate();
    assertEquals(expectedDate, dueDateTime);
  }

  private Job fetchJob(String processInstanceId) {
    return managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();
  }

  private BpmnModelInstance prepareProcessFailingServiceTask() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .serviceTask()
          .camundaClass(FAILING_CLASS)
          .camundaAsyncBefore()
        .endEvent()
        .done();
    return modelInstance;
  }

  private BpmnModelInstance prepareProcessFailingServiceTaskWithRetryCycle(String retryTimeCycle) {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .serviceTask()
          .camundaClass(FAILING_CLASS)
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle(retryTimeCycle)
        .endEvent()
        .done();
    return modelInstance;
  }

}
