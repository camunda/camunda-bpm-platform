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

import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.camunda.bpm.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.chrome.ChromeDriverService;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;

/**
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 *
 */
public abstract class AbstractWebIntegrationTest {

  private final static Logger LOGGER = Logger.getLogger(AbstractWebIntegrationTest.class.getName());
  
  protected static final String TASKLIST_PATH = "app/tasklist/default/";
  
  protected static final String COOKIE_HEADER = "Cookie";
  protected static final String X_XSRF_TOKEN_HEADER = "X-XSRF-TOKEN";

  protected static final String JSESSIONID_IDENTIFIER = "JSESSIONID=";
  protected static final String XSRF_TOKEN_IDENTIFIER = "XSRF-TOKEN=";
  
  protected static final String HOST_NAME = "localhost";

  protected String appBasePath;
  protected String appUrl;
  protected TestUtil testUtil;
  protected TestProperties testProperties;

  protected static ChromeDriverService service;

  protected ApacheHttpClient4 client;
  protected String httpPort;
  
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
  }

  public void createClient(String ctxPath) throws Exception {
    testProperties = new TestProperties();

    appBasePath = testProperties.getApplicationPath("/" + ctxPath);
    LOGGER.info("Connecting to application " + appBasePath);

    ClientConfig clientConfig = new DefaultApacheHttpClient4Config();
    clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    client = ApacheHttpClient4.create(clientConfig);
  }

  protected void getTokens() {
    // first request, first set of cookies
    ClientResponse clientResponse = client.resource(appBasePath + TASKLIST_PATH).get(ClientResponse.class);
    List<String> cookieValues = getCookieHeaders(clientResponse);
    clientResponse.close();

    String startCsrfCookie = getCookie(cookieValues, XSRF_TOKEN_IDENTIFIER);
    String startSessionCookie = getCookie(cookieValues, JSESSIONID_IDENTIFIER);
    
    // login with user, update session cookie
    clientResponse = client.resource(appBasePath + "api/admin/auth/user/default/login/cockpit")
        .entity("username=demo&password=demo", MediaType.APPLICATION_FORM_URLENCODED_TYPE)
        .header(COOKIE_HEADER, createCookieHeader(startCsrfCookie, startSessionCookie))
        .header(X_XSRF_TOKEN_HEADER, startCsrfCookie)
        .accept(MediaType.APPLICATION_JSON)
        .post(ClientResponse.class);
    cookieValues = clientResponse.getHeaders().get("Set-Cookie");
    clientResponse.close();
    
    sessionId = getCookie(cookieValues, JSESSIONID_IDENTIFIER);
    
    // update CSRF cookie
    clientResponse = client.resource(appBasePath + "api/engine/engine")
        .header(COOKIE_HEADER, createCookieHeader(startCsrfCookie, sessionId))
        .header(X_XSRF_TOKEN_HEADER, startCsrfCookie)
        .get(ClientResponse.class);
    
    cookieValues = getCookieHeaders(clientResponse);
    clientResponse.close();
    
    csrfToken = getCookie(cookieValues, XSRF_TOKEN_IDENTIFIER);
  }

  protected List<String> getCookieHeaders(ClientResponse response) {
    return response.getHeaders().get("Set-Cookie");
  }
  
  protected String getCookie(List<String> cookieValues, String cookieName) {
    String cookieValue = getCookieValue(cookieValues, cookieName);
    int valueEnd = cookieValue.contains(";") ? cookieValue.indexOf(';') : cookieValue.length() - 1;
    return cookieValue.substring(cookieName.length(), valueEnd);
  }
  
  protected String createCookieHeader() {
    return createCookieHeader(csrfToken, sessionId);
  }
  
  protected String createCookieHeader(String csrf, String session) {
    return XSRF_TOKEN_IDENTIFIER + csrf + "; " + JSESSIONID_IDENTIFIER + session;
  }

  protected String getXsrfTokenHeader(ClientResponse response) {
    return response.getHeaders().getFirst(X_XSRF_TOKEN_HEADER);
  }

  protected String getXsrfCookieValue(ClientResponse response) {
    return getCookieValue(response, XSRF_TOKEN_IDENTIFIER);
  }
  
  protected String getCookieValue(ClientResponse response, String cookieName) {
    return getCookieValue(getCookieHeaders(response), cookieName);
  }

  protected String getCookieValue(List<String> cookies, String cookieName) {
    for (String cookie : cookies) {
      if (cookie.startsWith(cookieName)) {
        return cookie;
      }
    }

    return "";
  }

  protected void preventRaceConditions() throws InterruptedException {
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
