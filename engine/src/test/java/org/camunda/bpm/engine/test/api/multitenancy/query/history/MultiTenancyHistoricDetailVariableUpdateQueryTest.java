/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.multitenancy.query.history;

import static org.assertj.core.api.Assertions.assertThat;

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

  protected final static String TENANT_NULL = null;
  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  protected final static String VARIABLE_NAME = "myVar";
  protected final static String TENANT_NULL_VAR = "tenantNullVar";
  protected final static String TENANT_ONE_VAR = "tenant1Var";
  protected final static String TENANT_TWO_VAR = "tenant2Var";

  @Override
  protected void setUp() {
    // given
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .userTask()
      .endEvent()
    .done();

    deploymentForTenant(TENANT_NULL, oneTaskProcess);
    deploymentForTenant(TENANT_ONE, oneTaskProcess);
    deploymentForTenant(TENANT_TWO, oneTaskProcess);

    ProcessInstance processInstanceNull = startProcessInstanceForTenant(TENANT_NULL, TENANT_NULL_VAR);
    ProcessInstance processInstanceOne = startProcessInstanceForTenant(TENANT_ONE, TENANT_ONE_VAR);
    ProcessInstance processInstanceTwo = startProcessInstanceForTenant(TENANT_TWO, TENANT_TWO_VAR);

    completeUserTask(processInstanceNull, TENANT_NULL_VAR + "_updated");
    completeUserTask(processInstanceOne, TENANT_ONE_VAR + "_updated");
    completeUserTask(processInstanceTwo, TENANT_TWO_VAR + "_updated");

  }

  public void testQueryWithoutTenantId() {
    // when
    HistoricDetailQuery query = historyService
        .createHistoricDetailQuery()
        .variableUpdates();

    // then
    assertThat(query.count()).isEqualTo(6L);
  }

  public void testQueryFilterWithoutTenantId() {
    // when
    HistoricDetailQuery query = historyService
        .createHistoricDetailQuery()
        .variableUpdates()
        .withoutTenantId();

    // then
    assertThat(query.count()).isEqualTo(2L);
  }

  public void testQueryByTenantId() {
    // when
    HistoricDetailQuery queryTenantOne = historyService
        .createHistoricDetailQuery()
        .variableUpdates()
        .tenantIdIn(TENANT_ONE);

    HistoricDetailQuery queryTenantTwo = historyService
        .createHistoricDetailQuery()
        .variableUpdates()
        .tenantIdIn(TENANT_TWO);

    // then
    assertThat(queryTenantOne.count()).isEqualTo(2L);
    assertThat(queryTenantTwo.count()).isEqualTo(2L);
  }

  public void testQueryByTenantIds() {
    // when
    HistoricDetailQuery query = historyService
        .createHistoricDetailQuery()
        .variableUpdates()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    // then
    assertThat(query.count()).isEqualTo(4L);
  }

  public void testQueryByNonExistingTenantId() {
    // when
    HistoricDetailQuery query = historyService
        .createHistoricDetailQuery()
        .variableUpdates()
        .tenantIdIn("nonExisting");

    // then
    assertThat(query.count()).isEqualTo(0L);
  }

  public void testFailQueryByTenantIdNull() {
    try {
      // when
      historyService.createHistoricDetailQuery()
        .variableUpdates()
        .tenantIdIn((String) null);

      fail("expected exception");

      // then
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    // when
    List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery()
        .variableUpdates()
        .orderByTenantId()
        .asc()
        .list();

    // then
    assertThat(historicDetails.size()).isEqualTo(6);
    assertThat(historicDetails.get(0).getTenantId()).isEqualTo(TENANT_NULL);
    assertThat(historicDetails.get(1).getTenantId()).isEqualTo(TENANT_NULL);
    assertThat(historicDetails.get(2).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(historicDetails.get(3).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(historicDetails.get(4).getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(historicDetails.get(5).getTenantId()).isEqualTo(TENANT_TWO);
  }

  public void testQuerySortingDesc() {
    // when
    List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery()
        .variableUpdates()
        .orderByTenantId()
        .desc()
        .list();

    // then
    assertThat(historicDetails.size()).isEqualTo(6);
    assertThat(historicDetails.get(0).getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(historicDetails.get(1).getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(historicDetails.get(2).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(historicDetails.get(3).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(historicDetails.get(4).getTenantId()).isEqualTo(TENANT_NULL);
    assertThat(historicDetails.get(5).getTenantId()).isEqualTo(TENANT_NULL);
  }

  public void testQueryNoAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, null);

    // when
    HistoricDetailQuery query = historyService.createHistoricDetailQuery();

    // then
    assertThat(query.count()).isEqualTo(2L); // null-tenant instances are still included
  }

  public void testQueryAuthenticatedTenant() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    HistoricDetailQuery query = historyService.createHistoricDetailQuery();

    // then
    assertThat(query.count()).isEqualTo(4L); // null-tenant instances are still included
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(2L);
    assertThat(query.withoutTenantId().count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count()).isEqualTo(2L);
  }

  public void testQueryAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    // when
    HistoricDetailQuery query = historyService.createHistoricDetailQuery();

    // then
    assertThat(query.count()).isEqualTo(6L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(2L);
  }

  public void testQueryDisabledTenantCheck() {
    // given
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    // when
    HistoricDetailQuery query = historyService.createHistoricDetailQuery();

    // then
    assertThat(query.count()).isEqualTo(6L); // null-tenant instances are still included
  }

  protected ProcessInstance startProcessInstanceForTenant(String tenant, String var) {
    return runtimeService.createProcessInstanceByKey("testProcess")
        .setVariable(VARIABLE_NAME, var)
        .processDefinitionTenantId(tenant)
        .execute();
  }

  protected void completeUserTask(ProcessInstance processInstance, String varValue) {
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();

    Map<String, Object> updatedVariables = new HashMap<String, Object>();
    updatedVariables.put(VARIABLE_NAME, varValue);
    taskService.complete(task.getId(), updatedVariables);
  }

}
