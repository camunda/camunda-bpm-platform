package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TASK_ID;
import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
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
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceSuspensionStateDto;
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
import org.camunda.bpm.engine.rest.util.ModificationInstructionBuilder;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationInstantiationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.SerializableValueType;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public abstract class AbstractProcessInstanceRestServiceInteractionTest extends
    AbstractRestServiceTest {

  protected static final String PROCESS_INSTANCE_URL = TEST_RESOURCE_ROOT_PATH + "/process-instance";
  protected static final String SINGLE_PROCESS_INSTANCE_URL = PROCESS_INSTANCE_URL + "/{id}";
  protected static final String PROCESS_INSTANCE_VARIABLES_URL = SINGLE_PROCESS_INSTANCE_URL + "/variables";
  protected static final String SINGLE_PROCESS_INSTANCE_VARIABLE_URL = PROCESS_INSTANCE_VARIABLES_URL + "/{varId}";
  protected static final String SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL = SINGLE_PROCESS_INSTANCE_VARIABLE_URL + "/data";
  protected static final String PROCESS_INSTANCE_ACTIVIY_INSTANCES_URL = SINGLE_PROCESS_INSTANCE_URL + "/activity-instances";
  private static final String EXAMPLE_PROCESS_INSTANCE_ID_WITH_NULL_VALUE_AS_VARIABLE = "aProcessInstanceWithNullValueAsVariable";
  protected static final String SINGLE_PROCESS_INSTANCE_SUSPENDED_URL = SINGLE_PROCESS_INSTANCE_URL + "/suspended";
  protected static final String PROCESS_INSTANCE_SUSPENDED_URL = PROCESS_INSTANCE_URL + "/suspended";
  protected static final String PROCESS_INSTANCE_MODIFICATION_URL = SINGLE_PROCESS_INSTANCE_URL + "/modification";

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

  private RuntimeServiceImpl runtimeServiceMock;

  @Before
  public void setUpRuntimeData() {
    runtimeServiceMock = mock(RuntimeServiceImpl.class);
    // variables
    when(runtimeServiceMock.getVariablesTyped(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, true)).thenReturn(EXAMPLE_VARIABLES);
    when(runtimeServiceMock.getVariablesTyped(MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID, true)).thenReturn(EXAMPLE_OBJECT_VARIABLES);
    when(runtimeServiceMock.getVariablesTyped(EXAMPLE_PROCESS_INSTANCE_ID_WITH_NULL_VALUE_AS_VARIABLE, true)).thenReturn(EXAMPLE_VARIABLES_WITH_NULL_VALUE);

    // activity instances
    when(runtimeServiceMock.getActivityInstance(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)).thenReturn(EXAMPLE_ACTIVITY_INSTANCE);

    // runtime service
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);
  }

  @Test
  public void testGetActivityInstanceTree() {
    Response response = given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(EXAMPLE_ACTIVITY_INSTANCE_ID))
        .body("parentActivityInstanceId", equalTo(EXAMPLE_PARENT_ACTIVITY_INSTANCE_ID))
        .body("activityId", equalTo(EXAMPLE_ACTIVITY_ID))
        .body("processInstanceId", equalTo(EXAMPLE_PROCESS_INSTANCE_ID))
        .body("processDefinitionId", equalTo(EXAMPLE_PROCESS_DEFINITION_ID))
        .body("executionIds", not(empty()))
        .body("executionIds[0]", equalTo(EXAMPLE_EXECUTION_ID))
        .body("activityName", equalTo(EXAMPLE_ACTIVITY_NAME))
        // "name" is deprecated and there for legacy reasons
        .body("name", equalTo(EXAMPLE_ACTIVITY_NAME))
        .body("childActivityInstances", not(empty()))
        .body("childActivityInstances[0].id", equalTo(CHILD_EXAMPLE_ACTIVITY_INSTANCE_ID))
        .body("childActivityInstances[0].parentActivityInstanceId", equalTo(CHILD_EXAMPLE_PARENT_ACTIVITY_INSTANCE_ID))
        .body("childActivityInstances[0].activityId", equalTo(CHILD_EXAMPLE_ACTIVITY_ID))
        .body("childActivityInstances[0].activityType", equalTo(CHILD_EXAMPLE_ACTIVITY_TYPE))
        .body("childActivityInstances[0].activityName", equalTo(CHILD_EXAMPLE_ACTIVITY_NAME))
        .body("childActivityInstances[0].name", equalTo(CHILD_EXAMPLE_ACTIVITY_NAME))
        .body("childActivityInstances[0].processInstanceId", equalTo(CHILD_EXAMPLE_PROCESS_INSTANCE_ID))
        .body("childActivityInstances[0].processDefinitionId", equalTo(CHILD_EXAMPLE_PROCESS_DEFINITION_ID))
        .body("childActivityInstances[0].executionIds", not(empty()))
        .body("childActivityInstances[0].childActivityInstances", empty())
        .body("childActivityInstances[0].childTransitionInstances", empty())
        .body("childTransitionInstances", not(empty()))
        .body("childTransitionInstances[0].id", equalTo(CHILD_EXAMPLE_ACTIVITY_INSTANCE_ID))
        .body("childTransitionInstances[0].parentActivityInstanceId", equalTo(CHILD_EXAMPLE_PARENT_ACTIVITY_INSTANCE_ID))
        .body("childTransitionInstances[0].activityId", equalTo(CHILD_EXAMPLE_ACTIVITY_ID))
        .body("childTransitionInstances[0].activityName", equalTo(CHILD_EXAMPLE_ACTIVITY_NAME))
        .body("childTransitionInstances[0].activityType", equalTo(CHILD_EXAMPLE_ACTIVITY_TYPE))
        .body("childTransitionInstances[0].targetActivityId", equalTo(CHILD_EXAMPLE_ACTIVITY_ID))
        .body("childTransitionInstances[0].processInstanceId", equalTo(CHILD_EXAMPLE_PROCESS_INSTANCE_ID))
        .body("childTransitionInstances[0].processDefinitionId", equalTo(CHILD_EXAMPLE_PROCESS_DEFINITION_ID))
        .body("childTransitionInstances[0].executionId", equalTo(EXAMPLE_EXECUTION_ID))
        .when().get(PROCESS_INSTANCE_ACTIVIY_INSTANCES_URL);

    Assert.assertEquals("Should return right number of properties", 11, response.jsonPath().getMap("").size());
  }

  @Test
  public void testGetActivityInstanceTreeForNonExistingProcessInstance() {
    when(runtimeServiceMock.getActivityInstance(anyString())).thenReturn(null);

    given().pathParam("id", "aNonExistingProcessInstanceId")
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Process instance with id aNonExistingProcessInstanceId does not exist"))
      .when().get(PROCESS_INSTANCE_ACTIVIY_INSTANCES_URL);
  }

  @Test
  public void testGetActivityInstanceTreeWithInternalError() {
    when(runtimeServiceMock.getActivityInstance(anyString())).thenThrow(new ProcessEngineException("expected exception"));

    given().pathParam("id", "aNonExistingProcessInstanceId")
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("expected exception"))
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
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(PROCESS_INSTANCE_ACTIVIY_INSTANCES_URL);
  }

  @Test
  public void testGetVariables() {
    Response response = given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(EXAMPLE_VARIABLE_KEY, notNullValue())
      .body(EXAMPLE_VARIABLE_KEY + ".value", equalTo(EXAMPLE_VARIABLE_VALUE.getValue()))
      .body(EXAMPLE_VARIABLE_KEY + ".type", equalTo(String.class.getSimpleName()))
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);

    Assert.assertEquals("Should return exactly one variable", 1, response.jsonPath().getMap("").size());
  }

  @Test
  public void testGetVariablesWithNullValue() {
    Response response = given().pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID_WITH_NULL_VALUE_AS_VARIABLE)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(EXAMPLE_ANOTHER_VARIABLE_KEY, notNullValue())
      .body(EXAMPLE_ANOTHER_VARIABLE_KEY + ".value", nullValue())
      .body(EXAMPLE_ANOTHER_VARIABLE_KEY + ".type", equalTo("Null"))
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);

    Assert.assertEquals("Should return exactly one variable", 1, response.jsonPath().getMap("").size());
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
      .body("valueInfo.mimeType", equalTo(mimeType))
      .body("valueInfo.filename", equalTo(filename))
      .body("value", nullValue())
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
      .body(is(equalTo("")))
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
      .body(is(equalTo(new String(byteContent))))
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
      .body(is(equalTo(new String(byteContent))))
    .when().get(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);

    String contentType = response.contentType().replaceAll(" ", "");
    assertThat(contentType, is(ContentType.TEXT + ";charset=" + encoding));
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
      .body(is(equalTo(new String(byteContent))))
      .header("Content-Disposition", containsString(filename))
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
      .body(EXAMPLE_VARIABLE_KEY, notNullValue())
      .body(EXAMPLE_VARIABLE_KEY + ".value.property1", equalTo("aPropertyValue"))
      .body(EXAMPLE_VARIABLE_KEY + ".value.property2", equalTo(true))
      .body(EXAMPLE_VARIABLE_KEY + ".type", equalTo(VariableTypeHelper.toExpectedValueTypeName(ValueType.OBJECT)))
      .body(EXAMPLE_VARIABLE_KEY + ".valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ExampleVariableObject.class.getName()))
      .body(EXAMPLE_VARIABLE_KEY + ".valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
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
      .body(variableKey + ".value", equalTo(payload))
      .body(variableKey + ".type", equalTo("Object"))
      .body(variableKey + ".valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body(variableKey + ".valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
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
      .body(variableKey + ".value", equalTo("a serialized value"))
      .body(variableKey + ".type", equalTo("Object"))
      .body(variableKey + ".valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body(variableKey + ".valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);

    // then
    verify(runtimeServiceMock).getVariablesTyped(EXAMPLE_PROCESS_INSTANCE_ID, false);
  }

  @Test
  public void testGetVariablesForNonExistingProcessInstance() {
    when(runtimeServiceMock.getVariablesTyped(anyString(), anyBoolean())).thenThrow(new ProcessEngineException("expected exception"));

    given().pathParam("id", "aNonExistingProcessInstanceId")
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
      .body("message", equalTo("expected exception"))
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
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
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
      .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("ended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_ENDED))
      .body("definitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("businessKey", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY))
      .body("suspended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED))
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
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Process instance with id aNonExistingInstanceId does not exist"))
      .when().get(SINGLE_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testDeleteProcessInstance() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().delete(SINGLE_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).deleteProcessInstance(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, null);
  }

  @Test
  public void testDeleteNonExistingProcessInstance() {
    doThrow(new ProcessEngineException("expected exception")).when(runtimeServiceMock).deleteProcessInstance(anyString(), anyString());

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Process instance with id " + MockProvider.EXAMPLE_PROCESS_INSTANCE_ID + " does not exist"))
      .when().delete(SINGLE_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testDeleteProcessInstanceThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(runtimeServiceMock).deleteProcessInstance(anyString(), anyString());

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .delete(SINGLE_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testNoGivenDeleteReason1() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().delete(SINGLE_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).deleteProcessInstance(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, null);
  }

  @Test
  public void testVariableModification() {
    String variableKey = "aKey";
    int variableValue = 123;

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    messageBodyJson.put("modifications", modifications);

    List<String> deletions = new ArrayList<String>();
    deletions.add("deleteKey");
    messageBodyJson.put("deletions", deletions);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);

    Map<String, Object> expectedModifications = new HashMap<String, Object>();
    expectedModifications.put(variableKey, variableValue);
    verify(runtimeServiceMock).updateVariables(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), argThat(new EqualsMap(expectedModifications)),
        argThat(new EqualsList(deletions)));
  }

  @Test
  public void testVariableModificationWithUnparseableInteger() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Integer";

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for process instance: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Integer.class)))
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationWithUnparseableShort() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Short";

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for process instance: "
      + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Short.class)))
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationWithUnparseableLong() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Long";

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for process instance: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Long.class)))
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationWithUnparseableDouble() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Double";

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for process instance: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Double.class)))
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationWithUnparseableDate() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Date";

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for process instance: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Date.class)))
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationWithNotSupportedType() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for process instance: Unsupported value type 'X'"))
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationForNonExistingProcessInstance() {
    doThrow(new ProcessEngineException("expected exception")).when(runtimeServiceMock).updateVariables(anyString(), anyMapOf(String.class, Object.class), anyCollectionOf(String.class));

    String variableKey = "aKey";
    int variableValue = 123;

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();

    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for process instance " + MockProvider.EXAMPLE_PROCESS_INSTANCE_ID + ": expected exception"))
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
    Map<String, Object> messageBodyJson = new HashMap<String, Object>();
    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    messageBodyJson.put("modifications", modifications);

    String message = "excpected exception";
    doThrow(new AuthorizationException(message)).when(runtimeServiceMock).updateVariables(anyString(), anyMapOf(String.class, Object.class), anyCollectionOf(String.class));

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(message))
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
      .body("value", is(123))
      .body("type", is("Integer"))
      .when().get(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testNonExistingVariable() {
    String variableKey = "aVariableKey";

    when(runtimeServiceMock.getVariableTyped(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), anyBoolean())).thenReturn(null);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", is("process instance variable with name " + variableKey + " does not exist"))
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
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(message))
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
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is("process instance variable with name " + "nonExisting" + " does not exist"))
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
      .body("value", equalTo(payload))
      .body("type", equalTo("Object"))
      .body("valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body("valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
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
      .body("value", equalTo("a serialized value"))
      .body("type", equalTo("Object"))
      .body("valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body("valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
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
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot get process instance variable " + variableKey + ": expected exception"))
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
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put process instance variable aVariableKey: "
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
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put process instance variable aVariableKey: "
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
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put process instance variable aVariableKey: "
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
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put process instance variable aVariableKey: "
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
    SimpleDateFormat pattern = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    String variableKey = "aVariableKey";
    String variableValue = pattern.format(now);
    String type = "Date";

    Date expectedValue = pattern.parse(variableValue);

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
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put process instance variable aVariableKey: "
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
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put process instance variable aVariableKey: Unsupported value type 'X'"))
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
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
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
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);
  }

  @Test
  public void testPutSingleSerializableVariable() throws Exception {

    ArrayList<String> serializable = new ArrayList<String>();
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

    ArrayList<String> serializable = new ArrayList<String>();
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
      .body(containsString("Unrecognized content type for serialized java type: unsupported"))
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
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put process instance variable aVariableKey: Unsupported value type 'aNonExistingType'"))
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
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot put process instance variable " + variableKey + ": expected exception"))
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
    assertThat(captured.getEncoding(), is(encoding));
    assertThat(captured.getFilename(), is(filename));
    assertThat(captured.getMimeType(), is(mimetype));
    assertThat(IoUtil.readInputStream(captured.getValue(), null), is(value));
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
    assertThat(captured.getEncoding(), is(nullValue()));
    assertThat(captured.getFilename(), is(filename));
    assertThat(captured.getMimeType(), is(MediaType.APPLICATION_OCTET_STREAM));
    assertThat(captured.getValue().available(), is(0));
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
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot delete process instance variable " + variableKey + ": expected exception"))
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
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(message))
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

    verify(runtimeServiceMock).activateProcessInstanceById(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
  }

  @Test
  public void testActivateThrowsProcessEngineException() {
    ProcessInstanceSuspensionStateDto dto = new ProcessInstanceSuspensionStateDto();
    dto.setSuspended(false);

    String expectedMessage = "expectedMessage";

    doThrow(new ProcessEngineException(expectedMessage))
      .when(runtimeServiceMock)
      .activateProcessInstanceById(eq(MockProvider.EXAMPLE_NON_EXISTENT_PROCESS_INSTANCE_ID));

    given()
      .pathParam("id", MockProvider.EXAMPLE_NON_EXISTENT_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(dto)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedMessage))
      .when()
        .put(SINGLE_PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testActivateThrowsAuthorizationException() {
    ProcessInstanceSuspensionStateDto dto = new ProcessInstanceSuspensionStateDto();
    dto.setSuspended(false);

    String message = "expectedMessage";

    doThrow(new AuthorizationException(message))
      .when(runtimeServiceMock)
      .activateProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID));

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(dto)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
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

    verify(runtimeServiceMock).suspendProcessInstanceById(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
  }

  @Test
  public void testSuspendThrowsProcessEngineException() {
    ProcessInstanceSuspensionStateDto dto = new ProcessInstanceSuspensionStateDto();
    dto.setSuspended(true);

    String expectedMessage = "expectedMessage";

    doThrow(new ProcessEngineException(expectedMessage))
      .when(runtimeServiceMock)
      .suspendProcessInstanceById(eq(MockProvider.EXAMPLE_NON_EXISTENT_PROCESS_INSTANCE_ID));

    given()
      .pathParam("id", MockProvider.EXAMPLE_NON_EXISTENT_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(dto)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedMessage))
      .when()
        .put(SINGLE_PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testSuspendWithMultipleByParameters() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String message = "Only one of processInstanceId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(SINGLE_PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testSuspendThrowsAuthorizationException() {
    ProcessInstanceSuspensionStateDto dto = new ProcessInstanceSuspensionStateDto();
    dto.setSuspended(true);

    String message = "expectedMessage";

    doThrow(new AuthorizationException(message))
      .when(runtimeServiceMock)
      .suspendProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID));

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(dto)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
    .put(SINGLE_PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionKey() {
    Map<String, Object> params = new HashMap<String, Object>();
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

    verify(runtimeServiceMock).activateProcessInstanceByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionKeyWithException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(runtimeServiceMock)
      .activateProcessInstanceByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionKeyThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String message = "expectedMessage";

    doThrow(new AuthorizationException(message))
      .when(runtimeServiceMock)
      .activateProcessInstanceByProcessDefinitionKey(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY));

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
    .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionKey() {
    Map<String, Object> params = new HashMap<String, Object>();
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

    verify(runtimeServiceMock).suspendProcessInstanceByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionKeyWithException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(runtimeServiceMock)
      .suspendProcessInstanceByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionKeyThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String message = "expectedMessage";

    doThrow(new AuthorizationException(message))
      .when(runtimeServiceMock)
      .suspendProcessInstanceByProcessDefinitionKey(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY));

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
    .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionId() {
    Map<String, Object> params = new HashMap<String, Object>();
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

    verify(runtimeServiceMock).activateProcessInstanceByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionIdWithException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(runtimeServiceMock)
      .activateProcessInstanceByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionIdThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String message = "expectedMessage";

    doThrow(new AuthorizationException(message))
      .when(runtimeServiceMock)
      .activateProcessInstanceByProcessDefinitionId(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionId() {
    Map<String, Object> params = new HashMap<String, Object>();
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

    verify(runtimeServiceMock).suspendProcessInstanceByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionIdWithException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(runtimeServiceMock)
      .suspendProcessInstanceByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionIdThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String message = "expectedMessage";

    doThrow(new AuthorizationException(message))
      .when(runtimeServiceMock)
      .suspendProcessInstanceByProcessDefinitionId(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testActivateProcessInstanceByIdShouldThrowException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    String message = "Either processDefinitionId or processDefinitionKey can be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessInstanceByIdShouldThrowException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    String message = "Either processDefinitionId or processDefinitionKey can be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessInstanceByNothing() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);

    String message = "Either processInstanceId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(PROCESS_INSTANCE_SUSPENDED_URL);
  }

  @Test
  public void testProcessInstanceModification() {
    ProcessInstanceModificationInstantiationBuilder mockModificationBuilder = setUpMockModificationBuilder();
    when(runtimeServiceMock.createProcessInstanceModification(anyString())).thenReturn(mockModificationBuilder);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("skipCustomListeners", true);
    json.put("skipIoMappings", true);

    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

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

    Map<String, Object> json = new HashMap<String, Object>();

    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

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
    Map<String, Object> json = new HashMap<String, Object>();

    // start before: missing activity id
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startBefore().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("'activityId' must be set"))
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_URL);

    // start after: missing ancestor activity instance id
    instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startAfter().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("'activityId' must be set"))
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_URL);

    // start transition: missing ancestor activity instance id
    instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startTransition().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("'transitionId' must be set"))
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_URL);

    // cancel: missing activity id and activity instance id
    instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.cancellation().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("For instruction type 'cancel': exactly one, "
          + "'activityId', 'activityInstanceId', or 'transitionInstanceId', is required"))
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_URL);

    // cancel: both, activity id and activity instance id, set
    instructions = new ArrayList<Map<String, Object>>();
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
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("For instruction type 'cancel': exactly one, "
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

    Map<String, Object> json = new HashMap<String, Object>();

    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

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
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(PROCESS_INSTANCE_MODIFICATION_URL);
  }


  @SuppressWarnings("unchecked")
  protected ProcessInstanceModificationInstantiationBuilder setUpMockModificationBuilder() {
    ProcessInstanceModificationInstantiationBuilder mockModificationBuilder =
        mock(ProcessInstanceModificationInstantiationBuilder.class);

    when(mockModificationBuilder.cancelActivityInstance(anyString())).thenReturn(mockModificationBuilder);
    when(mockModificationBuilder.cancelAllForActivity(anyString())).thenReturn(mockModificationBuilder);
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
}
