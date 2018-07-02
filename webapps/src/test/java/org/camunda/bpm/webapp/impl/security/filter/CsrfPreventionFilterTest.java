package org.camunda.bpm.webapp.impl.security.filter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.rule.PowerMockRule;
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

import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Nikola Koevski
 */
@RunWith(Parameterized.class)
@PowerMockIgnore("javax.security.*")
public class CsrfPreventionFilterTest {

  protected static final String SERVICE_PATH = "/camunda";
  protected static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
  protected static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
  protected static final String CSRF_HEADER_REQUIRED = "Required";

  @Rule
  public PowerMockRule rule = new PowerMockRule();

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
  public void setup() throws ServletException {
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

    Cookie cookieToken = response.getCookie(CSRF_COOKIE_NAME);
    String headerToken = (String) response.getHeader(CSRF_HEADER_NAME);

    Assert.assertNotNull(cookieToken);
    Assert.assertNotNull(headerToken);
    Assert.assertEquals("No Cookie Token!",false, cookieToken.getValue().isEmpty());
    Assert.assertEquals("No HTTP Header Token!",false, headerToken.isEmpty());
    Assert.assertEquals("Cookie and HTTP Header Tokens do not match!", cookieToken.getValue(), headerToken);
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
      MockHttpServletRequest modifyingRequest = new MockHttpServletRequest();
      modifyingRequest.setMethod("POST");
      modifyingRequest.setSession(session);
      modifyingRequest.setRequestURI(SERVICE_PATH  + modifyingRequestUrl);
      modifyingRequest.setContextPath(SERVICE_PATH);

      applyFilter(modifyingRequest, response2);
      Assert.assertEquals(CSRF_HEADER_REQUIRED, response2.getHeader(CSRF_HEADER_NAME));
      Assert.assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response2.getStatus());
      Assert.assertEquals("CSRFPreventionFilter: Token provided via HTTP Header is absent/empty.", response2.getErrorMessage());
    }
  }

  protected MockHttpServletResponse performNonModifyingRequest(String requestUrl, MockHttpSession session) throws IOException, ServletException {
    MockHttpServletResponse response = new MockHttpServletResponse();

    MockHttpServletRequest nonModifyingRequest = new MockHttpServletRequest();
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

    MockHttpServletRequest modifyingRequest = new MockHttpServletRequest();
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
}
