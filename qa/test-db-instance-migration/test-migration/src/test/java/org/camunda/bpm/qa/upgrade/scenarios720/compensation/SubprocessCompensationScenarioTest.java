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
package org.camunda.bpm.qa.upgrade.scenarios720.compensation;

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
 * @author Thorben Lindhauer
 *
 */
@ScenarioUnderTest("SubprocessCompensationScenario")
@Origin("7.2.0")
public class SubprocessCompensationScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("init.1")
  public void testInitCompletion() {
    // when compensation is thrown
    Task beforeCompensationTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensationTask.getId());

    // then there is an active compensation handler task
    Task compensationHandlerTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(compensationHandlerTask);
    Assert.assertEquals("undoTask", compensationHandlerTask.getTaskDefinitionKey());

    // and it can be completed such that the process instance ends successfully
    rule.getTaskService().complete(compensationHandlerTask.getId());

    Task afterCompensateTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterCompensateTask);
    Assert.assertEquals("afterCompensate", afterCompensateTask.getTaskDefinitionKey());

    rule.getTaskService().complete(afterCompensateTask.getId());

    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.2")
  public void testInitDeletion() {
    // when compensation is thrown
    Task beforeCompensationTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensationTask.getId());

    // then the process instance can be deleted
    rule.getRuntimeService().deleteProcessInstance(rule.processInstance().getId(), "");

    // and the process is ended
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.3")
  public void testInitActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when compensation is thrown
    Task beforeCompensationTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensationTask.getId());

    // then the activity instance tree is meaningful
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("throwCompensate")
        .beginScope("subProcess")
          .activity("undoTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("init.triggerCompensation.1")
  public void testInitTriggerCompensationCompletion() {
    // given active compensation
    Task compensationHandlerTask = rule.taskQuery().singleResult();

    // then it is possible to complete compensation and the follow-up task
    rule.getTaskService().complete(compensationHandlerTask.getId());

    Task afterCompensateTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterCompensateTask);
    Assert.assertEquals("afterCompensate", afterCompensateTask.getTaskDefinitionKey());

    rule.getTaskService().complete(afterCompensateTask.getId());

    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.triggerCompensation.2")
  public void testInitTriggerCompensationDeletion() {
    // given active compensation

    // then the process instance can be deleted
    rule.getRuntimeService().deleteProcessInstance(rule.processInstance().getId(), "");

    // and the process is ended
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.triggerCompensation.3")
  public void testInitTriggerCompensationActivityInstanceTree() {
    // given active compensation
    ProcessInstance instance = rule.processInstance();

    // then the activity instance tree is meaningful
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .beginScope("throwCompensate")
        // due to different activity instance id assingment in >= 7.4,
        // the subProcess instance is not represented and undoTask is a child of throwCompensate
//        .beginScope("subProcess")
          .activity("undoTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("init.concurrent.1")
  public void testInitConcurrentCompletion() {
    // when compensation is thrown
    Task beforeCompensationTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensationTask.getId());

    // then there are two active compensation handler task
    Assert.assertEquals(2, rule.taskQuery().count());
    Task undoTask1 = rule.taskQuery().taskDefinitionKey("undoTask1").singleResult();
    Assert.assertNotNull(undoTask1);

    Task undoTask2 = rule.taskQuery().taskDefinitionKey("undoTask2").singleResult();
    Assert.assertNotNull(undoTask2);

    // and they can be completed such that the process instance ends successfully
    rule.getTaskService().complete(undoTask1.getId());
    rule.getTaskService().complete(undoTask2.getId());

    Task afterCompensateTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterCompensateTask);
    Assert.assertEquals("afterCompensate", afterCompensateTask.getTaskDefinitionKey());

    rule.getTaskService().complete(afterCompensateTask.getId());

    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.concurrent.2")
  public void testInitConcurrentDeletion() {
    // when compensation is thrown
    Task beforeCompensationTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensationTask.getId());

    // then the process instance can be deleted
    rule.getRuntimeService().deleteProcessInstance(rule.processInstance().getId(), "");

    // and the process is ended
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.concurrent.3")
  public void testInitConcurrentActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when compensation is thrown
    Task beforeCompensationTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensationTask.getId());

    // then the activity instance tree is meaningful
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("throwCompensate")
        .beginScope("subProcess")
          .activity("undoTask1")
          .activity("undoTask2")
      .done());
  }

  @Test
  @ScenarioUnderTest("init.concurrent.triggerCompensation.1")
  public void testInitConcurrentTriggerCompensationCompletion() {
    // given active compensation
    Task undoTask1 = rule.taskQuery().taskDefinitionKey("undoTask1").singleResult();
    Task undoTask2 = rule.taskQuery().taskDefinitionKey("undoTask2").singleResult();

    // then it is possible to complete compensation and the follow-up task
    rule.getTaskService().complete(undoTask1.getId());
    rule.getTaskService().complete(undoTask2.getId());

    Task afterCompensateTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterCompensateTask);
    Assert.assertEquals("afterCompensate", afterCompensateTask.getTaskDefinitionKey());

    rule.getTaskService().complete(afterCompensateTask.getId());

    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.concurrent.triggerCompensation.2")
  public void testInitConcurrentTriggerCompensationDeletion() {
    // given active compensation

    // then the process instance can be deleted
    rule.getRuntimeService().deleteProcessInstance(rule.processInstance().getId(), "");

    // and the process is ended
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.concurrent.triggerCompensation.3")
  public void testInitConcurrentTriggerCompensationActivityInstanceTree() {
    // given active compensation
    ProcessInstance instance = rule.processInstance();

    // then the activity instance tree is meaningful
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .beginScope("throwCompensate")
        // due to different activity instance id assingment in >= 7.4,
        // the subProcess instance is not represented and undoTask is a child of throwCompensate
//        .beginScope("subProcess")
          .activity("undoTask1")
          .activity("undoTask2")
      .done());
  }
}
