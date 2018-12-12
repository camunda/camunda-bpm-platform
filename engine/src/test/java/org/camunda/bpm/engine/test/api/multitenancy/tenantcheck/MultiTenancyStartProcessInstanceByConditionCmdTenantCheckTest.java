/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
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

  public IdentityService identityService;
  public RepositoryService repositoryService;
  public RuntimeService runtimeService;

  @Before
  public void setUp() {
    identityService = engineRule.getIdentityService();
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();
  }

  @Test
  public void testNoAuthenticatedTenants() throws Exception {
    // given
    testRule.deployForTenant(TENANT_ONE, PROCESS);
    testRule.deployForTenant(TENANT_TWO, PROCESS);
    testRule.deploy(PROCESS);

    ensureEventSubscriptions(3);

    identityService.setAuthentication("user", null, null);

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

    identityService.clearAuthentication();

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

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

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

    identityService.clearAuthentication();

    ProcessInstanceQuery processInstanceQuery = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertEquals(1, processInstanceQuery.tenantIdIn(TENANT_ONE).count());
    assertEquals(0, processInstanceQuery.tenantIdIn(TENANT_TWO).count());
  }

  @Test
  public void testWithAuthenticatedTenant2() throws Exception {
    // given
    testRule.deployForTenant(TENANT_ONE, PROCESS);
    testRule.deployForTenant(TENANT_TWO, PROCESS);

    ensureEventSubscriptions(2);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("foo", "bar");

    // when
    List<ProcessInstance> processInstances = engineRule.getRuntimeService()
      .createConditionEvaluation()
      .setVariables(variableMap)
      .evaluateStartConditions();

    // then
    assertNotNull(processInstances);
    assertEquals(1, processInstances.size());

    identityService.clearAuthentication();

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
    identityService.setAuthentication("user", null, null);

    System.out.println(engineRule.getProcessEngineConfiguration().isAuthorizationEnabled());

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("foo", "bar");

    // when
    List<ProcessInstance> evaluateStartConditions = engineRule.getRuntimeService()
      .createConditionEvaluation()
      .setVariables(variableMap)
      .evaluateStartConditions();
    assertEquals(2, evaluateStartConditions.size());

    identityService.clearAuthentication();
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

    identityService.setAuthentication("user", null, null);

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

    identityService = engineRule.getIdentityService();
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

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

    identityService.clearAuthentication();

    ProcessInstanceQuery processInstanceQuery = engineRule.getRuntimeService().createProcessInstanceQuery();
    assertEquals(1, processInstanceQuery.tenantIdIn(TENANT_ONE).count());

    EventSubscription eventSubscription = engineRule.getRuntimeService().createEventSubscriptionQuery().singleResult();
    assertEquals(EventType.CONDITONAL.name(), eventSubscription.getEventType());
  }

  @Test
  public void testSubscriptionsWhenDeletingGroupsProcessDefinitionsByIds() {
    // given
    String processDefId1 = testRule.deployForTenantAndGetDefinition(TENANT_ONE, PROCESS).getId();
    String processDefId2 = testRule.deployForTenantAndGetDefinition(TENANT_ONE, PROCESS).getId();
    String processDefId3 = testRule.deployForTenantAndGetDefinition(TENANT_ONE, PROCESS).getId();

    @SuppressWarnings("unused")
    String processDefId4 = testRule.deployAndGetDefinition(PROCESS).getId();
    String processDefId5 = testRule.deployAndGetDefinition(PROCESS).getId();
    String processDefId6 = testRule.deployAndGetDefinition(PROCESS).getId();

    BpmnModelInstance processAnotherKey = Bpmn.createExecutableProcess("anotherKey")
        .startEvent()
          .conditionalEventDefinition()
            .condition("${true}")
          .conditionalEventDefinitionDone()
        .userTask()
        .endEvent()
        .done();

    String processDefId7 = testRule.deployForTenantAndGetDefinition(TENANT_ONE, processAnotherKey).getId();
    String processDefId8 = testRule.deployForTenantAndGetDefinition(TENANT_ONE, processAnotherKey).getId();
    String processDefId9 = testRule.deployForTenantAndGetDefinition(TENANT_ONE, processAnotherKey).getId();

    // assume
    assertEquals(3, runtimeService.createEventSubscriptionQuery().count());

    // when
    repositoryService.deleteProcessDefinitions()
                     .byIds(processDefId8, processDefId5, processDefId3, processDefId9, processDefId1)
                     .delete();

    // then
    List<EventSubscription> list = runtimeService.createEventSubscriptionQuery().list();
    assertEquals(3, list.size());
    for (EventSubscription eventSubscription : list) {
      EventSubscriptionEntity eventSubscriptionEntity = (EventSubscriptionEntity) eventSubscription;
      if (eventSubscriptionEntity.getConfiguration().equals(processDefId2)) {
        assertEquals(TENANT_ONE, eventSubscription.getTenantId());
      } else if (eventSubscriptionEntity.getConfiguration().equals(processDefId6)) {
        assertEquals(null, eventSubscription.getTenantId());
      } else if (eventSubscriptionEntity.getConfiguration().equals(processDefId7)) {
        assertEquals(TENANT_ONE, eventSubscription.getTenantId());
      } else {
        fail("This process definition '" + eventSubscriptionEntity.getConfiguration() + "' and the respective event subscription should not exist.");
      }
    }
  }

  protected void ensureEventSubscriptions(int count) {
    List<EventSubscription> eventSubscriptions = engineRule.getRuntimeService().createEventSubscriptionQuery().list();
    assertEquals(count, eventSubscriptions.size());
    for (EventSubscription eventSubscription : eventSubscriptions) {
      assertEquals(EventType.CONDITONAL.name(), eventSubscription.getEventType());
    }
  }
}
