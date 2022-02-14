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
package org.camunda.bpm.engine.test.bpmn.event.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;


/**
 * @author Daniel Meyer
 */
public class MessageStartEventTest extends PluggableProcessEngineTest {

  @Test
  public void testDeploymentCreatesSubscriptions() {
    String deploymentId = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/message/MessageStartEventTest.testSingleMessageStartEvent.bpmn20.xml")
        .deploy()
        .getId();

    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(1, eventSubscriptions.size());

    repositoryService.deleteDeployment(deploymentId);
  }

  @Test
  public void testSameMessageNameFails() {
    repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/message/MessageStartEventTest.testSingleMessageStartEvent.bpmn20.xml")
        .deploy()
        .getId();
    try {
      repositoryService
          .createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/message/otherProcessWithNewInvoiceMessage.bpmn20.xml")
          .deploy();
      fail("exception expected");
    } catch (ProcessEngineException e) {
      assertTrue(e.getMessage().contains("there already is a message event subscription for the message with name"));
    } finally {
      // clean db:
      List<org.camunda.bpm.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
      for (org.camunda.bpm.engine.repository.Deployment deployment : deployments) {
        repositoryService.deleteDeployment(deployment.getId(), true);
      }
      // Workaround for #CAM-4250: remove process definition of failed
      // deployment from deployment cache
      processEngineConfiguration.getDeploymentCache().getProcessDefinitionCache().clear();
    }
  }

  // SEE: https://app.camunda.com/jira/browse/CAM-1448
  @Test
  public void testEmptyMessageNameFails() {
    try {
      repositoryService
          .createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/message/MessageStartEventTest.testEmptyMessageNameFails.bpmn20.xml")
          .deploy();
      fail("exception expected");
    } catch (ParseException e) {
      assertTrue(e.getMessage().contains("Cannot have a message event subscription with an empty or missing name"));
      assertEquals("theStart", e.getResorceReports().get(0).getErrors().get(0).getMainElementId());
    }
  }

  @Test
  public void testSameMessageNameInSameProcessFails() {
    try {
      repositoryService
          .createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/message/testSameMessageNameInSameProcessFails.bpmn20.xml")
          .deploy();
      fail("exception expected");
    } catch (ProcessEngineException e) {
      assertTrue(e.getMessage().contains("Cannot have more than one message event subscription with name 'newInvoiceMessage' for scope"));
    }
  }

  @Test
  public void testUpdateProcessVersionCancelsSubscriptions() {
    String deploymentId = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/message/MessageStartEventTest.testSingleMessageStartEvent.bpmn20.xml")
        .deploy()
        .getId();

    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();

    assertEquals(1, eventSubscriptions.size());
    assertEquals(1, processDefinitions.size());

    String newDeploymentId = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/message/MessageStartEventTest.testSingleMessageStartEvent.bpmn20.xml")
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

    repositoryService.deleteDeployment(deploymentId);
    repositoryService.deleteDeployment(newDeploymentId);
  }

  @Deployment
  @Test
  public void testSingleMessageStartEvent() {

    // using startProcessInstanceByMessage triggers the message start event

    ProcessInstance processInstance = runtimeService.startProcessInstanceByMessage("newInvoiceMessage");

    assertFalse(processInstance.isEnded());

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    taskService.complete(task.getId());

    testRule.assertProcessEnded(processInstance.getId());

    // using startProcessInstanceByKey also triggers the message event, if there is a single start event

    processInstance = runtimeService.startProcessInstanceByKey("singleMessageStartEvent");

    assertFalse(processInstance.isEnded());

    task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    taskService.complete(task.getId());

    testRule.assertProcessEnded(processInstance.getId());

  }


  @Deployment
  @Test
  public void testMessageStartEventAndNoneStartEvent() {

    // using startProcessInstanceByKey triggers the none start event

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    assertFalse(processInstance.isEnded());

    Task task = taskService.createTaskQuery().taskDefinitionKey("taskAfterNoneStart").singleResult();
    assertNotNull(task);

    taskService.complete(task.getId());

    testRule.assertProcessEnded(processInstance.getId());

    // using startProcessInstanceByMessage triggers the message start event

    processInstance = runtimeService.startProcessInstanceByMessage("newInvoiceMessage");

    assertFalse(processInstance.isEnded());

    task = taskService.createTaskQuery().taskDefinitionKey("taskAfterMessageStart").singleResult();
    assertNotNull(task);

    taskService.complete(task.getId());

    testRule.assertProcessEnded(processInstance.getId());

  }

  @Deployment
  @Test
  public void testMultipleMessageStartEvents() {

    // sending newInvoiceMessage

    ProcessInstance processInstance = runtimeService.startProcessInstanceByMessage("newInvoiceMessage");

    assertFalse(processInstance.isEnded());

    Task task = taskService.createTaskQuery().taskDefinitionKey("taskAfterMessageStart").singleResult();
    assertNotNull(task);

    taskService.complete(task.getId());

    testRule.assertProcessEnded(processInstance.getId());

    // sending newInvoiceMessage2

    processInstance = runtimeService.startProcessInstanceByMessage("newInvoiceMessage2");

    assertFalse(processInstance.isEnded());

    task = taskService.createTaskQuery().taskDefinitionKey("taskAfterMessageStart2").singleResult();
    assertNotNull(task);

    taskService.complete(task.getId());

    testRule.assertProcessEnded(processInstance.getId());

    // starting the process using startProcessInstanceByKey is not possible:
    try {
      runtimeService.startProcessInstanceByKey("testProcess");
      fail("exception expected");
    } catch (ProcessEngineException e) {
      assertTrue("different exception expected, not " + e.getMessage(), e.getMessage().contains("has no default start activity"));
    }

  }

  @Deployment
  @Test
  public void testDeployStartAndIntermediateEventWithSameMessageInSameProcess() {
    ProcessInstance pi = null;
    try {
      runtimeService.startProcessInstanceByMessage("message");
      pi = runtimeService.createProcessInstanceQuery().singleResult();
      assertThat(pi.isEnded()).isFalse();

      String deploymentId = repositoryService
          .createDeployment()
          .addClasspathResource(
              "org/camunda/bpm/engine/test/bpmn/event/message/MessageStartEventTest.testDeployStartAndIntermediateEventWithSameMessageInSameProcess.bpmn")
          .name("deployment2").deploy().getId();
      assertThat(repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult()).isNotNull();
    } finally {
      // clean db:
      runtimeService.deleteProcessInstance(pi.getId(), "failure");
      List<org.camunda.bpm.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
      for (org.camunda.bpm.engine.repository.Deployment d : deployments) {
        repositoryService.deleteDeployment(d.getId(), true);
      }
      // Workaround for #CAM-4250: remove process definition of failed
      // deployment from deployment cache

      processEngineConfiguration.getDeploymentCache().getProcessDefinitionCache().clear();
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/message/MessageStartEventTest.testDeployStartAndIntermediateEventWithSameMessageDifferentProcesses.bpmn"})
  @Test
  public void testDeployStartAndIntermediateEventWithSameMessageDifferentProcessesFirstStartEvent() {
    ProcessInstance pi = null;
    try {
      runtimeService.startProcessInstanceByMessage("message");
      pi = runtimeService.createProcessInstanceQuery().singleResult();
      assertThat(pi.isEnded()).isFalse();

      String deploymentId = repositoryService
          .createDeployment()
          .addClasspathResource(
              "org/camunda/bpm/engine/test/bpmn/event/message/MessageStartEventTest.testDeployStartAndIntermediateEventWithSameMessageDifferentProcesses2.bpmn")
          .name("deployment2").deploy().getId();
      assertThat(repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult()).isNotNull();
    } finally {
      // clean db:
      runtimeService.deleteProcessInstance(pi.getId(), "failure");
      List<org.camunda.bpm.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
      for (org.camunda.bpm.engine.repository.Deployment d : deployments) {
        repositoryService.deleteDeployment(d.getId(), true);
      }
      // Workaround for #CAM-4250: remove process definition of failed
      // deployment from deployment cache

      processEngineConfiguration.getDeploymentCache().getProcessDefinitionCache().clear();
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/message/MessageStartEventTest.testDeployStartAndIntermediateEventWithSameMessageDifferentProcesses2.bpmn"})
  @Test
  public void testDeployStartAndIntermediateEventWithSameMessageDifferentProcessesFirstIntermediateEvent() {
    ProcessInstance pi = null;
    try {
      runtimeService.startProcessInstanceByKey("Process_2");
      pi = runtimeService.createProcessInstanceQuery().singleResult();
      assertThat(pi.isEnded()).isFalse();

      String deploymentId = repositoryService
          .createDeployment()
          .addClasspathResource(
              "org/camunda/bpm/engine/test/bpmn/event/message/MessageStartEventTest.testDeployStartAndIntermediateEventWithSameMessageDifferentProcesses.bpmn")
          .name("deployment2").deploy().getId();
      assertThat(repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult()).isNotNull();
    } finally {
      // clean db:
      runtimeService.deleteProcessInstance(pi.getId(), "failure");
      List<org.camunda.bpm.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
      for (org.camunda.bpm.engine.repository.Deployment d : deployments) {
        repositoryService.deleteDeployment(d.getId(), true);
      }
      // Workaround for #CAM-4250: remove process definition of failed
      // deployment from deployment cache

      processEngineConfiguration.getDeploymentCache().getProcessDefinitionCache().clear();
    }
  }

  @Test
  public void testUsingExpressionWithDollarTagInMessageStartEventNameThrowsException() {

    // given a process definition with a start message event that has a message name which contains an expression
    String processDefinition =
        "org/camunda/bpm/engine/test/bpmn/event/message/" +
            "MessageStartEventTest.testUsingExpressionWithDollarTagInMessageStartEventNameThrowsException.bpmn20.xml";
    try {
      // when deploying the process
      repositoryService
          .createDeployment()
          .addClasspathResource(processDefinition)
          .deploy();
      fail("exception expected");
    } catch (ProcessEngineException e) {
      // then a process engine exception should be thrown with a certain message
      assertTrue(e.getMessage().contains("Invalid message name"));
      assertTrue(e.getMessage().contains("expressions in the message start event name are not allowed!"));
    }
  }

  @Test
  public void testUsingExpressionWithHashTagInMessageStartEventNameThrowsException() {

    // given a process definition with a start message event that has a message name which contains an expression
    String processDefinition =
        "org/camunda/bpm/engine/test/bpmn/event/message/" +
            "MessageStartEventTest.testUsingExpressionWithHashTagInMessageStartEventNameThrowsException.bpmn20.xml";
    try {
      // when deploying the process
      repositoryService
          .createDeployment()
          .addClasspathResource(processDefinition)
          .deploy();
      fail("exception expected");
    } catch (ProcessEngineException e) {
      // then a process engine exception should be thrown with a certain message
      assertTrue(e.getMessage().contains("Invalid message name"));
      assertTrue(e.getMessage().contains("expressions in the message start event name are not allowed!"));
    }
  }

  //test fix CAM-10819
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/message/MessageStartEventTest.testMessageStartEventUsingCorrelationEngine.bpmn"})
  @Test
  public void testMessageStartEventUsingCorrelationEngineAndLocalVariable() {

    // when
    // sending newCorrelationStartMessage using correlation engine
    ProcessInstance  processInstance = runtimeService.createMessageCorrelation("newCorrelationStartMessage")
            .setVariableLocal("var", "value")
            .correlateWithResult().getProcessInstance();

    // then
    // ensure the variable is available
    String processInstanceValue = (String) runtimeService.getVariableLocal(processInstance.getId(), "var");
    assertThat(processInstanceValue).isEqualTo("value");
  }

}
