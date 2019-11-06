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

import java.io.IOException;
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
public class MultiTenancyHistoricDetailFormPropertyQueryTest extends PluggableProcessEngineTestCase {

  protected final static String TENANT_NULL = null;
  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  @Override
  protected void setUp() throws IOException {
    // given
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .userTask("userTask")
        .camundaFormField()
          .camundaId("myFormField")
          .camundaType("string")
          .camundaFormFieldDone()
      .endEvent()
    .done();

    deploymentForTenant(TENANT_NULL, oneTaskProcess);
    deploymentForTenant(TENANT_ONE, oneTaskProcess);
    deploymentForTenant(TENANT_TWO, oneTaskProcess);

    ProcessInstance processInstanceNull = startProcessInstanceForTenant(TENANT_NULL);
    ProcessInstance processInstanceOne = startProcessInstanceForTenant(TENANT_ONE);
    ProcessInstance processInstanceTwo = startProcessInstanceForTenant(TENANT_TWO);

    completeUserTask(processInstanceNull);
    completeUserTask(processInstanceOne);
    completeUserTask(processInstanceTwo);
  }

  public void testQueryWithoutTenantId() {
    // when
    HistoricDetailQuery query = historyService
        .createHistoricDetailQuery()
        .formFields();

    // given
    assertThat(query.count()).isEqualTo(3L);
  }

  public void testQueryFilterWithoutTenantId() {
    // when
    HistoricDetailQuery query = historyService
        .createHistoricDetailQuery()
        .withoutTenantId()
        .formFields();

    // then
    assertThat(query.count()).isEqualTo(1L);
  }

  public void testQueryByTenantId() {
    // when
    HistoricDetailQuery queryTenantOne = historyService
        .createHistoricDetailQuery()
        .formFields()
        .tenantIdIn(TENANT_ONE);

    HistoricDetailQuery queryTenantTwo = historyService
        .createHistoricDetailQuery()
        .formFields()
        .tenantIdIn(TENANT_TWO);

    // then
    assertThat(queryTenantOne.count()).isEqualTo(1L);
    assertThat(queryTenantTwo.count()).isEqualTo(1L);
  }

  public void testQueryByTenantIds() {
    // when
    HistoricDetailQuery query = historyService
        .createHistoricDetailQuery()
        .formFields()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    // then
    assertThat(query.count()).isEqualTo(2L);
  }

  public void testQueryByNonExistingTenantId() {
    // when
    HistoricDetailQuery query = historyService
        .createHistoricDetailQuery()
        .formFields()
        .tenantIdIn("nonExisting");

    // then
    assertThat(query.count()).isEqualTo(0L);
  }

  public void testFailQueryByTenantIdNull() {
    try {
      // when
      historyService.createHistoricDetailQuery()
        .formFields()
        .tenantIdIn((String) null);

      fail("expected exception");

      // then
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    // when
    List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery()
        .formFields()
        .orderByTenantId()
        .asc()
        .list();

    // then
    assertThat(historicDetails.size()).isEqualTo(3);
    assertThat(historicDetails.get(0).getTenantId()).isEqualTo(TENANT_NULL);
    assertThat(historicDetails.get(1).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(historicDetails.get(2).getTenantId()).isEqualTo(TENANT_TWO);
  }

  public void testQuerySortingDesc() {
    // when
    List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery()
        .formFields()
        .orderByTenantId()
        .desc()
        .list();

    // then
    assertThat(historicDetails.size()).isEqualTo(3);
    assertThat(historicDetails.get(0).getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(historicDetails.get(1).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(historicDetails.get(2).getTenantId()).isEqualTo(TENANT_NULL);
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
    assertThat(query.count()).isEqualTo(4L);
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
    assertThat(query.count()).isEqualTo(6L);  // null-tenant instances are still included
    assertThat(query.withoutTenantId().count()).isEqualTo(2L);
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
    assertThat(query.count()).isEqualTo(6L);
  }

  protected ProcessInstance startProcessInstanceForTenant(String tenant) {
    return runtimeService.createProcessInstanceByKey("testProcess")
        .processDefinitionTenantId(tenant)
        .execute();
  }

  protected void completeUserTask(ProcessInstance processInstance) {
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("myFormField", "myFormFieldValue");
    formService.submitTaskForm(task.getId(), properties);
  }

}
