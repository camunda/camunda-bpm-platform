package org.camunda.bpm.spring.boot.starter.webapp.filter;

public class LazyProcessEnginesFilter extends LazyDelegateFilter<ResourceLoaderDependingFilter> {

  public LazyProcessEnginesFilter() {
    super(ResourceLoadingProcessEnginesFilter.class);
  }

}
