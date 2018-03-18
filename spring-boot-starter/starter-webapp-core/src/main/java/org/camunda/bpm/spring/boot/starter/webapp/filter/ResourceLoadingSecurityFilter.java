package org.camunda.bpm.spring.boot.starter.webapp.filter;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.spring.boot.starter.property.WebappProperty;
import org.camunda.bpm.webapp.impl.security.filter.SecurityFilter;
import org.camunda.bpm.webapp.impl.security.filter.util.FilterRules;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class ResourceLoadingSecurityFilter extends SecurityFilter implements ResourceLoaderDependingFilter {

  private ResourceLoader resourceLoader;

  private WebappProperty webappProperty;
  @Override
  protected void loadFilterRules(FilterConfig filterConfig) throws ServletException {
    String configFileName = filterConfig.getInitParameter("configFile");
    Resource resource = resourceLoader.getResource("classpath:" +webappProperty.getWebjarClasspath() + configFileName);
    InputStream configFileResource;
    try {
      configFileResource = resource.getInputStream();
    } catch (IOException e1) {
      throw new ServletException("Could not read security filter config file '" + configFileName + "': no such resource in servlet context.");
    }
    try {
      filterRules = FilterRules.load(configFileResource);
    } catch (Exception e) {
      throw new RuntimeException("Exception while parsing '" + configFileName + "'", e);
    } finally {
      IoUtil.closeSilently(configFileResource);
    }
  }
  
  /**
   * @return the resourceLoader
   */
  public ResourceLoader getResourceLoader() {
    return resourceLoader;
  }

  /**
   * @param resourceLoader
   *          the resourceLoader to set
   */
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }
  
  /**
   * @return the webappProperty
   */
    public WebappProperty getWebappProperty() {
        return webappProperty;
    }

    /**
     * @param webappProperty 
     *          webappProperty to set
     */
    public void setWebappProperty(WebappProperty webappProperty) {
        this.webappProperty = webappProperty;
    }
  
}
