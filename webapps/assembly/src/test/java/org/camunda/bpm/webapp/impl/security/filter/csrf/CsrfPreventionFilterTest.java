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
package org.camunda.bpm.webapp.impl.security.filter.csrf;

import org.camunda.bpm.webapp.impl.security.filter.CsrfPreventionFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.webapp.impl.security.filter.util.CsrfConstants.CSRF_PATH_FIELD_NAME;
import static org.camunda.bpm.webapp.impl.security.filter.util.CookieConstants.SET_COOKIE_HEADER_NAME;

/**
 * @author Nikola Koevski
 */
@RunWith(Parameterized.class)
public class CsrfPreventionFilterTest {

  protected static final String SERVICE_PATH = "/camunda";
  protected static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
  protected static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
  protected static final String CSRF_HEADER_REQUIRED = "Required";

  protected Filter csrfPreventionFilter;

  protected String nonModifyingRequestUrl;
  protected String modifyingRequestUrl;

  // flags a modifying request (POST/PUT/DELETE) as a non-modifying one
  protected boolean isModifyingFetchRequest;

  @Parameterized.Parameters
  public static Collection<Object[]> getRequestUrls() {
    return Arrays.asList(new Object[][]{
      {"/app/cockpit/default/", "/api/admin/auth/user/default/login/cockpit", true},
      {"/app/cockpit/engine1/", "/api/admin/auth/user/engine1/login/cockpit", true},

      {"/app/cockpit/default/", "/api/engine/engine/default/history/task/count", false},
      {"/app/cockpit/engine1/", "/api/engine/engine/engine1/history/task/count", false},

      {"/app/tasklist/default/", "/api/admin/auth/user/default/login/tasklist", true},
      {"/app/tasklist/engine1/", "/api/admin/auth/user/engine1/login/tasklist", true},

      {"/app/tasklist/default/", "api/engine/engine/default/task/task-id/submit-form", false},
      {"/app/tasklist/engine2/", "api/engine/engine/engine2/task/task-id/submit-form", false},

      {"/app/admin/default/", "/api/admin/auth/user/default/login/admin", true},
      {"/app/admin/engine1/", "/api/admin/auth/user/engine1/login/admin", true},

      {"/app/admin/default/", "api/admin/setup/default/user/create", false},
      {"/app/admin/engine3/", "api/admin/setup/engine3/user/create", false},

      {"/app/welcome/default/", "/api/admin/auth/user/default/login/welcome", true},
      {"/app/welcome/engine1/", "/api/admin/auth/user/engine1/login/welcome", true}
    });
  }

  public CsrfPreventionFilterTest(String nonModifyingRequestUrl, String modifyingRequestUrl, boolean isModifyingFetchRequest) {
    this.nonModifyingRequestUrl = nonModifyingRequestUrl;
    this.modifyingRequestUrl = modifyingRequestUrl;
    this.isModifyingFetchRequest = isModifyingFetchRequest;
  }

  @Before
  public void setup() throws Exception {
    setupFilter();
  }

  protected void setupFilter() throws ServletException {
    MockFilterConfig config = new MockFilterConfig();
    csrfPreventionFilter = new CsrfPreventionFilter();
    csrfPreventionFilter.init(config);
  }

  protected void applyFilter(MockHttpServletRequest request, MockHttpServletResponse response) throws IOException, ServletException {
    FilterChain filterChain = new MockFilterChain();
    csrfPreventionFilter.doFilter(request, response, filterChain);
  }

  @Test
  public void testNonModifyingRequestTokenGeneration() throws IOException, ServletException {
    MockHttpServletResponse response = performNonModifyingRequest(nonModifyingRequestUrl, new MockHttpSession());

    String cookieToken = (String) response.getHeader(SET_COOKIE_HEADER_NAME);
    String headerToken = (String) response.getHeader(CSRF_HEADER_NAME);

    Assert.assertNotNull(cookieToken);
    Assert.assertNotNull(headerToken);

    String regex = CSRF_COOKIE_NAME + "=[A-Z0-9]{32}" + CSRF_PATH_FIELD_NAME + getCookiePath(SERVICE_PATH) + ";SameSite=Lax";
    assertThat(cookieToken).matches(regex.replace(";", ";\\s*"));

    Assert.assertEquals("No HTTP Header Token!",false, headerToken.isEmpty());
    assertThat(cookieToken).contains(headerToken);
  }

  @Test
  public void testNonModifyingRequestTokenGenerationWithRootContextPath() throws IOException, ServletException {
    // given
    MockHttpSession session = new MockHttpSession();
    MockHttpServletRequest nonModifyingRequest = getMockedRequest();
    nonModifyingRequest.setMethod("GET");
    nonModifyingRequest.setSession(session);

    // set root context path in request
    nonModifyingRequest.setRequestURI("/"  + nonModifyingRequestUrl);
    nonModifyingRequest.setContextPath("");

    // when
    MockHttpServletResponse response = new MockHttpServletResponse();
    applyFilter(nonModifyingRequest, response);

    // then
    String cookieToken = (String) response.getHeader(SET_COOKIE_HEADER_NAME);
    String headerToken = (String) response.getHeader(CSRF_HEADER_NAME);

    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    Assert.assertNotNull(cookieToken);
    Assert.assertNotNull(headerToken);

    String regex = CSRF_COOKIE_NAME + "=[A-Z0-9]{32}" + CSRF_PATH_FIELD_NAME + getCookiePath("") + ";SameSite=Lax";
    assertThat(cookieToken).matches(regex.replace(";", ";\\s*"));

    Assert.assertEquals("No HTTP Header Token!",false, headerToken.isEmpty());
    assertThat(cookieToken).contains(headerToken);
  }

  @Test
  public void testConsecutiveNonModifyingRequestTokens() throws IOException, ServletException {
    MockHttpSession session = new MockHttpSession();

    // first non-modifying request
    MockHttpServletResponse firstResponse = performNonModifyingRequest(nonModifyingRequestUrl, session);
    // second non-modifying request
    MockHttpServletResponse secondResponse = performNonModifyingRequest(nonModifyingRequestUrl, session);

    String headerToken1 = (String) firstResponse.getHeader(CSRF_HEADER_NAME);
    String headerToken2 = (String) secondResponse.getHeader(CSRF_HEADER_NAME);

    Assert.assertNotNull(headerToken1);
    Assert.assertNull(headerToken2);
  }

  @Test
  public void testModifyingRequestTokenValidation() throws IOException, ServletException {
    MockHttpSession session = new MockHttpSession();

    // first a non-modifying request to obtain a token
    MockHttpServletResponse nonModifyingResponse = performNonModifyingRequest(nonModifyingRequestUrl, session);

    if (!isModifyingFetchRequest) {
      String token = (String) nonModifyingResponse.getHeader(CSRF_HEADER_NAME);
      HttpServletResponse modifyingResponse = performModifyingRequest(token, session);
      Assert.assertEquals(Response.Status.OK.getStatusCode(), modifyingResponse.getStatus());
    }
  }

  @Test
  public void testModifyingRequestInvalidToken() throws IOException, ServletException {
    MockHttpSession session = new MockHttpSession();
    performNonModifyingRequest(nonModifyingRequestUrl, session);

    if (!isModifyingFetchRequest) {
      // invalid header token
      MockHttpServletResponse response = performModifyingRequest("invalid header token", session);
      Assert.assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
      Assert.assertEquals("CSRFPreventionFilter: Invalid HTTP Header Token.", response.getErrorMessage());

      // no token in header
      MockHttpServletResponse response2 = new MockHttpServletResponse();
      MockHttpServletRequest modifyingRequest = getMockedRequest();
      modifyingRequest.setMethod("POST");
      modifyingRequest.setSession(session);
      modifyingRequest.setRequestURI(SERVICE_PATH  + modifyingRequestUrl);
      modifyingRequest.setContextPath(SERVICE_PATH);

      applyFilter(modifyingRequest, response2);
      Assert.assertEquals(CSRF_HEADER_REQUIRED, response2.getHeader(CSRF_HEADER_NAME));
      Assert.assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response2.getStatus());
      Assert.assertEquals("CSRFPreventionFilter: Token provided via HTTP Header is absent/empty.", response2.getErrorMessage());
      Assert.assertNotEquals(modifyingRequest.getSession().getId(), session.getId());
    }
  }

  protected MockHttpServletResponse performNonModifyingRequest(String requestUrl, MockHttpSession session) throws IOException, ServletException {
    MockHttpServletResponse response = new MockHttpServletResponse();

    MockHttpServletRequest nonModifyingRequest = getMockedRequest();
    nonModifyingRequest.setMethod("GET");
    nonModifyingRequest.setSession(session);
    nonModifyingRequest.setRequestURI(SERVICE_PATH  + requestUrl);
    nonModifyingRequest.setContextPath(SERVICE_PATH);

    applyFilter(nonModifyingRequest, response);

    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    return response;
  }

  protected MockHttpServletResponse performModifyingRequest(String token, MockHttpSession session) throws IOException, ServletException {
    MockHttpServletResponse response = new MockHttpServletResponse();

    MockHttpServletRequest modifyingRequest = getMockedRequest();

    modifyingRequest.setMethod("POST");
    modifyingRequest.setSession(session);
    modifyingRequest.setRequestURI(SERVICE_PATH  + modifyingRequestUrl);
    modifyingRequest.setContextPath(SERVICE_PATH);

    modifyingRequest.addHeader(CSRF_HEADER_NAME, token);
    Cookie[] cookies = {new Cookie(CSRF_COOKIE_NAME, token)};
    modifyingRequest.setCookies(cookies);

    applyFilter(modifyingRequest, response);

    return response;
  }

  protected MockHttpServletRequest getMockedRequest() {
    return new MockHttpServletRequest();
  }

  protected String getCookiePath(String contextPath) {
    if (contextPath.isEmpty()) {
      return "/";

    } else {
      return contextPath;

    }
  }

}
