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

package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.FILTER;
import static org.camunda.bpm.engine.rest.dto.AbstractQueryDto.SORT_ORDER_ASC_VALUE;
import static org.camunda.bpm.engine.rest.dto.AbstractQueryDto.SORT_ORDER_DESC_VALUE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_ASSIGNEE_VALUE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_CASE_EXECUTION_ID_VALUE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_CASE_EXECUTION_VARIABLE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_CASE_INSTANCE_ID_VALUE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_CASE_INSTANCE_VARIABLE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_CREATE_TIME_VALUE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_DESCRIPTION_VALUE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_DUE_DATE_VALUE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_EXECUTION_ID_VALUE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_EXECUTION_VARIABLE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_FOLLOW_UP_VALUE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_ID_VALUE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_NAME_CASE_INSENSITIVE_VALUE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_NAME_VALUE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_PRIORITY_VALUE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_PROCESS_INSTANCE_ID_VALUE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_PROCESS_VARIABLE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_TASK_VARIABLE;
import static org.camunda.bpm.engine.rest.dto.task.TaskQueryDto.SORT_BY_TENANT_ID_VALUE;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_FILTER_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.mockFilter;
import static org.camunda.bpm.engine.rest.helper.MockProvider.mockVariableInstance;
import static org.camunda.bpm.engine.rest.helper.TaskQueryMatcher.hasName;
import static org.camunda.bpm.engine.variable.Variables.stringValue;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.filter.FilterQuery;
import org.camunda.bpm.engine.impl.AuthorizationServiceImpl;
import org.camunda.bpm.engine.impl.IdentityServiceImpl;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.persistence.entity.FilterEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.MockTaskBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.jayway.restassured.response.Response;
import org.mockito.ArgumentCaptor;

/**
 * @author Sebastian Menski
 */
public class FilterRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  public static final String FILTER_URL = TEST_RESOURCE_ROOT_PATH + FilterRestService.PATH;
  public static final String SINGLE_FILTER_URL = FILTER_URL + "/{id}";
  public static final String CREATE_FILTER_URL = FILTER_URL + "/create";
  public static final String EXECUTE_SINGLE_RESULT_FILTER_URL = SINGLE_FILTER_URL + "/singleResult";
  public static final String EXECUTE_LIST_FILTER_URL = SINGLE_FILTER_URL + "/list";
  public static final String EXECUTE_COUNT_FILTER_URL = SINGLE_FILTER_URL + "/count";

  public static final TaskQuery extendingQuery = new TaskQueryImpl().taskName(MockProvider.EXAMPLE_TASK_NAME);
  public static final TaskQueryDto extendingQueryDto = TaskQueryDto.fromQuery(extendingQuery);
  public static final TaskQuery extendingOrQuery = new TaskQueryImpl().or().taskDescription(MockProvider.EXAMPLE_TASK_DESCRIPTION).endOr().or().taskName(MockProvider.EXAMPLE_TASK_NAME).endOr();
  public static final TaskQueryDto extendingOrQueryDto = TaskQueryDto.fromQuery(extendingOrQuery);
  public static final String invalidExtendingQuery = "abc";

  public static final String PROCESS_INSTANCE_A_ID = "processInstanceA";
  public static final String CASE_INSTANCE_A_ID = "caseInstanceA";
  public static final String EXECUTION_A_ID = "executionA";
  public static final String EXECUTION_B_ID = "executionB";
  public static final String CASE_EXECUTION_A_ID = "caseExecutionA";
  public static final String TASK_A_ID = "taskA";
  public static final String TASK_B_ID = "taskB";
  public static final String TASK_C_ID = "taskC";

  protected FilterService filterServiceMock;
  protected Filter filterMock;

  protected AuthorizationService authorizationServiceMock;
  protected IdentityService identityServiceMock;
  protected VariableInstanceQuery variableInstanceQueryMock;
  protected ProcessEngineConfiguration processEngineConfigurationMock;

  @Before
  @SuppressWarnings("unchecked")
  public void setUpRuntimeData() {
    filterServiceMock = mock(FilterService.class);

    when(processEngine.getFilterService()).thenReturn(filterServiceMock);

    FilterQuery filterQuery = MockProvider.createMockFilterQuery();

    when(filterServiceMock.createFilterQuery()).thenReturn(filterQuery);

    filterMock = MockProvider.createMockFilter();

    when(filterServiceMock.newTaskFilter()).thenReturn(filterMock);
    when(filterServiceMock.saveFilter(eq(filterMock))).thenReturn(filterMock);
    when(filterServiceMock.getFilter(eq(EXAMPLE_FILTER_ID))).thenReturn(filterMock);
    when(filterServiceMock.getFilter(eq(MockProvider.NON_EXISTING_ID))).thenReturn(null);

    List<Object> mockTasks = Collections.<Object>singletonList(new TaskEntity());

    when(filterServiceMock.singleResult(eq(EXAMPLE_FILTER_ID)))
      .thenReturn(mockTasks.get(0));
    when(filterServiceMock.singleResult(eq(EXAMPLE_FILTER_ID), any(Query.class)))
      .thenReturn(mockTasks.get(0));
    when(filterServiceMock.list(eq(EXAMPLE_FILTER_ID)))
      .thenReturn(mockTasks);
    when(filterServiceMock.list(eq(EXAMPLE_FILTER_ID), any(Query.class)))
      .thenReturn(mockTasks);
    when(filterServiceMock.listPage(eq(EXAMPLE_FILTER_ID), anyInt(), anyInt()))
      .thenReturn(mockTasks);
    when(filterServiceMock.listPage(eq(EXAMPLE_FILTER_ID), any(Query.class), anyInt(), anyInt()))
      .thenReturn(mockTasks);
    when(filterServiceMock.count(eq(EXAMPLE_FILTER_ID)))
      .thenReturn((long) 1);
    when(filterServiceMock.count(eq(EXAMPLE_FILTER_ID), any(Query.class)))
      .thenReturn((long) 1);

    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).singleResult(eq(MockProvider.NON_EXISTING_ID));
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).singleResult(eq(MockProvider.NON_EXISTING_ID), any(Query.class));
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).list(eq(MockProvider.NON_EXISTING_ID));
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).list(eq(MockProvider.NON_EXISTING_ID), any(Query.class));
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).listPage(eq(MockProvider.NON_EXISTING_ID), anyInt(), anyInt());
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).listPage(eq(MockProvider.NON_EXISTING_ID), any(Query.class), anyInt(), anyInt());
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).count(eq(MockProvider.NON_EXISTING_ID));
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).count(eq(MockProvider.NON_EXISTING_ID), any(Query.class));
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).deleteFilter(eq(MockProvider.NON_EXISTING_ID));

    authorizationServiceMock = mock(AuthorizationServiceImpl.class);
    identityServiceMock = mock(IdentityServiceImpl.class);
    processEngineConfigurationMock = mock(ProcessEngineConfiguration.class);

    when(processEngine.getAuthorizationService()).thenReturn(authorizationServiceMock);
    when(processEngine.getIdentityService()).thenReturn(identityServiceMock);
    when(processEngine.getProcessEngineConfiguration()).thenReturn(processEngineConfigurationMock);

    TaskService taskService = processEngine.getTaskService();
    when(taskService.createTaskQuery()).thenReturn(new TaskQueryImpl());

    variableInstanceQueryMock = mock(VariableInstanceQuery.class);
    when(processEngine.getRuntimeService().createVariableInstanceQuery())
      .thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.variableScopeIdIn((String) anyVararg()))
      .thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.variableNameIn((String) anyVararg()))
      .thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableBinaryFetching()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableCustomObjectDeserialization()).thenReturn(variableInstanceQueryMock);
  }

  @Test
  public void testGetFilter() {
    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(EXAMPLE_FILTER_ID))
    .when()
      .get(SINGLE_FILTER_URL);

    verify(filterServiceMock).getFilter(eq(EXAMPLE_FILTER_ID));
  }

  @Test
  public void testGetNonExistingFilter() {
    given()
      .pathParam("id", MockProvider.NON_EXISTING_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
    .when()
      .get(SINGLE_FILTER_URL);

    verify(filterServiceMock).getFilter(eq(MockProvider.NON_EXISTING_ID));
  }

  @Test
  public void testGetFilterWithTaskQuery() {
    TaskQueryImpl query = mock(TaskQueryImpl.class);
    when(query.getAssignee()).thenReturn(MockProvider.EXAMPLE_TASK_ASSIGNEE_NAME);
    when(query.getAssigneeLike()).thenReturn(MockProvider.EXAMPLE_TASK_ASSIGNEE_NAME);
    when(query.getCaseDefinitionId()).thenReturn(MockProvider.EXAMPLE_CASE_DEFINITION_ID);
    when(query.getCaseDefinitionKey()).thenReturn(MockProvider.EXAMPLE_CASE_DEFINITION_KEY);
    when(query.getCaseDefinitionName()).thenReturn(MockProvider.EXAMPLE_CASE_DEFINITION_NAME);
    when(query.getCaseDefinitionNameLike()).thenReturn(MockProvider.EXAMPLE_CASE_DEFINITION_NAME_LIKE);
    when(query.getCaseExecutionId()).thenReturn(MockProvider.EXAMPLE_CASE_EXECUTION_ID);
    when(query.getCaseInstanceBusinessKey()).thenReturn(MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY);
    when(query.getCaseInstanceBusinessKeyLike()).thenReturn(MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY_LIKE);
    when(query.getCaseInstanceId()).thenReturn(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    when(query.getCandidateUser()).thenReturn(MockProvider.EXAMPLE_USER_ID);
    when(query.getCandidateGroup()).thenReturn(MockProvider.EXAMPLE_GROUP_ID);
    when(query.getProcessInstanceBusinessKey()).thenReturn(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);
    when(query.getProcessInstanceBusinessKeyLike()).thenReturn(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY_LIKE);
    when(query.getProcessDefinitionKey()).thenReturn(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    when(query.getProcessDefinitionKeys()).thenReturn(new String[]{MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY});
    when(query.getProcessDefinitionId()).thenReturn(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    when(query.getExecutionId()).thenReturn(MockProvider.EXAMPLE_EXECUTION_ID);
    when(query.getProcessDefinitionName()).thenReturn(MockProvider.EXAMPLE_PROCESS_DEFINITION_NAME);
    when(query.getProcessDefinitionNameLike()).thenReturn(MockProvider.EXAMPLE_PROCESS_DEFINITION_NAME_LIKE);
    when(query.getProcessInstanceId()).thenReturn(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    when(query.getKey()).thenReturn(MockProvider.EXAMPLE_TASK_DEFINITION_KEY);
    when(query.getKeys()).thenReturn(new String[]{MockProvider.EXAMPLE_TASK_DEFINITION_KEY, MockProvider.EXAMPLE_TASK_DEFINITION_KEY});
    when(query.getKeyLike()).thenReturn(MockProvider.EXAMPLE_TASK_DEFINITION_KEY);
    when(query.getDescription()).thenReturn(MockProvider.EXAMPLE_TASK_DESCRIPTION);
    when(query.getDescriptionLike()).thenReturn(MockProvider.EXAMPLE_TASK_DESCRIPTION);
    when(query.getInvolvedUser()).thenReturn(MockProvider.EXAMPLE_USER_ID);
    when(query.getPriority()).thenReturn(1);
    when(query.getMaxPriority()).thenReturn(2);
    when(query.getMinPriority()).thenReturn(3);
    when(query.getName()).thenReturn(MockProvider.EXAMPLE_TASK_NAME);
    when(query.getNameLike()).thenReturn(MockProvider.EXAMPLE_TASK_NAME);
    when(query.getOwner()).thenReturn(MockProvider.EXAMPLE_TASK_OWNER);
    when(query.getParentTaskId()).thenReturn(MockProvider.EXAMPLE_TASK_PARENT_TASK_ID);
    when(query.getTenantIds()).thenReturn(MockProvider.EXAMPLE_TENANT_ID_LIST.split(","));
    when(query.isTenantIdSet()).thenReturn(true);

    filterMock = MockProvider.createMockFilter(EXAMPLE_FILTER_ID, query);
    when(filterServiceMock.getFilter(EXAMPLE_FILTER_ID)).thenReturn(filterMock);

    given()
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("query.assignee", equalTo(MockProvider.EXAMPLE_TASK_ASSIGNEE_NAME))
      .body("query.assigneeLike", equalTo(MockProvider.EXAMPLE_TASK_ASSIGNEE_NAME))
      .body("query.caseDefinitionId", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_ID))
      .body("query.caseDefinitionKey", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_KEY))
      .body("query.caseDefinitionName", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_NAME))
      .body("query.caseDefinitionNameLike", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_NAME_LIKE))
      .body("query.caseExecutionId", equalTo(MockProvider.EXAMPLE_CASE_EXECUTION_ID))
      .body("query.caseInstanceBusinessKey", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY))
      .body("query.caseInstanceBusinessKeyLike", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY_LIKE))
      .body("query.caseInstanceId", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_ID))
      .body("query.candidateUser", equalTo(MockProvider.EXAMPLE_USER_ID))
      .body("query.candidateGroup", equalTo(MockProvider.EXAMPLE_GROUP_ID))
      .body("query.processInstanceBusinessKey", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY))
      .body("query.processInstanceBusinessKeyLike", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY_LIKE))
      .body("query.processDefinitionKey", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY))
      .body("query.processDefinitionKeyIn", hasItems(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY))
      .body("query.processDefinitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("query.executionId", equalTo(MockProvider.EXAMPLE_EXECUTION_ID))
      .body("query.processDefinitionName", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_NAME))
      .body("query.processDefinitionNameLike", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_NAME_LIKE))
      .body("query.processInstanceId", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("query.taskDefinitionKey", equalTo(MockProvider.EXAMPLE_TASK_DEFINITION_KEY))
      .body("query.taskDefinitionKeyIn", hasItems(MockProvider.EXAMPLE_TASK_DEFINITION_KEY, MockProvider.EXAMPLE_TASK_DEFINITION_KEY))
      .body("query.taskDefinitionKeyLike", equalTo(MockProvider.EXAMPLE_TASK_DEFINITION_KEY))
      .body("query.description", equalTo(MockProvider.EXAMPLE_TASK_DESCRIPTION))
      .body("query.descriptionLike", equalTo(MockProvider.EXAMPLE_TASK_DESCRIPTION))
      .body("query.involvedUser", equalTo(MockProvider.EXAMPLE_USER_ID))
      .body("query.priority", equalTo(1))
      .body("query.maxPriority", equalTo(2))
      .body("query.minPriority", equalTo(3))
      .body("query.name", equalTo(MockProvider.EXAMPLE_TASK_NAME))
      .body("query.nameLike", equalTo(MockProvider.EXAMPLE_TASK_NAME))
      .body("query.owner", equalTo(MockProvider.EXAMPLE_TASK_OWNER))
      .body("query.parentTaskId", equalTo(MockProvider.EXAMPLE_TASK_PARENT_TASK_ID))
      .body("query.tenantIdIn", hasItems(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID))
    .when()
      .get(SINGLE_FILTER_URL);

  }

  @Test
  public void testGetFilterWithCandidateGroupQuery() {
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskCandidateGroup("abc");
    Filter filter = new FilterEntity("Task").setName("test").setQuery(query);
    when(filterServiceMock.getFilter(EXAMPLE_FILTER_ID)).thenReturn(filter);

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("query.candidateGroup", equalTo("abc"))
      .body("query.containsKey('candidateGroups')", is(false))
      .body("query.containsKey('includeAssignedTasks')", is(false))
    .when()
      .get(SINGLE_FILTER_URL);
  }

  @Test
  public void testGetFilterWithCandidateUserQuery() {
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskCandidateUser("abc");
    Filter filter = new FilterEntity("Task").setName("test").setQuery(query);
    when(filterServiceMock.getFilter(EXAMPLE_FILTER_ID)).thenReturn(filter);

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("query.candidateUser", equalTo("abc"))
      .body("query.containsKey('candidateGroups')", is(false))
      .body("query.containsKey('includeAssignedTasks')", is(false))
  .when()
      .get(SINGLE_FILTER_URL);
  }

  @Test
  public void testGetFilterWithCandidateIncludeAssignedTasks() {
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskCandidateUser("abc").includeAssignedTasks();
    Filter filter = new FilterEntity("Task").setName("test").setQuery(query);
    when(filterServiceMock.getFilter(EXAMPLE_FILTER_ID)).thenReturn(filter);

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("query.candidateUser", equalTo("abc"))
      .body("query.containsKey('candidateGroups')", is(false))
      .body("query.includeAssignedTasks", is(true))
    .when()
      .get(SINGLE_FILTER_URL);
  }

  @Test
  public void testGetFilterWithoutTenantIdQuery() {
    TaskQueryImpl query = new TaskQueryImpl();
    query.withoutTenantId();
    Filter filter = new FilterEntity("Task").setName("test").setQuery(query);
    when(filterServiceMock.getFilter(EXAMPLE_FILTER_ID)).thenReturn(filter);

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("query.withoutTenantId", is(true))
    .when()
      .get(SINGLE_FILTER_URL);
  }

  @Test
  public void testGetFilterWithoutSorting() {
    TaskQuery query = new TaskQueryImpl();
    Filter filter = new FilterEntity("Task").setName("test").setQuery(query);
    when(filterServiceMock.getFilter(EXAMPLE_FILTER_ID)).thenReturn(filter);

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("query.sorting", isEmptyOrNullString())
    .when()
      .get(SINGLE_FILTER_URL);
  }

  @Test
  public void testGetFilterWithSingleSorting() {
    TaskQuery query = new TaskQueryImpl()
      .orderByTaskName().desc();

    Filter filter = new FilterEntity("Task").setName("test").setQuery(query);
    when(filterServiceMock.getFilter(EXAMPLE_FILTER_ID)).thenReturn(filter);

    Response response = given()
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(SINGLE_FILTER_URL);

    // validate sorting content
    String content = response.asString();
    List<Map<String, Object>> sortings = from(content).getJsonObject("query.sorting");
    assertThat(sortings).hasSize(1);
    assertSorting(sortings.get(0), SORT_BY_NAME_VALUE, SORT_ORDER_DESC_VALUE);
  }

  @Test
  public void testGetFilterWithMultipleSorting() {
    TaskQuery query = new TaskQueryImpl()
      .orderByDueDate().asc()
      .orderByCaseExecutionId().desc();

    Filter filter = new FilterEntity("Task").setName("test").setQuery(query);
    when(filterServiceMock.getFilter(EXAMPLE_FILTER_ID)).thenReturn(filter);

    Response response = given()
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(SINGLE_FILTER_URL);

    // validate sorting content
    String content = response.asString();
    List<Map<String, Object>> sortings = from(content).getJsonObject("query.sorting");
    assertThat(sortings).hasSize(2);
    assertSorting(sortings.get(0), SORT_BY_DUE_DATE_VALUE, SORT_ORDER_ASC_VALUE);
    assertSorting(sortings.get(1), SORT_BY_CASE_EXECUTION_ID_VALUE, SORT_ORDER_DESC_VALUE);
  }

  @Test
  public void testGetFilterWithAllPropertiesSorting() {
    TaskQuery query = new TaskQueryImpl()
      .orderByProcessInstanceId().asc()
      .orderByCaseInstanceId().asc()
      .orderByDueDate().asc()
      .orderByFollowUpDate().asc()
      .orderByExecutionId().asc()
      .orderByCaseExecutionId().asc()
      .orderByTaskAssignee().asc()
      .orderByTaskCreateTime().asc()
      .orderByTaskDescription().asc()
      .orderByTaskId().asc()
      .orderByTaskName().asc()
      .orderByTaskNameCaseInsensitive().asc()
      .orderByTaskPriority().asc()
      .orderByTenantId().asc();

    Filter filter = new FilterEntity("Task").setName("test").setQuery(query);
    when(filterServiceMock.getFilter(EXAMPLE_FILTER_ID)).thenReturn(filter);

    Response response = given()
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(SINGLE_FILTER_URL);

    // validate sorting content
    String content = response.asString();
    List<Map<String, Object>> sortings = from(content).getJsonObject("query.sorting");
    assertThat(sortings).hasSize(14);
    assertSorting(sortings.get(0), SORT_BY_PROCESS_INSTANCE_ID_VALUE, SORT_ORDER_ASC_VALUE);
    assertSorting(sortings.get(1), SORT_BY_CASE_INSTANCE_ID_VALUE, SORT_ORDER_ASC_VALUE);
    assertSorting(sortings.get(2), SORT_BY_DUE_DATE_VALUE, SORT_ORDER_ASC_VALUE);
    assertSorting(sortings.get(3), SORT_BY_FOLLOW_UP_VALUE, SORT_ORDER_ASC_VALUE);
    assertSorting(sortings.get(4), SORT_BY_EXECUTION_ID_VALUE, SORT_ORDER_ASC_VALUE);
    assertSorting(sortings.get(5), SORT_BY_CASE_EXECUTION_ID_VALUE, SORT_ORDER_ASC_VALUE);
    assertSorting(sortings.get(6), SORT_BY_ASSIGNEE_VALUE, SORT_ORDER_ASC_VALUE);
    assertSorting(sortings.get(7), SORT_BY_CREATE_TIME_VALUE, SORT_ORDER_ASC_VALUE);
    assertSorting(sortings.get(8), SORT_BY_DESCRIPTION_VALUE, SORT_ORDER_ASC_VALUE);
    assertSorting(sortings.get(9), SORT_BY_ID_VALUE, SORT_ORDER_ASC_VALUE);
    assertSorting(sortings.get(10), SORT_BY_NAME_VALUE, SORT_ORDER_ASC_VALUE);
    assertSorting(sortings.get(11), SORT_BY_NAME_CASE_INSENSITIVE_VALUE, SORT_ORDER_ASC_VALUE);
    assertSorting(sortings.get(12), SORT_BY_PRIORITY_VALUE, SORT_ORDER_ASC_VALUE);
    assertSorting(sortings.get(13), SORT_BY_TENANT_ID_VALUE, SORT_ORDER_ASC_VALUE);
  }

  @Test
  public void testGetFilterWithVariableTypeSorting() {
    TaskQuery query = new TaskQueryImpl()
      .orderByExecutionVariable("foo", ValueType.STRING).asc()
      .orderByProcessVariable("foo", ValueType.STRING).asc()
      .orderByTaskVariable("foo", ValueType.STRING).asc()
      .orderByCaseExecutionVariable("foo", ValueType.STRING).asc()
      .orderByCaseInstanceVariable("foo", ValueType.STRING).asc();

    Filter filter = new FilterEntity("Task").setName("test").setQuery(query);
    when(filterServiceMock.getFilter(EXAMPLE_FILTER_ID)).thenReturn(filter);

    Response response = given()
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(SINGLE_FILTER_URL);

    // validate sorting content
    String content = response.asString();
    List<Map<String, Object>> sortings = from(content).getJsonObject("query.sorting");
    assertThat(sortings).hasSize(5);
    assertSorting(sortings.get(0), SORT_BY_EXECUTION_VARIABLE, SORT_ORDER_ASC_VALUE, "foo", ValueType.STRING);
    assertSorting(sortings.get(1), SORT_BY_PROCESS_VARIABLE, SORT_ORDER_ASC_VALUE, "foo", ValueType.STRING);
    assertSorting(sortings.get(2), SORT_BY_TASK_VARIABLE, SORT_ORDER_ASC_VALUE, "foo", ValueType.STRING);
    assertSorting(sortings.get(3), SORT_BY_CASE_EXECUTION_VARIABLE, SORT_ORDER_ASC_VALUE, "foo", ValueType.STRING);
    assertSorting(sortings.get(4), SORT_BY_CASE_INSTANCE_VARIABLE, SORT_ORDER_ASC_VALUE, "foo", ValueType.STRING);
  }

  @Test
  public void testGetFilterWithVariableValueTypeSorting() {
    TaskQuery query = new TaskQueryImpl()
      .orderByExecutionVariable("foo", ValueType.STRING).asc()
      .orderByExecutionVariable("foo", ValueType.INTEGER).asc()
      .orderByExecutionVariable("foo", ValueType.SHORT).asc()
      .orderByExecutionVariable("foo", ValueType.DATE).asc()
      .orderByExecutionVariable("foo", ValueType.BOOLEAN).asc()
      .orderByExecutionVariable("foo", ValueType.LONG).asc()
      .orderByExecutionVariable("foo", ValueType.DOUBLE).asc();

    Filter filter = new FilterEntity("Task").setName("test").setQuery(query);
    when(filterServiceMock.getFilter(EXAMPLE_FILTER_ID)).thenReturn(filter);

    Response response = given()
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(SINGLE_FILTER_URL);

    // validate sorting content
    String content = response.asString();
    List<Map<String, Object>> sortings = from(content).getJsonObject("query.sorting");
    assertThat(sortings).hasSize(7);
    assertSorting(sortings.get(0), SORT_BY_EXECUTION_VARIABLE, SORT_ORDER_ASC_VALUE, "foo", ValueType.STRING);
    assertSorting(sortings.get(1), SORT_BY_EXECUTION_VARIABLE, SORT_ORDER_ASC_VALUE, "foo", ValueType.INTEGER);
    assertSorting(sortings.get(2), SORT_BY_EXECUTION_VARIABLE, SORT_ORDER_ASC_VALUE, "foo", ValueType.SHORT);
    assertSorting(sortings.get(3), SORT_BY_EXECUTION_VARIABLE, SORT_ORDER_ASC_VALUE, "foo", ValueType.DATE);
    assertSorting(sortings.get(4), SORT_BY_EXECUTION_VARIABLE, SORT_ORDER_ASC_VALUE, "foo", ValueType.BOOLEAN);
    assertSorting(sortings.get(5), SORT_BY_EXECUTION_VARIABLE, SORT_ORDER_ASC_VALUE, "foo", ValueType.LONG);
    assertSorting(sortings.get(6), SORT_BY_EXECUTION_VARIABLE, SORT_ORDER_ASC_VALUE, "foo", ValueType.DOUBLE);
  }

  @Test
  public void testGetFilterWithVariableSortOrderSorting() {
    TaskQuery query = new TaskQueryImpl()
      .orderByExecutionVariable("foo", ValueType.STRING).asc()
      .orderByExecutionVariable("foo", ValueType.STRING).desc();

    Filter filter = new FilterEntity("Task").setName("test").setQuery(query);
    when(filterServiceMock.getFilter(EXAMPLE_FILTER_ID)).thenReturn(filter);

    Response response = given()
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(SINGLE_FILTER_URL);

    // validate sorting content
    String content = response.asString();
    List<Map<String, Object>> sortings = from(content).getJsonObject("query.sorting");
    assertThat(sortings).hasSize(2);
    assertSorting(sortings.get(0), SORT_BY_EXECUTION_VARIABLE, SORT_ORDER_ASC_VALUE, "foo", ValueType.STRING);
    assertSorting(sortings.get(1), SORT_BY_EXECUTION_VARIABLE, SORT_ORDER_DESC_VALUE, "foo", ValueType.STRING);
  }

  protected void assertSorting(Map<String, Object> sorting, String sortBy, String sortOrder) {
    assertSorting(sorting, sortBy, sortOrder, null, null);
  }

  @SuppressWarnings("unchecked")
  protected void assertSorting(Map<String, Object> sorting, String sortBy, String sortOrder, String parametersVariable, ValueType parametersType) {
    assertThat(sorting.get("sortBy")).isEqualTo(sortBy);
    assertThat(sorting.get("sortOrder")).isEqualTo(sortOrder);
    if (parametersVariable == null) {
      assertThat(sorting.containsKey("parameters")).isFalse();
    }
    else {
      Map<String, Object> parameters = (Map<String, Object>) sorting.get("parameters");
      assertThat(parameters.get("variable")).isEqualTo(parametersVariable);
      assertThat(parameters.get("type")).isEqualTo(VariableValueDto.toRestApiTypeName(parametersType.getName()));
    }
  }

  @Test
  public void testGetFilterWithFollowUpBeforeOrNotExistentExpression() {
    TaskQueryImpl query = new TaskQueryImpl();
    query.followUpBeforeOrNotExistentExpression("#{now()}");
    Filter filter = new FilterEntity("Task").setName("test").setQuery(query);
    when(filterServiceMock.getFilter(EXAMPLE_FILTER_ID)).thenReturn(filter);

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("query.followUpBeforeOrNotExistentExpression", equalTo("#{now()}"))
    .when()
        .get(SINGLE_FILTER_URL);
  }

  @Test
  public void testCreateFilter() {
    Map<String, Object> json = toFilterRequest(MockProvider.createMockFilter());

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("id", notNullValue())
    .when()
      .post(CREATE_FILTER_URL);

    verify(filterServiceMock).newTaskFilter();
    verify(filterServiceMock).saveFilter(eq(filterMock));
  }

  @Test
  public void testCreateInvalidFilter() {
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(CREATE_FILTER_URL);
  }

  @Test
  public void testUpdateFilter() {
    Map<String, Object> json = toFilterRequest(MockProvider.createMockFilter());

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(SINGLE_FILTER_URL);

    verify(filterServiceMock).getFilter(eq(EXAMPLE_FILTER_ID));
    verify(filterServiceMock).saveFilter(eq(filterMock));
  }

  @Test
  public void testInvalidResourceType() {
    Map<String, Object> json = toFilterRequest(MockProvider.createMockFilter());
    json.put("resourceType", "invalid");

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .put(SINGLE_FILTER_URL);
  }

  protected Map<String, Object> toFilterRequest(Filter filter) {
    Map<String, Object> json = new HashMap<String, Object>();

    json.put("id", filter.getId());
    json.put("name", filter.getName());
    json.put("owner", filter.getOwner());
    json.put("properties", filter.getProperties());
    json.put("resourceType", filter.getResourceType());

    // should not use the dto classes in client-side tests
    json.put("query", TaskQueryDto.fromQuery(filter.getQuery()));

    return json;
  }

  @Test
  public void testUpdateNonExistingFilter() {
    given()
      .pathParam("id", MockProvider.NON_EXISTING_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
    .when()
      .put(SINGLE_FILTER_URL);
  }

  @Test
  public void testUpdateInvalidFilter() {
    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .put(SINGLE_FILTER_URL);
  }

  @Test
  public void testDeleteFilter() {
    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_FILTER_URL);

    verify(filterServiceMock).deleteFilter(eq(EXAMPLE_FILTER_ID));
  }

  @Test
  public void testDeleteNonExistingFilter() {
    given()
      .pathParam("id", MockProvider.NON_EXISTING_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
    .when()
      .delete(SINGLE_FILTER_URL);
  }

  @Test
  public void testExecuteSingleResult() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testEmptySingleResult() {
    when(filterServiceMock.singleResult(anyString(), any(Query.class))).thenReturn(null);

    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testInvalidSingleResult() {
    doThrow(new ProcessEngineException("More than one result found"))
      .when(filterServiceMock).singleResult(anyString(), any(Query.class));

    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteHalSingleResult() {
    given()
      .header(ACCEPT_HAL_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID),isNull(Query.class));
  }

  @Test
  public void testEmptyHalSingleResult() {
    when(filterServiceMock.singleResult(anyString(), any(Query.class))).thenReturn(null);

    given()
      .header(ACCEPT_HAL_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("_links.size()", equalTo(0))
      .body("_embedded.size()", equalTo(0))
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testInvalidHalSingleResult() {
    doThrow(new ProcessEngineException("More than one result found"))
      .when(filterServiceMock).singleResult(anyString(), any(Query.class));

    given()
      .header(ACCEPT_HAL_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteSingleResultOfNonExistingFilter() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", MockProvider.NON_EXISTING_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(MockProvider.NON_EXISTING_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteSingleResultAsPost() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID), any(Query.class));
  }

  @Test
  public void testExecuteHalSingleResultAsPost() {
    given()
      .header(ACCEPT_HAL_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID), any(Query.class));
  }

  @Test
  public void testExecuteSingleResultInvalidWithExtendingQuery() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(invalidExtendingQuery)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(EXECUTE_SINGLE_RESULT_FILTER_URL);
  }

  @Test
  public void testExecuteSingleResultWithExtendingQuery() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(extendingQueryDto)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID),
        argThat(hasName(MockProvider.EXAMPLE_TASK_NAME)));
  }

  @Test
  public void testExecuteSingleResultWithExtendingOrQuery() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(extendingOrQueryDto)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_SINGLE_RESULT_FILTER_URL);

    ArgumentCaptor<TaskQueryImpl> argument = ArgumentCaptor.forClass(TaskQueryImpl.class);
    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID), argument.capture());
    assertEquals(MockProvider.EXAMPLE_TASK_DESCRIPTION, argument.getValue().getQueries().get(1).getDescription());
    assertEquals(MockProvider.EXAMPLE_TASK_NAME, argument.getValue().getQueries().get(2).getName());
  }

  @Test
  public void testExecuteList() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testEmptyList() {
    when(filterServiceMock.list(anyString(), any(Query.class))).thenReturn(Collections.emptyList());

    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(0))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteHalList() {
    given()
      .header(ACCEPT_HAL_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testEmptyHalList() {
    when(filterServiceMock.list(anyString(), any(Query.class))).thenReturn(Collections.emptyList());
    when(filterServiceMock.count(anyString(), any(Query.class))).thenReturn(0l);

    given()
      .header(ACCEPT_HAL_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("_links.size()", equalTo(0))
      .body("_embedded.size()", equalTo(0))
      .body("count", equalTo(0))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteListOfNonExistingFilter() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", MockProvider.NON_EXISTING_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(MockProvider.NON_EXISTING_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteListWithPagination() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .queryParams("firstResult", 1)
      .queryParams("maxResults", 2)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).listPage(eq(EXAMPLE_FILTER_ID), isNull(Query.class), eq(1), eq(2));
  }

  @Test
  public void testExecuteListAsPost() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .post(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(EXAMPLE_FILTER_ID), any(Query.class));
  }

  @Test
  public void testExecuteHalListAsPost() {
    given()
      .header(ACCEPT_HAL_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .post(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(EXAMPLE_FILTER_ID), any(Query.class));
  }

  @Test
  public void testExecuteListAsPostWithPagination() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .queryParams("firstResult", 1)
      .queryParams("maxResults", 2)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .post(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).listPage(eq(EXAMPLE_FILTER_ID), isNull(Query.class), eq(1), eq(2));
  }

  @Test
  public void testExecuteListWithExtendingQuery() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(extendingQueryDto)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .post(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(EXAMPLE_FILTER_ID), argThat(hasName(MockProvider.EXAMPLE_TASK_NAME)));
  }

  @Test
  public void testExecuteListWithExtendingQueryWithPagination() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .queryParams("firstResult", 1)
      .queryParams("maxResults", 2)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(extendingQueryDto)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .post(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).listPage(eq(EXAMPLE_FILTER_ID), argThat(hasName(MockProvider.EXAMPLE_TASK_NAME)), eq(1), eq(2));
  }

  @Test
  public void testExecuteListWithInvalidExtendingQuery() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(invalidExtendingQuery)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(EXECUTE_LIST_FILTER_URL);
  }

  @Test
  public void testExecuteCount() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(EXECUTE_COUNT_FILTER_URL);

    verify(filterServiceMock).count(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteCountOfNonExistingFilter() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", MockProvider.NON_EXISTING_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
    .when()
      .get(EXECUTE_COUNT_FILTER_URL);

    verify(filterServiceMock).count(MockProvider.NON_EXISTING_ID, null);
  }

  @Test
  public void testExecuteCountAsPost() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .post(EXECUTE_COUNT_FILTER_URL);

    verify(filterServiceMock).count(eq(EXAMPLE_FILTER_ID), any(Query.class));
  }

  @Test
  public void testExecuteCountWithExtendingQuery() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(extendingQueryDto)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .post(EXECUTE_COUNT_FILTER_URL);

    verify(filterServiceMock).count(eq(EXAMPLE_FILTER_ID), argThat(hasName(MockProvider.EXAMPLE_TASK_NAME)));
  }

  @Test
  public void testExecuteCountWithInvalidExtendingQuery() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(invalidExtendingQuery)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(EXECUTE_COUNT_FILTER_URL);
  }


  @Test
  public void testAnonymousFilterOptions() {
    String fullFilterUrl = "http://localhost:" + PORT + FILTER_URL;

    // anonymity means the identityService returns a null authentication, so no need to mock here

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
      .then()
        .statusCode(Status.OK.getStatusCode())

        .body("links.size()", is(3))

        .body("links[0].href", equalTo(fullFilterUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("list"))

        .body("links[1].href", equalTo(fullFilterUrl + "/count"))
        .body("links[1].method", equalTo(HttpMethod.GET))
        .body("links[1].rel", equalTo("count"))

        .body("links[2].href", equalTo(fullFilterUrl + "/create"))
        .body("links[2].method", equalTo(HttpMethod.POST))
        .body("links[2].rel", equalTo("create"))

    .when()
        .options(FILTER_URL);

    verify(identityServiceMock, times(1)).getCurrentAuthentication();

  }

  @Test
  public void testRestrictedFilterOptions() {
    String fullFilterUrl = "http://localhost:" + PORT + FILTER_URL;

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);

    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, FILTER, ANY)).thenReturn(false);

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
      .then()
        .statusCode(Status.OK.getStatusCode())

        .body("links.size()", is(2))

        .body("links[0].href", equalTo(fullFilterUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("list"))

        .body("links[1].href", equalTo(fullFilterUrl + "/count"))
        .body("links[1].method", equalTo(HttpMethod.GET))
        .body("links[1].rel", equalTo("count"))

    .when()
        .options(FILTER_URL);

    verify(identityServiceMock, times(1)).getCurrentAuthentication();

  }

  @Test
  public void testFilterOptionsWithDisabledAuthorization() {
    String fullFilterUrl = "http://localhost:" + PORT + FILTER_URL;

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(false);

    given()
    .then()
      .statusCode(Status.OK.getStatusCode())

      .body("links.size()", is(3))

      .body("links[0].href", equalTo(fullFilterUrl))
      .body("links[0].method", equalTo(HttpMethod.GET))
      .body("links[0].rel", equalTo("list"))

      .body("links[1].href", equalTo(fullFilterUrl + "/count"))
      .body("links[1].method", equalTo(HttpMethod.GET))
      .body("links[1].rel", equalTo("count"))

      .body("links[2].href", equalTo(fullFilterUrl + "/create"))
      .body("links[2].method", equalTo(HttpMethod.POST))
      .body("links[2].rel", equalTo("create"))

    .when()
      .options(FILTER_URL);

    verifyNoAuthorizationCheckPerformed();
  }

  @Test
  public void testAnonymousFilterResourceOptions() {
    String fullFilterUrl = "http://localhost:" + PORT + FILTER_URL + "/" + EXAMPLE_FILTER_ID;

    // anonymity means the identityService returns a null authentication, so no need to mock here

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
        .pathParam("id", EXAMPLE_FILTER_ID)
    .then()
        .statusCode(Status.OK.getStatusCode())

        .body("links.size()", is(9))

        .body("links[0].href", equalTo(fullFilterUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("self"))

        .body("links[1].href", equalTo(fullFilterUrl + "/singleResult"))
        .body("links[1].method", equalTo(HttpMethod.GET))
        .body("links[1].rel", equalTo("singleResult"))

        .body("links[2].href", equalTo(fullFilterUrl + "/singleResult"))
        .body("links[2].method", equalTo(HttpMethod.POST))
        .body("links[2].rel", equalTo("singleResult"))

        .body("links[3].href", equalTo(fullFilterUrl + "/list"))
        .body("links[3].method", equalTo(HttpMethod.GET))
        .body("links[3].rel", equalTo("list"))

        .body("links[4].href", equalTo(fullFilterUrl + "/list"))
        .body("links[4].method", equalTo(HttpMethod.POST))
        .body("links[4].rel", equalTo("list"))

        .body("links[5].href", equalTo(fullFilterUrl + "/count"))
        .body("links[5].method", equalTo(HttpMethod.GET))
        .body("links[5].rel", equalTo("count"))

        .body("links[6].href", equalTo(fullFilterUrl + "/count"))
        .body("links[6].method", equalTo(HttpMethod.POST))
        .body("links[6].rel", equalTo("count"))

        .body("links[7].href", equalTo(fullFilterUrl))
        .body("links[7].method", equalTo(HttpMethod.DELETE))
        .body("links[7].rel", equalTo("delete"))

        .body("links[8].href", equalTo(fullFilterUrl))
        .body("links[8].method", equalTo(HttpMethod.PUT))
        .body("links[8].rel", equalTo("update"))

    .when()
        .options(SINGLE_FILTER_URL);

    verify(identityServiceMock, times(3)).getCurrentAuthentication();

  }

  @Test
  public void testFilterResourceOptionsUnauthorized() {
    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);

    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, READ, FILTER, EXAMPLE_FILTER_ID)).thenReturn(false);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, FILTER, EXAMPLE_FILTER_ID)).thenReturn(false);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, FILTER, EXAMPLE_FILTER_ID)).thenReturn(false);

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
        .pathParam("id", EXAMPLE_FILTER_ID)
    .then()
        .statusCode(Status.OK.getStatusCode())

        .body("links.size()", is(0))

    .when()
        .options(SINGLE_FILTER_URL);

    verify(identityServiceMock, times(3)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, READ, FILTER, EXAMPLE_FILTER_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, FILTER, EXAMPLE_FILTER_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, FILTER, EXAMPLE_FILTER_ID);

  }

  @Test
  public void testFilterResourceOptionsUpdateUnauthorized() {
    String fullFilterUrl = "http://localhost:" + PORT + FILTER_URL + "/" + EXAMPLE_FILTER_ID;

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, READ, FILTER, EXAMPLE_FILTER_ID)).thenReturn(true);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, FILTER, EXAMPLE_FILTER_ID)).thenReturn(true);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, FILTER, EXAMPLE_FILTER_ID)).thenReturn(false);

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
        .pathParam("id", EXAMPLE_FILTER_ID)
    .then()
        .statusCode(Status.OK.getStatusCode())

        .body("links.size()", is(8))

        .body("links[0].href", equalTo(fullFilterUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("self"))

        .body("links[1].href", equalTo(fullFilterUrl + "/singleResult"))
        .body("links[1].method", equalTo(HttpMethod.GET))
        .body("links[1].rel", equalTo("singleResult"))

        .body("links[2].href", equalTo(fullFilterUrl + "/singleResult"))
        .body("links[2].method", equalTo(HttpMethod.POST))
        .body("links[2].rel", equalTo("singleResult"))

        .body("links[3].href", equalTo(fullFilterUrl + "/list"))
        .body("links[3].method", equalTo(HttpMethod.GET))
        .body("links[3].rel", equalTo("list"))

        .body("links[4].href", equalTo(fullFilterUrl + "/list"))
        .body("links[4].method", equalTo(HttpMethod.POST))
        .body("links[4].rel", equalTo("list"))

        .body("links[5].href", equalTo(fullFilterUrl + "/count"))
        .body("links[5].method", equalTo(HttpMethod.GET))
        .body("links[5].rel", equalTo("count"))

        .body("links[6].href", equalTo(fullFilterUrl + "/count"))
        .body("links[6].method", equalTo(HttpMethod.POST))
        .body("links[6].rel", equalTo("count"))

        .body("links[7].href", equalTo(fullFilterUrl))
        .body("links[7].method", equalTo(HttpMethod.DELETE))
        .body("links[7].rel", equalTo("delete"))

    .when()
        .options(SINGLE_FILTER_URL);

    verify(identityServiceMock, times(3)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, READ, FILTER, EXAMPLE_FILTER_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, FILTER, EXAMPLE_FILTER_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, FILTER, EXAMPLE_FILTER_ID);

  }

  @Test
  public void testFilterResourceOptionsWithAuthorizationDisabled() {
    String fullFilterUrl = "http://localhost:" + PORT + FILTER_URL + "/" + EXAMPLE_FILTER_ID;

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(false);

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then()
      .statusCode(Status.OK.getStatusCode())

      .body("links.size()", is(9))

      .body("links[0].href", equalTo(fullFilterUrl))
      .body("links[0].method", equalTo(HttpMethod.GET))
      .body("links[0].rel", equalTo("self"))

      .body("links[1].href", equalTo(fullFilterUrl + "/singleResult"))
      .body("links[1].method", equalTo(HttpMethod.GET))
      .body("links[1].rel", equalTo("singleResult"))

      .body("links[2].href", equalTo(fullFilterUrl + "/singleResult"))
      .body("links[2].method", equalTo(HttpMethod.POST))
      .body("links[2].rel", equalTo("singleResult"))

      .body("links[3].href", equalTo(fullFilterUrl + "/list"))
      .body("links[3].method", equalTo(HttpMethod.GET))
      .body("links[3].rel", equalTo("list"))

      .body("links[4].href", equalTo(fullFilterUrl + "/list"))
      .body("links[4].method", equalTo(HttpMethod.POST))
      .body("links[4].rel", equalTo("list"))

      .body("links[5].href", equalTo(fullFilterUrl + "/count"))
      .body("links[5].method", equalTo(HttpMethod.GET))
      .body("links[5].rel", equalTo("count"))

      .body("links[6].href", equalTo(fullFilterUrl + "/count"))
      .body("links[6].method", equalTo(HttpMethod.POST))
      .body("links[6].rel", equalTo("count"))

      .body("links[7].href", equalTo(fullFilterUrl))
      .body("links[7].method", equalTo(HttpMethod.DELETE))
      .body("links[7].rel", equalTo("delete"))

      .body("links[8].href", equalTo(fullFilterUrl))
      .body("links[8].method", equalTo(HttpMethod.PUT))
      .body("links[8].rel", equalTo("update"))

    .when()
      .options(SINGLE_FILTER_URL);

    verifyNoAuthorizationCheckPerformed();
  }

  @Test
  public void testHalTaskQueryWithWrongFormatVariablesProperties() {
    // mock properties with variable name list in wrong format
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("variables", "foo");
    Filter filter =  mockFilter().properties(properties).build();
    when(filterServiceMock.getFilter(eq(EXAMPLE_FILTER_ID))).thenReturn(filter);

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .header(ACCEPT_HAL_HEADER)
    .expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock, times(1)).getFilter(eq(EXAMPLE_FILTER_ID));
    verify(variableInstanceQueryMock, never()).variableScopeIdIn((String) anyVararg());
    verify(variableInstanceQueryMock, never()).variableNameIn((String) anyVararg());
    verify(variableInstanceQueryMock, never()).list();
  }

  @Test
  public void testHalTaskQueryWithEmptyVariablesProperties() {
    // mock properties with variable name list in wrong format
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("variables", Collections.emptyList());
    Filter filter =  mockFilter().properties(properties).build();
    when(filterServiceMock.getFilter(eq(EXAMPLE_FILTER_ID))).thenReturn(filter);

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .header(ACCEPT_HAL_HEADER)
    .expect()
      .statusCode(Status.OK.getStatusCode())
      .body("_embedded", equalTo(null))
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock, times(1)).getFilter(eq(EXAMPLE_FILTER_ID));
    verify(variableInstanceQueryMock, never()).variableScopeIdIn((String) anyVararg());
    verify(variableInstanceQueryMock, never()).variableNameIn((String) anyVararg());
    verify(variableInstanceQueryMock, never()).list();
  }

  @Test
  public void testHalTaskQueryWithVariableNotSetOnTask() {
    // mock filter with variable names set
    mockFilterWithVariableNames();

    // mock resulting task
    Task task = createTaskMock(TASK_A_ID, PROCESS_INSTANCE_A_ID, EXECUTION_A_ID, null, null);
    when(filterServiceMock.singleResult(eq(EXAMPLE_FILTER_ID), any(Query.class))).thenReturn(task);

    // mock variable instances
    List<VariableInstance> variableInstances = Arrays.asList(
      createExecutionVariableInstanceMock("foo", stringValue("execution"), EXECUTION_B_ID),
      createExecutionVariableInstanceMock("execution", stringValue("bar"), EXECUTION_B_ID),
      createTaskVariableInstanceMock("foo", stringValue("task"), TASK_B_ID),
      createTaskVariableInstanceMock("task", stringValue("bar"), TASK_B_ID)
    );
    when(variableInstanceQueryMock.list()).thenReturn(variableInstances);

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .header(ACCEPT_HAL_HEADER)
    .expect()
      .statusCode(Status.OK.getStatusCode())
      .body("_embedded.containsKey('variable')", is(true))
      .body("_embedded.variable.size", equalTo(0))
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock, times(1)).getFilter(eq(EXAMPLE_FILTER_ID));
    verify(variableInstanceQueryMock, times(1)).variableScopeIdIn((String) anyVararg());
    verify(variableInstanceQueryMock).variableScopeIdIn(TASK_A_ID, EXECUTION_A_ID, PROCESS_INSTANCE_A_ID);
    verify(variableInstanceQueryMock, times(1)).variableNameIn((String) anyVararg());
    verify(variableInstanceQueryMock).variableNameIn("foo", "bar");
    verify(variableInstanceQueryMock, times(1)).list();
  }

  @Test
  public void testHalTaskQueryWithAdditionalVariables() {
    // mock filter with variable names set
    mockFilterWithVariableNames();

    // mock resulting task
    Task task = createTaskMock(TASK_A_ID, PROCESS_INSTANCE_A_ID, EXECUTION_A_ID, CASE_INSTANCE_A_ID, CASE_EXECUTION_A_ID);
    when(filterServiceMock.singleResult(eq(EXAMPLE_FILTER_ID), any(Query.class))).thenReturn(task);

    // mock variable instances
    List<VariableInstance> variableInstances = Arrays.asList(
      createProcessInstanceVariableInstanceMock("foo", stringValue("processInstance"), PROCESS_INSTANCE_A_ID),
      createProcessInstanceVariableInstanceMock("processInstance", stringValue("bar"), PROCESS_INSTANCE_A_ID),
      createExecutionVariableInstanceMock("foo", stringValue("execution"), EXECUTION_A_ID),
      createExecutionVariableInstanceMock("execution", stringValue("bar"), EXECUTION_A_ID),
      createTaskVariableInstanceMock("foo", stringValue("task"), TASK_A_ID),
      createTaskVariableInstanceMock("task", stringValue("bar"), TASK_A_ID),
      createCaseInstanceVariableInstanceMock("foo", stringValue("caseInstance"), CASE_INSTANCE_A_ID),
      createCaseInstanceVariableInstanceMock("caseInstance", stringValue("bar"), CASE_INSTANCE_A_ID),
      createCaseExecutionVariableInstanceMock("foo", stringValue("caseExecution"), CASE_EXECUTION_A_ID),
      createCaseExecutionVariableInstanceMock("caseExecution", stringValue("bar"), CASE_EXECUTION_A_ID)
    );
    when(variableInstanceQueryMock.list()).thenReturn(variableInstances);

    Response response = given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .header(ACCEPT_HAL_HEADER)
    .expect()
      .statusCode(Status.OK.getStatusCode())
      .body("_embedded.containsKey('variable')", is(true))
      .body("_embedded.variable.size", equalTo(6))
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock, times(1)).getFilter(eq(EXAMPLE_FILTER_ID));
    verify(variableInstanceQueryMock, times(1)).variableScopeIdIn((String) anyVararg());
    verify(variableInstanceQueryMock).variableScopeIdIn(TASK_A_ID, EXECUTION_A_ID, PROCESS_INSTANCE_A_ID, CASE_EXECUTION_A_ID, CASE_INSTANCE_A_ID);
    verify(variableInstanceQueryMock, times(1)).variableNameIn((String) anyVararg());
    verify(variableInstanceQueryMock).variableNameIn("foo", "bar");
    verify(variableInstanceQueryMock, times(1)).list();
    verify(variableInstanceQueryMock, times(1)).disableBinaryFetching();
    verify(variableInstanceQueryMock, times(1)).disableCustomObjectDeserialization();

    String content = response.asString();
    List<Map<String, Object>> variables = from(content).getJsonObject("_embedded.variable");

    // task variable 'foo'
    verifyTaskVariableValue(variables.get(0), "foo", "task", TASK_A_ID);
    // task variable 'task'
    verifyTaskVariableValue(variables.get(1), "task", "bar", TASK_A_ID);
    // execution variable 'execution'
    verifyExecutionVariableValue(variables.get(2), "execution", "bar", EXECUTION_A_ID);
    // process instance variable 'processInstance'
    verifyProcessInstanceVariableValue(variables.get(3), "processInstance", "bar", PROCESS_INSTANCE_A_ID);
    // caseExecution variable 'caseExecution'
    verifyCaseExecutionVariableValue(variables.get(4), "caseExecution", "bar", CASE_EXECUTION_A_ID);
    // case instance variable 'caseInstance'
    verifyCaseInstanceVariableValue(variables.get(5), "caseInstance", "bar", CASE_INSTANCE_A_ID);
  }

  @Test
  public void testHalTaskListQueryWithAdditionalVariables() {
    // mock filter with variable names set
    mockFilterWithVariableNames();

    // mock resulting task
    List<Task> tasks = Arrays.asList(
      createTaskMock(TASK_A_ID, PROCESS_INSTANCE_A_ID, EXECUTION_A_ID, null, null),
      createTaskMock(TASK_B_ID, PROCESS_INSTANCE_A_ID, EXECUTION_B_ID, null, null),
      createTaskMock(TASK_C_ID, null, null, CASE_INSTANCE_A_ID, CASE_EXECUTION_A_ID)
    );
    when(filterServiceMock.list(eq(EXAMPLE_FILTER_ID), any(Query.class))).thenReturn(tasks);

    // mock variable instances
    List<VariableInstance> variableInstances = Arrays.asList(
      createProcessInstanceVariableInstanceMock("foo", stringValue(PROCESS_INSTANCE_A_ID), PROCESS_INSTANCE_A_ID),
      createProcessInstanceVariableInstanceMock(PROCESS_INSTANCE_A_ID, stringValue("bar"), PROCESS_INSTANCE_A_ID),
      createExecutionVariableInstanceMock("foo", stringValue(EXECUTION_A_ID), EXECUTION_A_ID),
      createExecutionVariableInstanceMock(EXECUTION_A_ID, stringValue("bar"), EXECUTION_A_ID),
      createExecutionVariableInstanceMock("foo", stringValue(EXECUTION_B_ID), EXECUTION_B_ID),
      createExecutionVariableInstanceMock(EXECUTION_B_ID, stringValue("bar"), EXECUTION_B_ID),
      createTaskVariableInstanceMock("foo", stringValue(TASK_A_ID), TASK_A_ID),
      createTaskVariableInstanceMock(TASK_A_ID, stringValue("bar"), TASK_A_ID),
      createTaskVariableInstanceMock("foo", stringValue(TASK_B_ID), TASK_B_ID),
      createTaskVariableInstanceMock(TASK_B_ID, stringValue("bar"), TASK_B_ID),
      createTaskVariableInstanceMock("foo", stringValue(TASK_C_ID), TASK_C_ID),
      createTaskVariableInstanceMock(TASK_C_ID, stringValue("bar"), TASK_C_ID),
      createCaseInstanceVariableInstanceMock("foo", stringValue(CASE_INSTANCE_A_ID), CASE_INSTANCE_A_ID),
      createCaseInstanceVariableInstanceMock(CASE_INSTANCE_A_ID, stringValue("bar"), CASE_INSTANCE_A_ID),
      createCaseExecutionVariableInstanceMock("foo", stringValue(CASE_EXECUTION_A_ID), CASE_EXECUTION_A_ID),
      createCaseExecutionVariableInstanceMock(CASE_EXECUTION_A_ID, stringValue("bar"), CASE_EXECUTION_A_ID)
    );
    when(variableInstanceQueryMock.list()).thenReturn(variableInstances);

    Response response = given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .header(ACCEPT_HAL_HEADER)
    .expect()
      .statusCode(Status.OK.getStatusCode())
      .body("_embedded.task.size", equalTo(3))
      .body("_embedded.task.any { it._embedded.containsKey('variable') }", is(true))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock, times(1)).getFilter(eq(EXAMPLE_FILTER_ID));
    verify(variableInstanceQueryMock, times(1)).variableScopeIdIn((String) anyVararg());
    verify(variableInstanceQueryMock).variableScopeIdIn(TASK_A_ID, EXECUTION_A_ID, PROCESS_INSTANCE_A_ID, TASK_B_ID, EXECUTION_B_ID, TASK_C_ID, CASE_EXECUTION_A_ID, CASE_INSTANCE_A_ID);
    verify(variableInstanceQueryMock, times(1)).variableNameIn((String) anyVararg());
    verify(variableInstanceQueryMock).variableNameIn("foo", "bar");
    verify(variableInstanceQueryMock, times(1)).list();

    String content = response.asString();
    List<Map<String, Object>> taskList = from(content).getList("_embedded.task");

    // task A
    List<Map<String, Object>> variables = getEmbeddedTaskVariables(taskList.get(0));
    assertThat(variables).hasSize(4);

    // task variable 'foo'
    verifyTaskVariableValue(variables.get(0), "foo", TASK_A_ID, TASK_A_ID);
    // task variable 'taskA'
    verifyTaskVariableValue(variables.get(1), TASK_A_ID, "bar", TASK_A_ID);
    // execution variable 'executionA'
    verifyExecutionVariableValue(variables.get(2), EXECUTION_A_ID, "bar", EXECUTION_A_ID);
    // process instance variable 'processInstanceA'
    verifyProcessInstanceVariableValue(variables.get(3), PROCESS_INSTANCE_A_ID, "bar", PROCESS_INSTANCE_A_ID);

    // task B
    variables = getEmbeddedTaskVariables(taskList.get(1));
    assertThat(variables).hasSize(4);

    // task variable 'foo'
    verifyTaskVariableValue(variables.get(0), "foo", TASK_B_ID, TASK_B_ID);
    // task variable 'taskA'
    verifyTaskVariableValue(variables.get(1), TASK_B_ID, "bar", TASK_B_ID);
    // execution variable 'executionA'
    verifyExecutionVariableValue(variables.get(2), EXECUTION_B_ID, "bar", EXECUTION_B_ID);
    // process instance variable 'processInstanceA'
    verifyProcessInstanceVariableValue(variables.get(3), PROCESS_INSTANCE_A_ID, "bar", PROCESS_INSTANCE_A_ID);

    // task C
    variables = getEmbeddedTaskVariables(taskList.get(2));
    assertThat(variables).hasSize(4);

    // task variable 'foo'
    verifyTaskVariableValue(variables.get(0), "foo", TASK_C_ID, TASK_C_ID);
    // task variable 'taskC'
    verifyTaskVariableValue(variables.get(1), TASK_C_ID, "bar", TASK_C_ID);
    // case execution variable 'caseExecutionA'
    verifyCaseExecutionVariableValue(variables.get(2), CASE_EXECUTION_A_ID, "bar", CASE_EXECUTION_A_ID);
    // case instance variable 'caseInstanceA'
    verifyCaseInstanceVariableValue(variables.get(3), CASE_INSTANCE_A_ID, "bar", CASE_INSTANCE_A_ID);

  }

  @Test
  public void testHalTaskListCount() {
    // mock resulting task
    List<Task> tasks = Arrays.asList(
      createTaskMock(TASK_A_ID, PROCESS_INSTANCE_A_ID, EXECUTION_A_ID, null, null),
      createTaskMock(TASK_B_ID, PROCESS_INSTANCE_A_ID, EXECUTION_A_ID, null, null),
      createTaskMock(TASK_C_ID, PROCESS_INSTANCE_A_ID, EXECUTION_B_ID, null, null)
    );
    when(filterServiceMock.list(eq(EXAMPLE_FILTER_ID), any(Query.class))).thenReturn(tasks);
    when(filterServiceMock.listPage(eq(EXAMPLE_FILTER_ID), any(Query.class), eq(0), eq(2))).thenReturn(tasks.subList(0, 2));
    when(filterServiceMock.listPage(eq(EXAMPLE_FILTER_ID), any(Query.class), eq(5), eq(2))).thenReturn(Collections.emptyList());
    when(filterServiceMock.count(eq(EXAMPLE_FILTER_ID), any(Query.class))).thenReturn((long) tasks.size());

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .header(ACCEPT_HAL_HEADER)
    .then().expect()
      .body("_embedded.task.size", equalTo(3))
      .body("count", equalTo(3))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .queryParam("firstResult", 0)
      .queryParam("maxResults", 2)
      .header(ACCEPT_HAL_HEADER)
    .then().expect()
      .body("_embedded.task.size", equalTo(2))
      .body("count", equalTo(3))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .queryParam("firstResult", 5)
      .queryParam("maxResults", 2)
      .header(ACCEPT_HAL_HEADER)
    .then().expect()
      .body("_embedded.containsKey('task')", is(false))
      .body("count", equalTo(3))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);
  }

  @SuppressWarnings("unchecked")
  protected List<Map<String, Object>> getEmbeddedTaskVariables(Map<String, Object> task) {
    Map<String, Object> embedded = (Map<String, Object>) task.get("_embedded");
    assertThat(embedded).isNotNull();
    return (List<Map<String, Object>>) embedded.get("variable");
  }

  protected Filter mockFilterWithVariableNames() {
    // mock properties with variable name list (names are ignored but variable names list must not be empty)
    List<Map<String, String>> variables = new ArrayList<Map<String, String>>();
    Map<String, String> foo = new HashMap<String, String>();
    foo.put("name", "foo");
    foo.put("label", "Foo");
    variables.add(foo);
    Map<String, String> bar = new HashMap<String, String>();
    bar.put("name", "bar");
    bar.put("label", "Bar");
    variables.add(bar);

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("variables", variables);

    Filter filter = mockFilter().properties(properties).build();
    when(filterServiceMock.getFilter(eq(EXAMPLE_FILTER_ID))).thenReturn(filter);

    return filter;
  }

  protected Task createTaskMock(String taskId, String processInstanceId, String executionId, String caseInstanceId, String caseExecutionId) {
    return new MockTaskBuilder()
      .id(taskId)
      .processInstanceId(processInstanceId)
      .executionId(executionId)
      .caseInstanceId(caseInstanceId)
      .caseExecutionId(caseExecutionId)
      .build();
  }

  protected VariableInstance createTaskVariableInstanceMock(String name, TypedValue value, String taskId) {
    return createVariableInstanceMock(name, value, taskId, null, null, null, null);
  }

  protected VariableInstance createExecutionVariableInstanceMock(String name, TypedValue value, String executionId) {
    return createVariableInstanceMock(name, value, null, executionId, null, null, null);
  }

  protected VariableInstance createProcessInstanceVariableInstanceMock(String name, TypedValue value, String processInstanceId) {
    return createVariableInstanceMock(name, value, null, processInstanceId, processInstanceId, null, null);
  }

  protected VariableInstance createCaseExecutionVariableInstanceMock(String name, TypedValue value, String caseExecutionId) {
    return createVariableInstanceMock(name, value, null, null, null, caseExecutionId, null);
  }

  protected VariableInstance createCaseInstanceVariableInstanceMock(String name, TypedValue value, String caseInstanceId) {
    return createVariableInstanceMock(name, value, null, null, null, caseInstanceId, caseInstanceId);
  }

  protected VariableInstance createVariableInstanceMock(String name, TypedValue value, String taskId, String executionId, String processInstanceId, String caseExecutionId, String caseInstanceId) {
    return mockVariableInstance().name(name).typedValue(value).taskId(taskId)
      .executionId(executionId).processInstanceId(processInstanceId).caseExecutionId(caseExecutionId).caseInstanceId(caseInstanceId)
      .buildEntity();
  }

  protected void verifyTaskVariableValue(Map<String, Object> variable, String name, String value, String taskId) {
    verifyVariableValue(variable, name, value, TaskRestService.PATH, taskId, "localVariables");
  }

  protected void verifyExecutionVariableValue(Map<String, Object> variable, String name, String value, String executionId) {
    verifyVariableValue(variable, name, value, ExecutionRestService.PATH, executionId, "localVariables");
  }

  protected void verifyCaseExecutionVariableValue(Map<String, Object> variable, String name, String value, String caseExecutionId) {
    verifyVariableValue(variable, name, value, CaseExecutionRestService.PATH, caseExecutionId, "localVariables");
  }

  protected void verifyProcessInstanceVariableValue(Map<String, Object> variable, String name, String value, String processInstanceId) {
    verifyVariableValue(variable, name, value, ProcessInstanceRestService.PATH, processInstanceId, "variables");
  }

  protected void verifyCaseInstanceVariableValue(Map<String, Object> variable, String name, String value, String caseInstanceId) {
    verifyVariableValue(variable, name, value, CaseInstanceRestService.PATH, caseInstanceId, "variables");
  }

  @SuppressWarnings("unchecked")
  protected void verifyVariableValue(Map<String, Object> variable, String name, String value, String scopeResourcePath, String scopeId, String variablesName) {
    assertThat(variable.get("name")).isEqualTo(name);
    assertThat(variable.get("value")).isEqualTo(value);
    assertThat(variable.get("type")).isEqualTo("String");
    assertThat(variable.get("valueInfo")).isEqualTo(Collections.emptyMap());
    assertThat(variable.get("_embedded")).isNull();
    Map<String, Map<String, String>> links = (Map<String, Map<String, String>>) variable.get("_links");
    assertThat(links).hasSize(1);
    assertThat(links.get("self")).hasSize(1);
    assertThat(links.get("self").get("href")).isEqualTo(scopeResourcePath + "/" + scopeId + "/" + variablesName + "/" + name);
  }

  protected void verifyNoAuthorizationCheckPerformed() {
    verify(identityServiceMock, times(0)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(0)).isUserAuthorized(anyString(), anyListOf(String.class), any(Permission.class), any(Resource.class));
  }

}
