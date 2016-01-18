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

import java.util.List;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;

public class MultiTenancyDeploymentQueryTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant 1";
  protected static final String TENANT_TWO = "tenant 2";

  protected String deploymentOneId;
  protected String deploymentTwoId;

  @Override
  protected void setUp() throws Exception {
    deploymentOneId = repositoryService
        .createDeployment()
        .tenantId(TENANT_ONE)
        .addClasspathResource("org/camunda/bpm/engine/test/repository/one.bpmn20.xml")
        .deploy()
        .getId();

    deploymentTwoId = repositoryService
        .createDeployment()
        .tenantId(TENANT_TWO)
        .addClasspathResource("org/camunda/bpm/engine/test/repository/two.bpmn20.xml")
        .deploy()
        .getId();

    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    repositoryService.deleteDeployment(deploymentOneId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
  }

  public void testQueryWithoutTenantId() {
    DeploymentQuery query = repositoryService
        .createDeploymentQuery();

    assertEquals(2, query.count());
  }

  public void testQueryByTenantId() {
    DeploymentQuery query = repositoryService
        .createDeploymentQuery()
        .tenantId(TENANT_ONE);

    assertEquals(1, query.count());
  }

  public void testQueryByTenantIds() {
    DeploymentQuery query = repositoryService
        .createDeploymentQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);
    assertEquals(2, query.count());

    query = repositoryService
        .createDeploymentQuery()
        .tenantIdIn(TENANT_ONE);
    assertEquals(1, query.count());
  }

  public void testQuerySortingAsc() {
    List<Deployment> deployments = repositoryService.createDeploymentQuery()
        .orderByTenantId()
        .asc()
        .list();

    assertEquals(2, deployments.size());
    assertEquals(TENANT_ONE, deployments.get(0).getTenantId());
    assertEquals(TENANT_TWO, deployments.get(1).getTenantId());
  }

  public void testQuerySortingDesc() {
    List<Deployment> deployments = repositoryService.createDeploymentQuery()
        .orderByTenantId()
        .desc()
        .list();

    assertEquals(2, deployments.size());
    assertEquals(TENANT_TWO, deployments.get(0).getTenantId());
    assertEquals(TENANT_ONE, deployments.get(1).getTenantId());
  }

}
