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

import org.apache.http.client.methods.*;
import org.camunda.bpm.connect.ConnectorException;
import org.camunda.bpm.connect.rest.httpclient.RestHttpConnector;
import org.camunda.bpm.connect.rest.httpclient.RestHttpRequest;
import org.camunda.commons.utils.IoUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Stefan Hentschel.
 */
public class RestHttpClientTest {

  private RestHttpConnector connector;
  private RestHttpRequest request;

  @Before
  public void createRequest() {
    connector = new RestHttpConnector();
    request = connector.createRequest()
      .requestUrl("http://camunda.org/rest")
      .contentType("application/json")
      .header("test", "test2")
      .requestPayload("test");
  }

  @Test
  public void shouldComposeHttpPost() throws IOException {
    request.post();

    HttpPost httpPost = connector.createTarget(request);

    assertThat(httpPost.getMethod()).isEqualTo("POST");
    assertThat(httpPost.getURI().toASCIIString()).isEqualTo("http://camunda.org/rest");
    assertThat(httpPost.getAllHeaders()).hasSize(2);
    assertThat(httpPost.getFirstHeader("Content-Type").getValue()).isEqualTo("application/json");
    String content = IoUtil.inputStreamAsString(httpPost.getEntity().getContent());
    assertThat(content).isEqualTo("test");
  }

  @Test
  public void shouldComposeHttpGet() throws IOException {
    request.get();

    HttpGet httpGet = connector.createTarget(request);

    assertThat(httpGet.getMethod()).isEqualTo("GET");
    assertThat(httpGet.getURI().toASCIIString()).isEqualTo("http://camunda.org/rest");
    assertThat(httpGet.getAllHeaders()).hasSize(2);
    assertThat(httpGet.getFirstHeader("Content-Type").getValue()).isEqualTo("application/json");
  }

  @Test
  public void shouldComposeHttpPut() throws IOException {
    request.put();

    HttpPut httpPut = connector.createTarget(request);

    assertThat(httpPut.getMethod()).isEqualTo("PUT");
    assertThat(httpPut.getURI().toASCIIString()).isEqualTo("http://camunda.org/rest");
    assertThat(httpPut.getAllHeaders()).hasSize(2);
    assertThat(httpPut.getFirstHeader("Content-Type").getValue()).isEqualTo("application/json");
  }

  @Test
  public void shouldComposeHttpPatch() throws IOException {
    request.patch();

    HttpPatch httpPatch = connector.createTarget(request);

    assertThat(httpPatch.getMethod()).isEqualTo("PATCH");
    assertThat(httpPatch.getURI().toASCIIString()).isEqualTo("http://camunda.org/rest");
    assertThat(httpPatch.getAllHeaders()).hasSize(2);
    assertThat(httpPatch.getFirstHeader("Content-Type").getValue()).isEqualTo("application/json");
  }

  @Test
  public void shouldComposeHttpDelete() throws IOException {
    request.delete();

    HttpDelete httpDelete = connector.createTarget(request);

    assertThat(httpDelete.getMethod()).isEqualTo("DELETE");
    assertThat(httpDelete.getURI().toASCIIString()).isEqualTo("http://camunda.org/rest");
    assertThat(httpDelete.getAllHeaders()).hasSize(2);
    assertThat(httpDelete.getFirstHeader("Content-Type").getValue()).isEqualTo("application/json");
  }

  @Test
  public void shouldNotFailIfNoPayLoadIsSet() {
    request.requestPayload(null);

    connector.createHttpGet(request);
    connector.createHttpDelete(request);
  }

  @Test
  public void shouldNotFailIfNoHeadersAreSet() {
    request.setRequestParameter(RestHttpRequest.PARAM_NAME_REQUEST_HEADERS, null);
    request.post();
    connector.createTarget(request);
  }

  @Test
  public void shouldSetAdditionalHeaders() {
    request.header("Accept", "application/xml");

    HttpPost httpPost = connector.createHttpPost(request);

    assertThat(httpPost.getFirstHeader("Accept").getValue()).isEqualTo("application/xml");
  }

  @Test
  public void shouldNotSetAdditionalHeadersWithNull() {
    request.header("Accept", null);

    HttpPost httpPost = connector.createHttpPost(request);

    assertThat(httpPost.getFirstHeader("Accept")).isEqualTo(null);
  }

  @Test
  public void shouldSetAdditionalRequestParameters() {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("foo", "bar");
    params.put("test", "test");

    request
      .get()
      .setRequestParameter("hello", "world");
    request.setRequestParameters(params);

    assertThat(request.getRequestParameter("hello")).isEqualTo("world");
    assertThat(request.getRequestParameter("foo")).isEqualTo("bar");
    assertThat(request.getRequestParameter("test")).isEqualTo("test");
  }

  @Test(expected = ConnectorException.class)
  public void shouldFailIfNoPostPayLoadIsSet() {

    request.requestPayload(null);
    connector.createHttpPost(request);
  }

  @Test(expected = ConnectorException.class)
  public void shouldFailIfNoPutPostPayLoadIsSet() {

    request.requestPayload(null);
    connector.createHttpPut(request);
  }

  @Test(expected = ConnectorException.class)
  public void shouldFailIfNoPatchPayLoadIsSet() {

    request.requestPayload(null);
    connector.createHttpPatch(request);
  }

  @Test(expected = ConnectorException.class)
  public void shouldFailIfNoGetRequestUrlIsSet() {
    request.requestUrl(null);
    connector.createHttpGet(request);
  }

  @Test(expected = ConnectorException.class)
  public void shouldFailIfNoPostRequestUrlIsSet() {
    request.requestUrl(null);
    connector.createHttpPost(request);
  }

  @Test(expected = ConnectorException.class)
  public void shouldFailIfNoPutRequestUrlIsSet() {
    request.requestUrl(null);
    connector.createHttpPut(request);
  }

  @Test(expected = ConnectorException.class)
  public void shouldFailIfNoPatchRequestUrlIsSet() {
    request.requestUrl(null);
    connector.createHttpPatch(request);
  }

  @Test(expected = ConnectorException.class)
  public void shouldFailIfNoDeleteRequestUrlIsSet() {
    request.requestUrl(null);
    connector.createHttpDelete(request);
  }
}
