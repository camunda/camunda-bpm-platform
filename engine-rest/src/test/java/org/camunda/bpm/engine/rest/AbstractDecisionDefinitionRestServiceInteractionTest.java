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
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.helper.MockDecisionResultBuilder;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.sub.repository.impl.ProcessDefinitionResourceImpl;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public abstract class AbstractDecisionDefinitionRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String DECISION_DEFINITION_URL = TEST_RESOURCE_ROOT_PATH + "/decision-definition";
  protected static final String SINGLE_DECISION_DEFINITION_URL = DECISION_DEFINITION_URL + "/{id}";
  protected static final String SINGLE_DECISION_DEFINITION_BY_KEY_URL = DECISION_DEFINITION_URL + "/key/{key}";

  protected static final String XML_DEFINITION_URL = SINGLE_DECISION_DEFINITION_URL + "/xml";
  protected static final String XML_DEFINITION_BY_KEY_URL = SINGLE_DECISION_DEFINITION_BY_KEY_URL + "/xml";

  protected static final String DIAGRAM_DEFINITION_URL = SINGLE_DECISION_DEFINITION_URL + "/diagram";

  protected static final String EVALUATE_DECISION_URL = SINGLE_DECISION_DEFINITION_URL + "/evaluate";
  protected static final String EVALUATE_DECISION_BY_KEY_URL = SINGLE_DECISION_DEFINITION_BY_KEY_URL + "/evaluate";

  private RepositoryService repositoryServiceMock;
  private DecisionDefinitionQuery decisionDefinitionQueryMock;
  private DecisionService decisionServiceMock;

  @Before
  public void setUpRuntime() {
    DecisionDefinition mockDecisionDefinition = MockProvider.createMockDecisionDefinition();

    repositoryServiceMock = mock(RepositoryService.class);

    when(processEngine.getRepositoryService()).thenReturn(repositoryServiceMock);
    when(repositoryServiceMock.getDecisionDefinition(eq(MockProvider.EXAMPLE_DECISION_DEFINITION_ID))).thenReturn(mockDecisionDefinition);
    when(repositoryServiceMock.getDecisionModel(eq(MockProvider.EXAMPLE_DECISION_DEFINITION_ID))).thenReturn(createMockDecisionDefinitionDmnXml());

    decisionDefinitionQueryMock = mock(DecisionDefinitionQuery.class);
    when(decisionDefinitionQueryMock.decisionDefinitionKey(MockProvider.EXAMPLE_DECISION_DEFINITION_KEY)).thenReturn(decisionDefinitionQueryMock);
    when(decisionDefinitionQueryMock.latestVersion()).thenReturn(decisionDefinitionQueryMock);
    when(decisionDefinitionQueryMock.singleResult()).thenReturn(mockDecisionDefinition);
    when(repositoryServiceMock.createDecisionDefinitionQuery()).thenReturn(decisionDefinitionQueryMock);

    decisionServiceMock = mock(DecisionService.class);
    when(processEngine.getDecisionService()).thenReturn(decisionServiceMock);
  }

  private InputStream createMockDecisionDefinitionDmnXml() {
    // do not close the input stream, will be done in implementation
    InputStream dmnXmlInputStream = ReflectUtil.getResourceAsStream("decisions/decision-model.dmn");
    Assert.assertNotNull(dmnXmlInputStream);
    return dmnXmlInputStream;
  }

  @Test
  public void testDecisionDefinitionDmnXmlRetrieval() {
    Response response = given()
        .pathParam("id", MockProvider.EXAMPLE_DECISION_DEFINITION_ID)
      .then()
        .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(XML_DEFINITION_URL);

    String responseContent = response.asString();
    Assert.assertTrue(responseContent.contains(MockProvider.EXAMPLE_DECISION_DEFINITION_ID));
    Assert.assertTrue(responseContent.contains("<?xml"));
  }

  @Test
  public void testDefinitionRetrieval() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_DECISION_DEFINITION_ID)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_DECISION_DEFINITION_ID))
        .body("key", equalTo(MockProvider.EXAMPLE_DECISION_DEFINITION_KEY))
        .body("category", equalTo(MockProvider.EXAMPLE_DECISION_DEFINITION_CATEGORY))
        .body("name", equalTo(MockProvider.EXAMPLE_DECISION_DEFINITION_NAME))
        .body("deploymentId", equalTo(MockProvider.EXAMPLE_DEPLOYMENT_ID))
        .body("version", equalTo(MockProvider.EXAMPLE_DECISION_DEFINITION_VERSION))
        .body("resource", equalTo(MockProvider.EXAMPLE_DECISION_DEFINITION_RESOURCE_NAME))
    .when()
      .get(SINGLE_DECISION_DEFINITION_URL);

    verify(repositoryServiceMock).getDecisionDefinition(MockProvider.EXAMPLE_DECISION_DEFINITION_ID);
  }

  @Test
  public void testDecisionDefinitionDmnXmlRetrieval_ByKey() {
    Response response = given()
        .pathParam("key", MockProvider.EXAMPLE_DECISION_DEFINITION_KEY)
      .then()
        .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(XML_DEFINITION_BY_KEY_URL);

    String responseContent = response.asString();
    Assert.assertTrue(responseContent.contains(MockProvider.EXAMPLE_DECISION_DEFINITION_ID));
    Assert.assertTrue(responseContent.contains("<?xml"));
  }

  @Test
  public void testDefinitionRetrieval_ByKey() {
    given()
      .pathParam("key", MockProvider.EXAMPLE_DECISION_DEFINITION_KEY)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_DECISION_DEFINITION_ID))
        .body("key", equalTo(MockProvider.EXAMPLE_DECISION_DEFINITION_KEY))
        .body("category", equalTo(MockProvider.EXAMPLE_DECISION_DEFINITION_CATEGORY))
        .body("name", equalTo(MockProvider.EXAMPLE_DECISION_DEFINITION_NAME))
        .body("deploymentId", equalTo(MockProvider.EXAMPLE_DEPLOYMENT_ID))
        .body("version", equalTo(MockProvider.EXAMPLE_DECISION_DEFINITION_VERSION))
        .body("resource", equalTo(MockProvider.EXAMPLE_DECISION_DEFINITION_RESOURCE_NAME))
    .when()
      .get(SINGLE_DECISION_DEFINITION_BY_KEY_URL);

    verify(repositoryServiceMock).getDecisionDefinition(MockProvider.EXAMPLE_DECISION_DEFINITION_ID);
  }

  @Test
  public void testNonExistingDecisionDefinitionRetrieval_ByKey() {
    String nonExistingKey = "aNonExistingDefinitionKey";

    when(repositoryServiceMock.createDecisionDefinitionQuery().decisionDefinitionKey(nonExistingKey)).thenReturn(decisionDefinitionQueryMock);
    when(decisionDefinitionQueryMock.latestVersion()).thenReturn(decisionDefinitionQueryMock);
    when(decisionDefinitionQueryMock.singleResult()).thenReturn(null);

    given()
      .pathParam("key", nonExistingKey)
    .then()
      .expect()
        .statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
        .body("type", is(RestException.class.getSimpleName()))
        .body("message", containsString("No matching decision definition with key: " + nonExistingKey))
    .when()
      .get(SINGLE_DECISION_DEFINITION_BY_KEY_URL);
  }

  @Test
  public void testDecisionDiagramRetrieval() throws FileNotFoundException, URISyntaxException {
    // setup additional mock behavior
    File file = getFile("/processes/todo-process.png");
    when(repositoryServiceMock.getDecisionDiagram(MockProvider.EXAMPLE_DECISION_DEFINITION_ID))
        .thenReturn(new FileInputStream(file));

    // call method
    byte[] actual = given().pathParam("id", MockProvider.EXAMPLE_DECISION_DEFINITION_ID)
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType("image/png")
          .header("Content-Disposition", "attachment; filename=" +
              MockProvider.EXAMPLE_DECISION_DEFINITION_DIAGRAM_RESOURCE_NAME)
        .when().get(DIAGRAM_DEFINITION_URL).getBody().asByteArray();

    // verify service interaction
    verify(repositoryServiceMock).getDecisionDefinition(MockProvider.EXAMPLE_DECISION_DEFINITION_ID);
    verify(repositoryServiceMock).getDecisionDiagram(MockProvider.EXAMPLE_DECISION_DEFINITION_ID);

    // compare input stream with response body bytes
    byte[] expected = IoUtil.readInputStream(new FileInputStream(file), "decision diagram");
    Assert.assertArrayEquals(expected, actual);
  }

  @Test
  public void testDecisionDiagramNullFilename() throws FileNotFoundException, URISyntaxException {
    // setup additional mock behavior
    File file = getFile("/processes/todo-process.png");
    when(repositoryServiceMock.getDecisionDefinition(MockProvider.EXAMPLE_DECISION_DEFINITION_ID).getDiagramResourceName())
      .thenReturn(null);
    when(repositoryServiceMock.getDecisionDiagram(MockProvider.EXAMPLE_DECISION_DEFINITION_ID))
        .thenReturn(new FileInputStream(file));

    // call method
    byte[] actual = given().pathParam("id", MockProvider.EXAMPLE_DECISION_DEFINITION_ID)
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType("application/octet-stream")
      .header("Content-Disposition", "attachment; filename=" + null)
      .when().get(DIAGRAM_DEFINITION_URL).getBody().asByteArray();

    // verify service interaction
    verify(repositoryServiceMock).getDecisionDiagram(MockProvider.EXAMPLE_DECISION_DEFINITION_ID);

    // compare input stream with response body bytes
    byte[] expected = IoUtil.readInputStream(new FileInputStream(file), "decision diagram");
    Assert.assertArrayEquals(expected, actual);
  }

  @Test
  public void testDecisionDiagramNotExist() {
    // setup additional mock behavior
    when(repositoryServiceMock.getDecisionDiagram(MockProvider.EXAMPLE_DECISION_DEFINITION_ID)).thenReturn(null);

    // call method
    given().pathParam("id", MockProvider.EXAMPLE_DECISION_DEFINITION_ID)
        .expect().statusCode(Status.NO_CONTENT.getStatusCode())
        .when().get(DIAGRAM_DEFINITION_URL);

    // verify service interaction
    verify(repositoryServiceMock).getDecisionDefinition(MockProvider.EXAMPLE_DECISION_DEFINITION_ID);
    verify(repositoryServiceMock).getDecisionDiagram(MockProvider.EXAMPLE_DECISION_DEFINITION_ID);
  }

  @Test
  public void testDecisionDiagramMediaType() {
    Assert.assertEquals("image/png", ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix("decision.png"));
    Assert.assertEquals("image/png", ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix("decision.PNG"));
    Assert.assertEquals("image/svg+xml", ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix("decision.svg"));
    Assert.assertEquals("image/jpeg", ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix("decision.jpeg"));
    Assert.assertEquals("image/jpeg", ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix("decision.jpg"));
    Assert.assertEquals("image/gif", ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix("decision.gif"));
    Assert.assertEquals("image/bmp", ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix("decision.bmp"));
    Assert.assertEquals("application/octet-stream", ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix("decision.UNKNOWN"));
  }

  @Test
  public void testEvaluateDecisionByKey() {
    DmnDecisionResult decisionResult = MockProvider.createMockDecisionResult();

    when(decisionServiceMock.evaluateDecisionById(eq(MockProvider.EXAMPLE_DECISION_DEFINITION_ID), anyMapOf(String.class, Object.class)))
        .thenReturn(decisionResult);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables",
        VariablesBuilder.create()
          .variable("amount", 420)
          .variable("invoiceCategory", "MISC")
          .getVariables()
    );

    given().pathParam("key", MockProvider.EXAMPLE_DECISION_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when().post(EVALUATE_DECISION_BY_KEY_URL);

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("amount", 420);
    expectedVariables.put("invoiceCategory", "MISC");

    verify(decisionServiceMock).evaluateDecisionById(MockProvider.EXAMPLE_DECISION_DEFINITION_ID, expectedVariables);
  }

  @Test
  public void testEvaluateDecisionById() {
    DmnDecisionResult decisionResult = MockProvider.createMockDecisionResult();

    when(decisionServiceMock.evaluateDecisionById(eq(MockProvider.EXAMPLE_DECISION_DEFINITION_ID), anyMapOf(String.class, Object.class)))
        .thenReturn(decisionResult);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables",
        VariablesBuilder.create()
          .variable("amount", 420)
          .variable("invoiceCategory", "MISC")
          .getVariables()
    );

    given().pathParam("id", MockProvider.EXAMPLE_DECISION_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when().post(EVALUATE_DECISION_URL);

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("amount", 420);
    expectedVariables.put("invoiceCategory", "MISC");

    verify(decisionServiceMock).evaluateDecisionById(MockProvider.EXAMPLE_DECISION_DEFINITION_ID, expectedVariables);
  }

  @Test
  public void testEvaluateDecisionSingleDecisionOutput() {
    DmnDecisionResult decisionResult = new MockDecisionResultBuilder()
        .decisionOutput()
          .output("status", Variables.stringValue("gold"))
        .build();

    when(decisionServiceMock.evaluateDecisionById(eq(MockProvider.EXAMPLE_DECISION_DEFINITION_ID), anyMapOf(String.class, Object.class)))
        .thenReturn(decisionResult);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", Collections.emptyMap());

    given().pathParam("id", MockProvider.EXAMPLE_DECISION_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("size()", is(1))
        .body("[0].size()", is(1))
        .body("[0].status", is(notNullValue()))
        .body("[0].status.value", is("gold"))
      .when().post(EVALUATE_DECISION_URL);
  }

  @Test
  public void testEvaluateDecisionMultipleDecisionOutputs() {
    DmnDecisionResult decisionResult = new MockDecisionResultBuilder()
        .decisionOutput()
          .output("status", Variables.stringValue("gold"))
        .decisionOutput()
          .output("assignee", Variables.stringValue("manager"))
        .build();

    when(decisionServiceMock.evaluateDecisionById(eq(MockProvider.EXAMPLE_DECISION_DEFINITION_ID), anyMapOf(String.class, Object.class)))
        .thenReturn(decisionResult);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", Collections.emptyMap());

    given().pathParam("id", MockProvider.EXAMPLE_DECISION_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("size()", is(2))
        .body("[0].size()", is(1))
        .body("[0].status.value", is("gold"))
        .body("[1].size()", is(1))
        .body("[1].assignee.value", is("manager"))

      .when().post(EVALUATE_DECISION_URL);
  }

  @Test
  public void testEvaluateDecisionMultipleDecisionValues() {
    DmnDecisionResult decisionResult = new MockDecisionResultBuilder()
        .decisionOutput()
          .output("status", Variables.stringValue("gold"))
          .output("assignee", Variables.stringValue("manager"))
        .build();

    when(decisionServiceMock.evaluateDecisionById(eq(MockProvider.EXAMPLE_DECISION_DEFINITION_ID), anyMapOf(String.class, Object.class)))
        .thenReturn(decisionResult);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", Collections.emptyMap());

    given().pathParam("id", MockProvider.EXAMPLE_DECISION_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("size()", is(1))
        .body("[0].size()", is(2))
        .body("[0].status.value", is("gold"))
        .body("[0].assignee.value", is("manager"))

      .when().post(EVALUATE_DECISION_URL);
  }

}
