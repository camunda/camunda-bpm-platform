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
package org.camunda.bpm.client.impl;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.camunda.bpm.client.CamundaClient;
import org.camunda.bpm.client.CamundaClientException;
import org.camunda.bpm.client.LockedTask;
import org.camunda.bpm.client.LockedTaskHandler;
import org.camunda.bpm.client.WorkerSubscription;
import org.camunda.bpm.client.WorkerSubscriptionBuilder;
import org.camunda.bpm.client.helper.MockProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Tassilo Weidner
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpClients.class})
@PowerMockIgnore("javax.net.ssl.*")
public class SubscriptionTest {

  private HttpClient httpClient;
  private CamundaClient camundaClient;

  @Before
  public void setUp() throws IOException {
    mockStatic(HttpClients.class);
    httpClient = mock(CloseableHttpClient.class);
    when(HttpClients.createDefault())
      .thenReturn((CloseableHttpClient) httpClient);

    when(httpClient.execute(any(HttpUriRequest.class), any(AbstractResponseHandler.class)))
      .thenReturn(new LockedTask[]{MockProvider.createLockedTask()});

    camundaClient = CamundaClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();
  }

  @Test
  public void shouldSubscribeToTopicsWithLockDuration() throws IOException {
    // given
    LockedTaskHandler lockedTaskHandlerMock = mock(LockedTaskHandler.class);

    // when
    for (int i = 0; i < 10; i++) {
      WorkerSubscription workerSubscription = camundaClient.subscribe(MockProvider.TOPIC_NAME+i)
        .lockDuration(5000+i)
        .handler(lockedTaskHandlerMock)
        .open();

      // then
      WorkerSubscriptionImpl workerSubscriptionImpl = (WorkerSubscriptionImpl) workerSubscription;
      assertThat(workerSubscriptionImpl.getLockDuration(), is(5000L+i));
      assertThat(workerSubscriptionImpl.getTopicName(), is(MockProvider.TOPIC_NAME+i));
    }

    // then
    CamundaClientImpl camundaClientImpl = (CamundaClientImpl) camundaClient;
    WorkerManager workerManager = camundaClientImpl.getWorkerManager();
    assertThat(workerManager.getSubscriptions().size(), is(10));

    camundaClient.shutdown();
  }

  @Test
  public void shouldThrowExceptionDueToTopicNameIsNull() {
    // given
    try {
      // when
      camundaClient.subscribe(null)
        .open();

      fail("No CamundaClientException thrown!");
    } catch (CamundaClientException e) {
      // then
      assertThat(e.getMessage(), containsString("Topic name cannot be null"));
    }

    camundaClient.shutdown();
  }

  @Test
  public void shouldThrowExceptionDueToLockDurationIsNotGreaterThanZero() {
    // given
    for (int i = -1; i < 2; i++) {
      try {
        WorkerSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(MockProvider.TOPIC_NAME);

        // when
        if (i <= 0) {
          workerSubscriptionBuilder.lockDuration(i);
        }

        workerSubscriptionBuilder.open();

        fail("No CamundaClientException thrown!");
      } catch (CamundaClientException e) {
        // then
        assertThat(e.getMessage(), containsString("Lock duration is not greater than 0"));
      }
    }

    camundaClient.shutdown();
  }

  @Test
  public void shouldThrowExceptionDueToNoLockedTaskHandlerDefined() {
    // given
    try {
      // when
      camundaClient.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .open();

      fail("No CamundaClientException thrown!");
    } catch (CamundaClientException e) {
      // then
      assertThat(e.getMessage(), containsString("Locked task handler cannot be null"));
    }

    camundaClient.shutdown();
  }

  @Test
  public void shouldThrowExceptionDueToTopicNameAlreadySubscribed() {
    // given
    LockedTaskHandler lockedTaskHandlerMock = mock(LockedTaskHandler.class);
    camundaClient.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock)
      .open();

    try {
      // when
      camundaClient.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler(lockedTaskHandlerMock)
        .open();

      fail("No CamundaClientException thrown!");
    } catch (CamundaClientException e) {
      // then
      assertThat(e.getMessage(), containsString("Topic name has already been subscribed"));
    }

    camundaClient.shutdown();
  }

  @Test
  public void shouldExecuteHandler() throws IOException {
    // given
    List<LockedTask> lockedTasks = new ArrayList<LockedTask>();
    for (int i = 0; i < 5; i++) {
      lockedTasks.add(MockProvider.createLockedTask());
    }

    when(httpClient.execute(any(HttpUriRequest.class), any(AbstractResponseHandler.class)))
      .thenReturn(lockedTasks.toArray(new LockedTask[0]));

    LockedTaskHandler lockedTaskHandlerMock = mock(LockedTaskHandler.class);
    WorkerSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock);

    // when
    workerSubscriptionBuilder.open();
    camundaClient.shutdown();

    // then
    verify(lockedTaskHandlerMock, atLeast(5))
      .execute(any(LockedTask.class));
  }

}
