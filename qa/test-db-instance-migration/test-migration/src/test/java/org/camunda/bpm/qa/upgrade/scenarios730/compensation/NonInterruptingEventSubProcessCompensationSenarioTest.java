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
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests that the 7.2-to-current migration logic (where an event subprocess was no scope)
 * does not precede over 7.3-to-current migration logic (where a throwing compensation event was no scope)
 *
 * @author Thorben Lindhauer
 */
@ScenarioUnderTest("NonInterruptingEventSubProcessCompensationScenario")
@Origin("7.3.0")
public class NonInterruptingEventSubProcessCompensationSenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("init.throwCompensate.1")
  public void testInitThrowCompensateCompletionCase1() {
    // given
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();
    Task undoTask = rule.taskQuery().taskDefinitionKey("undoTask").singleResult();

    // when
    rule.getTaskService().complete(undoTask.getId());

    // then it is possible to complete the process successfully
    Task afterCompensateTask = rule.taskQuery().taskDefinitionKey("afterCompensate").singleResult();
    Assert.assertNotNull(afterCompensateTask);
    Assert.assertEquals("afterCompensate", afterCompensateTask.getTaskDefinitionKey());

    rule.getTaskService().complete(afterCompensateTask.getId());

    rule.getTaskService().complete(outerTask.getId());

    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.throwCompensate.2")
  public void testInitThrowCompensateCompletionCase2() {
    // given
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();
    Task undoTask = rule.taskQuery().taskDefinitionKey("undoTask").singleResult();

    // when
    rule.getTaskService().complete(outerTask.getId());
    rule.getTaskService().complete(undoTask.getId());

    // then it is possible to complete the process successfully
    Task afterCompensateTask = rule.taskQuery().taskDefinitionKey("afterCompensate").singleResult();
    Assert.assertNotNull(afterCompensateTask);
    Assert.assertEquals("afterCompensate", afterCompensateTask.getTaskDefinitionKey());

    rule.getTaskService().complete(afterCompensateTask.getId());

    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.throwCompensate.3")
  public void testInitThrowCompensateCompletion() {
    // given
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();
    Task undoTask = rule.taskQuery().taskDefinitionKey("undoTask").singleResult();

    // when
    rule.getTaskService().complete(undoTask.getId());

    // then it is possible to complete the process successfully
    rule.getTaskService().complete(outerTask.getId());

    Task afterCompensateTask = rule.taskQuery().taskDefinitionKey("afterCompensate").singleResult();
    Assert.assertNotNull(afterCompensateTask);
    Assert.assertEquals("afterCompensate", afterCompensateTask.getTaskDefinitionKey());

    rule.getTaskService().complete(afterCompensateTask.getId());

    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.throwCompensate.4")
  public void testInitThrowCompensateDeletion() {
    // given
    ProcessInstance processInstance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(processInstance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.throwCompensate.5")
  public void testInitThrowCompensateActivityInstanceTree() {
    // given
    ProcessInstance processInstance = rule.processInstance();

    // when
    ActivityInstance tree = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // then
    Assert.assertNotNull(tree);
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
        // - the eventSubProcess instance is missing because we wrongly assume that there is no
        //   execution for it (see CAM-4914)
        // - the undoTask instance is a child of the throwCompensate instance for the same reason;
        //   the throwCompensate instance wrongly receives the eventSubProcess instance id
        //   and undoTask is a child of it
        .beginScope("throwCompensate")
          .activity("undoTask")
//        .beginScope("eventSubProcess")
//          .activity("throwCompensate")
//          .activity("undoTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("init.throwCompensate.6")
  public void testInitThrowCompensateCancelEventSubProcess() {
    // given
    ProcessInstance processInstance = rule.processInstance();
    Task outerTask = rule.taskQuery().taskDefinitionKey("outerTask").singleResult();
    Task undoTask = rule.taskQuery().taskDefinitionKey("undoTask").singleResult();

    // when compensation is finished
    rule.getTaskService().complete(undoTask.getId());

    Task afterCompensateTask = rule.taskQuery().taskDefinitionKey("afterCompensate").singleResult();
    Assert.assertNotNull(afterCompensateTask);

    // then the event sub process instance can be cancelled with modification
    ActivityInstance tree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    ActivityInstance afterCompensateInstance = tree.getActivityInstances("afterCompensate")[0];

    rule.getRuntimeService()
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(afterCompensateInstance.getId())
      .execute();

    // and the remaining outerTask can be completed successfully
    rule.getTaskService().complete(outerTask.getId());
    rule.assertScenarioEnded();


  }
}
