package org.camunda.bpm.engine.test.api.runtime.migration;

import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.MessageReceiveModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class MigrationMessageStartEventTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testMigrateEventSubscription() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.MESSAGE_START_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), sourceProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // when
    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(migrationPlan.getSourceProcessDefinitionId());
    EventSubscription eventSubscription = rule.getRuntimeService().createEventSubscriptionQuery().activityId("startEvent").eventName(MessageReceiveModels.MESSAGE_NAME).singleResult();
    rule.getRuntimeService().newMigration(migrationPlan).processInstanceIds(processInstance.getId()).execute();

    // then
    assertEventSubscriptionMigrated(eventSubscription, "startEvent", MessageReceiveModels.MESSAGE_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateEventSubscriptionWithEventSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // when
    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(migrationPlan.getSourceProcessDefinitionId());
    rule.getRuntimeService().newMigration(migrationPlan).processInstanceIds(processInstance.getId()).execute();

    // then
    EventSubscription eventSubscriptionAfter = rule.getRuntimeService().createEventSubscriptionQuery().singleResult();

    assertNotNull(eventSubscriptionAfter);
    assertEquals(EventSubProcessModels.MESSAGE_NAME, eventSubscriptionAfter.getEventName());

    rule.getRuntimeService().correlateMessage(EventSubProcessModels.MESSAGE_NAME);
    testHelper.completeTask("eventSubProcessTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  protected void assertEventSubscriptionMigrated(EventSubscription eventSubscriptionBefore, String activityIdAfter, String eventName) {
    EventSubscription eventSubscriptionAfter = rule.getRuntimeService().createEventSubscriptionQuery().singleResult();
    assertNotNull("Expected that an event subscription with id '" + eventSubscriptionBefore.getId() + "' "
        + "exists after migration", eventSubscriptionAfter);

    assertEquals(eventSubscriptionBefore.getEventType(), eventSubscriptionAfter.getEventType());
    assertEquals(activityIdAfter, eventSubscriptionAfter.getActivityId());
    assertEquals(eventName, eventSubscriptionAfter.getEventName());
  }
}
