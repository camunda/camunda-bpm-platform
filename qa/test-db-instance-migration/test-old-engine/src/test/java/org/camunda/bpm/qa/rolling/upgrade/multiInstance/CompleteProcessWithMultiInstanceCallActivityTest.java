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
package org.camunda.bpm.qa.rolling.upgrade.multiInstance;

import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@ScenarioUnderTest("ProcessWithMultiInstanceCallActivityScenario")
@Origin("7.5.0")
public class CompleteProcessWithMultiInstanceCallActivityTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("init.1")
  public void testCompleteProcessWithCallActivity() {
    //given process with user task before multi-instance call activity
    TaskQuery taskQuery = rule.getTaskService().createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.taskName("Task before multi-instance").singleResult();
    assertNotNull(taskBeforeSubProcess);

    //when the task before is complete the process leads to calling the multi-instance subprocess
    rule.getTaskService().complete(taskBeforeSubProcess.getId());

    Task taskAfterSubProcess = taskQuery.taskName("Task after multi-instance").singleResult();
    assertNotNull(taskAfterSubProcess);

    //after completing the after task the process instance ends
    rule.getTaskService().complete(taskAfterSubProcess.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.complete.one.1")
  public void testCompleteProcessWithCallActivityAndOneCompletedTask() {
    //given process after multi-instance callactivity
    TaskQuery taskQuery = rule.getTaskService().createTaskQuery();
    Task taskAfterSubProcess = taskQuery.taskName("Task after multi-instance").singleResult();
    assertNotNull(taskAfterSubProcess);

    // Completing this task end the process instance
    rule.getTaskService().complete(taskAfterSubProcess.getId());
    rule.assertScenarioEnded();
  }

}
