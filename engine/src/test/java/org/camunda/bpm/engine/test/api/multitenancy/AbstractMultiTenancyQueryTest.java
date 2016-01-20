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

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.query.Query;

/**
 * Provide a basic setup for tests which verifies a query in a multi-tenancy scenario.
 *
 * @author Philipp Ossler
 *
 */
public abstract class AbstractMultiTenancyQueryTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant 1";
  protected static final String TENANT_TWO = "tenant 2";

  protected String deploymentOneId;
  protected String deploymentTwoId;

  @Override
  protected void setUp() throws Exception {
    deploymentOneId = deployProcessDefinitionForTenant(TENANT_ONE);
    deploymentTwoId = deployProcessDefinitionForTenant(TENANT_TWO);

    initScenario();

    super.setUp();
  }

  protected String deployProcessDefinitionForTenant(String tenantId) {
    return repositoryService
        .createDeployment()
        .tenantId(tenantId)
        .addClasspathResource("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
        .deploy()
        .getId();
  }

  protected abstract void initScenario();

  @Override
  protected void tearDown() throws Exception {
    repositoryService.deleteDeployment(deploymentOneId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
  }

  protected void verifyQueryResults(Query<?,?> query, int countExpected) {
    assertThat(query.list().size(), is(countExpected));
    assertThat(query.count(), is(Long.valueOf(countExpected)));
  }

}
