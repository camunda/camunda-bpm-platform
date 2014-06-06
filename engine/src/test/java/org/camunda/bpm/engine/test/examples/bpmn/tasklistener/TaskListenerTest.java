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

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;


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

}
