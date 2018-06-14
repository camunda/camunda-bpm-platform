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
    performNonModifyingRequest(nonModifyingRequestUrl, new MockHttpSession());
  }

  @Test
  public void testConsecutiveNonModifyingRequestTokenGeneration() throws IOException, ServletException {
    MockHttpSession session = new MockHttpSession();

    // first non-modifying request
    String firstToken = performNonModifyingRequest(nonModifyingRequestUrl, session);
    // second non-modifying request
    String secondToken = performNonModifyingRequest(nonModifyingRequestUrl, session);

    Assert.assertNotEquals(firstToken, secondToken);
  }

  @Test
  public void testNonCachedNonModifyingRequestTokenGeneration() throws IOException, ServletException {
    try {
      if (isModifyingFetchRequest) {
        // first fill up the cache (default size is 5)
        MockHttpSession session = new MockHttpSession();
        performNonModifyingRequest(nonModifyingRequestUrl, session);
        performNonModifyingRequest(nonModifyingRequestUrl, session);
        performNonModifyingRequest(nonModifyingRequestUrl, session);
        performNonModifyingRequest(nonModifyingRequestUrl, session);
        String lastToken = performNonModifyingRequest(nonModifyingRequestUrl, session);

        // TODO: search for a better solution for delay, ClockUtils doesn't work
        Thread.sleep(501L);

        // cache is full, sixth non-modifying request (ttl < 500ms) returns last token
        String token = performNonModifyingRequest(nonModifyingRequestUrl, session);

        Assert.assertNotEquals(lastToken, token);
      }
    } catch (InterruptedException e) {
      // nop
    }
  }

  @Test
  public void testCachedTokenOnNonModifyingRequest() throws IOException, ServletException {
    if (isModifyingFetchRequest) {
      // first fill up the cache (default size is 5)
      MockHttpSession session = new MockHttpSession();
      performNonModifyingRequest(nonModifyingRequestUrl, session);
      performNonModifyingRequest(nonModifyingRequestUrl, session);
      performNonModifyingRequest(nonModifyingRequestUrl, session);
      performNonModifyingRequest(nonModifyingRequestUrl, session);
      String lastToken = performNonModifyingRequest(nonModifyingRequestUrl, session);

      // cache is full, sixth non-modifying request (ttl < 500ms) returns last token
      String token = performNonModifyingRequest(nonModifyingRequestUrl, session);

      Assert.assertEquals(lastToken, token);
    }
  }

  @Test
  public void testModifyingRequestTokenValidation() throws IOException, ServletException {
    MockHttpSession session = new MockHttpSession();
    // first non-modifying request
    String token = performNonModifyingRequest(nonModifyingRequestUrl, session);

    if (isModifyingFetchRequest) {
      String secondToken = performNonModifyingRequest(modifyingRequestUrl, session);
      Assert.assertNotEquals(token, secondToken);
    } else {
      HttpServletResponse response = performModifyingRequest(token, session);
      Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }
  }

  @Test
  public void testModifyingRequestInvalidToken() throws IOException, ServletException {
    MockHttpSession session = new MockHttpSession();
    performNonModifyingRequest(nonModifyingRequestUrl, session);

    if (!isModifyingFetchRequest) {
      HttpServletResponse response = performModifyingRequest("invalid token", session);
      Assert.assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());

      response = performModifyingRequest("", session);
      Assert.assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());

      response = performModifyingRequest(null, session);
      Assert.assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }
  }

  protected String performNonModifyingRequest(String requestUrl, MockHttpSession session) throws IOException, ServletException {
    MockHttpServletResponse response = new MockHttpServletResponse();

    MockHttpServletRequest nonModifyingRequest = new MockHttpServletRequest();
    nonModifyingRequest.setMethod("GET");
    nonModifyingRequest.setSession(session);
    nonModifyingRequest.setRequestURI(SERVICE_PATH  + requestUrl);
    nonModifyingRequest.setContextPath(SERVICE_PATH);

    applyFilter(nonModifyingRequest, response);

    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    Assert.assertNotNull(response.getCookie("XSRF-TOKEN"));
    Assert.assertEquals(false, response.getCookie("XSRF-TOKEN").getValue().isEmpty());

    return response.getCookie("XSRF-TOKEN").getValue();
  }

  protected HttpServletResponse performModifyingRequest(String token, MockHttpSession session) throws IOException, ServletException {
    MockHttpServletResponse response = new MockHttpServletResponse();

    MockHttpServletRequest modifyingRequest = new MockHttpServletRequest();
    modifyingRequest.setMethod("POST");
    modifyingRequest.setSession(session);
    modifyingRequest.setRequestURI(SERVICE_PATH  + modifyingRequestUrl);
    modifyingRequest.setContextPath(SERVICE_PATH);

    Cookie[] cookies = {new Cookie("XSRF-TOKEN", token)};
    modifyingRequest.setCookies(cookies);

    applyFilter(modifyingRequest, response);

    return response;
  }
}
