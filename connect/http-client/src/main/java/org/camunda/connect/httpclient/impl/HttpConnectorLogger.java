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

import org.camunda.connect.ConnectorRequestException;
import org.camunda.connect.ConnectorResponseException;
import org.camunda.connect.impl.ConnectLogger;

public class HttpConnectorLogger extends ConnectLogger {

  public void setHeader(String field, String value) {
    logDebug("001", "Set header field '{}' to '{}'", field, value);
  }

  public void ignoreHeader(String field, String value) {
    logInfo("002", "Ignoring header with name '{}' and value '{}'", field, value);

  }

  public void payloadIgnoredForHttpMethod(String method) {
    logInfo("003", "Ignoring payload for HTTP '{}' method", method);
  }

  public ConnectorResponseException unableToReadResponse(Exception cause) {
    return new ConnectorResponseException(exceptionMessage("004", "Unable to read connectorResponse: {}", cause.getMessage()), cause);
  }

  public ConnectorRequestException requestUrlRequired() {
    return new ConnectorRequestException(exceptionMessage("005", "Request url required."));
  }

  public ConnectorRequestException unknownHttpMethod(String method) {
    return new ConnectorRequestException(exceptionMessage("006", "Unknown or unsupported HTTP method '{}'", method));
  }

  public ConnectorRequestException unableToExecuteRequest(Exception cause) {
    return new ConnectorRequestException(exceptionMessage("007", "Unable to execute HTTP request"), cause);
  }

}
