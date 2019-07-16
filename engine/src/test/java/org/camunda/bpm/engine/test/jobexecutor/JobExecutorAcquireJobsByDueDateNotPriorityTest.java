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

import java.util.List;

import org.camunda.bpm.engine.impl.persistence.entity.AcquirableJobEntity;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;

public class JobExecutorAcquireJobsByDueDateNotPriorityTest extends AbstractJobExecutorAcquireJobsTest {

  @Before
  public void prepareProcessEngineConfiguration() {
    configuration.setJobExecutorAcquireByDueDate(true);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/jobPrioProcess.bpmn20.xml")
  public void testJobPriorityIsNotConsidered() {
    // prio 5
    String instance1 = startProcess("jobPrioProcess", "task2");

    // prio 10
    incrementClock(1);
    String instance2 = startProcess("jobPrioProcess", "task1");

    // prio 5
    incrementClock(1);
    String instance3 = startProcess("jobPrioProcess", "task2");

    // prio 10
    incrementClock(1);
    String instance4 = startProcess("jobPrioProcess", "task1");

    List<AcquirableJobEntity> acquirableJobs = findAcquirableJobs();
    assertEquals(4, acquirableJobs.size());

    assertEquals(5, (int) findJobById(acquirableJobs.get(0).getId()).getPriority());
    assertEquals(instance1, acquirableJobs.get(0).getProcessInstanceId());
    assertEquals(10, (int) findJobById(acquirableJobs.get(1).getId()).getPriority());
    assertEquals(instance2, acquirableJobs.get(1).getProcessInstanceId());
    assertEquals(5, (int) findJobById(acquirableJobs.get(2).getId()).getPriority());
    assertEquals(instance3, acquirableJobs.get(2).getProcessInstanceId());
    assertEquals(10, (int) findJobById(acquirableJobs.get(3).getId()).getPriority());
    assertEquals(instance4, acquirableJobs.get(3).getProcessInstanceId());
  }


  protected Job findJobById(String id) {
    return managementService.createJobQuery().jobId(id).singleResult();
  }

}
