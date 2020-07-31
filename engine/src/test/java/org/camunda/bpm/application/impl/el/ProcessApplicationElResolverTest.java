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
package org.camunda.bpm.application.impl.el;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessApplicationElResolverTest extends PluggableProcessEngineTest {

  RuntimeContainerDelegate runtimeContainerDelegate = null;

  CallingProcessApplication callingApp;
  CalledProcessApplication calledApp;

  @Before
  public void setUp() throws Exception {
    runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get();
    runtimeContainerDelegate.registerProcessEngine(processEngine);

    callingApp = new CallingProcessApplication();
    calledApp = new CalledProcessApplication();

    callingApp.deploy();
    calledApp.deploy();
  }

  @After
  public void tearDown() {

    callingApp.undeploy();
    calledApp.undeploy();

    if (runtimeContainerDelegate != null) {
      runtimeContainerDelegate.unregisterProcessEngine(processEngine);
    }
  }

  /**
   * Tests that an expression for a call activity output parameter is resolved
   * in the context of the called process definition's application.
   */
  @Test
  public void testCallActivityOutputExpression() {
    // given an instance of the calling process that calls the called process
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("callingProcess");

    // when the called process is completed
    Task calledProcessTask = taskService.createTaskQuery().singleResult();
    taskService.complete(calledProcessTask.getId());

    // then the output mapping should have successfully resolved the expression
    String outVariable = (String) runtimeService.getVariable(instance.getId(), "outVar");
    assertEquals(CalledProcessApplication.STRING_VARIABLE_VALUE, outVariable);
  }

  /**
   * Tests that an expression on an outgoing flow leaving a call activity
   * is resolved in the context of the calling process definition's application.
   */
  @Test
  public void testCallActivityConditionalOutgoingFlow() {
    // given an instance of the calling process that calls the called process
    runtimeService.startProcessInstanceByKey("callingProcessConditionalFlow");

    // when the called process is completed
    Task calledProcessTask = taskService.createTaskQuery().singleResult();
    taskService.complete(calledProcessTask.getId());

    // then the conditional flow expression was resolved in the context of the calling process application, so
    // the following task has been reached successfully
    Task afterCallActivityTask = taskService.createTaskQuery().singleResult();
    assertNotNull(afterCallActivityTask);
    assertEquals("afterCallActivityTask", afterCallActivityTask.getTaskDefinitionKey());

  }
}
