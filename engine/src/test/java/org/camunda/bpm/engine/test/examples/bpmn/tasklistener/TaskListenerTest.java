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
package org.camunda.bpm.engine.test.examples.bpmn.tasklistener;

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

import static org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT;


/**
 * @author Joram Barrez
 */
public class TaskListenerTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = {"org/camunda/bpm/engine/test/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskCreateListener() {
    runtimeService.startProcessInstanceByKey("taskListenerProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Schedule meeting", task.getName());
    assertEquals("TaskCreateListener is listening!", task.getDescription());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskCompleteListener() {
    TaskDeleteListener.clear();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
    assertEquals(null, runtimeService.getVariable(processInstance.getId(), "greeting"));
    assertEquals(null, runtimeService.getVariable(processInstance.getId(), "expressionValue"));

    // Completing first task will change the description
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // Check that the completion did not execute the delete listener
    assertEquals(0, TaskDeleteListener.eventCounter);
    assertNull(TaskDeleteListener.lastTaskDefinitionKey);
    assertNull(TaskDeleteListener.lastDeleteReason);

    assertEquals("Hello from The Process", runtimeService.getVariable(processInstance.getId(), "greeting"));
    assertEquals("Act", runtimeService.getVariable(processInstance.getId(), "shortName"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskDeleteListenerByProcessDeletion() {
    TaskDeleteListener.clear();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");

    assertEquals(0, TaskDeleteListener.eventCounter);
    assertNull(TaskDeleteListener.lastTaskDefinitionKey);
    assertNull(TaskDeleteListener.lastDeleteReason);

    // delete process instance to delete task
    Task task = taskService.createTaskQuery().singleResult();
    runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), "test delete task listener");

    assertEquals(1, TaskDeleteListener.eventCounter);
    assertEquals(task.getTaskDefinitionKey(), TaskDeleteListener.lastTaskDefinitionKey);
    assertEquals("test delete task listener", TaskDeleteListener.lastDeleteReason);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskDeleteListenerByBoundaryEvent() {
    TaskDeleteListener.clear();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");

    assertEquals(0, TaskDeleteListener.eventCounter);
    assertNull(TaskDeleteListener.lastTaskDefinitionKey);
    assertNull(TaskDeleteListener.lastDeleteReason);

    // correlate message to delete task
    Task task = taskService.createTaskQuery().singleResult();
    runtimeService.correlateMessage("message");

    assertEquals(1, TaskDeleteListener.eventCounter);
    assertEquals(task.getTaskDefinitionKey(), TaskDeleteListener.lastTaskDefinitionKey);
    assertEquals("deleted", TaskDeleteListener.lastDeleteReason);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskListenerWithExpression() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
    assertEquals(null, runtimeService.getVariable(processInstance.getId(), "greeting2"));

    // Completing first task will change the description
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertEquals("Write meeting notes", runtimeService.getVariable(processInstance.getId(), "greeting2"));
  }

  @Deployment
  public void testScriptListener() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "create"));

    taskService.setAssignee(task.getId(), "test");
    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "assignment"));

    taskService.complete(task.getId());
    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "complete"));

    task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");

    if (processEngineConfiguration.getHistoryLevel().getId() >= HISTORYLEVEL_AUDIT) {
      HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().variableName("delete").singleResult();
      assertNotNull(variable);
      assertTrue((Boolean) variable.getValue());
    }
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/examples/bpmn/tasklistener/TaskListenerTest.testScriptResourceListener.bpmn20.xml",
    "org/camunda/bpm/engine/test/examples/bpmn/tasklistener/taskListener.groovy"
  })
  public void testScriptResourceListener() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "create"));

    taskService.setAssignee(task.getId(), "test");
    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "assignment"));

    taskService.complete(task.getId());
    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "complete"));

    task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");

    if (processEngineConfiguration.getHistoryLevel().getId() >= HISTORYLEVEL_AUDIT) {
      HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().variableName("delete").singleResult();
      assertNotNull(variable);
      assertTrue((Boolean) variable.getValue());
    }
  }

}
