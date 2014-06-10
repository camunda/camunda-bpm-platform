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
package org.camunda.bpm.connect.soap.httpclient;

import org.camunda.bpm.connect.impl.AbstractConnectorRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a soap http request.
 *
 * @author Daniel Meyer
 *
 */
public class SoapHttpRequest extends AbstractConnectorRequest<SoapHttpResponse> {

  public static final String PARAM_NAME_ENDPOINT_URL = "endpointUrl";
  public static final String PARAM_NAME_SOAP_ENVELOPE = "soapEnvelope";
  public static final String PARAM_NAME_HEADERS = "headers";

  public SoapHttpRequest(SoapHttpConnector connector) {
    super(connector);
  }

  // fluent configuration ///////////////////////

  public SoapHttpRequest endpointUrl(String endpointUrl) {
    setRequestParameter(PARAM_NAME_ENDPOINT_URL, endpointUrl);
    return this;
  }

  public SoapHttpRequest soapEnvelope(String envelope) {
    setRequestParameter(PARAM_NAME_SOAP_ENVELOPE, envelope);
    return this;
  }

  public SoapHttpRequest header(String name, String value) {
    Map<String,String> headers = getRequestParameter(PARAM_NAME_HEADERS);
    // ensure headers are initialized
    if(headers == null) {
      headers = new HashMap<String, String>();
      setRequestParameter(PARAM_NAME_HEADERS, headers);
    }
    headers.put(name, value);
    return this;
  }

  public SoapHttpRequest soapAction(String value) {
    return header("SOAPAction", value);
  }

  public SoapHttpRequest contentType(String value) {
    return header("Content-Type", value);
  }

  // getters ///////////////////////////////////

  public Map<String, String> getHeaders() {
    return getRequestParameter(PARAM_NAME_HEADERS);
  }

  public String getEndpointUrl() {
    return getRequestParameter(PARAM_NAME_ENDPOINT_URL);
  }

  public String getSoapEnvelope() {
    return getRequestParameter(PARAM_NAME_SOAP_ENVELOPE);
  }
}
