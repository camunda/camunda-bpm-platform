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
package org.camunda.bpm.engine.test.bpmn.usertask;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Event;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Daniel Meyer
 *
 */
public class UserTaskBpmnModelExecutionContextTest {

  private static final String PROCESS_ID = "process";
  private static final String USER_TASK_ID = "userTask";

  private RepositoryService repositoryService;
  private RuntimeService runtimeService;
  private TaskService taskService;

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testRule);

  @Before
  public void setup() {
    runtimeService = rule.getRuntimeService();
    repositoryService = rule.getRepositoryService();
    taskService = rule.getTaskService();
  }

  @After
  public void tearDown() {
    ModelExecutionContextTaskListener.clear();
  }

  @Test
  public void shouldGetBpmnModelElementInstanceOnCreate() {
    String eventName = TaskListener.EVENTNAME_CREATE;
    deployProcess(eventName);

    runtimeService.startProcessInstanceByKey(PROCESS_ID);

    assertModelInstance();
    assertUserTask(eventName);
  }

  @Test
  public void shouldGetBpmnModelElementInstanceOnAssignment() {
    String eventName = TaskListener.EVENTNAME_ASSIGNMENT;
    deployProcess(eventName);

    runtimeService.startProcessInstanceByKey(PROCESS_ID);

    assertNull(ModelExecutionContextTaskListener.modelInstance);
    assertNull(ModelExecutionContextTaskListener.userTask);

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.setAssignee(taskId, "demo");

    assertModelInstance();
    assertUserTask(eventName);
  }

  @Test
  public void shouldGetBpmnModelElementInstanceOnComplete() {
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

  @Test
  public void shouldGetBpmnModelElementInstanceOnUpdateAfterAssignment() {
    String eventName = TaskListener.EVENTNAME_UPDATE;
    deployProcess(eventName);

    runtimeService.startProcessInstanceByKey(PROCESS_ID);

    assertNull(ModelExecutionContextTaskListener.modelInstance);
    assertNull(ModelExecutionContextTaskListener.userTask);

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.setAssignee(taskId, "demo");

    assertNotNull(ModelExecutionContextTaskListener.modelInstance);
    assertNotNull(ModelExecutionContextTaskListener.userTask);

    taskService.complete(taskId);

    assertModelInstance();
    assertUserTask(eventName);
  }

  @Test
  @Deployment
  public void shouldGetBpmnModelElementInstanceOnTimeout() {
    runtimeService.startProcessInstanceByKey(PROCESS_ID);

    assertNull(ModelExecutionContextTaskListener.modelInstance);
    assertNull(ModelExecutionContextTaskListener.userTask);

    ClockUtil.offset(TimeUnit.MINUTES.toMillis(70L));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);

    assertModelInstance();
    assertUserTask(TaskListener.EVENTNAME_TIMEOUT);
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
        .camundaTaskListenerClass(eventName, ModelExecutionContextTaskListener.class)
      .endEvent()
      .done();

    rule.manageDeployment(repositoryService.createDeployment().addModelInstance("process.bpmn", modelInstance).deploy());
  }

}
