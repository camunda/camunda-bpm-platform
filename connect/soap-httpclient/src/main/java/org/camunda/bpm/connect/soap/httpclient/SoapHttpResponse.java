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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.camunda.bpm.connect.impl.AbstractCloseableConnectorResponse;

/**
 * @author Daniel Meyer
 *
 */
public class SoapHttpResponse extends AbstractCloseableConnectorResponse {

  public static final String PARAM_NAME_STATUS_CODE = "statusCode";

  protected CloseableHttpResponse httpResponse;

  public SoapHttpResponse(CloseableHttpResponse httpResponse) {
    this.httpResponse = httpResponse;
  }

  public boolean isSuccessful() {
    // TODO: do other status codes also indicate success in SOAP?
    return getStatusCode() == 200;
  }

  public int getStatusCode() {
    return httpResponse.getStatusLine().getStatusCode();
  }

  public InputStream getInputStream() {
    try {
      return httpResponse.getEntity().getContent();
    } catch (IllegalStateException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void collectResponseParameters(Map<String, Object> responseParameters) {
    responseParameters.put(PARAM_NAME_STATUS_CODE, getStatusCode());
  }

  protected Closeable getClosable() {
    return httpResponse;
  }

}
