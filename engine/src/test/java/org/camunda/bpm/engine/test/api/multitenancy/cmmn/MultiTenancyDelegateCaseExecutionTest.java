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

package org.camunda.bpm.engine.test.api.multitenancy.cmmn;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.test.api.multitenancy.listener.AssertingCaseExecutionListener;
import org.camunda.bpm.engine.test.api.multitenancy.listener.AssertingCaseExecutionListener.DelegateCaseExecutionAsserter;

/**
 * Tests if a {@link DelegateCaseExecution} has the correct tenant-id.
 */
public class MultiTenancyDelegateCaseExecutionTest extends PluggableProcessEngineTestCase {

  protected static final String HUMAN_TASK_CMMN_FILE = "org/camunda/bpm/engine/test/api/multitenancy/HumanTaskCaseExecutionListener.cmmn";
  protected static final String CASE_TASK_CMMN_FILE = "org/camunda/bpm/engine/test/api/multitenancy/CaseTaskCaseExecutionListener.cmmn";
  protected static final String CMMN_FILE = "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCaseWithManualActivation.cmmn";

  protected static final String TENANT_ID = "tenant1";

  public void testSingleExecution() {
    deploymentForTenant(TENANT_ID, HUMAN_TASK_CMMN_FILE);

    AssertingCaseExecutionListener.addAsserts(hasTenantId("tenant1"));

    createCaseInstance("case");
  }

  public void testCallCaseTask() {
    deploymentForTenant(TENANT_ID, CMMN_FILE);
    deployment(CASE_TASK_CMMN_FILE);

    AssertingCaseExecutionListener.addAsserts(hasTenantId("tenant1"));

    createCaseInstance("oneCaseTaskCase");
  }

  protected void createCaseInstance(String caseDefinitionKey) {
    CaseDefinition caseDefinition = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(caseDefinitionKey)
        .latestVersion()
        .singleResult();

    caseService.createCaseInstanceById(caseDefinition.getId());
  }

  @Override
  protected void tearDown() throws Exception {
    AssertingCaseExecutionListener.clear();
    super.tearDown();
  }

  protected static DelegateCaseExecutionAsserter hasTenantId(final String expectedTenantId) {
    return new DelegateCaseExecutionAsserter() {

      @Override
      public void doAssert(DelegateCaseExecution execution) {
        assertThat(execution.getTenantId(), is(expectedTenantId));
      }
    };
  }

}
