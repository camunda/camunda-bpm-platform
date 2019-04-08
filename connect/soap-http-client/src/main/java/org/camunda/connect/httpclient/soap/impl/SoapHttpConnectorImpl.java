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
package org.camunda.connect.httpclient.soap.impl;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.camunda.connect.httpclient.impl.AbstractHttpConnector;
import org.camunda.connect.httpclient.impl.AbstractHttpRequest;
import org.camunda.connect.httpclient.soap.SoapHttpConnector;
import org.camunda.connect.httpclient.soap.SoapHttpRequest;
import org.camunda.connect.httpclient.soap.SoapHttpResponse;

public class SoapHttpConnectorImpl extends AbstractHttpConnector<SoapHttpRequest, SoapHttpResponse> implements SoapHttpConnector {

  protected static final SoapHttpConnectorLogger LOG = SoapHttpLogger.SOAP_CONNECTOR_LOGGER;

  public SoapHttpConnectorImpl() {
    super(SoapHttpConnector.ID);
  }

  public SoapHttpConnectorImpl(String connectorId) {
    super(connectorId);
  }

  public SoapHttpRequest createRequest() {
    return new SoapHttpRequestImpl(this);
  }

  protected SoapHttpResponse createResponse(CloseableHttpResponse response) {
    return new SoapHttpResponseImpl(response);
  }

  @Override
  public SoapHttpResponse execute(SoapHttpRequest request) {
    // always use the POST method
    ((AbstractHttpRequest) request).post();

    return super.execute(request);
  }

  @Override
  protected <T extends HttpRequestBase> void applyPayload(T httpRequest, SoapHttpRequest request) {
    // SOAP requires soap envelop body
    if (request.getPayload() == null || request.getPayload().trim().isEmpty()) {
      throw LOG.noPayloadSet();
    }
    super.applyPayload(httpRequest, request);
  }

}
