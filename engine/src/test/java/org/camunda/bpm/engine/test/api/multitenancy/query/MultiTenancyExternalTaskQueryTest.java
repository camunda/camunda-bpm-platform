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
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyExternalTaskQueryTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  @Override
  protected void setUp() {
    BpmnModelInstance process = Bpmn.createExecutableProcess()
      .startEvent()
      .serviceTask()
        .camundaType("external")
        .camundaTopic("test")
      .endEvent()
    .done();

    deploymentForTenant(TENANT_ONE, process);
    deploymentForTenant(TENANT_TWO, process);

    startProcessInstance(TENANT_ONE);
    startProcessInstance(TENANT_TWO);
  }

  public void testQueryWithoutTenantId() {
    ExternalTaskQuery query = externalTaskService
        .createExternalTaskQuery();

    assertThat(query.count(), is(2L));
  }

  public void testQueryByTenantId() {
    ExternalTaskQuery query = externalTaskService
        .createExternalTaskQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    query = externalTaskService
        .createExternalTaskQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIds() {
    ExternalTaskQuery query = externalTaskService
        .createExternalTaskQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryByNonExistingTenantId() {
    ExternalTaskQuery query = externalTaskService
        .createExternalTaskQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      externalTaskService.createExternalTaskQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    List<ExternalTask> externalTasks = externalTaskService.createExternalTaskQuery()
        .orderByTenantId()
        .asc()
        .list();

    assertThat(externalTasks.size(), is(2));
    assertThat(externalTasks.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(externalTasks.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    List<ExternalTask> externalTasks = externalTaskService.createExternalTaskQuery()
        .orderByTenantId()
        .desc()
        .list();

    assertThat(externalTasks.size(), is(2));
    assertThat(externalTasks.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(externalTasks.get(1).getTenantId(), is(TENANT_ONE));
  }

  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();
    assertThat(query.count(), is(0L));
  }

  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();

    assertThat(query.count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count(), is(1L));
  }

  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();

    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();
    assertThat(query.count(), is(2L));
  }

  protected void startProcessInstance(String tenant) {
    String processDefinitionId = repositoryService
      .createProcessDefinitionQuery()
      .tenantIdIn(tenant)
      .singleResult()
      .getId();

    runtimeService.startProcessInstanceById(processDefinitionId);
  }

}
