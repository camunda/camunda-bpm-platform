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
package org.camunda.bpm;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.camunda.bpm.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.chrome.ChromeDriverService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

/**
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 *
 */
public abstract class AbstractWebIntegrationTest {

  private final static Logger LOGGER = Logger.getLogger(AbstractWebIntegrationTest.class.getName());
  
  protected static final String COOKIE_HEADER = "Cookie";
  protected static final String X_XSRF_TOKEN_HEADER = "X-XSRF-TOKEN";
  
  protected static final String TASKLIST_PATH = "app/tasklist/default/";

  private static final String JSESSIONID_IDENTIFIER = "JSESSIONID=";
  private static final String XSRF_TOKEN_IDENTIFIER = "XSRF-TOKEN=";
  
  public static final String HOST_NAME = "localhost";
  public String APP_BASE_PATH;

  protected String appUrl;
  protected TestUtil testUtil;
  protected TestProperties testProperties;

  protected static ChromeDriverService service;

  public ApacheHttpClient4 client;
  public DefaultHttpClient defaultHttpClient;
  public String httpPort;
  protected HttpURLConnection connection;
  
  protected String csrfToken;
  protected String sessionId;

  @Before
  public void before() throws Exception {
    testProperties = new TestProperties(48080);
    testUtil = new TestUtil(testProperties);
  }

  @After
  public void destroyClient() {
    client.destroy();
    connection = null;
  }

  public void createClient(String ctxPath) throws Exception {
    testProperties = new TestProperties();

    APP_BASE_PATH = testProperties.getApplicationPath("/" + ctxPath);
    LOGGER.info("Connecting to application "+APP_BASE_PATH);

    ClientConfig clientConfig = new DefaultApacheHttpClient4Config();
    clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    client = ApacheHttpClient4.create(clientConfig);

    defaultHttpClient = (DefaultHttpClient) client.getClientHandler().getHttpClient();
    HttpParams params = defaultHttpClient.getParams();
    HttpConnectionParams.setConnectionTimeout(params, 3 * 60 * 1000);
    HttpConnectionParams.setSoTimeout(params, 10 * 60 * 1000);
  }

  public URLConnection performRequest(String url, String method, String headerName, String headerValue) {
    try {
      connection =
          (HttpURLConnection) new URL(url)
              .openConnection();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if ("POST".equals(method)) {
      try {
        connection.setRequestMethod("POST");
      } catch (ProtocolException e) {
        throw new RuntimeException(e);
      }
    }

    if (headerName != null && headerValue != null) {
      connection.setRequestProperty(headerName, headerValue);
    }

    try {
      connection.connect();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return connection;
  }
  
  protected void getTokens() {
    // first request, first set of cookies
    ClientResponse clientResponse = client.resource(APP_BASE_PATH + TASKLIST_PATH).get(ClientResponse.class);
    List<String> cookieValues = clientResponse.getHeaders().get("Set-Cookie");
    clientResponse.close();

    String startCsrfCookie = getCookie(cookieValues, XSRF_TOKEN_IDENTIFIER);
    String startSessionCookie = getCookie(cookieValues, JSESSIONID_IDENTIFIER);
    
    // login with user, update session cookie
    clientResponse = client.resource(APP_BASE_PATH + "api/admin/auth/user/default/login/cockpit")
        .entity("username=demo&password=demo", MediaType.APPLICATION_FORM_URLENCODED_TYPE)
        .header(COOKIE_HEADER, createCookieHeader(startCsrfCookie, startSessionCookie))
        .header(X_XSRF_TOKEN_HEADER, startCsrfCookie)
        .accept(MediaType.APPLICATION_JSON)
        .post(ClientResponse.class);
    cookieValues = clientResponse.getHeaders().get("Set-Cookie");
    clientResponse.close();
    
    sessionId = getCookie(cookieValues, JSESSIONID_IDENTIFIER);
    
    // update CSRF cookie
    clientResponse = client.resource(APP_BASE_PATH + "api/engine/engine")
        .header(COOKIE_HEADER, createCookieHeader(startCsrfCookie, sessionId))
        .header(X_XSRF_TOKEN_HEADER, startCsrfCookie)
        .get(ClientResponse.class);
    
    cookieValues = clientResponse.getHeaders().get("Set-Cookie");
    clientResponse.close();
    
    csrfToken = getCookie(cookieValues, XSRF_TOKEN_IDENTIFIER);
  }

  protected String getCookie(List<String> cookieValues, String cookieName) {
    String cookieValue = null;
    for (String cookie : cookieValues) {
      if (cookie.startsWith(cookieName)) {
        cookieValue = cookie;
        break;
      }
    }

    if (cookieValue == null) {
      throw new RuntimeException("Cookie \"" + cookieName + "\" does not exists!");
    }
    int valueEnd = cookieValue.contains(";") ? cookieValue.indexOf(';') : cookieValue.length() - 1;
    return cookieValue.substring(cookieName.length(), valueEnd);
  }
  
  protected String createCookieHeader() {
    return createCookieHeader(csrfToken, sessionId);
  }
  
  protected String createCookieHeader(String csrf, String session) {
    return XSRF_TOKEN_IDENTIFIER + csrf + "; " + JSESSIONID_IDENTIFIER + session;
  }

  public String getXsrfTokenHeader() {
    return connection.getHeaderField("X-XSRF-TOKEN");
  }

  public String getXsrfCookieValue() {
    return getCookieValue("XSRF-TOKEN");
  }

  public String getCookieValue(String cookieName) {
    List<String> cookies = getCookieHeaders();

    for (String cookie : cookies) {
      if (cookie.startsWith(cookieName + "=")) {
        return cookie;
      }
    }

    return "";
  }

  public List<String> getCookieHeaders() {
    return getHeaders("Set-Cookie");
  }

  public List<String> getHeaders(String name) {
    Map<String, List<String>> headerFields = connection.getHeaderFields();
    return headerFields.get(name);
  }

  public void preventRaceConditions() throws InterruptedException {
    // just wait some seconds before starting because of Wildfly / Cargo race conditions
    Thread.sleep(5 * 1000);
  }

  protected String getWebappCtxPath() {
    return testProperties.getStringProperty("http.ctx-path.webapp", null);
  }

  protected String getRestCtxPath() {
    return testProperties.getStringProperty("http.ctx-path.rest", null);
  }
}
