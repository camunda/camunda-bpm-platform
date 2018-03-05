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
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.camunda.bpm.client.CamundaClient;
import org.camunda.bpm.client.LockedTask;
import org.camunda.bpm.client.LockedTaskHandler;
import org.camunda.bpm.client.WorkerSubscriptionBuilder;
import org.camunda.bpm.client.helper.MockProvider;
import org.camunda.bpm.client.impl.dto.AbstractDto;
import org.camunda.bpm.client.impl.dto.FetchAndLockRequestDto;
import org.camunda.bpm.client.impl.dto.TaskTopicRequestDto;
import org.camunda.bpm.client.impl.engineclient.EngineInteractionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
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
@PrepareForTest({HttpClients.class, EngineInteractionManager.class})
@PowerMockIgnore("javax.net.ssl.*")
public class EngineInteractionManagerTest {

  private ObjectMapper objectMapper;

  @Before
  public void setUp() throws JsonProcessingException {
    objectMapper = new ObjectMapper();

    mockStatic(HttpClients.class);
    final CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
    when(closeableHttpResponse.getStatusLine()).thenReturn(mock(StatusLine.class));
    CloseableHttpClient httpClient = spy(new ClosableHttpClientMock(closeableHttpResponse));

    when(HttpClients.createDefault())
      .thenReturn(httpClient);

    HttpEntity entity = new ByteArrayEntity(objectMapper.writeValueAsBytes(Collections.singletonList(MockProvider.createLockedTask())));
    doReturn(entity)
      .when(closeableHttpResponse).getEntity();
  }

  @Test
  public void shouldDeserializeResponse() throws IOException {
    // given
    CamundaClient camundaClient = CamundaClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    LockedTaskHandler lockedTaskHandlerMock = mock(LockedTaskHandler.class);
    WorkerSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock);

    // when
    workerSubscriptionBuilder.open();
    camundaClient.shutdown();

    // then
    ArgumentCaptor<LockedTask> argumentCaptor = ArgumentCaptor.forClass(LockedTask.class);
    verify(lockedTaskHandlerMock, atLeastOnce()).execute(argumentCaptor.capture());

    assertThat(argumentCaptor.getValue().getActivityId(), is(MockProvider.ACTIVITY_ID));
    assertThat(argumentCaptor.getValue().getActivityInstanceId(), is(MockProvider.ACTIVITY_INSTANCE_ID));
    assertThat(argumentCaptor.getValue().getExecutionId(), is(MockProvider.EXECUTION_ID));
    assertThat(argumentCaptor.getValue().getLockExpirationTime(), is(MockProvider.LOCK_EXPIRATION_TIME));
    assertThat(argumentCaptor.getValue().getProcessDefinitionId(), is(MockProvider.PROCESS_DEFINITION_ID));
    assertThat(argumentCaptor.getValue().getProcessDefinitionKey(), is(MockProvider.PROCESS_DEFINITION_KEY));
    assertThat(argumentCaptor.getValue().getProcessInstanceId(), is(MockProvider.PROCESS_INSTANCE_ID));
    assertThat(argumentCaptor.getValue().getId(), is(MockProvider.ID));
    assertThat(argumentCaptor.getValue().getWorkerId(), is(MockProvider.WORKER_ID));
    assertThat(argumentCaptor.getValue().getTopicName(), is(MockProvider.TOPIC_NAME));
    assertThat(argumentCaptor.getValue().getVariables(), is(MockProvider.VARIABLES));
    assertThat(argumentCaptor.getValue().getErrorMessage(), is(MockProvider.ERROR_MESSAGE));
    assertThat(argumentCaptor.getValue().getErrorDetails(), is(MockProvider.ERROR_DETAILS));
    assertThat(argumentCaptor.getValue().isSuspended(), is(MockProvider.SUSPENSION_STATE));
    assertThat(argumentCaptor.getValue().getTenantId(), is(MockProvider.TENANT_ID));
    assertThat(argumentCaptor.getValue().getRetries(), is(MockProvider.RETRIES));
    assertThat(argumentCaptor.getValue().getPriority(), is(MockProvider.PRIORITY));
  }

  @Test
  public void shouldThrowExceptionWhileParsingResponse() throws Exception {
    // given
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);
    when(objectMapper.readValue(any(InputStream.class), (Class<?>) any(Class.class)))
      .thenThrow(mock(JsonParseException.class));
    when(objectMapper.writeValueAsBytes(any(FetchAndLockRequestDto.class)))
      .thenReturn(serializeRequest(new FetchAndLockRequestDto(MockProvider.WORKER_ID, MockProvider.MAX_TASKS,
        Collections.<TaskTopicRequestDto>emptyList())));

    CamundaClient camundaClient = CamundaClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    LockedTaskHandler lockedTaskHandlerMock = mock(LockedTaskHandler.class);
    WorkerSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock);

    // when
    workerSubscriptionBuilder.open();
    camundaClient.shutdown();

    // then
    verifyZeroInteractions(lockedTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionWhileMappingResponse() throws Exception {
    // given
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);
    when(objectMapper.readValue(any(InputStream.class), (Class<?>) any(Class.class)))
      .thenThrow(mock(JsonMappingException.class));
    when(objectMapper.writeValueAsBytes(any(FetchAndLockRequestDto.class)))
      .thenReturn(serializeRequest(new FetchAndLockRequestDto(MockProvider.WORKER_ID, MockProvider.MAX_TASKS,
        Collections.<TaskTopicRequestDto>emptyList())));

    CamundaClient camundaClient = CamundaClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    LockedTaskHandler lockedTaskHandlerMock = mock(LockedTaskHandler.class);
    WorkerSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock);

    // when
    workerSubscriptionBuilder.open();
    camundaClient.shutdown();

    // then
    verifyZeroInteractions(lockedTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionWhileDeserializingResponse() throws Exception {
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);
    when(objectMapper.readValue(any(InputStream.class), (Class<?>) any(Class.class)))
      .thenThrow(new IOException());
    when(objectMapper.writeValueAsBytes(any(FetchAndLockRequestDto.class)))
      .thenReturn(serializeRequest(new FetchAndLockRequestDto(MockProvider.WORKER_ID, MockProvider.MAX_TASKS,
        Collections.<TaskTopicRequestDto>emptyList())));

    CamundaClient camundaClient = CamundaClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    LockedTaskHandler lockedTaskHandlerMock = mock(LockedTaskHandler.class);
    WorkerSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock);

    // when
    workerSubscriptionBuilder.open();
    camundaClient.shutdown();

    // then
    verifyZeroInteractions(lockedTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionWhileSerializingRequest() throws Exception {
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);
    when(objectMapper.writeValueAsBytes(any(FetchAndLockRequestDto.class)))
      .thenThrow(mock(JsonProcessingException.class));

    CamundaClient camundaClient = CamundaClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    LockedTaskHandler lockedTaskHandlerMock = mock(LockedTaskHandler.class);
    WorkerSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock);

    // when
    workerSubscriptionBuilder.open();
    camundaClient.shutdown();

    // then
    verifyZeroInteractions(lockedTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionDueToWrongHttpStatusCode() throws IOException {
    // given
    mockStatic(HttpClients.class);
    HttpClient httpClient = mock(CloseableHttpClient.class);
    when(HttpClients.createDefault())
      .thenReturn((CloseableHttpClient) httpClient);

    CamundaClient camundaClient = CamundaClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    when(httpClient.execute(any(HttpUriRequest.class), any(AbstractResponseHandler.class)))
      .thenThrow(new HttpResponseException(404, "Not Found"));

    LockedTaskHandler lockedTaskHandlerMock = mock(LockedTaskHandler.class);

    WorkerSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock);

    // when
    workerSubscriptionBuilder.open();
    camundaClient.shutdown();

    // then
    verifyZeroInteractions(lockedTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionDueToClientProtocolProblem() throws IOException {
    // given
    mockStatic(HttpClients.class);
    HttpClient httpClient = mock(CloseableHttpClient.class);
    when(HttpClients.createDefault())
      .thenReturn((CloseableHttpClient) httpClient);

    when(httpClient.execute(any(HttpUriRequest.class), any(AbstractResponseHandler.class)))
      .thenThrow(new ClientProtocolException());

    CamundaClient camundaClient = CamundaClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    LockedTaskHandler lockedTaskHandlerMock = mock(LockedTaskHandler.class);

    WorkerSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock);

    // when
    workerSubscriptionBuilder.open();
    camundaClient.shutdown();

    // then
    verifyZeroInteractions(lockedTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionDueToIoProblem() throws IOException {
    // given
    mockStatic(HttpClients.class);
    HttpClient httpClient = mock(CloseableHttpClient.class);
    when(HttpClients.createDefault())
      .thenReturn((CloseableHttpClient) httpClient);

    when(httpClient.execute(any(HttpUriRequest.class), any(AbstractResponseHandler.class)))
      .thenThrow(new IOException());

    CamundaClient camundaClient = CamundaClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    LockedTaskHandler lockedTaskHandlerMock = mock(LockedTaskHandler.class);

    WorkerSubscriptionBuilder workerSubscriptionBuilder = camundaClient.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(lockedTaskHandlerMock);

    // when
    workerSubscriptionBuilder.open();
    camundaClient.shutdown();

    // then
    verifyZeroInteractions(lockedTaskHandlerMock);
  }

  // helper //////////////////
  private byte[] serializeRequest(AbstractDto dto) throws JsonProcessingException {
      return objectMapper.writeValueAsBytes(dto);
  }

}
