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
package org.camunda.bpm.client.interceptor.impl;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.camunda.bpm.client.impl.EngineClientLogger;
import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class RequestInterceptorHandler implements HttpRequestInterceptor {

  protected static final EngineClientLogger LOG = ExternalTaskClientLogger.ENGINE_CLIENT_LOGGER;

  protected List<ClientRequestInterceptor> interceptors;

  public RequestInterceptorHandler(List<ClientRequestInterceptor> interceptors) {
    this.interceptors = interceptors;
  }

  @Override
  public void process(HttpRequest httpRequest, EntityDetails details, HttpContext context) throws HttpException, IOException {
    ClientRequestContextImpl interceptedRequest = new ClientRequestContextImpl();
    interceptors.forEach((ClientRequestInterceptor requestInterceptor) -> {
      try {
        requestInterceptor.intercept(interceptedRequest);
      }
      catch (Throwable e) {
        LOG.requestInterceptorException(e);
      }
    });

    Map<String, String> newHeaders = interceptedRequest.getHeaders();
    newHeaders.forEach((headerName, headerValue) -> httpRequest.addHeader(new BasicHeader(headerName, headerValue)));
  }

  public List<ClientRequestInterceptor> getInterceptors() {
    return interceptors;
  }

}
