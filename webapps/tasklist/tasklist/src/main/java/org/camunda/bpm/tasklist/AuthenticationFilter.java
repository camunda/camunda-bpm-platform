package org.camunda.bpm.tasklist;

import org.camunda.bpm.tasklist.util.StatusExposingServletResponse;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: drobisch
 */
public class AuthenticationFilter implements Filter {
  public static final String AUTH_USER = "authenticatedUser";

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
    String authUser = (String) httpServletRequest.getSession().getAttribute(AUTH_USER);

    StatusExposingServletResponse response = new StatusExposingServletResponse((HttpServletResponse)servletResponse);

    if ( authUser == null) {
      response.setStatus(401);
      response.getWriter().print("{}");
      response.getWriter().flush();
    }else {
      filterChain.doFilter(servletRequest, response);
    }

  }

  @Override
  public void destroy() {
  }
}
