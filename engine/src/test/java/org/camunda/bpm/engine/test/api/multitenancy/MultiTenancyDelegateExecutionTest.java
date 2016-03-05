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

package org.camunda.bpm.engine.test.api.multitenancy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.api.delegate.AssertingJavaDelegate;
import org.camunda.bpm.engine.test.api.delegate.AssertingJavaDelegate.DelegateExecutionAsserter;
import org.camunda.bpm.model.bpmn.Bpmn;

/**
 * Tests if a {@link DelegateExecution} has the correct tenant-id. The
 * assertions are checked inside the service tasks.
 */
public class MultiTenancyDelegateExecutionTest extends PluggableProcessEngineTestCase {

  protected static final String PROCESS_DEFINITION_KEY = "testProcess";

  public void testSingleExecution() {
    deploymentForTenant("tenant1", Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent()
      .serviceTask()
        .camundaClass(AssertingJavaDelegate.class.getName())
      .endEvent()
    .done());

    AssertingJavaDelegate.addAsserts(hasTenantId("tenant1"));

    startProcessInstance(PROCESS_DEFINITION_KEY);
  }

  public void testConcurrentExecution() {

    deploymentForTenant("tenant1", Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
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

    AssertingJavaDelegate.addAsserts(hasTenantId("tenant1"));

    startProcessInstance(PROCESS_DEFINITION_KEY);
  }

  public void testEmbeddedSubprocess() {
    deploymentForTenant("tenant1", Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
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

    AssertingJavaDelegate.addAsserts(hasTenantId("tenant1"));

    startProcessInstance(PROCESS_DEFINITION_KEY);
  }

  protected void startProcessInstance(String processDefinitionKey) {
    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .latestVersion()
        .singleResult();

    runtimeService.startProcessInstanceById(processDefinition.getId());
  }

  @Override
  protected void tearDown() throws Exception {
    AssertingJavaDelegate.clear();
    super.tearDown();
  }

  protected static DelegateExecutionAsserter hasTenantId(final String expectedTenantId) {
    return new DelegateExecutionAsserter() {

      @Override
      public void doAssert(DelegateExecution execution) {
        assertThat(execution.getTenantId(), is(expectedTenantId));
      }
    };
  }

}
