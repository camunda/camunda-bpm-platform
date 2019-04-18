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
package org.camunda.bpm.engine.test.api.task;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class SubTaskDataTest {

  @Rule
  public ProcessEngineRule rule = new ProvidedProcessEngineRule();

  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;

  @Before
  public void init() {
    repositoryService = rule.getRepositoryService();
    runtimeService = rule.getRuntimeService();
    taskService = rule.getTaskService();
  }

  @Test
  @Deployment
  public void testSubTaskData() {
    //given simple process with user task
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subTaskTest");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // when set variable to user task
    taskService.setVariable(task.getId(), "testVariable", "testValue");

    // then variable is set in the scope of execution
    Assert.assertEquals("testValue", runtimeService.getVariable(task.getExecutionId(), "testVariable"));

    // when sub task is created create subtask for user task
    Task subTask = taskService.newTask("123456789");
    subTask.setParentTaskId(task.getId());
    subTask.setName("Test Subtask");
    taskService.saveTask(subTask);

    // and variable is update
    taskService.setVariable(subTask.getId(), "testVariable", "newTestValue");

    //then variable is also updated in the scope execution
    Assert.assertEquals("newTestValue", runtimeService.getVariable(task.getExecutionId(), "testVariable"));
  }
}
