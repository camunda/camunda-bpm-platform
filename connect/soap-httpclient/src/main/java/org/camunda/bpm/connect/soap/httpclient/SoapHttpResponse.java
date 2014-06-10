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

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.camunda.bpm.connect.impl.AbstractConnectorResponse;
import org.camunda.commons.utils.IoUtil;

/**
 * @author Daniel Meyer
 *
 */
public class SoapHttpResponse extends AbstractConnectorResponse {

  private final SoapHttpConnectorLogger LOG = SoapHttpLogger.SOAP_CONNECTOR_LOGGER;

  public static final String PARAM_NAME_STATUS_CODE = "statusCode";
  public static final String PARAM_NAME_RESPONSE = "response";

  protected CloseableHttpResponse httpResponse;

  public SoapHttpResponse(CloseableHttpResponse httpResponse) {
    this.httpResponse = httpResponse;
  }

  public int getStatusCode() {
    return getResponseParameter(PARAM_NAME_STATUS_CODE);
  }

  public String getResponse() {
    return getResponseParameter(PARAM_NAME_RESPONSE);
  }

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
