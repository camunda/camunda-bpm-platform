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

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.ExternalTaskClientBuilder;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Tassilo Weidner
 */
public class ExternalTaskClientBuilderImpl implements ExternalTaskClientBuilder {

  protected static final ExternalTaskClientLogger LOG = ExternalTaskClientLogger.CLIENT_LOGGER;

  protected String baseUrl;
  protected String workerId;
  protected List<ClientRequestInterceptor> interceptors;

  public ExternalTaskClientBuilderImpl() {
    this.interceptors = new ArrayList<>();
  }

  public ExternalTaskClientBuilder baseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public ExternalTaskClientBuilder addInterceptor(ClientRequestInterceptor interceptor) {
    this.interceptors.add(interceptor);
    return this;
  }

  public ExternalTaskClient build() {
    if (baseUrl == null || baseUrl.isEmpty()) {
      throw LOG.baseUrlNullException();
    }

    checkInterceptors();

    String hostname = checkHostname();
    this.workerId = hostname + UUID.randomUUID();

    return new ExternalTaskClientImpl(this);
  }

  protected void checkInterceptors() {
    interceptors.forEach(interceptor -> {
      if (interceptor == null) {
        throw LOG.interceptorNullException();
      }
    });
  }

  public String checkHostname() {
    String hostname;
    try {
      hostname = getHostname();
    } catch (UnknownHostException e) {
      throw LOG.cannotGetHostnameException();
    }

    return hostname;
  }

  public String getHostname() throws UnknownHostException {
    return InetAddress.getLocalHost().getHostName();
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  protected String getWorkerId() {
    return workerId;
  }

  protected List<ClientRequestInterceptor> getInterceptors() {
    return interceptors;
  }

}
