/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.test.api.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.TaskQueryProperty;
import org.camunda.bpm.engine.impl.TaskQueryVariableValue;
import org.camunda.bpm.engine.impl.json.JsonTaskQueryConverter;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;

/**
 * @author Sebastian Menski
 */
public class FilterTaskQueryTest extends PluggableProcessEngineTestCase {

  protected Filter filter;

  protected String testString = "test";
  protected Integer testInteger = 1;
  protected DelegationState testDelegationState = DelegationState.PENDING;
  protected Date testDate = new Date(0);
  protected String[] testActivityInstances = new String[] {"a", "b", "c"};
  protected List<String> testCandidateGroups = new ArrayList<String>();

  protected String[] variableNames = new String[] {"a", "b", "c", "d", "e", "f"};
  protected Object[] variableValues = new Object[] {1, 2, "3", "4", 5, 6};
  protected QueryOperator[] variableOperators = new QueryOperator[] {QueryOperator.EQUALS, QueryOperator.GREATER_THAN_OR_EQUAL, QueryOperator.LESS_THAN, QueryOperator.LIKE, QueryOperator.NOT_EQUALS, QueryOperator.LESS_THAN_OR_EQUAL};
  protected boolean[] isTaskVariable = new boolean[] {true, true, false, false, false, false};
  protected boolean[] isProcessVariable = new boolean[] {false, false, true, true, false, false};
  protected String testOrderBy = TaskQueryProperty.EXECUTION_ID.getName() + " " + Direction.DESCENDING.getName() + ", " + TaskQueryProperty.DUE_DATE.getName() + " " + Direction.ASCENDING.getName();
  protected User testUser;
  protected Group testGroup;

  protected JsonTaskQueryConverter queryConverter;

  public void setUp() {
    filter = filterService.newTaskFilter("name").setOwner("owner").setQuery("{}").setProperties("properties");
    testUser = identityService.newUser("user");
    testGroup = identityService.newGroup("group");
    identityService.saveUser(testUser);
    identityService.saveGroup(testGroup);
    identityService.createMembership(testUser.getId(), testGroup.getId());

    Group anotherGroup = identityService.newGroup("anotherGroup");
    identityService.saveGroup(anotherGroup);
    testCandidateGroups.add(testGroup.getId());
    testCandidateGroups.add(anotherGroup.getId());

    createTasks();

    queryConverter = new JsonTaskQueryConverter();
  }

  public void tearDown() {
    for (Filter filter : filterService.createTaskFilterQuery().list()) {
      filterService.deleteFilter(filter.getId());
    }
    for (Group group : identityService.createGroupQuery().list()) {
      identityService.deleteGroup(group.getId());
    }
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Task task : taskService.createTaskQuery().list()) {
      taskService.deleteTask(task.getId(), true);
    }
  }

  public void testEmptyQuery() {
    TaskQuery emptyQuery = taskService.createTaskQuery();
    String emptyQueryJson = "{}";

    filter.setQuery(emptyQuery);

    assertEquals("{}", filter.getQuery());
    assertNotNull(filter.getTypeQuery());

    filter.setQuery(emptyQueryJson);

    assertEquals("{}", filter.getQuery());
    assertNotNull(filter.getTypeQuery());
  }

  public void testTaskQuery() {
    // create query
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskId(testString);
    query.taskName(testString);
    query.taskNameLike(testString);
    query.taskDescription(testString);
    query.taskDescriptionLike(testString);
    query.taskPriority(testInteger);
    query.taskMinPriority(testInteger);
    query.taskMaxPriority(testInteger);
    query.taskAssignee(testString);
    query.taskAssigneeLike(testString);
    query.taskInvolvedUser(testString);
    query.taskOwner(testString);
    query.taskUnassigned();
    query.taskDelegationState(testDelegationState);
    query.taskCandidateGroupIn(testCandidateGroups);
    query.processInstanceId(testString);
    query.executionId(testString);
    query.activityInstanceIdIn(testActivityInstances);
    query.taskCreatedOn(testDate);
    query.taskCreatedBefore(testDate);
    query.taskCreatedAfter(testDate);
    query.taskDefinitionKey(testString);
    query.taskDefinitionKeyLike(testString);
    query.processDefinitionKey(testString);
    query.processDefinitionId(testString);
    query.processDefinitionName(testString);
    query.processDefinitionNameLike(testString);
    query.processInstanceBusinessKey(testString);
    query.processInstanceBusinessKeyLike(testString);

    // variables
    query.taskVariableValueEquals(variableNames[0], variableValues[0]);
    query.taskVariableValueGreaterThanOrEquals(variableNames[1], variableValues[1]);
    query.processVariableValueLessThan(variableNames[2], variableValues[2]);
    query.processVariableValueLike(variableNames[3], (String) variableValues[3]);
    query.caseInstanceVariableValueNotEquals(variableNames[4], variableValues[4]);
    query.caseInstanceVariableValueLessThanOrEquals(variableNames[5], variableValues[5]);

    query.dueDate(testDate);
    query.dueBefore(testDate);
    query.dueAfter(testDate);
    query.followUpDate(testDate);
    query.followUpBefore(testDate);
    query.followUpAfter(testDate);
    query.excludeSubtasks();
    query.suspended();
    query.caseDefinitionKey(testString);
    query.caseDefinitionId(testString);
    query.caseDefinitionName(testString);
    query.caseDefinitionNameLike(testString);
    query.caseInstanceId(testString);
    query.caseInstanceBusinessKey(testString);
    query.caseInstanceBusinessKeyLike(testString);
    query.caseExecutionId(testString);

    // ordering
    query.orderByExecutionId().desc();
    query.orderByDueDate().asc();

    // save filter
    filter.setQuery(query);
    filterService.saveFilter(filter);

    // fetch from db
    filter = filterService.createTaskFilterQuery().singleResult();

    // test query
    query = filter.getTypeQuery();
    assertEquals(testString, query.getTaskId());
    assertEquals(testString, query.getName());
    assertEquals(testString, query.getNameLike());
    assertEquals(testString, query.getDescription());
    assertEquals(testString, query.getDescriptionLike());
    assertEquals(testInteger, query.getPriority());
    assertEquals(testInteger, query.getMinPriority());
    assertEquals(testInteger, query.getMaxPriority());
    assertEquals(testString, query.getAssignee());
    assertEquals(testString, query.getAssigneeLike());
    assertEquals(testString, query.getInvolvedUser());
    assertEquals(testString, query.getOwner());
    assertTrue(query.isUnassigned());
    assertEquals(testDelegationState, query.getDelegationState());
    assertEquals(testCandidateGroups, query.getCandidateGroups());
    assertEquals(testString, query.getProcessInstanceId());
    assertEquals(testString, query.getExecutionId());
    assertEquals(testActivityInstances.length, query.getActivityInstanceIdIn().length);
    for (int i = 0; i < query.getActivityInstanceIdIn().length; i++) {
      assertEquals(testActivityInstances[i], query.getActivityInstanceIdIn()[i]);
    }
    assertEquals(testDate, query.getCreateTime());
    assertEquals(testDate, query.getCreateTimeBefore());
    assertEquals(testDate, query.getCreateTimeAfter());
    assertEquals(testString, query.getKey());
    assertEquals(testString, query.getKeyLike());
    assertEquals(testString, query.getProcessDefinitionKey());
    assertEquals(testString, query.getProcessDefinitionId());
    assertEquals(testString, query.getProcessDefinitionName());
    assertEquals(testString, query.getProcessDefinitionNameLike());
    assertEquals(testString, query.getProcessInstanceBusinessKey());
    assertEquals(testString, query.getProcessInstanceBusinessKeyLike());

    // variables
    List<TaskQueryVariableValue> variables = query.getVariables();
    for (int i = 0; i < variables.size(); i++) {
      TaskQueryVariableValue variable = variables.get(i);
      assertEquals(variableNames[i], variable.getName());
      assertEquals(variableValues[i], variable.getValue());
      assertEquals(variableOperators[i].toString(), variable.getOperator());
      assertEquals(isTaskVariable[i], variable.isLocal());
      assertEquals(isProcessVariable[i], variable.isProcessInstanceVariable());
    }

    assertEquals(testDate, query.getDueDate());
    assertEquals(testDate, query.getDueBefore());
    assertEquals(testDate, query.getDueAfter());
    assertEquals(testDate, query.getFollowUpDate());
    assertEquals(testDate, query.getFollowUpBefore());
    assertEquals(testDate, query.getFollowUpAfter());
    assertTrue(query.isExcludeSubtasks());
    assertEquals(SuspensionState.SUSPENDED, query.getSuspensionState());
    assertEquals(testString, query.getCaseDefinitionKey());
    assertEquals(testString, query.getCaseDefinitionId());
    assertEquals(testString, query.getCaseDefinitionName());
    assertEquals(testString, query.getCaseDefinitionNameLike());
    assertEquals(testString, query.getCaseInstanceId());
    assertEquals(testString, query.getCaseInstanceBusinessKey());
    assertEquals(testString, query.getCaseInstanceBusinessKeyLike());
    assertEquals(testString, query.getCaseExecutionId());

    // ordering
    assertEquals(testOrderBy, query.getOrderBy());
  }

  public void testTaskQueryCandidateUser() {
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskCandidateUser(testUser.getId());

    filter.setQuery(query);
    query = filter.getTypeQuery();

    assertEquals(testUser.getId(), query.getCandidateUser());
  }

  public void testTaskQueryCandidateGroup() {
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskCandidateGroup(testGroup.getId());

    filter.setQuery(query);
    query = filter.getTypeQuery();

    assertEquals(testGroup.getId(), query.getCandidateGroup());
  }

  public void testExecuteTaskQueryList() {
    TaskQuery query = taskService.createTaskQuery();
    query.taskNameLike("Task%");

    saveQuery(query);

    List<Task> tasks = filterService.list(filter.getId());
    assertEquals(3, tasks.size());
    for (Task task : tasks) {
      assertEquals(testUser.getId(), task.getOwner());
    }
  }

  public void testExtendingTaskQueryList() {
    TaskQuery query = taskService.createTaskQuery();

    saveQuery(query);

    List<Task> tasks = filterService.list(filter.getId());
    assertEquals(3, tasks.size());

    tasks = filterService.list(filter.getId(), query);
    assertEquals(3, tasks.size());

    TaskQuery extendingQuery = taskService.createTaskQuery();

    extendingQuery.taskDelegationState(DelegationState.RESOLVED);

    tasks = filterService.list(filter.getId(), extendingQuery);
    assertEquals(2, tasks.size());

    for (Task task : tasks) {
      assertEquals(DelegationState.RESOLVED, task.getDelegationState());
    }

    String extendingQueryJson = queryConverter.toJson(extendingQuery);

    tasks = filterService.list(filter.getId(), extendingQueryJson);
    assertEquals(2, tasks.size());

    for (Task task : tasks) {
      assertEquals(DelegationState.RESOLVED, task.getDelegationState());
    }
  }

  public void testExecuteTaskQueryListPage() {
    TaskQuery query = taskService.createTaskQuery();
    query.taskNameLike("Task%");

    saveQuery(query);

    List<Task> tasks = filterService.listPage(filter.getId(), 1, 2);
    assertEquals(2, tasks.size());
    for (Task task : tasks) {
      assertEquals(testUser.getId(), task.getOwner());
    }
  }

  public void testExtendingTaskQueryListPage() {
    TaskQuery query = taskService.createTaskQuery();

    saveQuery(query);

    List<Task> tasks = filterService.listPage(filter.getId(), 1, 2);
    assertEquals(2, tasks.size());

    tasks = filterService.listPage(filter.getId(), query, 1, 2);
    assertEquals(2, tasks.size());

    TaskQuery extendingQuery = taskService.createTaskQuery();

    extendingQuery.taskDelegationState(DelegationState.RESOLVED);

    tasks = filterService.listPage(filter.getId(), extendingQuery, 1, 2);
    assertEquals(1, tasks.size());

    assertEquals(DelegationState.RESOLVED, tasks.get(0).getDelegationState());

    String extendingQueryJson = queryConverter.toJson(extendingQuery);

    tasks = filterService.listPage(filter.getId(), extendingQueryJson, 1, 2);
    assertEquals(1, tasks.size());

    assertEquals(DelegationState.RESOLVED, tasks.get(0).getDelegationState());
  }

  public void testExecuteTaskQuerySingleResult() {
    TaskQuery query = taskService.createTaskQuery();
    query.taskDelegationState(DelegationState.PENDING);

    saveQuery(query);

    Task task = filterService.singleResult(filter.getId());
    assertNotNull(task);
    assertEquals("Task 1", task.getName());
  }

  public void testFailTaskQuerySingleResult() {
    TaskQuery query = taskService.createTaskQuery();

    saveQuery(query);

    try {
      filterService.singleResult(filter.getId());
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      // expected
    }
  }

  public void testExtendingTaskQuerySingleResult() {
    TaskQuery query = taskService.createTaskQuery();
    query.taskDelegationState(DelegationState.PENDING);

    saveQuery(query);

    Task task = filterService.singleResult(filter.getId());
    assertNotNull(task);
    assertEquals("Task 1", task.getName());
    assertEquals("task1", task.getId());

    task = filterService.singleResult(filter.getId(), query);
    assertNotNull(task);
    assertEquals("Task 1", task.getName());
    assertEquals("task1", task.getId());

    TaskQuery extendingQuery = taskService.createTaskQuery();

    extendingQuery.taskId("task1");

    task = filterService.singleResult(filter.getId(), extendingQuery);
    assertNotNull(task);
    assertEquals("Task 1", task.getName());
    assertEquals("task1", task.getId());

    String extendingQueryJson = queryConverter.toJson(extendingQuery);

    task = filterService.singleResult(filter.getId(), extendingQueryJson);
    assertNotNull(task);
    assertEquals("Task 1", task.getName());
    assertEquals("task1", task.getId());
  }

  public void testExecuteTaskQueryCount() {
    TaskQuery query = taskService.createTaskQuery();

    saveQuery(query);

    long count = filterService.count(filter.getId());
    assertEquals(3, count);

    query.taskDelegationState(DelegationState.RESOLVED);

    saveQuery(query);

    count = filterService.count(filter.getId());
    assertEquals(2, count);
  }

  public void testExtendingTaskQueryCount() {
    TaskQuery query = taskService.createTaskQuery();

    saveQuery(query);

    TaskQuery extendingQuery = taskService.createTaskQuery();

    extendingQuery.taskId("task3");

    long count = filterService.count(filter.getId());

    assertEquals(3, count);

    count = filterService.count(filter.getId(), query);

    assertEquals(3, count);

    count = filterService.count(filter.getId(), extendingQuery);

    assertEquals(1, count);

    String extendingQueryJson = queryConverter.toJson(extendingQuery);

    count = filterService.count(filter.getId(), extendingQueryJson);

    assertEquals(1, count);
  }

  public void testSpecialExtendingQuery() {
    TaskQuery query = taskService.createTaskQuery();

    saveQuery(query);

    long count = filterService.count(filter.getId(), (String) null);
    assertEquals(3, count);

    count = filterService.count(filter.getId(), (Query) null);
    assertEquals(3, count);

    count = filterService.count(filter.getId(), "");
    assertEquals(3, count);

    try {
      filterService.count(filter.getId(), "abc");
      fail("Exception expected");
    }
    catch (NotValidException e) {
      // expected
    }
  }

  public void testExtendingSorting() {
    String sortByNameAsc = TaskQueryProperty.NAME.getName() + " " + Direction.ASCENDING.getName();
    String sortByAssigneeDescJson = "{\"sortBy\": \"assignee\", \"sortOrder\": \"desc\"}";
    String sortByAssigneeDesc = TaskQueryProperty.ASSIGNEE.getName() + " " + Direction.DESCENDING.getName();

    // create empty query
    TaskQueryImpl query = (TaskQueryImpl) taskService.createTaskQuery();
    saveQuery(query);

    // assert default sorting
    query = filter.getTypeQuery();
    String orderBy = query.getOrderBy();
    assertEquals(AbstractQuery.DEFAULT_ORDER_BY, orderBy);

    // extend query by new task query with sorting
    TaskQuery sortQuery = taskService.createTaskQuery().orderByTaskName().asc();
    Filter extendedFilter = filter.extend(sortQuery);
    query = extendedFilter.getTypeQuery();
    orderBy = query.getOrderBy();

    assertEquals(sortByNameAsc, orderBy);

    // extend query by new json query with sorting
    extendedFilter = extendedFilter.extend(sortByAssigneeDescJson);
    query = extendedFilter.getTypeQuery();
    orderBy = query.getOrderBy();

    assertEquals(sortByNameAsc + ", " + sortByAssigneeDesc, orderBy);

    // extend query by incomplete sorting query (sorting should not change)
    sortQuery = taskService.createTaskQuery().orderByCaseExecutionId();
    extendedFilter = extendedFilter.extend(sortQuery);
    query = extendedFilter.getTypeQuery();
    orderBy = query.getOrderBy();

    assertEquals(sortByNameAsc + ", " + sortByAssigneeDesc, orderBy);

    // extend query with invalid order attribute
    try {
      extendedFilter.extend("{\"sortOrder\": \"abc\"}");
      fail("Exception expected");
    }
    catch (NotValidException e) {
      // expected
    }

    // extend query with missing sortBy attribute
    try {
      extendedFilter.extend("{\"sortOrder\": \"asc\"}");
      fail("Exception expected");
    }
    catch (NotValidException e) {
      // expected
    }
  }

  protected void saveQuery(Query query) {
    filter.setQuery(query);
    filterService.saveFilter(filter);
    filter = filterService.getFilter(filter.getId());
  }

  protected void createTasks() {
    Task task = taskService.newTask("task1");
    task.setName("Task 1");
    task.setOwner(testUser.getId());
    task.setDelegationState(DelegationState.PENDING);
    taskService.saveTask(task);

    task = taskService.newTask("task2");
    task.setName("Task 2");
    task.setOwner(testUser.getId());
    task.setDelegationState(DelegationState.RESOLVED);
    taskService.saveTask(task);

    task = taskService.newTask("task3");
    task.setName("Task 3");
    task.setOwner(testUser.getId());
    task.setDelegationState(DelegationState.RESOLVED);
    taskService.saveTask(task);
  }

}
