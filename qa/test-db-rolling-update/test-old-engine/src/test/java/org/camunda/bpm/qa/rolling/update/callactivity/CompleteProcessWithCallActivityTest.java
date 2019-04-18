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
package org.camunda.bpm.qa.rolling.update.callactivity;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.qa.rolling.update.AbstractRollingUpdateTestCase;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@ScenarioUnderTest("ProcessWithCallActivityScenario")
public class CompleteProcessWithCallActivityTest extends AbstractRollingUpdateTestCase {

  protected RuntimeService runtimeService;

  @Before
  public void setUp() {
    runtimeService = rule.getRuntimeService();
  }

  @Test
  @ScenarioUnderTest("init.1")
  public void testCompleteProcessWithCallActivity() {
    //given process with user task before call activity
    ProcessInstance processInstance = rule.processInstance();

    TaskQuery taskQuery = rule.getTaskService().createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.processInstanceId(processInstance.getId()).taskName("Task before subprocess").singleResult();
    assertNotNull(taskBeforeSubProcess);

    // Completing the task continues the process which leads to calling the subprocess
    rule.getTaskService().complete(taskBeforeSubProcess.getId());
    Execution subProcess = runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).singleResult();
    Task taskInSubProcess = taskQuery.processInstanceId(subProcess.getId()).taskName("Task in subprocess").singleResult();
    assertNotNull(taskInSubProcess);

    // Completing the task in the subprocess, finishes the subprocess
    rule.getTaskService().complete(taskInSubProcess.getId());
    Task taskAfterSubProcess = taskQuery.processInstanceId(processInstance.getId()).taskName("Task after subprocess").singleResult();
    assertNotNull(taskAfterSubProcess);

    // Completing this task end the process instance
    rule.getTaskService().complete(taskAfterSubProcess.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.complete.one.1")
  public void testCompleteProcessWithCallActivityAndOneCompletedTask() {
    //given process within sub process
    ProcessInstance processInstance = rule.processInstance();
    Execution subProcess = runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).singleResult();

    TaskQuery taskQuery = rule.getTaskService().createTaskQuery();
    Task taskInSubProcess = taskQuery.processInstanceId(subProcess.getId()).taskName("Task in subprocess").singleResult();
    assertNotNull(taskInSubProcess);

    // Completing the task in the subprocess, finishes the subprocess
    rule.getTaskService().complete(taskInSubProcess.getId());
    Task taskAfterSubProcess = taskQuery.processInstanceId(processInstance.getId()).taskName("Task after subprocess").singleResult();
    assertNotNull(taskAfterSubProcess);

    // Completing this task end the process instance
    rule.getTaskService().complete(taskAfterSubProcess.getId());
    rule.assertScenarioEnded();
  }
}
