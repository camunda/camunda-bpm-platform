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
package org.camunda.bpm.engine.test.api.multitenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MultiTenancySignalReceiveTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final BpmnModelInstance SIGNAL_START_PROCESS = Bpmn.createExecutableProcess("signalStart")
      .startEvent()
        .signal("signal")
      .userTask()
      .endEvent()
      .done();

  protected static final BpmnModelInstance SIGNAL_CATCH_PROCESS = Bpmn.createExecutableProcess("signalCatch")
      .startEvent()
      .intermediateCatchEvent()
        .signal("signal")
      .userTask()
      .endEvent()
      .done();

  protected static final BpmnModelInstance SIGNAL_INTERMEDIATE_THROW_PROCESS = Bpmn.createExecutableProcess("signalThrow")
      .startEvent()
      .intermediateThrowEvent()
        .signal("signal")
      .endEvent()
      .done();

  protected static final BpmnModelInstance SIGNAL_END_THROW_PROCESS = Bpmn.createExecutableProcess("signalThrow")
      .startEvent()
      .endEvent()
        .signal("signal")
      .done();

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected TaskService taskService;

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
  }

  @Test
  public void sendSignalToStartEventForNonTenant() {
    testRule.deploy(SIGNAL_START_PROCESS);
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS);

    runtimeService.createSignalEvent("signal").withoutTenantId().send();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.singleResult().getTenantId()).isNull();
  }

  @Test
  public void sendSignalToStartEventForTenant() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_START_PROCESS);

    runtimeService.createSignalEvent("signal").tenantId(TENANT_ONE).send();
    runtimeService.createSignalEvent("signal").tenantId(TENANT_TWO).send();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
  }

  @Test
  public void sendSignalToStartEventWithoutTenantIdForNonTenant() {
    testRule.deploy(SIGNAL_START_PROCESS);

    runtimeService.createSignalEvent("signal").send();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void sendSignalToStartEventWithoutTenantIdForTenant() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS);

    runtimeService.createSignalEvent("signal").send();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void sendSignalToIntermediateCatchEventForNonTenant() {
    testRule.deploy(SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionWithoutTenantId().execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();

    runtimeService.createSignalEvent("signal").withoutTenantId().send();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.singleResult().getTenantId()).isNull();
  }

  @Test
  public void sendSignalToIntermediateCatchEventForTenant() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    runtimeService.createSignalEvent("signal").tenantId(TENANT_ONE).send();
    runtimeService.createSignalEvent("signal").tenantId(TENANT_TWO).send();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
  }

  @Test
  public void sendSignalToIntermediateCatchEventWithoutTenantIdForNonTenant() {
    testRule.deploy(SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").execute();

    runtimeService.createSignalEvent("signal").send();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void sendSignalToIntermediateCatchEventWithoutTenantIdForTenant() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").execute();

    runtimeService.createSignalEvent("signal").send();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void sendSignalToStartAndIntermediateCatchEventForNonTenant() {
    testRule.deploy(SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionWithoutTenantId().execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();

    runtimeService.createSignalEvent("signal").withoutTenantId().send();

    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks.size()).isEqualTo(2);
    assertThat(tasks.get(0).getTenantId()).isNull();
    assertThat(tasks.get(1).getTenantId()).isNull();
  }

  @Test
  public void sendSignalToStartAndIntermediateCatchEventForTenant() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    runtimeService.createSignalEvent("signal").tenantId(TENANT_ONE).send();
    runtimeService.createSignalEvent("signal").tenantId(TENANT_TWO).send();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(2L);
  }

  @Test
  public void sendSignalToStartEventsForMultipleTenants() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_START_PROCESS);

    runtimeService.createSignalEvent("signal").send();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
  }

  @Test
  public void sendSignalToIntermediateCatchEventsForMultipleTenants() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    runtimeService.createSignalEvent("signal").send();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
  }

  @Test
  public void sendSignalToStartAndIntermediateCatchEventForMultipleTenants() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_START_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").execute();

    runtimeService.createSignalEvent("signal").send();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
  }

  @Test
  public void failToSendSignalWithExecutionIdForTenant() {

    // when/then
    assertThatThrownBy(() -> runtimeService.createSignalEvent("signal").executionId("id").tenantId(TENANT_ONE).send())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("Cannot specify a tenant-id when deliver a signal to a single execution.");
  }

  @Test
  public void throwIntermediateSignalForTenant() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS, SIGNAL_INTERMEDIATE_THROW_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);
    testRule.deploy(SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionWithoutTenantId().execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    runtimeService.startProcessInstanceByKey("signalThrow");

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
    assertThat(taskService.createTaskQuery().withoutTenantId().count()).isEqualTo(2L);
  }

  @Test
  public void throwIntermediateSignalForNonTenant() {
    testRule.deploy(SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS, SIGNAL_INTERMEDIATE_THROW_PROCESS);
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionWithoutTenantId().execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();

    runtimeService.startProcessInstanceByKey("signalThrow");

    assertThat(taskService.createTaskQuery().withoutTenantId().count()).isEqualTo(2L);
    assertThat(taskService.createTaskQuery().tenantIdIn(TENANT_ONE).count()).isEqualTo(0L);
  }

  @Test
  public void throwEndSignalForTenant() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS, SIGNAL_END_THROW_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);
    testRule.deploy(SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionWithoutTenantId().execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    runtimeService.startProcessInstanceByKey("signalThrow");

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
    assertThat(taskService.createTaskQuery().withoutTenantId().count()).isEqualTo(2L);
  }

  @Test
  public void throwEndSignalForNonTenant() {
    testRule.deploy(SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS, SIGNAL_END_THROW_PROCESS);
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionWithoutTenantId().execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();

    runtimeService.startProcessInstanceByKey("signalThrow");

    assertThat(taskService.createTaskQuery().withoutTenantId().count()).isEqualTo(2L);
    assertThat(taskService.createTaskQuery().tenantIdIn(TENANT_ONE).count()).isEqualTo(0L);
  }
}
