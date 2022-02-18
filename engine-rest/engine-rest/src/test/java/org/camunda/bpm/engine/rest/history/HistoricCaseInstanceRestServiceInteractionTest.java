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

import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricCaseInstanceQuery;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class HistoricCaseInstanceRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();
  
  protected static final String HISTORIC_CASE_INSTANCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/case-instance";
  protected static final String HISTORIC_SINGLE_CASE_INSTANCE_URL = HISTORIC_CASE_INSTANCE_URL + "/{id}";

  protected HistoryService historyServiceMock;
  protected HistoricCaseInstance historicInstanceMock;
  protected HistoricCaseInstanceQuery historicQueryMock;

  @Before
  public void setUpRuntimeData() {
    historyServiceMock = mock(HistoryService.class);

    // runtime service
    when(processEngine.getHistoryService()).thenReturn(historyServiceMock);

    historicInstanceMock = MockProvider.createMockHistoricCaseInstance();
    historicQueryMock = mock(HistoricCaseInstanceQuery.class);

    when(historyServiceMock.createHistoricCaseInstanceQuery()).thenReturn(historicQueryMock);
    when(historicQueryMock.caseInstanceId(anyString())).thenReturn(historicQueryMock);
    when(historicQueryMock.singleResult()).thenReturn(historicInstanceMock);
  }

  @Test
  public void testGetSingleHistoricCaseInstance() {
    Response response = given()
        .pathParam("id", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_SINGLE_CASE_INSTANCE_URL);

    String content = response.asString();

    String returnedCaseInstanceId = from(content).getString("id");
    String returnedCaseInstanceBusinessKey = from(content).getString("businessKey");
    String returnedCaseDefinitionId = from(content).getString("caseDefinitionId");
    String returnedCreateTime = from(content).getString("createTime");
    String returnedCloseTime = from(content).getString("closeTime");
    long returnedDurationInMillis = from(content).getLong("durationInMillis");
    String returnedCreateUserId = from(content).getString("createUserId");
    String returnedSuperCaseInstanceId = from(content).getString("superCaseInstanceId");
    String returnedSuperProcessInstanceId = from(content).getString("superProcessInstanceId");
    String returnedTenantId = from(content).getString("tenantId");
    boolean active = from(content).getBoolean("active");
    boolean completed = from(content).getBoolean("completed");
    boolean terminated = from(content).getBoolean("terminated");
    boolean closed = from(content).getBoolean("closed");

    Assert.assertEquals(MockProvider.EXAMPLE_CASE_INSTANCE_ID, returnedCaseInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY, returnedCaseInstanceBusinessKey);
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_ID, returnedCaseDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATE_TIME, returnedCreateTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSE_TIME, returnedCloseTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_DURATION_MILLIS, returnedDurationInMillis);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATE_USER_ID, returnedCreateUserId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_SUPER_CASE_INSTANCE_ID, returnedSuperCaseInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_SUPER_PROCESS_INSTANCE_ID, returnedSuperProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_TENANT_ID, returnedTenantId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_IS_ACTIVE, active);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_IS_COMPLETED, completed);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_IS_TERMINATED, terminated);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_IS_CLOSED, closed);
  }

  @Test
  public void testGetNonExistingHistoricCaseInstance() {
    when(historicQueryMock.singleResult()).thenReturn(null);

    given()
      .pathParam("id", MockProvider.NON_EXISTING_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Historic case instance with id '" + MockProvider.NON_EXISTING_ID + "' does not exist"))
    .when()
      .get(HISTORIC_SINGLE_CASE_INSTANCE_URL);
  }

}
