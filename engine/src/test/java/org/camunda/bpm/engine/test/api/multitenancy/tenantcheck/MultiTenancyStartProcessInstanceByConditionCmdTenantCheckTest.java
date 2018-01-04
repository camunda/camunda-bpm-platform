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
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class MultiTenancyStartProcessInstanceByConditionCmdTenantCheckTest {
  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final BpmnModelInstance PROCESS = Bpmn.createExecutableProcess("conditionStart")
      .startEvent()
        .conditionalEventDefinition()
          .condition("${true}")
        .conditionalEventDefinitionDone()
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
  public void testNoAuthenticatedTenants() throws Exception {
    testRule.deployForTenant(TENANT_ONE, PROCESS);
    testRule.deployForTenant(TENANT_TWO, PROCESS);
    testRule.deploy(PROCESS);

    engineRule.getIdentityService().setAuthentication("user", null, null);

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("foo", "bar");

    engineRule.getRuntimeService()
      .createConditionCorrelation()
      .setVariables(variableMap)
      .correlateStartConditions();

    engineRule.getIdentityService().clearAuthentication();

    ProcessInstanceQuery processInstanceQuery = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(processInstanceQuery.count(), is(1L));
    assertThat(processInstanceQuery.withoutTenantId().count(), is(1L));

    List<EventSubscription> eventSubscriptions = engineRule.getRuntimeService().createEventSubscriptionQuery().list();
    assertEquals(3, eventSubscriptions.size());
    for (EventSubscription eventSubscription : eventSubscriptions) {
      assertEquals(EventType.CONDITONAL.name(), eventSubscription.getEventType());
    }
  }

  @Test
  public void testWithAuthenticatedTenant() throws Exception {
    testRule.deployForTenant(TENANT_ONE, PROCESS);
    testRule.deployForTenant(TENANT_TWO, PROCESS);

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("foo", "bar");

    engineRule.getRuntimeService()
      .createConditionCorrelation()
      .setVariables(variableMap)
      .tenantId(TENANT_ONE)
      .correlateStartConditions();

    engineRule.getIdentityService().clearAuthentication();

    ProcessInstanceQuery processInstanceQuery = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(processInstanceQuery.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(processInstanceQuery.tenantIdIn(TENANT_TWO).count(), is(0L));

    List<EventSubscription> eventSubscriptions = engineRule.getRuntimeService().createEventSubscriptionQuery().list();
    assertEquals(2, eventSubscriptions.size());
    for (EventSubscription eventSubscription : eventSubscriptions) {
      assertEquals(EventType.CONDITONAL.name(), eventSubscription.getEventType());
    }
  }

  @Test
  public void testDisabledTenantCheck() throws Exception {
    testRule.deployForTenant(TENANT_ONE, PROCESS);
    testRule.deployForTenant(TENANT_TWO, PROCESS);

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    engineRule.getIdentityService().setAuthentication("user", null, null);

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("foo", "bar");

    try {
      engineRule.getRuntimeService()
        .createConditionCorrelation()
        .setVariables(variableMap)
        .correlateStartConditions();
      fail("Exception expected");
    } catch (Exception e) {
      Assert.assertTrue(e.getMessage().contains("No subscriptions were found during correlation of the conditional start events."));
    } finally {
      engineRule.getIdentityService().clearAuthentication();
    }

    ProcessInstanceQuery processInstanceQuery = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertThat(processInstanceQuery.count(), is(0L));

    List<EventSubscription> eventSubscriptions = engineRule.getRuntimeService().createEventSubscriptionQuery().list();
    assertEquals(2, eventSubscriptions.size());
    for (EventSubscription eventSubscription : eventSubscriptions) {
      assertEquals(EventType.CONDITONAL.name(), eventSubscription.getEventType());
    }
  }
}
