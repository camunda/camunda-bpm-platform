package com.camunda.fox.cycle.web.security.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author nico.rehwaldt
 */
public class SecurityFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    performSecurityCheck((HttpServletRequest) request, (HttpServletResponse) response, chain);
  }

  private void performSecurityCheck(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
    
  }

  @Override
  public void destroy() {
    
  }
  
}
