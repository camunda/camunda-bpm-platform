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
package org.camunda.connect.httpclient;

import java.util.Map;

import org.camunda.connect.spi.ConnectorRequest;
import org.camunda.connect.spi.ConnectorResponse;

public interface HttpBaseRequest<Q extends HttpBaseRequest<?, ?>, R extends ConnectorResponse> extends ConnectorRequest<R> {

  String PARAM_NAME_REQUEST_URL  = "url";
  String PARAM_NAME_REQUEST_HEADERS = "headers";
  String PARAM_NAME_REQUEST_METHOD = "method";
  String PARAM_NAME_REQUEST_PAYLOAD = "payload";
  String HEADER_CONTENT_TYPE = "Content-Type";

  String PARAM_NAME_REQUEST_CONFIG = "request-config";

  /**
   * Set the url of this request.
   *
   * @param url the url to set
   * @return this request
   */
  Q url(String url);

  /**
   * @return the url of this request or null if none is set
   */
  String getUrl();

  /**
   * Set a HTTP header for this request.
   *
   * @param field HTTP header field
   * @param value HTTP header value
   * @return this request
   */
  Q header(String field, String value);

  /**
   * @return the HTTP header field value of this request or null if not set
   */
  String getHeader(String field);

  /**
   * @return the HTTP headers of this request or null if non set
   */
  Map<String, String> getHeaders();

  /**
   * Set the content type header for this request.
   *
   * @param contentType the content type
   * @return this request
   */
  Q contentType(String contentType);

  /**
   * @return the content-type of this request or null if non set
   */
  String getContentType();

  /**
   * Set the payload of this request.
   *
   * @param payload the payload to set
   * @return this request
   */
  Q payload(String payload);

  /**
   * @return the payload of this request or null if non set
   */
  String getPayload();

  /**
   * Sets the method of this request.
   *
   * @param method the method to set
   * @return this request
   */
  Q method(String method);

  /**
   * @return the method of this request or null if not set
   */
  String getMethod();

  /**
   * @return the HTTP configuration options of this request or null if non set
   */
  Map<String, Object> getConfigOptions();

  /**
   * @return the HTTP configuration option value of this request or null if non set
   */
  Object getConfigOption(String field);

  /**
   * Set a HTTP request configuration option for this request.
   *
   * @param field HTTP request configuration name
   * @param value HTTP request configuration value
   * @return this request
   */
  Q configOption(String field, Object value);
}
