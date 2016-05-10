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
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyJobQueryTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  @Override
  protected void setUp() {
    BpmnModelInstance asyncTaskProcess = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .userTask()
        .camundaAsyncBefore()
      .endEvent()
    .done();

    deployment(asyncTaskProcess);
    deploymentForTenant(TENANT_ONE, asyncTaskProcess);
    deploymentForTenant(TENANT_TWO, asyncTaskProcess);

    runtimeService.createProcessInstanceByKey("testProcess").processDefinitionWithoutTenantId().execute();
    runtimeService.createProcessInstanceByKey("testProcess").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("testProcess").processDefinitionTenantId(TENANT_TWO).execute();
  }

  public void testQueryNoTenantIdSet() {
    JobQuery query = managementService
        .createJobQuery();

    assertThat(query.count(), is(3L));
  }

  public void testQueryByTenantId() {
    JobQuery query = managementService
        .createJobQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    query = managementService
        .createJobQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIds() {
    JobQuery query = managementService
        .createJobQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryByJobsWithoutTenantId() {
    JobQuery query = managementService
        .createJobQuery()
        .withoutTenantId();

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIdsIncludeJobsWithoutTenantId() {
    JobQuery query = managementService
        .createJobQuery()
        .tenantIdIn(TENANT_ONE)
        .includeJobsWithoutTenantId();

    assertThat(query.count(), is(2L));

    query = managementService
        .createJobQuery()
        .tenantIdIn(TENANT_TWO)
        .includeJobsWithoutTenantId();

    assertThat(query.count(), is(2L));

    query = managementService
        .createJobQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeJobsWithoutTenantId();

    assertThat(query.count(), is(3L));
  }

  public void testQueryByNonExistingTenantId() {
    JobQuery query = managementService
        .createJobQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      managementService.createJobQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    // exclude jobs without tenant id because of database-specific ordering
    List<Job> jobs = managementService.createJobQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(jobs.size(), is(2));
    assertThat(jobs.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(jobs.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    // exclude jobs without tenant id because of database-specific ordering
    List<Job> jobs = managementService.createJobQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(jobs.size(), is(2));
    assertThat(jobs.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(jobs.get(1).getTenantId(), is(TENANT_ONE));
  }

  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    JobQuery query = managementService.createJobQuery();
    assertThat(query.count(), is(1L));
  }

  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    JobQuery query = managementService.createJobQuery();

    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).includeJobsWithoutTenantId().count(), is(2L));
  }

  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    JobQuery query = managementService.createJobQuery();

    assertThat(query.count(), is(3L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
    assertThat(query.withoutTenantId().count(), is(1L));
  }

  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    JobQuery query = managementService.createJobQuery();
    assertThat(query.count(), is(3L));
  }

}
