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
package org.camunda.bpm.engine.rest;

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.restassured.response.Response;

public class EventSubscriptionRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String EVENT_SUBSCRIPTION_URL = TEST_RESOURCE_ROOT_PATH + "/event-subscription";
  protected static final String EVENT_SUBSCRIPTION_COUNT_QUERY_URL = EVENT_SUBSCRIPTION_URL + "/count";

  private EventSubscriptionQuery mockedEventSubscriptionQuery;

  @Before
  public void setUpRuntimeData() {
    mockedEventSubscriptionQuery = setUpMockEventSubscriptionQuery(createMockEventSubscriptionList());
  }

  private EventSubscriptionQuery setUpMockEventSubscriptionQuery(List<EventSubscription> mockedInstances) {
    EventSubscriptionQuery sampleEventSubscriptionsQuery = mock(EventSubscriptionQuery.class);
    when(sampleEventSubscriptionsQuery.list()).thenReturn(mockedInstances);
    when(sampleEventSubscriptionsQuery.count()).thenReturn((long) mockedInstances.size());
    when(processEngine.getRuntimeService().createEventSubscriptionQuery()).thenReturn(sampleEventSubscriptionsQuery);
    return sampleEventSubscriptionsQuery;
  }

  private List<EventSubscription> createMockEventSubscriptionList() {
    List<EventSubscription> mocks = new ArrayList<EventSubscription>();

    mocks.add(MockProvider.createMockEventSubscription());
    return mocks;
  }

  @Test
  public void testEmptyQuery() {
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(EVENT_SUBSCRIPTION_URL);
  }

  @Test
  public void testEventSubscriptionRetrieval() {
    Response response =
        given()
          .then().expect()
            .statusCode(Status.OK.getStatusCode())
          .when().get(EVENT_SUBSCRIPTION_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(mockedEventSubscriptionQuery);
    inOrder.verify(mockedEventSubscriptionQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one event subscription returned.", 1, instances.size());
    Assert.assertNotNull("There should be one event subscription returned", instances.get(0));

    String returnedEventSubscriptionId = from(content).getString("[0].id");
    String returnedEventType = from(content).getString("[0].eventType");
    String returnedEventName = from(content).getString("[0].eventName");
    String returnedExecutionId = from(content).getString("[0].executionId");
    String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
    String returnedActivityId = from(content).getString("[0].activityId");
    String returnedCreatedDate = from(content).getString("[0].createdDate");
    String returnedTenantId = from(content).getString("[0].tenantId");

    Assert.assertEquals(MockProvider.EXAMPLE_EVENT_SUBSCRIPTION_ID, returnedEventSubscriptionId);
    Assert.assertEquals(MockProvider.EXAMPLE_EVENT_SUBSCRIPTION_TYPE, returnedEventType);
    Assert.assertEquals(MockProvider.EXAMPLE_EVENT_SUBSCRIPTION_NAME, returnedEventName);
    Assert.assertEquals(MockProvider.EXAMPLE_EXECUTION_ID, returnedExecutionId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_ACTIVITY_ID, returnedActivityId);
    Assert.assertEquals(MockProvider.EXAMPLE_EVENT_SUBSCRIPTION_CREATION_DATE, returnedCreatedDate);
    Assert.assertEquals(MockProvider.EXAMPLE_TENANT_ID, returnedTenantId);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("definitionId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  @Test
  public void testSortByParameterOnly() {
    given()
      .queryParam("sortBy", "created")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(EVENT_SUBSCRIPTION_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given()
      .queryParam("sortOrder", "asc")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(EVENT_SUBSCRIPTION_URL);
  }

  @Test
  public void testNoParametersQuery() {
    expect().statusCode(Status.OK.getStatusCode())
    .when().get(EVENT_SUBSCRIPTION_URL);

    verify(mockedEventSubscriptionQuery).list();
    verifyNoMoreInteractions(mockedEventSubscriptionQuery);
  }

  @Test
  public void testQueryParameters() {
    Map<String, String> queryParameters = getCompleteQueryParameters();

    given()
      .queryParams(queryParameters)
    .expect().statusCode(Status.OK.getStatusCode())
    .when().get(EVENT_SUBSCRIPTION_URL);

    verify(mockedEventSubscriptionQuery).eventSubscriptionId(queryParameters.get("eventSubscriptionId"));
    verify(mockedEventSubscriptionQuery).eventType(queryParameters.get("eventType"));
    verify(mockedEventSubscriptionQuery).eventName(queryParameters.get("eventName"));
    verify(mockedEventSubscriptionQuery).executionId(queryParameters.get("executionId"));
    verify(mockedEventSubscriptionQuery).processInstanceId(queryParameters.get("processInstanceId"));
    verify(mockedEventSubscriptionQuery).activityId(queryParameters.get("activityId"));
    verify(mockedEventSubscriptionQuery).list();
  }

  @Test
  public void testTenantIdListParameter() {
    mockedEventSubscriptionQuery = setUpMockEventSubscriptionQuery(createMockEventSubscriptionTwoTenants());

    Response response = 
        given()
          .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when().get(EVENT_SUBSCRIPTION_URL);

    verify(mockedEventSubscriptionQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedEventSubscriptionQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    assertThat(instances).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }


  @Test
  public void testWithoutTenantIdParameter() {
    mockedEventSubscriptionQuery = setUpMockEventSubscriptionQuery(Arrays.asList(MockProvider.createMockEventSubscription(null)));

    Response response = 
        given()
          .queryParam("withoutTenantId", true)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when().get(EVENT_SUBSCRIPTION_URL);

    verify(mockedEventSubscriptionQuery).withoutTenantId();
    verify(mockedEventSubscriptionQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(1);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId1).isEqualTo(null);
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedEventSubscriptionQuery);
    executeAndVerifySorting("created", "asc", Status.OK);
    inOrder.verify(mockedEventSubscriptionQuery).orderByCreated();
    inOrder.verify(mockedEventSubscriptionQuery).asc();

    inOrder = Mockito.inOrder(mockedEventSubscriptionQuery);
    executeAndVerifySorting("created", "desc", Status.OK);
    inOrder.verify(mockedEventSubscriptionQuery).orderByCreated();
    inOrder.verify(mockedEventSubscriptionQuery).desc();

    inOrder = Mockito.inOrder(mockedEventSubscriptionQuery);
    executeAndVerifySorting("tenantId", "asc", Status.OK);
    inOrder.verify(mockedEventSubscriptionQuery).orderByTenantId();
    inOrder.verify(mockedEventSubscriptionQuery).asc();

    inOrder = Mockito.inOrder(mockedEventSubscriptionQuery);
    executeAndVerifySorting("tenantId", "desc", Status.OK);
    inOrder.verify(mockedEventSubscriptionQuery).orderByTenantId();
    inOrder.verify(mockedEventSubscriptionQuery).desc();

  }

  @Test
  public void testSuccessfulPagination() {

    int firstResult = 0;
    int maxResults = 10;
    given()
      .queryParam("firstResult", firstResult)
      .queryParam("maxResults", maxResults)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(EVENT_SUBSCRIPTION_URL);

    verify(mockedEventSubscriptionQuery).listPage(firstResult, maxResults);
  }

  /**
   * If parameter "firstResult" is missing, we expect 0 as default.
   */
  @Test
  public void testMissingFirstResultParameter() {
    int maxResults = 10;
    given()
      .queryParam("maxResults", maxResults)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(EVENT_SUBSCRIPTION_URL);

    verify(mockedEventSubscriptionQuery).listPage(0, maxResults);
  }

  /**
   * If parameter "maxResults" is missing, we expect Integer.MAX_VALUE as
   * default.
   */
  @Test
  public void testMissingMaxResultsParameter() {
    int firstResult = 10;
    given()
      .queryParam("firstResult", firstResult)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when().get(EVENT_SUBSCRIPTION_URL);

    verify(mockedEventSubscriptionQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode()).body("count", equalTo(1))
    .when().get(EVENT_SUBSCRIPTION_COUNT_QUERY_URL);

    verify(mockedEventSubscriptionQuery).count();
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then().expect()
      .statusCode(expectedStatus.getStatusCode())
    .when().get(EVENT_SUBSCRIPTION_URL);
  }
  
  private Map<String, String> getCompleteQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("eventSubscriptionId", "anEventSubscriptionId");
    parameters.put("eventType", "aEventType");
    parameters.put("eventName", "aEventName");
    parameters.put("executionId", "aExecutionId");
    parameters.put("processInstanceId", "aProcessInstanceId");
    parameters.put("activityId", "aActivityId");

    return parameters;
  }

  private List<EventSubscription> createMockEventSubscriptionTwoTenants() {
    return Arrays.asList(
        MockProvider.createMockEventSubscription(MockProvider.EXAMPLE_TENANT_ID),
        MockProvider.createMockEventSubscription(MockProvider.ANOTHER_EXAMPLE_TENANT_ID)
      );
  }
}
