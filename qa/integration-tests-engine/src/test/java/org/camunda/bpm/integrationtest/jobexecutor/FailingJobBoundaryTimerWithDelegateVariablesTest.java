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
package org.camunda.bpm.integrationtest.jobexecutor;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.integrationtest.jobexecutor.beans.DemoDelegate;
import org.camunda.bpm.integrationtest.jobexecutor.beans.DemoVariableClass;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Test Camunda Platform container job exectuor.
 * FAILING ATM!
 * Expected a job with an exception but it isn't left in db with 0 retries, instead it is completely removed from the job table!
 *
 * @author christian.lipphardt@camunda.com
 */
@RunWith(Arquillian.class)
public class FailingJobBoundaryTimerWithDelegateVariablesTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
            .addClass(DemoDelegate.class)
            .addClass(DemoVariableClass.class)
            .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/ImmediatelyFailing.bpmn20.xml");
  }

  @Test
  public void testFailingJobBoundaryTimerWithDelegateVariables() throws InterruptedException {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("ImmediatelyFailing");

    List<Job> jobs = managementService.createJobQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(1, jobs.size());
    assertEquals(3, jobs.get(0).getRetries());

    assertEquals(1, runtimeService.createExecutionQuery().processInstanceId(pi.getProcessInstanceId()).activityId("usertask1").count());
    assertEquals(2, runtimeService.createExecutionQuery().processInstanceId(pi.getProcessInstanceId()).count());

    assertEquals(1, managementService.createJobQuery().processInstanceId(pi.getProcessInstanceId()).executable().count());

    waitForJobExecutorToProcessAllJobs();

    assertEquals(0, managementService.createJobQuery().processInstanceId(pi.getProcessInstanceId()).executable().count()); // should be 0, because it has failed 3 times
    assertEquals(1, managementService.createJobQuery().processInstanceId(pi.getProcessInstanceId()).withException().count()); // should be 1, because job failed!

    assertEquals(1, runtimeService.createExecutionQuery().processInstanceId(pi.getProcessInstanceId()).activityId("usertask1").count());
    assertEquals(2, runtimeService.createExecutionQuery().processInstanceId(pi.getProcessInstanceId()).count());

    taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult().getId()); // complete task with failed job => complete process

    assertEquals(0, runtimeService.createExecutionQuery().processInstanceId(pi.getProcessInstanceId()).count());
    assertEquals(0, managementService.createJobQuery().processInstanceId(pi.getProcessInstanceId()).count()); // should be 0, because process is finished.
  }

}
