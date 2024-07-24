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

import java.util.HashMap;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class SignalEventCatchBoundaryWithVariablesTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
            .addClass(SendSignalDelegate.class)
            .addClass(SignalReceivedDelegate.class)
            .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/SignalEventCatchBoundaryWithVariablesTest.catchAlertSignalBoundaryWithReceiveTask.bpmn20.xml")
            .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/SignalEventCatchBoundaryWithVariablesTest.throwAlertSignalWithDelegate.bpmn20.xml");
  }

  @Test
  public void testSignalCatchBoundaryWithVariables() throws InterruptedException {
    HashMap<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("processName", "catchSignal");
    ProcessInstance piCatchSignal = runtimeService.startProcessInstanceByKey("catchSignal", variables1);

    HashMap<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("processName", "throwSignal");
    variables2.put("signalProcessInstanceId", piCatchSignal.getProcessInstanceId());
    ProcessInstance piThrowSignal = runtimeService.startProcessInstanceByKey("throwSignal", variables2);

    waitForJobExecutorToProcessAllJobs();

    assertEquals(1, runtimeService.createExecutionQuery().processInstanceId(piCatchSignal.getProcessInstanceId()).activityId("receiveTask").count());
    assertEquals(1, runtimeService.createExecutionQuery().processInstanceId(piThrowSignal.getProcessInstanceId()).activityId("receiveTask").count());

    // TODO: THis fails because of http://jira.codehaus.org/browse/ACT-1257,
    // should be fixed and re-enabled :-)
    assertEquals("catchSignal-visited (was catchSignal)", runtimeService.getVariable(piCatchSignal.getId(), "processName"));
    assertEquals("throwSignal-visited (was throwSignal)", runtimeService.getVariable(piThrowSignal.getId(), "processName"));

    // clean up
    runtimeService.signal(piCatchSignal.getId());
    runtimeService.signal(piThrowSignal.getId());

    assertEquals(0, runtimeService.createExecutionQuery().processInstanceId(piCatchSignal.getProcessInstanceId()).count());
    assertEquals(0, runtimeService.createExecutionQuery().processInstanceId(piThrowSignal.getProcessInstanceId()).count());
  }

}
