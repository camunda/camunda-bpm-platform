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

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.UrlResolver;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

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
    public void testLoadBalanceHttpClient() {

        var address = List.of("server1", "server2", "server3");
        var tryAddress = new HashSet<>();


        RoundAddressResolver roundAddressResolver = new RoundAddressResolver(address);
        ExternalTaskClientBuilderImpl2 clientBuilder = new ExternalTaskClientBuilderImpl2().urlResolver(roundAddressResolver);
        clientBuilder.build();

        for (int i = 0; i < address.size(); i++) {
            tryAddress.add(clientBuilder.getEngineClient().getBaseUrl());
        }
        assertThat(new HashSet<>(address).equals(tryAddress)).isEqualTo(true);
    }


    //Just used for get EngineClient
    class ExternalTaskClientBuilderImpl2 extends ExternalTaskClientBuilderImpl {

        @Override
        public EngineClient getEngineClient() {
            return super.getEngineClient();
        }

        @Override
        public ExternalTaskClientBuilderImpl2 urlResolver(UrlResolver urlResolver) {
            this.urlResolver = urlResolver;
            return this;
        }

        @Override
        public ExternalTaskClient build() {
            if (maxTasks <= 0) {
                throw LOG.maxTasksNotGreaterThanZeroException(maxTasks);
            }

            if (asyncResponseTimeout != null && asyncResponseTimeout <= 0) {
                throw LOG.asyncResponseTimeoutNotGreaterThanZeroException(asyncResponseTimeout);
            }

            if (lockDuration <= 0L) {
                throw LOG.lockDurationIsNotGreaterThanZeroException(lockDuration);
            }

            if (urlResolver == null || getBaseUrl() == null || getBaseUrl().isEmpty()) {
                throw LOG.baseUrlNullException();
            }

            checkInterceptors();

            orderingConfig.validateOrderingProperties();

            initBaseUrl();
            initWorkerId();
            initObjectMapper();
            initEngineClient();
            initVariableMappers();
            initTopicSubscriptionManager();

            return new ExternalTaskClientImpl(topicSubscriptionManager);
        }
    }


    /**
     * Round robin load balancing used for test testLoadBalanceHttpClient
     */
    class RoundAddressResolver implements UrlResolver {

        public RoundAddressResolver(List<String> urls) {
            this.urls = urls;
            i = 0;
            max = urls.size();
        }

        List<String> urls;
        int i;
        int max;

        public String getBaseUrl() {

            String address = urls.get(i);
            if (i++ >= max) i = 0;
            return address;
        }
    }

}
