/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.qa.rolling.update.task;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.qa.rolling.update.RollingUpdateConstants;
import org.camunda.bpm.qa.rolling.upgrade.EngineVersions;
import org.camunda.bpm.qa.rolling.upgrade.RollingUpdateRule;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * This test ensures that the old engine can complete an
 * existing process with user task on the new schema.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@ScenarioUnderTest("ProcessWithUserTaskScenario")
@EngineVersions({ RollingUpdateConstants.OLD_ENGINE_TAG, RollingUpdateConstants.NEW_ENGINE_TAG})
public class CompleteProcessWithUserTaskTest {

  @Rule
  public RollingUpdateRule rule = new RollingUpdateRule();

  @Test
  @ScenarioUnderTest("init.1")
  public void testCompleteProcessWithUserTask() {
    //given an already started process instance
    ProcessInstance oldInstance = rule.processInstance();
    Assert.assertNotNull(oldInstance);

    //which waits on an user task
    TaskService taskService = rule.getTaskService();
    Task userTask = taskService.createTaskQuery().processInstanceId(oldInstance.getId()).singleResult();
    Assert.assertNotNull(userTask);

    //when completing the user task
    taskService.complete(userTask.getId());

    //then there exists no more tasks
    //and the process instance is also completed
    Assert.assertEquals(0, rule.taskQuery().count());
    rule.assertScenarioEnded();
  }

}
