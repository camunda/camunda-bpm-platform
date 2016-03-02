package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.OrderingBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.response.Response;

public class JobDefinitionRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String JOB_DEFINITION_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/job-definition";
  protected static final String JOB_DEFINITION_COUNT_QUERY_URL = JOB_DEFINITION_QUERY_URL + "/count";
  private JobDefinitionQuery mockedQuery;

  @Before
  public void setUpRuntimeData() {
    mockedQuery = setUpMockDefinitionQuery(MockProvider.createMockJobDefinitions());
  }

  private JobDefinitionQuery setUpMockDefinitionQuery(List<JobDefinition> mockedJobDefinitions) {
    JobDefinitionQuery query = mock(JobDefinitionQuery.class);

    when(query.list()).thenReturn(mockedJobDefinitions);
    when(query.count()).thenReturn((long) mockedJobDefinitions.size());
    when(processEngine.getManagementService().createJobDefinitionQuery()).thenReturn(query);

    return query;
  }

  @Test
  public void testEmptyQuery() {
    String queryJobDefinitionId = "";
    given().queryParam("jobDefinitionId", queryJobDefinitionId).then().expect()
        .statusCode(Status.OK.getStatusCode())
        .when().get(JOB_DEFINITION_QUERY_URL);

    verify(mockedQuery).list();
  }

  @Test
  public void testEmptyQueryAsPost() {
    Map<String, String> params = new HashMap<String, String>();
    params.put("jobDefinitionId", "");

    given().contentType(POST_JSON_CONTENT_TYPE).body(params)
    .expect().statusCode(Status.OK.getStatusCode())
    .when().post(JOB_DEFINITION_QUERY_URL);

    verify(mockedQuery).list();
  }

  @Test
  public void testNoParametersQuery() {
    expect().statusCode(Status.OK.getStatusCode()).when().get(JOB_DEFINITION_QUERY_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testNoParametersQueryAsPost() {
    given().contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().post(JOB_DEFINITION_QUERY_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("jobDefinitionId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then()
      .expect()
        .statusCode(expectedStatus.getStatusCode())
    .when()
      .get(JOB_DEFINITION_QUERY_URL);
  }

  @Test
  public void testSortByParameterOnly() {
    given().queryParam("sortBy", "jobDefinitionId")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(JOB_DEFINITION_QUERY_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given().queryParam("sortOrder", "asc")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(JOB_DEFINITION_QUERY_URL);
  }

  @Test
  public void testSortingParameters() {
    // asc
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("jobDefinitionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByJobDefinitionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByActivityId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processDefinitionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processDefinitionKey", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionKey();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("jobType", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByJobType();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("jobConfiguration", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByJobConfiguration();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("tenantId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTenantId();
    inOrder.verify(mockedQuery).asc();

    // desc
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("jobDefinitionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByJobDefinitionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByActivityId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processDefinitionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processDefinitionKey", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionKey();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("jobType", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByJobType();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("jobConfiguration", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByJobConfiguration();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("tenantId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTenantId();
    inOrder.verify(mockedQuery).desc();
  }

  @Test
  public void testSecondarySortingAsPost() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("sorting", OrderingBuilder.create()
      .orderBy("jobType").desc()
      .orderBy("jobConfiguration").asc()
      .getJson());
    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(JOB_DEFINITION_QUERY_URL);

    inOrder.verify(mockedQuery).orderByJobType();
    inOrder.verify(mockedQuery).desc();
    inOrder.verify(mockedQuery).orderByJobConfiguration();
    inOrder.verify(mockedQuery).asc();
  }

  @Test
  public void testSuccessfulPagination() {

    int firstResult = 0;
    int maxResults = 10;
    given().queryParam("firstResult", firstResult).queryParam("maxResults", maxResults)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(JOB_DEFINITION_QUERY_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);
  }

  /**
   * If parameter "firstResult" is missing, we expect 0 as default.
   */
  @Test
  public void testMissingFirstResultParameter() {
    int maxResults = 10;
    given().queryParam("maxResults", maxResults)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(JOB_DEFINITION_QUERY_URL);

    verify(mockedQuery).listPage(0, maxResults);
  }

  /**
   * If parameter "maxResults" is missing, we expect Integer.MAX_VALUE as default.
   */
  @Test
  public void testMissingMaxResultsParameter() {
    int firstResult = 10;
    given().queryParam("firstResult", firstResult)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(JOB_DEFINITION_QUERY_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testJobDefinitionRetrieval() {
    String queryJobDefinitionId = "aJobDefId";
    Response response = given().queryParam("jobDefinitionId", queryJobDefinitionId)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().get(JOB_DEFINITION_QUERY_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    inOrder.verify(mockedQuery).jobDefinitionId(queryJobDefinitionId);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, String>> jobDefinitions = from(content).getList("");

    assertThat(jobDefinitions).hasSize(1);
    assertThat(jobDefinitions.get(0)).isNotNull();

    String returnedId = from(content).getString("[0].id");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedProcessDefinitionKey = from(content).getString("[0].processDefinitionKey");
    String returnedActivityId = from(content).getString("[0].activityId");
    String returnedJobType = from(content).getString("[0].jobType");
    String returnedJobConfiguration = from(content).getString("[0].jobConfiguration");
    boolean returnedSuspensionState = from(content).getBoolean("[0].suspended");
    Long returnedJobPriority = from(content).getObject("[0].overridingJobPriority", Long.class);

    assertThat(returnedId).isEqualTo(MockProvider.EXAMPLE_JOB_DEFINITION_ID);
    assertThat(returnedProcessDefinitionId).isEqualTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    assertThat(returnedProcessDefinitionKey).isEqualTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    assertThat(returnedActivityId).isEqualTo(MockProvider.EXAMPLE_ACTIVITY_ID);
    assertThat(returnedJobType).isEqualTo(MockProvider.EXAMPLE_JOB_TYPE);
    assertThat(returnedJobConfiguration).isEqualTo(MockProvider.EXAMPLE_JOB_CONFIG);
    assertThat(returnedSuspensionState).isEqualTo(MockProvider.EXAMPLE_JOB_DEFINITION_IS_SUSPENDED);
    assertThat(returnedJobPriority).isEqualTo(MockProvider.EXAMPLE_JOB_DEFINITION_PRIORITY);
  }

  @Test
  public void testJobDefinitionRetrievalAsPost() {
    String queryJobDefinitionId = "aJobDefId";
    Map<String, String> queryParameter = new HashMap<String, String>();
    queryParameter.put("jobDefinitionId", queryJobDefinitionId);

    Response response = given().contentType(POST_JSON_CONTENT_TYPE).body(queryParameter)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().post(JOB_DEFINITION_QUERY_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    inOrder.verify(mockedQuery).jobDefinitionId(queryJobDefinitionId);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, String>> jobDefinitions = from(content).getList("");

    assertThat(jobDefinitions).hasSize(1);
    assertThat(jobDefinitions.get(0)).isNotNull();

    String returnedId = from(content).getString("[0].id");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedProcessDefinitionKey = from(content).getString("[0].processDefinitionKey");
    String returnedActivityId = from(content).getString("[0].activityId");
    String returnedJobType = from(content).getString("[0].jobType");
    String returnedJobConfiguration = from(content).getString("[0].jobConfiguration");
    boolean returnedSuspensionState = from(content).getBoolean("[0].suspended");

    assertThat(returnedId).isEqualTo(MockProvider.EXAMPLE_JOB_DEFINITION_ID);
    assertThat(returnedProcessDefinitionId).isEqualTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    assertThat(returnedProcessDefinitionKey).isEqualTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    assertThat(returnedActivityId).isEqualTo(MockProvider.EXAMPLE_ACTIVITY_ID);
    assertThat(returnedJobType).isEqualTo(MockProvider.EXAMPLE_JOB_TYPE);
    assertThat(returnedJobConfiguration).isEqualTo(MockProvider.EXAMPLE_JOB_CONFIG);
    assertThat(returnedSuspensionState).isEqualTo(MockProvider.EXAMPLE_JOB_DEFINITION_IS_SUSPENDED);
  }

  @Test
  public void testMultipleParameters() {
    Map<String, String> queryParameters = new HashMap<String, String>();

    queryParameters.put("jobDefinitionId", "aJobDefId");
    queryParameters.put("processDefinitionId", "aProcDefId");
    queryParameters.put("processDefinitionKey", "aProcDefKey");
    queryParameters.put("activityIdIn", "aActId,anotherActId");
    queryParameters.put("jobType", "aJobType");
    queryParameters.put("jobConfiguration", "aJobConfig");
    queryParameters.put("suspended", "true");
    queryParameters.put("active", "true");
    queryParameters.put("withOverridingJobPriority", "true");

    given().queryParams(queryParameters)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().get(JOB_DEFINITION_QUERY_URL);

    verify(mockedQuery).jobDefinitionId(queryParameters.get("jobDefinitionId"));
    verify(mockedQuery).processDefinitionId(queryParameters.get("processDefinitionId"));
    verify(mockedQuery).processDefinitionKey(queryParameters.get("processDefinitionKey"));
    verify(mockedQuery).activityIdIn("aActId", "anotherActId");
    verify(mockedQuery).jobType(queryParameters.get("jobType"));
    verify(mockedQuery).jobConfiguration(queryParameters.get("jobConfiguration"));
    verify(mockedQuery).active();
    verify(mockedQuery).suspended();
    verify(mockedQuery).withOverridingJobPriority();
    verify(mockedQuery).list();
  }

  @Test
  public void testMultipleParametersAsPost() {
    String aJobDefId = "aJobDefId";
    String aProcDefId = "aProcDefId";
    String aProcDefKey = "aProcDefKey";
    String aActId = "aActId";
    String anotherActId = "anotherActId";
    String aJobType = "aJobType";
    String aJobConfig = "aJobConfig";

    Map<String, Object> queryParameters = new HashMap<String, Object>();

    queryParameters.put("jobDefinitionId", aJobDefId);
    queryParameters.put("processDefinitionId", aProcDefId);
    queryParameters.put("processDefinitionKey", aProcDefKey);
    queryParameters.put("jobType", aJobType);
    queryParameters.put("jobConfiguration", aJobConfig);
    queryParameters.put("suspended", "true");
    queryParameters.put("active", "true");
    queryParameters.put("withOverridingJobPriority", "true");

    List<String> activityIdIn = new ArrayList<String>();
    activityIdIn.add(aActId);
    activityIdIn.add(anotherActId);
    queryParameters.put("activityIdIn", activityIdIn);

    given().contentType(POST_JSON_CONTENT_TYPE).body(queryParameters)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(JOB_DEFINITION_QUERY_URL);

    verify(mockedQuery).jobDefinitionId(aJobDefId);
    verify(mockedQuery).processDefinitionId(aProcDefId);
    verify(mockedQuery).processDefinitionKey(aProcDefKey);
    verify(mockedQuery).activityIdIn(aActId, anotherActId);
    verify(mockedQuery).jobType(aJobType);
    verify(mockedQuery).jobConfiguration(aJobConfig);
    verify(mockedQuery).active();
    verify(mockedQuery).suspended();
    verify(mockedQuery).withOverridingJobPriority();
    verify(mockedQuery).list();
  }

  @Test
  public void testQueryCount() {
    expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().get(JOB_DEFINITION_COUNT_QUERY_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testQueryCountAsPost() {
    given().contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().post(JOB_DEFINITION_COUNT_QUERY_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testTenantIdListParameter() {
    mockedQuery = setUpMockDefinitionQuery(createMockJobDefinitionsTwoTenants());

    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(JOB_DEFINITION_QUERY_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> jobs = from(content).getList("");
    assertThat(jobs).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  @Test
  public void testWithoutTenantIdParameter() {
    Response response = given()
      .queryParam("withoutTenantId", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(JOB_DEFINITION_QUERY_URL);

    verify(mockedQuery).withoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> jobs = from(content).getList("");
    assertThat(jobs).hasSize(1);

    String returnedTenantId = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId).isEqualTo(null);
  }

  @Test
  public void testIncludeJobDefinitionsWithoutTenantIdParameter() {
    List<JobDefinition> jobDefinitions = Arrays.asList(
        MockProvider.mockJobDefinition().tenantId(null).build(),
        MockProvider.mockJobDefinition().tenantId(MockProvider.EXAMPLE_TENANT_ID).build());
    mockedQuery = setUpMockDefinitionQuery(jobDefinitions);

    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID)
      .queryParam("includeJobDefinitionsWithoutTenantId", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(JOB_DEFINITION_QUERY_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID);
    verify(mockedQuery).includeJobDefinitionsWithoutTenantId();
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
  public void testTenantIdListPostParameter() {
    mockedQuery = setUpMockDefinitionQuery(createMockJobDefinitionsTwoTenants());

    Map<String, Object> queryParameters = new HashMap<String, Object>();
    queryParameters.put("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST.split(","));

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(JOB_DEFINITION_QUERY_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> jobs = from(content).getList("");
    assertThat(jobs).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  @Test
  public void testWithoutTenantIdPostParameter() {
    Map<String, Object> queryParameters = new HashMap<String, Object>();
    queryParameters.put("withoutTenantId", true);

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(JOB_DEFINITION_QUERY_URL);

    verify(mockedQuery).withoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> jobs = from(content).getList("");
    assertThat(jobs).hasSize(1);

    String returnedTenantId = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId).isEqualTo(null);
  }

  @Test
  public void testIncludeJobDefinitionsWithoutTenantIdPostParameter() {
    List<JobDefinition> jobDefinitions = Arrays.asList(
        MockProvider.mockJobDefinition().tenantId(null).build(),
        MockProvider.mockJobDefinition().tenantId(MockProvider.EXAMPLE_TENANT_ID).build());
    mockedQuery = setUpMockDefinitionQuery(jobDefinitions);

    Map<String, Object> queryParameters = new HashMap<String, Object>();
    queryParameters.put("tenantIdIn", new String[] { MockProvider.EXAMPLE_TENANT_ID });
    queryParameters.put("includeJobDefinitionsWithoutTenantId", true);

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(JOB_DEFINITION_QUERY_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID);
    verify(mockedQuery).includeJobDefinitionsWithoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(null);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
  }

  private List<JobDefinition> createMockJobDefinitionsTwoTenants() {
    return Arrays.asList(
        MockProvider.mockJobDefinition().tenantId(MockProvider.EXAMPLE_TENANT_ID).build(),
        MockProvider.mockJobDefinition().tenantId(MockProvider.ANOTHER_EXAMPLE_TENANT_ID).build());
  }

}
