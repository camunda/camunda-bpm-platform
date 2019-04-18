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
package org.camunda.bpm.qa.upgrade.scenarios730.compensation;

import static org.camunda.bpm.qa.upgrade.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.qa.upgrade.util.ActivityInstanceAssert.describeActivityInstanceTree;

import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests that the 7.2-to-current migration logic (where an event subprocess was no scope)
 * does not precede over 7.3-to-current migration logic (where a throwing compensation event was no scope)
 *
 * @author Thorben Lindhauer
 */
@ScenarioUnderTest("InterruptingEventSubProcessNestedCompensationScenario")
@Origin("7.3.0")
public class InterruptingEventSubProcessNestedCompensationSenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("init.throwCompensate.1")
  public void testInitThrowCompensateCompletionCase1() {
    // given
    Task undoTask = rule.taskQuery().singleResult();

    // when
    rule.getTaskService().complete(undoTask.getId());

    // then it is possible to complete the process successfully
    // by completing the sub process regularly
    Task afterCompensateTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterCompensateTask);
    Assert.assertEquals("afterCompensate", afterCompensateTask.getTaskDefinitionKey());

    rule.getTaskService().complete(afterCompensateTask.getId());

    rule.assertScenarioEnded();
  }

  /**
   * Fails because wrongly destroy the sub process scope execution when completing the
   * compensation throw event. See CAM-4914
   */
  @Ignore
  @Test
  @ScenarioUnderTest("init.throwCompensate.2")
  public void testInitThrowCompensateCompletionCase2() {
    // given
    Task undoTask = rule.taskQuery().singleResult();

    // when
    rule.getTaskService().complete(undoTask.getId());

    // then it is possible to complete the process successfully
    // by triggering the message boundary event
    rule.messageCorrelation("BoundaryEventMessage").correlate();
    Task afterBoundaryTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterBoundaryTask);
    Assert.assertEquals("afterBoundaryTask", afterBoundaryTask.getTaskDefinitionKey());

    rule.getTaskService().complete(afterBoundaryTask.getId());

    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.throwCompensate.3")
  public void testInitThrowCompensateDeletion() {
    // given
    ProcessInstance processInstance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(processInstance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.throwCompensate.4")
  public void testInitThrowCompensateActivityInstanceTree() {
    // given
    ProcessInstance processInstance = rule.processInstance();

    // when
    ActivityInstance tree = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // then
    Assert.assertNotNull(tree);
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        // - the eventSubprocess instance is missing, because we wrongly assume that the event
        //   sub process scope execution is missing (see CAM-4914)
        // - undoTask is a child of throwCompensate for the same reason; due to the wrong assumption,
        //   innerSubProcess receives the eventSubProcess id, throwCompensate receives the innerSubProcess id.
        //   The latter is the parent id for the undoTask instance.
        .beginScope("innerSubProcess")
          .beginScope("throwCompensate")
            .activity("undoTask")
//        .beginScope("eventSubProcess")
//          .beginScope("innerSubProcess")
//            .activity("throwCompensate")
//            .activity("undoTask")
      .done());
  }
}
