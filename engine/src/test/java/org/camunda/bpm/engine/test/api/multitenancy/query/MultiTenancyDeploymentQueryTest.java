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

package org.camunda.bpm.engine.test.api.multitenancy.query;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyDeploymentQueryTest extends PluggableProcessEngineTestCase {

  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  @Override
  public void setUp() throws Exception {
    BpmnModelInstance emptyProcess = Bpmn.createExecutableProcess().done();

    deployment(emptyProcess);
    deploymentForTenant(TENANT_ONE, emptyProcess);
    deploymentForTenant(TENANT_TWO, emptyProcess);
  }

  public void testQueryNoTenantIdSet() {
    DeploymentQuery query = repositoryService
        .createDeploymentQuery();

   assertThat(query.count(), is(3L));
  }

  public void testQueryByTenantId() {
    DeploymentQuery query = repositoryService
        .createDeploymentQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    query = repositoryService
        .createDeploymentQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIds() {
    DeploymentQuery query = repositoryService
        .createDeploymentQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryWithoutTenantId() {
    DeploymentQuery query = repositoryService
        .createDeploymentQuery()
        .withoutTenantId();

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIdsIncludeDeploymentsWithoutTenantId() {
    DeploymentQuery query = repositoryService
        .createDeploymentQuery()
        .tenantIdIn(TENANT_ONE)
        .includeDeploymentsWithoutTenantId();

    assertThat(query.count(), is(2L));

    query = repositoryService
        .createDeploymentQuery()
        .tenantIdIn(TENANT_TWO)
        .includeDeploymentsWithoutTenantId();

    assertThat(query.count(), is(2L));

    query = repositoryService
        .createDeploymentQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeDeploymentsWithoutTenantId();

    assertThat(query.count(), is(3L));
  }

  public void testQueryByNonExistingTenantId() {
    DeploymentQuery query = repositoryService
        .createDeploymentQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      repositoryService.createDeploymentQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    // exclude deployments without tenant id because of database-specific ordering
    List<Deployment> deployments = repositoryService.createDeploymentQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(deployments.size(), is(2));
    assertThat(deployments.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(deployments.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    // exclude deployments without tenant id because of database-specific ordering
    List<Deployment> deployments = repositoryService.createDeploymentQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(deployments.size(), is(2));
    assertThat(deployments.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(deployments.get(1).getTenantId(), is(TENANT_ONE));
  }

  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    DeploymentQuery query = repositoryService.createDeploymentQuery();
    assertThat(query.count(), is(1L));
  }

  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    DeploymentQuery query = repositoryService.createDeploymentQuery();

    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).includeDeploymentsWithoutTenantId().count(), is(2L));
  }

  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    DeploymentQuery query = repositoryService.createDeploymentQuery();

    assertThat(query.count(), is(3L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
    assertThat(query.withoutTenantId().count(), is(1L));
  }

  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    DeploymentQuery query = repositoryService.createDeploymentQuery();
    assertThat(query.count(), is(3L));
  }


}
