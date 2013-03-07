package org.camunda.bpm.security.web;

import java.io.IOException;
import java.util.ArrayList;
import static org.fest.assertions.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.camunda.bpm.cycle.entity.User;
import org.camunda.bpm.security.UserIdentity;
import org.camunda.bpm.security.service.SecurityService;
import org.camunda.bpm.security.web.SecurityFilter;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.*;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.web.context.WebApplicationContext;


/**
 *
 * @author nico.rehwaldt
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityFilterTest {

  private static String CTX_PATH = "/myApp";
  
  @Mock
  private SecurityService securityService;
  
  @Mock
  private WebApplicationContext webApplicationContext;
  
  @Mock
  private HttpServletRequest request;
  
  @Mock
  private HttpServletResponse response;
  
  @Mock
  private FilterChain filterChain;
  
  @Mock
  private HttpSession session;
  
  @Mock
  private RequestDispatcher requestDispatcher;
  
  private Map<String, Object> sessionVars = new HashMap<String, Object>();
  
  private Map<String, String> requestHeaders = new HashMap<String, String>();
  
  private Map<Class, Object> beans = new HashMap<Class, Object>();
  
  private String redirectUri;
  
  private String dispatchUri;
  
  // the filter to test
  private SecurityFilter securityFilter = new SecurityFilter();
  
  @Before
  public void before() throws IOException {
    // mock request / session stuff
    when(request.getSession()).thenReturn(session);

    when(session.getAttribute(anyString())).thenAnswer(new Answer<Object>() {
      
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        return sessionVars.get(invocation.getArguments()[0]);
      }
    });
    
    when(request.getContextPath()).thenReturn(CTX_PATH);
    
    doAnswer(new Answer<RequestDispatcher>() {

      @Override
      public RequestDispatcher answer(InvocationOnMock invocation) throws Throwable {
        dispatchUri = (String) invocation.getArguments()[0];
        return requestDispatcher;
      }
    }).when(request).getRequestDispatcher(anyString());
    
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        
        sessionVars.put((String) invocation.getArguments()[0], invocation.getArguments()[1]);
        return null;
      }
    }).when(session).setAttribute(anyString(), anyObject());
    
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        
        redirectUri = (String) invocation.getArguments()[0];
        return null;
      }
    }).when(response).sendRedirect(anyString());
    
    // mock web application context
    
    when(webApplicationContext.getBean(any(Class.class))).thenAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        return beans.get(invocation.getArguments()[0]);
      }
    });
    
    // provide security service
    
    beans.put(SecurityService.class, securityService);
    
    // wire filter and context
    securityFilter.setWebApplicationContext(webApplicationContext);
  }
  
  @Test
  public void testSetupShouldAllowCommonInvocations() throws Exception {
    // given
    givenRequest("app/secured/bla", "POST");
    
    // when
    sessionVars.put("foo", "bar");
    response.sendRedirect("asf");
    
    // then
    assertThat(request.getRequestURI()).isEqualTo(appUri("app/secured/bla"));
    assertThat(request.getMethod()).isEqualTo("POST");
    assertThat(request.getSession()).isEqualTo(session);
    assertThat(request.getSession().getAttribute("foo")).isEqualTo("bar");
    
    assertThat(redirectUri).isEqualTo("asf");
    
    assertThat(webApplicationContext.getBean(SecurityService.class)).isEqualTo(securityService);
  }
  
  @Test
  public void shouldNotStorePreLoginUrlOnPost() throws Exception {
    // given
    givenRequest("app/secured/bla", "POST");
    
    // when
    securityFilter.performSecurityCheck(appUri("app/secured/bla"), request, response);
    
    // then
    assertThat(sessionVars).doesNotContainKey(SecurityFilter.PRE_AUTHENTICATION_URL);
  }

  @Test
  public void shouldStorePreLoginUrlOnGet() throws Exception {
    // given
    givenRequest("app/secured/bla", "GET");
    
    // when
    securityFilter.performSecurityCheck(appUri("app/secured/bla"), request, response);
    
    // then
    assertThat(sessionVars).containsKey(SecurityFilter.PRE_AUTHENTICATION_URL);
    assertThat(sessionVars.get(SecurityFilter.PRE_AUTHENTICATION_URL)).isEqualTo(appUri("app/secured/bla"));
  }
  
  @Test
  public void shouldProvideLoggedInCredentialsInRequest() throws Exception {
    
    // given
    User user = new User();
    user.setName("klaus");
    user.setAdmin(true);
    
    sessionVars.put(SecurityFilter.IDENTITY_SESSION_KEY, new UserIdentity(user));
    
    // when
    securityFilter.doFilterSecure(request, response, filterChain);
    
    // then
    verify(filterChain).doFilter(argThat(reflectsCredentials(user)), any(HttpServletResponse.class));
  }
  
  @Test
  public void shouldFailLoginViaGet() throws Exception {
    // given
    givenRequest("j_security_check", "GET");
    
    givenRequestParameters(new HashMap<String, String>() {{
      put("j_username", "klaus");
      put("j_password", "***");
    }});
    
    given(securityService.login(CTX_PATH, CTX_PATH)).willReturn(null);
    
    // when
    securityFilter.doFilterSecure(request, response, filterChain);
    
    // then
    assertThat(sessionVars).doesNotContainKey(SecurityFilter.IDENTITY_SESSION_KEY);
    
    // filter chain should not have been called
    verify(filterChain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
  }
  
  @Test
  public void shouldFailLogin() throws Exception {
    // given
    givenRequest("j_security_check", "POST");
    
    givenRequestParameters(new HashMap<String, String>() {{
      put("j_username", "klaus");
      put("j_password", "***");
    }});
    
    given(securityService.login(anyString(), anyString())).willReturn(null);
    
    // when
    securityFilter.doFilterSecure(request, response, filterChain);
    
    // then
    assertThat(sessionVars).doesNotContainKey(SecurityFilter.IDENTITY_SESSION_KEY);
    assertIsRedirect("app/login/error");
    
    verifyZeroInteractions(filterChain);
  }
  
  @Test
  public void shouldFailLoginOnMissingParams() throws Exception {
    // given
    givenRequest("j_security_check", "POST");
    
    givenRequestParameters(new HashMap<String, String>() {{
      
    }});
    
    // when
    securityFilter.doFilterSecure(request, response, filterChain);
    
    // then
    assertThat(sessionVars).doesNotContainKey(SecurityFilter.IDENTITY_SESSION_KEY);
    assertIsRedirect("app/login/error");
    
    verifyZeroInteractions(filterChain);
  }
  
  @Test
  public void shouldLogin() throws Exception {
    
    UserIdentity identity = new UserIdentity("klaus");
    
    // given
    givenRequest("j_security_check", "POST");
    
    givenRequestParameters(new HashMap<String, String>() {{
      put("j_username", "klaus");
      put("j_password", "***");
    }});
    
    given(securityService.login("klaus", "***")).willReturn(identity);
    
    // when
    securityFilter.doFilterSecure(request, response, filterChain);
    
    // then
    assertThat(sessionVars).containsKey(SecurityFilter.IDENTITY_SESSION_KEY);
    assertThat(sessionVars.get(SecurityFilter.IDENTITY_SESSION_KEY)).isEqualTo(identity);
    assertIsRedirect("app/secured/view/index");
    
    verifyZeroInteractions(filterChain);
  }

  @Test
  public void shouldLoginAndRedirectToPreLoginPage() throws Exception {
    
    UserIdentity identity = new UserIdentity("klaus");
    
    // given
    givenRequest("j_security_check", "POST");
    
    givenRequestParameters(new HashMap<String, String>() {{
      put("j_username", "klaus");
      put("j_password", "***");
    }});
    
    given(securityService.login("klaus", "***")).willReturn(identity);
    
    sessionVars.put(SecurityFilter.PRE_AUTHENTICATION_URL, appUri("app/secured/bar"));
    
    // when
    securityFilter.doFilterSecure(request, response, filterChain);
    
    // then
    assertIsRedirect("app/secured/bar");
    
    verifyZeroInteractions(filterChain);
  }
  
  @Test
  public void shouldForwardToLoginPage() throws Exception {
    
    // given
    givenRequest("app/secured/bla", "GET");
    
    // when
    securityFilter.doFilterSecure(request, response, filterChain);
    
    assertIsForward("/app/login");
    
    // filter chain should not have been called
    verifyZeroInteractions(filterChain);
  }

  @Test
  public void shouldFilterOnNoLogin() throws Exception {
    // given
    givenRequest("app/asdf", "GET");

    // when
    securityFilter.doFilterSecure(request, response, filterChain);
    
    // filter chain should not have been called
    verify(filterChain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
  }

  // test helpers //////////////////////////////
  
  private void givenRequest(String uri, String method) {
    when(request.getRequestURI()).thenReturn(appUri(uri));
    when(request.getMethod()).thenReturn(method);
  }

  private void givenRequestParameters(final Map<String, String> parameters) {
    
    when(request.getParameter(anyString())).thenAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        return parameters.get((String) invocation.getArguments()[0]);
      }
    });
  }

  private String appUri(String uri) {
    return CTX_PATH + "/" + uri;
  }
  
  private void assertIsRedirect(String uri) {
    assertThat(appUri(uri)).isEqualTo(redirectUri);
  }
  
  private void assertIsForward(String uri) throws ServletException, IOException {
    assertThat(uri).isEqualTo(dispatchUri);
    verify(requestDispatcher).forward(any(ServletRequest.class), any(ServletResponse.class));
  }

  // matching of wrapped request ////////////////////////////////////
  
  private Matcher<HttpServletRequest> reflectsCredentials(User user) {
    
    List<String> roles = new ArrayList<String>();
    roles.add("user");
    
    if (user.isAdmin()) {
      roles.add("admin");
    }
    
    return 
      new WrappedRequestMatcher(user.getName(), roles.toArray(new String[0]), new String[] { "FooBAR0" });
  }
  
  private class WrappedRequestMatcher extends BaseMatcher<HttpServletRequest> {
    
    private final String name;
    private final String[] roles;
    private final String[] notRoles;
    
    public WrappedRequestMatcher(String name, String[] roles, String[] notRoles) {
      this.name = name;
      this.roles = roles;
      this.notRoles = notRoles;
    }

    @Override
    public boolean matches(Object item) {
      if (item instanceof HttpServletRequest) {
        HttpServletRequest request = (HttpServletRequest) item;
        
        for (String role: roles) {
          if (!request.isUserInRole(role)) {
            fail("request does not have expected role <" + role + ">");
          }
        }
        
        for (String role: notRoles) {
          if (request.isUserInRole(role)) {
            fail("request has unexpected role <" + role + ">");
          }
        }
        
        if (!request.getUserPrincipal().getName().equals(name)) {
          fail("request has unexpected principal <" + request.getUserPrincipal().getName() + ">; should have had <" + name + ">");
        }
      } 
      else {
        return false;
      }
      
      return true;
    }

    @Override
    public void describeTo(Description description) {
      description.appendValue("being very special");
    }
  }
}
