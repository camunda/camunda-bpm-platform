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

import org.apache.http.client.methods.HttpPost;
import org.camunda.bpm.connect.ConnectorException;
import org.camunda.commons.utils.IoUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Daniel Meyer
 *
 */
public class SoapHttpClientTest {

  private SoapHttpConnector connector;
  private SoapHttpRequest request;

  @Before
  public void createRequest() {
    connector = new SoapHttpConnector();
    request = connector.createRequest()
      .contentType("application/xml")
      .endpointUrl("http://camunda.org/soap")
      .soapAction("action")
      .soapEnvelope("test");
  }

  @Test
  public void shouldComposeHttpPost() throws IOException {
    HttpPost httpPost = connector.createHttpPost(request);

    assertThat(httpPost.getMethod()).isEqualTo("POST");
    assertThat(httpPost.getURI().toASCIIString()).isEqualTo("http://camunda.org/soap");
    assertThat(httpPost.getAllHeaders()).hasSize(2);
    assertThat(httpPost.getFirstHeader("Content-Type").getValue()).isEqualTo("application/xml");
    assertThat(httpPost.getFirstHeader("SOAPAction").getValue()).isEqualTo("action");
    String content = IoUtil.inputStreamAsString(httpPost.getEntity().getContent());
    assertThat(content).isEqualTo("test");
  }

  @Test(expected = ConnectorException.class)
  public void shouldFailIfNoEnvelopeIsSet() {
    request.soapEnvelope(null);
    connector.createHttpPost(request);
  }

  @Test(expected = ConnectorException.class)
  public void shouldFailIfNoEndpointUrlIsSet() {
    request.endpointUrl(null);
    connector.createHttpPost(request);
  }

  public void shouldNotFailIfNoHeadersAreSet() {
    request.setRequestParameter(SoapHttpRequest.PARAM_NAME_HEADERS, null);
    connector.createHttpPost(request);
  }

  @Test
  public void shouldSetAdditionalHeaders() {
    request.header("Accept", "application/xml");

    HttpPost httpPost = connector.createHttpPost(request);

    assertThat(httpPost.getFirstHeader("Accept").getValue()).isEqualTo("application/xml");
  }

  @Test
  public void shouldSetAdditionalRequestParameters() {
    request.setRequestParameter("hello", "world");

    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("foo", "bar");
    params.put("test", "test");
    request.setRequestParameters(params);

    assertThat(request.getRequestParameter("hello")).isEqualTo("world");
    assertThat(request.getRequestParameter("foo")).isEqualTo("bar");
    assertThat(request.getRequestParameter("test")).isEqualTo("test");

  }

}
