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
package org.camunda.bpm.client.spring.impl.client;

import static org.camunda.bpm.client.spring.annotation.EnableExternalTaskClient.STRING_ORDER_BY_ASC_VALUE;
import static org.camunda.bpm.client.spring.annotation.EnableExternalTaskClient.STRING_ORDER_BY_DESC_VALUE;

import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.ExternalTaskClientBuilder;
import org.camunda.bpm.client.backoff.BackoffStrategy;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.spring.exception.SpringExternalTaskClientException;
import org.camunda.bpm.client.spring.impl.client.util.ClientLoggerUtil;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;

public class ClientFactory
    implements FactoryBean<ExternalTaskClient>, InitializingBean {

  protected static final ClientLoggerUtil LOG = ClientLoggerUtil.CLIENT_LOGGER;

  protected ClientConfiguration clientConfiguration;

  protected BackoffStrategy backoffStrategy;
  protected List<ClientRequestInterceptor> requestInterceptors = new ArrayList<>();

  protected ExternalTaskClient client;

  protected PropertyResolver propertyResolver;

  @Override
  public ExternalTaskClient getObject() {
    if (client == null) {
      ExternalTaskClientBuilder clientBuilder = ExternalTaskClient.create();
      if (clientConfiguration.getBaseUrl() != null) {
          clientBuilder.baseUrl(resolve(clientConfiguration.getBaseUrl()));
      }
      if (clientConfiguration.getWorkerId() != null) {
        clientBuilder.workerId(resolve(clientConfiguration.getWorkerId()));
      }

      addClientRequestInterceptors(clientBuilder);

      if (clientConfiguration.getMaxTasks() != null) {
        clientBuilder.maxTasks(clientConfiguration.getMaxTasks());
      }
      if (clientConfiguration.getUsePriority() != null && !clientConfiguration.getUsePriority()) {
        clientBuilder.usePriority(false);
      }
      if (clientConfiguration.getDefaultSerializationFormat() != null) {
        clientBuilder.defaultSerializationFormat(resolve(clientConfiguration.getDefaultSerializationFormat()));
      }
      if (clientConfiguration.getDateFormat() != null) {
        clientBuilder.dateFormat(resolve(clientConfiguration.getDateFormat()));
      }
      if (clientConfiguration.getAsyncResponseTimeout() != null) {
        clientBuilder.asyncResponseTimeout(clientConfiguration.getAsyncResponseTimeout());
      }
      if (clientConfiguration.getLockDuration() != null) {
        clientBuilder.lockDuration(clientConfiguration.getLockDuration());
      }
      if (clientConfiguration.getDisableAutoFetching() != null &&
          clientConfiguration.getDisableAutoFetching()) {
        clientBuilder.disableAutoFetching();
      }
      if (clientConfiguration.getDisableBackoffStrategy() != null &&
          clientConfiguration.getDisableBackoffStrategy()) {
        clientBuilder.disableBackoffStrategy();
      }
      if (backoffStrategy != null) {
        clientBuilder.backoffStrategy(backoffStrategy);
      }

      tryConfigureCreateTimeOrder(clientBuilder);

      client = clientBuilder.build();
    }

    LOG.bootstrapped();

    return client;
  }

  protected void addClientRequestInterceptors(ExternalTaskClientBuilder taskClientBuilder) {
    requestInterceptors.forEach(taskClientBuilder::addInterceptor);
  }

  protected void tryConfigureCreateTimeOrder(ExternalTaskClientBuilder builder) {
    checkForCreateTimeMisconfiguration();

    if (isUseCreateTimeEnabled()) {
      builder.orderByCreateTime().desc();
      return;
    }

    if (isOrderByCreateTimeEnabled()) {
      handleOrderByCreateTimeConfig(builder);
    }
  }

  protected void handleOrderByCreateTimeConfig(ExternalTaskClientBuilder builder) {
    String orderByCreateTime = clientConfiguration.getOrderByCreateTime();

    if (STRING_ORDER_BY_ASC_VALUE.equals(orderByCreateTime)) {
      builder.orderByCreateTime().asc();
      return;
    }

    if (STRING_ORDER_BY_DESC_VALUE.equals(orderByCreateTime)) {
      builder.orderByCreateTime().desc();
      return;
    }

    throw new SpringExternalTaskClientException("Invalid value " + clientConfiguration.getOrderByCreateTime()
        + ". Please use either \"asc\" or \"desc\" value for configuring \"orderByCreateTime\" on the client");
  }

  protected boolean isOrderByCreateTimeEnabled() {
    return clientConfiguration.getOrderByCreateTime() != null;
  }

  protected boolean isUseCreateTimeEnabled() {
    return Boolean.TRUE.equals(clientConfiguration.getUseCreateTime());
  }

  protected void checkForCreateTimeMisconfiguration() {
    if (isUseCreateTimeEnabled() && isOrderByCreateTimeEnabled()) {
      throw new SpringExternalTaskClientException(
          "Both \"useCreateTime\" and \"orderByCreateTime\" are enabled. Please use one or the other");
    }
  }

  @Autowired(required = false)
  public void setRequestInterceptors(List<ClientRequestInterceptor> requestInterceptors) {
    if (requestInterceptors != null) {
      this.requestInterceptors.addAll(requestInterceptors);
      LOG.requestInterceptorsFound(this.requestInterceptors.size());
    }
  }

  @Autowired(required = false)
  public void setClientBackoffStrategy(BackoffStrategy backoffStrategy) {
    this.backoffStrategy = backoffStrategy;
    LOG.backoffStrategyFound();
  }

  @Override
  public Class<ExternalTaskClient> getObjectType() {
    return ExternalTaskClient.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
  }

  public ClientConfiguration getClientConfiguration() {
    return clientConfiguration;
  }

  public void setClientConfiguration(ClientConfiguration clientConfiguration) {
    this.clientConfiguration = clientConfiguration;
  }

  public List<ClientRequestInterceptor> getRequestInterceptors() {
    return requestInterceptors;
  }

  protected void close() {
    if (client != null) {
      client.stop();
    }
  }

  @Autowired(required = false)
  protected void setPropertyConfigurer(PropertySourcesPlaceholderConfigurer configurer) {
    PropertySources appliedPropertySources = configurer.getAppliedPropertySources();
    propertyResolver = new PropertySourcesPropertyResolver(appliedPropertySources);
  }

  protected String resolve(String property) {
    if (propertyResolver == null) {
      return property;
    }

    if (property != null) {
      return propertyResolver.resolvePlaceholders(property);
    } else {
      return null;
    }
  }

}