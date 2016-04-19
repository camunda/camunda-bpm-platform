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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class MultiTenancyHistoricDetailVariableUpdateQueryTest extends PluggableProcessEngineTestCase {

  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  protected final static String VARIABLE_NAME = "myVar";
  protected final static String TENANT_ONE_VAR = "tenant1Var";
  protected final static String TENANT_TWO_VAR = "tenant2Var";

  @Override
  protected void setUp() {
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .userTask()
      .endEvent()
    .done();

    deploymentForTenant(TENANT_ONE, oneTaskProcess);
    deploymentForTenant(TENANT_TWO, oneTaskProcess);

    ProcessInstance processInstanceOne = startProcessInstanceForTenant(TENANT_ONE, TENANT_ONE_VAR);
    ProcessInstance processInstanceTwo = startProcessInstanceForTenant(TENANT_TWO, TENANT_TWO_VAR);

    completeUserTask(processInstanceOne, TENANT_ONE_VAR + "_updated");
    completeUserTask(processInstanceTwo, TENANT_TWO_VAR + "_updated");

  }

  public void testQueryWithoutTenantId() {
    HistoricDetailQuery query = historyService
        .createHistoricDetailQuery()
        .variableUpdates();

    assertThat(query.count(), is(4L));
  }

  public void testQueryByTenantId() {
    HistoricDetailQuery query = historyService
        .createHistoricDetailQuery()
        .variableUpdates()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(2L));

    query = historyService
        .createHistoricDetailQuery()
        .variableUpdates()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryByTenantIds() {
    HistoricDetailQuery query = historyService
        .createHistoricDetailQuery()
        .variableUpdates()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(4L));
  }

  public void testQueryByNonExistingTenantId() {
    HistoricDetailQuery query = historyService
        .createHistoricDetailQuery()
        .variableUpdates()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      historyService.createHistoricDetailQuery()
        .variableUpdates()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery()
        .variableUpdates()
        .orderByTenantId()
        .asc()
        .list();

    assertThat(historicDetails.size(), is(4));
    assertThat(historicDetails.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(historicDetails.get(1).getTenantId(), is(TENANT_ONE));
    assertThat(historicDetails.get(2).getTenantId(), is(TENANT_TWO));
    assertThat(historicDetails.get(3).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery()
        .variableUpdates()
        .orderByTenantId()
        .desc()
        .list();

    assertThat(historicDetails.size(), is(4));
    assertThat(historicDetails.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(historicDetails.get(1).getTenantId(), is(TENANT_TWO));
    assertThat(historicDetails.get(2).getTenantId(), is(TENANT_ONE));
    assertThat(historicDetails.get(3).getTenantId(), is(TENANT_ONE));
  }

  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    HistoricDetailQuery query = historyService.createHistoricDetailQuery();
    assertThat(query.count(), is(0L));
  }

  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    HistoricDetailQuery query = historyService.createHistoricDetailQuery();

    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count(), is(2L));
  }

  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    HistoricDetailQuery query = historyService.createHistoricDetailQuery();

    assertThat(query.count(), is(4L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(2L));
  }

  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    HistoricDetailQuery query = historyService.createHistoricDetailQuery();
    assertThat(query.count(), is(4L));
  }

  protected ProcessInstance startProcessInstanceForTenant(String tenant, String var) {
    return runtimeService.createProcessInstanceByKey("testProcess")
        .setVariable(VARIABLE_NAME, var)
        .processDefinitionTenantId(tenant)
        .execute();
  }

  protected void completeUserTask(ProcessInstance processInstance, String varValue) {
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task, is(notNullValue()));

    Map<String, Object> updatedVariables = new HashMap<String, Object>();
    updatedVariables.put(VARIABLE_NAME, varValue);
    taskService.complete(task.getId(), updatedVariables);
  }

}
