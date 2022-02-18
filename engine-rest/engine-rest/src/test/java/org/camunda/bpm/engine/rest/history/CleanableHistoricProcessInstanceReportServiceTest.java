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
package org.camunda.bpm.engine.rest.history;

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReport;
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReportResult;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class CleanableHistoricProcessInstanceReportServiceTest extends AbstractRestServiceTest {

  private static final String EXAMPLE_PD_NAME = "aName";
  private static final String EXAMPLE_PD_KEY = "aKey";
  private static final int EXAMPLE_PD_VERSION = 42;
  private static final int EXAMPLE_TTL = 5;
  private static final long EXAMPLE_FINISHED_PI_COUNT = 10l;
  private static final long EXAMPLE_CLEANABLE_PI_COUNT = 5l;
  private static final String EXAMPLE_TENANT_ID = "aTenantId";

  protected static final String ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID = "anotherDefId";
  protected static final String ANOTHER_EXAMPLE_PD_KEY = "anotherDefKey";
  protected static final String ANOTHER_EXAMPLE_TENANT_ID = "anotherTenantId";

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORY_URL = TEST_RESOURCE_ROOT_PATH + "/history/process-definition";
  protected static final String HISTORIC_REPORT_URL = HISTORY_URL + "/cleanable-process-instance-report";
  protected static final String HISTORIC_REPORT_COUNT_URL = HISTORIC_REPORT_URL + "/count";

  private CleanableHistoricProcessInstanceReport historicProcessInstanceReport;

  @Before
  public void setUpRuntimeData() {
    setupHistoryReportMock();
  }

  private void setupHistoryReportMock() {
    CleanableHistoricProcessInstanceReport report = mock(CleanableHistoricProcessInstanceReport.class);

    when(report.processDefinitionIdIn(anyString())).thenReturn(report);
    when(report.processDefinitionKeyIn(anyString())).thenReturn(report);

    CleanableHistoricProcessInstanceReportResult reportResult = mock(CleanableHistoricProcessInstanceReportResult.class);

    when(reportResult.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(reportResult.getProcessDefinitionKey()).thenReturn(EXAMPLE_PD_KEY);
    when(reportResult.getProcessDefinitionName()).thenReturn(EXAMPLE_PD_NAME);
    when(reportResult.getProcessDefinitionVersion()).thenReturn(EXAMPLE_PD_VERSION);
    when(reportResult.getHistoryTimeToLive()).thenReturn(EXAMPLE_TTL);
    when(reportResult.getFinishedProcessInstanceCount()).thenReturn(EXAMPLE_FINISHED_PI_COUNT);
    when(reportResult.getCleanableProcessInstanceCount()).thenReturn(EXAMPLE_CLEANABLE_PI_COUNT);
    when(reportResult.getTenantId()).thenReturn(EXAMPLE_TENANT_ID);

    CleanableHistoricProcessInstanceReportResult anotherReportResult = mock(CleanableHistoricProcessInstanceReportResult.class);

    when(anotherReportResult.getProcessDefinitionId()).thenReturn(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID);
    when(anotherReportResult.getProcessDefinitionKey()).thenReturn(ANOTHER_EXAMPLE_PD_KEY);
    when(anotherReportResult.getProcessDefinitionName()).thenReturn("pdName");
    when(anotherReportResult.getProcessDefinitionVersion()).thenReturn(33);
    when(anotherReportResult.getHistoryTimeToLive()).thenReturn(null);
    when(anotherReportResult.getFinishedProcessInstanceCount()).thenReturn(13l);
    when(anotherReportResult.getCleanableProcessInstanceCount()).thenReturn(0l);
    when(anotherReportResult.getTenantId()).thenReturn(ANOTHER_EXAMPLE_TENANT_ID);

    List<CleanableHistoricProcessInstanceReportResult> mocks = new ArrayList<CleanableHistoricProcessInstanceReportResult>();
    mocks.add(reportResult);
    mocks.add(anotherReportResult);

    when(report.list()).thenReturn(mocks);
    when(report.count()).thenReturn((long) mocks.size());

    historicProcessInstanceReport = report;
    when(processEngine.getHistoryService().createCleanableHistoricProcessInstanceReport()).thenReturn(historicProcessInstanceReport);
  }

  @Test
  public void testGetReport() {
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when().get(HISTORIC_REPORT_URL);

    InOrder inOrder = Mockito.inOrder(historicProcessInstanceReport);
    inOrder.verify(historicProcessInstanceReport).list();
    verifyNoMoreInteractions(historicProcessInstanceReport);
  }

  @Test
  public void testReportRetrieval() {
    Response response = given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when().get(HISTORIC_REPORT_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(historicProcessInstanceReport);
    inOrder.verify(historicProcessInstanceReport).list();

    String content = response.asString();
    List<String> reportResults = from(content).getList("");
    Assert.assertEquals("There should be two report results returned.", 2, reportResults.size());
    Assert.assertNotNull(reportResults.get(0));

    String returnedDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedDefinitionKey = from(content).getString("[0].processDefinitionKey");
    String returnedDefinitionName = from(content).getString("[0].processDefinitionName");
    int returnedDefinitionVersion = from(content).getInt("[0].processDefinitionVersion");
    int returnedTTL = from(content).getInt("[0].historyTimeToLive");
    long returnedFinishedCount= from(content).getLong("[0].finishedProcessInstanceCount");
    long returnedCleanableCount = from(content).getLong("[0].cleanableProcessInstanceCount");
    String returnedTenantId = from(content).getString("[0].tenantId");

    Assert.assertEquals(EXAMPLE_PROCESS_DEFINITION_ID, returnedDefinitionId);
    Assert.assertEquals(EXAMPLE_PD_KEY, returnedDefinitionKey);
    Assert.assertEquals(EXAMPLE_PD_NAME, returnedDefinitionName);
    Assert.assertEquals(EXAMPLE_PD_VERSION, returnedDefinitionVersion);
    Assert.assertEquals(EXAMPLE_TTL, returnedTTL);
    Assert.assertEquals(EXAMPLE_FINISHED_PI_COUNT, returnedFinishedCount);
    Assert.assertEquals(EXAMPLE_CLEANABLE_PI_COUNT, returnedCleanableCount);
    Assert.assertEquals(EXAMPLE_TENANT_ID, returnedTenantId);
  }

  @Test
  public void testMissingAuthorization() {
    String message = "not authorized";
    when(historicProcessInstanceReport.list()).thenThrow(new AuthorizationException(message));

    given()
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when().get(HISTORIC_REPORT_URL);
  }

  @Test
  public void testQueryByDefinitionId() {
    given()
      .queryParam("processDefinitionIdIn",  EXAMPLE_PROCESS_DEFINITION_ID + "," + ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
      .when()
        .get(HISTORIC_REPORT_URL);

    verify(historicProcessInstanceReport).processDefinitionIdIn(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID);
    verify(historicProcessInstanceReport).list();
    verifyNoMoreInteractions(historicProcessInstanceReport);
  }

  @Test
  public void testQueryByDefinitionKey() {
    given()
      .queryParam("processDefinitionKeyIn", EXAMPLE_PD_KEY + "," + ANOTHER_EXAMPLE_PD_KEY)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
      .when()
        .get(HISTORIC_REPORT_URL);

    verify(historicProcessInstanceReport).processDefinitionKeyIn(EXAMPLE_PD_KEY, ANOTHER_EXAMPLE_PD_KEY);
    verify(historicProcessInstanceReport).list();
    verifyNoMoreInteractions(historicProcessInstanceReport);
  }

  @Test
  public void testQueryByTenantId() {
    given()
      .queryParam("tenantIdIn", EXAMPLE_TENANT_ID + "," + ANOTHER_EXAMPLE_TENANT_ID)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
      .when()
        .get(HISTORIC_REPORT_URL);

    verify(historicProcessInstanceReport).tenantIdIn(EXAMPLE_TENANT_ID, ANOTHER_EXAMPLE_TENANT_ID);
    verify(historicProcessInstanceReport).list();
    verifyNoMoreInteractions(historicProcessInstanceReport);
  }

  @Test
  public void testQueryWithoutTenantId() {
    given()
      .queryParam("withoutTenantId", true)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
      .when()
        .get(HISTORIC_REPORT_URL);

    verify(historicProcessInstanceReport).withoutTenantId();
    verify(historicProcessInstanceReport).list();
    verifyNoMoreInteractions(historicProcessInstanceReport);
  }

  @Test
  public void testQueryCompact() {
    given()
      .queryParam("compact", true)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
      .when()
        .get(HISTORIC_REPORT_URL);

    verify(historicProcessInstanceReport).compact();
    verify(historicProcessInstanceReport).list();
    verifyNoMoreInteractions(historicProcessInstanceReport);
  }

  @Test
  public void testFullQuery() {
    given()
      .params(getCompleteQueryParameters())
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
      .when()
        .get(HISTORIC_REPORT_URL);

    verifyQueryParameterInvocations();
    verify(historicProcessInstanceReport).list();
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(2))
    .when()
      .get(HISTORIC_REPORT_COUNT_URL);

    verify(historicProcessInstanceReport).count();
    verifyNoMoreInteractions(historicProcessInstanceReport);
  }

  @Test
  public void testFullQueryCount() {
    given()
      .params(getCompleteQueryParameters())
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(2))
    .when()
      .get(HISTORIC_REPORT_COUNT_URL);

    verifyQueryParameterInvocations();
    verify(historicProcessInstanceReport).count();
  }

  @Test
  public void testOrderByFinishedProcessInstanceAsc() {
    given()
      .queryParam("sortBy", "finished")
      .queryParam("sortOrder", "asc")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_REPORT_URL);

    verify(historicProcessInstanceReport).orderByFinished();
    verify(historicProcessInstanceReport).asc();
    verify(historicProcessInstanceReport).list();
    verifyNoMoreInteractions(historicProcessInstanceReport);
  }

  @Test
  public void testOrderByFinishedProcessInstanceDesc() {
    given()
      .queryParam("sortBy", "finished")
      .queryParam("sortOrder", "desc")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_REPORT_URL);

    verify(historicProcessInstanceReport).orderByFinished();
    verify(historicProcessInstanceReport).desc();
    verify(historicProcessInstanceReport).list();
    verifyNoMoreInteractions(historicProcessInstanceReport);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given()
    .queryParam("sortOrder", "asc")
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
      .when()
        .get(HISTORIC_REPORT_URL);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("finished", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then()
      .expect()
        .statusCode(expectedStatus.getStatusCode())
      .when()
        .get(HISTORIC_REPORT_URL);
  }

  protected Map<String, Object> getCompleteQueryParameters() {
    Map<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("processDefinitionIdIn", EXAMPLE_PROCESS_DEFINITION_ID + "," + ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID);
    parameters.put("processDefinitionKeyIn", EXAMPLE_PD_KEY + "," + ANOTHER_EXAMPLE_PD_KEY);
    parameters.put("tenantIdIn", EXAMPLE_TENANT_ID + "," + ANOTHER_EXAMPLE_TENANT_ID);
    parameters.put("withoutTenantId", true);
    parameters.put("compact", true);

    return parameters;
  }

  protected void verifyQueryParameterInvocations() {
    verify(historicProcessInstanceReport).processDefinitionIdIn(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID);
    verify(historicProcessInstanceReport).processDefinitionKeyIn(EXAMPLE_PD_KEY, ANOTHER_EXAMPLE_PD_KEY);
    verify(historicProcessInstanceReport).tenantIdIn(EXAMPLE_TENANT_ID, ANOTHER_EXAMPLE_TENANT_ID);
    verify(historicProcessInstanceReport).withoutTenantId();
    verify(historicProcessInstanceReport).compact();
  }
}
