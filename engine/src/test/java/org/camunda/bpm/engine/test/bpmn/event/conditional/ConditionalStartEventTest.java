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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Assert;
import org.junit.Test;

public class ConditionalStartEventTest extends PluggableProcessEngineTestCase {

//  private List<EventSubscription> eventSubscriptions;
//  private List<String> deploymentIds = new ArrayList<String>();


//  @After
//  public void cleanUp() {
//    for (String deploymentId : deploymentIds) {
//      repositoryService.deleteDeployment(deploymentId, true);
//    }
//
//    eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
//    assertEquals(0, eventSubscriptions.size());
//  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml")
  public void testDeploymentCreatesSubscriptions() {
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(1, eventSubscriptions.size());
    assertEquals(EventType.CONDITONAL.name(), eventSubscriptions.get(0).getEventType());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml")
  public void testUpdateProcessVersionCancelsSubscriptions() {

    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();

    assertEquals(1, eventSubscriptions.size());
    assertEquals(1, processDefinitions.size());

    String deploymentId = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml")
        .deploy()
        .getId();

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

    repositoryService.deleteDeployment(deploymentId, true);
  }

  @Test
  public void testTwoEqualConditionalStartEvent() {
    try {
      repositoryService
          .createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testTwoEqualConditionalStartEvent.bpmn20.xml")
          .deploy().getId();
      fail("exception expected");
    } catch (ProcessEngineException e) {
      assertTrue(e.getMessage().contains("Error while parsing process"));
    }

    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
    assertEquals(0, eventSubscriptions.size());
  }

  @Test
  @Deployment
  public void testTrueConditionalStartEvent() {
    List<ProcessInstance> conditionInstances = runtimeService
        .createConditionCorrelation()
        .setVariable("foo", "bar")
        .correlateStartConditions();

    assertEquals(1, conditionInstances.size());

    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processDefinitionKey("trueConditionProcess").list();
    assertEquals(1, processInstances.size());

    assertEquals(processInstances.get(0).getId(), conditionInstances.get(0).getId());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml")
  public void testVariableCondition() {
    List<ProcessInstance> instances = runtimeService
        .createConditionCorrelation()
        .setVariable("foo", 1)
        .correlateStartConditions();

    assertEquals(1, instances.size());

    VariableInstance vars = runtimeService.createVariableInstanceQuery().singleResult();
    assertEquals(vars.getProcessInstanceId(), instances.get(0).getId());
    assertEquals(1, vars.getValue());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml")
  public void testTransientVariableCondition() {
    VariableMap variableMap = Variables.createVariables()
        .putValueTyped("foo", Variables.integerValue(1, true));

    List<ProcessInstance> instances = runtimeService
        .createConditionCorrelation()
        .setVariables(variableMap)
        .correlateStartConditions();

    assertEquals(1, instances.size());

    VariableInstance vars = runtimeService.createVariableInstanceQuery().singleResult();
    assertNull(vars);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml")
  public void testWithoutResult() {
    try {
      runtimeService.createConditionCorrelation().setVariable("foo", 0).correlateStartConditions();
      fail("ProcessEngineException should be thrown");
    } catch (ProcessEngineException e) {
      Assert.assertTrue(e.getMessage().contains("No process instances were started during correlation of the conditional start events."));
    }

    assertNull(runtimeService.createVariableInstanceQuery().singleResult());
    assertNull(runtimeService.createProcessInstanceQuery().processDefinitionKey("conditionalEventProcess").singleResult());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testTrueConditionalStartEvent.bpmn20.xml")
  public void testWithoutVariables() throws Exception {
    try {
      runtimeService.createConditionCorrelation().correlateStartConditions();
      fail("BadUserRequestException should be thrown");
    } catch (BadUserRequestException e) {
      Assert.assertTrue(e.getMessage().contains("Variables are mandatory"));
    }
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testMultipleCondition.bpmn20.xml")
  public void testMultipleConditions() {
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(3, eventSubscriptions.size());
    for (EventSubscription eventSubscription : eventSubscriptions) {
      assertEquals(EventType.CONDITONAL.name(), eventSubscription.getEventType());
    }

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("foo", 1);
    variableMap.put("bar", true);

    List<ProcessInstance> resultInstances = runtimeService
        .createConditionCorrelation()
        .setVariables(variableMap)
        .correlateStartConditions();

    assertEquals(2, resultInstances.size());

    List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().processDefinitionKey("multipleConditions").list();
    assertEquals(2, instances.size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testMultipleCondition.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testTrueConditionalStartEvent.bpmn20.xml" })
  public void testMultipleSubscriptions() {
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(5, eventSubscriptions.size());

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("foo", 1);
    variableMap.put("bar", true);

    List<ProcessInstance> instances = runtimeService
        .createConditionCorrelation()
        .setVariables(variableMap)
        .correlateStartConditions();

    assertEquals(4, instances.size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testMultipleCondition.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testTrueConditionalStartEvent.bpmn20.xml" })
  public void testMultipleSubscriptionsWithLessVariables() {
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(5, eventSubscriptions.size());

    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("foo", 1);

    List<ProcessInstance> instances = runtimeService
        .createConditionCorrelation()
        .setVariables(variableMap)
        .correlateStartConditions();

    assertEquals(3, instances.size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testMultipleCondition.bpmn20.xml"})
  public void testWithBusinessKey() {
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(4, eventSubscriptions.size());

    List<ProcessInstance> instances = runtimeService
        .createConditionCorrelation()
        .setVariable("foo", 1)
        .processInstanceBusinessKey("conditionalEventProcess")
        .correlateStartConditions();

    assertEquals(1, instances.size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testMultipleCondition.bpmn20.xml"})
  public void testWithNonExistingBusinessKey() {
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(4, eventSubscriptions.size());

    try {
      runtimeService
        .createConditionCorrelation()
        .setVariable("foo", 1)
        .processInstanceBusinessKey("nonExisting")
        .correlateStartConditions();
      fail("Expected exception");
    } catch (ProcessEngineException e) {
      assertTrue(e.getMessage().contains("No process instances were started during correlation of the conditional start events."));
    }
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testTrueConditionalStartEvent.bpmn20.xml" })
  public void testWithProcessDefinitionId() {
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(2, eventSubscriptions.size());

    String processDefinitionId = repositoryService.createProcessDefinitionQuery().processDefinitionKey("trueConditionProcess").singleResult().getId();

    List<ProcessInstance> instances = runtimeService
        .createConditionCorrelation()
        .setVariable("foo", 1)
        .processDefinitionId(processDefinitionId)
        .correlateStartConditions();

    assertEquals(1, instances.size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testTrueConditionalStartEvent.bpmn20.xml" })
  public void testWithNonExistingProcessDefinitionId() {
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(2, eventSubscriptions.size());

    try {
      runtimeService
        .createConditionCorrelation()
        .setVariable("foo", 1)
        .processDefinitionId("nonExistingId")
        .correlateStartConditions();
      fail("Expected exception");
    } catch (ProcessEngineException e) {
      assertTrue(e.getMessage().contains("No process instances were started during correlation of the conditional start events."));
    }
  }
}
