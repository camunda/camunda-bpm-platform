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

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.camunda.connect.httpclient.HttpResponse;
import org.camunda.connect.impl.AbstractCloseableConnectorResponse;
import org.camunda.commons.utils.IoUtil;

public class HttpResponseImpl extends AbstractCloseableConnectorResponse implements HttpResponse {

  private final HttpConnectorLogger LOG = HttpLogger.HTTP_LOGGER;

  protected CloseableHttpResponse httpResponse;

  public HttpResponseImpl(CloseableHttpResponse httpResponse) {
    this.httpResponse = httpResponse;
  }

  public Integer getStatusCode() {
    return getResponseParameter(PARAM_NAME_STATUS_CODE);
  }

  public String getResponse() {
    return getResponseParameter(PARAM_NAME_RESPONSE);
  }

  public Map<String, String> getHeaders() {
    return getResponseParameter(PARAM_NAME_RESPONSE_HEADERS);
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

  protected void collectResponseParameters(Map<String, Object> responseParameters) {
    if (httpResponse.getStatusLine() != null) {
      responseParameters.put(PARAM_NAME_STATUS_CODE, httpResponse.getStatusLine().getStatusCode());
    }
    collectResponseHeaders();

    if (httpResponse.getEntity() != null) {
      try {
        String response = IoUtil.inputStreamAsString(httpResponse.getEntity().getContent());
        responseParameters.put(PARAM_NAME_RESPONSE, response);
      } catch (IOException e) {
        throw LOG.unableToReadResponse(e);
      } finally {
        IoUtil.closeSilently(httpResponse);
      }
    }
  }

  protected void collectResponseHeaders() {
    Map<String, String> headers = new HashMap<String, String>();
    for (Header header : httpResponse.getAllHeaders()) {
      headers.put(header.getName(), header.getValue());
    }
    responseParameters.put(PARAM_NAME_RESPONSE_HEADERS, headers);
  }

  protected Closeable getClosable() {
    return httpResponse;
  }

}
