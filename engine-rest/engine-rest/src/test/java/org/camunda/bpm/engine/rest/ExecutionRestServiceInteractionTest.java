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
import static org.camunda.bpm.engine.rest.util.DateTimeUtils.DATE_FORMAT_WITH_TIMEZONE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.helper.EqualsList;
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.camunda.bpm.engine.rest.helper.ErrorMessageHelper;
import org.camunda.bpm.engine.rest.helper.MockObjectValue;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.variable.EqualsNullValue;
import org.camunda.bpm.engine.rest.helper.variable.EqualsObjectValue;
import org.camunda.bpm.engine.rest.helper.variable.EqualsPrimitiveValue;
import org.camunda.bpm.engine.rest.helper.variable.EqualsUntypedValue;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.DeserializationTypeValidator;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.SerializableValueType;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ExecutionRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String EXECUTION_URL = TEST_RESOURCE_ROOT_PATH + "/execution/{id}";
  protected static final String SIGNAL_EXECUTION_URL = EXECUTION_URL + "/signal";
  protected static final String EXECUTION_LOCAL_VARIABLES_URL = EXECUTION_URL + "/localVariables";
  protected static final String SINGLE_EXECUTION_LOCAL_VARIABLE_URL = EXECUTION_LOCAL_VARIABLES_URL + "/{varId}";
  protected static final String SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL = SINGLE_EXECUTION_LOCAL_VARIABLE_URL + "/data";
  protected static final String MESSAGE_SUBSCRIPTION_URL = EXECUTION_URL + "/messageSubscriptions/{messageName}";
  protected static final String TRIGGER_MESSAGE_SUBSCRIPTION_URL = EXECUTION_URL + "/messageSubscriptions/{messageName}/trigger";
  protected static final String CREATE_INCIDENT_URL = EXECUTION_URL + "/create-incident";

  private RuntimeServiceImpl runtimeServiceMock;

  @Before
  public void setUpRuntimeData() {
    runtimeServiceMock = mock(RuntimeServiceImpl.class);
    when(runtimeServiceMock.getVariablesLocalTyped(MockProvider.EXAMPLE_EXECUTION_ID, true)).thenReturn(EXAMPLE_VARIABLES);
    mockEventSubscriptionQuery();

    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);
  }

  private void mockEventSubscriptionQuery() {
    EventSubscription mockSubscription = MockProvider.createMockEventSubscription();
    EventSubscriptionQuery mockQuery = mock(EventSubscriptionQuery.class);
    when(runtimeServiceMock.createEventSubscriptionQuery()).thenReturn(mockQuery);
    when(mockQuery.executionId(eq(MockProvider.EXAMPLE_EXECUTION_ID))).thenReturn(mockQuery);
    when(mockQuery.eventType(eq(MockProvider.EXAMPLE_EVENT_SUBSCRIPTION_TYPE))).thenReturn(mockQuery);
    when(mockQuery.eventName(eq(MockProvider.EXAMPLE_EVENT_SUBSCRIPTION_NAME))).thenReturn(mockQuery);
    when(mockQuery.singleResult()).thenReturn(mockSubscription);
  }

  @Test
  public void testGetSingleExecution() {
    Execution mockExecution = MockProvider.createMockExecution();
    ExecutionQuery sampleExecutionQuery = mock(ExecutionQuery.class);
    when(runtimeServiceMock.createExecutionQuery()).thenReturn(sampleExecutionQuery);
    when(sampleExecutionQuery.executionId(MockProvider.EXAMPLE_EXECUTION_ID)).thenReturn(sampleExecutionQuery);
    when(sampleExecutionQuery.singleResult()).thenReturn(mockExecution);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(MockProvider.EXAMPLE_EXECUTION_ID))
      .body("ended", equalTo(MockProvider.EXAMPLE_EXECUTION_IS_ENDED))
      .body("processInstanceId", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("tenantId", equalTo(MockProvider.EXAMPLE_TENANT_ID))
      .when().get(EXECUTION_URL);
  }

  @Test
  public void testGetNonExistingExecution() {
    ExecutionQuery sampleExecutionQuery = mock(ExecutionQuery.class);
    when(runtimeServiceMock.createExecutionQuery()).thenReturn(sampleExecutionQuery);
    when(sampleExecutionQuery.executionId(anyString())).thenReturn(sampleExecutionQuery);
    when(sampleExecutionQuery.singleResult()).thenReturn(null);

    String nonExistingExecutionId = "aNonExistingInstanceId";

    given().pathParam("id", nonExistingExecutionId)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Execution with id " + nonExistingExecutionId + " does not exist"))
      .when().get(EXECUTION_URL);
  }

  @Test
  public void testSignalExecution() {
    String variableKey = "aKey";
    int variableValue = 123;

    Map<String, Object> variablesJson = new HashMap<>();
    Map<String, Object> variables = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    variablesJson.put("variables", variables);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(variablesJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(SIGNAL_EXECUTION_URL);

    Map<String, Object> expectedSignalVariables = new HashMap<>();
    expectedSignalVariables.put(variableKey, variableValue);

    verify(runtimeServiceMock).signal(eq(MockProvider.EXAMPLE_EXECUTION_ID), argThat(new EqualsMap(expectedSignalVariables)));
  }

  @Test
  public void testSignalWithUnparseableIntegerVariable() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Integer";


    Map<String, Object> variablesJson = new HashMap<>();
    Map<String, Object> variables = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    variablesJson.put("variables", variables);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(variablesJson)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot signal execution anExecutionId: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Integer.class)))
    .when().post(SIGNAL_EXECUTION_URL);
  }

  @Test
  public void testSignalWithUnparseableShortVariable() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Short";


    Map<String, Object> variablesJson = new HashMap<>();
    Map<String, Object> variables = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    variablesJson.put("variables", variables);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(variablesJson)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot signal execution anExecutionId: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Short.class)))
    .when().post(SIGNAL_EXECUTION_URL);
  }

  @Test
  public void testSignalWithUnparseableLongVariable() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Long";

    Map<String, Object> variablesJson = new HashMap<>();
    Map<String, Object> variables = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    variablesJson.put("variables", variables);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(variablesJson)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot signal execution anExecutionId: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Long.class)))
    .when().post(SIGNAL_EXECUTION_URL);
  }

  @Test
  public void testSignalWithUnparseableDoubleVariable() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Double";


    Map<String, Object> variablesJson = new HashMap<>();
    Map<String, Object> variables = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    variablesJson.put("variables", variables);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(variablesJson)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot signal execution anExecutionId: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Double.class)))
    .when().post(SIGNAL_EXECUTION_URL);
  }

  @Test
  public void testSignalWithUnparseableDateVariable() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Date";

    Map<String, Object> variablesJson = new HashMap<>();
    Map<String, Object> variables = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    variablesJson.put("variables", variables);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(variablesJson)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot signal execution anExecutionId: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Date.class)))
    .when().post(SIGNAL_EXECUTION_URL);
  }

  @Test
  public void testSignalWithNotSupportedVariableType() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "X";


    Map<String, Object> variablesJson = new HashMap<>();
    Map<String, Object> variables = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    variablesJson.put("variables", variables);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(variablesJson)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot signal execution anExecutionId: Unsupported value type 'X'"))
    .when().post(SIGNAL_EXECUTION_URL);
  }

  @Test
  public void testSignalNonExistingExecution() {
    doThrow(new ProcessEngineException("expected exception")).when(runtimeServiceMock).signal(any(), any());

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot signal execution " + MockProvider.EXAMPLE_EXECUTION_ID + ": expected exception"))
      .when().post(SIGNAL_EXECUTION_URL);
  }

  @Test
  public void testSignalThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(runtimeServiceMock).signal(any(), any());

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .contentType(ContentType.JSON)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(SIGNAL_EXECUTION_URL);
  }

  @Test
  public void testGetLocalVariables() {
    Response response = given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(EXAMPLE_VARIABLE_KEY, notNullValue())
      .body(EXAMPLE_VARIABLE_KEY + ".value", equalTo(EXAMPLE_VARIABLE_VALUE.getValue()))
      .body(EXAMPLE_VARIABLE_KEY + ".type", equalTo(String.class.getSimpleName()))
      .when().get(EXECUTION_LOCAL_VARIABLES_URL);

    Assert.assertEquals("Should return exactly one variable", 1, response.jsonPath().getMap("").size());
  }

  @Test
  public void testGetLocalVariablesForNonExistingExecution() {
    when(runtimeServiceMock.getVariablesLocalTyped(anyString(), eq(true))).thenThrow(new ProcessEngineException("expected exception"));

    given().pathParam("id", "aNonExistingExecutionId")
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
      .body("message", equalTo("expected exception"))
      .when().get(EXECUTION_LOCAL_VARIABLES_URL);
  }

  @Test
  public void testGetLocalObjectVariables() {
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

    when(runtimeServiceMock.getVariablesLocalTyped(eq(MockProvider.EXAMPLE_EXECUTION_ID), anyBoolean()))
      .thenReturn(Variables.createVariables().putValueTyped(variableKey, variableValue));

    // when
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(variableKey + ".value", equalTo(payload))
      .body(variableKey + ".type", equalTo("Object"))
      .body(variableKey + ".valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body(variableKey + ".valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
      .when().get(EXECUTION_LOCAL_VARIABLES_URL);

    // then
    verify(runtimeServiceMock).getVariablesLocalTyped(MockProvider.EXAMPLE_EXECUTION_ID, true);
  }

  @Test
  public void testGetLocalObjectVariablesSerialized() {
    // given
    String variableKey = "aVariableId";

    ObjectValue variableValue =
        Variables
          .serializedObjectValue("a serialized value")
          .serializationDataFormat("application/json")
          .objectTypeName(ArrayList.class.getName())
          .create();

    when(runtimeServiceMock.getVariablesLocalTyped(eq(MockProvider.EXAMPLE_EXECUTION_ID), anyBoolean()))
      .thenReturn(Variables.createVariables().putValueTyped(variableKey, variableValue));

    // when
    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .queryParam("deserializeValues", false)
    .then().expect().statusCode(Status.OK.getStatusCode())
      .body(variableKey + ".value", equalTo("a serialized value"))
      .body(variableKey + ".type", equalTo("Object"))
      .body(variableKey + ".valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body(variableKey + ".valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
      .when().get(EXECUTION_LOCAL_VARIABLES_URL);

    // then
    verify(runtimeServiceMock).getVariablesLocalTyped(MockProvider.EXAMPLE_EXECUTION_ID, false);
  }

  @Test
  public void testGetLocalVariablesThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(runtimeServiceMock).getVariablesLocalTyped(anyString(), anyBoolean());

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(EXECUTION_LOCAL_VARIABLES_URL);
  }

  @Test
  public void testLocalVariableModification() {
    Map<String, Object> messageBodyJson = new HashMap<>();

    String variableKey = "aKey";
    int variableValue = 123;

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    messageBodyJson.put("modifications", modifications);

    List<String> deletions = new ArrayList<>();
    deletions.add("deleteKey");
    messageBodyJson.put("deletions", deletions);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(EXECUTION_LOCAL_VARIABLES_URL);

    Map<String, Object> expectedModifications = new HashMap<>();
    expectedModifications.put(variableKey, variableValue);
    verify(runtimeServiceMock).updateVariablesLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), argThat(new EqualsMap(expectedModifications)),
        argThat(new EqualsList(deletions)));
  }

  @Test
  public void testLocalVariableModificationForNonExistingExecution() {
    doThrow(new ProcessEngineException("expected exception")).when(runtimeServiceMock).updateVariablesLocal(any(), any(), any());

    Map<String, Object> messageBodyJson = new HashMap<>();

    String variableKey = "aKey";
    int variableValue = 123;
    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for execution " + MockProvider.EXAMPLE_EXECUTION_ID + ": expected exception"))
      .when().post(EXECUTION_LOCAL_VARIABLES_URL);
  }

  @Test
  public void testEmptyLocalVariableModification() {
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(EXECUTION_LOCAL_VARIABLES_URL);
  }

  @Test
  public void testLocalVariableModificationThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(runtimeServiceMock).updateVariablesLocal(any(), any(), any());

    Map<String, Object> messageBodyJson = new HashMap<>();

    String variableKey = "aKey";
    int variableValue = 123;
    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    messageBodyJson.put("modifications", modifications);

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(EXECUTION_LOCAL_VARIABLES_URL);
  }

  @Test
  public void testGetSingleLocalVariable() {
    String variableKey = "aVariableKey";
    int variableValue = 123;

    when(runtimeServiceMock.getVariableLocalTyped(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey), anyBoolean())).thenReturn(Variables.integerValue(variableValue));

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("value", is(123))
      .body("type", is("Integer"))
      .when().get(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }


  @Test
  public void testGetSingleLocalVariableData() {

    when(runtimeServiceMock.getVariableLocalTyped(anyString(), eq(EXAMPLE_BYTES_VARIABLE_KEY), eq(false))).thenReturn(EXAMPLE_VARIABLE_VALUE_BYTES);

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .pathParam("varId", EXAMPLE_BYTES_VARIABLE_KEY)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
    .when()
      .get(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock).getVariableLocalTyped(MockProvider.EXAMPLE_EXECUTION_ID, EXAMPLE_BYTES_VARIABLE_KEY, false);
  }

  @Test
  public void testGetSingleLocalVariableDataNonExisting() {

    when(runtimeServiceMock.getVariableLocalTyped(anyString(), eq("nonExisting"), eq(false))).thenReturn(null);

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .pathParam("varId", "nonExisting")
    .then()
      .expect()
        .statusCode(Status.NOT_FOUND.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is("execution variable with name " + "nonExisting" + " does not exist"))
    .when()
      .get(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock).getVariableLocalTyped(MockProvider.EXAMPLE_EXECUTION_ID, "nonExisting", false);
  }

  @Test
  public void testGetSingleLocalVariabledataNotBinary() {

    when(runtimeServiceMock.getVariableLocalTyped(anyString(), eq(EXAMPLE_VARIABLE_KEY), eq(false))).thenReturn(EXAMPLE_VARIABLE_VALUE);

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .pathParam("varId", EXAMPLE_VARIABLE_KEY)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock).getVariableLocalTyped(MockProvider.EXAMPLE_EXECUTION_ID, EXAMPLE_VARIABLE_KEY, false);
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

    when(runtimeServiceMock.getVariableLocalTyped(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    // when
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("value", equalTo(payload))
      .body("type", equalTo("Object"))
      .body("valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body("valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
      .when().get(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);

    // then
    verify(runtimeServiceMock).getVariableLocalTyped(MockProvider.EXAMPLE_EXECUTION_ID, variableKey, true);
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

    when(runtimeServiceMock.getVariableLocalTyped(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    // when
    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .pathParam("varId", variableKey)
      .queryParam("deserializeValue", false)
    .then().expect().statusCode(Status.OK.getStatusCode())
      .body("value", equalTo("a serialized value"))
      .body("type", equalTo("Object"))
      .body("valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body("valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
      .when().get(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);

    // then
    verify(runtimeServiceMock).getVariableLocalTyped(MockProvider.EXAMPLE_EXECUTION_ID, variableKey, false);
  }

  @Test
  public void testNonExistingLocalVariable() {
    String variableKey = "aVariableKey";

    when(runtimeServiceMock.getVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey))).thenReturn(null);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", is("execution variable with name " + variableKey + " does not exist"))
      .when().get(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }

  @Test
  public void testGetLocalVariableForNonExistingExecution() {
    String variableKey = "aVariableKey";

    when(runtimeServiceMock.getVariableLocalTyped(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey), eq(true)))
      .thenThrow(new ProcessEngineException("expected exception"));

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot get execution variable " + variableKey + ": expected exception"))
      .when().get(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }

  @Test
  public void testGetLocalVariableThrowsAuthorizationException() {
    String variableKey = "aVariableKey";

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(runtimeServiceMock).getVariableLocalTyped(anyString(), anyString(), anyBoolean());

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }

  @Test
  public void testGetFileVariable() {
    String variableKey = "aVariableKey";
    final byte[] byteContent = "some bytes".getBytes();
    String filename = "test.txt";
    String mimeType = "text/plain";
    FileValue variableValue = Variables.fileValue(filename).file(byteContent).mimeType(mimeType).create();

    when(runtimeServiceMock.getVariableLocalTyped(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON.toString())
    .and()
      .body("valueInfo.mimeType", equalTo(mimeType))
      .body("valueInfo.filename", equalTo(filename))
      .body("value", nullValue())
    .when().get(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }

  @Test
  public void testGetNullFileVariable() {
    String variableKey = "aVariableKey";
    String filename = "test.txt";
    String mimeType = "text/plain";
    FileValue variableValue = Variables.fileValue(filename).mimeType(mimeType).create();

    when(runtimeServiceMock.getVariableLocalTyped(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey), anyBoolean()))
      .thenReturn(variableValue);

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.TEXT.toString())
    .and()
      .body(is(equalTo("")))
    .when().get(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);
  }

  @Test
  public void testGetFileVariableDownloadWithType() {
    String variableKey = "aVariableKey";
    final byte[] byteContent = "some bytes".getBytes();
    String filename = "test.txt";
    FileValue variableValue = Variables.fileValue(filename).file(byteContent).mimeType(ContentType.TEXT.toString()).create();

    when(runtimeServiceMock.getVariableLocalTyped(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.TEXT.toString())
    .and()
      .body(is(equalTo(new String(byteContent))))
    .when().get(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);
  }

  @Test
  public void testGetFileVariableDownloadWithTypeAndEncoding() {
    String variableKey = "aVariableKey";
    final byte[] byteContent = "some bytes".getBytes();
    String filename = "test.txt";
    String encoding = "UTF-8";
    FileValue variableValue = Variables.fileValue(filename).file(byteContent).mimeType(ContentType.TEXT.toString()).encoding(encoding).create();

    when(runtimeServiceMock.getVariableLocalTyped(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    Response response = given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body(is(equalTo(new String(byteContent))))
    .when().get(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);

    String contentType = response.contentType().replaceAll(" ", "");
    assertThat(contentType, is(ContentType.TEXT + ";charset=" + encoding));
  }

  @Test
  public void testGetFileVariableDownloadWithoutType() {
    String variableKey = "aVariableKey";
    final byte[] byteContent = "some bytes".getBytes();
    String filename = "test.txt";
    FileValue variableValue = Variables.fileValue(filename).file(byteContent).create();

    when(runtimeServiceMock.getVariableLocalTyped(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
    .and()
      .body(is(equalTo(new String(byteContent))))
      .header("Content-Disposition", containsString(filename))
    .when().get(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);
  }

  @Test
  public void testCannotDownloadVariableOtherThanFile() {
    String variableKey = "aVariableKey";
    BooleanValue variableValue = Variables.booleanValue(true);

    when(runtimeServiceMock.getVariableLocalTyped(eq(EXAMPLE_TASK_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);
  }

  @Test
  public void testPutSingleLocalVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "aVariableValue";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);

    verify(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
        argThat(EqualsUntypedValue.matcher().value(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithTypeInteger() {
    String variableKey = "aVariableKey";
    Integer variableValue = 123;
    String type = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);

    verify(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.integerValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableInteger() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put execution variable aVariableKey: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Integer.class)))
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeShort() {
    String variableKey = "aVariableKey";
    Short variableValue = 123;
    String type = "Short";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);

    verify(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.shortValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableShort() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Short";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put execution variable aVariableKey: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Short.class)))
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeLong() {
    String variableKey = "aVariableKey";
    Long variableValue = Long.valueOf(123);
    String type = "Long";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);

    verify(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.longValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableLong() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Long";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put execution variable aVariableKey: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Long.class)))
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeDouble() {
    String variableKey = "aVariableKey";
    Double variableValue = 123.456;
    String type = "Double";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);

    verify(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.doubleValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableDouble() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Double";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put execution variable aVariableKey: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Double.class)))
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeBoolean() {
    String variableKey = "aVariableKey";
    Boolean variableValue = true;
    String type = "Boolean";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);

    verify(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
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

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);

    verify(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.dateValue(expectedValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableDate() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Date";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put execution variable aVariableKey: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Date.class)))
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithNotSupportedType() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "X";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put execution variable aVariableKey: Unsupported value type 'X'"))
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableThrowsAuthorizationException() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "String";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(runtimeServiceMock).setVariableLocal(anyString(), anyString(), any());

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }

  @Test
  public void testPutSingleLocalBinaryVariable() {
    byte[] bytes = "someContent".getBytes();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .multiPart("data", null, bytes)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock).setVariableLocal(
        eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.bytesValue(bytes)));
  }

  @Test
  public void testPutSingleLocalBinaryVariableWithValueType() {
    byte[] bytes = "someContent".getBytes();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .multiPart("data", null, bytes)
      .multiPart("valueType", "Bytes", "text/plain")
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock).setVariableLocal(
        eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.bytesValue(bytes)));
  }

  @Test
  public void testPutSingleLocalBinaryVariableWithUnknownValueType() {
    byte[] bytes = "someContent".getBytes();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .multiPart("data", null, bytes)
      .multiPart("valueType", "SomeUnknownType", "text/plain")
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Unsupported value type 'SomeUnknownType'"))
    .when()
      .post(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock, never()).setVariableLocal(anyString(), anyString(), any(Object.class));
  }

  @Test
  public void testPutSingleLocalBinaryVariableWithValueTypeOfWrongMimeType() {
    byte[] bytes = "someContent".getBytes();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .multiPart("data", null, bytes)
      .multiPart("valueType", "{ \"type\": \"Bytes\"", "application/json")
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Form part with name 'valueType' must have a text/plain value"))
    .when()
      .post(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock, never()).setVariableLocal(anyString(), anyString(), any(Object.class));
  }

  @Test
  public void testPutSingleLocalBinaryVariableWithNoValue() {
    byte[] bytes = new byte[0];

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .multiPart("data", null, bytes)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock).setVariableLocal(
        eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.bytesValue(bytes)));
  }

  @Test
  public void testPutSingleLocalSerializableVariableFromJson() throws Exception {

    ArrayList<String> serializable = new ArrayList<>();
    serializable.add("foo");

    ObjectMapper mapper = new ObjectMapper();
    String jsonBytes = mapper.writeValueAsString(serializable);
    String typeName = TypeFactory.defaultInstance().constructType(serializable.getClass()).toCanonical();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .multiPart("data", jsonBytes, MediaType.APPLICATION_JSON)
      .multiPart("type", typeName, MediaType.TEXT_PLAIN)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
        argThat(EqualsObjectValue.objectValueMatcher().isDeserialized().value(serializable)));
  }

  @Test
  public void testValidationOnPutSingleLocalSerializableVariableFromJson() throws Exception {
    boolean previousIsValidationEnabled = processEngine.getProcessEngineConfiguration().isDeserializationTypeValidationEnabled();
    DeserializationTypeValidator previousValidator = processEngine.getProcessEngineConfiguration().getDeserializationTypeValidator();

    DeserializationTypeValidator validatorMock = mock(DeserializationTypeValidator.class);
    when(validatorMock.validate(anyString())).thenReturn(true);
    when(processEngine.getProcessEngineConfiguration().isDeserializationTypeValidationEnabled()).thenReturn(true);
    when(processEngine.getProcessEngineConfiguration().getDeserializationTypeValidator()).thenReturn(validatorMock);

    try {
      ObjectMapper mapper = new ObjectMapper();
      String jsonBytes = mapper.writeValueAsString("test");
      String typeName = TypeFactory.defaultInstance().constructType(String.class).toCanonical();

      String variableKey = "aVariableKey";

      given()
        .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
        .multiPart("data", jsonBytes, MediaType.APPLICATION_JSON)
        .multiPart("type", typeName, MediaType.TEXT_PLAIN)
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .post(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);

      verify(validatorMock).validate("java.lang.String");
      verifyNoMoreInteractions(validatorMock);

      verify(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
          argThat(EqualsObjectValue.objectValueMatcher().isDeserialized().value("test")));
    } finally {
      when(processEngine.getProcessEngineConfiguration().isDeserializationTypeValidationEnabled()).thenReturn(previousIsValidationEnabled);
      when(processEngine.getProcessEngineConfiguration().getDeserializationTypeValidator()).thenReturn(previousValidator);
    }
  }

  @Test
  public void testFailingValidationOnPutSingleLocalSerializableVariableFromJson() throws Exception {
    boolean previousIsValidationEnabled = processEngine.getProcessEngineConfiguration().isDeserializationTypeValidationEnabled();
    DeserializationTypeValidator previousValidator = processEngine.getProcessEngineConfiguration().getDeserializationTypeValidator();

    DeserializationTypeValidator validatorMock = mock(DeserializationTypeValidator.class);
    when(validatorMock.validate(anyString())).thenReturn(false);
    when(processEngine.getProcessEngineConfiguration().isDeserializationTypeValidationEnabled()).thenReturn(true);
    when(processEngine.getProcessEngineConfiguration().getDeserializationTypeValidator()).thenReturn(validatorMock);

    try {
      ObjectMapper mapper = new ObjectMapper();
      String jsonBytes = mapper.writeValueAsString("test");
      String typeName = TypeFactory.defaultInstance().constructType(String.class).toCanonical();

      String variableKey = "aVariableKey";

      given()
        .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
        .multiPart("data", jsonBytes, MediaType.APPLICATION_JSON)
        .multiPart("type", typeName, MediaType.TEXT_PLAIN)
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .when()
        .post(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);

      verify(validatorMock).validate("java.lang.String");
      verifyNoMoreInteractions(validatorMock);
    } finally {
      when(processEngine.getProcessEngineConfiguration().isDeserializationTypeValidationEnabled()).thenReturn(previousIsValidationEnabled);
      when(processEngine.getProcessEngineConfiguration().getDeserializationTypeValidator()).thenReturn(previousValidator);
    }
  }

  @Test
  public void testPutSingleLocalSerializableVariableUnsupportedMediaType() throws Exception {

    ArrayList<String> serializable = new ArrayList<>();
    serializable.add("foo");

    ObjectMapper mapper = new ObjectMapper();
    String jsonBytes = mapper.writeValueAsString(serializable);
    String typeName = TypeFactory.defaultInstance().constructType(serializable.getClass()).toCanonical();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .multiPart("data", jsonBytes, "unsupported")
      .multiPart("type", typeName, MediaType.TEXT_PLAIN)
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body(containsString("Unrecognized content type for serialized java type: unsupported"))
    .when()
      .post(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock, never()).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
        eq(serializable));
  }

  @Test
  public void testPutSingleLocalBinaryVariableThrowsAuthorizationException() {
    byte[] bytes = "someContent".getBytes();
    String variableKey = "aVariableKey";

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(runtimeServiceMock).setVariableLocal(anyString(), anyString(), any());

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .multiPart("data", "unspecified", bytes)
    .expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);
  }

  @Test
  public void testPutSingleLocalVariableFromSerialized() {
    String serializedValue = "{\"prop\" : \"value\"}";
    Map<String, Object> requestJson = VariablesBuilder
        .getObjectValueMap(serializedValue, ValueType.OBJECT.getName(), "aDataFormat", "aRootType");

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON)
      .body(requestJson)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);

    verify(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
        argThat(EqualsObjectValue
          .objectValueMatcher()
          .serializationFormat("aDataFormat")
          .objectTypeName("aRootType")
          .serializedValue(serializedValue)));
  }

  @Test
  public void testPutSingleLocalVariableFromInvalidSerialized() {
    String serializedValue = "{\"prop\" : \"value\"}";

    Map<String, Object> requestJson = VariablesBuilder
        .getObjectValueMap(serializedValue, "aNonExistingType", null, null);

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON)
      .body(requestJson)
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put execution variable aVariableKey: Unsupported value type 'aNonExistingType'"))
    .when()
      .put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }

  @Test
  public void testPutSingleLocalVariableFromSerializedWithNoValue() {
    String variableKey = "aVariableKey";

    Map<String, Object> requestJson = VariablesBuilder
        .getObjectValueMap(null, ValueType.OBJECT.getName(), null, null);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(requestJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);

    verify(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
        argThat(EqualsObjectValue
          .objectValueMatcher()
          .serializationFormat(null)
          .objectTypeName(null)
          .serializedValue(null)));
  }

  @Test
  public void testPutSingleLocalVariableWithNoValue() {
    String variableKey = "aVariableKey";

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);

    verify(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
        argThat(EqualsNullValue.matcher()));
  }


  @Test
  public void testPutLocalVariableForNonExistingExecution() {
    String variableKey = "aVariableKey";
    String variableValue = "aVariableValue";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue);

    doThrow(new BadUserRequestException("expected exception"))
      .when(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey), any());

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot put execution variable " + variableKey + ": expected exception"))
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }

  @Test
  public void testDeleteSingleLocalVariable() {
    String variableKey = "aVariableKey";

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().delete(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);

    verify(runtimeServiceMock).removeVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey));
  }

  @Test
  public void testPostSingleLocalFileVariableWithEncodingAndMimeType() {

    byte[] value = "some text".getBytes();
    String variableKey = "aVariableKey";
    String encoding = "utf-8";
    String filename = "test.txt";
    String mimetype = MediaType.TEXT_PLAIN;

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .multiPart("data", filename, value, mimetype + "; encoding="+encoding)
      .multiPart("valueType", "File", "text/plain")
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);

    ArgumentCaptor<FileValue> captor = ArgumentCaptor.forClass(FileValue.class);
    verify(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
        captor.capture());
    FileValue captured = captor.getValue();
    assertThat(captured.getEncoding(), is(encoding));
    assertThat(captured.getFilename(), is(filename));
    assertThat(captured.getMimeType(), is(mimetype));
    assertThat(IoUtil.readInputStream(captured.getValue(), null), is(value));
  }

  @Test
  public void testPostSingleLocalFileVariableWithMimeType() {

    byte[] value = "some text".getBytes();
    String variableKey = "aVariableKey";
    String filename = "test.txt";
    String mimetype = MediaType.TEXT_PLAIN;

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .multiPart("data", filename, value, mimetype)
      .multiPart("valueType", "File", "text/plain")
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);

    ArgumentCaptor<FileValue> captor = ArgumentCaptor.forClass(FileValue.class);
    verify(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
        captor.capture());
    FileValue captured = captor.getValue();
    assertThat(captured.getEncoding(), is(nullValue()));
    assertThat(captured.getFilename(), is(filename));
    assertThat(captured.getMimeType(), is(mimetype));
    assertThat(IoUtil.readInputStream(captured.getValue(), null), is(value));
  }

  @Test
  public void testPostSingleLocalFileVariableWithEncoding() {

    byte[] value = "some text".getBytes();
    String variableKey = "aVariableKey";
    String encoding = "utf-8";
    String filename = "test.txt";

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .multiPart("data", filename, value, "encoding="+encoding)
      .multiPart("valueType", "File", "text/plain")
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
    //when the user passes an encoding, he has to provide the type, too
    .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);
  }

  @Test
  public void testPostSingleLocalFileVariableOnlyFilename() throws Exception {

    String variableKey = "aVariableKey";
    String filename = "test.txt";

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .multiPart("data", filename, new byte[0])
      .multiPart("valueType", "File", "text/plain")
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_EXECUTION_LOCAL_BINARY_VARIABLE_URL);

    ArgumentCaptor<FileValue> captor = ArgumentCaptor.forClass(FileValue.class);
    verify(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey),
        captor.capture());
    FileValue captured = captor.getValue();
    assertThat(captured.getEncoding(), is(nullValue()));
    assertThat(captured.getFilename(), is(filename));
    assertThat(captured.getMimeType(), is(MediaType.APPLICATION_OCTET_STREAM));
    assertThat(captured.getValue().available(), is(0));
  }

  @Test
  public void testDeleteLocalVariableForNonExistingExecution() {
    String variableKey = "aVariableKey";

    doThrow(new ProcessEngineException("expected exception"))
      .when(runtimeServiceMock).removeVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey));

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot delete execution variable " + variableKey + ": expected exception"))
      .when().delete(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }

  @Test
  public void testDeleteLocalVariableThrowsAuthorizationException() {
    String variableKey = "aVariableKey";

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(runtimeServiceMock).removeVariableLocal(anyString(), anyString());

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(message))
    .when()
      .delete(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }

  @Test
  public void testGetMessageEventSubscription() {
    String messageName = MockProvider.EXAMPLE_EVENT_SUBSCRIPTION_NAME;

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("messageName", messageName)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .body("id", equalTo(MockProvider.EXAMPLE_EVENT_SUBSCRIPTION_ID))
    .body("eventType", equalTo(MockProvider.EXAMPLE_EVENT_SUBSCRIPTION_TYPE))
    .body("eventName", equalTo(MockProvider.EXAMPLE_EVENT_SUBSCRIPTION_NAME))
    .body("executionId", equalTo(MockProvider.EXAMPLE_EXECUTION_ID))
    .body("processInstanceId", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
    .body("activityId", equalTo(MockProvider.EXAMPLE_ACTIVITY_ID))
    .body("createdDate", equalTo(MockProvider.EXAMPLE_EVENT_SUBSCRIPTION_CREATION_DATE))
    .body("tenantId", equalTo(MockProvider.EXAMPLE_TENANT_ID))
    .when().get(MESSAGE_SUBSCRIPTION_URL);
  }

  @Test
  public void testGetNonExistingMessageEventSubscription() {
    EventSubscriptionQuery sampleEventSubscriptionQuery = mock(EventSubscriptionQuery.class);
    when(runtimeServiceMock.createEventSubscriptionQuery()).thenReturn(sampleEventSubscriptionQuery);
    when(sampleEventSubscriptionQuery.executionId(anyString())).thenReturn(sampleEventSubscriptionQuery);
    when(sampleEventSubscriptionQuery.eventName(anyString())).thenReturn(sampleEventSubscriptionQuery);
    when(sampleEventSubscriptionQuery.eventType(anyString())).thenReturn(sampleEventSubscriptionQuery);
    when(sampleEventSubscriptionQuery.singleResult()).thenReturn(null);

    String executionId = MockProvider.EXAMPLE_EXECUTION_ID;
    String nonExistingMessageName = "aMessage";

    given().pathParam("id", executionId).pathParam("messageName", nonExistingMessageName)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Message event subscription for execution " + executionId + " named " + nonExistingMessageName + " does not exist"))
      .when().get(MESSAGE_SUBSCRIPTION_URL);
  }

  @Test
  public void testMessageEventTriggering() {
    String messageName = "aMessageName";
    String variableKey1 = "aVarName";
    String variableValue1 = "aVarValue";
    String variableKey2 = "anotherVarName";
    String variableValue2 = "anotherVarValue";

    Map<String, Object> variables = VariablesBuilder.create()
        .variable(variableKey1, variableValue1)
        .variable(variableKey2, variableValue2).getVariables();

    Map<String, Object> variablesJson = new HashMap<>();
    variablesJson.put("variables", variables);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("messageName", messageName)
      .contentType(ContentType.JSON).body(variablesJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(TRIGGER_MESSAGE_SUBSCRIPTION_URL);

    Map<String, Object> expectedVariables = new HashMap<>();
    expectedVariables.put(variableKey1, variableValue1);
    expectedVariables.put(variableKey2, variableValue2);

    verify(runtimeServiceMock).messageEventReceived(eq(messageName), eq(MockProvider.EXAMPLE_EXECUTION_ID),
        argThat(new EqualsMap(expectedVariables)));
  }

  @Test
  public void testMessageEventTriggeringWithoutVariables() {
    String messageName = "aMessageName";

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("messageName", messageName)
      .contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(TRIGGER_MESSAGE_SUBSCRIPTION_URL);

    verify(runtimeServiceMock).messageEventReceived(eq(messageName), eq(MockProvider.EXAMPLE_EXECUTION_ID),
        argThat(new EqualsMap(null)));
  }

  @Test
  public void testFailingMessageEventTriggering() {
    String messageName = "someMessage";
    doThrow(new ProcessEngineException("expected exception"))
      .when(runtimeServiceMock).messageEventReceived(any(), any(), any());

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("messageName", messageName)
      .contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot trigger message " + messageName + " for execution " + MockProvider.EXAMPLE_EXECUTION_ID + ": expected exception"))
      .when().post(TRIGGER_MESSAGE_SUBSCRIPTION_URL);
  }

  @Test
  public void testMessageEventTriggeringThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message))
      .when(runtimeServiceMock).messageEventReceived(any(), any(), any());

    given()
      .pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .pathParam("messageName", "someMessage")
      .contentType(ContentType.JSON)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(message))
    .when()
      .post(TRIGGER_MESSAGE_SUBSCRIPTION_URL);
  }

  @Test
  public void testCreateIncident() {
    when(runtimeServiceMock.createIncident(anyString(), anyString(), anyString(), anyString())).thenReturn(mock(Incident.class));
    Map<String, Object> json = new HashMap<>();
    json.put("incidentType", "incidentType");
    json.put("configuration", "configuration");
    json.put("message", "message");

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(json).then().expect().statusCode(Status.OK.getStatusCode())
        .when().post(CREATE_INCIDENT_URL);

    verify(runtimeServiceMock).createIncident("incidentType", MockProvider.EXAMPLE_EXECUTION_ID, "configuration", "message");
  }

  @Test
  public void testCreateIncidentWithNullIncidentType() {
    doThrow(new BadUserRequestException()).when(runtimeServiceMock).createIncident(any(), any(), any(), any());
    Map<String, Object> json = new HashMap<>();
    json.put("configuration", "configuration");
    json.put("message", "message");

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(json).then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).when().post(CREATE_INCIDENT_URL);
  }
}