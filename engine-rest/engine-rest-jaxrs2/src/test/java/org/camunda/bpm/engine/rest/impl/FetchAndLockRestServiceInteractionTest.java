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
package org.camunda.bpm.engine.rest.impl;

import com.jayway.restassured.http.ContentType;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryTopicBuilder;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.dto.externaltask.FetchExternalTasksExtendedDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Tassilo Weidner
 */
@RunWith(MockitoJUnitRunner.class)
public class FetchAndLockRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  private static final String FETCH_EXTERNAL_TASK_URL =  "/external-task/fetchAndLock";
  private static final String FETCH_EXTERNAL_TASK_URL_NAMED_ENGINE =  NamedProcessEngineRestServiceImpl.PATH + "/{name}" + FETCH_EXTERNAL_TASK_URL;

  @Mock
  private ExternalTaskService externalTaskService;

  @Mock
  private ExternalTaskQueryTopicBuilder fetchTopicBuilder;

  @Mock
  private IdentityService identityServiceMock;

  private LockedExternalTask lockedExternalTaskMock;

  private List<String> groupIds;
  private List<String> tenantIds;

  @Before
  public void setUpRuntimeData() {
    when(processEngine.getExternalTaskService())
      .thenReturn(externalTaskService);

    lockedExternalTaskMock = MockProvider.createMockLockedExternalTask();
    when(externalTaskService.fetchAndLock(anyInt(), any(String.class), any(Boolean.class)))
      .thenReturn(fetchTopicBuilder);

    when(fetchTopicBuilder.topic(any(String.class), anyLong()))
      .thenReturn(fetchTopicBuilder);

    when(fetchTopicBuilder.variables(anyListOf(String.class)))
      .thenReturn(fetchTopicBuilder);

    when(fetchTopicBuilder.enableCustomObjectDeserialization())
      .thenReturn(fetchTopicBuilder);

    // for authentication
    when(processEngine.getIdentityService())
      .thenReturn(identityServiceMock);

    List<Group> groupMocks = MockProvider.createMockGroups();
    groupIds = setupGroupQueryMock(groupMocks);

    List<Tenant> tenantMocks = Collections.singletonList(MockProvider.createMockTenant());
    tenantIds = setupTenantQueryMock(tenantMocks);

    new FetchAndLockContextListener().contextInitialized(null);
  }

  @Test
  public void shouldFetchAndLock() {
    when(fetchTopicBuilder.execute())
      .thenReturn(new ArrayList<LockedExternalTask>(Collections.singleton(lockedExternalTaskMock)));
    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(null, true, true, false);

    given()
      .contentType(ContentType.JSON)
      .body(fetchExternalTasksDto)
      .pathParam("name", "default")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("[0].id", equalTo(MockProvider.EXTERNAL_TASK_ID))
      .body("[0].topicName", equalTo(MockProvider.EXTERNAL_TASK_TOPIC_NAME))
      .body("[0].workerId", equalTo(MockProvider.EXTERNAL_TASK_WORKER_ID))
      .body("[0].lockExpirationTime", equalTo(MockProvider.EXTERNAL_TASK_LOCK_EXPIRATION_TIME))
      .body("[0].processInstanceId", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("[0].executionId", equalTo(MockProvider.EXAMPLE_EXECUTION_ID))
      .body("[0].activityId", equalTo(MockProvider.EXAMPLE_ACTIVITY_ID))
      .body("[0].activityInstanceId", equalTo(MockProvider.EXAMPLE_ACTIVITY_INSTANCE_ID))
      .body("[0].processDefinitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("[0].processDefinitionKey", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY))
      .body("[0].tenantId", equalTo(MockProvider.EXAMPLE_TENANT_ID))
      .body("[0].retries", equalTo(MockProvider.EXTERNAL_TASK_RETRIES))
      .body("[0].errorMessage", equalTo(MockProvider.EXTERNAL_TASK_ERROR_MESSAGE))
      .body("[0].errorMessage", equalTo(MockProvider.EXTERNAL_TASK_ERROR_MESSAGE))
      .body("[0].priority", equalTo(MockProvider.EXTERNAL_TASK_PRIORITY))
      .body("[0].variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME,
        notNullValue())
      .body("[0].variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME + ".value",
        equalTo(MockProvider.EXAMPLE_PRIMITIVE_VARIABLE_VALUE.getValue()))
      .body("[0].variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME + ".type",
        equalTo("String"))
    .when().post(FETCH_EXTERNAL_TASK_URL_NAMED_ENGINE);

    InOrder inOrder = inOrder(fetchTopicBuilder, externalTaskService);
    inOrder.verify(externalTaskService).fetchAndLock(5, "aWorkerId", true);
    inOrder.verify(fetchTopicBuilder).topic("aTopicName", 12354L);
    inOrder.verify(fetchTopicBuilder).variables(Collections.singletonList(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME));
    inOrder.verify(fetchTopicBuilder).execute();
    verifyNoMoreInteractions(fetchTopicBuilder, externalTaskService);
  }

  @Test
  public void shouldFetchWithoutVariables() {
    when(fetchTopicBuilder.execute())
      .thenReturn(new ArrayList<LockedExternalTask>(Collections.singleton(lockedExternalTaskMock)));
    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(null);

    given()
      .contentType(ContentType.JSON)
      .body(fetchExternalTasksDto)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .body("[0].id", equalTo(MockProvider.EXTERNAL_TASK_ID))
    .when()
      .post(FETCH_EXTERNAL_TASK_URL);

    InOrder inOrder = inOrder(fetchTopicBuilder, externalTaskService);
    inOrder.verify(externalTaskService).fetchAndLock(5, "aWorkerId", false);
    inOrder.verify(fetchTopicBuilder).topic("aTopicName", 12354L);
    inOrder.verify(fetchTopicBuilder).execute();
    verifyNoMoreInteractions(fetchTopicBuilder, externalTaskService);
  }

  @Test
  public void shouldFetchWithCustomObjectDeserializationEnabled() {
    when(fetchTopicBuilder.execute())
      .thenReturn(new ArrayList<LockedExternalTask>(Collections.singleton(lockedExternalTaskMock)));
    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(null, false, true, true);

    given()
      .contentType(ContentType.JSON)
      .body(fetchExternalTasksDto)
      .pathParam("name", "default")
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(FETCH_EXTERNAL_TASK_URL_NAMED_ENGINE);

    InOrder inOrder = inOrder(fetchTopicBuilder, externalTaskService);
    inOrder.verify(externalTaskService).fetchAndLock(5, "aWorkerId", false);
    inOrder.verify(fetchTopicBuilder).topic("aTopicName", 12354L);
    inOrder.verify(fetchTopicBuilder).variables(Collections.singletonList(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME));
    inOrder.verify(fetchTopicBuilder).enableCustomObjectDeserialization();
    inOrder.verify(fetchTopicBuilder).execute();
    verifyNoMoreInteractions(fetchTopicBuilder, externalTaskService);
  }

  @Test
  public void shouldThrowInvalidRequestExceptionOnMaxTimeoutExceeded() {
    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(FetchAndLockHandlerImpl.MAX_TIMEOUT + 1);

    given()
      .contentType(ContentType.JSON)
      .body(fetchExternalTasksDto)
      .pathParam("name", "default")
    .then()
      .expect()
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("The asynchronous response timeout cannot be set to a value greater than "))
        .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(FETCH_EXTERNAL_TASK_URL_NAMED_ENGINE);
  }

  @Test
  public void shouldThrowProcessEngineExceptionDuringTimeout() {
    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(500L);

    when(fetchTopicBuilder.execute())
      .thenReturn(Collections.<LockedExternalTask>emptyList())
      .thenReturn(Collections.<LockedExternalTask>emptyList())
      .thenThrow(new ProcessEngineException("anExceptionMessage"));

    given()
      .contentType(ContentType.JSON)
      .body(fetchExternalTasksDto)
      .pathParam("name", "default")
    .then()
      .expect()
        .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
        .body("message", equalTo("anExceptionMessage"))
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
    .when()
      .post(FETCH_EXTERNAL_TASK_URL_NAMED_ENGINE);

    verify(fetchTopicBuilder, atLeastOnce()).execute();
  }

  @Test
  public void shouldThrowProcessEngineExceptionNotDuringTimeout() {
    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(500L);

    when(fetchTopicBuilder.execute())
      .thenThrow(new ProcessEngineException("anExceptionMessage"));

    given()
      .contentType(ContentType.JSON)
      .body(fetchExternalTasksDto)
      .pathParam("name", "default")
    .then()
      .expect()
        .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
        .body("message", equalTo("anExceptionMessage"))
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
    .when()
      .post(FETCH_EXTERNAL_TASK_URL_NAMED_ENGINE);

    verify(fetchTopicBuilder, times(1)).execute();
  }

  @Test
  public void shouldResponseImmediatelyDueToAvailableTasks() {
    when(fetchTopicBuilder.execute())
      .thenReturn(new ArrayList<LockedExternalTask>(Collections.singleton(lockedExternalTaskMock)));

    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(500L);

    given()
      .contentType(ContentType.JSON)
      .body(fetchExternalTasksDto)
    .then()
      .expect()
        .body("size()", is(1))
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(FETCH_EXTERNAL_TASK_URL);
  }

  @Ignore
  @Test
  public void shouldSetAuthenticationProperly() {
    when(identityServiceMock.getCurrentAuthentication())
      .thenReturn(new Authentication(MockProvider.EXAMPLE_USER_ID, groupIds, tenantIds));

    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(500L);

    given()
      .contentType(ContentType.JSON)
      .body(fetchExternalTasksDto)
      .pathParam("name", "default")
    .when()
      .post(FETCH_EXTERNAL_TASK_URL_NAMED_ENGINE);

    ArgumentCaptor<Authentication> argumentCaptor = ArgumentCaptor.forClass(Authentication.class);
    verify(identityServiceMock, atLeastOnce()).setAuthentication(argumentCaptor.capture());

    assertThat(argumentCaptor.getValue().getUserId(), is(MockProvider.EXAMPLE_USER_ID));
    assertThat(argumentCaptor.getValue().getGroupIds(), is(groupIds));
    assertThat(argumentCaptor.getValue().getTenantIds(), is(tenantIds));
  }
  
  // helper /////////////////////////

  private FetchExternalTasksExtendedDto createDto(Long responseTimeout) {
    return createDto(responseTimeout, false, false, false);
  }
  
  private FetchExternalTasksExtendedDto createDto(Long responseTimeout, boolean usePriority, boolean withVariables, boolean withDeserialization) {
    FetchExternalTasksExtendedDto fetchExternalTasksDto = new FetchExternalTasksExtendedDto();
    if (responseTimeout != null) {
      fetchExternalTasksDto.setAsyncResponseTimeout(responseTimeout);
    }
    fetchExternalTasksDto.setMaxTasks(5);
    fetchExternalTasksDto.setWorkerId("aWorkerId");
    fetchExternalTasksDto.setUsePriority(usePriority);
    FetchExternalTasksExtendedDto.FetchExternalTaskTopicDto topicDto = new FetchExternalTasksExtendedDto.FetchExternalTaskTopicDto();
    fetchExternalTasksDto.setTopics(Collections.singletonList(topicDto));
    topicDto.setTopicName("aTopicName");
    topicDto.setLockDuration(12354L);
    if (withVariables) {
      topicDto.setVariables(Collections.singletonList(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME));
    }
    topicDto.setDeserializeValues(withDeserialization);
    fetchExternalTasksDto.setTopics(Collections.singletonList(topicDto));
    return fetchExternalTasksDto;
  }

  private List<String> setupGroupQueryMock(List<Group> groups) {
    GroupQuery mockGroupQuery = mock(GroupQuery.class);

    when(identityServiceMock.createGroupQuery()).thenReturn(mockGroupQuery);
    when(mockGroupQuery.groupMember(anyString())).thenReturn(mockGroupQuery);
    when(mockGroupQuery.list()).thenReturn(groups);

    List<String> groupIds = new ArrayList<String>();
    for (Group groupMock : groups) {
      groupIds.add(groupMock.getId());
    }
    return groupIds;
  }

  private List<String> setupTenantQueryMock(List<Tenant> tenants) {
    TenantQuery mockTenantQuery = mock(TenantQuery.class);

    when(identityServiceMock.createTenantQuery()).thenReturn(mockTenantQuery);
    when(mockTenantQuery.userMember(anyString())).thenReturn(mockTenantQuery);
    when(mockTenantQuery.includingGroupsOfUser(anyBoolean())).thenReturn(mockTenantQuery);
    when(mockTenantQuery.list()).thenReturn(tenants);

    List<String> tenantIds = new ArrayList<String>();
    for(Tenant tenant: tenants) {
      tenantIds.add(tenant.getId());
    }
    return tenantIds;
  }

}
