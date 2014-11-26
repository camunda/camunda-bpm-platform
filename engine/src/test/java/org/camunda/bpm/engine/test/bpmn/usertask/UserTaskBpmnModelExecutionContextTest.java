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
package org.camunda.bpm.engine.test.bpmn.usertask;

import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.Collection;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

/**
 * @author Daniel Meyer
 *
 */
public class UserTaskBpmnModelExecutionContextTest extends PluggableProcessEngineTestCase {

  private static final String PROCESS_ID = "process";
  private static final String USER_TASK_ID = "userTask";
  private String deploymentId;

  public void testGetBpmnModelElementInstanceOnCreate() {
    String eventName = TaskListener.EVENTNAME_CREATE;
    deployProcess(eventName);

    runtimeService.startProcessInstanceByKey(PROCESS_ID);

    assertModelInstance();
    assertUserTask(eventName);

    String taskId = taskService.createTaskQuery().active().singleResult().getId();
    taskService.complete(taskId);
  }

  public void testGetBpmnModelElementInstanceOnAssignment() {
    String eventName = TaskListener.EVENTNAME_ASSIGNMENT;
    deployProcess(eventName);

    runtimeService.startProcessInstanceByKey(PROCESS_ID);

    assertNull(ModelExecutionContextTaskListener.modelInstance);
    assertNull(ModelExecutionContextTaskListener.userTask);

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.setAssignee(taskId, "demo");

    assertModelInstance();
    assertUserTask(eventName);

    taskService.complete(taskId);
  }

  public void testGetBpmnModelElementInstanceOnComplete() {
    String eventName = TaskListener.EVENTNAME_COMPLETE;
    deployProcess(eventName);

    runtimeService.startProcessInstanceByKey(PROCESS_ID);

    assertNull(ModelExecutionContextTaskListener.modelInstance);
    assertNull(ModelExecutionContextTaskListener.userTask);

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.setAssignee(taskId, "demo");

    assertNull(ModelExecutionContextTaskListener.modelInstance);
    assertNull(ModelExecutionContextTaskListener.userTask);

    taskService.complete(taskId);

    assertModelInstance();
    assertUserTask(eventName);
  }

  private void assertModelInstance() {
    BpmnModelInstance modelInstance = ModelExecutionContextTaskListener.modelInstance;
    assertNotNull(modelInstance);

    Collection<ModelElementInstance> events = modelInstance.getModelElementsByType(modelInstance.getModel().getType(Event.class));
    assertEquals(2, events.size());

    Collection<ModelElementInstance> tasks = modelInstance.getModelElementsByType(modelInstance.getModel().getType(Task.class));
    assertEquals(1, tasks.size());

    Process process = (Process) modelInstance.getDefinitions().getRootElements().iterator().next();
    assertEquals(PROCESS_ID, process.getId());
    assertTrue(process.isExecutable());
  }

  private void assertUserTask(String eventName) {
    UserTask userTask = ModelExecutionContextTaskListener.userTask;
    assertNotNull(userTask);

    ModelElementInstance taskListener = userTask.getExtensionElements().getUniqueChildElementByNameNs(CAMUNDA_NS, "taskListener");
    assertEquals(eventName, taskListener.getAttributeValueNs(CAMUNDA_NS, "event"));
    assertEquals(ModelExecutionContextTaskListener.class.getName(), taskListener.getAttributeValueNs(CAMUNDA_NS, "class"));

    BpmnModelInstance modelInstance = ModelExecutionContextTaskListener.modelInstance;
    Collection<ModelElementInstance> tasks = modelInstance.getModelElementsByType(modelInstance.getModel().getType(Task.class));
    assertTrue(tasks.contains(userTask));
  }

  private void deployProcess(String eventName) {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
      .startEvent()
      .userTask(USER_TASK_ID)
      .endEvent()
      .done();

    ExtensionElements extensionElements = modelInstance.newInstance(ExtensionElements.class);
    ModelElementInstance taskListener = extensionElements.addExtensionElement(CAMUNDA_NS, "taskListener");
    taskListener.setAttributeValueNs(CAMUNDA_NS, "class", ModelExecutionContextTaskListener.class.getName());
    taskListener.setAttributeValueNs(CAMUNDA_NS, "event", eventName);

    UserTask userTask = modelInstance.getModelElementById(USER_TASK_ID);
    userTask.setExtensionElements(extensionElements);

    deploymentId = repositoryService.createDeployment().addModelInstance("process.bpmn", modelInstance).deploy().getId();
  }

  public void tearDown() {
    ModelExecutionContextTaskListener.clear();
    repositoryService.deleteDeployment(deploymentId, true);
  }
}
