package org.camunda.bpm.cockpit.impl.web.filter.engine;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.cockpit.impl.web.filter.AbstractTemplateFilter;

/**
 *
 * @author nico.rehwaldt
 */
public class EnginesFilter extends AbstractTemplateFilter {

  protected static final String COCKPIT_ROOT_PLACEHOLDER = "$COCKPIT_ROOT";

  protected static Pattern APPLICATION_BASE_PATTERN = Pattern.compile("/app/(?:(\\w+)/)?");

  @Override
  protected void applyFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

    String contextPath = request.getContextPath();
    String requestUri = request.getRequestURI().substring(contextPath.length());

    Matcher applicationBaseMatcher = APPLICATION_BASE_PATTERN.matcher(requestUri);

    if (applicationBaseMatcher.matches()) {
      // access to base url, either /app/ or /app/{engineName}/

      String engineName = applicationBaseMatcher.group(1);
      if (engineName == null) {

        // access to /app/
        // redirect to /app/{defaultEngineName}/
        response.sendRedirect(String.format("%s/app/%s/", contextPath, getDefaultEngineName()));
      } else {

        // access to /app/{engineName},
        // internal forward to /app/

        // serve the index page
        serveIndexPage(engineName, request, response);
      }
    } else {
      // request to normal resource
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {

  }

  protected String getDefaultEngineName() {
    return BpmPlatform.getDefaultProcessEngine().getName();
  }

  protected void serveIndexPage(String engineName, HttpServletRequest request, HttpServletResponse response) throws IOException {
    String data = getWebResourceContents("/app/index.html");

    data = data.replace(COCKPIT_ROOT_PLACEHOLDER, request.getContextPath());

    response.setContentLength(data.getBytes("UTF-8").length);
    response.setContentType("text/html");

    response.getWriter().append(data);
  }
}
