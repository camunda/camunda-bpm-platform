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
package org.camunda.bpm.qa.upgrade.scenarios720.multiinstance;

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

@ScenarioUnderTest("SequentialMultiInstanceSubprocessScenario")
@Origin("7.2.0")
public class SequentialMultiInstanceScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("init.1")
  public void testInitCompletionCase1() {
    // given
    Task subProcessTask = rule.taskQuery().taskDefinitionKey("subProcessTask").singleResult();

    // when the first instance and the other two instances are completed
    rule.getTaskService().complete(subProcessTask.getId());

    for (int i = 0; i < 2; i++) {
      subProcessTask = rule.taskQuery().taskDefinitionKey("subProcessTask").singleResult();
      Assert.assertNotNull(subProcessTask);
      rule.getTaskService().complete(subProcessTask.getId());
    }

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.2")
  public void testInitActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .beginMiBody("miSubProcess")
          // the subprocess itself misses because it was no scope in 7.2
          .activity("subProcessTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("init.3")
  public void testInitDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.4")
  public void testInitThrowError() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task miSubprocessTask = rule.taskQuery().taskDefinitionKey("subProcessTask").singleResult();

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.ERROR_INDICATOR_VARIABLE, true);
    rule.getTaskService().complete(miSubprocessTask.getId());

    // then
    Task escalatedTask = rule.taskQuery().singleResult();
    Assert.assertEquals("escalatedTask", escalatedTask.getTaskDefinitionKey());
    Assert.assertNotNull(escalatedTask);

    rule.getTaskService().complete(escalatedTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.5")
  public void testInitUnhandledException() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task miSubprocessTask = rule.taskQuery().taskDefinitionKey("subProcessTask").list().get(0);

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_INDICATOR_VARIABLE, true);
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_MESSAGE_VARIABLE, "unhandledException");

    // then
    try {
      rule.getTaskService().complete(miSubprocessTask.getId());
      Assert.fail("should throw a ThrowBpmnErrorDelegateException");

    } catch (ThrowBpmnErrorDelegateException e) {
      Assert.assertEquals("unhandledException", e.getMessage());
    }
  }

  @Test
  @ScenarioUnderTest("initNonInterruptingBoundaryEvent.1")
  public void testInitNonInterruptingBoundaryEventCompletionCase1() {
    // given
    Task subProcessTask = rule.taskQuery().taskDefinitionKey("subProcessTask").singleResult();
    Task afterBoundaryTask = rule.taskQuery().taskDefinitionKey("afterBoundaryTask").singleResult();

    // when the first instance and the other two instances are completed
    rule.getTaskService().complete(subProcessTask.getId());

    for (int i = 0; i < 2; i++) {
      subProcessTask = rule.taskQuery().taskDefinitionKey("subProcessTask").singleResult();
      Assert.assertNotNull(subProcessTask);
      rule.getTaskService().complete(subProcessTask.getId());
    }

    rule.getTaskService().complete(afterBoundaryTask.getId());

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initNonInterruptingBoundaryEvent.2")
  public void testInitNonInterruptingBoundaryEventCompletionCase2() {
    // given
    Task subProcessTask = rule.taskQuery().taskDefinitionKey("subProcessTask").singleResult();
    Task afterBoundaryTask = rule.taskQuery().taskDefinitionKey("afterBoundaryTask").singleResult();

    // when the first instance and the other two instances are completed
    rule.getTaskService().complete(afterBoundaryTask.getId());

    rule.getTaskService().complete(subProcessTask.getId());
    for (int i = 0; i < 2; i++) {
      subProcessTask = rule.taskQuery().taskDefinitionKey("subProcessTask").singleResult();
      Assert.assertNotNull(subProcessTask);
      rule.getTaskService().complete(subProcessTask.getId());
    }


    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initNonInterruptingBoundaryEvent.3")
  public void testInitNonInterruptingBoundaryEventCompletionCase3() {
    // given
    Task subProcessTask = rule.taskQuery().taskDefinitionKey("subProcessTask").singleResult();
    Task afterBoundaryTask = rule.taskQuery().taskDefinitionKey("afterBoundaryTask").singleResult();

    // when the first instance and the other two instances are completed
    rule.getTaskService().complete(subProcessTask.getId());

    rule.getTaskService().complete(afterBoundaryTask.getId());

    for (int i = 0; i < 2; i++) {
      subProcessTask = rule.taskQuery().taskDefinitionKey("subProcessTask").singleResult();
      Assert.assertNotNull(subProcessTask);
      rule.getTaskService().complete(subProcessTask.getId());
    }


    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initNonInterruptingBoundaryEvent.4")
  public void testInitNonInterruptingBoundaryEventActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("afterBoundaryTask")
        .beginMiBody("miSubProcess")
          // the subprocess itself misses because it was no scope in 7.2
          .activity("subProcessTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("initNonInterruptingBoundaryEvent.5")
  public void testInitNonInterruptingBoundaryEventDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initNonInterruptingBoundaryEvent.6")
  public void testInitNonInterruptingBoundaryEventThrowError() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task miSubprocessTask = rule.taskQuery().taskDefinitionKey("subProcessTask").singleResult();
    Task afterBoundaryTask = rule.taskQuery().taskDefinitionKey("afterBoundaryTask").singleResult();

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.ERROR_INDICATOR_VARIABLE, true);
    rule.getTaskService().complete(miSubprocessTask.getId());

    // then
    Assert.assertEquals(2, rule.taskQuery().count());

    Task escalatedTask = rule.taskQuery().taskDefinitionKey("escalatedTask").singleResult();
    Assert.assertNotNull(escalatedTask);

    // and
    rule.getTaskService().complete(escalatedTask.getId());
    rule.getTaskService().complete(afterBoundaryTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initNonInterruptingBoundaryEvent.7")
  public void testInitNonInterruptingBoundaryEventThrowUnhandledException() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task miSubprocessTask = rule.taskQuery().taskDefinitionKey("subProcessTask").list().get(0);

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_INDICATOR_VARIABLE, true);
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_MESSAGE_VARIABLE, "unhandledException");

    // then
    try {
      rule.getTaskService().complete(miSubprocessTask.getId());
      Assert.fail("should throw a ThrowBpmnErrorDelegateException");

    } catch (ThrowBpmnErrorDelegateException e) {
      Assert.assertEquals("unhandledException", e.getMessage());
    }
  }

}
