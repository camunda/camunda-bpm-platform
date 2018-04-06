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
package org.camunda.bpm.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.client.helper.MockProvider.BASE_URL;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.helper.ClosableHttpClientMock;
import org.camunda.bpm.client.helper.MockProvider;
import org.camunda.bpm.client.impl.EngineClient;
import org.camunda.bpm.client.impl.ExternalTaskClientBuilderImpl;
import org.camunda.bpm.client.impl.ExternalTaskClientImpl;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.interceptor.auth.BasicAuthProvider;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.camunda.bpm.client.topic.impl.dto.FetchAndLockRequestDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Tassilo Weidner
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpClients.class, ExternalTaskClientImpl.class})
public class ExternalTaskClientTest {

  protected ExternalTaskClient client;
  protected CloseableHttpResponse closeableHttpResponse;

  @Before
  public void setUp() {
    mockStatic(HttpClients.class);

    HttpClientBuilder httpClientBuilderMock = mock(HttpClientBuilder.class, RETURNS_DEEP_STUBS);
    when(HttpClients.custom())
      .thenReturn(httpClientBuilderMock);

    closeableHttpResponse = mock(CloseableHttpResponse.class);
    when(closeableHttpResponse.getStatusLine())
      .thenReturn(mock(StatusLine.class));

    CloseableHttpClient httpClient = spy(new ClosableHttpClientMock(closeableHttpResponse));
    when(httpClientBuilderMock.build())
      .thenReturn(httpClient);
  }

  @After
  public void tearDown() throws Exception {
    if (client != null) {
      client.stop();
    }
  }

  @Test
  public void shouldSucceedAfterSanitizingBaseUrl() {
    // given & when
    client = ExternalTaskClient.create()
      .baseUrl(BASE_URL + " / / / ")
      .build();

    ExternalTaskClientImpl clientImpl = ((ExternalTaskClientImpl) client);
    EngineClient engineClient = clientImpl.getTopicSubscriptionManager().getEngineClient();

    // then
    assertThat(engineClient.getBaseUrl()).isEqualTo(BASE_URL);
    assertThat(engineClient.getWorkerId().isEmpty()).isFalse();
  }

  @Test
  public void shouldThrowExceptionDueToBaseUrlIsEmpty() {
    // given & When
    try {
      client = ExternalTaskClient.create()
        .baseUrl("")
        .build();

      fail("No ExternalTaskClientException thrown!");
    } catch (ExternalTaskClientException e) {
      // then
      assertThat(e.getMessage()).contains("Base URL cannot be null or an empty string");
    }
  }

  @Test
  public void shouldThrowExceptionDueToBaseUrlIsNull() {
    // given & When
    try {
      client = ExternalTaskClient.create()
        .baseUrl(null)
        .build();

      fail("No ExternalTaskClientException thrown!");
    } catch (ExternalTaskClientException e) {
      // then
      assertThat(e.getMessage()).contains("Base URL cannot be null or an empty string");
    }
  }

  @Test
  public void shouldThrowExceptionDueToUnknownHostname() throws UnknownHostException {
    // given
    ExternalTaskClientBuilderImpl clientBuilder = spy(new ExternalTaskClientBuilderImpl());
    when(clientBuilder.getBaseUrl()).thenReturn(BASE_URL);
    when(clientBuilder.getHostname()).thenThrow(UnknownHostException.class);

    try {
      // when
      clientBuilder.checkHostname();

      fail("No ExternalTaskClientException thrown!");
    } catch (ExternalTaskClientException e) {
      // then
      assertThat(e.getMessage()).contains("Cannot get hostname");
    }
  }

  @Test
  public void shouldAddInterceptors() {
    // given
    ExternalTaskClientBuilder clientBuilder = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .addInterceptor(new BasicAuthProvider("demo", "demo"));

    // when
    client = clientBuilder.build();

    // then
    List<ClientRequestInterceptor> interceptors = ((ExternalTaskClientImpl)client)
      .getRequestInterceptorHandler()
      .getInterceptors();

    assertThat(interceptors.size()).isEqualTo(1);

    // when
    clientBuilder.addInterceptor(request -> {
      // another interceptor implementation
    });

    // then
    assertThat(interceptors.size()).isEqualTo(2);
  }

  @Test
  public void shouldUseDefaultAmountOfMaxTasks() throws Exception {
    // given
    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ObjectMapper objectMapper = spy(new ObjectMapper());
    whenNew(ObjectMapper.class).withNoArguments()
      .thenReturn(objectMapper);

    client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {
          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    assertMaxTasksAccordingToFetchAndLockPayload(objectMapper, 10);
  }

  @Test
  public void shouldSpecifyMaxTasks() throws Exception {
    // given
    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ObjectMapper objectMapper = spy(new ObjectMapper());
    whenNew(ObjectMapper.class).withNoArguments()
      .thenReturn(objectMapper);

    client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .maxTasks(5)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {
          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    assertMaxTasksAccordingToFetchAndLockPayload(objectMapper, 5);
  }

  @Test
  public void shouldThrowExceptionDueToMaxTasksNotGreaterThanZeroException() {
    // given
    ExternalTaskClientBuilder clientBuilder = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .maxTasks(-5);

    try {
      // when
      clientBuilder.build();

      fail("No ExternalTaskClientException thrown!");
    } catch (ExternalTaskClientException e) {
      // then
      assertThat(e.getMessage()).contains("Maximum amount of fetched tasks must be greater than zero");
    }
  }

  @Test
  public void shouldUseCustomWorkerId() throws Exception {
    // given
    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments()
      .thenReturn(objectMapper);

    client = ExternalTaskClient.create()
      .workerId("aWorkerId")
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> handlerInvoked.set(true));

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    assertWorkerIdAccordingToFetchAndLockPayload(objectMapper, "aWorkerId");
  }

  @Test
  public void shouldThrowExceptionDueToAsyncResponseTimeoutNotGreaterThanZero() throws Exception {
    // given
    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments()
      .thenReturn(objectMapper);

    ExternalTaskClientBuilder clientBuilder = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .asyncResponseTimeout(0);

    try {
      // when
      client = clientBuilder.build();

      fail("No ExternalTaskClientException thrown!");
    } catch (ExternalTaskClientException e) {
      assertThat(e.getMessage()).contains("Asynchronous response timeout must be greater than zero");
    }
  }

  @Test
  public void shouldUseAsyncResponseTimeout() throws Exception {
    // given
    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments()
      .thenReturn(objectMapper);

    client = ExternalTaskClient.create()
      .asyncResponseTimeout(5000)
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> handlerInvoked.set(true));

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }

    // then
    assertAsyncResponseTimeoutAccordingToFetchAndLockPayload(objectMapper, 5000L);
  }

  @Test
  public void shouldNotUseAsyncResponseTimeout() throws Exception {
    // given
    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments()
      .thenReturn(objectMapper);

    client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> handlerInvoked.set(true));

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }

    // then
    assertAsyncResponseTimeoutAccordingToFetchAndLockPayload(objectMapper, null);
  }

  @Test
  public void shouldUseDefaultLockDuration() throws Exception {
    // given
    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments()
      .thenReturn(objectMapper);

    client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .handler((externalTask, externalTaskService) -> handlerInvoked.set(true));

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }

    // then
    assertLockDurationAccordingToFetchAndLockPayload(objectMapper, 20000L);
  }

  @Test
  public void shouldUseClientLockDuration() throws Exception {
    // given
    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments()
      .thenReturn(objectMapper);

    client = ExternalTaskClient.create()
      .lockDuration(4711)
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .handler((externalTask, externalTaskService) -> handlerInvoked.set(true));

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }

    // then
    assertLockDurationAccordingToFetchAndLockPayload(objectMapper, 4711L);
  }

  @Test
  public void shouldUseSubscriptionLockDuration() throws Exception {
    // given
    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments()
      .thenReturn(objectMapper);

    client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(4711)
        .handler((externalTask, externalTaskService) -> handlerInvoked.set(true));

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }

    // then
    assertLockDurationAccordingToFetchAndLockPayload(objectMapper, 4711L);
  }

  @Test
  public void shouldOverrideClientBySubscriptionLockDuration() throws Exception {
    // given
    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments()
      .thenReturn(objectMapper);

    client = ExternalTaskClient.create()
      .lockDuration(31415)
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(4711)
        .handler((externalTask, externalTaskService) -> handlerInvoked.set(true));

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }

    // then
    assertLockDurationAccordingToFetchAndLockPayload(objectMapper, 4711L);
  }

  @Test
  public void shouldThrowExceptionDueToClientLockDurationNotGreaterThanZero() throws Exception {
    // given
    client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    TopicSubscriptionBuilder topicSubscriptionBuilder = client.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(0)
      .handler(mock(ExternalTaskHandler.class));

    try {
      // when
      topicSubscriptionBuilder.open();

      fail("No ExternalTaskClientException thrown!");
    } catch (ExternalTaskClientException e) {
      // then
      assertThat(e.getMessage()).contains("Lock duration is not greater than 0");
    }
  }

  // helper /////////////////////////////////////////

  protected void mockFetchAndLockResponse(List<ExternalTask> externalTasks) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    byte[] externalTasksAsBytes = objectMapper.writeValueAsBytes(externalTasks);
    HttpEntity entity = new ByteArrayEntity(externalTasksAsBytes);
    doReturn(entity)
      .when(closeableHttpResponse).getEntity();
  }

  protected void assertMaxTasksAccordingToFetchAndLockPayload(ObjectMapper objectMapper, int expectedMaxTasks) throws JsonProcessingException {
    assertThat(assertAccordingToFetchAndLockPayload(objectMapper).getMaxTasks()).isEqualTo(expectedMaxTasks);
  }

  protected void assertWorkerIdAccordingToFetchAndLockPayload(ObjectMapper objectMapper, String workerId) throws JsonProcessingException {
    assertThat(assertAccordingToFetchAndLockPayload(objectMapper).getWorkerId()).isEqualTo(workerId);
  }

  protected void assertAsyncResponseTimeoutAccordingToFetchAndLockPayload(ObjectMapper objectMapper, Long asyncResponseTimeout) throws JsonProcessingException {
    if (asyncResponseTimeout == null) {
      assertThat(assertAccordingToFetchAndLockPayload(objectMapper).getAsyncResponseTimeout()).isNull();
    } else {
      assertThat(assertAccordingToFetchAndLockPayload(objectMapper).getAsyncResponseTimeout()).isEqualTo(asyncResponseTimeout);
    }
  }

  protected void assertLockDurationAccordingToFetchAndLockPayload(ObjectMapper objectMapper, Long lockDuration) throws JsonProcessingException {
      assertThat(assertAccordingToFetchAndLockPayload(objectMapper).getTopics().get(0).getLockDuration()).isEqualTo(lockDuration);
  }

  protected FetchAndLockRequestDto assertAccordingToFetchAndLockPayload(ObjectMapper objectMapper) throws JsonProcessingException {
    ArgumentCaptor<Object> payloads = ArgumentCaptor.forClass(Object.class);
    verify(objectMapper, atLeastOnce()).writeValueAsBytes(payloads.capture());

    return (FetchAndLockRequestDto) payloads.getAllValues().stream()
      .filter(payload -> payload instanceof FetchAndLockRequestDto)
      .findFirst()
      .orElse(null);
  }

}
