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
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.EventBasedGatewayModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationEventBasedGatewayTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testMigrateGatewayExecutionTree() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventBasedGateway", "eventBasedGateway")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("eventBasedGateway").scope().id(testHelper.getSingleExecutionIdForActivityBeforeMigration("eventBasedGateway"))
        .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("eventBasedGateway", testHelper.getSingleActivityInstanceBeforeMigration("eventBasedGateway").getId())
        .done());
  }

  @Test
  public void testMigrateGatewayWithTimerEvent() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventBasedGateway", "eventBasedGateway")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertIntermediateTimerJobRemoved("timerCatch");
    testHelper.assertIntermediateTimerJobCreated("timerCatch");

    Job timerJob = testHelper.snapshotAfterMigration.getJobs().get(0);
    rule.getManagementService().executeJob(timerJob.getId());

    testHelper.completeTask("afterTimerCatch");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateGatewayWithMessageEvent() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.MESSAGE_EVENT_BASED_GW_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.MESSAGE_EVENT_BASED_GW_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventBasedGateway", "eventBasedGateway")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("messageCatch", EventBasedGatewayModels.MESSAGE_NAME);
    testHelper.assertEventSubscriptionCreated("messageCatch", EventBasedGatewayModels.MESSAGE_NAME);

    rule.getRuntimeService().correlateMessage(EventBasedGatewayModels.MESSAGE_NAME);

    testHelper.completeTask("afterMessageCatch");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateGatewayWithSignalEvent() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.SIGNAL_EVENT_BASED_GW_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.SIGNAL_EVENT_BASED_GW_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventBasedGateway", "eventBasedGateway")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("signalCatch", EventBasedGatewayModels.SIGNAL_NAME);
    testHelper.assertEventSubscriptionCreated("signalCatch", EventBasedGatewayModels.SIGNAL_NAME);

    rule.getRuntimeService().signalEventReceived(EventBasedGatewayModels.SIGNAL_NAME);

    testHelper.completeTask("afterSignalCatch");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateGatewayWithTimerEventMapEvent() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventBasedGateway", "eventBasedGateway")
      .mapActivities("timerCatch", "timerCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertIntermediateTimerJobMigrated("timerCatch", "timerCatch");

    Job timerJob = testHelper.snapshotAfterMigration.getJobs().get(0);
    rule.getManagementService().executeJob(timerJob.getId());

    testHelper.completeTask("afterTimerCatch");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateGatewayWithMessageEventMapEvent() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.MESSAGE_EVENT_BASED_GW_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.MESSAGE_EVENT_BASED_GW_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventBasedGateway", "eventBasedGateway")
      .mapActivities("messageCatch", "messageCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionMigrated("messageCatch", "messageCatch", EventBasedGatewayModels.MESSAGE_NAME);

    rule.getRuntimeService().correlateMessage(EventBasedGatewayModels.MESSAGE_NAME);

    testHelper.completeTask("afterMessageCatch");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateGatewayWithSignalEventMapEvent() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.SIGNAL_EVENT_BASED_GW_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.SIGNAL_EVENT_BASED_GW_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventBasedGateway", "eventBasedGateway")
      .mapActivities("signalCatch", "signalCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionMigrated("signalCatch", "signalCatch", EventBasedGatewayModels.SIGNAL_NAME);

    rule.getRuntimeService().signalEventReceived(EventBasedGatewayModels.SIGNAL_NAME);

    testHelper.completeTask("afterSignalCatch");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateGatewayAddTimerEvent() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS)
        .flowNodeBuilder("eventBasedGateway")
        .intermediateCatchEvent("newTimerCatch")
          .timerWithDuration("PT50M")
        .userTask("afterNewTimerCatch")
        .endEvent()
        .done());

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("eventBasedGateway", "eventBasedGateway")
        .mapActivities("timerCatch", "timerCatch")
        .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertIntermediateTimerJobCreated("newTimerCatch");
    testHelper.assertIntermediateTimerJobMigrated("timerCatch", "timerCatch");

    Job newTimerJob = rule.getManagementService().createJobQuery().activityId("newTimerCatch").singleResult();
    rule.getManagementService().executeJob(newTimerJob.getId());

    testHelper.completeTask("afterNewTimerCatch");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateGatewayAddMessageEvent() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.MESSAGE_EVENT_BASED_GW_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(EventBasedGatewayModels.MESSAGE_EVENT_BASED_GW_PROCESS)
      .flowNodeBuilder("eventBasedGateway")
      .intermediateCatchEvent("newMessageCatch")
        .message("new" + EventBasedGatewayModels.MESSAGE_NAME)
      .userTask("afterNewMessageCatch")
      .endEvent()
      .done());

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventBasedGateway", "eventBasedGateway")
      .mapActivities("messageCatch", "messageCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionCreated("newMessageCatch", "new" + EventBasedGatewayModels.MESSAGE_NAME);
    testHelper.assertEventSubscriptionMigrated("messageCatch", "messageCatch", EventBasedGatewayModels.MESSAGE_NAME);

    rule.getRuntimeService().correlateMessage("new" + EventBasedGatewayModels.MESSAGE_NAME);

    testHelper.completeTask("afterNewMessageCatch");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateGatewayAddSignalEvent() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.SIGNAL_EVENT_BASED_GW_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(EventBasedGatewayModels.SIGNAL_EVENT_BASED_GW_PROCESS)
        .flowNodeBuilder("eventBasedGateway")
        .intermediateCatchEvent("newSignalCatch")
          .signal("new" + EventBasedGatewayModels.SIGNAL_NAME)
        .userTask("afterNewSignalCatch")
        .endEvent()
        .done());

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventBasedGateway", "eventBasedGateway")
      .mapActivities("signalCatch", "signalCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionCreated("newSignalCatch", "new" + EventBasedGatewayModels.SIGNAL_NAME);
    testHelper.assertEventSubscriptionMigrated("signalCatch", "signalCatch", EventBasedGatewayModels.SIGNAL_NAME);

    rule.getRuntimeService().signalEventReceived("new" + EventBasedGatewayModels.SIGNAL_NAME);

    testHelper.completeTask("afterNewSignalCatch");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateGatewayRemoveTimerEvent() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS)
        .flowNodeBuilder("eventBasedGateway")
        .intermediateCatchEvent("oldTimerCatch")
          .timerWithDuration("PT50M")
        .endEvent()
        .done());
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("eventBasedGateway", "eventBasedGateway")
        .mapActivities("timerCatch", "timerCatch")
        .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertIntermediateTimerJobRemoved("oldTimerCatch");
    testHelper.assertIntermediateTimerJobMigrated("timerCatch", "timerCatch");
  }

  @Test
  public void testMigrateGatewayRemoveMessageEvent() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(EventBasedGatewayModels.MESSAGE_EVENT_BASED_GW_PROCESS)
        .flowNodeBuilder("eventBasedGateway")
        .intermediateCatchEvent("oldMessageCatch")
        .message("old" + EventBasedGatewayModels.MESSAGE_NAME)
        .endEvent()
        .done());
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.MESSAGE_EVENT_BASED_GW_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventBasedGateway", "eventBasedGateway")
      .mapActivities("messageCatch", "messageCatch")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("oldMessageCatch", "old" + EventBasedGatewayModels.MESSAGE_NAME);
    testHelper.assertEventSubscriptionMigrated("messageCatch", "messageCatch", EventBasedGatewayModels.MESSAGE_NAME);
  }

  @Test
  public void testMigrateGatewayRemoveSignalEvent() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(EventBasedGatewayModels.SIGNAL_EVENT_BASED_GW_PROCESS)
        .flowNodeBuilder("eventBasedGateway")
        .intermediateCatchEvent("oldSignalCatch")
          .signal("old" + EventBasedGatewayModels.SIGNAL_NAME)
        .endEvent()
        .done());
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.SIGNAL_EVENT_BASED_GW_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventBasedGateway", "eventBasedGateway")
      .mapActivities("signalCatch", "signalCatch")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("oldSignalCatch", "old" + EventBasedGatewayModels.SIGNAL_NAME);
    testHelper.assertEventSubscriptionMigrated("signalCatch", "signalCatch", EventBasedGatewayModels.SIGNAL_NAME);
  }

  @Test
  public void testMigrateGatewayWithTimerEventChangeId() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS)
        .changeElementId("timerCatch", "newTimerCatch"));

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventBasedGateway", "eventBasedGateway")
      .mapActivities("timerCatch", "newTimerCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertIntermediateTimerJobMigrated("timerCatch", "newTimerCatch");

    Job timerJob = testHelper.snapshotAfterMigration.getJobs().get(0);
    rule.getManagementService().executeJob(timerJob.getId());

    testHelper.completeTask("afterTimerCatch");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateGatewayWithMessageEventChangeId() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.MESSAGE_EVENT_BASED_GW_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(EventBasedGatewayModels.MESSAGE_EVENT_BASED_GW_PROCESS)
        .changeElementId("messageCatch", "newMessageCatch"));

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventBasedGateway", "eventBasedGateway")
      .mapActivities("messageCatch", "newMessageCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionMigrated("messageCatch", "newMessageCatch", EventBasedGatewayModels.MESSAGE_NAME);

    rule.getRuntimeService().correlateMessage(EventBasedGatewayModels.MESSAGE_NAME);

    testHelper.completeTask("afterMessageCatch");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateGatewayWithSignalEventChangeId() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.SIGNAL_EVENT_BASED_GW_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(EventBasedGatewayModels.SIGNAL_EVENT_BASED_GW_PROCESS)
        .changeElementId("signalCatch", "newSignalCatch"));

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventBasedGateway", "eventBasedGateway")
      .mapActivities("signalCatch", "newSignalCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionMigrated("signalCatch", "newSignalCatch", EventBasedGatewayModels.SIGNAL_NAME);

    rule.getRuntimeService().signalEventReceived(EventBasedGatewayModels.SIGNAL_NAME);

    testHelper.completeTask("afterSignalCatch");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateGatewayWithIncident() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventBasedGateway", "eventBasedGateway")
      .mapActivities("timerCatch", "timerCatch")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(migrationPlan.getSourceProcessDefinitionId());

    Job timerJob = rule.getManagementService().createJobQuery().singleResult();
    // create an incident
    rule.getManagementService().setJobRetries(timerJob.getId(), 0);
    Incident incidentBeforeMigration = rule.getRuntimeService().createIncidentQuery().singleResult();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then job and incident still exist
    testHelper.assertIntermediateTimerJobMigrated("timerCatch", "timerCatch");

    Job jobAfterMigration = testHelper.snapshotAfterMigration.getJobs().get(0);

    Incident incidentAfterMigration = rule.getRuntimeService().createIncidentQuery().singleResult();
    assertNotNull(incidentAfterMigration);

    assertEquals(incidentBeforeMigration.getId(), incidentAfterMigration.getId());
    assertEquals(jobAfterMigration.getId(), incidentAfterMigration.getConfiguration());

    assertEquals("timerCatch", incidentAfterMigration.getActivityId());
    assertEquals(targetProcessDefinition.getId(), incidentAfterMigration.getProcessDefinitionId());

    // and it is possible to complete the process
    rule.getManagementService().executeJob(jobAfterMigration.getId());

    testHelper.completeTask("afterTimerCatch");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateGatewayRemoveIncidentOnMigration() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventBasedGateway", "eventBasedGateway")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(migrationPlan.getSourceProcessDefinitionId());

    Job timerJob = rule.getManagementService().createJobQuery().singleResult();
    // create an incident
    rule.getManagementService().setJobRetries(timerJob.getId(), 0);

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then the incident is gone
    Assert.assertEquals(0, rule.getRuntimeService().createIncidentQuery().count());
  }

}
