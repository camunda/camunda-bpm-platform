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

import java.util.List;

import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 */
@ScenarioUnderTest("NestedMultiInstanceCompensationScenario")
@Origin("7.3.0")
public class NestedMultiInstanceCompensationScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("init.throwInner.1")
  public void testInitThrowInnerCompletion() {
    // given
    List<Task> undoTasks = rule.taskQuery().list();

    // when
    for (Task undoTask : undoTasks) {
      rule.getTaskService().complete(undoTask.getId());
    }

    // then the process has successfully completed
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.throwInner.2")
  public void testInitThrowInnerDeletion() {
    // given
    ProcessInstance processInstance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(processInstance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.throwInner.3")
  public void testInitThrowInnerActivityInstanceTree() {
    // given
    ProcessInstance processInstance = rule.processInstance();

    // when
    ActivityInstance tree = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // then
    Assert.assertNotNull(tree);
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginMiBody("subProcess")
          .beginScope("subProcess")
            .beginScope("throwCompensate")
            // - no mi-body due to missing event scope execution
            // - no undoSubProcess activity instances due to missing concurrent executions
            // - undoTask instances are children of throwCompensate due to missing concurrent executions
              .activity("undoTask")
              .activity("undoTask")
            .endScope()
          .endScope()
          .beginScope("subProcess")
            .beginScope("throwCompensate")
              .activity("undoTask")
              .activity("undoTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("init.throwOuter.1")
  public void testInitThrowOuterCompletion() {
    // given
    List<Task> undoTasks = rule.taskQuery().list();

    // when
    for (Task undoTask : undoTasks) {
      rule.getTaskService().complete(undoTask.getId());
    }

    // then the process has successfully completed
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.throwOuter.2")
  public void testInitThrowOuterDeletion() {
    // given
    ProcessInstance processInstance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(processInstance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.throwOuter.3")
  public void testInitThrowOuterActivityInstanceTree() {
    // given
    ProcessInstance processInstance = rule.processInstance();

    // when
    ActivityInstance tree = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // then
    Assert.assertNotNull(tree);
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        // - no mi-bodies due to missing event scope executions
        // - no outerSubprocess activity instances due to missing concurrent executions
        //   and different activity instance id assignment in >= 7.4
        // - innerSubProcess instances are children of throwCompensate due to missing concurrent executions
        //   and different activty instance id assignment in >= 7.4
        .beginScope("throwCompensate", "outerSubProcess")
          .beginScope("innerSubProcess")
            .beginScope("undoSubProcess")
              .activity("undoTask")
              .activity("undoTask")
            .endScope()
          .endScope()
          .beginScope("innerSubProcess")
            .beginScope("undoSubProcess")
              .activity("undoTask")
              .activity("undoTask")
      .done());
  }
}
