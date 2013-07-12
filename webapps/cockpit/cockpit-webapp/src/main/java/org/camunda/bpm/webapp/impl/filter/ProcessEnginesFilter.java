package org.camunda.bpm.webapp.impl.filter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.camunda.bpm.cockpit.Cockpit;

/**
 *
 * @author nico.rehwaldt
 * @author Daniel Meyer
 */
public class ProcessEnginesFilter extends AbstractTemplateFilter {

  public static final String APP_ROOT_PLACEHOLDER = "$APP_ROOT";

  public static Pattern HTML_FILE_PATTERN = Pattern.compile("/app/(\\w+?)/(?:(\\w+)/(?:(|(.*\\.html))))?");

  @Override
  protected void applyFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

    String contextPath = request.getContextPath();
    String requestUri = request.getRequestURI().substring(contextPath.length());

    Matcher applicationBaseMatcher = HTML_FILE_PATTERN.matcher(requestUri);

    if (applicationBaseMatcher.matches()) {
      // access to base url, either /app/.* or /app/{engineName}/.*

      String appName = applicationBaseMatcher.group(1);
      String engineName = applicationBaseMatcher.group(2);
      String page = applicationBaseMatcher.group(3);

      if (engineName == null) {

        // access to /app/
        // redirect to /app/{defaultEngineName}/
        response.sendRedirect(String.format("%s/app/%s/%s/index.html", contextPath, appName, getDefaultEngineName()));
      } else {

        // access to /app/{engineName},
        // internal forward to /app/

        // serve the index page
        servePage(appName, engineName, page, request, response);
      }
    } else {
      // request to normal resource
      chain.doFilter(request, response);
    }
  }

  protected void servePage(String appName, String engineName, String page, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    if ("".equals(page) || "index.html".equals(page)) {
      serveIndexPage(appName, request, response);
    } else {
      request.getRequestDispatcher("/app/" + appName + "/" + page).forward(request, response);
    }
  }
  
  protected void serveIndexPage(String appName, HttpServletRequest request, HttpServletResponse response) throws IOException {
    String data = getWebResourceContents("/app/"+appName+"/index.html");
    
    data = data.replace(APP_ROOT_PLACEHOLDER, request.getContextPath());
    
    response.setContentLength(data.getBytes("UTF-8").length);
    response.setContentType("text/html");
    
    response.getWriter().append(data);
  }
  
  protected String getDefaultEngineName() {
    return Cockpit.getRuntimeDelegate().getDefaultProcessEngine().getName();
  }
}
