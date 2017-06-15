/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TASK_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.NON_EXISTING_ID;
import static org.camunda.bpm.engine.rest.util.DateTimeUtils.DATE_FORMAT_WITH_TIMEZONE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.helper.EqualsList;
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.camunda.bpm.engine.rest.helper.ErrorMessageHelper;
import org.camunda.bpm.engine.rest.helper.MockObjectValue;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.VariableTypeHelper;
import org.camunda.bpm.engine.rest.helper.variable.EqualsNullValue;
import org.camunda.bpm.engine.rest.helper.variable.EqualsObjectValue;
import org.camunda.bpm.engine.rest.helper.variable.EqualsPrimitiveValue;
import org.camunda.bpm.engine.rest.helper.variable.EqualsUntypedValue;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
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
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * @author Daniel Meyer
 *
 */
public class TaskVariableRestResourceInteractionTest extends
  AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String TASK_SERVICE_URL = TEST_RESOURCE_ROOT_PATH + "/task";
  protected static final String SINGLE_TASK_URL = TASK_SERVICE_URL + "/{id}";

  protected static final String SINGLE_TASK_VARIABLES_URL = SINGLE_TASK_URL + "/variables";
  protected static final String SINGLE_TASK_SINGLE_VARIABLE_URL = SINGLE_TASK_VARIABLES_URL + "/{varId}";
  protected static final String SINGLE_TASK_PUT_SINGLE_VARIABLE_URL = SINGLE_TASK_SINGLE_VARIABLE_URL;
  protected static final String SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL = SINGLE_TASK_PUT_SINGLE_VARIABLE_URL + "/data";
  protected static final String SINGLE_TASK_DELETE_SINGLE_VARIABLE_URL = SINGLE_TASK_SINGLE_VARIABLE_URL;
  protected static final String SINGLE_TASK_MODIFY_VARIABLES_URL = SINGLE_TASK_VARIABLES_URL;

  protected TaskService taskServiceMock;

  @Before
  public void setUpRuntimeData() {
    taskServiceMock = mock(TaskService.class);
    when(processEngine.getTaskService()).thenReturn(taskServiceMock);
  }

  private TaskServiceImpl mockTaskServiceImpl() {
    TaskServiceImpl taskServiceMock = mock(TaskServiceImpl.class);
    when(processEngine.getTaskService()).thenReturn(taskServiceMock);
    return taskServiceMock;
  }

  @Test
  public void testGetVariables() {

    when(taskServiceMock.getVariablesTyped(EXAMPLE_TASK_ID, true)).thenReturn(EXAMPLE_VARIABLES);

    Response response = given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(EXAMPLE_VARIABLE_KEY, notNullValue())
      .body(EXAMPLE_VARIABLE_KEY + ".value", equalTo(EXAMPLE_VARIABLE_VALUE.getValue()))
      .body(EXAMPLE_VARIABLE_KEY + ".type", equalTo(VariableTypeHelper.toExpectedValueTypeName(EXAMPLE_VARIABLE_VALUE.getType())))
      .when().get(SINGLE_TASK_VARIABLES_URL);

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

    when(taskServiceMock.getVariablesTyped(eq(EXAMPLE_TASK_ID), anyBoolean()))
      .thenReturn(Variables.createVariables().putValueTyped(variableKey, variableValue));

    // when
    given().pathParam("id", EXAMPLE_TASK_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(variableKey + ".value", equalTo(payload))
      .body(variableKey + ".type", equalTo("Object"))
      .body(variableKey + ".valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body(variableKey + ".valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
      .when().get(SINGLE_TASK_VARIABLES_URL);

    // then
    verify(taskServiceMock).getVariablesTyped(EXAMPLE_TASK_ID, true);
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

    when(taskServiceMock.getVariablesTyped(eq(EXAMPLE_TASK_ID), anyBoolean()))
      .thenReturn(Variables.createVariables().putValueTyped(variableKey, variableValue));

    // when
    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .queryParam("deserializeValues", false)
    .then().expect().statusCode(Status.OK.getStatusCode())
      .body(variableKey + ".value", equalTo("a serialized value"))
      .body(variableKey + ".type", equalTo("Object"))
      .body(variableKey + ".valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body(variableKey + ".valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
      .when().get(SINGLE_TASK_VARIABLES_URL);

    // then
    verify(taskServiceMock).getVariablesTyped(EXAMPLE_TASK_ID, false);
  }

  @Test
  public void testGetVariablesForNonExistingTaskId() {
    when(taskServiceMock.getVariablesTyped(NON_EXISTING_ID, true)).thenThrow(new ProcessEngineException("task " + NON_EXISTING_ID + " doesn't exist"));

    given().pathParam("id", NON_EXISTING_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
      .body("message", equalTo("task " + NON_EXISTING_ID + " doesn't exist"))
      .when().get(SINGLE_TASK_VARIABLES_URL);
  }

  @Test
  public void testGetVariablesThrowsAuthorizationException() {
    String message = "expected exception";
    when(taskServiceMock.getVariablesTyped(anyString(), anyBoolean())).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(SINGLE_TASK_VARIABLES_URL);
  }

  @Test
  public void testVariableModification() {
    TaskServiceImpl taskServiceMock = mockTaskServiceImpl();

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    String variableKey = "aKey";
    int variableValue = 123;

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    messageBodyJson.put("modifications", modifications);

    List<String> deletions = new ArrayList<String>();
    deletions.add("deleteKey");
    messageBodyJson.put("deletions", deletions);

    given().pathParam("id", EXAMPLE_TASK_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(SINGLE_TASK_MODIFY_VARIABLES_URL);

    Map<String, Object> expectedModifications = new HashMap<String, Object>();
    expectedModifications.put(variableKey, variableValue);
    verify(taskServiceMock).updateVariables(eq(EXAMPLE_TASK_ID), argThat(new EqualsMap(expectedModifications)),
        argThat(new EqualsList(deletions)));
  }

  @Test
  public void testVariableModificationForNonExistingTaskId() {
    TaskServiceImpl taskServiceMock = mockTaskServiceImpl();
    doThrow(new ProcessEngineException("Cannot find task with id " + NON_EXISTING_ID)).when(taskServiceMock).updateVariables(anyString(), any(Map.class), any(List.class));

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    String variableKey = "aKey";
    int variableValue = 123;
    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", NON_EXISTING_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for task " + NON_EXISTING_ID + ": Cannot find task with id " + NON_EXISTING_ID))
      .when().post(SINGLE_TASK_MODIFY_VARIABLES_URL);
  }

  @Test
  public void testEmptyVariableModification() {
    mockTaskServiceImpl();

    given().pathParam("id", EXAMPLE_TASK_ID).contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(SINGLE_TASK_MODIFY_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationThrowsAuthorizationException() {
    String variableKey = "aKey";
    int variableValue = 123;
    Map<String, Object> messageBodyJson = new HashMap<String, Object>();
    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    messageBodyJson.put("modifications", modifications);

    TaskServiceImpl taskServiceMock = mockTaskServiceImpl();
    String message = "excpected exception";
    doThrow(new AuthorizationException(message)).when(taskServiceMock).updateVariables(anyString(), any(Map.class), any(List.class));

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(message))
    .when()
      .post(SINGLE_TASK_MODIFY_VARIABLES_URL);
  }

  @Test
  public void testGetSingleVariable() {
    String variableKey = "aVariableKey";
    int variableValue = 123;

    when(taskServiceMock.getVariableTyped(eq(EXAMPLE_TASK_ID), eq(variableKey), anyBoolean()))
      .thenReturn(Variables.integerValue(variableValue));

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("value", is(123))
      .body("type", is("Integer"))
      .when().get(SINGLE_TASK_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testGetSingleVariableData() {

    when(taskServiceMock.getVariableTyped(anyString(), eq(EXAMPLE_BYTES_VARIABLE_KEY), eq(false))).thenReturn(EXAMPLE_VARIABLE_VALUE_BYTES);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .pathParam("varId", EXAMPLE_BYTES_VARIABLE_KEY)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
    .when()
      .get(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    verify(taskServiceMock).getVariableTyped(MockProvider.EXAMPLE_TASK_ID, EXAMPLE_BYTES_VARIABLE_KEY, false);
  }

  @Test
  public void testGetSingleVariableDataNonExisting() {

    when(taskServiceMock.getVariableTyped(anyString(), eq("nonExisting"), eq(false))).thenReturn(null);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .pathParam("varId", "nonExisting")
    .then()
      .expect()
        .statusCode(Status.NOT_FOUND.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is("task variable with name " + "nonExisting" + " does not exist"))
    .when()
      .get(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    verify(taskServiceMock).getVariableTyped(MockProvider.EXAMPLE_TASK_ID, "nonExisting", false);
  }

  @Test
  public void testGetSingleVariabledataNotBinary() {

    when(taskServiceMock.getVariableTyped(anyString(), eq(EXAMPLE_VARIABLE_KEY), eq(false))).thenReturn(EXAMPLE_VARIABLE_VALUE);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .pathParam("varId", EXAMPLE_VARIABLE_KEY)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    verify(taskServiceMock).getVariableTyped(MockProvider.EXAMPLE_TASK_ID, EXAMPLE_VARIABLE_KEY, false);
  }

  @Test
  public void testGetSingleObjectVariable() {
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

    when(taskServiceMock.getVariableTyped(eq(EXAMPLE_TASK_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    // when
    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("value", equalTo(payload))
      .body("type", equalTo("Object"))
      .body("valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body("valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
      .when().get(SINGLE_TASK_SINGLE_VARIABLE_URL);

    // then
    verify(taskServiceMock).getVariableTyped(EXAMPLE_TASK_ID, variableKey, true);
  }

  @Test
  public void testGetSingleObjectVariableSerialized() {
    // given
    String variableKey = "aVariableId";

    ObjectValue variableValue =
        Variables
          .serializedObjectValue("a serialized value")
          .serializationDataFormat("application/json")
          .objectTypeName(ArrayList.class.getName())
          .create();

    when(taskServiceMock.getVariableTyped(eq(EXAMPLE_TASK_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    // when
    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("varId", variableKey)
      .queryParam("deserializeValue", false)
    .then().expect().statusCode(Status.OK.getStatusCode())
      .body("value", equalTo("a serialized value"))
      .body("type", equalTo("Object"))
      .body("valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body("valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
      .when().get(SINGLE_TASK_SINGLE_VARIABLE_URL);

    // then
    verify(taskServiceMock).getVariableTyped(EXAMPLE_TASK_ID, variableKey, false);
  }

  @Test
  public void testNonExistingVariable() {
    String variableKey = "aVariableKey";

    when(taskServiceMock.getVariable(eq(EXAMPLE_TASK_ID), eq(variableKey))).thenReturn(null);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", is("task variable with name " + variableKey + " does not exist"))
      .when().get(SINGLE_TASK_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testGetVariableForNonExistingTaskId() {
    String variableKey = "aVariableKey";

    when(taskServiceMock.getVariableTyped(eq(NON_EXISTING_ID), eq(variableKey), anyBoolean()))
      .thenThrow(new ProcessEngineException("task " + NON_EXISTING_ID + " doesn't exist"));

    given().pathParam("id", NON_EXISTING_ID).pathParam("varId", variableKey)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot get task variable " + variableKey + ": task " + NON_EXISTING_ID + " doesn't exist"))
      .when().get(SINGLE_TASK_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testGetSingleVariableThrowsAuthorizationException() {
    String variableKey = "aVariableKey";

    String message = "excpected exception";
    when(taskServiceMock.getVariableTyped(anyString(), anyString(), anyBoolean())).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(message))
    .when()
      .get(SINGLE_TASK_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testGetFileVariable() {
    String variableKey = "aVariableKey";
    final byte[] byteContent = "some bytes".getBytes();
    String filename = "test.txt";
    String mimeType = "text/plain";
    FileValue variableValue = Variables.fileValue(filename).file(byteContent).mimeType(mimeType).create();

    when(taskServiceMock.getVariableTyped(eq(EXAMPLE_TASK_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON.toString())
    .and()
      .body("valueInfo.mimeType", equalTo(mimeType))
      .body("valueInfo.filename", equalTo(filename))
      .body("value", nullValue())
    .when().get(SINGLE_TASK_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testGetNullFileVariable() {
    String variableKey = "aVariableKey";
    String filename = "test.txt";
    String mimeType = "text/plain";
    FileValue variableValue = Variables.fileValue(filename).mimeType(mimeType).create();

    when(taskServiceMock.getVariableTyped(eq(MockProvider.EXAMPLE_TASK_ID), eq(variableKey), anyBoolean()))
      .thenReturn(variableValue);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .pathParam("varId", variableKey)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.TEXT.toString())
      .and()
        .body(is(equalTo("")))
      .when().get(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);
  }

  @Test
  public void testGetFileVariableDownloadWithType() {
    String variableKey = "aVariableKey";
    final byte[] byteContent = "some bytes".getBytes();
    String filename = "test.txt";
    FileValue variableValue = Variables.fileValue(filename).file(byteContent).mimeType(ContentType.TEXT.toString()).create();

    when(taskServiceMock.getVariableTyped(eq(EXAMPLE_TASK_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.TEXT.toString())
    .and()
      .body(is(equalTo(new String(byteContent))))
    .when().get(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);
  }

  @Test
  public void testGetFileVariableDownloadWithTypeAndEncoding() {
    String variableKey = "aVariableKey";
    final byte[] byteContent = "some bytes".getBytes();
    String filename = "test.txt";
    String encoding = "UTF-8";
    FileValue variableValue = Variables.fileValue(filename).file(byteContent).mimeType(ContentType.TEXT.toString()).encoding(encoding).create();

    when(taskServiceMock.getVariableTyped(eq(EXAMPLE_TASK_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    Response response = given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body(is(equalTo(new String(byteContent))))
    .when().get(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    String contentType = response.contentType().replaceAll(" ", "");
    assertThat(contentType, is(ContentType.TEXT + ";charset=" + encoding));
  }

  @Test
  public void testGetFileVariableDownloadWithoutType() {
    String variableKey = "aVariableKey";
    final byte[] byteContent = "some bytes".getBytes();
    String filename = "test.txt";
    FileValue variableValue = Variables.fileValue(filename).file(byteContent).create();

    when(taskServiceMock.getVariableTyped(eq(EXAMPLE_TASK_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
    .and()
      .body(is(equalTo(new String(byteContent))))
      .header("Content-Disposition", containsString(filename))
    .when().get(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);
  }

  @Test
  public void testCannotDownloadVariableOtherThanFile() {
    String variableKey = "aVariableKey";
    BooleanValue variableValue = Variables.booleanValue(true);

    when(taskServiceMock.getVariableTyped(eq(EXAMPLE_TASK_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "aVariableValue";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariable(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsUntypedValue.matcher().value(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithTypeInteger() {
    String variableKey = "aVariableKey";
    Integer variableValue = 123;
    String type = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariable(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.integerValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableInteger() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put task variable " + variableKey + ": "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Integer.class)))
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeShort() {
    String variableKey = "aVariableKey";
    Short variableValue = 123;
    String type = "Short";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariable(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.shortValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableShort() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Short";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put task variable " +  variableKey + ": "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Short.class)))
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeLong() {
    String variableKey = "aVariableKey";
    Long variableValue = Long.valueOf(123);
    String type = "Long";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariable(eq(EXAMPLE_TASK_ID), eq(variableKey),
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
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put task variable " + variableKey + ": "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Long.class)))
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeDouble() {
    String variableKey = "aVariableKey";
    Double variableValue = 123.456;
    String type = "Double";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariable(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.doubleValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableDouble() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Double";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put task variable " + variableKey + ": "
            + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Double.class)))
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeBoolean() {
    String variableKey = "aVariableKey";
    Boolean variableValue = true;
    String type = "Boolean";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariable(eq(EXAMPLE_TASK_ID), eq(variableKey),
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

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariable(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.dateValue(expectedValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableDate() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Date";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put task variable " + variableKey + ": "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Date.class)))
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithNotSupportedType() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "X";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put task variable " + variableKey + ": Unsupported value type 'X'"))
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithNoValue() {
    String variableKey = "aVariableKey";

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariable(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsNullValue.matcher()));
  }

  @Test
  public void testPutVariableForNonExistingTaskId() {
    String variableKey = "aVariableKey";
    String variableValue = "aVariableValue";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue);

    doThrow(new ProcessEngineException("Cannot find task with id " + NON_EXISTING_ID))
      .when(taskServiceMock).setVariable(eq(NON_EXISTING_ID), eq(variableKey), any());

    given().pathParam("id", NON_EXISTING_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot put task variable " + variableKey + ": Cannot find task with id " + NON_EXISTING_ID))
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableThrowsAuthorizationException() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "String";
    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(taskServiceMock).setVariable(anyString(), anyString(), any());

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("varId", variableKey)
      .contentType(ContentType.JSON)
      .body(variableJson)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPostSingleBinaryVariable() throws Exception {
    byte[] bytes = "someContent".getBytes();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .multiPart("data", null, bytes)
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    verify(taskServiceMock).setVariable(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.bytesValue(bytes)));
  }

  @Test
  public void testPostSingleBinaryVariableWithValueType() throws Exception {
    byte[] bytes = "someContent".getBytes();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .multiPart("data", null, bytes)
      .multiPart("valueType", "Bytes", "text/plain")
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    verify(taskServiceMock).setVariable(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.bytesValue(bytes)));
  }

  @Test
  public void testPostSingleBinaryVariableWithNoValue() throws Exception {
    byte[] bytes = new byte[0];

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .multiPart("data", null, bytes)
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    verify(taskServiceMock).setVariable(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.bytesValue(bytes)));
  }

  @Test
  public void testPutSingleBinaryVariableThrowsAuthorizationException() {
    byte[] bytes = "someContent".getBytes();
    String variableKey = "aVariableKey";

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(taskServiceMock).setVariable(anyString(), anyString(), any());

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("varId", variableKey)
      .multiPart("data", "unspecified", bytes)
    .expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);
  }

  @Test
  public void testPostSingleSerializableVariable() throws Exception {

    ArrayList<String> serializable = new ArrayList<String>();
    serializable.add("foo");

    ObjectMapper mapper = new ObjectMapper();
    String jsonBytes = mapper.writeValueAsString(serializable);
    String typeName = TypeFactory.defaultInstance().constructType(serializable.getClass()).toCanonical();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .multiPart("data", jsonBytes, MediaType.APPLICATION_JSON)
      .multiPart("type", typeName, MediaType.TEXT_PLAIN)
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    verify(taskServiceMock).setVariable(eq(MockProvider.EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsObjectValue.objectValueMatcher().isDeserialized().value(serializable)));
  }

  @Test
  public void testPostSingleSerializableVariableUnsupportedMediaType() throws Exception {

    ArrayList<String> serializable = new ArrayList<String>();
    serializable.add("foo");

    ObjectMapper mapper = new ObjectMapper();
    String jsonBytes = mapper.writeValueAsString(serializable);
    String typeName = TypeFactory.defaultInstance().constructType(serializable.getClass()).toCanonical();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .multiPart("data", jsonBytes, "unsupported")
      .multiPart("type", typeName, MediaType.TEXT_PLAIN)
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body(containsString("Unrecognized content type for serialized java type: unsupported"))
    .when()
      .post(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    verify(taskServiceMock, never()).setVariable(eq(EXAMPLE_TASK_ID), eq(variableKey),
        eq(serializable));
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
      .post(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    ArgumentCaptor<FileValue> captor = ArgumentCaptor.forClass(FileValue.class);
    verify(taskServiceMock).setVariable(eq(MockProvider.EXAMPLE_TASK_ID), eq(variableKey),
        captor.capture());
    FileValue captured = captor.getValue();
    assertThat(captured.getEncoding(), is(encoding));
    assertThat(captured.getFilename(), is(filename));
    assertThat(captured.getMimeType(), is(mimetype));
    assertThat(IoUtil.readInputStream(captured.getValue(), null), is(value));
  }

  @Test
  public void testPostSingleFileVariableWithMimeType() throws Exception {

    byte[] value = "some text".getBytes();
    String base64 = Base64.encodeBase64String(value);
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
      .post(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    ArgumentCaptor<FileValue> captor = ArgumentCaptor.forClass(FileValue.class);
    verify(taskServiceMock).setVariable(eq(MockProvider.EXAMPLE_TASK_ID), eq(variableKey),
        captor.capture());
    FileValue captured = captor.getValue();
    assertThat(captured.getEncoding(), is(nullValue()));
    assertThat(captured.getFilename(), is(filename));
    assertThat(captured.getMimeType(), is(mimetype));
    assertThat(IoUtil.readInputStream(captured.getValue(), null), is(value));
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
      .post(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);
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
      .post(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    ArgumentCaptor<FileValue> captor = ArgumentCaptor.forClass(FileValue.class);
    verify(taskServiceMock).setVariable(eq(MockProvider.EXAMPLE_TASK_ID), eq(variableKey),
        captor.capture());
    FileValue captured = captor.getValue();
    assertThat(captured.getEncoding(), is(nullValue()));
    assertThat(captured.getFilename(), is(filename));
    assertThat(captured.getMimeType(), is(MediaType.APPLICATION_OCTET_STREAM));
    assertThat(captured.getValue().available(), is(0));
  }

  @Test
  public void testPutSingleVariableFromSerialized() throws Exception {
    String serializedValue = "{\"prop\" : \"value\"}";
    Map<String, Object> requestJson = VariablesBuilder
        .getObjectValueMap(serializedValue, ValueType.OBJECT.getName(), "aDataFormat", "aRootType");

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON)
      .body(requestJson)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariable(
        eq(MockProvider.EXAMPLE_TASK_ID), eq(variableKey),
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
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON)
      .body(requestJson)
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put task variable aVariableKey: Unsupported value type 'aNonExistingType'"))
    .when()
      .put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableFromSerializedWithNoValue() {
    String variableKey = "aVariableKey";

    Map<String, Object> requestJson = VariablesBuilder
        .getObjectValueMap(null, ValueType.OBJECT.getName(), null, null);

    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(requestJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariable(
        eq(MockProvider.EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsObjectValue.objectValueMatcher()));
  }

  @Test
  public void testDeleteSingleVariable() {
    String variableKey = "aVariableKey";

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().delete(SINGLE_TASK_DELETE_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).removeVariable(eq(EXAMPLE_TASK_ID), eq(variableKey));
  }

  @Test
  public void testDeleteVariableForNonExistingTaskId() {
    String variableKey = "aVariableKey";

    doThrow(new ProcessEngineException("Cannot find task with id " + NON_EXISTING_ID))
      .when(taskServiceMock).removeVariable(eq(NON_EXISTING_ID), eq(variableKey));

    given().pathParam("id", NON_EXISTING_ID).pathParam("varId", variableKey)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot delete task variable " + variableKey + ": Cannot find task with id " + NON_EXISTING_ID))
      .when().delete(SINGLE_TASK_DELETE_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testDeleteVariableThrowsAuthorizationException() {
    String variableKey = "aVariableKey";

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(taskServiceMock).removeVariable(anyString(), anyString());

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("varId", variableKey)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(message))
    .when()
      .delete(SINGLE_TASK_DELETE_SINGLE_VARIABLE_URL);
  }

}
