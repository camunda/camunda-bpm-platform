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
package org.camunda.bpm.engine.test.bpmn.event.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
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

import junit.framework.AssertionFailedError;

public class MessageStartEventSubscriptionTest {

  private static final String SINGLE_MESSAGE_START_EVENT_XML = "org/camunda/bpm/engine/test/bpmn/event/message/MessageStartEventTest.testSingleMessageStartEvent.bpmn20.xml";
  private static final String ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
  private static final String MESSAGE_EVENT_PROCESS = "singleMessageStartEvent";

  private static final BpmnModelInstance MODEL_WITHOUT_MESSAGE = Bpmn.createExecutableProcess(MESSAGE_EVENT_PROCESS)
      .startEvent()
      .userTask()
      .endEvent()
      .done();

  private static final BpmnModelInstance MODEL = Bpmn.createExecutableProcess("another")
      .startEvent()
      .message("anotherMessage")
      .userTask()
      .endEvent()
      .done();

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown= ExpectedException.none();

  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;

  @Before
  public void setUp() throws Exception {
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
  }

  @Test
  public void testUpdateProcessVersionCancelsSubscriptions() {
    testRule.deploy(SINGLE_MESSAGE_START_EVENT_XML);
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();

    assertEquals(1, eventSubscriptions.size());
    assertEquals(1, processDefinitions.size());

    // when
    testRule.deploy(SINGLE_MESSAGE_START_EVENT_XML);

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
  public void testEventSubscriptionAfterDeleteLatestProcessVersion() {
    // given a deployed process
    testRule.deploy(SINGLE_MESSAGE_START_EVENT_XML);
    ProcessDefinition processDefinitionV1 = repositoryService.createProcessDefinitionQuery().singleResult();
    assertNotNull(processDefinitionV1);

    // deploy second version of the process
    String deploymentId = testRule.deploy(SINGLE_MESSAGE_START_EVENT_XML).getId();

    // when
    repositoryService.deleteDeployment(deploymentId, true);

    // then
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(MESSAGE_EVENT_PROCESS).singleResult();
    assertEquals(processDefinitionV1.getId(), processDefinition.getId());

    EventSubscriptionEntity eventSubscription = (EventSubscriptionEntity) runtimeService.createEventSubscriptionQuery().singleResult();
    assertNotNull(eventSubscription);
    assertEquals(processDefinitionV1.getId(), eventSubscription.getConfiguration());
  }

  @Test
  public void testStartInstanceAfterDeleteLatestProcessVersionByIds() {
    // given a deployed process
    testRule.deploy(SINGLE_MESSAGE_START_EVENT_XML);
    // deploy second version of the process
    DeploymentWithDefinitions deployment = testRule.deploy(SINGLE_MESSAGE_START_EVENT_XML);
    ProcessDefinition processDefinition = deployment.getDeployedProcessDefinitions().get(0);

    // delete it
    repositoryService.deleteProcessDefinitions()
      .byIds(processDefinition.getId())
      .delete();

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByMessage("newInvoiceMessage");

    // then
    assertFalse(processInstance.isEnded());
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    taskService.complete(task.getId());

    ProcessInstance completedInstance = runtimeService
        .createProcessInstanceQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();

    if (completedInstance != null) {
      throw new AssertionFailedError("Expected finished process instance '" + completedInstance + "' but it was still in the db");
    }
  }

  @Test
  public void testStartInstanceAfterDeleteLatestProcessVersion() {
    // given a deployed process
    testRule.deploy(SINGLE_MESSAGE_START_EVENT_XML);
    // deploy second version of the process
    String deploymentId = testRule.deploy(SINGLE_MESSAGE_START_EVENT_XML).getId();
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();

    // delete it
    repositoryService.deleteDeployment(deployment.getId(), true);

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("singleMessageStartEvent");

    assertFalse(processInstance.isEnded());

    Task  task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    taskService.complete(task.getId());

    ProcessInstance completedInstance = runtimeService
        .createProcessInstanceQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();

    if (completedInstance != null) {
      throw new AssertionFailedError("Expected finished process instance '" + completedInstance + "' but it was still in the db");
    }
  }

  @Test
  public void testVersionWithoutConditionAfterDeleteLatestProcessVersionWithCondition() {
    // given a process
    testRule.deploy(MODEL_WITHOUT_MESSAGE);

    // deploy second version of the process
    String deploymentId = testRule.deploy(SINGLE_MESSAGE_START_EVENT_XML).getId();
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();

    // delete it
    repositoryService.deleteDeployment(deployment.getId(), true);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("No subscriptions were found during evaluation of the conditional start events.");

    // when
    runtimeService
      .createConditionEvaluation()
      .setVariable("foo", 1)
      .evaluateStartConditions();
  }

  @Test
  public void testSubscriptionsWhenDeletingProcessDefinitionsInOneTransactionByKeys() {
    // given three versions of the process
    testRule.deploy(SINGLE_MESSAGE_START_EVENT_XML);
    testRule.deploy(SINGLE_MESSAGE_START_EVENT_XML);
    testRule.deploy(SINGLE_MESSAGE_START_EVENT_XML);

    // when
    repositoryService.deleteProcessDefinitions()
      .byKey(MESSAGE_EVENT_PROCESS)
      .delete();

    // then
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());
  }

  @Test
  public void testSubscriptionsWhenDeletingGroupsProcessDefinitionsByIds() {
    // given
    String processDefId11 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String processDefId12 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String processDefId13 = testRule.deployAndGetDefinition(MODEL_WITHOUT_MESSAGE).getId();

    String processDefId21 = deployModel(MODEL);
    String processDefId22 = deployModel(MODEL);
    String processDefId23 = deployModel(MODEL);

    String processDefId31 = deployProcess(ONE_TASK_PROCESS);
    @SuppressWarnings("unused")
    String processDefId32 = deployProcess(ONE_TASK_PROCESS);

    // assume
    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());

    // when
    repositoryService.deleteProcessDefinitions()
      .byIds(processDefId21,processDefId23,processDefId13,
          processDefId12,processDefId31)
      .delete();

    // then
    List<EventSubscription> list = runtimeService.createEventSubscriptionQuery().list();
    assertEquals(2, list.size());
    for (EventSubscription eventSubscription : list) {
      EventSubscriptionEntity eventSubscriptionEntity = (EventSubscriptionEntity) eventSubscription;
      if (!eventSubscriptionEntity.getConfiguration().equals(processDefId11) && !eventSubscriptionEntity.getConfiguration().equals(processDefId22)) {
        fail("This process definition '" + eventSubscriptionEntity.getConfiguration() + "' and the respective event subscription should not exist.");
      }
    }
  }

  @Test
  public void testSubscriptionsWhenDeletingProcessDefinitionsInOneTransactionByIdOrdered() {
    // given
    String definitionId1 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String definitionId2 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String definitionId3 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);

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
    String definitionId1 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String definitionId2 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String definitionId3 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);

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
    String definitionId1 = deployModel(MODEL_WITHOUT_MESSAGE);
    String definitionId2 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String definitionId3 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);

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
    String definitionId1 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String definitionId2 = deployModel(MODEL_WITHOUT_MESSAGE);
    String definitionId3 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);

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
    String definitionId1 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String definitionId2 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String definitionId3 = deployModel(MODEL_WITHOUT_MESSAGE);

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
    String definitionId1 = deployModel(MODEL_WITHOUT_MESSAGE);
    String definitionId2 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String definitionId3 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);

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
    String definitionId1 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String definitionId2 = deployModel(MODEL_WITHOUT_MESSAGE);
    String definitionId3 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);

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
    String definitionId1 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String definitionId2 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String definitionId3 = deployModel(MODEL_WITHOUT_MESSAGE);

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
    String definitionId1 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String definitionId2 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String definitionId3 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);

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

    String definitionId1 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String definitionId2 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML);
    String definitionId3 = deployProcess(SINGLE_MESSAGE_START_EVENT_XML); //we're deleting version 3, but as version 2 is already deleted, we must subscribe version 1

    // when
    repositoryService.deleteProcessDefinitions()
      .byIds(definitionId2, definitionId3)
      .delete();

    // then
    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());
    assertEquals(definitionId1, ((EventSubscriptionEntity) runtimeService.createEventSubscriptionQuery().singleResult()).getConfiguration());
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
