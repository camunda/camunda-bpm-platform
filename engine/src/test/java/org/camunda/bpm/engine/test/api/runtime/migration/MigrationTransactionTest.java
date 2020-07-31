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

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;

import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.TransactionModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationTransactionTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testRule = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testRule);

  @Test
  public void testContinueProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testRule.deployAndGetDefinition(TransactionModels.ONE_TASK_TRANSACTION);
    ProcessDefinition targetProcessDefinition = testRule.deployAndGetDefinition(TransactionModels.ONE_TASK_TRANSACTION);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("transaction", "transaction")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    ProcessInstance processInstance = testRule.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testRule.completeTask("userTask");
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testContinueProcessTriggerCancellation() {
    // given
    ProcessDefinition sourceProcessDefinition = testRule.deployAndGetDefinition(TransactionModels.ONE_TASK_TRANSACTION);
    ProcessDefinition targetProcessDefinition = testRule.deployAndGetDefinition(TransactionModels.CANCEL_BOUNDARY_EVENT);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("transaction", "transaction")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    ProcessInstance processInstance = testRule.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testRule.completeTask("userTask");
    testRule.completeTask("afterBoundaryTask");
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAssertTrees() {
    // given
    ProcessDefinition sourceProcessDefinition = testRule.deployAndGetDefinition(TransactionModels.ONE_TASK_TRANSACTION);
    ProcessDefinition targetProcessDefinition = testRule.deployAndGetDefinition(TransactionModels.ONE_TASK_TRANSACTION);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("transaction", "transaction")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testRule.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testRule.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testRule.snapshotBeforeMigration.getProcessInstanceId())
          .child("userTask").scope().id(testRule.getSingleExecutionIdForActivityBeforeMigration("userTask")).up()
        .done());

    testRule.assertActivityTreeAfterMigration().hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("transaction", testRule.getSingleActivityInstanceBeforeMigration("transaction").getId())
          .activity("userTask", testRule.getSingleActivityInstanceBeforeMigration("userTask").getId())
      .done());
  }

  @Test
  public void testAddTransactionContinueProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testRule.deployAndGetDefinition(TransactionModels.ONE_TASK_TRANSACTION);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    ProcessInstance processInstance = testRule.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testRule.completeTask("userTask");
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddTransactionTriggerCancellation() {
    // given
    ProcessDefinition sourceProcessDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testRule.deployAndGetDefinition(TransactionModels.CANCEL_BOUNDARY_EVENT);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    ProcessInstance processInstance = testRule.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testRule.completeTask("userTask");
    testRule.completeTask("afterBoundaryTask");
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddTransactionAssertTrees() {
    // given
    ProcessDefinition sourceProcessDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testRule.deployAndGetDefinition(TransactionModels.ONE_TASK_TRANSACTION);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testRule.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testRule.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testRule.snapshotBeforeMigration.getProcessInstanceId())
          .child("userTask").scope()
        .done());

    testRule.assertActivityTreeAfterMigration().hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("transaction")
          .activity("userTask", testRule.getSingleActivityInstanceBeforeMigration("userTask").getId())
      .done());
  }

  @Test
  public void testRemoveTransactionContinueProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testRule.deployAndGetDefinition(TransactionModels.ONE_TASK_TRANSACTION);
    ProcessDefinition targetProcessDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    ProcessInstance processInstance = testRule.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testRule.completeTask("userTask");
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testRemoveTransactionAssertTrees() {
    // given
    ProcessDefinition sourceProcessDefinition = testRule.deployAndGetDefinition(TransactionModels.ONE_TASK_TRANSACTION);
    ProcessDefinition targetProcessDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testRule.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testRule.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree("userTask").scope().id(testRule.snapshotBeforeMigration.getProcessInstanceId())
        .done());

    testRule.assertActivityTreeAfterMigration().hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("userTask", testRule.getSingleActivityInstanceBeforeMigration("userTask").getId())
      .done());
  }

  @Test
  public void testMigrateTransactionToEmbeddedSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testRule.deployAndGetDefinition(TransactionModels.ONE_TASK_TRANSACTION);
    ProcessDefinition targetProcessDefinition = testRule.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("transaction", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    ProcessInstance processInstance = testRule.createProcessInstanceAndMigrate(migrationPlan);

    // then
    Assert.assertEquals(
        testRule.getSingleActivityInstanceBeforeMigration("transaction").getId(),
        testRule.getSingleActivityInstanceAfterMigration("subProcess").getId());

    testRule.completeTask("userTask");
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateEventSubProcessToTransaction() {
    // given
    ProcessDefinition sourceProcessDefinition = testRule.deployAndGetDefinition(EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testRule.deployAndGetDefinition(TransactionModels.ONE_TASK_TRANSACTION);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventSubProcess", "transaction")
      .mapActivities("eventSubProcessTask", "userTask")
      .build();

    // when
    ProcessInstance processInstance = rule.getRuntimeService()
      .createProcessInstanceById(sourceProcessDefinition.getId())
      .startBeforeActivity("eventSubProcessTask")
      .execute();

    testRule.migrateProcessInstance(migrationPlan, processInstance);

    // then
    Assert.assertEquals(
        testRule.getSingleActivityInstanceBeforeMigration("eventSubProcess").getId(),
        testRule.getSingleActivityInstanceAfterMigration("transaction").getId());

    testRule.completeTask("userTask");
    testRule.assertProcessEnded(processInstance.getId());
  }

}
