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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

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
    if (delegate != null) {
      delegate.destroy();
    }
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
