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
package org.camunda.bpm.qa.upgrade.scenarios730.boundary;

import java.util.List;

import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
@ScenarioUnderTest("NonInterruptingBoundaryEventScenario")
@Origin("7.3.0")
public class NonInterruptingBoundaryEventScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("init.1")
  public void testInitActivityInstanceStatistics() {
    // given
    ProcessInstance processInstance = rule.processInstance();

    // when
    List<ActivityStatistics> activityStatistics = rule.getManagementService()
      .createActivityStatisticsQuery(processInstance.getProcessDefinitionId())
      .list();

    // then
    Assert.assertEquals(2, activityStatistics.size());

    ActivityStatistics outerTaskStatistics = getStatistics(activityStatistics, "outerTask");
    Assert.assertNotNull(outerTaskStatistics);
    Assert.assertEquals("outerTask", outerTaskStatistics.getId());
    Assert.assertEquals(1, outerTaskStatistics.getInstances());

    ActivityStatistics afterBoundaryStatistics = getStatistics(activityStatistics, "afterBoundaryTask");
    Assert.assertNotNull(afterBoundaryStatistics);
    Assert.assertEquals("afterBoundaryTask", afterBoundaryStatistics.getId());
    Assert.assertEquals(1, afterBoundaryStatistics.getInstances());
  }

  protected ActivityStatistics getStatistics(List<ActivityStatistics> activityStatistics, String activityId) {
    for (ActivityStatistics statistics : activityStatistics) {
      if (activityId.equals(statistics.getId())) {
        return statistics;
      }
    }

    return null;
  }
}
