package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import javax.ws.rs.core.Response.Status;

import org.junit.Test;

/**
 * This test only tests the REST interface, but no interaction with the engine as it assumes a stubbed 
 * implementation.
 * @author Thorben Lindhauer
 *
 */
public class ProcessDefinitionStatisticsStubTest extends AbstractRestServiceTest {

  private static final String PROCESS_DEFINITION_STATISTICS_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition/statistics";
  
  private static final String EXAMPLE_PROCESS_DEFINITION_ID = "processDefinition1";
  private static final int EXAMPLE_PROCESS_INSTANCES = 42;
  
  private static final String ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID = "processDefinition2";
  private static final int ANOTHER_EXAMPLE_PROCESS_INSTANCES = 123;
  
  @Test
  public void testStatisticsRetrievalPerProcessDefinition() throws IOException {
    setupTestScenario();
    
    given().queryParam("groupBy", "definition")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(2))
      .body("id", hasItems(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID))
      .body("instances", hasItems(EXAMPLE_PROCESS_INSTANCES, ANOTHER_EXAMPLE_PROCESS_INSTANCES))
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
  }
  
  @Test
  public void testStatisticsRetrievalPerProcessDefinitionVersion() throws IOException {
    setupTestScenario();
    
    String firstExampleProcessDefinitionId = EXAMPLE_PROCESS_DEFINITION_ID + ":1";
    String secondExampleProcessDefinitionId = EXAMPLE_PROCESS_DEFINITION_ID + ":2";
    String firstAnotherExampleProcessDefinitionId = EXAMPLE_PROCESS_DEFINITION_ID + ":1";
    String secondAnotherExampleProcessDefinitionId = EXAMPLE_PROCESS_DEFINITION_ID + ":2";
    
    given().queryParam("groupBy", "version")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(4))
      .body("id", hasItems(firstExampleProcessDefinitionId, secondExampleProcessDefinitionId,
          firstAnotherExampleProcessDefinitionId, secondAnotherExampleProcessDefinitionId))
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
  }
  
  @Test
  public void testInvalidGroupingOption() throws IOException {
    setupTestScenario();
    
    given().queryParam("groupBy", "aBadGroupingOption")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
  }
}
