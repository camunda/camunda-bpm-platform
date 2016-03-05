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

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.api.delegate.AssertingTaskListener;
import org.camunda.bpm.engine.test.api.delegate.AssertingTaskListener.DelegateTaskAsserter;

/**
 * Tests if a {@link DelegateTask} has the correct tenant-id. The
 * assertions are checked inside the task listener.
 */
public class MultiTenancyDelegateTaskTest extends PluggableProcessEngineTestCase {

  protected static final String BPMN = "org/camunda/bpm/engine/test/api/multitenancy/taskListener.bpmn";

  public void testSingleExecutionWithUserTask() {
    deploymentForTenant("tenant1", BPMN);

    AssertingTaskListener.addAsserts(hasTenantId("tenant1"));

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceById(processDefinition.getId());
  }

  protected static DelegateTaskAsserter hasTenantId(final String expectedTenantId) {
    return new DelegateTaskAsserter() {

      @Override
      public void doAssert(DelegateTask task) {
        assertThat(task.getTenantId(), is(expectedTenantId));
      }
    };
  }

  @Override
  protected void tearDown() throws Exception {
    AssertingTaskListener.clear();
    super.tearDown();
  }

}
