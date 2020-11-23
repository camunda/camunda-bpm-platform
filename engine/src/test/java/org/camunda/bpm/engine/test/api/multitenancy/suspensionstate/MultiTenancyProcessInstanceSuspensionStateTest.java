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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.JobQuery;
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

public class MultiTenancyProcessInstanceSuspensionStateTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String PROCESS_DEFINITION_KEY = "testProcess";

  protected static final BpmnModelInstance PROCESS = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent()
      .parallelGateway("fork")
        .userTask()
      .moveToLastGateway()
        .sendTask()
          .camundaType("external")
          .camundaTopic("test")
        .boundaryEvent()
          .timerWithDuration("PT1M")
      .done();

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void setUp() throws Exception {

    testRule.deployForTenant(TENANT_ONE, PROCESS);
    testRule.deployForTenant(TENANT_TWO, PROCESS);
    testRule.deploy(PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey(PROCESS_DEFINITION_KEY).processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey(PROCESS_DEFINITION_KEY).processDefinitionTenantId(TENANT_TWO).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey(PROCESS_DEFINITION_KEY).processDefinitionWithoutTenantId().execute();
  }

  @Test
  public void suspendAndActivateProcessInstancesForAllTenants() {
    // given activated process instances
    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    // first suspend
    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    // then activate
    engineRule.getRuntimeService()
    .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .activate();

    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);
  }

  @Test
  public void suspendProcessInstanceForTenant() {
    // given activated process instances
    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void suspendProcessInstanceForNonTenant() {
    // given activated process instances
    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void activateProcessInstanceForTenant() {
    // given suspended process instances
    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void activateProcessInstanceForNonTenant() {
    // given suspended process instances
    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void suspendAndActivateProcessInstancesIncludingUserTasksForAllTenants() {
    // given activated user tasks
    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    // first suspend
    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    // then activate
    engineRule.getRuntimeService()
    .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .activate();

    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);
  }

  @Test
  public void suspendProcessInstanceIncludingUserTaskForTenant() {
    // given activated user tasks
    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void suspendProcessInstanceIncludingUserTaskForNonTenant() {
    // given activated user tasks
    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void activateProcessInstanceIncludingUserTaskForTenant() {
    // given suspended user tasks
    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void activateProcessInstanceIncludingUserTaskForNonTenant() {
    // given suspended user tasks
    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void suspendAndActivateProcessInstancesIncludingExternalTasksForAllTenants() {
    // given activated external tasks
    ExternalTaskQuery query = engineRule.getExternalTaskService().createExternalTaskQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    // first suspend
    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    // then activate
    engineRule.getRuntimeService()
    .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .activate();

    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);
  }

  @Test
  public void suspendProcessInstanceIncludingExternalTaskForTenant() {
    // given activated external tasks
    ExternalTaskQuery query = engineRule.getExternalTaskService().createExternalTaskQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void suspendProcessInstanceIncludingExternalTaskForNonTenant() {
    // given activated external tasks
    ExternalTaskQuery query = engineRule.getExternalTaskService().createExternalTaskQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().singleResult().getTenantId()).isNull();
  }

  @Test
  public void activateProcessInstanceIncludingExternalTaskForTenant() {
    // given suspended external tasks
    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    ExternalTaskQuery query = engineRule.getExternalTaskService().createExternalTaskQuery();
    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void activateProcessInstanceIncludingExternalTaskForNonTenant() {
    // given suspended external tasks
    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    ExternalTaskQuery query = engineRule.getExternalTaskService().createExternalTaskQuery();
    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().singleResult().getTenantId()).isNull();
  }

  @Test
  public void suspendAndActivateProcessInstancesIncludingJobsForAllTenants() {
    // given activated jobs
    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    // first suspend
    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    // then activate
    engineRule.getRuntimeService()
    .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .activate();

    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);
  }

  @Test
  public void suspendProcessInstanceIncludingJobForTenant() {
    // given activated jobs
    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void suspendProcessInstanceIncludingJobForNonTenant() {
    // given activated jobs
    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().singleResult().getTenantId()).isNull();
  }

  @Test
  public void activateProcessInstanceIncludingJobForTenant() {
    // given suspended job
    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void activateProcessInstanceIncludingJobForNonTenant() {
    // given suspended jobs
    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().singleResult().getTenantId()).isNull();
  }

  @Test
  public void suspendProcessInstanceNoAuthenticatedTenants() {
    // given activated process instances
    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    engineRule.getIdentityService().clearAuthentication();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void failToSuspendProcessInstanceByProcessDefinitionIdNoAuthenticatedTenants() {
    ProcessDefinition processDefinition = engineRule.getRepositoryService().createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY).tenantIdIn(TENANT_ONE).singleResult();

    engineRule.getIdentityService().setAuthentication("user", null, null);

    // when/then
    assertThatThrownBy(() -> engineRule.getRuntimeService()
        .updateProcessInstanceSuspensionState()
        .byProcessDefinitionId(processDefinition.getId())
        .suspend())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot update the process definition '"
          + processDefinition.getId() +"' because it belongs to no authenticated tenant");
  }

  @Test
  public void suspendProcessInstanceWithAuthenticatedTenant() {
    // given activated process instances
    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    engineRule.getIdentityService().clearAuthentication();

    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
    assertThat(query.suspended().withoutTenantId().count()).isEqualTo(1L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void suspendProcessInstanceDisabledTenantCheck() {
    // given activated process instances
    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService()
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE, TENANT_TWO).count()).isEqualTo(2L);
    assertThat(query.suspended().withoutTenantId().count()).isEqualTo(1L);
  }
}
