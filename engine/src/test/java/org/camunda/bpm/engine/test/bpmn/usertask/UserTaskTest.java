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
package org.camunda.bpm.engine.test.bpmn.usertask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Joram Barrez
 */
public class UserTaskTest extends PluggableProcessEngineTest {

  @Before
  public void setUp() throws Exception {
    identityService.saveUser(identityService.newUser("fozzie"));
    identityService.saveUser(identityService.newUser("kermit"));

    identityService.saveGroup(identityService.newGroup("accountancy"));
    identityService.saveGroup(identityService.newGroup("management"));

    identityService.createMembership("fozzie", "accountancy");
    identityService.createMembership("kermit", "management");
  }

  @After
  public void tearDown() throws Exception {
    identityService.deleteUser("fozzie");
    identityService.deleteUser("kermit");
    identityService.deleteGroup("accountancy");
    identityService.deleteGroup("management");
  }

  @Deployment
  @Test
  public void testTaskPropertiesNotNull() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task.getId());
    assertEquals("my task", task.getName());
    assertEquals("Very important", task.getDescription());
    assertTrue(task.getPriority() > 0);
    assertEquals("kermit", task.getAssignee());
    assertEquals(processInstance.getId(), task.getProcessInstanceId());
    assertEquals(processInstance.getId(), task.getExecutionId());
    assertNotNull(task.getProcessDefinitionId());
    assertNotNull(task.getTaskDefinitionKey());
    assertNotNull(task.getCreateTime());

    // the next test verifies that if an execution creates a task, that no events are created during creation of the task.
    if (processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      assertEquals(0, taskService.getTaskEvents(task.getId()).size());
    }
  }

  @Deployment
  @Test
  public void testQuerySortingWithParameter() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertEquals(1, taskService.createTaskQuery().processInstanceId(processInstance.getId()).list().size());
  }

  @Deployment
  @Test
  public void testCompleteAfterParallelGateway() throws InterruptedException {
	  // related to http://jira.codehaus.org/browse/ACT-1054

	  // start the process
    runtimeService.startProcessInstanceByKey("ForkProcess");
    List<Task> taskList = taskService.createTaskQuery().list();
    assertNotNull(taskList);
    assertEquals(2, taskList.size());

    // make sure user task exists
    Task task = taskService.createTaskQuery().taskDefinitionKey("SimpleUser").singleResult();
  	assertNotNull(task);

  	// attempt to complete the task and get PersistenceException pointing to "referential integrity constraint violation"
  	taskService.complete(task.getId());
	}

  @Deployment
  @Test
  public void testComplexScenarioWithSubprocessesAndParallelGateways() {
    runtimeService.startProcessInstanceByKey("processWithSubProcessesAndParallelGateways");

    List<Task> taskList = taskService.createTaskQuery().list();
    assertNotNull(taskList);
    assertEquals(13, taskList.size());

  }

  @Deployment
  @Test
  public void testSimpleProcess() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("financialReport");

    List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("fozzie").list();
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("Write monthly financial report", task.getName());

    taskService.claim(task.getId(), "fozzie");
    tasks = taskService
      .createTaskQuery()
      .taskAssignee("fozzie")
      .list();

    assertEquals(1, tasks.size());
    taskService.complete(task.getId());

    tasks = taskService.createTaskQuery().taskCandidateUser("fozzie").list();
    assertEquals(0, tasks.size());
    tasks = taskService.createTaskQuery().taskCandidateUser("kermit").list();
    assertEquals(1, tasks.size());
    assertEquals("Verify monthly financial report", tasks.get(0).getName());
    taskService.complete(tasks.get(0).getId());

    testRule.assertProcessEnded(processInstance.getId());
  }
}
