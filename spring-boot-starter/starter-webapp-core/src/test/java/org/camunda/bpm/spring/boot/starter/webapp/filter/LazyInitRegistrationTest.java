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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.Filter;

import org.camunda.bpm.spring.boot.starter.webapp.filter.LazyDelegateFilter.InitHook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class LazyInitRegistrationTest {

  @Mock
  private LazyDelegateFilter<ResourceLoaderDependingFilter> lazyDelegateFilterMock;

  @Mock
  private ApplicationContext applicationContextMock;

  @Mock
  private InitHook<ResourceLoaderDependingFilter> initHookMock;

  @Before
  public void init() {
    LazyInitRegistration.APPLICATION_CONTEXT = null;
    LazyInitRegistration.REGISTRATION.clear();
  }

  @Test
  public void registerTest() {
    LazyInitRegistration.register(lazyDelegateFilterMock);
    assertTrue(LazyInitRegistration.getRegistrations().contains(lazyDelegateFilterMock));
  }

  @Test
  public void getInitHookWithoutApplicationContextTest() {
    assertNull(LazyInitRegistration.getInitHook());
  }

  @Test
  public void getInitHookWithoutResourceLoaderDependingInitHook() {
    LazyInitRegistration.APPLICATION_CONTEXT = applicationContextMock;
    when(applicationContextMock.containsBean(LazyInitRegistration.RESOURCE_LOADER_DEPENDING_INIT_HOOK)).thenReturn(false);
    assertNull(LazyInitRegistration.getInitHook());
  }

  @Test
  public void getInitHookWithResourceLoaderDependingInitHook() {
    LazyInitRegistration.APPLICATION_CONTEXT = applicationContextMock;
    when(applicationContextMock.containsBean(LazyInitRegistration.RESOURCE_LOADER_DEPENDING_INIT_HOOK)).thenReturn(true);
    when(applicationContextMock.getBean(LazyInitRegistration.RESOURCE_LOADER_DEPENDING_INIT_HOOK, InitHook.class)).thenReturn(initHookMock);

    assertEquals(initHookMock, LazyInitRegistration.getInitHook());
  }

  @Test
  public void isRegisteredTest() {
    assertFalse(LazyInitRegistration.isRegistered(lazyDelegateFilterMock));
    LazyInitRegistration.register(lazyDelegateFilterMock);
    assertTrue(LazyInitRegistration.isRegistered(lazyDelegateFilterMock));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void lazyInitWithoutApplicationContext() {
    assertFalse(LazyInitRegistration.lazyInit(lazyDelegateFilterMock));
    verify(lazyDelegateFilterMock, times(0)).setInitHook(Mockito.any(InitHook.class));
    verify(lazyDelegateFilterMock, times(0)).lazyInit();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void lazyInitWithoutRegistration() {
    LazyInitRegistration.APPLICATION_CONTEXT = applicationContextMock;
    assertFalse(LazyInitRegistration.lazyInit(lazyDelegateFilterMock));
    verify(lazyDelegateFilterMock, times(0)).setInitHook(Mockito.any(InitHook.class));
    verify(lazyDelegateFilterMock, times(0)).lazyInit();
  }

  @Test
  public void lazyInitWithRegistration() {
    LazyInitRegistration.APPLICATION_CONTEXT = applicationContextMock;
    LazyInitRegistration.register(lazyDelegateFilterMock);
    when(applicationContextMock.containsBean(LazyInitRegistration.RESOURCE_LOADER_DEPENDING_INIT_HOOK)).thenReturn(true);
    when(applicationContextMock.getBean(LazyInitRegistration.RESOURCE_LOADER_DEPENDING_INIT_HOOK, InitHook.class)).thenReturn(initHookMock);

    assertTrue(LazyInitRegistration.lazyInit(lazyDelegateFilterMock));
    verify(lazyDelegateFilterMock, times(1)).setInitHook(initHookMock);
    verify(lazyDelegateFilterMock, times(1)).lazyInit();
    assertFalse(LazyInitRegistration.getRegistrations().contains(lazyDelegateFilterMock));
  }

  @Test
  public void lazyInitWithoutInitHook() {
    LazyInitRegistration.APPLICATION_CONTEXT = applicationContextMock;
    LazyInitRegistration.register(lazyDelegateFilterMock);
    when(applicationContextMock.containsBean(LazyInitRegistration.RESOURCE_LOADER_DEPENDING_INIT_HOOK)).thenReturn(false);

    assertTrue(LazyInitRegistration.lazyInit(lazyDelegateFilterMock));
    verify(lazyDelegateFilterMock, times(1)).setInitHook(null);
    verify(lazyDelegateFilterMock, times(1)).lazyInit();
    assertFalse(LazyInitRegistration.getRegistrations().contains(lazyDelegateFilterMock));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getRegistrationsTest() {
    LazyInitRegistration.getRegistrations().add(lazyDelegateFilterMock);
  }

  @Test
  public void setApplicationContextTest() {
    try (MockedStatic<LazyInitRegistration> theMock = Mockito.mockStatic(LazyInitRegistration.class)) {
      LazyInitRegistration.register(lazyDelegateFilterMock);
      Set<LazyDelegateFilter<? extends Filter>> registrations = new HashSet<>();
      registrations.add(lazyDelegateFilterMock);
      theMock.when(() -> LazyInitRegistration.getRegistrations()).thenReturn(registrations);
  
      new LazyInitRegistration().setApplicationContext(applicationContextMock);
  
      assertEquals(LazyInitRegistration.APPLICATION_CONTEXT, applicationContextMock);
      theMock.verify(() -> LazyInitRegistration.lazyInit(lazyDelegateFilterMock));
    }
  }
}
