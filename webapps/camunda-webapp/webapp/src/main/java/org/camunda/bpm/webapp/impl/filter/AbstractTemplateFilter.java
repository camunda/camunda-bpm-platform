package org.camunda.bpm.webapp.impl.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A {@link Filter} implementation that can be used to realize basic templating.
 *
 * @author nico.rehwaldt
 */
public abstract class AbstractTemplateFilter implements Filter {

  private FilterConfig filterConfig;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;
  }

  @Override
  public void destroy() {
    filterConfig = null;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    applyFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
  }

  /**
   * Apply the filter to the given request/response.
   *
   * This method must be provided by subclasses to perform actual work.
   *
   * @param request
   * @param response
   * @param chain
   *
   * @throws IOException
   * @throws ServletException
   */
  protected abstract void applyFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException;

  /**
   * Returns true if the given web resource exists.
   * 
   * @param name
   * @return
   */
  protected boolean hasWebResource(String name) {
    try {
      URL resource = filterConfig.getServletContext().getResource(name);
      return resource != null;
    } catch (MalformedURLException e) {
      return false;
    }
  }

  /**
   * Returns the string contents of a web resource with the given name.
   *
   * The resource must be static and text based.
   *
   * @param name the name of the resource
   *
   * @return the resource contents
   *
   * @throws IOException
   */
  protected String getWebResourceContents(String name) throws IOException {

    InputStream is = null;

    try {
      is = filterConfig.getServletContext().getResourceAsStream(name);

      BufferedReader reader = new BufferedReader(new InputStreamReader(is));

      StringWriter writer = new StringWriter();
      String line = null;

      while ((line = reader.readLine()) != null) {
        writer.write(line);
        writer.append("\n");
      }

      return writer.toString();
    } finally {
      if (is != null) {
        try { is.close(); } catch (IOException e) { }
      }
    }
  }
}
