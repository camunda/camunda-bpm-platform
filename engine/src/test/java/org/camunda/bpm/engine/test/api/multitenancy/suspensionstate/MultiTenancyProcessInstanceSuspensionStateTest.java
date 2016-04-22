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

package org.camunda.bpm.engine.test.api.multitenancy.suspensionstate;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyProcessInstanceSuspensionStateTest extends PluggableProcessEngineTestCase {

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

  @Override
  protected void setUp() throws Exception {

    deploymentForTenant(TENANT_ONE, PROCESS);
    deploymentForTenant(TENANT_TWO, PROCESS);
    deployment(PROCESS);

    runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).processDefinitionTenantId(TENANT_TWO).execute();
    runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).processDefinitionWithoutTenantId().execute();
  }

  public void testSuspendAndActivateProcessInstancesForAllTenants() {
    // given activated process instances
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    // first suspend
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count(), is(0L));
    assertThat(query.suspended().count(), is(3L));

    // then activate
    runtimeService
    .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .activate();

    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));
  }

  public void testSuspendProcessInstanceForTenant() {
    // given activated process instances
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .suspend();

    assertThat(query.active().count(), is(2L));
    assertThat(query.suspended().count(), is(1L));
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testSuspendProcessInstanceForNonTenant() {
    // given activated process instances
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .suspend();

    assertThat(query.active().count(), is(2L));
    assertThat(query.suspended().count(), is(1L));
    assertThat(query.suspended().withoutTenantId().count(), is(1L));
  }

  public void testActivateProcessInstanceForTenant() {
    // given suspended process instances
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.active().count(), is(0L));
    assertThat(query.suspended().count(), is(3L));

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .activate();

    assertThat(query.suspended().count(), is(2L));
    assertThat(query.active().count(), is(1L));
    assertThat(query.active().tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testActivateProcessInstanceForNonTenant() {
    // given suspended process instances
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.active().count(), is(0L));
    assertThat(query.suspended().count(), is(3L));

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .activate();

    assertThat(query.suspended().count(), is(2L));
    assertThat(query.active().count(), is(1L));
    assertThat(query.active().withoutTenantId().count(), is(1L));
  }

  public void testSuspendAndActivateProcessInstancesIncludingUserTasksForAllTenants() {
    // given activated user tasks
    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    // first suspend
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count(), is(0L));
    assertThat(query.suspended().count(), is(3L));

    // then activate
    runtimeService
    .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .activate();

    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));
  }

  public void testSuspendProcessInstanceIncludingUserTaskForTenant() {
    // given activated user tasks
    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .suspend();

    assertThat(query.active().count(), is(2L));
    assertThat(query.suspended().count(), is(1L));
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testSuspendProcessInstanceIncludingUserTaskForNonTenant() {
    // given activated user tasks
    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .suspend();

    assertThat(query.active().count(), is(2L));
    assertThat(query.suspended().count(), is(1L));
    assertThat(query.suspended().withoutTenantId().count(), is(1L));
  }

  public void testActivateProcessInstanceIncludingUserTaskForTenant() {
    // given suspended user tasks
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.active().count(), is(0L));
    assertThat(query.suspended().count(), is(3L));

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .activate();

    assertThat(query.suspended().count(), is(2L));
    assertThat(query.active().count(), is(1L));
    assertThat(query.active().tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testActivateProcessInstanceIncludingUserTaskForNonTenant() {
    // given suspended user tasks
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.active().count(), is(0L));
    assertThat(query.suspended().count(), is(3L));

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .activate();

    assertThat(query.suspended().count(), is(2L));
    assertThat(query.active().count(), is(1L));
    assertThat(query.active().withoutTenantId().count(), is(1L));
  }

  public void testSuspendAndActivateProcessInstancesIncludingExternalTasksForAllTenants() {
    // given activated external tasks
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    // first suspend
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count(), is(0L));
    assertThat(query.suspended().count(), is(3L));

    // then activate
    runtimeService
    .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .activate();

    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));
  }

  public void testSuspendProcessInstanceIncludingExternalTaskForTenant() {
    // given activated external tasks
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .suspend();

    assertThat(query.active().count(), is(2L));
    assertThat(query.suspended().count(), is(1L));
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testSuspendProcessInstanceIncludingExternalTaskForNonTenant() {
    // given activated external tasks
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .suspend();

    assertThat(query.active().count(), is(2L));
    assertThat(query.suspended().count(), is(1L));
    assertThat(query.suspended().singleResult().getTenantId(), is(nullValue()));
  }

  public void testActivateProcessInstanceIncludingExternalTaskForTenant() {
    // given suspended external tasks
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();
    assertThat(query.active().count(), is(0L));
    assertThat(query.suspended().count(), is(3L));

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .activate();

    assertThat(query.suspended().count(), is(2L));
    assertThat(query.active().count(), is(1L));
    assertThat(query.active().tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testActivateProcessInstanceIncludingExternalTaskForNonTenant() {
    // given suspended external tasks
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();
    assertThat(query.active().count(), is(0L));
    assertThat(query.suspended().count(), is(3L));

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .activate();

    assertThat(query.suspended().count(), is(2L));
    assertThat(query.active().count(), is(1L));
    assertThat(query.active().singleResult().getTenantId(), is(nullValue()));
  }

  public void testSuspendAndActivateProcessInstancesIncludingJobsForAllTenants() {
    // given activated jobs
    JobQuery query = managementService.createJobQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    // first suspend
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count(), is(0L));
    assertThat(query.suspended().count(), is(3L));

    // then activate
    runtimeService
    .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .activate();

    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));
  }

  public void testSuspendProcessInstanceIncludingJobForTenant() {
    // given activated jobs
    JobQuery query = managementService.createJobQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .suspend();

    assertThat(query.active().count(), is(2L));
    assertThat(query.suspended().count(), is(1L));
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testSuspendProcessInstanceIncludingJobForNonTenant() {
    // given activated jobs
    JobQuery query = managementService.createJobQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .suspend();

    assertThat(query.active().count(), is(2L));
    assertThat(query.suspended().count(), is(1L));
    assertThat(query.suspended().singleResult().getTenantId(), is(nullValue()));
  }

  public void testActivateProcessInstanceIncludingJobForTenant() {
    // given suspended job
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    JobQuery query = managementService.createJobQuery();
    assertThat(query.active().count(), is(0L));
    assertThat(query.suspended().count(), is(3L));

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .activate();

    assertThat(query.suspended().count(), is(2L));
    assertThat(query.active().count(), is(1L));
    assertThat(query.active().tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testActivateProcessInstanceIncludingJobForNonTenant() {
    // given suspended jobs
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    JobQuery query = managementService.createJobQuery();
    assertThat(query.active().count(), is(0L));
    assertThat(query.suspended().count(), is(3L));

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .activate();

    assertThat(query.suspended().count(), is(2L));
    assertThat(query.active().count(), is(1L));
    assertThat(query.active().singleResult().getTenantId(), is(nullValue()));
  }

  public void testSuspendProcessInstanceNoAuthenticatedTenants() {
    // given activated process instances
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    identityService.setAuthentication("user", null, null);

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    identityService.clearAuthentication();

    assertThat(query.active().count(), is(2L));
    assertThat(query.suspended().count(), is(1L));
    assertThat(query.suspended().withoutTenantId().count(), is(1L));
  }

  public void testFailToSuspendProcessInstanceByProcessDefinitionIdNoAuthenticatedTenants() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY).tenantIdIn(TENANT_ONE).singleResult();

    identityService.setAuthentication("user", null, null);

    try {
      runtimeService
        .updateProcessInstanceSuspensionState()
        .byProcessDefinitionId(processDefinition.getId())
        .suspend();

        fail("expected exception");
    } catch(ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("Cannot update the suspension state of an instance of the process definition"));
    }
  }

  public void testSuspendProcessInstanceWithAuthenticatedTenant() {
    // given activated process instances
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    identityService.clearAuthentication();

    assertThat(query.active().count(), is(1L));
    assertThat(query.suspended().count(), is(2L));
    assertThat(query.active().tenantIdIn(TENANT_TWO).count(), is(1L));
    assertThat(query.suspended().withoutTenantId().count(), is(1L));
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testSuspendProcessInstanceDisabledTenantCheck() {
    // given activated process instances
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count(), is(0L));
    assertThat(query.suspended().count(), is(3L));
    assertThat(query.suspended().tenantIdIn(TENANT_ONE, TENANT_TWO).count(), is(2L));
    assertThat(query.suspended().withoutTenantId().count(), is(1L));
  }
}
