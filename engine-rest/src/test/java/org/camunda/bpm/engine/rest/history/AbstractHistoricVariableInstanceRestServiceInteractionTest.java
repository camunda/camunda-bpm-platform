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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.helper.MockHistoricVariableInstanceBuilder;
import org.camunda.bpm.engine.rest.helper.MockObjectValue;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.VariableTypeHelper;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
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
public abstract class AbstractHistoricVariableInstanceRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String HISTORIC_VARIABLE_INSTANCE_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/variable-instance";
  protected static final String VARIABLE_INSTANCE_URL = HISTORIC_VARIABLE_INSTANCE_RESOURCE_URL + "/{id}";
  protected static final String VARIABLE_INSTANCE_BINARY_DATA_URL = VARIABLE_INSTANCE_URL + "/data";

  protected HistoryService historyServiceMock;

  protected HistoricVariableInstanceQuery variableInstanceQueryMock;

  @Before
  public void setupTestData() {
    historyServiceMock = mock(HistoryService.class);
    variableInstanceQueryMock = mock(HistoricVariableInstanceQuery.class);

    // mock engine service.
    when(processEngine.getHistoryService()).thenReturn(historyServiceMock);
    when(historyServiceMock.createHistoricVariableInstanceQuery()).thenReturn(variableInstanceQueryMock);
  }

  @Test
  public void testGetSingleVariableInstance() {
    MockHistoricVariableInstanceBuilder builder = MockProvider.mockHistoricVariableInstance();

    HistoricVariableInstance variableInstanceMock = builder.build();

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableBinaryFetching()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableCustomObjectDeserialization()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(variableInstanceMock);

    given().pathParam("id", builder.getId())
    .then().expect().statusCode(Status.OK.getStatusCode())
    .and()
      .body("id", equalTo(builder.getId()))
      .body("name", equalTo(builder.getName()))
      .body("type", equalTo(VariableTypeHelper.toExpectedValueTypeName(builder.getTypedValue().getType())))
      .body("value", equalTo(builder.getValue()))
      .body("processInstanceId", equalTo(builder.getProcessInstanceId()))
      .body("errorMessage", equalTo(builder.getErrorMessage()))
      .body("activityInstanceId", equalTo(builder.getActivityInstanceId()))
    .when().get(VARIABLE_INSTANCE_URL);

    verify(variableInstanceQueryMock, times(1)).disableBinaryFetching();

  }

  @Test
  public void testGetSingleVariableInstanceDeserialized() {
    ObjectValue serializedValue = MockObjectValue.fromObjectValue(
        Variables.objectValue("a value").serializationDataFormat("aDataFormat").create())
        .objectTypeName("aTypeName");

    MockHistoricVariableInstanceBuilder builder = MockProvider.mockHistoricVariableInstance().typedValue(serializedValue);
    HistoricVariableInstance variableInstanceMock = builder.build();

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableBinaryFetching()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(variableInstanceMock);

    given()
      .pathParam("id", MockProvider.EXAMPLE_VARIABLE_INSTANCE_ID)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .and()
      .body("id", equalTo(builder.getId()))
      .body("name", equalTo(builder.getName()))
      .body("type", equalTo(VariableTypeHelper.toExpectedValueTypeName(builder.getTypedValue().getType())))
      .body("value", equalTo("a value"))
      .body("valueInfo.serializationDataFormat", equalTo("aDataFormat"))
      .body("valueInfo.objectTypeName", equalTo("aTypeName"))
      .body("processInstanceId", equalTo(builder.getProcessInstanceId()))
      .body("errorMessage", equalTo(builder.getErrorMessage()))
      .body("activityInstanceId", equalTo(builder.getActivityInstanceId()))
    .when().get(VARIABLE_INSTANCE_URL);

    verify(variableInstanceQueryMock, times(1)).disableBinaryFetching();
    verify(variableInstanceQueryMock, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testGetSingleVariableInstanceSerialized() {
    ObjectValue serializedValue = Variables.serializedObjectValue("a serialized value")
        .serializationDataFormat("aDataFormat").objectTypeName("aTypeName").create();

    MockHistoricVariableInstanceBuilder builder = MockProvider.mockHistoricVariableInstance().typedValue(serializedValue);
    HistoricVariableInstance variableInstanceMock = builder.build();

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableBinaryFetching()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableCustomObjectDeserialization()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(variableInstanceMock);

    given()
      .pathParam("id", MockProvider.EXAMPLE_VARIABLE_INSTANCE_ID)
      .queryParam("deserializeValue", false)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .and()
      .body("id", equalTo(builder.getId()))
      .body("name", equalTo(builder.getName()))
      .body("type", equalTo(VariableTypeHelper.toExpectedValueTypeName(builder.getTypedValue().getType())))
      .body("value", equalTo("a serialized value"))
      .body("valueInfo.serializationDataFormat", equalTo("aDataFormat"))
      .body("valueInfo.objectTypeName", equalTo("aTypeName"))
      .body("processInstanceId", equalTo(builder.getProcessInstanceId()))
      .body("errorMessage", equalTo(builder.getErrorMessage()))
      .body("activityInstanceId", equalTo(builder.getActivityInstanceId()))
    .when().get(VARIABLE_INSTANCE_URL);

    verify(variableInstanceQueryMock, times(1)).disableBinaryFetching();
    verify(variableInstanceQueryMock, times(1)).disableCustomObjectDeserialization();
  }

  @Test
  public void testGetSingleVariableInstanceForBinaryVariable() {
    MockHistoricVariableInstanceBuilder builder = MockProvider.mockHistoricVariableInstance();

    HistoricVariableInstance variableInstanceMock = builder
      .typedValue(Variables.byteArrayValue(null))
      .build();

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableBinaryFetching()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableCustomObjectDeserialization()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(variableInstanceMock);

    given().pathParam("id", MockProvider.EXAMPLE_VARIABLE_INSTANCE_ID)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .and()
      .body("type", equalTo(VariableTypeHelper.toExpectedValueTypeName(ValueType.BYTES)))
      .body("value", nullValue())
    .when().get(VARIABLE_INSTANCE_URL);

    verify(variableInstanceQueryMock, times(1)).disableBinaryFetching();

  }

  @Test
  public void testGetNonExistingVariableInstance() {

    String nonExistingId = "nonExistingId";

    when(variableInstanceQueryMock.variableId(nonExistingId)).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableBinaryFetching()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableCustomObjectDeserialization()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(null);

    given().pathParam("id", nonExistingId)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
    .body(containsString("Variable instance with Id 'nonExistingId' does not exist."))
    .when().get(VARIABLE_INSTANCE_URL);

    verify(variableInstanceQueryMock, times(1)).disableBinaryFetching();

  }

  @Test
  public void testBinaryDataForBinaryVariable() {
    final byte[] byteContent = "some bytes".getBytes();
    HistoricVariableInstance variableInstanceMock = MockProvider.mockHistoricVariableInstance()
        .typedValue(Variables.byteArrayValue(byteContent))
        .build();

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableCustomObjectDeserialization()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(variableInstanceMock);

    Response response = given().pathParam("id", MockProvider.EXAMPLE_VARIABLE_INSTANCE_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.BINARY.toString())
    .when().get(VARIABLE_INSTANCE_BINARY_DATA_URL);

    byte[] responseBytes = response.getBody().asByteArray();
    Assert.assertEquals(new String(byteContent), new String(responseBytes));
    verify(variableInstanceQueryMock, never()).disableBinaryFetching();

  }

  @Test
  public void testBinaryDataForNonBinaryVariable() {
    HistoricVariableInstance variableInstanceMock = MockProvider.createMockHistoricVariableInstance();

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableCustomObjectDeserialization()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(variableInstanceMock);

    given().pathParam("id", MockProvider.EXAMPLE_VARIABLE_INSTANCE_ID)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body(containsString("Value of variable "+variableInstanceMock.getId()+" is not a binary value"))
    .when().get(VARIABLE_INSTANCE_BINARY_DATA_URL);

    verify(variableInstanceQueryMock, never()).disableBinaryFetching();

  }

  @Test
  public void testGetBinaryDataForNonExistingVariableInstance() {

    String nonExistingId = "nonExistingId";

    when(variableInstanceQueryMock.variableId(nonExistingId)).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableCustomObjectDeserialization()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(null);

    given().pathParam("id", nonExistingId)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
    .body(containsString("Historic variable instance with Id 'nonExistingId' does not exist."))
    .when().get(VARIABLE_INSTANCE_BINARY_DATA_URL);

    verify(variableInstanceQueryMock, never()).disableBinaryFetching();

  }

}
