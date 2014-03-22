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

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.xml.registry.InvalidRequestException;

import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * @author Roman Smirnov
 *
 */
public class AbstractHistoricDetailRestServiceQueryTest extends AbstractRestServiceTest {

  protected static final String HISTORIC_DETAIL_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/detail";

  protected static final String HISTORIC_DETAIL_COUNT_RESOURCE_URL = HISTORIC_DETAIL_RESOURCE_URL + "/count";

  protected HistoricDetailQuery mockedQuery;

  @Before
  public void setUpRuntimeData() {
    mockedQuery = mock(HistoricDetailQuery.class);

    when(processEngine.getHistoryService().createHistoricDetailQuery()).thenReturn(mockedQuery);

    List<HistoricDetail> details = MockProvider.createMockHistoricDetails();

    when(mockedQuery.list()).thenReturn(details);
    when(mockedQuery.count()).thenReturn((long) details.size());
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
    expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).list();
    verify(mockedQuery).disableBinaryFetching();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("processInstanceId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
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
        .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);
  }

  @Test
  public void testMissingFirstResultParameter() {
    int maxResults = 10;

    given()
      .queryParam("maxResults", maxResults)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).listPage(0, maxResults);
  }

  @Test
  public void testMissingMaxResultsParameter() {
    int firstResult = 10;

    given()
      .queryParam("firstResult", firstResult)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_DETAIL_RESOURCE_URL);

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
    Response response = given()
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .get(HISTORIC_DETAIL_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> details = from(content).getList("");
    Assert.assertEquals("There should be two activity instance returned.", 2, details.size());
    Assert.assertNotNull("The returned details should not be null.", details.get(0));
    Assert.assertNotNull("The returned details should not be null.", details.get(1));

    String returnedId1 = from(content).getString("[0].id");
    String returnedProcessInstanceId1 = from(content).getString("[0].processInstanceId");
    String returnedActivityInstanceId1 = from(content).getString("[0].activityInstanceId");
    String returnedExecutionId1 = from(content).getString("[0].executionId");
    String returnedTaskId1 = from(content).getString("[0].taskId");
    Date returnedTime1 = DateTimeUtil.parseDateTime(from(content).getString("[0].time")).toDate();
    String returnedVariableName = from(content).getString("[0].variableName");
    String returnedVariableTypeName = from(content).getString("[0].variableTypeName");
    String returnedValue = from(content).getString("[0].value");
    int returnedRevision = from(content).getInt("[0].revision");

    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_ID, returnedId1);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_PROC_INST_ID, returnedProcessInstanceId1);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_ACT_INST_ID, returnedActivityInstanceId1);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_EXEC_ID, returnedExecutionId1);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_TASK_ID, returnedTaskId1);
    Assert.assertEquals(DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_TIME).toDate(), returnedTime1);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_NAME, returnedVariableName);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_TYPE_NAME, returnedVariableTypeName);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_VALUE, returnedValue);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_REVISION, returnedRevision);

    String returnedId2 = from(content).getString("[1].id");
    String returnedProcessInstanceId2 = from(content).getString("[1].processInstanceId");
    String returnedActivityInstanceId2 = from(content).getString("[1].activityInstanceId");
    String returnedExecutionId2 = from(content).getString("[1].executionId");
    String returnedTaskId2 = from(content).getString("[1].taskId");
    Date returnedTime2 = DateTimeUtil.parseDateTime(from(content).getString("[1].time")).toDate();
    String returnedFieldId = from(content).getString("[1].fieldId");
    String returnedFieldValue = from(content).getString("[1].fieldValue");

    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_ID, returnedId2);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_PROC_INST_ID, returnedProcessInstanceId2);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_ACT_INST_ID, returnedActivityInstanceId2);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_EXEC_ID, returnedExecutionId2);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_TASK_ID, returnedTaskId2);
    Assert.assertEquals(DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_TIME).toDate(), returnedTime2);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_FIELD_ID, returnedFieldId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_FORM_FIELD_VALUE, returnedFieldValue);
  }

  @Test
  public void testQueryByProcessInstanceId() {
    String processInstanceId = MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_PROC_INST_ID;
    given()
      .queryParam("processInstanceId", processInstanceId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).processInstanceId(processInstanceId);
  }

  @Test
  public void testQueryByExecutionId() {
    String executionId = MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_EXEC_ID;
    given()
      .queryParam("executionId", executionId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).executionId(executionId);
  }

  @Test
  public void testQueryByActivityInstanceId() {
    String activityInstanceId = MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_ACT_INST_ID;
    given()
      .queryParam("activityInstanceId", activityInstanceId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).activityInstanceId(activityInstanceId);
  }

  @Test
  public void testQueryByTaskId() {
    String taskId = MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_TASK_ID;
    given()
      .queryParam("taskId", taskId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).taskId(taskId);
  }

  @Test
  public void testQueryByVariableInstanceId() {
    String variableInstanceId = MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_ID;
    given()
      .queryParam("variableInstanceId", variableInstanceId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).variableInstanceId(variableInstanceId);
  }

  @Test
  public void testQueryByFormFields() {
    given()
      .queryParam("formFields", "true")
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).formFields();
  }

  @Test
  public void testQueryByVariableUpdates() {
    given()
      .queryParam("variableUpdates", "true")
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).variableUpdates();
  }

  @Test
  public void testQueryByExcludeTaskDetails() {
    given()
      .queryParam("excludeTaskDetails", "true")
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_DETAIL_RESOURCE_URL);

    verify(mockedQuery).excludeTaskDetails();
  }
}
