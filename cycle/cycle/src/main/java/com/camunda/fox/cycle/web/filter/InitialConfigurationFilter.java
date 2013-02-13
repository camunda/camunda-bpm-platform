package com.camunda.fox.cycle.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.camunda.fox.cycle.repository.UserRepository;

/**
 *
 * @author nico.rehwaldt
 */
public class InitialConfigurationFilter implements Filter {

  private WebApplicationContext context;
  
  private UserRepository userRepository;

  @Override
  public void init(FilterConfig config) throws ServletException {
    context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
    userRepository = context.getBean(UserRepository.class);
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    filterInitialConfiguration((HttpServletRequest) request, (HttpServletResponse) response, chain);
  }

  void filterInitialConfiguration(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (!isAtLeastOneUserConfigured() && !isConfigurationPage(request)) {
      redirectToConfigurationPage(request, response);
    } else {
      chain.doFilter(request, response);
    }
  }

  protected boolean isAtLeastOneUserConfigured() {
    return userRepository.countAll()>0;
  }

  private void redirectToConfigurationPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.sendRedirect(request.getContextPath() + "/" + "app/first-time-setup");
  }

  private boolean isConfigurationPage(HttpServletRequest request) {
    return request.getRequestURI().matches(".*/app/first-time-setup");
  }
}
