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
import org.camunda.bpm.engine.history.CleanableHistoricCaseInstanceReport;
import org.camunda.bpm.engine.history.CleanableHistoricCaseInstanceReportResult;
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

public class CleanableHistoricCaseInstanceReportServiceTest extends AbstractRestServiceTest {

  private static final String EXAMPLE_CD_ID = "anId";
  private static final String EXAMPLE_CD_KEY = "aKey";
  private static final String EXAMPLE_CD_NAME = "aName";
  private static final int EXAMPLE_CD_VERSION = 42;
  private static final int EXAMPLE_TTL = 5;
  private static final long EXAMPLE_FINISHED_CI_COUNT = 10l;
  private static final long EXAMPLE_CLEANABLE_CI_COUNT = 5l;
  private static final String EXAMPLE_TENANT_ID = "aTenantId";

  protected static final String ANOTHER_EXAMPLE_CD_ID = "anotherCaseDefId";
  protected static final String ANOTHER_EXAMPLE_CD_KEY = "anotherCaseDefKey";
  protected static final String ANOTHER_EXAMPLE_TENANT_ID = "anotherTenantId";

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORY_URL = TEST_RESOURCE_ROOT_PATH + "/history/case-definition";
  protected static final String HISTORIC_REPORT_URL = HISTORY_URL + "/cleanable-case-instance-report";
  protected static final String HISTORIC_REPORT_COUNT_URL = HISTORIC_REPORT_URL + "/count";

  private CleanableHistoricCaseInstanceReport historicCaseInstanceReport;

  @Before
  public void setUpRuntimeData() {
    setupHistoryReportMock();
  }

  private void setupHistoryReportMock() {
    CleanableHistoricCaseInstanceReport report = mock(CleanableHistoricCaseInstanceReport.class);

    when(report.caseDefinitionIdIn(anyString())).thenReturn(report);
    when(report.caseDefinitionKeyIn(anyString())).thenReturn(report);

    CleanableHistoricCaseInstanceReportResult reportResult = mock(CleanableHistoricCaseInstanceReportResult.class);

    when(reportResult.getCaseDefinitionId()).thenReturn(EXAMPLE_CD_ID);
    when(reportResult.getCaseDefinitionKey()).thenReturn(EXAMPLE_CD_KEY);
    when(reportResult.getCaseDefinitionName()).thenReturn(EXAMPLE_CD_NAME);
    when(reportResult.getCaseDefinitionVersion()).thenReturn(EXAMPLE_CD_VERSION);
    when(reportResult.getHistoryTimeToLive()).thenReturn(EXAMPLE_TTL);
    when(reportResult.getFinishedCaseInstanceCount()).thenReturn(EXAMPLE_FINISHED_CI_COUNT);
    when(reportResult.getCleanableCaseInstanceCount()).thenReturn(EXAMPLE_CLEANABLE_CI_COUNT);
    when(reportResult.getTenantId()).thenReturn(EXAMPLE_TENANT_ID);

    CleanableHistoricCaseInstanceReportResult anotherReportResult = mock(CleanableHistoricCaseInstanceReportResult.class);

    when(anotherReportResult.getCaseDefinitionId()).thenReturn(ANOTHER_EXAMPLE_CD_ID);
    when(anotherReportResult.getCaseDefinitionKey()).thenReturn(ANOTHER_EXAMPLE_CD_KEY);
    when(anotherReportResult.getCaseDefinitionName()).thenReturn("cdName");
    when(anotherReportResult.getCaseDefinitionVersion()).thenReturn(33);
    when(anotherReportResult.getHistoryTimeToLive()).thenReturn(null);
    when(anotherReportResult.getFinishedCaseInstanceCount()).thenReturn(13l);
    when(anotherReportResult.getCleanableCaseInstanceCount()).thenReturn(0l);
    when(anotherReportResult.getTenantId()).thenReturn(ANOTHER_EXAMPLE_TENANT_ID);


    List<CleanableHistoricCaseInstanceReportResult> mocks = new ArrayList<CleanableHistoricCaseInstanceReportResult>();
    mocks.add(reportResult);
    mocks.add(anotherReportResult);

    when(report.list()).thenReturn(mocks);
    when(report.count()).thenReturn((long) mocks.size());

    historicCaseInstanceReport = report;
    when(processEngine.getHistoryService().createCleanableHistoricCaseInstanceReport()).thenReturn(historicCaseInstanceReport);
  }

  @Test
  public void testGetReport() {
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when().get(HISTORIC_REPORT_URL);

    InOrder inOrder = Mockito.inOrder(historicCaseInstanceReport);
    inOrder.verify(historicCaseInstanceReport).list();
    verifyNoMoreInteractions(historicCaseInstanceReport);
  }

  @Test
  public void testReportRetrieval() {
    Response response = given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when().get(HISTORIC_REPORT_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(historicCaseInstanceReport);
    inOrder.verify(historicCaseInstanceReport).list();

    String content = response.asString();
    List<String> reportResults = from(content).getList("");
    Assert.assertEquals("There should be two report results returned.", 2, reportResults.size());
    Assert.assertNotNull(reportResults.get(0));

    String returnedDefinitionId = from(content).getString("[0].caseDefinitionId");
    String returnedDefinitionKey = from(content).getString("[0].caseDefinitionKey");
    String returnedDefinitionName = from(content).getString("[0].caseDefinitionName");
    int returnedDefinitionVersion = from(content).getInt("[0].caseDefinitionVersion");
    int returnedTTL = from(content).getInt("[0].historyTimeToLive");
    long returnedFinishedCount= from(content).getLong("[0].finishedCaseInstanceCount");
    long returnedCleanableCount = from(content).getLong("[0].cleanableCaseInstanceCount");
    String returnedTenantId = from(content).getString("[0].tenantId");

    Assert.assertEquals(EXAMPLE_CD_ID, returnedDefinitionId);
    Assert.assertEquals(EXAMPLE_CD_KEY, returnedDefinitionKey);
    Assert.assertEquals(EXAMPLE_CD_NAME, returnedDefinitionName);
    Assert.assertEquals(EXAMPLE_CD_VERSION, returnedDefinitionVersion);
    Assert.assertEquals(EXAMPLE_TTL, returnedTTL);
    Assert.assertEquals(EXAMPLE_FINISHED_CI_COUNT, returnedFinishedCount);
    Assert.assertEquals(EXAMPLE_CLEANABLE_CI_COUNT, returnedCleanableCount);
    Assert.assertEquals(EXAMPLE_TENANT_ID, returnedTenantId);
  }

  @Test
  public void testMissingAuthorization() {
    String message = "not authorized";
    when(historicCaseInstanceReport.list()).thenThrow(new AuthorizationException(message));

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
      .queryParam("caseDefinitionIdIn",  EXAMPLE_CD_ID + "," + ANOTHER_EXAMPLE_CD_ID)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
      .when()
        .get(HISTORIC_REPORT_URL);

    verify(historicCaseInstanceReport).caseDefinitionIdIn(EXAMPLE_CD_ID, ANOTHER_EXAMPLE_CD_ID);
    verify(historicCaseInstanceReport).list();
    verifyNoMoreInteractions(historicCaseInstanceReport);
  }

  @Test
  public void testQueryByDefinitionKey() {
    given()
      .queryParam("caseDefinitionKeyIn", EXAMPLE_CD_KEY + "," + ANOTHER_EXAMPLE_CD_KEY)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
      .when()
        .get(HISTORIC_REPORT_URL);

    verify(historicCaseInstanceReport).caseDefinitionKeyIn(EXAMPLE_CD_KEY, ANOTHER_EXAMPLE_CD_KEY);
    verify(historicCaseInstanceReport).list();
    verifyNoMoreInteractions(historicCaseInstanceReport);
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

    verify(historicCaseInstanceReport).tenantIdIn(EXAMPLE_TENANT_ID, ANOTHER_EXAMPLE_TENANT_ID);
    verify(historicCaseInstanceReport).list();
    verifyNoMoreInteractions(historicCaseInstanceReport);
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

    verify(historicCaseInstanceReport).withoutTenantId();
    verify(historicCaseInstanceReport).list();
    verifyNoMoreInteractions(historicCaseInstanceReport);
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

    verify(historicCaseInstanceReport).compact();
    verify(historicCaseInstanceReport).list();
    verifyNoMoreInteractions(historicCaseInstanceReport);
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
    verify(historicCaseInstanceReport).list();
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(2))
    .when()
      .get(HISTORIC_REPORT_COUNT_URL);

    verify(historicCaseInstanceReport).count();
    verifyNoMoreInteractions(historicCaseInstanceReport);
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
    verify(historicCaseInstanceReport).count();
  }

  @Test
  public void testOrderByFinishedCaseInstanceAsc() {
    given()
      .queryParam("sortBy", "finished")
      .queryParam("sortOrder", "asc")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_REPORT_URL);

    verify(historicCaseInstanceReport).orderByFinished();
    verify(historicCaseInstanceReport).asc();
    verify(historicCaseInstanceReport).list();
    verifyNoMoreInteractions(historicCaseInstanceReport);
  }

  @Test
  public void testOrderByFinishedCaseInstanceDesc() {
    given()
      .queryParam("sortBy", "finished")
      .queryParam("sortOrder", "desc")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_REPORT_URL);

    verify(historicCaseInstanceReport).orderByFinished();
    verify(historicCaseInstanceReport).desc();
    verify(historicCaseInstanceReport).list();
    verifyNoMoreInteractions(historicCaseInstanceReport);
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

    parameters.put("caseDefinitionIdIn", EXAMPLE_CD_ID + "," + ANOTHER_EXAMPLE_CD_ID);
    parameters.put("caseDefinitionKeyIn", EXAMPLE_CD_KEY + "," + ANOTHER_EXAMPLE_CD_KEY);
    parameters.put("tenantIdIn", EXAMPLE_TENANT_ID + "," + ANOTHER_EXAMPLE_TENANT_ID);
    parameters.put("withoutTenantId", true);
    parameters.put("compact", true);

    return parameters;
  }

  protected void verifyQueryParameterInvocations() {
    verify(historicCaseInstanceReport).caseDefinitionIdIn(EXAMPLE_CD_ID, ANOTHER_EXAMPLE_CD_ID);
    verify(historicCaseInstanceReport).caseDefinitionKeyIn(EXAMPLE_CD_KEY, ANOTHER_EXAMPLE_CD_KEY);
    verify(historicCaseInstanceReport).tenantIdIn(EXAMPLE_TENANT_ID, ANOTHER_EXAMPLE_TENANT_ID);
    verify(historicCaseInstanceReport).withoutTenantId();
    verify(historicCaseInstanceReport).compact();
  }
}
