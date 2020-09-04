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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
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
public class TaskCandidateTest extends PluggableProcessEngineTest {

  private static final String MANAGEMENT = "management";

  private static final String KERMIT = "kermit";

  private static final String GONZO = "gonzo";

  @Before
  public void setUp() throws Exception {


    Group accountants = identityService.newGroup("accountancy");
    identityService.saveGroup(accountants);
    Group managers = identityService.newGroup(MANAGEMENT);
    identityService.saveGroup(managers);
    Group sales = identityService.newGroup("sales");
    identityService.saveGroup(sales);

    User kermit = identityService.newUser(KERMIT);
    identityService.saveUser(kermit);
    identityService.createMembership(KERMIT, "accountancy");

    User gonzo = identityService.newUser(GONZO);
    identityService.saveUser(gonzo);
    identityService.createMembership(GONZO, MANAGEMENT);
    identityService.createMembership(GONZO, "accountancy");
    identityService.createMembership(GONZO, "sales");
  }

  @After
  public void tearDown() throws Exception {
    identityService.deleteUser(KERMIT);
    identityService.deleteUser(GONZO);
    identityService.deleteGroup("sales");
    identityService.deleteGroup("accountancy");
    identityService.deleteGroup(MANAGEMENT);


  }

  @Deployment
  @Test
  public void testSingleCandidateGroup() {

    // Deploy and start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("singleCandidateGroup");

    // Task should not yet be assigned to kermit
    List<Task> tasks = taskService
      .createTaskQuery()
      .taskAssignee(KERMIT)
      .list();
    assertTrue(tasks.isEmpty());

    // The task should be visible in the candidate task list
    tasks = taskService.createTaskQuery().taskCandidateUser(KERMIT).list();
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("Pay out expenses", task.getName());

    // The above query again, now between 'or' and 'endOr'
    tasks = taskService.createTaskQuery().or().taskCandidateUser(KERMIT).endOr().list();
    assertEquals(1, tasks.size());
    task = tasks.get(0);
    assertEquals("Pay out expenses", task.getName());

    // Claim the task
    taskService.claim(task.getId(), KERMIT);

    // The task must now be gone from the candidate task list
    tasks = taskService.createTaskQuery().taskCandidateUser(KERMIT).list();
    assertTrue(tasks.isEmpty());

    // The task will be visible on the personal task list
    tasks = taskService
      .createTaskQuery()
      .taskAssignee(KERMIT)
      .list();
    assertEquals(1, tasks.size());
    task = tasks.get(0);
    assertEquals("Pay out expenses", task.getName());

    // Completing the task ends the process
    taskService.complete(task.getId());

    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment
  @Test
  public void testMultipleCandidateGroups() {

    // Deploy and start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("multipleCandidatesGroup");

    // Task should not yet be assigned to anyone
    List<Task> tasks = taskService
      .createTaskQuery()
      .taskAssignee(KERMIT)
      .list();

    assertTrue(tasks.isEmpty());
    tasks = taskService
      .createTaskQuery()
      .taskAssignee(GONZO)
      .list();

    assertTrue(tasks.isEmpty());

    // The task should be visible in the candidate task list of Gonzo and Kermit
    // and anyone in the management/accountancy group
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser(KERMIT).list().size());
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser(GONZO).list().size());
    assertEquals(1, taskService.createTaskQuery().taskCandidateGroup(MANAGEMENT).count());
    assertEquals(1, taskService.createTaskQuery().taskCandidateGroup("accountancy").count());
    assertEquals(0, taskService.createTaskQuery().taskCandidateGroup("sales").count());

    // Gonzo claims the task
    tasks = taskService.createTaskQuery().taskCandidateUser(GONZO).list();
    Task task = tasks.get(0);
    assertEquals("Approve expenses", task.getName());
    taskService.claim(task.getId(), GONZO);

    // The task must now be gone from the candidate task lists
    assertTrue(taskService.createTaskQuery().taskCandidateUser(KERMIT).list().isEmpty());
    assertTrue(taskService.createTaskQuery().taskCandidateUser(GONZO).list().isEmpty());
    assertEquals(0, taskService.createTaskQuery().taskCandidateGroup(MANAGEMENT).count());

    // The task will be visible on the personal task list of Gonzo
    assertEquals(1, taskService
      .createTaskQuery()
      .taskAssignee(GONZO)
      .count());

    // But not on the personal task list of (for example) Kermit
    assertEquals(0, taskService.createTaskQuery().taskAssignee(KERMIT).count());

    // Completing the task ends the process
    taskService.complete(task.getId());

    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment
  @Test
  public void testMultipleCandidateUsers() {
    runtimeService.startProcessInstanceByKey("multipleCandidateUsersExample");

    assertEquals(1, taskService.createTaskQuery().taskCandidateUser(GONZO).list().size());
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser(KERMIT).list().size());
  }

  @Deployment
  @Test
  public void testMixedCandidateUserAndGroup() {
    runtimeService.startProcessInstanceByKey("mixedCandidateUserAndGroupExample");

    assertEquals(1, taskService.createTaskQuery().taskCandidateUser(GONZO).list().size());
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser(KERMIT).list().size());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/usertask/groupTest.bpmn")
  @Test
  public void testInvolvedUserQuery() {

    // given
    identityService.createMembership(KERMIT, MANAGEMENT);

    runtimeService.startProcessInstanceByKey("Process_13pqtqg");

    // when
    List<Task> tasks = taskService.createTaskQuery()
        .taskInvolvedUser(KERMIT)
        .list();

    assertThat(tasks).hasSize(1);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testInvolvedUserQueryForAssignee() {

    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task task = taskService.createTaskQuery().singleResult();
    taskService.setAssignee(task.getId(), KERMIT);

    // when
    List<Task> tasks = taskService.createTaskQuery()
        .taskInvolvedUser(KERMIT)
        .list();

    // then
    assertThat(tasks).hasSize(1);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testInvolvedUserQueryForOwner() {

    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task task = taskService.createTaskQuery().singleResult();
    taskService.setOwner(task.getId(), KERMIT);

    // when
    List<Task> tasks = taskService.createTaskQuery()
        .taskInvolvedUser(KERMIT)
        .list();

    // then
    assertThat(tasks).hasSize(1);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/usertask/groupTest.bpmn")
  @Test
  public void testInvolvedUserQueryOr() {

    // given
    identityService.createMembership(KERMIT, MANAGEMENT);

    runtimeService.startProcessInstanceByKey("Process_13pqtqg");

    // when
    List<Task> tasks = taskService.createTaskQuery()
        .or()
          .taskCandidateGroup(MANAGEMENT)
          .taskInvolvedUser(KERMIT)
        .endOr()
        .list();

    assertThat(tasks).hasSize(2);
  }
}
