/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest;

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinitionQuery;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class DecisionRequirementsDefinitionRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String DECISION_REQUIREMENTS_DEFINITION_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/decision-requirements-definition";
  protected static final String DECISION_REQUIREMENTS_DEFINITION_COUNT_QUERY_URL = DECISION_REQUIREMENTS_DEFINITION_QUERY_URL + "/count";

  private DecisionRequirementsDefinitionQuery mockedQuery;

  @Before
  public void setUpRuntime() {
    DecisionRequirementsDefinition mockDecisionRequirementsDefinition = MockProvider.createMockDecisionRequirementsDefinition();
    mockedQuery = createMockQuery(Collections.singletonList(mockDecisionRequirementsDefinition));
  }

  private DecisionRequirementsDefinitionQuery createMockQuery(List<DecisionRequirementsDefinition> mockedDefinitions) {
    DecisionRequirementsDefinitionQuery query = mock(DecisionRequirementsDefinitionQuery.class);

    when(query.list()).thenReturn(mockedDefinitions);
    when(query.count()).thenReturn((long) mockedDefinitions.size());
    when(processEngine.getRepositoryService().createDecisionRequirementsDefinitionQuery()).thenReturn(query);

    return query;
  }

  @Test
  public void testEmptyQuery() {
    given()
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(DECISION_REQUIREMENTS_DEFINITION_QUERY_URL);

    verify(mockedQuery).list();
  }

  @Test
  public void testInvalidNumericParameter() {
    String anInvalidIntegerQueryParam = "aString";

    given()
      .queryParam("version", anInvalidIntegerQueryParam)
      .then()
        .expect()
          .statusCode(Status.BAD_REQUEST.getStatusCode())
          .contentType(ContentType.JSON)
          .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
          .body("message", equalTo("Cannot set query parameter 'version' to value 'aString': "
            + "Cannot convert value aString to java type java.lang.Integer"))
      .when()
        .get(DECISION_REQUIREMENTS_DEFINITION_QUERY_URL);
  }

  @Test
  public void testInvalidBooleanParameter() {
    String anInvalidBooleanQueryParam = "neitherTrueNorFalse";

    given().queryParam("latestVersion", anInvalidBooleanQueryParam)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot set query parameter 'latestVersion' to value 'neitherTrueNorFalse': "
          + "Cannot convert value neitherTrueNorFalse to java type java.lang.Boolean"))
      .when().get(DECISION_REQUIREMENTS_DEFINITION_QUERY_URL);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("id", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  @Test
  public void testSortByParameterOnly() {
    given()
      .queryParam("sortBy", "id")
      .then()
        .expect()
          .statusCode(Status.BAD_REQUEST.getStatusCode())
      .when()
        .get(DECISION_REQUIREMENTS_DEFINITION_QUERY_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given()
      .queryParam("sortOrder", "asc")
      .then()
        .expect()
          .statusCode(Status.BAD_REQUEST.getStatusCode())
      .when()
        .get(DECISION_REQUIREMENTS_DEFINITION_QUERY_URL);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then()
      .expect()
        .statusCode(expectedStatus.getStatusCode())
    .when()
      .get(DECISION_REQUIREMENTS_DEFINITION_QUERY_URL);
  }

  @Test
  public void testSortingParameters() {
    // asc
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("id", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionRequirementsDefinitionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("name", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionRequirementsDefinitionName();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("version", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionRequirementsDefinitionVersion();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("key", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionRequirementsDefinitionKey();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("category", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionRequirementsDefinitionCategory();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("deploymentId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByDeploymentId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("tenantId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTenantId();
    inOrder.verify(mockedQuery).asc();

    // desc
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("id", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionRequirementsDefinitionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("name", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionRequirementsDefinitionName();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("version", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionRequirementsDefinitionVersion();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("key", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionRequirementsDefinitionKey();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("category", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionRequirementsDefinitionCategory();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("deploymentId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDeploymentId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("tenantId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTenantId();
    inOrder.verify(mockedQuery).desc();
  }

  @Test
  public void testSuccessfulPagination() {
    int firstResult = 0;
    int maxResults = 10;

    given()
      .queryParam("firstResult", firstResult)
      .queryParam("maxResults", maxResults)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(DECISION_REQUIREMENTS_DEFINITION_QUERY_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);
  }

  /**
   * If parameter "firstResult" is missing, we expect 0 as default.
   */
  @Test
  public void testMissingFirstResultParameter() {
    int maxResults = 10;

    given()
      .queryParam("maxResults", maxResults)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(DECISION_REQUIREMENTS_DEFINITION_QUERY_URL);

    verify(mockedQuery).listPage(0, maxResults);
  }

  /**
   * If parameter "maxResults" is missing, we expect Integer.MAX_VALUE as default.
   */
  @Test
  public void testMissingMaxResultsParameter() {
    int firstResult = 10;

    given()
      .queryParam("firstResult", firstResult)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(DECISION_REQUIREMENTS_DEFINITION_QUERY_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testDecisionRequirementsDefinitionRetrieval() {
    Response response = given()
        .then()
          .expect()
            .statusCode(Status.OK.getStatusCode())
        .when()
          .get(DECISION_REQUIREMENTS_DEFINITION_QUERY_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, String>> decisionRequirementsDefinitions = from(content).getList("");

    assertThat(decisionRequirementsDefinitions).hasSize(1);
    assertThat(decisionRequirementsDefinitions.get(0)).isNotNull();

    String returnedId = from(content).getString("[0].id");
    String returnedKey = from(content).getString("[0].key");
    String returnedCategory = from(content).getString("[0].category");
    String returnedName = from(content).getString("[0].name");
    int returnedVersion = from(content).getInt("[0].version");
    String returnedResource = from(content).getString("[0].resource");
    String returnedDeploymentId = from(content).getString("[0].deploymentId");
    String returnedTenantId = from(content).getString("[0].tenantId");

    assertThat(returnedId).isEqualTo(MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID);
    assertThat(returnedKey).isEqualTo(MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_KEY);
    assertThat(returnedCategory).isEqualTo(MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_CATEGORY);
    assertThat(returnedName).isEqualTo(MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_NAME);
    assertThat(returnedVersion).isEqualTo(MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_VERSION);
    assertThat(returnedResource).isEqualTo(MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_RESOURCE_NAME);
    assertThat(returnedDeploymentId).isEqualTo(MockProvider.EXAMPLE_DEPLOYMENT_ID);
    assertThat(returnedTenantId).isEqualTo(null);
  }

  @Test
  public void testDecisionRequirementsDefinitionRetrievalByList() {
    mockedQuery = createMockQuery(MockProvider.createMockTwoDecisionRequirementsDefinitions());

    Response response = given()
      .queryParam("decisionRequirementsDefinitionIdIn", MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID_LIST)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(DECISION_REQUIREMENTS_DEFINITION_QUERY_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    inOrder.verify(mockedQuery).decisionRequirementsDefinitionIdIn(MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID, MockProvider.ANOTHER_EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(2);

    String returnedDefinitionId1 = from(content).getString("[0].id");
    String returnedDefinitionId2 = from(content).getString("[1].id");

    assertThat(returnedDefinitionId1).isEqualTo(MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID);
    assertThat(returnedDefinitionId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID);
  }

  @Test
  public void testDecisionRequirementsDefinitionRetrievalByEmptyList() {
    given()
      .queryParam("decisionRequirementsDefinitionIdIn", "")
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(DECISION_REQUIREMENTS_DEFINITION_QUERY_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    inOrder.verify(mockedQuery, never()).decisionRequirementsDefinitionIdIn(Matchers.<String[]>anyVararg());
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testAdditionalParameters() {
    Map<String, String> queryParameters = getCompleteQueryParameters();

    given()
      .queryParams(queryParameters)
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(DECISION_REQUIREMENTS_DEFINITION_QUERY_URL);

    // assert query invocation
    verify(mockedQuery).decisionRequirementsDefinitionId(queryParameters.get("decisionRequirementsDefinitionId"));
    verify(mockedQuery).decisionRequirementsDefinitionCategory(queryParameters.get("category"));
    verify(mockedQuery).decisionRequirementsDefinitionCategoryLike(queryParameters.get("categoryLike"));
    verify(mockedQuery).decisionRequirementsDefinitionName(queryParameters.get("name"));
    verify(mockedQuery).decisionRequirementsDefinitionNameLike(queryParameters.get("nameLike"));
    verify(mockedQuery).decisionRequirementsDefinitionKey(queryParameters.get("key"));
    verify(mockedQuery).decisionRequirementsDefinitionKeyLike(queryParameters.get("keyLike"));
    verify(mockedQuery).decisionRequirementsDefinitionVersion(Integer.parseInt(queryParameters.get("version")));
    verify(mockedQuery).latestVersion();
    verify(mockedQuery).decisionRequirementsDefinitionResourceName(queryParameters.get("resourceName"));
    verify(mockedQuery).decisionRequirementsDefinitionResourceNameLike(queryParameters.get("resourceNameLike"));
    verify(mockedQuery).deploymentId(queryParameters.get("deploymentId"));
    verify(mockedQuery).list();
  }

  @Test
  public void testDecisionRequirementDefinitionTenantIdList() {
    List<DecisionRequirementsDefinition> decisionRequirementsDefinitions = Arrays.asList(
        MockProvider.mockDecisionRequirementsDefinition().tenantId(MockProvider.EXAMPLE_TENANT_ID).build(),
        MockProvider.createAnotherMockDecisionRequirementsDefinition());
    mockedQuery = createMockQuery(decisionRequirementsDefinitions);

    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(DECISION_REQUIREMENTS_DEFINITION_QUERY_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  @Test
  public void testDecisionRequirementsDefinitionWithoutTenantId() {
    Response response = given()
      .queryParam("withoutTenantId", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(DECISION_REQUIREMENTS_DEFINITION_QUERY_URL);

    verify(mockedQuery).withoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(1);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId1).isEqualTo(null);
  }

  @Test
  public void testDecisionRequirementsDefinitionTenantIdIncludeDefinitionsWithoutTenantid() {
    List<DecisionRequirementsDefinition> decisionDefinitions = Arrays.asList(
        MockProvider.mockDecisionRequirementsDefinition().tenantId(null).build(),
        MockProvider.mockDecisionRequirementsDefinition().tenantId(MockProvider.EXAMPLE_TENANT_ID).build());
    mockedQuery = createMockQuery(decisionDefinitions);

    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID)
      .queryParam("includeDecisionRequirementsDefinitionsWithoutTenantId", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(DECISION_REQUIREMENTS_DEFINITION_QUERY_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID);
    verify(mockedQuery).includeDecisionRequirementsDefinitionsWithoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(null);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
  }

  @Test
  public void testQueryCount() {
    expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().get(DECISION_REQUIREMENTS_DEFINITION_COUNT_QUERY_URL);

    verify(mockedQuery).count();
  }

  private Map<String, String> getCompleteQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("decisionRequirementsDefinitionId", "anId");
    parameters.put("category", "cat");
    parameters.put("categoryLike", "catlike");
    parameters.put("name", "name");
    parameters.put("nameLike", "namelike");
    parameters.put("key", "key");
    parameters.put("keyLike", "keylike");
    parameters.put("version", "1");
    parameters.put("latestVersion", "true");
    parameters.put("resourceName", "res");
    parameters.put("resourceNameLike", "resLike");
    parameters.put("deploymentId", "depId");

    return parameters;
  }

}
