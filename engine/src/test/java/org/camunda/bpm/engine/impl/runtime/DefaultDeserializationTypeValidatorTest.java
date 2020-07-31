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
package org.camunda.bpm.engine.impl.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

public class DefaultDeserializationTypeValidatorTest {

  private static final String ANOTHER_CLASS = "another.class.Class";
  private static final String ANOTHER_PACKAGE = "another.class";
  private static final String SOME_CLASS = "some.class.Class";
  private static final String SOME_PACKAGE = "some.class";

  protected DefaultDeserializationTypeValidator validator;

  @Before
  public void setUp() {
    validator = new DefaultDeserializationTypeValidator();
  }

  // SETTERS

  @Test
  public void shouldAcceptNullListOfAllowedClasses() {
    // when
    validator.setAllowedClasses(null);
    // then
    assertThat(validator.allowedClasses).isEmpty();
  }

  @Test
  public void shouldAcceptEmptyListOfAllowedClasses() {
    // when
    validator.setAllowedClasses("");
    // then
    assertThat(validator.allowedClasses).isEmpty();
  }

  @Test
  public void shouldAcceptListOfEmptyAllowedClasses() {
    // when
    validator.setAllowedClasses("\r\n  , \t , ,,\n,\r");
    // then
    assertThat(validator.allowedClasses).isEmpty();
  }

  @Test
  public void shouldAcceptSingleStringOfAllowedClasses() {
    // when
    validator.setAllowedClasses("some.Class");
    // then
    assertThat(validator.allowedClasses).containsExactly("some.Class");
  }

  @Test
  public void shouldAcceptStringListOfAllowedClasses() {
    // when
    validator.setAllowedClasses("  some.class.Class , another.class.Class  ");
    // then
    assertThat(validator.allowedClasses).containsExactlyInAnyOrder(SOME_CLASS, ANOTHER_CLASS);
  }

  @Test
  public void shouldOverrideAllowedClasses() {
    // given
    validator.setAllowedClasses(SOME_CLASS);
    // when
    validator.setAllowedClasses(ANOTHER_CLASS);
    // then
    assertThat(validator.allowedClasses).containsExactly(ANOTHER_CLASS);
  }

  @Test
  public void shouldClearAllowedClasses() {
    // given
    validator.setAllowedClasses(SOME_CLASS);
    // when
    validator.setAllowedClasses(null);
    // then
    assertThat(validator.allowedClasses).isEmpty();
  }

  @Test
  public void shouldAcceptNullListOfAllowedPackages() {
    // when
    validator.setAllowedPackages(null);
    // then
    assertThat(validator.allowedPackages).isEmpty();
  }

  @Test
  public void shouldAcceptEmptyListOfAllowedPackages() {
    // when
    validator.setAllowedPackages("");
    // then
    assertThat(validator.allowedPackages).isEmpty();
  }

  @Test
  public void shouldAcceptListOfEmptyAllowedPackages() {
    // when
    validator.setAllowedPackages("\r\n  , \t , ,,\n,\r");
    // then
    assertThat(validator.allowedPackages).isEmpty();
  }

  @Test
  public void shouldAcceptSingleStringOfAllowedPackages() {
    // when
    validator.setAllowedPackages("some.");
    // then
    assertThat(validator.allowedPackages).containsExactly("some.");
  }

  @Test
  public void shouldAcceptStringListOfAllowedPackages() {
    // when
    validator.setAllowedPackages("  some.class , another.class  ");
    // then
    assertThat(validator.allowedPackages).containsExactlyInAnyOrder("some.class", "another.class");
  }

  @Test
  public void shouldOverrideAllowedPackages() {
    // given
    validator.setAllowedPackages(SOME_PACKAGE);
    // when
    validator.setAllowedPackages(ANOTHER_PACKAGE);
    // then
    assertThat(validator.allowedPackages).containsExactly(ANOTHER_PACKAGE);
  }

  @Test
  public void shouldClearAllowedPackages() {
    // given
    validator.setAllowedPackages(SOME_PACKAGE);
    // when
    validator.setAllowedPackages(null);
    // then
    assertThat(validator.allowedPackages).isEmpty();
  }

  // EMPTY WHITELIST

  @Test
  public void shouldForbidUnknownClassOnEmptyWhitelist() {
    // then
    assertThat(validator.validate(SOME_CLASS)).isFalse();
  }

  @Test
  public void shouldAllowJavaLangClassOnEmptyWhitelist() {
    // then
    assertThat(validator.validate(Number.class.getName())).isTrue();
  }

  @Test
  public void shouldAllowJavaUtilContainerClassesOnEmptyWhitelist() {
    // then
    assertThat(validator.validate(ArrayList.class.getName())).isTrue();
    assertThat(validator.validate(HashMap.class.getName())).isTrue();
    assertThat(validator.validate(Arrays.asList("a", "n").getClass().getName())).isTrue();
  }

  // ALLOWED CLASS(ES)

  @Test
  public void shouldAllowClassOnWhitelistedClass() {
    // given
    validator.setAllowedClasses(SOME_CLASS);
    // then
    assertThat(validator.validate(SOME_CLASS)).isTrue();
  }

  @Test
  public void shouldAllowClassOnWhitelistedClasses() {
    // given
    validator.setAllowedClasses(SOME_CLASS + "," + ANOTHER_CLASS);
    // then
    assertThat(validator.validate(SOME_CLASS)).isTrue();
  }

  @Test
  public void shouldForbidClassOnNonWhitelistedClass() {
    // given
    validator.setAllowedClasses(SOME_CLASS);
    // then
    assertThat(validator.validate(ANOTHER_CLASS)).isFalse();
  }

  @Test
  public void shouldForbidClassOnNonWhitelistedClasses() {
    // given
    validator.setAllowedClasses(SOME_CLASS + "," + ANOTHER_CLASS);
    // then
    assertThat(validator.validate("different.Class")).isFalse();
  }

  @Test
  public void shouldForbidClassOnEmptyClasses() {
    // given
    validator.setAllowedClasses(",  ,,");
    // then
    assertThat(validator.validate(SOME_CLASS)).isFalse();
  }

  // ALLOWED PACKAGE(S)

  @Test
  public void shouldAllowClassOnWhitelistedPackage() {
    // given
    validator.setAllowedPackages(SOME_PACKAGE);
    // then
    assertThat(validator.validate(SOME_CLASS)).isTrue();
  }

  @Test
  public void shouldAllowClassOnWhitelistedPackages() {
    // given
    validator.setAllowedPackages(SOME_PACKAGE + "," + ANOTHER_PACKAGE);
    // then
    assertThat(validator.validate(SOME_CLASS)).isTrue();
  }

  @Test
  public void shouldForbidClassOnNonWhitelistedPackage() {
    // given
    validator.setAllowedPackages(SOME_PACKAGE);
    // then
    assertThat(validator.validate(ANOTHER_CLASS)).isFalse();
  }

  @Test
  public void shouldForbidClassOnNonWhitelistedPackages() {
    // given
    validator.setAllowedPackages(SOME_PACKAGE + "," + ANOTHER_PACKAGE);
    // then
    assertThat(validator.validate("different.Class")).isFalse();
  }

  @Test
  public void shouldForbidClassOnEmptyPackages() {
    // given
    validator.setAllowedPackages(",  ,,");
    // then
    assertThat(validator.validate(SOME_CLASS)).isFalse();
  }

  // ALLOWED CLASS(ES) AND PACKAGE(S)

  @Test
  public void shouldAllowClassOnWhitelistedClassAndNonWhitelistedPackage() {
    // given
    validator.setAllowedClasses(SOME_CLASS);
    validator.setAllowedPackages(ANOTHER_PACKAGE);
    // then
    assertThat(validator.validate(SOME_CLASS)).isTrue();
  }

  @Test
  public void shouldAllowClassOnNonWhitelistedClassAndWhitelistedPackage() {
    // given
    validator.setAllowedClasses(ANOTHER_CLASS);
    validator.setAllowedPackages(SOME_PACKAGE);
    // then
    assertThat(validator.validate(SOME_CLASS)).isTrue();
  }

  @Test
  public void shouldForbidClassOnNonWhitelistedClassAndNonWhitelistedPackage() {
    // given
    validator.setAllowedPackages(SOME_PACKAGE);
    validator.setAllowedClasses(SOME_CLASS);
    // then
    assertThat(validator.validate(ANOTHER_CLASS)).isFalse();
  }
}
