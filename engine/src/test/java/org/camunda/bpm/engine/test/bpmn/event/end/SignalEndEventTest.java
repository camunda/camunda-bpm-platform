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
package org.camunda.bpm.engine.test.bpmn.event.end;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Kristin Polenz
 */
public class SignalEndEventTest extends PluggableProcessEngineTestCase {
  
  @Deployment
  public void testCatchSignalEndEventInEmbeddedSubprocess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("catchSignalEndEventInEmbeddedSubprocess");
    assertNotNull(processInstance);
    
    // After process start, usertask in subprocess should exist
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("subprocessTask", task.getName());
    
    // After task completion, signal end event is reached and caught
    taskService.complete(task.getId());
    
    task = taskService.createTaskQuery().singleResult();
    assertEquals("task after catching the signal", task.getName());
    
    taskService.complete(task.getId());
    assertProcessEnded(processInstance.getId());
  }
  
  @Deployment(resources={
      "org/camunda/bpm/engine/test/bpmn/event/end/SignalEndEventTest.catchSignalEndEvent.bpmn20.xml", 
      "org/camunda/bpm/engine/test/bpmn/event/end/SignalEndEventTest.processWithSignalEndEvent.bpmn20.xml" 
    })
  public void testCatchSignalEndEventInCallActivity() throws Exception {
    // first, start process to wait of the signal event
    ProcessInstance processInstanceCatchEvent = runtimeService.startProcessInstanceByKey("catchSignalEndEvent");
    assertNotNull(processInstanceCatchEvent);

    // now we have a subscription for the signal event:
    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());
    assertEquals("alert", runtimeService.createEventSubscriptionQuery().singleResult().getEventName());

    // start process which throw the signal end event
    ProcessInstance processInstanceEndEvent = runtimeService.startProcessInstanceByKey("processWithSignalEndEvent");
    assertNotNull(processInstanceEndEvent);
    assertProcessEnded(processInstanceEndEvent.getId());

    // user task of process catchSignalEndEvent
    assertEquals(1, taskService.createTaskQuery().count());
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterSignalCatch", task.getTaskDefinitionKey());
    
    // complete user task
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstanceCatchEvent.getId());
  }

}
