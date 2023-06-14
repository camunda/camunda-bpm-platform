/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.spring.boot.starter.webapp.filter;

import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;

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
  protected void loadFilterRules(FilterConfig filterConfig, String applicationPath) throws ServletException {
    String configFileName = filterConfig.getInitParameter("configFile");
    Resource resource = resourceLoader.getResource("classpath:" +webappProperty.getWebjarClasspath() + configFileName);
    InputStream configFileResource;
    try {
      configFileResource = resource.getInputStream();
    } catch (IOException e1) {
      throw new ServletException("Could not read security filter config file '" + configFileName + "': no such resource in servlet context.");
    }
    try {
      filterRules = FilterRules.load(configFileResource, applicationPath);
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
