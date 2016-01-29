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
package org.camunda.bpm.engine.test.api.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.test.api.delegate.AssertingJavaDelegate.DelegateExecutionAsserter;
import org.camunda.bpm.model.bpmn.Bpmn;

/**
 * Tests for the execution hierarchy methods exposed in delegate execution
 *
 * @author Daniel Meyer
 *
 */
public class DelegateExecutionHierarchyTest extends PluggableProcessEngineTestCase {

  @Override
  protected void tearDown() throws Exception {
    AssertingJavaDelegate.clear();
    super.tearDown();
  }

  public void testSingleNonScopeActivity() {

    deployment(Bpmn.createExecutableProcess("testProcess")
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

  public void testConcurrentServiceTasks() {

    deployment(Bpmn.createExecutableProcess("testProcess")
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

  public void testTaskInsideEmbeddedSubprocess() {
    deployment(Bpmn.createExecutableProcess("testProcess")
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

  public void testSubProcessInstance() {

    deployment(
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
