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

import org.camunda.connect.ConnectorRequestException;
import org.camunda.connect.httpclient.impl.HttpConnectorImpl;
import org.camunda.connect.impl.DebugRequestInterceptor;
import org.junit.Before;
import org.junit.Test;

public class HttpResponseTest {

  protected HttpConnector connector;
  protected TestResponse testResponse;

  @Before
  public void getConnector() {
    testResponse = new TestResponse();
    connector = new HttpConnectorImpl();
    connector.addRequestInterceptor(new DebugRequestInterceptor(testResponse));
  }

  @Test
  public void testResponseCode() {
    testResponse.statusCode(123);
    HttpResponse response = getResponse();
    assertThat(response.getStatusCode()).isEqualTo(123);
  }

  @Test
  public void testResponseBody() {
    testResponse.payload("test");
    HttpResponse response = getResponse();
    assertThat(response.getResponse()).isEqualTo("test");
  }

  @Test
  public void testEmptyResponseBody() {
    testResponse.payload("");
    HttpResponse response = getResponse();
    assertThat(response.getResponse()).isEmpty();
  }

  @Test
  public void testNullResponseBody() {
    HttpResponse response = getResponse();
    assertThat(response.getResponse()).isNull();
  }

  @Test
  public void testEmptyHeaders() {
    HttpResponse response = getResponse();
    assertThat(response.getHeaders()).isEmpty();
  }

  @Test
  public void testHeaders() {
    testResponse
      .header("foo", "bar")
      .header("hello", "world");
    HttpResponse response = getResponse();
    assertThat(response.getHeaders())
      .hasSize(2)
      .containsEntry("foo", "bar")
      .containsEntry("hello", "world")
      .doesNotContainKey("unknown");

    assertThat(response.getHeader("foo")).isEqualTo("bar");
    assertThat(response.getHeader("hello")).isEqualTo("world");
    assertThat(response.getHeader("unknown")).isNull();
  }

  protected HttpResponse getResponse() {
    return connector.createRequest().url("http://camunda.com").get().execute();
  }

  @Test
  public void testSuccessfulResponseCode() {
    testResponse.statusCode(200);
    HttpResponse response = getResponse();
    assertThat(response.getStatusCode()).isEqualTo(200);
  }
  
  @Test
  public void testResponseErrorCodeForMalformedRequest() {
    testResponse.statusCode(400);
    HttpResponse response = getResponse();
    assertThat(response.getStatusCode()).isEqualTo(400);
  }
  
  @Test
  public void testResponseErrorCodeForServerError() {
    testResponse.statusCode(500);
    HttpResponse response = getResponse();
    assertThat(response.getStatusCode()).isEqualTo(500);
  }
  
  @Test
  public void testServerErrorResponseWithConfigOptionSet() {
    try {
      // when
    	testResponse.statusCode(500);
    	connector.createRequest().configOption("throw-http-error", "TRUE")
    	    .url("http://camunda.com").get()
    	    .execute();
    } catch (ConnectorRequestException e) {
     // then
      assertThat(e).hasMessageContaining("Unable to execute HTTP request");
      assertThat(e).hasCauseExactlyInstanceOf(ConnectorRequestException.class);
    }
  }
  
  @Test
  public void testMalformedRequestWithConfigOptionSet() {
    try {
      // when
    	testResponse.statusCode(400);
    	connector.createRequest().configOption("throw-http-error", "TRUE")
    	    .url("http://camunda.com").get()
    	    .execute();
    } catch (ConnectorRequestException e) {
     // then
      assertThat(e).hasMessageContaining("Unable to execute HTTP request");
      assertThat(e).hasCauseExactlyInstanceOf(ConnectorRequestException.class);
    }
  }
  
  @Test
  public void testSuccessResponseWithConfigOptionSet() {
     // when
      testResponse.statusCode(200);
      connector.createRequest().configOption("throw-http-error", "TRUE")
    	    .url("http://camunda.com").get()
    	    .execute();
      HttpResponse response = getResponse();
     // then
      assertThat(response.getStatusCode()).isEqualTo(200);
  }


}
