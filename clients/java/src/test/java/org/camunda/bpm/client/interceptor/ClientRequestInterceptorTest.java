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
package org.camunda.bpm.client.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.protocol.HttpContext;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.helper.MockProvider;
import org.camunda.bpm.client.impl.ExternalTaskClientImpl;
import org.camunda.bpm.client.interceptor.auth.BasicAuthProvider;
import org.camunda.bpm.client.interceptor.impl.RequestInterceptorHandler;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Tassilo Weidner
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ExternalTaskClientImpl.class})
@PowerMockIgnore("javax.net.ssl.*")
public class ClientRequestInterceptorTest {

  @Test
  public void shouldSetAuthorizationHeader() throws Exception {
    // given
    AtomicBoolean interceptorInvoked = new AtomicBoolean(false);
    BasicAuthProvider basicAuthProvider = new BasicAuthProvider("demo", "demo") {

      @Override
      public void intercept(ClientRequestContext requestContext) {
        super.intercept(requestContext);
        interceptorInvoked.set(true);
      }

    };

    RequestInterceptorHandler requestInterceptorHandler = new RequestInterceptorHandler(Collections.singletonList(basicAuthProvider));
    RequestInterceptorHandler interceptionHandlerSpy = spy(requestInterceptorHandler);
    whenNew(RequestInterceptorHandler.class).withAnyArguments()
      .thenReturn(interceptionHandlerSpy);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      // .addInterceptor(...) interceptor is injected as a spy
      .build();

    // when
    client.subscribe(MockProvider.TOPIC_NAME)
      .lockDuration(5000)
      .handler(mock(ExternalTaskHandler.class))
      .open();


    while (!interceptorInvoked.get()) {
      // busy waiting
    }

    client.stop();

    // then
    ArgumentCaptor<HttpRequest> argumentCaptor = ArgumentCaptor.forClass(HttpRequest.class);
    verify(interceptionHandlerSpy).process(argumentCaptor.capture(), any(HttpContext.class));

    Header[] headers = argumentCaptor.getValue().getHeaders(HttpHeaders.AUTHORIZATION);
    assertThat(headers.length).isEqualTo(1);
    assertThat(headers[0].getValue()).isEqualTo("Basic ZGVtbzpkZW1v");
  }

  @Test
  public void shouldThrowExternalTaskClientExceptionDueToUsernameNull() {
    // given
    try {
      // when
      new BasicAuthProvider(null, "s3cret");

      fail("No ExternalTaskClientException thrown!");
    } catch (ExternalTaskClientException e) {
      // then
      assertThat(e.getMessage()).contains("Basic authentication credentials (username, password) cannot be null");
    }
  }

  @Test
  public void shouldThrowExternalTaskClientExceptionDueToPasswordNull() {
    // given
    try {
      // when
      new BasicAuthProvider("demo", null);

      fail("No ExternalTaskClientException thrown!");
    } catch (ExternalTaskClientException e) {
      // then
      assertThat(e.getMessage()).contains("Basic authentication credentials (username, password) cannot be null");
    }
  }

}
