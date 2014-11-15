package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.impl.core.variable.type.ObjectTypeImpl;
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
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        .body("name", equalTo(EXAMPLE_ACTIVITY_NAME))
        .body("childActivityInstances", not(empty()))
        .body("childActivityInstances[0].id", equalTo(CHILD_EXAMPLE_ACTIVITY_INSTANCE_ID))
        .body("childActivityInstances[0].parentActivityInstanceId", equalTo(CHILD_EXAMPLE_PARENT_ACTIVITY_INSTANCE_ID))
        .body("childActivityInstances[0].activityId", equalTo(CHILD_EXAMPLE_ACTIVITY_ID))
        .body("childActivityInstances[0].activityType", equalTo(CHILD_EXAMPLE_ACTIVITY_TYPE))
        .body("childActivityInstances[0].processInstanceId", equalTo(CHILD_EXAMPLE_PROCESS_INSTANCE_ID))
        .body("childActivityInstances[0].processDefinitionId", equalTo(CHILD_EXAMPLE_PROCESS_DEFINITION_ID))
        .body("childActivityInstances[0].executionIds", not(empty()))
        .body("childActivityInstances[0].childActivityInstances", empty())
        .body("childActivityInstances[0].childTransitionInstances", empty())
        .body("childActivityInstances[0].name", equalTo(CHILD_EXAMPLE_ACTIVITY_NAME))
        .body("childTransitionInstances", not(empty()))
        .body("childTransitionInstances[0].id", equalTo(CHILD_EXAMPLE_ACTIVITY_INSTANCE_ID))
        .body("childTransitionInstances[0].parentActivityInstanceId", equalTo(CHILD_EXAMPLE_PARENT_ACTIVITY_INSTANCE_ID))
        .body("childTransitionInstances[0].targetActivityId", equalTo(CHILD_EXAMPLE_ACTIVITY_ID))
        .body("childTransitionInstances[0].processInstanceId", equalTo(CHILD_EXAMPLE_PROCESS_INSTANCE_ID))
        .body("childTransitionInstances[0].processDefinitionId", equalTo(CHILD_EXAMPLE_PROCESS_DEFINITION_ID))
        .body("childTransitionInstances[0].executionId", equalTo(EXAMPLE_EXECUTION_ID))
        .when().get(PROCESS_INSTANCE_ACTIVIY_INSTANCES_URL);

    Assert.assertEquals("Should return right number of properties", 10, response.jsonPath().getMap("").size());
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
  public void testJavaObjectVariableSerialization() {
    Response response = given().pathParam("id", MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(EXAMPLE_VARIABLE_KEY, notNullValue())
      .body(EXAMPLE_VARIABLE_KEY + ".value.property1", equalTo("aPropertyValue"))
      .body(EXAMPLE_VARIABLE_KEY + ".value.property2", equalTo(true))
      .body(EXAMPLE_VARIABLE_KEY + ".type", equalTo(VariableTypeHelper.toExpectedValueTypeName(ValueType.OBJECT)))
      .body(EXAMPLE_VARIABLE_KEY + ".valueInfo." + ObjectTypeImpl.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ExampleVariableObject.class.getName()))
      .body(EXAMPLE_VARIABLE_KEY + ".valueInfo." + ObjectTypeImpl.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
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
      .body(variableKey + ".valueInfo." + ObjectTypeImpl.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body(variableKey + ".valueInfo." + ObjectTypeImpl.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
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
      .body(variableKey + ".valueInfo." + ObjectTypeImpl.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body(variableKey + ".valueInfo." + ObjectTypeImpl.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
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

    when(runtimeServiceMock.getVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey))).thenReturn(null);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", is("process instance variable with name " + variableKey + " does not exist"))
      .when().get(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
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
      .body("valueInfo." + ObjectTypeImpl.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body("valueInfo." + ObjectTypeImpl.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
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
      .body("valueInfo." + ObjectTypeImpl.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body("valueInfo." + ObjectTypeImpl.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
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
  public void testPutSingleBinaryVariable() throws Exception {
    byte[] bytes = "someContent".getBytes();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .multiPart("data", "unspecified", bytes)
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
      .multiPart("data", "unspecified", bytes)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_PROCESS_INSTANCE_BINARY_VARIABLE_URL);

    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.bytesValue(bytes)));
  }

  @Test
  public void testPutSingleSerializableVariable() throws Exception {

    ArrayList<String> serializable = new ArrayList<String>();
    serializable.add("foo");

    ObjectMapper mapper = new ObjectMapper();
    String jsonBytes = mapper.writeValueAsString(serializable);
    String typeName = TypeFactory.type(serializable.getClass()).toCanonical();

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
    String typeName = TypeFactory.type(serializable.getClass()).toCanonical();

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
}
