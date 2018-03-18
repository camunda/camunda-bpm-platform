package org.camunda.bpm.spring.boot.starter.webapp.filter;

import javax.servlet.Filter;
import org.camunda.bpm.spring.boot.starter.property.WebappProperty;

import org.springframework.core.io.ResourceLoader;

public interface ResourceLoaderDependingFilter extends Filter {

  void setResourceLoader(ResourceLoader resourceLoader);
  void setWebappProperty(WebappProperty webappProperty);
}
