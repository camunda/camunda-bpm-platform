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

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.history.HistoricFormField;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockHistoricVariableUpdateBuilder;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.VariableTypeHelper;
import org.camunda.bpm.engine.rest.util.OrderingBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.SerializableValueType;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Roman Smirnov
 * @author Nikola Koevski
 */
public class HistoricDetailRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORIC_DETAIL_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/detail";

  protected static final String HISTORIC_DETAIL_COUNT_RESOURCE_URL = HISTORIC_DETAIL_RESOURCE_URL + "/count";

  protected HistoricDetailQuery mockedQuery;

  protected HistoricVariableUpdate historicUpdateMock;
  protected MockHistoricVariableUpdateBuilder historicUpdateBuilder;

  protected HistoricFormField historicFormFieldMock;

  @Before
  public void setUpRuntimeData() {
    List<HistoricDetail> details = new ArrayList<HistoricDetail>();

    historicUpdateBuilder = MockProvider.mockHistoricVariableUpdate();
    historicUpdateMock = historicUpdateBuilder.build();
    historicFormFieldMock = MockProvider.createMockHistoricFormField();

    details.add(historicUpdateMock);
    details.add(historicFormFieldMock);

    mockedQuery = setUpMockedDetailsQuery(details);
  }

  protected HistoricDetailQuery setUpMockedDetailsQuery(List<HistoricDetail> detailMocks) {
    HistoricDetailQuery mock = mock(HistoricDetailQuery.class);
    when(mock.list()).thenReturn(detailMocks);
    when(mock.count()).thenReturn((long) detailMocks.size());

    when(processEngine.getHistoryService().createHistoricDetailQuery()).thenReturn(mock);
    return mock;
  }

  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given()
      .queryParam("processInstanceId", queryKey)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_DETAIL_RESOURCE_URL);
  }

  @Test
  public void testNoParametersQuery() {
    // GET
    expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).list();
    verify(mockedQuery).disableBinaryFetching();
    verifyNoMoreInteractions(mockedQuery);

    reset(mockedQuery);

    // POST
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(Collections.emptyMap())
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).list();
    verify(mockedQuery).disableBinaryFetching();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testNoParametersQueryDisableObjectDeserialization() {
    // GET
    given()
      .queryParam("deserializeValues", false)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).list();
    verify(mockedQuery).disableBinaryFetching();
    verify(mockedQuery).disableCustomObjectDeserialization();
    verifyNoMoreInteractions(mockedQuery);

    reset(mockedQuery);

    // POST
    given()
      .queryParam("deserializeValues", false)
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(Collections.emptyMap())
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).list();
    verify(mockedQuery).disableBinaryFetching();
    verify(mockedQuery).disableCustomObjectDeserialization();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testSecondarySortingAsPost() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySortingAsPost(
      OrderingBuilder.create()
        .orderBy("processInstanceId").desc()
        .orderBy("variableName").asc()
        .orderBy("variableType").desc()
        .orderBy("variableRevision").asc()
        .orderBy("formPropertyId").desc()
        .orderBy("time").asc()
        .orderBy("occurrence").desc()
        .orderBy("tenantId").asc()
        .getJson(),
      Status.OK);

    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).desc();
    inOrder.verify(mockedQuery).orderByVariableName();
    inOrder.verify(mockedQuery).asc();
    inOrder.verify(mockedQuery).orderByVariableType();
    inOrder.verify(mockedQuery).desc();
    inOrder.verify(mockedQuery).orderByVariableRevision();
    inOrder.verify(mockedQuery).asc();
    inOrder.verify(mockedQuery).orderByFormPropertyId();
    inOrder.verify(mockedQuery).desc();
    inOrder.verify(mockedQuery).orderByTime();
    inOrder.verify(mockedQuery).asc();
    inOrder.verify(mockedQuery).orderPartiallyByOccurrence();
    inOrder.verify(mockedQuery).desc();
    inOrder.verify(mockedQuery).orderByTenantId();
    inOrder.verify(mockedQuery).asc();
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("processInstanceId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  @Test
  public void testInvalidSecondarySortingOptions() {
    executeAndVerifySortingAsPost(
      OrderingBuilder.create()
        .orderBy("processInstanceId").desc()
        .orderBy("invalidParameter").asc()
        .getJson(),
      Status.BAD_REQUEST
    );
  }

  @Test
  public void testMissingSecondarySortingOptions() {
    executeAndVerifySortingAsPost(
      OrderingBuilder.create()
        .orderBy("processInstanceId").desc()
        .orderBy("variableName")
        .getJson(),
      Status.BAD_REQUEST
    );
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then()
      .expect()
        .statusCode(expectedStatus.getStatusCode())
      .when()
        .get(HISTORIC_DETAIL_RESOURCE_URL);
  }

  protected void executeAndVerifySortingAsPost(List<Map<String, Object>> sortingJson, Status expectedStatus) {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("sorting", sortingJson);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(json)
    .then()
      .expect()
        .statusCode(expectedStatus.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);
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
      .get(HISTORIC_DETAIL_RESOURCE_URL);
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processInstanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processInstanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("variableName", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByVariableName();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("variableName", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByVariableName();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("formPropertyId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByFormPropertyId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("formPropertyId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByFormPropertyId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("variableType", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByVariableType();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("variableType", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByVariableType();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("variableRevision", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByVariableRevision();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("variableRevision", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByVariableRevision();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("time", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("time", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTime();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("occurrence", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderPartiallyByOccurrence();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("occurrence", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderPartiallyByOccurrence();
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

    // GET
    given()
      .queryParam("firstResult", firstResult)
      .queryParam("maxResults", maxResults)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);

    reset(mockedQuery);

    // POST
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .queryParam("firstResult", firstResult)
      .queryParam("maxResults", maxResults)
      .body(Collections.emptyMap())
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);
  }

  @Test
  public void testMissingFirstResultParameter() {
    int maxResults = 10;

    // GET
    given()
      .queryParam("maxResults", maxResults)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).listPage(0, maxResults);

    reset(mockedQuery);

    // POST
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .queryParam("maxResults", maxResults)
      .body(Collections.emptyMap())
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).listPage(0, maxResults);
  }

  @Test
  public void testMissingMaxResultsParameter() {
    int firstResult = 10;

    // GET
    given()
      .queryParam("firstResult", firstResult)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);

    reset(mockedQuery);

    // POST
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .queryParam("firstResult", firstResult)
      .body(Collections.emptyMap())
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(2))
    .when()
      .get(HISTORIC_DETAIL_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testSimpleHistoricActivityQuery() {
    // GET
    Response response = given()
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .and()
          .body("[0].id", equalTo(historicUpdateBuilder.getId()))
          .body("[0].variableName", equalTo(historicUpdateBuilder.getName()))
          .body("[0].variableInstanceId", equalTo(historicUpdateBuilder.getVariableInstanceId()))
          .body("[0].variableType", equalTo(VariableTypeHelper.toExpectedValueTypeName(historicUpdateBuilder.getTypedValue().getType())))
          .body("[0].value", equalTo(historicUpdateBuilder.getTypedValue().getValue()))
          .body("[0].processDefinitionKey", equalTo(historicUpdateBuilder.getProcessDefinitionKey()))
          .body("[0].processDefinitionId", equalTo(historicUpdateBuilder.getProcessDefinitionId()))
          .body("[0].processInstanceId", equalTo(historicUpdateBuilder.getProcessInstanceId()))
          .body("[0].errorMessage", equalTo(historicUpdateBuilder.getErrorMessage()))
          .body("[0].activityInstanceId", equalTo(historicUpdateBuilder.getActivityInstanceId()))
          .body("[0].revision", equalTo(historicUpdateBuilder.getRevision()))
          .body("[0].time", equalTo(historicUpdateBuilder.getTime()))
          .body("[0].taskId", equalTo(historicUpdateBuilder.getTaskId()))
          .body("[0].executionId", equalTo(historicUpdateBuilder.getExecutionId()))
          .body("[0].type", equalTo("variableUpdate"))
          .body("[0].caseDefinitionKey", equalTo(historicUpdateBuilder.getCaseDefinitionKey()))
          .body("[0].caseDefinitionId", equalTo(historicUpdateBuilder.getCaseDefinitionId()))
          .body("[0].caseInstanceId", equalTo(historicUpdateBuilder.getCaseInstanceId()))
          .body("[0].caseExecutionId", equalTo(historicUpdateBuilder.getCaseExecutionId()))
          .body("[0].tenantId", equalTo(historicUpdateBuilder.getTenantId()))
          .body("[0].userOperationId", equalTo(historicUpdateBuilder.getUserOperationId()))
      .when()
        .get(HISTORIC_DETAIL_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).list();

    verifySimpleHistoricActivityQueryResponse(response);
  }

  @Test
  public void testSimpleHistoricActivityQueryPost() {
    // POST
    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .header("accept", MediaType.APPLICATION_JSON)
        .body(Collections.emptyMap())
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .and()
          .body("[0].id", equalTo(historicUpdateBuilder.getId()))
          .body("[0].variableName", equalTo(historicUpdateBuilder.getName()))
          .body("[0].variableInstanceId", equalTo(historicUpdateBuilder.getVariableInstanceId()))
          .body("[0].variableType", equalTo(VariableTypeHelper.toExpectedValueTypeName(
            historicUpdateBuilder.getTypedValue().getType())))
          .body("[0].value", equalTo(historicUpdateBuilder.getTypedValue().getValue()))
          .body("[0].processDefinitionKey", equalTo(historicUpdateBuilder.getProcessDefinitionKey()))
          .body("[0].processDefinitionId", equalTo(historicUpdateBuilder.getProcessDefinitionId()))
          .body("[0].processInstanceId", equalTo(historicUpdateBuilder.getProcessInstanceId()))
          .body("[0].errorMessage", equalTo(historicUpdateBuilder.getErrorMessage()))
          .body("[0].activityInstanceId", equalTo(historicUpdateBuilder.getActivityInstanceId()))
          .body("[0].revision", equalTo(historicUpdateBuilder.getRevision()))
          .body("[0].time", equalTo(historicUpdateBuilder.getTime()))
          .body("[0].taskId", equalTo(historicUpdateBuilder.getTaskId()))
          .body("[0].executionId", equalTo(historicUpdateBuilder.getExecutionId()))
          .body("[0].type", equalTo("variableUpdate"))
          .body("[0].caseDefinitionKey", equalTo(historicUpdateBuilder.getCaseDefinitionKey()))
          .body("[0].caseDefinitionId", equalTo(historicUpdateBuilder.getCaseDefinitionId()))
          .body("[0].caseInstanceId", equalTo(historicUpdateBuilder.getCaseInstanceId()))
          .body("[0].caseExecutionId", equalTo(historicUpdateBuilder.getCaseExecutionId()))
          .body("[0].tenantId", equalTo(historicUpdateBuilder.getTenantId()))
          .body("[0].userOperationId", equalTo(historicUpdateBuilder.getUserOperationId()))
      .when()
        .post(HISTORIC_DETAIL_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).list();

    verifySimpleHistoricActivityQueryResponse(response);
  }

  private void verifySimpleHistoricActivityQueryResponse(Response response) {
    String content = response.asString();
    List<String> details = from(content).getList("");
    Assert.assertEquals("There should be two activity instance returned.", 2, details.size());
    Assert.assertNotNull("The returned details should not be null.", details.get(0));
    Assert.assertNotNull("The returned details should not be null.", details.get(1));

    // note: element [0] is asserted as part of the fluent rest-assured invocation

    String returnedId2 = from(content).getString("[1].id");
    String returnedProcessDefinitionKey2 = from(content).getString("[1].processDefinitionKey");
    String returnedProcessDefinitionId2 = from(content).getString("[1].processDefinitionId");
    String returnedProcessInstanceId2 = from(content).getString("[1].processInstanceId");
    String returnedActivityInstanceId2 = from(content).getString("[1].activityInstanceId");
    String returnedExecutionId2 = from(content).getString("[1].executionId");
    String returnedTaskId2 = from(content).getString("[1].taskId");
    Date returnedTime2 = DateTimeUtil.parseDate(from(content).getString("[1].time"));
    Date returnedRemovalTime = DateTimeUtil.parseDate(from(content).getString("[1].removalTime"));
    String returnedFieldId = from(content).getString("[1].fieldId");
    String returnedFieldValue = from(content).getString("[1].fieldValue");
    String returnedType = from(content).getString("[1].type");
    String returnedCaseDefinitionKey2 = from(content).getString("[1].caseDefinitionKey");
    String returnedCaseDefinitionId2 = from(content).getString("[1].caseDefinitionId");
    String returnedCaseInstanceId2 = from(content).getString("[1].caseInstanceId");
    String returnedCaseExecutionId2 = from(content).getString("[1].caseExecutionId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");
    String returnedOperationId2 = from(content).getString("[1].userOperationId");
    String returnedRootProcessInstanceId = from(content).getString("[1].rootProcessInstanceId");

    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_ID, returnedId2);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_PROC_DEF_KEY, returnedProcessDefinitionKey2);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_PROC_DEF_ID, returnedProcessDefinitionId2);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_PROC_INST_ID, returnedProcessInstanceId2);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_ACT_INST_ID, returnedActivityInstanceId2);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_EXEC_ID, returnedExecutionId2);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_TASK_ID, returnedTaskId2);
    Assert.assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_TIME), returnedTime2);
    Assert.assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_TIME), returnedRemovalTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_FIELD_ID, returnedFieldId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_VALUE, returnedFieldValue);
    Assert.assertEquals("formField", returnedType);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_CASE_DEF_ID, returnedCaseDefinitionId2);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_CASE_DEF_KEY, returnedCaseDefinitionKey2);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_CASE_INST_ID, returnedCaseInstanceId2);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_CASE_EXEC_ID, returnedCaseExecutionId2);
    Assert.assertEquals(MockProvider.EXAMPLE_TENANT_ID, returnedTenantId2);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_EXEC_ID, returnedExecutionId2);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_OPERATION_ID, returnedOperationId2);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_ROOT_PROCESS_INSTANCE_ID, returnedRootProcessInstanceId);
  }

  @Test
  public void testSerializableVariableInstanceRetrieval() {
    ObjectValue serializedValue = Variables.serializedObjectValue("a serialized value").create();
    MockHistoricVariableUpdateBuilder builder = MockProvider.mockHistoricVariableUpdate()
        .typedValue(serializedValue);

    List<HistoricDetail> details = new ArrayList<HistoricDetail>();
    details.add(builder.build());

    // GET
    mockedQuery = setUpMockedDetailsQuery(details);

    given()
        .then().expect().statusCode(Status.OK.getStatusCode())
        .and()
          .body("[0].value", equalTo("a serialized value"))
          .body("[0].variableType", equalTo(VariableTypeHelper.toExpectedValueTypeName(serializedValue.getType())))
          .body("[0].errorMessage", nullValue())
        .when().get(HISTORIC_DETAIL_RESOURCE_URL);

    // should not resolve custom objects but existing API requires it
//  verify(mockedQuery).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();

    // POST
    mockedQuery = setUpMockedDetailsQuery(details);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(Collections.emptyMap())
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .and()
        .body("[0].value", equalTo("a serialized value"))
        .body("[0].variableType", equalTo(VariableTypeHelper.toExpectedValueTypeName(serializedValue.getType())))
        .body("[0].errorMessage", nullValue())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    // should not resolve custom objects but existing API requires it
    //  verify(mockedQuery).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testSpinVariableInstanceRetrieval() {
    MockHistoricVariableUpdateBuilder builder = MockProvider.mockHistoricVariableUpdate()
        .typedValue(Variables
            .serializedObjectValue("aSerializedValue")
            .serializationDataFormat("aDataFormat")
            .objectTypeName("aRootType")
            .create());

    List<HistoricDetail> details = new ArrayList<HistoricDetail>();
    details.add(builder.build());

    // GET
    mockedQuery = setUpMockedDetailsQuery(details);

    given()
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .and()
          .body("[0].variableType", equalTo(VariableTypeHelper.toExpectedValueTypeName(ValueType.OBJECT)))
          .body("[0].errorMessage", nullValue())
          .body("[0].value", equalTo("aSerializedValue"))
          .body("[0].valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME,
              equalTo("aRootType"))
          .body("[0].valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT,
              equalTo("aDataFormat"))
      .when()
        .get(HISTORIC_DETAIL_RESOURCE_URL);

    // POST
    mockedQuery = setUpMockedDetailsQuery(details);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(Collections.emptyMap())
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .and()
        .body("[0].variableType", equalTo(VariableTypeHelper.toExpectedValueTypeName(ValueType.OBJECT)))
        .body("[0].errorMessage", nullValue())
        .body("[0].value", equalTo("aSerializedValue"))
        .body("[0].valueInfo." + SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME,
          equalTo("aRootType"))
        .body("[0].valueInfo." + SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT,
          equalTo("aDataFormat"))
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);
  }

  @Test
  public void testQueryByProcessInstanceId() {
    String processInstanceId = MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_PROC_INST_ID;

    // GET
    given()
      .queryParam("processInstanceId", processInstanceId)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).processInstanceId(processInstanceId);

    reset(mockedQuery);

    // POST
    Map<String, String> jsonBody = new HashMap<String, String>();
    jsonBody.put("processInstanceId", processInstanceId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(jsonBody)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).processInstanceId(processInstanceId);
  }

  @Test
  public void testQueryByExecutionId() {
    String executionId = MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_EXEC_ID;

    // GET
    given()
      .queryParam("executionId", executionId)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).executionId(executionId);

    reset(mockedQuery);

    // POST
    Map<String, String> jsonBody = new HashMap<String, String>();
    jsonBody.put("executionId", executionId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(jsonBody)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).executionId(executionId);
  }

  @Test
  public void testQueryByOperationId() {
    String operationId = MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_OPERATION_ID;

    // GET
    given()
      .queryParam("userOperationId", operationId)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).userOperationId(operationId);

    reset(mockedQuery);

    // POST
    Map<String, String> jsonBody = new HashMap<String, String>();
    jsonBody.put("userOperationId", operationId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(jsonBody)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).userOperationId(operationId);
  }

  @Test
  public void testQueryByActivityInstanceId() {
    String activityInstanceId = MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_ACT_INST_ID;

    // GET
    given()
      .queryParam("activityInstanceId", activityInstanceId)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).activityInstanceId(activityInstanceId);

    reset(mockedQuery);

    // POST
    Map<String, String> jsonBody = new HashMap<String, String>();
    jsonBody.put("activityInstanceId", activityInstanceId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(jsonBody)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).activityInstanceId(activityInstanceId);
  }

  @Test
  public void testQueryByTaskId() {
    String taskId = MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_TASK_ID;

    // GET
    given()
      .queryParam("taskId", taskId)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).taskId(taskId);

    reset(mockedQuery);

    // POST
    Map<String, String> jsonBody = new HashMap<String, String>();
    jsonBody.put("taskId", taskId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(jsonBody)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).taskId(taskId);
  }

  @Test
  public void testQueryByVariableInstanceId() {
    String variableInstanceId = MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_ID;

    // GET
    given()
      .queryParam("variableInstanceId", variableInstanceId)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).variableInstanceId(variableInstanceId);

    reset(mockedQuery);

    // POST
    Map<String, String> jsonBody = new HashMap<String, String>();
    jsonBody.put("variableInstanceId", variableInstanceId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(jsonBody)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).variableInstanceId(variableInstanceId);
  }

  @Test
  public void testQueryByVariableTypeIn() {
    String aVariableType = "string";
    String anotherVariableType = "integer";

    // GET
    given()
      .queryParam("variableTypeIn", aVariableType + "," + anotherVariableType)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).variableTypeIn(aVariableType, anotherVariableType);

    reset(mockedQuery);

    // POST
    Map<String, Object> jsonBody = new HashMap<String, Object>();
    String[] variableTypeIn = { aVariableType, anotherVariableType};
    jsonBody.put("variableTypeIn", variableTypeIn);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(jsonBody)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).variableTypeIn(aVariableType, anotherVariableType);
  }

  @Test
  public void testQueryByFormFields() {
    // GET
    given()
      .queryParam("formFields", "true")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).formFields();

    reset(mockedQuery);

    // POST
    Map<String, Object> jsonBody = new HashMap<String, Object>();
    jsonBody.put("formFields", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(jsonBody)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).formFields();
  }

  @Test
  public void testQueryByVariableUpdates() {
    // GET
    given()
      .queryParam("variableUpdates", "true")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).variableUpdates();

    reset(mockedQuery);

    // POST
    Map<String, Object> jsonBody = new HashMap<String, Object>();
    jsonBody.put("variableUpdates", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(jsonBody)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).variableUpdates();
  }

  @Test
  public void testQueryByExcludeTaskDetails() {
    // GET
    given()
      .queryParam("excludeTaskDetails", "true")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).excludeTaskDetails();

    reset(mockedQuery);

    // POST
    Map<String, Object> jsonBody = new HashMap<String, Object>();
    jsonBody.put("excludeTaskDetails", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(jsonBody)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).excludeTaskDetails();
  }

  @Test
  public void testQueryByCaseInstanceId() {
    // GET
    given()
      .queryParam("caseInstanceId", MockProvider.EXAMPLE_CASE_INSTANCE_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).caseInstanceId(MockProvider.EXAMPLE_CASE_INSTANCE_ID);

    reset(mockedQuery);

    // POST
    Map<String, Object> jsonBody = new HashMap<String, Object>();
    jsonBody.put("caseInstanceId", MockProvider.EXAMPLE_CASE_INSTANCE_ID);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(jsonBody)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).caseInstanceId(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
  }

  @Test
  public void testQueryByCaseExecutionId() {
    // GET
    given()
      .queryParam("caseExecutionId", MockProvider.EXAMPLE_CASE_EXECUTION_ID)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).caseExecutionId(MockProvider.EXAMPLE_CASE_EXECUTION_ID);

    reset(mockedQuery);

    // POST
    Map<String, Object> jsonBody = new HashMap<String, Object>();
    jsonBody.put("caseExecutionId", MockProvider.EXAMPLE_CASE_EXECUTION_ID);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(jsonBody)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).caseExecutionId(MockProvider.EXAMPLE_CASE_EXECUTION_ID);
  }

  @Test
  public void testTenantIdListParameter() {
    mockedQuery = setUpMockedDetailsQuery(createMockHistoricDetailsTwoTenants());

    // GET
    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST)
      .queryParam("variableUpdates", "true")
      .queryParam("formFields", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).variableUpdates();
    verify(mockedQuery).formFields();
    verify(mockedQuery).list();

    verifyTenantIdListParameterResponse(response);

    mockedQuery = setUpMockedDetailsQuery(createMockHistoricDetailsTwoTenants());

    // POST
    Map<String, Object> jsonBody = new HashMap<String, Object>();
    String[] exampleTenantIdList = {MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID};
    jsonBody.put("tenantIdIn", exampleTenantIdList);
    jsonBody.put("variableUpdates", true);
    jsonBody.put("formFields", true);

    response = given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(jsonBody)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).variableUpdates();
    verify(mockedQuery).formFields();
    verify(mockedQuery).list();

    verifyTenantIdListParameterResponse(response);
  }

  @Test
  public void testQueryWithoutTenantIdQueryParameter() {
    // given
    List<HistoricDetail> details = new ArrayList<HistoricDetail>();

    historicUpdateBuilder = MockProvider.mockHistoricVariableUpdate(null);
    historicUpdateMock = historicUpdateBuilder.build();
    historicFormFieldMock = MockProvider.createMockHistoricFormField(null);

    details.add(historicUpdateMock);
    details.add(historicFormFieldMock);

    mockedQuery = setUpMockedDetailsQuery(details);

    // when
    Response response = given()
        .queryParam("withoutTenantId", true)
        .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .when()
        .get(HISTORIC_DETAIL_RESOURCE_URL);

    // then
    verify(mockedQuery).withoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(2);

    String returnedTenantId = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId).isEqualTo(null);
  }

  @Test
  public void testQueryWithoutTenantIdPostParameter() {
    // given
    List<HistoricDetail> details = new ArrayList<HistoricDetail>();

    historicUpdateBuilder = MockProvider.mockHistoricVariableUpdate(null);
    historicUpdateMock = historicUpdateBuilder.build();
    historicFormFieldMock = MockProvider.createMockHistoricFormField(null);

    details.add(historicUpdateMock);
    details.add(historicFormFieldMock);

    mockedQuery = setUpMockedDetailsQuery(details);
    Map<String, Object> queryParameters = Collections.singletonMap("withoutTenantId", (Object) true);

    // when
    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
        .expect()
        .statusCode(Status.OK.getStatusCode())
        .when()
        .post(HISTORIC_DETAIL_RESOURCE_URL);

    // then
    verify(mockedQuery).withoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(2);

    String returnedTenantId = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId).isEqualTo(null);
  }

  private void verifyTenantIdListParameterResponse(Response response) {
    String content = response.asString();
    List<String> historicDetails = from(content).getList("");
    assertThat(historicDetails).hasSize(4);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");
    String returnedTenantId3 = from(content).getString("[2].tenantId");
    String returnedTenantId4 = from(content).getString("[3].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId3).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId4).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

   @Test
  public void testByProcessInstanceIdIn () {
    String aProcessInstanceId = "aProcessInstanceId";
    String anotherProcessInstanceId = "anotherProcessInstanceId";

    // GET
    given()
      .queryParam("processInstanceIdIn", aProcessInstanceId + "," + anotherProcessInstanceId)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery)
      .processInstanceIdIn(aProcessInstanceId, anotherProcessInstanceId);

    reset(mockedQuery);

     // POST
     Map<String, Object> jsonBody = new HashMap<String, Object>();
     String[] processInstanceIdIn = {aProcessInstanceId, anotherProcessInstanceId};
     jsonBody.put("processInstanceIdIn", processInstanceIdIn);

     given()
       .contentType(POST_JSON_CONTENT_TYPE)
       .header("accept", MediaType.APPLICATION_JSON)
       .body(jsonBody)
     .then()
       .expect()
         .statusCode(Status.OK.getStatusCode())
     .when()
       .post(HISTORIC_DETAIL_RESOURCE_URL);

     verify(mockedQuery)
       .processInstanceIdIn(aProcessInstanceId, anotherProcessInstanceId);
  }

  @Test
  public void testByOccurredBefore () {
    // GET
    given()
      .queryParam("occurredBefore", MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_TIME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery)
      .occurredBefore(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_TIME));

    reset(mockedQuery);

    // POST
    Map<String, Object> jsonBody = new HashMap<String, Object>();
    jsonBody.put("occurredBefore", MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_TIME);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(jsonBody)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery)
      .occurredBefore(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_TIME));
  }

  @Test
  public void testByOccurredAfter () {
    // GET
    given()
      .queryParam("occurredAfter", MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_TIME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery)
      .occurredAfter(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_TIME));

    reset(mockedQuery);

    // POST
    Map<String, Object> jsonBody = new HashMap<String, Object>();
    jsonBody.put("occurredAfter", MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_TIME);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(jsonBody)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery)
      .occurredAfter(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_TIME));
  }

  @Test
  public void testGetQueryWhereFileWasDeleted() {
    doThrow(new IllegalArgumentException("Parameter 'filename' is null")).when(historicUpdateMock).getTypedValue();

    // GET
    expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).list();
    verify(mockedQuery).disableBinaryFetching();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testPostQueryWhereFileWasDeleted() {
    doThrow(new IllegalArgumentException("Parameter 'filename' is null")).when(historicUpdateMock).getTypedValue();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(Collections.emptyMap())
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).list();
    verify(mockedQuery).disableBinaryFetching();
    verifyNoMoreInteractions(mockedQuery);
  }

  private List<HistoricDetail> createMockHistoricDetailsTwoTenants() {
    List<HistoricDetail> mockHistoricDetails = MockProvider.createMockHistoricDetails(MockProvider.EXAMPLE_TENANT_ID);
    List<HistoricDetail> mockHistoricDetails2 = MockProvider.createMockHistoricDetails(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    mockHistoricDetails.addAll(mockHistoricDetails2);
    return mockHistoricDetails;
  }

}
