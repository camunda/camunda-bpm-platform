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
import org.camunda.bpm.engine.rest.dto.externaltask.LockedExternalTaskDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.container.AsyncResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Tassilo Weidner
 */
public class FetchAndLockHandlerTest {

  private ProcessEngine processEngine;

  private ExternalTaskQueryTopicBuilder fetchTopicBuilder;
  private LockedExternalTask lockedExternalTaskMock;

  private FetchAndLockHandlerImpl fetchAndLockHandler;

  @Before
  public void setUpRuntimeData() {
    processEngine = mock(ProcessEngine.class);

    IdentityService identityServiceMock = mock(IdentityService.class);
    when(processEngine.getIdentityService()).thenReturn(identityServiceMock);

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

    fetchAndLockHandler = new FetchAndLockHandlerImpl();
  }

  @After
  public void tearDown() {
    fetchAndLockHandler.getHandlerThread().stop(); // provoke thread death
    ClockUtil.reset();
  }

  @Test(timeout = 30000)
  public void shouldResumeMultipleConcurrentRequestsDueToTasksAvailable() throws InterruptedException {
    when(fetchTopicBuilder.execute())
      .thenReturn(Collections.<LockedExternalTask>emptyList());
    fetchAndLockHandler.start();

    final List<Runnable> runnables = new ArrayList<Runnable>();
    final AsyncResponse asyncResponse = mock(AsyncResponse.class);
    for (int i = 0; i < 100; i++) {
      runnables.add(new Runnable() {
        @Override
        public void run() {
          fetchAndLockHandler.addPendingRequest(createDto(5000L), asyncResponse, processEngine);
        }
      });
    }

    assertConcurrent(runnables);

    assertThat(fetchAndLockHandler.getPendingRequests().size(), is(100));

    when(fetchTopicBuilder.execute())
      .thenReturn(Collections.singletonList(lockedExternalTaskMock));

    while (!fetchAndLockHandler.getPendingRequests().isEmpty()) {
      // busy waiting
    }

    assertThat(fetchAndLockHandler.getPendingRequests().size(), is(0));
    ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);
    verify(asyncResponse, times(100)).resume(argumentCaptor.capture());

    for (List lockedExternalTasks : argumentCaptor.getAllValues()) {
      assertThat(((LockedExternalTaskDto)lockedExternalTasks.get(0)).getActivityId(),
        is(lockedExternalTaskMock.getActivityId()));
    }
  }

  @Test(timeout = 30000)
  public void shouldResumeMultipleConcurrentRequestsDueToTimeout() throws InterruptedException {
    when(fetchTopicBuilder.execute())
      .thenReturn(Collections.<LockedExternalTask>emptyList());

    fetchAndLockHandler.start();

    final List<Runnable> runners = new ArrayList<Runnable>();

    final AsyncResponse asyncResponse = mock(AsyncResponse.class);
    for (int i = 0; i < 100; i++) {
      runners.add(new Runnable() {
        @Override
        public void run() {
          fetchAndLockHandler.addPendingRequest(createDto(3000L), asyncResponse, processEngine);
        }
      });
    }

    assertConcurrent(runners);

    while (!fetchAndLockHandler.getPendingRequests().isEmpty()) {
      // busy waiting
    }

    assertThat(fetchAndLockHandler.getPendingRequests().size(), is(0));

    ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);
    verify(asyncResponse, times(100)).resume(argumentCaptor.capture());

    for (List lockedExternalTasks : argumentCaptor.getAllValues()) {
      assertThat(lockedExternalTasks.isEmpty(), is(true));
    }
  }

  @Test(timeout = 30000)
  public void shouldResumeAllRequestsDueToTimeout() {
    // given
    when(fetchTopicBuilder.execute())
      .thenReturn(Collections.<LockedExternalTask>emptyList());

    AsyncResponse asyncResponse = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(100L), asyncResponse, processEngine);
    fetchAndLockHandler.addPendingRequest(createDto(100L), asyncResponse, processEngine);
    fetchAndLockHandler.addPendingRequest(createDto(100L), asyncResponse, processEngine);
    fetchAndLockHandler.addPendingRequest(createDto(100L), asyncResponse, processEngine);

    // when
    fetchAndLockHandler.start();

    while (!fetchAndLockHandler.getPendingRequests().isEmpty()) {
      // busy waiting
    }

    // then
    verify(asyncResponse, times(4)).resume(anyList());
    assertThat(fetchAndLockHandler.getPendingRequests().size(), is(0));
  }

  @Test
  public void shouldSetCorrectSlacktime() {
    // given
    when(fetchTopicBuilder.execute())
      .thenReturn(Collections.<LockedExternalTask>emptyList());

    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime()));

    AsyncResponse asyncResponse = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(5000L), asyncResponse, processEngine);
    fetchAndLockHandler.addPendingRequest(createDto(3000L), asyncResponse, processEngine);
    fetchAndLockHandler.addPendingRequest(createDto(7000L), asyncResponse, processEngine);
    fetchAndLockHandler.addPendingRequest(createDto(6000L), asyncResponse, processEngine);

    // when
    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + 1000L));
    long backOffTime = fetchAndLockHandler.checkPendingRequests();

    // then
    verifyNoMoreInteractions(asyncResponse);
    assertThat(fetchAndLockHandler.getPendingRequests().size(), is(4));
    assertThat(backOffTime, is(2000L));
  }

  @Test
  public void shouldResumeRequestsDueToAvailableTasks() {
    // given
    when(fetchTopicBuilder.execute())
      .thenReturn(Collections.<LockedExternalTask>emptyList())
      .thenReturn(Collections.<LockedExternalTask>emptyList())
      .thenReturn(Collections.<LockedExternalTask>emptyList())
      .thenReturn(Collections.<LockedExternalTask>emptyList())
      .thenReturn(new ArrayList<LockedExternalTask>(Collections.singleton(lockedExternalTaskMock)));

    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime()));

    AsyncResponse asyncResponse1 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(5000L), asyncResponse1, processEngine);

    AsyncResponse asyncResponse2 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(3000L), asyncResponse2, processEngine);

    AsyncResponse asyncResponse3 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(7000L), asyncResponse3, processEngine);

    AsyncResponse asyncResponse4 = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(6000L), asyncResponse4, processEngine);

    // when
    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + 1000L));
    long backOffTime = fetchAndLockHandler.checkPendingRequests();

    // then
    verify(asyncResponse1).resume(anyList());
    verify(asyncResponse2).resume(anyList());
    verify(asyncResponse3).resume(anyList());
    verify(asyncResponse4).resume(anyList());

    assertThat(fetchAndLockHandler.getPendingRequests().size(), is(0));
    assertThat(backOffTime, is(Long.MAX_VALUE));
  }

  @Test
  public void shouldResumeRequestDueToProcessEngineException() {
    // given
    when(fetchTopicBuilder.execute())
      .thenReturn(Collections.<LockedExternalTask>emptyList())
      .thenThrow(new ProcessEngineException("anExceptionMessage"));

    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime()));

    AsyncResponse asyncResponse = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(5000L), asyncResponse, processEngine);

    // when
    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + 1000L));
    long backOffTime = fetchAndLockHandler.checkPendingRequests();

    // then
    ArgumentCaptor<ProcessEngineException> argumentCaptor = ArgumentCaptor.forClass(ProcessEngineException.class);
    verify(asyncResponse, times(1)).resume(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().getMessage(), is("anExceptionMessage"));
    assertThat(fetchAndLockHandler.getPendingRequests().size(), is(0));
    assertThat(backOffTime, is(Long.MAX_VALUE));
  }

  @Test
  public void shouldResumeRequestImmediatelyDueToNegativeTimeout() {
    // given
    when(fetchTopicBuilder.execute())
      .thenReturn(Collections.<LockedExternalTask>emptyList());

    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime()));

    AsyncResponse asyncResponse = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(-5000L), asyncResponse, processEngine);

    // when
    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + 1000L));
    long backOffTime = fetchAndLockHandler.checkPendingRequests();

    // then
    verify(asyncResponse, times(1)).resume(Collections.emptyList());
    assertThat(fetchAndLockHandler.getPendingRequests().size(), is(0));
    assertThat(backOffTime, is(Long.MAX_VALUE));
  }

  @Test
  public void shouldShutdownThreadGracefully() {
    // given
    AsyncResponse asyncResponse = mock(AsyncResponse.class);
    fetchAndLockHandler.addPendingRequest(createDto(FetchAndLockHandlerImpl.MAX_TIMEOUT), asyncResponse, processEngine);
    fetchAndLockHandler.run();

    // when
    fetchAndLockHandler.shutdown();
    fetchAndLockHandler.getHandlerThread().interrupt();

    // then
    ArgumentCaptor<InvalidRequestException> argumentCaptor = ArgumentCaptor.forClass(InvalidRequestException.class);
    verify(asyncResponse, times(1)).resume(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().getMessage(), is("Request rejected due to shutdown of application server."));
  }

  // helper /////////////////////////
  private void assertConcurrent(List<? extends Runnable> runnables) throws InterruptedException {
    final int numThreads = runnables.size();
    final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());
    final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
    try {
        final CountDownLatch allExecutorThreadsReady = new CountDownLatch(numThreads);
        final CountDownLatch afterInitBlocker = new CountDownLatch(1);
        final CountDownLatch allDone = new CountDownLatch(numThreads);
        for (final Runnable submittedTestRunnable : runnables) {
            threadPool.submit(new Runnable() {
                public void run() {
                    allExecutorThreadsReady.countDown();
                    try {
                        afterInitBlocker.await();
                        submittedTestRunnable.run();
                    } catch (final Throwable e) {
                        exceptions.add(e);
                    } finally {
                        allDone.countDown();
                    }
                }
            });
        }
        // wait until all threads are ready
        assertTrue("Timeout initializing threads! Perform long lasting initializations before passing runnables to assertConcurrent",
          allExecutorThreadsReady.await(runnables.size() * 10, TimeUnit.MILLISECONDS));

        // start all test runners
        afterInitBlocker.countDown();
        final int maxTimeoutSeconds = 3;
        assertTrue("Timeout! More than " + maxTimeoutSeconds + " seconds to start runners", allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS));
    } finally {
        threadPool.shutdownNow();
    }
    assertTrue("Failed with exception(s) " + exceptions, exceptions.isEmpty());
  }

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

}
