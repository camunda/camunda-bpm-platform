package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;

public abstract class AbstractExceptionHandlerTest extends AbstractRestServiceTest {

  private static final String EXCEPTION_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/unannotated";
  
  private static final String GENERAL_EXCEPTION_URL = EXCEPTION_RESOURCE_URL + "/exception";
  private static final String PROCESS_ENGINE_EXCEPTION_URL = EXCEPTION_RESOURCE_URL + "/processEngineException";
  private static final String REST_EXCEPTION_URL = EXCEPTION_RESOURCE_URL + "/restException";
  
  
  @Test
  public void testGeneralExceptionHandler() {
    given().header("Accept", "*/*")
    .expect().contentType(ContentType.JSON)
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", equalTo(Exception.class.getSimpleName()))
      .body("message", equalTo("expected exception"))
    .when().get(GENERAL_EXCEPTION_URL);
  }
  
  @Test
  public void testRestExceptionHandler() {
    given().header("Accept", "*/*")
    .expect().contentType(ContentType.JSON)
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("expected exception"))
    .when().get(REST_EXCEPTION_URL);
  }
  
  @Test
  public void testProcessEngineExceptionHandler() {
    given().header("Accept", "*/*")
    .expect().contentType(ContentType.JSON)
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
      .body("message", equalTo("expected exception"))
    .when().get(PROCESS_ENGINE_EXCEPTION_URL);
  }
}
