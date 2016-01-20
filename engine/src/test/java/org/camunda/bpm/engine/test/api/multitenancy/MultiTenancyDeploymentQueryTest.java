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

import java.util.List;

import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;

public class MultiTenancyDeploymentQueryTest extends AbstractMultiTenancyQueryTest {

  @Override
  protected void initScenario() {
    // two deployments for different tenant ids
  }

  public void testQueryWithoutTenantId() {
    DeploymentQuery query = repositoryService
        .createDeploymentQuery();

   verifyQueryResults(query, 2);
  }

  public void testQueryByTenantId() {
    DeploymentQuery query = repositoryService
        .createDeploymentQuery()
        .tenantIdIn(TENANT_ONE);

    verifyQueryResults(query, 1);

    query = repositoryService
        .createDeploymentQuery()
        .tenantIdIn(TENANT_TWO);

    verifyQueryResults(query, 1);
  }

  public void testQueryByTenantIds() {
    DeploymentQuery query = repositoryService
        .createDeploymentQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    verifyQueryResults(query, 2);
  }

  public void testQuerySortingAsc() {
    List<Deployment> deployments = repositoryService.createDeploymentQuery()
        .orderByTenantId()
        .asc()
        .list();

    assertThat(deployments.size(), is(2));
    assertThat(deployments.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(deployments.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    List<Deployment> deployments = repositoryService.createDeploymentQuery()
        .orderByTenantId()
        .desc()
        .list();

    assertThat(deployments.size(), is(2));
    assertThat(deployments.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(deployments.get(1).getTenantId(), is(TENANT_ONE));
  }

}
