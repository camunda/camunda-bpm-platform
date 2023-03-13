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
package org.camunda.bpm.engine.rest;

import static io.restassured.RestAssured.given;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TASK_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.createMockBatch;
import static org.camunda.bpm.engine.rest.helper.MockProvider.createMockHistoricProcessInstance;
import static org.camunda.bpm.engine.rest.util.DateTimeUtils.DATE_FORMAT_WITH_TIMEZONE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.HistoryServiceImpl;
import org.camunda.bpm.engine.impl.ManagementServiceImpl;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.management.SetJobRetriesByProcessAsyncBuilder;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceSuspensionStateDto;
import org.camunda.bpm.engine.rest.dto.runtime.batch.CorrelationMessageAsyncDto;
import org.camunda.bpm.engine.rest.dto.runtime.batch.DeleteProcessInstancesDto;
import org.camunda.bpm.engine.rest.dto.runtime.batch.SetVariablesAsyncDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.helper.EqualsList;
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.camunda.bpm.engine.rest.helper.ErrorMessageHelper;
import org.camunda.bpm.engine.rest.helper.ExampleVariableObject;
import org.camunda.bpm.engine.rest.helper.MockObjectValue;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.VariableTypeHelper;
import org.camunda.bpm.engine.rest.helper.variable.EqualsNullValue;
import org.camunda.bpm.engine.rest.helper.variable.EqualsObjectValue;
import org.camunda.bpm.engine.rest.helper.variable.EqualsPrimitiveValue;
import org.camunda.bpm.engine.rest.helper.variable.EqualsUntypedValue;
import org.camunda.bpm.engine.rest.util.JsonPathUtil;
import org.camunda.bpm.engine.rest.util.ModificationInstructionBuilder;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.MessageCorrelationAsyncBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationInstantiationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.UpdateProcessInstanceSuspensionStateSelectBuilder;
import org.camunda.bpm.engine.runtime.UpdateProcessInstanceSuspensionStateTenantBuilder;
import org.camunda.bpm.engine.runtime.UpdateProcessInstancesSuspensionStateBuilder;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.SerializableValueType;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class ProcessInstanceRestServiceInteractionTest extends
    AbstractRestServiceTest {

  protected static final String TEST_DELETE_REASON = "test";
  protected static final String RETRIES = "retries";
  protected static final String FAIL_IF_NOT_EXISTS = "failIfNotExists";
  protected static final String DELETE_REASON = "deleteReason";

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String PROCESS_INSTANCE_URL = TEST_RESOURCE_ROOT_PATH + "/process-instance";
  protected static final String SINGLE_PROCESS_INSTANCE_URL = PROCESS_INSTANCE_URL + "/{id}";
  protected static final String PROCESS_INSTANCE_VARIABLES_URL = SINGLE_PROCESS_INSTANCE_URL + "/variables";
  protected static final String PROCESS_INSTANCE_COMMENTS_URL = SINGLE_PROCESS_INSTANCE_URL + "/comment";
  protected static final String DELETE_PROCESS_INSTANCES_ASYNC_URL = PROCESS_INSTANCE_URL + "/delete";
  protected static final String DELETE_PROCESS_INSTANCES_ASYNC_HIST_QUERY_URL = PROCESS_INSTANCE_URL + "/delete-historic-query-based";
  protected static final String SET_JOB_RETRIES_ASYNC_URL = PROCESS_INSTANCE_URL + "/job-retries";
  protected static final String SET_JOB_RETRIES_ASYNC_HIST_QUERY_URL = PROCESS_INSTANCE_URL + "/job-retries-historic-query-based";
  protected static final String SINGLE_PROCESS_INSTANCE_VARIABLE_URL = PROCESS_INSTANCE_VARIABLES_URL + "/{varId}";
  protected static final String SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL = SINGLE_PROCESS_INSTANCE_VARIABLE_URL + "/data";
  protected static final String PROCESS_INSTANCE_ACTIVIY_INSTANCES_URL = SINGLE_PROCESS_INSTANCE_URL + "/activity-instances";
  protected static final String EXAMPLE_PROCESS_INSTANCE_ID_WITH_NULL_VALUE_AS_VARIABLE = "aProcessInstanceWithNullValueAsVariable";
  protected static final String SINGLE_PROCESS_INSTANCE_SUSPENDED_URL = SINGLE_PROCESS_INSTANCE_URL + "/suspended";
  protected static final String PROCESS_INSTANCE_SUSPENDED_URL = PROCESS_INSTANCE_URL + "/suspended";
  protected static final String PROCESS_INSTANCE_SUSPENDED_ASYNC_URL = PROCESS_INSTANCE_URL + "/suspended-async";
  protected static final String PROCESS_INSTANCE_MODIFICATION_URL = SINGLE_PROCESS_INSTANCE_URL + "/modification";
  protected static final String PROCESS_INSTANCE_MODIFICATION_ASYNC_URL = SINGLE_PROCESS_INSTANCE_URL + "/modification-async";
  protected static final String PROCESS_INSTANCE_SET_VARIABLES_ASYNC_URL = PROCESS_INSTANCE_URL + "/variables-async";
  protected static final String PROCESS_INSTANCE_CORRELATE_MESSAGE_ASYNC_URL = PROCESS_INSTANCE_URL + "/message-async";

  protected static final Answer<Object> RETURNS_SELF = invocation -> {
    Object mock = invocation.getMock();
    if(invocation.getMethod().getReturnType().isInstance(mock)){
        return mock;
    }
    return RETURNS_DEFAULTS.answer(invocation);
  };

  protected static final VariableMap EXAMPLE_OBJECT_VARIABLES = Variables.createVariables();
  static {
    ExampleVariableObject variableValue = new ExampleVariableObject();
    variableValue.setProperty1("aPropertyValue");
    variableValue.setProperty2(true);

    EXAMPLE_OBJECT_VARIABLES.putValueTyped(EXAMPLE_VARIABLE_KEY,
        MockObjectValue
          .fromObjectValue(Variables.objectValue(variableValue).serializationDataFormat("application/json").create())
          .objectTypeName(ExampleVariableObject.class.getName()));
  }

  private List<Comment> mockTaskComments;
  private HistoricProcessInstanceQuery historicProcessInstanceQueryMock;

  private RuntimeServiceImpl runtimeServiceMock;
  private ManagementServiceImpl mockManagementService;
  private HistoryServiceImpl historyServiceMock;
  private TaskService taskServiceMock;

  private UpdateProcessInstanceSuspensionStateTenantBuilder mockUpdateSuspensionStateBuilder;
  private UpdateProcessInstanceSuspensionStateSelectBuilder mockUpdateSuspensionStateSelectBuilder;
  private UpdateProcessInstancesSuspensionStateBuilder mockUpdateProcessInstancesSuspensionStateBuilder;

  private SetJobRetriesByProcessAsyncBuilder mockSetJobRetriesByProcessAsyncBuilder;

  @Before
  public void setUpRuntimeData() {
    runtimeServiceMock = mock(RuntimeServiceImpl.class);
    mockManagementService = mock(ManagementServiceImpl.class);
    historyServiceMock = mock(HistoryServiceImpl.class);

    // variables
    when(runtimeServiceMock.getVariablesTyped(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, true)).thenReturn(EXAMPLE_VARIABLES);
    when(runtimeServiceMock.getVariablesTyped(MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID, true)).thenReturn(EXAMPLE_OBJECT_VARIABLES);
    when(runtimeServiceMock.getVariablesTyped(EXAMPLE_PROCESS_INSTANCE_ID_WITH_NULL_VALUE_AS_VARIABLE, true)).thenReturn(EXAMPLE_VARIABLES_WITH_NULL_VALUE);

    // activity instances
    when(runtimeServiceMock.getActivityInstance(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)).thenReturn(EXAMPLE_ACTIVITY_INSTANCE);

    mockUpdateSuspensionStateSelectBuilder = mock(UpdateProcessInstanceSuspensionStateSelectBuilder.class);
    when(runtimeServiceMock.updateProcessInstanceSuspensionState()).thenReturn(mockUpdateSuspensionStateSelectBuilder);

    mockUpdateSuspensionStateBuilder = mock(UpdateProcessInstanceSuspensionStateTenantBuilder.class);
    when(mockUpdateSuspensionStateSelectBuilder.byProcessInstanceId(any())).thenReturn(mockUpdateSuspensionStateBuilder);
    when(mockUpdateSuspensionStateSelectBuilder.byProcessDefinitionId(any())).thenReturn(mockUpdateSuspensionStateBuilder);
    when(mockUpdateSuspensionStateSelectBuilder.byProcessDefinitionKey(any())).thenReturn(mockUpdateSuspensionStateBuilder);

    mockUpdateProcessInstancesSuspensionStateBuilder = mock(UpdateProcessInstancesSuspensionStateBuilder.class);
    when(mockUpdateSuspensionStateSelectBuilder.byProcessInstanceIds(Mockito.<List<String>>any())).thenReturn(mockUpdateProcessInstancesSuspensionStateBuilder);
    when(mockUpdateSuspensionStateSelectBuilder.byProcessInstanceQuery(any())).thenReturn(mockUpdateProcessInstancesSuspensionStateBuilder);
    when(mockUpdateSuspensionStateSelectBuilder.byHistoricProcessInstanceQuery(any())).thenReturn(mockUpdateProcessInstancesSuspensionStateBuilder);

    // tasks and task service
    taskServiceMock = mock(TaskService.class);
    when(processEngine.getTaskService()).thenReturn(taskServiceMock);

    mockTaskComments = MockProvider.createMockTaskComments();
    when(taskServiceMock.getProcessInstanceComments(EXAMPLE_PROCESS_INSTANCE_ID)).thenReturn(mockTaskComments);

    historicProcessInstanceQueryMock = mock(HistoricProcessInstanceQuery.class);
    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(historicProcessInstanceQueryMock);
    when(historicProcessInstanceQueryMock.processInstanceId(eq(EXAMPLE_PROCESS_INSTANCE_ID))).thenReturn(historicProcessInstanceQueryMock);
    HistoricProcessInstance historicProcessInstanceMock = createMockHistoricProcessInstance();
    when(historicProcessInstanceQueryMock.singleResult()).thenReturn(historicProcessInstanceMock);

    // runtime service
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);
    when(processEngine.getManagementService()).thenReturn(mockManagementService);
    when(processEngine.getHistoryService()).thenReturn(historyServiceMock);

    // jobs
    mockSetJobRetriesByProcessAsyncBuilder = MockProvider.createMockSetJobRetriesByProcessAsyncBuilder(mockManagementService);
  }

  public void mockHistoryFull() {
    when(mockManagementService.getHistoryLevel()).thenReturn(ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL);
  }

  public void mockHistoryDisabled() {
    when(mockManagementService.getHistoryLevel()).thenReturn(ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE);
  }

  @Test
  public void testGetActivityInstanceTree() {
    Response response = given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .body("id", Matchers.equalTo(EXAMPLE_ACTIVITY_INSTANCE_ID))
        .body("parentActivityInstanceId", Matchers.equalTo(EXAMPLE_PARENT_ACTIVITY_INSTANCE_ID))
        .body("activityId", Matchers.equalTo(EXAMPLE_ACTIVITY_ID))
        .body("processInstanceId", Matchers.equalTo(EXAMPLE_PROCESS_INSTANCE_ID))
        .body("processDefinitionId", Matchers.equalTo(EXAMPLE_PROCESS_DEFINITION_ID))
        .body("executionIds", Matchers.not(Matchers.empty()))
        .body("executionIds[0]", Matchers.equalTo(EXAMPLE_EXECUTION_ID))
        .body("activityName", Matchers.equalTo(EXAMPLE_ACTIVITY_NAME))
        // "name" is deprecated and there for legacy reasons
        .body("name", Matchers.equalTo(EXAMPLE_ACTIVITY_NAME))
        .body("incidentIds", Matchers.empty())
        .body("incidents", Matchers.not(Matchers.empty()))
        .body("incidents[0].id", Matchers.equalTo("anIncidentId"))
        .body("incidents[0].activityId", Matchers.equalTo("anActivityId"))
        .body("childActivityInstances", Matchers.not(Matchers.empty()))
        .body("childActivityInstances[0].id", Matchers.equalTo(CHILD_EXAMPLE_ACTIVITY_INSTANCE_ID))
        .body("childActivityInstances[0].parentActivityInstanceId", Matchers.equalTo(CHILD_EXAMPLE_PARENT_ACTIVITY_INSTANCE_ID))
        .body("childActivityInstances[0].activityId", Matchers.equalTo(CHILD_EXAMPLE_ACTIVITY_ID))
        .body("childActivityInstances[0].activityType", Matchers.equalTo(CHILD_EXAMPLE_ACTIVITY_TYPE))
        .body("childActivityInstances[0].activityName", Matchers.equalTo(CHILD_EXAMPLE_ACTIVITY_NAME))
        .body("childActivityInstances[0].name", Matchers.equalTo(CHILD_EXAMPLE_ACTIVITY_NAME))
        .body("childActivityInstances[0].processInstanceId", Matchers.equalTo(CHILD_EXAMPLE_PROCESS_INSTANCE_ID))
        .body("childActivityInstances[0].processDefinitionId", Matchers.equalTo(CHILD_EXAMPLE_PROCESS_DEFINITION_ID))
        .body("childActivityInstances[0].executionIds", Matchers.not(Matchers.empty()))
        .body("childActivityInstances[0].childActivityInstances", Matchers.empty())
        .body("childActivityInstances[0].childTransitionInstances", Matchers.empty())
        .body("childActivityInstances[0].incidentIds", Matchers.not(Matchers.empty()))
        .body("childActivityInstances[0].incidentIds[0]", Matchers.equalTo(EXAMPLE_INCIDENT_ID))
        .body("childActivityInstances[0].incidents[0].id", Matchers.equalTo("anIncidentId"))
        .body("childActivityInstances[0].incidents[0].activityId", Matchers.equalTo("anActivityId"))
        .body("childTransitionInstances", Matchers.not(Matchers.empty()))
        .body("childTransitionInstances[0].id", Matchers.equalTo(CHILD_EXAMPLE_ACTIVITY_INSTANCE_ID))
        .body("childTransitionInstances[0].parentActivityInstanceId", Matchers.equalTo(CHILD_EXAMPLE_PARENT_ACTIVITY_INSTANCE_ID))
        .body("childTransitionInstances[0].activityId", Matchers.equalTo(CHILD_EXAMPLE_ACTIVITY_ID))
        .body("childTransitionInstances[0].activityName", Matchers.equalTo(CHILD_EXAMPLE_ACTIVITY_NAME))
        .body("childTransitionInstances[0].activityType", Matchers.equalTo(CHILD_EXAMPLE_ACTIVITY_TYPE))
        .body("childTransitionInstances[0].targetActivityId", Matchers.equalTo(CHILD_EXAMPLE_ACTIVITY_ID))
        .body("childTransitionInstances[0].processInstanceId", Matchers.equalTo(CHILD_EXAMPLE_PROCESS_INSTANCE_ID))
        .body("childTransitionInstances[0].processDefinitionId", Matchers.equalTo(CHILD_EXAMPLE_PROCESS_DEFINITION_ID))
        .body("childTransitionInstances[0].executionId", Matchers.equalTo(EXAMPLE_EXECUTION_ID))
        .body("childTransitionInstances[0].incidentIds", Matchers.not(Matchers.empty()))
        .body("childTransitionInstances[0].incidentIds[0]", Matchers.equalTo(EXAMPLE_ANOTHER_INCIDENT_ID))
        .body("childTransitionInstances[0].incidents[0].id", Matchers.equalTo("anIncidentId"))
        .body("childTransitionInstances[0].incidents[0].activityId", Matchers.equalTo("anActivityId"))
        .when().get(PROCESS_INSTANCE_ACTIVIY_INSTANCES_URL);

    Assert.assertEquals("Should return right number of properties", 13,
        response.jsonPath().getMap("").size());
  }

  @Test
  public void testGetActivityInstanceTreeForNonExistingProcessInstance() {
    when(runtimeServiceMock.getActivityInstance(anyString())).thenReturn(null);

    given().pathParam("id", "aNonExistingProcessInstanceId")
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.equalTo("Process instance with id aNonExistingProcessInstanceId does not exist"))
      .when().get(PROCESS_INSTANCE_ACTIVIY_INSTANCES_URL);
  }

  @Test
  public void testGetActivityInstanceTreeWithInternalError() {
    when(runtimeServiceMock.getActivityInstance(anyString())).thenThrow(new ProcessEngineException("expected exception"));

    given().pathParam("id", "aNonExistingProcessInstanceId")
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.equalTo("expected exception"))
      .when().get(PROCESS_INSTANCE_ACTIVIY_INSTANCES_URL);
  }

  @Test
  public void testGetActivityInstanceTreeThrowsAuthorizationException() {
    String message = "expected exception";
    when(runtimeServiceMock.getActivityInstance(anyString())).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", Matchers.equalTo(message))
    .when()
      .get(PROCESS_INSTANCE_ACTIVIY_INSTANCES_URL);
  }

  @Test
  public void testGetVariables() {
    Response response = given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(EXAMPLE_VARIABLE_KEY, Matchers.notNullValue())
      .body(EXAMPLE_VARIABLE_KEY + ".value", Matchers.equalTo(EXAMPLE_VARIABLE_VALUE.getValue()))
      .body(EXAMPLE_VARIABLE_KEY + ".type", Matchers.equalTo(String.class.getSimpleName()))
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);

    Assert.assertEquals("Should return exactly one variable", 1, response.jsonPath().getMap("").size());
  }

  @Test
  public void testDeleteAsync() {
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    when(runtimeServiceMock.deleteProcessInstancesAsync(any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(new BatchEntity());

    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("processInstanceIds", ids);
    messageBodyJson.put(DELETE_REASON, TEST_DELETE_REASON);

    given()
        .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .when().post(DELETE_PROCESS_INSTANCES_ASYNC_URL);

    verify(runtimeServiceMock, times(1)).deleteProcessInstancesAsync(ids, null, TEST_DELETE_REASON, false, false);
  }

  @Test
  public void testDeleteAsyncWithQuery() {
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put(DELETE_REASON, TEST_DELETE_REASON);
    ProcessInstanceQueryDto query = new ProcessInstanceQueryDto();
    messageBodyJson.put("processInstanceQuery", query);
    when(runtimeServiceMock.deleteProcessInstancesAsync(any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(new BatchEntity());

    given()
        .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .when().post(DELETE_PROCESS_INSTANCES_ASYNC_URL);

    verify(runtimeServiceMock, times(1)).deleteProcessInstancesAsync(
        any(), Mockito.any(), Mockito.eq(TEST_DELETE_REASON), Mockito.eq(false), Mockito.eq(false));
  }

  @Test
  public void testDeleteAsyncWithBadRequestQuery() {
    doThrow(new BadUserRequestException("process instance ids are empty"))
      .when(runtimeServiceMock).deleteProcessInstancesAsync(eq((List<String>) null), eq((ProcessInstanceQuery) null), anyString(), anyBoolean(), anyBoolean());

    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put(DELETE_REASON, TEST_DELETE_REASON);

    given()
        .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .when().post(DELETE_PROCESS_INSTANCES_ASYNC_URL);
  }

  @Test
  public void testDeleteAsyncWithSkipCustomListeners() {
    when(runtimeServiceMock.deleteProcessInstancesAsync(any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(new BatchEntity());
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put(DELETE_REASON, TEST_DELETE_REASON);
    messageBodyJson.put("processInstanceIds", Arrays.asList("processInstanceId1", "processInstanceId2"));
    messageBodyJson.put("skipCustomListeners", true);

    given()
        .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .when().post(DELETE_PROCESS_INSTANCES_ASYNC_URL);

    verify(runtimeServiceMock).deleteProcessInstancesAsync(anyList(), any(), Mockito.eq(TEST_DELETE_REASON), Mockito.eq(true), Mockito.eq(false));
  }

  @Test
  public void testDeleteAsyncWithSkipSubprocesses() {
    when(runtimeServiceMock.deleteProcessInstancesAsync(any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(new BatchEntity());
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put(DELETE_REASON, TEST_DELETE_REASON);
    messageBodyJson.put("processInstanceIds", Arrays.asList("processInstanceId1", "processInstanceId2"));
    messageBodyJson.put("skipSubprocesses", true);

    given()
        .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .when().post(DELETE_PROCESS_INSTANCES_ASYNC_URL);

    verify(runtimeServiceMock).deleteProcessInstancesAsync(
        anyList(),
        any(),
        eq(TEST_DELETE_REASON),
        eq(false),
        eq(true));
  }

  @Test
  public void testDeleteAsyncHistoricQueryBasedWithQuery() {
    when(runtimeServiceMock.deleteProcessInstancesAsync(
      any(),
      eq((ProcessInstanceQuery)null),
      any(),
      any(),
      anyBoolean(),
      anyBoolean()))
    .thenReturn(new BatchEntity());

    HistoricProcessInstanceQuery mockedHistoricProcessInstanceQuery = mock(HistoricProcessInstanceQueryImpl.class);
    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(mockedHistoricProcessInstanceQuery);

    DeleteProcessInstancesDto body = new DeleteProcessInstancesDto();
    body.setHistoricProcessInstanceQuery(new HistoricProcessInstanceQueryDto());

    given()
      .contentType(ContentType.JSON).body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().post(DELETE_PROCESS_INSTANCES_ASYNC_HIST_QUERY_URL);

    verify(runtimeServiceMock,
      times(1)).deleteProcessInstancesAsync(
        null,
        null,
        mockedHistoricProcessInstanceQuery,
        null,
        false,
        false);
  }

  @Test
  public void testDeleteAsyncHistoricQueryBasedWithProcessInstanceIds() {
    when(runtimeServiceMock.deleteProcessInstancesAsync(
      any(),
      eq((ProcessInstanceQuery)null),
      eq((HistoricProcessInstanceQuery)null),
      any(),
      anyBoolean(),
      anyBoolean()))
    .thenReturn(new BatchEntity());

    DeleteProcessInstancesDto body = new DeleteProcessInstancesDto();
    body.setProcessInstanceIds(Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID));

    given()
      .contentType(ContentType.JSON).body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().post(DELETE_PROCESS_INSTANCES_ASYNC_HIST_QUERY_URL);

    verify(runtimeServiceMock,
      times(1)).deleteProcessInstancesAsync(
      Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID),
      null,
      null,
      null,
      false,
      false);
  }

  @Test
  public void testDeleteAsyncHistoricQueryBasedWithQueryAndProcessInstanceIds() {
    when(runtimeServiceMock.deleteProcessInstancesAsync(
      any(),
      eq((ProcessInstanceQuery)null),
      any(),
      any(),
      anyBoolean(),
      anyBoolean()))
    .thenReturn(new BatchEntity());

    HistoricProcessInstanceQuery mockedHistoricProcessInstanceQuery = mock(HistoricProcessInstanceQueryImpl.class);
    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(mockedHistoricProcessInstanceQuery);

    DeleteProcessInstancesDto body = new DeleteProcessInstancesDto();
    body.setHistoricProcessInstanceQuery(new HistoricProcessInstanceQueryDto());
    body.setProcessInstanceIds(Collections.singletonList(MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID));

    given()
      .contentType(ContentType.JSON).body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().post(DELETE_PROCESS_INSTANCES_ASYNC_HIST_QUERY_URL);

    verify(runtimeServiceMock,
      times(1)).deleteProcessInstancesAsync(
      Collections.singletonList(MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID),
      null,
      mockedHistoricProcessInstanceQuery,
      null,
      false,
      false);
  }

  @Test
  public void testDeleteAsyncHistoricQueryBasedWithoutQueryAndWithoutProcessInstanceIds() {
    doThrow(new BadUserRequestException("processInstanceIds is empty"))
      .when(runtimeServiceMock).deleteProcessInstancesAsync(
        eq((List<String>)null),
        eq((ProcessInstanceQuery)null),
        eq((HistoricProcessInstanceQuery)null),
        any(),
        anyBoolean(),
        anyBoolean());

    given()
      .contentType(ContentType.JSON).body(new DeleteProcessInstancesDto())
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().post(DELETE_PROCESS_INSTANCES_ASYNC_HIST_QUERY_URL);

    verify(runtimeServiceMock,
      times(1)).deleteProcessInstancesAsync(
        null,
        null,
        null,
        null,
        false,
        false);
  }

  @Test
  public void testDeleteAsyncHistoricQueryBasedWithDeleteReason() {
    when(runtimeServiceMock.deleteProcessInstancesAsync(
      any(),
      any(),
      any(),
      any(),
      anyBoolean(),
      anyBoolean()))
    .thenReturn(new BatchEntity());

    DeleteProcessInstancesDto body = new DeleteProcessInstancesDto();
    body.setDeleteReason(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_DELETE_REASON);

    given()
      .contentType(ContentType.JSON).body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().post(DELETE_PROCESS_INSTANCES_ASYNC_HIST_QUERY_URL);

    verify(runtimeServiceMock,
      times(1)).deleteProcessInstancesAsync(
        null,
        null,
        null,
        MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_DELETE_REASON,
        false,
        false);
  }

  @Test
  public void testDeleteAsyncHistoricQueryBasedWithSkipCustomListenerTrue() {
    when(runtimeServiceMock.deleteProcessInstancesAsync(
      eq((List<String>)null),
      eq((ProcessInstanceQuery)null),
      any(),
      any(),
      anyBoolean(),
      anyBoolean()))
    .thenReturn(new BatchEntity());

    DeleteProcessInstancesDto body = new DeleteProcessInstancesDto();
    body.setSkipCustomListeners(true);

    given()
      .contentType(ContentType.JSON).body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().post(DELETE_PROCESS_INSTANCES_ASYNC_HIST_QUERY_URL);

    verify(runtimeServiceMock,
      times(1)).deleteProcessInstancesAsync(
      null,
      null,
      null,
      null,
      true,
      false);
  }

  @Test
  public void testDeleteAsyncHistoricQueryBasedWithSkipSubprocesses() {
    when(runtimeServiceMock.deleteProcessInstancesAsync(
      eq((List<String>)null),
      eq((ProcessInstanceQuery)null),
      eq((HistoricProcessInstanceQuery)null),
      any(),
      anyBoolean(),
      anyBoolean()))
    .thenReturn(new BatchEntity());

    DeleteProcessInstancesDto body = new DeleteProcessInstancesDto();
    body.setSkipSubprocesses(true);

    given()
      .contentType(ContentType.JSON).body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().post(DELETE_PROCESS_INSTANCES_ASYNC_HIST_QUERY_URL);

    verify(runtimeServiceMock,
      times(1)).deleteProcessInstancesAsync(
      null,
      null,
      null,
      null,
      false,
      true);
  }

  @Test
  public void testGetVariablesWithNullValue() {
    Response response = given().pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID_WITH_NULL_VALUE_AS_VARIABLE)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(EXAMPLE_ANOTHER_VARIABLE_KEY, Matchers.notNullValue())
      .body(EXAMPLE_ANOTHER_VARIABLE_KEY + ".value", Matchers.nullValue())
      .body(EXAMPLE_ANOTHER_VARIABLE_KEY + ".type", Matchers.equalTo("Null"))
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);

    Assert.assertEquals("Should return exactly one variable", 1, response.jsonPath().getMap("").size());
  }

  @Test
  public void testGetProcessInstanceComments() {
    mockHistoryFull();

    Response response = given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
      .body("$.size()", equalTo(1))
    .when()
      .get(PROCESS_INSTANCE_COMMENTS_URL);

    verifyTaskComments(mockTaskComments, response);
    verify(taskServiceMock).getProcessInstanceComments(EXAMPLE_PROCESS_INSTANCE_ID);
  }

  @Test
  public void testGetProcessInstanceCommentsWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
      .body("$.size()", equalTo(0))
    .when()
      .get(PROCESS_INSTANCE_COMMENTS_URL);
  }

  @Test
  public void testGetFileVariable() {
    String variableKey = "aVariableKey";
    final byte[] byteContent = "some bytes".getBytes();
    String filename = "test.txt";
    String mimeType = "text/plain";
    FileValue variableValue = Variables.fileValue(filename).file(byteContent).mimeType(mimeType).create();

    when(runtimeServiceMock.getVariableTyped(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), eq(true)))
    .thenReturn(variableValue);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON.toString())
    .and()
      .body("valueInfo.mimeType", Matchers.equalTo(mimeType))
      .body("valueInfo.filename", Matchers.equalTo(filename))
      .body("value", Matchers.nullValue())
    .when().get(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testGetNullFileVariable() {
    String variableKey = "aVariableKey";
    String filename = "test.txt";
    String mimeType = "text/plain";
    FileValue variableValue = Variables.fileValue(filename).mimeType(mimeType).create();

    when(runtimeServiceMock.getVariableTyped(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), anyBoolean()))
      .thenReturn(variableValue);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.TEXT.toString())
    .and()
      .body(Matchers.is(Matchers.equalTo("")))
    .when().get(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);
  }

  @Test
  public void testGetFileVariableDownloadWithType() {
    String variableKey = "aVariableKey";
    final byte[] byteContent = "some bytes".getBytes();
    String filename = "test.txt";
    FileValue variableValue = Variables.fileValue(filename).file(byteContent).mimeType(ContentType.TEXT.toString()).create();

    when(runtimeServiceMock.getVariableTyped(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), anyBoolean()))
    .thenReturn(variableValue);

  given()
    .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
    .pathParam("varId", variableKey)
  .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.TEXT.toString())
    .and()
      .body(Matchers.is(Matchers.equalTo(new String(byteContent))))
    .when().get(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);
  }

  @Test
  public void testGetFileVariableDownloadWithTypeAndEncoding() {
    String variableKey = "aVariableKey";
    final byte[] byteContent = "some bytes".getBytes();
    String filename = "test.txt";
    String encoding = "UTF-8";
    FileValue variableValue = Variables.fileValue(filename).file(byteContent).mimeType(ContentType.TEXT.toString()).encoding(encoding).create();

    when(runtimeServiceMock.getVariableTyped(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), anyBoolean()))
    .thenReturn(variableValue);

    Response response = given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body(Matchers.is(Matchers.equalTo(new String(byteContent))))
    .when().get(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);

    String contentType = response.contentType().replaceAll(" ", "");
    assertThat(contentType, Matchers.is(ContentType.TEXT + ";charset=" + encoding));
  }

  @Test
  public void testGetFileVariableDownloadWithoutType() {
    String variableKey = "aVariableKey";
    final byte[] byteContent = "some bytes".getBytes();
    String filename = "test.txt";
    FileValue variableValue = Variables.fileValue(filename).file(byteContent).create();

    when(runtimeServiceMock.getVariableTyped(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), anyBoolean()))
    .thenReturn(variableValue);

   given()
    .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
    .pathParam("varId", variableKey)
  .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
    .and()
      .body(Matchers.is(Matchers.equalTo(new String(byteContent))))
      .header("Content-Disposition", Matchers.containsString(filename))
    .when().get(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);
  }

  @Test
  public void testCannotDownloadVariableOtherThanFile() {
    String variableKey = "aVariableKey";
    LongValue variableValue = Variables.longValue(123L);

    when(runtimeServiceMock.getVariableTyped(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), anyBoolean()))
    .thenReturn(variableValue);

   given()
    .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
    .pathParam("varId", variableKey)
  .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(MediaType.APPLICATION_JSON)
    .and()
    .when().get(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);
  }

  @Test
  public void testJavaObjectVariableSerialization() {
    Response response = given().pathParam("id", MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(EXAMPLE_VARIABLE_KEY, Matchers.notNullValue())
      .body(EXAMPLE_VARIABLE_KEY + ".value.property1", Matchers.equalTo("aPropertyValue"))
      .body(EXAMPLE_VARIABLE_KEY + ".value.property2", Matchers.equalTo(true))
      .body(EXAMPLE_VARIABLE_KEY + ".type", Matchers.equalTo(VariableTypeHelper.toExpectedValueTypeName(ValueType.OBJECT)))
      .body(EXAMPLE_VARIABLE_KEY + ".valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, Matchers.equalTo(ExampleVariableObject.class.getName()))
      .body(EXAMPLE_VARIABLE_KEY + ".valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, Matchers.equalTo("application/json"))
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);

    Assert.assertEquals("Should return exactly one variable", 1, response.jsonPath().getMap("").size());
  }

  @Test
  public void testGetObjectVariables() {
    // given
    String variableKey = "aVariableId";

    List<String> payload = Arrays.asList("a", "b");
    ObjectValue variableValue =
        MockObjectValue
            .fromObjectValue(Variables
                .objectValue(payload)
                .serializationDataFormat("application/json")
                .create())
            .objectTypeName(ArrayList.class.getName())
            .serializedValue("a serialized value"); // this should differ from the serialized json

    when(runtimeServiceMock.getVariablesTyped(eq(EXAMPLE_PROCESS_INSTANCE_ID), anyBoolean()))
      .thenReturn(Variables.createVariables().putValueTyped(variableKey, variableValue));

    // when
    given().pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(variableKey + ".value", Matchers.equalTo(payload))
      .body(variableKey + ".type", Matchers.equalTo("Object"))
      .body(variableKey + ".valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, Matchers.equalTo("application/json"))
      .body(variableKey + ".valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, Matchers.equalTo(ArrayList.class.getName()))
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);

    // then
    verify(runtimeServiceMock).getVariablesTyped(EXAMPLE_PROCESS_INSTANCE_ID, true);
  }

  @Test
  public void testGetObjectVariablesSerialized() {
    // given
    String variableKey = "aVariableId";

    ObjectValue variableValue =
        Variables
          .serializedObjectValue("a serialized value")
          .serializationDataFormat("application/json")
          .objectTypeName(ArrayList.class.getName())
          .create();

    when(runtimeServiceMock.getVariablesTyped(eq(EXAMPLE_PROCESS_INSTANCE_ID), anyBoolean()))
      .thenReturn(Variables.createVariables().putValueTyped(variableKey, variableValue));

    // when
    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .queryParam("deserializeValues", false)
    .then().expect().statusCode(Status.OK.getStatusCode())
      .body(variableKey + ".value", Matchers.equalTo("a serialized value"))
      .body(variableKey + ".type", Matchers.equalTo("Object"))
      .body(variableKey + ".valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, Matchers.equalTo("application/json"))
      .body(variableKey + ".valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, Matchers.equalTo(ArrayList.class.getName()))
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);

    // then
    verify(runtimeServiceMock).getVariablesTyped(EXAMPLE_PROCESS_INSTANCE_ID, false);
  }

  @Test
  public void testGetVariablesForNonExistingProcessInstance() {
    when(runtimeServiceMock.getVariablesTyped(anyString(), anyBoolean())).thenThrow(new ProcessEngineException("expected exception"));

    given().pathParam("id", "aNonExistingProcessInstanceId")
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(ProcessEngineException.class.getSimpleName()))
      .body("message", Matchers.equalTo("expected exception"))
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testGetVariablesThrowsAuthorizationException() {
    String message = "expected exception";
    when(runtimeServiceMock.getVariablesTyped(anyString(), anyBoolean())).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", Matchers.equalTo(message))
    .when()
      .get(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testGetSingleInstance() {
    ProcessInstance mockInstance = MockProvider.createMockInstance();
    ProcessInstanceQuery sampleInstanceQuery = mock(ProcessInstanceQuery.class);
    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.processInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.singleResult()).thenReturn(mockInstance);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("id", Matchers.equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("ended", Matchers.equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_ENDED))
      .body("definitionId", Matchers.equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("businessKey", Matchers.equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY))
      .body("suspended", Matchers.equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED))
      .body("tenantId", Matchers.equalTo(MockProvider.EXAMPLE_TENANT_ID))
      .when().get(SINGLE_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testGetNonExistingProcessInstance() {
    ProcessInstanceQuery sampleInstanceQuery = mock(ProcessInstanceQuery.class);
    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.processInstanceId(anyString())).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.singleResult()).thenReturn(null);

    given().pathParam("id", "aNonExistingInstanceId")
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.equalTo("Process instance with id aNonExistingInstanceId does not exist"))
      .when().get(SINGLE_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testDeleteProcessInstance() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().delete(SINGLE_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).deleteProcessInstance(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, null, false, true, false, false);
  }

  @Test
  public void testDeleteNonExistingProcessInstance() {
    doThrow(new NotFoundException()).when(runtimeServiceMock).deleteProcessInstance(any(), any(), anyBoolean(), anyBoolean(), anyBoolean() ,anyBoolean());

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(InvalidRequestException.class.getSimpleName()))
      .when().delete(SINGLE_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testDeleteNonExistingProcessInstanceIfExists() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).queryParam("failIfNotExists", false)
    .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
    .when().delete(SINGLE_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).deleteProcessInstanceIfExists(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, null, false, true, false, false);
  }

  @Test
  public void testDeleteProcessInstanceThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(runtimeServiceMock).deleteProcessInstance(any(), any(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", Matchers.equalTo(message))
    .when()
      .delete(SINGLE_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testNoGivenDeleteReason1() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().delete(SINGLE_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).deleteProcessInstance(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, null, false, true, false, false);
  }

  @Test
  public void testDeleteProcessInstanceSkipCustomListeners() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).queryParams("skipCustomListeners", true).then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode()).when().delete(SINGLE_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).deleteProcessInstance(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, null, true, true, false, false);
  }

  @Test
  public void testDeleteProcessInstanceWithCustomListeners() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).queryParams("skipCustomListeners", false).then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode()).when().delete(SINGLE_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).deleteProcessInstance(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, null, false, true, false, false);
  }

  @Test
  public void testDeleteProcessInstanceSkipIoMappings() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).queryParams("skipIoMappings", true).then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode()).when().delete(SINGLE_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).deleteProcessInstance(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, null, false, true, true, false);
  }

  @Test
  public void testDeleteProcessInstanceWithoutSkipingIoMappings() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).queryParams("skipIoMappings", false).then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode()).when().delete(SINGLE_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).deleteProcessInstance(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, null, false, true, false, false);
  }

  @Test
  public void testDeleteProcessInstanceSkipSubprocesses() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).queryParams("skipSubprocesses", true).then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode()).when().delete(SINGLE_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).deleteProcessInstance(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, null, false, true, false, true);
  }

  @Test
  public void testDeleteProcessInstanceWithoutSkipSubprocesses() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).queryParams("skipSubprocesses", false).then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode()).when().delete(SINGLE_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).deleteProcessInstance(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, null, false, true, false, false);
  }

  @Test
  public void testVariableModification() {
    String variableKey = "aKey";
    int variableValue = 123;

    Map<String, Object> messageBodyJson = new HashMap<>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    messageBodyJson.put("modifications", modifications);

    List<String> deletions = new ArrayList<>();
    deletions.add("deleteKey");
    messageBodyJson.put("deletions", deletions);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);

    Map<String, Object> expectedModifications = new HashMap<>();
    expectedModifications.put(variableKey, variableValue);
    verify(runtimeServiceMock).updateVariables(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), argThat(new EqualsMap(expectedModifications)),
        argThat(new EqualsList(deletions)));
  }

  @Test
  public void testVariableModificationWithUnparseableInteger() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Integer";

    Map<String, Object> messageBodyJson = new HashMap<>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.equalTo("Cannot modify variables for process instance: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Integer.class)))
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationWithUnparseableShort() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Short";

    Map<String, Object> messageBodyJson = new HashMap<>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.equalTo("Cannot modify variables for process instance: "
      + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Short.class)))
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationWithUnparseableLong() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Long";

    Map<String, Object> messageBodyJson = new HashMap<>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.equalTo("Cannot modify variables for process instance: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Long.class)))
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationWithUnparseableDouble() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Double";

    Map<String, Object> messageBodyJson = new HashMap<>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.equalTo("Cannot modify variables for process instance: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Double.class)))
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationWithUnparseableDate() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Date";

    Map<String, Object> messageBodyJson = new HashMap<>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.equalTo("Cannot modify variables for process instance: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Date.class)))
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationWithNotSupportedType() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";

    Map<String, Object> messageBodyJson = new HashMap<>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.equalTo("Cannot modify variables for process instance: Unsupported value type 'X'"))
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationForNonExistingProcessInstance() {
    doThrow(new ProcessEngineException("expected exception")).when(runtimeServiceMock).updateVariables(any(), any(), any());

    String variableKey = "aKey";
    int variableValue = 123;

    Map<String, Object> messageBodyJson = new HashMap<>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();

    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(RestException.class.getSimpleName()))
      .body("message", Matchers.equalTo("Cannot modify variables for process instance " + MockProvider.EXAMPLE_PROCESS_INSTANCE_ID + ": expected exception"))
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testEmptyVariableModification() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationThrowsAuthorizationException() {
    String variableKey = "aKey";
    int variableValue = 123;
    Map<String, Object> messageBodyJson = new HashMap<>();
    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    messageBodyJson.put("modifications", modifications);

    String message = "excpected exception";
    doThrow(new AuthorizationException(message)).when(runtimeServiceMock).updateVariables(any(), any(), any());

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", Matchers.is(AuthorizationException.class.getSimpleName()))
      .body("message", Matchers.is(message))
    .when()
      .post(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testGetSingleVariable() {
    String variableKey = "aVariableKey";
    int variableValue = 123;

    when(runtimeServiceMock.getVariableTyped(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), eq(true)))
      .thenReturn(Variables.integerValue(variableValue));

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("value", Matchers.is(123))
      .body("type", Matchers.is("Integer"))
      .when().get(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testNonExistingVariable() {
    String variableKey = "aVariableKey";

    when(runtimeServiceMock.getVariableTyped(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), anyBoolean())).thenReturn(null);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", Matchers.is(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.is("process instance variable with name " + variableKey + " does not exist"))
      .when().get(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testGetSingleVariableThrowsAuthorizationException() {
    String variableKey = "aVariableKey";

    String message = "excpected exception";
    when(runtimeServiceMock.getVariableTyped(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), anyBoolean())).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", Matchers.is(AuthorizationException.class.getSimpleName()))
      .body("message", Matchers.is(message))
    .when()
      .get(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testGetSingleLocalVariableData() {

    when(runtimeServiceMock.getVariableTyped(anyString(), eq(EXAMPLE_BYTES_VARIABLE_KEY), eq(false))).thenReturn(EXAMPLE_VARIABLE_VALUE_BYTES);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .pathParam("varId", EXAMPLE_BYTES_VARIABLE_KEY)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
    .when()
      .get(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock).getVariableTyped(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, EXAMPLE_BYTES_VARIABLE_KEY, false);
  }

  @Test
  public void testGetSingleLocalVariableDataNonExisting() {

    when(runtimeServiceMock.getVariableTyped(anyString(), eq("nonExisting"), eq(false))).thenReturn(null);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .pathParam("varId", "nonExisting")
    .then()
      .expect()
        .statusCode(Status.NOT_FOUND.getStatusCode())
        .body("type", Matchers.is(InvalidRequestException.class.getSimpleName()))
        .body("message", Matchers.is("process instance variable with name " + "nonExisting" + " does not exist"))
    .when()
      .get(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock).getVariableTyped(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, "nonExisting", false);
  }

  @Test
  public void testGetSingleLocalVariabledataNotBinary() {

    when(runtimeServiceMock.getVariableTyped(anyString(), eq(EXAMPLE_VARIABLE_KEY), eq(false))).thenReturn(EXAMPLE_VARIABLE_VALUE);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .pathParam("varId", EXAMPLE_VARIABLE_KEY)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock).getVariableTyped(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, EXAMPLE_VARIABLE_KEY, false);
  }

  @Test
  public void testGetSingleLocalObjectVariable() {
    // given
    String variableKey = "aVariableId";

    List<String> payload = Arrays.asList("a", "b");
    ObjectValue variableValue =
        MockObjectValue
            .fromObjectValue(Variables
                .objectValue(payload)
                .serializationDataFormat("application/json")
                .create())
            .objectTypeName(ArrayList.class.getName())
            .serializedValue("a serialized value"); // this should differ from the serialized json

    when(runtimeServiceMock.getVariableTyped(eq(EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    // when
    given().pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("value", Matchers.equalTo(payload))
      .body("type", Matchers.equalTo("Object"))
      .body("valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, Matchers.equalTo("application/json"))
      .body("valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, Matchers.equalTo(ArrayList.class.getName()))
      .when().get(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);

    // then
    verify(runtimeServiceMock).getVariableTyped(EXAMPLE_PROCESS_INSTANCE_ID, variableKey, true);
  }

  @Test
  public void testGetSingleLocalObjectVariableSerialized() {
    // given
    String variableKey = "aVariableId";

    ObjectValue variableValue =
        Variables
          .serializedObjectValue("a serialized value")
          .serializationDataFormat("application/json")
          .objectTypeName(ArrayList.class.getName())
          .create();

    when(runtimeServiceMock.getVariableTyped(eq(EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    // when
    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .pathParam("varId", variableKey)
      .queryParam("deserializeValue", false)
    .then().expect().statusCode(Status.OK.getStatusCode())
      .body("value", Matchers.equalTo("a serialized value"))
      .body("type", Matchers.equalTo("Object"))
      .body("valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, Matchers.equalTo("application/json"))
      .body("valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, Matchers.equalTo(ArrayList.class.getName()))
      .when().get(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);

    // then
    verify(runtimeServiceMock).getVariableTyped(EXAMPLE_PROCESS_INSTANCE_ID, variableKey, false);
  }

  @Test
  public void testGetVariableForNonExistingInstance() {
    String variableKey = "aVariableKey";

    when(runtimeServiceMock.getVariableTyped(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), eq(true)))
      .thenThrow(new ProcessEngineException("expected exception"));

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", Matchers.is(RestException.class.getSimpleName()))
      .body("message", Matchers.is("Cannot get process instance variable " + variableKey + ": expected exception"))
      .when().get(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "aVariableValue";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);

    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey),
        argThat(EqualsUntypedValue.matcher().value(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithTypeString() {
    String variableKey = "aVariableKey";
    String variableValue = "aVariableValue";
    String type = "String";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);

    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.stringValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithTypeInteger() {
    String variableKey = "aVariableKey";
    Integer variableValue = 123;
    String type = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);

    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.integerValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableInteger() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.equalTo("Cannot put process instance variable aVariableKey: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Integer.class)))
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeShort() {
    String variableKey = "aVariableKey";
    Short variableValue = 123;
    String type = "Short";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);

    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.shortValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableShort() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Short";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.equalTo("Cannot put process instance variable aVariableKey: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Short.class)))
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeLong() {
    String variableKey = "aVariableKey";
    Long variableValue = Long.valueOf(123);
    String type = "Long";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);

    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.longValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableLong() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Long";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.equalTo("Cannot put process instance variable aVariableKey: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Long.class)))
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeDouble() {
    String variableKey = "aVariableKey";
    Double variableValue = 123.456;
    String type = "Double";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);

    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.doubleValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableDouble() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Double";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.equalTo("Cannot put process instance variable aVariableKey: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Double.class)))
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeBoolean() {
    String variableKey = "aVariableKey";
    Boolean variableValue = true;
    String type = "Boolean";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);

    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.booleanValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithTypeDate() throws Exception {
    Date now = new Date();

    String variableKey = "aVariableKey";
    String variableValue = DATE_FORMAT_WITH_TIMEZONE.format(now);
    String type = "Date";

    Date expectedValue = DATE_FORMAT_WITH_TIMEZONE.parse(variableValue);

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);

    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.dateValue(expectedValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableDate() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Date";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.equalTo("Cannot put process instance variable aVariableKey: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Date.class)))
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithNotSupportedType() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "X";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.equalTo("Cannot put process instance variable aVariableKey: Unsupported value type 'X'"))
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableThrowsAuthorizationException() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "String";
    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(runtimeServiceMock).setVariable(anyString(), anyString(), any());

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .pathParam("varId", variableKey)
      .contentType(ContentType.JSON)
      .body(variableJson)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", Matchers.equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", Matchers.equalTo(message))
    .when()
      .put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleBinaryVariable() throws Exception {
    byte[] bytes = "someContent".getBytes();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .multiPart("data", null, bytes)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.bytesValue(bytes)));
  }

  @Test
  public void testPutSingleBinaryVariableWithValueType() throws Exception {
    byte[] bytes = "someContent".getBytes();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .multiPart("data", null, bytes)
      .multiPart("valueType", "Bytes", "text/plain")
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.bytesValue(bytes)));
  }

  @Test
  public void testPutSingleBinaryVariableWithNoValue() throws Exception {
    byte[] bytes = new byte[0];

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .multiPart("data", null, bytes)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.bytesValue(bytes)));
  }

  @Test
  public void testPutSingleBinaryVariableThrowsAuthorizationException() {
    byte[] bytes = "someContent".getBytes();
    String variableKey = "aVariableKey";

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(runtimeServiceMock).setVariable(anyString(), anyString(), any());

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .pathParam("varId", variableKey)
      .multiPart("data", "unspecified", bytes)
    .expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", Matchers.equalTo(message))
    .when()
      .post(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);
  }

  @Test
  public void testPutSingleSerializableVariable() throws Exception {

    ArrayList<String> serializable = new ArrayList<>();
    serializable.add("foo");

    ObjectMapper mapper = new ObjectMapper();
    String jsonBytes = mapper.writeValueAsString(serializable);
    String typeName = TypeFactory.defaultInstance().constructType(serializable.getClass()).toCanonical();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .multiPart("data", jsonBytes, MediaType.APPLICATION_JSON)
      .multiPart("type", typeName, MediaType.TEXT_PLAIN)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey),
        argThat(EqualsObjectValue.objectValueMatcher().isDeserialized().value(serializable)));
  }

  @Test
  public void testPutSingleSerializableVariableUnsupportedMediaType() throws Exception {

    ArrayList<String> serializable = new ArrayList<>();
    serializable.add("foo");

    ObjectMapper mapper = new ObjectMapper();
    String jsonBytes = mapper.writeValueAsString(serializable);
    String typeName = TypeFactory.defaultInstance().constructType(serializable.getClass()).toCanonical();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .multiPart("data", jsonBytes, "unsupported")
      .multiPart("type", typeName, MediaType.TEXT_PLAIN)
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body(Matchers.containsString("Unrecognized content type for serialized java type: unsupported"))
    .when()
      .post(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock, never()).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey),
        eq(serializable));
  }

  @Test
  public void testPutSingleVariableFromSerialized() throws Exception {
    String serializedValue = "{\"prop\" : \"value\"}";
    Map<String, Object> requestJson = VariablesBuilder
        .getObjectValueMap(serializedValue, ValueType.OBJECT.getName(), "aDataFormat", "aRootType");

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON)
      .body(requestJson)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);

    verify(runtimeServiceMock).setVariable(
        eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey),
        argThat(EqualsObjectValue.objectValueMatcher()
          .serializedValue(serializedValue)
          .serializationFormat("aDataFormat")
          .objectTypeName("aRootType")));
  }

  @Test
  public void testPutSingleVariableFromInvalidSerialized() throws Exception {
    String serializedValue = "{\"prop\" : \"value\"}";

    Map<String, Object> requestJson = VariablesBuilder
        .getObjectValueMap(serializedValue, "aNonExistingType", null, null);

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON)
      .body(requestJson)
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.equalTo("Cannot put process instance variable aVariableKey: Unsupported value type 'aNonExistingType'"))
    .when()
      .put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableFromSerializedWithNoValue() {
    String variableKey = "aVariableKey";
    Map<String, Object> requestJson = VariablesBuilder
        .getObjectValueMap(null, ValueType.OBJECT.getName(), null, null);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(requestJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);

    verify(runtimeServiceMock).setVariable(
        eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey),
        argThat(EqualsObjectValue.objectValueMatcher()));
  }

  @Test
  public void testPutSingleVariableWithNoValue() {
    String variableKey = "aVariableKey";

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);

    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey),
        argThat(EqualsNullValue.matcher()));
  }

  @Test
  public void testPutVariableForNonExistingInstance() {
    String variableKey = "aVariableKey";
    String variableValue = "aVariableValue";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue);

    doThrow(new ProcessEngineException("expected exception"))
      .when(runtimeServiceMock)
      .setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), any());

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", Matchers.is(RestException.class.getSimpleName()))
      .body("message", Matchers.is("Cannot put process instance variable " + variableKey + ": expected exception"))
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void shouldReturnErrorOnSettingVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "aVariableValue";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue);

    doThrow(new ProcessEngineException("foo", 123))
        .when(runtimeServiceMock)
        .setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), any());

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
    .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", Matchers.is(RestException.class.getSimpleName()))
      .body("message", Matchers.is("Cannot put process instance variable aVariableKey: foo"))
      .body("code", Matchers.is(123))
    .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPostSingleFileVariableWithEncodingAndMimeType() throws Exception {

    byte[] value = "some text".getBytes();
    String variableKey = "aVariableKey";
    String encoding = "utf-8";
    String filename = "test.txt";
    String mimetype = MediaType.TEXT_PLAIN;

    given()
      .pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .multiPart("data", filename, value, mimetype + "; encoding="+encoding)
      .multiPart("valueType", "File", "text/plain")
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);

    ArgumentCaptor<FileValue> captor = ArgumentCaptor.forClass(FileValue.class);
    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_TASK_ID), eq(variableKey),
        captor.capture());
    FileValue captured = captor.getValue();
    assertThat(captured.getEncoding(), Matchers.is(encoding));
    assertThat(captured.getFilename(), Matchers.is(filename));
    assertThat(captured.getMimeType(), Matchers.is(mimetype));
    assertThat(IoUtil.readInputStream(captured.getValue(), null), Matchers.is(value));
  }

  @Test
  public void testPostSingleFileVariableWithMimeType() throws Exception {

    byte[] value = "some text".getBytes();
    String variableKey = "aVariableKey";
    String filename = "test.txt";
    String mimetype = MediaType.TEXT_PLAIN;

    given()
      .pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .multiPart("data", filename, value, mimetype)
      .multiPart("valueType", "File", "text/plain")
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);

    ArgumentCaptor<FileValue> captor = ArgumentCaptor.forClass(FileValue.class);
    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_TASK_ID), eq(variableKey),
        captor.capture());
    FileValue captured = captor.getValue();
    assertThat(captured.getEncoding(), Matchers.is(Matchers.nullValue()));
    assertThat(captured.getFilename(), Matchers.is(filename));
    assertThat(captured.getMimeType(), Matchers.is(mimetype));
    assertThat(IoUtil.readInputStream(captured.getValue(), null), Matchers.is(value));
  }

  @Test
  public void testPostSingleFileVariableWithEncoding() throws Exception {

    byte[] value = "some text".getBytes();
    String variableKey = "aVariableKey";
    String encoding = "utf-8";
    String filename = "test.txt";

    given()
      .pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .multiPart("data", filename, value, "encoding="+encoding)
      .multiPart("valueType", "File", "text/plain")
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      //when the user passes an encoding, he has to provide the type, too
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);
  }

  @Test
  public void testPostSingleFileVariableOnlyFilename() throws Exception {

    String variableKey = "aVariableKey";
    String filename = "test.txt";

    given()
      .pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .multiPart("data", filename, new byte[0])
      .multiPart("valueType", "File", "text/plain")
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);

    ArgumentCaptor<FileValue> captor = ArgumentCaptor.forClass(FileValue.class);
    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_TASK_ID), eq(variableKey),
        captor.capture());
    FileValue captured = captor.getValue();
    assertThat(captured.getEncoding(), Matchers.is(Matchers.nullValue()));
    assertThat(captured.getFilename(), Matchers.is(filename));
    assertThat(captured.getMimeType(), Matchers.is(MediaType.APPLICATION_OCTET_STREAM));
    assertThat(captured.getValue().available(), Matchers.is(0));
  }

  @Test
  public void testDeleteSingleVariable() {
    String variableKey = "aVariableKey";

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().delete(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);

    verify(runtimeServiceMock).removeVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey));
  }

  @Test
  public void testDeleteVariableForNonExistingInstance() {
    String variableKey = "aVariableKey";

    doThrow(new ProcessEngineException("expected exception"))
      .when(runtimeServiceMock).removeVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey));

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", Matchers.is(RestException.class.getSimpleName()))
      .body("message", Matchers.is("Cannot delete process instance variable " + variableKey + ": expected exception"))
      .when().delete(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testDeleteVariableThrowsAuthorizationException() {
    String variableKey = "aVariableKey";

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(runtimeServiceMock).removeVariable(anyString(), anyString());

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", Matchers.is(AuthorizationException.class.getSimpleName()))
      .body("message", Matchers.is(message))
    .when()
      .delete(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testActivateInstance() {
    ProcessInstanceSuspensionStateDto dto = new ProcessInstanceSuspensionStateDto();
    dto.setSuspended(false);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(dto)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_INSTANCE_SUSPENDED_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    verify(mockUpdateSuspensionStateBuilder).activate();
  }

  @Test
  public void testActivateThrowsProcessEngineException() {
    ProcessInstanceSuspensionStateDto dto = new ProcessInstanceSuspensionStateDto();
    dto.setSuspended(false);

    String expectedMessage = "expectedMessage";

    doThrow(new ProcessEngineException(expectedMessage))
      .when(mockUpdateSuspensionStateBuilder)
      .activate();

    given()
      .pathParam("id", MockProvider.EXAMPLE_NON_EXISTENT_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(dto)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", Matchers.is(ProcessEngineException.class.getSimpleName()))
        .body("message", Matchers.is(expectedMessage))
      .when()
        .put(SINGLE_PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testActivateThrowsAuthorizationException() {
    ProcessInstanceSuspensionStateDto dto = new ProcessInstanceSuspensionStateDto();
    dto.setSuspended(false);

    String message = "expectedMessage";

    doThrow(new AuthorizationException(message))
      .when(mockUpdateSuspensionStateBuilder)
      .activate();

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(dto)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", Matchers.equalTo(message))
    .when()
    .put(SINGLE_PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testSuspendInstance() {
    ProcessInstanceSuspensionStateDto dto = new ProcessInstanceSuspensionStateDto();
    dto.setSuspended(true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(dto)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_INSTANCE_SUSPENDED_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    verify(mockUpdateSuspensionStateBuilder).suspend();
  }

  @Test
  public void testSuspendThrowsProcessEngineException() {
    ProcessInstanceSuspensionStateDto dto = new ProcessInstanceSuspensionStateDto();
    dto.setSuspended(true);

    String expectedMessage = "expectedMessage";

    doThrow(new ProcessEngineException(expectedMessage))
      .when(mockUpdateSuspensionStateBuilder)
      .suspend();

    given()
      .pathParam("id", MockProvider.EXAMPLE_NON_EXISTENT_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(dto)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", Matchers.is(ProcessEngineException.class.getSimpleName()))
        .body("message", Matchers.is(expectedMessage))
      .when()
        .put(SINGLE_PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testSuspendThrowsAuthorizationException() {
    ProcessInstanceSuspensionStateDto dto = new ProcessInstanceSuspensionStateDto();
    dto.setSuspended(true);

    String message = "expectedMessage";

    doThrow(new AuthorizationException(message))
      .when(mockUpdateSuspensionStateBuilder)
      .suspend();

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(dto)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", Matchers.equalTo(message))
    .when()
    .put(SINGLE_PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    verify(mockUpdateSuspensionStateBuilder).activate();
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionKeyWithException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockUpdateSuspensionStateBuilder)
      .activate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", Matchers.is(ProcessEngineException.class.getSimpleName()))
        .body("message", Matchers.is(expectedException))
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionKeyThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String message = "expectedMessage";

    doThrow(new AuthorizationException(message))
      .when(mockUpdateSuspensionStateBuilder)
      .activate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", Matchers.equalTo(message))
    .when()
    .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    verify(mockUpdateSuspensionStateBuilder).suspend();
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionKeyWithException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockUpdateSuspensionStateBuilder)
      .suspend();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", Matchers.is(ProcessEngineException.class.getSimpleName()))
        .body("message", Matchers.is(expectedException))
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionKeyThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String message = "expectedMessage";

    doThrow(new AuthorizationException(message))
      .when(mockUpdateSuspensionStateBuilder)
      .suspend();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", Matchers.equalTo(message))
    .when()
    .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionKeyAndTenantId() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("processDefinitionTenantId", MockProvider.EXAMPLE_TENANT_ID);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    verify(mockUpdateSuspensionStateBuilder).processDefinitionTenantId(MockProvider.EXAMPLE_TENANT_ID);
    verify(mockUpdateSuspensionStateBuilder).activate();
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionKeyWithoutTenantId() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("processDefinitionWithoutTenantId", true);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    verify(mockUpdateSuspensionStateBuilder).processDefinitionWithoutTenantId();
    verify(mockUpdateSuspensionStateBuilder).activate();
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionKeyAndTenantId() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("processDefinitionTenantId", MockProvider.EXAMPLE_TENANT_ID);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    verify(mockUpdateSuspensionStateBuilder).processDefinitionTenantId(MockProvider.EXAMPLE_TENANT_ID);
    verify(mockUpdateSuspensionStateBuilder).suspend();
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionKeyWithoutTenantId() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("processDefinitionWithoutTenantId", true);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    verify(mockUpdateSuspensionStateBuilder).processDefinitionWithoutTenantId();
    verify(mockUpdateSuspensionStateBuilder).suspend();
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionId() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    verify(mockUpdateSuspensionStateBuilder).activate();
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionIdWithException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockUpdateSuspensionStateBuilder)
      .activate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", Matchers.is(ProcessEngineException.class.getSimpleName()))
        .body("message", Matchers.is(expectedException))
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionIdThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String message = "expectedMessage";

    doThrow(new AuthorizationException(message))
      .when(mockUpdateSuspensionStateBuilder)
      .activate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", Matchers.equalTo(message))
    .when()
      .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionId() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    verify(mockUpdateSuspensionStateBuilder).suspend();
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionIdWithException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockUpdateSuspensionStateBuilder)
      .suspend();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", Matchers.is(ProcessEngineException.class.getSimpleName()))
        .body("message", Matchers.is(expectedException))
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionIdThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String message = "expectedMessage";

    doThrow(new AuthorizationException(message))
      .when(mockUpdateSuspensionStateBuilder)
      .suspend();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", Matchers.equalTo(message))
    .when()
      .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testActivateProcessInstanceByIdShouldThrowException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    String message = "Either processDefinitionId or processDefinitionKey should be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", Matchers.is(InvalidRequestException.class.getSimpleName()))
        .body("message", Matchers.is(message))
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessInstanceByIdShouldThrowException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    String message = "Either processDefinitionId or processDefinitionKey should be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", Matchers.is(InvalidRequestException.class.getSimpleName()))
        .body("message", Matchers.is(message))
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testSuspendWithMultipleByParameters() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String message = "Only one of processDefinitionId or processDefinitionKey should be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", Matchers.is(InvalidRequestException.class.getSimpleName()))
        .body("message", Matchers.is(message))
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessInstanceByNothing() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);

    String message = "Either processDefinitionId or processDefinitionKey should be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", Matchers.is(InvalidRequestException.class.getSimpleName()))
        .body("message", Matchers.is(message))
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }


  @Test
  public void testSuspendInstances() {
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("processInstanceIds", ids);
    messageBodyJson.put("suspended", true);
    given()
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessInstanceIds(ids);
    verify(mockUpdateProcessInstancesSuspensionStateBuilder).suspend();
  }


  @Test
  public void testActivateInstances() {
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("processInstanceIds", ids);
    messageBodyJson.put("suspended", false);

    given()
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessInstanceIds(ids);
    verify(mockUpdateProcessInstancesSuspensionStateBuilder).activate();
  }

  @Test
  public void testSuspendInstancesMultipleGroupOperations() {
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    ProcessInstanceQueryDto query = new ProcessInstanceQueryDto();
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("processInstanceIds", ids);
    messageBodyJson.put("processInstanceQuery", query);
    messageBodyJson.put("suspended", true);


    given()
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
      .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
      .put(PROCESS_INSTANCE_SUSPENDED_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessInstanceIds(ids);
    verify(mockUpdateProcessInstancesSuspensionStateBuilder).byProcessInstanceQuery(query.toQuery(processEngine));
    verify(mockUpdateProcessInstancesSuspensionStateBuilder).suspend();
  }


  @Test
  public void testSuspendProcessInstanceQuery() {
    ProcessInstanceQueryDto query = new ProcessInstanceQueryDto();
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("processInstanceQuery", query);
    messageBodyJson.put("suspended", true);
    given()
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessInstanceQuery(query.toQuery(processEngine));
    verify(mockUpdateProcessInstancesSuspensionStateBuilder).suspend();
  }


  @Test
  public void testActivateProcessInstanceQuery() {
    ProcessInstanceQueryDto query = new ProcessInstanceQueryDto();
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("processInstanceQuery", query);
    messageBodyJson.put("suspended", false);

    given()
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessInstanceQuery(query.toQuery(processEngine));
    verify(mockUpdateProcessInstancesSuspensionStateBuilder).activate();
  }


  @Test
  public void testSuspendHistoricProcessInstanceQuery() {
    HistoricProcessInstanceQueryDto query = new HistoricProcessInstanceQueryDto();
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("historicProcessInstanceQuery", query);
    messageBodyJson.put("suspended", true);
    given()
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byHistoricProcessInstanceQuery(any());
    verify(mockUpdateProcessInstancesSuspensionStateBuilder).suspend();
  }


  @Test
  public void testActivateHistoricProcessInstanceQuery() {
    HistoricProcessInstanceDto query = new HistoricProcessInstanceDto();
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("historicProcessInstanceQuery", query);
    messageBodyJson.put("suspended", false);

    given()
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byHistoricProcessInstanceQuery(any());
    verify(mockUpdateProcessInstancesSuspensionStateBuilder).activate();
  }

  @Test
  public void testSuspendAsyncWithProcessInstances() {
    Map<String, Object> messageBodyJson = new HashMap<>();
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    messageBodyJson.put("processInstanceIds", ids);
    messageBodyJson.put("suspended", true);

    when(mockUpdateProcessInstancesSuspensionStateBuilder.suspendAsync()).thenReturn(new BatchEntity());
    given()
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(PROCESS_INSTANCE_SUSPENDED_ASYNC_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessInstanceIds(ids);
    verify(mockUpdateProcessInstancesSuspensionStateBuilder).suspendAsync();
  }

  @Test
  public void testActivateAsyncWithProcessInstances() {
    Map<String, Object> messageBodyJson = new HashMap<>();
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    messageBodyJson.put("processInstanceIds", ids);
    messageBodyJson.put("suspended", false);

    when(mockUpdateProcessInstancesSuspensionStateBuilder.activateAsync()).thenReturn(new BatchEntity());
    given()
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(PROCESS_INSTANCE_SUSPENDED_ASYNC_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessInstanceIds(ids);
    verify(mockUpdateProcessInstancesSuspensionStateBuilder).activateAsync();
  }

  @Test
  public void testSuspendAsyncWithProcessInstanceQuery() {
    ProcessInstanceQueryDto query = new ProcessInstanceQueryDto();
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("processInstanceQuery", query);
    messageBodyJson.put("suspended", true);


    when(mockUpdateProcessInstancesSuspensionStateBuilder.suspendAsync()).thenReturn(new BatchEntity());
    given()
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .post(PROCESS_INSTANCE_SUSPENDED_ASYNC_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessInstanceQuery(query.toQuery(processEngine));
    verify(mockUpdateProcessInstancesSuspensionStateBuilder).suspendAsync();
  }

  @Test
  public void testActivateAsyncWithProcessInstanceQuery() {
    ProcessInstanceQueryDto query = new ProcessInstanceQueryDto();
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("processInstanceQuery", query);
    messageBodyJson.put("suspended", false);

    when(mockUpdateProcessInstancesSuspensionStateBuilder.activateAsync()).thenReturn(new BatchEntity());
    given()
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .post(PROCESS_INSTANCE_SUSPENDED_ASYNC_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessInstanceQuery(query.toQuery(processEngine));
    verify(mockUpdateProcessInstancesSuspensionStateBuilder).activateAsync();
  }

  @Test
  public void testSuspendAsyncWithHistoricProcessInstanceQuery() {
    Map<String, Object> messageBodyJson = new HashMap<>();
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    messageBodyJson.put("processInstanceIds", ids);
    messageBodyJson.put("suspended", true);

    when(mockUpdateProcessInstancesSuspensionStateBuilder.suspendAsync()).thenReturn(new BatchEntity());
    given()
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .post(PROCESS_INSTANCE_SUSPENDED_ASYNC_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessInstanceIds(ids);
    verify(mockUpdateProcessInstancesSuspensionStateBuilder).suspendAsync();
  }

  @Test
  public void testActivateAsyncWithHistoricProcessInstanceQuery() {
    Map<String, Object> messageBodyJson = new HashMap<>();
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    messageBodyJson.put("processInstanceIds", ids);
    messageBodyJson.put("suspended", false);

    when(mockUpdateProcessInstancesSuspensionStateBuilder.activateAsync()).thenReturn(new BatchEntity());
    given()
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .post(PROCESS_INSTANCE_SUSPENDED_ASYNC_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessInstanceIds(ids);
    verify(mockUpdateProcessInstancesSuspensionStateBuilder).activateAsync();
  }

  @Test
  public void testSuspendAsyncWithMultipleGroupOperations() {
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    ProcessInstanceQueryDto query = new ProcessInstanceQueryDto();
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("processInstanceIds", ids);
    messageBodyJson.put("processInstanceQuery", query);
    messageBodyJson.put("suspended", true);

    when(mockUpdateProcessInstancesSuspensionStateBuilder.suspendAsync()).thenReturn(new BatchEntity());
    given()
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .post(PROCESS_INSTANCE_SUSPENDED_ASYNC_URL);

    verify(mockUpdateSuspensionStateSelectBuilder).byProcessInstanceIds(ids);
    verify(mockUpdateProcessInstancesSuspensionStateBuilder).byProcessInstanceQuery(query.toQuery(processEngine));
    verify(mockUpdateProcessInstancesSuspensionStateBuilder).suspendAsync();
  }


  @Test
  public void testSuspendAsyncWithNothing() {
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("suspended", true);

    String message = "Either processInstanceIds, processInstanceQuery or historicProcessInstanceQuery should be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
      .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.is(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.is(message))
      .when()
      .post(PROCESS_INSTANCE_SUSPENDED_ASYNC_URL);

  }

  @Test
  public void testProcessInstanceModification() {
    ProcessInstanceModificationInstantiationBuilder mockModificationBuilder = setUpMockModificationBuilder();
    when(runtimeServiceMock.createProcessInstanceModification(anyString())).thenReturn(mockModificationBuilder);

    Map<String, Object> json = new HashMap<>();
    json.put("skipCustomListeners", true);
    json.put("skipIoMappings", true);

    List<Map<String, Object>> instructions = new ArrayList<>();

    instructions.add(ModificationInstructionBuilder.cancellation().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.cancellation().activityInstanceId("activityInstanceId").getJson());
    instructions.add(ModificationInstructionBuilder.cancellation().transitionInstanceId("transitionInstanceId").getJson());
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.startBefore()
        .activityId("activityId").ancestorActivityInstanceId("ancestorActivityInstanceId").getJson());
    instructions.add(ModificationInstructionBuilder.startAfter().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.startAfter()
        .activityId("activityId").ancestorActivityInstanceId("ancestorActivityInstanceId").getJson());
    instructions.add(ModificationInstructionBuilder.startTransition().transitionId("transitionId").getJson());
    instructions.add(ModificationInstructionBuilder.startTransition()
        .transitionId("transitionId").ancestorActivityInstanceId("ancestorActivityInstanceId").getJson());

    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .post(PROCESS_INSTANCE_MODIFICATION_URL);

    verify(runtimeServiceMock).createProcessInstanceModification(eq(EXAMPLE_PROCESS_INSTANCE_ID));

    InOrder inOrder = inOrder(mockModificationBuilder);
    inOrder.verify(mockModificationBuilder).cancelAllForActivity("activityId");
    inOrder.verify(mockModificationBuilder).cancelActivityInstance("activityInstanceId");
    inOrder.verify(mockModificationBuilder).cancelTransitionInstance("transitionInstanceId");
    inOrder.verify(mockModificationBuilder).startBeforeActivity("activityId");
    inOrder.verify(mockModificationBuilder).startBeforeActivity("activityId", "ancestorActivityInstanceId");
    inOrder.verify(mockModificationBuilder).startAfterActivity("activityId");
    inOrder.verify(mockModificationBuilder).startAfterActivity("activityId", "ancestorActivityInstanceId");
    inOrder.verify(mockModificationBuilder).startTransition("transitionId");
    inOrder.verify(mockModificationBuilder).startTransition("transitionId", "ancestorActivityInstanceId");

    inOrder.verify(mockModificationBuilder).execute(true, true);

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testProcessInstanceModificationWithVariables() {
    ProcessInstanceModificationInstantiationBuilder mockModificationBuilder = setUpMockModificationBuilder();
    when(runtimeServiceMock.createProcessInstanceModification(anyString())).thenReturn(mockModificationBuilder);

    Map<String, Object> json = new HashMap<>();

    List<Map<String, Object>> instructions = new ArrayList<>();

    instructions.add(
        ModificationInstructionBuilder.startBefore()
          .activityId("activityId")
          .variables(VariablesBuilder.create()
              .variable("var", "value", "String", false)
              .variable("varLocal", "valueLocal", "String", true)
              .getVariables())
          .getJson());
    instructions.add(
        ModificationInstructionBuilder.startAfter()
          .activityId("activityId")
          .variables(VariablesBuilder.create()
              .variable("var", 52, "Integer", false)
              .variable("varLocal", 74, "Integer", true)
              .getVariables())
          .getJson());

    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .post(PROCESS_INSTANCE_MODIFICATION_URL);

    verify(runtimeServiceMock).createProcessInstanceModification(eq(EXAMPLE_PROCESS_INSTANCE_ID));

    InOrder inOrder = inOrder(mockModificationBuilder);
    inOrder.verify(mockModificationBuilder).startBeforeActivity("activityId");

    verify(mockModificationBuilder).setVariableLocal(eq("varLocal"), argThat(EqualsPrimitiveValue.stringValue("valueLocal")));
    verify(mockModificationBuilder).setVariable(eq("var"), argThat(EqualsPrimitiveValue.stringValue("value")));

    inOrder.verify(mockModificationBuilder).startAfterActivity("activityId");

    verify(mockModificationBuilder).setVariable(eq("var"), argThat(EqualsPrimitiveValue.integerValue(52)));
    verify(mockModificationBuilder).setVariableLocal(eq("varLocal"), argThat(EqualsPrimitiveValue.integerValue(74)));

    inOrder.verify(mockModificationBuilder).execute(false, false);

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testInvalidModification() {
    Map<String, Object> json = new HashMap<>();

    // start before: missing activity id
    List<Map<String, Object>> instructions = new ArrayList<>();
    instructions.add(ModificationInstructionBuilder.startBefore().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.is(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.containsString("'activityId' must be set"))
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_URL);

    // start after: missing ancestor activity instance id
    instructions = new ArrayList<>();
    instructions.add(ModificationInstructionBuilder.startAfter().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.is(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.containsString("'activityId' must be set"))
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_URL);

    // start transition: missing ancestor activity instance id
    instructions = new ArrayList<>();
    instructions.add(ModificationInstructionBuilder.startTransition().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.is(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.containsString("'transitionId' must be set"))
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_URL);

    // cancel: missing activity id and activity instance id
    instructions = new ArrayList<>();
    instructions.add(ModificationInstructionBuilder.cancellation().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.is(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.containsString("For instruction type 'cancel': exactly one, "
          + "'activityId', 'activityInstanceId', or 'transitionInstanceId', is required"))
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_URL);

    // cancel: both, activity id and activity instance id, set
    instructions = new ArrayList<>();
    instructions.add(ModificationInstructionBuilder.cancellation().activityId("anActivityId")
        .activityInstanceId("anActivityInstanceId").getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.is(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.containsString("For instruction type 'cancel': exactly one, "
          + "'activityId', 'activityInstanceId', or 'transitionInstanceId', is required"))
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_URL);

  }

  @Test
  public void testModifyProcessInstanceThrowsAuthorizationException() {
    ProcessInstanceModificationInstantiationBuilder mockModificationBuilder = setUpMockModificationBuilder();
    when(runtimeServiceMock.createProcessInstanceModification(anyString())).thenReturn(mockModificationBuilder);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(mockModificationBuilder).execute(anyBoolean(), anyBoolean());

    Map<String, Object> json = new HashMap<>();

    List<Map<String, Object>> instructions = new ArrayList<>();

    instructions.add(
        ModificationInstructionBuilder.startBefore()
          .activityId("activityId")
          .variables(VariablesBuilder.create()
              .variable("var", "value", "String", false)
              .variable("varLocal", "valueLocal", "String", true)
              .getVariables())
          .getJson());
    instructions.add(
        ModificationInstructionBuilder.startAfter()
          .activityId("activityId")
          .variables(VariablesBuilder.create()
              .variable("var", 52, "Integer", false)
              .variable("varLocal", 74, "Integer", true)
              .getVariables())
          .getJson());

    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", Matchers.equalTo(message))
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_URL);
  }

  @Test
  public void testSyncProcessInstanceModificationWithAnnotation() {
    ProcessInstanceModificationInstantiationBuilder mockModificationBuilder = setUpMockModificationBuilder();
    when(runtimeServiceMock.createProcessInstanceModification(anyString())).thenReturn(mockModificationBuilder);

    Map<String, Object> json = new HashMap<>();

    List<Map<String, Object>> instructions = new ArrayList<>();
    instructions.add(
        ModificationInstructionBuilder.startBefore()
          .activityId("activityId")
          .getJson());
    json.put("instructions", instructions);
    json.put("annotation", "anAnnotation");

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .post(PROCESS_INSTANCE_MODIFICATION_URL);

    verify(runtimeServiceMock).createProcessInstanceModification(eq(EXAMPLE_PROCESS_INSTANCE_ID));

    InOrder inOrder = inOrder(mockModificationBuilder);
    inOrder.verify(mockModificationBuilder).startBeforeActivity("activityId");
    inOrder.verify(mockModificationBuilder).setAnnotation("anAnnotation");


    inOrder.verify(mockModificationBuilder).execute(false, false);

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testSetRetriesByProcessAsync() {
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("processInstances", ids);
    messageBodyJson.put(RETRIES, MockProvider.EXAMPLE_JOB_RETRIES);

    Response response = given()
        .contentType(ContentType.JSON)
        .body(messageBodyJson)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(SET_JOB_RETRIES_ASYNC_URL);

    verifyBatchJson(response.asString());

    verify(mockManagementService, times(1)).setJobRetriesByProcessAsync(eq(MockProvider.EXAMPLE_JOB_RETRIES));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceIds(Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceQuery(null);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByProcessAsyncBuilder);
  }

  @Test
  public void testSetRetriesByProcessAsyncWithDueDate() {
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("processInstances", ids);
    messageBodyJson.put(RETRIES, MockProvider.EXAMPLE_JOB_RETRIES);
    Date newDueDate = new Date(1675752840000L);
    messageBodyJson.put("dueDate", newDueDate);

    Response response =
        given()
          .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(SET_JOB_RETRIES_ASYNC_URL);

    verifyBatchJson(response.asString());

    verify(mockManagementService, times(1)).setJobRetriesByProcessAsync(eq(MockProvider.EXAMPLE_JOB_RETRIES));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceIds(Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceQuery(null);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).dueDate(eq(newDueDate));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByProcessAsyncBuilder);
  }

  @Test
  public void testSetRetriesByProcessAsyncWithNullDueDate() {
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("processInstances", ids);
    messageBodyJson.put(RETRIES, MockProvider.EXAMPLE_JOB_RETRIES);
    messageBodyJson.put("dueDate", null);

    Response response =
        given()
          .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(SET_JOB_RETRIES_ASYNC_URL);

    verifyBatchJson(response.asString());

    verify(mockManagementService, times(1)).setJobRetriesByProcessAsync(eq(MockProvider.EXAMPLE_JOB_RETRIES));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceIds(Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceQuery(null);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).dueDate(null);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByProcessAsyncBuilder);
  }

  @Test
  public void testSetRetriesByProcessAsyncWithQueryAndDueDate() {
    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(new ProcessInstanceQueryImpl());
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put(RETRIES, MockProvider.EXAMPLE_JOB_RETRIES);
    ProcessInstanceQueryDto query = new ProcessInstanceQueryDto();
    messageBodyJson.put("processInstanceQuery", query);
    Date newDueDate = new Date(1675752840000L);
    messageBodyJson.put("dueDate", newDueDate);

    Response response = given()
        .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .when().post(SET_JOB_RETRIES_ASYNC_URL);

    verifyBatchJson(response.asString());

    verify(mockManagementService, times(1)).setJobRetriesByProcessAsync(eq(MockProvider.EXAMPLE_JOB_RETRIES));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceIds(null);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceQuery(any(ProcessInstanceQuery.class));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).dueDate(newDueDate);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByProcessAsyncBuilder);
  }

  @Test
  public void testSetRetriesByProcessAsyncWithQuery() {
    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(new ProcessInstanceQueryImpl());
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put(RETRIES, MockProvider.EXAMPLE_JOB_RETRIES);
    ProcessInstanceQueryDto query = new ProcessInstanceQueryDto();
    messageBodyJson.put("processInstanceQuery", query);

    Response response =
        given()
          .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(SET_JOB_RETRIES_ASYNC_URL);

    verifyBatchJson(response.asString());

    verify(mockManagementService, times(1)).setJobRetriesByProcessAsync(eq(MockProvider.EXAMPLE_JOB_RETRIES));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceIds(null);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceQuery(any(ProcessInstanceQuery.class));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByProcessAsyncBuilder);
  }

  @Test
  public void testSetRetriesByProcessWithBadRequestQuery() {
    doThrow(new BadUserRequestException("job ids are empty"))
        .when(mockSetJobRetriesByProcessAsyncBuilder).executeAsync();

    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put(RETRIES, MockProvider.EXAMPLE_JOB_RETRIES);

    given()
        .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .when().post(SET_JOB_RETRIES_ASYNC_URL);

    verify(mockManagementService, times(1)).setJobRetriesByProcessAsync(eq(MockProvider.EXAMPLE_JOB_RETRIES));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceIds(null);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceQuery(null);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByProcessAsyncBuilder);
  }

  @Test
  public void testSetRetriesByProcessWithoutRetries() {
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("processInstances", null);

    given()
        .contentType(ContentType.JSON)
        .body(messageBodyJson)
        .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .when().post(SET_JOB_RETRIES_ASYNC_URL);
  }

  @Test
  public void testSetRetriesByProcessWithNegativeRetries() {
    doThrow(new BadUserRequestException("retries are negative"))
        .when(mockManagementService).setJobRetriesByProcessAsync(eq(MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES));

    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put(RETRIES, MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES);
    HistoricProcessInstanceQueryDto query = new HistoricProcessInstanceQueryDto();
    messageBodyJson.put("processInstanceQuery", query);

    given()
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(SET_JOB_RETRIES_ASYNC_URL);

    verify(mockManagementService, times(1)).setJobRetriesByProcessAsync(eq(MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES));
    verifyNoMoreInteractions(mockSetJobRetriesByProcessAsyncBuilder);
  }

  @Test
  public void testSetRetriesByProcessAsyncHistoricQueryBasedWithQuery() {
    HistoricProcessInstanceQuery mockedHistoricProcessInstanceQuery = mock(HistoricProcessInstanceQuery.class);
    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(mockedHistoricProcessInstanceQuery);
    List<HistoricProcessInstance> historicProcessInstances = MockProvider.createMockRunningHistoricProcessInstances();
    when(mockedHistoricProcessInstanceQuery.list()).thenReturn(historicProcessInstances);

    Map<String, Object> body = new HashMap<>();
    body.put(RETRIES, MockProvider.EXAMPLE_JOB_RETRIES);
    body.put("historicProcessInstanceQuery", new HistoricProcessInstanceQueryDto());

    Response response = given()
      .contentType(ContentType.JSON).body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(SET_JOB_RETRIES_ASYNC_HIST_QUERY_URL);

    verifyBatchJson(response.asString());

    verify(mockManagementService, times(1)).setJobRetriesByProcessAsync(eq(MockProvider.EXAMPLE_JOB_RETRIES));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceIds(null);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).historicProcessInstanceQuery(any());
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByProcessAsyncBuilder);
  }

  @Test
  public void testSetRetriesByProcessAsyncHistoricQueryBasedWithQueryAndDueDate() {
    HistoricProcessInstanceQuery mockedHistoricProcessInstanceQuery = mock(HistoricProcessInstanceQuery.class);
    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(mockedHistoricProcessInstanceQuery);
    List<HistoricProcessInstance> historicProcessInstances = MockProvider.createMockRunningHistoricProcessInstances();
    when(mockedHistoricProcessInstanceQuery.list()).thenReturn(historicProcessInstances);

    Map<String, Object> body = new HashMap<>();
    body.put(RETRIES, MockProvider.EXAMPLE_JOB_RETRIES);
    body.put("historicProcessInstanceQueryDto", new HistoricProcessInstanceQueryDto());
    Date newDueDate = new Date(1675752840000L);
    body.put("dueDate", newDueDate);

    Response response =
        given()
          .contentType(ContentType.JSON).body(body)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(SET_JOB_RETRIES_ASYNC_HIST_QUERY_URL);

    verifyBatchJson(response.asString());

    verify(mockManagementService, times(1)).setJobRetriesByProcessAsync(eq(MockProvider.EXAMPLE_JOB_RETRIES));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceIds(null);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).historicProcessInstanceQuery(any());
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).dueDate(newDueDate);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByProcessAsyncBuilder);
  }

  @Test
  public void testSetRetriesByProcessAsyncHistoricQueryBasedWithQueryAndNullDueDate() {
    HistoricProcessInstanceQuery mockedHistoricProcessInstanceQuery = mock(HistoricProcessInstanceQuery.class);
    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(mockedHistoricProcessInstanceQuery);
    List<HistoricProcessInstance> historicProcessInstances = MockProvider.createMockRunningHistoricProcessInstances();
    when(mockedHistoricProcessInstanceQuery.list()).thenReturn(historicProcessInstances);

    Map<String, Object> body = new HashMap<>();
    body.put(RETRIES, MockProvider.EXAMPLE_JOB_RETRIES);
    body.put("historicProcessInstanceQuery", new HistoricProcessInstanceQueryDto());
    body.put("dueDate",null);

    Response response =
        given()
          .contentType(ContentType.JSON).body(body)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(SET_JOB_RETRIES_ASYNC_HIST_QUERY_URL);

    verifyBatchJson(response.asString());

    verify(mockManagementService, times(1)).setJobRetriesByProcessAsync(eq(MockProvider.EXAMPLE_JOB_RETRIES));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceIds(null);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).historicProcessInstanceQuery(any());
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).dueDate(null);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByProcessAsyncBuilder);
  }

  @Test
  public void testSetRetriesByProcessAsyncHistoricQueryBasedWithProcessInstanceIds() {
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put(RETRIES, MockProvider.EXAMPLE_JOB_RETRIES);
    messageBodyJson.put("processInstances", Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID));

    given()
      .contentType(ContentType.JSON).body(messageBodyJson)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().post(SET_JOB_RETRIES_ASYNC_HIST_QUERY_URL);

    verify(mockManagementService, times(1)).setJobRetriesByProcessAsync(eq(MockProvider.EXAMPLE_JOB_RETRIES));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceIds(Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).historicProcessInstanceQuery(null);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByProcessAsyncBuilder);
  }

  @Test
  public void testSetRetriesByProcessAsyncHistoricQueryBasedWithProcessInstanceIdsAndDueDate() {
    Map<String, Object> body = new HashMap<>();
    body.put(RETRIES, MockProvider.EXAMPLE_JOB_RETRIES);
    body.put("processInstances", Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID));
    Date newDueDate = new Date(1675752840000L);
    body.put("dueDate", newDueDate);

    given()
      .contentType(ContentType.JSON).body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(SET_JOB_RETRIES_ASYNC_HIST_QUERY_URL);

    verify(mockManagementService, times(1)).setJobRetriesByProcessAsync(eq(MockProvider.EXAMPLE_JOB_RETRIES));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceIds(Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).historicProcessInstanceQuery(null);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).dueDate(newDueDate);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByProcessAsyncBuilder);
  }

  @Test
  public void testSetRetriesByProcessAsyncHistoricQueryBasedWithQueryAndProcessInstanceIds() {
    HistoricProcessInstanceQuery mockedHistoricProcessInstanceQuery = mock(HistoricProcessInstanceQuery.class);
    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(mockedHistoricProcessInstanceQuery);
    List<HistoricProcessInstance> historicProcessInstances = MockProvider.createMockRunningHistoricProcessInstances();
    when(mockedHistoricProcessInstanceQuery.list()).thenReturn(historicProcessInstances);

    Map<String, Object> body = new HashMap<>();
    body.put(RETRIES, MockProvider.EXAMPLE_JOB_RETRIES);
    body.put("processInstances", Arrays.asList(MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID));
    body.put("historicProcessInstanceQuery", new HistoricProcessInstanceQueryDto());

    given()
      .contentType(ContentType.JSON).body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().post(SET_JOB_RETRIES_ASYNC_HIST_QUERY_URL);

    verify(mockManagementService, times(1)).setJobRetriesByProcessAsync(eq(MockProvider.EXAMPLE_JOB_RETRIES));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceIds(Arrays.asList(MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).historicProcessInstanceQuery(any());
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByProcessAsyncBuilder);
  }

  @Test
  public void testSetRetriesByProcessAsyncHistoricQueryBasedWithBadRequestQuery() {
    doThrow(new BadUserRequestException("jobIds is empty"))
      .when(mockSetJobRetriesByProcessAsyncBuilder).executeAsync();

    Map<String, Object> body = new HashMap<>();
    body.put(RETRIES, MockProvider.EXAMPLE_JOB_RETRIES);

    given()
      .contentType(ContentType.JSON).body(body)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().post(SET_JOB_RETRIES_ASYNC_HIST_QUERY_URL);

    verify(mockManagementService, times(1)).setJobRetriesByProcessAsync(eq(MockProvider.EXAMPLE_JOB_RETRIES));
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).processInstanceIds(null);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).historicProcessInstanceQuery(null);
    verify(mockSetJobRetriesByProcessAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByProcessAsyncBuilder);
  }

  @Test
  public void testSetRetriesByProcessAsyncHistoricQueryBasedWithNegativeRetries() {
    doThrow(new BadUserRequestException("retries are negative"))
      .when(mockManagementService).setJobRetriesByProcessAsync(eq(MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES));

    HistoricProcessInstanceQuery mockedHistoricProcessInstanceQuery = mock(HistoricProcessInstanceQuery.class);
    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(mockedHistoricProcessInstanceQuery);
    List<HistoricProcessInstance> historicProcessInstances = MockProvider.createMockRunningHistoricProcessInstances();
    when(mockedHistoricProcessInstanceQuery.list()).thenReturn(historicProcessInstances);

    Map<String, Object> body = new HashMap<>();
    body.put(RETRIES, MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES);
    body.put("historicProcessInstanceQuery", new HistoricProcessInstanceQueryDto());

    given()
      .contentType(ContentType.JSON).body(body)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().post(SET_JOB_RETRIES_ASYNC_HIST_QUERY_URL);

    verify(mockManagementService, times(1)).setJobRetriesByProcessAsync(eq(MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES));
    verifyNoMoreInteractions(mockSetJobRetriesByProcessAsyncBuilder);
  }

  @Test
  public void testProcessInstanceModificationAsync() {
    ProcessInstanceModificationInstantiationBuilder mockModificationBuilder = setUpMockModificationBuilder();
    when(runtimeServiceMock.createProcessInstanceModification(anyString())).thenReturn(mockModificationBuilder);
    Batch batchMock = createMockBatch();
    when(mockModificationBuilder.executeAsync(anyBoolean(), anyBoolean())).thenReturn(batchMock);

    Map<String, Object> json = new HashMap<>();
    json.put("skipCustomListeners", true);
    json.put("skipIoMappings", true);

    List<Map<String, Object>> instructions = new ArrayList<>();

    instructions.add(ModificationInstructionBuilder.cancellation().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.cancellation().activityInstanceId("activityInstanceId").getJson());
    instructions.add(ModificationInstructionBuilder.cancellation().transitionInstanceId("transitionInstanceId").getJson());
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.startBefore()
        .activityId("activityId").ancestorActivityInstanceId("ancestorActivityInstanceId").getJson());
    instructions.add(ModificationInstructionBuilder.startAfter().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.startAfter()
        .activityId("activityId").ancestorActivityInstanceId("ancestorActivityInstanceId").getJson());
    instructions.add(ModificationInstructionBuilder.startTransition().transitionId("transitionId").getJson());
    instructions.add(ModificationInstructionBuilder.startTransition()
        .transitionId("transitionId").ancestorActivityInstanceId("ancestorActivityInstanceId").getJson());

    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(PROCESS_INSTANCE_MODIFICATION_ASYNC_URL);

    verify(runtimeServiceMock).createProcessInstanceModification(eq(EXAMPLE_PROCESS_INSTANCE_ID));

    InOrder inOrder = inOrder(mockModificationBuilder);
    inOrder.verify(mockModificationBuilder).cancelAllForActivity("activityId");
    inOrder.verify(mockModificationBuilder).cancelActivityInstance("activityInstanceId");
    inOrder.verify(mockModificationBuilder).cancelTransitionInstance("transitionInstanceId");
    inOrder.verify(mockModificationBuilder).startBeforeActivity("activityId");
    inOrder.verify(mockModificationBuilder).startBeforeActivity("activityId", "ancestorActivityInstanceId");
    inOrder.verify(mockModificationBuilder).startAfterActivity("activityId");
    inOrder.verify(mockModificationBuilder).startAfterActivity("activityId", "ancestorActivityInstanceId");
    inOrder.verify(mockModificationBuilder).startTransition("transitionId");
    inOrder.verify(mockModificationBuilder).startTransition("transitionId", "ancestorActivityInstanceId");

    inOrder.verify(mockModificationBuilder).executeAsync(true, true);

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testInvalidModificationAsync() {
    Map<String, Object> json = new HashMap<>();

    // start before: missing activity id
    List<Map<String, Object>> instructions = new ArrayList<>();
    instructions.add(ModificationInstructionBuilder.startBefore().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.is(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.containsString("'activityId' must be set"))
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_ASYNC_URL);

    // start after: missing ancestor activity instance id
    instructions = new ArrayList<>();
    instructions.add(ModificationInstructionBuilder.startAfter().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.is(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.containsString("'activityId' must be set"))
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_ASYNC_URL);

    // start transition: missing ancestor activity instance id
    instructions = new ArrayList<>();
    instructions.add(ModificationInstructionBuilder.startTransition().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.is(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.containsString("'transitionId' must be set"))
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_ASYNC_URL);

    // cancel: missing activity id and activity instance id
    instructions = new ArrayList<>();
    instructions.add(ModificationInstructionBuilder.cancellation().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.is(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.containsString("For instruction type 'cancel': exactly one, "
          + "'activityId', 'activityInstanceId', or 'transitionInstanceId', is required"))
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_ASYNC_URL);

    // cancel: both, activity id and activity instance id, set
    instructions = new ArrayList<>();
    instructions.add(ModificationInstructionBuilder.cancellation().activityId("anActivityId")
        .activityInstanceId("anActivityInstanceId").getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", Matchers.is(InvalidRequestException.class.getSimpleName()))
      .body("message", Matchers.containsString("For instruction type 'cancel': exactly one, "
          + "'activityId', 'activityInstanceId', or 'transitionInstanceId', is required"))
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_ASYNC_URL);

  }

  @Test
  public void testModifyProcessInstanceAsyncThrowsAuthorizationException() {
    ProcessInstanceModificationInstantiationBuilder mockModificationBuilder = setUpMockModificationBuilder();
    when(runtimeServiceMock.createProcessInstanceModification(anyString())).thenReturn(mockModificationBuilder);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(mockModificationBuilder).executeAsync(anyBoolean(), anyBoolean());

    Map<String, Object> json = new HashMap<>();

    List<Map<String, Object>> instructions = new ArrayList<>();

    instructions.add(
        ModificationInstructionBuilder.startBefore()
          .activityId("activityId")
          .getJson());
    instructions.add(
        ModificationInstructionBuilder.startAfter()
          .activityId("activityId")
          .getJson());

    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", Matchers.equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", Matchers.equalTo(message))
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_ASYNC_URL);
  }


  @Test
  public void testAsyncProcessInstanceModificationWithAnnotation() {
    ProcessInstanceModificationInstantiationBuilder mockModificationBuilder = setUpMockModificationBuilder();
    when(runtimeServiceMock.createProcessInstanceModification(anyString())).thenReturn(mockModificationBuilder);
    Batch batchMock = createMockBatch();
    when(mockModificationBuilder.executeAsync(anyBoolean(), anyBoolean())).thenReturn(batchMock);

    Map<String, Object> json = new HashMap<>();

    List<Map<String, Object>> instructions = new ArrayList<>();
    instructions.add(ModificationInstructionBuilder.cancellation().activityId("activityId").getJson());

    json.put("instructions", instructions);
    json.put("annotation", "anAnnotation");

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(PROCESS_INSTANCE_MODIFICATION_ASYNC_URL);

    verify(runtimeServiceMock).createProcessInstanceModification(eq(EXAMPLE_PROCESS_INSTANCE_ID));

    InOrder inOrder = inOrder(mockModificationBuilder);
    inOrder.verify(mockModificationBuilder).cancelAllForActivity("activityId");
    inOrder.verify(mockModificationBuilder).setAnnotation("anAnnotation");

    inOrder.verify(mockModificationBuilder).executeAsync(false, false);

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testSyncProcessInstanceModificationCancellationSource() {
    ProcessInstanceModificationInstantiationBuilder mockModificationBuilder = setUpMockModificationBuilder();
    when(runtimeServiceMock.createProcessInstanceModification(anyString())).thenReturn(mockModificationBuilder);

    Map<String, Object> json = new HashMap<>();
    List<Map<String, Object>> instructions = new ArrayList<>();
    instructions.add(ModificationInstructionBuilder.cancellation().activityId("activityId").getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .post(PROCESS_INSTANCE_MODIFICATION_URL);

    verify(mockModificationBuilder).cancellationSourceExternal(true);
  }

  @Test
  public void testAsyncProcessInstanceModificationCancellationSource() {
    ProcessInstanceModificationInstantiationBuilder mockModificationBuilder = setUpMockModificationBuilder();
    when(runtimeServiceMock.createProcessInstanceModification(anyString())).thenReturn(mockModificationBuilder);
    Batch batchMock = createMockBatch();
    when(mockModificationBuilder.executeAsync(anyBoolean(), anyBoolean())).thenReturn(batchMock);

    Map<String, Object> json = new HashMap<>();
    List<Map<String, Object>> instructions = new ArrayList<>();
    instructions.add(ModificationInstructionBuilder.cancellation().activityId("activityId").getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_ASYNC_URL);

    verify(mockModificationBuilder).cancellationSourceExternal(true);
  }

  @Test
  public void shouldSetVariablesAsync() {
    // given
    Batch batchMock = createMockBatch();
    when(runtimeServiceMock.setVariablesAsync(any(), any(), any(), any()))
        .thenReturn(batchMock);

    SetVariablesAsyncDto body = new SetVariablesAsyncDto();

    VariableValueDto variableValueDto = new VariableValueDto();
    variableValueDto.setValue("bar");

    body.setVariables(Collections.singletonMap("foo", variableValueDto));

    // when
    Response response = given()
          .contentType(ContentType.JSON)
          .body(body)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(PROCESS_INSTANCE_SET_VARIABLES_ASYNC_URL);

    // then
    ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
    verify(runtimeServiceMock).setVariablesAsync(
        eq(null),
        eq(null),
        eq(null),
        captor.capture()
    );

    Assertions.assertThat(captor.getValue().get("foo")).isEqualTo("bar");

    verifyBatchJson(response.asString());
  }

  @Test
  public void shouldThrowExceptionWhenSetVariablesAsync_UnsupportedType() {
    // given
    Batch batchMock = createMockBatch();
    when(runtimeServiceMock.setVariablesAsync(any(), any(), any(), any()))
        .thenReturn(batchMock);

    SetVariablesAsyncDto body = new SetVariablesAsyncDto();

    VariableValueDto variableValueDto = new VariableValueDto();
    variableValueDto.setValue("bar");
    variableValueDto.setType("unknown");

    body.setVariables(Collections.singletonMap("foo", variableValueDto));

    // when + then
    given()
          .contentType(ContentType.JSON)
          .body(body)
        .then().expect()
          .statusCode(Status.BAD_REQUEST.getStatusCode())
          .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
          .body("message", equalTo("Cannot set variables: Unsupported value type 'unknown'"))
        .when()
          .post(PROCESS_INSTANCE_SET_VARIABLES_ASYNC_URL);
  }

  /**
   * Thrown when java serialization format is prohibited and java serialized variable is set
   * or null value is given.
   */
  @Test
  public void shouldTransformProcessEngineExceptionToInvalidRequestException() {
    // given
    doThrow(new ProcessEngineException("message"))
        .when(runtimeServiceMock).setVariablesAsync(any(), any(), any(), any());

    // when + then
    given()
          .contentType(ContentType.JSON)
          .body("{}")
        .then().expect()
          .statusCode(Status.BAD_REQUEST.getStatusCode())
          .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
          .body("message", equalTo("message"))
        .when()
          .post(PROCESS_INSTANCE_SET_VARIABLES_ASYNC_URL);
  }

  @Test
  public void shouldThrowExceptionWhenSetVariablesAsync_AuthorizationException() {
    // given
    doThrow(new AuthorizationException("message"))
        .when(runtimeServiceMock).setVariablesAsync(any(), any(), any(), any());

    // when + then
    given()
          .contentType(ContentType.JSON)
          .body("{}")
        .then().expect()
          .statusCode(Status.FORBIDDEN.getStatusCode())
          .body(Matchers.containsString("{\"type\":\"AuthorizationException\""))
        .when()
          .post(PROCESS_INSTANCE_SET_VARIABLES_ASYNC_URL);
  }

  @Test
  public void shouldThrowExceptionWhenSetVariablesAsync_NullValueException() {
    // given
    doThrow(new NullValueException("message"))
        .when(runtimeServiceMock).setVariablesAsync(any(), any(), any(), any());

    // when + then
    given()
          .contentType(ContentType.JSON)
          .body("{}")
        .then().expect()
          .statusCode(Status.BAD_REQUEST.getStatusCode())
          .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
          .body("message", equalTo("message"))
        .when()
          .post(PROCESS_INSTANCE_SET_VARIABLES_ASYNC_URL);
  }

  @Test
  public void shouldThrowExceptionWhenSetVariablesAsync_BadUserRequestException() {
    // given
    doThrow(new BadUserRequestException("message"))
        .when(runtimeServiceMock).setVariablesAsync(any(), any(), any(), any());

    // when + then
    given()
          .contentType(ContentType.JSON)
          .body("{}")
        .then().expect()
          .statusCode(Status.BAD_REQUEST.getStatusCode())
          .body("type", equalTo(BadUserRequestException.class.getSimpleName()))
          .body("message", equalTo("message"))
        .when()
          .post(PROCESS_INSTANCE_SET_VARIABLES_ASYNC_URL);
  }

  @Test
  public void shouldSetVariablesAsync_RuntimeQuery() {
    // given
    Batch batchMock = createMockBatch();
    when(runtimeServiceMock.setVariablesAsync(any(), any(), any(), any()))
        .thenReturn(batchMock);

    ProcessInstanceQuery mockedProcessInstanceQuery = mock(ProcessInstanceQuery.class);
    when(runtimeServiceMock.createProcessInstanceQuery())
        .thenReturn(mockedProcessInstanceQuery);

    SetVariablesAsyncDto body = new SetVariablesAsyncDto();

    ProcessInstanceQueryDto processInstanceQueryDto = new ProcessInstanceQueryDto();
    processInstanceQueryDto.setProcessDefinitionId("foo");
    body.setProcessInstanceQuery(processInstanceQueryDto);

    // when
    Response response = given()
          .contentType(ContentType.JSON)
          .body(body)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(PROCESS_INSTANCE_SET_VARIABLES_ASYNC_URL);

    // then

    verify(mockedProcessInstanceQuery)
        .processDefinitionId("foo");

    verify(runtimeServiceMock).setVariablesAsync(
        eq(null),
        eq(mockedProcessInstanceQuery),
        eq(null),
        eq(null)
    );

    verifyBatchJson(response.asString());
  }

  @Test
  public void shouldSetVariablesAsync_HistoryQuery() {
    // given
    Batch batchMock = createMockBatch();
    when(runtimeServiceMock.setVariablesAsync(any(), any(), any(), any()))
        .thenReturn(batchMock);

    HistoricProcessInstanceQuery mockedHistoricProcessInstanceQuery =
        mock(HistoricProcessInstanceQuery.class);
    when(historyServiceMock.createHistoricProcessInstanceQuery())
        .thenReturn(mockedHistoricProcessInstanceQuery);

    SetVariablesAsyncDto body = new SetVariablesAsyncDto();

    HistoricProcessInstanceQueryDto historicProcessInstanceQueryDto =
        new HistoricProcessInstanceQueryDto();
    historicProcessInstanceQueryDto.setProcessDefinitionId("foo");
    body.setHistoricProcessInstanceQuery(historicProcessInstanceQueryDto);

    // when
    Response response = given()
          .contentType(ContentType.JSON)
          .body(body)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(PROCESS_INSTANCE_SET_VARIABLES_ASYNC_URL);

    // then
    verify(mockedHistoricProcessInstanceQuery)
        .processDefinitionId("foo");

    verify(runtimeServiceMock).setVariablesAsync(
        eq(null),
        eq(null),
        eq(mockedHistoricProcessInstanceQuery),
        eq(null)
    );

    verifyBatchJson(response.asString());
  }

  @Test
  public void shouldSetVariablesAsync_ByIds() {
    // given
    Batch batchMock = createMockBatch();
    when(runtimeServiceMock.setVariablesAsync(any(), any(), any(), any()))
        .thenReturn(batchMock);

    SetVariablesAsyncDto body = new SetVariablesAsyncDto();

    List<String> processInstanceIds = Arrays.asList("foo", "bar");
    body.setProcessInstanceIds(processInstanceIds);

    // when
    Response response = given()
          .contentType(ContentType.JSON)
          .body(body)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(PROCESS_INSTANCE_SET_VARIABLES_ASYNC_URL);

    // then
    verify(runtimeServiceMock).setVariablesAsync(
        eq(processInstanceIds),
        eq(null),
        eq(null),
        eq(null)
    );

    verifyBatchJson(response.asString());
  }

  @Test
  public void shouldCorrelateMessageAsync() {
    // given
    Batch batchMock = createMockBatch();
    MessageCorrelationAsyncBuilder builderMock = mock(MessageCorrelationAsyncBuilder.class, RETURNS_SELF);
    when(builderMock.correlateAllAsync()).thenReturn(batchMock);
    when(runtimeServiceMock.createMessageCorrelationAsync(any())).thenReturn(builderMock);

    CorrelationMessageAsyncDto body = new CorrelationMessageAsyncDto();

    // when
    Response response = given()
      .contentType(ContentType.JSON)
      .body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(PROCESS_INSTANCE_CORRELATE_MESSAGE_ASYNC_URL);

    // then
    verify(runtimeServiceMock).createMessageCorrelationAsync(null);
    verify(builderMock).correlateAllAsync();

    verifyBatchJson(response.asString());
  }

  @Test
  public void shouldNotTransformProcessEngineExceptionToInvalidRequestExceptionWhenCorrelateMessageAsync() {
    // given
    doThrow(new ProcessEngineException("message")).when(runtimeServiceMock).createMessageCorrelationAsync(any());

    // when + then
    given()
      .contentType(ContentType.JSON)
      .body("{}")
    .then().expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
      .body("message", equalTo("message"))
    .when()
      .post(PROCESS_INSTANCE_CORRELATE_MESSAGE_ASYNC_URL);
  }

  @Test
  public void shouldThrowExceptionWhenCorrelateMessageAsync_AuthorizationException() {
    // given
    doThrow(new AuthorizationException("message")).when(runtimeServiceMock).createMessageCorrelationAsync(any());

    // when + then
    given()
      .contentType(ContentType.JSON)
      .body("{}")
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body(Matchers.containsString("{\"type\":\"AuthorizationException\""))
    .when()
      .post(PROCESS_INSTANCE_CORRELATE_MESSAGE_ASYNC_URL);
  }

  @Test
  public void shouldThrowExceptionWhenCorrelateMessageAsync_NullValueException() {
    // given
    doThrow(new NullValueException("message")).when(runtimeServiceMock).createMessageCorrelationAsync(any());

    // when + then
    given()
      .contentType(ContentType.JSON)
      .body("{}")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("message"))
    .when()
      .post(PROCESS_INSTANCE_CORRELATE_MESSAGE_ASYNC_URL);
  }

  @Test
  public void shouldThrowExceptionWhenCorrelateMessageAsync_BadUserRequestException() {
    // given
    doThrow(new BadUserRequestException("message")).when(runtimeServiceMock).createMessageCorrelationAsync(any());

    // when + then
    given()
      .contentType(ContentType.JSON)
      .body("{}")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(BadUserRequestException.class.getSimpleName()))
      .body("message", equalTo("message"))
    .when()
      .post(PROCESS_INSTANCE_CORRELATE_MESSAGE_ASYNC_URL);
  }

  @Test
  public void shouldCorrelateMessageAsync_RuntimeQuery() {
    // given
    Batch batchMock = createMockBatch();
    MessageCorrelationAsyncBuilder builderMock = mock(MessageCorrelationAsyncBuilder.class, RETURNS_SELF);
    when(builderMock.correlateAllAsync()).thenReturn(batchMock);
    when(runtimeServiceMock.createMessageCorrelationAsync(any())).thenReturn(builderMock);

    ProcessInstanceQuery mockedProcessInstanceQuery = mock(ProcessInstanceQuery.class);
    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(mockedProcessInstanceQuery);

    CorrelationMessageAsyncDto body = new CorrelationMessageAsyncDto();

    ProcessInstanceQueryDto processInstanceQueryDto = new ProcessInstanceQueryDto();
    processInstanceQueryDto.setProcessDefinitionId("foo");
    body.setProcessInstanceQuery(processInstanceQueryDto);

    // when
    Response response = given()
      .contentType(ContentType.JSON)
      .body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(PROCESS_INSTANCE_CORRELATE_MESSAGE_ASYNC_URL);

    // then

    verify(mockedProcessInstanceQuery)
        .processDefinitionId("foo");

    verify(runtimeServiceMock).createMessageCorrelationAsync(null);
    verify(builderMock).processInstanceQuery(mockedProcessInstanceQuery);
    verify(builderMock).correlateAllAsync();

    verifyBatchJson(response.asString());
  }

  @Test
  public void shouldCorrelateMessageAsync_HistoryQuery() {
    // given
    Batch batchMock = createMockBatch();
    MessageCorrelationAsyncBuilder builderMock = mock(MessageCorrelationAsyncBuilder.class, RETURNS_SELF);
    when(builderMock.correlateAllAsync()).thenReturn(batchMock);
    when(runtimeServiceMock.createMessageCorrelationAsync(any())).thenReturn(builderMock);

    HistoricProcessInstanceQuery mockedHistoricProcessInstanceQuery =
        mock(HistoricProcessInstanceQuery.class);
    when(historyServiceMock.createHistoricProcessInstanceQuery())
        .thenReturn(mockedHistoricProcessInstanceQuery);

    CorrelationMessageAsyncDto body = new CorrelationMessageAsyncDto();

    HistoricProcessInstanceQueryDto historicProcessInstanceQueryDto =
        new HistoricProcessInstanceQueryDto();
    historicProcessInstanceQueryDto.setProcessDefinitionId("foo");
    body.setHistoricProcessInstanceQuery(historicProcessInstanceQueryDto);

    // when
    Response response = given()
      .contentType(ContentType.JSON)
      .body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(PROCESS_INSTANCE_CORRELATE_MESSAGE_ASYNC_URL);

    // then
    verify(mockedHistoricProcessInstanceQuery)
        .processDefinitionId("foo");

    verify(runtimeServiceMock).createMessageCorrelationAsync(null);
    verify(builderMock).historicProcessInstanceQuery(mockedHistoricProcessInstanceQuery);
    verify(builderMock).correlateAllAsync();

    verifyBatchJson(response.asString());
  }

  @Test
  public void shouldCorrelateMessageAsync_ByIds() {
    // given
    Batch batchMock = createMockBatch();
    MessageCorrelationAsyncBuilder builderMock = mock(MessageCorrelationAsyncBuilder.class, RETURNS_SELF);
    when(builderMock.correlateAllAsync()).thenReturn(batchMock);
    when(runtimeServiceMock.createMessageCorrelationAsync(any())).thenReturn(builderMock);

    CorrelationMessageAsyncDto body = new CorrelationMessageAsyncDto();

    List<String> processInstanceIds = Arrays.asList("foo", "bar");
    body.setProcessInstanceIds(processInstanceIds);

    // when
    Response response = given()
      .contentType(ContentType.JSON)
      .body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(PROCESS_INSTANCE_CORRELATE_MESSAGE_ASYNC_URL);

    // then
    verify(runtimeServiceMock).createMessageCorrelationAsync(null);
    verify(builderMock).processInstanceIds(processInstanceIds);
    verify(builderMock).correlateAllAsync();

    verifyBatchJson(response.asString());
  }

  @SuppressWarnings("unchecked")
  protected ProcessInstanceModificationInstantiationBuilder setUpMockModificationBuilder() {
    ProcessInstanceModificationInstantiationBuilder mockModificationBuilder =
        mock(ProcessInstanceModificationInstantiationBuilder.class);

    when(mockModificationBuilder.cancelActivityInstance(anyString())).thenReturn(mockModificationBuilder);
    when(mockModificationBuilder.cancelAllForActivity(anyString())).thenReturn(mockModificationBuilder);
    when(mockModificationBuilder.cancellationSourceExternal(anyBoolean())).thenReturn(mockModificationBuilder);
    when(mockModificationBuilder.startAfterActivity(anyString())).thenReturn(mockModificationBuilder);
    when(mockModificationBuilder.startAfterActivity(anyString(), anyString())).thenReturn(mockModificationBuilder);
    when(mockModificationBuilder.startBeforeActivity(anyString())).thenReturn(mockModificationBuilder);
    when(mockModificationBuilder.startBeforeActivity(anyString(), anyString())).thenReturn(mockModificationBuilder);
    when(mockModificationBuilder.startTransition(anyString())).thenReturn(mockModificationBuilder);
    when(mockModificationBuilder.startTransition(anyString(), anyString())).thenReturn(mockModificationBuilder);
    when(mockModificationBuilder.setVariables(any(Map.class))).thenReturn(mockModificationBuilder);
    when(mockModificationBuilder.setVariablesLocal(any(Map.class))).thenReturn(mockModificationBuilder);

    return mockModificationBuilder;

  }

  protected void verifyBatchJson(String batchJson) {
    BatchDto batch = JsonPathUtil.from(batchJson).getObject("", BatchDto.class);
    assertNotNull("The returned batch should not be null.", batch);
    assertEquals(MockProvider.EXAMPLE_BATCH_ID, batch.getId());
    assertEquals(MockProvider.EXAMPLE_BATCH_TYPE, batch.getType());
    assertEquals(MockProvider.EXAMPLE_BATCH_TOTAL_JOBS, batch.getTotalJobs());
    assertEquals(MockProvider.EXAMPLE_BATCH_JOBS_PER_SEED, batch.getBatchJobsPerSeed());
    assertEquals(MockProvider.EXAMPLE_INVOCATIONS_PER_BATCH_JOB, batch.getInvocationsPerBatchJob());
    assertEquals(MockProvider.EXAMPLE_SEED_JOB_DEFINITION_ID, batch.getSeedJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_MONITOR_JOB_DEFINITION_ID, batch.getMonitorJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_BATCH_JOB_DEFINITION_ID, batch.getBatchJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_TENANT_ID, batch.getTenantId());
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void verifyTaskComments(List<Comment> mockTaskComments, Response response) {
    List list = response.as(List.class);
    assertEquals(1, list.size());

    LinkedHashMap<String, String> resourceHashMap = (LinkedHashMap<String, String>) list.get(0);

    String returnedId = resourceHashMap.get("id");
    String returnedUserId = resourceHashMap.get("userId");
    String returnedTaskId = resourceHashMap.get("taskId");
    Date returnedTime = DateTimeUtil.parseDate(resourceHashMap.get("time"));
    String returnedFullMessage = resourceHashMap.get("message");

    Comment mockComment = mockTaskComments.get(0);

    assertEquals(mockComment.getId(), returnedId);
    assertEquals(mockComment.getTaskId(), returnedTaskId);
    assertEquals(mockComment.getUserId(), returnedUserId);
    assertEquals(mockComment.getTime(), returnedTime);
    assertEquals(mockComment.getFullMessage(), returnedFullMessage);
  }
}
