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
package org.camunda.bpm.qa.upgrade.scenarios720.boundary;

import static org.camunda.bpm.qa.upgrade.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.qa.upgrade.util.ActivityInstanceAssert.describeActivityInstanceTree;

import java.util.List;

import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Job;
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

@ScenarioUnderTest("NestedNonInterruptingBoundaryEventOnOuterSubprocessScenario")
@Origin("7.2.0")
public class NestedNonInterruptingBoundaryEventOnOuterSubprocessScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("initTimer.1")
  public void testInitTimerCompletionCase1() {
    // given
    Task afterBoundaryTask = rule.taskQuery().taskDefinitionKey("afterBoundaryTask").singleResult();

    // when
    rule.messageCorrelation("ReceiveTaskMessage").correlate();
    rule.getTaskService().complete(afterBoundaryTask.getId());

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initTimer.2")
  public void testInitTimerCompletionCase2() {
    // given
    Task afterBoundaryTask = rule.taskQuery().taskDefinitionKey("afterBoundaryTask").singleResult();

    // when
    rule.getTaskService().complete(afterBoundaryTask.getId());
    rule.messageCorrelation("ReceiveTaskMessage").correlate();

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initTimer.3")
  public void testInitTimerActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
        describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("afterBoundaryTask")
        .beginScope("outerSubProcess")
          .beginScope("innerSubProcess")
            .activity("innerSubProcessTask")
        .done());
  }

  @Test
  @ScenarioUnderTest("initTimer.4")
  public void testInitTimerDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initTimer.5")
  public void testInitTimerTriggerBoundary() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when the boundary event is triggered another 2 times
    for (int i = 0; i < 2; i++) {
      Job job = rule.getManagementService().createJobQuery()
        .processInstanceId(instance.getId()).singleResult();
      rule.getManagementService().executeJob(job.getId());
    }

    // and the tasks are completed
    List<Task> afterBoundaryTasks = rule.taskQuery().list();
    Assert.assertEquals(3, afterBoundaryTasks.size());

    for (Task afterBoundaryTask : afterBoundaryTasks) {
      rule.getTaskService().complete(afterBoundaryTask.getId());
    }

    rule.messageCorrelation("ReceiveTaskMessage").correlate();

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initTimer.6")
  public void testInitTimerThrowError() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.ERROR_INDICATOR_VARIABLE, true);
    rule.messageCorrelation("ReceiveTaskMessage").correlate();

    // then
    // there is only one task since the task after the message boundary event has been cancelled due to bug CAM-3727 in 7.2.0
    Assert.assertEquals(1, rule.taskQuery().count());

    Task afterErrorTask = rule.taskQuery().taskDefinitionKey("escalatedTask").singleResult();
    Assert.assertNotNull(afterErrorTask);

    // and
    rule.getTaskService().complete(afterErrorTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initTimer.7")
  public void testInitTimerThrowUnhandledException() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_INDICATOR_VARIABLE, true);
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_MESSAGE_VARIABLE, "unhandledException");

    // then
    try {
      rule.messageCorrelation("ReceiveTaskMessage").correlate();
      Assert.fail("should throw a ThrowBpmnErrorDelegateException");

    } catch (ThrowBpmnErrorDelegateException e) {
      Assert.assertEquals("unhandledException", e.getMessage());
    }
  }

  @Test
  @ScenarioUnderTest("initMessage.1")
  public void testInitMessageCompletionCase1() {
    // given
    Task afterBoundaryTask = rule.taskQuery().taskDefinitionKey("afterBoundaryTask").singleResult();

    // when
    rule.messageCorrelation("ReceiveTaskMessage").correlate();
    rule.getTaskService().complete(afterBoundaryTask.getId());

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initMessage.2")
  public void testInitMessageCompletionCase2() {
    // given
    Task afterBoundaryTask = rule.taskQuery().taskDefinitionKey("afterBoundaryTask").singleResult();

    // when
    rule.getTaskService().complete(afterBoundaryTask.getId());
    rule.messageCorrelation("ReceiveTaskMessage").correlate();

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initMessage.3")
  public void testInitMessageActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
        describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("afterBoundaryTask")
        .beginScope("outerSubProcess")
          .beginScope("innerSubProcess")
            .activity("innerSubProcessTask")
        .done());
  }

  @Test
  @ScenarioUnderTest("initMessage.4")
  public void testInitMessageDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initMessage.5")
  public void testInitMessageTriggerBoundary() {
    // when the boundary event is triggered another 2 times
    for (int i = 0; i < 2; i++) {
      rule.messageCorrelation("BoundaryEventMessage").correlate();
    }

    // and the tasks are completed
    List<Task> afterBoundaryTasks = rule.taskQuery().list();
    Assert.assertEquals(3, afterBoundaryTasks.size());

    for (Task afterBoundaryTask : afterBoundaryTasks) {
      rule.getTaskService().complete(afterBoundaryTask.getId());
    }

    rule.messageCorrelation("ReceiveTaskMessage").correlate();

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initMessage.6")
  public void testInitMessageThrowError() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.ERROR_INDICATOR_VARIABLE, true);
    rule.messageCorrelation("ReceiveTaskMessage").correlate();

    // then
    // there is only one task since the task after the message boundary event has been cancelled due to bug CAM-3727 in 7.2.0
    Assert.assertEquals(1, rule.taskQuery().count());

    Task afterErrorTask = rule.taskQuery().taskDefinitionKey("escalatedTask").singleResult();
    Assert.assertNotNull(afterErrorTask);

    // and
    rule.getTaskService().complete(afterErrorTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initMessage.7")
  public void testInitMessageThrowUnhandledException() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_INDICATOR_VARIABLE, true);
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_MESSAGE_VARIABLE, "unhandledException");

    // then
    try {
      rule.messageCorrelation("ReceiveTaskMessage").correlate();
      Assert.fail("should throw a ThrowBpmnErrorDelegateException");

    } catch (ThrowBpmnErrorDelegateException e) {
      Assert.assertEquals("unhandledException", e.getMessage());
    }
  }

}
