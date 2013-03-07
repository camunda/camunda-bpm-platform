package org.camunda.bpm.cycle.web.filter;

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
 * Makes sure views are always requested with a ending slash ("/") unless they have an extension.
 * That makes sure we always have the same semantics when we deal with resource urls. 
 * 
 * This filter must be configured in the <code>web.xml</code>.
 * 
 * @author nico.rehwaldt
 */
public class SlashUriFilter implements Filter {
  
  public void init(FilterConfig filterConfig) throws ServletException {
    
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    filter((HttpServletRequest) request, (HttpServletResponse) response, chain);
  }

  public void filter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    
    String uri = request.getRequestURI();
    int lastIndexOfSlash = uri.lastIndexOf("/");
    
    // Do we have the slash at the end of the uri? If so, forward, if not send redirect to proper location
    
    if (uri.lastIndexOf(".") > lastIndexOfSlash || lastIndexOfSlash == uri.length() - 1) {
      chain.doFilter(request, response);
    } else {
      String query = request.getQueryString();
      response.sendRedirect(uri + "/" + (query != null ? query : ""));
    }
  }
  
  public void destroy() {
    
  }
}
