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

import org.camunda.bpm.connect.ConnectorException;

/**
 * @author Stefan Hentschel.
 */
public class RestHttpConnectorLogger extends RestHttpLogger {

  public void setHeader(String field, String value) {
    logDebug("001", "Set header field '{}' to '{}'", field, value);
  }

  public ConnectorException unableToReadResponse(Exception cause) {
    return new ConnectorException(exceptionMessage("001", "Unable to read connectorResponse: {}", cause.getMessage()), cause);
  }

  public ConnectorException invalidRequestParameter(String paramName, String value) {
    return new ConnectorException(exceptionMessage("002", "Invalid value for request parameter '{}': '{}'", paramName, value));
  }

  public void removedPayload(String type) {
    logDebug("003", "Request is of type: '{}'. Payload will not be send with this request!", type.toUpperCase());
  }

  public void removeNullHeader() {
    logDebug("004", "Header cannot be null! Don't attach header to request.");
  }

  public ConnectorException unableToExecuteRequest(Exception cause) {
    return new ConnectorException(exceptionMessage("005", "Unable to execute request"), cause);
  }

}
