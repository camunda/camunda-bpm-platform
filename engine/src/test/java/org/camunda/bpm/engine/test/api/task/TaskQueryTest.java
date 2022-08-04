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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.taskByAssignee;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.taskByCaseExecutionId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.taskByCaseInstanceId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.taskByCreateTime;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.taskByDescription;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.taskByDueDate;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.taskByExecutionId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.taskByFollowUpDate;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.taskById;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.taskByName;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.taskByPriority;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.taskByProcessInstanceId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.verifySortingAndCount;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.form.CamundaFormRef;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Joram Barrez
 * @author Frederik Heremans
 * @author Falko Menge
 */
public class TaskQueryTest extends PluggableProcessEngineTest {

  private List<String> taskIds;

  // The range of Oracle's NUMBER field is limited to ~10e+125
  // which is below Double.MAX_VALUE, so we only test with the following
  // max value
  protected static final double MAX_DOUBLE_VALUE = 10E+124;

  @Before
  public void setUp() throws Exception {

    identityService.saveUser(identityService.newUser("kermit"));
    identityService.saveUser(identityService.newUser("gonzo"));
    identityService.saveUser(identityService.newUser("fozzie"));

    identityService.saveGroup(identityService.newGroup("management"));
    identityService.saveGroup(identityService.newGroup("accountancy"));

    identityService.createMembership("kermit", "management");
    identityService.createMembership("kermit", "accountancy");
    identityService.createMembership("fozzie", "management");

    taskIds = generateTestTasks();
  }

  @After
  public void tearDown() throws Exception {
    identityService.deleteGroup("accountancy");
    identityService.deleteGroup("management");
    identityService.deleteUser("fozzie");
    identityService.deleteUser("gonzo");
    identityService.deleteUser("kermit");
    taskService.deleteTasks(taskIds, true);
  }

  public void tesBasicTaskPropertiesNotNull() {
    Task task = taskService.createTaskQuery().taskId(taskIds.get(0)).singleResult();
    assertNotNull(task.getDescription());
    assertNotNull(task.getId());
    assertNotNull(task.getName());
    assertNotNull(task.getCreateTime());
  }

  @Test
  public void testQueryNoCriteria() {
    TaskQuery query = taskService.createTaskQuery();
    assertEquals(12, query.count());
    assertEquals(12, query.list().size());
    try {
      query.singleResult();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
  }

  @Test
  public void testQueryByTaskId() {
    String taskId = taskIds.get(0);
    TaskQuery query = taskService.createTaskQuery().taskId(taskId);
    assertNotNull(query.singleResult());
    assertEquals(1, query.count());
    List<Task> foundTasks = query.list();
    assertEquals(1, foundTasks.size());
    List<String> foundTaskIds = foundTasks.stream().map(Task::getId).collect(Collectors.toList());
    assertThat(foundTaskIds).containsOnly(taskId);
  }

  @Test
  public void testQueryByTaskIdIn() {
    String task0Id = taskIds.get(0);
    String task1Id = taskIds.get(1);
    TaskQuery query = taskService.createTaskQuery().taskIdIn(task0Id, task1Id);
    assertEquals(2, query.count());
    List<Task> foundTasks = query.list();
    assertEquals(2, foundTasks.size());
    List<String> foundTaskIds = foundTasks.stream().map(Task::getId).collect(Collectors.toList());
    assertThat(foundTaskIds).containsOnly(task0Id, task1Id);
  }

  @Test
  public void testQueryByInvalidTaskId() {
    TaskQuery query = taskService.createTaskQuery().taskId("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());

    try {
      taskService.createTaskQuery().taskId(null);
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
  }

  @Test
  public void testQueryByName() {
    TaskQuery query = taskService.createTaskQuery().taskName("testTask");
    assertEquals(6, query.list().size());
    assertEquals(6, query.count());

    try {
      query.singleResult();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
  }

  @Test
  public void testQueryByInvalidName() {
    TaskQuery query = taskService.createTaskQuery().taskName("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());

    try {
      taskService.createTaskQuery().taskName(null).singleResult();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
  }

  @Test
  public void testQueryByNameLike() {
    TaskQuery query = taskService.createTaskQuery().taskNameLike("gonzo\\_%");
    assertNotNull(query.singleResult());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  public void testQueryByInvalidNameLike() {
    TaskQuery query = taskService.createTaskQuery().taskName("1");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());

    try {
      taskService.createTaskQuery().taskName(null).singleResult();
      fail();
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void testQueryByDescription() {
    TaskQuery query = taskService.createTaskQuery().taskDescription("testTask description");
    assertEquals(6, query.list().size());
    assertEquals(6, query.count());

    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Test
  public void testQueryByInvalidDescription() {
    TaskQuery query = taskService.createTaskQuery().taskDescription("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());

    try {
      taskService.createTaskQuery().taskDescription(null).list();
      fail();
    } catch (ProcessEngineException e) {

    }
  }


  /**
   * CAM-6363
   *
   * Verify that search by name returns case insensitive results
   */
  @Test
  public void testTaskQueryLookupByNameCaseInsensitive() {
    TaskQuery query = taskService.createTaskQuery();
    query.taskName("testTask");


    List<Task> tasks = query.list();
    assertNotNull(tasks);
    assertThat(tasks.size()).isEqualTo(6);

    query = taskService.createTaskQuery();
    query.taskName("TeStTaSk");

    tasks = query.list();
    assertNotNull(tasks);
    assertThat(tasks.size()).isEqualTo(6);
  }

  /**
   * CAM-6165
   *
   * Verify that search by name like returns case insensitive results
   */
  @Test
  public void testTaskQueryLookupByNameLikeCaseInsensitive() {
    TaskQuery query = taskService.createTaskQuery();
    query.taskNameLike("%task%");


    List<Task> tasks = query.list();
    assertNotNull(tasks);
    assertThat(tasks.size()).isEqualTo(10);

    query = taskService.createTaskQuery();
    query.taskNameLike("%Task%");

    tasks = query.list();
    assertNotNull(tasks);
    assertThat(tasks.size()).isEqualTo(10);
  }

  @Test
  public void testQueryByDescriptionLike() {
    TaskQuery query = taskService.createTaskQuery().taskDescriptionLike("%gonzo\\_%");
    assertNotNull(query.singleResult());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  public void testQueryByInvalidDescriptionLike() {
    TaskQuery query = taskService.createTaskQuery().taskDescriptionLike("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());

    try {
      taskService.createTaskQuery().taskDescriptionLike(null).list();
      fail();
    } catch (ProcessEngineException e) {

    }
  }

  @Test
  public void testQueryByPriority() {
    TaskQuery query = taskService.createTaskQuery().taskPriority(10);
    assertEquals(2, query.list().size());
    assertEquals(2, query.count());

    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {}

    query = taskService.createTaskQuery().taskPriority(100);
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());

    query = taskService.createTaskQuery().taskMinPriority(50);
    assertEquals(3, query.list().size());

    query = taskService.createTaskQuery().taskMinPriority(10);
    assertEquals(5, query.list().size());

    query = taskService.createTaskQuery().taskMaxPriority(10);
    assertEquals(9, query.list().size());

    query = taskService.createTaskQuery().taskMaxPriority(3);
    assertEquals(6, query.list().size());

    query = taskService.createTaskQuery().taskMinPriority(50).taskMaxPriority(10);
    assertEquals(0, query.list().size());

    query = taskService.createTaskQuery().taskPriority(30).taskMaxPriority(10);
    assertEquals(0, query.list().size());

    query = taskService.createTaskQuery().taskMinPriority(30).taskPriority(10);
    assertEquals(0, query.list().size());

    query = taskService.createTaskQuery().taskMinPriority(30).taskPriority(20).taskMaxPriority(10);
    assertEquals(0, query.list().size());
  }

  @Test
  public void testQueryByInvalidPriority() {
    try {
      taskService.createTaskQuery().taskPriority(null);
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
  }

  @Test
  public void testQueryByAssignee() {
    TaskQuery query = taskService.createTaskQuery().taskAssignee("gonzo_");
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
    assertNotNull(query.singleResult());

    query = taskService.createTaskQuery().taskAssignee("kermit");
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    assertNull(query.singleResult());
  }

  @Test
  public void testQueryByAssigneeLike() {
    TaskQuery query = taskService.createTaskQuery().taskAssigneeLike("gonz%\\_");
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
    assertNotNull(query.singleResult());

    query = taskService.createTaskQuery().taskAssignee("gonz");
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    assertNull(query.singleResult());
  }

  @Test
  public void testQueryByNullAssignee() {
    try {
      taskService.createTaskQuery().taskAssignee(null).list();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
  }

  @Test
  public void testQueryByAssigneeInPositive() {
    // given
    String[] assignees = {"fozzie", "john", "mary"};

    // when
    TaskQuery query = taskService.createTaskQuery().taskAssigneeIn(assignees);

    // then
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
  }

  @Test
  public void testQueryByAssigneeInNegative() {
    // given
    String[] assignees = {"kermit", "gonzo"};

    // when
    TaskQuery query = taskService.createTaskQuery().taskAssigneeIn(assignees);

    // then
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }

  @Test
  public void testQueryByAssigneeAndAssigneeIn () {
    // given
    String assignee = "fozzie";
    String[] assignees = {"fozzie", "john", "mary"};

    // when
    TaskQuery query = taskService.createTaskQuery()
      .taskAssignee(assignee).taskAssigneeIn(assignees);

    // then
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
  }

  @Test
  public void testQueryByAssigneeInNull() {
    // given
    String[] assignees = null;

    // when
    TaskQuery query = taskService.createTaskQuery();

    // then
    try {
      query.taskAssigneeIn(assignees);
      fail("Exception expected");
    } catch (Exception ex) {
      assertEquals("Assignees is null", ex.getMessage());
    }
  }

  @Test
  public void testQueryByAssigneeNotInPositive() {
    // given
    String[] assignees = {"fozzie", "john", "mary"};

    // when
    TaskQuery query = taskService.createTaskQuery().taskAssigneeNotIn(assignees);

    // then
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
  }

  @Test
  public void testQueryByAssigneeNotInNegative() {
    // given
    String[] assignees = {"gonzo_", "fozzie"};

    // when
    TaskQuery query = taskService.createTaskQuery().taskAssigneeNotIn(assignees);

    // then
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }

  @Test
  public void testQueryByAssigneeInAndAssigneeNotIn () {
    // given
    String[] assigneesIn = {"fozzie", "gonzo"};
    String[] assigneesNotIn = {"john", "mary"};

    // when
    TaskQuery query = taskService.createTaskQuery()
            .taskAssigneeIn(assigneesIn).taskAssigneeNotIn(assigneesNotIn);

    // then
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
  }

  @Test
  public void testQueryByAssigneeNotInNull() {
    // given
    String[] assignees = null;

    // when
    TaskQuery query = taskService.createTaskQuery();

    // then
    try {
        query.taskAssigneeNotIn(assignees);
        fail("Exception expected");
    } catch (Exception ex) {
        assertEquals("Assignees is null", ex.getMessage());
    }
  }

  @Test
  public void testQueryByUnassigned() {
    TaskQuery query = taskService.createTaskQuery().taskUnassigned();
    assertEquals(10, query.count());
    assertEquals(10, query.list().size());
  }

  @Test
  public void testQueryByAssigned() {
    TaskQuery query = taskService.createTaskQuery().taskAssigned();
    assertEquals(2, query.count());
    assertEquals(2, query.list().size());
  }

  @Test
  public void testQueryByCandidateUser() {
    // kermit is candidate for 12 tasks, two of them are already assigned
    TaskQuery query = taskService.createTaskQuery().taskCandidateUser("kermit");
    assertEquals(10, query.count());
    assertEquals(10, query.list().size());
    try {
      query.singleResult();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }

    // test including assigned tasks
    query = taskService.createTaskQuery().taskCandidateUser("kermit").includeAssignedTasks();
    assertEquals(12, query.count());
    assertEquals(12, query.list().size());

    // fozzie is candidate for one task and her groups are candidate for 2 tasks, one of them is already assigned
    query = taskService.createTaskQuery().taskCandidateUser("fozzie");
    assertEquals(2, query.count());
    assertEquals(2, query.list().size());
    try {
      query.singleResult();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }

    // test including assigned tasks
    query = taskService.createTaskQuery().taskCandidateUser("fozzie").includeAssignedTasks();
    assertEquals(3, query.count());
    assertEquals(3, query.list().size());

    // gonzo is candidate for one task, which is already assinged
    query = taskService.createTaskQuery().taskCandidateUser("gonzo");
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

    // test including assigned tasks
    query = taskService.createTaskQuery().taskCandidateUser("gonzo").includeAssignedTasks();
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
  }

  @Test
  public void testQueryByNullCandidateUser() {
    try {
      taskService.createTaskQuery().taskCandidateUser(null).list();
      fail();
    } catch(ProcessEngineException e) {}
  }

  @Test
  public void testQueryByIncludeAssignedTasksWithMissingCandidateUserOrGroup() {
    try {
      taskService.createTaskQuery().includeAssignedTasks();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
  }

  @Test
  public void testQueryByCandidateGroup() {
    // management group is candidate for 3 tasks, one of them is already assigned
    TaskQuery query = taskService.createTaskQuery().taskCandidateGroup("management");
    assertEquals(2, query.count());
    assertEquals(2, query.list().size());
    try {
      query.singleResult();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }

    // test including assigned tasks
    query = taskService.createTaskQuery().taskCandidateGroup("management").includeAssignedTasks();
    assertEquals(3, query.count());
    assertEquals(3, query.list().size());


    // accountancy group is candidate for 3 tasks, one of them is already assigned
    query = taskService.createTaskQuery().taskCandidateGroup("accountancy");
    assertEquals(2, query.count());
    assertEquals(2, query.list().size());

    // test including assigned tasks
    query = taskService.createTaskQuery().taskCandidateGroup("accountancy").includeAssignedTasks();
    assertEquals(3, query.count());
    assertEquals(3, query.list().size());

    // sales group is candidate for no tasks
    query = taskService.createTaskQuery().taskCandidateGroup("sales");
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

    // test including assigned tasks
    query = taskService.createTaskQuery().taskCandidateGroup("sales").includeAssignedTasks();
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }

  @Test
  public void testQueryWithCandidateGroups() {
    // test withCandidateGroups
    TaskQuery query = taskService.createTaskQuery().withCandidateGroups();
    assertEquals(4, query.count());
    assertEquals(4, query.list().size());

    assertEquals(5, query.includeAssignedTasks().count());
    assertEquals(5, query.includeAssignedTasks().list().size());
  }

  @Test
  public void testQueryWithoutCandidateGroups() {
    // test withoutCandidateGroups
    TaskQuery query = taskService.createTaskQuery().withoutCandidateGroups();
    assertEquals(6, query.count());
    assertEquals(6, query.list().size());

    assertEquals(7, query.includeAssignedTasks().count());
    assertEquals(7, query.includeAssignedTasks().list().size());
  }

  @Test
  public void shouldQueryWithoutCandidateGroupsAndUsers() {
    // given
    Task task = taskService.newTask();
    taskService.saveTask(task);
    taskIds.add(task.getId());

    // when
    TaskQuery query = taskService.createTaskQuery()
        .withoutCandidateGroups()
        .withoutCandidateUsers();

    // then
    assertThat(query.list()).extracting("id").containsExactly(task.getId());
  }

  @Test
  public void testQueryByNullCandidateGroup() {
    try {
      taskService.createTaskQuery().taskCandidateGroup(null).list();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
  }

  @Test
  public void testQueryByCandidateGroupIn() {
    List<String> groups = Arrays.asList("management", "accountancy");
    TaskQuery query = taskService.createTaskQuery().taskCandidateGroupIn(groups);
    assertEquals(4, query.count());
    assertEquals(4, query.list().size());
    try {
      query.singleResult();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }

    // test including assigned tasks
    query = taskService.createTaskQuery().taskCandidateGroupIn(groups).includeAssignedTasks();
    assertEquals(5, query.count());
    assertEquals(5, query.list().size());

    // Unexisting groups or groups that don't have candidate tasks shouldn't influence other results
    groups = Arrays.asList("management", "accountancy", "sales", "unexising");
    query = taskService.createTaskQuery().taskCandidateGroupIn(groups);
    assertEquals(4, query.count());
    assertEquals(4, query.list().size());

    // test including assigned tasks
    query = taskService.createTaskQuery().taskCandidateGroupIn(groups).includeAssignedTasks();
    assertEquals(5, query.count());
    assertEquals(5, query.list().size());
  }

  @Test
  public void testQueryByCandidateGroupInAndCandidateGroup() {
    List<String> groups = Arrays.asList("management", "accountancy");
    String candidateGroup = "management";
    TaskQuery query = taskService.createTaskQuery().taskCandidateGroupIn(groups).taskCandidateGroup(candidateGroup);
    assertEquals(2, query.count());
    assertEquals(2, query.list().size());
    try {
      query.singleResult();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }

    // test including assigned tasks
    query = taskService.createTaskQuery().taskCandidateGroupIn(groups).taskCandidateGroup(candidateGroup).includeAssignedTasks();
    assertEquals(3, query.count());
    assertEquals(3, query.list().size());

    // Unexisting groups or groups that don't have candidate tasks shouldn't influence other results
    groups = Arrays.asList("management", "accountancy", "sales", "unexising");
    query = taskService.createTaskQuery().taskCandidateGroupIn(groups).taskCandidateGroup(candidateGroup);
    assertEquals(2, query.count());
    assertEquals(2, query.list().size());
    try {
      query.singleResult();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }

    // test including assigned tasks
    query = taskService.createTaskQuery().taskCandidateGroupIn(groups).taskCandidateGroup(candidateGroup).includeAssignedTasks();
    assertEquals(3, query.count());
    assertEquals(3, query.list().size());

    // sales group is candidate for no tasks
    query = taskService.createTaskQuery().taskCandidateGroupIn(groups).taskCandidateGroup("sales");
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

    // test including assigned tasks
    query = taskService.createTaskQuery().taskCandidateGroupIn(groups).taskCandidateGroup("sales").includeAssignedTasks();
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }

  @Test
  public void testQueryByCandidateGroupInAndCandidateGroupNotIntersected() {
    List<String> groups = Arrays.asList("accountancy");
    String candidateGroup = "management";
    TaskQuery query = taskService.createTaskQuery().taskCandidateGroupIn(groups).taskCandidateGroup(candidateGroup);
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }

  @Test
  public void testQueryByNullCandidateGroupIn() {
    try {
      taskService.createTaskQuery().taskCandidateGroupIn(null).list();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
    try {
      taskService.createTaskQuery().taskCandidateGroupIn(new ArrayList<String>()).list();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
  }

  @Test
  public void testQueryByDelegationState() {
    TaskQuery query = taskService.createTaskQuery().taskDelegationState(null);
    assertEquals(12, query.count());
    assertEquals(12, query.list().size());
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.PENDING);
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.RESOLVED);
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

    String taskId= taskService.createTaskQuery().taskAssignee("gonzo_").singleResult().getId();
    taskService.delegateTask(taskId, "kermit");

    query = taskService.createTaskQuery().taskDelegationState(null);
    assertEquals(11, query.count());
    assertEquals(11, query.list().size());
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.PENDING);
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.RESOLVED);
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

    taskService.resolveTask(taskId);

    query = taskService.createTaskQuery().taskDelegationState(null);
    assertEquals(11, query.count());
    assertEquals(11, query.list().size());
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.PENDING);
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.RESOLVED);
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
  }

  @Test
  public void testQueryCreatedOn() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    // Exact matching of createTime, should result in 6 tasks
    Date createTime = sdf.parse("01/01/2001 01:01:01.000");

    TaskQuery query = taskService.createTaskQuery().taskCreatedOn(createTime);
    assertEquals(6, query.count());
    assertEquals(6, query.list().size());
  }

  @Test
  public void testQueryCreatedBefore() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    // Should result in 7 tasks
    Date before = sdf.parse("03/02/2002 02:02:02.000");

    TaskQuery query = taskService.createTaskQuery().taskCreatedBefore(before);
    assertEquals(7, query.count());
    assertEquals(7, query.list().size());

    before = sdf.parse("01/01/2001 01:01:01.000");
    query = taskService.createTaskQuery().taskCreatedBefore(before);
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }

  @Test
  public void testQueryCreatedAfter() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    // Should result in 3 tasks
    Date after = sdf.parse("03/03/2003 03:03:03.000");

    TaskQuery query = taskService.createTaskQuery().taskCreatedAfter(after);
    assertEquals(3, query.count());
    assertEquals(3, query.list().size());

    after = sdf.parse("05/05/2005 05:05:05.000");
    query = taskService.createTaskQuery().taskCreatedAfter(after);
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }

  @Test
  public void testCreateTimeCombinations() throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    // Exact matching of createTime, should result in 6 tasks
    Date createTime = sdf.parse("01/01/2001 01:01:01.000");

    Date oneHourAgo = new Date(createTime.getTime() - 60 * 60 * 1000);
    Date oneHourLater = new Date(createTime.getTime() + 60 * 60 * 1000);

    assertEquals(6, taskService.createTaskQuery()
        .taskCreatedAfter(oneHourAgo).taskCreatedOn(createTime).taskCreatedBefore(oneHourLater).count());
    assertEquals(0, taskService.createTaskQuery()
        .taskCreatedAfter(oneHourLater).taskCreatedOn(createTime).taskCreatedBefore(oneHourAgo).count());
    assertEquals(0, taskService.createTaskQuery()
        .taskCreatedAfter(oneHourLater).taskCreatedOn(createTime).count());
    assertEquals(0, taskService.createTaskQuery()
        .taskCreatedOn(createTime).taskCreatedBefore(oneHourAgo).count());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/taskDefinitionProcess.bpmn20.xml")
  @Test
  public void testTaskDefinitionKey() throws Exception {

    // Start process instance, 2 tasks will be available
    runtimeService.startProcessInstanceByKey("taskDefinitionKeyProcess");

    // 1 task should exist with key "taskKey_1"
    List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey("taskKey_1").list();
    assertNotNull(tasks);
    assertEquals(1, tasks.size());

    assertEquals("taskKey_1", tasks.get(0).getTaskDefinitionKey());

    // No task should be found with unexisting key
    Long count = taskService.createTaskQuery().taskDefinitionKey("unexistingKey").count();
    assertEquals(0L, count.longValue());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/taskDefinitionProcess.bpmn20.xml")
  @Test
  public void testTaskDefinitionKeyLike() throws Exception {

    // Start process instance, 2 tasks will be available
    runtimeService.startProcessInstanceByKey("taskDefinitionKeyProcess");

    // Ends with matching, TaskKey_1 and TaskKey_123 match
    List<Task> tasks = taskService.createTaskQuery().taskDefinitionKeyLike("taskKey\\_1%").orderByTaskName().asc().list();
    assertNotNull(tasks);
    assertEquals(2, tasks.size());

    assertEquals("taskKey_1", tasks.get(0).getTaskDefinitionKey());
    assertEquals("taskKey_123", tasks.get(1).getTaskDefinitionKey());

    // Starts with matching, TaskKey_123 matches
    tasks = taskService.createTaskQuery().taskDefinitionKeyLike("%\\_123").orderByTaskName().asc().list();
    assertNotNull(tasks);
    assertEquals(1, tasks.size());

    assertEquals("taskKey_123", tasks.get(0).getTaskDefinitionKey());

    // Contains matching, TaskKey_123 matches
    tasks = taskService.createTaskQuery().taskDefinitionKeyLike("%Key\\_12%").orderByTaskName().asc().list();
    assertNotNull(tasks);
    assertEquals(1, tasks.size());

    assertEquals("taskKey_123", tasks.get(0).getTaskDefinitionKey());


    // No task should be found with unexisting key
    Long count = taskService.createTaskQuery().taskDefinitionKeyLike("%unexistingKey%").count();
    assertEquals(0L, count.longValue());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/taskDefinitionProcess.bpmn20.xml")
  @Test
  public void testTaskDefinitionKeyIn() throws Exception {

    // Start process instance, 2 tasks will be available
    runtimeService.startProcessInstanceByKey("taskDefinitionKeyProcess");

    // 1 Task should be found with TaskKey1
    List<Task> tasks = taskService.createTaskQuery().taskDefinitionKeyIn("taskKey_1").list();
    assertNotNull(tasks);
    assertEquals(1, tasks.size());

    assertEquals("taskKey_1", tasks.get(0).getTaskDefinitionKey());

    // 2 Tasks should be found with TaskKey_1 and TaskKey_123
    tasks = taskService.createTaskQuery().taskDefinitionKeyIn("taskKey_1", "taskKey_123").orderByTaskName().asc().list();
    assertNotNull(tasks);
    assertEquals(2, tasks.size());

    assertEquals("taskKey_1", tasks.get(0).getTaskDefinitionKey());
    assertEquals("taskKey_123", tasks.get(1).getTaskDefinitionKey());

    // 2 Tasks should be found with TaskKey1, TaskKey123 and UnexistingKey
    tasks = taskService.createTaskQuery().taskDefinitionKeyIn("taskKey_1", "taskKey_123", "unexistingKey").orderByTaskName().asc().list();
    assertNotNull(tasks);
    assertEquals(2, tasks.size());

    assertEquals("taskKey_1", tasks.get(0).getTaskDefinitionKey());
    assertEquals("taskKey_123", tasks.get(1).getTaskDefinitionKey());

    // No task should be found with UnexistingKey
    Long count = taskService.createTaskQuery().taskDefinitionKeyIn("unexistingKey").count();
    assertEquals(0L, count.longValue());

    count = taskService.createTaskQuery().taskDefinitionKey("unexistingKey").taskDefinitionKeyIn("taskKey1").count();
    assertEquals(0l, count.longValue());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testTaskVariableNameEqualsIgnoreCase() throws Exception {
    String variableName = "someVariable";
    String variableValue = "someCamelCaseValue";

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setVariableLocal(task.getId(), variableName, variableValue);

    // query for case-insensitive variable name should only return a result if case-insensitive search is used
    assertEquals(1, taskService.createTaskQuery().matchVariableNamesIgnoreCase().taskVariableValueEquals(variableName.toLowerCase(), variableValue).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals(variableName.toLowerCase(), variableValue).count());

    // query should treat all variables case-insensitively, even when flag is set after variable
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals(variableName.toLowerCase(), variableValue).matchVariableNamesIgnoreCase().count());
  }

  @Deployment
  @Test
  public void testTaskVariableValueEquals() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // No task should be found for an unexisting var
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("unexistingVar", "value").count());

    // Create a map with a variable for all default types
    Map<String, Object> variables = new HashMap<>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "stringValue");
    variables.put("booleanVar", true);
    Date date = Calendar.getInstance().getTime();
    variables.put("dateVar", date);
    variables.put("nullVar", null);

    taskService.setVariablesLocal(task.getId(), variables);

    // Test query matches
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals("longVar", 928374L).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals("shortVar",  (short) 123).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals("integerVar", 1234).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals("stringVar", "stringValue").count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals("booleanVar", true).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals("dateVar", date).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals("nullVar", null).count());

    // Test query for other values on existing variables
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("longVar", 999L).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("shortVar",  (short) 999).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("integerVar", 999).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("stringVar", "999").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("booleanVar", false).count());
    Calendar otherDate = Calendar.getInstance();
    otherDate.add(Calendar.YEAR, 1);
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("dateVar", otherDate.getTime()).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("nullVar", "999").count());

    // Test query for not equals
    assertEquals(1, taskService.createTaskQuery().taskVariableValueNotEquals("longVar", 999L).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueNotEquals("shortVar",  (short) 999).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueNotEquals("integerVar", 999).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueNotEquals("stringVar", "999").count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueNotEquals("booleanVar", false).count());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/TaskQueryTest.testTaskVariableValueEquals.bpmn20.xml")
  @Test
  public void testTaskVariableValueEqualsIgnoreCase() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    String variableName = "someVariable";
    String variableValue = "someCamelCaseValue";

    taskService.setVariableLocal(task.getId(), variableName, variableValue);

    // query for existing variable should return one result
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals(variableName, variableValue).count());
    assertEquals(1, taskService.createTaskQuery().matchVariableValuesIgnoreCase().taskVariableValueEquals(variableName, variableValue.toLowerCase()).count());

    // query for non existing variable should return zero results
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("nonExistentVariable", variableValue.toLowerCase()).count());
    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().taskVariableValueEquals("nonExistentVariable", variableValue.toLowerCase()).count());

    // query for existing variable with different value should return zero results
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals(variableName, "nonExistentValue").count());
    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().taskVariableValueEquals(variableName, "nonExistentValue".toLowerCase()).count());

    // query for case-insensitive variable value should only return a result when case-insensitive search is used
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals(variableName, variableValue.toLowerCase()).count());
    assertEquals(1, taskService.createTaskQuery().matchVariableValuesIgnoreCase().taskVariableValueEquals(variableName, variableValue.toLowerCase()).count());

    // query for case-insensitive variable with not equals operator should only return a result when case-sensitive search is used
    assertEquals(1, taskService.createTaskQuery().taskVariableValueNotEquals(variableName, variableValue.toLowerCase()).count());
    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().taskVariableValueNotEquals(variableName, variableValue.toLowerCase()).count());

    // query should treat all variables case-insensitively, even when flag is set after variable
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals(variableName, variableValue.toLowerCase()).matchVariableValuesIgnoreCase().count());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testTaskVariableValueNameEqualsIgnoreCase() throws Exception {
    String variableName = "someVariable";
    String variableValue = "someCamelCaseValue";

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setVariableLocal(task.getId(), variableName, variableValue);

    // query for case-insensitive variable name should only return a result if case-insensitive search is used
    assertEquals(1, taskService.createTaskQuery().matchVariableNamesIgnoreCase().matchVariableValuesIgnoreCase().taskVariableValueEquals(variableName.toLowerCase(), variableValue.toLowerCase()).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals(variableName.toLowerCase(), variableValue).count());

    // query should treat all variables case-insensitively, even when flag is set after variable
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals(variableName.toLowerCase(), variableValue).matchVariableNamesIgnoreCase().count());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/TaskQueryTest.testTaskVariableValueEquals.bpmn20.xml")
  @Test
  public void testTaskVariableValueLike() throws Exception {

  	ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
  	Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

  	Map<String, Object> variables = new HashMap<>();
  	variables.put("stringVar", "stringValue");

  	taskService.setVariablesLocal(task.getId(), variables);

    assertEquals(1, taskService.createTaskQuery().taskVariableValueLike("stringVar", "stringVal%").count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueLike("stringVar", "%ngValue").count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueLike("stringVar", "%ngVal%").count());

    assertEquals(0, taskService.createTaskQuery().taskVariableValueLike("stringVar", "stringVar%").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueLike("stringVar", "%ngVar").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueLike("stringVar", "%ngVar%").count());

    assertEquals(0, taskService.createTaskQuery().taskVariableValueLike("stringVar", "stringVal").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueLike("nonExistingVar", "string%").count());

    // test with null value
    try {
      taskService.createTaskQuery().taskVariableValueLike("stringVar", null).count();
      fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/TaskQueryTest.testTaskVariableValueEquals.bpmn20.xml")
  @Test
  public void testTaskVariableValueLikeIgnoreCase() throws Exception {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "stringValue");

    taskService.setVariablesLocal(task.getId(), variables);

    assertEquals(0, taskService.createTaskQuery().taskVariableValueLike("stringVar", "stringVal%".toLowerCase()).count());
    assertEquals(1, taskService.createTaskQuery().matchVariableValuesIgnoreCase().taskVariableValueLike("stringVar", "stringVal%".toLowerCase()).count());
    assertEquals(1, taskService.createTaskQuery().matchVariableValuesIgnoreCase().taskVariableValueLike("stringVar", "%ngValue".toLowerCase()).count());
    assertEquals(1, taskService.createTaskQuery().matchVariableValuesIgnoreCase().taskVariableValueLike("stringVar", "%ngVal%".toLowerCase()).count());

    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().taskVariableValueLike("stringVar", "stringVar%".toLowerCase()).count());
    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().taskVariableValueLike("stringVar", "%ngVar".toLowerCase()).count());
    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().taskVariableValueLike("stringVar", "%ngVar%".toLowerCase()).count());

    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().taskVariableValueLike("stringVar", "stringVal".toLowerCase()).count());
    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().taskVariableValueLike("nonExistingVar", "stringVal%".toLowerCase()).count());

    // test with null value
    try {
      taskService.createTaskQuery().taskVariableValueLike("stringVar", null).count();
      fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/TaskQueryTest.testTaskVariableValueEquals.bpmn20.xml")
  @Test
  public void testTaskVariableValueCompare() throws Exception {

  	ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
  	Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

  	Map<String, Object> variables = new HashMap<>();
  	variables.put("numericVar", 928374);
  	Date date = new GregorianCalendar(2014, 2, 2, 2, 2, 2).getTime();
  	variables.put("dateVar", date);
  	variables.put("stringVar", "ab");
  	variables.put("nullVar", null);

  	taskService.setVariablesLocal(task.getId(), variables);

    // test compare methods with numeric values
    assertEquals(1, taskService.createTaskQuery().taskVariableValueGreaterThan("numericVar", 928373).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueGreaterThan("numericVar", 928374).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueGreaterThan("numericVar", 928375).count());

    assertEquals(1, taskService.createTaskQuery().taskVariableValueGreaterThanOrEquals("numericVar", 928373).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueGreaterThanOrEquals("numericVar", 928374).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueGreaterThanOrEquals("numericVar", 928375).count());

    assertEquals(1, taskService.createTaskQuery().taskVariableValueLessThan("numericVar", 928375).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueLessThan("numericVar", 928374).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueLessThan("numericVar", 928373).count());

    assertEquals(1, taskService.createTaskQuery().taskVariableValueLessThanOrEquals("numericVar", 928375).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueLessThanOrEquals("numericVar", 928374).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueLessThanOrEquals("numericVar", 928373).count());

    // test compare methods with date values
    Date before = new GregorianCalendar(2014, 2, 2, 2, 2, 1).getTime();
    Date after = new GregorianCalendar(2014, 2, 2, 2, 2, 3).getTime();

    assertEquals(1, taskService.createTaskQuery().taskVariableValueGreaterThan("dateVar", before).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueGreaterThan("dateVar", date).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueGreaterThan("dateVar", after).count());

    assertEquals(1, taskService.createTaskQuery().taskVariableValueGreaterThanOrEquals("dateVar", before).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueGreaterThanOrEquals("dateVar", date).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueGreaterThanOrEquals("dateVar", after).count());

    assertEquals(1, taskService.createTaskQuery().taskVariableValueLessThan("dateVar", after).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueLessThan("dateVar", date).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueLessThan("dateVar", before).count());

    assertEquals(1, taskService.createTaskQuery().taskVariableValueLessThanOrEquals("dateVar", after).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueLessThanOrEquals("dateVar", date).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueLessThanOrEquals("dateVar", before).count());

    //test with string values
    assertEquals(1, taskService.createTaskQuery().taskVariableValueGreaterThan("stringVar", "aa").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueGreaterThan("stringVar", "ab").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueGreaterThan("stringVar", "ba").count());

    assertEquals(1, taskService.createTaskQuery().taskVariableValueGreaterThanOrEquals("stringVar", "aa").count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueGreaterThanOrEquals("stringVar", "ab").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueGreaterThanOrEquals("stringVar", "ba").count());

    assertEquals(1, taskService.createTaskQuery().taskVariableValueLessThan("stringVar", "ba").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueLessThan("stringVar", "ab").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueLessThan("stringVar", "aa").count());

    assertEquals(1, taskService.createTaskQuery().taskVariableValueLessThanOrEquals("stringVar", "ba").count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueLessThanOrEquals("stringVar", "ab").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueLessThanOrEquals("stringVar", "aa").count());

    // test with null value
    try {
      taskService.createTaskQuery().taskVariableValueGreaterThan("nullVar", null).count();
      fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
  	  taskService.createTaskQuery().taskVariableValueGreaterThanOrEquals("nullVar", null).count();
  	  fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
  	  taskService.createTaskQuery().taskVariableValueLessThan("nullVar", null).count();
  	  fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
  	  taskService.createTaskQuery().taskVariableValueLessThanOrEquals("nullVar", null).count();
  	  fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}

    // test with boolean value
    try {
      taskService.createTaskQuery().taskVariableValueGreaterThan("nullVar", true).count();
      fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
  	  taskService.createTaskQuery().taskVariableValueGreaterThanOrEquals("nullVar", false).count();
  	  fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
  	  taskService.createTaskQuery().taskVariableValueLessThan("nullVar", true).count();
  	  fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
  	  taskService.createTaskQuery().taskVariableValueLessThanOrEquals("nullVar", false).count();
  	  fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}

 // test non existing variable
    assertEquals(0, taskService.createTaskQuery().taskVariableValueLessThanOrEquals("nonExisting", 123).count());
  }

  @Deployment
  @Test
  public void testProcessVariableValueEquals() throws Exception {
    Map<String, Object> variables = new HashMap<>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "stringValue");
    variables.put("booleanVar", true);
    Date date = Calendar.getInstance().getTime();
    variables.put("dateVar", date);
    variables.put("nullVar", null);

    // Start process-instance with all types of variables
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    // Test query matches
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("longVar", 928374L).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("shortVar",  (short) 123).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("integerVar", 1234).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("stringVar", "stringValue").count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("booleanVar", true).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("dateVar", date).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("nullVar", null).count());

    // Test query for other values on existing variables
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("longVar", 999L).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("shortVar",  (short) 999).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("integerVar", 999).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("stringVar", "999").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("booleanVar", false).count());
    Calendar otherDate = Calendar.getInstance();
    otherDate.add(Calendar.YEAR, 1);
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("dateVar", otherDate.getTime()).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("nullVar", "999").count());

    // Test querying for task variables don't match the process-variables
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("longVar", 928374L).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("shortVar",  (short) 123).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("integerVar", 1234).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("stringVar", "stringValue").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("booleanVar", true).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("dateVar", date).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("nullVar", null).count());

    // Test querying for task variables not equals
    assertEquals(1, taskService.createTaskQuery().processVariableValueNotEquals("longVar", 999L).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueNotEquals("shortVar",  (short) 999).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueNotEquals("integerVar", 999).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueNotEquals("stringVar", "999").count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueNotEquals("booleanVar", false).count());

    // and query for the existing variable with NOT should result in nothing found:
    assertEquals(0, taskService.createTaskQuery().processVariableValueNotEquals("longVar", 928374L).count());

    // Test combination of task-variable and process-variable
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setVariableLocal(task.getId(), "taskVar", "theValue");
    taskService.setVariableLocal(task.getId(), "longVar", 928374L);

    assertEquals(1, taskService.createTaskQuery()
        .processVariableValueEquals("longVar", 928374L)
        .taskVariableValueEquals("taskVar", "theValue")
        .count());

    assertEquals(1, taskService.createTaskQuery()
        .processVariableValueEquals("longVar", 928374L)
        .taskVariableValueEquals("longVar", 928374L)
        .count());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableNameEqualsIgnoreCase() throws Exception {
    String variableName = "someVariable";
    String variableValue = "someCamelCaseValue";
    Map<String, Object> variables = new HashMap<>();
    variables.put(variableName, variableValue);

    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    // query for case-insensitive variable name should only return a result if case-insensitive search is used
    assertEquals(1, taskService.createTaskQuery().matchVariableNamesIgnoreCase().processVariableValueEquals(variableName.toLowerCase(), variableValue).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals(variableName.toLowerCase(), variableValue).count());

    // query should treat all variables case-insensitively, even when flag is set after variable
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals(variableName.toLowerCase(), variableValue).matchVariableNamesIgnoreCase().count());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/TaskQueryTest.testTaskVariableValueEquals.bpmn20.xml")
  @Test
  public void testProcessVariableValueEqualsIgnoreCase() throws Exception {
    String variableName = "someVariable";
    String variableValue = "someCamelCaseValue";
    Map<String, Object> variables = new HashMap<>();
    variables.put(variableName, variableValue);

    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    // query for existing variable should return one result
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals(variableName, variableValue).count());
    assertEquals(1, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueEquals(variableName, variableValue.toLowerCase()).count());

    // query for non existing variable should return zero results
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("nonExistentVariable", variableValue.toLowerCase()).count());
    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueEquals("nonExistentVariable", variableValue.toLowerCase()).count());

    // query for existing variable with different value should return zero results
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals(variableName, "nonExistentValue").count());
    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueEquals(variableName, "nonExistentValue".toLowerCase()).count());

    // query for case-insensitive variable value should only return a result when case-insensitive search is used
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals(variableName, variableValue.toLowerCase()).count());
    assertEquals(1, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueEquals(variableName, variableValue.toLowerCase()).count());

    // query for case-insensitive variable with not equals operator should only return a result when case-sensitive search is used
    assertEquals(1, taskService.createTaskQuery().processVariableValueNotEquals(variableName, variableValue.toLowerCase()).count());
    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotEquals(variableName, variableValue.toLowerCase()).count());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessVariableValueEquals.bpmn20.xml")
  @Test
  public void testProcessVariableValueLike() throws Exception {

    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "stringValue");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    assertEquals(1, taskService.createTaskQuery().processVariableValueLike("stringVar", "stringVal%").count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueLike("stringVar", "%ngValue").count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueLike("stringVar", "%ngVal%").count());

    assertEquals(0, taskService.createTaskQuery().processVariableValueLike("stringVar", "stringVar%").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueLike("stringVar", "%ngVar").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueLike("stringVar", "%ngVar%").count());

    assertEquals(0, taskService.createTaskQuery().processVariableValueLike("stringVar", "stringVal").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueLike("nonExistingVar", "string%").count());

    // test with null value
    try {
      taskService.createTaskQuery().processVariableValueLike("stringVar", null).count();
      fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessVariableValueEquals.bpmn20.xml")
  @Test
  public void testProcessVariableValueLikeIgnoreCase() throws Exception {

    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "stringValue");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    assertEquals(0, taskService.createTaskQuery().processVariableValueLike("stringVar", "stringVal%".toLowerCase()).count());
    assertEquals(1, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("stringVar", "stringVal%".toLowerCase()).count());
    assertEquals(1, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("stringVar", "%ngValue".toLowerCase()).count());
    assertEquals(1, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("stringVar", "%ngVal%".toLowerCase()).count());

    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("stringVar", "stringVar%".toLowerCase()).count());
    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("stringVar", "%ngVar".toLowerCase()).count());
    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("stringVar", "%ngVar%".toLowerCase()).count());

    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("stringVar", "stringVal".toLowerCase()).count());
    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("nonExistingVar", "stringVal%".toLowerCase()).count());

    // test with null value
    try {
      taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("stringVar", null).count();
      fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessVariableValueEquals.bpmn20.xml")
  @Test
  public void testProcessVariableValueNotLike() throws Exception {

    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "stringValue");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    assertEquals(0, taskService.createTaskQuery().processVariableValueNotLike("stringVar", "stringVal%").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueNotLike("stringVar", "%ngValue").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueNotLike("stringVar", "%ngVal%").count());

    assertEquals(1, taskService.createTaskQuery().processVariableValueNotLike("stringVar", "stringVar%").count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueNotLike("stringVar", "%ngVar").count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueNotLike("stringVar", "%ngVar%").count());

    assertEquals(1, taskService.createTaskQuery().processVariableValueNotLike("stringVar", "stringVal").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueNotLike("nonExistingVar", "string%").count());

    // test with null value
    assertThatThrownBy(() -> taskService.createTaskQuery().processVariableValueNotLike("stringVar", null).count())
      .isInstanceOf(ProcessEngineException.class);
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessVariableValueEquals.bpmn20.xml")
  @Test
  public void testProcessVariableValueNotLikeIgnoreCase() throws Exception {

    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "stringValue");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    assertEquals(1, taskService.createTaskQuery().processVariableValueNotLike("stringVar", "stringVal%".toLowerCase()).count());
    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("stringVar", "stringVal%".toLowerCase()).count());
    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("stringVar", "%ngValue".toLowerCase()).count());
    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("stringVar", "%ngVal%".toLowerCase()).count());

    assertEquals(1, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("stringVar", "stringVar%".toLowerCase()).count());
    assertEquals(1, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("stringVar", "%ngVar".toLowerCase()).count());
    assertEquals(1, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("stringVar", "%ngVar%".toLowerCase()).count());

    assertEquals(1, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("stringVar", "stringVal".toLowerCase()).count());
    assertEquals(0, taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("nonExistingVar", "stringVal%".toLowerCase()).count());

    // test with null value
    assertThatThrownBy(() -> taskService.createTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("stringVar", null).count())
      .isInstanceOf(ProcessEngineException.class);
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessVariableValueEquals.bpmn20.xml")
  @Test
  public void testProcessVariableValueCompare() throws Exception {

  	Map<String, Object> variables = new HashMap<>();
  	variables.put("numericVar", 928374);
  	Date date = new GregorianCalendar(2014, 2, 2, 2, 2, 2).getTime();
  	variables.put("dateVar", date);
  	variables.put("stringVar", "ab");
  	variables.put("nullVar", null);

    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    // test compare methods with numeric values
    assertEquals(1, taskService.createTaskQuery().processVariableValueGreaterThan("numericVar", 928373).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueGreaterThan("numericVar", 928374).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueGreaterThan("numericVar", 928375).count());

    assertEquals(1, taskService.createTaskQuery().processVariableValueGreaterThanOrEquals("numericVar", 928373).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueGreaterThanOrEquals("numericVar", 928374).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueGreaterThanOrEquals("numericVar", 928375).count());

    assertEquals(1, taskService.createTaskQuery().processVariableValueLessThan("numericVar", 928375).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueLessThan("numericVar", 928374).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueLessThan("numericVar", 928373).count());

    assertEquals(1, taskService.createTaskQuery().processVariableValueLessThanOrEquals("numericVar", 928375).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueLessThanOrEquals("numericVar", 928374).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueLessThanOrEquals("numericVar", 928373).count());

    // test compare methods with date values
    Date before = new GregorianCalendar(2014, 2, 2, 2, 2, 1).getTime();
    Date after = new GregorianCalendar(2014, 2, 2, 2, 2, 3).getTime();

    assertEquals(1, taskService.createTaskQuery().processVariableValueGreaterThan("dateVar", before).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueGreaterThan("dateVar", date).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueGreaterThan("dateVar", after).count());

    assertEquals(1, taskService.createTaskQuery().processVariableValueGreaterThanOrEquals("dateVar", before).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueGreaterThanOrEquals("dateVar", date).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueGreaterThanOrEquals("dateVar", after).count());

    assertEquals(1, taskService.createTaskQuery().processVariableValueLessThan("dateVar", after).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueLessThan("dateVar", date).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueLessThan("dateVar", before).count());

    assertEquals(1, taskService.createTaskQuery().processVariableValueLessThanOrEquals("dateVar", after).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueLessThanOrEquals("dateVar", date).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueLessThanOrEquals("dateVar", before).count());

    //test with string values
    assertEquals(1, taskService.createTaskQuery().processVariableValueGreaterThan("stringVar", "aa").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueGreaterThan("stringVar", "ab").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueGreaterThan("stringVar", "ba").count());

    assertEquals(1, taskService.createTaskQuery().processVariableValueGreaterThanOrEquals("stringVar", "aa").count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueGreaterThanOrEquals("stringVar", "ab").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueGreaterThanOrEquals("stringVar", "ba").count());

    assertEquals(1, taskService.createTaskQuery().processVariableValueLessThan("stringVar", "ba").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueLessThan("stringVar", "ab").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueLessThan("stringVar", "aa").count());

    assertEquals(1, taskService.createTaskQuery().processVariableValueLessThanOrEquals("stringVar", "ba").count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueLessThanOrEquals("stringVar", "ab").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueLessThanOrEquals("stringVar", "aa").count());

    // test with null value
    try {
      taskService.createTaskQuery().processVariableValueGreaterThan("nullVar", null).count();
      fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
  	  taskService.createTaskQuery().processVariableValueGreaterThanOrEquals("nullVar", null).count();
  	  fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
  	  taskService.createTaskQuery().processVariableValueLessThan("nullVar", null).count();
  	  fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
  	  taskService.createTaskQuery().processVariableValueLessThanOrEquals("nullVar", null).count();
  	  fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}

    // test with boolean value
    try {
      taskService.createTaskQuery().processVariableValueGreaterThan("nullVar", true).count();
      fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
  	  taskService.createTaskQuery().processVariableValueGreaterThanOrEquals("nullVar", false).count();
  	  fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
  	  taskService.createTaskQuery().processVariableValueLessThan("nullVar", true).count();
  	  fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
  	  taskService.createTaskQuery().processVariableValueLessThanOrEquals("nullVar", false).count();
  	  fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}

    // test non existing variable
    assertEquals(0, taskService.createTaskQuery().processVariableValueLessThanOrEquals("nonExisting", 123).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueEqualsNumber() throws Exception {
    // long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123L));

    // non-matching long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 12345L));

    // short
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", (short) 123));

    // double
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123.0d));

    // integer
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123));

    // untyped null (should not match)
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", null));

    // typed null (should not match)
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", Variables.longValue(null)));

    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "123"));

    assertEquals(4, taskService.createTaskQuery().processVariableValueEquals("var", Variables.numberValue(123)).count());
    assertEquals(4, taskService.createTaskQuery().processVariableValueEquals("var", Variables.numberValue(123L)).count());
    assertEquals(4, taskService.createTaskQuery().processVariableValueEquals("var", Variables.numberValue(123.0d)).count());
    assertEquals(4, taskService.createTaskQuery().processVariableValueEquals("var", Variables.numberValue((short) 123)).count());

    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("var", Variables.numberValue(null)).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueNumberComparison() throws Exception {
    // long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123L));

    // non-matching long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 12345L));

    // short
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", (short) 123));

    // double
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123.0d));

    // integer
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123));

    // untyped null
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", null));

    // typed null
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", Variables.longValue(null)));

    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "123"));

    assertEquals(4, taskService.createTaskQuery().processVariableValueNotEquals("var", Variables.numberValue(123)).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueGreaterThan("var", Variables.numberValue(123)).count());
    assertEquals(5, taskService.createTaskQuery().processVariableValueGreaterThanOrEquals("var", Variables.numberValue(123)).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueLessThan("var", Variables.numberValue(123)).count());
    assertEquals(4, taskService.createTaskQuery().processVariableValueLessThanOrEquals("var", Variables.numberValue(123)).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testTaskVariableValueEqualsNumber() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("oneTaskProcess").list();
    assertEquals(8, tasks.size());
    taskService.setVariableLocal(tasks.get(0).getId(), "var", 123L);
    taskService.setVariableLocal(tasks.get(1).getId(), "var", 12345L);
    taskService.setVariableLocal(tasks.get(2).getId(), "var", (short) 123);
    taskService.setVariableLocal(tasks.get(3).getId(), "var", 123.0d);
    taskService.setVariableLocal(tasks.get(4).getId(), "var", 123);
    taskService.setVariableLocal(tasks.get(5).getId(), "var", null);
    taskService.setVariableLocal(tasks.get(6).getId(), "var", Variables.longValue(null));
    taskService.setVariableLocal(tasks.get(7).getId(), "var", "123");

    assertEquals(4, taskService.createTaskQuery().taskVariableValueEquals("var", Variables.numberValue(123)).count());
    assertEquals(4, taskService.createTaskQuery().taskVariableValueEquals("var", Variables.numberValue(123L)).count());
    assertEquals(4, taskService.createTaskQuery().taskVariableValueEquals("var", Variables.numberValue(123.0d)).count());
    assertEquals(4, taskService.createTaskQuery().taskVariableValueEquals("var", Variables.numberValue((short) 123)).count());

    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals("var", Variables.numberValue(null)).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testVariableEqualsNumberMax() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", MAX_DOUBLE_VALUE));
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", Long.MAX_VALUE));

    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("var", Variables.numberValue(MAX_DOUBLE_VALUE)).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("var", Variables.numberValue(Long.MAX_VALUE)).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testVariableEqualsNumberLongValueOverflow() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", MAX_DOUBLE_VALUE));

    // this results in an overflow
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", (long) MAX_DOUBLE_VALUE));

    // the query should not find the long variable
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("var", Variables.numberValue(MAX_DOUBLE_VALUE)).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testVariableEqualsNumberNonIntegerDoubleShouldNotMatchInteger() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", 42).putValue("var2", 52.4d));

    // querying by 42.4 should not match the integer variable 42
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("var", Variables.numberValue(42.4d)).count());

    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 42.4d));

    // querying by 52 should not find the double variable 52.4
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("var", Variables.numberValue(52)).count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  @Test
  public void testProcessDefinitionId() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Task> tasks = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).list();
    assertEquals(1, tasks.size());
    assertEquals(processInstance.getId(), tasks.get(0).getProcessInstanceId());

    assertEquals(0, taskService.createTaskQuery().processDefinitionId("unexisting").count());
  }


  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  @Test
  public void testProcessDefinitionKey() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("oneTaskProcess").list();
    assertEquals(1, tasks.size());
    assertEquals(processInstance.getId(), tasks.get(0).getProcessInstanceId());

    assertEquals(0, taskService.createTaskQuery().processDefinitionKey("unexisting").count());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/task/taskDefinitionProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
  })
  @Test
  public void testProcessDefinitionKeyIn() throws Exception {

    // Start for each deployed process definition a process instance
    runtimeService.startProcessInstanceByKey("taskDefinitionKeyProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // 1 task should be found with oneTaskProcess
    List<Task> tasks = taskService.createTaskQuery().processDefinitionKeyIn("oneTaskProcess").list();
    assertNotNull(tasks);
    assertEquals(1, tasks.size());

    assertEquals("theTask", tasks.get(0).getTaskDefinitionKey());

    // 2 Tasks should be found with both process definition keys
    tasks = taskService.createTaskQuery()
      .processDefinitionKeyIn("oneTaskProcess", "taskDefinitionKeyProcess")
      .list();
    assertNotNull(tasks);
    assertEquals(3, tasks.size());

    Set<String> keysFound = new HashSet<>();
    for (Task task : tasks) {
      keysFound.add(task.getTaskDefinitionKey());
    }
    assertTrue(keysFound.contains("taskKey_123"));
    assertTrue(keysFound.contains("theTask"));
    assertTrue(keysFound.contains("taskKey_1"));

    // 1 Tasks should be found with oneTaskProcess,and NonExistingKey
    tasks = taskService.createTaskQuery().processDefinitionKeyIn("oneTaskProcess", "NonExistingKey").orderByTaskName().asc().list();
    assertNotNull(tasks);
    assertEquals(1, tasks.size());

    assertEquals("theTask", tasks.get(0).getTaskDefinitionKey());

    // No task should be found with NonExistingKey
    Long count = taskService.createTaskQuery().processDefinitionKeyIn("NonExistingKey").count();
    assertEquals(0L, count.longValue());

    count = taskService.createTaskQuery()
        .processDefinitionKeyIn("oneTaskProcess").processDefinitionKey("NonExistingKey").count();
    assertEquals(0L, count.longValue());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  @Test
  public void testProcessDefinitionName() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Task> tasks = taskService.createTaskQuery().processDefinitionName("The%One%Task%Process").list();
    assertEquals(1, tasks.size());
    assertEquals(processInstance.getId(), tasks.get(0).getProcessInstanceId());

    assertEquals(0, taskService.createTaskQuery().processDefinitionName("unexisting").count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  @Test
  public void testProcessDefinitionNameLike() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Task> tasks = taskService.createTaskQuery().processDefinitionNameLike("The\\%One\\%Task%").list();
    assertEquals(1, tasks.size());
    assertEquals(processInstance.getId(), tasks.get(0).getProcessInstanceId());

    assertEquals(0, taskService.createTaskQuery().processDefinitionNameLike("The One Task").count());
    assertEquals(0, taskService.createTaskQuery().processDefinitionNameLike("The Other Task%").count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  @Test
  public void testProcessInstanceBusinessKey() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", "BUSINESS-KEY-1");

    assertEquals(1, taskService.createTaskQuery().processDefinitionName("The%One%Task%Process").processInstanceBusinessKey("BUSINESS-KEY-1").list().size());
    assertEquals(1, taskService.createTaskQuery().processInstanceBusinessKey("BUSINESS-KEY-1").list().size());
    assertEquals(0, taskService.createTaskQuery().processInstanceBusinessKey("NON-EXISTING").count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  @Test
  public void testProcessInstanceBusinessKeyIn() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", "BUSINESS-KEY-1");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", "BUSINESS-KEY-2");

    // 1 task should be found with BUSINESS-KEY-1
    List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKeyIn("BUSINESS-KEY-1").list();
    assertNotNull(tasks);
    assertEquals(1, tasks.size());

    assertEquals("theTask", tasks.get(0).getTaskDefinitionKey());

    // 2 tasks should be found with BUSINESS-KEY-1 and BUSINESS-KEY-2
    tasks = taskService.createTaskQuery()
      .processInstanceBusinessKeyIn("BUSINESS-KEY-1", "BUSINESS-KEY-2")
      .list();
    assertNotNull(tasks);
    assertEquals(2, tasks.size());

    for (Task task : tasks) {
      assertEquals("theTask", task.getTaskDefinitionKey());
    }

    // 1 tasks should be found with BUSINESS-KEY-1 and NON-EXISTING-KEY
    Task task = taskService.createTaskQuery()
      .processInstanceBusinessKeyIn("BUSINESS-KEY-1", "NON-EXISTING-KEY")
      .singleResult();

    assertNotNull(tasks);
    assertEquals("theTask", task.getTaskDefinitionKey());

    long count = taskService.createTaskQuery().processInstanceBusinessKeyIn("BUSINESS-KEY-1").processInstanceBusinessKey("NON-EXISTING-KEY")
        .count();
    assertEquals(0l, count);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  @Test
  public void testProcessInstanceBusinessKeyLike() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", "BUSINESS-KEY-1");

    assertEquals(1, taskService.createTaskQuery().processDefinitionName("The%One%Task%Process").processInstanceBusinessKey("BUSINESS-KEY-1").list().size());
    assertEquals(1, taskService.createTaskQuery().processInstanceBusinessKeyLike("BUSINESS-KEY%").list().size());
    assertEquals(0, taskService.createTaskQuery().processInstanceBusinessKeyLike("BUSINESS-KEY").count());
    assertEquals(0, taskService.createTaskQuery().processInstanceBusinessKeyLike("BUZINESS-KEY%").count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  @Test
  public void testTaskDueDate() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // Set due-date on task
    Date dueDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 01:12:13");
    task.setDueDate(dueDate);
    taskService.saveTask(task);

    assertEquals(1, taskService.createTaskQuery().dueDate(dueDate).count());

    Calendar otherDate = Calendar.getInstance();
    otherDate.add(Calendar.YEAR, 1);
    assertEquals(0, taskService.createTaskQuery().dueDate(otherDate.getTime()).count());

    Calendar priorDate = Calendar.getInstance();
    priorDate.setTime(dueDate);
    priorDate.roll(Calendar.YEAR, -1);
    assertEquals(1, taskService.createTaskQuery().dueAfter(priorDate.getTime())
        .count());

    assertEquals(1, taskService.createTaskQuery()
        .dueBefore(otherDate.getTime()).count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  @Test
  public void testTaskDueBefore() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // Set due-date on task
    Calendar dueDateCal = Calendar.getInstance();
    task.setDueDate(dueDateCal.getTime());
    taskService.saveTask(task);

    Calendar oneHourAgo = Calendar.getInstance();
    oneHourAgo.setTime(dueDateCal.getTime());
    oneHourAgo.add(Calendar.HOUR, -1);

    Calendar oneHourLater = Calendar.getInstance();
    oneHourLater.setTime(dueDateCal.getTime());
    oneHourLater.add(Calendar.HOUR, 1);

    assertEquals(1, taskService.createTaskQuery().dueBefore(oneHourLater.getTime()).count());
    assertEquals(0, taskService.createTaskQuery().dueBefore(oneHourAgo.getTime()).count());

    // Update due-date to null, shouldn't show up anymore in query that matched before
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    task.setDueDate(null);
    taskService.saveTask(task);

    assertEquals(0, taskService.createTaskQuery().dueBefore(oneHourLater.getTime()).count());
    assertEquals(0, taskService.createTaskQuery().dueBefore(oneHourAgo.getTime()).count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  @Test
  public void testTaskDueAfter() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // Set due-date on task
    Calendar dueDateCal = Calendar.getInstance();
    task.setDueDate(dueDateCal.getTime());
    taskService.saveTask(task);

    Calendar oneHourAgo = Calendar.getInstance();
    oneHourAgo.setTime(dueDateCal.getTime());
    oneHourAgo.add(Calendar.HOUR, -1);

    Calendar oneHourLater = Calendar.getInstance();
    oneHourLater.setTime(dueDateCal.getTime());
    oneHourLater.add(Calendar.HOUR, 1);

    assertEquals(1, taskService.createTaskQuery().dueAfter(oneHourAgo.getTime()).count());
    assertEquals(0, taskService.createTaskQuery().dueAfter(oneHourLater.getTime()).count());

    // Update due-date to null, shouldn't show up anymore in query that matched before
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    task.setDueDate(null);
    taskService.saveTask(task);

    assertEquals(0, taskService.createTaskQuery().dueAfter(oneHourLater.getTime()).count());
    assertEquals(0, taskService.createTaskQuery().dueAfter(oneHourAgo.getTime()).count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  @Test
  public void testTaskDueDateCombinations() throws ParseException {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // Set due-date on task
    Date dueDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 01:12:13");
    task.setDueDate(dueDate);
    taskService.saveTask(task);

    Date oneHourAgo = new Date(dueDate.getTime() - 60 * 60 * 1000);
    Date oneHourLater = new Date(dueDate.getTime() + 60 * 60 * 1000);

    assertEquals(1, taskService.createTaskQuery()
        .dueAfter(oneHourAgo).dueDate(dueDate).dueBefore(oneHourLater).count());
    assertEquals(0, taskService.createTaskQuery()
        .dueAfter(oneHourLater).dueDate(dueDate).dueBefore(oneHourAgo).count());
    assertEquals(0, taskService.createTaskQuery()
        .dueAfter(oneHourLater).dueDate(dueDate).count());
    assertEquals(0, taskService.createTaskQuery()
        .dueDate(dueDate).dueBefore(oneHourAgo).count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  @Test
  public void shouldQueryForTasksWithoutDueDate() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    task.setDueDate(ClockUtil.now());
    taskService.saveTask(task);

    // then
    assertEquals(12, taskService.createTaskQuery().withoutDueDate().count());
  }

  @Test
  public void shouldRejectDueDateAndWithoutDueDateCombination() {
    assertThatThrownBy(() -> taskService.createTaskQuery().dueDate(ClockUtil.now()).withoutDueDate())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage");
  }

  @Test
  public void shouldRejectWithoutDueDateAndDueDateCombination() {
    assertThatThrownBy(() -> taskService.createTaskQuery().withoutDueDate().dueDate(ClockUtil.now()))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage");
  }

  @Test
  public void shouldRejectDueBeforeAndWithoutDueDateCombination() {
    assertThatThrownBy(() -> taskService.createTaskQuery().dueBefore(ClockUtil.now()).withoutDueDate())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage");
  }

  @Test
  public void shouldRejectWithoutDueDateAndDueBeforeCombination() {
    assertThatThrownBy(() -> taskService.createTaskQuery().withoutDueDate().dueBefore(ClockUtil.now()))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage");
  }

  @Test
  public void shouldRejectDueAfterAndWithoutDueDateCombination() {
    assertThatThrownBy(() -> taskService.createTaskQuery().dueAfter(ClockUtil.now()).withoutDueDate())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage");
  }

  @Test
  public void shouldRejectWithoutDueDateAndDueAfterCombination() {
    assertThatThrownBy(() -> taskService.createTaskQuery().withoutDueDate().dueAfter(ClockUtil.now()))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage");
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  @Test
  public void testFollowUpDate() throws Exception {
    Calendar otherDate = Calendar.getInstance();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // do not find any task instances with follow up date
    assertEquals(0, taskService.createTaskQuery().followUpDate(otherDate.getTime()).count());
    assertEquals(1, taskService.createTaskQuery().processInstanceId(processInstance.getId())
        // we might have tasks from other test cases - so we limit to the current PI
        .followUpBeforeOrNotExistent(otherDate.getTime()).count());

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // set follow-up date on task
    Date followUpDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 01:12:13");
    task.setFollowUpDate(followUpDate);
    taskService.saveTask(task);

    assertEquals(followUpDate, taskService.createTaskQuery().taskId(task.getId()).singleResult().getFollowUpDate());
    assertEquals(1, taskService.createTaskQuery().followUpDate(followUpDate).count());

    otherDate.setTime(followUpDate);

    otherDate.add(Calendar.YEAR, 1);
    assertEquals(0, taskService.createTaskQuery().followUpDate(otherDate.getTime()).count());
    assertEquals(1, taskService.createTaskQuery().followUpBefore(otherDate.getTime()).count());
    assertEquals(1, taskService.createTaskQuery().processInstanceId(processInstance.getId()) //
        .followUpBeforeOrNotExistent(otherDate.getTime()).count());
    assertEquals(0, taskService.createTaskQuery().followUpAfter(otherDate.getTime()).count());

    otherDate.add(Calendar.YEAR, -2);
    assertEquals(1, taskService.createTaskQuery().followUpAfter(otherDate.getTime()).count());
    assertEquals(0, taskService.createTaskQuery().followUpBefore(otherDate.getTime()).count());
    assertEquals(0, taskService.createTaskQuery().processInstanceId(processInstance.getId()) //
        .followUpBeforeOrNotExistent(otherDate.getTime()).count());

    taskService.complete(task.getId());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  @Test
  public void testFollowUpDateCombinations() throws ParseException {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // Set follow-up date on task
    Date dueDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 01:12:13");
    task.setFollowUpDate(dueDate);
    taskService.saveTask(task);

    Date oneHourAgo = new Date(dueDate.getTime() - 60 * 60 * 1000);
    Date oneHourLater = new Date(dueDate.getTime() + 60 * 60 * 1000);

    assertEquals(1, taskService.createTaskQuery()
        .followUpAfter(oneHourAgo).followUpDate(dueDate).followUpBefore(oneHourLater).count());
    assertEquals(0, taskService.createTaskQuery()
        .followUpAfter(oneHourLater).followUpDate(dueDate).followUpBefore(oneHourAgo).count());
    assertEquals(0, taskService.createTaskQuery()
        .followUpAfter(oneHourLater).followUpDate(dueDate).count());
    assertEquals(0, taskService.createTaskQuery()
        .followUpDate(dueDate).followUpBefore(oneHourAgo).count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  @Test
  public void testQueryByActivityInstanceId() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    String activityInstanceId = runtimeService.getActivityInstance(processInstance.getId())
                                              .getChildActivityInstances()[0].getId();

    assertEquals(1, taskService.createTaskQuery().activityInstanceIdIn(activityInstanceId).list().size());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  @Test
  public void testQueryByMultipleActivityInstanceIds() throws Exception {
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    String activityInstanceId1 = runtimeService.getActivityInstance(processInstance1.getId())
                                              .getChildActivityInstances()[0].getId();

    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    String activityInstanceId2 = runtimeService.getActivityInstance(processInstance2.getId())
                                              .getChildActivityInstances()[0].getId();

    List<Task> result1 = taskService.createTaskQuery().activityInstanceIdIn(activityInstanceId1).list();
    assertEquals(1, result1.size());
    assertEquals(processInstance1.getId(), result1.get(0).getProcessInstanceId());

    List<Task> result2 = taskService.createTaskQuery().activityInstanceIdIn(activityInstanceId2).list();
    assertEquals(1, result2.size());
    assertEquals(processInstance2.getId(), result2.get(0).getProcessInstanceId());

    assertEquals(2, taskService.createTaskQuery().activityInstanceIdIn(activityInstanceId1, activityInstanceId2).list().size());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  @Test
  public void testQueryByInvalidActivityInstanceId() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    assertEquals(0, taskService.createTaskQuery().activityInstanceIdIn("anInvalidActivityInstanceId").list().size());
  }

  @Test
  public void testQueryPaging() {
    TaskQuery query = taskService.createTaskQuery().taskCandidateUser("kermit");

    assertEquals(10, query.listPage(0, Integer.MAX_VALUE).size());

    // Verifying the un-paged results
    assertEquals(10, query.count());
    assertEquals(10, query.list().size());

    // Verifying paged results
    assertEquals(2, query.listPage(0, 2).size());
    assertEquals(2, query.listPage(2, 2).size());
    assertEquals(3, query.listPage(4, 3).size());
    assertEquals(1, query.listPage(9, 3).size());
    assertEquals(1, query.listPage(9, 1).size());

    // Verifying odd usages
    assertEquals(0, query.listPage(-1, -1).size());
    assertEquals(0, query.listPage(10, 2).size()); // 9 is the last index with a result
    assertEquals(10, query.listPage(0, 15).size()); // there are only 10 tasks
  }

  @Test
  public void testQuerySorting() {
    // default ordering is by id
    int expectedCount = 12;
    verifySortingAndCount(taskService.createTaskQuery(), expectedCount, taskById());
    verifySortingAndCount(taskService.createTaskQuery().orderByTaskId().asc(), expectedCount, taskById());
    verifySortingAndCount(taskService.createTaskQuery().orderByTaskName().asc(), expectedCount, taskByName());
    verifySortingAndCount(taskService.createTaskQuery().orderByTaskPriority().asc(), expectedCount, taskByPriority());
    verifySortingAndCount(taskService.createTaskQuery().orderByTaskAssignee().asc(), expectedCount, taskByAssignee());
    verifySortingAndCount(taskService.createTaskQuery().orderByTaskDescription().asc(), expectedCount, taskByDescription());
    verifySortingAndCount(taskService.createTaskQuery().orderByProcessInstanceId().asc(), expectedCount, taskByProcessInstanceId());
    verifySortingAndCount(taskService.createTaskQuery().orderByExecutionId().asc(), expectedCount, taskByExecutionId());
    verifySortingAndCount(taskService.createTaskQuery().orderByTaskCreateTime().asc(), expectedCount, taskByCreateTime());
    verifySortingAndCount(taskService.createTaskQuery().orderByDueDate().asc(), expectedCount, taskByDueDate());
    verifySortingAndCount(taskService.createTaskQuery().orderByFollowUpDate().asc(), expectedCount, taskByFollowUpDate());
    verifySortingAndCount(taskService.createTaskQuery().orderByCaseInstanceId().asc(), expectedCount, taskByCaseInstanceId());
    verifySortingAndCount(taskService.createTaskQuery().orderByCaseExecutionId().asc(), expectedCount, taskByCaseExecutionId());

    verifySortingAndCount(taskService.createTaskQuery().orderByTaskId().desc(), expectedCount, inverted(taskById()));
    verifySortingAndCount(taskService.createTaskQuery().orderByTaskName().desc(), expectedCount, inverted(taskByName()));
    verifySortingAndCount(taskService.createTaskQuery().orderByTaskPriority().desc(), expectedCount, inverted(taskByPriority()));
    verifySortingAndCount(taskService.createTaskQuery().orderByTaskAssignee().desc(), expectedCount, inverted(taskByAssignee()));
    verifySortingAndCount(taskService.createTaskQuery().orderByTaskDescription().desc(), expectedCount, inverted(taskByDescription()));
    verifySortingAndCount(taskService.createTaskQuery().orderByProcessInstanceId().desc(), expectedCount, inverted(taskByProcessInstanceId()));
    verifySortingAndCount(taskService.createTaskQuery().orderByExecutionId().desc(), expectedCount, inverted(taskByExecutionId()));
    verifySortingAndCount(taskService.createTaskQuery().orderByTaskCreateTime().desc(), expectedCount, inverted(taskByCreateTime()));
    verifySortingAndCount(taskService.createTaskQuery().orderByDueDate().desc(), expectedCount, inverted(taskByDueDate()));
    verifySortingAndCount(taskService.createTaskQuery().orderByFollowUpDate().desc(), expectedCount, inverted(taskByFollowUpDate()));
    verifySortingAndCount(taskService.createTaskQuery().orderByCaseInstanceId().desc(), expectedCount, inverted(taskByCaseInstanceId()));
    verifySortingAndCount(taskService.createTaskQuery().orderByCaseExecutionId().desc(), expectedCount, inverted(taskByCaseExecutionId()));

    verifySortingAndCount(taskService.createTaskQuery().orderByTaskId().taskName("testTask").asc(), 6, taskById());
    verifySortingAndCount(taskService.createTaskQuery().orderByTaskId().taskName("testTask").desc(), 6, inverted(taskById()));
  }

  @Test
  public void testQuerySortingByNameShouldBeCaseInsensitive() {
    // create task with capitalized name
    Task task = taskService.newTask("caseSensitiveTestTask");
    task.setName("CaseSensitiveTestTask");
    taskService.saveTask(task);

    // create task filter
    Filter filter = filterService.newTaskFilter("taskNameOrdering");
    filterService.saveFilter(filter);

    List<String> sortedNames = getTaskNamesFromTasks(taskService.createTaskQuery().list());
    Collections.sort(sortedNames, String.CASE_INSENSITIVE_ORDER);

    // ascending ordering
    TaskQuery taskQuery = taskService.createTaskQuery().orderByTaskNameCaseInsensitive().asc();
    List<String> ascNames = getTaskNamesFromTasks(taskQuery.list());
    assertEquals(sortedNames, ascNames);

    // test filter merging
    ascNames = getTaskNamesFromTasks(filterService.list(filter.getId(), taskQuery));
    assertEquals(sortedNames, ascNames);

    // descending ordering

    // reverse sorted names to test descending ordering
    Collections.reverse(sortedNames);

    taskQuery = taskService.createTaskQuery().orderByTaskNameCaseInsensitive().desc();
    List<String> descNames = getTaskNamesFromTasks(taskQuery.list());
    assertEquals(sortedNames, descNames);

    // test filter merging
    descNames = getTaskNamesFromTasks(filterService.list(filter.getId(), taskQuery));
    assertEquals(sortedNames, descNames);

    // delete test task
    taskService.deleteTask(task.getId(), true);

    // delete filter
    filterService.deleteFilter(filter.getId());
  }

  @Test
  public void testQueryOrderByTaskName() {

    // asc
    List<Task> tasks = taskService.createTaskQuery()
      .orderByTaskName()
      .asc()
      .list();
    assertEquals(12, tasks.size());


    List<String> taskNames = getTaskNamesFromTasks(tasks);
    assertEquals("accountancy description", taskNames.get(0));
    assertEquals("accountancy description", taskNames.get(1));
    assertEquals("gonzo_Task", taskNames.get(2));
    assertEquals("managementAndAccountancyTask", taskNames.get(3));
    assertEquals("managementTask", taskNames.get(4));
    assertEquals("managementTask", taskNames.get(5));
    assertEquals("testTask", taskNames.get(6));
    assertEquals("testTask", taskNames.get(7));
    assertEquals("testTask", taskNames.get(8));
    assertEquals("testTask", taskNames.get(9));
    assertEquals("testTask", taskNames.get(10));
    assertEquals("testTask", taskNames.get(11));

    // desc
    tasks = taskService.createTaskQuery()
      .orderByTaskName()
      .desc()
      .list();
    assertEquals(12, tasks.size());

    taskNames = getTaskNamesFromTasks(tasks);
    assertEquals("testTask", taskNames.get(0));
    assertEquals("testTask", taskNames.get(1));
    assertEquals("testTask", taskNames.get(2));
    assertEquals("testTask", taskNames.get(3));
    assertEquals("testTask", taskNames.get(4));
    assertEquals("testTask", taskNames.get(5));
    assertEquals("managementTask", taskNames.get(6));
    assertEquals("managementTask", taskNames.get(7));
    assertEquals("managementAndAccountancyTask", taskNames.get(8));
    assertEquals("gonzo_Task", taskNames.get(9));
    assertEquals("accountancy description", taskNames.get(10));
    assertEquals("accountancy description", taskNames.get(11));
  }

  public List<String> getTaskNamesFromTasks(List<Task> tasks) {
    List<String> names = new ArrayList<>();
    for (Task task : tasks) {
      names.add(task.getName());
    }
    return names;
  }

  @Test
  public void testNativeQuery() {
    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();
    assertEquals(tablePrefix + "ACT_RU_TASK", managementService.getTableName(Task.class));
    assertEquals(tablePrefix + "ACT_RU_TASK", managementService.getTableName(TaskEntity.class));
    assertEquals(12, taskService.createNativeTaskQuery().sql("SELECT * FROM " + managementService.getTableName(Task.class)).list().size());
    assertEquals(12, taskService.createNativeTaskQuery().sql("SELECT count(*) FROM " + managementService.getTableName(Task.class)).count());

    assertEquals(144, taskService.createNativeTaskQuery().sql("SELECT count(*) FROM " + tablePrefix + "ACT_RU_TASK T1, " + tablePrefix + "ACT_RU_TASK T2").count());

    // join task and variable instances
    assertEquals(1, taskService.createNativeTaskQuery().sql("SELECT count(*) FROM " + managementService.getTableName(Task.class) + " T1, "+managementService.getTableName(VariableInstanceEntity.class)+" V1 WHERE V1.TASK_ID_ = T1.ID_").count());
    List<Task> tasks = taskService.createNativeTaskQuery().sql("SELECT T1.* FROM " + managementService.getTableName(Task.class) + " T1, "+managementService.getTableName(VariableInstanceEntity.class)+" V1 WHERE V1.TASK_ID_ = T1.ID_").list();
    assertEquals(1, tasks.size());
    assertEquals("gonzo_Task", tasks.get(0).getName());

    // select with distinct
    assertEquals(12, taskService.createNativeTaskQuery().sql("SELECT DISTINCT T1.* FROM " + tablePrefix + "ACT_RU_TASK T1").list().size());

    assertEquals(1, taskService.createNativeTaskQuery().sql("SELECT count(*) FROM " + managementService.getTableName(Task.class) + " T WHERE T.NAME_ = 'gonzo_Task'").count());
    assertEquals(1, taskService.createNativeTaskQuery().sql("SELECT * FROM " + managementService.getTableName(Task.class) + " T WHERE T.NAME_ = 'gonzo_Task'").list().size());

    // use parameters
    assertEquals(1, taskService.createNativeTaskQuery().sql("SELECT count(*) FROM " + managementService.getTableName(Task.class) + " T WHERE T.NAME_ = #{taskName}").parameter("taskName", "gonzo_Task").count());
  }

  @Test
  public void testNativeQueryPaging() {
    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();
    assertEquals(tablePrefix + "ACT_RU_TASK", managementService.getTableName(Task.class));
    assertEquals(tablePrefix + "ACT_RU_TASK", managementService.getTableName(TaskEntity.class));
    assertEquals(5, taskService.createNativeTaskQuery().sql("SELECT * FROM " + managementService.getTableName(Task.class)).listPage(0, 5).size());
    assertEquals(2, taskService.createNativeTaskQuery().sql("SELECT * FROM " + managementService.getTableName(Task.class)).listPage(10, 12).size());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByCaseDefinitionId() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseDefinitionId(caseDefinitionId);

    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidCaseDefinitionId() {
    TaskQuery query = taskService.createTaskQuery();

    query.caseDefinitionId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseDefinitionId(null);
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByCaseDefinitionKey() {
    String caseDefinitionKey = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getKey();

    caseService
      .withCaseDefinitionByKey(caseDefinitionKey)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseDefinitionKey(caseDefinitionKey);

    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidCaseDefinitionKey() {
    TaskQuery query = taskService.createTaskQuery();

    query.caseDefinitionKey("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseDefinitionKey(null);
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByCaseDefinitionName() {
    CaseDefinition caseDefinition = repositoryService
        .createCaseDefinitionQuery()
        .singleResult();

    String caseDefinitionId = caseDefinition.getId();
    String caseDefinitionName = caseDefinition.getName();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseDefinitionName(caseDefinitionName);

    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidCaseDefinitionName() {
    TaskQuery query = taskService.createTaskQuery();

    query.caseDefinitionName("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseDefinitionName(null);
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn", "org/camunda/bpm/engine/test/api/repository/three_.cmmn"})
  @Test
  public void testQueryByCaseDefinitionNameLike() {
    List<String> caseDefinitionIds = getCaseDefinitionIds();

    for (String caseDefinitionId : caseDefinitionIds) {
      caseService
          .withCaseDefinition(caseDefinitionId)
          .create();
    }
    TaskQuery query = taskService.createTaskQuery();

    query.caseDefinitionNameLike("One T%");
    verifyQueryResults(query, 1);

    query.caseDefinitionNameLike("%Task Case");
    verifyQueryResults(query, 1);

    query.caseDefinitionNameLike("%Task%");
    verifyQueryResults(query, 1);

    query.caseDefinitionNameLike("%z\\_");
    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidCaseDefinitionNameLike() {
    TaskQuery query = taskService.createTaskQuery();

    query.caseDefinitionNameLike("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseDefinitionNameLike(null);
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByCaseInstanceId() {
    String caseDefinitionId = getCaseDefinitionId();

    String caseInstanceId = caseService
      .withCaseDefinition(caseDefinitionId)
      .create()
      .getId();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceId(caseInstanceId);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources=
    {
      "org/camunda/bpm/engine/test/api/task/TaskQueryTest.testQueryByCaseInstanceIdHierarchy.cmmn",
      "org/camunda/bpm/engine/test/api/task/TaskQueryTest.testQueryByCaseInstanceIdHierarchy.bpmn20.xml"
      })
  @Test
  public void testQueryByCaseInstanceIdHierarchy() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String processTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_ProcessTask_1")
        .singleResult()
        .getId();

    // then

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceId(caseInstanceId);

    verifyQueryResults(query, 2);

    for (Task task : query.list()) {
      assertEquals(caseInstanceId, task.getCaseInstanceId());
      taskService.complete(task.getId());
    }

    verifyQueryResults(query, 1);
    assertEquals(caseInstanceId, query.singleResult().getCaseInstanceId());

    taskService.complete(query.singleResult().getId());

    verifyQueryResults(query, 0);
  }

  @Test
  public void testQueryByInvalidCaseInstanceId() {
    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseInstanceId(null);
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByCaseInstanceBusinessKey() {
    String caseDefinitionId = getCaseDefinitionId();

    String businessKey = "aBusinessKey";

    caseService
      .withCaseDefinition(caseDefinitionId)
      .businessKey(businessKey)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceBusinessKey(businessKey);

    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidCaseInstanceBusinessKey() {
    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceBusinessKey("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseInstanceBusinessKey(null);
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByCaseInstanceBusinessKeyLike() {
    String caseDefinitionId = getCaseDefinitionId();

    String businessKey = "aBusiness_Key";

    caseService
      .withCaseDefinition(caseDefinitionId)
      .businessKey(businessKey)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceBusinessKeyLike("aBus%");
    verifyQueryResults(query, 1);

    query.caseInstanceBusinessKeyLike("%siness\\_Key");
    verifyQueryResults(query, 1);

    query.caseInstanceBusinessKeyLike("%sines%");
    verifyQueryResults(query, 1);

    query.caseInstanceBusinessKeyLike("%sines%");
    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidCaseInstanceBusinessKeyLike() {
    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceBusinessKeyLike("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseInstanceBusinessKeyLike(null);
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithManualActivation.cmmn"})
  @Test
  public void testQueryByCaseExecutionId() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .create();

    String humanTaskExecutionId = startDefaultCaseExecutionManually();

    TaskQuery query = taskService.createTaskQuery();

    query.caseExecutionId(humanTaskExecutionId);

    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidCaseExecutionId() {
    TaskQuery query = taskService.createTaskQuery();

    query.caseExecutionId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseExecutionId(null);
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // OK
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByNullCaseInstanceVariableValueEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aNullValue", null)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueEquals("aNullValue", null);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByStringCaseInstanceVariableValueEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aStringValue", "abc")
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueEquals("aStringValue", "abc");

    verifyQueryResults(query, 1);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  @Test
  public void testCaseInstanceVariableNameEqualsIgnoreCase() throws Exception {
    String caseDefinitionId = getCaseDefinitionId();

    String variableName = "someVariable";
    String variableValue = "someCamelCaseValue";

    caseService.withCaseDefinition(caseDefinitionId).setVariable(variableName, variableValue).create();

    // query for case-insensitive variable name should only return a result if case-insensitive search is used
    assertEquals(1, taskService.createTaskQuery().matchVariableNamesIgnoreCase().caseInstanceVariableValueEquals(variableName.toLowerCase(), variableValue).count());
    assertEquals(0, taskService.createTaskQuery().caseInstanceVariableValueEquals(variableName.toLowerCase(), variableValue).count());

    // query should treat all variables case-insensitively, even when flag is set after variable
    assertEquals(1, taskService.createTaskQuery().caseInstanceVariableValueEquals(variableName.toLowerCase(), variableValue).matchVariableNamesIgnoreCase().count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByStringCaseInstanceVariableValueEqualsIgnoreCase() {
    String caseDefinitionId = getCaseDefinitionId();

    String variableName = "someVariable";
    String variableValue = "someCamelCaseValue";

    caseService
    .withCaseDefinition(caseDefinitionId)
    .setVariable(variableName, variableValue)
    .create();

    TaskQuery query;

    // query for case-insensitive variable value should only return a result when case-insensitive search is used
    query = taskService.createTaskQuery().matchVariableValuesIgnoreCase().caseInstanceVariableValueEquals(variableName, variableValue.toLowerCase());
    verifyQueryResults(query, 1);
    query = taskService.createTaskQuery().caseInstanceVariableValueEquals(variableName, variableValue.toLowerCase());
    verifyQueryResults(query, 0);

    // query for non existing variable should return zero results
    query = taskService.createTaskQuery().matchVariableValuesIgnoreCase().caseInstanceVariableValueEquals("nonExistingVariable", variableValue.toLowerCase());
    verifyQueryResults(query, 0);

    // query for existing variable with different value should return zero results
    query = taskService.createTaskQuery().matchVariableValuesIgnoreCase().caseInstanceVariableValueEquals(variableName, "nonExistentValue".toLowerCase());
    verifyQueryResults(query, 0);

    // query for case-insensitive variable with not equals operator should only return a result when case-sensitive search is used
    query = taskService.createTaskQuery().matchVariableValuesIgnoreCase().caseInstanceVariableValueNotEquals(variableName, variableValue.toLowerCase());
    verifyQueryResults(query, 0);
    query = taskService.createTaskQuery().caseInstanceVariableValueNotEquals(variableName, variableValue.toLowerCase());
    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByBooleanCaseInstanceVariableValueEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aBooleanValue", true)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueEquals("aBooleanValue", true);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByShortCaseInstanceVariableValueEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aShortValue", (short) 123)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueEquals("aShortValue", (short) 123);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByIntegerCaseInstanceVariableValueEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("anIntegerValue", 456)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueEquals("anIntegerValue", 456);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByLongCaseInstanceVariableValueEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aLongValue", (long) 789)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueEquals("aLongValue", (long) 789);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByDateCaseInstanceVariableValueEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    Date now = new Date();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aDateValue", now)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueEquals("aDateValue", now);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByDoubleCaseInstanceVariableValueEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aDoubleValue", 1.5)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueEquals("aDoubleValue", 1.5);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByByteArrayCaseInstanceVariableValueEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aByteArrayValue", bytes)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueEquals("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryBySerializableCaseInstanceVariableValueEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    List<String> serializable = new ArrayList<>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aSerializableValue", serializable)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueEquals("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByFileCaseInstanceVariableValueEquals() {
    FileValue fileValue = createDefaultFileValue();
    String variableName = "aFileValue";

    startDefaultCaseWithVariable(fileValue, variableName);
    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueEquals(variableName, fileValue).list();
      fail();
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage()).contains("Variables of type File cannot be used to query");
    }
  }

  /**
   * Starts the one deployed case at the point of the manual activity PI_HumanTask_1
   * with the given variable.
   */
  protected void startDefaultCaseWithVariable(Object variableValue, String variableName) {
    String caseDefinitionId = getCaseDefinitionId();
    createCaseWithVariable(caseDefinitionId, variableValue, variableName);
  }

  /**
   * @return the case definition id if only one case is deployed.
   */
  protected String getCaseDefinitionId() {
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();
    return caseDefinitionId;
  }

  /**
   * @return the case definition ids
   */
  protected List<String> getCaseDefinitionIds() {
    List<String> caseDefinitionIds = new ArrayList<>();
    List<CaseDefinition> caseDefinitions = repositoryService.createCaseDefinitionQuery().list();
    for (CaseDefinition caseDefinition: caseDefinitions) {
      caseDefinitionIds.add(caseDefinition.getId());
    }
    return caseDefinitionIds;
  }

  protected void createCaseWithVariable(String caseDefinitionId, Object variableValue, String variableName) {
    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable(variableName, variableValue)
      .create();
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByStringCaseInstanceVariableValueNotEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aStringValue", "abc")
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueNotEquals("aStringValue", "abd");

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByBooleanCaseInstanceVariableValueNotEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aBooleanValue", true)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueNotEquals("aBooleanValue", false);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByShortCaseInstanceVariableValueNotEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aShortValue", (short) 123)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueNotEquals("aShortValue", (short) 124);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByIntegerCaseInstanceVariableValueNotEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("anIntegerValue", 456)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueNotEquals("anIntegerValue", 457);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByLongCaseInstanceVariableValueNotEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aLongValue", (long) 789)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueNotEquals("aLongValue", (long) 790);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByDateCaseInstanceVariableValueNotEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    Date now = new Date();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aDateValue", now)
      .create();

    Date before = new Date(now.getTime() - 100000);

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueNotEquals("aDateValue", before);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByDoubleCaseInstanceVariableValueNotEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aDoubleValue", 1.5)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueNotEquals("aDoubleValue", 1.6);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByFileCaseInstanceVariableValueNotEquals() {
    FileValue fileValue = createDefaultFileValue();
    String variableName = "aFileValue";

    startDefaultCaseWithVariable(fileValue, variableName);
    TaskQuery query = taskService.createTaskQuery();
    try {
      query.caseInstanceVariableValueNotEquals(variableName, fileValue).list();
      fail();
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage()).contains("Variables of type File cannot be used to query");
    }
  }

  /**
   * @return
   */
  protected FileValue createDefaultFileValue() {
    FileValue fileValue = Variables.fileValue("tst.txt").file("somebytes".getBytes()).create();
    return fileValue;
  }

  /**
   * Starts the case execution for oneTaskCase.cmmn<p>
   * Only works for testcases, which deploy that process.
   *
   * @return the execution id for the activity PI_HumanTask_1
   */
  protected String startDefaultCaseExecutionManually() {
    String humanTaskExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskExecutionId)
      .manualStart();
    return humanTaskExecutionId;
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryBySerializableCaseInstanceVariableValueNotEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    List<String> serializable = new ArrayList<>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aSerializableValue", serializable)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueNotEquals("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByByteArrayCaseInstanceVariableValueNotEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aByteArrayValue", bytes)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueNotEquals("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByNullCaseInstanceVariableValueGreaterThan() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aNullValue", null)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueGreaterThan("aNullValue", null).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByStringCaseInstanceVariableValueGreaterThan() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aStringValue", "abc")
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueGreaterThan("aStringValue", "ab");

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByBooleanCaseInstanceVariableValueGreaterThan() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aBooleanValue", true)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueGreaterThan("aBooleanValue", false).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByShortCaseInstanceVariableValueGreaterThan() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aShortValue", (short) 123)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueGreaterThan("aShortValue", (short) 122);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByIntegerCaseInstanceVariableValueGreaterThan() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("anIntegerValue", 456)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueGreaterThan("anIntegerValue", 455);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByLongCaseInstanceVariableValueGreaterThan() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aLongValue", (long) 789)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueGreaterThan("aLongValue", (long) 788);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByDateCaseInstanceVariableValueGreaterThan() {
    String caseDefinitionId = getCaseDefinitionId();

    Date now = new Date();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aDateValue", now)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    Date before = new Date(now.getTime() - 100000);

    query.caseInstanceVariableValueGreaterThan("aDateValue", before);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByDoubleCaseInstanceVariableValueGreaterThan() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aDoubleValue", 1.5)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueGreaterThan("aDoubleValue", 1.4);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByByteArrayCaseInstanceVariableValueGreaterThan() {
    String caseDefinitionId = getCaseDefinitionId();

    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aByteArrayValue", bytes)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueGreaterThan("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryBySerializableCaseInstanceVariableGreaterThan() {
    String caseDefinitionId = getCaseDefinitionId();

    List<String> serializable = new ArrayList<>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aSerializableValue", serializable)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueGreaterThan("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithManualActivation.cmmn"})
  @Test
  public void testQueryByFileCaseInstanceVariableValueGreaterThan() {
    FileValue fileValue = createDefaultFileValue();
    String variableName = "aFileValue";

    startDefaultCaseWithVariable(fileValue, variableName);
    startDefaultCaseExecutionManually();
    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueGreaterThan(variableName, fileValue).list();
      fail();
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage()).contains("Variables of type File cannot be used to query");
      }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByNullCaseInstanceVariableValueGreaterThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aNullValue", null)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueGreaterThanOrEquals("aNullValue", null).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByStringCaseInstanceVariableValueGreaterThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aStringValue", "abc")
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueGreaterThanOrEquals("aStringValue", "ab");

    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery();

    query.caseInstanceVariableValueGreaterThanOrEquals("aStringValue", "abc");

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByBooleanCaseInstanceVariableValueGreaterThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aBooleanValue", true)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueGreaterThanOrEquals("aBooleanValue", false).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByShortCaseInstanceVariableValueGreaterThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aShortValue", (short) 123)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueGreaterThanOrEquals("aShortValue", (short) 122);

    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery();

    query.caseInstanceVariableValueGreaterThanOrEquals("aShortValue", (short) 123);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByIntegerCaseInstanceVariableValueGreaterThanOrEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("anIntegerValue", 456)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueGreaterThanOrEquals("anIntegerValue", 455);

    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery();

    query.caseInstanceVariableValueGreaterThanOrEquals("anIntegerValue", 456);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByLongCaseInstanceVariableValueGreaterThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aLongValue", (long) 789)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueGreaterThanOrEquals("aLongValue", (long) 788);

    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery();

    query.caseInstanceVariableValueGreaterThanOrEquals("aLongValue", (long) 789);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByDateCaseInstanceVariableValueGreaterThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    Date now = new Date();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aDateValue", now)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    Date before = new Date(now.getTime() - 100000);

    query.caseInstanceVariableValueGreaterThanOrEquals("aDateValue", before);

    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery();

    query.caseInstanceVariableValueGreaterThanOrEquals("aDateValue", now);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByDoubleCaseInstanceVariableValueGreaterThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aDoubleValue", 1.5)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueGreaterThanOrEquals("aDoubleValue", 1.4);

    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery();

    query.caseInstanceVariableValueGreaterThanOrEquals("aDoubleValue", 1.5);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByByteArrayCaseInstanceVariableValueGreaterThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aByteArrayValue", bytes)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueGreaterThanOrEquals("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryBySerializableCaseInstanceVariableGreaterThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    List<String> serializable = new ArrayList<>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aSerializableValue", serializable)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueGreaterThanOrEquals("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByFileCaseInstanceVariableValueGreaterThanOrEqual() {
    FileValue fileValue = createDefaultFileValue();
    String variableName = "aFileValue";

    startDefaultCaseWithVariable(fileValue, variableName);
    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueGreaterThanOrEquals(variableName, fileValue).list();
      fail();
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage()).contains("Variables of type File cannot be used to query");
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByNullCaseInstanceVariableValueLessThan() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aNullValue", null)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueLessThan("aNullValue", null).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByStringCaseInstanceVariableValueLessThan() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aStringValue", "abc")
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLessThan("aStringValue", "abd");

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByBooleanCaseInstanceVariableValueLessThan() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aBooleanValue", true)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueLessThan("aBooleanValue", false).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByShortCaseInstanceVariableValueLessThan() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aShortValue", (short) 123)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLessThan("aShortValue", (short) 124);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByIntegerCaseInstanceVariableValueLessThan() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("anIntegerValue", 456)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLessThan("anIntegerValue", 457);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByLongCaseInstanceVariableValueLessThan() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aLongValue", (long) 789)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLessThan("aLongValue", (long) 790);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByDateCaseInstanceVariableValueLessThan() {
    String caseDefinitionId = getCaseDefinitionId();

    Date now = new Date();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aDateValue", now)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    Date after = new Date(now.getTime() + 100000);

    query.caseInstanceVariableValueLessThan("aDateValue", after);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByDoubleCaseInstanceVariableValueLessThan() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aDoubleValue", 1.5)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLessThan("aDoubleValue", 1.6);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByByteArrayCaseInstanceVariableValueLessThan() {
    String caseDefinitionId = getCaseDefinitionId();

    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aByteArrayValue", bytes)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueLessThan("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryBySerializableCaseInstanceVariableLessThan() {
    String caseDefinitionId = getCaseDefinitionId();

    List<String> serializable = new ArrayList<>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aSerializableValue", serializable)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueLessThan("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByFileCaseInstanceVariableValueLessThan() {
    FileValue fileValue = createDefaultFileValue();
    String variableName = "aFileValue";

    startDefaultCaseWithVariable(fileValue, variableName);
    TaskQuery query = taskService.createTaskQuery();
    try {
      query.caseInstanceVariableValueLessThan(variableName, fileValue).list();
      fail();
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage()).contains("Variables of type File cannot be used to query");
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByNullCaseInstanceVariableValueLessThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aNullValue", null)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueLessThanOrEquals("aNullValue", null).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByStringCaseInstanceVariableValueLessThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aStringValue", "abc")
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLessThanOrEquals("aStringValue", "abd");

    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLessThanOrEquals("aStringValue", "abc");

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByBooleanCaseInstanceVariableValueLessThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aBooleanValue", true)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueLessThanOrEquals("aBooleanValue", false).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByShortCaseInstanceVariableValueLessThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aShortValue", (short) 123)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLessThanOrEquals("aShortValue", (short) 124);

    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLessThanOrEquals("aShortValue", (short) 123);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByIntegerCaseInstanceVariableValueLessThanOrEquals() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("anIntegerValue", 456)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLessThanOrEquals("anIntegerValue", 457);

    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLessThanOrEquals("anIntegerValue", 456);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByLongCaseInstanceVariableValueLessThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aLongValue", (long) 789)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLessThanOrEquals("aLongValue", (long) 790);

    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLessThanOrEquals("aLongValue", (long) 789);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByDateCaseInstanceVariableValueLessThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    Date now = new Date();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aDateValue", now)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    Date after = new Date(now.getTime() + 100000);

    query.caseInstanceVariableValueLessThanOrEquals("aDateValue", after);

    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLessThanOrEquals("aDateValue", now);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByDoubleCaseInstanceVariableValueLessThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aDoubleValue", 1.5)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLessThanOrEquals("aDoubleValue", 1.6);

    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLessThanOrEquals("aDoubleValue", 1.5);

    verifyQueryResults(query, 1);

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByByteArrayCaseInstanceVariableValueLessThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    byte[] bytes = "somebytes".getBytes();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aByteArrayValue", bytes)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueLessThanOrEquals("aByteArrayValue", bytes).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryBySerializableCaseInstanceVariableLessThanOrEqual() {
    String caseDefinitionId = getCaseDefinitionId();

    List<String> serializable = new ArrayList<>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aSerializableValue", serializable)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueLessThanOrEquals("aSerializableValue", serializable).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByFileCaseInstanceVariableValueLessThanOrEqual() {
    FileValue fileValue = createDefaultFileValue();
    String variableName = "aFileValue";

    startDefaultCaseWithVariable(fileValue, variableName);
    TaskQuery query = taskService.createTaskQuery();
    try {
      query.caseInstanceVariableValueLessThanOrEquals(variableName, fileValue).list();
      fail();
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage()).contains("Variables of type File cannot be used to query");
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByNullCaseInstanceVariableValueLike() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aNullValue", null)
      .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueLike("aNullValue", null).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByNullCaseInstanceVariableValueNotLike() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
            .withCaseDefinition(caseDefinitionId)
            .setVariable("aNullValue", null)
            .create();

    TaskQuery query = taskService.createTaskQuery();

    try {
      query.caseInstanceVariableValueNotLike("aNullValue", null).list();
      fail();
    } catch (ProcessEngineException e) {}

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByStringCaseInstanceVariableValueLike() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
      .withCaseDefinition(caseDefinitionId)
      .setVariable("aStringValue", "abc")
      .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLike("aStringValue", "ab%");

    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLike("aStringValue", "%bc");

    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLike("aStringValue", "%b%");

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByStringCaseInstanceVariableValueLikeIgnoreCase() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
    .withCaseDefinition(caseDefinitionId)
    .setVariable("aStringVariable", "aStringValue")
    .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueLike("aStringVariable", "aString%".toLowerCase());

    verifyQueryResults(query, 0);

    query = taskService.createTaskQuery().matchVariableValuesIgnoreCase().caseInstanceVariableValueLike("aStringVariable", "aString%".toLowerCase());

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByStringCaseInstanceVariableValueNotLike() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
            .withCaseDefinition(caseDefinitionId)
            .setVariable("aStringValue", "abc")
            .create();

    TaskQuery query = taskService.createTaskQuery();
    query.caseInstanceVariableValueNotLike("aStringValue", "abc%");
    verifyQueryResults(query, 0);

    query = taskService.createTaskQuery();
    query.caseInstanceVariableValueNotLike("aStringValue", "%bc");
    verifyQueryResults(query, 0);

    query = taskService.createTaskQuery();
    query.caseInstanceVariableValueNotLike("aStringValue", "%b%");
    verifyQueryResults(query, 0);

    query = taskService.createTaskQuery();
    query.caseInstanceVariableValueNotLike("aStringValue", "abx%");
    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery();
    query.caseInstanceVariableValueNotLike("aStringValue", "%be");
    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery();
    query.caseInstanceVariableValueNotLike("aStringValue", "abd");
    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery();
    query.caseInstanceVariableValueNotLike("nonExistingVar", "%b%");
    verifyQueryResults(query, 0);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryByStringCaseInstanceVariableValueNotLikeIgnoreCase() {
    String caseDefinitionId = getCaseDefinitionId();

    caseService
            .withCaseDefinition(caseDefinitionId)
            .setVariable("aStringVariable", "aStringValue")
            .create();

    TaskQuery query = taskService.createTaskQuery();

    query.caseInstanceVariableValueNotLike("aStringVariable", "aString%".toLowerCase());
    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery().matchVariableValuesIgnoreCase().caseInstanceVariableValueNotLike("aStringVariable", "aString%".toLowerCase());
    verifyQueryResults(query, 0);

    query = taskService.createTaskQuery().matchVariableValuesIgnoreCase().caseInstanceVariableValueNotLike("aStringVariable", "%ringValue".toLowerCase());
    verifyQueryResults(query, 0);

    query = taskService.createTaskQuery().matchVariableValuesIgnoreCase().caseInstanceVariableValueNotLike("aStringVariable", "%ngVal%".toLowerCase());
    verifyQueryResults(query, 0);

    query = taskService.createTaskQuery().matchVariableValuesIgnoreCase().caseInstanceVariableValueNotLike("aStringVariable", "aStrong%".toLowerCase());
    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery().matchVariableValuesIgnoreCase().caseInstanceVariableValueNotLike("aStringVariable", "%Strong".toLowerCase());
    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery().matchVariableValuesIgnoreCase().caseInstanceVariableValueNotLike("aStringVariable", "%ngVar%".toLowerCase());
    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery().matchVariableValuesIgnoreCase().caseInstanceVariableValueNotLike("aStringVariable", "stringVal".toLowerCase());
    verifyQueryResults(query, 1);

    query = taskService.createTaskQuery().matchVariableValuesIgnoreCase().caseInstanceVariableValueNotLike("nonExistingVar", "%String%".toLowerCase());
    verifyQueryResults(query, 0);
  }

  @Deployment
  @Test
  public void testQueryByVariableInParallelBranch() throws Exception {
    runtimeService.startProcessInstanceByKey("parallelGateway");

    // when there are two process variables of the same name but different types
    Execution task1Execution = runtimeService.createExecutionQuery().activityId("task1").singleResult();
    runtimeService.setVariableLocal(task1Execution.getId(), "var", 12345L);
    Execution task2Execution = runtimeService.createExecutionQuery().activityId("task2").singleResult();
    runtimeService.setVariableLocal(task2Execution.getId(), "var", 12345);

    // then the task query should be able to filter by both variables and return both tasks
    assertEquals(2, taskService.createTaskQuery().processVariableValueEquals("var", 12345).count());
    assertEquals(2, taskService.createTaskQuery().processVariableValueEquals("var", 12345L).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  @Test
  public void testQueryResultOrderingByProcessVariables() {
    // given three tasks with String process instance variables
    ProcessInstance instance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "bValue"));
    ProcessInstance instance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "cValue"));
    ProcessInstance instance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "aValue"));

    // when I make a task query with ascending variable ordering by String values
    List<Task> tasks = taskService.createTaskQuery()
      .processDefinitionKey("oneTaskProcess")
      .orderByProcessVariable("var", ValueType.STRING)
      .asc()
      .list();

    // then the tasks are ordered correctly
    assertEquals(3, tasks.size());
    // then in alphabetical order
    assertEquals(instance3.getId(), tasks.get(0).getProcessInstanceId());
    assertEquals(instance1.getId(), tasks.get(1).getProcessInstanceId());
    assertEquals(instance2.getId(), tasks.get(2).getProcessInstanceId());

    // when I make a task query with descending variable ordering by String values
    tasks = taskService.createTaskQuery()
      .processDefinitionKey("oneTaskProcess")
      .orderByProcessVariable("var", ValueType.STRING)
      .desc()
      .list();

    // then the tasks are ordered correctly
    assertEquals(3, tasks.size());
    // then in alphabetical order
    assertEquals(instance2.getId(), tasks.get(0).getProcessInstanceId());
    assertEquals(instance1.getId(), tasks.get(1).getProcessInstanceId());
    assertEquals(instance3.getId(), tasks.get(2).getProcessInstanceId());


    // when I make a task query with variable ordering by Integer values
    List<Task> unorderedTasks = taskService.createTaskQuery()
      .processDefinitionKey("oneTaskProcess")
      .orderByProcessVariable("var", ValueType.INTEGER)
      .asc()
      .list();

    // then the tasks are in no particular ordering
    assertEquals(3, unorderedTasks.size());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/task/TaskQueryTest.testLocalExecutionVariable.bpmn20.xml")
  @Test
  public void testQueryResultOrderingByExecutionVariables() {
    // given three tasks with String process instance variables
    ProcessInstance instance1 = runtimeService.startProcessInstanceByKey("parallelGateway",
      Collections.<String, Object>singletonMap("var", "aValue"));
    ProcessInstance instance2 = runtimeService.startProcessInstanceByKey("parallelGateway",
      Collections.<String, Object>singletonMap("var", "bValue"));
    ProcessInstance instance3 = runtimeService.startProcessInstanceByKey("parallelGateway",
      Collections.<String, Object>singletonMap("var", "cValue"));

    // and some local variables on the tasks
    Task task1 = taskService.createTaskQuery().processInstanceId(instance1.getId()).singleResult();
    runtimeService.setVariableLocal(task1.getExecutionId(), "var", "cValue");

    Task task2 = taskService.createTaskQuery().processInstanceId(instance2.getId()).singleResult();
    runtimeService.setVariableLocal(task2.getExecutionId(), "var", "bValue");

    Task task3 = taskService.createTaskQuery().processInstanceId(instance3.getId()).singleResult();
    runtimeService.setVariableLocal(task3.getExecutionId(), "var", "aValue");

    // when I make a task query with ascending variable ordering by tasks variables
    List<Task> tasks = taskService.createTaskQuery()
      .processDefinitionKey("parallelGateway")
      .orderByExecutionVariable("var", ValueType.STRING)
      .asc()
      .list();

    // then the tasks are ordered correctly by their local variables
    assertEquals(3, tasks.size());
    assertEquals(instance3.getId(), tasks.get(0).getProcessInstanceId());
    assertEquals(instance2.getId(), tasks.get(1).getProcessInstanceId());
    assertEquals(instance1.getId(), tasks.get(2).getProcessInstanceId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  @Test
  public void testQueryResultOrderingByTaskVariables() {
    // given three tasks with String process instance variables
    ProcessInstance instance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
      Collections.<String, Object>singletonMap("var", "aValue"));
    ProcessInstance instance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
      Collections.<String, Object>singletonMap("var", "bValue"));
    ProcessInstance instance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
      Collections.<String, Object>singletonMap("var", "cValue"));

    // and some local variables on the tasks
    Task task1 = taskService.createTaskQuery().processInstanceId(instance1.getId()).singleResult();
    taskService.setVariableLocal(task1.getId(), "var", "cValue");

    Task task2 = taskService.createTaskQuery().processInstanceId(instance2.getId()).singleResult();
    taskService.setVariableLocal(task2.getId(), "var", "bValue");

    Task task3 = taskService.createTaskQuery().processInstanceId(instance3.getId()).singleResult();
    taskService.setVariableLocal(task3.getId(), "var", "aValue");

    // when I make a task query with ascending variable ordering by tasks variables
    List<Task> tasks = taskService.createTaskQuery()
      .processDefinitionKey("oneTaskProcess")
      .orderByTaskVariable("var", ValueType.STRING)
      .asc()
      .list();

    // then the tasks are ordered correctly by their local variables
    assertEquals(3, tasks.size());
    assertEquals(instance3.getId(), tasks.get(0).getProcessInstanceId());
    assertEquals(instance2.getId(), tasks.get(1).getProcessInstanceId());
    assertEquals(instance1.getId(), tasks.get(2).getProcessInstanceId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn")
  @Test
  public void testQueryResultOrderingByCaseInstanceVariables() {
    // given three tasks with String case instance variables
    CaseInstance instance1 = caseService.createCaseInstanceByKey("oneTaskCase",
        Collections.<String, Object>singletonMap("var", "cValue"));
    CaseInstance instance2 = caseService.createCaseInstanceByKey("oneTaskCase",
        Collections.<String, Object>singletonMap("var", "aValue"));
    CaseInstance instance3 = caseService.createCaseInstanceByKey("oneTaskCase",
        Collections.<String, Object>singletonMap("var", "bValue"));

    // when I make a task query with ascending variable ordering by tasks variables
    List<Task> tasks = taskService.createTaskQuery()
      .caseDefinitionKey("oneTaskCase")
      .orderByCaseInstanceVariable("var", ValueType.STRING)
      .asc()
      .list();

    // then the tasks are ordered correctly by their local variables
    assertEquals(3, tasks.size());
    assertEquals(instance2.getId(), tasks.get(0).getCaseInstanceId());
    assertEquals(instance3.getId(), tasks.get(1).getCaseInstanceId());
    assertEquals(instance1.getId(), tasks.get(2).getCaseInstanceId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithManualActivation.cmmn")
  @Test
  public void testQueryResultOrderingByCaseExecutionVariables() {
    // given three tasks with String case instance variables
    CaseInstance instance1 = caseService.createCaseInstanceByKey("oneTaskCase",
        Collections.<String, Object>singletonMap("var", "cValue"));
    CaseInstance instance2 = caseService.createCaseInstanceByKey("oneTaskCase",
        Collections.<String, Object>singletonMap("var", "aValue"));
    CaseInstance instance3 = caseService.createCaseInstanceByKey("oneTaskCase",
        Collections.<String, Object>singletonMap("var", "bValue"));

    // and local case execution variables
    CaseExecution caseExecution1 = caseService.createCaseExecutionQuery()
      .activityId("PI_HumanTask_1")
      .caseInstanceId(instance1.getId())
      .singleResult();

    caseService
      .withCaseExecution(caseExecution1.getId())
      .setVariableLocal("var", "aValue")
      .manualStart();

    CaseExecution caseExecution2 = caseService.createCaseExecutionQuery()
      .activityId("PI_HumanTask_1")
      .caseInstanceId(instance2.getId())
      .singleResult();

    caseService
      .withCaseExecution(caseExecution2.getId())
      .setVariableLocal("var", "bValue")
      .manualStart();

    CaseExecution caseExecution3 = caseService.createCaseExecutionQuery()
      .activityId("PI_HumanTask_1")
      .caseInstanceId(instance3.getId())
      .singleResult();

    caseService
      .withCaseExecution(caseExecution3.getId())
      .setVariableLocal("var", "cValue")
      .manualStart();

    // when I make a task query with ascending variable ordering by tasks variables
    List<Task> tasks = taskService.createTaskQuery()
        .caseDefinitionKey("oneTaskCase")
        .orderByCaseExecutionVariable("var", ValueType.STRING)
        .asc()
        .list();

    // then the tasks are ordered correctly by their local variables
    assertEquals(3, tasks.size());
    assertEquals(instance1.getId(), tasks.get(0).getCaseInstanceId());
    assertEquals(instance2.getId(), tasks.get(1).getCaseInstanceId());
    assertEquals(instance3.getId(), tasks.get(2).getCaseInstanceId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  @Test
  public void testQueryResultOrderingByVariablesWithNullValues() {
    // given three tasks with String process instance variables
    ProcessInstance instance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "bValue"));
    ProcessInstance instance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "cValue"));
    ProcessInstance instance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "aValue"));
    ProcessInstance instance4 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when I make a task query with variable ordering by String values
    List<Task> tasks = taskService.createTaskQuery()
        .processDefinitionKey("oneTaskProcess")
        .orderByProcessVariable("var", ValueType.STRING)
        .asc()
        .list();

    Task firstTask = tasks.get(0);

    // the null-valued task should be either first or last
    if (firstTask.getProcessInstanceId().equals(instance4.getId())) {
      // then the others in ascending order
      assertEquals(instance3.getId(), tasks.get(1).getProcessInstanceId());
      assertEquals(instance1.getId(), tasks.get(2).getProcessInstanceId());
      assertEquals(instance2.getId(), tasks.get(3).getProcessInstanceId());
    } else {
      assertEquals(instance3.getId(), tasks.get(0).getProcessInstanceId());
      assertEquals(instance1.getId(), tasks.get(1).getProcessInstanceId());
      assertEquals(instance2.getId(), tasks.get(2).getProcessInstanceId());
      assertEquals(instance4.getId(), tasks.get(3).getProcessInstanceId());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  @Test
  public void testQueryResultOrderingByVariablesWithMixedTypes() {
    // given three tasks with String and Integer process instance variables
    ProcessInstance instance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 42));
    ProcessInstance instance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "cValue"));
    ProcessInstance instance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "aValue"));

    // when I make a task query with variable ordering by String values
    List<Task> tasks = taskService.createTaskQuery()
      .processDefinitionKey("oneTaskProcess")
      .orderByProcessVariable("var", ValueType.STRING)
      .asc()
      .list();

    Task firstTask = tasks.get(0);

    // the numeric-valued task should be either first or last
    if (firstTask.getProcessInstanceId().equals(instance1.getId())) {
      // then the others in ascending order
      assertEquals(instance3.getId(), tasks.get(1).getProcessInstanceId());
      assertEquals(instance2.getId(), tasks.get(2).getProcessInstanceId());
    } else {
      assertEquals(instance3.getId(), tasks.get(0).getProcessInstanceId());
      assertEquals(instance2.getId(), tasks.get(1).getProcessInstanceId());
      assertEquals(instance1.getId(), tasks.get(2).getProcessInstanceId());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  @Test
  public void testQueryResultOrderingByStringVariableWithMixedCase() {
    // given three tasks with String and Integer process instance variables
    ProcessInstance instance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "a"));
    ProcessInstance instance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "B"));
    ProcessInstance instance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "c"));

    // when I make a task query with variable ordering by String values
    List<Task> tasks = taskService.createTaskQuery()
      .processDefinitionKey("oneTaskProcess")
      .orderByProcessVariable("var", ValueType.STRING)
      .asc()
      .list();

    // then the tasks are ordered correctly
    assertEquals(3, tasks.size());
    // first the numeric valued task (since it is treated like null-valued)
    assertEquals(instance1.getId(), tasks.get(0).getProcessInstanceId());
    // then the others in alphabetical order
    assertEquals(instance2.getId(), tasks.get(1).getProcessInstanceId());
    assertEquals(instance3.getId(), tasks.get(2).getProcessInstanceId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  @Test
  public void testQueryResultOrderingByVariablesOfAllPrimitiveTypes() {
    // given three tasks with String and Integer process instance variables
    ProcessInstance booleanInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", true));
    ProcessInstance shortInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", (short) 16));
    ProcessInstance longInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 500L));
    ProcessInstance intInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 400));
    ProcessInstance stringInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "300"));
    ProcessInstance dateInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", new Date(1000L)));
    ProcessInstance doubleInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 42.5d));

    // when I make a task query with variable ordering by String values
    List<Task> tasks = taskService.createTaskQuery()
      .processDefinitionKey("oneTaskProcess")
      .orderByProcessVariable("var", ValueType.BOOLEAN)
      .asc()
      .list();

    verifyFirstOrLastTask(tasks, booleanInstance);

    tasks = taskService.createTaskQuery()
      .processDefinitionKey("oneTaskProcess")
      .orderByProcessVariable("var", ValueType.SHORT)
      .asc()
      .list();

    verifyFirstOrLastTask(tasks, shortInstance);

    tasks = taskService.createTaskQuery()
      .processDefinitionKey("oneTaskProcess")
      .orderByProcessVariable("var", ValueType.LONG)
      .asc()
      .list();

    verifyFirstOrLastTask(tasks, longInstance);

    tasks = taskService.createTaskQuery()
      .processDefinitionKey("oneTaskProcess")
      .orderByProcessVariable("var", ValueType.INTEGER)
      .asc()
      .list();

    verifyFirstOrLastTask(tasks, intInstance);

    tasks = taskService.createTaskQuery()
      .processDefinitionKey("oneTaskProcess")
      .orderByProcessVariable("var", ValueType.STRING)
      .asc()
      .list();

    verifyFirstOrLastTask(tasks, stringInstance);

    tasks = taskService.createTaskQuery()
      .processDefinitionKey("oneTaskProcess")
      .orderByProcessVariable("var", ValueType.DATE)
      .asc()
      .list();

    verifyFirstOrLastTask(tasks, dateInstance);

    tasks = taskService.createTaskQuery()
      .processDefinitionKey("oneTaskProcess")
      .orderByProcessVariable("var", ValueType.DOUBLE)
      .asc()
      .list();

    verifyFirstOrLastTask(tasks, doubleInstance);
  }

  @Test
  public void testQueryByUnsupportedValueTypes() {
    try {
      taskService.createTaskQuery().orderByProcessVariable("var", ValueType.BYTES);
      fail("this type is not supported");
    } catch (ProcessEngineException e) {
      // happy path
      testRule.assertTextPresent("Cannot order by variables of type byte", e.getMessage());
    }

    try {
      taskService.createTaskQuery().orderByProcessVariable("var", ValueType.NULL);
      fail("this type is not supported");
    } catch (ProcessEngineException e) {
      // happy path
      testRule.assertTextPresent("Cannot order by variables of type null", e.getMessage());
    }

    try {
      taskService.createTaskQuery().orderByProcessVariable("var", ValueType.NUMBER);
      fail("this type is not supported");
    } catch (ProcessEngineException e) {
      // happy path
      testRule.assertTextPresent("Cannot order by variables of type number", e.getMessage());
    }

    try {
      taskService.createTaskQuery().orderByProcessVariable("var", ValueType.OBJECT);
      fail("this type is not supported");
    } catch (ProcessEngineException e) {
      // happy path
      testRule.assertTextPresent("Cannot order by variables of type object", e.getMessage());
    }

    try {
      taskService.createTaskQuery().orderByProcessVariable("var", ValueType.FILE);
      fail("this type is not supported");
    } catch (ProcessEngineException e) {
      // happy path
      testRule.assertTextPresent("Cannot order by variables of type file", e.getMessage());
    }
  }

  /**
   * verify that either the first or the last task of the list belong to the given process instance
   */
  protected void verifyFirstOrLastTask(List<Task> tasks, ProcessInstance belongingProcessInstance) {
    if (tasks.size() == 0) {
      fail("no tasks given");
    }

    int numTasks = tasks.size();
    boolean matches = tasks.get(0).getProcessInstanceId().equals(belongingProcessInstance.getId());
    matches = matches || tasks.get(numTasks - 1).getProcessInstanceId()
        .equals(belongingProcessInstance.getId());

    assertTrue("neither first nor last task belong to process instance " + belongingProcessInstance.getId(),
        matches);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  @Test
  public void testQueryResultOrderingByVariablesWithMixedTypesAndSameColumn() {
    // given three tasks with Integer and Long process instance variables
    ProcessInstance instance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 42));
    ProcessInstance instance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 800));
    ProcessInstance instance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 500L));

    // when I make a task query with variable ordering by String values
    List<Task> tasks = taskService.createTaskQuery()
        .processDefinitionKey("oneTaskProcess")
        .orderByProcessVariable("var", ValueType.INTEGER)
        .asc()
        .list();

    // then the tasks are ordered correctly
    assertEquals(3, tasks.size());

    Task firstTask = tasks.get(0);

    // the Long-valued task should be either first or last
    if (firstTask.getProcessInstanceId().equals(instance3.getId())) {
      // then the others in ascending order
      assertEquals(instance1.getId(), tasks.get(1).getProcessInstanceId());
      assertEquals(instance2.getId(), tasks.get(2).getProcessInstanceId());
    } else {
      assertEquals(instance1.getId(), tasks.get(0).getProcessInstanceId());
      assertEquals(instance2.getId(), tasks.get(1).getProcessInstanceId());
      assertEquals(instance3.getId(), tasks.get(2).getProcessInstanceId());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  @Test
  public void testQueryResultOrderingByTwoVariables() {
    // given three tasks with String process instance variables
    ProcessInstance bInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", "b").putValue("var2", 14));
    ProcessInstance bInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", "b").putValue("var2", 30));
    ProcessInstance cInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", "c").putValue("var2", 50));
    ProcessInstance cInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", "c").putValue("var2", 30));
    ProcessInstance aInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", "a").putValue("var2", 14));
    ProcessInstance aInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", "a").putValue("var2", 50));

    // when I make a task query with variable primary ordering by var values
    // and secondary ordering by var2 values
    List<Task> tasks = taskService.createTaskQuery()
        .processDefinitionKey("oneTaskProcess")
        .orderByProcessVariable("var", ValueType.STRING)
        .desc()
        .orderByProcessVariable("var2", ValueType.INTEGER)
        .asc()
        .list();

    // then the tasks are ordered correctly
    assertEquals(6, tasks.size());
    // var = c; var2 = 30
    assertEquals(cInstance2.getId(), tasks.get(0).getProcessInstanceId());
    // var = c; var2 = 50
    assertEquals(cInstance1.getId(), tasks.get(1).getProcessInstanceId());
    // var = b; var2 = 14
    assertEquals(bInstance1.getId(), tasks.get(2).getProcessInstanceId());
    // var = b; var2 = 30
    assertEquals(bInstance2.getId(), tasks.get(3).getProcessInstanceId());
    // var = a; var2 = 14
    assertEquals(aInstance1.getId(), tasks.get(4).getProcessInstanceId());
    // var = a; var2 = 50
    assertEquals(aInstance2.getId(), tasks.get(5).getProcessInstanceId());

    // when I make a task query with variable primary ordering by var2 values
    // and secondary ordering by var values
    tasks = taskService.createTaskQuery()
        .processDefinitionKey("oneTaskProcess")
        .orderByProcessVariable("var2", ValueType.INTEGER)
        .desc()
        .orderByProcessVariable("var", ValueType.STRING)
        .asc()
        .list();

    // then the tasks are ordered correctly
    assertEquals(6, tasks.size());
    // var = a; var2 = 50
    assertEquals(aInstance2.getId(), tasks.get(0).getProcessInstanceId());
    // var = c; var2 = 50
    assertEquals(cInstance1.getId(), tasks.get(1).getProcessInstanceId());
    // var = b; var2 = 30
    assertEquals(bInstance2.getId(), tasks.get(2).getProcessInstanceId());
    // var = c; var2 = 30
    assertEquals(cInstance2.getId(), tasks.get(3).getProcessInstanceId());
    // var = a; var2 = 14
    assertEquals(aInstance1.getId(), tasks.get(4).getProcessInstanceId());
    // var = b; var2 = 14
    assertEquals(bInstance1.getId(), tasks.get(5).getProcessInstanceId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  @Test
  public void testQueryResultOrderingByVariablesWithSecondaryOrderingByProcessInstanceId() {
    // given three tasks with String process instance variables
    ProcessInstance bInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", "b"));
    ProcessInstance bInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", "b"));
    ProcessInstance cInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", "c"));
    ProcessInstance cInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", "c"));
    ProcessInstance aInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", "a"));
    ProcessInstance aInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", "a"));

    // when I make a task query with variable ordering by String values
    List<Task> tasks = taskService.createTaskQuery()
        .processDefinitionKey("oneTaskProcess")
        .orderByProcessVariable("var", ValueType.STRING)
        .asc()
        .orderByProcessInstanceId()
        .asc()
        .list();

    // then the tasks are ordered correctly
    assertEquals(6, tasks.size());

    // var = a
    verifyTasksSortedByProcessInstanceId(Arrays.asList(aInstance1, aInstance2),
        tasks.subList(0, 2));

    // var = b
    verifyTasksSortedByProcessInstanceId(Arrays.asList(bInstance1, bInstance2),
        tasks.subList(2, 4));

    // var = c
    verifyTasksSortedByProcessInstanceId(Arrays.asList(cInstance1, cInstance2),
        tasks.subList(4, 6));
  }

  @Test
  public void testQueryResultOrderingWithInvalidParameters() {
    try {
      taskService.createTaskQuery().orderByProcessVariable(null, ValueType.STRING).asc().list();
      fail("should not succeed");
    } catch (NullValueException e) {
      // happy path
    }

    try {
      taskService.createTaskQuery().orderByProcessVariable("var", null).asc().list();
      fail("should not succeed");
    } catch (NullValueException e) {
      // happy path
    }

    try {
      taskService.createTaskQuery().orderByExecutionVariable(null, ValueType.STRING).asc().list();
      fail("should not succeed");
    } catch (NullValueException e) {
      // happy path
    }

    try {
      taskService.createTaskQuery().orderByExecutionVariable("var", null).asc().list();
      fail("should not succeed");
    } catch (NullValueException e) {
      // happy path
    }

    try {
      taskService.createTaskQuery().orderByTaskVariable(null, ValueType.STRING).asc().list();
      fail("should not succeed");
    } catch (NullValueException e) {
      // happy path
    }

    try {
      taskService.createTaskQuery().orderByTaskVariable("var", null).asc().list();
      fail("should not succeed");
    } catch (NullValueException e) {
      // happy path
    }

    try {
      taskService.createTaskQuery().orderByCaseInstanceVariable(null, ValueType.STRING).asc().list();
      fail("should not succeed");
    } catch (NullValueException e) {
      // happy path
    }

    try {
      taskService.createTaskQuery().orderByCaseInstanceVariable("var", null).asc().list();
      fail("should not succeed");
    } catch (NullValueException e) {
      // happy path
    }

    try {
      taskService.createTaskQuery().orderByCaseExecutionVariable(null, ValueType.STRING).asc().list();
      fail("should not succeed");
    } catch (NullValueException e) {
      // happy path
    }

    try {
      taskService.createTaskQuery().orderByCaseExecutionVariable("var", null).asc().list();
      fail("should not succeed");
    } catch (NullValueException e) {
      // happy path
    }
  }

  protected void verifyTasksSortedByProcessInstanceId(List<ProcessInstance> expectedProcessInstances,
      List<Task> actualTasks) {

    assertEquals(expectedProcessInstances.size(), actualTasks.size());
    List<ProcessInstance> instances = new ArrayList<>(expectedProcessInstances);

    Collections.sort(instances, new Comparator<ProcessInstance>() {
      public int compare(ProcessInstance p1, ProcessInstance p2) {
        return p1.getId().compareTo(p2.getId());
      }
    });

    for (int i = 0; i < instances.size(); i++) {
      assertEquals(instances.get(i).getId(), actualTasks.get(i).getProcessInstanceId());
    }
  }

  private void verifyQueryResults(TaskQuery query, int countExpected) {
    assertEquals(countExpected, query.list().size());
    assertEquals(countExpected, query.count());

    if (countExpected == 1) {
      assertNotNull(query.singleResult());
    } else if (countExpected > 1){
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertNull(query.singleResult());
    }
  }

  private void verifySingleResultFails(TaskQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/oneTaskWithFormKeyProcess.bpmn20.xml"})
  @Test
  public void testInitializeFormKeys() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // if initializeFormKeys
    Task task = taskService.createTaskQuery()
      .processInstanceId(processInstance.getId())
      .initializeFormKeys()
      .singleResult();

    // then the form key is present
    assertEquals("exampleFormKey", task.getFormKey());

    // if NOT initializeFormKeys
    task = taskService.createTaskQuery()
      .processInstanceId(processInstance.getId())
      .singleResult();

    try {
      // then the form key is not retrievable
      task.getFormKey();
      fail("exception expected.");
    } catch (BadUserRequestException e) {
      assertEquals("ENGINE-03052 The form key / form reference is not initialized. You must call initializeFormKeys() on the task query before you can retrieve the form key or the form reference.", e.getMessage());
    }
  }

  @Test
  @Deployment
  public void testInitializeFormKeysCamundaFormRef() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("formRefProcess");

    // if initializeFormKeys
    Task task = taskService.createTaskQuery()
        .processInstanceId(processInstance.getId())
        .initializeFormKeys()
        .singleResult();

    // then the form key is present
    CamundaFormRef camundaFormRef = task.getCamundaFormRef();
    assertThat(camundaFormRef.getKey()).isEqualTo("myForm");
    assertThat(camundaFormRef.getBinding()).isEqualTo("latest");
    assertThat(camundaFormRef.getVersion()).isNull();

    // if NOT initializeFormKeys
    Task task2 = taskService.createTaskQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();

    assertThatThrownBy(() -> {
      task2.getCamundaFormRef();
    }).isInstanceOf(BadUserRequestException.class)
    .hasMessage("ENGINE-03052 The form key / form reference is not initialized. You must call initializeFormKeys() on the task query before you can retrieve the form key or the form reference.");
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithFormKey.cmmn"})
  @Test
  public void testInitializeFormKeysForCaseInstance() {
    String caseDefinitionId = getCaseDefinitionId();

    CaseInstance caseInstance = caseService
      .withCaseDefinition(caseDefinitionId)
      .create();

    Task task = taskService.createTaskQuery().initializeFormKeys().caseInstanceId(caseInstance.getId()).singleResult();
    assertEquals("aFormKey", task.getFormKey());

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  @Test
  public void testQueryOrderByProcessVariableInteger() {
    ProcessInstance instance500 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", 500));
    ProcessInstance instance1000 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", 1000));
    ProcessInstance instance250 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables().putValue("var", 250));

    // asc
    List<Task> tasks = taskService.createTaskQuery()
      .processDefinitionKey("oneTaskProcess")
      .orderByProcessVariable("var", ValueType.INTEGER)
      .asc()
      .list();

    assertEquals(3, tasks.size());
    assertEquals(instance250.getId(), tasks.get(0).getProcessInstanceId());
    assertEquals(instance500.getId(), tasks.get(1).getProcessInstanceId());
    assertEquals(instance1000.getId(), tasks.get(2).getProcessInstanceId());

    // desc
    tasks = taskService.createTaskQuery()
      .processDefinitionKey("oneTaskProcess")
      .orderByProcessVariable("var", ValueType.INTEGER)
      .desc()
      .list();

    assertEquals(3, tasks.size());
    assertEquals(instance1000.getId(), tasks.get(0).getProcessInstanceId());
    assertEquals(instance500.getId(), tasks.get(1).getProcessInstanceId());
    assertEquals(instance250.getId(), tasks.get(2).getProcessInstanceId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  @Test
  public void testQueryOrderByTaskVariableInteger() {
    ProcessInstance instance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance instance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance instance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task task500 = taskService.createTaskQuery().processInstanceId(instance1.getId()).singleResult();
    taskService.setVariableLocal(task500.getId(), "var", 500);

    Task task250 = taskService.createTaskQuery().processInstanceId(instance2.getId()).singleResult();
    taskService.setVariableLocal(task250.getId(), "var", 250);

    Task task1000 = taskService.createTaskQuery().processInstanceId(instance3.getId()).singleResult();
    taskService.setVariableLocal(task1000.getId(), "var", 1000);

    // asc
    List<Task> tasks = taskService.createTaskQuery()
      .processDefinitionKey("oneTaskProcess")
      .orderByTaskVariable("var", ValueType.INTEGER)
      .asc()
      .list();

    assertEquals(3, tasks.size());
    assertEquals(task250.getId(), tasks.get(0).getId());
    assertEquals(task500.getId(), tasks.get(1).getId());
    assertEquals(task1000.getId(), tasks.get(2).getId());

    // desc
    tasks = taskService.createTaskQuery()
      .processDefinitionKey("oneTaskProcess")
      .orderByProcessVariable("var", ValueType.INTEGER)
      .desc()
      .list();

    assertEquals(3, tasks.size());
    assertEquals(task1000.getId(), tasks.get(0).getId());
    assertEquals(task500.getId(), tasks.get(1).getId());
    assertEquals(task250.getId(), tasks.get(2).getId());
  }

  @Test
  public void testQueryByParentTaskId() {
    String parentTaskId = "parentTask";
    Task parent = taskService.newTask(parentTaskId);
    taskService.saveTask(parent);

    Task sub1 = taskService.newTask("subTask1");
    sub1.setParentTaskId(parentTaskId);
    taskService.saveTask(sub1);

    Task sub2 = taskService.newTask("subTask2");
    sub2.setParentTaskId(parentTaskId);
    taskService.saveTask(sub2);

    TaskQuery query = taskService.createTaskQuery().taskParentTaskId(parentTaskId);

    verifyQueryResults(query, 2);

    taskService.deleteTask(parentTaskId, true);
  }

  @Test
  public void testExtendTaskQueryList_ProcessDefinitionKeyIn() {
    // given
    String processDefinitionKey = "invoice";
    TaskQuery query = taskService
        .createTaskQuery()
        .processDefinitionKeyIn(processDefinitionKey);

    TaskQuery extendingQuery = taskService.createTaskQuery();

    // when
    TaskQuery result = ((TaskQueryImpl)query).extend(extendingQuery);

    // then
    String[] processDefinitionKeys = ((TaskQueryImpl) result).getProcessDefinitionKeys();
    assertEquals(1, processDefinitionKeys.length);
    assertEquals(processDefinitionKey, processDefinitionKeys[0]);
  }

  @Test
  public void testExtendingTaskQueryList_ProcessDefinitionKeyIn() {
    // given
    String processDefinitionKey = "invoice";
    TaskQuery query = taskService.createTaskQuery();

    TaskQuery extendingQuery = taskService
        .createTaskQuery()
        .processDefinitionKeyIn(processDefinitionKey);

    // when
    TaskQuery result = ((TaskQueryImpl)query).extend(extendingQuery);

    // then
    String[] processDefinitionKeys = ((TaskQueryImpl) result).getProcessDefinitionKeys();
    assertEquals(1, processDefinitionKeys.length);
    assertEquals(processDefinitionKey, processDefinitionKeys[0]);
  }

  @Test
  public void testExtendTaskQueryList_TaskDefinitionKeyIn() {
    // given
    String taskDefinitionKey = "assigneApprover";
    TaskQuery query = taskService
        .createTaskQuery()
        .taskDefinitionKeyIn(taskDefinitionKey);

    TaskQuery extendingQuery = taskService.createTaskQuery();

    // when
    TaskQuery result = ((TaskQueryImpl)query).extend(extendingQuery);

    // then
    String[] key = ((TaskQueryImpl) result).getKeys();
    assertEquals(1, key.length);
    assertEquals(taskDefinitionKey, key[0]);
  }

  @Test
  public void testExtendingTaskQueryList_TaskDefinitionKeyIn() {
    // given
    String taskDefinitionKey = "assigneApprover";
    TaskQuery query = taskService.createTaskQuery();

    TaskQuery extendingQuery = taskService
        .createTaskQuery()
        .taskDefinitionKeyIn(taskDefinitionKey);

    // when
    TaskQuery result = ((TaskQueryImpl)query).extend(extendingQuery);

    // then
    String[] key = ((TaskQueryImpl) result).getKeys();
    assertEquals(1, key.length);
    assertEquals(taskDefinitionKey, key[0]);
  }

  @Test
  public void testQueryWithCandidateUsers() {
    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask()
        .camundaCandidateUsers("anna")
      .endEvent()
      .done();

   testRule.deploy(process);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    List<Task> tasks = taskService.createTaskQuery()
        .processInstanceId(processInstance.getId())
        .withCandidateUsers()
        .list();
    assertEquals(1, tasks.size());
  }

  @Test
  public void testQueryWithoutCandidateUsers() {
    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask()
        .camundaCandidateGroups("sales")
      .endEvent()
      .done();

   testRule.deploy(process);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    List<Task> tasks = taskService.createTaskQuery()
        .processInstanceId(processInstance.getId())
        .withoutCandidateUsers()
        .list();
    assertEquals(1, tasks.size());
  }

  @Test
  public void testQueryAssignedTasksWithCandidateUsers() {
    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask()
        .camundaCandidateGroups("sales")
      .endEvent()
      .done();

   testRule.deploy(process);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    try{
      taskService.createTaskQuery()
        .processInstanceId(processInstance.getId())
        .includeAssignedTasks()
        .withCandidateUsers()
        .list();
       fail("exception expected");
    } catch (ProcessEngineException e) {}
  }


  @Test
  public void testQueryAssignedTasksWithoutCandidateUsers() {
    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask()
        .camundaCandidateGroups("sales")
      .endEvent()
      .done();

   testRule.deploy(process);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    try{
       taskService.createTaskQuery()
        .processInstanceId(processInstance.getId())
        .includeAssignedTasks()
        .withoutCandidateUsers()
        .list();
       fail("exception expected");
    } catch (ProcessEngineException e) {}
  }

  @Test
  public void testQueryByNameNotEqual() {
    TaskQuery query = taskService.createTaskQuery().taskNameNotEqual("gonzo_Task");
    assertEquals(11, query.list().size());
  }

  @Test
  public void testQueryByNameNotLike() {
    TaskQuery query = taskService.createTaskQuery().taskNameNotLike("management%");
    assertEquals(9, query.list().size());
    assertEquals(9, query.count());

    query = taskService.createTaskQuery().taskNameNotLike("gonzo\\_%");
    assertEquals(11, query.list().size());
    assertEquals(11, query.count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  @Test
  public void testQueryByProcessInstanceIdIn() {
    // given three process instances
    String instance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
    String instance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when filter for two of them and non existing one
    List<Task> tasks = taskService.createTaskQuery().processInstanceIdIn(instance1, instance2, "nonexisting").list();

    // then
    assertThat(tasks.size()).isEqualTo(2);
    for (Task task : tasks) {
      assertThat(task.getProcessInstanceId()).isIn(instance1, instance2);
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  @Test
  public void testQueryByProcessInstanceIdInNonExisting() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    List<Task> tasks = taskService.createTaskQuery().processInstanceIdIn("nonexisting").list();

    // then
    assertThat(tasks.size()).isZero();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/task/TaskQueryTest.shouldContainCamundaFormRefIfInitialized.bpmn")
  @Test
  public void shouldContainCamundaFormRefIfInitialized() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskFormRefVersion").getId();

    // when
    List<Task> withoutFormKeys = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    List<Task> withFormKeys = taskService.createTaskQuery().processInstanceId(processInstanceId).initializeFormKeys().list();

    // then
    assertThat(withoutFormKeys).hasSize(1);
    assertThat(withFormKeys).hasSize(1);

    Task taskWithoutFormKey = withoutFormKeys.get(0);
    assertThatThrownBy(() -> {
      taskWithoutFormKey.getCamundaFormRef();
    }).isInstanceOf(BadUserRequestException.class)
    .hasMessage("ENGINE-03052 The form key / form reference is not initialized. You must call initializeFormKeys() on the task query before you can retrieve the form key or the form reference.");

    Task taskWithFormKey = withFormKeys.get(0);
    CamundaFormRef camundaFormRefWithFormKey = taskWithFormKey.getCamundaFormRef();

    assertThat(camundaFormRefWithFormKey).isNotNull();
    assertThat(camundaFormRefWithFormKey.getKey()).isEqualTo("key");
    assertThat(camundaFormRefWithFormKey.getBinding()).isEqualTo("version");
    assertThat(camundaFormRefWithFormKey.getVersion()).isEqualTo(3);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldFindTaskLastUpdatedNullUseCreateDate() {
    // given
    Date beforeStart = getBeforeCurrentTime();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    // no update to task, lastUpdated = null

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskUpdatedAfter(beforeStart).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isNull();
    assertThat(taskResult.getCreateTime()).isAfter(beforeStart);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldNotFindTaskLastUpdatedNullCreateDateBeforeQueryDate() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Date afterStart = getAfterCurrentTime();

    // when
    // no update to task, lastUpdated = null

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskUpdatedAfter(afterStart).singleResult();
    assertThat(taskResult).isNull();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldNotFindTaskLastUpdatedBeforeQueryDate() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Date lastUpdatedBeforeUpdate = task.getLastUpdated();
    task.setAssignee("myself");

    // assume
    assertThat(lastUpdatedBeforeUpdate).isNull();

    // when
    taskService.saveTask(task);

    // then
    Date afterUpdate = getAfterCurrentTime();
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskUpdatedAfter(afterUpdate).singleResult();
    assertThat(taskResult).isNull();
    taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isBefore(afterUpdate);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldNotFindTaskLastUpdatedEqualsQueryDate() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    task.setAssignee("myself");

    // when
    taskService.saveTask(task);

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isNotNull();
    taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskUpdatedAfter(taskResult.getLastUpdated()).singleResult();
    assertThat(taskResult).isNull();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldReturnResultsOrderedByLastUpdatedAsc() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Date beforeUpdates = getBeforeCurrentTime();

    Task task2 = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult();
    taskService.setOwner(task2.getId(), "myself");

    ClockUtil.offset(1000L);

    Task task1 = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult();
    taskService.setVariableLocal(task1.getId(), "myVar", "varVal");

    ClockUtil.offset(2000L);

    Task task3 = taskService.createTaskQuery().processInstanceId(processInstance3.getId()).singleResult();
    taskService.setPriority(task3.getId(), 3);

    // when
    List<Task> tasks = taskService.createTaskQuery()
        .processInstanceIdIn(processInstance1.getId(), processInstance2.getId(), processInstance3.getId())
        .taskUpdatedAfter(beforeUpdates).orderByLastUpdated().asc().list();

    // then
    assertThat(tasks).hasSize(3);
    assertThat(tasks).extracting("id").containsExactly(task2.getId(), task1.getId(), task3.getId());
    assertThat(tasks).extracting("lastUpdated").isSorted();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldReturnResultsOrderedByLastUpdatedDesc() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Date beforeUpdates = getBeforeCurrentTime();

    Task task2 = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult();
    taskService.setOwner(task2.getId(), "myself");

    ClockUtil.offset(1000L);

    Task task1 = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult();
    taskService.setVariableLocal(task1.getId(), "myVar", "varVal");

    ClockUtil.offset(2000L);

    Task task3 = taskService.createTaskQuery().processInstanceId(processInstance3.getId()).singleResult();
    taskService.setPriority(task3.getId(), 3);

    // when
    List<Task> tasks = taskService.createTaskQuery()
        .processInstanceIdIn(processInstance1.getId(), processInstance2.getId(), processInstance3.getId())
        .taskUpdatedAfter(beforeUpdates).orderByLastUpdated().desc().list();

    // then
    assertThat(tasks).hasSize(3);
    assertThat(tasks).extracting("id").containsExactly(task3.getId(), task1.getId(), task2.getId());
    assertThat(tasks).extracting("lastUpdated").isSortedAccordingTo(Collections.reverseOrder());
  }

  @Test
  public void shouldFindStandaloneTaskWithoutUpdateByLastUpdated() {
    // given
    Date beforeCreateTask = getBeforeCurrentTime();
    Task task = taskService.newTask("myTask");
    taskIds.add(task.getId());
    task.setAssignee("myself");
    Date beforeSave = getAfterCurrentTime();

    // when
    taskService.saveTask(task);

    // then
    Task taskResult = taskService.createTaskQuery().taskId("myTask").taskUpdatedAfter(beforeCreateTask).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isNull();
    assertThat(taskResult.getCreateTime()).isBefore(beforeSave);
  }

  @Test
  public void shouldFindStandaloneTaskWithUpdateByLastUpdated() {
    // given
    Task task = taskService.newTask("myTask");
    taskIds.add(task.getId());
    task.setAssignee("myself");
    Date beforeSave = getBeforeCurrentTime();
    taskService.saveTask(task);
    // make sure time passes between create and update
    ClockUtil.offset(1000L);

    // when
    taskService.setPriority(task.getId(), 2);

    // then
    Task taskResult = taskService.createTaskQuery().taskId("myTask").taskUpdatedAfter(beforeSave).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isAfter(beforeSave);
    assertThat(taskResult.getLastUpdated()).isAfter(taskResult.getCreateTime());
  }

  // ---------------------- HELPER ------------------------------

  // make sure that time passes between two fast operations
  private Date getAfterCurrentTime() {
    return new Date(ClockUtil.getCurrentTime().getTime() + 1000L);
  }

  //make sure that time passes between two fast operations
  private Date getBeforeCurrentTime() {
    return new Date(ClockUtil.getCurrentTime().getTime() - 1000L);
  }

  /**
   * Generates some test tasks.
   * - 6 tasks where kermit is a candidate
   * - 1 tasks where gonzo is assignee and kermit and gonzo are candidates
   * - 2 tasks assigned to management group
   * - 2 tasks assigned to accountancy group
   * - 1 task assigned to fozzie and to both the management and accountancy group
   */
  private List<String> generateTestTasks() throws Exception {
    List<String> ids = new ArrayList<>();

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
    // 6 tasks for kermit
    ClockUtil.setCurrentTime(sdf.parse("01/01/2001 01:01:01.000"));
    for (int i = 0; i < 6; i++) {
      Task task = taskService.newTask();
      task.setName("testTask");
      task.setDescription("testTask description");
      task.setPriority(3);
      taskService.saveTask(task);
      ids.add(task.getId());
      taskService.addCandidateUser(task.getId(), "kermit");
    }

    ClockUtil.setCurrentTime(sdf.parse("02/02/2002 02:02:02.000"));
    // 1 task for gonzo
    Task task = taskService.newTask();
    task.setName("gonzo_Task");
    task.setDescription("gonzo_description");
    task.setPriority(4);
    taskService.saveTask(task);
    taskService.setAssignee(task.getId(), "gonzo_");
    taskService.setVariable(task.getId(), "testVar", "someVariable");
    taskService.addCandidateUser(task.getId(), "kermit");
    taskService.addCandidateUser(task.getId(), "gonzo");
    ids.add(task.getId());

    ClockUtil.setCurrentTime(sdf.parse("03/03/2003 03:03:03.000"));
    // 2 tasks for management group
    for (int i = 0; i < 2; i++) {
      task = taskService.newTask();
      task.setName("managementTask");
      task.setPriority(10);
      taskService.saveTask(task);
      taskService.addCandidateGroup(task.getId(), "management");
      ids.add(task.getId());
    }

    ClockUtil.setCurrentTime(sdf.parse("04/04/2004 04:04:04.000"));
    // 2 tasks for accountancy group
    for (int i = 0; i < 2; i++) {
      task = taskService.newTask();
      task.setName("accountancyTask");
      task.setName("accountancy description");
      taskService.saveTask(task);
      taskService.addCandidateGroup(task.getId(), "accountancy");
      ids.add(task.getId());
    }

    ClockUtil.setCurrentTime(sdf.parse("05/05/2005 05:05:05.000"));
    // 1 task assigned to management and accountancy group
    task = taskService.newTask();
    task.setName("managementAndAccountancyTask");
    taskService.saveTask(task);
    taskService.setAssignee(task.getId(), "fozzie");
    taskService.addCandidateGroup(task.getId(), "management");
    taskService.addCandidateGroup(task.getId(), "accountancy");
    ids.add(task.getId());

    ClockUtil.reset();

    return ids;
  }

}
