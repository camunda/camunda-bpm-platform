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

import java.util.List;

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

/**
 * @author Thorben Lindhauer
 *
 */
@ScenarioUnderTest("TwoLevelNestedNonInterruptingEventSubprocessScenario")
@Origin("7.2.0")
public class TwoLevelNestedNonInterruptingEventSubprocessScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("initLevel1.1")
  public void testInitLevel1CompletionCase1() {
    // given
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();
    Task innerTask = rule.taskQuery().taskDefinitionKey("subProcessTask").singleResult();

    // when
    rule.getTaskService().complete(outerTask.getId());
    rule.getTaskService().complete(innerTask.getId());

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initLevel1.2")
  public void testInitLevel1CompletionCase2() {
    // given
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();
    Task innerTask = rule.taskQuery().taskDefinitionKey("subProcessTask").singleResult();

    // when
    rule.getTaskService().complete(innerTask.getId());
    rule.getTaskService().complete(outerTask.getId());

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initLevel1.3")
  public void testInitLevel1CompletionCase3() {
    // given
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();
    Task innerTask = rule.taskQuery().taskDefinitionKey("subProcessTask").singleResult();

    // when
    rule.messageCorrelation("InnerEventSubProcessMessage").correlate();

    // then
    Assert.assertEquals(3, rule.taskQuery().count());

    Task innerEventSubprocessTask = rule.taskQuery().taskDefinitionKey("innerEventSubProcessTask").singleResult();
    Assert.assertNotNull(innerEventSubprocessTask);

    // and
    rule.getTaskService().complete(innerTask.getId());
    rule.getTaskService().complete(innerEventSubprocessTask.getId());
    rule.getTaskService().complete(outerTask.getId());

    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initLevel1.4")
  public void testInitLevel1ActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
        describeActivityInstanceTree(instance.getProcessDefinitionId())
          .activity("outerTask")
          // eventSubProcess was previously no scope so it misses here
          .beginScope("subProcess")
            .activity("subProcessTask")
        .done());
  }

  @Test
  @ScenarioUnderTest("initLevel1.5")
  public void testInitLevel1Deletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @ScenarioUnderTest("initLevel1.6")
  public void testInitLevel1ThrowError() {
    // given
    ProcessInstance instance = rule.processInstance();
    rule.messageCorrelation("InnerEventSubProcessMessage").correlate();
    Task innerEventSubprocessTask = rule.taskQuery().taskDefinitionKey("innerEventSubProcessTask").singleResult();

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.ERROR_INDICATOR_VARIABLE, true);
    rule.getTaskService().complete(innerEventSubprocessTask.getId());

    // then
    Task escalatedTask = rule.taskQuery().singleResult();
    Assert.assertEquals("escalatedTask", escalatedTask.getTaskDefinitionKey());
    Assert.assertNotNull(escalatedTask);

    rule.getTaskService().complete(escalatedTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initLevel1.7")
  public void testInitLevel1ThrowUnhandledException() {
    // given
    ProcessInstance instance = rule.processInstance();
    rule.messageCorrelation("InnerEventSubProcessMessage").correlate();
    Task innerEventSubprocessTask = rule.taskQuery().taskDefinitionKey("innerEventSubProcessTask").singleResult();

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_INDICATOR_VARIABLE, true);
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_MESSAGE_VARIABLE, "unhandledException");

    // then
    try {
      rule.getTaskService().complete(innerEventSubprocessTask.getId());
      Assert.fail("should throw a ThrowBpmnErrorDelegateException");

    } catch (ThrowBpmnErrorDelegateException e) {
      Assert.assertEquals("unhandledException", e.getMessage());
    }
  }

  @Test
  @ScenarioUnderTest("initLevel1.initLevel2.1")
  public void testInitLevel1InitLevel2CompletionCase1() {
    // given
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();
    Task innerTask = rule.taskQuery().taskDefinitionKey("subProcessTask").singleResult();
    Task innerEventSubprocessTask = rule.taskQuery().taskDefinitionKey("innerEventSubProcessTask").singleResult();

    // when
    rule.getTaskService().complete(outerTask.getId());
    rule.getTaskService().complete(innerTask.getId());
    rule.getTaskService().complete(innerEventSubprocessTask.getId());

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initLevel1.initLevel2.2")
  public void testInitLevel1InitLevel2CompletionCase2() {
    // given
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();
    Task innerTask = rule.taskQuery().taskDefinitionKey("subProcessTask").singleResult();
    Task innerEventSubprocessTask = rule.taskQuery().taskDefinitionKey("innerEventSubProcessTask").singleResult();

    // when
    rule.getTaskService().complete(innerEventSubprocessTask.getId());
    rule.getTaskService().complete(innerTask.getId());
    rule.getTaskService().complete(outerTask.getId());

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initLevel1.initLevel2.3")
  public void testInitLevel1InitLevel2CompletionCase3() {
    // given
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();
    Task innerTask = rule.taskQuery().taskDefinitionKey("subProcessTask").singleResult();

    // when (the inner subprocess is triggered another time)
    rule.messageCorrelation("InnerEventSubProcessMessage").correlate();

    // then
    Assert.assertEquals(4, rule.taskQuery().count());

    List<Task> innerEventSubprocessTasks = rule.taskQuery().taskDefinitionKey("innerEventSubProcessTask").list();
    Assert.assertEquals(2, innerEventSubprocessTasks.size());

    // and
    rule.getTaskService().complete(innerTask.getId());
    rule.getTaskService().complete(innerEventSubprocessTasks.get(0).getId());
    rule.getTaskService().complete(outerTask.getId());
    rule.getTaskService().complete(innerEventSubprocessTasks.get(1).getId());

    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initLevel1.initLevel2.4")
  public void testInitLevel1InitLevel2ActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
        describeActivityInstanceTree(instance.getProcessDefinitionId())
          .activity("outerTask")
          // eventSubProcess was previously no scope so it misses here
          .beginScope("subProcess")
            .activity("subProcessTask")
            // eventSubProcess was previously no scope so it misses here
            .activity("innerEventSubProcessTask")
        .done());
  }

  @Test
  @ScenarioUnderTest("initLevel1.initLevel2.5")
  public void testInitLevel1InitLevel2Deletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @ScenarioUnderTest("initLevel1.initLevel2.6")
  public void testInitLevel1InitLevel2ThrowError() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task innerEventSubprocessTask = rule.taskQuery().taskDefinitionKey("innerEventSubProcessTask").singleResult();

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.ERROR_INDICATOR_VARIABLE, true);
    rule.getTaskService().complete(innerEventSubprocessTask.getId());

    // then
    Task escalatedTask = rule.taskQuery().singleResult();
    Assert.assertEquals("escalatedTask", escalatedTask.getTaskDefinitionKey());
    Assert.assertNotNull(escalatedTask);

    rule.getTaskService().complete(escalatedTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initLevel1.initLevel2.7")
  public void testInitLevel1InitLevel2ThrowUnhandledException() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task innerEventSubprocessTask = rule.taskQuery().taskDefinitionKey("innerEventSubProcessTask").singleResult();

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_INDICATOR_VARIABLE, true);
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_MESSAGE_VARIABLE, "unhandledException");

    // then
    try {
      rule.getTaskService().complete(innerEventSubprocessTask.getId());
      Assert.fail("should throw a ThrowBpmnErrorDelegateException");

    } catch (ThrowBpmnErrorDelegateException e) {
      Assert.assertEquals("unhandledException", e.getMessage());
    }
  }
}
