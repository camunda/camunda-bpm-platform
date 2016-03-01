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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyHistoricTaskInstanceQueryTest extends PluggableProcessEngineTestCase {

  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  @Override
  protected void setUp() {
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .userTask()
      .endEvent()
    .done();

    deploymentForTenant(TENANT_ONE, oneTaskProcess);
    deploymentForTenant(TENANT_TWO, oneTaskProcess);

    ProcessInstance processInstanceOne = startProcessInstanceForTenant(TENANT_ONE);
    ProcessInstance processInstanceTwo = startProcessInstanceForTenant(TENANT_TWO);

    completeUserTask(processInstanceOne);
    completeUserTask(processInstanceTwo);
  }

  public void testQueryWithoutTenantId() {
    HistoricTaskInstanceQuery query = historyService.
        createHistoricTaskInstanceQuery();

    assertThat(query.count(), is(2L));
  }

  public void testQueryByTenantId() {
    HistoricTaskInstanceQuery query = historyService
        .createHistoricTaskInstanceQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    query = historyService
        .createHistoricTaskInstanceQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIds() {
    HistoricTaskInstanceQuery query = historyService
        .createHistoricTaskInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryByNonExistingTenantId() {
    HistoricTaskInstanceQuery query = historyService
        .createHistoricTaskInstanceQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      historyService.createHistoricTaskInstanceQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
        .orderByTenantId()
        .asc()
        .list();

    assertThat(historicTaskInstances.size(), is(2));
    assertThat(historicTaskInstances.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(historicTaskInstances.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
        .orderByTenantId()
        .desc()
        .list();

    assertThat(historicTaskInstances.size(), is(2));
    assertThat(historicTaskInstances.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(historicTaskInstances.get(1).getTenantId(), is(TENANT_ONE));
  }

  protected ProcessInstance startProcessInstanceForTenant(String tenant) {
    return runtimeService.createProcessInstanceByKey("testProcess")
        .processDefinitionTenantId(tenant)
        .execute();
  }

  protected void completeUserTask(ProcessInstance processInstance) {
   Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
   assertThat(task, is(notNullValue()));
   taskService.complete(task.getId());
 }

}
