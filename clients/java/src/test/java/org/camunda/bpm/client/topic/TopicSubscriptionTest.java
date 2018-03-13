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
package org.camunda.bpm.client.topic;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.helper.MockProvider;
import org.camunda.bpm.client.impl.ExternalTaskClientImpl;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.client.topic.impl.TopicSubscriptionImpl;
import org.camunda.bpm.client.topic.impl.TopicSubscriptionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
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
public class TopicSubscriptionTest {

  private CloseableHttpClient httpClient;
  private ExternalTaskClient client;

  @Before
  public void setUp() throws IOException {
    mockStatic(HttpClients.class);

    HttpClientBuilder httpClientBuilderMock = mock(HttpClientBuilder.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(HttpClients.custom())
      .thenReturn(httpClientBuilderMock);

    httpClient = mock(CloseableHttpClient.class);
    when(httpClientBuilderMock.build())
      .thenReturn(httpClient);

    when(httpClient.execute(any(HttpUriRequest.class), any(AbstractResponseHandler.class)))
      .thenReturn(new ExternalTask[]{MockProvider.createLockedTask()});

    client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();
  }

  @Test
  public void shouldSubscribeToTopicsWithLockDuration() throws IOException {
    // given
    ExternalTaskHandler lockedTaskHandlerMock = mock(ExternalTaskHandler.class);

    // when
    for (int i = 0; i < 10; i++) {
      TopicSubscription topicSubscription = client.subscribe(MockProvider.TOPIC_NAME+i)
        .lockDuration(5000+i)
        .handler(lockedTaskHandlerMock)
        .open();

      // then
      TopicSubscriptionImpl topicSubscriptionImpl = (TopicSubscriptionImpl) topicSubscription;
      assertThat(topicSubscriptionImpl.getLockDuration(), is(5000L+i));
      assertThat(topicSubscriptionImpl.getTopicName(), is(MockProvider.TOPIC_NAME+i));
    }

    // then
    ExternalTaskClientImpl clientImpl = (ExternalTaskClientImpl) client;
    TopicSubscriptionManager topicSubscriptionManager = clientImpl.getTopicSubscriptionManager();
    assertThat(topicSubscriptionManager.getSubscriptions().size(), is(10));

    client.stop();
  }

  @Test
  public void shouldThrowExceptionDueToTopicNameIsNull() {
    // given
    try {
      // when
      client.subscribe(null)
        .open();

      fail("No ExternalTaskClientException thrown!");
    } catch (ExternalTaskClientException e) {
      // then
      assertThat(e.getMessage(), containsString("Topic name cannot be null"));
    }

    client.stop();
  }

  @Test
  public void shouldThrowExceptionDueToLockDurationIsNotGreaterThanZero() {
    // given
    for (int i = -1; i < 2; i++) {
      try {
        TopicSubscriptionBuilder topicSubscriptionBuilder = client.subscribe(MockProvider.TOPIC_NAME);

        // when
        if (i <= 0) {
          topicSubscriptionBuilder.lockDuration(i);
        }

        topicSubscriptionBuilder.open();

        fail("No ExternalTaskClientException thrown!");
      } catch (ExternalTaskClientException e) {
        // then
        assertThat(e.getMessage(), containsString("Lock duration is not greater than 0"));
      }
    }

    client.stop();
  }

  @Test
  public void shouldThrowExceptionDueToNoLockedTaskHandlerDefined() {
    // given
    try {
      // when
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .open();

      fail("No ExternalTaskClientException thrown!");
    } catch (ExternalTaskClientException e) {
      // then
      assertThat(e.getMessage(), containsString("Locked task handler cannot be null"));
    }

    client.stop();
  }

  @Test
  public void shouldThrowExceptionDueToTopicNameAlreadySubscribed() {
    // given
    ExternalTaskHandler lockedTaskHandlerMock = mock(ExternalTaskHandler.class);
    client.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock)
      .open();

    try {
      // when
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler(lockedTaskHandlerMock)
        .open();

      fail("No ExternalTaskClientException thrown!");
    } catch (ExternalTaskClientException e) {
      // then
      assertThat(e.getMessage(), containsString("Topic name has already been subscribed"));
    }

    client.stop();
  }

  @Test
  public void shouldUnsubscribeFromTopic() {
    // given
    TopicSubscription topicSubscription = client.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(mock(ExternalTaskHandler.class))
      .open();

    // when
    topicSubscription.close();

    // then
    try {
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler(mock(ExternalTaskHandler.class))
        .open();
    } catch (ExternalTaskClientException e) {
      fail("ExternalTaskClientException thrown!");
    }
    
    client.stop();
  }

  @Test
  public void shouldExecuteHandler() throws IOException, InterruptedException {
    // given
    List<ExternalTask> lockedTasks = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      lockedTasks.add(MockProvider.createLockedTask());
    }

    when(httpClient.execute(any(HttpUriRequest.class), any(AbstractResponseHandler.class)))
      .thenReturn(lockedTasks.toArray(new ExternalTask[0]));

    ExternalTaskHandler lockedTaskHandlerMock = mock(ExternalTaskHandler.class);
    final AtomicBoolean invoked = new AtomicBoolean();
    doAnswer((Answer<Void>) invocationOnMock -> {
      invoked.set(true);
      return null;
    }).when(lockedTaskHandlerMock).execute(any(ExternalTask.class), any(ExternalTaskService.class));

    TopicSubscriptionBuilder topicSubscriptionBuilder = client.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock);

    // when
    topicSubscriptionBuilder.open();

    // then
    while (!invoked.get()) {
      // sync
    }
    client.stop();

    verify(lockedTaskHandlerMock, atLeast(5))
      .execute(any(ExternalTask.class), any(ExternalTaskService.class));
  }

}
