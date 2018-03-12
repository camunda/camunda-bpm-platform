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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
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
import org.camunda.bpm.client.helper.MockProvider;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.camunda.bpm.client.topic.impl.dto.FetchAndLockRequestDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

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
    
    CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
    when(closeableHttpResponse.getStatusLine())
      .thenReturn(mock(StatusLine.class));

    HttpClientBuilder httpClientBuilderMock = mock(HttpClientBuilder.class, Mockito.RETURNS_DEEP_STUBS);
    when(HttpClients.custom())
      .thenReturn(httpClientBuilderMock);

    CloseableHttpClient httpClient = spy(new ClosableHttpClientMock(closeableHttpResponse));
    when(httpClientBuilderMock.build())
      .thenReturn(httpClient);

    List<ExternalTask> lockedTasks = Collections.singletonList(MockProvider.createLockedTask());
    ObjectMapper objectMapper = new ObjectMapper();
    byte[] lockedTasksAsBytes = objectMapper.writeValueAsBytes(lockedTasks);
    HttpEntity entity = new ByteArrayEntity(lockedTasksAsBytes);
    doReturn(entity)
      .when(closeableHttpResponse).getEntity();
  }

  @Test
  public void shouldDeserializeFetchAndLockResponse() throws IOException {
    // given
    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    final List<ExternalTask> lockedTaskReference = new ArrayList<ExternalTask>(); // list, as container must be final and changeable
    ExternalTaskHandler lockedTaskHandler = new ExternalTaskHandler() {
      @Override
      public void execute(ExternalTask lockedTask, ExternalTaskService lockedTaskService) {
        lockedTaskReference.add(lockedTask);
        handlerInvoked.set(true);
      }
    };

    TopicSubscriptionBuilder workerSubscriptionBuilder =
      camundaClient.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler(lockedTaskHandler);

    // when
    workerSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    camundaClient.shutdown();

    // then
    assertThat(lockedTaskReference.get(0).getActivityId(), is(MockProvider.ACTIVITY_ID));
    assertThat(lockedTaskReference.get(0).getActivityInstanceId(), is(MockProvider.ACTIVITY_INSTANCE_ID));
    assertThat(lockedTaskReference.get(0).getExecutionId(), is(MockProvider.EXECUTION_ID));
    assertThat(lockedTaskReference.get(0).getLockExpirationTime(), is(MockProvider.LOCK_EXPIRATION_TIME));
    assertThat(lockedTaskReference.get(0).getProcessDefinitionId(), is(MockProvider.PROCESS_DEFINITION_ID));
    assertThat(lockedTaskReference.get(0).getProcessDefinitionKey(), is(MockProvider.PROCESS_DEFINITION_KEY));
    assertThat(lockedTaskReference.get(0).getProcessInstanceId(), is(MockProvider.PROCESS_INSTANCE_ID));
    assertThat(lockedTaskReference.get(0).getId(), is(MockProvider.ID));
    assertThat(lockedTaskReference.get(0).getWorkerId(), is(MockProvider.WORKER_ID));
    assertThat(lockedTaskReference.get(0).getTopicName(), is(MockProvider.TOPIC_NAME));
    assertThat(lockedTaskReference.get(0).getVariables(), is(MockProvider.VARIABLES));
    assertThat(lockedTaskReference.get(0).getErrorMessage(), is(MockProvider.ERROR_MESSAGE));
    assertThat(lockedTaskReference.get(0).getErrorDetails(), is(MockProvider.ERROR_DETAILS));
    assertThat(lockedTaskReference.get(0).isSuspended(), is(MockProvider.SUSPENSION_STATE));
    assertThat(lockedTaskReference.get(0).getTenantId(), is(MockProvider.TENANT_ID));
    assertThat(lockedTaskReference.get(0).getRetries(), is(MockProvider.RETRIES));
    assertThat(lockedTaskReference.get(0).getPriority(), is(MockProvider.PRIORITY));
  }

  @Test
  public void shouldThrowExceptionWhileParsingResponse() throws Exception {
    // given
    mockDeserializationException(JsonParseException.class);

    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    ExternalTaskHandler lockedTaskHandlerMock = mock(ExternalTaskHandler.class);
    TopicSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock);

    // when
    workerSubscriptionBuilder.open();
    Thread.sleep(1000);
    camundaClient.shutdown();

    // then
    verifyZeroInteractions(lockedTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionWhileMappingResponse() throws Exception {
    // given
    mockDeserializationException(JsonMappingException.class);

    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    ExternalTaskHandler lockedTaskHandlerMock = mock(ExternalTaskHandler.class);
    TopicSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock);

    // when
    workerSubscriptionBuilder.open();
    Thread.sleep(1000);
    camundaClient.shutdown();

    // then
    verifyZeroInteractions(lockedTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionWhileDeserializingResponse() throws Exception {
    // given
    mockDeserializationException(IOException.class);

    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    ExternalTaskHandler lockedTaskHandlerMock = mock(ExternalTaskHandler.class);
    TopicSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock);

    // when
    workerSubscriptionBuilder.open();
    Thread.sleep(1000);
    camundaClient.shutdown();

    // then
    verifyZeroInteractions(lockedTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionWhileSerializingRequest() throws Exception {
    // given
    mockSerializationException(JsonProcessingException.class);

    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    ExternalTaskHandler lockedTaskHandlerMock = mock(ExternalTaskHandler.class);
    TopicSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock);

    // when
    workerSubscriptionBuilder.open();
    Thread.sleep(1000);
    camundaClient.shutdown();

    // then
    verifyZeroInteractions(lockedTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionDueToHttpRequestWrongHttpStatusCode() throws IOException, InterruptedException {
    // given
    mockHttpRequestException(HttpResponseException.class);

    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    ExternalTaskHandler lockedTaskHandlerMock = mock(ExternalTaskHandler.class);

    TopicSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock);

    // when
    workerSubscriptionBuilder.open();
    Thread.sleep(1000);
    camundaClient.shutdown();

    // then
    verifyZeroInteractions(lockedTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionDueToHttpRequestClientProtocolProblem() throws IOException, InterruptedException {
    // given
    mockHttpRequestException(ClientProtocolException.class);

    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    ExternalTaskHandler lockedTaskHandlerMock = mock(ExternalTaskHandler.class);

    TopicSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock);

    // when
    workerSubscriptionBuilder.open();
    Thread.sleep(1000);
    camundaClient.shutdown();

    // then
    verifyZeroInteractions(lockedTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionDueToHttpRequestIoProblem() throws IOException, InterruptedException {
    // given
    mockHttpRequestException(IOException.class);

    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    ExternalTaskHandler lockedTaskHandlerMock = mock(ExternalTaskHandler.class);

    TopicSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock);

    // when
    workerSubscriptionBuilder.open();
    Thread.sleep(1000);
    camundaClient.shutdown();

    // then
    verifyZeroInteractions(lockedTaskHandlerMock);
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

    HttpClientBuilder httpClientBuilderMock = mock(HttpClientBuilder.class, Mockito.RETURNS_DEEP_STUBS);
    when(HttpClients.custom())
      .thenReturn(httpClientBuilderMock);

    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    when(httpClientBuilderMock.build())
      .thenReturn(httpClient);

    when(httpClient.execute(any(HttpUriRequest.class), any(AbstractResponseHandler.class)))
      .thenThrow(mock(exception));
  }

}
