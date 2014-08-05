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

import org.apache.http.client.methods.*;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.camunda.bpm.connect.impl.AbstractConnector;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author Stefan Hentschel.
 */
public class RestHttpConnector extends AbstractConnector<RestHttpRequest, RestHttpResponse> {

  private final RestHttpConnectorLogger LOG = RestHttpLogger.REST_CONNECTOR_LOGGER;
  public static final String ID = "rest-http-connector";

  protected CloseableHttpClient httpClient;

  public RestHttpConnector() {
    httpClient = createClient();
  }

  protected CloseableHttpClient createClient() {
    return HttpClients.createDefault();
  }

  public String getId() {
    return ID;
  }

  /**
   * create a new  REST request.
   *
   * @return {@link RestHttpRequest} the new request
   */
  public RestHttpRequest createRequest() {
    return new RestHttpRequest(this);
  }

  /**
   * Executes the request.
   *
   * @param request the request
   * @return {@link RestHttpResponse} The response for the given request.
   */
  public RestHttpResponse execute(RestHttpRequest request) {
    HttpRequestBase http = createTarget(request);

    return invoke(request, http);
  }

  /**
   * Executes the request.
   *
   * @param request the given request
   * @param http a apache Http* representation of the request
   * @return {@link RestHttpResponse} The response for the given request.
   */
  public RestHttpResponse execute(RestHttpRequest request, HttpRequestBase http) {
    return invoke(request, http);
  }

  protected RestHttpResponse invoke(RestHttpRequest request, HttpRequestBase http) {
    try {
      HttpRequestInvocation invocation = new HttpRequestInvocation(http, request, requestInterceptors, httpClient);

      // route request through interceptor chain
      return new RestHttpResponse((CloseableHttpResponse) invocation.proceed());

    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * creates a apache Http* representation of the request.
   *
   * @param httpRequest the given request
   * @return {@link HttpRequestBase} an apache representation of the request
   */
  public HttpRequestBase createTarget(RestHttpRequest httpRequest) {
    String requestType = httpRequest.getRequestType();
    HttpRequestBase http;

    if(requestType.equals("GET")) {
      http = createHttpGet(httpRequest);

    } else if(requestType.equals("POST")) {
      http = createHttpPost(httpRequest);

    } else if(requestType.equals("PUT")) {
      http = createHttpPut(httpRequest);

    } else if(requestType.equals("DELETE")) {
      http = createHttpDelete(httpRequest);

    } else if(requestType.equals("PATCH")) {
      http = createHttpPatch(httpRequest);

    } else {
      throw LOG.invalidRequestParameter(RestHttpRequest.PARAM_NAME_REQUEST_TYPE, "param must be set!");
    }

    return http;
  }

  /**
   * creates a HttpPost representation of the request
   *
   * @param httpRequest the given request.
   * @return {@link HttpPost} the HttpPost representation of the request.
   */
  public HttpPost createHttpPost(RestHttpRequest httpRequest) {
    // handle endpoint
    String requestUrl = handleRequestUrl(httpRequest.getRequestUrl());
    HttpPost httpPost = new HttpPost(requestUrl);

    // handle headers
    handleHeaders(httpPost, httpRequest.getRequestHeaders());

    // handle payload
    handlePayload(httpPost, httpRequest.getRequestPayload());

    return httpPost;
  }

  /**
   * creates a HttpGet representation of the request
   *
   * @param httpRequest the given request.
   * @return {@link HttpGet} the HttpGet representation of the request.
   */
  public HttpGet createHttpGet(RestHttpRequest httpRequest) {
    // handle endpoint
    String requestUrl = handleRequestUrl(httpRequest.getRequestUrl());
    HttpGet httpGet = new HttpGet(requestUrl);

    // handle headers
    handleHeaders(httpGet, httpRequest.getRequestHeaders());

    //handle payload
    if(httpRequest.getRequestPayload() != null) {
      LOG.removedPayload(httpGet.getMethod());
    }

    return httpGet;
  }

  /**
   * creates a HttpDelete representation of the request
   *
   * @param httpRequest the given request.
   * @return {@link HttpDelete} the HttpDelete representation of the request.
   */
  public HttpDelete createHttpDelete(RestHttpRequest httpRequest) {
    // handle endpoint
    String requestUrl = handleRequestUrl(httpRequest.getRequestUrl());
    HttpDelete httpDelete = new HttpDelete(requestUrl);

    // handle headers
    handleHeaders(httpDelete, httpRequest.getRequestHeaders());

    //handle payload
    if(httpRequest.getRequestPayload() != null) {
      LOG.removedPayload(httpDelete.getMethod());
    }

    return httpDelete;
  }

  /**
   * creates a HttpPut representation of the request
   *
   * @param httpRequest the given request.
   * @return {@link HttpPut} the HttpPut representation of the request.
   */
  public HttpPut createHttpPut(RestHttpRequest httpRequest) {
    // handle endpoint
    String requestUrl = handleRequestUrl(httpRequest.getRequestUrl());
    HttpPut httpPut = new HttpPut(requestUrl);

    // handle headers
    handleHeaders(httpPut, httpRequest.getRequestHeaders());

    // handle payload
    handlePayload(httpPut, httpRequest.getRequestPayload());

    return httpPut;
  }

  /**
   * creates a HttpPatch representation of the request
   *
   * @param httpRequest the given request.
   * @return {@link HttpPatch} the HttpPatch representation of the request.
   */
  public HttpPatch createHttpPatch(RestHttpRequest httpRequest) {
    // handle endpoint
    String requestUrl = handleRequestUrl(httpRequest.getRequestUrl());
    HttpPatch httpPatch = new HttpPatch(requestUrl);

    // handle headers
    handleHeaders(httpPatch, httpRequest.getRequestHeaders());

    // handle payload
    handlePayload(httpPatch, httpRequest.getRequestPayload());

    return httpPatch;
  }

  /**
   * Check if the requestUrl is not empty or null
   *
   * @param requestUrl target to which the request should be send
   * @return target to which the request should be send
   */
  protected String handleRequestUrl(String requestUrl) {
    if(requestUrl == null || requestUrl.isEmpty()) {
      throw LOG.invalidRequestParameter(RestHttpRequest.PARAM_NAME_REQUEST_URL, "param must be set");
    }

    return requestUrl;
  }

  /**
   * Check if the headers are not null.
   *
   * @param headers current headers
   * @param httpRequestBase apache request representation
   */
  protected void handleHeaders(HttpRequestBase httpRequestBase, Map<String, String> headers) {
    if(headers != null) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        httpRequestBase.setHeader(entry.getKey(), entry.getValue());
        LOG.setHeader(entry.getKey(), entry.getValue());
      }
    }
  }

  /**
   * Check if the payload are not empty or null.
   *
   * @param payload request data
   * @param httpEntityEnclosingRequestBase apache request representation
   */
  protected void handlePayload(HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase, String payload) {
    // handle payload
    if(payload == null || payload.isEmpty()) {
      throw LOG.invalidRequestParameter(RestHttpRequest.PARAM_NAME_REQUEST_PAYLOAD, "param must be set");
    }
    ByteArrayInputStream envelopeStream = new ByteArrayInputStream(payload.getBytes(Charset.forName("utf-8")));
    httpEntityEnclosingRequestBase.setEntity(new InputStreamEntity(envelopeStream));
  }
}
