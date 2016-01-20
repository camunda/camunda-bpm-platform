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

import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;

public class MultiTenancyExecutionQueryTest extends AbstractMultiTenancyQueryTest {

  @Override
  protected void initScenario() {
    startProcessInstanceForTenant(TENANT_ONE);
    startProcessInstanceForTenant(TENANT_TWO);
  }

  public void testQueryWithoutTenantId() {
    ExecutionQuery query = runtimeService.
        createExecutionQuery();

    verifyQueryResults(query, 2);
  }

  public void testQueryByTenantId() {
    ExecutionQuery query = runtimeService
        .createExecutionQuery()
        .tenantIdIn(TENANT_ONE);

    verifyQueryResults(query, 1);

    query = runtimeService
        .createExecutionQuery()
        .tenantIdIn(TENANT_TWO);

    verifyQueryResults(query, 1);
  }

  public void testQueryByTenantIds() {
    ExecutionQuery query = runtimeService
        .createExecutionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    verifyQueryResults(query, 2);
  }

  public void testQuerySortingAsc() {
    List<Execution> executions = runtimeService.createExecutionQuery()
        .orderByTenantId()
        .asc()
        .list();

    assertThat(executions.size(), is(2));
    assertThat(executions.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(executions.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    List<Execution> executions = runtimeService.createExecutionQuery()
        .orderByTenantId()
        .desc()
        .list();

    assertThat(executions.size(), is(2));
    assertThat(executions.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(executions.get(1).getTenantId(), is(TENANT_ONE));
  }

  protected void startProcessInstanceForTenant(String tenant) {
    String processDefinitionId = repositoryService
      .createProcessDefinitionQuery()
      .tenantIdIn(tenant)
      .singleResult()
      .getId();

    runtimeService.startProcessInstanceById(processDefinitionId);
  }

}
