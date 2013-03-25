package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.rest.helper.ExampleDataProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.response.Response;

public abstract class AbstractProcessDefinitionRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String SINGLE_PROCESS_DEFINITION_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition/{id}";
  protected static final String START_PROCESS_INSTANCE_URL = SINGLE_PROCESS_DEFINITION_URL + "/start";
  protected static final String XML_DEFINITION_URL = SINGLE_PROCESS_DEFINITION_URL + "/xml";
  protected static final String START_FORM_URL = SINGLE_PROCESS_DEFINITION_URL + "/startForm";
  
  /**
   * Override this in specific test cases to create runtime data that is asserted.
   */
  @Before
  public abstract void setUpRuntimeData();
  
  @Test
  public void testSimpleProcessInstantiation() {
    given().pathParam("id", ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_URL);
  }
  

  @Test
  public void testInstanceResourceLinkResult() {
    String fullInstanceUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/process-instance/" + ExampleDataProvider.EXAMPLE_PROCESS_INSTANCE_ID;
    
    given().pathParam("id", ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("links[0].href", equalTo(fullInstanceUrl))
      .when().post(START_PROCESS_INSTANCE_URL);
  }
  

  @Test
  public void testInstanceResourceLinkWithEnginePrefix() {
    String startInstanceOnExplicitEngineUrl = TEST_RESOURCE_ROOT_PATH + "/engine/default/process-definition/{id}/start";
    
    String fullInstanceUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/engine/default/process-instance/" + ExampleDataProvider.EXAMPLE_PROCESS_INSTANCE_ID;
    
    given().pathParam("id", ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("links[0].href", equalTo(fullInstanceUrl))
      .when().post(startInstanceOnExplicitEngineUrl);
  }

  @Test
  public void testProcessDefinitionBpmn20XmlRetrieval() {
    // Rest-assured has problems with extracting json with escaped quotation marks, i.e. the xml content in our case
    Response response = given().pathParam("id", ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
//      .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
//      .body("bpmn20Xml", startsWith("<?xml"))
    .when().get(XML_DEFINITION_URL);

    String responseContent = response.asString();
    Assert.assertTrue(responseContent.contains(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    Assert.assertTrue(responseContent.contains("<?xml"));
  }

  @Test
  public void testGetStartFormData() {
    given().pathParam("id", ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("key", equalTo(ExampleDataProvider.EXAMPLE_FORM_KEY))
    .when().get(START_FORM_URL);
  }
}
