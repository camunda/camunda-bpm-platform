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
package org.camunda.bpm.qa.upgrade.scenarios720.eventsubprocess;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.camunda.bpm.qa.upgrade.util.CompleteTaskThread;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
@ScenarioUnderTest("NestedInterruptingEventSubprocessParallelScenario")
@Origin("7.2.0")
public class NestedInterruptingEventSubprocessParallelScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("init.1")
  public void testInitSynchronization() {
    // given
    Task eventSubProcessTask1 = rule.taskQuery().taskDefinitionKey("innerEventSubProcessTask1").singleResult();
    Task eventSubProcessTask2 = rule.taskQuery().taskDefinitionKey("innerEventSubProcessTask2").singleResult();

    // when
    CompleteTaskThread completeTaskThread1 = new CompleteTaskThread(eventSubProcessTask1.getId(),
        (ProcessEngineConfigurationImpl) rule.getProcessEngine().getProcessEngineConfiguration());

    CompleteTaskThread completeTaskThread2 = new CompleteTaskThread(eventSubProcessTask2.getId(),
        (ProcessEngineConfigurationImpl) rule.getProcessEngine().getProcessEngineConfiguration());

    completeTaskThread1.startAndWaitUntilControlIsReturned();
    completeTaskThread2.startAndWaitUntilControlIsReturned();

    completeTaskThread1.proceedAndWaitTillDone();
    completeTaskThread2.proceedAndWaitTillDone();

    // then
    Assert.assertNull(completeTaskThread1.getException());
    Assert.assertNotNull(completeTaskThread2.getException());
  }
}
