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
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryTopicBuilder;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.rest.dto.externaltask.FetchExternalTasksExtendedDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.AsyncResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tassilo Weidner
 */
@RunWith(MockitoJUnitRunner.class)
public class FetchAndLockHandlerTest {

  @Mock
  protected ProcessEngine processEngine;

  @Mock
  protected IdentityService identityService;

  @Mock
  protected ExternalTaskService externalTaskService;

  @Mock
  protected ExternalTaskQueryTopicBuilder fetchTopicBuilder;

  @Spy
  protected FetchAndLockHandlerImpl handler;

  protected LockedExternalTask lockedExternalTaskMock;

  protected static final Date START_DATE = new Date(1457326800000L);

  @Before
  public void initMocks() {
    when(processEngine.getIdentityService()).thenReturn(identityService);
    when(processEngine.getExternalTaskService()).thenReturn(externalTaskService);
    when(processEngine.getName()).thenReturn("default");

    when(externalTaskService.fetchAndLock(anyInt(), any(String.class), any(Boolean.class)))
      .thenReturn(fetchTopicBuilder);
    when(fetchTopicBuilder.topic(any(String.class), anyLong()))
      .thenReturn(fetchTopicBuilder);
    when(fetchTopicBuilder.variables(anyListOf(String.class)))
      .thenReturn(fetchTopicBuilder);
    when(fetchTopicBuilder.enableCustomObjectDeserialization())
      .thenReturn(fetchTopicBuilder);

    doNothing().when(handler).suspend(anyLong());
    doReturn(processEngine).when(handler).getProcessEngine(any(FetchAndLockRequest.class));

    lockedExternalTaskMock = MockProvider.createMockLockedExternalTask();
  }

  @Before
  public void setClock() {
    ClockUtil.setCurrentTime(START_DATE);
  }

  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  @Test
  public void shouldResumeAsyncResponseDueToAvailableTasks() {
    // given
    List<LockedExternalTask> tasks = new ArrayList<LockedExternalTask>();
    tasks.add(lockedExternalTaskMock);
    doReturn(tasks).when(fetchTopicBuilder).execute();

    AsyncResponse asyncResponse = mock(AsyncResponse.class);
    handler.addPendingRequest(createDto(5000L), asyncResponse, processEngine);

    // when
    handler.acquire();

    // then
    verify(asyncResponse).resume(argThat(IsCollectionWithSize.hasSize(1)));
    assertThat(handler.getPendingRequests().size(), is(0));
    verify(handler).suspend(Long.MAX_VALUE - ClockUtil.getCurrentTime().getTime());
  }

  @Test
  public void shouldNotResumeAsyncResponseDueToNoAvailableTasks() {
    // given
    doReturn(Collections.emptyList()).when(fetchTopicBuilder).execute();

    AsyncResponse asyncResponse = mock(AsyncResponse.class);
    handler.addPendingRequest(createDto(5000L), asyncResponse, processEngine);

    // when
    handler.acquire();

    // then
    verify(asyncResponse, never()).resume(any());
    assertThat(handler.getPendingRequests().size(), is(1));
    verify(handler).suspend(5000L);
  }

  @Test
  public void shouldResumeAsyncResponseDueToTimeoutExpired_1() {
    // given
    doReturn(Collections.emptyList()).when(fetchTopicBuilder).execute();

    AsyncResponse asyncResponse = mock(AsyncResponse.class);
    handler.addPendingRequest(createDto(5000L), asyncResponse, processEngine);
    handler.acquire();

    // assume
    assertThat(handler.getPendingRequests().size(), is(1));
    verify(handler).suspend(5000L);

    List<LockedExternalTask> tasks = new ArrayList<LockedExternalTask>();
    tasks.add(lockedExternalTaskMock);
    doReturn(tasks).when(fetchTopicBuilder).execute();

    addSecondsToClock(5);

    // when
    handler.acquire();

    // then
    verify(asyncResponse).resume(argThat(IsCollectionWithSize.hasSize(1)));
    assertThat(handler.getPendingRequests().size(), is(0));
    verify(handler).suspend(Long.MAX_VALUE - ClockUtil.getCurrentTime().getTime());
  }

  @Test
  public void shouldResumeAsyncResponseDueToTimeoutExpired_2() {
    // given
    doReturn(Collections.emptyList()).when(fetchTopicBuilder).execute();

    AsyncResponse asyncResponse = mock(AsyncResponse.class);
    handler.addPendingRequest(createDto(5000L), asyncResponse, processEngine);

    addSecondsToClock(1);
    handler.acquire();

    // assume
    assertThat(handler.getPendingRequests().size(), is(1));
    verify(handler).suspend(4000L);

    addSecondsToClock(4);

    // when
    handler.acquire();

    // then
    verify(asyncResponse).resume(argThat(IsCollectionWithSize.hasSize(0)));
    assertThat(handler.getPendingRequests().size(), is(0));
    verify(handler).suspend(Long.MAX_VALUE - ClockUtil.getCurrentTime().getTime());
  }

  @Test
  public void shouldResumeAsyncResponseDueToTimeoutExpired_3() {
    // given
    doReturn(Collections.emptyList()).when(fetchTopicBuilder).execute();

    AsyncResponse asyncResponse = mock(AsyncResponse.class);
    handler.addPendingRequest(createDto(5000L), asyncResponse, processEngine);
    handler.addPendingRequest(createDto(4000L), asyncResponse, processEngine);

    addSecondsToClock(1);
    handler.acquire();

    // assume
    assertThat(handler.getPendingRequests().size(), is(2));
    verify(handler).suspend(3000L);

    addSecondsToClock(4);

    // when
    handler.acquire();

    // then
    verify(asyncResponse, times(2)).resume(Collections.emptyList());
    assertThat(handler.getPendingRequests().size(), is(0));
    verify(handler).suspend(Long.MAX_VALUE - ClockUtil.getCurrentTime().getTime());
  }

  @Test
  public void shouldResumeAsyncResponseImmediatelyDueToProcessEngineException() {
    // given
    doThrow(new ProcessEngineException()).when(fetchTopicBuilder).execute();

    // when
    AsyncResponse asyncResponse = mock(AsyncResponse.class);
    handler.addPendingRequest(createDto(5000L), asyncResponse, processEngine);

    // Then
    assertThat(handler.getPendingRequests().size(), is(0));
    verify(handler, never()).suspend(anyLong());
    verify(asyncResponse).resume(any(ProcessEngineException.class));
  }

  @Test
  public void shouldResumeAsyncResponseAfterBackoffDueToProcessEngineException() {
    // given
    doReturn(Collections.emptyList()).when(fetchTopicBuilder).execute();

    AsyncResponse asyncResponse = mock(AsyncResponse.class);
    handler.addPendingRequest(createDto(5000L), asyncResponse, processEngine);
    handler.acquire();

    // assume
    assertThat(handler.getPendingRequests().size(), is(1));
    verify(handler).suspend(5000L);

    // when
    doThrow(new ProcessEngineException()).when(fetchTopicBuilder).execute();
    handler.acquire();

    // then
    assertThat(handler.getPendingRequests().size(), is(0));
    verify(handler).suspend(Long.MAX_VALUE - ClockUtil.getCurrentTime().getTime());
    verify(asyncResponse).resume(any(ProcessEngineException.class));
  }

  @Test
  public void shouldResumeAsyncResponseDueToTimeoutExceeded() {
    // given - no pending requests

    // assume
    assertThat(handler.getPendingRequests().size(), is(0));

    // when
    AsyncResponse asyncResponse = mock(AsyncResponse.class);
    handler.addPendingRequest(createDto(FetchAndLockHandlerImpl.MAX_TIMEOUT + 1), asyncResponse, processEngine);

    // then
    verify(handler, never()).suspend(anyLong());
    assertThat(handler.getPendingRequests().size(), is(0));

    ArgumentCaptor<InvalidRequestException> argumentCaptor = ArgumentCaptor.forClass(InvalidRequestException.class);
    verify(asyncResponse).resume(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().getMessage(), is("The asynchronous response timeout cannot " +
      "be set to a value greater than " + FetchAndLockHandlerImpl.MAX_TIMEOUT +  " milliseconds"));
  }

  @Test
  public void shouldResumeAsyncResponseDueToTooManyRequests() {
    // given

    // when
    AsyncResponse asyncResponse = mock(AsyncResponse.class);
    handler.errorTooManyRequests(asyncResponse);

    // then
    ArgumentCaptor<InvalidRequestException> argumentCaptor = ArgumentCaptor.forClass(InvalidRequestException.class);
    verify(asyncResponse).resume(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().getMessage(), is("At the moment the server has to handle too " +
      "many requests at the same time. Please try again later."));
  }

  @Test
  public void shouldSuspendForeverDueToNoPendingRequests() {
    // given - no pending requests

    // assume
    assertThat(handler.getPendingRequests().size(), is(0));

    // when
    handler.acquire();

    // then
    assertThat(handler.getPendingRequests().size(), is(0));
    verify(handler).suspend(Long.MAX_VALUE - ClockUtil.getCurrentTime().getTime());
  }

  @Test
  public void shouldRejectRequestDueToShutdown() {
    // given
    AsyncResponse asyncResponse = mock(AsyncResponse.class);
    handler.addPendingRequest(createDto(5000L), asyncResponse, processEngine);
    handler.acquire();

    // assume
    assertThat(handler.getPendingRequests().size(), is(1));

    // when
    handler.rejectPendingRequests();

    // then
    ArgumentCaptor<InvalidRequestException> argumentCaptor = ArgumentCaptor.forClass(InvalidRequestException.class);
    verify(asyncResponse).resume(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().getMessage(), is("Request rejected due to shutdown of application server."));
  }

  protected FetchExternalTasksExtendedDto createDto(Long responseTimeout) {
    FetchExternalTasksExtendedDto externalTask = new FetchExternalTasksExtendedDto();

    FetchExternalTasksExtendedDto.FetchExternalTaskTopicDto topic = new FetchExternalTasksExtendedDto.FetchExternalTaskTopicDto();
    topic.setTopicName("aTopicName");
    topic.setLockDuration(12354L);

    externalTask.setMaxTasks(5);
    externalTask.setWorkerId("aWorkerId");
    externalTask.setTopics(Collections.singletonList(topic));

    if (responseTimeout != null) {
      externalTask.setAsyncResponseTimeout(responseTimeout);
    }

    return externalTask;
  }

  protected Date addSeconds(Date date, int seconds) {
    return new Date(date.getTime() + seconds * 1000);
  }

  protected void addSecondsToClock(int seconds) {
    Date newDate = addSeconds(ClockUtil.getCurrentTime(), seconds);
    ClockUtil.setCurrentTime(newDate);
  }

}
