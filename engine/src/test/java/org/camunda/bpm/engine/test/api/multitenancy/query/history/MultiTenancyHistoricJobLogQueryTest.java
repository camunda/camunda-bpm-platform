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

package org.camunda.bpm.engine.test.api.multitenancy.query.history;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricJobLogQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;

public class MultiTenancyHistoricJobLogQueryTest extends PluggableProcessEngineTestCase {

  protected static final String BPMN = "org/camunda/bpm/engine/test/api/multitenancy/failingTask.bpmn";

  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  @Override
  protected void setUp() {
    deploymentForTenant(TENANT_ONE, BPMN);
    deploymentForTenant(TENANT_TWO, BPMN);

    startProcessInstanceAndExecuteFailingJobForTenant(TENANT_ONE);
    startProcessInstanceAndExecuteFailingJobForTenant(TENANT_TWO);
  }

  public void testQueryWithoutTenantId() {
    HistoricJobLogQuery query = historyService.
        createHistoricJobLogQuery();

    assertThat(query.count(), is(4L));
  }

  public void testQueryByTenantId() {
    HistoricJobLogQuery query = historyService
        .createHistoricJobLogQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(2L));

    query = historyService
        .createHistoricJobLogQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryByTenantIds() {
    HistoricJobLogQuery query = historyService
        .createHistoricJobLogQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(4L));
  }

  public void testQueryByNonExistingTenantId() {
    HistoricJobLogQuery query = historyService
        .createHistoricJobLogQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      historyService.createHistoricJobLogQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    List<HistoricJobLog> historicJobLogs = historyService.createHistoricJobLogQuery()
        .orderByTenantId()
        .asc()
        .list();

    assertThat(historicJobLogs.size(), is(4));
    assertThat(historicJobLogs.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(historicJobLogs.get(1).getTenantId(), is(TENANT_ONE));
    assertThat(historicJobLogs.get(2).getTenantId(), is(TENANT_TWO));
    assertThat(historicJobLogs.get(3).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    List<HistoricJobLog> historicJobLogs = historyService.createHistoricJobLogQuery()
        .orderByTenantId()
        .desc()
        .list();

    assertThat(historicJobLogs.size(), is(4));
    assertThat(historicJobLogs.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(historicJobLogs.get(1).getTenantId(), is(TENANT_TWO));
    assertThat(historicJobLogs.get(2).getTenantId(), is(TENANT_ONE));
    assertThat(historicJobLogs.get(3).getTenantId(), is(TENANT_ONE));
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
