package com.camunda.fox.cycle.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.camunda.fox.cycle.security.PrincipalHolder;

/**
 * Servlet Filter implementation class PrincipalHolderFilter
 */
public class PrincipalHolderFilter implements Filter {

  /**
   * Default constructor.
   */
  public PrincipalHolderFilter() {
  }

  /**
   * @see Filter#destroy()
   */
  public void destroy() {
  }

  /**
   * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    PrincipalHolder.setPrincipal(((HttpServletRequest) request).getUserPrincipal());
    chain.doFilter(request, response);
  }

  /**
   * @see Filter#init(FilterConfig)
   */
  public void init(FilterConfig fConfig) throws ServletException {
  }
}
