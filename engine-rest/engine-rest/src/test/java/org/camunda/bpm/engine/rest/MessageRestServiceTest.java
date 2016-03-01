package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.camunda.bpm.engine.rest.helper.ErrorMessageHelper;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Matchers;

import com.jayway.restassured.http.ContentType;

public class MessageRestServiceTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String MESSAGE_URL = TEST_RESOURCE_ROOT_PATH + "/message";

  private RuntimeService runtimeServiceMock;
  private MessageCorrelationBuilder messageCorrelationBuilderMock;

  @Before
  public void setupMocks() {
    runtimeServiceMock = mock(RuntimeService.class);
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);

    messageCorrelationBuilderMock = mock(MessageCorrelationBuilder.class);

    when(runtimeServiceMock.createMessageCorrelation(anyString())).thenReturn(messageCorrelationBuilderMock);
    when(messageCorrelationBuilderMock.processInstanceId(anyString())).thenReturn(messageCorrelationBuilderMock);
    when(messageCorrelationBuilderMock.processInstanceBusinessKey(anyString())).thenReturn(messageCorrelationBuilderMock);
    when(messageCorrelationBuilderMock.processInstanceVariableEquals(anyString(), any())).thenReturn(messageCorrelationBuilderMock);
    when(messageCorrelationBuilderMock.setVariables(Matchers.<Map<String,Object>>any())).thenReturn(messageCorrelationBuilderMock);
    when(messageCorrelationBuilderMock.setVariable(anyString(), any())).thenReturn(messageCorrelationBuilderMock);
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

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).processInstanceBusinessKey(eq(businessKey));
    verify(messageCorrelationBuilderMock).setVariables(argThat(new EqualsMap(expectedVariables)));

    for (Entry<String, Object> expectedKey : expectedCorrelationKeys.entrySet()) {
      String name = expectedKey.getKey();
      Object value = expectedKey.getValue();
      verify(messageCorrelationBuilderMock).processInstanceVariableEquals(name, value);
    }

    verify(messageCorrelationBuilderMock).correlate();

//    verify(runtimeServiceMock).correlateMessage(eq(messageName), eq(businessKey),
//        argThat(new EqualsMap(expectedCorrelationKeys)), argThat(new EqualsMap(expectedVariables)));
  }

  @Test
  public void testFullMessageCorrelationAll() {
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
    messageParameters.put("all", true);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_URL);

    Map<String, Object> expectedCorrelationKeys = new HashMap<String, Object>();
    expectedCorrelationKeys.put("aKey", "aValue");
    expectedCorrelationKeys.put("anotherKey", 1);
    expectedCorrelationKeys.put("aThirdKey", true);

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("aKey", "aValue");

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).processInstanceBusinessKey(eq(businessKey));
    verify(messageCorrelationBuilderMock).setVariables(argThat(new EqualsMap(expectedVariables)));

    for (Entry<String, Object> expectedKey : expectedCorrelationKeys.entrySet()) {
      String name = expectedKey.getKey();
      Object value = expectedKey.getValue();
      verify(messageCorrelationBuilderMock).processInstanceVariableEquals(name, value);
    }

    verify(messageCorrelationBuilderMock).correlateAll();

  }

  @Test
  public void testMessageNameOnlyCorrelation() {
    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_URL);

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).processInstanceBusinessKey(eq((String) null));
    verify(messageCorrelationBuilderMock).setVariables(argThat(new EqualsMap(null)));
    verify(messageCorrelationBuilderMock).correlate();

//    verify(runtimeServiceMock).correlateMessage(eq(messageName), eq((String) null),
//        argThat(new EqualsMap(null)), argThat(new EqualsMap(null)));
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

//    verify(runtimeServiceMock).correlateMessage(eq(messageName), eq(businessKey),
//        argThat(new EqualsMap(null)), argThat(new EqualsMap(null)));

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).processInstanceBusinessKey(eq(businessKey));
    verify(messageCorrelationBuilderMock).setVariables(argThat(new EqualsMap(null)));
    verify(messageCorrelationBuilderMock).correlate();

  }

  @Test
  public void testMessageNameAndBusinessKeyCorrelationAll() {
    String messageName = "aMessageName";
    String businessKey = "aBusinessKey";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("businessKey", businessKey);
    messageParameters.put("all", true);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_URL);

//    verify(runtimeServiceMock).correlateMessage(eq(messageName), eq(businessKey),
//        argThat(new EqualsMap(null)), argThat(new EqualsMap(null)));

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).processInstanceBusinessKey(eq(businessKey));
    verify(messageCorrelationBuilderMock).setVariables(argThat(new EqualsMap(null)));
    verify(messageCorrelationBuilderMock).correlateAll();

  }

  @Test
  public void testMismatchingCorrelation() {
    String messageName = "aMessage";

    doThrow(new MismatchingMessageCorrelationException(messageName, "Expected exception: cannot correlate"))
      .when(messageCorrelationBuilderMock).correlate();

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
      .when(messageCorrelationBuilderMock).correlate();

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
  public void testMessageCorrelationWithTenantId() {
    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("tenantId", MockProvider.EXAMPLE_TENANT_ID);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_URL);

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).processInstanceBusinessKey(eq((String) null));
    verify(messageCorrelationBuilderMock).setVariables(argThat(new EqualsMap(null)));
    verify(messageCorrelationBuilderMock).tenantId(MockProvider.EXAMPLE_TENANT_ID);
    verify(messageCorrelationBuilderMock).correlate();
  }

  @Test
  public void testMessageCorrelationWithoutTenantId() {
    String messageName = "aMessageName";

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_URL);

    verify(runtimeServiceMock).createMessageCorrelation(eq(messageName));
    verify(messageCorrelationBuilderMock).processInstanceBusinessKey(eq((String) null));
    verify(messageCorrelationBuilderMock).setVariables(argThat(new EqualsMap(null)));
    verify(messageCorrelationBuilderMock).withoutTenantId();
    verify(messageCorrelationBuilderMock).correlate();
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

    Map<String, Object> messageParameters = new HashMap<String, Object>();
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

    Map<String, Object> messageParameters = new HashMap<String, Object>();
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

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("correlationKeys", variableJson);

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

    Map<String, Object> messageParameters = new HashMap<String, Object>();
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

    Map<String, Object> messageParameters = new HashMap<String, Object>();
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

    Map<String, Object> messageParameters = new HashMap<String, Object>();
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

    Map<String, Object> messageParameters = new HashMap<String, Object>();
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

    Map<String, Object> messageParameters = new HashMap<String, Object>();
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

    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("processVariables", variableJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Cannot deliver message: Unsupported value type 'X'"))
    .when().post(MESSAGE_URL);
  }

  @Test
  public void testCorrelateThrowsAuthorizationException() {
    String messageName = "aMessageName";
    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(messageCorrelationBuilderMock).correlate();

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
  public void testCorrelateAllThrowsAuthorizationException() {
    String messageName = "aMessageName";
    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("all", true);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(messageCorrelationBuilderMock).correlateAll();

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

}
