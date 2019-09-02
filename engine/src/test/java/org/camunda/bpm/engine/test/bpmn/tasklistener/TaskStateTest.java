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
package org.camunda.bpm.engine.test.bpmn.tasklistener;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.TaskState;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.List;

public class TaskStateTest {

  public              ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public              ProcessEngineTestRule     testRule   = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService                 runtimeService;
  protected TaskService                    taskService;
  protected HistoryService                 historyService;

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    historyService = engineRule.getHistoryService();
  }

  @After
  public void tearDown() {
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.deleteTask(task.getId(), true);
    }
  }

  @Test
  public void testCreatedStateAfterInit() {
    // given
    Task newTask = taskService.newTask();

    // when
    taskService.saveTask(newTask);

    // then
    Task createdTask = taskService.createTaskQuery().singleResult();
    assertThat(createdTask.getLifecycleState()).isEqualToIgnoringCase(TaskState.STATE_CREATED);
  }

  @Test
  public void testCreatedStateAfterUpdates() {
    // given
    Task newTask = taskService.newTask();
    taskService.saveTask(newTask);

    // when
    Task updatedTask = taskService.createTaskQuery().singleResult();
    updatedTask.setAssignee("demo");
    updatedTask.setOwner("john");
    taskService.saveTask(updatedTask);

    // then
    updatedTask = taskService.createTaskQuery().singleResult();
    assertThat(updatedTask.getLifecycleState()).isEqualToIgnoringCase(TaskState.STATE_CREATED);
  }

  @Ignore("CAM-10724")
  @Test
  public void testCompletedStateAfterComplete() {
    // given
    Task newTask = taskService.newTask();
    taskService.saveTask(newTask);

    // when
    taskService.complete(newTask.getId());

    // then
    assertThat(newTask.getLifecycleState()).isEqualToIgnoringCase(TaskState.STATE_COMPLETED);
  }

  @Ignore("CAM-10724")
  @Test
  public void testDeletedStateAfterDelete() {
    // given
    Task newTask = taskService.newTask();
    taskService.saveTask(newTask);

    // when
    taskService.deleteTask(newTask.getId());

    // then
    assertThat(newTask.getLifecycleState()).isEqualToIgnoringCase(TaskState.STATE_DELETED);
  }
}
