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

import org.apache.http.client.methods.HttpPost;
import org.camunda.connect.httpclient.impl.AbstractHttpRequest;
import org.camunda.connect.httpclient.soap.SoapHttpConnector;
import org.camunda.connect.httpclient.soap.SoapHttpRequest;
import org.camunda.connect.httpclient.soap.SoapHttpResponse;

public class SoapHttpRequestImpl extends AbstractHttpRequest<SoapHttpRequest, SoapHttpResponse> implements SoapHttpRequest {

  protected static final SoapHttpConnectorLogger LOG = SoapHttpLogger.SOAP_CONNECTOR_LOGGER;

  public SoapHttpRequestImpl(SoapHttpConnector connector) {
    super(connector);
  }

  public SoapHttpRequest soapAction(String value) {
    return header(HEADER_SOAP_ACTION, value);
  }

  public String getSoapAction() {
    return getHeader(HEADER_SOAP_ACTION);
  }

  @Override
  public SoapHttpRequest method(String method) {
    // only allow post request
    if (!HttpPost.METHOD_NAME.equals(method)) {
      throw LOG.invalidRequestMethod(method);
    }
    super.method(method);
    return this;
  }

}
