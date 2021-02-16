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
package org.camunda.bpm.client.spring.boot.starter.impl;

import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.interceptor.auth.BasicAuthProvider;
import org.camunda.bpm.client.spring.boot.starter.impl.properties.BasicAuthProperties;
import org.camunda.bpm.client.spring.boot.starter.impl.properties.CamundaBpmClientProperties;
import org.camunda.bpm.client.spring.impl.client.ClientConfiguration;
import org.camunda.bpm.client.spring.impl.client.ClientFactory;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class PropertiesAwareClientFactory extends ClientFactory {

  @Autowired
  protected CamundaBpmClientProperties camundaBpmClientProperties;

  @Override
  public void afterPropertiesSet() throws Exception {
    applyPropertiesFrom(camundaBpmClientProperties);
    addBasicAuthInterceptor();
    super.afterPropertiesSet();
  }

  protected void addBasicAuthInterceptor() {
    BasicAuthProperties basicAuth = camundaBpmClientProperties.getBasicAuth();
    if (basicAuth.isEnabled()) {
      List<ClientRequestInterceptor> interceptors = getRequestInterceptors();

      String username = basicAuth.getUsername();
      String password = basicAuth.getPassword();
      BasicAuthProvider basicAuthProvider = new BasicAuthProvider(username, password);

      interceptors.add(basicAuthProvider);
    }
  }

  public void applyPropertiesFrom(CamundaBpmClientProperties clientConfigurationProps) {
    ClientConfiguration clientConfiguration = new ClientConfiguration();
    if (clientConfigurationProps.getBaseUrl() != null) {
      clientConfiguration.setBaseUrl(clientConfigurationProps.getBaseUrl());
    }
    if (clientConfigurationProps.getWorkerId() != null) {
      clientConfiguration.setWorkerId(clientConfigurationProps.getWorkerId());
    }
    if (clientConfigurationProps.getMaxTasks() != null) {
      clientConfiguration.setMaxTasks(clientConfigurationProps.getMaxTasks());
    }
    if (clientConfigurationProps.getUsePriority() != null && !clientConfigurationProps.getUsePriority()) {
      clientConfiguration.setUsePriority(false);
    }
    if (clientConfigurationProps.getDefaultSerializationFormat() != null) {
      clientConfiguration.setDefaultSerializationFormat(clientConfigurationProps.getDefaultSerializationFormat());
    }
    if (clientConfigurationProps.getDateFormat() != null) {
      clientConfiguration.setDateFormat(clientConfigurationProps.getDateFormat());
    }
    if (clientConfigurationProps.getLockDuration() != null) {
      clientConfiguration.setLockDuration(clientConfigurationProps.getLockDuration());
    }
    if (clientConfigurationProps.getAsyncResponseTimeout() != null) {
      clientConfiguration.setAsyncResponseTimeout(clientConfigurationProps.getAsyncResponseTimeout());
    }
    if (clientConfigurationProps.getDisableAutoFetching() != null &&
        clientConfigurationProps.getDisableAutoFetching()) {
      clientConfiguration.setDisableAutoFetching(true);
    }
    if (clientConfigurationProps.getDisableBackoffStrategy() != null &&
        clientConfigurationProps.getDisableBackoffStrategy()) {
      clientConfiguration.setDisableBackoffStrategy(true);
    }
    setClientConfiguration(clientConfiguration);
  }

}