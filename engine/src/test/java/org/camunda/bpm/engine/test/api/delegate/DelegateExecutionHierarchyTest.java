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
package org.camunda.bpm.engine.test.api.delegate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.test.api.delegate.AssertingJavaDelegate.DelegateExecutionAsserter;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.After;
import org.junit.Test;

/**
 * Tests for the execution hierarchy methods exposed in delegate execution
 *
 * @author Daniel Meyer
 *
 */
public class DelegateExecutionHierarchyTest extends PluggableProcessEngineTest {

  @After
  public void tearDown() throws Exception {
    AssertingJavaDelegate.clear();

  }

  @Test
  public void testSingleNonScopeActivity() {

   testRule.deploy(Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .serviceTask()
        .camundaClass(AssertingJavaDelegate.class.getName())
      .endEvent()
    .done());

    AssertingJavaDelegate.addAsserts(
      new DelegateExecutionAsserter() {
        public void doAssert(DelegateExecution execution) {
          assertEquals(execution, execution.getProcessInstance());
          assertNull(execution.getSuperExecution());
        }
      }
    );

    runtimeService.startProcessInstanceByKey("testProcess");

  }

  @Test
  public void testConcurrentServiceTasks() {

   testRule.deploy(Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .parallelGateway("fork")
        .serviceTask()
          .camundaClass(AssertingJavaDelegate.class.getName())
        .parallelGateway("join")
        .endEvent()
        .moveToNode("fork")
          .serviceTask()
          .camundaClass(AssertingJavaDelegate.class.getName())
          .connectTo("join")
          .done());

    AssertingJavaDelegate.addAsserts(
      new DelegateExecutionAsserter() {
        public void doAssert(DelegateExecution execution) {
          assertFalse(execution.equals(execution.getProcessInstance()));
          assertNull(execution.getSuperExecution());
        }
      }
    );

    runtimeService.startProcessInstanceByKey("testProcess");

  }

  @Test
  public void testTaskInsideEmbeddedSubprocess() {
   testRule.deploy(Bpmn.createExecutableProcess("testProcess")
        .startEvent()
        .subProcess()
          .embeddedSubProcess()
            .startEvent()
            .serviceTask()
              .camundaClass(AssertingJavaDelegate.class.getName())
            .endEvent()
        .subProcessDone()
        .endEvent()
      .done());

    AssertingJavaDelegate.addAsserts(
      new DelegateExecutionAsserter() {
        public void doAssert(DelegateExecution execution) {
          assertFalse(execution.equals(execution.getProcessInstance()));
          assertNull(execution.getSuperExecution());
        }
      }
    );

    runtimeService.startProcessInstanceByKey("testProcess");
  }

  @Test
  public void testSubProcessInstance() {

   testRule.deploy(
      Bpmn.createExecutableProcess("testProcess")
        .startEvent()
        .callActivity()
          .calledElement("testProcess2")
        .endEvent()
      .done(),
      Bpmn.createExecutableProcess("testProcess2")
        .startEvent()
        .serviceTask()
          .camundaClass(AssertingJavaDelegate.class.getName())
        .endEvent()
      .done());

    AssertingJavaDelegate.addAsserts(
      new DelegateExecutionAsserter() {
        public void doAssert(DelegateExecution execution) {
          assertTrue(execution.equals(execution.getProcessInstance()));
          assertNotNull(execution.getSuperExecution());
        }
      }
    );

    runtimeService.startProcessInstanceByKey("testProcess");
  }
}
