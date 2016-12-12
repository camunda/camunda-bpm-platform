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
package org.camunda.bpm.engine.test.bpmn.async;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;

public class AsyncStartEventTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testAsyncStartEvent() {
    runtimeService.startProcessInstanceByKey("asyncStartEvent");

    Task task = taskService.createTaskQuery().singleResult();
    Assert.assertNull("The user task should not have been reached yet", task);

    Assert.assertEquals(1, runtimeService.createExecutionQuery().activityId("startEvent").count());

    executeAvailableJobs();
    task = taskService.createTaskQuery().singleResult();

    Assert.assertEquals(0, runtimeService.createExecutionQuery().activityId("startEvent").count());

    Assert.assertNotNull("The user task should have been reached", task);
  }

  @Deployment
  public void testAsyncStartEventListeners() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("asyncStartEvent");

    Assert.assertNull(runtimeService.getVariable(instance.getId(), "listener"));

    executeAvailableJobs();

    Assert.assertNotNull(runtimeService.getVariable(instance.getId(), "listener"));
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testAsyncStartEvent.bpmn20.xml")
  public void testAsyncStartEventActivityInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncStartEvent");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .transition("startEvent")
        .done());
  }

  @Deployment
  public void testMultipleAsyncStartEvents() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("foo", "bar");
    runtimeService.correlateMessage("newInvoiceMessage", new HashMap<String, Object>(), variables);

    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    executeAvailableJobs();

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("taskAfterMessageStartEvent", task.getTaskDefinitionKey());

    taskService.complete(task.getId());

    // assert process instance is ended
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testCallActivity-super.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testCallActivity-sub.bpmn20.xml"
  })
  public void testCallActivity() {
    runtimeService.startProcessInstanceByKey("super");

    ProcessInstance pi = runtimeService
        .createProcessInstanceQuery()
        .processDefinitionKey("sub")
        .singleResult();

    assertTrue(pi instanceof ExecutionEntity);

    assertEquals("theSubStart", ((ExecutionEntity)pi).getActivityId());

  }

  @Deployment
  public void testAsyncSubProcessStartEvent() {
    runtimeService.startProcessInstanceByKey("process");

    Task task = taskService.createTaskQuery().singleResult();
    assertNull("The subprocess user task should not have been reached yet", task);

    assertEquals(1, runtimeService.createExecutionQuery().activityId("StartEvent_2").count());

    executeAvailableJobs();
    task = taskService.createTaskQuery().singleResult();

    assertEquals(0, runtimeService.createExecutionQuery().activityId("StartEvent_2").count());
    assertNotNull("The subprocess user task should have been reached", task);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testAsyncSubProcessStartEvent.bpmn")
  public void testAsyncSubProcessStartEventActivityInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("SubProcess_1")
            .transition("StartEvent_2")
        .done());
  }
}
