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
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicTaskInstanceByTenantId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.verifySorting;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
public class MultiTenancyHistoricTaskInstanceQueryTest {

  protected final static String TENANT_NULL = null;
  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected TaskService taskService;
  protected IdentityService identityService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
    identityService = engineRule.getIdentityService();

    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .userTask()
      .endEvent()
    .done();

    // given
    testRule.deployForTenant(TENANT_NULL, oneTaskProcess);
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    testRule.deployForTenant(TENANT_TWO, oneTaskProcess);

    ProcessInstance processInstanceNull = startProcessInstanceForTenant(TENANT_NULL);
    ProcessInstance processInstanceOne = startProcessInstanceForTenant(TENANT_ONE);
    ProcessInstance processInstanceTwo = startProcessInstanceForTenant(TENANT_TWO);

    completeUserTask(processInstanceNull);
    completeUserTask(processInstanceOne);
    completeUserTask(processInstanceTwo);
  }

  @Test
  public void shouldQueryWithoutTenantId() {
    //when
    HistoricTaskInstanceQuery query = historyService
        .createHistoricTaskInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void shouldQueryByTenantId() {
    // when
    HistoricTaskInstanceQuery queryTenantOne = historyService
        .createHistoricTaskInstanceQuery()
        .tenantIdIn(TENANT_ONE);

    HistoricTaskInstanceQuery queryTenantTwo = historyService
        .createHistoricTaskInstanceQuery()
        .tenantIdIn(TENANT_TWO);

    // then
    assertThat(queryTenantOne.count()).isEqualTo(1L);
    assertThat(queryTenantTwo.count()).isEqualTo(1L);
  }

  @Test
  public void shouldQueryFilterWithoutTenantId() {
    //when
    HistoricTaskInstanceQuery query = historyService
        .createHistoricTaskInstanceQuery()
        .withoutTenantId();

    // then
    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void shouldQueryByTenantIds() {
    // given
    HistoricTaskInstanceQuery query = historyService
        .createHistoricTaskInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    // then
    assertThat(query.count()).isEqualTo(2L);
  }

  @Test
  public void shouldQueryByNonExistingTenantId() {
    // when
    HistoricTaskInstanceQuery query = historyService
        .createHistoricTaskInstanceQuery()
        .tenantIdIn("nonExisting");

    // then
    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void shouldFailQueryByTenantIdNull() {
    try {
      // when
      historyService.createHistoricTaskInstanceQuery()
        .tenantIdIn((String) null);

      fail("expected exception");

      // then
    } catch (NullValueException e) {
    }
  }

  @Test
  public void shouldQuerySortingAsc() {
    // when
    List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
        .orderByTenantId()
        .asc()
        .list();

    // then
    assertThat(historicTaskInstances.size()).isEqualTo(3);
    verifySorting(historicTaskInstances, historicTaskInstanceByTenantId());
  }

  @Test
  public void shouldQuerySortingDesc() {
    // when
    List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
        .orderByTenantId()
        .desc()
        .list();

    // then
    assertThat(historicTaskInstances.size()).isEqualTo(3);
    verifySorting(historicTaskInstances, inverted(historicTaskInstanceByTenantId()));
  }

  @Test
  public void shouldQueryNoAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, null);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(1L); // null-tenant instances are included
  }

  @Test
  public void shouldQueryAuthenticatedTenant() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(2L); // null-tenant instances are included
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count()).isEqualTo(1L);
  }

  @Test
  public void shouldQueryAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(3L); // null-tenant instances are included
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
  }

  @Test
  public void shouldQueryDisabledTenantCheck() {
    // given
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(3L);
  }

  protected ProcessInstance startProcessInstanceForTenant(String tenant) {
    return runtimeService.createProcessInstanceByKey("testProcess")
        .processDefinitionTenantId(tenant)
        .execute();
  }

  protected void completeUserTask(ProcessInstance processInstance) {
   Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
   assertThat(task).isNotNull();
   taskService.complete(task.getId());
 }

}
