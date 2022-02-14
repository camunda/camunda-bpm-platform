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
package org.camunda.bpm.engine.test.api.multitenancy.suspensionstate;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProvider;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.multitenancy.StaticTenantIdTestProvider;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MultiTenancyProcessDefinitionSuspensionStateTenantIdProviderTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String PROCESS_DEFINITION_KEY = "testProcess";
  protected static final BpmnModelInstance PROCESS = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent()
      .userTask()
        .camundaAsyncBefore()
      .endEvent()
    .done();

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration -> {
    TenantIdProvider tenantIdProvider = new StaticTenantIdTestProvider(TENANT_ONE);
    configuration.setTenantIdProvider(tenantIdProvider);
  });
  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void setUp() throws Exception {
    testRule.deploy(PROCESS);
  }

  @Test
  public void suspendProcessDefinitionByIdIncludeInstancesFromAllTenants() {
    // given active process instances with tenant id of process definition without tenant id
    engineRule.getRuntimeService().createProcessInstanceByKey(PROCESS_DEFINITION_KEY).processDefinitionWithoutTenantId().execute();

    ProcessDefinition processDefinition = engineRule.getRepositoryService()
        .createProcessDefinitionQuery()
        .withoutTenantId()
        .singleResult();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery().processDefinitionId(processDefinition.getId());
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    // suspend all instances of process definition
    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionId(processDefinition.getId())
      .includeProcessInstances(true)
      .suspend();

    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void activateProcessDefinitionByIdIncludeInstancesFromAllTenants() {
    // given suspended process instances with tenant id of process definition without tenant id
    engineRule.getRuntimeService().createProcessInstanceByKey(PROCESS_DEFINITION_KEY).processDefinitionWithoutTenantId().execute();

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .includeProcessInstances(true)
      .suspend();

    ProcessDefinition processDefinition = engineRule.getRepositoryService()
        .createProcessDefinitionQuery()
        .withoutTenantId()
        .singleResult();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery().processDefinitionId(processDefinition.getId());
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.active().count()).isEqualTo(0L);

    // activate all instance of process definition
    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionId(processDefinition.getId())
      .includeProcessInstances(true)
      .activate();

    assertThat(query.suspended().count()).isEqualTo(0L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }
}
