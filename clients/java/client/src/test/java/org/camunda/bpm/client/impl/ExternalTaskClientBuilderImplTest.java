/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.client.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.UrlResolver;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ExternalTaskClientBuilderImplTest {

  @Test
  public void testCustomizeHttpClientExposesInternalHttpClientBuilder() {
    // given
    var clientBuilder = new ExternalTaskClientBuilderImpl();
    var requestConfigArgumentCaptor = ArgumentCaptor.forClass(RequestConfig.class);
    var httpClientBuilderSpy = spy(HttpClientBuilder.class);
    var httpClientBuilderField = ReflectUtil.getField("httpClientBuilder", clientBuilder);
    ReflectUtil.setField(httpClientBuilderField, clientBuilder, httpClientBuilderSpy);

    ExternalTaskClient client = null;
    try {
      // when
      client = clientBuilder.baseUrl("localhost")
          .customizeHttpClient(httpClientBuilder -> httpClientBuilder.setDefaultRequestConfig(RequestConfig.custom()
              .setResponseTimeout(Timeout.ofSeconds(5))
              .setConnectionRequestTimeout(Timeout.ofSeconds(6))
              .build()))
          .build();

      // then
      verify(httpClientBuilderSpy).build();
      verify(httpClientBuilderSpy).setDefaultRequestConfig(requestConfigArgumentCaptor.capture());

      var requestConfig = requestConfigArgumentCaptor.getValue();
      assertThat(requestConfig.getResponseTimeout().toSeconds()).isEqualTo(5);
      assertThat(requestConfig.getConnectionRequestTimeout().toSeconds()).isEqualTo(6);
    } finally {
      if (client != null) {
        client.stop();
      }
    }
  }

  @Test
  public void testCustomBaseUrlResolver() {
    // given
    var expectedBaseUrl = "expectedBaseUrl";
    TestUrlResolver testUrlResolver = new TestUrlResolver(expectedBaseUrl);

    // when
    var clientBuilder = new ExternalTaskClientBuilderImpl();
    clientBuilder.urlResolver(testUrlResolver);
    clientBuilder.build();

    // then
    assertThat(spy(clientBuilder).engineClient.getBaseUrl()).isEqualTo(expectedBaseUrl);
  }

  static class TestUrlResolver implements UrlResolver {
    final String baseUrl;

    public TestUrlResolver(final String baseURl) {
      this.baseUrl = baseURl;
    }

    public String getBaseUrl() {
      return baseUrl;
    }
  }

}
