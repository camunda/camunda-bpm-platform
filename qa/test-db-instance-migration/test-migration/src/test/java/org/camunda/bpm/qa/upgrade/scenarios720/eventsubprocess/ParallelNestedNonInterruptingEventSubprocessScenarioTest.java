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
package org.camunda.bpm.qa.upgrade.scenarios720.eventsubprocess;

import static org.camunda.bpm.qa.upgrade.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.qa.upgrade.util.ActivityInstanceAssert.describeActivityInstanceTree;

import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.camunda.bpm.qa.upgrade.util.ThrowBpmnErrorDelegate;
import org.camunda.bpm.qa.upgrade.util.ThrowBpmnErrorDelegate.ThrowBpmnErrorDelegateException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

@ScenarioUnderTest("ParallelNestedNonInterruptingEventSubprocessScenario")
@Origin("7.2.0")
public class ParallelNestedNonInterruptingEventSubprocessScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("init.1")
  public void testInitCompletionCase1() {
    // given
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();
    Task innerTask = rule.taskQuery().taskDefinitionKey("innerTask").singleResult();
    Task eventSubprocessTask = rule.taskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();

    // when
    rule.getTaskService().complete(outerTask.getId());
    rule.getTaskService().complete(innerTask.getId());
    rule.getTaskService().complete(eventSubprocessTask.getId());

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.2")
  public void testInitCompletionCase2() {
    // given
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();
    Task innerTask = rule.taskQuery().taskDefinitionKey("innerTask").singleResult();
    Task eventSubprocessTask = rule.taskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();

    // when
    rule.getTaskService().complete(eventSubprocessTask.getId());
    rule.getTaskService().complete(innerTask.getId());
    rule.getTaskService().complete(outerTask.getId());

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.3")
  public void testInitActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
        describeActivityInstanceTree(instance.getProcessDefinitionId())
          .activity("outerTask")
          .beginScope("subProcess")
            .activity("innerTask")
            // eventSubProcess was previously no scope so it misses here
            .activity("eventSubProcessTask")
        .done());
  }

  @Test
  @ScenarioUnderTest("init.4")
  public void testInitDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @ScenarioUnderTest("init.5")
  public void testInitThrowError() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task eventSubprocessTask = rule.taskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();

    // when
    rule.getTaskService().complete(outerTask.getId());

    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.ERROR_INDICATOR_VARIABLE, true);
    rule.getTaskService().complete(eventSubprocessTask.getId());

    // then
    Task escalatedTask = rule.taskQuery().singleResult();
    Assert.assertEquals("escalatedTask", escalatedTask.getTaskDefinitionKey());
    Assert.assertNotNull(escalatedTask);

    rule.getTaskService().complete(escalatedTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.6")
  public void testInitThrowUnhandledException() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task eventSubprocessTask = rule.taskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();

    // when
    rule.getTaskService().complete(outerTask.getId());

    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_INDICATOR_VARIABLE, true);
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_MESSAGE_VARIABLE, "unhandledException");

    // then
    try {
      rule.getTaskService().complete(eventSubprocessTask.getId());
      Assert.fail("should throw a ThrowBpmnErrorDelegateException");

    } catch (ThrowBpmnErrorDelegateException e) {
      Assert.assertEquals("unhandledException", e.getMessage());
    }
  }

  @Test
  @ScenarioUnderTest("init.innerTask.1")
  public void testInitInnerTaskCompletion1() {
    // given
    Task eventSubprocessTask = rule.taskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();

    // when
    rule.getTaskService().complete(eventSubprocessTask.getId());
    rule.getTaskService().complete(outerTask.getId());

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.innerTask.2")
  public void testInitInnerTaskCompletion2() {
    // given
    Task eventSubprocessTask = rule.taskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();

    // when
    rule.getTaskService().complete(outerTask.getId());
    rule.getTaskService().complete(eventSubprocessTask.getId());

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.innerTask.3")
  public void testInitInnerTaskActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
        describeActivityInstanceTree(instance.getProcessDefinitionId())
          .activity("outerTask")
          .beginScope("subProcess")
            // eventSubProcess was previously no scope so it misses here
            .activity("eventSubProcessTask")
        .done());
  }

  @Test
  @ScenarioUnderTest("init.innerTask.4")
  public void testInitInnerTaskDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.innerTask.5")
  public void testInitInnerTaskThrowError() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task eventSubprocessTask = rule.taskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();

    // when
    rule.getTaskService().complete(outerTask.getId());
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.ERROR_INDICATOR_VARIABLE, true);
    rule.getTaskService().complete(eventSubprocessTask.getId());

    // then
    Task escalatedTask = rule.taskQuery().singleResult();
    Assert.assertEquals("escalatedTask", escalatedTask.getTaskDefinitionKey());
    Assert.assertNotNull(escalatedTask);

    rule.getTaskService().complete(escalatedTask.getId());

    // the instance is deadlocked since no token has arrived on the sequence flow leaving the outer subprocess
    Assert.assertEquals(1, rule.executionQuery().count());
    Assert.assertEquals(1, rule.executionQuery().activityId("join").count());
  }

  @Test
  @ScenarioUnderTest("init.innerTask.6")
  public void testInitInnerTaskThrowUnhandledException() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task eventSubprocessTask = rule.taskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();

    // when
    rule.getTaskService().complete(outerTask.getId());

    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_INDICATOR_VARIABLE, true);
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_MESSAGE_VARIABLE, "unhandledException");

    // then
    try {
      rule.getTaskService().complete(eventSubprocessTask.getId());
      Assert.fail("should throw a ThrowBpmnErrorDelegateException");

    } catch (ThrowBpmnErrorDelegateException e) {
      Assert.assertEquals("unhandledException", e.getMessage());
    }
  }

}
