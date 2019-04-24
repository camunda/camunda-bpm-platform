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
package org.camunda.bpm.engine.test.api.runtime.migration;

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;
import static org.camunda.bpm.engine.test.util.MigratingProcessInstanceValidationReportAssert.assertThat;
import static org.camunda.bpm.engine.test.util.MigrationPlanValidationReportAssert.assertThat;

import java.util.List;

import org.camunda.bpm.engine.migration.MigratingProcessInstanceValidationException;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.CompensationModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
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
public class MigrationCompensationTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
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
  public void testCannotMigrateWithoutMappingCompensationBoundaryEvents() {
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
        .hasActivityInstanceFailures(
            sourceProcessDefinition.getId(),
            "Cannot migrate subscription for compensation handler 'compensationHandler'. "
            + "There is no migration instruction for the compensation boundary event");
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
        .hasActivityInstanceFailures(
            sourceProcessDefinition.getId(),
            "Cannot migrate subscription for compensation handler 'compensationHandler'. "
            + "There is no migration instruction for the compensation boundary event");
    }
  }

  @Test
  public void testCanRemoveCompensationBoundaryWithoutEventSubscriptions() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);
    testHelper.completeTask("userTask1");

    // then
    Assert.assertEquals(0, testHelper.snapshotAfterMigration.getEventSubscriptions().size());

    testHelper.completeTask("userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
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
    testHelper.completeTask("userTask1");
    Assert.assertEquals(1, rule.getRuntimeService().createEventSubscriptionQuery().count());

    testHelper.completeTask("userTask2");
    testHelper.completeTask("compensationHandler");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testCanMigrateWithCompensationSubscriptionsInMigratingScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertEventSubscriptionMigrated("compensationHandler", "compensationHandler", null);

    // and the compensation can be triggered and completed
    testHelper.completeTask("userTask2");
    testHelper.completeTask("compensationHandler");

    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testCanMigrateWithCompensationSubscriptionsInMigratingScopeAssertActivityInstance() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    // a migrated process instance
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // when triggering compensation
    testHelper.completeTask("userTask2");

    // then the activity instance tree is correct
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("compensationEvent")
        .activity("compensationHandler")
      .done());
  }

  @Test
  public void testCanMigrateWithCompensationSubscriptionsInMigratingScopeAssertExecutionTree() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree("userTask2").scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .done());
  }

  @Test
  public void testCanMigrateWithCompensationSubscriptionsInMigratingScopeChangeIds() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(CompensationModels.ONE_COMPENSATION_TASK_MODEL)
        .changeElementId("userTask1", "newUserTask1")
        .changeElementId("compensationBoundary", "newCompensationBoundary")
        .changeElementId("compensationHandler", "newCompensationHandler"));

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("compensationBoundary", "newCompensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertEventSubscriptionMigrated("compensationHandler", "newCompensationHandler", null);

    // and the compensation can be triggered and completed
    testHelper.completeTask("userTask2");
    testHelper.completeTask("newCompensationHandler");

    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testCanMigrateWithCompensationEventScopeExecution() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("subProcess", "subProcess")
        .mapActivities("userTask2", "userTask2")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertEventSubscriptionMigrated("subProcess", "subProcess", null);
    testHelper.assertEventSubscriptionMigrated("compensationHandler", "compensationHandler", null);

    // and the compensation can be triggered and completed
    testHelper.completeTask("userTask2");
    testHelper.completeTask("compensationHandler");

    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testCanMigrateWithCompensationEventScopeExecutionAssertActivityInstance() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("subProcess", "subProcess")
        .mapActivities("userTask2", "userTask2")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // when
    testHelper.completeTask("userTask2");

    // then
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("compensationEvent")
        .beginScope("subProcess")
          .activity("compensationHandler")
      .done());
  }

  @Test
  public void testCanMigrateWithCompensationEventScopeExecutionAssertExecutionTree() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("subProcess", "subProcess")
        .mapActivities("userTask2", "userTask2")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    Execution eventScopeExecution = rule.getRuntimeService()
      .createExecutionQuery()
      .activityId("subProcess")
      .singleResult();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree("userTask2").scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("subProcess").scope().eventScope().id(eventScopeExecution.getId())
          .done());
  }

  @Test
  public void testCanMigrateWithCompensationEventScopeExecutionChangeIds() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL)
        .changeElementId("subProcess", "newSubProcess")
        .changeElementId("userTask1", "newUserTask1")
        .changeElementId("compensationBoundary", "newCompensationBoundary")
        .changeElementId("compensationHandler", "newCompensationHandler"));

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("subProcess", "newSubProcess")
        .mapActivities("compensationBoundary", "newCompensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertEventSubscriptionMigrated("subProcess", "newSubProcess", null);
    testHelper.assertEventSubscriptionMigrated("compensationHandler", "newCompensationHandler", null);

    // and the compensation can be triggered and completed
    testHelper.completeTask("userTask2");
    testHelper.completeTask("newCompensationHandler");

    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testCanMigrateWithCompensationEventScopeExecutionChangeIdsAssertActivityInstance() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL)
        .changeElementId("subProcess", "newSubProcess")
        .changeElementId("userTask1", "newUserTask1")
        .changeElementId("compensationBoundary", "newCompensationBoundary")
        .changeElementId("compensationHandler", "newCompensationHandler"));

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("subProcess", "newSubProcess")
        .mapActivities("compensationBoundary", "newCompensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // when
    testHelper.completeTask("userTask2");

    // then
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("compensationEvent")
        .beginScope("newSubProcess")
          .activity("newCompensationHandler")
      .done());
  }

  @Test
  public void testCanMigrateWithCompensationEventScopeExecutionChangeIdsAssertExecutionTree() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL)
        .changeElementId("subProcess", "newSubProcess")
        .changeElementId("userTask1", "newUserTask1")
        .changeElementId("compensationBoundary", "newCompensationBoundary")
        .changeElementId("compensationHandler", "newCompensationHandler"));

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("subProcess", "newSubProcess")
        .mapActivities("compensationBoundary", "newCompensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    Execution eventScopeExecution = rule.getRuntimeService()
      .createExecutionQuery()
      .activityId("subProcess")
      .singleResult();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree("userTask2").scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("newSubProcess").scope().eventScope().id(eventScopeExecution.getId())
          .done());

  }

  @Test
  public void testCanMigrateEventScopeVariables() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("subProcess", "subProcess")
        .mapActivities("userTask2", "userTask2")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    Execution subProcessExecution = rule.getRuntimeService()
        .createExecutionQuery()
        .activityId("userTask1")
        .singleResult();
    rule.getRuntimeService().setVariableLocal(subProcessExecution.getId(), "foo", "bar");

    testHelper.completeTask("userTask1");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    VariableInstance beforeMigration = testHelper.snapshotBeforeMigration.getSingleVariable("foo");
    testHelper.assertVariableMigratedToExecution(beforeMigration, beforeMigration.getExecutionId());

    // and the compensation can be triggered and completed
    testHelper.completeTask("userTask2");
    testHelper.completeTask("compensationHandler");

    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testCanMigrateWithEventSubProcessHandler() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_EVENT_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_EVENT_SUBPROCESS_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("subProcess", "subProcess")
        .mapActivities("eventSubProcessStart", "eventSubProcessStart")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    testHelper.completeTask("userTask1");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertEventSubscriptionMigrated("eventSubProcess", "eventSubProcess", null);

    // and the compensation can be triggered and completed
    testHelper.completeTask("userTask2");
    testHelper.completeTask("eventSubProcessTask");
    testHelper.completeTask("compensationHandler");

    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testCanMigrateWithEventSubProcessHandlerAssertActivityInstance() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_EVENT_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_EVENT_SUBPROCESS_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("subProcess", "subProcess")
        .mapActivities("eventSubProcessStart", "eventSubProcessStart")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    testHelper.completeTask("userTask1");
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // when compensation is triggered
    testHelper.completeTask("userTask2");

    // then
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("compensationEvent")
        .beginScope("subProcess")
          .beginScope("eventSubProcess")
            .activity("eventSubProcessTask")
      .done());
  }

  @Test
  public void testCanMigrateWithEventSubProcessHandlerAssertExecutionTree() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_EVENT_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_EVENT_SUBPROCESS_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("subProcess", "subProcess")
        .mapActivities("eventSubProcessStart", "eventSubProcessStart")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    testHelper.completeTask("userTask1");

    Execution eventScopeExecution = rule.getRuntimeService()
      .createExecutionQuery()
      .activityId("subProcess")
      .singleResult();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree("userTask2").scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("subProcess").scope().eventScope().id(eventScopeExecution.getId())
          .done());

  }

  @Test
  public void testCanMigrateWithEventSubProcessHandlerChangeIds() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_EVENT_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(CompensationModels.COMPENSATION_EVENT_SUBPROCESS_MODEL)
        .changeElementId("eventSubProcess", "newEventSubProcess"));

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .mapActivities("subProcess", "subProcess")
        .mapActivities("eventSubProcessStart", "eventSubProcessStart")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    testHelper.completeTask("userTask1");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertEventSubscriptionMigrated("eventSubProcess", "newEventSubProcess", null);

    // and the compensation can be triggered and completed
    testHelper.completeTask("userTask2");
    testHelper.completeTask("eventSubProcessTask");
    testHelper.completeTask("compensationHandler");

    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testCanMigrateSiblingEventScopeExecutions() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.DOUBLE_SUBPROCESS_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("subProcess", "outerSubProcess")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    // starting a second instances of the sub process
    rule.getRuntimeService().createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("subProcess")
      .execute();

    List<Execution> subProcessExecutions = rule.getRuntimeService().createExecutionQuery().activityId("userTask1").list();
    for (Execution subProcessExecution : subProcessExecutions) {
      // set the same variable to a distinct value
      rule.getRuntimeService().setVariableLocal(subProcessExecution.getId(), "var", subProcessExecution.getId());
    }

    testHelper.completeAnyTask("userTask1");
    testHelper.completeAnyTask("userTask1");


    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then the variable snapshots during compensation are not shared
    testHelper.completeAnyTask("userTask2");

    List<Task> compensationTasks = rule.getTaskService()
        .createTaskQuery()
        .taskDefinitionKey("compensationHandler")
        .list();
    Assert.assertEquals(2, compensationTasks.size());

    Object value1 = rule.getTaskService().getVariable(compensationTasks.get(0).getId(), "var");
    Object value2 = rule.getTaskService().getVariable(compensationTasks.get(1).getId(), "var");
    Assert.assertNotEquals(value1, value2);
  }

  @Test
  public void testCannotMigrateWithoutCompensationStartEventCase1() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_EVENT_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_EVENT_SUBPROCESS_MODEL);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask2", "userTask2")
      .mapActivities("compensationBoundary", "compensationBoundary")
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
        .hasActivityInstanceFailures(sourceProcessDefinition.getId(),
            "Cannot migrate subscription for compensation handler 'eventSubProcess'. "
            + "There is no migration instruction for the compensation start event");
    }
  }

  @Test
  public void testCannotMigrateWithoutCompensationStartEventCase2() {
    // given
    BpmnModelInstance model = modify(CompensationModels.COMPENSATION_EVENT_SUBPROCESS_MODEL)
        .removeFlowNode("compensationBoundary");

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(model);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(model);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
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
        .hasActivityInstanceFailures(sourceProcessDefinition.getId(),
            "Cannot migrate subscription for compensation handler 'eventSubProcess'. "
            + "There is no migration instruction for the compensation start event");
    }
  }

  @Test
  public void testEventScopeHierarchyPreservation() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.DOUBLE_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.DOUBLE_SUBPROCESS_MODEL);

    try {
      // when
      rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("outerSubProcess", "innerSubProcess")
        .mapActivities("innerSubProcess", "outerSubProcess")
        .build();
      Assert.fail("exception expected");
    } catch (MigrationPlanValidationException e) {
      // then
      assertThat(e.getValidationReport())
      .hasInstructionFailures("innerSubProcess",
        "The closest mapped ancestor 'outerSubProcess' is mapped to scope 'innerSubProcess' "
        + "which is not an ancestor of target scope 'outerSubProcess'"
      );
    }

  }

  @Test
  public void testCompensationBoundaryHierarchyPreservation() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL)
        .addSubProcessTo(ProcessModels.PROCESS_KEY)
          .id("addedSubProcess")
          .embeddedSubProcess()
          .startEvent()
          .endEvent()
        .done());

    try {
      // when
      rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("subProcess", "addedSubProcess")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();
      Assert.fail("exception expected");
    } catch (MigrationPlanValidationException e) {
      // then
      assertThat(e.getValidationReport())
      .hasInstructionFailures("compensationBoundary",
        "The closest mapped ancestor 'subProcess' is mapped to scope 'addedSubProcess' "
        + "which is not an ancestor of target scope 'compensationBoundary'"
      );
    }
  }

  @Test
  public void testCannotMapCompensateStartEventWithoutMappingEventScopeCase1() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_EVENT_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_EVENT_SUBPROCESS_MODEL);

    try {
      // when
      rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("eventSubProcessStart", "eventSubProcessStart")
        .build();
      Assert.fail("exception expected");
    } catch (MigrationPlanValidationException e) {
      // then
      assertThat(e.getValidationReport())
        .hasInstructionFailures("eventSubProcessStart",
          "The source activity's event scope (subProcess) must be mapped to the target activity's event scope (subProcess)"
        );
    }
  }

  @Test
  public void testCannotMapCompensateStartEventWithoutMappingEventScopeCase2() {
    // given
    BpmnModelInstance model = modify(CompensationModels.COMPENSATION_EVENT_SUBPROCESS_MODEL)
        .removeFlowNode("compensationBoundary");

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(model);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(model);

    try {
      // when
      rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("eventSubProcessStart", "eventSubProcessStart")
        .build();
      Assert.fail("exception expected");
    } catch (MigrationPlanValidationException e) {
      // then
      assertThat(e.getValidationReport())
        .hasInstructionFailures("eventSubProcessStart",
          "The source activity's event scope (subProcess) must be mapped to the target activity's event scope (subProcess)"
      );
    }
  }
}
