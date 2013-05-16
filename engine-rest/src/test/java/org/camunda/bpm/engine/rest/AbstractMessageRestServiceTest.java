package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
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
import org.camunda.bpm.engine.rest.helper.EqualsMap;
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
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aKey", "aValue");
    
    Map<String, Object> correlationKeys = new HashMap<String, Object>();
    correlationKeys.put("aKey", "aValue");
    correlationKeys.put("anotherKey", 1);
    correlationKeys.put("aThirdKey", true);
    
    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("correlationKeys", correlationKeys);
    messageParameters.put("processVariables", variables);
    messageParameters.put("businessKey", businessKey);
    
    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_URL);
    
    verify(runtimeServiceMock).correlateMessage(eq(messageName), eq(businessKey), 
        argThat(new EqualsMap(correlationKeys)), argThat(new EqualsMap(variables)));
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
  public void testMismatchingCorrelation() {
    String messageName = "aMessage";
    
    doThrow(new MismatchingMessageCorrelationException(messageName, "Expected exception: cannot correlate"))
      .when(runtimeServiceMock).correlateMessage(any(String.class), any(String.class), any(Map.class), any(Map.class));
    
    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(MismatchingMessageCorrelationException.class.getSimpleName()))
      .body("message", equalTo("Expected exception: cannot correlate"))
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
      .body("message", equalTo("Message name has to be supplied"))
      .when().post(MESSAGE_URL);
  }
}
