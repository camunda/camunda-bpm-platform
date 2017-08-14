package org.camunda.bpm.spring.boot.starter.webapp.filter;

public class LazySecurityFilter extends LazyDelegateFilter<ResourceLoaderDependingFilter> {

  public LazySecurityFilter() {
    super(ResourceLoadingSecurityFilter.class);
  }

}
