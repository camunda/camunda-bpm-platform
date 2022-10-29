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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.anyString;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.camunda.bpm.engine.rest.helper.ErrorMessageHelper;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.VariableTypeHelper;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultWithVariables;
import org.camunda.bpm.engine.variable.type.ValueType;

import static org.mockito.Mockito.when;
import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MessageRestServiceTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String MESSAGE_URL = TEST_RESOURCE_ROOT_PATH +  MessageRestService.PATH;

  private RuntimeService runtimeServiceMock;
  private MessageCorrelationBuilder messageCorrelationBuilderMock;
  private MessageCorrelationResult executionResult;
  private MessageCorrelationResult procInstanceResult;
  private List<MessageCorrelationResult> executionResultList;
  private List<MessageCorrelationResult> procInstanceResultList;
  private List<MessageCorrelationResult> mixedResultList;

  private MessageCorrelationResultWithVariables executionResultWithVariables;
  private List<MessageCorrelationResultWithVariables> execResultWithVariablesList;

  @Before
  public void setupMocks() {
    runtimeServiceMock = mock(RuntimeService.class);
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);

    messageCorrelationBuilderMock = mock(MessageCorrelationBuilder.class);

    when(runtimeServiceMock.createMessageCorrelation(anyString())).thenReturn(messageCorrelationBuilderMock);
    when(messageCorrelationBuilderMock.processInstanceId(anyString())).thenReturn(messageCorrelationBuilderMock);
    when(messageCorrelationBuilderMock.processInstanceBusinessKey(anyString())).thenReturn(messageCorrelationBuilderMock);
    when(messageCorrelationBuilderMock.processInstanceVariableEquals(anyString(), any())).thenReturn(messageCorrelationBuilderMock);
    when(messageCorrelationBuilderMock.setVariables(Mockito.any())).thenReturn(messageCorrelationBuilderMock);
    when(messageCorrelationBuilderMock.setVariable(anyString(), any())).thenReturn(messageCorrelationBuilderMock);

    executionResult = MockProvider.createMessageCorrelationResult(MessageCorrelationResultType.Execution);
    procInstanceResult = MockProvider.createMessageCorrelationResult(MessageCorrelationResultType.ProcessDefinition);
    executionResultList = MockProvider.createMessageCorrelationResultList(MessageCorrelationResultType.Execution);
    procInstanceResultList = MockProvider.createMessageCorrelationResultList(MessageCorrelationResultType.ProcessDefinition);
    mixedResultList = new ArrayList<>(executionResultList);
    mixedResultList.addAll(procInstanceResultList);

    executionResultWithVariables = MockProvider.createMessageCorrelationResultWithVariables(MessageCorrelationResultType.Execution);
    execResultWithVariablesList = new ArrayList<>();
    execResultWithVariablesList.add(executionResultWithVariables);

  }

  @Test
  public void testFullMessageCorrelation() {
    String messageName = "aMessageName";
    String businessKey = "aBusinessKey";
    Map<String, Object> variables = VariablesBuilder.create().variable("aKey", "aValue").getVariables();
    Map<String, Object> variablesLocal = VariablesBuilder.create().variable("aKeyLocal", "aValueLocal").getVariables();

    Map<String, Object> correlationKeys = VariablesBuilder.create()
        .variable("aKey", "aValue")
        .variable("anotherKey", 1)
        .variable("aThirdKey", true).getVariables();

    Map<String, Object> localCorrelationKeys = VariablesBuilder.create()
        .variable("aLocalKey", "aValue")
        .variable("anotherLocalKey", 1)
        .variable("aThirdLocalKey", false).getVariables();

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("correlationKeys", correlationKeys);
    messageParameters.put("localCorrelationKeys", localCorrelationKeys);
    messageParameters.put("processVariables", variables);
    messageParameters.put("processVariablesLocal", variablesLocal);
    messageParameters.put("businessKey", businessKey);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_URL);

    Map<String, Object> expectedCorrelationKeys = new HashMap<>();
    expectedCorrelationKeys.put("aKey", "aValue");
    expectedCorrelationKeys.put("anotherKey", 1);
    expectedCorrelationKeys.put("aThirdKey", true);

    Map<String, Object> expectedLocalCorrelationKeys = new HashMap<>();
    expectedLocalCorrelationKeys.put("aLocalKey", "aValue");
    expectedLocalCorrelationKeys.put("anotherLocalKey", 1);
    expectedLocalCorrelationKeys.put("aThirdLocalKey", false);

    Map<String, Object> expectedVariables = new HashMap<>();
    expectedVariables.put("aKey", "aValue");
    Map<String, Object> expectedVariablesLocal = new HashMap<>();
    expectedVariablesLocal.put("aKeyLocal", "aValueLocal");

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).processInstanceBusinessKey(eq(businessKey));
    verify(messageCorrelationBuilderMock).setVariables(argThat(new EqualsMap(expectedVariables)));
    verify(messageCorrelationBuilderMock).setVariablesLocal(argThat(new EqualsMap(expectedVariablesLocal)));

    for (Entry<String, Object> expectedKey : expectedCorrelationKeys.entrySet()) {
      String name = expectedKey.getKey();
      Object value = expectedKey.getValue();
      verify(messageCorrelationBuilderMock).processInstanceVariableEquals(name, value);
    }

    for (Entry<String, Object> expectedLocalKey : expectedLocalCorrelationKeys.entrySet()) {
      String name = expectedLocalKey.getKey();
      Object value = expectedLocalKey.getValue();
      verify(messageCorrelationBuilderMock).localVariableEquals(name, value);
    }

    verify(messageCorrelationBuilderMock).correlateWithResult();

//    verify(runtimeServiceMock).correlateMessage(eq(messageName), eq(businessKey),
//        argThat(new EqualsMap(expectedCorrelationKeys)), argThat(new EqualsMap(expectedVariables)));
  }

  @Test
  public void testFullMessageCorrelationWithExecutionResult() {
    //given
    when(messageCorrelationBuilderMock.correlateWithResult()).thenReturn(executionResult);

    String messageName = "aMessageName";
    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("resultEnabled", true);

    //when
    Response response = given().contentType(POST_JSON_CONTENT_TYPE)
           .body(messageParameters)
    .then().expect()
           .contentType(ContentType.JSON)
           .statusCode(Status.OK.getStatusCode())
    .when().post(MESSAGE_URL);

    //then
    assertNotNull(response);
    String content = response.asString();
    assertTrue(!content.isEmpty());
    checkExecutionResult(content, 0);

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).correlateWithResult();
  }

  protected void checkExecutionResult(String content, int idx) {
    //resultType should be execution
    String resultType = from(content).get("[" + idx + "].resultType").toString();
    assertEquals(MessageCorrelationResultType.Execution.name(), resultType);
    //execution should be filled and process instance should be null
    assertEquals(MockProvider.EXAMPLE_EXECUTION_ID, from(content).get("[" + idx + "].execution.id"));
    assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, from(content).get("[" + idx + "].execution.processInstanceId"));
    assertNull(from(content).get("[" + idx + "].processInstance"));
  }

  @Test
  public void testFullMessageCorrelationWithProcessDefinitionResult() {
    //given
    when(messageCorrelationBuilderMock.correlateWithResult()).thenReturn(procInstanceResult);

    String messageName = "aMessageName";
    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("resultEnabled", true);

    //when
    Response response = given().contentType(POST_JSON_CONTENT_TYPE)
           .body(messageParameters)
    .then().expect()
           .contentType(ContentType.JSON)
           .statusCode(Status.OK.getStatusCode())
    .when().post(MESSAGE_URL);

    //then
    assertNotNull(response);
    String content = response.asString();
    assertTrue(!content.isEmpty());
    checkProcessInstanceResult(content, 0);

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).correlateWithResult();
  }

  protected void checkProcessInstanceResult(String content, int idx) {
    //resultType should be set to process definition
    String resultType = from(content).get("[" + idx + "].resultType");
    Assert.assertEquals(MessageCorrelationResultType.ProcessDefinition.name(), resultType);

    //process instance should be filled and execution should be null
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, from(content).get("[" + idx + "].processInstance.id"));
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, from(content).get("[" + idx + "].processInstance.definitionId"));
    Assert.assertNull(from(content).get("[" + idx + "].execution"));
  }

  @Test
  public void testFullMessageCorrelationAll() {
    String messageName = "aMessageName";
    String businessKey = "aBusinessKey";
    Map<String, Object> variables = VariablesBuilder.create().variable("aKey", "aValue").getVariables();
    Map<String, Object> variablesLocal = VariablesBuilder.create().variable("aKeyLocal", "aValueLocal").getVariables();

    Map<String, Object> correlationKeys = VariablesBuilder.create()
        .variable("aKey", "aValue")
        .variable("anotherKey", 1)
        .variable("aThirdKey", true).getVariables();

    Map<String, Object> localCorrelationKeys = VariablesBuilder.create()
        .variable("aLocalKey", "aValue")
        .variable("anotherLocalKey", 1)
        .variable("aThirdLocalKey", false).getVariables();

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("correlationKeys", correlationKeys);
    messageParameters.put("localCorrelationKeys", localCorrelationKeys);
    messageParameters.put("processVariables", variables);
    messageParameters.put("processVariablesLocal", variablesLocal);
    messageParameters.put("businessKey", businessKey);
    messageParameters.put("all", true);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_URL);

    Map<String, Object> expectedCorrelationKeys = new HashMap<>();
    expectedCorrelationKeys.put("aKey", "aValue");
    expectedCorrelationKeys.put("anotherKey", 1);
    expectedCorrelationKeys.put("aThirdKey", true);

    Map<String, Object> expectedLocalCorrelationKeys = new HashMap<>();
    expectedLocalCorrelationKeys.put("aLocalKey", "aValue");
    expectedLocalCorrelationKeys.put("anotherLocalKey", 1);
    expectedLocalCorrelationKeys.put("aThirdLocalKey", false);

    Map<String, Object> expectedVariables = new HashMap<>();
    expectedVariables.put("aKey", "aValue");
    Map<String, Object> expectedVariablesLocal = new HashMap<>();
    expectedVariablesLocal.put("aKeyLocal", "aValueLocal");

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).processInstanceBusinessKey(eq(businessKey));
    verify(messageCorrelationBuilderMock).setVariables(argThat(new EqualsMap(expectedVariables)));
    verify(messageCorrelationBuilderMock).setVariablesLocal(argThat(new EqualsMap(expectedVariablesLocal)));

    for (Entry<String, Object> expectedKey : expectedCorrelationKeys.entrySet()) {
      String name = expectedKey.getKey();
      Object value = expectedKey.getValue();
      verify(messageCorrelationBuilderMock).processInstanceVariableEquals(name, value);
    }

    for (Entry<String, Object> expectedLocalKey : expectedLocalCorrelationKeys.entrySet()) {
      String name = expectedLocalKey.getKey();
      Object value = expectedLocalKey.getValue();
      verify(messageCorrelationBuilderMock).localVariableEquals(name, value);
    }

    verify(messageCorrelationBuilderMock).correlateAllWithResult();

  }

  @Test
  public void testFullMessageCorrelationAllWithExecutionResult() {
    //given
    when(messageCorrelationBuilderMock.correlateAllWithResult()).thenReturn(executionResultList);

    String messageName = "aMessageName";
    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("all", true);
    messageParameters.put("resultEnabled", true);

    //when
    Response response = given().contentType(POST_JSON_CONTENT_TYPE)
           .body(messageParameters)
    .then().expect()
           .contentType(ContentType.JSON)
           .statusCode(Status.OK.getStatusCode())
    .when().post(MESSAGE_URL);

    //then
    assertNotNull(response);
    String content = response.asString();
    assertTrue(!content.isEmpty());

    List<HashMap> results = from(content).getList("");
    assertEquals(2, results.size());
    for (int i = 0; i < 2; i++) {
      checkExecutionResult(content, i);
    }

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).correlateAllWithResult();
  }

 @Test
  public void testFullMessageCorrelationAllWithProcessInstanceResult() {
    //given
    when(messageCorrelationBuilderMock.correlateAllWithResult()).thenReturn(procInstanceResultList);

    String messageName = "aMessageName";
    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("all", true);
    messageParameters.put("resultEnabled", true);

    //when
    Response response = given().contentType(POST_JSON_CONTENT_TYPE)
           .body(messageParameters)
    .then().expect()
           .contentType(ContentType.JSON)
           .statusCode(Status.OK.getStatusCode())
    .when().post(MESSAGE_URL);

    //then
    assertNotNull(response);
    String content = response.asString();
    assertTrue(!content.isEmpty());

    List<HashMap> results = from(content).getList("");
    assertEquals(2, results.size());
    for (int i = 0; i < 2; i++) {
      checkProcessInstanceResult(content, i);
    }

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).correlateAllWithResult();
  }

  @Test
  public void testFullMessageCorrelationAllWithMixedResult() {
    //given
    when(messageCorrelationBuilderMock.correlateAllWithResult()).thenReturn(mixedResultList);

    String messageName = "aMessageName";
    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("all", true);
    messageParameters.put("resultEnabled", true);

    //when
    Response response = given().contentType(POST_JSON_CONTENT_TYPE)
           .body(messageParameters)
    .then().expect()
           .contentType(ContentType.JSON)
           .statusCode(Status.OK.getStatusCode())
    .when().post(MESSAGE_URL);

    //then
    assertNotNull(response);
    String content = response.asString();
    assertTrue(!content.isEmpty());

    List<HashMap> results = from(content).getList("");
    assertEquals(4, results.size());
    for (int i = 0; i < 2; i++) {
      String resultType = from(content).get("[" + i + "].resultType");
      assertNotNull(resultType);
      if (resultType.equals(MessageCorrelationResultType.Execution.name())) {
        checkExecutionResult(content, i);
      } else {
        checkProcessInstanceResult(content, i);
      }
    }

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).correlateAllWithResult();
  }


  @Test
  public void testFullMessageCorrelationAllWithNoResult() {
    //given
    when(messageCorrelationBuilderMock.correlateAllWithResult()).thenReturn(mixedResultList);

    String messageName = "aMessageName";
    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("all", true);

    //when
    Response response = given().contentType(POST_JSON_CONTENT_TYPE)
           .body(messageParameters)
    .then().expect()
           .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().post(MESSAGE_URL);

    //then
    assertNotNull(response);
    String content = response.asString();
    assertTrue(content.isEmpty());

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).correlateAllWithResult();
  }

  @Test
  public void testMessageNameOnlyCorrelation() {
    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_URL);

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).correlateWithResult();
    verifyNoMoreInteractions(messageCorrelationBuilderMock);
  }

  @Test
  public void testMessageNameAndBusinessKeyCorrelation() {
    String messageName = "aMessageName";
    String businessKey = "aBusinessKey";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("businessKey", businessKey);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_URL);

//    verify(runtimeServiceMock).correlateMessage(eq(messageName), eq(businessKey),
//        argThat(new EqualsMap(null)), argThat(new EqualsMap(null)));

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).processInstanceBusinessKey(eq(businessKey));
    verify(messageCorrelationBuilderMock).correlateWithResult();
    verifyNoMoreInteractions(messageCorrelationBuilderMock);

  }

  @Test
  public void testMessageNameAndBusinessKeyCorrelationAll() {
    String messageName = "aMessageName";
    String businessKey = "aBusinessKey";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("businessKey", businessKey);
    messageParameters.put("all", true);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_URL);

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).processInstanceBusinessKey(eq(businessKey));
    verify(messageCorrelationBuilderMock).correlateAllWithResult();
    verifyNoMoreInteractions(messageCorrelationBuilderMock);

  }

  @Test
  public void testMismatchingCorrelation() {
    String messageName = "aMessage";

    doThrow(new MismatchingMessageCorrelationException(messageName, "Expected exception: cannot correlate"))
      .when(messageCorrelationBuilderMock).correlateWithResult();

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Expected exception: cannot correlate"))
      .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingInstantiation() {
    String messageName = "aMessage";

    // thrown, if instantiation of the process or signalling the instance fails
    doThrow(new ProcessEngineException("Expected exception"))
      .when(messageCorrelationBuilderMock).correlateWithResult();

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
      .body("message", equalTo("Expected exception"))
      .when().post(MESSAGE_URL);
  }

  @Test
  public void testNoMessageNameCorrelation() {
    given().contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("No message name supplied"))
      .when().post(MESSAGE_URL);
  }

  @Test
  public void testMessageCorrelationWithTenantId() {
    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("tenantId", MockProvider.EXAMPLE_TENANT_ID);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_URL);

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).tenantId(MockProvider.EXAMPLE_TENANT_ID);
    verify(messageCorrelationBuilderMock).correlateWithResult();
    verifyNoMoreInteractions(messageCorrelationBuilderMock);
  }

  @Test
  public void testMessageCorrelationWithoutTenantId() {
    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("withoutTenantId", true);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_URL);

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).withoutTenantId();
    verify(messageCorrelationBuilderMock).correlateWithResult();
    verifyNoMoreInteractions(messageCorrelationBuilderMock);
  }

  @Test
  public void testFailingInvalidTenantParameters() {
    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("tenantId", MockProvider.EXAMPLE_TENANT_ID);
    messageParameters.put("withoutTenantId", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(messageParameters)
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Parameter 'tenantId' cannot be used together with parameter 'withoutTenantId'."))
    .when()
      .post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableIntegerInCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("correlationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver message: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Integer.class)))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableIntegerInLocalCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("localCorrelationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
        .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Cannot deliver message: "
            + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Integer.class)))
        .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableShortInCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Short";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("correlationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver message: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Short.class)))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableShortInLocalCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Short";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("localCorrelationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
        .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Cannot deliver message: "
            + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Short.class)))
        .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableLongInCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Long";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("correlationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver message: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Long.class)))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableLongInLocalCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Long";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("localCorrelationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
        .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Cannot deliver message: "
            + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Long.class)))
        .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableDoubleInCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Double";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("correlationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver message: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Double.class)))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableDoubleInLocalCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Double";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("localCorrelationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
        .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Cannot deliver message: "
            + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Double.class)))
        .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableDateInCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Date";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("correlationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver message: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Date.class)))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableDateInLocalCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Date";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("localCorrelationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
        .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Cannot deliver message: "
            + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Date.class)))
        .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToNotSupportedTypeInCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("correlationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver message: Unsupported value type 'X'"))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToNotSupportedTypeInLocalCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("localCorrelationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
        .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Cannot deliver message: Unsupported value type 'X'"))
        .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableIntegerInProcessVariables() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("processVariables", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver message: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Integer.class)))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableShortInProcessVariables() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Short";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("processVariables", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver message: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Short.class)))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableLongInProcessVariables() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Long";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("processVariables", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver message: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Long.class)))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableDoubleInProcessVariables() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Double";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("processVariables", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver message: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Double.class)))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableDateInProcessVariables() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Date";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("processVariables", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver message: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Date.class)))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToNotSupportedTypeInProcessVariables() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("processVariables", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver message: Unsupported value type 'X'"))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableIntegerInProcessVariablesLocal() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Integer";

    Map<String, Object> variableLocalJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("processVariablesLocal", variableLocalJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver message: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Integer.class)))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToNotSupportedTypeInProcessVariablesLocal() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";

    Map<String, Object> variableLocalJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("processVariablesLocal", variableLocalJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver message: Unsupported value type 'X'"))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableDateInProcessVariablesLocal() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Date";

    Map<String, Object> variableLocalJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("processVariablesLocal", variableLocalJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver message: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Date.class)))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testCorrelateThrowsAuthorizationException() {
    String messageName = "aMessageName";
    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(messageCorrelationBuilderMock).correlateWithResult();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(messageParameters)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(MESSAGE_URL);
  }

  @Test
  public void testcorrelateAllThrowsAuthorizationException() {
    String messageName = "aMessageName";
    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("all", true);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(messageCorrelationBuilderMock).correlateAllWithResult();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(messageParameters)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(MESSAGE_URL);
  }

  @Test
  public void testMessageCorrelationWithProcessInstanceId() {
    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
       .body(messageParameters)
    .then()
      .expect().statusCode(Status.NO_CONTENT.getStatusCode())
    .when().post(MESSAGE_URL);

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).processInstanceId(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID));

    verify(messageCorrelationBuilderMock).correlateWithResult();
  }

  @Test
  public void testMessageCorrelationWithoutBusinessKey() {
    when(messageCorrelationBuilderMock.processInstanceBusinessKey(null))
      .thenThrow(new NullValueException());

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
       .body(messageParameters)
    .then()
      .expect().statusCode(Status.NO_CONTENT.getStatusCode())
    .when().post(MESSAGE_URL);

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));

    verify(messageCorrelationBuilderMock, Mockito.never()).processInstanceBusinessKey(anyString());
    verify(messageCorrelationBuilderMock).correlateWithResult();
    verifyNoMoreInteractions(messageCorrelationBuilderMock);
  }

  @Test
  public void testCorrelationWithVariablesInResult() {
    // given
    when(messageCorrelationBuilderMock.correlateWithResultAndVariables(false)).thenReturn(executionResultWithVariables);

    String messageName = "aMessageName";
    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("resultEnabled", true);
    messageParameters.put("variablesInResultEnabled", true);

    // when
    Response response = given().contentType(POST_JSON_CONTENT_TYPE)
           .body(messageParameters)
    .then().expect()
           .contentType(ContentType.JSON)
           .statusCode(Status.OK.getStatusCode())
    .when().post(MESSAGE_URL);

    // then
    assertNotNull(response);
    String content = response.asString();
    assertTrue(!content.isEmpty());
    checkVariablesInResult(content, 0);

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).correlateWithResultAndVariables(false);
  }

  @Test
  public void testCorrelationAllWithVariablesInResult() {
    // given
    when(messageCorrelationBuilderMock.correlateAllWithResultAndVariables(false)).thenReturn(execResultWithVariablesList);

    String messageName = "aMessageName";
    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("all", true);
    messageParameters.put("resultEnabled", true);
    messageParameters.put("variablesInResultEnabled", true);

    // when
    Response response = given().contentType(POST_JSON_CONTENT_TYPE)
           .body(messageParameters)
    .then().expect()
           .contentType(ContentType.JSON)
           .statusCode(Status.OK.getStatusCode())
    .when().post(MESSAGE_URL);

    // then
    assertNotNull(response);
    String content = response.asString();
    assertTrue(!content.isEmpty());

    List<HashMap<Object, Object>> results = from(content).getList("");
    assertEquals(1, results.size());
    checkVariablesInResult(content, 0);

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).correlateAllWithResultAndVariables(false);
  }

  @Test
  public void testFailingCorrelationWithVariablesInResultDueToDisabledResult() {
    // given
    String messageName = "aMessageName";
    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("resultEnabled", false);
    messageParameters.put("variablesInResultEnabled", true);

    // when/
    given().contentType(POST_JSON_CONTENT_TYPE)
           .body(messageParameters)
    .then().expect()
           .contentType(ContentType.JSON)
           .statusCode(Status.BAD_REQUEST.getStatusCode())
           .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
           .body("message", equalTo("Parameter 'variablesInResultEnabled' cannot be used without 'resultEnabled' set to true."))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void shouldReturnErrorOnMessageCorrelation() {
    // given
    doThrow(new ProcessEngineException("foo", 123))
        .when(messageCorrelationBuilderMock).correlateWithResult();

    String messageName = "aMessageName";
    Map<String, Object> messageParameters = new HashMap<>();
    messageParameters.put("messageName", messageName);

    // when/
    given().contentType(POST_JSON_CONTENT_TYPE)
           .body(messageParameters)
    .then().expect()
           .contentType(ContentType.JSON)
           .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
           .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
           .body("message", equalTo("foo"))
           .body("code", equalTo(123))
    .when().post(MESSAGE_URL);
  }

  protected void checkVariablesInResult(String content, int idx) {
    List<String> variableNames = java.util.Arrays.asList(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME, MockProvider.EXAMPLE_DESERIALIZED_VARIABLE_INSTANCE_NAME);

    for (String variableName : variableNames) {
      String variablePath = "[" + idx + "].variables." + variableName;
      assertEquals(MockProvider.FORMAT_APPLICATION_JSON, from(content).getMap(variablePath + ".valueInfo").get("serializationDataFormat"));
      assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_SERIALIZED_VALUE, from(content).get(variablePath + ".value"));
      assertEquals(VariableTypeHelper.toExpectedValueTypeName(ValueType.OBJECT), from(content).get(variablePath + ".type"));
    }

    assertEquals(ArrayList.class.getName(),
        from(content).getMap("[" + idx + "].variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME + ".valueInfo").get("objectTypeName"));
    assertEquals(Object.class.getName(),
        from(content).getMap("[" + idx + "].variables." + MockProvider.EXAMPLE_DESERIALIZED_VARIABLE_INSTANCE_NAME + ".valueInfo").get("objectTypeName"));
  }

}
