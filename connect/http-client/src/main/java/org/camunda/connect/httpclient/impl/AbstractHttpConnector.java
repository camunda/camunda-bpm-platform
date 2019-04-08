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

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.camunda.connect.httpclient.HttpBaseRequest;
import org.camunda.connect.httpclient.HttpResponse;
import org.camunda.connect.impl.AbstractConnector;

public abstract class AbstractHttpConnector<Q extends HttpBaseRequest<Q, R>, R extends HttpResponse> extends AbstractConnector<Q, R> {

  protected static HttpConnectorLogger LOG = HttpLogger.HTTP_LOGGER;

  protected CloseableHttpClient httpClient;
  protected final Charset charset;

  public AbstractHttpConnector(String connectorId) {
    super(connectorId);
    httpClient = createClient();
    charset = Charset.forName("utf-8");
  }

  protected CloseableHttpClient createClient() {
    return HttpClients.createSystem();
  }

  public CloseableHttpClient getHttpClient() {
    return httpClient;
  }

  public void setHttpClient(CloseableHttpClient httpClient) {
    this.httpClient = httpClient;
  }

  @Override
  public R execute(Q request) {
    HttpRequestBase httpRequest = createHttpRequest(request);

    HttpRequestInvocation invocation = new HttpRequestInvocation(httpRequest, request, requestInterceptors, httpClient);

    try {
      return createResponse((CloseableHttpResponse) invocation.proceed());
    } catch (Exception e) {
      throw LOG.unableToExecuteRequest(e);
    }

  }

  protected abstract R createResponse(CloseableHttpResponse response);

  @Override
  public abstract Q createRequest();

  /**
   * creates a apache Http* representation of the request.
   *
   * @param request the given request
   * @return {@link HttpRequestBase} an apache representation of the request
   */
  protected <T extends HttpRequestBase> T createHttpRequest(Q request) {
    T httpRequest = createHttpRequestBase(request);

    applyHeaders(httpRequest, request.getHeaders());

    applyPayload(httpRequest, request);

    return httpRequest;
  }

  @SuppressWarnings("unchecked")
  protected <T extends HttpRequestBase> T createHttpRequestBase(Q request) {
    String url = request.getUrl();
    if (url != null && !url.trim().isEmpty()) {
      String method = request.getMethod();
      if (HttpGet.METHOD_NAME.equals(method)) {
        return (T) new HttpGet(url);
      } else if (HttpPost.METHOD_NAME.equals(method)) {
        return (T) new HttpPost(url);
      } else if (HttpPut.METHOD_NAME.equals(method)) {
        return (T) new HttpPut(url);
      } else if (HttpDelete.METHOD_NAME.equals(method)) {
        return (T) new HttpDelete(url);
      } else if (HttpPatch.METHOD_NAME.equals(method)) {
        return (T) new HttpPatch(url);
      } else if (HttpHead.METHOD_NAME.equals(method)) {
        return (T) new HttpHead(url);
      } else if (HttpOptions.METHOD_NAME.equals(method)) {
        return (T) new HttpOptions(url);
      } else if (HttpTrace.METHOD_NAME.equals(method)) {
        return (T) new HttpTrace(url);
      } else {
        throw LOG.unknownHttpMethod(method);
      }
    }
    else {
      throw LOG.requestUrlRequired();
    }
  }

  protected <T extends HttpRequestBase> void applyHeaders(T httpRequest, Map<String, String> headers) {
    if (headers != null) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        httpRequest.setHeader(entry.getKey(), entry.getValue());
        LOG.setHeader(entry.getKey(), entry.getValue());
      }
    }
  }

  protected <T extends HttpRequestBase> void applyPayload(T httpRequest, Q request) {
    if (httpMethodSupportsPayload(httpRequest)) {
      if (request.getPayload() != null) {
        byte[] bytes = request.getPayload().getBytes(charset);
        ByteArrayInputStream payload = new ByteArrayInputStream(bytes);
        InputStreamEntity entity = new InputStreamEntity(payload, bytes.length);
        ((HttpEntityEnclosingRequestBase) httpRequest).setEntity(entity);
      }
    }
    else if (request.getPayload() != null) {
      LOG.payloadIgnoredForHttpMethod(request.getMethod());
    }
  }

  protected <T extends HttpRequestBase> boolean httpMethodSupportsPayload(T httpRequest) {
    return httpRequest instanceof HttpEntityEnclosingRequestBase;
  }

}
