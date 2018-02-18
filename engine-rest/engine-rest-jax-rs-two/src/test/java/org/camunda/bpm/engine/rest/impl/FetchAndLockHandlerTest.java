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

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryTopicBuilder;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.rest.dto.externaltask.LockedExternalTaskDto;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.impl.fetchAndLock.FetchAndLockHandler;
import org.camunda.bpm.engine.rest.impl.fetchAndLock.FetchAndLockRequest;
import org.camunda.bpm.engine.rest.impl.fetchAndLock.FetchExternalTasksExtendedDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Tassilo Weidner
 */
public class FetchAndLockHandlerTest {

  private static final String AUTH_HEADER = FetchAndLockHandler.BASIC_AUTH_HEADER_PREFIX + "dXNlcklkOnMzY3JldA=="; // userId:s3cret

  private ProcessEngine processEngine;

  private ExternalTaskQueryTopicBuilder fetchTopicBuilder;
  private LockedExternalTask lockedExternalTaskMock;
  private IdentityService identityServiceMock;

  private HttpHeaders httpHeaders;

  private List<String> groupIds;
  private List<String> tenantIds;

  private FetchAndLockHandler fetchAndLockHandler;

  @Before
  public void setUpRuntimeData() {
    processEngine = mock(ProcessEngine.class);
    ExternalTaskService externalTaskService = mock(ExternalTaskService.class);
    when(processEngine.getExternalTaskService()).thenReturn(externalTaskService);
    fetchTopicBuilder = mock(ExternalTaskQueryTopicBuilder.class);
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
    identityServiceMock = mock(IdentityService.class);
    when(processEngine.getIdentityService()).thenReturn(identityServiceMock);

    httpHeaders = mock(HttpHeaders.class);
    when(httpHeaders.getHeaderString(anyString()))
      .thenReturn(AUTH_HEADER);
    when(identityServiceMock.checkPassword(MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_USER_PASSWORD))
      .thenReturn(true);

    List<Group> groupMocks = MockProvider.createMockGroups();
    groupIds = setupGroupQueryMock(groupMocks);

    List<Tenant> tenantMocks = Collections.singletonList(MockProvider.createMockTenant());
    tenantIds = setupTenantQueryMock(tenantMocks);


    fetchAndLockHandler = new FetchAndLockHandler();
  }

  @After
  public void tearDown() {
    fetchAndLockHandler.getHandlerThread().stop(); // provoke thread death
    ClockUtil.reset();
  }

  @Test
  public void shouldQueryAtLeast10TimesBeforeResumeRequest() throws InterruptedException {
    // given
    AsyncResponse asyncResponse1 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT),
      asyncResponse1, httpHeaders, processEngine);

    // when
    Thread.sleep(FetchAndLockHandler.MIN_TIMEOUT + 500);

    // then
    verify(fetchTopicBuilder, atLeast(10)).execute();
    verify(fetchTopicBuilder, atMost(15)).execute();
  }

  @Test
  public void shouldQueryAtLeast40TimesBeforeResumeRequests() throws InterruptedException {
    // given
    AsyncResponse asyncResponse1 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT),
      asyncResponse1, httpHeaders, processEngine);

    AsyncResponse asyncResponse2 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT),
      asyncResponse2, httpHeaders, processEngine);

    AsyncResponse asyncResponse3 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT),
      asyncResponse3, httpHeaders, processEngine);

    AsyncResponse asyncResponse4 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT),
      asyncResponse4, httpHeaders, processEngine);

    // when
    Thread.sleep(FetchAndLockHandler.MIN_TIMEOUT + 500);

    // then
    verify(fetchTopicBuilder, atLeast(40)).execute();
    verify(fetchTopicBuilder, atMost(50)).execute();
  }

  @Test
  public void shouldQueryAtLeast50TimesBeforeResumeRequests() throws InterruptedException {
    // given
    AsyncResponse asyncResponse1 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT * 2),
      asyncResponse1, httpHeaders, processEngine);

    AsyncResponse asyncResponse2 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT),
      asyncResponse2, httpHeaders, processEngine);

    AsyncResponse asyncResponse3 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT),
      asyncResponse3, httpHeaders, processEngine);

    AsyncResponse asyncResponse4 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT),
      asyncResponse4, httpHeaders, processEngine);

    // when
    Thread.sleep(FetchAndLockHandler.MIN_TIMEOUT * 2 + 500);

    // then
    verify(fetchTopicBuilder, atLeast(50)).execute();
    verify(fetchTopicBuilder, atMost(60)).execute();
  }

  @Test
  public void shouldResumeAllRequestsOrderedByTimeout() throws InterruptedException {
    // given
    AsyncResponse asyncResponse1 = mock(AsyncResponse.class);
    FetchAndLockRequest incomingRequest1 = new FetchAndLockRequest()
      .setProcessEngine(processEngine)
      .setAsyncResponse(asyncResponse1)
      .setDto(createDto(FetchAndLockHandler.MIN_TIMEOUT + 3000))
      .setAuthHeader(AUTH_HEADER);

    AsyncResponse asyncResponse2 = mock(AsyncResponse.class);
    FetchAndLockRequest incomingRequest2 = new FetchAndLockRequest()
      .setProcessEngine(processEngine)
      .setAsyncResponse(asyncResponse2)
      .setDto(createDto(FetchAndLockHandler.MIN_TIMEOUT + 2000))
      .setAuthHeader(AUTH_HEADER);

    AsyncResponse asyncResponse3 = mock(AsyncResponse.class);
    FetchAndLockRequest incomingRequest3 = new FetchAndLockRequest()
      .setProcessEngine(processEngine)
      .setAsyncResponse(asyncResponse3)
      .setDto(createDto(FetchAndLockHandler.MIN_TIMEOUT + 1000))
      .setAuthHeader(AUTH_HEADER);

    List<FetchAndLockRequest> pendingRequests = fetchAndLockHandler.getPendingRequests();
    pendingRequests.addAll(Arrays.asList(incomingRequest1, incomingRequest2, incomingRequest3));

    // when
    AsyncResponse asyncResponse4 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT + 10000),
      asyncResponse4, httpHeaders, processEngine);

    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + FetchAndLockHandler.MIN_TIMEOUT + 10000));

    fetchAndLockHandler.getHandlerThread().interrupt();
    Thread.sleep(500);

    // then
    InOrder inOrder = inOrder(asyncResponse1, asyncResponse2, asyncResponse3, asyncResponse4);
    inOrder.verify(asyncResponse3).resume(Collections.emptyList());
    inOrder.verify(asyncResponse2).resume(Collections.emptyList());
    inOrder.verify(asyncResponse1).resume(Collections.emptyList());
    inOrder.verify(asyncResponse4).resume(Collections.emptyList());
  }

  @Test
  public void shouldResumeRequestWithAvailableResultFirst() throws InterruptedException {
    // given
    when(fetchTopicBuilder.execute())
      .thenReturn(Collections.singletonList(lockedExternalTaskMock)) // return non-empty list once
      .thenReturn(Collections.<LockedExternalTask>emptyList());

    ClockUtil.setCurrentTime(ClockUtil.getCurrentTime());

    AsyncResponse asyncResponse1 = mock(AsyncResponse.class);
    FetchAndLockRequest incomingRequest1 = new FetchAndLockRequest()
      .setProcessEngine(processEngine)
      .setAsyncResponse(asyncResponse1)
      .setDto(createDto(FetchAndLockHandler.MIN_TIMEOUT))
      .setAuthHeader(AUTH_HEADER);

    AsyncResponse asyncResponse2 = mock(AsyncResponse.class);
    FetchAndLockRequest incomingRequest2 = new FetchAndLockRequest()
      .setProcessEngine(processEngine)
      .setAsyncResponse(asyncResponse2)
      .setDto(createDto(FetchAndLockHandler.MIN_TIMEOUT))
      .setAuthHeader(AUTH_HEADER);

    AsyncResponse asyncResponse3 = mock(AsyncResponse.class);
    FetchAndLockRequest incomingRequest3 = new FetchAndLockRequest()
      .setProcessEngine(processEngine)
      .setAsyncResponse(asyncResponse3)
      .setDto(createDto(FetchAndLockHandler.MIN_TIMEOUT))
      .setAuthHeader(AUTH_HEADER);

    List<FetchAndLockRequest> pendingRequests = fetchAndLockHandler.getPendingRequests();
    List<FetchAndLockRequest> allRequests = new ArrayList<FetchAndLockRequest>(Arrays.asList(
      incomingRequest1, incomingRequest2, incomingRequest3));
    pendingRequests.addAll(allRequests);

    assertThat(pendingRequests.size(), is(3));

    // when
    fetchAndLockHandler.getHandlerThread().interrupt();
    Thread.sleep(500);

    assertThat(pendingRequests.size(), is(2));

    List<FetchAndLockRequest> requestsWithoutResult = new ArrayList<FetchAndLockRequest>(pendingRequests);
    allRequests.removeAll(pendingRequests);
    FetchAndLockRequest requestWithResult = allRequests.get(0);
    verify(requestWithResult.getAsyncResponse()).resume(anyListOf(LockedExternalTaskDto.class));

    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + FetchAndLockHandler.MIN_TIMEOUT));

    fetchAndLockHandler.getHandlerThread().interrupt();
    Thread.sleep(500);

    // then
    for (FetchAndLockRequest fetchAndLockRequest : requestsWithoutResult) {
      verify(fetchAndLockRequest.getAsyncResponse()).resume(Collections.EMPTY_LIST);
    }
  }

  @Test
  public void shouldResumeSecondRequestByAvailableResult() {
    // given
    when(fetchTopicBuilder.execute())
      .thenReturn(Collections.<LockedExternalTask>emptyList())
      .thenReturn(Collections.singletonList(lockedExternalTaskMock))
      .thenReturn(Collections.<LockedExternalTask>emptyList());

    // when
    AsyncResponse asyncResponse1 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT + 1000),
      asyncResponse1, httpHeaders, processEngine);

    AsyncResponse asyncResponse2 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT + 5000),
      asyncResponse2, httpHeaders, processEngine);

    // then
    verify(asyncResponse2, times(1)).resume(anyList());
    verifyNoMoreInteractions(asyncResponse1);
  }

  @Test
  public void shouldAuthorizeUserWithGroupAndTenant()  {
    // given & when
    AsyncResponse asyncResponse1 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT + 1000),
      asyncResponse1, httpHeaders, processEngine);

    // then
    verify(identityServiceMock, atLeastOnce()).setAuthentication(MockProvider.EXAMPLE_USER_ID, groupIds, tenantIds);
  }

  @Test
  public void shouldThrowNotAuthorizedException() throws InterruptedException {
    // given
    HttpHeaders httpHeaders = mock(HttpHeaders.class);
    when(httpHeaders.getHeaderString(anyString()))
      .thenReturn(AUTH_HEADER)
      .thenReturn(AUTH_HEADER + "wroong");

    when(identityServiceMock.checkPassword(MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_USER_PASSWORD))
      .thenReturn(true)
      .thenReturn(false);

    // when
    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime()));

    AsyncResponse asyncResponse1 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT + 1000),
      asyncResponse1, httpHeaders, processEngine);

    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + FetchAndLockHandler.MIN_TIMEOUT + 1000));

    fetchAndLockHandler.getHandlerThread().interrupt();
    Thread.sleep(500);

    // then
    verify(asyncResponse1).resume(any(NotAuthorizedException.class));
  }

  @Test
  public void shouldShutdownHandlerThread() {
    // given
    fetchAndLockHandler.shutdown();
    fetchAndLockHandler.getHandlerThread().interrupt();

    // when
    AsyncResponse asyncResponse1 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT + 1000),
      asyncResponse1, httpHeaders, processEngine);

    AsyncResponse asyncResponse2 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT + 5000),
      asyncResponse2, httpHeaders, processEngine);

    AsyncResponse asyncResponse3 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT + 2000),
      asyncResponse3, httpHeaders, processEngine);

    AsyncResponse asyncResponse4 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandler.MIN_TIMEOUT + 3000),
      asyncResponse4, httpHeaders, processEngine);

    // then
    verifyNoMoreInteractions(asyncResponse1, asyncResponse2, asyncResponse3, asyncResponse4);
  }

  // helper /////////////////////////
  private FetchExternalTasksExtendedDto createDto(Long responseTimeout) {
    FetchExternalTasksExtendedDto fetchExternalTasksDto = new FetchExternalTasksExtendedDto();
    if (responseTimeout != null) {
      fetchExternalTasksDto.setAsyncResponseTimeout(responseTimeout);
    }
    fetchExternalTasksDto.setMaxTasks(5);
    fetchExternalTasksDto.setWorkerId("aWorkerId");
    FetchExternalTasksExtendedDto.FetchExternalTaskTopicDto topicDto =
      new FetchExternalTasksExtendedDto.FetchExternalTaskTopicDto();
    fetchExternalTasksDto.setTopics(Collections.singletonList(topicDto));
    topicDto.setTopicName("aTopicName");
    topicDto.setLockDuration(12354L);
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
