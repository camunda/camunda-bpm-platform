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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.TimerActivateProcessDefinitionHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerSuspendProcessDefinitionHandler;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MultiTenancyProcessDefinitionSuspensionStateTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String PROCESS_DEFINITION_KEY = "testProcess";

  protected static final BpmnModelInstance PROCESS = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent()
      .userTask()
        .camundaAsyncBefore()
      .endEvent()
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
  public void suspendAndActivateProcessDefinitionsForAllTenants() {
    // given activated process definitions
    ProcessDefinitionQuery query = engineRule.getRepositoryService().createProcessDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    // first suspend
    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    // then activate
    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .activate();

    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);
  }

  @Test
  public void suspendProcessDefinitionForTenant() {
    // given activated process definitions
    ProcessDefinitionQuery query = engineRule.getRepositoryService().createProcessDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void suspendProcessDefinitionForNonTenant() {
    // given activated process definitions
    ProcessDefinitionQuery query = engineRule.getRepositoryService().createProcessDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void activateProcessDefinitionForTenant() {
    // given suspend process definitions
    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    ProcessDefinitionQuery query = engineRule.getRepositoryService().createProcessDefinitionQuery();
    assertThat(query.suspended().count()).isEqualTo(3L);
    assertThat(query.active().count()).isEqualTo(0L);

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void activateProcessDefinitionForNonTenant() {
    // given suspend process definitions
    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    ProcessDefinitionQuery query = engineRule.getRepositoryService().createProcessDefinitionQuery();
    assertThat(query.suspended().count()).isEqualTo(3L);
    assertThat(query.active().count()).isEqualTo(0L);

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void suspendAndActivateProcessDefinitionsIncludeInstancesForAllTenants() {
    // given activated process instances
    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    // first suspend
    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .includeProcessInstances(true)
      .suspend();

    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    // then activate
    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .includeProcessInstances(true)
      .activate();

    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);
  }

  @Test
  public void suspendProcessDefinitionIncludeInstancesForTenant() {
    // given activated process instances
    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .includeProcessInstances(true)
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void suspendProcessDefinitionIncludeInstancesForNonTenant() {
    // given activated process instances
    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .includeProcessInstances(true)
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void activateProcessDefinitionIncludeInstancesForTenant() {
    // given suspended process instances
    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .includeProcessInstances(true)
      .suspend();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.suspended().count()).isEqualTo(3L);
    assertThat(query.active().count()).isEqualTo(0L);

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .includeProcessInstances(true)
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void activateProcessDefinitionIncludeInstancesForNonTenant() {
    // given suspended process instances
    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .includeProcessInstances(true)
      .suspend();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.suspended().count()).isEqualTo(3L);
    assertThat(query.active().count()).isEqualTo(0L);

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .includeProcessInstances(true)
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void delayedSuspendProcessDefinitionsForAllTenants() {
    // given activated process definitions

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .executionDate(tomorrow())
      .suspend();

    ProcessDefinitionQuery query = engineRule.getRepositoryService().createProcessDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    // when execute the job to suspend the process definitions
    Job job = engineRule.getManagementService().createJobQuery().timers().singleResult();
    assertThat(job).isNotNull();
    List<String> expectedDeploymentIds = query.active().list().stream().map(ProcessDefinition::getDeploymentId).collect(Collectors.toList());
    assertThat(expectedDeploymentIds).contains(job.getDeploymentId());

    engineRule.getManagementService().executeJob(job.getId());

    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);
  }

  @Test
  public void delayedSuspendProcessDefinitionsForTenant() {
    // given activated process definitions

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .executionDate(tomorrow())
      .suspend();

    ProcessDefinitionQuery query = engineRule.getRepositoryService().createProcessDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    // when execute the job to suspend the process definition
    Job job = engineRule.getManagementService().createJobQuery().timers().singleResult();
    assertThat(job).isNotNull();
    String expectedDeploymentId = engineRule.getRepositoryService().createProcessDefinitionQuery()
        .active().tenantIdIn(TENANT_ONE).singleResult().getDeploymentId();
    assertThat(job.getDeploymentId()).isEqualTo(expectedDeploymentId);

    engineRule.getManagementService().executeJob(job.getId());

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void delayedSuspendProcessDefinitionsForNonTenant() {
    // given activated process definitions

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .executionDate(tomorrow())
      .suspend();

    ProcessDefinitionQuery query = engineRule.getRepositoryService().createProcessDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    // when execute the job to suspend the process definition
    Job job = engineRule.getManagementService().createJobQuery().timers().singleResult();
    assertThat(job).isNotNull();
    String expectedDeploymentId = engineRule.getRepositoryService().createProcessDefinitionQuery()
        .active().withoutTenantId().singleResult().getDeploymentId();
    assertThat(job.getDeploymentId()).isEqualTo(expectedDeploymentId);

    engineRule.getManagementService().executeJob(job.getId());

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void delayedActivateProcessDefinitionsForAllTenants() {
    // given suspended process definitions
    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .executionDate(tomorrow())
      .activate();

    ProcessDefinitionQuery query = engineRule.getRepositoryService().createProcessDefinitionQuery();
    assertThat(query.suspended().count()).isEqualTo(3L);
    assertThat(query.active().count()).isEqualTo(0L);

    // when execute the job to activate the process definitions
    Job job = engineRule.getManagementService().createJobQuery().timers().singleResult();
    assertThat(job).isNotNull();
    List<String> expectedDeploymentIds = query.suspended().list().stream().map(ProcessDefinition::getDeploymentId).collect(Collectors.toList());
    assertThat(expectedDeploymentIds).contains(job.getDeploymentId());

    engineRule.getManagementService().executeJob(job.getId());

    assertThat(query.suspended().count()).isEqualTo(0L);
    assertThat(query.active().count()).isEqualTo(3L);
  }

  @Test
  public void delayedActivateProcessDefinitionsForTenant() {
    // given suspended process definitions
    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .executionDate(tomorrow())
      .activate();

    ProcessDefinitionQuery query = engineRule.getRepositoryService().createProcessDefinitionQuery();
    assertThat(query.suspended().count()).isEqualTo(3L);
    assertThat(query.active().count()).isEqualTo(0L);

    // when execute the job to activate the process definition
    Job job = engineRule.getManagementService().createJobQuery().timers().singleResult();
    assertThat(job).isNotNull();
    String expectedDeploymentId = engineRule.getRepositoryService().createProcessDefinitionQuery()
        .suspended().tenantIdIn(TENANT_ONE).singleResult().getDeploymentId();
    assertThat(job.getDeploymentId()).isEqualTo(expectedDeploymentId);

    engineRule.getManagementService().executeJob(job.getId());

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void delayedActivateProcessDefinitionsForNonTenant() {
    // given suspended process definitions
    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .executionDate(tomorrow())
      .activate();

    ProcessDefinitionQuery query = engineRule.getRepositoryService().createProcessDefinitionQuery();
    assertThat(query.suspended().count()).isEqualTo(3L);
    assertThat(query.active().count()).isEqualTo(0L);

    // when execute the job to activate the process definition
    Job job = engineRule.getManagementService().createJobQuery().timers().singleResult();
    assertThat(job).isNotNull();
    String expectedDeploymentId = engineRule.getRepositoryService().createProcessDefinitionQuery()
        .suspended().withoutTenantId().singleResult().getDeploymentId();
    assertThat(job.getDeploymentId()).isEqualTo(expectedDeploymentId);

    engineRule.getManagementService().executeJob(job.getId());

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void suspendProcessDefinitionIncludingJobDefinitionsForAllTenants() {
    // given activated jobs
    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);
  }

  @Test
  public void suspendProcessDefinitionIncludingJobDefinitionsForTenant() {
    // given activated jobs
    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void suspendProcessDefinitionIncludingJobDefinitionsForNonTenant() {
    // given activated jobs
    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void activateProcessDefinitionIncludingJobDefinitionsForAllTenants() {
    // given suspended jobs
    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .activate();

    assertThat(query.suspended().count()).isEqualTo(0L);
    assertThat(query.active().count()).isEqualTo(3L);
  }

  @Test
  public void activateProcessDefinitionIncludingJobDefinitionsForTenant() {
    // given suspended jobs
    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void activateProcessDefinitionIncludingJobDefinitionsForNonTenant() {
    // given suspended jobs
    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void suspendProcessDefinitionNoAuthenticatedTenants() {
    // given activated process definitions
    ProcessDefinitionQuery query = engineRule.getRepositoryService().createProcessDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    engineRule.getIdentityService().clearAuthentication();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void failToSuspendProcessDefinitionByIdNoAuthenticatedTenants() {
    ProcessDefinition processDefinition = engineRule.getRepositoryService().createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY).tenantIdIn(TENANT_ONE).singleResult();

    engineRule.getIdentityService().setAuthentication("user", null, null);

    // when/then
    assertThatThrownBy(() -> engineRule.getRepositoryService()
        .updateProcessDefinitionSuspensionState()
        .byProcessDefinitionId(processDefinition.getId())
        .suspend())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot update the process definition '"+ processDefinition.getId()
      + "' because it belongs to no authenticated tenant");
  }

  @Test
  public void suspendProcessDefinitionWithAuthenticatedTenant() {
    // given activated process definitions
    ProcessDefinitionQuery query = engineRule.getRepositoryService().createProcessDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    engineRule.getIdentityService().clearAuthentication();

    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.suspended().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void suspendProcessDefinitionDisabledTenantCheck() {
    // given activated process definitions
    ProcessDefinitionQuery query = engineRule.getRepositoryService().createProcessDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    ProcessEngineConfigurationImpl processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    processEngineConfiguration.setTenantCheckEnabled(false);
    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRepositoryService()
      .updateProcessDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE, TENANT_TWO).includeProcessDefinitionsWithoutTenantId().count()).isEqualTo(3L);
  }

  protected Date tomorrow() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_YEAR, 1);
    return calendar.getTime();
  }

  @After
  public void tearDown() throws Exception {
    CommandExecutor commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerActivateProcessDefinitionHandler.TYPE);
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerSuspendProcessDefinitionHandler.TYPE);
        return null;
      }
    });
  }

}
