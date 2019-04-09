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
package org.camunda.bpm.qa.upgrade.scenarios730.compensation;

import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.task.Task;
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
@ScenarioUnderTest("NestedCompensationScenario")
@Origin("7.3.0")
public class NestedCompensationScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("init.throwCompensate.1")
  public void testHistory() {
    // given
    Task compensationHandler = rule.taskQuery().singleResult();

    // when
    rule.getTaskService().complete(compensationHandler.getId());

    // then history is written for the remaining activity instances
    HistoricProcessInstance historicProcessInstance = rule.historicProcessInstance();
    Assert.assertNotNull(historicProcessInstance);
    Assert.assertNotNull(historicProcessInstance.getEndTime());

    HistoricActivityInstance subProcessInstance = rule.getHistoryService()
        .createHistoricActivityInstanceQuery()
        .processInstanceId(historicProcessInstance.getId())
        .activityId("subProcess")
        .singleResult();

    Assert.assertNotNull(subProcessInstance);
    Assert.assertNotNull(subProcessInstance.getEndTime());
    Assert.assertEquals(historicProcessInstance.getId(), subProcessInstance.getParentActivityInstanceId());

    HistoricActivityInstance compensationThrowInstance = rule.getHistoryService()
        .createHistoricActivityInstanceQuery()
        .processInstanceId(historicProcessInstance.getId())
        .activityId("throwCompensate")
        .singleResult();

    Assert.assertNotNull(compensationThrowInstance);
    Assert.assertNotNull(compensationThrowInstance.getEndTime());
    Assert.assertEquals(subProcessInstance.getId(), compensationThrowInstance.getParentActivityInstanceId());

    HistoricActivityInstance compensationHandlerInstance = rule.getHistoryService()
        .createHistoricActivityInstanceQuery()
        .processInstanceId(historicProcessInstance.getId())
        .activityId("undoTask")
        .singleResult();

    Assert.assertNotNull(compensationHandlerInstance);
    Assert.assertNotNull(compensationHandlerInstance.getEndTime());
    Assert.assertEquals(subProcessInstance.getId(), compensationHandlerInstance.getParentActivityInstanceId());
  }
}
