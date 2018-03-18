package org.camunda.bpm.spring.boot.starter.webapp.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import org.camunda.bpm.spring.boot.starter.property.WebappProperty;

import org.camunda.bpm.webapp.impl.engine.ProcessEnginesFilter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class ResourceLoadingProcessEnginesFilter extends ProcessEnginesFilter implements ResourceLoaderDependingFilter {

  private ResourceLoader resourceLoader;
  private WebappProperty webappProperty;

  @Override
  protected String getWebResourceContents(String name) throws IOException {
    InputStream is = null;

    try {
      Resource resource = resourceLoader.getResource("classpath:"+webappProperty.getWebjarClasspath() + name);
      is = resource.getInputStream();

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
        try {
          is.close();
        } catch (IOException e) {
        }
      }
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
