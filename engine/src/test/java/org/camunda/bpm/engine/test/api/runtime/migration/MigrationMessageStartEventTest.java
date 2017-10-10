package org.camunda.bpm.engine.test.api.runtime.migration;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.MessageReceiveModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class MigrationMessageStartEventTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);
  protected RuntimeService runtimeService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Before
  public void setup() {
    runtimeService = rule.getRuntimeService();
  }

  @Test
  public void testMigrateEventSubscription() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.MESSAGE_START_PROCESS);
    String sourceProcessDefinitionId = sourceProcessDefinition.getId();

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinitionId, sourceProcessDefinitionId)
      .mapEqualActivities()
      .build();

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceProcessDefinitionId);
    EventSubscription eventSubscription = runtimeService
        .createEventSubscriptionQuery()
        .activityId("startEvent")
        .eventName(MessageReceiveModels.MESSAGE_NAME)
        .singleResult();

    // when
    runtimeService.newMigration(migrationPlan).processInstanceIds(processInstance.getId()).execute();

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

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    runtimeService.newMigration(migrationPlan).processInstanceIds(processInstance.getId()).execute();

    // then
    EventSubscription eventSubscriptionAfter = runtimeService.createEventSubscriptionQuery().singleResult();

    assertNotNull(eventSubscriptionAfter);
    assertEquals(EventSubProcessModels.MESSAGE_NAME, eventSubscriptionAfter.getEventName());

    runtimeService.correlateMessage(EventSubProcessModels.MESSAGE_NAME);
    testHelper.completeTask("eventSubProcessTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  protected void assertEventSubscriptionMigrated(EventSubscription eventSubscriptionBefore, String activityIdAfter, String eventName) {
    EventSubscription eventSubscriptionAfter = runtimeService.createEventSubscriptionQuery().singleResult();
    assertNotNull("Expected that an event subscription with id '" + eventSubscriptionBefore.getId() + "' "
        + "exists after migration", eventSubscriptionAfter);

    assertEquals(eventSubscriptionBefore.getEventType(), eventSubscriptionAfter.getEventType());
    assertEquals(activityIdAfter, eventSubscriptionAfter.getActivityId());
    assertEquals(eventName, eventSubscriptionAfter.getEventName());
  }
}
