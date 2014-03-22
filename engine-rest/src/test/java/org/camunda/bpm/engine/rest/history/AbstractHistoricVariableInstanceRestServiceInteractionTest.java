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
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.camunda.bpm.engine.impl.variable.ByteArrayType;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
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

    HistoricVariableInstance variableInstanceMock = MockProvider.createMockHistoricVariableInstance();

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableBinaryFetching()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(variableInstanceMock);

    Response response = given().pathParam("id", MockProvider.EXAMPLE_VARIABLE_INSTANCE_ID)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(VARIABLE_INSTANCE_URL);

    verifyResponse(variableInstanceMock, response);
    verify(variableInstanceQueryMock, times(1)).disableBinaryFetching();

  }

  @Test
  public void testGetSingleVariableInstanceForBinaryVariable() {
    final ByteArrayType type = new ByteArrayType();

    HistoricVariableInstanceEntity variableInstanceMock = (HistoricVariableInstanceEntity) MockProvider.createMockHistoricVariableInstance();
    when(variableInstanceMock.getVariableType()).thenReturn(type);
    when(variableInstanceMock.getVariableTypeName()).thenReturn(type.getTypeNameForValue(null));
    when(variableInstanceMock.getValue()).thenReturn(null);

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableBinaryFetching()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(variableInstanceMock);

    Response response = given().pathParam("id", MockProvider.EXAMPLE_VARIABLE_INSTANCE_ID)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(VARIABLE_INSTANCE_URL);

    verifyResponse(variableInstanceMock, response);
    verify(variableInstanceQueryMock, times(1)).disableBinaryFetching();

  }

  @Test
  public void testGetNonExistingVariableInstance() {

    String nonExistingId = "nonExistingId";

    when(variableInstanceQueryMock.variableId(nonExistingId)).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableBinaryFetching()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(null);

    given().pathParam("id", nonExistingId)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
    .body(containsString("Variable instance with Id 'nonExistingId' does not exist."))
    .when().get(VARIABLE_INSTANCE_URL);

    verify(variableInstanceQueryMock, times(1)).disableBinaryFetching();

  }

  @Test
  public void testBinaryDataForBinaryVariable() {
    final ByteArrayType type = new ByteArrayType();
    final byte[] byteContent = "some bytes".getBytes();

    HistoricVariableInstanceEntity variableInstanceMock = (HistoricVariableInstanceEntity) MockProvider.createMockHistoricVariableInstance();
    when(variableInstanceMock.getVariableType()).thenReturn(type);
    when(variableInstanceMock.getVariableTypeName()).thenReturn(type.getTypeNameForValue(null));
    when(variableInstanceMock.getValue()).thenReturn(byteContent);

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
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
    HistoricVariableInstance variableInstanceMock =  MockProvider.createMockHistoricVariableInstance();

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(variableInstanceMock);

    given().pathParam("id", MockProvider.EXAMPLE_VARIABLE_INSTANCE_ID)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body(containsString("Variable instance with Id '"+variableInstanceMock.getId()+"' is not a binary variable"))
    .when().get(VARIABLE_INSTANCE_BINARY_DATA_URL);

    verify(variableInstanceQueryMock, never()).disableBinaryFetching();

  }

  @Test
  public void testGetBinaryDataForNonExistingVariableInstance() {

    String nonExistingId = "nonExistingId";

    when(variableInstanceQueryMock.variableId(nonExistingId)).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(null);

    given().pathParam("id", nonExistingId)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
    .body(containsString("Variable instance with Id 'nonExistingId' does not exist."))
    .when().get(VARIABLE_INSTANCE_BINARY_DATA_URL);

    verify(variableInstanceQueryMock, never()).disableBinaryFetching();

  }

  private void verifyResponse(HistoricVariableInstance variableInstanceMock, Response response) {
    String content = response.asString();

    JsonPath path = from(content);
    String returnedId = path.getString("id");
    String returnedName = path.getString("name");
    String returnedType = path.getString("type");
    String returnedValue = path.getString("value");
    String returnedProcessInstanceId = path.getString("processInstanceId");
    String returnedActivityInstanceId = path.getString("activityInstanceId");

    Assert.assertEquals(variableInstanceMock.getId(), returnedId);
    Assert.assertEquals(variableInstanceMock.getVariableName(), returnedName);
    Assert.assertEquals(variableInstanceMock.getVariableTypeName(), returnedType);
    Assert.assertEquals(variableInstanceMock.getValue(), returnedValue);
    Assert.assertEquals(variableInstanceMock.getProcessInstanceId(), returnedProcessInstanceId);
    Assert.assertEquals(variableInstanceMock.getActivtyInstanceId(), returnedActivityInstanceId);
  }
}
