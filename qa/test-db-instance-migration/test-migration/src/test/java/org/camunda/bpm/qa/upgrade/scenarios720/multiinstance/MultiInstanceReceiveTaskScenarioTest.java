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
package org.camunda.bpm.qa.upgrade.scenarios720.multiinstance;

import static org.camunda.bpm.qa.upgrade.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.qa.upgrade.util.ActivityInstanceAssert.describeActivityInstanceTree;

import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@ScenarioUnderTest("MultiInstanceReceiveTaskScenario")
@Origin("7.2.0")
public class MultiInstanceReceiveTaskScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("initParallel.1")
  public void testInitParallelCompletion() {
    // when the receive task messages are correlated
    rule.messageCorrelation("Message").correlateAll();

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initParallel.2")
  public void testInitParallelActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        // no mi body due to missing execution
        .activity("miReceiveTask")
        .activity("miReceiveTask")
        .activity("miReceiveTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("initParallel.3")
  public void testInitParallelDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @Ignore("CAM-6408")
  @ScenarioUnderTest("initParallel.4")
  public void testInitParallelMigration() {
    // given
    ProcessInstance instance = rule.processInstance();
    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(instance.getProcessDefinitionId(), instance.getProcessDefinitionId())
      .mapEqualActivities()
      .build();

    // when
    rule.getRuntimeService().newMigration(migrationPlan)
      .processInstanceIds(instance.getId())
      .execute();

    // then the receive task messages can be correlated
    rule.messageCorrelation("Message").correlateAll();
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initSequential.1")
  public void testInitSequentialCompletion() {
    // when the receive task messages are correlated
    for (int i = 0; i < 3; i++) {
      rule.messageCorrelation("Message").correlate();
    }

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initSequential.2")
  public void testInitSequentialActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("miReceiveTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("initSequential.3")
  public void testInitSequentialDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @Ignore("CAM-6408")
  @ScenarioUnderTest("initSequential.4")
  public void testInitSequentialMigration() {
    // given
    ProcessInstance instance = rule.processInstance();
    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(instance.getProcessDefinitionId(), instance.getProcessDefinitionId())
      .mapEqualActivities()
      .build();

    // when
    rule.getRuntimeService().newMigration(migrationPlan)
      .processInstanceIds(instance.getId())
      .execute();

    // then the receive task messages can be correlated
    for (int i = 0; i < 3; i++) {
      rule.messageCorrelation("Message").correlate();
    }

    rule.assertScenarioEnded();
  }

}
