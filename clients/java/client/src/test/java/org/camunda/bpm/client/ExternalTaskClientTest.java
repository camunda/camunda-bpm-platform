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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.List;

import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.helper.MockProvider;
import org.camunda.bpm.client.impl.EngineClient;
import org.camunda.bpm.client.impl.ExternalTaskClientBuilderImpl;
import org.camunda.bpm.client.impl.ExternalTaskClientImpl;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.interceptor.auth.BasicAuthProvider;
import org.junit.Test;

/**
 * @author Tassilo Weidner
 */
public class ExternalTaskClientTest {

  @Test
  public void shouldSucceedAfterSanitizingBaseUrl() {
    // given & when
    ExternalTaskClient client = ExternalTaskClient.create()
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
      ExternalTaskClient.create()
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
      ExternalTaskClient.create()
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
    ExternalTaskClientBuilderImpl clientBuilder = spy(ExternalTaskClientBuilderImpl.class);
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
    ExternalTaskClient client = clientBuilder.build();

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

}
