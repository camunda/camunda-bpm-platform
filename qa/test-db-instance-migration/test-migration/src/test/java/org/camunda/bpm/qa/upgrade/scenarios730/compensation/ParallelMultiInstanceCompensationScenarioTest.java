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
 *
 */
@ScenarioUnderTest("ParallelMultiInstanceCompensationScenario")
@Origin("7.3.0")
public class ParallelMultiInstanceCompensationScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("singleActivityHandler.multiInstancePartial.1")
  public void testSingleActivityHandlerMultiInstancePartialCompletion() {
    // given the last multi instance task
    Task lastMiTask = rule.taskQuery().singleResult();

    // when completing it
    rule.getTaskService().complete(lastMiTask.getId());

    // then it is possible to throw compensation, compensate the three instances,
    // and finish the process successfully
    Task beforeCompensateTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(beforeCompensateTask);
    rule.getTaskService().complete(beforeCompensateTask.getId());

    List<Task> miCompensationTasks = rule.taskQuery().list();
    Assert.assertEquals(3, miCompensationTasks.size());

    for (int i = 0; i < miCompensationTasks.size(); i++) {
      Assert.assertEquals(3 - i, rule.taskQuery().count());

      Task compensationTask = miCompensationTasks.get(i);
      Assert.assertEquals("undoTask", compensationTask.getTaskDefinitionKey());

      rule.getTaskService().complete(compensationTask.getId());
    }

    Task afterCompensateTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterCompensateTask);
    rule.getTaskService().complete(afterCompensateTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("singleActivityHandler.multiInstancePartial.2")
  public void testSingleActivityHandlerMultiInstancePartialDeletion() {
    // when throwing compensation
    Task lastMiTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(lastMiTask.getId());

    Task beforeCompensateTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensateTask.getId());

    // then it is possible to delete the process instance
    rule.getRuntimeService().deleteProcessInstance(rule.processInstance().getId(), null);

    // and the process is ended
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("singleActivityHandler.multiInstancePartial.3")
  public void testSingleActivityHandlerMultiInstancePartialActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when throwing compensation
    Task lastMiTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(lastMiTask.getId());

    Task beforeCompensateTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensateTask.getId());

    // then the activity instance tree is meaningful
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .beginScope("throwCompensate")
        // not children of the mi-body scope since their event subscriptions were created
        // when there was no mi-body event scope execution
        //
        // activity execution mapping assumes the compensation throwing execution is
        // the mi body execution
          .activity("undoTask")
          .activity("undoTask")
        .endScope()
        .beginMiBody("userTask")
          .activity("undoTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("singleActivityHandler.beforeCompensate.1")
  public void testSingleActivityHandlerBeforeCompensateCompletion() {
    // given
    Task beforeCompensateTask = rule.taskQuery().singleResult();

    // when throwing compensation
    rule.getTaskService().complete(beforeCompensateTask.getId());

    // then it is possible to compensate the three instances,
    // and finish the process successfully
    List<Task> miCompensationTasks = rule.taskQuery().list();
    Assert.assertEquals(3, miCompensationTasks.size());

    for (int i = 0; i < miCompensationTasks.size(); i++) {
      Assert.assertEquals(3 - i, rule.taskQuery().count());

      Task compensationTask = miCompensationTasks.get(i);
      Assert.assertEquals("undoTask", compensationTask.getTaskDefinitionKey());

      rule.getTaskService().complete(compensationTask.getId());
    }

    Task afterCompensateTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterCompensateTask);
    rule.getTaskService().complete(afterCompensateTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("singleActivityHandler.beforeCompensate.2")
  public void testSingleActivityHandlerBeforeCompensateDeletion() {
    // when throwing compensation
    Task beforeCompensateTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensateTask.getId());

    // then it is possible to delete the process instance
    rule.getRuntimeService().deleteProcessInstance(rule.processInstance().getId(), null);

    // and the process is ended
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("singleActivityHandler.beforeCompensate.3")
  public void testSingleActivityHandlerBeforeCompensateActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when throwing compensation
    Task beforeCompensateTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensateTask.getId());

    // then the activity instance tree is meaningful
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        // missing mi body execution as there was no event subscription for it
//        .beginMiBody("userTask")

        // activity execution mapping assumes the compensation throwing execution is
        // the the mi body execution
        .beginScope("throwCompensate")
          .activity("undoTask")
          .activity("undoTask")
          .activity("undoTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("singleActivityHandler.beforeCompensate.throwCompensate.1")
  public void testSingleActivityHandlerThrowCompensateCompletion() {
    // given active compensation
    List<Task> miCompensationTasks = rule.taskQuery().list();
    Assert.assertEquals(3, miCompensationTasks.size());

    // when completing the compensation handlers
    for (int i = 0; i < miCompensationTasks.size(); i++) {
      Assert.assertEquals(3 - i, rule.taskQuery().count());

      Task compensationTask = miCompensationTasks.get(i);
      Assert.assertEquals("undoTask", compensationTask.getTaskDefinitionKey());

      rule.getTaskService().complete(compensationTask.getId());
    }

    // then it is possible to complete the process successfully
    Task afterCompensateTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterCompensateTask);

    rule.getTaskService().complete(afterCompensateTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("singleActivityHandler.beforeCompensate.throwCompensate.2")
  public void testSingleActivityHandlerThrowCompensateDeletion() {
    // it is possible to delete the process instance
    rule.getRuntimeService().deleteProcessInstance(rule.processInstance().getId(), null);

    // and the process is ended
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("singleActivityHandler.beforeCompensate.throwCompensate.3")
  public void testSingleActivityHandlerThrowCompensateActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // then the activity instance tree is meaningful
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("throwCompensate")
        // no mi-body scope due to missing event scope execution
        .activity("undoTask")
        .activity("undoTask")
        .activity("undoTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("defaultHandler.multiInstancePartial.1")
  public void testDefaultHandlerMultiInstancePartialCompletion() {
    // given the last multi instance task
    Task lastMiTask = rule.taskQuery().singleResult();

    // when completing it
    rule.getTaskService().complete(lastMiTask.getId());

    // then it is possible to throw compensation, compensate the three instances,
    // and finish the process successfully
    Task beforeCompensateTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(beforeCompensateTask);
    rule.getTaskService().complete(beforeCompensateTask.getId());

    List<Task> miCompensationTasks = rule.taskQuery().list();
    Assert.assertEquals(3, miCompensationTasks.size());

    for (int i = 0; i < miCompensationTasks.size(); i++) {
      Assert.assertEquals(3 - i, rule.taskQuery().count());

      Task compensationTask = miCompensationTasks.get(i);
      Assert.assertEquals("undoTask", compensationTask.getTaskDefinitionKey());

      rule.getTaskService().complete(compensationTask.getId());
    }

    Task afterCompensateTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterCompensateTask);
    rule.getTaskService().complete(afterCompensateTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("defaultHandler.multiInstancePartial.2")
  public void testDefaultHandlerMultiInstancePartialDeletion() {
    // when throwing compensation
    Task lastMiTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(lastMiTask.getId());

    Task beforeCompensateTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensateTask.getId());

    // then it is possible to delete the process instance
    rule.getRuntimeService().deleteProcessInstance(rule.processInstance().getId(), null);

    // and the process is ended
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("defaultHandler.multiInstancePartial.3")
  public void testDefaultHandlerMultiInstancePartialActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when throwing compensation
    Task lastMiTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(lastMiTask.getId());

    Task beforeCompensateTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensateTask.getId());

    // then the activity instance tree is meaningful
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("throwCompensate")
        // non-deterministic behavior (i.e. depends on order that executions are returned from database)
        // traversing the tree from pre-migration compensation handling executions, the mi-body execution misses
        // traversing the tree from the post-migration compensation handling executions, the mi-body execution exists
        .beginScope("subProcess", "userTask#multiInstanceBody", "subProcess#multiInstanceBody")
          .beginScope("subProcess")
            .activity("undoTask")
          .endScope()
          // missing parent scope due to missing concurrent executions
          .activity("undoTask")
          .activity("undoTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("defaultHandler.beforeCompensate.1")
  public void testDefaultHandlerBeforeCompensateCompletion() {
    // given
    Task beforeCompensateTask = rule.taskQuery().singleResult();

    // when throwing compensation
    rule.getTaskService().complete(beforeCompensateTask.getId());

    // then it is possible to compensate the three instances,
    // and finish the process successfully
    List<Task> miCompensationTasks = rule.taskQuery().list();
    Assert.assertEquals(3, miCompensationTasks.size());

    for (int i = 0; i < miCompensationTasks.size(); i++) {
      Assert.assertEquals(3 - i, rule.taskQuery().count());

      Task compensationTask = miCompensationTasks.get(i);
      Assert.assertEquals("undoTask", compensationTask.getTaskDefinitionKey());

      rule.getTaskService().complete(compensationTask.getId());
    }

    Task afterCompensateTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterCompensateTask);
    rule.getTaskService().complete(afterCompensateTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("defaultHandler.beforeCompensate.2")
  public void testDefaultHandlerBeforeCompensateDeletion() {
    // when throwing compensation
    Task beforeCompensateTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensateTask.getId());

    // then it is possible to delete the process instance
    rule.getRuntimeService().deleteProcessInstance(rule.processInstance().getId(), null);

    // and the process is ended
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("defaultHandler.beforeCompensate.3")
  public void testDefaultHandlerBeforeCompensateActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when throwing compensation
    Task beforeCompensateTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensateTask.getId());

    // then the activity instance tree is meaningful
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("throwCompensate")
        // missing mi body execution as there was no event subscription for it
//        .beginMiBody("subProcess")
        // there is only one sub process scope although there are three subprocess executions
        // due to the differences in activity instance id assignment with >= 7.4
          .beginScope("subProcess")
            .activity("undoTask")
            .activity("undoTask")
            .activity("undoTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("defaultHandler.beforeCompensate.throwCompensate.1")
  public void testDefaultHandlerThrowCompensateCompletion() {
    // given active compensation
    List<Task> miCompensationTasks = rule.taskQuery().list();
    Assert.assertEquals(3, miCompensationTasks.size());

    // when completing the compensation handlers
    for (int i = 0; i < miCompensationTasks.size(); i++) {
      Assert.assertEquals(3 - i, rule.taskQuery().count());

      Task compensationTask = miCompensationTasks.get(i);
      Assert.assertEquals("undoTask", compensationTask.getTaskDefinitionKey());

      rule.getTaskService().complete(compensationTask.getId());
    }

    // then it is possible to complete the process successfully
    Task afterCompensateTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterCompensateTask);

    rule.getTaskService().complete(afterCompensateTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("defaultHandler.beforeCompensate.throwCompensate.2")
  public void testDefaultHandlerThrowCompensateDeletion() {
    // it is possible to delete the process instance
    rule.getRuntimeService().deleteProcessInstance(rule.processInstance().getId(), null);

    // and the process is ended
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("defaultHandler.beforeCompensate.throwCompensate.3")
  public void testDefaultHandlerThrowCompensateActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // then the activity instance tree is meaningful
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        // no mi-body scope due to missing event scope execution
        //
        // there is only one sub process scope instance although there are three subprocess executions
        // due to the differences in activity instance id assignment with >= 7.4 and missing concurrent
        // executions
        .beginScope("throwCompensate", "subProcess")
          .activity("undoTask")
          .activity("undoTask")
          .activity("undoTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("subProcessHandler.multiInstancePartial.1")
  public void testSubProcessHandlerMultiInstancePartialCompletion() {
    // given the last multi instance task
    Task lastMiTask = rule.taskQuery().singleResult();

    // when completing it
    rule.getTaskService().complete(lastMiTask.getId());

    // then it is possible to throw compensation, compensate the three instances,
    // and finish the process successfully
    Task beforeCompensateTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(beforeCompensateTask);
    rule.getTaskService().complete(beforeCompensateTask.getId());

    List<Task> miCompensationTasks = rule.taskQuery().list();
    Assert.assertEquals(3, miCompensationTasks.size());

    for (int i = 0; i < miCompensationTasks.size(); i++) {
      Assert.assertEquals(3 - i, rule.taskQuery().count());

      Task compensationTask = miCompensationTasks.get(i);
      Assert.assertEquals("undoTask", compensationTask.getTaskDefinitionKey());

      rule.getTaskService().complete(compensationTask.getId());
    }

    Task afterCompensateTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterCompensateTask);
    rule.getTaskService().complete(afterCompensateTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("subProcessHandler.multiInstancePartial.2")
  public void testSubProcessHandlerMultiInstancePartialDeletion() {
    // when throwing compensation
    Task lastMiTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(lastMiTask.getId());

    Task beforeCompensateTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensateTask.getId());

    // then it is possible to delete the process instance
    rule.getRuntimeService().deleteProcessInstance(rule.processInstance().getId(), null);

    // and the process is ended
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("subProcessHandler.multiInstancePartial.3")
  public void testSubProcessHandlerMultiInstancePartialActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when throwing compensation
    Task lastMiTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(lastMiTask.getId());

    Task beforeCompensateTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensateTask.getId());

    // then the activity instance tree is meaningful
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("throwCompensate")
        // non-deterministic behavior (i.e. depends on order that executions are returned from database)
        // traversing the tree from pre-migration compensation handling executions, the mi-body execution misses
        // traversing the tree from the post-migration compensation handling executions, the mi-body execution exists
        .beginScope("undoSubProcess", "userTask#multiInstanceBody")
          .beginScope("undoSubProcess")
            .activity("undoTask")
          .endScope()
          // missing scope due to missing concurrent executions
          .activity("undoTask")
          .activity("undoTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("subProcessHandler.beforeCompensate.1")
  public void testSubProcessHandlerBeforeCompensateCompletion() {
    // given
    Task beforeCompensateTask = rule.taskQuery().singleResult();

    // when throwing compensation
    rule.getTaskService().complete(beforeCompensateTask.getId());

    // then it is possible to compensate the three instances,
    // and finish the process successfully
    List<Task> miCompensationTasks = rule.taskQuery().list();
    Assert.assertEquals(3, miCompensationTasks.size());

    for (int i = 0; i < miCompensationTasks.size(); i++) {
      Assert.assertEquals(3 - i, rule.taskQuery().count());

      Task compensationTask = miCompensationTasks.get(i);
      Assert.assertEquals("undoTask", compensationTask.getTaskDefinitionKey());

      rule.getTaskService().complete(compensationTask.getId());
    }

    Task afterCompensateTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterCompensateTask);
    rule.getTaskService().complete(afterCompensateTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("subProcessHandler.beforeCompensate.2")
  public void testSubProcessHandlerBeforeCompensateDeletion() {
    // when throwing compensation
    Task beforeCompensateTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensateTask.getId());

    // then it is possible to delete the process instance
    rule.getRuntimeService().deleteProcessInstance(rule.processInstance().getId(), null);

    // and the process is ended
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("subProcessHandler.beforeCompensate.3")
  public void testSubProcessHandlerBeforeCompensateActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when throwing compensation
    Task beforeCompensateTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensateTask.getId());

    // then the activity instance tree is meaningful
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("throwCompensate")
        // missing mi body execution as there was no event subscription for it
//        .beginMiBody("userTask")
        // there is only one sub process scope although there are three subprocess executions
        // due to missing concurrent executions
        .beginScope("undoSubProcess")
          .activity("undoTask")
          .activity("undoTask")
          .activity("undoTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("subProcessHandler.beforeCompensate.throwCompensate.1")
  public void testSubProcessHandlerThrowCompensateCompletion() {
    // given active compensation
    List<Task> miCompensationTasks = rule.taskQuery().list();
    Assert.assertEquals(3, miCompensationTasks.size());

    // when completing the compensation handlers
    for (int i = 0; i < miCompensationTasks.size(); i++) {
      Assert.assertEquals(3 - i, rule.taskQuery().count());

      Task compensationTask = miCompensationTasks.get(i);
      Assert.assertEquals("undoTask", compensationTask.getTaskDefinitionKey());

      rule.getTaskService().complete(compensationTask.getId());
    }

    // then it is possible to complete the process successfully
    Task afterCompensateTask = rule.taskQuery().singleResult();
    Assert.assertNotNull(afterCompensateTask);

    rule.getTaskService().complete(afterCompensateTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("subProcessHandler.beforeCompensate.throwCompensate.2")
  public void testSubProcessHandlerThrowCompensateDeletion() {
    // it is possible to delete the process instance
    rule.getRuntimeService().deleteProcessInstance(rule.processInstance().getId(), null);

    // and the process is ended
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("subProcessHandler.beforeCompensate.throwCompensate.3")
  public void testSubProcessHandlerThrowCompensateActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // then the activity instance tree is meaningful
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        // - no mi-body scope due to missing event scope execution
        // - due to missing concurrent executions, there are no dedicated undoSubProcess activity instance ids
        // - due to missing throw compensation scope execution, there is no dedicated throw compensation activity instance id
        // - depending on the order the executions are returned from the database, the throwing execution's activity instance
        //   id is either interpreted as an undoSubProcess instance or as a throwCompensateInstance
        .beginScope("throwCompensate", "undoSubProcess")
          .activity("undoTask")
          .activity("undoTask")
          .activity("undoTask")
      .done());
  }

}
