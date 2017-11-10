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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.QueryEntityRelationCondition;
import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.TaskQueryProperty;
import org.camunda.bpm.engine.impl.TaskQueryVariableValue;
import org.camunda.bpm.engine.impl.VariableOrderProperty;
import org.camunda.bpm.engine.impl.json.JsonTaskQueryConverter;
import org.camunda.bpm.engine.impl.persistence.entity.FilterEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

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
  protected String[] testKeys = new String[] {"d", "e"};
  protected List<String> testCandidateGroups = new ArrayList<String>();

  protected String[] variableNames = new String[] {"a", "b", "c", "d", "e", "f"};
  protected Object[] variableValues = new Object[] {1, 2, "3", "4", 5, 6};
  protected QueryOperator[] variableOperators = new QueryOperator[] {QueryOperator.EQUALS, QueryOperator.GREATER_THAN_OR_EQUAL, QueryOperator.LESS_THAN, QueryOperator.LIKE, QueryOperator.NOT_EQUALS, QueryOperator.LESS_THAN_OR_EQUAL};
  protected boolean[] isTaskVariable = new boolean[] {true, true, false, false, false, false};
  protected boolean[] isProcessVariable = new boolean[] {false, false, true, true, false, false};
  protected User testUser;
  protected Group testGroup;

  protected JsonTaskQueryConverter queryConverter;

  @Override
  public void setUp() {
    filter = filterService.newTaskFilter("name").setOwner("owner").setQuery(taskService.createTaskQuery()).setProperties(new HashMap<String, Object>());
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

  @Override
  public void tearDown() {
    processEngineConfiguration.setEnableExpressionsInAdhocQueries(false);

    Mocks.reset();

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
      if (task.getProcessInstanceId() == null) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }

  public void testEmptyQuery() {
    TaskQuery emptyQuery = taskService.createTaskQuery();
    String emptyQueryJson = "{}";

    filter.setQuery(emptyQuery);

    assertEquals(emptyQueryJson, ((FilterEntity) filter).getQueryInternal());
    assertNotNull(filter.getQuery());
  }

  public void testTaskQuery() {
    // create query
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskId(testString);
    query.taskName(testString);
    query.taskNameNotEqual(testString);
    query.taskNameLike(testString);
    query.taskNameNotLike(testString);
    query.taskDescription(testString);
    query.taskDescriptionLike(testString);
    query.taskPriority(testInteger);
    query.taskMinPriority(testInteger);
    query.taskMaxPriority(testInteger);
    query.taskAssignee(testString);
    query.taskAssigneeExpression(testString);
    query.taskAssigneeLike(testString);
    query.taskAssigneeLikeExpression(testString);
    query.taskInvolvedUser(testString);
    query.taskInvolvedUserExpression(testString);
    query.taskOwner(testString);
    query.taskOwnerExpression(testString);
    query.taskUnassigned();
    query.taskAssigned();
    query.taskDelegationState(testDelegationState);
    query.taskCandidateGroupIn(testCandidateGroups);
    query.taskCandidateGroupInExpression(testString);
    query.withCandidateGroups();
    query.withoutCandidateGroups();
    query.withCandidateUsers();
    query.withoutCandidateUsers();
    query.processInstanceId(testString);
    query.executionId(testString);
    query.activityInstanceIdIn(testActivityInstances);
    query.taskCreatedOn(testDate);
    query.taskCreatedOnExpression(testString);
    query.taskCreatedBefore(testDate);
    query.taskCreatedBeforeExpression(testString);
    query.taskCreatedAfter(testDate);
    query.taskCreatedAfterExpression(testString);
    query.taskDefinitionKey(testString);
    query.taskDefinitionKeyIn(testKeys);
    query.taskDefinitionKeyLike(testString);
    query.processDefinitionKey(testString);
    query.processDefinitionKeyIn(testKeys);
    query.processDefinitionId(testString);
    query.processDefinitionName(testString);
    query.processDefinitionNameLike(testString);
    query.processInstanceBusinessKey(testString);
    query.processInstanceBusinessKeyExpression(testString);
    query.processInstanceBusinessKeyIn(testKeys);
    query.processInstanceBusinessKeyLike(testString);
    query.processInstanceBusinessKeyLikeExpression(testString);

    // variables
    query.taskVariableValueEquals(variableNames[0], variableValues[0]);
    query.taskVariableValueGreaterThanOrEquals(variableNames[1], variableValues[1]);
    query.processVariableValueLessThan(variableNames[2], variableValues[2]);
    query.processVariableValueLike(variableNames[3], (String) variableValues[3]);
    query.caseInstanceVariableValueNotEquals(variableNames[4], variableValues[4]);
    query.caseInstanceVariableValueLessThanOrEquals(variableNames[5], variableValues[5]);

    query.dueDate(testDate);
    query.dueDateExpression(testString);
    query.dueBefore(testDate);
    query.dueBeforeExpression(testString);
    query.dueAfter(testDate);
    query.dueAfterExpression(testString);
    query.followUpDate(testDate);
    query.followUpDateExpression(testString);
    query.followUpBefore(testDate);
    query.followUpBeforeExpression(testString);
    query.followUpAfter(testDate);
    query.followUpAfterExpression(testString);
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
    query.orderByProcessVariable("var", ValueType.STRING).desc();

    List<QueryOrderingProperty> expectedOrderingProperties = query.getOrderingProperties();

    // save filter
    filter.setQuery(query);
    filterService.saveFilter(filter);

    // fetch from db
    filter = filterService.createTaskFilterQuery().singleResult();

    // test query
    query = filter.getQuery();
    assertEquals(testString, query.getTaskId());
    assertEquals(testString, query.getName());
    assertEquals(testString, query.getNameNotEqual());
    assertEquals(testString, query.getNameNotLike());
    assertEquals(testString, query.getNameLike());
    assertEquals(testString, query.getDescription());
    assertEquals(testString, query.getDescriptionLike());
    assertEquals(testInteger, query.getPriority());
    assertEquals(testInteger, query.getMinPriority());
    assertEquals(testInteger, query.getMaxPriority());
    assertEquals(testString, query.getAssignee());
    assertEquals(testString, query.getExpressions().get("taskAssignee"));
    assertEquals(testString, query.getAssigneeLike());
    assertEquals(testString, query.getExpressions().get("taskAssigneeLike"));
    assertEquals(testString, query.getInvolvedUser());
    assertEquals(testString, query.getExpressions().get("taskInvolvedUser"));
    assertEquals(testString, query.getOwner());
    assertEquals(testString, query.getExpressions().get("taskOwner"));
    assertTrue(query.isUnassigned());
    assertTrue(query.isAssigned());
    assertEquals(testDelegationState, query.getDelegationState());
    assertEquals(testCandidateGroups, query.getCandidateGroups());
    assertTrue(query.isWithCandidateGroups());
    assertTrue(query.isWithoutCandidateGroups());
    assertTrue(query.isWithCandidateUsers());
    assertTrue(query.isWithoutCandidateUsers());
    assertEquals(testString, query.getExpressions().get("taskCandidateGroupIn"));
    assertEquals(testString, query.getProcessInstanceId());
    assertEquals(testString, query.getExecutionId());
    assertEquals(testActivityInstances.length, query.getActivityInstanceIdIn().length);
    for (int i = 0; i < query.getActivityInstanceIdIn().length; i++) {
      assertEquals(testActivityInstances[i], query.getActivityInstanceIdIn()[i]);
    }
    assertEquals(testDate, query.getCreateTime());
    assertEquals(testString, query.getExpressions().get("taskCreatedOn"));
    assertEquals(testDate, query.getCreateTimeBefore());
    assertEquals(testString, query.getExpressions().get("taskCreatedBefore"));
    assertEquals(testDate, query.getCreateTimeAfter());
    assertEquals(testString, query.getExpressions().get("taskCreatedAfter"));
    assertEquals(testString, query.getKey());
    assertEquals(testKeys.length, query.getKeys().length);
    for (int i = 0; i < query.getKeys().length; i++) {
      assertEquals(testKeys[i], query.getKeys()[i]);
    }
    assertEquals(testString, query.getKeyLike());
    assertEquals(testString, query.getProcessDefinitionKey());
    for (int i = 0; i < query.getProcessDefinitionKeys().length; i++) {
      assertEquals(testKeys[i], query.getProcessDefinitionKeys()[i]);
    }
    assertEquals(testString, query.getProcessDefinitionId());
    assertEquals(testString, query.getProcessDefinitionName());
    assertEquals(testString, query.getProcessDefinitionNameLike());
    assertEquals(testString, query.getProcessInstanceBusinessKey());
    assertEquals(testString, query.getExpressions().get("processInstanceBusinessKey"));
    for (int i = 0; i < query.getProcessInstanceBusinessKeys().length; i++) {
      assertEquals(testKeys[i], query.getProcessInstanceBusinessKeys()[i]);
    }
    assertEquals(testString, query.getProcessInstanceBusinessKeyLike());
    assertEquals(testString, query.getExpressions().get("processInstanceBusinessKeyLike"));

    // variables
    List<TaskQueryVariableValue> variables = query.getVariables();
    for (int i = 0; i < variables.size(); i++) {
      TaskQueryVariableValue variable = variables.get(i);
      assertEquals(variableNames[i], variable.getName());
      assertEquals(variableValues[i], variable.getValue());
      assertEquals(variableOperators[i], variable.getOperator());
      assertEquals(isTaskVariable[i], variable.isLocal());
      assertEquals(isProcessVariable[i], variable.isProcessInstanceVariable());
    }

    assertEquals(testDate, query.getDueDate());
    assertEquals(testString, query.getExpressions().get("dueDate"));
    assertEquals(testDate, query.getDueBefore());
    assertEquals(testString, query.getExpressions().get("dueBefore"));
    assertEquals(testDate, query.getDueAfter());
    assertEquals(testString, query.getExpressions().get("dueAfter"));
    assertEquals(testDate, query.getFollowUpDate());
    assertEquals(testString, query.getExpressions().get("followUpDate"));
    assertEquals(testDate, query.getFollowUpBefore());
    assertEquals(testString, query.getExpressions().get("followUpBefore"));
    assertEquals(testDate, query.getFollowUpAfter());
    assertEquals(testString, query.getExpressions().get("followUpAfter"));
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
    verifyOrderingProperties(expectedOrderingProperties, query.getOrderingProperties());
  }

  protected void verifyOrderingProperties(List<QueryOrderingProperty> expectedProperties,
      List<QueryOrderingProperty> actualProperties) {
    assertEquals(expectedProperties.size(), actualProperties.size());

    for (int i = 0; i < expectedProperties.size(); i++) {
      QueryOrderingProperty expectedProperty = expectedProperties.get(i);
      QueryOrderingProperty actualProperty = actualProperties.get(i);

      assertEquals(expectedProperty.getRelation(), actualProperty.getRelation());
      assertEquals(expectedProperty.getDirection(), actualProperty.getDirection());
      assertEquals(expectedProperty.isContainedProperty(), actualProperty.isContainedProperty());
      assertEquals(expectedProperty.getQueryProperty(), actualProperty.getQueryProperty());

      List<QueryEntityRelationCondition> expectedRelationConditions = expectedProperty.getRelationConditions();
      List<QueryEntityRelationCondition> actualRelationConditions = expectedProperty.getRelationConditions();

      if (expectedRelationConditions != null && actualRelationConditions != null) {
        assertEquals(expectedRelationConditions.size(), actualRelationConditions.size());

        for (int j = 0; j < expectedRelationConditions.size(); j++) {
          QueryEntityRelationCondition expectedFilteringProperty = expectedRelationConditions.get(j);
          QueryEntityRelationCondition actualFilteringProperty = expectedRelationConditions.get(j);

          assertEquals(expectedFilteringProperty.getProperty(), actualFilteringProperty.getProperty());
          assertEquals(expectedFilteringProperty.getComparisonProperty(), actualFilteringProperty.getComparisonProperty());
          assertEquals(expectedFilteringProperty.getScalarValue(), actualFilteringProperty.getScalarValue());
        }
      } else if ((expectedRelationConditions == null && actualRelationConditions != null) ||
          (expectedRelationConditions != null && actualRelationConditions == null)) {
        fail("Expected filtering properties: " + expectedRelationConditions + ". "
            + "Actual filtering properties: " + actualRelationConditions);
      }
    }
  }

  public void testTaskQueryByBusinessKeyExpression() {
    // given
    String aBusinessKey = "business key";
    Mocks.register("aBusinessKey", aBusinessKey);

    createDeploymentWithBusinessKey(aBusinessKey);

    // when
    TaskQueryImpl extendedQuery = (TaskQueryImpl)taskService.createTaskQuery()
      .processInstanceBusinessKeyExpression("${ " + Mocks.getMocks().keySet().toArray()[0] + " }");

    Filter filter = filterService.newTaskFilter("aFilterName");
    filter.setQuery(extendedQuery);
    filterService.saveFilter(filter);

    TaskQueryImpl filterQuery = filterService.getFilter(filter.getId()).getQuery();

    // then
    assertEquals(extendedQuery.getExpressions().get("processInstanceBusinessKey"),
      filterQuery.getExpressions().get("processInstanceBusinessKey"));
    assertEquals(1, filterService.list(filter.getId()).size());
  }

  public void testTaskQueryByBusinessKeyExpressionInAdhocQuery() {
    // given
    processEngineConfiguration.setEnableExpressionsInAdhocQueries(true);

    String aBusinessKey = "business key";
    Mocks.register("aBusinessKey", aBusinessKey);

    createDeploymentWithBusinessKey(aBusinessKey);

    // when
    Filter filter = filterService.newTaskFilter("aFilterName");
    filter.setQuery(taskService.createTaskQuery());
    filterService.saveFilter(filter);

    TaskQueryImpl extendingQuery = (TaskQueryImpl)taskService.createTaskQuery()
      .processInstanceBusinessKeyExpression("${ " + Mocks.getMocks().keySet().toArray()[0] + " }");

    // then
    assertEquals(extendingQuery.getExpressions().get("processInstanceBusinessKey"),
      "${ " + Mocks.getMocks().keySet().toArray()[0] + " }");
    assertEquals(1, filterService.list(filter.getId(), extendingQuery).size());
  }

  public void testTaskQueryByBusinessKeyLikeExpression() {
    // given
    String aBusinessKey = "business key";
    Mocks.register("aBusinessKeyLike", "%" + aBusinessKey.substring(5));

    createDeploymentWithBusinessKey(aBusinessKey);

    // when
    TaskQueryImpl extendedQuery = (TaskQueryImpl)taskService.createTaskQuery()
      .processInstanceBusinessKeyLikeExpression("${ " + Mocks.getMocks().keySet().toArray()[0] + " }");

    Filter filter = filterService.newTaskFilter("aFilterName");
    filter.setQuery(extendedQuery);
    filterService.saveFilter(filter);

    TaskQueryImpl filterQuery = filterService.getFilter(filter.getId()).getQuery();

    // then
    assertEquals(extendedQuery.getExpressions().get("processInstanceBusinessKeyLike"),
      filterQuery.getExpressions().get("processInstanceBusinessKeyLike"));
    assertEquals(1, filterService.list(filter.getId()).size());
  }

  public void testTaskQueryByBusinessKeyLikeExpressionInAdhocQuery() {
    // given
    processEngineConfiguration.setEnableExpressionsInAdhocQueries(true);

    String aBusinessKey = "business key";
    Mocks.register("aBusinessKeyLike", "%" + aBusinessKey.substring(5));

    createDeploymentWithBusinessKey(aBusinessKey);

    // when
    Filter filter = filterService.newTaskFilter("aFilterName");
    filter.setQuery(taskService.createTaskQuery());
    filterService.saveFilter(filter);

    TaskQueryImpl extendingQuery = (TaskQueryImpl)taskService.createTaskQuery()
      .processInstanceBusinessKeyLikeExpression("${ " + Mocks.getMocks().keySet().toArray()[0] + " }");

    // then
    assertEquals(extendingQuery.getExpressions().get("processInstanceBusinessKeyLike"),
      "${ " + Mocks.getMocks().keySet().toArray()[0] + " }");
    assertEquals(1, filterService.list(filter.getId(), extendingQuery).size());
  }

  protected void createDeploymentWithBusinessKey(String aBusinessKey) {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("aProcessDefinition")
      .startEvent()
        .userTask()
      .endEvent()
      .done();

    deployment(modelInstance);

    runtimeService.startProcessInstanceByKey("aProcessDefinition", aBusinessKey);
  }

  public void testTaskQueryByFollowUpBeforeOrNotExistent() {
    // create query
    TaskQueryImpl query = new TaskQueryImpl();

    query.followUpBeforeOrNotExistent(testDate);

    // save filter
    filter.setQuery(query);
    filterService.saveFilter(filter);

    // fetch from db
    filter = filterService.createTaskFilterQuery().singleResult();

    // test query
    query = filter.getQuery();
    assertTrue(query.isFollowUpNullAccepted());
    assertEquals(testDate, query.getFollowUpBefore());
  }

  public void testTaskQueryByFollowUpBeforeOrNotExistentExtendingQuery() {
    // create query
    TaskQueryImpl query = new TaskQueryImpl();

    query.followUpBeforeOrNotExistent(testDate);

    // save filter without query
    filterService.saveFilter(filter);

    // fetch from db
    filter = filterService.createTaskFilterQuery().singleResult();

    // use query as extending query
    List<Task> tasks = filterService.list(filter.getId(), query);
    assertEquals(3, tasks.size());

    // set as filter query and save filter
    filter.setQuery(query);
    filterService.saveFilter(filter);

    // fetch from db
    filter = filterService.createTaskFilterQuery().singleResult();

    tasks = filterService.list(filter.getId());
    assertEquals(3, tasks.size());

    TaskQuery extendingQuery = taskService.createTaskQuery();

    extendingQuery
      .orderByTaskCreateTime()
      .asc();

    tasks = filterService.list(filter.getId(), extendingQuery);
    assertEquals(3, tasks.size());
  }

  public void testTaskQueryByFollowUpBeforeOrNotExistentExpression() {
    // create query
    TaskQueryImpl query = new TaskQueryImpl();

    query.followUpBeforeOrNotExistentExpression(testString);

    // save filter
    filter.setQuery(query);
    filterService.saveFilter(filter);

    // fetch from db
    filter = filterService.createTaskFilterQuery().singleResult();

    // test query
    query = filter.getQuery();
    assertTrue(query.isFollowUpNullAccepted());
    assertEquals(testString, query.getExpressions().get("followUpBeforeOrNotExistent"));
  }

  public void testTaskQueryByFollowUpBeforeOrNotExistentExpressionExtendingQuery() {
    // create query
    TaskQueryImpl query = new TaskQueryImpl();

    query.followUpBeforeOrNotExistentExpression("${dateTime().withMillis(0)}");

    // save filter without query
    filterService.saveFilter(filter);

    // fetch from db
    filter = filterService.createTaskFilterQuery().singleResult();

    // use query as extending query
    List<Task> tasks = filterService.list(filter.getId(), query);
    assertEquals(3, tasks.size());

    // set as filter query and save filter
    filter.setQuery(query);
    filterService.saveFilter(filter);

    // fetch from db
    filter = filterService.createTaskFilterQuery().singleResult();

    tasks = filterService.list(filter.getId());
    assertEquals(3, tasks.size());

    TaskQuery extendingQuery = taskService.createTaskQuery();

    extendingQuery
      .orderByTaskCreateTime()
      .asc();

    tasks = filterService.list(filter.getId(), extendingQuery);
    assertEquals(3, tasks.size());
  }

  public void testTaskQueryCandidateUser() {
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskCandidateUser(testUser.getId());
    query.taskCandidateUserExpression(testUser.getId());

    filter.setQuery(query);
    query = filter.getQuery();

    assertEquals(testUser.getId(), query.getCandidateUser());
    assertEquals(testUser.getId(), query.getExpressions().get("taskCandidateUser"));
  }

  public void testTaskQueryCandidateGroup() {
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskCandidateGroup(testGroup.getId());
    query.taskCandidateGroupExpression(testGroup.getId());

    filter.setQuery(query);
    query = filter.getQuery();

    assertEquals(testGroup.getId(), query.getCandidateGroup());
    assertEquals(testGroup.getId(), query.getExpressions().get("taskCandidateGroup"));
  }

  public void testTaskQueryCandidateUserIncludeAssignedTasks() {
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskCandidateUser(testUser.getId());
    query.includeAssignedTasks();

    saveQuery(query);
    query = filterService.getFilter(filter.getId()).getQuery();

    assertEquals(testUser.getId(), query.getCandidateUser());
    assertTrue(query.isIncludeAssignedTasks());
  }

  public void testTaskQueryCandidateUserExpressionIncludeAssignedTasks() {
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskCandidateUserExpression(testString);
    query.includeAssignedTasks();

    saveQuery(query);
    query = filterService.getFilter(filter.getId()).getQuery();

    assertEquals(testString, query.getExpressions().get("taskCandidateUser"));
    assertTrue(query.isIncludeAssignedTasks());
  }

  public void testTaskQueryCandidateGroupIncludeAssignedTasks() {
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskCandidateGroup(testGroup.getId());
    query.includeAssignedTasks();

    saveQuery(query);
    query = filterService.getFilter(filter.getId()).getQuery();

    assertEquals(testGroup.getId(), query.getCandidateGroup());
    assertTrue(query.isIncludeAssignedTasks());
  }

  public void testTaskQueryCandidateGroupExpressionIncludeAssignedTasks() {
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskCandidateGroupExpression(testString);
    query.includeAssignedTasks();

    saveQuery(query);
    query = filterService.getFilter(filter.getId()).getQuery();

    assertEquals(testString, query.getExpressions().get("taskCandidateGroup"));
    assertTrue(query.isIncludeAssignedTasks());
  }

  public void testTaskQueryCandidateGroupsIncludeAssignedTasks() {
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskCandidateGroupIn(testCandidateGroups);
    query.includeAssignedTasks();

    saveQuery(query);
    query = filterService.getFilter(filter.getId()).getQuery();

    assertEquals(testCandidateGroups, query.getCandidateGroupsInternal());
    assertTrue(query.isIncludeAssignedTasks());
  }

  public void testTaskQueryCandidateGroupsExpressionIncludeAssignedTasks() {
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskCandidateGroupInExpression(testString);
    query.includeAssignedTasks();

    saveQuery(query);
    query = filterService.getFilter(filter.getId()).getQuery();

    assertEquals(testString, query.getExpressions().get("taskCandidateGroupIn"));
    assertTrue(query.isIncludeAssignedTasks());
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
  }

  public void testExtendingTaskQueryListWithCandidateGroups() {
    TaskQuery query = taskService.createTaskQuery();

    List<String> candidateGroups = new ArrayList<String>();
    candidateGroups.add("accounting");
    query.taskCandidateGroupIn(candidateGroups);

    saveQuery(query);

    List<Task> tasks = filterService.list(filter.getId());
    assertEquals(1, tasks.size());

    tasks = filterService.list(filter.getId(), query);
    assertEquals(1, tasks.size());

    TaskQuery extendingQuery = taskService.createTaskQuery();

    extendingQuery
      .orderByTaskCreateTime()
      .asc();

    tasks = filterService.list(filter.getId(), extendingQuery);
    assertEquals(1, tasks.size());
  }

  public void testExtendingTaskQueryListWithIncludeAssignedTasks() {
    TaskQuery query = taskService.createTaskQuery();

    query.taskCandidateGroup("accounting");

    saveQuery(query);

    List<Task> tasks = filterService.list(filter.getId());
    assertEquals(1, tasks.size());

    TaskQuery extendingQuery = taskService.createTaskQuery();

    extendingQuery
      .taskCandidateGroup("accounting")
      .includeAssignedTasks();

    tasks = filterService.list(filter.getId(), extendingQuery);
    assertEquals(2, tasks.size());
  }

  public void testExtendTaskQueryWithCandidateUserExpressionAndIncludeAssignedTasks() {
    // create an empty query and save it as a filter
    TaskQuery emptyQuery = taskService.createTaskQuery();
    Filter emptyFilter = filterService.newTaskFilter("empty");
    emptyFilter.setQuery(emptyQuery);

    // create a query with candidate user expression and include assigned tasks
    // and save it as filter
    TaskQuery query = taskService.createTaskQuery();
    query.taskCandidateUserExpression("${'test'}").includeAssignedTasks();
    Filter filter = filterService.newTaskFilter("filter");
    filter.setQuery(query);

    // extend empty query by query with candidate user expression and include assigned tasks
    Filter extendedFilter = emptyFilter.extend(query);
    TaskQueryImpl extendedQuery = extendedFilter.getQuery();
    assertEquals("${'test'}", extendedQuery.getExpressions().get("taskCandidateUser"));
    assertTrue(extendedQuery.isIncludeAssignedTasks());

    // extend query with candidate user expression and include assigned tasks with empty query
    extendedFilter = filter.extend(emptyQuery);
    extendedQuery = extendedFilter.getQuery();
    assertEquals("${'test'}", extendedQuery.getExpressions().get("taskCandidateUser"));
    assertTrue(extendedQuery.isIncludeAssignedTasks());
  }

  public void testExtendTaskQueryWithCandidateGroupExpressionAndIncludeAssignedTasks() {
    // create an empty query and save it as a filter
    TaskQuery emptyQuery = taskService.createTaskQuery();
    Filter emptyFilter = filterService.newTaskFilter("empty");
    emptyFilter.setQuery(emptyQuery);

    // create a query with candidate group expression and include assigned tasks
    // and save it as filter
    TaskQuery query = taskService.createTaskQuery();
    query.taskCandidateGroupExpression("${'test'}").includeAssignedTasks();
    Filter filter = filterService.newTaskFilter("filter");
    filter.setQuery(query);

    // extend empty query by query with candidate group expression and include assigned tasks
    Filter extendedFilter = emptyFilter.extend(query);
    TaskQueryImpl extendedQuery = extendedFilter.getQuery();
    assertEquals("${'test'}", extendedQuery.getExpressions().get("taskCandidateGroup"));
    assertTrue(extendedQuery.isIncludeAssignedTasks());

    // extend query with candidate group expression and include assigned tasks with empty query
    extendedFilter = filter.extend(emptyQuery);
    extendedQuery = extendedFilter.getQuery();
    assertEquals("${'test'}", extendedQuery.getExpressions().get("taskCandidateGroup"));
    assertTrue(extendedQuery.isIncludeAssignedTasks());
  }

  public void testExtendTaskQueryWithCandidateGroupInAndCandidateGroup() {
    // create an query with candidate group in and save it as a filter
    TaskQueryImpl candidateGroupInQuery = (TaskQueryImpl)taskService.createTaskQuery().taskCandidateGroupIn(Arrays.asList("testGroup", "testGroup2"));
    assertEquals(2, candidateGroupInQuery.getCandidateGroups().size());
    assertEquals("testGroup", candidateGroupInQuery.getCandidateGroups().get(0));
    assertEquals("testGroup2", candidateGroupInQuery.getCandidateGroups().get(1));
    Filter candidateGroupInFilter = filterService.newTaskFilter("Groups filter");
    candidateGroupInFilter.setQuery(candidateGroupInQuery);

    // create a query with candidate group
    // and save it as filter
    TaskQuery candidateGroupQuery = taskService.createTaskQuery().taskCandidateGroup("testGroup2");

    // extend candidate group in filter by query with candidate group
    Filter extendedFilter = candidateGroupInFilter.extend(candidateGroupQuery);
    TaskQueryImpl extendedQuery = extendedFilter.getQuery();
    assertEquals(1, extendedQuery.getCandidateGroups().size());
    assertEquals("testGroup2", extendedQuery.getCandidateGroups().get(0));
  }

  public void testTaskQueryWithCandidateGroupInExpressionAndCandidateGroup() {
    // create an query with candidate group in expression and candidate group at once
    TaskQueryImpl candidateGroupInQuery = (TaskQueryImpl)taskService.createTaskQuery().taskCandidateGroupInExpression("${'test'}").taskCandidateGroup("testGroup");
    assertEquals("${'test'}", candidateGroupInQuery.getExpressions().get("taskCandidateGroupIn"));
    assertEquals("testGroup", candidateGroupInQuery.getCandidateGroup());
  }

  public void testTaskQueryWithCandidateGroupInAndCandidateGroupExpression() {
    // create an query with candidate group in and candidate group expression
    TaskQueryImpl candidateGroupInQuery = (TaskQueryImpl)taskService.createTaskQuery().taskCandidateGroupIn(Arrays.asList("testGroup", "testGroup2")).taskCandidateGroupExpression("${'test'}");
    assertEquals("${'test'}", candidateGroupInQuery.getExpressions().get("taskCandidateGroup"));
    assertEquals(2, candidateGroupInQuery.getCandidateGroups().size());
    assertEquals("testGroup", candidateGroupInQuery.getCandidateGroups().get(0));
    assertEquals("testGroup2", candidateGroupInQuery.getCandidateGroups().get(1));
  }

  public void testExtendTaskQueryWithCandidateGroupInExpressionAndIncludeAssignedTasks() {
    // create an empty query and save it as a filter
    TaskQuery emptyQuery = taskService.createTaskQuery();
    Filter emptyFilter = filterService.newTaskFilter("empty");
    emptyFilter.setQuery(emptyQuery);

    // create a query with candidate group in expression and include assigned tasks
    // and save it as filter
    TaskQuery query = taskService.createTaskQuery();
    query.taskCandidateGroupInExpression("${'test'}").includeAssignedTasks();
    Filter filter = filterService.newTaskFilter("filter");
    filter.setQuery(query);

    // extend empty query by query with candidate group in expression and include assigned tasks
    Filter extendedFilter = emptyFilter.extend(query);
    TaskQueryImpl extendedQuery = extendedFilter.getQuery();
    assertEquals("${'test'}", extendedQuery.getExpressions().get("taskCandidateGroupIn"));
    assertTrue(extendedQuery.isIncludeAssignedTasks());

    // extend query with candidate group in expression and include assigned tasks with empty query
    extendedFilter = filter.extend(emptyQuery);
    extendedQuery = extendedFilter.getQuery();
    assertEquals("${'test'}", extendedQuery.getExpressions().get("taskCandidateGroupIn"));
    assertTrue(extendedQuery.isIncludeAssignedTasks());
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
  }

  /**
   * CAM-6363
   *
   * Verify that search by name returns case insensitive results
   */
  public void testTaskQueryLookupByNameCaseInsensitive() {
    TaskQuery query = taskService.createTaskQuery();
    query.taskName("task 1");
    saveQuery(query);

    List<Task> tasks = filterService.list(filter.getId());
    assertNotNull(tasks);
    assertThat(tasks.size(),is(1));

    query = taskService.createTaskQuery();
    query.taskName("tASk 2");
    saveQuery(query);

    tasks = filterService.list(filter.getId());
    assertNotNull(tasks);
    assertThat(tasks.size(),is(1));
  }

  /**
   * CAM-6165
   *
   * Verify that search by name like returns case insensitive results
   */
  public void testTaskQueryLookupByNameLikeCaseInsensitive() {
    TaskQuery query = taskService.createTaskQuery();
    query.taskNameLike("%task%");
    saveQuery(query);

    List<Task> tasks = filterService.list(filter.getId());
    assertNotNull(tasks);
    assertThat(tasks.size(),is(3));

    query = taskService.createTaskQuery();
    query.taskNameLike("%Task%");
    saveQuery(query);

    tasks = filterService.list(filter.getId());
    assertNotNull(tasks);
    assertThat(tasks.size(),is(3));
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
  }

  public void testSpecialExtendingQuery() {
    TaskQuery query = taskService.createTaskQuery();

    saveQuery(query);

    long count = filterService.count(filter.getId(), (Query) null);
    assertEquals(3, count);
  }

  public void testExtendingSorting() {
    // create empty query
    TaskQueryImpl query = (TaskQueryImpl) taskService.createTaskQuery();
    saveQuery(query);

    // assert default sorting
    query = filter.getQuery();
    assertTrue(query.getOrderingProperties().isEmpty());

    // extend query by new task query with sorting
    TaskQueryImpl sortQuery = (TaskQueryImpl) taskService.createTaskQuery().orderByTaskName().asc();
    Filter extendedFilter = filter.extend(sortQuery);
    query = extendedFilter.getQuery();

    List<QueryOrderingProperty> expectedOrderingProperties =
        new ArrayList<QueryOrderingProperty>(sortQuery.getOrderingProperties());

    verifyOrderingProperties(expectedOrderingProperties, query.getOrderingProperties());

    // extend query by new task query with additional sorting
    TaskQueryImpl extendingQuery = (TaskQueryImpl) taskService.createTaskQuery().orderByTaskAssignee().desc();
    extendedFilter = extendedFilter.extend(extendingQuery);
    query = extendedFilter.getQuery();

    expectedOrderingProperties.addAll(extendingQuery.getOrderingProperties());

    verifyOrderingProperties(expectedOrderingProperties, query.getOrderingProperties());

    // extend query by incomplete sorting query (should add sorting anyway)
    sortQuery = (TaskQueryImpl) taskService.createTaskQuery().orderByCaseExecutionId();
    extendedFilter = extendedFilter.extend(sortQuery);
    query = extendedFilter.getQuery();

    expectedOrderingProperties.addAll(sortQuery.getOrderingProperties());

    verifyOrderingProperties(expectedOrderingProperties, query.getOrderingProperties());
  }


  /**
   * Tests compatibility with serialization format that was used in 7.2
   */
  @SuppressWarnings("deprecation")
  public void testDeprecatedOrderingFormatDeserializationSingleOrdering() {
    String sortByNameAsc = "RES." + TaskQueryProperty.NAME.getName() + " " + Direction.ASCENDING.getName();

    JsonTaskQueryConverter converter = (JsonTaskQueryConverter) FilterEntity.queryConverter.get(EntityTypes.TASK);
    JSONObject queryJson = converter.toJsonObject(filter.<TaskQuery>getQuery());

    // when I apply a specific ordering by one dimension
    queryJson.put(JsonTaskQueryConverter.ORDER_BY, sortByNameAsc);
    TaskQueryImpl deserializedTaskQuery = (TaskQueryImpl) converter.toObject(queryJson);

    // then the ordering is applied accordingly
    assertEquals(1, deserializedTaskQuery.getOrderingProperties().size());

    QueryOrderingProperty orderingProperty =
        deserializedTaskQuery.getOrderingProperties().get(0);
    assertNull(orderingProperty.getRelation());
    assertEquals("asc", orderingProperty.getDirection().getName());
    assertNull(orderingProperty.getRelationConditions());
    assertTrue(orderingProperty.isContainedProperty());
    assertEquals(TaskQueryProperty.NAME.getName(), orderingProperty.getQueryProperty().getName());
    assertNull(orderingProperty.getQueryProperty().getFunction());

  }

  /**
   * Tests compatibility with serialization format that was used in 7.2
   */
  @SuppressWarnings("deprecation")
  public void testDeprecatedOrderingFormatDeserializationSecondaryOrdering() {
    String sortByNameAsc = "RES." + TaskQueryProperty.NAME.getName() + " " + Direction.ASCENDING.getName();
    String secondaryOrdering = sortByNameAsc + ", RES." + TaskQueryProperty.ASSIGNEE.getName() + " " + Direction.DESCENDING.getName();

    JsonTaskQueryConverter converter = (JsonTaskQueryConverter) FilterEntity.queryConverter.get(EntityTypes.TASK);
    JSONObject queryJson = converter.toJsonObject(filter.<TaskQuery>getQuery());

    // when I apply a secondary ordering
    queryJson.put(JsonTaskQueryConverter.ORDER_BY, secondaryOrdering);
    TaskQueryImpl deserializedTaskQuery = (TaskQueryImpl) converter.toObject(queryJson);

    // then the ordering is applied accordingly
    assertEquals(2, deserializedTaskQuery.getOrderingProperties().size());

    QueryOrderingProperty orderingProperty1 =
        deserializedTaskQuery.getOrderingProperties().get(0);
    assertNull(orderingProperty1.getRelation());
    assertEquals("asc", orderingProperty1.getDirection().getName());
    assertNull(orderingProperty1.getRelationConditions());
    assertTrue(orderingProperty1.isContainedProperty());
    assertEquals(TaskQueryProperty.NAME.getName(), orderingProperty1.getQueryProperty().getName());
    assertNull(orderingProperty1.getQueryProperty().getFunction());

    QueryOrderingProperty orderingProperty2 =
        deserializedTaskQuery.getOrderingProperties().get(1);
    assertNull(orderingProperty2.getRelation());
    assertEquals("desc", orderingProperty2.getDirection().getName());
    assertNull(orderingProperty2.getRelationConditions());
    assertTrue(orderingProperty2.isContainedProperty());
    assertEquals(TaskQueryProperty.ASSIGNEE.getName(), orderingProperty2.getQueryProperty().getName());
    assertNull(orderingProperty2.getQueryProperty().getFunction());
  }

  /**
   * Tests compatibility with serialization format that was used in 7.2
   */
  @SuppressWarnings("deprecation")
  public void testDeprecatedOrderingFormatDeserializationFunctionOrdering() {
    String orderingWithFunction = "LOWER(RES." + TaskQueryProperty.NAME.getName() + ") asc";

    JsonTaskQueryConverter converter = (JsonTaskQueryConverter) FilterEntity.queryConverter.get(EntityTypes.TASK);
    JSONObject queryJson = converter.toJsonObject(filter.<TaskQuery>getQuery());

    // when I apply an ordering with a function
    queryJson.put(JsonTaskQueryConverter.ORDER_BY, orderingWithFunction);
    TaskQueryImpl deserializedTaskQuery = (TaskQueryImpl) converter.toObject(queryJson);

    assertEquals(1, deserializedTaskQuery.getOrderingProperties().size());

    // then the ordering is applied accordingly
    QueryOrderingProperty orderingProperty =
        deserializedTaskQuery.getOrderingProperties().get(0);
    assertNull(orderingProperty.getRelation());
    assertEquals("asc", orderingProperty.getDirection().getName());
    assertNull(orderingProperty.getRelationConditions());
    assertFalse(orderingProperty.isContainedProperty());
    assertEquals(TaskQueryProperty.NAME_CASE_INSENSITIVE.getName(),
        orderingProperty.getQueryProperty().getName());
    assertEquals(TaskQueryProperty.NAME_CASE_INSENSITIVE.getFunction(),
      orderingProperty.getQueryProperty().getFunction());
  }


  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/oneTaskWithFormKeyProcess.bpmn20.xml"})
    public void testInitializeFormKeysEnabled() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    TaskQuery query = taskService.createTaskQuery()
      .processInstanceId(processInstance.getId());

    saveQuery(query);

    Task task = (Task) filterService.list(filter.getId()).get(0);

    assertEquals("exampleFormKey", task.getFormKey());

    task = filterService.singleResult(filter.getId());

    assertEquals("exampleFormKey", task.getFormKey());

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
  }

  public void testExtendingVariableQuery() {
    TaskQuery taskQuery = taskService.createTaskQuery().processVariableValueEquals("hello", "world");
    saveQuery(taskQuery);

    // variables won't overridden variables with same name in different scopes
    TaskQuery extendingQuery = taskService.createTaskQuery()
      .taskVariableValueEquals("hello", "world")
      .caseInstanceVariableValueEquals("hello", "world");

    Filter extendedFilter = filter.extend(extendingQuery);
    TaskQueryImpl extendedQuery = extendedFilter.getQuery();
    List<TaskQueryVariableValue> variables = extendedQuery.getVariables();

    assertEquals(3, variables.size());

    // assert variables (ordering: extending variables are inserted first)
    assertEquals("hello", variables.get(0).getName());
    assertEquals("world", variables.get(0).getValue());
    assertEquals(QueryOperator.EQUALS, variables.get(0).getOperator());
    assertFalse(variables.get(0).isProcessInstanceVariable());
    assertTrue(variables.get(0).isLocal());
    assertEquals("hello", variables.get(1).getName());
    assertEquals("world", variables.get(1).getValue());
    assertEquals(QueryOperator.EQUALS, variables.get(1).getOperator());
    assertFalse(variables.get(1).isProcessInstanceVariable());
    assertFalse(variables.get(1).isLocal());
    assertEquals("hello", variables.get(2).getName());
    assertEquals("world", variables.get(2).getValue());
    assertEquals(QueryOperator.EQUALS, variables.get(2).getOperator());
    assertTrue(variables.get(2).isProcessInstanceVariable());
    assertFalse(variables.get(2).isLocal());

    // variables will override variables with same name in same scope
    extendingQuery = taskService.createTaskQuery()
      .processVariableValueLessThan("hello", 42)
      .taskVariableValueLessThan("hello", 42)
      .caseInstanceVariableValueLessThan("hello", 42);

    extendedFilter = filter.extend(extendingQuery);
    extendedQuery = extendedFilter.getQuery();
    variables = extendedQuery.getVariables();

    assertEquals(3, variables.size());

    // assert variables (ordering: extending variables are inserted first)
    assertEquals("hello", variables.get(0).getName());
    assertEquals(42, variables.get(0).getValue());
    assertEquals(QueryOperator.LESS_THAN, variables.get(0).getOperator());
    assertTrue(variables.get(0).isProcessInstanceVariable());
    assertFalse(variables.get(0).isLocal());
    assertEquals("hello", variables.get(1).getName());
    assertEquals(42, variables.get(1).getValue());
    assertEquals(QueryOperator.LESS_THAN, variables.get(1).getOperator());
    assertFalse(variables.get(1).isProcessInstanceVariable());
    assertTrue(variables.get(1).isLocal());
    assertEquals("hello", variables.get(2).getName());
    assertEquals(42, variables.get(2).getValue());
    assertEquals(QueryOperator.LESS_THAN, variables.get(2).getOperator());
    assertFalse(variables.get(2).isProcessInstanceVariable());
    assertFalse(variables.get(2).isLocal());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testExtendTaskQueryByOrderByProcessVariable() {
    ProcessInstance instance500 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
      Variables.createVariables().putValue("var", 500));
    ProcessInstance instance1000 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
      Variables.createVariables().putValue("var", 1000));
    ProcessInstance instance250 = runtimeService.startProcessInstanceByKey("oneTaskProcess",
      Variables.createVariables().putValue("var", 250));

    TaskQuery query = taskService.createTaskQuery().processDefinitionKey("oneTaskProcess");
    saveQuery(query);

    // asc
    TaskQuery extendingQuery = taskService
        .createTaskQuery()
        .orderByProcessVariable("var", ValueType.INTEGER)
        .asc();

    List<Task> tasks = filterService.list(filter.getId(), extendingQuery);

    assertEquals(3, tasks.size());
    assertEquals(instance250.getId(), tasks.get(0).getProcessInstanceId());
    assertEquals(instance500.getId(), tasks.get(1).getProcessInstanceId());
    assertEquals(instance1000.getId(), tasks.get(2).getProcessInstanceId());

    // desc
    extendingQuery = taskService
        .createTaskQuery()
        .orderByProcessVariable("var", ValueType.INTEGER)
        .desc();

    tasks = filterService.list(filter.getId(), extendingQuery);

    assertEquals(3, tasks.size());
    assertEquals(instance1000.getId(), tasks.get(0).getProcessInstanceId());
    assertEquals(instance500.getId(), tasks.get(1).getProcessInstanceId());
    assertEquals(instance250.getId(), tasks.get(2).getProcessInstanceId());

    runtimeService.deleteProcessInstance(instance250.getId(), null);
    runtimeService.deleteProcessInstance(instance500.getId(), null);
    runtimeService.deleteProcessInstance(instance1000.getId(), null);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testExtendTaskQueryByOrderByTaskVariable() {
    ProcessInstance instance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance instance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance instance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task task500 = taskService.createTaskQuery().processInstanceId(instance1.getId()).singleResult();
    taskService.setVariableLocal(task500.getId(), "var", 500);

    Task task250 = taskService.createTaskQuery().processInstanceId(instance2.getId()).singleResult();
    taskService.setVariableLocal(task250.getId(), "var", 250);

    Task task1000 = taskService.createTaskQuery().processInstanceId(instance3.getId()).singleResult();
    taskService.setVariableLocal(task1000.getId(), "var", 1000);

    TaskQuery query = taskService.createTaskQuery().processDefinitionKey("oneTaskProcess");
    saveQuery(query);

    // asc
    TaskQuery extendingQuery = taskService
        .createTaskQuery()
        .orderByProcessVariable("var", ValueType.INTEGER)
        .asc();

    List<Task> tasks = filterService.list(filter.getId(), extendingQuery);

    assertEquals(3, tasks.size());
    assertEquals(task250.getId(), tasks.get(0).getId());
    assertEquals(task500.getId(), tasks.get(1).getId());
    assertEquals(task1000.getId(), tasks.get(2).getId());

    // desc
    extendingQuery = taskService
        .createTaskQuery()
        .orderByProcessVariable("var", ValueType.INTEGER)
        .desc();

    tasks = filterService.list(filter.getId(), extendingQuery);

    assertEquals(3, tasks.size());
    assertEquals(task1000.getId(), tasks.get(0).getId());
    assertEquals(task500.getId(), tasks.get(1).getId());
    assertEquals(task250.getId(), tasks.get(2).getId());

    runtimeService.deleteProcessInstance(instance1.getId(), null);
    runtimeService.deleteProcessInstance(instance2.getId(), null);
    runtimeService.deleteProcessInstance(instance3.getId(), null);
  }

  public void testExtendTaskQuery_ORInExtendingQuery() {
    // given
    createTasksForOrQueries();

    // when
    TaskQuery extendedQuery = taskService.createTaskQuery()
      .taskName("taskForOr");

    Filter extendedFilter = filterService.newTaskFilter("extendedOrFilter");
    extendedFilter.setQuery(extendedQuery);
    filterService.saveFilter(extendedFilter);

    TaskQuery extendingQuery = taskService.createTaskQuery()
      .or()
        .taskDescription("aTaskDescription")
        .taskOwner("aTaskOwner")
      .endOr()
      .or()
        .taskPriority(3)
        .taskAssignee("aTaskAssignee")
      .endOr();

    // then
    assertEquals(4, extendedQuery.list().size());
    assertEquals(4, filterService.list(extendedFilter.getId()).size());
    assertEquals(6, extendingQuery.list().size());
    assertEquals(3, filterService.list(extendedFilter.getId(), extendingQuery).size());
  }

  public void testExtendTaskQuery_ORInExtendedQuery() {
    // given
    createTasksForOrQueries();

    // when
    TaskQuery extendedQuery = taskService.createTaskQuery()
      .or()
        .taskDescription("aTaskDescription")
        .taskOwner("aTaskOwner")
      .endOr()
      .or()
        .taskPriority(3)
        .taskAssignee("aTaskAssignee")
      .endOr();

    Filter extendedFilter = filterService.newTaskFilter("extendedOrFilter");
    extendedFilter.setQuery(extendedQuery);
    filterService.saveFilter(extendedFilter);

    TaskQuery extendingQuery = taskService.createTaskQuery()
      .taskName("taskForOr");

    // then
    assertEquals(6, extendedQuery.list().size());
    assertEquals(6, filterService.list(extendedFilter.getId()).size());
    assertEquals(4, extendingQuery.list().size());
    assertEquals(3, filterService.list(extendedFilter.getId(), extendingQuery).size());
  }

  public void testExtendTaskQuery_ORInBothExtendedAndExtendingQuery() {
    // given
    createTasksForOrQueries();

    // when
    TaskQuery extendedQuery = taskService.createTaskQuery()
      .or()
        .taskName("taskForOr")
        .taskDescription("aTaskDescription")
      .endOr();

    Filter extendedFilter = filterService.newTaskFilter("extendedOrFilter");
    extendedFilter.setQuery(extendedQuery);
    filterService.saveFilter(extendedFilter);

    TaskQuery extendingQuery = taskService.createTaskQuery()
      .or()
        .tenantIdIn("aTenantId")
        .taskOwner("aTaskOwner")
      .endOr()
      .or()
        .taskPriority(3)
        .taskAssignee("aTaskAssignee")
      .endOr();

    // then
    assertEquals(6, extendedQuery.list().size());
    assertEquals(6, filterService.list(extendedFilter.getId()).size());
    assertEquals(4, extendingQuery.list().size());
    assertEquals(3, filterService.list(extendedFilter.getId(), extendingQuery).size());
  }

  public void testOrderByVariables() {
    // given
    TaskQueryImpl query = (TaskQueryImpl) taskService.createTaskQuery()
        .orderByProcessVariable("foo", ValueType.STRING).asc()
        .orderByExecutionVariable("foo", ValueType.STRING).asc()
        .orderByCaseInstanceVariable("foo", ValueType.STRING).asc()
        .orderByCaseExecutionVariable("foo", ValueType.STRING).asc()
        .orderByTaskVariable("foo", ValueType.STRING).asc();

    Filter filter = filterService.newTaskFilter("extendedOrFilter");
    filter.setQuery(query);
    filterService.saveFilter(filter);

    // when
    filter = filterService.getFilter(filter.getId());

    // then
    List<QueryOrderingProperty> expectedOrderingProperties =
        new ArrayList<QueryOrderingProperty>(query.getOrderingProperties());

    verifyOrderingProperties(expectedOrderingProperties, ((TaskQueryImpl) filter.getQuery()).getOrderingProperties());

    for (QueryOrderingProperty prop : ((TaskQueryImpl) filter.getQuery()).getOrderingProperties()) {
      assertTrue(prop instanceof VariableOrderProperty);
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
    taskService.addCandidateGroup(task.getId(), "accounting");

    task = taskService.newTask("task2");
    task.setName("Task 2");
    task.setOwner(testUser.getId());
    task.setDelegationState(DelegationState.RESOLVED);
    taskService.saveTask(task);
    taskService.setAssignee(task.getId(), "kermit");
    taskService.addCandidateGroup(task.getId(), "accounting");

    task = taskService.newTask("task3");
    task.setName("Task 3");
    task.setOwner(testUser.getId());
    task.setDelegationState(DelegationState.RESOLVED);
    taskService.saveTask(task);
  }

  protected void createTasksForOrQueries() {
    Task task1 = taskService.newTask();
    task1.setName("taskForOr");
    task1.setDescription("aTaskDescription");
    task1.setPriority(3);
    taskService.saveTask(task1);

    Task task2 = taskService.newTask();
    task2.setName("taskForOr");
    task2.setDescription("aTaskDescription");
    task2.setAssignee("aTaskAssignee");
    task2.setTenantId("aTenantId");
    taskService.saveTask(task2);

    Task task3 = taskService.newTask();
    task3.setName("taskForOr");
    task3.setOwner("aTaskOwner");
    taskService.saveTask(task3);

    Task task4 = taskService.newTask();
    task4.setName("taskForOr");
    task4.setOwner("aTaskOwner");
    task4.setPriority(3);
    taskService.saveTask(task4);

    Task task5 = taskService.newTask();
    task5.setDescription("aTaskDescription");
    task5.setAssignee("aTaskAssignee");
    taskService.saveTask(task5);

    Task task6 = taskService.newTask();
    task6.setDescription("aTaskDescription");
    task6.setAssignee("aTaskAssignee");
    task6.setTenantId("aTenantId");
    taskService.saveTask(task6);

    Task task7 = taskService.newTask();
    task7.setTenantId("aTenantId");
    task7.setOwner("aTaskOwner");
    task7.setPriority(3);
    task7.setAssignee("aTaskAssignee");
    taskService.saveTask(task7);
  }

}
