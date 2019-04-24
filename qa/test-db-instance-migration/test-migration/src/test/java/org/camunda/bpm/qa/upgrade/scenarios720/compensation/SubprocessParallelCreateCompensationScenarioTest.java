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
package org.camunda.bpm.qa.upgrade.scenarios720.compensation;

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
@ScenarioUnderTest("SubprocessParallelCreateCompensationScenario")
@Origin("7.2.0")
public class SubprocessParallelCreateCompensationScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("init.1")
  public void testInitCompletionCase1() {
    // given
    Task afterUserTask1 = rule.taskQuery().taskDefinitionKey("afterUserTask1").singleResult();
    Task userTask2 = rule.taskQuery().taskDefinitionKey("userTask2").singleResult();

    // when the subprocess is completed by first compacting the concurrent execution
    // in which context a compensation subscription was already created
    rule.getTaskService().complete(afterUserTask1.getId());

    // and then userTask2 is completed
    rule.getTaskService().complete(userTask2.getId());

    // and compensation is thrown
    Task beforeCompensationTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensationTask.getId());

    // then there is are two active compensation handler task
    Assert.assertEquals(2, rule.taskQuery().count());
    Task undoTask1 = rule.taskQuery().taskDefinitionKey("undoTask1").singleResult();
    Task undoTask2 = rule.taskQuery().taskDefinitionKey("undoTask2").singleResult();
    Assert.assertNotNull(undoTask1);
    Assert.assertNotNull(undoTask2);

    // and they can be completed such that the process instance ends successfully
    rule.getTaskService().complete(undoTask1.getId());
    rule.getTaskService().complete(undoTask2.getId());

    Task afterCompensateTask = rule.taskQuery().taskDefinitionKey("afterCompensate").singleResult();
    Assert.assertNotNull(afterCompensateTask);

    rule.getTaskService().complete(afterCompensateTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.2")
  public void testInitCompletionCase2() {
    // given
    Task afterUserTask1 = rule.taskQuery().taskDefinitionKey("afterUserTask1").singleResult();
    Task userTask2 = rule.taskQuery().taskDefinitionKey("userTask2").singleResult();

    // when the task is completed first that belongs to an execution in which context
    // no event subscription was created yet
    rule.getTaskService().complete(userTask2.getId());

    // and then afterUserTask1 is completed
    rule.getTaskService().complete(afterUserTask1.getId());

    // and compensation is thrown
    Task beforeCompensationTask = rule.taskQuery().singleResult();
    rule.getTaskService().complete(beforeCompensationTask.getId());

    // then there is are two active compensation handler task
    Assert.assertEquals(2, rule.taskQuery().count());
    Task undoTask1 = rule.taskQuery().taskDefinitionKey("undoTask1").singleResult();
    Task undoTask2 = rule.taskQuery().taskDefinitionKey("undoTask2").singleResult();
    Assert.assertNotNull(undoTask2);
    Assert.assertNotNull(undoTask1);

    // and they can be completed such that the process instance ends successfully
    rule.getTaskService().complete(undoTask1.getId());
    rule.getTaskService().complete(undoTask2.getId());

    Task afterCompensateTask = rule.taskQuery().taskDefinitionKey("afterCompensate").singleResult();
    Assert.assertNotNull(afterCompensateTask);

    rule.getTaskService().complete(afterCompensateTask.getId());
    rule.assertScenarioEnded();
  }
}

