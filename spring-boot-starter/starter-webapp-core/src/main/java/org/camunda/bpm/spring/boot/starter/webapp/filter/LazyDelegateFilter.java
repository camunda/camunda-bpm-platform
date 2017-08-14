package org.camunda.bpm.spring.boot.starter.webapp.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class LazyDelegateFilter<T extends Filter> implements Filter {

  protected final Class<? extends T> delegateClass;
  protected InitHook<T> initHook;
  protected T delegate;
  protected FilterConfig filterConfig;

  public LazyDelegateFilter(Class<? extends T> delegateClass) {
    this.delegateClass = delegateClass;
    LazyInitRegistration.register(this);
  }

  public void lazyInit() {
    try {
      delegate = createNewFilterInstance();
      if (initHook != null) {
        initHook.init(delegate);
      }
      delegate.init(filterConfig);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;
    LazyInitRegistration.lazyInit(this);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    delegate.doFilter(request, response, chain);
  }

  @Override
  public void destroy() {
    delegate.destroy();
  }

  public InitHook<T> getInitHook() {
    return initHook;
  }

  public void setInitHook(InitHook<T> initHook) {
    this.initHook = initHook;
  }

  public Class<? extends T> getDelegateClass() {
    return delegateClass;
  }

  protected T createNewFilterInstance() throws InstantiationException, IllegalAccessException {
    return delegateClass.newInstance();
  }

  public static interface InitHook<T extends Filter> {

    void init(T filter);

  }
}
