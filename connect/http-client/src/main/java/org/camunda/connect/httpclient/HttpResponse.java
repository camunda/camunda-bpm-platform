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

import org.camunda.connect.spi.CloseableConnectorResponse;

public interface HttpResponse extends CloseableConnectorResponse {

  static final String PARAM_NAME_STATUS_CODE = "statusCode";
  static final String PARAM_NAME_RESPONSE = "response";
  static final String PARAM_NAME_RESPONSE_HEADERS = "headers";

  /**
   * @return the HTTP status code of the response
   */
  Integer getStatusCode();

  /**
   * @return the response body or null if non exists
   */
  String getResponse();

  /**
   * @return the response headers
   */
  Map<String, String> getHeaders();

  /**
   * @return return the response header value for the given field or null if not set
   */
  String getHeader(String field);

}
