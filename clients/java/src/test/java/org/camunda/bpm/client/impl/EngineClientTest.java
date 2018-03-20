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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.helper.ClosableHttpClientMock;
import org.camunda.bpm.client.helper.MockProvider;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.camunda.bpm.client.topic.impl.dto.FetchAndLockRequestDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tassilo Weidner
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpClients.class, RequestExecutor.class})
@PowerMockIgnore("javax.net.ssl.*")
public class EngineClientTest {

  @Before
  public void setUp() throws JsonProcessingException {
    mockStatic(HttpClients.class);
    
    CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
    when(httpResponse.getStatusLine())
      .thenReturn(mock(StatusLine.class));

    HttpClientBuilder httpClientBuilderMock = mock(HttpClientBuilder.class, RETURNS_DEEP_STUBS);
    when(HttpClients.custom())
      .thenReturn(httpClientBuilderMock);

    CloseableHttpClient httpClientSpy = spy(new ClosableHttpClientMock(httpResponse));
    when(httpClientBuilderMock.build())
      .thenReturn(httpClientSpy);

    List<ExternalTask> externalTasks = Collections.singletonList(MockProvider.createExternalTask());
    ObjectMapper objectMapper = new ObjectMapper();
    byte[] externalTasksAsBytes = objectMapper.writeValueAsBytes(externalTasks);
    HttpEntity entity = new ByteArrayEntity(externalTasksAsBytes);
    doReturn(entity)
      .when(httpResponse).getEntity();
  }

  @Test
  public void shouldDeserializeFetchAndLockResponse() throws IOException {
    // given
    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    final List<ExternalTask> externalTaskReference = new ArrayList<>(); // list, as container must be final and changeable

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {
          externalTaskReference.add(externalTask);
          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    ExternalTask externalTask = externalTaskReference.get(0);
    assertThat(externalTask.getActivityId()).isEqualTo(MockProvider.ACTIVITY_ID);
    assertThat(externalTask.getActivityInstanceId()).isEqualTo(MockProvider.ACTIVITY_INSTANCE_ID);
    assertThat(externalTask.getExecutionId()).isEqualTo(MockProvider.EXECUTION_ID);
    assertThat(externalTask.getLockExpirationTime()).isEqualTo(MockProvider.LOCK_EXPIRATION_TIME);
    assertThat(externalTask.getProcessDefinitionId()).isEqualTo(MockProvider.PROCESS_DEFINITION_ID);
    assertThat(externalTask.getProcessDefinitionKey()).isEqualTo(MockProvider.PROCESS_DEFINITION_KEY);
    assertThat(externalTask.getProcessInstanceId()).isEqualTo(MockProvider.PROCESS_INSTANCE_ID);
    assertThat(externalTask.getId()).isEqualTo(MockProvider.ID);
    assertThat(externalTask.getWorkerId()).isEqualTo(MockProvider.WORKER_ID);
    assertThat(externalTask.getTopicName()).isEqualTo(MockProvider.TOPIC_NAME);
    assertThat(externalTask.getErrorMessage()).isEqualTo(MockProvider.ERROR_MESSAGE);
    assertThat(externalTask.getErrorDetails()).isEqualTo(MockProvider.ERROR_DETAILS);
    assertThat(externalTask.isSuspended()).isEqualTo(MockProvider.SUSPENSION_STATE);
    assertThat(externalTask.getTenantId()).isEqualTo(MockProvider.TENANT_ID);
    assertThat(externalTask.getRetries()).isEqualTo(MockProvider.RETRIES);
    assertThat(externalTask.getPriority()).isEqualTo(MockProvider.PRIORITY);
  }

  @Test
  public void shouldThrowExceptionWhileParsingResponse() throws Exception {
    // given
    mockDeserializationException(JsonParseException.class);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    ExternalTaskHandler externalTaskHandlerMock = mock(ExternalTaskHandler.class);
    TopicSubscriptionBuilder topicSubscriptionBuilder = client.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(externalTaskHandlerMock);

    // when
    topicSubscriptionBuilder.open();
    Thread.sleep(1000);
    client.stop();

    // then
    verifyZeroInteractions(externalTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionWhileMappingResponse() throws Exception {
    // given
    mockDeserializationException(JsonMappingException.class);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    ExternalTaskHandler externalTaskHandlerMock = mock(ExternalTaskHandler.class);
    TopicSubscriptionBuilder topicSubscriptionBuilder = client.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(externalTaskHandlerMock);

    // when
    topicSubscriptionBuilder.open();
    Thread.sleep(1000);
    client.stop();

    // then
    verifyZeroInteractions(externalTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionWhileDeserializingResponse() throws Exception {
    // given
    mockDeserializationException(IOException.class);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    ExternalTaskHandler externalTaskHandlerMock = mock(ExternalTaskHandler.class);
    TopicSubscriptionBuilder topicSubscriptionBuilder = client.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(externalTaskHandlerMock);

    // when
    topicSubscriptionBuilder.open();
    Thread.sleep(1000);
    client.stop();

    // then
    verifyZeroInteractions(externalTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionWhileSerializingRequest() throws Exception {
    // given
    mockSerializationException(JsonProcessingException.class);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    ExternalTaskHandler externalTaskHandlerMock = mock(ExternalTaskHandler.class);
    TopicSubscriptionBuilder topicSubscriptionBuilder = client.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(externalTaskHandlerMock);

    // when
    topicSubscriptionBuilder.open();
    Thread.sleep(1000);
    client.stop();

    // then
    verifyZeroInteractions(externalTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionDueToHttpRequestWrongHttpStatusCode() throws IOException, InterruptedException {
    // given
    mockHttpRequestException(HttpResponseException.class);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    ExternalTaskHandler externalTaskHandlerMock = mock(ExternalTaskHandler.class);

    TopicSubscriptionBuilder topicSubscriptionBuilder = client.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(externalTaskHandlerMock);

    // when
    topicSubscriptionBuilder.open();
    Thread.sleep(1000);
    client.stop();

    // then
    verifyZeroInteractions(externalTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionDueToHttpRequestClientProtocolProblem() throws IOException, InterruptedException {
    // given
    mockHttpRequestException(ClientProtocolException.class);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    ExternalTaskHandler externalTaskHandlerMock = mock(ExternalTaskHandler.class);

    TopicSubscriptionBuilder topicSubscriptionBuilder = client.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(externalTaskHandlerMock);

    // when
    topicSubscriptionBuilder.open();
    Thread.sleep(1000);
    client.stop();

    // then
    verifyZeroInteractions(externalTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionDueToHttpRequestIoProblem() throws IOException, InterruptedException {
    // given
    mockHttpRequestException(IOException.class);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    ExternalTaskHandler externalTaskHandlerMock = mock(ExternalTaskHandler.class);

    TopicSubscriptionBuilder topicSubscriptionBuilder = client.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(externalTaskHandlerMock);

    // when
    topicSubscriptionBuilder.open();
    Thread.sleep(1000);
    client.stop();

    // then
    verifyZeroInteractions(externalTaskHandlerMock);
  }

  // helper //////////////////////////////
  private ObjectMapper mockObjectMapper() throws Exception {
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments()
      .thenReturn(objectMapper);
    return objectMapper;
  }

  private void mockDeserializationException(Class<? extends Throwable> exception) throws Exception {
    ObjectMapper objectMapper = mockObjectMapper();
    when(objectMapper.readValue(any(InputStream.class), (Class<?>) any(Class.class)))
      .thenThrow(mock(exception));
  }

  private void mockSerializationException(Class<? extends Throwable> exception) throws Exception {
    ObjectMapper objectMapper = mockObjectMapper();
    when(objectMapper.writeValueAsBytes(any(FetchAndLockRequestDto.class)))
      .thenThrow(mock(exception));
  }

  private void mockHttpRequestException(Class<? extends Throwable> exception) throws IOException {
    mockStatic(HttpClients.class);

    HttpClientBuilder httpClientBuilderMock = mock(HttpClientBuilder.class, RETURNS_DEEP_STUBS);
    when(HttpClients.custom())
      .thenReturn(httpClientBuilderMock);

    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    when(httpClientBuilderMock.build())
      .thenReturn(httpClient);

    when(httpClient.execute(any(HttpUriRequest.class), any(AbstractResponseHandler.class)))
      .thenThrow(mock(exception));
  }

}
