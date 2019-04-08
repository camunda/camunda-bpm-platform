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
package org.camunda.connect.httpclient.impl;

import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.camunda.connect.spi.ConnectorRequest;
import org.camunda.connect.impl.AbstractRequestInvocation;
import org.camunda.connect.spi.ConnectorRequestInterceptor;

public class HttpRequestInvocation  extends AbstractRequestInvocation<HttpRequestBase> {

  protected HttpClient client;

  public HttpRequestInvocation(HttpRequestBase target, ConnectorRequest<?> request, List<ConnectorRequestInterceptor> interceptorChain, HttpClient client) {
    super(target, request, interceptorChain);
    this.client = client;
  }

  public Object invokeTarget() throws Exception {
    // execute the request
    return client.execute(target);
  }

}
