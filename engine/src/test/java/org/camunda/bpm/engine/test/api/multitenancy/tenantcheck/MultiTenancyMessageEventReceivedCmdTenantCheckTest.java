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

public class MultiTenancyMessageEventReceivedCmdTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final BpmnModelInstance MESSAGE_CATCH_PROCESS = Bpmn.createExecutableProcess("messageCatch")
      .startEvent()
      .intermediateCatchEvent()
        .message("message")
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
  public void correlateReceivedMessageToIntermediateCatchEventNoAuthenticatedTenants() {
    testRule.deploy(MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").execute();

    Execution execution = engineRule.getRuntimeService().createExecutionQuery()
      .processDefinitionKey("messageCatch")
      .messageEventSubscriptionName("message")
      .singleResult();

    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().messageEventReceived("message", execution.getId());

    engineRule.getIdentityService().clearAuthentication();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.withoutTenantId().count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(0L));
  }

  @Test
  public void correlateReceivedMessageToIntermediateCatchEventWithAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").execute();

    Execution execution = engineRule.getRuntimeService().createExecutionQuery()
      .processDefinitionKey("messageCatch")
      .messageEventSubscriptionName("message")
      .singleResult();

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    engineRule.getRuntimeService().messageEventReceived("message", execution.getId());

    engineRule.getIdentityService().clearAuthentication();

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  @Test
  public void correlateReceivedMessageToIntermediateCatchEventDisabledTenantCheck() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);
    testRule.deployForTenant(TENANT_TWO, MESSAGE_CATCH_PROCESS);

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();
    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_TWO).execute();

    Execution execution = engineRule.getRuntimeService().createExecutionQuery()
      .processDefinitionKey("messageCatch")
      .messageEventSubscriptionName("message")
      .tenantIdIn(TENANT_ONE)
      .singleResult();

    engineRule.getRuntimeService().messageEventReceived("message", execution.getId());

    TaskQuery query = engineRule.getTaskService().createTaskQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  @Test
  public void failToCorrelateReceivedMessageToIntermediateCatchEventNoAuthenticatedTenants() {
    testRule.deployForTenant(TENANT_ONE, MESSAGE_CATCH_PROCESS);

    engineRule.getRuntimeService().createProcessInstanceByKey("messageCatch").processDefinitionTenantId(TENANT_ONE).execute();

    Execution execution = engineRule.getRuntimeService().createExecutionQuery()
      .processDefinitionKey("messageCatch")
      .messageEventSubscriptionName("message")
      .tenantIdIn(TENANT_ONE)
      .singleResult();

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process instance");

    engineRule.getIdentityService().setAuthentication("user", null, null);

    engineRule.getRuntimeService().messageEventReceived("message", execution.getId());
  }

}
