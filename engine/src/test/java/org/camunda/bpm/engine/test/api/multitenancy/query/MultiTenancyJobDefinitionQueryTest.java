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

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.JobDefinitionQuery;

public class MultiTenancyJobDefinitionQueryTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String BPMN = "org/camunda/bpm/engine/test/api/multitenancy/timerStartEvent.bpmn";

  @Override
  protected void setUp() {
    deployment(repositoryService.createDeployment()
        .tenantId(TENANT_ONE)
        .addClasspathResource(BPMN));

    deployment(repositoryService.createDeployment()
        .tenantId(TENANT_TWO)
        .addClasspathResource(BPMN));

    // the deployed process definition contains a timer start event
    // - so a job definition is created on deployment.
  }

  public void testQueryWithoutTenantId() {
    JobDefinitionQuery query = managementService
        .createJobDefinitionQuery();

    assertThat(query.count(), is(2L));
  }

  public void testQueryByTenantId() {
    JobDefinitionQuery query = managementService
        .createJobDefinitionQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    query = managementService
        .createJobDefinitionQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIds() {
    JobDefinitionQuery query = managementService
        .createJobDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryByNonExistingTenantId() {
    JobDefinitionQuery query = managementService
        .createJobDefinitionQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      managementService.createJobDefinitionQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    List<JobDefinition> jobDefinitions = managementService.createJobDefinitionQuery()
        .orderByTenantId()
        .asc()
        .list();

    assertThat(jobDefinitions.size(), is(2));
    assertThat(jobDefinitions.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(jobDefinitions.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    List<JobDefinition> jobDefinitions = managementService.createJobDefinitionQuery()
        .orderByTenantId()
        .desc()
        .list();

    assertThat(jobDefinitions.size(), is(2));
    assertThat(jobDefinitions.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(jobDefinitions.get(1).getTenantId(), is(TENANT_ONE));
  }

}
