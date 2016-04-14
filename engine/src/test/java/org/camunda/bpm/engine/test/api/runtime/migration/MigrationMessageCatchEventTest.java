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

import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.MessageReceiveModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationMessageCatchEventTest {

  protected ProcessEngineRule rule = new PluggableProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testMigrateEventSubscription() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(MessageReceiveModels.ONE_MESSAGE_CATCH_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(MessageReceiveModels.ONE_MESSAGE_CATCH_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("messageCatch", "messageCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    testHelper.assertEventSubscriptionMigrated("messageCatch", "messageCatch", MessageReceiveModels.MESSAGE_NAME);

    // and it is possible to trigger the receive task
    rule.getRuntimeService().correlateMessage(MessageReceiveModels.MESSAGE_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateEventSubscriptionChangeActivityId() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(MessageReceiveModels.ONE_MESSAGE_CATCH_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(MessageReceiveModels.ONE_MESSAGE_CATCH_PROCESS)
        .changeElementId("messageCatch", "newMessageCatch"));

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("messageCatch", "newMessageCatch")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    testHelper.assertEventSubscriptionMigrated("messageCatch", "newMessageCatch", MessageReceiveModels.MESSAGE_NAME);

    // and it is possible to trigger the receive task
    rule.getRuntimeService().correlateMessage(MessageReceiveModels.MESSAGE_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateEventSubscriptionChangeMessageName() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(MessageReceiveModels.ONE_MESSAGE_CATCH_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.newModel()
        .startEvent()
        .intermediateCatchEvent("messageCatch")
          .message("new" + MessageReceiveModels.MESSAGE_NAME)
        .userTask("userTask")
        .endEvent()
        .done());

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("messageCatch", "messageCatch")
        .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then the message event subscription's event name has not changed
    testHelper.assertEventSubscriptionMigrated("messageCatch", "messageCatch", MessageReceiveModels.MESSAGE_NAME);

    // and it is possible to trigger the receive task
    rule.getRuntimeService().correlateMessage(MessageReceiveModels.MESSAGE_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

}
