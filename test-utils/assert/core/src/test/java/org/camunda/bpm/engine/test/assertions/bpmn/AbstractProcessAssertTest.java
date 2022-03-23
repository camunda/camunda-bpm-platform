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
package org.camunda.bpm.engine.test.assertions.bpmn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Iterator;

import org.camunda.bpm.engine.ProcessEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

@SuppressWarnings("unchecked")
public class AbstractProcessAssertTest {

  ProcessEngine processEngine;
  Class<AbstractProcessAssert<?, ?>> anAssertClass;
  Class<?> anActualClass;
  Object anActual;

  Iterator<Class<AbstractProcessAssert<?, ?>>> allAsserts;

  @Before
  public void setUp() {
    processEngine = Mockito.mock(ProcessEngine.class);
    AbstractAssertions.init(processEngine);
    allAsserts = Arrays.asList((Class<AbstractProcessAssert<?, ?>>[]) new Class[] {
      JobAssert.class,
      ProcessDefinitionAssert.class,
      ProcessInstanceAssert.class,
      TaskAssert.class
    }).iterator();
  }

  @After
  public void tearDown() {
    AbstractAssertions.reset();
  }

  @Test
  public void testConstructorPattern() throws Exception {
    while(allAsserts.hasNext()) {
      mockActual(allAsserts.next());
      AbstractProcessAssert<?, ?> newInstanceFromExpectedConstructor = newInstanceFromExpectedConstructor();
      assertThat(newInstanceFromExpectedConstructor).isNotNull();
    }
  }

  @Test
  public void testFactoryMethodPattern() throws Exception {
    while(allAsserts.hasNext()) {
      mockActual(allAsserts.next());
      AbstractProcessAssert<?, ?> newInstanceFromExpectedFactoryMethod = newInstanceFromExpectedFactoryMethod();
      assertThat(newInstanceFromExpectedFactoryMethod).isNotNull();
    }
  }

  @Test
  public void testLastAssert_BeforeFirstAssert() {
    while(allAsserts.hasNext()) {
      mockActual(allAsserts.next());
      assertThat(AbstractProcessAssert.getLastAssert(anAssertClass)).isNull();
    }
  }

  @Test
  public void testLastAssert_AfterFirstAssert() {
    while(allAsserts.hasNext()) {
      mockActual(allAsserts.next());
      AbstractProcessAssert<?, ?> assertInstance = newInstanceFromExpectedFactoryMethod();
      assertThat(assertInstance).isNotNull();
      assertThat(AbstractProcessAssert.getLastAssert(anAssertClass)).isSameAs(assertInstance);
    }
  }

  @Test
  public void testLastAssert_AfterSecondAssert() {
    while(allAsserts.hasNext()) {
      mockActual(allAsserts.next());
      AbstractProcessAssert<?, ?> assertInstance1 = newInstanceFromExpectedFactoryMethod();
      assertThat(assertInstance1).isNotNull();
      AbstractProcessAssert<?, ?> assertInstance2 = newInstanceFromExpectedFactoryMethod();
      assertThat(assertInstance2).isNotNull();
      assertThat(AbstractProcessAssert.getLastAssert(anAssertClass)).isSameAs(assertInstance2);
      assertThat(assertInstance1).isNotSameAs(assertInstance2);
    }
  }

  private <A extends AbstractProcessAssert<?, ?>> A newInstanceFromExpectedConstructor() {
    Constructor<?> constructor = null;
    try {
      constructor = anAssertClass.getDeclaredConstructor(ProcessEngine.class, anActualClass);
    } catch (NoSuchMethodException e) {
      fail("Cannot find expected constructor!", e);
    }
    assert constructor != null;
    A assertInstance = null;
    try {
      assertInstance = (A) constructor.newInstance(processEngine, Mockito.mock(anActualClass));
    } catch (Exception e) {
      fail("Cannot create instance from constructor!", e);
    }
    return assertInstance;
  }

  private <A extends AbstractProcessAssert<?, ?>> A newInstanceFromExpectedFactoryMethod() {
    Method method = null;
    try {
      method = anAssertClass.getDeclaredMethod("assertThat", ProcessEngine.class, anActualClass);
    } catch (NoSuchMethodException e) {
      fail("Cannot find expected factory method!", e);
    }
    assert method != null;
    A assertInstance = null;
    try {
      assertInstance = (A) method.invoke(anAssertClass, processEngine, anActual);
    } catch (Exception e) {
      fail("Cannot create instance from constructor!", e);
    }
    return assertInstance;
  }

  private void mockActual(Class<AbstractProcessAssert<?, ?>> assertClass) {
    anAssertClass = assertClass;
    assertThat(assertClass).isNotNull();
    ParameterizedType type = (ParameterizedType) assertClass.getGenericSuperclass();
    assertThat(type.getActualTypeArguments()).hasSize(2);
    assertThat(type.getActualTypeArguments()[0]).isSameAs(assertClass);
    assertThat(type.getActualTypeArguments()[1]).isInstanceOf(Class.class);
    anActualClass = (Class<?>) type.getActualTypeArguments()[1];
    assertThat(anActualClass).isNotNull();
    anActual = Mockito.mock(anActualClass);
  }

}
