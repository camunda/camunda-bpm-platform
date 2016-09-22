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
package org.camunda.bpm.engine.test.bpmn.event.conditional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class IntermediateConditionalEventTest {

  @Rule
  public final ProcessEngineRule engine = new ProvidedProcessEngineRule();

  private RuntimeService runtimeService;
  private TaskService taskService;

  @BeforeClass
  public static void setUp() throws Exception {
    org.h2.tools.Server.createWebServer("-web").start();
  }

  @Before
  public void init() {
    this.runtimeService = engine.getRuntimeService();
    this.taskService = engine.getTaskService();
  }

  @Test
  @Deployment
  public void testFalseIntermediateConditionalEvent() {
    //given process with intermediate conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("conditionalEventProcess");

    TaskQuery taskQuery = taskService.createTaskQuery();
    Task task = taskQuery.processInstanceId(procInst.getId()).singleResult();
    assertNotNull(task);
    assertEquals("Before Condition", task.getName());

    //when task before condition is completed
    taskService.complete(task.getId());

    //then next wait state is on conditional event, since condition is false
    Execution execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId("conditionalEvent")
             .singleResult();
    assertNotNull(execution);
  }

  @Test
  @Deployment
  public void testTrueIntermediateConditionalEvent() {
    //given process with intermediate conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("conditionalEventProcess");

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals("Before Condition", task.getName());

    //when task before condition is completed
    taskService.complete(task.getId());

    //then next wait state is on user task after conditional event, since condition was true
    Execution execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId("conditionalEvent")
             .singleResult();
    assertNull(execution);

    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals("After Condition", task.getName());
  }
}
