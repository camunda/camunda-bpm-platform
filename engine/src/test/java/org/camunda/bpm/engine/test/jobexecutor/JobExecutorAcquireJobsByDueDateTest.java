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
package org.camunda.bpm.engine.test.jobexecutor;

import static org.camunda.bpm.engine.test.util.ClockTestUtil.incrementClock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.impl.persistence.entity.AcquirableJobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;

public class JobExecutorAcquireJobsByDueDateTest extends AbstractJobExecutorAcquireJobsTest {

  @Before
  public void prepareProcessEngineConfiguration() {
    configuration.setJobExecutorAcquireByDueDate(true);
  }

  @Test
  public void testProcessEngineConfiguration() {
    assertFalse(configuration.isJobExecutorPreferTimerJobs());
    assertTrue(configuration.isJobExecutorAcquireByDueDate());
    assertFalse(configuration.isJobExecutorAcquireByPriority());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void testMessageJobHasDueDateSet() {
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job.getDuedate());
    assertEquals(ClockUtil.getCurrentTime(), job.getDuedate());
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/jobexecutor/processWithTimerCatch.bpmn20.xml"
  })
  public void testOldJobsArePreferred() {
    // first start process with timer job
    ProcessInstance timerProcess1 = runtimeService.startProcessInstanceByKey("testProcess");
    // then start process with async task
    incrementClock(1);
    ProcessInstance asyncProcess1 = runtimeService.startProcessInstanceByKey("simpleAsyncProcess");
    // then start process with timer job
    incrementClock(1);
    ProcessInstance timerProcess2 = runtimeService.startProcessInstanceByKey("testProcess");
    // and another process with async task after the timers are acquirable
    incrementClock(61);
    ProcessInstance asyncProcess2 = runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    Job timerJob1 = managementService.createJobQuery().processInstanceId(timerProcess1.getId()).singleResult();
    Job timerJob2 = managementService.createJobQuery().processInstanceId(timerProcess2.getId()).singleResult();
    Job messageJob1 = managementService.createJobQuery().processInstanceId(asyncProcess1.getId()).singleResult();
    Job messageJob2 = managementService.createJobQuery().processInstanceId(asyncProcess2.getId()).singleResult();

    assertNotNull(timerJob1.getDuedate());
    assertNotNull(timerJob2.getDuedate());
    assertNotNull(messageJob1.getDuedate());
    assertNotNull(messageJob2.getDuedate());

    assertTrue(messageJob1.getDuedate().before(timerJob1.getDuedate()));
    assertTrue(timerJob1.getDuedate().before(timerJob2.getDuedate()));
    assertTrue(timerJob2.getDuedate().before(messageJob2.getDuedate()));

    List<AcquirableJobEntity> acquirableJobs = findAcquirableJobs();
    assertEquals(4, acquirableJobs.size());
    assertEquals(messageJob1.getId(), acquirableJobs.get(0).getId());
    assertEquals(timerJob1.getId(), acquirableJobs.get(1).getId());
    assertEquals(timerJob2.getId(), acquirableJobs.get(2).getId());
    assertEquals(messageJob2.getId(), acquirableJobs.get(3).getId());
  }

}
