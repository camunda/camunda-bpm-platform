/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.runtime.migration;

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;

import java.util.List;

import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.MessageReceiveModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationReceiveTaskTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testCannotMigrateActivityInstance() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("receiveTask", "receiveTask")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    testHelper.assertEventSubscriptionMigrated("receiveTask", "receiveTask", MessageReceiveModels.MESSAGE_NAME);

    // and it is possible to trigger the receive task
    rule.getRuntimeService().correlateMessage(MessageReceiveModels.MESSAGE_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateEventSubscriptionProperties() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("receiveTask", "receiveTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    EventSubscription eventSubscriptionBefore = testHelper.snapshotBeforeMigration.getEventSubscriptions().get(0);

    List<EventSubscription> eventSubscriptionsAfter = testHelper.snapshotAfterMigration.getEventSubscriptions();
    Assert.assertEquals(1, eventSubscriptionsAfter.size());
    EventSubscription eventSubscriptionAfter = eventSubscriptionsAfter.get(0);
    Assert.assertEquals(eventSubscriptionBefore.getCreated(), eventSubscriptionAfter.getCreated());
    Assert.assertEquals(eventSubscriptionBefore.getExecutionId(), eventSubscriptionAfter.getExecutionId());
    Assert.assertEquals(eventSubscriptionBefore.getProcessInstanceId(), eventSubscriptionAfter.getProcessInstanceId());
  }

  @Test
  public void testMigrateEventSubscriptionChangeActivityId() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS)
        .changeElementId("receiveTask", "newReceiveTask"));

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("receiveTask", "newReceiveTask")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    testHelper.assertEventSubscriptionMigrated("receiveTask", "newReceiveTask", MessageReceiveModels.MESSAGE_NAME);

    // and it is possible to trigger the receive task
    rule.getRuntimeService().correlateMessage(MessageReceiveModels.MESSAGE_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateEventSubscriptionPreserveMessageName() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS)
        .renameMessage(MessageReceiveModels.MESSAGE_NAME, "new" + MessageReceiveModels.MESSAGE_NAME));

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("receiveTask", "receiveTask")
        .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then the message event subscription's event name has not changed
    testHelper.assertEventSubscriptionMigrated("receiveTask", "receiveTask", MessageReceiveModels.MESSAGE_NAME);

    // and it is possible to trigger the receive task
    rule.getRuntimeService().correlateMessage(MessageReceiveModels.MESSAGE_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateEventSubscriptionUpdateMessageName() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS)
      .renameMessage(MessageReceiveModels.MESSAGE_NAME, "new" + MessageReceiveModels.MESSAGE_NAME));

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("receiveTask", "receiveTask")
          .updateEventTrigger()
        .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then the message event subscription's event name has not changed
    testHelper.assertEventSubscriptionMigrated(
        "receiveTask", MessageReceiveModels.MESSAGE_NAME,
        "receiveTask", "new" + MessageReceiveModels.MESSAGE_NAME);

    // and it is possible to trigger the event
    rule.getRuntimeService().correlateMessage("new" + MessageReceiveModels.MESSAGE_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateParallelMultiInstanceEventSubscription() {
    BpmnModelInstance parallelMiReceiveTaskProcess = modify(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS)
      .activityBuilder("receiveTask")
        .multiInstance()
        .parallel()
        .cardinality("3")
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(parallelMiReceiveTaskProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(parallelMiReceiveTaskProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("receiveTask#multiInstanceBody", "receiveTask#multiInstanceBody")
      .mapActivities("receiveTask", "receiveTask")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    testHelper.assertEventSubscriptionsMigrated("receiveTask", "receiveTask", MessageReceiveModels.MESSAGE_NAME);

    // and it is possible to trigger the receive tasks
    rule.getRuntimeService().createMessageCorrelation(MessageReceiveModels.MESSAGE_NAME).correlateAll();

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateSequentialMultiInstanceEventSubscription() {
    BpmnModelInstance parallelMiReceiveTaskProcess = modify(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS)
      .activityBuilder("receiveTask")
        .multiInstance()
        .sequential()
        .cardinality("3")
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(parallelMiReceiveTaskProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(parallelMiReceiveTaskProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("receiveTask#multiInstanceBody", "receiveTask#multiInstanceBody")
      .mapActivities("receiveTask", "receiveTask")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    testHelper.assertEventSubscriptionsMigrated("receiveTask", "receiveTask", MessageReceiveModels.MESSAGE_NAME);

    // and it is possible to trigger the receive tasks
    for (int i = 0; i < 3; i++) {
      rule.getRuntimeService().correlateMessage(MessageReceiveModels.MESSAGE_NAME);
    }

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateEventSubscriptionAddParentScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.SUBPROCESS_RECEIVE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("receiveTask", "receiveTask")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    testHelper.assertEventSubscriptionMigrated("receiveTask", "receiveTask", MessageReceiveModels.MESSAGE_NAME);

    // and it is possible to trigger the receive task
    rule.getRuntimeService().correlateMessage(MessageReceiveModels.MESSAGE_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }
}
