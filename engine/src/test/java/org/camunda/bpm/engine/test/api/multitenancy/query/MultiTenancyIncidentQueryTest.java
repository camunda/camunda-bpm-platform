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

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.IncidentQuery;
import org.camunda.bpm.engine.runtime.Job;

public class MultiTenancyIncidentQueryTest extends PluggableProcessEngineTestCase {

  protected static final String BPMN = "org/camunda/bpm/engine/test/api/multitenancy/failingTask.bpmn";

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  @Override
  protected void setUp() {
    deployment(repositoryService.createDeployment()
        .tenantId(TENANT_ONE)
        .addClasspathResource(BPMN));

    deployment(repositoryService.createDeployment()
        .tenantId(TENANT_TWO)
        .addClasspathResource(BPMN));

    startProcessInstanceAndExecuteFailingJobForTenant(TENANT_ONE);
    startProcessInstanceAndExecuteFailingJobForTenant(TENANT_TWO);
  }

  public void testQueryWithoutTenantId() {
    IncidentQuery query = runtimeService
        .createIncidentQuery();

    assertThat(query.count(), is(2L));
  }

  public void testQueryByTenantId() {
    IncidentQuery query = runtimeService
        .createIncidentQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    query = runtimeService
        .createIncidentQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIds() {
    IncidentQuery query = runtimeService
        .createIncidentQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryByNonExistingTenantId() {
    IncidentQuery query = runtimeService
        .createIncidentQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      runtimeService.createIncidentQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    List<Incident> incidents = runtimeService.createIncidentQuery()
        .orderByTenantId()
        .asc()
        .list();

    assertThat(incidents.size(), is(2));
    assertThat(incidents.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(incidents.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    List<Incident> incidents = runtimeService.createIncidentQuery()
        .orderByTenantId()
        .desc()
        .list();

    assertThat(incidents.size(), is(2));
    assertThat(incidents.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(incidents.get(1).getTenantId(), is(TENANT_ONE));
  }

  protected void startProcessInstanceAndExecuteFailingJobForTenant(String tenant) {
    String processDefinitionId = repositoryService
      .createProcessDefinitionQuery()
      .tenantIdIn(tenant)
      .singleResult()
      .getId();

    runtimeService.startProcessInstanceById(processDefinitionId);

    // execute the job of the async activity
    Job job = managementService.createJobQuery().processDefinitionId(processDefinitionId).singleResult();
    try {
      managementService.executeJob(job.getId());
    } catch (ProcessEngineException e) {
      // the job failed and created an incident
    }
  }

}
