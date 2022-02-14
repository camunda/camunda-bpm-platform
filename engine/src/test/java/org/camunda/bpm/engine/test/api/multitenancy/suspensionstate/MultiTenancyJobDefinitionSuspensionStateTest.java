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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.TimerActivateJobDefinitionHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerSuspendJobDefinitionHandler;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
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

public class MultiTenancyJobDefinitionSuspensionStateTest {

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
  public void suspendAndActivateJobDefinitionsForAllTenants() {
    // given activated job definitions
    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    // first suspend
    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    // then activate
    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .activate();

    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);
  }

  @Test
  public void suspendJobDefinitionForTenant() {
    // given activated job definitions
    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void suspendJobDefinitionForNonTenant() {
    // given activated job definitions
    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void activateJobDefinitionForTenant() {
    // given suspend job definitions
    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.suspended().count()).isEqualTo(3L);
    assertThat(query.active().count()).isEqualTo(0L);

    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void jobProcessDefinitionForNonTenant() {
    // given suspend job definitions
    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.suspended().count()).isEqualTo(3L);
    assertThat(query.active().count()).isEqualTo(0L);

    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void suspendAndActivateJobDefinitionsIncludingJobsForAllTenants() {
    // given activated job definitions
    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    // first suspend
    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .includeJobs(true)
      .suspend();

    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    // then activate
    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .includeJobs(true)
      .activate();

    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);
  }

  @Test
  public void suspendJobDefinitionIncludingJobsForTenant() {
    // given activated job definitions
    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .includeJobs(true)
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void suspendJobDefinitionIncludingJobsForNonTenant() {
    // given activated job definitions
    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .includeJobs(true)
      .suspend();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void activateJobDefinitionIncludingJobsForTenant() {
    // given suspend job definitions
    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .includeJobs(true)
      .suspend();

    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.suspended().count()).isEqualTo(3L);
    assertThat(query.active().count()).isEqualTo(0L);

    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .includeJobs(true)
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void activateJobDefinitionIncludingJobsForNonTenant() {
    // given suspend job definitions
    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .includeJobs(true)
      .suspend();

    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.suspended().count()).isEqualTo(3L);
    assertThat(query.active().count()).isEqualTo(0L);

    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .includeJobs(true)
      .activate();

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void delayedSuspendJobDefinitionsForAllTenants() {
    // given activated job definitions

    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .executionDate(tomorrow())
      .suspend();

    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    // when execute the job to suspend the job definitions
    Job job = engineRule.getManagementService().createJobQuery().timers().singleResult();
    assertThat(job).isNotNull();
    assertThat(getDeploymentIds(query.active())).contains(job.getDeploymentId());

    engineRule.getManagementService().executeJob(job.getId());

    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);
  }

  @Test
  public void delayedSuspendJobDefinitionsForTenant() {
    // given activated job definitions

    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .executionDate(tomorrow())
      .suspend();

    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    // when execute the job to suspend the job definitions
    Job job = engineRule.getManagementService().createJobQuery().timers().singleResult();
    assertThat(job).isNotNull();
    JobDefinition expectedJobDefinition = engineRule.getManagementService().createJobDefinitionQuery()
        .active().tenantIdIn(TENANT_ONE).singleResult();
    assertThat(job.getDeploymentId()).isEqualTo(getDeploymentId(expectedJobDefinition));

    engineRule.getManagementService().executeJob(job.getId());

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void delayedSuspendJobDefinitionsForNonTenant() {
    // given activated job definitions

    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .executionDate(tomorrow())
      .suspend();

    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    // when execute the job to suspend the job definitions
    Job job = engineRule.getManagementService().createJobQuery().timers().singleResult();
    assertThat(job).isNotNull();
    JobDefinition expectedJobDefinition = engineRule.getManagementService().createJobDefinitionQuery()
        .active().withoutTenantId().singleResult();
    assertThat(job.getDeploymentId()).isEqualTo(getDeploymentId(expectedJobDefinition));

    engineRule.getManagementService().executeJob(job.getId());

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void delayedActivateJobDefinitionsForAllTenants() {
    // given suspend job definitions
    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .executionDate(tomorrow())
      .activate();

    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    // when execute the job to activate the job definitions
    Job job = engineRule.getManagementService().createJobQuery().timers().singleResult();
    assertThat(job).isNotNull();
    assertThat(getDeploymentIds(query.suspended())).contains(job.getDeploymentId());

    engineRule.getManagementService().executeJob(job.getId());

    assertThat(query.suspended().count()).isEqualTo(0L);
    assertThat(query.active().count()).isEqualTo(3L);
  }

  @Test
  public void delayedActivateJobDefinitionsForTenant() {
    // given suspend job definitions
    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .executionDate(tomorrow())
      .activate();

    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    // when execute the job to activate the job definitions
    Job job = engineRule.getManagementService().createJobQuery().timers().singleResult();
    assertThat(job).isNotNull();
    JobDefinition expectedJobDefinition = engineRule.getManagementService().createJobDefinitionQuery()
        .suspended().tenantIdIn(TENANT_ONE).singleResult();
    assertThat(job.getDeploymentId()).isEqualTo(getDeploymentId(expectedJobDefinition));

    engineRule.getManagementService().executeJob(job.getId());

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void delayedActivateJobDefinitionsForNonTenant() {
    // given suspend job definitions
    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .executionDate(tomorrow())
      .activate();

    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);

    // when execute the job to activate the job definitions
    Job job = engineRule.getManagementService().createJobQuery().timers().singleResult();
    assertThat(job).isNotNull();
    JobDefinition expectedJobDefinition = engineRule.getManagementService().createJobDefinitionQuery()
        .suspended().withoutTenantId().singleResult();
    assertThat(job.getDeploymentId()).isEqualTo(getDeploymentId(expectedJobDefinition));

    engineRule.getManagementService().executeJob(job.getId());

    assertThat(query.suspended().count()).isEqualTo(2L);
    assertThat(query.active().count()).isEqualTo(1L);
    assertThat(query.active().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void suspendJobDefinitionNoAuthenticatedTenants() {
    // given activated job definitions
    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    engineRule.getIdentityService().clearAuthentication();

    assertThat(query.active().count()).isEqualTo(2L);
    assertThat(query.suspended().count()).isEqualTo(1L);
    assertThat(query.suspended().withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void suspendJobDefinitionWithAuthenticatedTenant() {
    // given activated job definitions
    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
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
  public void suspendJobDefinitionDisabledTenantCheck() {
    // given activated job definitions
    JobDefinitionQuery query = engineRule.getManagementService().createJobDefinitionQuery();
    assertThat(query.active().count()).isEqualTo(3L);
    assertThat(query.suspended().count()).isEqualTo(0L);

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getManagementService()
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count()).isEqualTo(0L);
    assertThat(query.suspended().count()).isEqualTo(3L);
    assertThat(query.suspended().tenantIdIn(TENANT_ONE, TENANT_TWO).includeJobDefinitionsWithoutTenantId().count()).isEqualTo(3L);
  }

  protected Date tomorrow() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_YEAR, 1);
    return calendar.getTime();
  }

  protected List<String> getDeploymentIds(JobDefinitionQuery jobDefinitionQuery){
    return jobDefinitionQuery.list().stream().map(this::getDeploymentId).collect(Collectors.toList());
  }

  protected String getDeploymentId(JobDefinition jobDefinition) {
    return engineRule.getRepositoryService().createProcessDefinitionQuery()
      .processDefinitionId(jobDefinition.getProcessDefinitionId())
      .singleResult()
      .getDeploymentId();
  }

  @After
  public void tearDown() throws Exception {
    CommandExecutor commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerActivateJobDefinitionHandler.TYPE);
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerSuspendJobDefinitionHandler.TYPE);
        return null;
      }
    });
  }

}
