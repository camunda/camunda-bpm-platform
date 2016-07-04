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

import java.util.Arrays;

import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MultiTenancyJobSuspensionStateTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String PROCESS_DEFINITION_KEY = "testProcess";

  protected static final BpmnModelInstance PROCESS = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent()
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
  public void suspendAndActivateJobsForAllTenants() {
    // given activated jobs
    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    // first suspend
    engineRule.getManagementService()
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count(), is(0L));
    assertThat(query.suspended().count(), is(3L));

    // then activate
    engineRule.getManagementService()
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .activate();

    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));
  }

  @Test
  public void suspendJobForTenant() {
    // given activated jobs
    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    engineRule.getManagementService()
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .suspend();

    assertThat(query.active().count(), is(2L));
    assertThat(query.suspended().count(), is(1L));
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  @Test
  public void suspendJobsForNonTenant() {
    // given activated jobs
    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    engineRule.getManagementService()
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .suspend();

    assertThat(query.active().count(), is(2L));
    assertThat(query.suspended().count(), is(1L));
    assertThat(query.suspended().withoutTenantId().count(), is(1L));
  }

  @Test
  public void activateJobsForTenant() {
    // given suspend jobs
    engineRule.getManagementService()
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.suspended().count(), is(3L));
    assertThat(query.active().count(), is(0L));

    engineRule.getManagementService()
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionTenantId(TENANT_ONE)
      .activate();

    assertThat(query.suspended().count(), is(2L));
    assertThat(query.active().count(), is(1L));
    assertThat(query.active().tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  @Test
  public void activateJobsForNonTenant() {
    // given suspend jobs
    engineRule.getManagementService()
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.suspended().count(), is(3L));
    assertThat(query.active().count(), is(0L));

    engineRule.getManagementService()
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .processDefinitionWithoutTenantId()
      .activate();

    assertThat(query.suspended().count(), is(2L));
    assertThat(query.active().count(), is(1L));
    assertThat(query.active().withoutTenantId().count(), is(1L));
  }

  @Test
  public void suspendJobNoAuthenticatedTenants() {
    // given activated jobs
    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getManagementService()
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    engineRule.getIdentityService().clearAuthentication();

    assertThat(query.active().count(), is(2L));
    assertThat(query.suspended().count(), is(1L));
    assertThat(query.suspended().withoutTenantId().count(), is(1L));
  }

  @Test
  public void suspendJobWithAuthenticatedTenant() {
    // given activated jobs
    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    engineRule.getManagementService()
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    engineRule.getIdentityService().clearAuthentication();

    assertThat(query.active().count(), is(1L));
    assertThat(query.suspended().count(), is(2L));
    assertThat(query.active().tenantIdIn(TENANT_TWO).count(), is(1L));
    assertThat(query.suspended().withoutTenantId().count(), is(1L));
    assertThat(query.suspended().tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  @Test
  public void suspendJobDisabledTenantCheck() {
    // given activated jobs
    JobQuery query = engineRule.getManagementService().createJobQuery();
    assertThat(query.active().count(), is(3L));
    assertThat(query.suspended().count(), is(0L));

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getManagementService()
      .updateJobSuspensionState()
      .byProcessDefinitionKey(PROCESS_DEFINITION_KEY)
      .suspend();

    assertThat(query.active().count(), is(0L));
    assertThat(query.suspended().count(), is(3L));
    assertThat(query.suspended().tenantIdIn(TENANT_ONE, TENANT_TWO).includeJobsWithoutTenantId().count(), is(3L));
  }

}
