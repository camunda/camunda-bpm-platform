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
package org.camunda.bpm.engine.rest.history;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public class HistoricDecisionInstanceRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORIC_DECISION_INSTANCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/decision-instance";
  protected static final String HISTORIC_SINGLE_DECISION_INSTANCE_URL = HISTORIC_DECISION_INSTANCE_URL + "/{id}";

  protected HistoryService historyServiceMock;
  protected HistoricDecisionInstance historicInstanceMock;
  protected HistoricDecisionInstanceQuery historicQueryMock;

  @Before
  public void setUpRuntimeData() {
    historyServiceMock = mock(HistoryService.class);

    // runtime service
    when(processEngine.getHistoryService()).thenReturn(historyServiceMock);

    historicInstanceMock = MockProvider.createMockHistoricDecisionInstance();
    historicQueryMock = mock(HistoricDecisionInstanceQuery.class);

    when(historyServiceMock.createHistoricDecisionInstanceQuery()).thenReturn(historicQueryMock);
    when(historicQueryMock.decisionInstanceId(anyString())).thenReturn(historicQueryMock);
    when(historicQueryMock.singleResult()).thenReturn(historicInstanceMock);
  }

  @Test
  public void testGetSingleHistoricDecisionInstance() {
    Response response = given()
        .pathParam("id", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_SINGLE_DECISION_INSTANCE_URL);

    InOrder inOrder = inOrder(historicQueryMock);
    inOrder.verify(historicQueryMock).decisionInstanceId(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    inOrder.verify(historicQueryMock).singleResult();

    String content = response.asString();

    String returnedHistoricDecisionInstanceId = from(content).getString("id");
    String returnedDecisionDefinitionId = from(content).getString("decisionDefinitionId");
    String returnedDecisionDefinitionKey = from(content).getString("decisionDefinitionKey");
    String returnedDecisionDefinitionName = from(content).getString("decisionDefinitionName");
    String returnedEvaluationTime = from(content).getString("evaluationTime");
    String returnedProcessDefinitionId = from(content).getString("processDefinitionId");
    String returnedProcessDefinitionKey = from(content).getString("processDefinitionKey");
    String returnedProcessInstanceId = from(content).getString("processInstanceId");
    String returnedCaseDefinitionId = from(content).getString("caseDefinitionId");
    String returnedCaseDefinitionKey = from(content).getString("caseDefinitionKey");
    String returnedCaseInstanceId = from(content).getString("caseInstanceId");
    String returnedActivityId = from(content).getString("activityId");
    String returnedActivityInstanceId = from(content).getString("activityInstanceId");
    String returnedUserId = from(content).getString("userId");
    List<Map<String, Object>> returnedInputs = from(content).getList("inputs");
    List<Map<String, Object>> returnedOutputs = from(content).getList("outputs");
    Double returnedCollectResultValue = from(content).getDouble("collectResultValue");
    String returnedTenantId = from(content).getString("tenantId");

    assertThat(returnedHistoricDecisionInstanceId, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID));
    assertThat(returnedDecisionDefinitionId, is(MockProvider.EXAMPLE_DECISION_DEFINITION_ID));
    assertThat(returnedDecisionDefinitionKey, is(MockProvider.EXAMPLE_DECISION_DEFINITION_KEY));
    assertThat(returnedDecisionDefinitionName, is(MockProvider.EXAMPLE_DECISION_DEFINITION_NAME));
    assertThat(returnedEvaluationTime, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_EVALUATION_TIME));
    assertThat(returnedProcessDefinitionId, is(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    assertThat(returnedProcessDefinitionKey, is(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY));
    assertThat(returnedProcessInstanceId, is(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID));
    assertThat(returnedCaseDefinitionId, is(MockProvider.EXAMPLE_CASE_DEFINITION_ID));
    assertThat(returnedCaseDefinitionKey, is(MockProvider.EXAMPLE_CASE_DEFINITION_KEY));
    assertThat(returnedCaseInstanceId, is(MockProvider.EXAMPLE_CASE_INSTANCE_ID));
    assertThat(returnedActivityId, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ACTIVITY_ID));
    assertThat(returnedActivityInstanceId, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ACTIVITY_INSTANCE_ID));
    assertThat(returnedUserId, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_USER_ID));
    assertThat(returnedInputs, is(nullValue()));
    assertThat(returnedOutputs, is(nullValue()));
    assertThat(returnedCollectResultValue, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_COLLECT_RESULT_VALUE));
    assertThat(returnedTenantId, is(MockProvider.EXAMPLE_TENANT_ID));
  }

  @Test
  public void testGetSingleHistoricDecisionInstanceWithInputs() {
    historicInstanceMock = MockProvider.createMockHistoricDecisionInstanceWithInputs();
    when(historicQueryMock.singleResult()).thenReturn(historicInstanceMock);

    Response response = given()
        .pathParam("id", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID)
        .queryParam("includeInputs", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_SINGLE_DECISION_INSTANCE_URL);

    InOrder inOrder = inOrder(historicQueryMock);
    inOrder.verify(historicQueryMock).decisionInstanceId(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    inOrder.verify(historicQueryMock).includeInputs();
    inOrder.verify(historicQueryMock, never()).includeOutputs();
    inOrder.verify(historicQueryMock).singleResult();

    String content = response.asString();

    List<Map<String, Object>> returnedInputs = from(content).getList("inputs");
    List<Map<String, Object>> returnedOutputs = from(content).getList("outputs");
    assertThat(returnedInputs, is(notNullValue()));
    assertThat(returnedInputs, hasSize(3));
    assertThat(returnedOutputs, is(nullValue()));
  }

  @Test
  public void testGetSingleHistoricDecisionInstanceWithOutputs() {
    historicInstanceMock = MockProvider.createMockHistoricDecisionInstanceWithOutputs();
    when(historicQueryMock.singleResult()).thenReturn(historicInstanceMock);

    Response response = given()
        .pathParam("id", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID)
        .queryParam("includeOutputs", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_SINGLE_DECISION_INSTANCE_URL);

    InOrder inOrder = inOrder(historicQueryMock);
    inOrder.verify(historicQueryMock).decisionInstanceId(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    inOrder.verify(historicQueryMock, never()).includeInputs();
    inOrder.verify(historicQueryMock).includeOutputs();
    inOrder.verify(historicQueryMock).singleResult();

    String content = response.asString();

    List<Map<String, Object>> returnedInputs = from(content).getList("inputs");
    List<Map<String, Object>> returnedOutputs = from(content).getList("outputs");
    assertThat(returnedInputs, is(nullValue()));
    assertThat(returnedOutputs, is(notNullValue()));
    assertThat(returnedOutputs, hasSize(3));
  }

  @Test
  public void testGetSingleHistoricDecisionInstanceWithInputsAndOutputs() {
    historicInstanceMock = MockProvider.createMockHistoricDecisionInstanceWithInputsAndOutputs();
    when(historicQueryMock.singleResult()).thenReturn(historicInstanceMock);

    Response response = given()
        .pathParam("id", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID)
        .queryParam("includeInputs", true)
        .queryParam("includeOutputs", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_SINGLE_DECISION_INSTANCE_URL);

    InOrder inOrder = inOrder(historicQueryMock);
    inOrder.verify(historicQueryMock).decisionInstanceId(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    inOrder.verify(historicQueryMock).includeInputs();
    inOrder.verify(historicQueryMock).includeOutputs();
    inOrder.verify(historicQueryMock).singleResult();

    String content = response.asString();

    List<Map<String, Object>> returnedInputs = from(content).getList("inputs");
    List<Map<String, Object>> returnedOutputs = from(content).getList("outputs");
    assertThat(returnedInputs, is(notNullValue()));
    assertThat(returnedInputs, hasSize(3));
    assertThat(returnedOutputs, is(notNullValue()));
    assertThat(returnedOutputs, hasSize(3));
  }

  @Test
  public void testGetSingleHistoricDecisionInstanceWithDisabledBinaryFetching() {
    historicInstanceMock = MockProvider.createMockHistoricDecisionInstanceWithInputsAndOutputs();
    when(historicQueryMock.singleResult()).thenReturn(historicInstanceMock);

    given()
        .pathParam("id", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID)
        .queryParam("disableBinaryFetching", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_SINGLE_DECISION_INSTANCE_URL);

    InOrder inOrder = inOrder(historicQueryMock);
    inOrder.verify(historicQueryMock).decisionInstanceId(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    inOrder.verify(historicQueryMock).disableBinaryFetching();
    inOrder.verify(historicQueryMock).singleResult();
  }

  @Test
  public void testGetSingleHistoricDecisionInstanceWithDisabledCustomObjectDeserialization() {
    historicInstanceMock = MockProvider.createMockHistoricDecisionInstanceWithInputsAndOutputs();
    when(historicQueryMock.singleResult()).thenReturn(historicInstanceMock);

    given()
        .pathParam("id", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID)
        .queryParam("disableCustomObjectDeserialization", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_SINGLE_DECISION_INSTANCE_URL);

    InOrder inOrder = inOrder(historicQueryMock);
    inOrder.verify(historicQueryMock).decisionInstanceId(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    inOrder.verify(historicQueryMock).disableCustomObjectDeserialization();
    inOrder.verify(historicQueryMock).singleResult();
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
      .body("message", equalTo("Historic decision instance with id '" + MockProvider.NON_EXISTING_ID + "' does not exist"))
    .when()
      .get(HISTORIC_SINGLE_DECISION_INSTANCE_URL);
  }

}
