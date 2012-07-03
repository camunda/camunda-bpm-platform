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
package com.camunda.fox.platform.test.jobexecutor;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import javax.inject.Inject;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;

@RunWith(Arquillian.class)
public class SignalEventCatchBoundaryWithVariablesTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
            .addClass(SendSignalDelegate.class)
            .addClass(SignalReceivedDelegate.class)
            .addAsResource("com/camunda/fox/platform/test/jobexecutor/SignalEventCatchBoundaryWithVariablesTest.catchAlertSignalBoundaryWithReceiveTask.bpmn20.xml")
            .addAsResource("com/camunda/fox/platform/test/jobexecutor/SignalEventCatchBoundaryWithVariablesTest.throwAlertSignalWithDelegate.bpmn20.xml")
            .addAsWebInfResource("persistence.xml", "classes/META-INF/persistence.xml");
  }

  @Inject
  private RuntimeService runtimeService;

  @Test
  public void testSignalCatchBoundaryWithVariables() throws InterruptedException {
    HashMap<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("processName", "catchSignal");
    ProcessInstance piCatchSignal = runtimeService.startProcessInstanceByKey("catchSignal", variables1);

    HashMap<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("processName", "throwSignal");
    variables2.put("signalProcessInstanceId", piCatchSignal.getProcessInstanceId());
    ProcessInstance piThrowSignal = runtimeService.startProcessInstanceByKey("throwSignal", variables2);

    waitForJobExecutorToProcessAllJobs(2000, 200);

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
