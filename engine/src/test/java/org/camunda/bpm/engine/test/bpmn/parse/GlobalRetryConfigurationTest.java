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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class GlobalRetryConfigurationTest {

  private static final String PROCESS_ID = "process";
  private static final String FAILING_CLASS = "this.class.does.not.Exist";
  private static final String FAILING_EVENT = "failingEvent";
  private static final String SCHEDULE = "R5/PT5M";
  private static final int JOB_RETRIES = 4;

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
      configuration.setFailedJobRetryTimeCycle(SCHEDULE));
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(testRule);

  private RuntimeService runtimeService;
  private ManagementService managementService;

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
  }

  @Test
  public void testFailedServiceTaskStandardStrategy() {
    engineRule.getProcessEngineConfiguration().setFailedJobRetryTimeCycle(null);
    BpmnModelInstance bpmnModelInstance = prepareFailingServiceTask();

    testRule.deploy(bpmnModelInstance);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS_ID);

    assertJobRetries(pi, 2);
  }

  @Test
  public void testFailedIntermediateThrowingSignalEventAsync() {
    BpmnModelInstance bpmnModelInstance = prepareSignalEventProcessWithoutRetry();

    testRule.deploy(bpmnModelInstance);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS_ID);
    assertJobRetries(pi, 4);
  }

  @Test
  public void testFailedServiceTask() {
    BpmnModelInstance bpmnModelInstance = prepareFailingServiceTask();

    testRule.deploy(bpmnModelInstance);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS_ID);

    assertJobRetries(pi, 4);
  }

  @Test
  public void testFailedServiceTaskMixConfiguration() {
    BpmnModelInstance bpmnModelInstance = prepareFailingServiceTaskWithRetryCycle();

    testRule.deploy(bpmnModelInstance);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS_ID);

    assertJobRetries(pi, 9);
  }

  @Test
  public void testFailedBusinessRuleTask() {
    BpmnModelInstance bpmnModelInstance = prepareFailingBusinessRuleTask();

    testRule.deploy(bpmnModelInstance);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS_ID);

    assertJobRetries(pi, JOB_RETRIES);
  }

  @Test
  public void testFailedCallActivity() {

    testRule.deploy(
      Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .callActivity()
          .calledElement("testProcess2")
        .endEvent()
        .done(),
      Bpmn.createExecutableProcess("testProcess2")
        .startEvent()
        .serviceTask()
          .camundaClass(FAILING_CLASS)
          .camundaAsyncBefore()
        .endEvent()
      .done());

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess2");

    assertJobRetries(pi, 4);
  }

  @Test
  public void testFailingScriptTask() {
    BpmnModelInstance bpmnModelInstance = prepareFailingScriptTask();

    testRule.deploy(bpmnModelInstance);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS_ID);

    assertJobRetries(pi, 4);
  }

  @Test
  public void testFailingSubProcess() {
    BpmnModelInstance bpmnModelInstance = prepareFailingSubProcess();

    testRule.deploy(bpmnModelInstance);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS_ID);

    assertJobRetries(pi, 4);
  }

  @Test
  public void testRetryOnAsyncStartEvent() throws Exception {
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle("R5/PT5M")
        .serviceTask()
          .camundaClass("bar")
        .endEvent()
        .done();

    testRule.deploy(bpmnModelInstance);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    assertJobRetries(processInstance, 4);
  }

  private void assertJobRetries(ProcessInstance pi, int expectedJobRetries) {
    assertThat(pi, is(notNullValue()));

    Job job = fetchJob(pi.getProcessInstanceId());

    try {

      managementService.executeJob(job.getId());
    } catch (Exception e) {
    }

    // update job
    job = fetchJob(pi.getProcessInstanceId());
    assertEquals(expectedJobRetries, job.getRetries());
  }

  private Job fetchJob(String processInstanceId) {
    return managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();
  }

  private BpmnModelInstance prepareSignalEventProcessWithoutRetry() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .intermediateThrowEvent(FAILING_EVENT)
            .camundaAsyncBefore(true)
            .signal("start")
          .serviceTask()
            .camundaClass(FAILING_CLASS)
        .endEvent()
        .done();
    return modelInstance;
  }

  private BpmnModelInstance prepareFailingServiceTask() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .serviceTask()
          .camundaClass(FAILING_CLASS)
          .camundaAsyncBefore()
        .endEvent()
        .done();
    return modelInstance;
  }

  private BpmnModelInstance prepareFailingServiceTaskWithRetryCycle() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .serviceTask()
          .camundaClass(FAILING_CLASS)
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle("R10/PT5M")
        .endEvent()
        .done();
    return modelInstance;
  }

  private BpmnModelInstance prepareFailingBusinessRuleTask() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .businessRuleTask()
          .camundaClass(FAILING_CLASS)
          .camundaAsyncBefore()
        .endEvent()
        .done();
    return modelInstance;
  }

  private BpmnModelInstance prepareFailingScriptTask() {
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
      .startEvent()
      .scriptTask()
        .scriptFormat("groovy")
        .scriptText("x = 5 / 0")
        .camundaAsyncBefore()
      .userTask()
      .endEvent()
    .done();
    return bpmnModelInstance;
  }

  private BpmnModelInstance prepareFailingSubProcess() {
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
      .startEvent()
      .subProcess()
        .embeddedSubProcess()
          .startEvent()
          .serviceTask()
            .camundaClass(FAILING_CLASS)
            .camundaAsyncBefore()
          .endEvent()
      .subProcessDone()
      .endEvent()
    .done();
    return bpmnModelInstance;
  }
}