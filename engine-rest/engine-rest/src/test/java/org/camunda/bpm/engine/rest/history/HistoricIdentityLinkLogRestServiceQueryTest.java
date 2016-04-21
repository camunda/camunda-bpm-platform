package org.camunda.bpm.engine.rest.history;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLogQuery;
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

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import static com.jayway.restassured.RestAssured.expect;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 *
 * @author Deivarayan Azhagappan
 *
 */
public class HistoricIdentityLinkLogRestServiceQueryTest extends AbstractRestServiceTest {
  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORY_IDENTITY_LINK_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/history/identity-link-log";
  protected static final String HISTORY_IDENTITY_LINK_COUNT_QUERY_URL = HISTORY_IDENTITY_LINK_QUERY_URL + "/count";

  private HistoricIdentityLinkLogQuery mockedQuery;

  @Before
  public void setUpRuntimeData() {
    mockedQuery = setUpMockHistoricIdentityLinkQuery(MockProvider.createMockHistoricIdentityLinks());
  }

  private HistoricIdentityLinkLogQuery setUpMockHistoricIdentityLinkQuery(List<HistoricIdentityLinkLog> mockedHistoricIdentityLinks) {

    HistoricIdentityLinkLogQuery mockedHistoricIdentityLinkQuery = mock(HistoricIdentityLinkLogQuery.class);
    when(mockedHistoricIdentityLinkQuery.list()).thenReturn(mockedHistoricIdentityLinks);
    when(mockedHistoricIdentityLinkQuery.count()).thenReturn((long) mockedHistoricIdentityLinks.size());

    when(processEngine.getHistoryService().createHistoricIdentityLinkLogQuery()).thenReturn(mockedHistoricIdentityLinkQuery);

    return mockedHistoricIdentityLinkQuery;
  }

  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given().queryParam("userId", queryKey).then().expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORY_IDENTITY_LINK_QUERY_URL);
  }

  @Test
  public void testNoParametersQuery() {
    expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORY_IDENTITY_LINK_QUERY_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("processInstanceId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given().queryParam("sortBy", sortBy).queryParam("sortOrder", sortOrder).then().expect().statusCode(expectedStatus.getStatusCode()).when()
        .get(HISTORY_IDENTITY_LINK_QUERY_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given().queryParam("sortOrder", "asc").then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required")).when()
        .get(HISTORY_IDENTITY_LINK_QUERY_URL);
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);

    // assignerId
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("assignerId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByAssignerId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("assignerId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByAssignerId();
    inOrder.verify(mockedQuery).desc();

    // userId
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("userId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByUserId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("userId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByUserId();
    inOrder.verify(mockedQuery).desc();

    // groupId
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("groupId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByGroupId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("groupId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByGroupId();
    inOrder.verify(mockedQuery).desc();

    // taskId
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskId();
    inOrder.verify(mockedQuery).desc();

    // operationType
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("operationType", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByOperationType();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("operationType", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByOperationType();
    inOrder.verify(mockedQuery).desc();

    // processDefinitionId
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processDefinitionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processDefinitionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).desc();

    // processDefinitionKey
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processDefinitionKey", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionKey();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processDefinitionKey", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionKey();
    inOrder.verify(mockedQuery).desc();

    // type
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("type", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByType();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("type", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByType();
    inOrder.verify(mockedQuery).desc();

    // tenantId
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
  public void testSimpleHistoricIdentityLinkQuery() {
    Response response = given().then().expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORY_IDENTITY_LINK_QUERY_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> identityLinks = from(content).getList("");
    Assert.assertEquals("There should be one incident returned.", 1, identityLinks.size());
    Assert.assertNotNull("The returned incident should not be null.", identityLinks.get(0));

    String returnedAssignerId = from(content).getString("[0].assignerId");
    String returnedTenantId = from(content).getString("[0].tenantId");
    String returnedUserId = from(content).getString("[0].userId");
    String returnedGroupId = from(content).getString("[0].groupId");
    String returnedTaskId = from(content).getString("[0].taskId");
    String returnedType = from(content).getString("[0].type");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedProcessDefinitionKey = from(content).getString("[0].processDefinitionKey");
    String returnedOperationType = from(content).getString("[0].operationType");
    Date loggedDate = DateTimeUtil.parseDate(from(content).getString("[0].time"));

    Assert.assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HIST_IDENTITY_LINK_TIME), loggedDate);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_IDENTITY_LINK_ASSIGNER_ID, returnedAssignerId);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_IDENTITY_LINK_USER_ID, returnedUserId);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_IDENTITY_LINK_GROUP_ID, returnedGroupId);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_IDENTITY_LINK_TASK_ID, returnedTaskId);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_IDENTITY_LINK_PROC_DEFINITION_ID, returnedProcessDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_IDENTITY_LINK_PROC_DEFINITION_KEY, returnedProcessDefinitionKey);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_IDENTITY_LINK_TYPE, returnedType);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_IDENTITY_LINK_OPERATION_TYPE, returnedOperationType);
    Assert.assertEquals(MockProvider.EXAMPLE_TENANT_ID, returnedTenantId);
  }

  @Test
  public void testQueryByTenantIds() {
    mockedQuery = setUpMockHistoricIdentityLinkQuery(Arrays.asList(
        MockProvider.createMockHistoricIdentityLink(MockProvider.EXAMPLE_TENANT_ID),
        MockProvider.createMockHistoricIdentityLink(MockProvider.ANOTHER_EXAMPLE_TENANT_ID)));

    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORY_IDENTITY_LINK_QUERY_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> executions = from(content).getList("");
    assertThat(executions).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  @Test
  public void testSuccessfulPagination() {
    int firstResult = 0;
    int maxResults = 10;

    given().queryParam("firstResult", firstResult).queryParam("maxResults", maxResults).then().expect().statusCode(Status.OK.getStatusCode()).when()
        .get(HISTORY_IDENTITY_LINK_QUERY_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);
  }

  @Test
  public void testMissingFirstResultParameter() {
    int maxResults = 10;

    given().queryParam("maxResults", maxResults).then().expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORY_IDENTITY_LINK_QUERY_URL);

    verify(mockedQuery).listPage(0, maxResults);
  }

  @Test
  public void testMissingMaxResultsParameter() {
    int firstResult = 10;

    given().queryParam("firstResult", firstResult).then().expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORY_IDENTITY_LINK_QUERY_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testQueryCount() {
    expect().statusCode(Status.OK.getStatusCode()).body("count", equalTo(1)).when().get(HISTORY_IDENTITY_LINK_COUNT_QUERY_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testQueryByAssignerId() {
    String assignerId = MockProvider.EXAMPLE_HIST_IDENTITY_LINK_ASSIGNER_ID;

    given().queryParam("assignerId", assignerId).then().expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORY_IDENTITY_LINK_QUERY_URL);

    verify(mockedQuery).assignerId(assignerId);
  }

  @Test
  public void testQueryByUserId() {
    String userId = MockProvider.EXAMPLE_HIST_IDENTITY_LINK_USER_ID;

    given().queryParam("userId", userId).then().expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORY_IDENTITY_LINK_QUERY_URL);

    verify(mockedQuery).userId(userId);
  }

  @Test
  public void testQueryByGroupId() {
    String groupId = MockProvider.EXAMPLE_HIST_IDENTITY_LINK_GROUP_ID;

    given().queryParam("groupId", groupId).then().expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORY_IDENTITY_LINK_QUERY_URL);

    verify(mockedQuery).groupId(groupId);
  }

  @Test
  public void testQueryByTaskId() {
    String taskId = MockProvider.EXAMPLE_HIST_IDENTITY_LINK_TASK_ID;

    given().queryParam("taskId", taskId).then().expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORY_IDENTITY_LINK_QUERY_URL);

    verify(mockedQuery).taskId(taskId);
  }

  @Test
  public void testQueryByProcessDefinitionId() {
    String processDefinitionId = MockProvider.EXAMPLE_HIST_IDENTITY_LINK_PROC_DEFINITION_ID;

    given().queryParam("processDefinitionId", processDefinitionId).then().expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORY_IDENTITY_LINK_QUERY_URL);

    verify(mockedQuery).processDefinitionId(processDefinitionId);
  }

  @Test
  public void testQueryByProcessDefinitionKey() {
    String processDefinitionKey = MockProvider.EXAMPLE_HIST_IDENTITY_LINK_PROC_DEFINITION_KEY;

    given().queryParam("processDefinitionKey", processDefinitionKey).then().expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORY_IDENTITY_LINK_QUERY_URL);

    verify(mockedQuery).processDefinitionKey(processDefinitionKey);
  }
  
  @Test
  public void testQueryByType() {
    String type = MockProvider.EXAMPLE_HIST_IDENTITY_LINK_TYPE;

    given().queryParam("type", type).then().expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORY_IDENTITY_LINK_QUERY_URL);

    verify(mockedQuery).type(type);
  }

  @Test
  public void testQueryByOperationType() {
    String operationType = MockProvider.EXAMPLE_HIST_IDENTITY_LINK_OPERATION_TYPE;

    given().queryParam("operationType", operationType).then().expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORY_IDENTITY_LINK_QUERY_URL);

    verify(mockedQuery).operationType(operationType);
  }

  @Test
  public void testQueryByDateBefore() {
    Date dateBefore = DateTimeUtil.parseDate(MockProvider.EXAMPLE_HIST_IDENTITY_LINK_TIME);
    given().queryParam("dateBefore", MockProvider.EXAMPLE_HIST_IDENTITY_LINK_TIME).then().expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORY_IDENTITY_LINK_QUERY_URL);

    verify(mockedQuery).dateBefore(dateBefore);
  }

  @Test
  public void testQueryByDateAfter() {
    Date dateAfter = DateTimeUtil.parseDate(MockProvider.EXAMPLE_HIST_IDENTITY_LINK_TIME);

    given().queryParam("dateAfter", MockProvider.EXAMPLE_HIST_IDENTITY_LINK_TIME).then().expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORY_IDENTITY_LINK_QUERY_URL);

    verify(mockedQuery).dateAfter(dateAfter);
  }

  @Test
  public void testQueryByTenantId() {
    String tenantId = MockProvider.EXAMPLE_TENANT_ID;

    given().queryParam("tenantIdIn", tenantId).then().expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORY_IDENTITY_LINK_QUERY_URL);

    verify(mockedQuery).tenantIdIn(tenantId);
  }

  @Test
  public void testAdditionalParameters() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    given()
      .queryParams(stringQueryParameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORY_IDENTITY_LINK_QUERY_URL);

    verifyStringParameterQueryInvocations();
  }
  protected Map<String, String> getCompleteStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("assignerId", MockProvider.EXAMPLE_HIST_IDENTITY_LINK_ASSIGNER_ID);
    parameters.put("dateBefore", MockProvider.EXAMPLE_HIST_IDENTITY_LINK_DATE_BEFORE);
    parameters.put("dateAfter", MockProvider.EXAMPLE_HIST_IDENTITY_LINK_DATE_AFTER);
    parameters.put("userId", MockProvider.EXAMPLE_HIST_IDENTITY_LINK_USER_ID);
    parameters.put("groupId", MockProvider.EXAMPLE_HIST_IDENTITY_LINK_GROUP_ID);
    parameters.put("taskId", MockProvider.EXAMPLE_HIST_IDENTITY_LINK_TASK_ID);
    parameters.put("processDefinitionId", MockProvider.EXAMPLE_HIST_IDENTITY_LINK_PROC_DEFINITION_ID);
    parameters.put("processDefinitionKey", MockProvider.EXAMPLE_HIST_IDENTITY_LINK_PROC_DEFINITION_KEY);
    parameters.put("operationType", MockProvider.EXAMPLE_HIST_IDENTITY_LINK_OPERATION_TYPE);
    parameters.put("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID);
    parameters.put("type", MockProvider.EXAMPLE_HIST_IDENTITY_LINK_TYPE);
    return parameters;
  }
  protected void verifyStringParameterQueryInvocations() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();
    verify(mockedQuery).assignerId(stringQueryParameters.get("assignerId"));
    verify(mockedQuery).userId(stringQueryParameters.get("userId"));
    verify(mockedQuery).groupId(stringQueryParameters.get("groupId"));
    verify(mockedQuery).taskId(stringQueryParameters.get("taskId"));
    verify(mockedQuery).dateBefore(DateTimeUtil.parseDate(stringQueryParameters.get("dateBefore")));
    verify(mockedQuery).dateAfter(DateTimeUtil.parseDate(stringQueryParameters.get("dateAfter")));
    verify(mockedQuery).type(stringQueryParameters.get("type"));
    verify(mockedQuery).operationType(stringQueryParameters.get("operationType"));
    verify(mockedQuery).processDefinitionId(stringQueryParameters.get("processDefinitionId"));
    verify(mockedQuery).processDefinitionKey(stringQueryParameters.get("processDefinitionKey"));
    verify(mockedQuery).tenantIdIn(stringQueryParameters.get("tenantIdIn"));
    verify(mockedQuery).list();
  }
}
