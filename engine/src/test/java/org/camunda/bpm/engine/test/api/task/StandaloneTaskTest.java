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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class StandaloneTaskTest extends PluggableProcessEngineTest {

  @Before
  public void setUp() throws Exception {

    identityService.saveUser(identityService.newUser("kermit"));
    identityService.saveUser(identityService.newUser("gonzo"));
  }

  @After
  public void tearDown() throws Exception {
    identityService.deleteUser("kermit");
    identityService.deleteUser("gonzo");

  }

  @Test
  public void testCreateToComplete() {

    // Create and save task
    Task task = taskService.newTask();
    task.setName("testTask");
    taskService.saveTask(task);
    String taskId = task.getId();

    // Add user as candidate user
    taskService.addCandidateUser(taskId, "kermit");
    taskService.addCandidateUser(taskId, "gonzo");

    // Retrieve task list for jbarrez
    List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("kermit").list();
    assertEquals(1, tasks.size());
    assertEquals("testTask", tasks.get(0).getName());

    // Retrieve task list for tbaeyens
    tasks = taskService.createTaskQuery().taskCandidateUser("gonzo").list();
    assertEquals(1, tasks.size());
    assertEquals("testTask", tasks.get(0).getName());

    // Claim task
    taskService.claim(taskId, "kermit");

    // Tasks shouldn't appear in the candidate tasklists anymore
    assertTrue(taskService.createTaskQuery().taskCandidateUser("kermit").list().isEmpty());
    assertTrue(taskService.createTaskQuery().taskCandidateUser("gonzo").list().isEmpty());

    // Complete task
    taskService.deleteTask(taskId, true);

    // Task should be removed from runtime data
    // TODO: check for historic data when implemented!
    assertNull(taskService.createTaskQuery().taskId(taskId).singleResult());
  }

  @Test
  public void testOptimisticLockingThrownOnMultipleUpdates() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    String taskId = task.getId();

    // first modification
    Task task1 = taskService.createTaskQuery().taskId(taskId).singleResult();
    Task task2 = taskService.createTaskQuery().taskId(taskId).singleResult();

    task1.setDescription("first modification");
    taskService.saveTask(task1);

    // second modification on the initial instance
    task2.setDescription("second modification");
    try {
      taskService.saveTask(task2);
      fail("should get an exception here as the task was modified by someone else.");
    } catch (OptimisticLockingException expected) {
      //  exception was thrown as expected
    }

    taskService.deleteTask(taskId, true);
  }

  // See http://jira.codehaus.org/browse/ACT-1290
  @Test
  public void testRevisionUpdatedOnSave() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    assertEquals(1, ((TaskEntity) task).getRevision());

    task.setDescription("first modification");
    taskService.saveTask(task);
    assertEquals(2, ((TaskEntity) task).getRevision());

    task.setDescription("second modification");
    taskService.saveTask(task);
    assertEquals(3, ((TaskEntity) task).getRevision());

    taskService.deleteTask(task.getId(), true);
  }

  @Test
  public void testSaveTaskWithGenericResourceId() {
    Task task = taskService.newTask("*");
    try {
      taskService.saveTask(task);
      fail("it should not be possible to save a task with the generic resource id *");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("Entity Task[*] has an invalid id: id cannot be *. * is a reserved identifier", e.getMessage());
    }
  }


}
