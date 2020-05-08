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
import static org.camunda.bpm.engine.rest.util.DateTimeUtils.withTimezone;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.OrderingBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class JobRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String JOBS_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/job";
  protected static final String JOBS_QUERY_COUNT_URL = JOBS_RESOURCE_URL + "/count";

  private JobQuery mockQuery;
  private static final int MAX_RESULTS_TEN = 10;
  private static final int FIRST_RESULTS_ZERO = 0;
  protected static final long JOB_QUERY_MAX_PRIORITY = Long.MAX_VALUE;
  protected static final long JOB_QUERY_MIN_PRIORITY = Long.MIN_VALUE;

  @Before
  public void setUpRuntimeData() {
    mockQuery = setUpMockJobQuery(MockProvider.createMockJobs());
  }

  private JobQuery setUpMockJobQuery(List<Job> mockedJobs) {
    JobQuery sampleJobQuery = mock(JobQuery.class);

    when(sampleJobQuery.list()).thenReturn(mockedJobs);
    when(sampleJobQuery.count()).thenReturn((long) mockedJobs.size());

    when(processEngine.getManagementService().createJobQuery()).thenReturn(sampleJobQuery);

    return sampleJobQuery;
  }

  @Test
  public void testEmptyQuery() {
    String queryJobId = "";
    given().queryParam("id", queryJobId).then().expect()
        .statusCode(Status.OK.getStatusCode())
        .when().get(JOBS_RESOURCE_URL);
  }

  @Test
  public void testNoParametersQuery() {
    expect().statusCode(Status.OK.getStatusCode())
    .when().get(JOBS_RESOURCE_URL);

    verify(mockQuery).list();
    verifyNoMoreInteractions(mockQuery);
  }

  @Test
  public void testSortByParameterOnly() {
    given().queryParam("sortBy", "jobDueDate")
        .then()
        .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type",
            equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message",
            equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
        .when().get(JOBS_RESOURCE_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given().queryParam("sortOrder", "asc")
        .then()
        .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type",
            equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message",
            equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
        .when().get(JOBS_RESOURCE_URL);
  }

  @Test
  public void testSimpleJobQuery() {
    String jobId = MockProvider.EXAMPLE_JOB_ID;

    Response response = given().queryParam("jobId", jobId).then().expect()
        .statusCode(Status.OK.getStatusCode()).when()
        .get(JOBS_RESOURCE_URL);

    InOrder inOrder = inOrder(mockQuery);
    inOrder.verify(mockQuery).jobId(jobId);
    inOrder.verify(mockQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one job returned.", 1, instances.size());
    Assert.assertNotNull("The returned job should not be null.", instances.get(0));

    String returnedJobId = from(content).getString("[0].id");
    String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedProcessDefinitionKey = from(content).getString("[0].processDefinitionKey");
    String returnedExecutionId = from(content).getString("[0].executionId");
    String returnedExceptionMessage = from(content).getString("[0].exceptionMessage");
    String returnedFailedActivityId = from(content).getString("[0].failedActivityId");
    int returnedRetries = from(content).getInt("[0].retries");
    Date returnedDueDate = DateTimeUtil.parseDate(from(content).getString("[0].dueDate"));
    boolean returnedSuspended = from(content).getBoolean("[0].suspended");
    long returnedPriority = from(content).getLong("[0].priority");
    String returnedJobDefinitionId= from(content).getString("[0].jobDefinitionId");
    String returnedTenantId = from(content).getString("[0].tenantId");
    String returnedCreateTime = from(content).getString("[0].createTime");

    Assert.assertEquals(MockProvider.EXAMPLE_JOB_ID, returnedJobId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, returnedProcessDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, returnedProcessDefinitionKey);
    Assert.assertEquals(MockProvider.EXAMPLE_EXECUTION_ID, returnedExecutionId);
    Assert.assertEquals(MockProvider.EXAMPLE_JOB_NO_EXCEPTION_MESSAGE, returnedExceptionMessage);
    Assert.assertEquals(MockProvider.EXAMPLE_JOB_FAILED_ACTIVITY_ID, returnedFailedActivityId);
    Assert.assertEquals(MockProvider.EXAMPLE_JOB_RETRIES, returnedRetries);
    Assert.assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_DUE_DATE), returnedDueDate);
    Assert.assertEquals(MockProvider.EXAMPLE_JOB_IS_SUSPENDED, returnedSuspended);
    Assert.assertEquals(MockProvider.EXAMPLE_JOB_PRIORITY, returnedPriority);
    Assert.assertEquals(MockProvider.EXAMPLE_JOB_DEFINITION_ID, returnedJobDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_TENANT_ID, returnedTenantId);
    Assert.assertEquals(MockProvider.EXAMPLE_JOB_CREATE_TIME, returnedCreateTime);
  }

  private interface DateParameters {
    String name();

    String description();

    void expectLowerThan(JobQuery query, Date date);

    void expectHigherThan(JobQuery query, Date date);
  }

  private static final DateParameters DUE_DATES = new DateParameters() {
    @Override
    public String name() {
      return "dueDates";
    }

    @Override
    public String description() {
      return "due date";
    }

    @Override
    public void expectLowerThan(JobQuery query, Date date) {
      query.duedateLowerThan(date);
    }

    @Override
    public void expectHigherThan(JobQuery query, Date date) {
      query.duedateHigherThan(date);
    }
  };

  private static final DateParameters CREATE_TIMES = new DateParameters() {
    @Override
    public String name() {
      return "createTimes";
    }

    @Override
    public String description() {
      return "create time";
    }

    @Override
    public void expectLowerThan(JobQuery query, Date date) {
      query.createdBefore(date);
    }

    @Override
    public void expectHigherThan(JobQuery query, Date date) {
      query.createdAfter(date);
    }
  };

  private void testInvalidDateComparator(DateParameters parameters) {

    String variableValue = withTimezone("2013-05-05T00:00:00");
    String invalidComparator = "bt";

    String queryValue = invalidComparator + "_" + variableValue;
    given().queryParam(parameters.name(), queryValue).then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Invalid " + parameters.description() + " comparator specified: " + invalidComparator)).when().get(JOBS_RESOURCE_URL);
  }

  private void testInvalidDateComparatorAsPost(DateParameters parameters) {
    String invalidComparator = "bt";

    Map<String, Object> conditionJson = new HashMap<>();
    conditionJson.put("operator", invalidComparator);
    conditionJson.put("value", withTimezone("2013-05-05T00:00:00"));

    List<Map<String, Object>> conditions = new ArrayList<>();
    conditions.add(conditionJson);

    Map<String, Object> json = new HashMap<>();
    json.put(parameters.name(), conditions);

    given().contentType(POST_JSON_CONTENT_TYPE).body(json).then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Invalid " + parameters.description() + " comparator specified: " + invalidComparator)).when().post(JOBS_RESOURCE_URL);
  }

  private void testInvalidDate(DateParameters parameters) {
    String variableValue = "invalidValue";
    String invalidComparator = "lt";

    String queryValue = invalidComparator + "_" + variableValue;
    given().queryParam(parameters.name(), queryValue).then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Invalid " + parameters.description() + " format: Cannot convert value \"invalidValue\" to java type java.util.Date")).when()
        .get(JOBS_RESOURCE_URL);
  }

  private void testInvalidDateAsPost(DateParameters parameters) {
    Map<String, Object> conditionJson = new HashMap<>();
    conditionJson.put("operator", "lt");
    conditionJson.put("value", "invalidValue");

    List<Map<String, Object>> conditions = new ArrayList<>();
    conditions.add(conditionJson);

    Map<String, Object> json = new HashMap<>();
    json.put(parameters.name(), conditions);

    given().contentType(POST_JSON_CONTENT_TYPE).body(json).then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Invalid " + parameters.description() + " format: Cannot convert value \"invalidValue\" to java type java.util.Date")).when()
        .post(JOBS_RESOURCE_URL);
  }

  @Test
  public void testInvalidDueDateComparator() {
    testInvalidDateComparator(DUE_DATES);
  }

  @Test
  public void testInvalidCreateTimeComparator() {
    testInvalidDateComparator(CREATE_TIMES);
  }

  @Test
  public void testInvalidDueDateComperatorAsPost() {
    testInvalidDateComparatorAsPost(DUE_DATES);
  }

  @Test
  public void testInvalidCreateTimeComparatorAsPost() {
    testInvalidDateComparatorAsPost(CREATE_TIMES);
  }

  @Test
  public void testInvalidDueDate() {
    testInvalidDate(DUE_DATES);
  }

  @Test
  public void testInvalidCreateTime() {
    testInvalidDate(CREATE_TIMES);
  }

  @Test
  public void testInvalidDueDateAsPost() {
    testInvalidDateAsPost(DUE_DATES);
  }

  @Test
  public void testInvalidCreateTimeAsPost() {
    testInvalidDateAsPost(CREATE_TIMES);
  }

  @Test
  public void testAdditionalParametersExcludingDueDates() {
    Map<String, Object> parameters = getCompleteParameters();

    given().queryParams(parameters).then().expect()
        .statusCode(Status.OK.getStatusCode()).when()
        .get(JOBS_RESOURCE_URL);

    verifyParameterQueryInvocations();
    verify(mockQuery).list();
  }

  @Test
  public void testMessagesParameter() {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("messages", MockProvider.EXAMPLE_MESSAGES);

    given().queryParams(parameters)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(JOBS_RESOURCE_URL);

    verify(mockQuery).messages();
    verify(mockQuery).list();
  }

  @Test
  public void testMessagesTimersParameter() {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("messages", MockProvider.EXAMPLE_MESSAGES);
    parameters.put("timers", MockProvider.EXAMPLE_TIMERS);

    given().queryParams(parameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .contentType(ContentType.JSON)
    .body("type",equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Parameter timers cannot be used together with parameter messages."))
    .when().get(JOBS_RESOURCE_URL);
  }

  @Test
  public void testMessagesTimersParameterAsPost() {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("messages", MockProvider.EXAMPLE_MESSAGES);
    parameters.put("timers", MockProvider.EXAMPLE_TIMERS);

    given().contentType(POST_JSON_CONTENT_TYPE).body(parameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .contentType(ContentType.JSON)
    .body("type",equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Parameter timers cannot be used together with parameter messages."))
    .when().post(JOBS_RESOURCE_URL);
  }

  @Test
  public void testMessagesParameterAsPost() {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("messages", MockProvider.EXAMPLE_MESSAGES);

    given().contentType(POST_JSON_CONTENT_TYPE).body(parameters)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().post(JOBS_RESOURCE_URL);

    verify(mockQuery).messages();
    verify(mockQuery).list();
  }

  private Map<String, Object> getCompleteParameters() {
    Map<String, Object> parameters = new HashMap<>();

    parameters.put("activityId", MockProvider.EXAMPLE_ACTIVITY_ID);
    parameters.put("jobId", MockProvider.EXAMPLE_JOB_ID);
    parameters.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    parameters.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    parameters.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    parameters.put("executionId", MockProvider.EXAMPLE_EXECUTION_ID);
    parameters.put("withRetriesLeft", MockProvider.EXAMPLE_WITH_RETRIES_LEFT);
    parameters.put("executable", MockProvider.EXAMPLE_EXECUTABLE);
    parameters.put("timers", MockProvider.EXAMPLE_TIMERS);
    parameters.put("withException", MockProvider.EXAMPLE_WITH_EXCEPTION);
    parameters.put("exceptionMessage", MockProvider.EXAMPLE_EXCEPTION_MESSAGE);
    parameters.put("failedActivityId", MockProvider.EXAMPLE_JOB_FAILED_ACTIVITY_ID);
    parameters.put("noRetriesLeft", MockProvider.EXAMPLE_NO_RETRIES_LEFT);
    parameters.put("active", true);
    parameters.put("suspended", true);
    parameters.put("priorityLowerThanOrEquals", JOB_QUERY_MAX_PRIORITY);
    parameters.put("priorityHigherThanOrEquals", JOB_QUERY_MIN_PRIORITY);
    parameters.put("jobDefinitionId", MockProvider.EXAMPLE_JOB_DEFINITION_ID);
    return parameters;
  }

  @Test
  public void testAdditionalParametersExcludingDueDatesAsPost() {
    Map<String, Object> parameters = getCompleteParameters();

    given().contentType(POST_JSON_CONTENT_TYPE).body(parameters)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().post(JOBS_RESOURCE_URL);

    verifyParameterQueryInvocations();
    verify(mockQuery).list();
  }


  private void verifyParameterQueryInvocations() {
    Map<String, Object> parameters = getCompleteParameters();

    verify(mockQuery).jobId((String) parameters.get("jobId"));
    verify(mockQuery).processInstanceId((String) parameters.get("processInstanceId"));
    verify(mockQuery).processDefinitionId((String) parameters.get("processDefinitionId"));
    verify(mockQuery).processDefinitionKey((String) parameters.get("processDefinitionKey"));
    verify(mockQuery).executionId((String) parameters.get("executionId"));
    verify(mockQuery).activityId((String) parameters.get("activityId"));
    verify(mockQuery).withRetriesLeft();
    verify(mockQuery).executable();
    verify(mockQuery).timers();
    verify(mockQuery).withException();
    verify(mockQuery).exceptionMessage((String) parameters.get("exceptionMessage"));
    verify(mockQuery).failedActivityId((String) parameters.get("failedActivityId"));
    verify(mockQuery).noRetriesLeft();
    verify(mockQuery).active();
    verify(mockQuery).suspended();
    verify(mockQuery).priorityLowerThanOrEquals(JOB_QUERY_MAX_PRIORITY);
    verify(mockQuery).priorityHigherThanOrEquals(JOB_QUERY_MIN_PRIORITY);
    verify(mockQuery).jobDefinitionId(MockProvider.EXAMPLE_JOB_DEFINITION_ID);
  }

  private void testDateParameters(DateParameters parameters) {
    String variableValue = withTimezone("2013-05-05T00:00:00");
    Date date = DateTimeUtil.parseDate(variableValue);

    String queryValue = "lt_" + variableValue;
    given().queryParam(parameters.name(), queryValue).then().expect().statusCode(Status.OK.getStatusCode()).when().get(JOBS_RESOURCE_URL);

    InOrder inOrder = inOrder(mockQuery);
    parameters.expectLowerThan(inOrder.verify(mockQuery), date);
    inOrder.verify(mockQuery).list();

    queryValue = "gt_" + variableValue;
    given().queryParam(parameters.name(), queryValue).then().expect().statusCode(Status.OK.getStatusCode()).when().get(JOBS_RESOURCE_URL);

    inOrder = inOrder(mockQuery);
    parameters.expectHigherThan(inOrder.verify(mockQuery), date);
    inOrder.verify(mockQuery).list();
  }

  private void testDateParametersAsPost(DateParameters parameters) {
    String value = withTimezone("2013-05-18T00:00:00");
    String anotherValue = withTimezone("2013-05-05T00:00:00");

    Date date = DateTimeUtil.parseDate(value);
    Date anotherDate = DateTimeUtil.parseDate(anotherValue);

    Map<String, Object> conditionJson = new HashMap<>();
    conditionJson.put("operator", "lt");
    conditionJson.put("value", value);

    Map<String, Object> anotherConditionJson = new HashMap<>();
    anotherConditionJson.put("operator", "gt");
    anotherConditionJson.put("value", anotherValue);

    List<Map<String, Object>> conditions = new ArrayList<>();
    conditions.add(conditionJson);
    conditions.add(anotherConditionJson);

    Map<String, Object> json = new HashMap<>();
    json.put(parameters.name(), conditions);

    given().contentType(POST_JSON_CONTENT_TYPE).body(json).then().expect().statusCode(Status.OK.getStatusCode()).when().post(JOBS_RESOURCE_URL);

    parameters.expectHigherThan(verify(mockQuery), anotherDate);
    parameters.expectLowerThan(verify(mockQuery), date);
  }

  private void testMultipleDateParameters(DateParameters parameters) {
    String variableValue1 = withTimezone("2012-05-05T00:00:00");
    String variableParameter1 = "gt_" + variableValue1;

    String variableValue2 = withTimezone("2013-02-02T00:00:00");
    String variableParameter2 = "lt_" + variableValue2;

    Date date = DateTimeUtil.parseDate(variableValue1);
    Date anotherDate = DateTimeUtil.parseDate(variableValue2);

    String queryValue = variableParameter1 + "," + variableParameter2;

    given().queryParam(parameters.name(), queryValue).then().expect().statusCode(Status.OK.getStatusCode()).when().get(JOBS_RESOURCE_URL);

    parameters.expectHigherThan(verify(mockQuery), date);
    parameters.expectLowerThan(verify(mockQuery), anotherDate);
  }

  @Test
  public void testDueDateParameters() {
    testDateParameters(DUE_DATES);
  }

  @Test
  public void testCreateTimeParameters() {
    testDateParameters(CREATE_TIMES);
  }

  @Test
  public void testDueDateParametersAsPost() {
    testDateParametersAsPost(DUE_DATES);
  }

  @Test
  public void testCreateTimeParametersAsPost() {
    testDateParametersAsPost(CREATE_TIMES);
  }

  @Test
  public void testMultipleDueDateParameters() {
    testMultipleDateParameters(DUE_DATES);
  }

  @Test
  public void testMultipleCreateTimeParameters() {
    testMultipleDateParameters(CREATE_TIMES);
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("jobId", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByJobId();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("processInstanceId", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByProcessInstanceId();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("executionId", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByExecutionId();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("jobRetries", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByJobRetries();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("jobDueDate", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByJobDuedate();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("jobPriority", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByJobPriority();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("tenantId", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByTenantId();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("tenantId", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByTenantId();
    inOrder.verify(mockQuery).desc();
  }

  private void executeAndVerifySorting(String sortBy, String sortOrder,
      Status expectedStatus) {
    given().queryParam("sortBy", sortBy).queryParam("sortOrder", sortOrder)
        .then().expect().statusCode(expectedStatus.getStatusCode())
        .when().get(JOBS_RESOURCE_URL);
  }

  @Test
  public void testSecondarySortingAsPost() {
    InOrder inOrder = Mockito.inOrder(mockQuery);
    Map<String, Object> json = new HashMap<>();
    json.put("sorting", OrderingBuilder.create()
      .orderBy("jobRetries").desc()
      .orderBy("jobDueDate").asc()
      .getJson());
    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(JOBS_RESOURCE_URL);

    inOrder.verify(mockQuery).orderByJobRetries();
    inOrder.verify(mockQuery).desc();
    inOrder.verify(mockQuery).orderByJobDuedate();
    inOrder.verify(mockQuery).asc();
  }

  @Test
  public void testSuccessfulPagination() {

    int firstResult = FIRST_RESULTS_ZERO;
    int maxResults = MAX_RESULTS_TEN;
    given().queryParam("firstResult", firstResult)
        .queryParam("maxResults", maxResults).then().expect()
        .statusCode(Status.OK.getStatusCode()).when()
        .get(JOBS_RESOURCE_URL);

    verify(mockQuery).listPage(firstResult, maxResults);
  }

  @Test
  public void testQueryCount() {
    given()
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when()
        .get(JOBS_QUERY_COUNT_URL);

    verify(mockQuery).count();
  }

  @Test
  public void testQueryCountForPost() {
    given().contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .header("accept", MediaType.APPLICATION_JSON)
    .expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().post(JOBS_QUERY_COUNT_URL);

    verify(mockQuery).count();
  }

  @Test
  public void testTenantIdListParameter() {
    mockQuery = setUpMockJobQuery(createMockJobsTwoTenants());

    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(JOBS_RESOURCE_URL);

    verify(mockQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockQuery).list();

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
    Job mockJob = MockProvider.mockJob().tenantId(null).build();
    mockQuery = setUpMockJobQuery(Arrays.asList(mockJob));

    Response response = given()
      .queryParam("withoutTenantId", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(JOBS_RESOURCE_URL);

    verify(mockQuery).withoutTenantId();
    verify(mockQuery).list();

    String content = response.asString();
    List<String> jobs = from(content).getList("");
    assertThat(jobs).hasSize(1);

    String returnedTenantId = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId).isEqualTo(null);
  }

  @Test
  public void testIncludeJobsWithoutTenantIdParameter() {
    List<Job> jobs = Arrays.asList(
        MockProvider.mockJob().tenantId(null).build(),
        MockProvider.mockJob().tenantId(MockProvider.EXAMPLE_TENANT_ID).build());
    mockQuery = setUpMockJobQuery(jobs);

    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID)
      .queryParam("includeJobsWithoutTenantId", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(JOBS_RESOURCE_URL);

    verify(mockQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID);
    verify(mockQuery).includeJobsWithoutTenantId();
    verify(mockQuery).list();

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
    mockQuery = setUpMockJobQuery(createMockJobsTwoTenants());

    Map<String, Object> queryParameters = new HashMap<>();
    queryParameters.put("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST.split(","));

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(JOBS_RESOURCE_URL);

    verify(mockQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockQuery).list();

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
    Job mockJob = MockProvider.mockJob().tenantId(null).build();
    mockQuery = setUpMockJobQuery(Arrays.asList(mockJob));

    Map<String, Object> queryParameters = new HashMap<>();
    queryParameters.put("withoutTenantId", true);

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(JOBS_RESOURCE_URL);

    String content = response.asString();
    List<String> jobs = from(content).getList("");
    assertThat(jobs).hasSize(1);

    String returnedTenantId = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId).isEqualTo(null);
  }

  @Test
  public void testIncludeJobsWithoutTenantIdPostParameter() {
    List<Job> jobs = Arrays.asList(
        MockProvider.mockJob().tenantId(null).build(),
        MockProvider.mockJob().tenantId(MockProvider.EXAMPLE_TENANT_ID).build());
    mockQuery = setUpMockJobQuery(jobs);

    Map<String, Object> queryParameters = new HashMap<>();
    queryParameters.put("tenantIdIn", new String[] { MockProvider.EXAMPLE_TENANT_ID });
    queryParameters.put("includeJobsWithoutTenantId", true);

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(JOBS_RESOURCE_URL);

    verify(mockQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID);
    verify(mockQuery).includeJobsWithoutTenantId();
    verify(mockQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(null);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
  }

  private List<Job> createMockJobsTwoTenants() {
    return Arrays.asList(
        MockProvider.mockJob().tenantId(MockProvider.EXAMPLE_TENANT_ID).build(),
        MockProvider.mockJob().tenantId(MockProvider.ANOTHER_EXAMPLE_TENANT_ID).build());
  }

  @Test
  public void testProcessInstanceIdListParameter() {
    mockQuery = setUpMockJobQuery(createMockJobsTwoProcessInstances());

    Response response = given()
      .queryParam("processInstanceIds", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(JOBS_RESOURCE_URL);

    Set<String> expectedIds = new HashSet<>();
    Collections.addAll(expectedIds, MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID);
    verify(mockQuery).processInstanceIds(expectedIds);
    verify(mockQuery).list();

    String content = response.asString();
    List<String> jobs = from(content).getList("");
    assertThat(jobs).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].processInstanceId");
    String returnedTenantId2 = from(content).getString("[1].processInstanceId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID);
  }

  @Test
  public void testProcessInstanceIdListPostParameter() {
    mockQuery = setUpMockJobQuery(createMockJobsTwoProcessInstances());

    Map<String, Object> queryParameters = new HashMap<>();
    queryParameters.put("processInstanceIds", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID_LIST.split(","));

    Response response = given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(JOBS_RESOURCE_URL);

    Set<String> expectedIds = new HashSet<>();
    Collections.addAll(expectedIds, MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID);
    verify(mockQuery).processInstanceIds(expectedIds);
    verify(mockQuery).list();

    String content = response.asString();
    List<String> jobs = from(content).getList("");
    assertThat(jobs).hasSize(2);

    String returnedProcessInstaneId1 = from(content).getString("[0].processInstanceId");
    String returnedProcessInstaneId2 = from(content).getString("[1].processInstanceId");

    assertThat(returnedProcessInstaneId1).isEqualTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    assertThat(returnedProcessInstaneId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID);
  }

  private List<Job> createMockJobsTwoProcessInstances() {
    return Arrays.asList(
        MockProvider.mockJob().processInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).build(),
        MockProvider.mockJob().processInstanceId(MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID).build());
  }

  @Test
  public void testJobIdListParameter() {
    mockQuery = setUpMockJobQuery(createMockJobsTwoJobIds());

    Response response = given()
      .queryParam("jobIds", MockProvider.EXAMPLE_JOB_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(JOBS_RESOURCE_URL);

    Set<String> expectedIds = new HashSet<>();
    Collections.addAll(expectedIds, MockProvider.EXAMPLE_JOB_ID, MockProvider.ANOTHER_EXAMPLE_JOB_ID);
    verify(mockQuery).jobIds(expectedIds);
    verify(mockQuery).list();

    String content = response.asString();
    List<String> jobs = from(content).getList("");
    assertThat(jobs).hasSize(2);

    String returnedJobId1 = from(content).getString("[0].id");
    String returnedJobId2 = from(content).getString("[1].id");

    assertThat(returnedJobId1).isEqualTo(MockProvider.EXAMPLE_JOB_ID);
    assertThat(returnedJobId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_JOB_ID);
  }

  @Test
  public void testJobIdListPostParameter() {
    mockQuery = setUpMockJobQuery(createMockJobsTwoJobIds());

    Map<String, Object> queryParameters = new HashMap<>();
    queryParameters.put("jobIds", MockProvider.EXAMPLE_JOB_ID_LIST.split(","));

    Response response = given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(JOBS_RESOURCE_URL);

    Set<String> expectedIds = new HashSet<>();
    Collections.addAll(expectedIds, MockProvider.EXAMPLE_JOB_ID, MockProvider.ANOTHER_EXAMPLE_JOB_ID);
    verify(mockQuery).jobIds(expectedIds);
    verify(mockQuery).list();

    String content = response.asString();
    List<String> jobs = from(content).getList("");
    assertThat(jobs).hasSize(2);

    String returnedJobId1 = from(content).getString("[0].id");
    String returnedJobId2 = from(content).getString("[1].id");

    assertThat(returnedJobId1).isEqualTo(MockProvider.EXAMPLE_JOB_ID);
    assertThat(returnedJobId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_JOB_ID);
  }

  private List<Job> createMockJobsTwoJobIds() {
    return Arrays.asList(
        MockProvider.mockJob().build(),
        MockProvider.mockJob().id(MockProvider.ANOTHER_EXAMPLE_JOB_ID).build());
  }
}
