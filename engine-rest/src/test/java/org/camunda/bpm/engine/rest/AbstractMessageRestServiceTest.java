package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;

public abstract class AbstractMessageRestServiceTest extends AbstractRestServiceTest {

  protected static final String MESSAGE_URL = TEST_RESOURCE_ROOT_PATH + "/message";

  private RuntimeService runtimeServiceMock;

  @Before
  public void setupMocks() {
    runtimeServiceMock = mock(RuntimeService.class);
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);
  }

  @Test
  public void testFullMessageCorrelation() {
    String messageName = "aMessageName";
    String businessKey = "aBusinessKey";
    Map<String, Object> variables = VariablesBuilder.create().variable("aKey", "aValue").getVariables();

    Map<String, Object> correlationKeys = VariablesBuilder.create()
        .variable("aKey", "aValue")
        .variable("anotherKey", 1)
        .variable("aThirdKey", true).getVariables();

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("correlationKeys", correlationKeys);
    messageParameters.put("processVariables", variables);
    messageParameters.put("businessKey", businessKey);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_URL);

    Map<String, Object> expectedCorrelationKeys = new HashMap<String, Object>();
    expectedCorrelationKeys.put("aKey", "aValue");
    expectedCorrelationKeys.put("anotherKey", 1);
    expectedCorrelationKeys.put("aThirdKey", true);

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("aKey", "aValue");

    verify(runtimeServiceMock).correlateMessage(eq(messageName), eq(businessKey),
        argThat(new EqualsMap(expectedCorrelationKeys)), argThat(new EqualsMap(expectedVariables)));
  }

  @Test
  public void testMessageNameOnlyCorrelation() {
    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_URL);

    verify(runtimeServiceMock).correlateMessage(eq(messageName), eq((String) null),
        argThat(new EqualsMap(null)), argThat(new EqualsMap(null)));
  }

  @Test
  public void testMessageNameAndBusinessKeyCorrelation() {
    String messageName = "aMessageName";
    String businessKey = "aBusinessKey";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("businessKey", businessKey);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_URL);

    verify(runtimeServiceMock).correlateMessage(eq(messageName), eq(businessKey),
        argThat(new EqualsMap(null)), argThat(new EqualsMap(null)));
  }

  @Test
  public void testMismatchingCorrelation() {
    String messageName = "aMessage";

    doThrow(new MismatchingMessageCorrelationException(messageName, "Expected exception: cannot correlate"))
      .when(runtimeServiceMock).correlateMessage(any(String.class), any(String.class), any(Map.class), any(Map.class));

    Map<String, Object> messageParameters = new HashMap<String, Object>();
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
      .when(runtimeServiceMock).correlateMessage(any(String.class), any(String.class), any(Map.class), any(Map.class));

    Map<String, Object> messageParameters = new HashMap<String, Object>();
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
  public void testFailingDueToUnparseableIntegerInCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("correlationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(RestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver a message due to number format exception: For input string: \"1abc\""))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableShortInCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Short";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("correlationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(RestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver a message due to number format exception: For input string: \"1abc\""))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableLongInCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Long";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("correlationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(RestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver a message due to number format exception: For input string: \"1abc\""))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableDoubleInCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Double";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("correlationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(RestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver a message due to number format exception: For input string: \"1abc\""))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableDateInCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Date";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("correlationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(RestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver a message due to parse exception: Unparseable date: \"1abc\""))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToNotSupportedTypeInCorrelationKeys() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("correlationKeys", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(RestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver a message: The variable type 'X' is not supported."))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableIntegerInProcessVariables() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("processVariables", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(RestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver a message due to number format exception: For input string: \"1abc\""))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableShortInProcessVariables() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Short";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("processVariables", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(RestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver a message due to number format exception: For input string: \"1abc\""))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableLongInProcessVariables() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Long";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("processVariables", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(RestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver a message due to number format exception: For input string: \"1abc\""))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableDoubleInProcessVariables() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Double";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("processVariables", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(RestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver a message due to number format exception: For input string: \"1abc\""))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToUnparseableDateInProcessVariables() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Date";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("processVariables", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(RestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver a message due to parse exception: Unparseable date: \"1abc\""))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testFailingDueToNotSupportedTypeInProcessVariables() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("processVariables", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(RestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver a message: The variable type 'X' is not supported."))
    .when().post(MESSAGE_URL);
  }
}
