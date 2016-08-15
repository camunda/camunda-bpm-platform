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
package org.camunda.bpm.qa.rolling.upgrade.callactivity;

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
@ScenarioUnderTest("ProcessWithCallActivityScenario")
@Origin("7.5.0")
public class CompleteProcessWithCallActivityTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("init.1")
  public void testCompleteProcessWithCallActivity() {
    //given process with user task before call activity
    TaskQuery taskQuery = rule.getTaskService().createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.taskName("Task before subprocess").singleResult();
    assertNotNull(taskBeforeSubProcess);

    // Completing the task continues the process which leads to calling the subprocess
    rule.getTaskService().complete(taskBeforeSubProcess.getId());
    Task taskInSubProcess = taskQuery.taskName("Task in subprocess").singleResult();
    assertNotNull(taskInSubProcess);

    // Completing the task in the subprocess, finishes the subprocess
    rule.getTaskService().complete(taskInSubProcess.getId());
    Task taskAfterSubProcess = taskQuery.taskName("Task after subprocess").singleResult();
    assertNotNull(taskAfterSubProcess);

    // Completing this task end the process instance
    rule.getTaskService().complete(taskAfterSubProcess.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.complete.one.1")
  public void testCompleteProcessWithCallActivityAndOneCompletedTask() {
    //given process within sub process
    TaskQuery taskQuery = rule.getTaskService().createTaskQuery();
    Task taskInSubProcess = taskQuery.taskName("Task in subprocess").singleResult();
    assertNotNull(taskInSubProcess);

    // Completing the task in the subprocess, finishes the subprocess
    rule.getTaskService().complete(taskInSubProcess.getId());
    Task taskAfterSubProcess = taskQuery.taskName("Task after subprocess").singleResult();
    assertNotNull(taskAfterSubProcess);

    // Completing this task end the process instance
    rule.getTaskService().complete(taskAfterSubProcess.getId());
    rule.assertScenarioEnded();
  }
}
