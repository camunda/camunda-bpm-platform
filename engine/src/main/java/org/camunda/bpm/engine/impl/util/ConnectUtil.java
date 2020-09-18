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
package org.camunda.bpm.engine.impl.util;

import java.util.HashMap;
import java.util.Map;

public class ConnectUtil {

  // request
  public static final String PARAM_NAME_REQUEST_URL = "url";
  public static final String PARAM_NAME_REQUEST_METHOD = "method";
  public static final String PARAM_NAME_REQUEST_PAYLOAD = "payload";
  public static final String PARAM_NAME_REQUEST_CONFIG = "request-config";

  // request methods
  public static final String METHOD_NAME_POST = "POST";

  // config options
  public static final String CONFIG_NAME_CONNECTION_TIMEOUT = "connection-timeout";
  public static final String CONFIG_NAME_SOCKET_TIMEOUT = "socket-timeout";

  // response
  public static final String PARAM_NAME_RESPONSE_STATUS_CODE = "statusCode";
  public static final String PARAM_NAME_RESPONSE = "response";

  // common between request and response
  public static final String PARAM_NAME_HEADERS = "headers";
  public static final String HEADER_CONTENT_TYPE = "Content-Type";

  // helper methods
  public static Map<String, Object> assembleRequestParameters(String methodName,
                                                             String url,
                                                             String contentType,
                                                             String payload) {
    Map<String, String> requestHeaders = new HashMap<>();
    requestHeaders.put(HEADER_CONTENT_TYPE, contentType);

    Map<String, Object> requestParams = new HashMap<>();
    requestParams.put(PARAM_NAME_REQUEST_METHOD, methodName);
    requestParams.put(PARAM_NAME_REQUEST_URL, url);
    requestParams.put(PARAM_NAME_HEADERS, requestHeaders);
    requestParams.put(PARAM_NAME_REQUEST_PAYLOAD, payload);

    return requestParams;
  }

  public static Map<String, Object> addRequestTimeoutConfiguration(Map<String, Object> requestParams,
                                                           int timeout) {
    Map<String, Object> config = new HashMap<>();
    config.put(CONFIG_NAME_CONNECTION_TIMEOUT, timeout);
    config.put(CONFIG_NAME_SOCKET_TIMEOUT, timeout);

    requestParams.put(PARAM_NAME_REQUEST_CONFIG, config);

    return requestParams;
  }
}
