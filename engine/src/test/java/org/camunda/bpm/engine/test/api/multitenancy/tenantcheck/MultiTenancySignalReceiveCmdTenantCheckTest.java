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

package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

  @Rule
  public ExpectedException thrown= ExpectedException.none();

  @Test
  public void sendSignalToStartEventNoAuthenticatedTenants() {
    testRule.deploy(SIGNAL_START_PROCESS);
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS);

    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().createSignalEvent("signal").send();

    engineRule.getIdentityService().clearAuthentication();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.withoutTenantId().count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(0L));
  }

  @Test
  public void sendSignalToStartEventWithAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_START_PROCESS);

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    engineRule.getRuntimeService().createSignalEvent("signal").send();

    engineRule.getIdentityService().clearAuthentication();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  @Test
  public void sendSignalToStartEventDisabledTenantCheck() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_START_PROCESS);

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().createSignalEvent("signal").send();

    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  @Test
  public void sendSignalToIntermediateCatchEventNoAuthenticatedTenants() {
    testRule.deploy(SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").processDefinitionWithoutTenantId().execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();

    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().createSignalEvent("signal").send();

    engineRule.getIdentityService().clearAuthentication();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.withoutTenantId().count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(0L));
  }

  @Test
  public void sendSignalToIntermediateCatchEventWithAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    engineRule.getRuntimeService().createSignalEvent("signal").send();

    engineRule.getIdentityService().clearAuthentication();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  @Test
  public void sendSignalToIntermediateCatchEventDisabledTenantCheck() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().createSignalEvent("signal").send();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  @Test
  public void sendSignalToStartAndIntermediateCatchEventNoAuthenticatedTenants() {
    testRule.deploy(SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").processDefinitionWithoutTenantId().execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();

    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().createSignalEvent("signal").send();

    engineRule.getIdentityService().clearAuthentication();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(2L));
    assertThat(query.withoutTenantId().count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(0L));
  }

  @Test
  public void sendSignalToStartAndIntermediateCatchEventWithAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    engineRule.getRuntimeService().createSignalEvent("signal").send();

    engineRule.getIdentityService().clearAuthentication();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  @Test
  public void sendSignalToStartAndIntermediateCatchEventDisabledTenantCheck() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().createSignalEvent("signal").send();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(4L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(2L));
  }

  @Test
  public void sendSignalToIntermediateCatchEventWithExecutionIdAndAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();

    Execution execution = engineRule.getRuntimeService().createExecutionQuery()
      .processDefinitionKey("signalCatch")
      .signalEventSubscriptionName("signal")
      .singleResult();

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    engineRule.getRuntimeService().createSignalEvent("signal").executionId(execution.getId()).send();

    engineRule.getIdentityService().clearAuthentication();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  @Test
  public void failToSendSignalToIntermediateCatchEventWithExecutionIdAndNoAuthenticatedTenants() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();

    Execution execution = engineRule.getRuntimeService().createExecutionQuery()
      .processDefinitionKey("signalCatch")
      .signalEventSubscriptionName("signal")
      .singleResult();

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process instance");

    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().createSignalEvent("signal").executionId(execution.getId()).send();
  }

  @Test
  public void signalIntermediateCatchEventNoAuthenticatedTenants() {
    testRule.deploy(SIGNAL_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").execute();

    Execution execution = engineRule.getRuntimeService().createExecutionQuery()
      .processDefinitionKey("signalCatch")
      .signalEventSubscriptionName("signal")
      .singleResult();

    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().signal(execution.getId(), "signal", null, null);

    engineRule.getIdentityService().clearAuthentication();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.withoutTenantId().count(), is(1L));
  }

  @Test
  public void signalIntermediateCatchEventWithAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();

    Execution execution = engineRule.getRuntimeService().createExecutionQuery()
      .processDefinitionKey("signalCatch")
      .signalEventSubscriptionName("signal")
      .singleResult();

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    engineRule.getRuntimeService().signal(execution.getId(), "signal", null, null);

    engineRule.getIdentityService().clearAuthentication();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  @Test
  public void signalIntermediateCatchEventDisabledTenantCheck() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, SIGNAL_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    Execution execution = engineRule.getRuntimeService().createExecutionQuery()
      .processDefinitionKey("signalCatch")
      .signalEventSubscriptionName("signal")
      .tenantIdIn(TENANT_ONE)
      .singleResult();

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().signal(execution.getId(), "signal", null, null);

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  @Test
  public void failToSignalIntermediateCatchEventNoAuthenticatedTenants() {
    testRule.deployForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("signalCatch").execute();

    Execution execution = engineRule.getRuntimeService().createExecutionQuery()
      .processDefinitionKey("signalCatch")
      .signalEventSubscriptionName("signal")
      .singleResult();

    // declared expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process instance");

    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().signal(execution.getId(), "signal", null, null);
  }
}
