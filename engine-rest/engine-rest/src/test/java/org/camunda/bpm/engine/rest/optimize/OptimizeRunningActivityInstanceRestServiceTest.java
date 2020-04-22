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
import org.camunda.bpm.engine.impl.OptimizeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.Date;

import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.rest.util.DateTimeUtils.DATE_FORMAT_WITH_TIMEZONE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


public class OptimizeRunningActivityInstanceRestServiceTest extends AbstractRestServiceTest {

  public static final String OPTIMIZE_RUNNING_ACTIVITY_INSTANCE_PATH =
    TEST_RESOURCE_ROOT_PATH + "/optimize/activity-instance/running";

  protected OptimizeService mockedOptimizeService;
  protected ProcessEngine namedProcessEngine;

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  @Before
  public void setUpRuntimeData() {
    mockedOptimizeService = mock(OptimizeService.class);
    ProcessEngineConfigurationImpl mockedConfig = mock(ProcessEngineConfigurationImpl.class);


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
      .get(OPTIMIZE_RUNNING_ACTIVITY_INSTANCE_PATH);

    verify(mockedOptimizeService).getRunningHistoricActivityInstances(null, null, Integer.MAX_VALUE);
    verifyNoMoreInteractions(mockedOptimizeService);
  }

  @Test
  public void testStartedAfterQueryParameter() {
    Date now = new Date();
    given()
      .queryParam("startedAfter", DATE_FORMAT_WITH_TIMEZONE.format(now))
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
    .when()
      .get(OPTIMIZE_RUNNING_ACTIVITY_INSTANCE_PATH);

    verify(mockedOptimizeService).getRunningHistoricActivityInstances(now, null, Integer.MAX_VALUE);
    verifyNoMoreInteractions(mockedOptimizeService);
  }

  @Test
  public void testStartedAtQueryParameter() {
    Date now = new Date();
    given()
      .queryParam("startedAt", DATE_FORMAT_WITH_TIMEZONE.format(now))
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
    .when()
      .get(OPTIMIZE_RUNNING_ACTIVITY_INSTANCE_PATH);

    verify(mockedOptimizeService).getRunningHistoricActivityInstances(null, now, Integer.MAX_VALUE);
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
      .get(OPTIMIZE_RUNNING_ACTIVITY_INSTANCE_PATH);

    verify(mockedOptimizeService).getRunningHistoricActivityInstances(null, null, 10);
    verifyNoMoreInteractions(mockedOptimizeService);
  }

  @Test
  public void testQueryParameterCombination() {
    Date now = new Date();
    given()
      .queryParam("startedAfter", DATE_FORMAT_WITH_TIMEZONE.format(now))
      .queryParam("startedAt", DATE_FORMAT_WITH_TIMEZONE.format(now))
      .queryParam("maxResults", 10)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
    .when()
      .get(OPTIMIZE_RUNNING_ACTIVITY_INSTANCE_PATH);

    verify(mockedOptimizeService).getRunningHistoricActivityInstances(now, now, 10);
    verifyNoMoreInteractions(mockedOptimizeService);
  }

  @Test
  public void testPresenceOfSequenceCounterProperty() {
    final HistoricActivityInstanceEntity mock = mock(HistoricActivityInstanceEntity.class);
    when(mock.getSequenceCounter()).thenReturn(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_SEQUENCE_COUNTER);
    when(mockedOptimizeService.getRunningHistoricActivityInstances(null, null, Integer.MAX_VALUE))
      .thenReturn(Collections.singletonList(mock));

    final Response response = given()
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(MediaType.APPLICATION_JSON)
      .when()
        .get(OPTIMIZE_RUNNING_ACTIVITY_INSTANCE_PATH);

    String content = response.asString();
    long sequenceCounter = from(content).getLong("[0].sequenceCounter");

    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_SEQUENCE_COUNTER, sequenceCounter);
  }

}
