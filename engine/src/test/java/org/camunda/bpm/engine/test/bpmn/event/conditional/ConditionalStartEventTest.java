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

package org.camunda.bpm.engine.test.bpmn.event.conditional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class ConditionalStartEventTest {

  private static final String MULTIPLE_CONDITIONS = "multipleConditions";
  private static final String TRUE_CONDITION_PROCESS = "trueConditionProcess";
  private static final String CONDITIONAL_EVENT_PROCESS = "conditionalEventProcess";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown= ExpectedException.none();

  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;

  @Before
  public void setUp() throws Exception {
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml")
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
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml")
  public void testUpdateProcessVersionCancelsSubscriptions() {
    // given a deployed process
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();

    assertEquals(1, eventSubscriptions.size());
    assertEquals(1, processDefinitions.size());

    // when
    testRule.deploy("org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml");

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
  @Ignore("CAM-8635")
  public void testTwoEqualConditionalStartEvent() {
    // given a deployed process

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Error while parsing process");

    // when
    testRule.deploy("org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testTwoEqualConditionalStartEvent.bpmn20.xml");

    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
    assertEquals(0, eventSubscriptions.size());
  }

  @Test
  @Deployment
  public void testTrueConditionalStartEvent() {
    // given a deployed process

    // when
    List<ProcessInstance> conditionInstances = runtimeService
        .createConditionCorrelation()
        .setVariable("foo", "bar")
        .correlateStartConditions();

    // when
    assertEquals(1, conditionInstances.size());

    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processDefinitionKey(TRUE_CONDITION_PROCESS).list();
    assertEquals(1, processInstances.size());

    assertEquals(processInstances.get(0).getId(), conditionInstances.get(0).getId());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml")
  public void testVariableCondition() {
    // given a deployed process

    // when
    List<ProcessInstance> instances = runtimeService
        .createConditionCorrelation()
        .setVariable("foo", 1)
        .correlateStartConditions();

    // then
    assertEquals(1, instances.size());

    VariableInstance vars = runtimeService.createVariableInstanceQuery().singleResult();
    assertEquals(vars.getProcessInstanceId(), instances.get(0).getId());
    assertEquals(1, vars.getValue());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml")
  public void testTransientVariableCondition() {
    // given a deployed process
    VariableMap variableMap = Variables.createVariables()
        .putValueTyped("foo", Variables.integerValue(1, true));

    // when
    List<ProcessInstance> instances = runtimeService
        .createConditionCorrelation()
        .setVariables(variableMap)
        .correlateStartConditions();

    // then
    assertEquals(1, instances.size());

    VariableInstance vars = runtimeService.createVariableInstanceQuery().singleResult();
    assertNull(vars);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml")
  public void testWithoutResult() {
    // given a deployed process

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("No process instances were started during correlation of the conditional start events.");

    // when
    runtimeService
      .createConditionCorrelation()
      .setVariable("foo", 0)
      .correlateStartConditions();

    assertNull(runtimeService.createVariableInstanceQuery().singleResult());
    assertNull(runtimeService.createProcessInstanceQuery().processDefinitionKey(CONDITIONAL_EVENT_PROCESS).singleResult());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testTrueConditionalStartEvent.bpmn20.xml")
  public void testWithoutVariables() throws Exception {
    // given a deployed process

    thrown.expect(BadUserRequestException.class);
    thrown.expectMessage("Variables are mandatory");

    // when
    runtimeService
        .createConditionCorrelation()
        .correlateStartConditions();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testMultipleCondition.bpmn20.xml")
  public void testMultipleConditions() {
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
        .createConditionCorrelation()
        .setVariables(variableMap)
        .correlateStartConditions();

    // then
    assertEquals(2, resultInstances.size());

    List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().processDefinitionKey(MULTIPLE_CONDITIONS).list();
    assertEquals(2, instances.size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testMultipleCondition.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testTrueConditionalStartEvent.bpmn20.xml" })
  public void testMultipleSubscriptions() {
    // given three deployed processes
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(5, eventSubscriptions.size());

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("foo", 1);
    variableMap.put("bar", true);

    // when
    List<ProcessInstance> instances = runtimeService
        .createConditionCorrelation()
        .setVariables(variableMap)
        .correlateStartConditions();

    // then
    assertEquals(4, instances.size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testMultipleCondition.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testTrueConditionalStartEvent.bpmn20.xml" })
  public void testMultipleSubscriptionsWithoutProvidingAllVariables() {
    // given three deployed processes
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(5, eventSubscriptions.size());

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("foo", 1);

    // when, it should not throw PropertyNotFoundException
    List<ProcessInstance> instances = runtimeService
        .createConditionCorrelation()
        .setVariables(variableMap)
        .correlateStartConditions();

    // then
    assertEquals(3, instances.size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testMultipleCondition.bpmn20.xml"})
  public void testWithBusinessKey() {
    // given two deployed processes
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(4, eventSubscriptions.size());

    // when
    List<ProcessInstance> instances = runtimeService
        .createConditionCorrelation()
        .setVariable("foo", 1)
        .processInstanceBusinessKey("humuhumunukunukuapua")
        .correlateStartConditions();

    // then
    assertEquals(2, instances.size());
    assertEquals(2, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("humuhumunukunukuapua").count());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testTrueConditionalStartEvent.bpmn20.xml" })
  public void testWithProcessDefinitionId() {
    // given two deployed processes
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(2, eventSubscriptions.size());

    String processDefinitionId = repositoryService.createProcessDefinitionQuery().processDefinitionKey(TRUE_CONDITION_PROCESS).singleResult().getId();

    // when
    List<ProcessInstance> instances = runtimeService
        .createConditionCorrelation()
        .setVariable("foo", 1)
        .processDefinitionId(processDefinitionId)
        .correlateStartConditions();

    // then
    assertEquals(1, instances.size());
    assertEquals(processDefinitionId, instances.get(0).getProcessDefinitionId());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testMultipleCondition.bpmn20.xml"})
  public void testWithProcessDefinitionFirstVersion() {
    // given two deployed processes
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().processDefinitionKey(CONDITIONAL_EVENT_PROCESS).singleResult().getId();

    // assume
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
    assertEquals(4, eventSubscriptions.size());

    // when deploy another version
    testRule.deploy("org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml");

    List<ProcessInstance> instances = runtimeService
        .createConditionCorrelation()
        .setVariable("foo", 1)
        .processDefinitionId(processDefinitionId)
        .correlateStartConditions();

    // then
    assertEquals(1, instances.size());
    assertEquals(processDefinitionId, instances.get(0).getProcessDefinitionId());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testTrueConditionalStartEvent.bpmn20.xml" })
  public void testWithNonExistingProcessDefinitionId() {
    // given two deployed processes
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(2, eventSubscriptions.size());

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("no deployed process definition found with id 'nonExistingId': processDefinition is null");

    // when
    runtimeService
        .createConditionCorrelation()
        .setVariable("foo", 1)
        .processDefinitionId("nonExistingId")
        .correlateStartConditions();
  }

  @Test
  @Deployment
  public void testVariableName() {
    // given deployed process with two conditional start events:
    // ${true} variableName="foo"
    // ${true}

    // assume
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
    assertEquals(2, eventSubscriptions.size());

    // when
    List<ProcessInstance> instances = runtimeService
        .createConditionCorrelation()
        .setVariable("foo", 42)
        .correlateStartConditions();

    // then
    assertEquals(2, instances.size());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testVariableName.bpmn20.xml")
  public void testVariableNameNotFullfilled() {
    // given deployed process with two conditional start events:
    // ${true} variableName="foo"
    // ${true}

    // assume
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
    assertEquals(2, eventSubscriptions.size());

    // when
    List<ProcessInstance> instances = runtimeService
        .createConditionCorrelation()
        .setVariable("bar", 42)
        .correlateStartConditions();

    // then
    assertEquals(1, instances.size());
  }
}
