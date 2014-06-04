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

import java.util.Map.Entry;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.camunda.bpm.connect.impl.AbstractConnector;

/**
 * @author Daniel Meyer
 *
 */
public class SoapHttpConnector extends AbstractConnector<SoapHttpRequest, SoapHttpResponse> {

  public static final String ID = "soap-http-connector";

  protected CloseableHttpClient httpClient;

  public SoapHttpConnector() {
    httpClient = createClient();
  }

  public String getId() {
    return ID;
  }

  // lifecycle and configuration //////////////////////////////

  protected CloseableHttpClient createClient() {
    return HttpClients.createDefault();
  }

  // request handling ////////////////////////////////////////

  public SoapHttpRequest createRequest() {
    return new SoapHttpRequest(this);
  }

  public SoapHttpResponse execute(SoapHttpRequest soapHttpRequest) {

    // create the http post
    HttpPost httpPost = createHttpPost(soapHttpRequest);

    try {

      HttpRequestInvocation invocation = new HttpRequestInvocation(httpPost, soapHttpRequest, requestInterceptors, httpClient);

      // route request through interceptor chain
      return new SoapHttpResponse((CloseableHttpResponse) invocation.proceed());

    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Transforms the {@link SoapHttpRequest} into a {@link HttpPost} object which can be executed by the HttpClient.
   *
   * @param soapHttpRequest the request object to transform
   * @return the HttpPost object
   */
  protected HttpPost createHttpPost(SoapHttpRequest soapHttpRequest) {
    HttpPost httpPost = new HttpPost(soapHttpRequest.getEndpointUrl());

    for (Entry<String, String> entry : soapHttpRequest.getHeaders().entrySet()) {
      httpPost.setHeader(entry.getKey(), entry.getValue());
    }

    httpPost.setEntity(new InputStreamEntity(soapHttpRequest.getSoapEnvelope()));

    return httpPost;
  }

}
