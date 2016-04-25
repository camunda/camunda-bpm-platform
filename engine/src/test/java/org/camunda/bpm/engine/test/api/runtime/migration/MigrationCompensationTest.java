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

import static org.camunda.bpm.engine.test.util.MigratingProcessInstanceValidationReportAssert.assertThat;

import org.camunda.bpm.engine.migration.MigratingProcessInstanceValidationException;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.CompensationModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.CachedProcessEngineRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationCompensationTest {

  protected ProcessEngineRule rule = new CachedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testCannotMigrateActivityInstanceForCompensationThrowingEvent() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");
    testHelper.completeTask("userTask2");

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("compensationEvent", "compensationEvent")
      .mapActivities("compensationHandler", "compensationHandler")
      .build();

    // when
    try {
      testHelper.migrateProcessInstance(migrationPlan, processInstance);
      Assert.fail("should fail");
    }
    catch (MigratingProcessInstanceValidationException e) {
      // then
      assertThat(e.getValidationReport())
        .hasProcessInstanceId(testHelper.snapshotBeforeMigration.getProcessInstanceId())
        .hasActivityInstanceFailures("compensationEvent",
          "The type of the source activity is not supported for activity instance migration"
        );
    }
  }

  @Test
  public void testCannotMigrateActivityInstanceForCancelEndEvent() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.TRANSACTION_COMPENSATION_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.TRANSACTION_COMPENSATION_MODEL);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask");

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("transactionEndEvent", "transactionEndEvent")
      .mapActivities("compensationHandler", "compensationHandler")
      .build();

    // when
    try {
      testHelper.migrateProcessInstance(migrationPlan, processInstance);
      Assert.fail("should fail");
    }
    catch (MigratingProcessInstanceValidationException e) {
      // then
      assertThat(e.getValidationReport())
        .hasProcessInstanceId(testHelper.snapshotBeforeMigration.getProcessInstanceId())
        .hasActivityInstanceFailures("transactionEndEvent",
          "The type of the source activity is not supported for activity instance migration"
        );
    }
  }

  @Test
  public void testCannotMigrateActiveCompensationWithoutInstructionForThrowingEventCase1() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");
    testHelper.completeTask("userTask2");

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("compensationHandler", "compensationHandler")
      .build();

    // when
    try {
      testHelper.migrateProcessInstance(migrationPlan, processInstance);
      Assert.fail("should fail");
    }
    catch (MigratingProcessInstanceValidationException e) {
      // then
      assertThat(e.getValidationReport())
        .hasProcessInstanceId(testHelper.snapshotBeforeMigration.getProcessInstanceId())
        .hasActivityInstanceFailures("compensationEvent",
          "There is no migration instruction for this instance's activity",
          "The type of the source activity is not supported for activity instance migration"
        );
    }
  }

  @Test
  public void testCannotMigrateActiveCompensationWithoutInstructionForThrowingEventCase2() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_END_EVENT_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_END_EVENT_MODEL);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");
    testHelper.completeTask("userTask2");

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("compensationHandler", "compensationHandler")
      .build();

    // when
    try {
      testHelper.migrateProcessInstance(migrationPlan, processInstance);
      Assert.fail("should fail");
    }
    catch (MigratingProcessInstanceValidationException e) {
      // then
      assertThat(e.getValidationReport())
        .hasProcessInstanceId(testHelper.snapshotBeforeMigration.getProcessInstanceId())
        .hasActivityInstanceFailures("compensationEvent",
          "There is no migration instruction for this instance's activity",
          "The type of the source activity is not supported for activity instance migration"
        );
    }
  }

  @Test
  public void testCannotMigrateCompensationEventSubscriptions() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    try {
      testHelper.migrateProcessInstance(migrationPlan, processInstance);
      Assert.fail("should fail");
    }
    catch (MigratingProcessInstanceValidationException e) {
      // then
      assertThat(e.getValidationReport())
        .hasProcessInstanceId(testHelper.snapshotBeforeMigration.getProcessInstanceId())
        .hasFailures("Process instance contains not migrated event subscriptions")
        .hasActivityInstanceFailures(sourceProcessDefinition.getId(), "Scope contains compensate event subscriptions. "
            + "Migrating these is currently not supported");
    }
  }

  @Test
  public void testCannotRemoveCompensationEventSubscriptions() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    try {
      testHelper.migrateProcessInstance(migrationPlan, processInstance);
      Assert.fail("should fail");
    }
    catch (MigratingProcessInstanceValidationException e) {
      // then
      assertThat(e.getValidationReport())
        .hasProcessInstanceId(testHelper.snapshotBeforeMigration.getProcessInstanceId())
        .hasFailures("Process instance contains not migrated event subscriptions")
        .hasActivityInstanceFailures(sourceProcessDefinition.getId(), "Scope contains compensate event subscriptions. "
            + "Migrating these is currently not supported");
    }
  }

  @Test
  public void testCannotTriggerAddedCompensationForCompletedInstances() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    Assert.assertEquals(0, testHelper.snapshotAfterMigration.getEventSubscriptions().size());

    testHelper.completeTask("userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testCanTriggerAddedCompensationForActiveInstances() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask1")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.completeTask("userTask");
    Assert.assertEquals(1, rule.getRuntimeService().createEventSubscriptionQuery().count());

    testHelper.completeTask("userTask2");
    testHelper.completeTask("compensationHandler");
    testHelper.assertProcessEnded(processInstance.getId());
  }
}
