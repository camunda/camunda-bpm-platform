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

import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.SignalCatchModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.HashMap;

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationSignalCatchEventTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testMigrateEventSubscription() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(SignalCatchModels.ONE_SIGNAL_CATCH_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(SignalCatchModels.ONE_SIGNAL_CATCH_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("signalCatch", "signalCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionMigrated("signalCatch", "signalCatch", SignalCatchModels.SIGNAL_NAME);

    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("signalCatch").scope().id(testHelper.getSingleExecutionIdForActivityBeforeMigration("signalCatch"))
        .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("signalCatch", testHelper.getSingleActivityInstanceBeforeMigration("signalCatch").getId())
      .done());

    // and it is possible to trigger the event
    rule.getRuntimeService().signalEventReceived(SignalCatchModels.SIGNAL_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateEventSubscriptionChangeActivityId() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(SignalCatchModels.ONE_SIGNAL_CATCH_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(SignalCatchModels.ONE_SIGNAL_CATCH_PROCESS)
        .changeElementId("signalCatch", "newSignalCatch"));

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("signalCatch", "newSignalCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionMigrated("signalCatch", "newSignalCatch", SignalCatchModels.SIGNAL_NAME);

    // and it is possible to trigger the event
    rule.getRuntimeService().signalEventReceived(SignalCatchModels.SIGNAL_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateEventSubscriptionPreserveSignalName() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(SignalCatchModels.ONE_SIGNAL_CATCH_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.newModel()
        .startEvent()
        .intermediateCatchEvent("signalCatch")
          .signal("new" + SignalCatchModels.SIGNAL_NAME)
        .userTask("userTask")
        .endEvent()
        .done());

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("signalCatch", "signalCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then the signal name of the event subscription has not changed
    testHelper.assertEventSubscriptionMigrated("signalCatch", "signalCatch", SignalCatchModels.SIGNAL_NAME);

    // and it is possible to trigger the event
    rule.getRuntimeService().signalEventReceived(SignalCatchModels.SIGNAL_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateEventSubscriptionUpdateSignalName() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(SignalCatchModels.ONE_SIGNAL_CATCH_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.newModel()
        .startEvent()
        .intermediateCatchEvent("signalCatch")
          .signal("new" + SignalCatchModels.SIGNAL_NAME)
        .userTask("userTask")
        .endEvent()
        .done());

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("signalCatch", "signalCatch")
          .updateEventTrigger()
        .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then the message event subscription's event name has not changed
    testHelper.assertEventSubscriptionMigrated(
        "signalCatch", SignalCatchModels.SIGNAL_NAME,
        "signalCatch", "new" + SignalCatchModels.SIGNAL_NAME);

    // and it is possible to trigger the event
    rule.getRuntimeService().signalEventReceived("new" + SignalCatchModels.SIGNAL_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateJobAddParentScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(SignalCatchModels.ONE_SIGNAL_CATCH_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(SignalCatchModels.SUBPROCESS_SIGNAL_CATCH_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("signalCatch", "signalCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionMigrated("signalCatch", "signalCatch", SignalCatchModels.SIGNAL_NAME);

    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child(null).scope()
            .child("signalCatch").scope().id(testHelper.getSingleExecutionIdForActivityBeforeMigration("signalCatch"))
        .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("subProcess")
          .activity("signalCatch", testHelper.getSingleActivityInstanceBeforeMigration("signalCatch").getId())
      .done());

    // and it is possible to trigger the event
    rule.getRuntimeService().signalEventReceived(SignalCatchModels.SIGNAL_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateEventSubscriptionUpdateSignalExpressionNameWithVariables() {
    // given
    String newSignalName = "new" + SignalCatchModels.SIGNAL_NAME + "-${var}";
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(SignalCatchModels.ONE_SIGNAL_CATCH_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.newModel()
        .startEvent()
        .intermediateCatchEvent("signalCatch")
        .signal(newSignalName)
        .userTask("userTask")
        .endEvent()
        .done());

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("signalCatch", "signalCatch")
        .updateEventTrigger()
        .build();

    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("var", "foo");


    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan, variables);

    // then there should be a variable
    VariableInstance beforeMigration = testHelper.snapshotBeforeMigration.getSingleVariable("var");
    Assert.assertEquals(1, testHelper.snapshotAfterMigration.getVariables().size());
    testHelper.assertVariableMigratedToExecution(beforeMigration, beforeMigration.getExecutionId());

    // and the signal event subscription's event name has changed
    String resolvedSignalName = "new" + SignalCatchModels.SIGNAL_NAME + "-foo";
    testHelper.assertEventSubscriptionMigrated(
        "signalCatch", SignalCatchModels.SIGNAL_NAME,
        "signalCatch", resolvedSignalName);

    // and it is possible to trigger the event and complete the task afterwards
    rule.getRuntimeService().signalEventReceived(resolvedSignalName);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

}
