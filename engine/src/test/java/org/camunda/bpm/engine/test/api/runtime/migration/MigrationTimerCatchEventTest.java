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

import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.TimerCatchModels;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationTimerCatchEventTest {

  protected ProcessEngineRule rule = new ProcessEngineRule(true);
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testMigrateJob() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(TimerCatchModels.ONE_TIMER_CATCH_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(TimerCatchModels.ONE_TIMER_CATCH_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("timerCatch", "timerCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    testHelper.assertJobMigrated(
        testHelper.snapshotBeforeMigration.getJobs().get(0),
        "timerCatch");

    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("timerCatch").scope().id(testHelper.getSingleExecutionIdForActivityBeforeMigration("timerCatch"))
        .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("timerCatch", testHelper.getSingleActivityInstanceBeforeMigration("timerCatch").getId())
      .done());

    // and it is possible to trigger the event
    Job jobAfterMigration = testHelper.snapshotAfterMigration.getJobs().get(0);
    rule.getManagementService().executeJob(jobAfterMigration.getId());

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateJobChangeActivityId() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(TimerCatchModels.ONE_TIMER_CATCH_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(TimerCatchModels.ONE_TIMER_CATCH_PROCESS)
        .changeElementId("timerCatch", "newTimerCatch"));

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("timerCatch", "newTimerCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    testHelper.assertJobMigrated(
        testHelper.snapshotBeforeMigration.getJobs().get(0),
        "newTimerCatch");

    // and it is possible to trigger the event
    Job jobAfterMigration = testHelper.snapshotAfterMigration.getJobs().get(0);
    rule.getManagementService().executeJob(jobAfterMigration.getId());

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateJobChangeTimerConfiguration() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(TimerCatchModels.ONE_TIMER_CATCH_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.newModel()
      .startEvent()
      .intermediateCatchEvent("timerCatch")
        .timerWithDuration("PT50M")
      .userTask("userTask")
      .endEvent()
      .done());

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("timerCatch", "timerCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertJobMigrated(       // this also asserts that the due has not changed
        testHelper.snapshotBeforeMigration.getJobs().get(0),
        "timerCatch");

    // and it is possible to trigger the event
    Job jobAfterMigration = testHelper.snapshotAfterMigration.getJobs().get(0);
    rule.getManagementService().executeJob(jobAfterMigration.getId());

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateJobChangeProcessKey() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(TimerCatchModels.ONE_TIMER_CATCH_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(TimerCatchModels.ONE_TIMER_CATCH_PROCESS)
        .changeElementId(ProcessModels.PROCESS_KEY, "new" + ProcessModels.PROCESS_KEY));

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("timerCatch", "timerCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    testHelper.assertJobMigrated(
        testHelper.snapshotBeforeMigration.getJobs().get(0),
        "timerCatch");

    // and it is possible to trigger the event
    Job jobAfterMigration = testHelper.snapshotAfterMigration.getJobs().get(0);
    rule.getManagementService().executeJob(jobAfterMigration.getId());

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateJobAddParentScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(TimerCatchModels.ONE_TIMER_CATCH_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(TimerCatchModels.SUBPROCESS_TIMER_CATCH_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("timerCatch", "timerCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertJobMigrated(
        testHelper.snapshotBeforeMigration.getJobs().get(0),
        "timerCatch");

    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child(null).scope()
            .child("timerCatch").scope().id(testHelper.getSingleExecutionIdForActivityBeforeMigration("timerCatch"))
        .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("subProcess")
          .activity("timerCatch", testHelper.getSingleActivityInstanceBeforeMigration("timerCatch").getId())
      .done());

    // and it is possible to trigger the event
    Job jobAfterMigration = testHelper.snapshotAfterMigration.getJobs().get(0);
    rule.getManagementService().executeJob(jobAfterMigration.getId());

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }
}
