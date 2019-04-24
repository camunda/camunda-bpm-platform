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
package org.camunda.bpm.engine.rest.util;

import static org.camunda.bpm.engine.rest.helper.NoIntermediaryInvocation.immediatelyAfter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoAssertionError;

/**
 * @author Thorben Lindhauer
 */
public class NoIntermediaryInvocationTest {

  protected Foo foo;

  @Before
  public void setUp() {
    foo = Mockito.mock(Foo.class);
  }

  @Test
  public void testSucceess() {

    // given
    foo.getFoo();
    foo.getBar();

    // when
    InOrder inOrder = Mockito.inOrder(foo);
    inOrder.verify(foo).getFoo();

    // then
    inOrder.verify(foo, immediatelyAfter()).getBar();

  }

  @Test
  public void testFailureWhenInvocationNotPresent() {
    // given
    foo.getFoo();
    foo.getBaz();

    // when
    InOrder inOrder = Mockito.inOrder(foo);
    inOrder.verify(foo).getFoo();

    // then
    try {
      inOrder.verify(foo, immediatelyAfter()).getBar();
      Assert.fail("should not verify");
    } catch (MockitoAssertionError e) {
      // happy path
    }
  }

  @Test
  public void testFailureWhenInvocationNotPresentCase2() {
    // given
    foo.getFoo();

    // when
    InOrder inOrder = Mockito.inOrder(foo);
    inOrder.verify(foo).getFoo();

    // then
    try {
      inOrder.verify(foo, immediatelyAfter()).getBar();
      Assert.fail("should not verify");
    } catch (MockitoAssertionError e) {
      // happy path
    }
  }

  @Test
  public void testFailureOnWrongInvocationOrder() {
    // given
    foo.getBar();
    foo.getFoo();

    // when
    InOrder inOrder = Mockito.inOrder(foo);
    inOrder.verify(foo).getFoo();

    // then
    try {
      inOrder.verify(foo, immediatelyAfter()).getBar();
      Assert.fail("should not verify");
    } catch (MockitoAssertionError e) {
      // happy path
    }
  }

  @Test
  public void testFailureWithIntermittentInvocations() {
    // given
    foo.getFoo();
    foo.getBaz();
    foo.getBar();

    // when
    InOrder inOrder = Mockito.inOrder(foo);
    inOrder.verify(foo).getFoo();

    // then
    try {
      inOrder.verify(foo, immediatelyAfter()).getBar();
      Assert.fail("should not verify");
    } catch (MockitoAssertionError e) {
      // happy path
    }
  }

  public static interface Foo {

    String getFoo();

    String getBar();

    String getBaz();
  }
}
