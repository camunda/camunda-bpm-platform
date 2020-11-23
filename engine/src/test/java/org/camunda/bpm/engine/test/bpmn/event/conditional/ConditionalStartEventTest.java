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
package org.camunda.bpm.engine.test.bpmn.event.conditional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class ConditionalStartEventTest {

  private static final String SINGLE_CONDITIONAL_START_EVENT_XML = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml";
  private static final String SINGLE_CONDITIONAL_XML = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent1.bpmn20.xml";
  private static final String TRUE_CONDITION_START_XML = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testStartInstanceWithTrueConditionalStartEvent.bpmn20.xml";
  private static final String TWO_EQUAL_CONDITIONAL_START_EVENT_XML = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testTwoEqualConditionalStartEvent.bpmn20.xml";
  private static final String MULTIPLE_CONDITION_XML = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testMultipleCondition.bpmn20.xml";
  private static final String START_INSTANCE_WITH_VARIABLE_NAME_XML = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testStartInstanceWithVariableName.bpmn20.xml";
  private static final String ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";

  private static final String MULTIPLE_CONDITIONS = "multipleConditions";
  private static final String TRUE_CONDITION_PROCESS = "trueConditionProcess";
  private static final String CONDITIONAL_EVENT_PROCESS = "conditionalEventProcess";

  private static final BpmnModelInstance MODEL_WITHOUT_CONDITION = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS)
      .startEvent()
      .userTask()
      .endEvent()
      .done();

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;

  @Before
  public void setUp() throws Exception {
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();
  }

  @Test
  @Deployment(resources = SINGLE_CONDITIONAL_START_EVENT_XML)
  public void testDeploymentCreatesSubscriptions() {
    // given a deployed process
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().processDefinitionKey(CONDITIONAL_EVENT_PROCESS).singleResult().getId();

    // when
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    // then
    assertEquals(1, eventSubscriptions.size());
    EventSubscriptionEntity conditionalEventSubscription = (EventSubscriptionEntity) eventSubscriptions.get(0);
    assertEquals(EventType.CONDITONAL.name(), conditionalEventSubscription.getEventType());
    assertEquals(processDefinitionId, conditionalEventSubscription.getConfiguration());
    assertNull(conditionalEventSubscription.getEventName());
    assertNull(conditionalEventSubscription.getExecutionId());
    assertNull(conditionalEventSubscription.getProcessInstanceId());
  }

  @Test
  @Deployment(resources = SINGLE_CONDITIONAL_START_EVENT_XML)
  public void testUpdateProcessVersionCancelsSubscriptions() {
    // given a deployed process
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();

    assertEquals(1, eventSubscriptions.size());
    assertEquals(1, processDefinitions.size());

    // when
    testRule.deploy(SINGLE_CONDITIONAL_START_EVENT_XML);

    // then
    List<EventSubscription> newEventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
    List<ProcessDefinition> newProcessDefinitions = repositoryService.createProcessDefinitionQuery().list();

    assertEquals(1, newEventSubscriptions.size());
    assertEquals(2, newProcessDefinitions.size());
    for (ProcessDefinition processDefinition : newProcessDefinitions) {
      if (processDefinition.getVersion() == 1) {
        for (EventSubscription subscription : newEventSubscriptions) {
          EventSubscriptionEntity subscriptionEntity = (EventSubscriptionEntity) subscription;
          assertFalse(subscriptionEntity.getConfiguration().equals(processDefinition.getId()));
        }
      } else {
        for (EventSubscription subscription : newEventSubscriptions) {
          EventSubscriptionEntity subscriptionEntity = (EventSubscriptionEntity) subscription;
          assertTrue(subscriptionEntity.getConfiguration().equals(processDefinition.getId()));
        }
      }
    }
    assertFalse(eventSubscriptions.equals(newEventSubscriptions));
  }

  @Test
  @Deployment(resources = SINGLE_CONDITIONAL_START_EVENT_XML)
  public void testEventSubscriptionAfterDeleteLatestProcessVersion() {
    // given a deployed process
    ProcessDefinition processDefinitionV1 = repositoryService.createProcessDefinitionQuery().singleResult();
    assertNotNull(processDefinitionV1);

    // deploy second version of the process
    String deploymentId = testRule.deploy(SINGLE_CONDITIONAL_XML).getId();

    // when
    repositoryService.deleteDeployment(deploymentId, true);

    // then
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(CONDITIONAL_EVENT_PROCESS).singleResult();
    assertEquals(processDefinitionV1.getId(), processDefinition.getId());

    EventSubscriptionEntity eventSubscription = (EventSubscriptionEntity) runtimeService.createEventSubscriptionQuery().singleResult();
    assertNotNull(eventSubscription);
    assertEquals(processDefinitionV1.getId(), eventSubscription.getConfiguration());
  }

  @Test
  @Deployment(resources = SINGLE_CONDITIONAL_START_EVENT_XML)
  public void testStartInstanceAfterDeleteLatestProcessVersionByIds() {
    // given a deployed process

    // deploy second version of the process
    DeploymentWithDefinitions deployment = testRule.deploy(SINGLE_CONDITIONAL_XML);
    ProcessDefinition processDefinition = deployment.getDeployedProcessDefinitions().get(0);

    // delete it
    repositoryService.deleteProcessDefinitions()
      .byIds(processDefinition.getId())
      .delete();

    // when
    List<ProcessInstance> conditionInstances = runtimeService
        .createConditionEvaluation()
        .setVariable("foo", 1)
        .evaluateStartConditions();

    // then
    assertEquals(1, conditionInstances.size());
    assertNotNull(conditionInstances.get(0));
  }

  @Test
  @Deployment(resources = SINGLE_CONDITIONAL_START_EVENT_XML)
  public void testStartInstanceAfterDeleteLatestProcessVersion() {
    // given a deployed process

    // deploy second version of the process
    String deploymentId = testRule.deploy(SINGLE_CONDITIONAL_XML).getId();
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();

    // delete it
    repositoryService.deleteDeployment(deployment.getId(), true);

    // when
    List<ProcessInstance> conditionInstances = runtimeService
        .createConditionEvaluation()
        .setVariable("foo", 1)
        .evaluateStartConditions();

    // then
    assertEquals(1, conditionInstances.size());
    assertNotNull(conditionInstances.get(0));
  }

  @Test
  public void testVersionWithoutConditionAfterDeleteLatestProcessVersionWithCondition() {
    // given a process
    testRule.deploy(MODEL_WITHOUT_CONDITION);

    // deploy second version of the process
    String deploymentId = testRule.deploy(SINGLE_CONDITIONAL_XML).getId();
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();

    // delete it
    repositoryService.deleteDeployment(deployment.getId(), true);

    // when/then
    assertThatThrownBy(() -> runtimeService
        .createConditionEvaluation()
        .setVariable("foo", 1)
        .evaluateStartConditions())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("No subscriptions were found during evaluation of the conditional start events.");

  }

  @Test
  public void testSubscriptionsWhenDeletingProcessDefinitionsInOneTransactionByKeys() {
    // given three versions of the process
    testRule.deploy(SINGLE_CONDITIONAL_XML);
    testRule.deploy(SINGLE_CONDITIONAL_XML);
    testRule.deploy(SINGLE_CONDITIONAL_XML);

    // when
    repositoryService.deleteProcessDefinitions()
      .byKey(CONDITIONAL_EVENT_PROCESS)
      .delete();

    // then
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());
  }

  @Test
  public void testSubscriptionsWhenDeletingGroupsProcessDefinitionsByIds() {
    // given
    String processDefId1 = deployProcess(SINGLE_CONDITIONAL_XML);
    String processDefId2 = deployProcess(SINGLE_CONDITIONAL_XML);
    String processDefId3 = deployModel(MODEL_WITHOUT_CONDITION); // with the same process definition key

    String processDefId4 = deployProcess(TRUE_CONDITION_START_XML);
    String processDefIа5 = deployProcess(TRUE_CONDITION_START_XML);
    String processDefId6 = deployProcess(TRUE_CONDITION_START_XML);

    // two versions of a process without conditional start event
    String processDefId7 = deployProcess(ONE_TASK_PROCESS);
    @SuppressWarnings("unused")
    String processDefId8 = deployProcess(ONE_TASK_PROCESS);

    // assume
    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());

    // when
    repositoryService.deleteProcessDefinitions()
        .byIds(processDefId4, processDefId6, processDefId3, processDefId2, processDefId7)
      .delete();

    // then
    List<EventSubscription> list = runtimeService.createEventSubscriptionQuery().list();
    assertEquals(2, list.size());
    for (EventSubscription eventSubscription : list) {
      EventSubscriptionEntity eventSubscriptionEntity = (EventSubscriptionEntity) eventSubscription;
      if (!eventSubscriptionEntity.getConfiguration().equals(processDefId1)
       && !eventSubscriptionEntity.getConfiguration().equals(processDefIа5)) {
        fail("This process definition '" + eventSubscriptionEntity.getConfiguration() + "' and the respective event subscription should not exist.");
      }
    }
  }

  @Test
  public void testSubscriptionsWhenDeletingProcessDefinitionsInOneTransactionByIdOrdered() {
    // given
    String definitionId1 = deployProcess(SINGLE_CONDITIONAL_XML);
    String definitionId2 = deployProcess(SINGLE_CONDITIONAL_XML);
    String definitionId3 = deployProcess(SINGLE_CONDITIONAL_XML);

    // when
    repositoryService.deleteProcessDefinitions()
        .byIds(definitionId1, definitionId2, definitionId3)
        .delete();

    // then
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());
  }

  @Test
  public void testSubscriptionsWhenDeletingProcessDefinitionsInOneTransactionByIdReverseOrder() {
    // given
    String definitionId1 = deployProcess(SINGLE_CONDITIONAL_XML);
    String definitionId2 = deployProcess(SINGLE_CONDITIONAL_XML);
    String definitionId3 = deployProcess(SINGLE_CONDITIONAL_XML);

    // when
    repositoryService.deleteProcessDefinitions()
        .byIds(definitionId3, definitionId2, definitionId1)
        .delete();

    // then
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());
  }

  @Test
  public void testMixedSubscriptionsWhenDeletingProcessDefinitionsInOneTransactionById1() {
    // given first version without condition
    String definitionId1 = deployModel(MODEL_WITHOUT_CONDITION);
    String definitionId2 = deployProcess(SINGLE_CONDITIONAL_XML);
    String definitionId3 = deployProcess(SINGLE_CONDITIONAL_XML);

    // when
    repositoryService.deleteProcessDefinitions()
        .byIds(definitionId1, definitionId2, definitionId3)
        .delete();

    // then
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());
  }

  @Test
  public void testMixedSubscriptionsWhenDeletingProcessDefinitionsInOneTransactionById2() {
    // given second version without condition
    String definitionId1 = deployProcess(SINGLE_CONDITIONAL_XML);
    String definitionId2 = deployModel(MODEL_WITHOUT_CONDITION);
    String definitionId3 = deployProcess(SINGLE_CONDITIONAL_XML);

    // when
    repositoryService.deleteProcessDefinitions()
        .byIds(definitionId1, definitionId2, definitionId3)
        .delete();

    // then
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());
  }

  @Test
  public void testMixedSubscriptionsWhenDeletingProcessDefinitionsInOneTransactionById3() {
    // given third version without condition
    String definitionId1 = deployProcess(SINGLE_CONDITIONAL_XML);
    String definitionId2 = deployProcess(SINGLE_CONDITIONAL_XML);
    String definitionId3 = deployModel(MODEL_WITHOUT_CONDITION);

    // when
    repositoryService.deleteProcessDefinitions()
        .byIds(definitionId1, definitionId2, definitionId3)
        .delete();

    // then
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());
  }

  @Test
  public void testMixedSubscriptionsWhenDeletingTwoProcessDefinitionsInOneTransaction1() {
    // given first version without condition
    String definitionId1 = deployModel(MODEL_WITHOUT_CONDITION);
    String definitionId2 = deployProcess(SINGLE_CONDITIONAL_XML);
    String definitionId3 = deployProcess(SINGLE_CONDITIONAL_XML);

    // when
    repositoryService.deleteProcessDefinitions()
        .byIds(definitionId2, definitionId3)
        .delete();

    // then
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());
    assertEquals(definitionId1, repositoryService.createProcessDefinitionQuery().singleResult().getId());
  }

  @Test
  public void testMixedSubscriptionsWhenDeletingTwoProcessDefinitionsInOneTransaction2() {
    // given second version without condition
    String definitionId1 = deployProcess(SINGLE_CONDITIONAL_XML);
    String definitionId2 = deployModel(MODEL_WITHOUT_CONDITION);
    String definitionId3 = deployProcess(SINGLE_CONDITIONAL_XML);

    // when
    repositoryService.deleteProcessDefinitions()
        .byIds(definitionId2, definitionId3)
        .delete();

    // then
    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());
    assertEquals(definitionId1, ((EventSubscriptionEntity) runtimeService.createEventSubscriptionQuery().singleResult()).getConfiguration());
  }

  @Test
  public void testMixedSubscriptionsWhenDeletingTwoProcessDefinitionsInOneTransaction3() {
    // given third version without condition
    String definitionId1 = deployProcess(SINGLE_CONDITIONAL_XML);
    String definitionId2 = deployProcess(SINGLE_CONDITIONAL_XML);
    String definitionId3 = deployModel(MODEL_WITHOUT_CONDITION);

    // when
    repositoryService.deleteProcessDefinitions()
        .byIds(definitionId2, definitionId3)
        .delete();

    // then
    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());
    assertEquals(definitionId1, ((EventSubscriptionEntity) runtimeService.createEventSubscriptionQuery().singleResult()).getConfiguration());
  }

  /**
   * Tests the case, when no new subscription is needed, as it is not the latest version, that is being deleted.
   */
  @Test
  public void testDeleteNotLatestVersion() {
    @SuppressWarnings("unused")
    String definitionId1 = deployProcess(SINGLE_CONDITIONAL_XML);
    String definitionId2 = deployProcess(SINGLE_CONDITIONAL_XML);
    String definitionId3 = deployProcess(SINGLE_CONDITIONAL_XML);

    // when
    repositoryService.deleteProcessDefinitions()
      .byIds(definitionId2)
      .delete();

    // then
    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());
    assertEquals(definitionId3, ((EventSubscriptionEntity) runtimeService.createEventSubscriptionQuery().singleResult()).getConfiguration());
  }

  /**
   * Tests the case when the previous of the previous version will be needed.
   */
  @Test
  public void testSubscribePreviousPreviousVersion() {

    String definitionId1 = deployProcess(SINGLE_CONDITIONAL_XML);
    String definitionId2 = deployProcess(SINGLE_CONDITIONAL_XML);
    String definitionId3 = deployProcess(SINGLE_CONDITIONAL_XML); //we're deleting version 3, but as version 2 is already deleted, we must subscribe version 1

    // when
    repositoryService.deleteProcessDefinitions()
      .byIds(definitionId2, definitionId3)
      .delete();

    // then
    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());
    assertEquals(definitionId1, ((EventSubscriptionEntity) runtimeService.createEventSubscriptionQuery().singleResult()).getConfiguration());
  }

  @Test
  public void testDeploymentOfTwoEqualConditionalStartEvent() {
    try {
      // when
      testRule.deploy(TWO_EQUAL_CONDITIONAL_START_EVENT_XML);
      fail("Expected exception");
    } catch (ParseException e) {
      // then
      assertThat(e.getMessage()).contains("Cannot have more than one conditional event subscription with the same condition '${variable == 1}'");
      assertThat(e.getResorceReports().get(0).getErrors().get(0).getMainElementId()).isEqualTo("StartEvent_2");
      List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
      assertEquals(0, eventSubscriptions.size());
    }
  }

  @Test
  @Deployment
  public void testStartInstanceWithTrueConditionalStartEvent() {
    // given a deployed process

    // when
    List<ProcessInstance> conditionInstances = runtimeService
        .createConditionEvaluation()
        .evaluateStartConditions();

    // then
    assertEquals(1, conditionInstances.size());

    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processDefinitionKey(TRUE_CONDITION_PROCESS).list();
    assertEquals(1, processInstances.size());

    assertEquals(processInstances.get(0).getId(), conditionInstances.get(0).getId());
  }

  @Test
  @Deployment(resources = SINGLE_CONDITIONAL_START_EVENT_XML)
  public void testStartInstanceWithVariableCondition() {
    // given a deployed process

    // when
    List<ProcessInstance> instances = runtimeService
        .createConditionEvaluation()
        .setVariable("foo", 1)
        .evaluateStartConditions();

    // then
    assertEquals(1, instances.size());

    VariableInstance vars = runtimeService.createVariableInstanceQuery().singleResult();
    assertEquals(vars.getProcessInstanceId(), instances.get(0).getId());
    assertEquals(1, vars.getValue());
  }

  @Test
  @Deployment(resources = SINGLE_CONDITIONAL_START_EVENT_XML)
  public void testStartInstanceWithTransientVariableCondition() {
    // given a deployed process
    VariableMap variableMap = Variables.createVariables()
        .putValueTyped("foo", Variables.integerValue(1, true));

    // when
    List<ProcessInstance> instances = runtimeService
        .createConditionEvaluation()
        .setVariables(variableMap)
        .evaluateStartConditions();

    // then
    assertEquals(1, instances.size());

    VariableInstance vars = runtimeService.createVariableInstanceQuery().singleResult();
    assertNull(vars);
  }

  @Test
  @Deployment(resources = SINGLE_CONDITIONAL_START_EVENT_XML)
  public void testStartInstanceWithoutResult() {
    // given a deployed process

    // when
    List<ProcessInstance> processes = runtimeService
      .createConditionEvaluation()
      .setVariable("foo", 0)
      .evaluateStartConditions();

    assertNotNull(processes);
    assertEquals(0, processes.size());

    assertNull(runtimeService.createVariableInstanceQuery().singleResult());
    assertNull(runtimeService.createProcessInstanceQuery().processDefinitionKey(CONDITIONAL_EVENT_PROCESS).singleResult());
  }

  @Test
  @Deployment(resources = MULTIPLE_CONDITION_XML)
  public void testStartInstanceWithMultipleConditions() {
    // given a deployed process with three conditional start events
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(3, eventSubscriptions.size());
    for (EventSubscription eventSubscription : eventSubscriptions) {
      assertEquals(EventType.CONDITONAL.name(), eventSubscription.getEventType());
    }

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("foo", 1);
    variableMap.put("bar", true);

    // when
    List<ProcessInstance> resultInstances = runtimeService
        .createConditionEvaluation()
        .setVariables(variableMap)
        .evaluateStartConditions();

    // then
    assertEquals(2, resultInstances.size());

    List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().processDefinitionKey(MULTIPLE_CONDITIONS).list();
    assertEquals(2, instances.size());
  }

  @Test
  @Deployment(resources = { SINGLE_CONDITIONAL_START_EVENT_XML,
                            MULTIPLE_CONDITION_XML,
                            TRUE_CONDITION_START_XML })
  public void testStartInstanceWithMultipleSubscriptions() {
    // given three deployed processes
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(5, eventSubscriptions.size());

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("foo", 1);
    variableMap.put("bar", true);

    // when
    List<ProcessInstance> instances = runtimeService
        .createConditionEvaluation()
        .setVariables(variableMap)
        .evaluateStartConditions();

    // then
    assertEquals(4, instances.size());
  }

  @Test
  @Deployment(resources = { SINGLE_CONDITIONAL_START_EVENT_XML,
                            MULTIPLE_CONDITION_XML,
                            TRUE_CONDITION_START_XML })
  public void testStartInstanceWithMultipleSubscriptionsWithoutProvidingAllVariables() {
    // given three deployed processes
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(5, eventSubscriptions.size());

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("foo", 1);

    // when, it should not throw PropertyNotFoundException
    List<ProcessInstance> instances = runtimeService
        .createConditionEvaluation()
        .setVariables(variableMap)
        .evaluateStartConditions();

    // then
    assertEquals(3, instances.size());
  }

  @Test
  @Deployment(resources = { SINGLE_CONDITIONAL_START_EVENT_XML, MULTIPLE_CONDITION_XML })
  public void testStartInstanceWithBusinessKey() {
    // given two deployed processes
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(4, eventSubscriptions.size());

    // when
    List<ProcessInstance> instances = runtimeService
        .createConditionEvaluation()
        .setVariable("foo", 1)
        .processInstanceBusinessKey("humuhumunukunukuapua")
        .evaluateStartConditions();

    // then
    assertEquals(2, instances.size());
    assertEquals(2, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("humuhumunukunukuapua").count());
  }

  @Test
  @Deployment(resources = { SINGLE_CONDITIONAL_START_EVENT_XML, TRUE_CONDITION_START_XML })
  public void testStartInstanceByProcessDefinitionId() {
    // given two deployed processes
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(2, eventSubscriptions.size());

    String processDefinitionId = repositoryService.createProcessDefinitionQuery().processDefinitionKey(TRUE_CONDITION_PROCESS).singleResult().getId();

    // when
    List<ProcessInstance> instances = runtimeService
        .createConditionEvaluation()
        .setVariable("foo", 1)
        .processDefinitionId(processDefinitionId)
        .evaluateStartConditions();

    // then
    assertEquals(1, instances.size());
    assertEquals(processDefinitionId, instances.get(0).getProcessDefinitionId());
  }

  @Test
  @Deployment(resources = { SINGLE_CONDITIONAL_START_EVENT_XML, MULTIPLE_CONDITION_XML})
  public void testStartInstanceByProcessDefinitionFirstVersion() {
    // given two deployed processes
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().processDefinitionKey(CONDITIONAL_EVENT_PROCESS).singleResult().getId();

    // assume
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
    assertEquals(4, eventSubscriptions.size());

    // when deploy another version
    testRule.deploy(SINGLE_CONDITIONAL_START_EVENT_XML);

    List<ProcessInstance> instances = runtimeService
        .createConditionEvaluation()
        .setVariable("foo", 1)
        .processDefinitionId(processDefinitionId)
        .evaluateStartConditions();

    // then
    assertEquals(1, instances.size());
    assertEquals(processDefinitionId, instances.get(0).getProcessDefinitionId());
  }

  @Test
  @Deployment(resources = { SINGLE_CONDITIONAL_START_EVENT_XML, TRUE_CONDITION_START_XML })
  public void testStartInstanceByNonExistingProcessDefinitionId() {
    // given two deployed processes
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(2, eventSubscriptions.size());

    // when/then
    assertThatThrownBy(() -> runtimeService
        .createConditionEvaluation()
        .setVariable("foo", 1)
        .processDefinitionId("nonExistingId")
        .evaluateStartConditions())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("no deployed process definition found with id 'nonExistingId': processDefinition is null");
  }

  @Test
  @Deployment(resources = { ONE_TASK_PROCESS })
  public void testStartInstanceByProcessDefinitionIdWithoutCondition() {
    // given deployed process without conditional start event
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").singleResult().getId();

    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(0, eventSubscriptions.size());

    // when/then
    assertThatThrownBy(() -> runtimeService
        .createConditionEvaluation()
        .processDefinitionId(processDefinitionId)
        .evaluateStartConditions())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Process definition with id '" + processDefinitionId + "' does not declare conditional start event");
  }

  @Test
  @Deployment
  public void testStartInstanceWithVariableName() {
    // given deployed process
    // ${true} variableName="foo"

    // assume
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
    assertEquals(1, eventSubscriptions.size());

    // when
    List<ProcessInstance> instances = runtimeService
        .createConditionEvaluation()
        .setVariable("foo", true)
        .evaluateStartConditions();

    // then
    assertEquals(1, instances.size());
  }

  @Test
  @Deployment(resources = START_INSTANCE_WITH_VARIABLE_NAME_XML)
  public void testStartInstanceWithVariableNameNotFullfilled() {
    // given deployed process
    // ${true} variableName="foo"

    // assume
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
    assertEquals(1, eventSubscriptions.size());

    // when
    List<ProcessInstance> instances = runtimeService
        .createConditionEvaluation()
        .evaluateStartConditions();

    // then
    assertEquals(0, instances.size());
  }

  protected String deployProcess(String resourcePath) {
    List<ProcessDefinition> deployedProcessDefinitions = testRule.deploy(resourcePath).getDeployedProcessDefinitions();
    assertEquals(1, deployedProcessDefinitions.size());
    return deployedProcessDefinitions.get(0).getId();
  }

  protected String deployModel(BpmnModelInstance model) {
    List<ProcessDefinition> deployedProcessDefinitions = testRule.deploy(model).getDeployedProcessDefinitions();
    assertEquals(1, deployedProcessDefinitions.size());
    String definitionId2 = deployedProcessDefinitions.get(0).getId();
    return definitionId2;
  }
}
