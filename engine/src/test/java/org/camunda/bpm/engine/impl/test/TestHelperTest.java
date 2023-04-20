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
package org.camunda.bpm.engine.impl.test;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestHelperTest {

  @Test
  public void shouldGetPublicMethod() throws NoSuchMethodException {
    // WHEN we call get method to retrieve a method with public accessor, no exception should be thrown
    Object methodName = TestHelper.getMethod(SomeTestClass.class, "testSomethingWithPublicAccessor", new Class[0]);
    assertThat(methodName.toString()).isEqualTo("public void org.camunda.bpm.engine.impl.test.TestHelperTest$SomeTestClass.testSomethingWithPublicAccessor()");
  }

  @Test
  public void shouldGetPublicMethodFromSuperClass() throws NoSuchMethodException {
    // WHEN we call get method to retrieve a method with public accessor, no exception should be thrown
    Object methodName = TestHelper.getMethod(SomeOtherTestClass.class, "testSomethingWithPublicAccessor", new Class[0]);
    assertThat(methodName.toString()).isEqualTo("public void org.camunda.bpm.engine.impl.test.TestHelperTest$SomeTestClass.testSomethingWithPublicAccessor()");
  }

  @Test
  public void shouldGetPackagePrivateMethod() throws NoSuchMethodException {
    // WHEN we call get method to retrieve a method with package private accessor, no exception should be thrown
    Object methodName = TestHelper.getMethod(SomeTestClass.class, "testSomethingWithPackagePrivateAccessor", new Class[0]);
    assertThat(methodName.toString()).isEqualTo("void org.camunda.bpm.engine.impl.test.TestHelperTest$SomeTestClass.testSomethingWithPackagePrivateAccessor()");
  }

  @Test
  public void shouldGetPackagePrivateMethodFromSuperClass() throws NoSuchMethodException {
    // WHEN we call get method to retrieve a method with package private accessor, no exception should be thrown
    Object methodName = TestHelper.getMethod(SomeOtherTestClass.class, "testSomethingWithPackagePrivateAccessor", new Class[0]);
    assertThat(methodName.toString()).isEqualTo("void org.camunda.bpm.engine.impl.test.TestHelperTest$SomeTestClass.testSomethingWithPackagePrivateAccessor()");
  }

  @Test
  public void shouldGetProtectedMethod() throws NoSuchMethodException {
    // WHEN we call get method to retrieve a method with protected accessor, no exception should be thrown
    Object methodName = TestHelper.getMethod(SomeTestClass.class, "testSomethingWithProtected", new Class[0]);
    assertThat(methodName.toString()).isEqualTo("protected void org.camunda.bpm.engine.impl.test.TestHelperTest$SomeTestClass.testSomethingWithProtected()");
  }

  @Test
  public void shouldGetProtectedMethodFromSuperClass() throws NoSuchMethodException {
    // WHEN we call get method to retrieve a method with protected accessor, no exception should be thrown
    Object methodName = TestHelper.getMethod(SomeOtherTestClass.class, "testSomethingWithProtected", new Class[0]);
    assertThat(methodName.toString()).isEqualTo("protected void org.camunda.bpm.engine.impl.test.TestHelperTest$SomeTestClass.testSomethingWithProtected()");
  }

  static class SomeTestClass {

    public void testSomethingWithPublicAccessor() {
    }

    void testSomethingWithPackagePrivateAccessor() {
    }

    protected void testSomethingWithProtected() {
    }

  }

  static class SomeOtherTestClass extends SomeTestClass {
  }

}
