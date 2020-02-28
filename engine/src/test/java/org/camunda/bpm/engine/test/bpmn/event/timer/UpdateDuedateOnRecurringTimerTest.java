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
package org.camunda.bpm.engine.test.bpmn.event.timer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class UpdateDuedateOnRecurringTimerTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  RuntimeService runtimeService;
  ManagementService managementService;
  Date t0;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();

    t0 = new Date(0);
    ClockUtil.setCurrentTime(t0);
  }

  @After
  public void resetClock() {
    ClockUtil.resetClock();
  }

  @Test
  public void testCascadeChangeToRecurringTimerAddToDuedate() {
    // given timer R2/PT30M
    BpmnModelInstance process = Bpmn.createExecutableProcess("process").startEvent().userTask("userTask").boundaryEvent().cancelActivity(false)
        .timerWithCycle("R2/PT30M").endEvent().moveToActivity("userTask").endEvent().done();
    testRule.deploy(process);
    runtimeService.startProcessInstanceByKey("process");

    // when

    // offset job1 due date by +15 minutes (=> due t0 + 45 minutes)
    Job job1 = managementService.createJobQuery().singleResult();
    job1 = modifyDueDate(job1, true, minutes(15));

    // currentTime = due date + 5 seconds
    Date t1 = new Date(t0.getTime() + minutes(45) + 5000);
    setTimeAndExecuteJobs(t1);

    // job2 should keep the offset of +15 minutes (=> due t1 + 30 minutes)
    Job job2 = managementService.createJobQuery().singleResult();

    // currentTime = due date + 5 seconds
    Date t2 = new Date(t1.getTime() + minutes(30));
    setTimeAndExecuteJobs(t2);

    // then
    assertThat(job1.getId()).isNotEqualTo(job2.getId());
    // job1 is due after 45 minutes (30 + 15 offset)
    assertThat(job1.getDuedate().getTime()).isEqualTo(t0.getTime() + minutes(45));
    // job2 is due 30 minutes after job1 (keeps offset due to cascade=true)
    assertThat(job2.getDuedate().getTime()).isEqualTo(job1.getDuedate().getTime() + minutes(30));
  }

  @Test
  public void testCascadeChangeToRecurringTimerAddToDuedateMultipleTimes() {
    // given timer R3/PT30M
    BpmnModelInstance process = Bpmn.createExecutableProcess("process").startEvent().userTask("userTask").boundaryEvent().cancelActivity(false)
        .timerWithCycle("R3/PT30M").endEvent().moveToActivity("userTask").endEvent().done();
    testRule.deploy(process);
    runtimeService.startProcessInstanceByKey("process");

    // when

    // offset job1 due date by +15 minutes (=> due t0 + 45 minutes)
    Job job1 = managementService.createJobQuery().singleResult();
    job1 = modifyDueDate(job1, true, minutes(15));

    // currentTime = due date + 5 seconds
    Date t1 = new Date(t0.getTime() + minutes(45) + 5000);
    setTimeAndExecuteJobs(t1);

    // job2 should keep the offset of +15 minutes (=> due t1 + 30 minutes)
    Job job2 = managementService.createJobQuery().singleResult();
    // offset job2 due date by -5 minutes, which makes an overall offset of +10
    // minutes (=> due t1 + 25 minutes or t0 + 70 minutes)
    job2 = modifyDueDate(job2, true, negative(minutes(5)));

    // currentTime = due date + 5 seconds
    Date t2 = new Date(t1.getTime() + minutes(25));
    setTimeAndExecuteJobs(t2);

    // job3 should keep the offset of +10 minutes (=> due t2 + 30 minutes or t0
    // + 100 minutes)
    Job job3 = managementService.createJobQuery().singleResult();

    // currentTime = due date + 5 seconds
    Date t3 = new Date(t2.getTime() + minutes(30));
    setTimeAndExecuteJobs(t3);

    // then
    assertThat(ClockUtil.getCurrentTime()).isAfter(job3.getDuedate());
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
    // no duplicates
    assertThat(new HashSet<String>(Arrays.asList(job1.getId(), job2.getId(), job3.getId())).size()).isEqualTo(3);
    // job1 is due after 45 minutes (30 + 15 offset)
    assertThat(job1.getDuedate().getTime()).isEqualTo(t0.getTime() + minutes(45));
    // job2 is due 25 minutes after job1 (keeps offset due to cascade=true and
    // is offset by additional -5 minutes)
    assertThat(job2.getDuedate().getTime()).isEqualTo(job1.getDuedate().getTime() + minutes(25));
    // job3 is due 30 minutes after job2 (keeps offset due to cascade=true)
    assertThat(job3.getDuedate().getTime()).isEqualTo(job2.getDuedate().getTime() + minutes(30));
  }

  @Test
  public void testCascadeChangeToRecurringTimerSubstractFromDuedate() {
    // given timer R2/PT30M
    BpmnModelInstance process = Bpmn.createExecutableProcess("process").startEvent().userTask("userTask").boundaryEvent().cancelActivity(false)
        .timerWithCycle("R2/PT30M").endEvent().moveToActivity("userTask").endEvent().done();
    testRule.deploy(process);
    runtimeService.startProcessInstanceByKey("process");

    // when

    // offset job1 due date by -15 minutes (=> due t0 + 15 minutes)
    Job job1 = managementService.createJobQuery().singleResult();
    job1 = modifyDueDate(job1, true, negative(minutes(15)));

    // currentTime = due date + 5 seconds
    Date t1 = new Date(t0.getTime() + minutes(15) + 5000);
    setTimeAndExecuteJobs(t1);

    // job2 should keep the offset of -15 minutes (=> due t1 + 30 minutes)
    Job job2 = managementService.createJobQuery().singleResult();

    // currentTime = due date + 5 seconds
    Date t2 = new Date(t1.getTime() + minutes(30));
    setTimeAndExecuteJobs(t2);

    // then
    assertThat(job1.getId()).isNotEqualTo(job2.getId());
    // job1 is due after 15 minutes (30 - 15 offset)
    assertThat(job1.getDuedate().getTime()).isEqualTo(t0.getTime() + minutes(15));
    // job2 is due 30 minutes after job1 (keeps offset due to cascade=true)
    assertThat(job2.getDuedate().getTime()).isEqualTo(job1.getDuedate().getTime() + minutes(30));
  }

  @Test
  public void testCascadeMixedChangesToRecurringTimerDuedate() {
    // given timer R3/PT30M
    BpmnModelInstance process = Bpmn.createExecutableProcess("process").startEvent().userTask("userTask").boundaryEvent().cancelActivity(false)
        .timerWithCycle("R3/PT30M").endEvent().moveToActivity("userTask").endEvent().done();
    testRule.deploy(process);
    runtimeService.startProcessInstanceByKey("process");

    // when

    Job job1 = managementService.createJobQuery().singleResult();
    job1 = modifyDueDate(job1, true, negative(minutes(15)));

    // currentTime = due date + 5 seconds
    Date t1 = new Date(t0.getTime() + minutes(15) + 5000);
    setTimeAndExecuteJobs(t1);

    Job job2 = managementService.createJobQuery().singleResult();
    job2 = modifyDueDate(job2, false, minutes(10));

    // currentTime = due date + 5 seconds
    Date t2 = new Date(t1.getTime() + minutes(55));
    setTimeAndExecuteJobs(t2);

    Job job3 = managementService.createJobQuery().singleResult();

    // currentTime = due date + 5 seconds
    Date t3 = new Date(t2.getTime() + minutes(10));
    setTimeAndExecuteJobs(t3);

    // then
    // no duplicate jobs
    assertThat(new HashSet<String>(Arrays.asList(job1.getId(), job2.getId(), job3.getId())).size()).isEqualTo(3);
    // job1 is due at t=15
    assertThat(job1.getDuedate().getTime()).isEqualTo(t0.getTime() + minutes(15));
    // job2 is due 40 minutes after job1 (keeps offset due to cascade=true at
    // job1,
    // additional offset by +10 that does not cascade)
    assertThat(job2.getDuedate().getTime()).isEqualTo(job1.getDuedate().getTime() + minutes(40));
    // job3 is due 60 minutes after job1 (keeps offset due to cascade=true at
    // job1,
    // offset at job2 is ignored due to cascade=false)
    assertThat(job3.getDuedate().getTime()).isEqualTo(job1.getDuedate().getTime() + minutes(60));
  }

  @Test
  public void testChangesToRecurringTimerDuedateShouldNotCascade() {
    // given timer R3/PT30M
    BpmnModelInstance process = Bpmn.createExecutableProcess("process").startEvent().userTask("userTask").boundaryEvent().cancelActivity(false)
        .timerWithCycle("R3/PT30M").endEvent().moveToActivity("userTask").endEvent().done();
    testRule.deploy(process);
    runtimeService.startProcessInstanceByKey("process");

    // when

    Job job1 = managementService.createJobQuery().singleResult();
    job1 = modifyDueDate(job1, false, negative(minutes(15)));

    // currentTime = due date + 5 seconds
    Date t1 = new Date(t0.getTime() + minutes(15) + 5000);
    setTimeAndExecuteJobs(t1);

    Job job2 = managementService.createJobQuery().singleResult();

    // currentTime = due date + 5 seconds
    Date t2 = new Date(t1.getTime() + minutes(15));
    setTimeAndExecuteJobs(t2);

    Job job3 = managementService.createJobQuery().singleResult();

    // currentTime = due date + 5 seconds
    Date t3 = new Date(t2.getTime() + minutes(30));
    setTimeAndExecuteJobs(t3);

    // then
    // no duplicate jobs
    assertThat(new HashSet<String>(Arrays.asList(job1.getId(), job2.getId(), job3.getId())).size()).isEqualTo(3);
    // job1 is due at t=15
    assertThat(job1.getDuedate().getTime()).isEqualTo(t0.getTime() + minutes(15));
    // job2 is due 15 minutes after job1 (ignores offset due to cascade=false at
    // job1)
    assertThat(job2.getDuedate().getTime()).isEqualTo(job1.getDuedate().getTime() + minutes(15));
    // job3 is due 30 minutes after job2 (ignores offset due to cascade=false at
    // job1)
    assertThat(job3.getDuedate().getTime()).isEqualTo(job2.getDuedate().getTime() + minutes(30));
  }

  private void setTimeAndExecuteJobs(Date time) {
    ClockUtil.setCurrentTime(time);
    testRule.waitForJobExecutorToProcessAllJobs(5000);
  }

  private Job modifyDueDate(Job job, boolean cascade, long offset) {
    managementService.setJobDuedate(job.getId(), new Date(job.getDuedate().getTime() + offset), cascade);
    return managementService.createJobQuery().singleResult();
  }

  private long minutes(int minutes) {
    return TimeUnit.MINUTES.toMillis(minutes);
  }

  private long negative(long value) {
    return value * -1;
  }
}
