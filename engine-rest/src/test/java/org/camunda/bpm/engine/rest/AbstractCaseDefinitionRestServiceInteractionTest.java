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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.rest.dto.runtime.VariableValueDto;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * @author Roman Smirnov
 *
 */
public abstract class AbstractCaseDefinitionRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String CASE_DEFINITION_URL = TEST_RESOURCE_ROOT_PATH + "/case-definition";
  protected static final String SINGLE_CASE_DEFINITION_URL = CASE_DEFINITION_URL + "/{id}";
  protected static final String SINGLE_CASE_DEFINITION_BY_KEY_URL = CASE_DEFINITION_URL + "/key/{key}";

  protected static final String XML_DEFINITION_URL = SINGLE_CASE_DEFINITION_URL + "/xml";
  protected static final String XML_DEFINITION_BY_KEY_URL = SINGLE_CASE_DEFINITION_BY_KEY_URL + "/xml";

  protected static final String CREATE_INSTANCE_URL = SINGLE_CASE_DEFINITION_URL + "/create";
  protected static final String CREATE_INSTANCE_BY_KEY_URL = SINGLE_CASE_DEFINITION_BY_KEY_URL + "/create";

  private RepositoryService repositoryServiceMock;
  private CaseService caseServiceMock;
  private CaseDefinitionQuery caseDefinitionQueryMock;
  private CaseInstanceBuilder caseInstanceBuilder;

  @Before
  public void setUpRuntime() {
    CaseDefinition mockCaseDefinition = MockProvider.createMockCaseDefinition();

    repositoryServiceMock = mock(RepositoryService.class);

    when(processEngine.getRepositoryService()).thenReturn(repositoryServiceMock);
    when(repositoryServiceMock.getCaseDefinition(eq(MockProvider.EXAMPLE_CASE_DEFINITION_ID))).thenReturn(mockCaseDefinition);
    when(repositoryServiceMock.getCaseModel(eq(MockProvider.EXAMPLE_CASE_DEFINITION_ID))).thenReturn(createMockCaseDefinitionCmmnXml());

    caseDefinitionQueryMock = mock(CaseDefinitionQuery.class);
    when(caseDefinitionQueryMock.caseDefinitionKey(MockProvider.EXAMPLE_CASE_DEFINITION_KEY)).thenReturn(caseDefinitionQueryMock);
    when(caseDefinitionQueryMock.latestVersion()).thenReturn(caseDefinitionQueryMock);
    when(caseDefinitionQueryMock.singleResult()).thenReturn(mockCaseDefinition);
    when(repositoryServiceMock.createCaseDefinitionQuery()).thenReturn(caseDefinitionQueryMock);

    caseServiceMock = mock(CaseService.class);

    when(processEngine.getCaseService()).thenReturn(caseServiceMock);

    caseInstanceBuilder = mock(CaseInstanceBuilder.class);
    CaseInstance mockCaseInstance = MockProvider.createMockCaseInstance();

    when(caseServiceMock.createCaseInstanceById(MockProvider.EXAMPLE_CASE_DEFINITION_ID)).thenReturn(caseInstanceBuilder);
    when(caseInstanceBuilder.businessKey(anyString())).thenReturn(caseInstanceBuilder);
    when(caseInstanceBuilder.setVariables(Matchers.<Map<String, Object>>any())).thenReturn(caseInstanceBuilder);
    when(caseInstanceBuilder.create()).thenReturn(mockCaseInstance);
  }

  private InputStream createMockCaseDefinitionCmmnXml() {
    // do not close the input stream, will be done in implementation
    InputStream cmmnXmlInputStream = ReflectUtil.getResourceAsStream("cases/case-model.cmmn");
    Assert.assertNotNull(cmmnXmlInputStream);
    return cmmnXmlInputStream;
  }

  @Test
  public void testCaseDefinitionCmmnXmlRetrieval() {
    Response response = given()
        .pathParam("id", MockProvider.EXAMPLE_CASE_DEFINITION_ID)
      .then()
        .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(XML_DEFINITION_URL);

    String responseContent = response.asString();
    Assert.assertTrue(responseContent.contains(MockProvider.EXAMPLE_CASE_DEFINITION_ID));
    Assert.assertTrue(responseContent.contains("<?xml"));
  }

  @Test
  public void testDefinitionRetrieval() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_DEFINITION_ID)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_ID))
        .body("key", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_KEY))
        .body("category", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_CATEGORY))
        .body("name", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_NAME))
        .body("deploymentId", equalTo(MockProvider.EXAMPLE_DEPLOYMENT_ID))
        .body("version", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_VERSION))
        .body("resource", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_RESOURCE_NAME))
    .when()
      .get(SINGLE_CASE_DEFINITION_URL);

    verify(repositoryServiceMock).getCaseDefinition(MockProvider.EXAMPLE_CASE_DEFINITION_ID);
  }

  @Test
  public void testCaseDefinitionCmmnXmlRetrieval_ByKey() {
    Response response = given()
        .pathParam("key", MockProvider.EXAMPLE_CASE_DEFINITION_KEY)
      .then()
        .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(XML_DEFINITION_BY_KEY_URL);

    String responseContent = response.asString();
    Assert.assertTrue(responseContent.contains(MockProvider.EXAMPLE_CASE_DEFINITION_ID));
    Assert.assertTrue(responseContent.contains("<?xml"));
  }

  @Test
  public void testDefinitionRetrieval_ByKey() {
    given()
      .pathParam("key", MockProvider.EXAMPLE_CASE_DEFINITION_KEY)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_ID))
        .body("key", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_KEY))
        .body("category", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_CATEGORY))
        .body("name", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_NAME))
        .body("deploymentId", equalTo(MockProvider.EXAMPLE_DEPLOYMENT_ID))
        .body("version", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_VERSION))
        .body("resource", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_RESOURCE_NAME))
    .when()
      .get(SINGLE_CASE_DEFINITION_BY_KEY_URL);

    verify(repositoryServiceMock).getCaseDefinition(MockProvider.EXAMPLE_CASE_DEFINITION_ID);
  }

  @Test
  public void testNonExistingCaseDefinitionRetrieval_ByKey() {
    String nonExistingKey = "aNonExistingDefinitionKey";

    when(repositoryServiceMock.createCaseDefinitionQuery().caseDefinitionKey(nonExistingKey)).thenReturn(caseDefinitionQueryMock);
    when(caseDefinitionQueryMock.latestVersion()).thenReturn(caseDefinitionQueryMock);
    when(caseDefinitionQueryMock.singleResult()).thenReturn(null);

    given()
      .pathParam("key", nonExistingKey)
    .then()
      .expect()
        .statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
        .body("type", is(RestException.class.getSimpleName()))
        .body("message", containsString("No matching case definition with key: " + nonExistingKey))
    .when()
      .get(SINGLE_CASE_DEFINITION_BY_KEY_URL);
  }

  @Test
  public void testCreateCaseInstanceByCaseDefinitionId() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_ID))
    .when()
      .post(CREATE_INSTANCE_URL);

    verify(caseServiceMock).createCaseInstanceById(MockProvider.EXAMPLE_CASE_DEFINITION_ID);
    verify(caseInstanceBuilder).businessKey(null);
    verify(caseInstanceBuilder).setVariables(null);
    verify(caseInstanceBuilder).create();

  }

  @Test
  public void testCreateCaseInstanceByCaseDefinitionKey() {
    given()
      .pathParam("key", MockProvider.EXAMPLE_CASE_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_ID))
    .when()
      .post(CREATE_INSTANCE_BY_KEY_URL);

    verify(caseServiceMock).createCaseInstanceById(MockProvider.EXAMPLE_CASE_DEFINITION_ID);
    verify(caseInstanceBuilder).businessKey(null);
    verify(caseInstanceBuilder).setVariables(null);
    verify(caseInstanceBuilder).create();
  }

  @Test
  public void testCreateCaseInstanceByCaseDefinitionIdWithBusinessKey() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("businessKey", MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_ID))
    .when()
      .post(CREATE_INSTANCE_URL);

    verify(caseServiceMock).createCaseInstanceById(MockProvider.EXAMPLE_CASE_DEFINITION_ID);
    verify(caseInstanceBuilder).businessKey(MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY);
    verify(caseInstanceBuilder).setVariables(null);
    verify(caseInstanceBuilder).create();

  }

  @Test
  public void testCreateCaseInstanceByCaseDefinitionKeyWithBusinessKey() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("businessKey", MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY);

    given()
      .pathParam("key", MockProvider.EXAMPLE_CASE_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_ID))
    .when()
      .post(CREATE_INSTANCE_BY_KEY_URL);

    verify(caseServiceMock).createCaseInstanceById(MockProvider.EXAMPLE_CASE_DEFINITION_ID);
    verify(caseInstanceBuilder).businessKey(MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY);
    verify(caseInstanceBuilder).setVariables(null);
    verify(caseInstanceBuilder).create();
  }

  @Test
  public void testCreateCaseInstanceByCaseDefinitionIdWithVariables() {
    VariableValueDto aVariable = new VariableValueDto("abc", null);
    VariableValueDto anotherVariable = new VariableValueDto(999, null);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", aVariable);
    variables.put("anotherVariableName", anotherVariable);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("variables", variables);

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_ID))
    .when()
      .post(CREATE_INSTANCE_URL);

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("aVariableName", "abc");
    expectedVariables.put("anotherVariableName", 999);

    verify(caseServiceMock).createCaseInstanceById(MockProvider.EXAMPLE_CASE_DEFINITION_ID);
    verify(caseInstanceBuilder).businessKey(null);
    verify(caseInstanceBuilder).setVariables(argThat(new EqualsMap(expectedVariables)));
    verify(caseInstanceBuilder).create();

  }

  @Test
  public void testCreateCaseInstanceByCaseDefinitionKeyWithVariables() {
    VariableValueDto aVariable = new VariableValueDto("abc", null);
    VariableValueDto anotherVariable = new VariableValueDto(999, null);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", aVariable);
    variables.put("anotherVariableName", anotherVariable);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("variables", variables);

    given()
      .pathParam("key", MockProvider.EXAMPLE_CASE_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_ID))
    .when()
      .post(CREATE_INSTANCE_BY_KEY_URL);

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("aVariableName", "abc");
    expectedVariables.put("anotherVariableName", 999);

    verify(caseServiceMock).createCaseInstanceById(MockProvider.EXAMPLE_CASE_DEFINITION_ID);
    verify(caseInstanceBuilder).businessKey(null);
    verify(caseInstanceBuilder).setVariables(argThat(new EqualsMap(expectedVariables)));
    verify(caseInstanceBuilder).create();
  }

  @Test
  public void testCreateCaseInstanceByCaseDefinitionIdWithBusinessKeyAndVariables() {
    VariableValueDto aVariable = new VariableValueDto("abc", null);
    VariableValueDto anotherVariable = new VariableValueDto(999, null);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", aVariable);
    variables.put("anotherVariableName", anotherVariable);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("variables", variables);
    params.put("businessKey", "aBusinessKey");

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_ID))
    .when()
      .post(CREATE_INSTANCE_URL);

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("aVariableName", "abc");
    expectedVariables.put("anotherVariableName", 999);

    verify(caseServiceMock).createCaseInstanceById(MockProvider.EXAMPLE_CASE_DEFINITION_ID);
    verify(caseInstanceBuilder).businessKey("aBusinessKey");
    verify(caseInstanceBuilder).setVariables(argThat(new EqualsMap(expectedVariables)));
    verify(caseInstanceBuilder).create();

  }

  @Test
  public void testCreateCaseInstanceByCaseDefinitionKeyWithBusinessKeyAndVariables() {
    VariableValueDto aVariable = new VariableValueDto("abc", null);
    VariableValueDto anotherVariable = new VariableValueDto(999, null);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariableName", aVariable);
    variables.put("anotherVariableName", anotherVariable);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("variables", variables);
    params.put("businessKey", "aBusinessKey");

    given()
      .pathParam("key", MockProvider.EXAMPLE_CASE_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_ID))
    .when()
      .post(CREATE_INSTANCE_BY_KEY_URL);

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("aVariableName", "abc");
    expectedVariables.put("anotherVariableName", 999);

    verify(caseServiceMock).createCaseInstanceById(MockProvider.EXAMPLE_CASE_DEFINITION_ID);
    verify(caseInstanceBuilder).businessKey("aBusinessKey");
    verify(caseInstanceBuilder).setVariables(argThat(new EqualsMap(expectedVariables)));
    verify(caseInstanceBuilder).create();
  }

  @Test
  public void testCreateCaseInstanceByInvalidCaseDefinitionId() {
    when(caseInstanceBuilder.create())
      .thenThrow(new ProcessEngineException("expected exception"));

    given()
      .pathParam("id", MockProvider.EXAMPLE_CASE_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(RestException.class.getSimpleName()))
        .body("message", containsString("Cannot instantiate case definition aCaseDefnitionId: expected exception"))
    .when()
      .post(CREATE_INSTANCE_URL);
  }

  @Test
  public void testCreateCaseInstanceByInvalidCaseDefinitionKey() {
    when(caseInstanceBuilder.create())
      .thenThrow(new ProcessEngineException("expected exception"));

    given()
      .pathParam("key", MockProvider.EXAMPLE_CASE_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(RestException.class.getSimpleName()))
        .body("message", containsString("Cannot instantiate case definition aCaseDefnitionId: expected exception"))
    .when()
      .post(CREATE_INSTANCE_BY_KEY_URL);
  }

}
