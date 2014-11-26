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
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.helper.MockHistoricVariableUpdateBuilder;
import org.camunda.bpm.engine.rest.helper.MockObjectValue;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.VariableTypeHelper;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * @author Daniel Meyer
 *
 */
public abstract class AbstractHistoricDetailRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String HISTORIC_DETAIL_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/detail";
  protected static final String HISTORIC_DETAIL_URL = HISTORIC_DETAIL_RESOURCE_URL + "/{id}";
  protected static final String VARIABLE_INSTANCE_BINARY_DATA_URL = HISTORIC_DETAIL_URL + "/data";

  protected HistoryService historyServiceMock;

  protected HistoricDetailQuery historicDetailQueryMock;

  @Before
  public void setupTestData() {
    historyServiceMock = mock(HistoryService.class);
    historicDetailQueryMock = mock(HistoricDetailQuery.class);

    // mock engine service.
    when(processEngine.getHistoryService()).thenReturn(historyServiceMock);
    when(historyServiceMock.createHistoricDetailQuery()).thenReturn(historicDetailQueryMock);
  }

  @Test
  public void testGetSingleDetail() {
    MockHistoricVariableUpdateBuilder builder = MockProvider.mockHistoricVariableUpdate();

    HistoricVariableUpdate detailMock = builder.build();

    when(historicDetailQueryMock.detailId(detailMock.getId())).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.disableBinaryFetching()).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.disableCustomObjectDeserialization()).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.singleResult()).thenReturn(detailMock);

    given().pathParam("id", MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_ID)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .and()
      .body("id", equalTo(builder.getId()))
      .body("variableName", equalTo(builder.getName()))
      .body("variableType", equalTo(VariableTypeHelper.toExpectedValueTypeName(builder.getTypedValue().getType())))
      .body("value", equalTo(builder.getValue()))
      .body("processInstanceId", equalTo(builder.getProcessInstanceId()))
      .body("errorMessage", equalTo(builder.getErrorMessage()))
      .body("activityInstanceId", equalTo(builder.getActivityInstanceId()))
      .body("revision", equalTo(builder.getRevision()))
      .body("time", equalTo(builder.getTime()))
      .body("taskId", equalTo(builder.getTaskId()))
      .body("executionId", equalTo(builder.getExecutionId()))
    .when().get(HISTORIC_DETAIL_URL);

    verify(historicDetailQueryMock, times(1)).disableBinaryFetching();
  }

  @Test
  public void testGetSingleVariableUpdateDeserialized() {
    ObjectValue serializedValue = MockObjectValue.fromObjectValue(
        Variables.objectValue("a value").serializationDataFormat("aDataFormat").create())
        .objectTypeName("aTypeName");

    MockHistoricVariableUpdateBuilder builder = MockProvider.mockHistoricVariableUpdate().typedValue(serializedValue);
    HistoricVariableUpdate variableInstanceMock = builder.build();

    when(historicDetailQueryMock.detailId(variableInstanceMock.getId())).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.disableBinaryFetching()).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.singleResult()).thenReturn(variableInstanceMock);

    given()
      .pathParam("id", builder.getId())
    .then().expect().statusCode(Status.OK.getStatusCode())
    .and()
      .body("id", equalTo(builder.getId()))
      .body("variableName", equalTo(builder.getName()))
      .body("variableType", equalTo(VariableTypeHelper.toExpectedValueTypeName(builder.getTypedValue().getType())))
      .body("value", equalTo("a value"))
      .body("valueInfo.serializationDataFormat", equalTo("aDataFormat"))
      .body("valueInfo.objectTypeName", equalTo("aTypeName"))
      .body("processInstanceId", equalTo(builder.getProcessInstanceId()))
      .body("errorMessage", equalTo(builder.getErrorMessage()))
      .body("activityInstanceId", equalTo(builder.getActivityInstanceId()))
      .body("revision", equalTo(builder.getRevision()))
      .body("time", equalTo(builder.getTime()))
      .body("taskId", equalTo(builder.getTaskId()))
      .body("executionId", equalTo(builder.getExecutionId()))
    .when().get(HISTORIC_DETAIL_URL);

    verify(historicDetailQueryMock, times(1)).disableBinaryFetching();
    verify(historicDetailQueryMock, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testGetSingleVariableUpdateSerialized() {
    ObjectValue serializedValue = Variables.serializedObjectValue("a serialized value")
        .serializationDataFormat("aDataFormat").objectTypeName("aTypeName").create();

    MockHistoricVariableUpdateBuilder builder = MockProvider.mockHistoricVariableUpdate().typedValue(serializedValue);
    HistoricVariableUpdate variableInstanceMock = builder.build();

    when(historicDetailQueryMock.detailId(variableInstanceMock.getId())).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.disableBinaryFetching()).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.disableCustomObjectDeserialization()).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.singleResult()).thenReturn(variableInstanceMock);

    given()
      .pathParam("id", builder.getId())
      .queryParam("deserializeValue", false)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .and()
      .body("id", equalTo(builder.getId()))
      .body("variableName", equalTo(builder.getName()))
      .body("variableType", equalTo(VariableTypeHelper.toExpectedValueTypeName(builder.getTypedValue().getType())))
      .body("value", equalTo("a serialized value"))
      .body("valueInfo.serializationDataFormat", equalTo("aDataFormat"))
      .body("valueInfo.objectTypeName", equalTo("aTypeName"))
      .body("processInstanceId", equalTo(builder.getProcessInstanceId()))
      .body("errorMessage", equalTo(builder.getErrorMessage()))
      .body("activityInstanceId", equalTo(builder.getActivityInstanceId()))
      .body("revision", equalTo(builder.getRevision()))
      .body("time", equalTo(builder.getTime()))
      .body("taskId", equalTo(builder.getTaskId()))
      .body("executionId", equalTo(builder.getExecutionId()))
    .when().get(HISTORIC_DETAIL_URL);

    verify(historicDetailQueryMock, times(1)).disableBinaryFetching();
    verify(historicDetailQueryMock, times(1)).disableCustomObjectDeserialization();
  }

  @Test
  public void testGetSingleVariableInstanceForBinaryVariable() {
    MockHistoricVariableUpdateBuilder builder = MockProvider.mockHistoricVariableUpdate();

    HistoricVariableUpdate detailMock = builder
        .typedValue(Variables.byteArrayValue(null))
        .build();

    when(historicDetailQueryMock.detailId(detailMock.getId())).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.disableBinaryFetching()).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.disableCustomObjectDeserialization()).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.singleResult()).thenReturn(detailMock);

    given().pathParam("id", MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_ID)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .and()
      .body("id", equalTo(builder.getId()))
      .body("variableName", equalTo(builder.getName()))
      .body("variableType", equalTo(VariableTypeHelper.toExpectedValueTypeName(builder.getTypedValue().getType())))
      .body("value", equalTo(builder.getValue()))
      .body("processInstanceId", equalTo(builder.getProcessInstanceId()))
      .body("errorMessage", equalTo(builder.getErrorMessage()))
      .body("activityInstanceId", equalTo(builder.getActivityInstanceId()))
      .body("revision", equalTo(builder.getRevision()))
      .body("time", equalTo(builder.getTime()))
      .body("taskId", equalTo(builder.getTaskId()))
      .body("executionId", equalTo(builder.getExecutionId()))
    .when().get(HISTORIC_DETAIL_URL);

    verify(historicDetailQueryMock, times(1)).disableBinaryFetching();

  }

  @Test
  public void testGetNonExistingVariableInstance() {

    String nonExistingId = "nonExistingId";

    when(historicDetailQueryMock.detailId(nonExistingId)).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.disableBinaryFetching()).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.disableCustomObjectDeserialization()).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.singleResult()).thenReturn(null);

    given().pathParam("id", nonExistingId)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
    .body(containsString("Historic detail with Id 'nonExistingId' does not exist."))
    .when().get(HISTORIC_DETAIL_URL);

    verify(historicDetailQueryMock, times(1)).disableBinaryFetching();

  }

  @Test
  public void testBinaryDataForBinaryVariable() {
    final byte[] byteContent = "some bytes".getBytes();

    MockHistoricVariableUpdateBuilder builder = MockProvider.mockHistoricVariableUpdate();

    HistoricVariableUpdate detailMock = builder
        .typedValue(Variables.byteArrayValue(byteContent))
        .build();

    when(historicDetailQueryMock.detailId(detailMock.getId())).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.disableCustomObjectDeserialization()).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.singleResult()).thenReturn(detailMock);

    Response response = given().pathParam("id", MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.BINARY.toString())
    .when().get(VARIABLE_INSTANCE_BINARY_DATA_URL);

    byte[] responseBytes = response.getBody().asByteArray();
    Assert.assertEquals(new String(byteContent), new String(responseBytes));
    verify(historicDetailQueryMock, never()).disableBinaryFetching();

  }

  @Test
  public void testBinaryDataForNonBinaryVariable() {
    HistoricVariableUpdate detailMock =  MockProvider.createMockHistoricVariableUpdate();

    when(historicDetailQueryMock.detailId(detailMock.getId())).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.disableCustomObjectDeserialization()).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.singleResult()).thenReturn(detailMock);

    given().pathParam("id", MockProvider.EXAMPLE_HISTORIC_VAR_UPDATE_ID)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body(containsString("Value of variable aHistoricVariableUpdateId is not a binary value"))
    .when().get(VARIABLE_INSTANCE_BINARY_DATA_URL);

    verify(historicDetailQueryMock, never()).disableBinaryFetching();

  }

  @Test
  public void testGetBinaryDataForNonExistingVariableInstance() {

    String nonExistingId = "nonExistingId";

    when(historicDetailQueryMock.detailId(nonExistingId)).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.disableCustomObjectDeserialization()).thenReturn(historicDetailQueryMock);
    when(historicDetailQueryMock.singleResult()).thenReturn(null);

    given().pathParam("id", nonExistingId)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
    .body(containsString("Historic detail instance with Id '"+nonExistingId+"' does not exist"))
    .when().get(VARIABLE_INSTANCE_BINARY_DATA_URL);

    verify(historicDetailQueryMock, never()).disableBinaryFetching();

  }
}
