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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.rest.dto.runtime.VariableNameDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.helper.ExampleVariableObject;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.runtime.CaseExecutionCommandBuilder;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
*
* @author Roman Smirnov
*
*/
public class AbstractCaseInstanceRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String CASE_INSTANCE_URL = TEST_RESOURCE_ROOT_PATH + "/case-instance";
  protected static final String SINGLE_CASE_INSTANCE_URL = CASE_INSTANCE_URL + "/{id}";

  protected static final String CASE_INSTANCE_COMPLETE_URL = SINGLE_CASE_INSTANCE_URL + "/complete";
  protected static final String CASE_INSTANCE_CLOSE_URL = SINGLE_CASE_INSTANCE_URL + "/close";

  protected static final String CASE_INSTANCE_VARIABLES_URL = SINGLE_CASE_INSTANCE_URL + "/variables";
  protected static final String SINGLE_CASE_INSTANCE_VARIABLE_URL = CASE_INSTANCE_VARIABLES_URL + "/{varId}";

  protected static final Map<String, Object> EXAMPLE_OBJECT_VARIABLES = new HashMap<String, Object>();
  static {
    ExampleVariableObject variableValue = new ExampleVariableObject();
    variableValue.setProperty1("aPropertyValue");
    variableValue.setProperty2(true);

    EXAMPLE_OBJECT_VARIABLES.put(EXAMPLE_VARIABLE_KEY, variableValue);
  }

  private CaseService caseServiceMock;
  private CaseInstanceQuery caseInstanceQueryMock;
  private CaseExecutionCommandBuilder caseExecutionCommandBuilderMock;

  @Before
  public void setUpRuntime() {
    CaseInstance mockCaseInstance = MockProvider.createMockCaseInstance();

    caseServiceMock = mock(CaseService.class);

    when(processEngine.getCaseService()).thenReturn(caseServiceMock);

    caseInstanceQueryMock = mock(CaseInstanceQuery.class);

    when(caseServiceMock.createCaseInstanceQuery()).thenReturn(caseInstanceQueryMock);
    when(caseInstanceQueryMock.caseInstanceId(MockProvider.EXAMPLE_CASE_INSTANCE_ID)).thenReturn(caseInstanceQueryMock);
    when(caseInstanceQueryMock.singleResult()).thenReturn(mockCaseInstance);

    when(caseServiceMock.getVariable(anyString(), anyString())).thenReturn(EXAMPLE_VARIABLE_VALUE);
    when(caseServiceMock.getVariables(anyString())).thenReturn(EXAMPLE_VARIABLES);
    when(caseServiceMock.getVariables(anyString(), Matchers.<Collection<String>>any())).thenReturn(EXAMPLE_VARIABLES);

    caseExecutionCommandBuilderMock = mock(CaseExecutionCommandBuilder.class);

    when(caseServiceMock.withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID)).thenReturn(caseExecutionCommandBuilderMock);

    when(caseExecutionCommandBuilderMock.setVariable(anyString(), any())).thenReturn(caseExecutionCommandBuilderMock);
    when(caseExecutionCommandBuilderMock.setVariables(Matchers.<Map<String, Object>>any())).thenReturn(caseExecutionCommandBuilderMock);

    when(caseExecutionCommandBuilderMock.removeVariable(anyString())).thenReturn(caseExecutionCommandBuilderMock);
    when(caseExecutionCommandBuilderMock.removeVariables(Matchers.<Collection<String>>any())).thenReturn(caseExecutionCommandBuilderMock);
  }

  @Test
  public void testCaseInstanceRetrieval() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_ID))
        .body("businessKey", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY))
        .body("caseDefinitionId", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_CASE_DEFINITION_ID))
        .body("active", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_IS_ACTIVE))
    .when()
      .get(SINGLE_CASE_INSTANCE_URL);

    verify(caseServiceMock).createCaseInstanceQuery();
    verify(caseInstanceQueryMock).caseInstanceId(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseInstanceQueryMock).singleResult();
  }


  @Test
  public void testGetVariables() {
    Response response = given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(EXAMPLE_VARIABLE_KEY, notNullValue())
      .body(EXAMPLE_VARIABLE_KEY + ".value", equalTo(EXAMPLE_VARIABLE_VALUE))
      .body(EXAMPLE_VARIABLE_KEY + ".type", equalTo(String.class.getSimpleName()))
      .when().get(CASE_INSTANCE_VARIABLES_URL);

    Assert.assertEquals("Should return exactly one variable", 1, response.jsonPath().getMap("").size());

    verify(caseServiceMock).getVariables(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
  }

  @Test
  public void testGetVariablesWithNullValue() {
    when(caseServiceMock.getVariables(MockProvider.EXAMPLE_CASE_INSTANCE_ID)).thenReturn(EXAMPLE_VARIABLES_WITH_NULL_VALUE);

    Response response = given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(EXAMPLE_ANOTHER_VARIABLE_KEY, notNullValue())
      .body(EXAMPLE_ANOTHER_VARIABLE_KEY + ".value", nullValue())
      .body(EXAMPLE_ANOTHER_VARIABLE_KEY + ".type", equalTo("Null"))
      .when().get(CASE_INSTANCE_VARIABLES_URL);

    Assert.assertEquals("Should return exactly one variable", 1, response.jsonPath().getMap("").size());

    verify(caseServiceMock).getVariables(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
  }


  @Test
  public void testJavaObjectVariableSerialization() {
    when(caseServiceMock.getVariables(MockProvider.EXAMPLE_CASE_INSTANCE_ID)).thenReturn(EXAMPLE_OBJECT_VARIABLES);

    Response response = given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(EXAMPLE_VARIABLE_KEY, notNullValue())
      .body(EXAMPLE_VARIABLE_KEY + ".value.property1", equalTo("aPropertyValue"))
      .body(EXAMPLE_VARIABLE_KEY + ".value.property2", equalTo(true))
      .body(EXAMPLE_VARIABLE_KEY + ".type", equalTo(ExampleVariableObject.class.getSimpleName()))
      .when().get(CASE_INSTANCE_VARIABLES_URL);

    Assert.assertEquals("Should return exactly one variable", 1, response.jsonPath().getMap("").size());

    verify(caseServiceMock).getVariables(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
  }

  @Test
  public void testGetVariablesForNonExistingCaseInstance() {
    when(caseServiceMock.getVariables(anyString())).thenThrow(new ProcessEngineException("expected exception"));

    given().pathParam("id", "aNonExistingCaseInstanceId")
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
      .body("message", equalTo("expected exception"))
      .when().get(CASE_INSTANCE_VARIABLES_URL);

    verify(caseServiceMock).getVariables("aNonExistingCaseInstanceId");
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

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(CASE_INSTANCE_VARIABLES_URL);

    Map<String, Object> expectedModifications = new HashMap<String, Object>();
    expectedModifications.put(variableKey, variableValue);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariables(eq(expectedModifications));
    verify(caseExecutionCommandBuilderMock).removeVariables(eq(deletions));
    verify(caseExecutionCommandBuilderMock).execute();

  }

  @Test
  public void testVariableModificationWithUnparseableInteger() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Integer";

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for case execution due to number format exception: For input string: \"1abc\""))
      .when().post(CASE_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationWithUnparseableShort() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Short";

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for case execution due to number format exception: For input string: \"1abc\""))
      .when().post(CASE_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationWithUnparseableLong() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Long";

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for case execution due to number format exception: For input string: \"1abc\""))
      .when().post(CASE_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationWithUnparseableDouble() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Double";

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for case execution due to number format exception: For input string: \"1abc\""))
      .when().post(CASE_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationWithUnparseableDate() {
    String variableKey = "aKey";
    String variableValue = "1abc";
    String variableType = "Date";

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for case execution due to parse exception: Unparseable date: \"1abc\""))
      .when().post(CASE_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationWithNotSupportedType() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for case execution: The value type 'X' is not supported."))
      .when().post(CASE_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testVariableModificationForNonExistingCaseInstance() {
    doThrow(new ProcessEngineException("expected exception")).when(caseExecutionCommandBuilderMock).execute();

    String variableKey = "aKey";
    int variableValue = 123;

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();

    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for case execution " + MockProvider.EXAMPLE_CASE_INSTANCE_ID + ": expected exception"))
      .when().post(CASE_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testEmptyVariableModification() {
    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(CASE_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testGetSingleVariable() {
    String variableKey = "aVariableKey";
    int variableValue = 123;

    when(caseServiceMock.getVariable(eq(MockProvider.EXAMPLE_CASE_INSTANCE_ID), eq(variableKey))).thenReturn(variableValue);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("value", is(123))
      .body("type", is("Integer"))
      .when().get(SINGLE_CASE_INSTANCE_VARIABLE_URL);

    verify(caseServiceMock).getVariable(MockProvider.EXAMPLE_CASE_INSTANCE_ID, variableKey);
  }

  @Test
  public void testNonExistingVariable() {
    String variableKey = "aVariableKey";

    when(caseServiceMock.getVariable(eq(MockProvider.EXAMPLE_CASE_INSTANCE_ID), eq(variableKey))).thenReturn(null);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", is("case execution variable with name " + variableKey + " does not exist or is null"))
      .when().get(SINGLE_CASE_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testGetVariableForNonExistingInstance() {
    String variableKey = "aVariableKey";

    when(caseServiceMock.getVariable(eq(MockProvider.EXAMPLE_CASE_INSTANCE_ID), eq(variableKey)))
      .thenThrow(new ProcessEngineException("expected exception"));

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot get case execution variable " + variableKey + ": expected exception"))
      .when().get(SINGLE_CASE_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "aVariableValue";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_CASE_INSTANCE_VARIABLE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariable(variableKey, variableValue);
    verify(caseExecutionCommandBuilderMock).execute();
  }

  @Test
  public void testPutSingleVariableWithTypeString() {
    String variableKey = "aVariableKey";
    String variableValue = "aVariableValue";
    String type = "String";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_CASE_INSTANCE_VARIABLE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariable(variableKey, variableValue);
    verify(caseExecutionCommandBuilderMock).execute();
  }

  @Test
  public void testPutSingleVariableWithTypeInteger() {
    String variableKey = "aVariableKey";
    Integer variableValue = 123;
    String type = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_CASE_INSTANCE_VARIABLE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariable(variableKey, variableValue);
    verify(caseExecutionCommandBuilderMock).execute();
  }

  @Test
  public void testPutSingleVariableWithUnparseableInteger() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put case execution variable aVariableKey due to number format exception: For input string: \"1abc\""))
      .when().put(SINGLE_CASE_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeShort() {
    String variableKey = "aVariableKey";
    Short variableValue = 123;
    String type = "Short";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_CASE_INSTANCE_VARIABLE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariable(variableKey, variableValue);
    verify(caseExecutionCommandBuilderMock).execute();
  }

  @Test
  public void testPutSingleVariableWithUnparseableShort() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Short";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put case execution variable aVariableKey due to number format exception: For input string: \"1abc\""))
      .when().put(SINGLE_CASE_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeLong() {
    String variableKey = "aVariableKey";
    Long variableValue = Long.valueOf(123);
    String type = "Long";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_CASE_INSTANCE_VARIABLE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariable(variableKey, variableValue);
    verify(caseExecutionCommandBuilderMock).execute();
  }

  @Test
  public void testPutSingleVariableWithUnparseableLong() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Long";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put case execution variable aVariableKey due to number format exception: For input string: \"1abc\""))
      .when().put(SINGLE_CASE_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeDouble() {
    String variableKey = "aVariableKey";
    Double variableValue = 123.456;
    String type = "Double";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_CASE_INSTANCE_VARIABLE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariable(variableKey, variableValue);
    verify(caseExecutionCommandBuilderMock).execute();
  }

  @Test
  public void testPutSingleVariableWithUnparseableDouble() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Double";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put case execution variable aVariableKey due to number format exception: For input string: \"1abc\""))
      .when().put(SINGLE_CASE_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeBoolean() {
    String variableKey = "aVariableKey";
    Boolean variableValue = true;
    String type = "Boolean";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_CASE_INSTANCE_VARIABLE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariable(variableKey, variableValue);
    verify(caseExecutionCommandBuilderMock).execute();
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

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_CASE_INSTANCE_VARIABLE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariable(variableKey, expectedValue);
    verify(caseExecutionCommandBuilderMock).execute();
  }

  @Test
  public void testPutSingleVariableWithUnparseableDate() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Date";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put case execution variable aVariableKey due to parse exception: Unparseable date: \"1abc\""))
      .when().put(SINGLE_CASE_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithNotSupportedType() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "X";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put case execution variable aVariableKey: Invalid combination of variable type 'null' and value type 'X'"))
      .when().put(SINGLE_CASE_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleBinaryVariable() throws Exception {
    byte[] bytes = "someContent".getBytes();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .multiPart("data", "unspecified", bytes)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_CASE_INSTANCE_VARIABLE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariable(variableKey, bytes);
    verify(caseExecutionCommandBuilderMock).execute();
  }

  @Test
  public void testPutSingleBinaryVariableWithNoValue() throws Exception {
    byte[] bytes = new byte[0];

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .multiPart("data", "unspecified", bytes)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_CASE_INSTANCE_VARIABLE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariable(variableKey, bytes);
    verify(caseExecutionCommandBuilderMock).execute();
  }

  @Test
  public void testPutSingleLocalBinaryVariableOnDeprecatedPath() throws Exception {
    byte[] bytes = "someContent".getBytes();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .pathParam("varId", variableKey)
      .multiPart("data", "unspecified", bytes)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_CASE_INSTANCE_VARIABLE_URL + "/data");

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariable(variableKey, bytes);
    verify(caseExecutionCommandBuilderMock).execute();
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
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .multiPart("data", jsonBytes, MediaType.APPLICATION_JSON)
      .multiPart("type", typeName, MediaType.TEXT_PLAIN)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_CASE_INSTANCE_VARIABLE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariable(variableKey, serializable);
    verify(caseExecutionCommandBuilderMock).execute();
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
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .multiPart("data", jsonBytes, "unsupported")
      .multiPart("type", typeName, MediaType.TEXT_PLAIN)
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body(containsString("Unrecognized content type for serialized java type: unsupported"))
    .when()
      .post(SINGLE_CASE_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithNoValue() {
    String variableKey = "aVariableKey";

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_CASE_INSTANCE_VARIABLE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariable(eq(variableKey), isNull());
    verify(caseExecutionCommandBuilderMock).execute();
  }

  @Test
  public void testPutVariableForNonExistingInstance() {
    String variableKey = "aVariableKey";
    String variableValue = "aVariableValue";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue);

    doThrow(new ProcessEngineException("expected exception")).when(caseExecutionCommandBuilderMock).execute();

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot put case execution variable " + variableKey + ": expected exception"))
      .when().put(SINGLE_CASE_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testDeleteSingleVariable() {
    String variableKey = "aVariableKey";

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().delete(SINGLE_CASE_INSTANCE_VARIABLE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).removeVariable(variableKey);
    verify(caseExecutionCommandBuilderMock).execute();
  }

  @Test
  public void testDeleteVariableForNonExistingInstance() {
    String variableKey = "aVariableKey";

    doThrow(new ProcessEngineException("expected exception")).when(caseExecutionCommandBuilderMock).execute();

    given().pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot delete case execution variable " + variableKey + ": expected exception"))
      .when().delete(SINGLE_CASE_INSTANCE_VARIABLE_URL);
  }

  @Test
  public void testComplete() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(EMPTY_JSON_OBJECT)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_COMPLETE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).complete();
  }

  @Test
  public void testUnsuccessfulComplete() {
    doThrow(new NotValidException("expected exception")).when(caseExecutionCommandBuilderMock).complete();

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(EMPTY_JSON_OBJECT)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot complete case instance " + MockProvider.EXAMPLE_CASE_INSTANCE_ID + ": expected exception"))
    .when()
      .post(CASE_INSTANCE_COMPLETE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).complete();
  }

  @Test
  public void testCompleteWithSetVariable() {
    String aVariableKey = "aKey";
    int aVariableValue = 123;

    String anotherVariableKey = "anotherKey";
    String anotherVariableValue = "abc";

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    Map<String, Object> variables = VariablesBuilder
        .create()
          .variable(aVariableKey, aVariableValue, "Integer")
          .variable(anotherVariableKey, anotherVariableValue, "String")
          .getVariables();

    variablesJson.put("variables", variables);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_COMPLETE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariable(aVariableKey, aVariableValue);
    verify(caseExecutionCommandBuilderMock).setVariable(anotherVariableKey, anotherVariableValue);
    verify(caseExecutionCommandBuilderMock).complete();
  }

  @Test
  public void testCompleteWithSetVariableLocal() {
    String aVariableKey = "aKey";
    int aVariableValue = 123;

    String anotherVariableKey = "anotherKey";
    String anotherVariableValue = "abc";

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    Map<String, Object> variables = VariablesBuilder
        .create()
          .variable(aVariableKey, aVariableValue, "Integer", true)
          .variable(anotherVariableKey, anotherVariableValue, "String", true)
          .getVariables();

    variablesJson.put("variables", variables);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_COMPLETE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariableLocal(aVariableKey, aVariableValue);
    verify(caseExecutionCommandBuilderMock).setVariableLocal(anotherVariableKey, anotherVariableValue);
    verify(caseExecutionCommandBuilderMock).complete();
  }

  @Test
  public void testCompleteWithSetVariableAndVariableLocal() {
    String aVariableKey = "aKey";
    int aVariableValue = 123;

    String anotherVariableKey = "anotherKey";
    String anotherVariableValue = "abc";

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    Map<String, Object> variables = VariablesBuilder
        .create()
          .variable(aVariableKey, aVariableValue, "Integer")
          .variable(anotherVariableKey, anotherVariableValue, "String", true)
          .getVariables();

    variablesJson.put("variables", variables);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_COMPLETE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariable(aVariableKey, aVariableValue);
    verify(caseExecutionCommandBuilderMock).setVariableLocal(anotherVariableKey, anotherVariableValue);
    verify(caseExecutionCommandBuilderMock).complete();
  }

  @Test
  public void testCompleteWithRemoveVariable() {
    String aVariableKey = "aKey";
    String anotherVariableKey = "anotherKey";

    List<VariableNameDto> variableNames = new ArrayList<VariableNameDto>();

    VariableNameDto firstVariableName = new VariableNameDto(aVariableKey);
    variableNames.add(firstVariableName);
    VariableNameDto secondVariableName = new VariableNameDto(anotherVariableKey);
    variableNames.add(secondVariableName);

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    variablesJson.put("deletions", variableNames);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_COMPLETE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).removeVariable(aVariableKey);
    verify(caseExecutionCommandBuilderMock).removeVariable(anotherVariableKey);
    verify(caseExecutionCommandBuilderMock).complete();
  }

  @Test
  public void testCompleteWithRemoveVariableLocal() {
    String aVariableKey = "aKey";
    String anotherVariableKey = "anotherKey";

    List<VariableNameDto> variableNames = new ArrayList<VariableNameDto>();

    VariableNameDto firstVariableName = new VariableNameDto(aVariableKey, true);
    variableNames.add(firstVariableName);
    VariableNameDto secondVariableName = new VariableNameDto(anotherVariableKey, true);
    variableNames.add(secondVariableName);

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    variablesJson.put("deletions", variableNames);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_COMPLETE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).removeVariableLocal(aVariableKey);
    verify(caseExecutionCommandBuilderMock).removeVariableLocal(anotherVariableKey);
    verify(caseExecutionCommandBuilderMock).complete();
  }

  @Test
  public void testCompleteWithRemoveVariableAndVariableLocal() {
    String aVariableKey = "aKey";
    String anotherVariableKey = "anotherKey";

    List<VariableNameDto> variableNames = new ArrayList<VariableNameDto>();

    VariableNameDto firstVariableName = new VariableNameDto(aVariableKey, true);
    variableNames.add(firstVariableName);
    VariableNameDto secondVariableName = new VariableNameDto(anotherVariableKey);
    variableNames.add(secondVariableName);

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    variablesJson.put("deletions", variableNames);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_COMPLETE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).removeVariableLocal(aVariableKey);
    verify(caseExecutionCommandBuilderMock).removeVariable(anotherVariableKey);
    verify(caseExecutionCommandBuilderMock).complete();
  }

  @Test
  public void testCompleteWithSetVariableAndRemoveVariable() {
    String aVariableKey = "aKey";
    String anotherVariableKey = "anotherKey";
    String anotherVariableValue = "abc";

    Map<String, Object> variables = VariablesBuilder
        .create()
          .variable(anotherVariableKey, anotherVariableValue, "String")
          .getVariables();

    List<VariableNameDto> variableNames = new ArrayList<VariableNameDto>();

    VariableNameDto firstVariableName = new VariableNameDto(aVariableKey);
    variableNames.add(firstVariableName);

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    variablesJson.put("variables", variables);
    variablesJson.put("deletions", variableNames);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_COMPLETE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).removeVariable(aVariableKey);
    verify(caseExecutionCommandBuilderMock).setVariable(anotherVariableKey, anotherVariableValue);
    verify(caseExecutionCommandBuilderMock).complete();
  }

  @Test
  public void testCompleteWithSetVariableAndRemoveVariableLocal() {
    String aVariableKey = "aKey";
    String anotherVariableKey = "anotherKey";
    String anotherVariableValue = "abc";

    Map<String, Object> variables = VariablesBuilder
        .create()
          .variable(anotherVariableKey, anotherVariableValue, "String")
          .getVariables();

    List<VariableNameDto> variableNames = new ArrayList<VariableNameDto>();

    VariableNameDto firstVariableName = new VariableNameDto(aVariableKey, true);
    variableNames.add(firstVariableName);

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    variablesJson.put("variables", variables);
    variablesJson.put("deletions", variableNames);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_COMPLETE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).removeVariableLocal(aVariableKey);
    verify(caseExecutionCommandBuilderMock).setVariable(anotherVariableKey, anotherVariableValue);
    verify(caseExecutionCommandBuilderMock).complete();
  }

  @Test
  public void testCompleteWithSetVariableLocalAndRemoveVariable() {
    String aVariableKey = "aKey";
    String anotherVariableKey = "anotherKey";
    String anotherVariableValue = "abc";

    Map<String, Object> variables = VariablesBuilder
        .create()
          .variable(anotherVariableKey, anotherVariableValue, "String", true)
          .getVariables();

    List<VariableNameDto> variableNames = new ArrayList<VariableNameDto>();

    VariableNameDto firstVariableName = new VariableNameDto(aVariableKey);
    variableNames.add(firstVariableName);

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    variablesJson.put("variables", variables);
    variablesJson.put("deletions", variableNames);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_COMPLETE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).removeVariable(aVariableKey);
    verify(caseExecutionCommandBuilderMock).setVariableLocal(anotherVariableKey, anotherVariableValue);
    verify(caseExecutionCommandBuilderMock).complete();
  }

  @Test
  public void testCompleteWithSetVariableLocalAndRemoveVariableLocal() {
    String aVariableKey = "aKey";
    String anotherVariableKey = "anotherKey";
    String anotherVariableValue = "abc";

    Map<String, Object> variables = VariablesBuilder
        .create()
          .variable(anotherVariableKey, anotherVariableValue, "String", true)
          .getVariables();

    List<VariableNameDto> variableNames = new ArrayList<VariableNameDto>();

    VariableNameDto firstVariableName = new VariableNameDto(aVariableKey, true);
    variableNames.add(firstVariableName);

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    variablesJson.put("variables", variables);
    variablesJson.put("deletions", variableNames);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_COMPLETE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).removeVariableLocal(aVariableKey);
    verify(caseExecutionCommandBuilderMock).setVariableLocal(anotherVariableKey, anotherVariableValue);
    verify(caseExecutionCommandBuilderMock).complete();
  }


  @Test
  public void testClose() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(EMPTY_JSON_OBJECT)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_CLOSE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).close();
  }

  @Test
  public void testUnsuccessfulClose() {
    doThrow(new NotValidException("expected exception")).when(caseExecutionCommandBuilderMock).close();

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(EMPTY_JSON_OBJECT)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot close case instance " + MockProvider.EXAMPLE_CASE_INSTANCE_ID + ": expected exception"))
    .when()
      .post(CASE_INSTANCE_CLOSE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).close();
  }

  @Test
  public void testCloseWithSetVariable() {
    String aVariableKey = "aKey";
    int aVariableValue = 123;

    String anotherVariableKey = "anotherKey";
    String anotherVariableValue = "abc";

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    Map<String, Object> variables = VariablesBuilder
        .create()
          .variable(aVariableKey, aVariableValue, "Integer")
          .variable(anotherVariableKey, anotherVariableValue, "String")
          .getVariables();

    variablesJson.put("variables", variables);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_CLOSE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariable(aVariableKey, aVariableValue);
    verify(caseExecutionCommandBuilderMock).setVariable(anotherVariableKey, anotherVariableValue);
    verify(caseExecutionCommandBuilderMock).close();
  }

  @Test
  public void testCloseWithSetVariableLocal() {
    String aVariableKey = "aKey";
    int aVariableValue = 123;

    String anotherVariableKey = "anotherKey";
    String anotherVariableValue = "abc";

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    Map<String, Object> variables = VariablesBuilder
        .create()
          .variable(aVariableKey, aVariableValue, "Integer", true)
          .variable(anotherVariableKey, anotherVariableValue, "String", true)
          .getVariables();

    variablesJson.put("variables", variables);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_CLOSE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariableLocal(aVariableKey, aVariableValue);
    verify(caseExecutionCommandBuilderMock).setVariableLocal(anotherVariableKey, anotherVariableValue);
    verify(caseExecutionCommandBuilderMock).close();
  }

  @Test
  public void testCloseWithSetVariableAndVariableLocal() {
    String aVariableKey = "aKey";
    int aVariableValue = 123;

    String anotherVariableKey = "anotherKey";
    String anotherVariableValue = "abc";

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    Map<String, Object> variables = VariablesBuilder
        .create()
          .variable(aVariableKey, aVariableValue, "Integer")
          .variable(anotherVariableKey, anotherVariableValue, "String", true)
          .getVariables();

    variablesJson.put("variables", variables);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_CLOSE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).setVariable(aVariableKey, aVariableValue);
    verify(caseExecutionCommandBuilderMock).setVariableLocal(anotherVariableKey, anotherVariableValue);
    verify(caseExecutionCommandBuilderMock).close();
  }

  @Test
  public void testCloseWithRemoveVariable() {
    String aVariableKey = "aKey";
    String anotherVariableKey = "anotherKey";

    List<VariableNameDto> variableNames = new ArrayList<VariableNameDto>();

    VariableNameDto firstVariableName = new VariableNameDto(aVariableKey);
    variableNames.add(firstVariableName);
    VariableNameDto secondVariableName = new VariableNameDto(anotherVariableKey);
    variableNames.add(secondVariableName);

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    variablesJson.put("deletions", variableNames);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_CLOSE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).removeVariable(aVariableKey);
    verify(caseExecutionCommandBuilderMock).removeVariable(anotherVariableKey);
    verify(caseExecutionCommandBuilderMock).close();
  }

  @Test
  public void testCloseWithRemoveVariableLocal() {
    String aVariableKey = "aKey";
    String anotherVariableKey = "anotherKey";

    List<VariableNameDto> variableNames = new ArrayList<VariableNameDto>();

    VariableNameDto firstVariableName = new VariableNameDto(aVariableKey, true);
    variableNames.add(firstVariableName);
    VariableNameDto secondVariableName = new VariableNameDto(anotherVariableKey, true);
    variableNames.add(secondVariableName);

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    variablesJson.put("deletions", variableNames);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_CLOSE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).removeVariableLocal(aVariableKey);
    verify(caseExecutionCommandBuilderMock).removeVariableLocal(anotherVariableKey);
    verify(caseExecutionCommandBuilderMock).close();
  }

  @Test
  public void testCloseWithRemoveVariableAndVariableLocal() {
    String aVariableKey = "aKey";
    String anotherVariableKey = "anotherKey";

    List<VariableNameDto> variableNames = new ArrayList<VariableNameDto>();

    VariableNameDto firstVariableName = new VariableNameDto(aVariableKey, true);
    variableNames.add(firstVariableName);
    VariableNameDto secondVariableName = new VariableNameDto(anotherVariableKey);
    variableNames.add(secondVariableName);

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    variablesJson.put("deletions", variableNames);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_CLOSE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).removeVariableLocal(aVariableKey);
    verify(caseExecutionCommandBuilderMock).removeVariable(anotherVariableKey);
    verify(caseExecutionCommandBuilderMock).close();
  }

  @Test
  public void testCloseWithSetVariableAndRemoveVariable() {
    String aVariableKey = "aKey";
    String anotherVariableKey = "anotherKey";
    String anotherVariableValue = "abc";

    Map<String, Object> variables = VariablesBuilder
        .create()
          .variable(anotherVariableKey, anotherVariableValue, "String")
          .getVariables();

    List<VariableNameDto> variableNames = new ArrayList<VariableNameDto>();

    VariableNameDto firstVariableName = new VariableNameDto(aVariableKey);
    variableNames.add(firstVariableName);

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    variablesJson.put("variables", variables);
    variablesJson.put("deletions", variableNames);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_CLOSE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).removeVariable(aVariableKey);
    verify(caseExecutionCommandBuilderMock).setVariable(anotherVariableKey, anotherVariableValue);
    verify(caseExecutionCommandBuilderMock).close();
  }

  @Test
  public void testCloseWithSetVariableAndRemoveVariableLocal() {
    String aVariableKey = "aKey";
    String anotherVariableKey = "anotherKey";
    String anotherVariableValue = "abc";

    Map<String, Object> variables = VariablesBuilder
        .create()
          .variable(anotherVariableKey, anotherVariableValue, "String")
          .getVariables();

    List<VariableNameDto> variableNames = new ArrayList<VariableNameDto>();

    VariableNameDto firstVariableName = new VariableNameDto(aVariableKey, true);
    variableNames.add(firstVariableName);

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    variablesJson.put("variables", variables);
    variablesJson.put("deletions", variableNames);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_CLOSE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).removeVariableLocal(aVariableKey);
    verify(caseExecutionCommandBuilderMock).setVariable(anotherVariableKey, anotherVariableValue);
    verify(caseExecutionCommandBuilderMock).close();
  }

  @Test
  public void testCloseWithSetVariableLocalAndRemoveVariable() {
    String aVariableKey = "aKey";
    String anotherVariableKey = "anotherKey";
    String anotherVariableValue = "abc";

    Map<String, Object> variables = VariablesBuilder
        .create()
          .variable(anotherVariableKey, anotherVariableValue, "String", true)
          .getVariables();

    List<VariableNameDto> variableNames = new ArrayList<VariableNameDto>();

    VariableNameDto firstVariableName = new VariableNameDto(aVariableKey);
    variableNames.add(firstVariableName);

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    variablesJson.put("variables", variables);
    variablesJson.put("deletions", variableNames);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_CLOSE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).removeVariable(aVariableKey);
    verify(caseExecutionCommandBuilderMock).setVariableLocal(anotherVariableKey, anotherVariableValue);
    verify(caseExecutionCommandBuilderMock).close();
  }

  @Test
  public void testCloseWithSetVariableLocalAndRemoveVariableLocal() {
    String aVariableKey = "aKey";
    String anotherVariableKey = "anotherKey";
    String anotherVariableValue = "abc";

    Map<String, Object> variables = VariablesBuilder
        .create()
          .variable(anotherVariableKey, anotherVariableValue, "String", true)
          .getVariables();

    List<VariableNameDto> variableNames = new ArrayList<VariableNameDto>();

    VariableNameDto firstVariableName = new VariableNameDto(aVariableKey, true);
    variableNames.add(firstVariableName);

    Map<String, Object> variablesJson = new HashMap<String, Object>();

    variablesJson.put("variables", variables);
    variablesJson.put("deletions", variableNames);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .contentType(ContentType.JSON)
      .body(variablesJson)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(CASE_INSTANCE_CLOSE_URL);

    verify(caseServiceMock).withCaseExecution(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    verify(caseExecutionCommandBuilderMock).removeVariableLocal(aVariableKey);
    verify(caseExecutionCommandBuilderMock).setVariableLocal(anotherVariableKey, anotherVariableValue);
    verify(caseExecutionCommandBuilderMock).close();
  }

}
