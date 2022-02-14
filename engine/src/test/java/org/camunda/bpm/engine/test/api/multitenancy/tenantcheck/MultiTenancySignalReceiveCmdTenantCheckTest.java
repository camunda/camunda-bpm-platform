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
package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
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

/**
 * @author kristin.polenz
 */
public class MultiTenancySignalReceiveCmdTenantCheckTest {

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

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected IdentityService identityService;

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    identityService = engineRule.getIdentityService();
  }

  @Test
  public void sendSignalToStartEventNoAuthenticatedTenants() {
    testRule.deploy(SIGNAL_START_PROCESS);
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS);

    identityService.setAuthentication("user", null, null);

    runtimeService.createSignalEvent("signal").send();

    identityService.clearAuthentication();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(0L);
  }

  @Test
  public void sendSignalToStartEventWithAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_START_PROCESS);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    runtimeService.createSignalEvent("signal").send();

    identityService.clearAuthentication();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
  }

  @Test
  public void sendSignalToStartEventDisabledTenantCheck() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_START_PROCESS);

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    runtimeService.createSignalEvent("signal").send();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
  }

  @Test
  public void sendSignalToIntermediateCatchEventNoAuthenticatedTenants() {
    testRule.deploy(SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionWithoutTenantId().execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();

    identityService.setAuthentication("user", null, null);

    runtimeService.createSignalEvent("signal").send();

    identityService.clearAuthentication();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(0L);
    assertThat(taskService.createTaskQuery().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void sendSignalToIntermediateCatchEventWithAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    runtimeService.createSignalEvent("signal").send();

    identityService.clearAuthentication();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
  }

  @Test
  public void sendSignalToIntermediateCatchEventDisabledTenantCheck() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    runtimeService.createSignalEvent("signal").send();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
  }

  @Test
  public void sendSignalToStartAndIntermediateCatchEventNoAuthenticatedTenants() {
    testRule.deploy(SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionWithoutTenantId().execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();

    identityService.setAuthentication("user", null, null);

    runtimeService.createSignalEvent("signal").send();

    identityService.clearAuthentication();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(0L);
    assertThat(taskService.createTaskQuery().withoutTenantId().count()).isEqualTo(2L);
  }

  @Test
  public void sendSignalToStartAndIntermediateCatchEventWithAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    runtimeService.createSignalEvent("signal").send();

    identityService.clearAuthentication();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
  }

  @Test
  public void sendSignalToStartAndIntermediateCatchEventDisabledTenantCheck() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    runtimeService.createSignalEvent("signal").send();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count()).isEqualTo(4L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(2L);
  }

  @Test
  public void sendSignalToIntermediateCatchEventWithExecutionIdAndAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();

    Execution execution = runtimeService.createExecutionQuery()
      .processDefinitionKey("signalCatch")
      .signalEventSubscriptionName("signal")
      .singleResult();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    runtimeService.createSignalEvent("signal").executionId(execution.getId()).send();

    identityService.clearAuthentication();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void failToSendSignalToIntermediateCatchEventWithExecutionIdAndNoAuthenticatedTenants() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();

    Execution execution = runtimeService.createExecutionQuery()
      .processDefinitionKey("signalCatch")
      .signalEventSubscriptionName("signal")
      .singleResult();

    identityService.setAuthentication("user", null, null);

    // when/then
    assertThatThrownBy(() -> runtimeService.createSignalEvent("signal").executionId(execution.getId()).send())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot update the process instance");

  }

  @Test
  public void signalIntermediateCatchEventNoAuthenticatedTenants() {
    testRule.deploy(SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").execute();

    Execution execution = runtimeService.createExecutionQuery()
      .processDefinitionKey("signalCatch")
      .signalEventSubscriptionName("signal")
      .singleResult();

    identityService.setAuthentication("user", null, null);

    runtimeService.signal(execution.getId(), "signal", null, null);

    identityService.clearAuthentication();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void signalIntermediateCatchEventWithAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();

    Execution execution = runtimeService.createExecutionQuery()
      .processDefinitionKey("signalCatch")
      .signalEventSubscriptionName("signal")
      .singleResult();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    runtimeService.signal(execution.getId(), "signal", null, null);

    identityService.clearAuthentication();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void signalIntermediateCatchEventDisabledTenantCheck() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    Execution execution = runtimeService.createExecutionQuery()
      .processDefinitionKey("signalCatch")
      .signalEventSubscriptionName("signal")
      .tenantIdIn(TENANT_ONE)
      .singleResult();

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    runtimeService.signal(execution.getId(), "signal", null, null);

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
  }

  @Test
  public void failToSignalIntermediateCatchEventNoAuthenticatedTenants() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").execute();

    Execution execution = runtimeService.createExecutionQuery()
      .processDefinitionKey("signalCatch")
      .signalEventSubscriptionName("signal")
      .singleResult();

    identityService.setAuthentication("user", null, null);

    // when/then
    assertThatThrownBy(() -> runtimeService.signal(execution.getId(), "signal", null, null))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot update the process instance");
  }
}
