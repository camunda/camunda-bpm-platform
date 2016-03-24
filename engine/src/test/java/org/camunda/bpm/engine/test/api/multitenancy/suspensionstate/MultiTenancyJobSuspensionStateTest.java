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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyJobSuspensionStateTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String PROCESS_DEFINITION_KEY = "testProcess";

  protected static final BpmnModelInstance PROCESS = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent()
        .camundaAsyncBefore()
      .endEvent()
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

  public void testSuspendAndActivateJobsForAllTenants() {
    // given activated jobs
    JobQuery query = managementService.createJobQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    // first suspend
    managementService
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count(), is(0L));
    assertThat(query.suspended().count(), is(3L));

    // then activate
    managementService
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .activate();

    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));
  }

  public void testSuspendJobForTenant() {
    // given activated jobs
    JobQuery query = managementService.createJobQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    managementService
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .suspend();

    assertThat(query.active().count(), is(2L));
    assertThat(query.suspended().count(), is(1L));
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testSuspendJobsForNonTenant() {
    // given activated jobs
    JobQuery query = managementService.createJobQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    managementService
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .suspend();

    assertThat(query.active().count(), is(2L));
    assertThat(query.suspended().count(), is(1L));
    assertThat(query.suspended().withoutTenantId().count(), is(1L));
  }

  public void testActivateJobsForTenant() {
    // given suspend jobs
    managementService
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    JobQuery query = managementService.createJobQuery();
    assertThat(query.suspended().count(), is(3L));
    assertThat(query.active().count(), is(0L));

    managementService
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .activate();

    assertThat(query.suspended().count(), is(2L));
    assertThat(query.active().count(), is(1L));
    assertThat(query.active().tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testActivateJobsForNonTenant() {
    // given suspend jobs
    managementService
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    JobQuery query = managementService.createJobQuery();
    assertThat(query.suspended().count(), is(3L));
    assertThat(query.active().count(), is(0L));

    managementService
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .activate();

    assertThat(query.suspended().count(), is(2L));
    assertThat(query.active().count(), is(1L));
    assertThat(query.active().withoutTenantId().count(), is(1L));
  }

}
