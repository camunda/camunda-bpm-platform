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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.camunda.connect.Connectors;
import org.junit.Before;
import org.junit.Test;

public class HttpRequestTest {

  public static final String EXAMPLE_URL = "http://camunda.org";
  public static final String EXAMPLE_CONTENT_TYPE = "text/plain";
  public static final String EXAMPLE_PAYLOAD = "test";

  protected HttpConnector connector;

  @Before
  public void createRequest() {
    connector = Connectors.getConnector(HttpConnector.ID);
  }

  @Test
  public void createAnEmptyRequest() {
    HttpRequest request = connector.createRequest();

    assertThat(request.getMethod()).isNull();
    assertThat(request.getUrl()).isNull();
    assertThat(request.getContentType()).isNull();
    assertThat(request.getHeaders()).isNull();
    assertThat(request.getPayload()).isNull();
    assertThat(request.getRequestParameters()).isEmpty();
  }

  @Test
  public void setHttpMethod() {
    HttpRequest request = connector.createRequest().get();
    assertThat(request.getMethod()).isEqualTo(HttpGet.METHOD_NAME);

    request = connector.createRequest().post();
    assertThat(request.getMethod()).isEqualTo(HttpPost.METHOD_NAME);

    request = connector.createRequest().put();
    assertThat(request.getMethod()).isEqualTo(HttpPut.METHOD_NAME);

    request = connector.createRequest().delete();
    assertThat(request.getMethod()).isEqualTo(HttpDelete.METHOD_NAME);

    request = connector.createRequest().patch();
    assertThat(request.getMethod()).isEqualTo(HttpPatch.METHOD_NAME);

    request = connector.createRequest().head();
    assertThat(request.getMethod()).isEqualTo(HttpHead.METHOD_NAME);

    request = connector.createRequest().options();
    assertThat(request.getMethod()).isEqualTo(HttpOptions.METHOD_NAME);

    request = connector.createRequest().trace();
    assertThat(request.getMethod()).isEqualTo(HttpTrace.METHOD_NAME);
  }

  @Test
  public void setUrl() {
    HttpRequest request = connector.createRequest().url(EXAMPLE_URL);
    assertThat(request.getUrl()).isEqualTo(EXAMPLE_URL);
  }

  @Test
  public void setContentType() {
    HttpRequest request = connector.createRequest().contentType(EXAMPLE_CONTENT_TYPE);
    assertThat(request.getContentType()).isEqualTo(EXAMPLE_CONTENT_TYPE);
    assertThat(request.getHeaders())
      .hasSize(1)
      .containsEntry(HttpBaseRequest.HEADER_CONTENT_TYPE, EXAMPLE_CONTENT_TYPE);
  }

  @Test
  public void shouldIgnoreNullOrEmptyContentType() {
    HttpRequest request = connector.createRequest().contentType(null);
    assertThat(request.getContentType()).isNull();

    request.contentType("");
    assertThat(request.getContentType()).isNull();
  }

  @Test
  public void setHeaders() {
    HttpRequest request = connector.createRequest().header("hello", "world").header("foo", "bar");
    assertThat(request.getHeaders())
      .hasSize(2)
      .containsEntry("hello", "world")
      .containsEntry("foo", "bar");

    assertThat(request.getHeader("hello")).isEqualTo("world");
    assertThat(request.getHeader("unknown")).isNull();
  }

  @Test
  public void shouldIgnoreHeadersWithNullOrEmptyNameOrValue() {
    HttpRequest request = connector.createRequest().header(null, "test");
    assertThat(request.getHeaders()).isNull();

    request.header("", "test");
    assertThat(request.getHeaders()).isNull();

    request.header("test", null);
    assertThat(request.getHeaders()).isNull();

    request.header("test", "");
    assertThat(request.getHeaders()).isNull();
  }

  @Test
  public void setPayLoad() {
    HttpRequest request = connector.createRequest().payload(EXAMPLE_PAYLOAD);
    assertThat(request.getPayload()).isEqualTo(EXAMPLE_PAYLOAD);
  }

  @Test
  public void setRequestParameters() {
    HttpRequest request = connector.createRequest();

    request.setRequestParameter("hello", "world");

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("foo", "bar");
    params.put("number", 42);
    request.setRequestParameters(params);

    assertThat(request.getRequestParameters())
      .hasSize(3)
      .containsEntry("hello", "world")
      .containsEntry("foo", "bar")
      .containsEntry("number", 42);
  }

  @Test
  public void setConfigOption() {
    Object value = new Object();
    HttpRequest request = connector.createRequest()
        .configOption("object-field", value)
        .configOption("int-field", 15)
        .configOption("long-field", 15l)
        .configOption("boolean-field", true)
        .configOption("string-field", "string-value");

    assertThat(request.getConfigOption("object-field")).isEqualTo(value);
    assertThat(request.getConfigOption("int-field")).isEqualTo(15);
    assertThat(request.getConfigOption("long-field")).isEqualTo(15l);
    assertThat(request.getConfigOption("boolean-field")).isEqualTo(true);
    assertThat(request.getConfigOption("string-field")).isEqualTo("string-value");
  }

  @Test
  public void shouldIgnoreConfigWithNullOrEmptyNameOrNullValue() {
      HttpRequest request = connector.createRequest();

      request.configOption(null, "test");
      assertThat(request.getConfigOptions()).isNull();

      request.configOption("", "test");
      assertThat(request.getConfigOptions()).isNull();

      request.configOption("test", null);
      assertThat(request.getConfigOptions()).isNull();
  }

  @Test
  public void setRequestConfig() {
    HttpRequest request = connector.createRequest()
        .configOption("throw-http-error", "TRUE")
        .configOption("connection-timeout", "10000")
        .configOption("socket-timeout", "30000");

    assertThat(request.getConfigOptions()).hasSize(3)
        .containsEntry("throw-http-error", "TRUE")
        .containsEntry("connection-timeout", "10000")
        .containsEntry("socket-timeout", "30000");
    assertThat(request.getConfigOption("throw-http-error")).isEqualTo("TRUE");
    assertThat(request.getConfigOption("connection-timeout")).isEqualTo("10000");
    assertThat(request.getConfigOption("socket-timeout")).isEqualTo("30000");
    assertThat(request.getHeader("unknown")).isNull();
  }

  @Test
  public void shouldIgnoreRequestConfigWithNullOrEmptyNameOrValue() {
    HttpRequest request = connector.createRequest().configOption(null, "test");
    assertThat(request.getConfigOptions()).isNull();

    request.configOption("throw-http-error", null);
    assertThat(request.getConfigOption("throw-http-error")).isNull();

    request.configOption("throw-http-error", "");
    assertThat(request.getConfigOption("throw-http-error")).isEqualTo("");
  }

}
