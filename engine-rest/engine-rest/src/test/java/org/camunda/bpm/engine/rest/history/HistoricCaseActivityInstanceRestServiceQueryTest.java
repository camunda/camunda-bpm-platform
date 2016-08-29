package org.camunda.bpm.engine.rest.history;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstanceQuery;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import javax.ws.rs.core.Response.Status;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public class HistoricCaseActivityInstanceRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/case-activity-instance";

  protected static final String HISTORIC_CASE_ACTIVITY_INSTANCE_COUNT_RESOURCE_URL = HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL + "/count";

  protected HistoricCaseActivityInstanceQuery mockedQuery;

  @Before
  public void setUpRuntimeData() {
    mockedQuery = setUpMockHistoricCaseActivityInstanceQuery(MockProvider.createMockHistoricCaseActivityInstances());
  }

  protected HistoricCaseActivityInstanceQuery setUpMockHistoricCaseActivityInstanceQuery(List<HistoricCaseActivityInstance> mockedHistoricCaseActivityInstances) {
    HistoricCaseActivityInstanceQuery mockedHistoricCaseActivityInstanceQuery = mock(HistoricCaseActivityInstanceQuery.class);
    when(mockedHistoricCaseActivityInstanceQuery.list()).thenReturn(mockedHistoricCaseActivityInstances);
    when(mockedHistoricCaseActivityInstanceQuery.count()).thenReturn((long) mockedHistoricCaseActivityInstances.size());

    when(processEngine.getHistoryService().createHistoricCaseActivityInstanceQuery()).thenReturn(mockedHistoricCaseActivityInstanceQuery);

    return mockedHistoricCaseActivityInstanceQuery;
  }

  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given()
      .queryParam("caseInstanceId", queryKey)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testNoParametersQuery() {
    expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("instanceId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then()
      .expect()
        .statusCode(expectedStatus.getStatusCode())
      .when()
        .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testSortByParameterOnly() {
    given()
      .queryParam("sortBy", "caseInstanceId")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given()
    .queryParam("sortOrder", "asc")
  .then().expect()
    .statusCode(Status.BAD_REQUEST.getStatusCode())
    .contentType(ContentType.JSON)
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
  .when()
    .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseActivityInstanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricCaseActivityInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseActivityInstanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricCaseActivityInstanceId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseInstanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseInstanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseExecutionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseExecutionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseExecutionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseExecutionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseActivityId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseActivityId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseActivityId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseActivityId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseActivityName", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseActivityName();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseActivityName", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseActivityName();
    inOrder.verify(mockedQuery).desc();

    executeAndVerifySorting("caseActivityType", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseActivityType();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseActivityType", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseActivityType();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("createTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricCaseActivityInstanceCreateTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("createTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricCaseActivityInstanceCreateTime();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("endTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricCaseActivityInstanceEndTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("endTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricCaseActivityInstanceEndTime();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("duration", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricCaseActivityInstanceDuration();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("duration", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricCaseActivityInstanceDuration();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseDefinitionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseDefinitionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseDefinitionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseDefinitionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("tenantId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTenantId();
    inOrder.verify(mockedQuery).asc();

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
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);
  }

  @Test
  public void testMissingFirstResultParameter() {
    int maxResults = 10;

    given()
      .queryParam("maxResults", maxResults)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(0, maxResults);
  }

  @Test
  public void testMissingMaxResultsParameter() {
    int firstResult = 10;

    given()
      .queryParam("firstResult", firstResult)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(HISTORIC_CASE_ACTIVITY_INSTANCE_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testSimpleHistoricActivityQuery() {
    String caseInstanceId = MockProvider.EXAMPLE_CASE_INSTANCE_ID;

    Response response = given()
        .queryParam("caseInstanceId", caseInstanceId)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).caseInstanceId(caseInstanceId);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals(1, instances.size());
    Assert.assertNotNull(instances.get(0));

    String returnedId = from(content).getString("[0].id");
    String returnedParentCaseActivityInstanceId = from(content).getString("[0].parentCaseActivityInstanceId");
    String returnedCaseActivityId = from(content).getString("[0].caseActivityId");
    String returnedCaseActivityName = from(content).getString("[0].caseActivityName");
    String returnedCaseActivityType = from(content).getString("[0].caseActivityType");
    String returnedCaseDefinitionId = from(content).getString("[0].caseDefinitionId");
    String returnedCaseInstanceId = from(content).getString("[0].caseInstanceId");
    String returnedCaseExecutionId = from(content).getString("[0].caseExecutionId");
    String returnedTaskId = from(content).getString("[0].taskId");
    String returnedCalledProcessInstanceId = from(content).getString("[0].calledProcessInstanceId");
    String returnedCalledCaseInstanceId = from(content).getString("[0].calledCaseInstanceId");
    String returnedTenantId = from(content).getString("[0].tenantId");
    Date returnedCreateTime = DateTimeUtil.parseDate(from(content).getString("[0].createTime"));
    Date returnedEndTime = DateTimeUtil.parseDate(from(content).getString("[0].endTime"));
    long returnedDurationInMillis = from(content).getLong("[0].durationInMillis");
    boolean required = from(content).getBoolean("[0].required");
    boolean available = from(content).getBoolean("[0].available");
    boolean enabled = from(content).getBoolean("[0].enabled");
    boolean disabled = from(content).getBoolean("[0].disabled");
    boolean active = from(content).getBoolean("[0].active");
    boolean completed = from(content).getBoolean("[0].completed");
    boolean terminated = from(content).getBoolean("[0].terminated");

    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_ID, returnedId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_PARENT_CASE_ACTIVITY_INSTANCE_ID, returnedParentCaseActivityInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_ID, returnedCaseActivityId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_NAME, returnedCaseActivityName);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_TYPE, returnedCaseActivityType);
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_ID, returnedCaseDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_INSTANCE_ID, returnedCaseInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_EXECUTION_ID, returnedCaseExecutionId);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_ID, returnedTaskId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CALLED_PROCESS_INSTANCE_ID, returnedCalledProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CALLED_CASE_INSTANCE_ID, returnedCalledCaseInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_TENANT_ID, returnedTenantId);
    Assert.assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CREATE_TIME), returnedCreateTime);
    Assert.assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_END_TIME), returnedEndTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_DURATION, returnedDurationInMillis);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_REQUIRED, required);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_AVAILABLE, available);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_ENABLED, enabled);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_DISABLED, disabled);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_ACTIVE, active);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_COMPLETED, completed);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_TERMINATED, terminated);
  }

  @Test
  public void testAdditionalParameters() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    given()
      .queryParams(stringQueryParameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);

    verifyStringParameterQueryInvocations();
  }

  @Test
  public void testBooleanParameters() {
    Map<String, Boolean> params = getCompleteBooleanQueryParameters();

    given()
      .queryParams(params)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);

    verifyBooleanParameterQueryInvocations();
  }

  protected Map<String, String> getCompleteStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("caseActivityInstanceId", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_ID);
    parameters.put("caseInstanceId", MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    parameters.put("caseDefinitionId", MockProvider.EXAMPLE_CASE_DEFINITION_ID);
    parameters.put("caseExecutionId", MockProvider.EXAMPLE_CASE_EXECUTION_ID);
    parameters.put("caseActivityId", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_ID);
    parameters.put("caseActivityName", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_NAME);
    parameters.put("caseActivityType", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_TYPE);

    return parameters;
  }

  protected void verifyStringParameterQueryInvocations() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    verify(mockedQuery).caseActivityInstanceId(stringQueryParameters.get("caseActivityInstanceId"));
    verify(mockedQuery).caseInstanceId(stringQueryParameters.get("caseInstanceId"));
    verify(mockedQuery).caseDefinitionId(stringQueryParameters.get("caseDefinitionId"));
    verify(mockedQuery).caseExecutionId(stringQueryParameters.get("caseExecutionId"));
    verify(mockedQuery).caseActivityId(stringQueryParameters.get("caseActivityId"));
    verify(mockedQuery).caseActivityName(stringQueryParameters.get("caseActivityName"));
    verify(mockedQuery).caseActivityType(stringQueryParameters.get("caseActivityType"));

    verify(mockedQuery).list();
  }

  protected Map<String, Boolean> getCompleteBooleanQueryParameters() {
    Map<String, Boolean> parameters = new HashMap<String, Boolean>();

    parameters.put("required", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_REQUIRED);
    parameters.put("finished", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_FINISHED);
    parameters.put("unfinished", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_UNFINISHED);
    parameters.put("available", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_AVAILABLE);
    parameters.put("enabled", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_ENABLED);
    parameters.put("disabled", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_DISABLED);
    parameters.put("active", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_ACTIVE);
    parameters.put("completed", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_COMPLETED);
    parameters.put("terminated", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_IS_TERMINATED);

    return parameters;
  }

  protected void verifyBooleanParameterQueryInvocations() {
    Map<String, Boolean> booleanParams = getCompleteBooleanQueryParameters();
    Boolean required = booleanParams.get("required");
    Boolean finished = booleanParams.get("finished");
    Boolean unfinished = booleanParams.get("unfinished");
    Boolean available = booleanParams.get("available");
    Boolean enabled = booleanParams.get("enabled");
    Boolean disabled = booleanParams.get("disabled");
    Boolean active = booleanParams.get("active");
    Boolean completed = booleanParams.get("completed");
    Boolean terminated = booleanParams.get("terminated");

    if (required != null && required) {
      verify(mockedQuery).required();
    }
    if (finished != null && finished) {
      verify(mockedQuery).ended();
    }
    if (unfinished != null && unfinished) {
      verify(mockedQuery).notEnded();
    }
    if (available != null && available) {
      verify(mockedQuery).available();
    }
    if (enabled != null && enabled) {
      verify(mockedQuery).enabled();
    }
    if (disabled != null && disabled) {
      verify(mockedQuery).disabled();
    }
    if (active != null && active) {
      verify(mockedQuery).active();
    }
    if (completed != null && completed) {
      verify(mockedQuery).completed();
    }
    if (terminated != null && terminated) {
      verify(mockedQuery).terminated();
    }

    verify(mockedQuery).list();
  }

  @Test
  public void testFinishedHistoricCaseActivityQuery() {
    Response response = given()
        .queryParam("finished", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).ended();
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals(1, instances.size());
    Assert.assertNotNull(instances.get(0));

    String returnedCaseDefinitionId = from(content).getString("[0].caseDefinitionId");
    String returnedActivityEndTime = from(content).getString("[0].endTime");

    Assert.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_ID, returnedCaseDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_END_TIME, returnedActivityEndTime);
  }

  @Test
  public void testUnfinishedHistoricCaseActivityQuery() {
    List<HistoricCaseActivityInstance> mockedHistoricCaseActivityInstances = MockProvider.createMockRunningHistoricCaseActivityInstances();
    HistoricCaseActivityInstanceQuery mockedHistoricCaseActivityInstanceQuery = mock(HistoricCaseActivityInstanceQuery.class);
    when(mockedHistoricCaseActivityInstanceQuery.list()).thenReturn(mockedHistoricCaseActivityInstances);
    when(processEngine.getHistoryService().createHistoricCaseActivityInstanceQuery()).thenReturn(mockedHistoricCaseActivityInstanceQuery);

    Response response = given()
        .queryParam("unfinished", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedHistoricCaseActivityInstanceQuery);
    inOrder.verify(mockedHistoricCaseActivityInstanceQuery).notEnded();
    inOrder.verify(mockedHistoricCaseActivityInstanceQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals(1, instances.size());
    Assert.assertNotNull(instances.get(0));

    String returnedCaseDefinitionId = from(content).getString("[0].caseDefinitionId");
    String returnedActivityEndTime = from(content).getString("[0].endTime");

    Assert.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_ID, returnedCaseDefinitionId);
    Assert.assertNull(returnedActivityEndTime);
  }

  @Test
  public void testHistoricAfterAndBeforeCreateTimeQuery() {
    given()
      .queryParam("createdAfter", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CREATED_AFTER)
      .queryParam("createdBefore", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CREATED_BEFORE)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);

    verifyCreateParameterQueryInvocations();
  }

  protected Map<String, Date> getCompleteCreateDateQueryParameters() {
    Map<String, Date> parameters = new HashMap<String, Date>();

    parameters.put("createdAfter", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CREATED_AFTER));
    parameters.put("createdBefore", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_CREATED_BEFORE));

    return parameters;
  }

  protected void verifyCreateParameterQueryInvocations() {
    Map<String, Date> startDateParameters = getCompleteCreateDateQueryParameters();

    verify(mockedQuery).createdAfter(startDateParameters.get("createdAfter"));
    verify(mockedQuery).createdBefore(startDateParameters.get("createdBefore"));

    verify(mockedQuery).list();
  }

  @Test
  public void testHistoricAfterAndBeforeEndTimeQuery() {
    given()
      .queryParam("endedAfter", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_ENDED_AFTER)
      .queryParam("endedBefore", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_ENDED_BEFORE)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);

    verifyEndedParameterQueryInvocations();
  }

  protected Map<String, Date> getCompleteEndedDateQueryParameters() {
    Map<String, Date> parameters = new HashMap<String, Date>();

    parameters.put("endedAfter", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_ENDED_AFTER));
    parameters.put("endedBefore", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_ENDED_BEFORE));

    return parameters;
  }

  protected void verifyEndedParameterQueryInvocations() {
    Map<String, Date> finishedDateParameters = getCompleteEndedDateQueryParameters();

    verify(mockedQuery).endedAfter(finishedDateParameters.get("endedAfter"));
    verify(mockedQuery).endedBefore(finishedDateParameters.get("endedBefore"));

    verify(mockedQuery).list();
  }

  @Test
  public void testTenantIdListParameter() {
    mockedQuery = setUpMockHistoricCaseActivityInstanceQuery(createMockHistoricCaseActivityInstancesTwoTenants());

    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> historicCaseActivityInstances = from(content).getList("");
    assertThat(historicCaseActivityInstances).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  @Test
  public void testCaseActivityInstanceIdListParameter() {

    given()
      .queryParam("caseActivityInstanceIdIn", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_ID + "," + MockProvider.EXAMPLE_HISTORIC_ANOTHER_CASE_ACTIVITY_INSTANCE_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).caseActivityInstanceIdIn(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_INSTANCE_ID, MockProvider.EXAMPLE_HISTORIC_ANOTHER_CASE_ACTIVITY_INSTANCE_ID);
    verify(mockedQuery).list();
  }

  @Test
  public void testCaseActivityIdListParameter() {

    given()
      .queryParam("caseActivityIdIn", MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_ID + "," + MockProvider.EXAMPLE_HISTORIC_ANOTHER_CASE_ACTIVITY_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_ACTIVITY_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).caseActivityIdIn(MockProvider.EXAMPLE_HISTORIC_CASE_ACTIVITY_ID, MockProvider.EXAMPLE_HISTORIC_ANOTHER_CASE_ACTIVITY_ID);
    verify(mockedQuery).list();
  }

  private List<HistoricCaseActivityInstance> createMockHistoricCaseActivityInstancesTwoTenants() {
    return Arrays.asList(
        MockProvider.createMockHistoricCaseActivityInstance(MockProvider.EXAMPLE_TENANT_ID),
        MockProvider.createMockHistoricCaseActivityInstance(MockProvider.ANOTHER_EXAMPLE_TENANT_ID));
  }

}
