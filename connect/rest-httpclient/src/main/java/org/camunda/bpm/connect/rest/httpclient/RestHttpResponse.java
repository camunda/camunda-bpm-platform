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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.camunda.bpm.connect.impl.AbstractConnectorResponse;
import org.camunda.commons.utils.IoUtil;

import java.io.IOException;
import java.util.Map;

/**
 * @author Stefan Hentschel.
 */
public class RestHttpResponse extends AbstractConnectorResponse {

  private final RestHttpConnectorLogger LOG = RestHttpLogger.REST_CONNECTOR_LOGGER;

  public static final String PARAM_NAME_STATUS_CODE = "statusCode";
  public static final String PARAM_NAME_RESPONSE = "response";

  protected CloseableHttpResponse httpResponse;

  public RestHttpResponse(CloseableHttpResponse httpResponse) {
    this.httpResponse = httpResponse;
  }

  /**
   * Fetch the status code of the response
   *
   * @return the status code of the response
   */
  public Integer getStatusCode() {
    return getResponseParameter(PARAM_NAME_STATUS_CODE);
  }

  /**
   * Fetch the body of the response
   *
   * @return the response body
   */
  public String getResponse() {
    return getResponseParameter(PARAM_NAME_RESPONSE);
  }

  /**
   * @param responseParameters response parameters.
   */
  protected void collectResponseParameters(Map<String, Object> responseParameters) {
    try {
      responseParameters.put(PARAM_NAME_STATUS_CODE, httpResponse.getStatusLine().getStatusCode());
      responseParameters.put(PARAM_NAME_RESPONSE, IoUtil.inputStreamAsString(httpResponse.getEntity().getContent()));
    } catch (IllegalStateException e) {
      throw LOG.unableToReadResponse(e);
    } catch (IOException e) {
      throw LOG.unableToReadResponse(e);
    } finally {
      IoUtil.closeSilently(httpResponse);
    }
  }
}
