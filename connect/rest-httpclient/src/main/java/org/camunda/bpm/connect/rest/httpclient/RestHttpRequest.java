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
package org.camunda.bpm.connect.rest.httpclient;

import org.camunda.bpm.connect.impl.AbstractConnectorRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Hentschel.
 */
public class RestHttpRequest extends AbstractConnectorRequest<RestHttpResponse> {

  private final RestHttpConnectorLogger LOG = RestHttpLogger.REST_CONNECTOR_LOGGER;

  public static final String PARAM_NAME_REQUEST_URL = "requestUrl";
  public static final String PARAM_NAME_REQUEST_HEADERS = "requestHeaders";
  public static final String PARAM_NAME_REQUEST_TYPE = "requestType";
  public static final String PARAM_NAME_REQUEST_PAYLOAD = "requestPayload";

  public RestHttpRequest(RestHttpConnector connector) {
    super(connector);
  }

  /**
   * Set the url to which the request should be send.
   *
   * @param requestUrl url to which the request should be send
   * @return {@link RestHttpRequest} current representation of the request
   */
  public RestHttpRequest requestUrl(String requestUrl) {
    setRequestParameter(PARAM_NAME_REQUEST_URL, requestUrl);
    return this;
  }

  /**
   * Set a request type to the current request
   * @param requestType type of the request
   * @return {@link RestHttpRequest} current representation of the request
   */
  public RestHttpRequest requestType(String requestType) {
    setRequestParameter(PARAM_NAME_REQUEST_TYPE, requestType.toUpperCase());
    return this;
  }

  /**
   * set a specific header.
   *
   * @param name name of the header
   * @param value value of the header
   * @return {@link RestHttpRequest} current representation of the request
   */
  public RestHttpRequest header(String name, String value) {

    // A header with null name or null value would be senseless
    if(name == null || value == null ) {
      LOG.removeNullHeader();
      return this;
    }

    Map<String, String> headers = getRequestParameter(PARAM_NAME_REQUEST_HEADERS);

    if(headers == null) {
      headers = new HashMap<String, String>();
    }
    headers.put(name, value);
    setRequestParameter(PARAM_NAME_REQUEST_HEADERS, headers);
    return this;
  }

  /**
   * Set the request data for this request.
   *
   * @param payload request data
   * @return {@link RestHttpRequest} current representation of the request
   */
  public RestHttpRequest requestPayload(String payload) {
    setRequestParameter(PARAM_NAME_REQUEST_PAYLOAD, payload);
    return this;
  }

  /**
   * Set the content type of the current request
   *
   * @return RestHttpRequest the representation of the current request
   */
  public RestHttpRequest contentType(String value) {
    return header("content-type", value);
  }

  /**
   * Get the endpoint of the current request
   *
   * @return String request url
   */
  public String getRequestUrl() {
    return getRequestParameter(PARAM_NAME_REQUEST_URL);
  }

  /**
   * get all headers of the current request
   *
   * @return map of headers
   */
  public Map<String, String> getRequestHeaders() {
    return getRequestParameter(PARAM_NAME_REQUEST_HEADERS);
  }

  /**
   * get the payload of the current request.
   *
   * @return String payload
   */
  public String getRequestPayload() {
    return getRequestParameter(PARAM_NAME_REQUEST_PAYLOAD);
  }

  /**
   * get the request type of the current request.
   *
   * @return String request type
   */
  public String getRequestType() {
    return getRequestParameter(PARAM_NAME_REQUEST_TYPE);
  }

  /**
   * Set POST as request type
   *
   * @return RestHttpRequest the representation of the current request
   */
  public RestHttpRequest post() {
    return requestType("POST");
  }

  /**
   * Set GET as request type
   *
   * @return RestHttpRequest the representation of the current request
   */
  public RestHttpRequest get() {
    return requestType("GET");
  }

  /**
   * Set PUT as request type
   *
   * @return RestHttpRequest the representation of the current request
   */
  public RestHttpRequest put() {
    return requestType("PUT");
  }

  /**
   * Set PATCH as request type
   *
   * @return RestHttpRequest the representation of the current request
   */
  public RestHttpRequest patch() {
    return requestType("PATCH");
  }

  /**
   * Set DELETE as request type
   *
   * @return RestHttpRequest the representation of the current request
   */
  public RestHttpRequest delete() {
    return requestType("DELETE");
  }
}