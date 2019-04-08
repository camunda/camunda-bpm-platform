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

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.camunda.connect.httpclient.HttpBaseRequest;
import org.camunda.connect.httpclient.HttpResponse;
import org.camunda.connect.impl.AbstractConnectorRequest;
import org.camunda.connect.spi.Connector;

public class AbstractHttpRequest<Q extends HttpBaseRequest<?, ?>, R extends HttpResponse> extends AbstractConnectorRequest<R> {

  private final HttpConnectorLogger LOG = HttpLogger.HTTP_LOGGER;

  public AbstractHttpRequest(Connector connector) {
    super(connector);
  }

  @SuppressWarnings("unchecked")
  public Q url(String url) {
    setRequestParameter(HttpBaseRequest.PARAM_NAME_REQUEST_URL, url);
    return (Q) this;
  }

  public String getUrl() {
    return getRequestParameter(HttpBaseRequest.PARAM_NAME_REQUEST_URL);
  }

  @SuppressWarnings("unchecked")
  public Q method(String method) {
    setRequestParameter(HttpBaseRequest.PARAM_NAME_REQUEST_METHOD, method);
    return (Q) this;
  }

  public String getMethod() {
    return getRequestParameter(HttpBaseRequest.PARAM_NAME_REQUEST_METHOD);
  }

  @SuppressWarnings("unchecked")
  public Q header(String field, String value) {
    if (field == null || field.isEmpty() || value == null || value.isEmpty()) {
      LOG.ignoreHeader(field, value);
    }
    else {
      Map<String, String> headers = getRequestParameter(HttpBaseRequest.PARAM_NAME_REQUEST_HEADERS);

      if (headers == null) {
        headers = new HashMap<String, String>();
        setRequestParameter(HttpBaseRequest.PARAM_NAME_REQUEST_HEADERS, headers);
      }
      headers.put(field, value);
    }

    return (Q) this;
  }

  public String getHeader(String field) {
    Map<String, String> headers = getHeaders();
    if (headers != null) {
      return headers.get(field);
    }
    else {
      return null;
    }
  }

  public Map<String, String> getHeaders() {
    return getRequestParameter(HttpBaseRequest.PARAM_NAME_REQUEST_HEADERS);
  }

  public Q contentType(String contentType) {
    return header(HttpBaseRequest.HEADER_CONTENT_TYPE, contentType);
  }

  public String getContentType() {
    return getHeader(HttpBaseRequest.HEADER_CONTENT_TYPE);
  }

  @SuppressWarnings("unchecked")
  public Q payload(String payload) {
    setRequestParameter(HttpBaseRequest.PARAM_NAME_REQUEST_PAYLOAD, payload);
    return (Q) this;
  }

  public String getPayload() {
    return getRequestParameter(HttpBaseRequest.PARAM_NAME_REQUEST_PAYLOAD);
  }

  public Q get() {
    return method(HttpGet.METHOD_NAME);
  }

  public Q post() {
    return method(HttpPost.METHOD_NAME);
  }

  public Q put() {
    return method(HttpPut.METHOD_NAME);
  }

  public Q delete() {
    return method(HttpDelete.METHOD_NAME);
  }

  public Q patch() {
    return method(HttpPatch.METHOD_NAME);
  }

  public Q head() {
    return method(HttpHead.METHOD_NAME);
  }

  public Q options() {
    return method(HttpOptions.METHOD_NAME);
  }

  public Q trace() {
    return method(HttpTrace.METHOD_NAME);
  }

}
