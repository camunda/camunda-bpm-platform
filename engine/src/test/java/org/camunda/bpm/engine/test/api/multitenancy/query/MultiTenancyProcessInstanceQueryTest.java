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
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyProcessInstanceQueryTest extends PluggableProcessEngineTestCase {

  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  @Override
  protected void setUp() {
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .userTask()
      .endEvent()
    .done();

    deployment(oneTaskProcess);
    deploymentForTenant(TENANT_ONE, oneTaskProcess);
    deploymentForTenant(TENANT_TWO, oneTaskProcess);

    runtimeService.createProcessInstanceByKey("testProcess").processDefinitionWithoutTenantId().execute();
    runtimeService.createProcessInstanceByKey("testProcess").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("testProcess").processDefinitionTenantId(TENANT_TWO).execute();
  }

  public void testQueryNoTenantIdSet() {
    ProcessInstanceQuery query = runtimeService.
        createProcessInstanceQuery();

    assertThat(query.count(), is(3L));
  }

  public void testQueryByTenantId() {
    ProcessInstanceQuery query = runtimeService
        .createProcessInstanceQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    query = runtimeService
        .createProcessInstanceQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIds() {
    ProcessInstanceQuery query = runtimeService
        .createProcessInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryByInstancesWithoutTenantId() {
    ProcessInstanceQuery query = runtimeService
        .createProcessInstanceQuery()
        .withoutTenantId();

    assertThat(query.count(), is(1L));
  }

  public void testQueryByNonExistingTenantId() {
    ProcessInstanceQuery query = runtimeService
        .createProcessInstanceQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      runtimeService.createProcessInstanceQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    // exclude instances without tenant id because of database-specific ordering
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(processInstances.size(), is(2));
    assertThat(processInstances.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(processInstances.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    // exclude instances without tenant id because of database-specific ordering
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(processInstances.size(), is(2));
    assertThat(processInstances.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(processInstances.get(1).getTenantId(), is(TENANT_ONE));
  }

  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.count(), is(1L));
  }

  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count(), is(1L));
  }

  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    assertThat(query.count(), is(3L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
    assertThat(query.withoutTenantId().count(), is(1L));
  }

  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.count(), is(3L));
  }

}
