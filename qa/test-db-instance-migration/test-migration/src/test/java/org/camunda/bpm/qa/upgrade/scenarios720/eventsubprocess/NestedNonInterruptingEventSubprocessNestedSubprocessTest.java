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

@ScenarioUnderTest("NestedNonInterruptingEventSubprocessNestedSubprocessScenario")
@Origin("7.2.0")
public class NestedNonInterruptingEventSubprocessNestedSubprocessTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("init.1")
  public void testInitCompletionCase1() {
    // given
    Task outerSubProcessTask = rule.taskQuery().taskDefinitionKey("outerSubProcessTask").singleResult();
    Task eventSubprocessTask = rule.taskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();

    // when
    rule.getTaskService().complete(outerSubProcessTask.getId());
    rule.getTaskService().complete(eventSubprocessTask.getId());

    // then
    Task innerSubprocessTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(innerSubprocessTask);
    rule.getTaskService().complete(innerSubprocessTask.getId());

    // and
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.2")
  public void testInitCompletionCase2() {
    // given
    Task outerSubProcessTask = rule.taskQuery().taskDefinitionKey("outerSubProcessTask").singleResult();
    Task eventSubprocessTask = rule.taskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();

    // when
    rule.getTaskService().complete(eventSubprocessTask.getId());
    rule.getTaskService().complete(outerSubProcessTask.getId());

    // then
    Task innerSubprocessTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(innerSubprocessTask);
    rule.getTaskService().complete(innerSubprocessTask.getId());

    // and
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
          .beginScope("outerSubProcess")
            .activity("outerSubProcessTask")
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

  @Test
  @ScenarioUnderTest("init.5")
  public void testInitThrowError() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task eventSubProcessTask = rule.taskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();

    // when
    rule.getTaskService().complete(eventSubProcessTask.getId());

    // and
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.ERROR_INDICATOR_VARIABLE, true);
    Task innerSubProcessTask = rule.taskQuery().taskDefinitionKey("innerSubProcessTask").singleResult();
    Assert.assertNotNull(innerSubProcessTask);
    rule.getTaskService().complete(innerSubProcessTask.getId());

    // then
    Task afterErrorTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterErrorTask);
    Assert.assertEquals("escalatedTask", afterErrorTask.getTaskDefinitionKey());

    // and
    rule.getTaskService().complete(afterErrorTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.6")
  public void testInitThrowUnhandledException() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task eventSubProcessTask = rule.taskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();

    // when
    rule.getTaskService().complete(eventSubProcessTask.getId());

    // and
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_INDICATOR_VARIABLE, true);
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_MESSAGE_VARIABLE, "unhandledException");
    Task innerSubProcessTask = rule.taskQuery().taskDefinitionKey("innerSubProcessTask").singleResult();
    Assert.assertNotNull(innerSubProcessTask);

    // then
    try {
      rule.getTaskService().complete(innerSubProcessTask.getId());
      Assert.fail("should throw a ThrowBpmnErrorDelegateException");

    } catch (ThrowBpmnErrorDelegateException e) {
      Assert.assertEquals("unhandledException", e.getMessage());
    }
  }

  @Test
  @ScenarioUnderTest("init.innerSubProcess.1")
  public void testInitInnerSubProcessCompletionCase1() {
    // given
    Task outerSubProcessTask = rule.taskQuery().taskDefinitionKey("outerSubProcessTask").singleResult();
    Task innerSubProcessTask = rule.taskQuery().taskDefinitionKey("innerSubProcessTask").singleResult();

    // when
    rule.getTaskService().complete(outerSubProcessTask.getId());
    rule.getTaskService().complete(innerSubProcessTask.getId());

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.innerSubProcess.2")
  public void testInitInnerSubProcessCompletionCase2() {
    // given
    Task outerSubProcessTask = rule.taskQuery().taskDefinitionKey("outerSubProcessTask").singleResult();
    Task innerSubProcessTask = rule.taskQuery().taskDefinitionKey("innerSubProcessTask").singleResult();

    // when
    rule.getTaskService().complete(innerSubProcessTask.getId());
    rule.getTaskService().complete(outerSubProcessTask.getId());

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.innerSubProcess.3")
  public void testInitInnerSubProcessActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
        describeActivityInstanceTree(instance.getProcessDefinitionId())
          .beginScope("outerSubProcess")
            .activity("outerSubProcessTask")
            // eventSubProcess was previously no scope so it misses here
            .beginScope("innerSubProcess")
              .activity("innerSubProcessTask")
        .done());
  }

  @Test
  @ScenarioUnderTest("init.innerSubProcess.4")
  public void testInitInnerSubProcessDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.innerSubProcess.5")
  public void testInitInnerSubProcessThrowError() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task innerSubProcessTask = rule.taskQuery().taskDefinitionKey("innerSubProcessTask").singleResult();

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.ERROR_INDICATOR_VARIABLE, true);
    rule.getTaskService().complete(innerSubProcessTask.getId());

    // then
    Task afterErrorTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterErrorTask);
    Assert.assertEquals("escalatedTask", afterErrorTask.getTaskDefinitionKey());

    // and
    rule.getTaskService().complete(afterErrorTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.innerSubProcess.6")
  public void testInitInnerSubProcessThrowUnhandledException() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task innerSubProcessTask = rule.taskQuery().taskDefinitionKey("innerSubProcessTask").singleResult();

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_INDICATOR_VARIABLE, true);
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_MESSAGE_VARIABLE, "unhandledException");

    // then
    try {
      rule.getTaskService().complete(innerSubProcessTask.getId());
      Assert.fail("should throw a ThrowBpmnErrorDelegateException");

    } catch (ThrowBpmnErrorDelegateException e) {
      Assert.assertEquals("unhandledException", e.getMessage());
    }
  }

}
