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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.exception.BpmnErrorException;
import org.camunda.bpm.client.exception.CompleteTaskException;
import org.camunda.bpm.client.exception.ExtendLockException;
import org.camunda.bpm.client.exception.TaskFailureException;
import org.camunda.bpm.client.exception.UnlockTaskException;
import org.camunda.bpm.client.helper.MockProvider;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.client.task.impl.FailureRequestDto;
import org.camunda.bpm.client.task.impl.dto.BpmnErrorRequestDto;
import org.camunda.bpm.client.task.impl.dto.ExtendLockRequestDto;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * @author Tassilo Weidner
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpClients.class, RequestExecutor.class})
@PowerMockIgnore("javax.net.ssl.*")
public class LockedTaskServiceTest {

  private CloseableHttpClient httpClient;
  private CloseableHttpResponse closeableHttpResponse;

  @Before
  public void setUp() throws JsonProcessingException {
    mockStatic(HttpClients.class);

    closeableHttpResponse = mock(CloseableHttpResponse.class);
    Mockito.when(closeableHttpResponse.getStatusLine())
      .thenReturn(mock(StatusLine.class));

    httpClient = spy(new ClosableHttpClientMock(closeableHttpResponse));
    Mockito.when(HttpClients.createDefault())
      .thenReturn(httpClient);

    List<ExternalTask> lockedTasks = Collections.singletonList(MockProvider.createLockedTask());
    ObjectMapper objectMapper = new ObjectMapper();
    byte[] lockedTasksAsBytes = objectMapper.writeValueAsBytes(lockedTasks);
    HttpEntity entity = new ByteArrayEntity(lockedTasksAsBytes);
    doReturn(entity)
      .when(closeableHttpResponse).getEntity();
  }

  @Test
  public void shouldUnlockTask() throws IOException {
    // given
    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    ExternalTaskHandler lockedTaskHandler = new ExternalTaskHandler() {
      @Override
      public void execute(ExternalTask lockedTask, ExternalTaskService lockedTaskService) {
        lockedTaskService.unlock();
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
      // sync
    }

    // then
    assertRequestPerformed(EngineClient.UNLOCK_RESOURCE_PATH);

    camundaClient.shutdown();
  }

  @Test
  public void shouldCompleteTask() throws IOException {
    // given
    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    ExternalTaskHandler lockedTaskHandler = new ExternalTaskHandler() {
      @Override
      public void execute(ExternalTask lockedTask, ExternalTaskService lockedTaskService) {
        lockedTaskService.complete();
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
      // sync
    }

    // then
    assertRequestPerformed(EngineClient.COMPLETE_RESOURCE_PATH);

    camundaClient.shutdown();
  }

  @Test
  public void shouldHandleFailure() throws Exception {
    // given
    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    ExternalTaskHandler lockedTaskHandler = new ExternalTaskHandler() {
      @Override
      public void execute(ExternalTask lockedTask, ExternalTaskService lockedTaskService) {
        lockedTaskService.failure(MockProvider.ERROR_MESSAGE, MockProvider.ERROR_DETAILS,
          MockProvider.RETRIES, MockProvider.RETRY_TIMEOUT);
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
      // sync
    }

    // then
    ArgumentCaptor<Object> payloads = ArgumentCaptor.forClass(Object.class);
    verify(objectMapper, atLeastOnce()).writeValueAsBytes(payloads.capture());

    for (Object request : payloads.getAllValues()) {
      if (request instanceof FailureRequestDto) {
        FailureRequestDto failureRequestDto = (FailureRequestDto) request;
        assertTrue(failureRequestDto.getWorkerId().length() > 35);
        assertThat(failureRequestDto.getErrorDetails(), is(MockProvider.ERROR_DETAILS));
        assertThat(failureRequestDto.getErrorMessage(), is(MockProvider.ERROR_MESSAGE));
        assertThat(failureRequestDto.getRetries(), is(MockProvider.RETRIES));
        assertThat(failureRequestDto.getRetryTimeout(), is(MockProvider.RETRY_TIMEOUT));
      }
    }

    assertRequestPerformed(EngineClient.FAILURE_RESOURCE_PATH);

    camundaClient.shutdown();
  }

  @Test
  public void shouldHandleBpmnError() throws Exception {
    // given
    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    ExternalTaskHandler lockedTaskHandler = new ExternalTaskHandler() {
      @Override
      public void execute(ExternalTask lockedTask, ExternalTaskService lockedTaskService) {
        lockedTaskService.bpmnError(MockProvider.ERROR_CODE);
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
      // sync
    }

    // then
    ArgumentCaptor<Object> payloads = ArgumentCaptor.forClass(Object.class);
    verify(objectMapper, atLeastOnce()).writeValueAsBytes(payloads.capture());

    for (Object request : payloads.getAllValues()) {
      if (request instanceof BpmnErrorRequestDto) {
        BpmnErrorRequestDto bpmnErrorRequestDto = (BpmnErrorRequestDto) request;
        assertTrue(bpmnErrorRequestDto.getWorkerId().length() > 35);
        assertThat(bpmnErrorRequestDto.getErrorCode(), is(MockProvider.ERROR_CODE));
      }
    }

    assertRequestPerformed(EngineClient.BPMN_ERROR_RESOURCE_PATH);

    camundaClient.shutdown();
  }

  @Test
  public void shouldExtendLock() throws Exception {
    // given
    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    ExternalTaskHandler lockedTaskHandler = new ExternalTaskHandler() {
      @Override
      public void execute(ExternalTask lockedTask, ExternalTaskService lockedTaskService) {
        lockedTaskService.extendLock(MockProvider.NEW_DURATION);
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
      // sync
    }

    // then
    ArgumentCaptor<Object> payloads = ArgumentCaptor.forClass(Object.class);
    verify(objectMapper, atLeastOnce()).writeValueAsBytes(payloads.capture());

    for (Object request : payloads.getAllValues()) {
      if (request instanceof ExtendLockRequestDto) {
        ExtendLockRequestDto extendLockRequestDto = (ExtendLockRequestDto) request;
        assertTrue(extendLockRequestDto.getWorkerId().length() > 35);
        assertThat(extendLockRequestDto.getNewDuration(), is(MockProvider.NEW_DURATION));
      }
    }

    assertRequestPerformed(EngineClient.EXTEND_LOCK_RESOURCE_PATH);

    camundaClient.shutdown();
  }

  @Test
  public void shouldThrowUnlockTaskException() throws IOException {
    // given
    CloseableHttpClient httpClient = mockHttpResponseException(EngineClient.UNLOCK_RESOURCE_PATH);

    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    final AtomicBoolean exceptionThrown = new AtomicBoolean(false);
    final List<UnlockTaskException> unlockTaskException = new ArrayList<UnlockTaskException>(); // list, as container must be final and changeable
    ExternalTaskHandler lockedTaskHandler = new ExternalTaskHandler() {
      @Override
      public void execute(ExternalTask lockedTask, ExternalTaskService lockedTaskService) {
        try {
          lockedTaskService.unlock();
        } catch (UnlockTaskException e) {
          unlockTaskException.add(e);
          exceptionThrown.set(true);
        }
      }
    };

    TopicSubscriptionBuilder workerSubscriptionBuilder =
      camundaClient.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler(lockedTaskHandler);

    // when
    workerSubscriptionBuilder.open();

    while (!exceptionThrown.get()) {
      // sync
    }

    // then
    assertThat(unlockTaskException.get(0).getMessage(), containsString("Exception while unlocking task"));
    assertThat(unlockTaskException.get(0).getMessage(), containsString("returned error: status code '404' - message: Not Found!"));
    assertRequestPerformed(EngineClient.UNLOCK_RESOURCE_PATH, httpClient);

    camundaClient.shutdown();
  }

  @Test
  public void shouldThrowCompleteTaskException() throws IOException {
    // given
    CloseableHttpClient httpClient = mockHttpResponseException(EngineClient.COMPLETE_RESOURCE_PATH);

    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    final AtomicBoolean exceptionThrown = new AtomicBoolean(false);
    final List<CompleteTaskException> completeTaskException = new ArrayList<CompleteTaskException>(); // list, as container must be final and changeable
    ExternalTaskHandler lockedTaskHandler = new ExternalTaskHandler() {
      @Override
      public void execute(ExternalTask lockedTask, ExternalTaskService lockedTaskService) {
        try {
          lockedTaskService.complete();
        } catch (CompleteTaskException e) {
          completeTaskException.add(e);
          exceptionThrown.set(true);
        }
      }
    };

    TopicSubscriptionBuilder workerSubscriptionBuilder =
      camundaClient.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler(lockedTaskHandler);

    // when
    workerSubscriptionBuilder.open();

    while (!exceptionThrown.get()) {
      // sync
    }

    // then
    assertThat(completeTaskException.get(0).getMessage(), containsString("Exception while completing task"));
    assertThat(completeTaskException.get(0).getMessage(), containsString("returned error: status code '404' - message: Not Found!"));
    assertRequestPerformed(EngineClient.COMPLETE_RESOURCE_PATH, httpClient);

    camundaClient.shutdown();
  }

  @Test
  public void shouldThrowTaskFailureException() throws IOException {
    // given
    CloseableHttpClient httpClient = mockHttpResponseException(EngineClient.FAILURE_RESOURCE_PATH);

    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    final AtomicBoolean exceptionThrown = new AtomicBoolean(false);
    final List<TaskFailureException> taskFailureException = new ArrayList<TaskFailureException>(); // list, as container must be final and changeable
    ExternalTaskHandler lockedTaskHandler = new ExternalTaskHandler() {
      @Override
      public void execute(ExternalTask lockedTask, ExternalTaskService lockedTaskService) {
        try {
          lockedTaskService.failure(MockProvider.ERROR_MESSAGE, MockProvider.ERROR_DETAILS, MockProvider.RETRIES, MockProvider.RETRY_TIMEOUT);
        } catch (TaskFailureException e) {
          taskFailureException.add(e);
          exceptionThrown.set(true);
        }
      }
    };

    TopicSubscriptionBuilder workerSubscriptionBuilder =
      camundaClient.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler(lockedTaskHandler);

    // when
    workerSubscriptionBuilder.open();

    while (!exceptionThrown.get()) {
      // sync
    }

    // then
    assertThat(taskFailureException.get(0).getMessage(), containsString("Exception while notifying task failure"));
    assertThat(taskFailureException.get(0).getMessage(), containsString("returned error: status code '404' - message: Not Found!"));
    assertRequestPerformed(EngineClient.FAILURE_RESOURCE_PATH, httpClient);

    camundaClient.shutdown();
  }

  @Test
  public void shouldThrowBpmnErrorException() throws IOException {
    // given
    CloseableHttpClient httpClient = mockHttpResponseException(EngineClient.BPMN_ERROR_RESOURCE_PATH);

    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    final AtomicBoolean exceptionThrown = new AtomicBoolean(false); // list, as container must be final and changeable
    final List<BpmnErrorException> bpmnErrorException = new ArrayList<BpmnErrorException>();
    ExternalTaskHandler lockedTaskHandler = new ExternalTaskHandler() {
      @Override
      public void execute(ExternalTask lockedTask, ExternalTaskService lockedTaskService) {
        try {
          lockedTaskService.bpmnError(MockProvider.ERROR_CODE);
        } catch (BpmnErrorException e) {
          bpmnErrorException.add(e);
          exceptionThrown.set(true);
        }
      }
    };

    TopicSubscriptionBuilder workerSubscriptionBuilder =
      camundaClient.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler(lockedTaskHandler);

    // when
    workerSubscriptionBuilder.open();

    while (!exceptionThrown.get()) {
      // sync
    }

    // then
    assertThat(bpmnErrorException.get(0).getMessage(), containsString("Exception while notifying bpmn error"));
    assertThat(bpmnErrorException.get(0).getMessage(), containsString("returned error: status code '404' - message: Not Found!"));
    assertRequestPerformed(EngineClient.BPMN_ERROR_RESOURCE_PATH, httpClient);

    camundaClient.shutdown();
  }

  @Test
  public void shouldThrowExtendLockException() throws IOException {
    // given
    CloseableHttpClient httpClient = mockHttpResponseException(EngineClient.EXTEND_LOCK_RESOURCE_PATH);

    ExternalTaskClient camundaClient = ExternalTaskClient.create()
      .endpointUrl(MockProvider.ENDPOINT_URL)
      .build();

    final AtomicBoolean exceptionThrown = new AtomicBoolean(false);
    final List<ExtendLockException> extendLockException = new ArrayList<ExtendLockException>(); // list, as container must be final and changeable
    ExternalTaskHandler lockedTaskHandler = new ExternalTaskHandler() {
      @Override
      public void execute(ExternalTask lockedTask, ExternalTaskService lockedTaskService) {
        try {
          lockedTaskService.extendLock(MockProvider.NEW_DURATION);
        } catch (ExtendLockException e) {
          extendLockException.add(e);
          exceptionThrown.set(true);
        }
      }
    };

    TopicSubscriptionBuilder workerSubscriptionBuilder =
      camundaClient.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler(lockedTaskHandler);

    // when
    workerSubscriptionBuilder.open();

    while (!exceptionThrown.get()) {
      // sync
    }

    // then
    assertThat(extendLockException.get(0).getMessage(), containsString("Exception while extending lock"));
    assertThat(extendLockException.get(0).getMessage(), containsString("returned error: status code '404' - message: Not Found!"));
    assertRequestPerformed(EngineClient.EXTEND_LOCK_RESOURCE_PATH, httpClient);

    camundaClient.shutdown();
  }

  // helper ////////////////////////////////////////////////
  private void assertRequestPerformed(String resourcePath) throws IOException {
    assertRequestPerformed(resourcePath, httpClient);
  }

  private void assertRequestPerformed(String resourcePath, CloseableHttpClient httpClient) throws IOException {
    ArgumentCaptor<HttpUriRequest> argumentCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
    verify(httpClient, atLeastOnce()).execute(argumentCaptor.capture(), any(AbstractResponseHandler.class));

    String resourceUrl = MockProvider.ENDPOINT_URL + resourcePath;
    resourceUrl = resourceUrl.replace(EngineClient.ID_PATH_PARAM, MockProvider.ID);

    List<String> requestUrls = new ArrayList<String>();
    List<HttpUriRequest> requests = argumentCaptor.getAllValues();
    for (HttpUriRequest request : requests) {
      requestUrls.add(request.getURI().toString());
    }

    assertThat(requestUrls, hasItems(resourceUrl));
  }

  private CloseableHttpClient mockHttpResponseException(final String resourcePath) {
    CloseableHttpClient httpClient = spy(new ClosableHttpClientMock(closeableHttpResponse) {
      @Override
      protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
        String resourceUrl = MockProvider.ENDPOINT_URL + resourcePath;
        resourceUrl = resourceUrl.replace(EngineClient.ID_PATH_PARAM, MockProvider.ID);
        if (request.toString().equals("POST " + resourceUrl + " HTTP/1.1"))
          throw new HttpResponseException(404, "Not Found!");

        return closeableHttpResponse;
      }
    });

    when(HttpClients.createDefault())
      .thenReturn(httpClient);

    return httpClient;
  }

}
