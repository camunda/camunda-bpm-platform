package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.rest.helper.ExampleDataProvider;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractStatisticsRestTest extends AbstractRestServiceTest {

  protected static final String PROCESS_DEFINITION_STATISTICS_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition/statistics";
  protected static final String ACTIVITY_STATISTICS_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition/{id}/statistics";
  
  /**
   * Override this in specific test cases to create runtime data that is asserted.
   */
  @Before
  public abstract void setUpRuntimeData();
  
  @Test
  public void testStatisticsRetrievalPerProcessDefinitionVersion() {
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(2))
      .body("definition.size()", is(2))
      .body("definition.id", hasItems(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID, ExampleDataProvider.ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID))
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
  }
  
  @Test
  public void testActivityStatisticsRetrieval() {
    given().pathParam("id", "aDefinitionId")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(2))
      .body("id", hasItems(ExampleDataProvider.EXAMPLE_ACTIVITY_ID, ExampleDataProvider.ANOTHER_EXAMPLE_ACTIVITY_ID))
    .when().get(ACTIVITY_STATISTICS_URL);
  }
}
