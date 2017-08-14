package org.camunda.bpm.spring.boot.starter.webapp.filter;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.camunda.bpm.spring.boot.starter.webapp.filter.LazyDelegateFilter.InitHook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LazyInitRegistration.class)
public class LazyDelegateFilterTest {

  @Mock
  private Filter filterMock;

  @Mock
  private FilterConfig filterConfigMock;

  @Mock
  private InitHook<Filter> initHookMock;

  @Test
  public void initTest() throws Exception {
    PowerMockito.mockStatic(LazyInitRegistration.class);
    LazyDelegateFilter<Filter> delegateFilter = new LazyDelegateFilter<Filter>(filterMock.getClass());
    delegateFilter.delegate = filterMock;
    delegateFilter.init(filterConfigMock);
    assertSame(filterConfigMock, delegateFilter.filterConfig);
    verify(filterMock, times(0)).init(Mockito.any(FilterConfig.class));
    PowerMockito.verifyStatic();
    LazyInitRegistration.lazyInit(delegateFilter);
  }

  @Test
  public void lazyInitTest() throws Exception {
    LazyDelegateFilter<Filter> delegateFilter = spy(new LazyDelegateFilter<Filter>(filterMock.getClass()));
    delegateFilter.init(filterConfigMock);
    doReturn(filterMock).when(delegateFilter).createNewFilterInstance();
    delegateFilter.lazyInit();
    verify(filterMock).init(filterConfigMock);
  }

  @Test
  public void lazyInitWithHookTest() throws Exception {
    LazyDelegateFilter<Filter> delegateFilter = spy(new LazyDelegateFilter<Filter>(filterMock.getClass()));
    delegateFilter.setInitHook(initHookMock);
    delegateFilter.init(filterConfigMock);
    doReturn(filterMock).when(delegateFilter).createNewFilterInstance();
    delegateFilter.lazyInit();
    InOrder order = inOrder(filterMock, initHookMock);
    order.verify(initHookMock).init(filterMock);
    order.verify(filterMock).init(filterConfigMock);
  }

  @Test
  public void doFilterTest() throws Exception {
    LazyDelegateFilter<Filter> delegateFilter = new LazyDelegateFilter<Filter>(filterMock.getClass());
    delegateFilter.delegate = filterMock;
    ServletRequest request = mock(ServletRequest.class);
    ServletResponse response = mock(ServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    delegateFilter.doFilter(request, response, chain);
    verify(filterMock).doFilter(request, response, chain);
  }

  @Test
  public void destroyTest() {
    LazyDelegateFilter<Filter> delegateFilter = new LazyDelegateFilter<Filter>(filterMock.getClass());
    delegateFilter.delegate = filterMock;
    delegateFilter.destroy();
    verify(filterMock).destroy();
  }

  @Test
  public void lazyInitRegistrationTest() {
    PowerMockito.mockStatic(LazyInitRegistration.class);
    LazyDelegateFilter<Filter> delegateFilter = new LazyDelegateFilter<Filter>(filterMock.getClass());
    PowerMockito.verifyStatic();
    LazyInitRegistration.register(delegateFilter);
  }

}
