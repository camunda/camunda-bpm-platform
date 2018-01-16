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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
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
    // given
    testRule.deployForTenant(TENANT_ONE, PROCESS);
    testRule.deployForTenant(TENANT_TWO, PROCESS);
    testRule.deploy(PROCESS);

    ensureEventSubscriptions(3);

    engineRule.getIdentityService().setAuthentication("user", null, null);

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("foo", "bar");

    // when
    List<ProcessInstance> instances = engineRule.getRuntimeService()
      .createConditionEvaluation()
      .setVariables(variableMap)
      .evaluateStartConditions();

    // then
    assertNotNull(instances);
    assertEquals(1, instances.size());

    engineRule.getIdentityService().clearAuthentication();

    ProcessInstanceQuery processInstanceQuery = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertEquals(1, processInstanceQuery.count());
    assertEquals(1, processInstanceQuery.withoutTenantId().count());
  }

  @Test
  public void testWithAuthenticatedTenant() throws Exception {
    // given
    testRule.deployForTenant(TENANT_ONE, PROCESS);
    testRule.deployForTenant(TENANT_TWO, PROCESS);

    ensureEventSubscriptions(2);

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("foo", "bar");

    // when
    List<ProcessInstance> processInstances = engineRule.getRuntimeService()
      .createConditionEvaluation()
      .setVariables(variableMap)
      .tenantId(TENANT_ONE)
      .evaluateStartConditions();

    // then
    assertNotNull(processInstances);
    assertEquals(1, processInstances.size());

    engineRule.getIdentityService().clearAuthentication();

    ProcessInstanceQuery processInstanceQuery = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertEquals(1, processInstanceQuery.tenantIdIn(TENANT_ONE).count());
    assertEquals(0, processInstanceQuery.tenantIdIn(TENANT_TWO).count());
  }

  @Test
  public void testDisabledTenantCheck() throws Exception {
    // given
    testRule.deployForTenant(TENANT_ONE, PROCESS);
    testRule.deployForTenant(TENANT_TWO, PROCESS);

    ensureEventSubscriptions(2);

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    engineRule.getIdentityService().setAuthentication("user", null, null);

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("foo", "bar");

    try {
      // when
      engineRule.getRuntimeService()
        .createConditionEvaluation()
        .setVariables(variableMap)
        .evaluateStartConditions();
      fail("Exception expected");
    } catch (Exception e) {
      // then
      Assert.assertTrue(e.getMessage().contains("No subscriptions were found during evaluation of the conditional start events."));
    } finally {
      engineRule.getIdentityService().clearAuthentication();
    }

    ProcessInstanceQuery processInstanceQuery = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertEquals(0, processInstanceQuery.count());
  }

  @Test
  public void testFailToEvaluateConditionByProcessDefinitionIdNoAuthenticatedTenants() {
    // given
    testRule.deployForTenant(TENANT_ONE, PROCESS);

    ensureEventSubscriptions(1);

    ProcessDefinition processDefinition = engineRule.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("conditionStart").singleResult();

    // expected
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot create an instance of the process definition");

    engineRule.getIdentityService().setAuthentication("user", null, null);

    // when
    engineRule.getRuntimeService()
      .createConditionEvaluation()
      .setVariable("foo", "bar")
      .processDefinitionId(processDefinition.getId())
      .evaluateStartConditions();
  }

  @Test
  public void testEvaluateConditionByProcessDefinitionIdWithAuthenticatedTenants() {
    // given
    testRule.deployForTenant(TENANT_ONE, PROCESS);

    ensureEventSubscriptions(1);

    ProcessDefinition processDefinition = engineRule.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("conditionStart").singleResult();

    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    List<ProcessInstance> instances = engineRule.getRuntimeService()
      .createConditionEvaluation()
      .setVariable("foo", "bar")
      .tenantId(TENANT_ONE)
      .processDefinitionId(processDefinition.getId())
      .evaluateStartConditions();

    // then
    assertNotNull(instances);
    assertEquals(1, instances.size());
    assertEquals(TENANT_ONE, instances.get(0).getTenantId());

    engineRule.getIdentityService().clearAuthentication();

    ProcessInstanceQuery processInstanceQuery = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertEquals(1, processInstanceQuery.tenantIdIn(TENANT_ONE).count());

    EventSubscription eventSubscription = engineRule.getRuntimeService().createEventSubscriptionQuery().singleResult();
    assertEquals(EventType.CONDITONAL.name(), eventSubscription.getEventType());
  }

  protected void ensureEventSubscriptions(int count) {
    List<EventSubscription> eventSubscriptions = engineRule.getRuntimeService().createEventSubscriptionQuery().list();
    assertEquals(count, eventSubscriptions.size());
    for (EventSubscription eventSubscription : eventSubscriptions) {
      assertEquals(EventType.CONDITONAL.name(), eventSubscription.getEventType());
    }
  }
}
