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
package org.camunda.bpm.engine.rest.optimize;

import io.restassured.response.Response;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.OptimizeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.helper.MockHistoricVariableUpdateBuilder;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_PRIMITIVE_VARIABLE_VALUE;
import static org.camunda.bpm.engine.rest.util.DateTimeUtils.DATE_FORMAT_WITH_TIMEZONE;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class OptimizeVariableUpdateRestServiceTest extends AbstractRestServiceTest {

  public static final String OPTIMIZE_VARIABLE_UPDATE_PATH =
    TEST_RESOURCE_ROOT_PATH + "/optimize/variable-update";

  protected OptimizeService mockedOptimizeService;
  protected ProcessEngine namedProcessEngine;

  protected HistoricVariableUpdate historicUpdateMock;
  protected MockHistoricVariableUpdateBuilder historicUpdateBuilder;

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  @Before
  public void setUpRuntimeData() {
    historicUpdateBuilder = MockProvider.mockHistoricVariableUpdate();
    historicUpdateMock = historicUpdateBuilder.build();

    mockedOptimizeService = mock(OptimizeService.class);
    ProcessEngineConfigurationImpl mockedConfig = mock(ProcessEngineConfigurationImpl.class);

    when(mockedOptimizeService.getHistoricVariableUpdates(any(Date.class), any(Date.class), anyBoolean(), anyInt())).thenReturn(Arrays.asList(historicUpdateMock));

    namedProcessEngine = getProcessEngine(MockProvider.EXAMPLE_PROCESS_ENGINE_NAME);
    when(namedProcessEngine.getProcessEngineConfiguration()).thenReturn(mockedConfig);
    when(mockedConfig.getOptimizeService()).thenReturn(mockedOptimizeService);
  }

  @Test
  public void testNoQueryParameters() {
    given()
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
    .when()
      .get(OPTIMIZE_VARIABLE_UPDATE_PATH);

    verify(mockedOptimizeService).getHistoricVariableUpdates(null, null, false, Integer.MAX_VALUE);
    verifyNoMoreInteractions(mockedOptimizeService);
  }

  @Test
  public void testOccurredAfterQueryParameter() {
    Date now = new Date();
    given()
      .queryParam("occurredAfter", DATE_FORMAT_WITH_TIMEZONE.format(now))
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
    .when()
      .get(OPTIMIZE_VARIABLE_UPDATE_PATH);

    verify(mockedOptimizeService).getHistoricVariableUpdates(now, null, false, Integer.MAX_VALUE);
    verifyNoMoreInteractions(mockedOptimizeService);
  }

  @Test
  public void testOccurredAtQueryParameter() {
    Date now = new Date();
    given()
      .queryParam("occurredAt", DATE_FORMAT_WITH_TIMEZONE.format(now))
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
    .when()
      .get(OPTIMIZE_VARIABLE_UPDATE_PATH);

    verify(mockedOptimizeService).getHistoricVariableUpdates(null, now, false, Integer.MAX_VALUE);
    verifyNoMoreInteractions(mockedOptimizeService);
  }

  @Test
  public void testExcludeObjectValuesQueryParameter() {
    given()
      .queryParam("excludeObjectValues", true)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
    .when()
      .get(OPTIMIZE_VARIABLE_UPDATE_PATH);

    verify(mockedOptimizeService).getHistoricVariableUpdates(null, null, true, Integer.MAX_VALUE);
    verifyNoMoreInteractions(mockedOptimizeService);
  }

  @Test
  public void testMaxResultsQueryParameter() {
    given()
      .queryParam("maxResults", 10)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
    .when()
      .get(OPTIMIZE_VARIABLE_UPDATE_PATH);

    verify(mockedOptimizeService).getHistoricVariableUpdates(null, null, false, 10);
    verifyNoMoreInteractions(mockedOptimizeService);
  }

  @Test
  public void testQueryParameterCombination() {
    Date now = new Date();
    given()
      .queryParam("occurredAfter", DATE_FORMAT_WITH_TIMEZONE.format(now))
      .queryParam("occurredAt", DATE_FORMAT_WITH_TIMEZONE.format(now))
      .queryParam("excludeObjectValues", true)
      .queryParam("maxResults", 10)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
    .when()
      .get(OPTIMIZE_VARIABLE_UPDATE_PATH);

    verify(mockedOptimizeService).getHistoricVariableUpdates(now, now, true, 10);
    verifyNoMoreInteractions(mockedOptimizeService);
  }

  @Test
  public void testQueryWhenFileWasDeleted() {
    doThrow(new IllegalArgumentException("Parameter 'filename' is null")).when(historicUpdateMock).getTypedValue();

    given()
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
    .when()
      .get(OPTIMIZE_VARIABLE_UPDATE_PATH);

    verify(mockedOptimizeService).getHistoricVariableUpdates(null, null, false, Integer.MAX_VALUE);
    verifyNoMoreInteractions(mockedOptimizeService);
  }

  @Test
  public void testPresenceOfSequenceCounterProperty() {
    final HistoricDetailVariableInstanceUpdateEntity mock = mock(HistoricDetailVariableInstanceUpdateEntity.class);
    when(mock.getSequenceCounter()).thenReturn(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_SEQUENCE_COUNTER);
    when(mock.getTypedValue()).thenReturn(EXAMPLE_PRIMITIVE_VARIABLE_VALUE);
    when(mockedOptimizeService.getHistoricVariableUpdates(null, null, false, Integer.MAX_VALUE))
      .thenReturn(Collections.singletonList(mock));

    final Response response = given()
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(MediaType.APPLICATION_JSON)
      .when()
        .get(OPTIMIZE_VARIABLE_UPDATE_PATH);

    String content = response.asString();
    long sequenceCounter = from(content).getLong("[0].sequenceCounter");

    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_SEQUENCE_COUNTER, sequenceCounter);
  }

}
