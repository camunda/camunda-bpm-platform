/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class IncremenatalRetryIntervalConfigurationTest {

  private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  private static final String PROCESS_ID = "process";
  private static final String FAILING_CLASS = "this.class.does.not.Exist";

  public ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      configuration.setFailedJobRetryTimeCycle("R6/PT5M");
      configuration.setIncrementalIntervals("PT20M, PT3M");
      return configuration;
    }
  };

  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(testRule);

  private Deployment currentDeployment;
  private RuntimeService runtimeService;
  private ManagementService managementService;

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
  }

  @After
  public void tearDown() {
    if (currentDeployment != null) {

      engineRule.getRepositoryService().deleteDeployment(currentDeployment.getId(), true, true);
    }
    ClockUtil.setCurrentTime(new Date());
  }

  @Test
  public void testFailedServiceTask() throws ParseException {
    // given global retry conf. ("R6/PT5M") & ("PT20M, PT3M")
    BpmnModelInstance bpmnModelInstance = prepareFailingServiceTask();
    currentDeployment = testRule.deploy(bpmnModelInstance);

    ClockUtil.setCurrentTime(SIMPLE_DATE_FORMAT.parse("2017-01-01T09:55:00"));

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS_ID);

    Date currentTime = SIMPLE_DATE_FORMAT.parse("2017-01-01T10:00:00");
    ClockUtil.setCurrentTime(currentTime);

    String processInstanceId = pi.getProcessInstanceId();

    int jobRetries = executeJob(processInstanceId);
    assertEquals(5, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 5);
    assertLockExpirationTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    jobRetries = executeJob(processInstanceId);
    assertEquals(4, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 20);
    assertLockExpirationTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    for (int i = 0; i < 4; i++) {
      jobRetries = executeJob(processInstanceId);
      assertEquals(3 - i, jobRetries);
      currentTime = DateUtils.addMinutes(currentTime, 3);
      assertLockExpirationTime(currentTime);
      ClockUtil.setCurrentTime(currentTime);
    }
  }

  @Test
  public void testFailedServiceTaskMixConfiguration() throws ParseException {
    // given local retry conf. "R3/PT1M"
    BpmnModelInstance bpmnModelInstance = prepareFailingServiceTaskWithRetryCycle();

    currentDeployment = testRule.deploy(bpmnModelInstance);

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
      assertLockExpirationTime(currentTime);
      ClockUtil.setCurrentTime(currentTime);
    }
  }

  @Test
  public void testFailedServiceTaskIncrementalIntervals() throws ParseException {
    // given local retry conf. ("R5/PT3M") & ("PT10M,PT8M")
    BpmnModelInstance bpmnModelInstance = prepareFailingServiceTaskWithRetryCycleAndInterval();
    currentDeployment = testRule.deploy(bpmnModelInstance);

    ClockUtil.setCurrentTime(SIMPLE_DATE_FORMAT.parse("2017-01-01T09:55:00"));

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS_ID);
    assertNotNull(pi);

    Date currentTime = SIMPLE_DATE_FORMAT.parse("2017-01-01T10:00:00");
    ClockUtil.setCurrentTime(currentTime);

    String processInstanceId = pi.getProcessInstanceId();

    int jobRetries = executeJob(processInstanceId);
    assertEquals(4, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 3);
    assertLockExpirationTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    jobRetries = executeJob(processInstanceId);
    assertEquals(3, jobRetries);
    currentTime = DateUtils.addMinutes(currentTime, 10);
    assertLockExpirationTime(currentTime);
    ClockUtil.setCurrentTime(currentTime);

    for (int i = 0; i < 3; i++) {
      jobRetries = executeJob(processInstanceId);
      assertEquals(2 - i, jobRetries);
      currentTime = DateUtils.addMinutes(currentTime, 8);
      assertLockExpirationTime(currentTime);
      ClockUtil.setCurrentTime(currentTime);
    }
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

  private void assertLockExpirationTime(Date expectedDate) throws ParseException {
    Date lockExpirationTime = ((JobEntity) managementService.createJobQuery().singleResult()).getLockExpirationTime();
    assertEquals(expectedDate, lockExpirationTime);
  }

  private Job fetchJob(String processInstanceId) {
    return managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();
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
          .camundaFailedJobRetryTimeCycle("R3/PT1M")
        .endEvent()
        .done();
    return modelInstance;
  }

  private BpmnModelInstance prepareFailingServiceTaskWithRetryCycleAndInterval() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
        .serviceTask()
          .camundaClass(FAILING_CLASS)
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle("R5/PT3M")
          .camundaIncrementalIntervals("PT10M,PT8M")
        .endEvent()
        .done();
    return modelInstance;
  }

}
