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

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyProcessDefinitionQueryTest extends PluggableProcessEngineTestCase {

  protected static final String PROCESS_DEFINITION_KEY = "process";
  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  @Override
  protected void setUp() {
    deployProcessDefinitionForTenant(TENANT_ONE);
    deployProcessDefinitionForTenant(TENANT_TWO);
  }

  protected void deployProcessDefinitionForTenant(String tenantId) {
    BpmnModelInstance emptyProcess = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).done();

    deploymentForTenant(tenantId, emptyProcess);
  }

  public void testQueryWithoutTenantId() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery();

    assertThat(query.count(), is(2L));
  }

  public void testQueryByTenantId() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    query = repositoryService.
        createProcessDefinitionQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIds() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryByKey() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY);
    // one definition for each tenant
    assertThat(query.count(), is(2L));

    query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));
  }

  public void testQueryByLatestWithoutTenantId() {
    // deploy a second version for tenant one
    deployProcessDefinitionForTenant(TENANT_ONE);

    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .latestVersion()
        .orderByTenantId()
        .asc();
    // one definition for each tenant
    assertThat(query.count(), is(2L));

    List<ProcessDefinition> processDefinitions = query.list();
    assertThat(processDefinitions.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(processDefinitions.get(0).getVersion(), is(2));
    assertThat(processDefinitions.get(1).getTenantId(), is(TENANT_TWO));
    assertThat(processDefinitions.get(1).getVersion(), is(1));
  }

  public void testQueryByLatestWithTenantId() {
    // deploy a second version for tenant one
    deployProcessDefinitionForTenant(TENANT_ONE);

    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    ProcessDefinition processDefinition = query.singleResult();
    assertThat(processDefinition.getTenantId(), is(TENANT_ONE));
    assertThat(processDefinition.getVersion(), is(2));

    query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));

    processDefinition = query.singleResult();
    assertThat(processDefinition.getTenantId(), is(TENANT_TWO));
    assertThat(processDefinition.getVersion(), is(1));
  }

  public void testQueryByLatestWithTenantIds() {
    // deploy a second version for tenant one
    deployProcessDefinitionForTenant(TENANT_ONE);

    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc();
    // one definition for each tenant
    assertThat(query.count(), is(2L));

    List<ProcessDefinition> processDefinitions = query.list();
    assertThat(processDefinitions.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(processDefinitions.get(0).getVersion(), is(2));
    assertThat(processDefinitions.get(1).getTenantId(), is(TENANT_TWO));
    assertThat(processDefinitions.get(1).getVersion(), is(1));
  }

  public void testQueryByNonExistingTenantId() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      repositoryService.createProcessDefinitionQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    List<ProcessDefinition> processDefinitions = repositoryService
        .createProcessDefinitionQuery()
        .orderByTenantId()
        .asc()
        .list();

    assertThat(processDefinitions.size(), is(2));
    assertThat(processDefinitions.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(processDefinitions.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    List<ProcessDefinition> processDefinitions = repositoryService
        .createProcessDefinitionQuery()
        .orderByTenantId()
        .desc()
        .list();

    assertThat(processDefinitions.size(), is(2));
    assertThat(processDefinitions.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(processDefinitions.get(1).getTenantId(), is(TENANT_ONE));
  }

}
