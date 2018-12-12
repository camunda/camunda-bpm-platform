/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.test.bpmn.event.error;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class UnhandledBpmnErrorTest {

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      configuration.setEnableExceptionsAfterUnhandledBpmnError(true);
      return configuration;
    }
  };

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private RuntimeService runtimeService;
  private TaskService taskService;

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
  }

  @Test
  public void testThrownInJavaDelegate() {
    // expect
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage(containsString("no error handler"));

    // given
    BpmnModelInstance instance = Bpmn.createExecutableProcess("process")
      .startEvent()
      .serviceTask().camundaClass(ThrowBpmnErrorDelegate.class)
      .endEvent().done();
    testRule.deploy(instance);

    // when
    runtimeService.startProcessInstanceByKey("process");
  }

  @Test
  @Deployment
  public void testUncaughtErrorSimpleProcess() {
    // expect
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage(containsString("no error handler"));

    // given simple process definition

    // when
    runtimeService.startProcessInstanceByKey("process");
  }

  @Test
  @Deployment
  public void testUnhandledErrorInEmbeddedSubprocess() {
    // expect
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage(containsString("no error handler"));

    // given
    runtimeService.startProcessInstanceByKey("boundaryErrorOnEmbeddedSubprocess");

    // assume
    // After process start, usertask in subprocess should exist
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("subprocessTask", task.getName());

    // when
    // After task completion, error end event is reached which is never caught in the process
    taskService.complete(task.getId());
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/error/UnhandledBpmnErrorTest.testUncaughtErrorOnCallActivity.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/error/UnhandledBpmnErrorTest.subprocess.bpmn20.xml" })
  public void testUncaughtErrorOnCallActivity() {
    // expect
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage(containsString("no error handler"));

    // given
    runtimeService.startProcessInstanceByKey("uncaughtErrorOnCallActivity");

    // assume
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Task in subprocess", task.getName());

    // when
    // Completing the task will reach the end error event,
    // which is never caught in the process
    taskService.complete(task.getId());
  }

  @Test
  @Deployment
  public void testUncaughtErrorOnEventSubprocess() {
    // expect
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage(containsString("no error handler"));

    // given
    runtimeService.startProcessInstanceByKey("process").getId();

    // assume
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("subprocessTask", task.getName());

    // when
    // After task completion, error end event is reached which is never caught in the process
    taskService.complete(task.getId());
  }
}