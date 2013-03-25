package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.rest.helper.ExampleDataProvider;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractProcessEngineRestServiceTest extends
    AbstractRestServiceTest {

  protected static final String ENGINES_URL = TEST_RESOURCE_ROOT_PATH + "/engine";
  protected static final String SINGLE_ENGINE_URL = ENGINES_URL + "/{name}";
  protected static final String PROCESS_DEFINITION_URL = SINGLE_ENGINE_URL + "/process-definition/{id}";
  
  protected String EXAMPLE_ENGINE_NAME = "anEngineName";

  /**
   * Override this in specific test cases to create runtime data that is asserted.
   */
  @Before
  public abstract void setUpRuntimeData();

  @Test
  public void testNonExistingEngineAccess() {
    given().pathParam("name", ExampleDataProvider.NON_EXISTING_PROCESS_ENGINE_NAME)
      .pathParam("id", ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(PROCESS_DEFINITION_URL);
  }
  
  @Test
  public void testEngineNamesList() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(2))
      .body("[0].name", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_ENGINE_NAME))
      .body("[1].name", equalTo(ExampleDataProvider.ANOTHER_EXAMPLE_PROCESS_ENGINE_NAME))
    .when().get(ENGINES_URL);
  }
}
