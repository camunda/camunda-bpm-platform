package org.camunda.bpm.webapp.impl.security.filter;

import org.camunda.bpm.webapp.impl.security.filter.util.CsrfConstants;
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
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;


/**
 * @author Nikola Koevski
 */
@RunWith(Parameterized.class)
@PowerMockIgnore("javax.security.*")
public class CsrfPreventionFilterTest {

  protected static final String SERVICE_PATH = "/camunda";

  @Rule
  public PowerMockRule rule = new PowerMockRule();

  protected Filter csrfPreventionFilter;

  protected MockHttpSession session;
  protected String token;

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
    this.session = new MockHttpSession();
    this.token = CsrfPreventionFilter.generateCSRFToken();
    this.session.setAttribute(CsrfConstants.CSRF_TOKEN_SESSION_ATTR_NAME, token);
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
  public void testNonModifyingRequestTokenValidation() throws IOException, ServletException {
    MockHttpServletResponse response = performHttpRequest(nonModifyingRequestUrl, "GET");
    String headerToken = (String) response.getHeader(CsrfConstants.CSRF_TOKEN_HEADER_NAME);

    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    Assert.assertNull(headerToken);
  }

  @Test
  public void testModifyingRequestTokenValidation() throws IOException, ServletException {
    MockHttpServletResponse response = performHttpRequest(modifyingRequestUrl, "POST");
    String headerToken = (String) response.getHeader(CsrfConstants.CSRF_TOKEN_HEADER_NAME);

    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    Assert.assertNull(headerToken);
  }

  @Test
  public void testModifyingRequestNoToken() throws IOException, ServletException {
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockHttpServletRequest modifyingRequest = new MockHttpServletRequest();
    modifyingRequest.setMethod("POST");
    modifyingRequest.setSession(session);
    modifyingRequest.setRequestURI(SERVICE_PATH  + modifyingRequestUrl);
    modifyingRequest.setContextPath(SERVICE_PATH);
    modifyingRequest.addHeader(CsrfConstants.CSRF_TOKEN_HEADER_NAME, "");

    applyFilter(modifyingRequest, response);

    Assert.assertEquals(CsrfConstants.CSRF_TOKEN_HEADER_REQUIRED, response.getHeader(CsrfConstants.CSRF_TOKEN_HEADER_NAME));
    Assert.assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    Assert.assertEquals("CSRFPreventionFilter: Token provided via HTTP Header is absent/empty.", response.getErrorMessage());
    Assert.assertNotEquals(modifyingRequest.getSession().getId(), session.getId());
  }

  @Test
  public void testModifyingRequestInvalidToken() throws IOException, ServletException {
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockHttpServletRequest modifyingRequest = new MockHttpServletRequest();
    modifyingRequest.setMethod("POST");
    modifyingRequest.setSession(session);
    modifyingRequest.setRequestURI(SERVICE_PATH  + modifyingRequestUrl);
    modifyingRequest.setContextPath(SERVICE_PATH);
    modifyingRequest.addHeader(CsrfConstants.CSRF_TOKEN_HEADER_NAME, "invalid_token");

    applyFilter(modifyingRequest, response);

    Assert.assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    Assert.assertEquals("CSRFPreventionFilter: Invalid HTTP Header Token.", response.getErrorMessage());
    Assert.assertNotEquals(modifyingRequest.getSession().getId(), session.getId());
  }

  protected MockHttpServletResponse performHttpRequest(String requestUrl, String requestType) throws IOException, ServletException {
    return performHttpRequest(requestUrl, requestType, token);
  }

  protected MockHttpServletResponse performHttpRequest(String requestUrl, String requestType, String token) throws IOException, ServletException {
    MockHttpServletResponse response = new MockHttpServletResponse();

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setMethod(requestType);
    request.setSession(session);
    request.setRequestURI(SERVICE_PATH  + requestUrl);
    request.setContextPath(SERVICE_PATH);
    request.addHeader(CsrfConstants.CSRF_TOKEN_HEADER_NAME, token);

    applyFilter(request, response);

    return response;
  }
}
