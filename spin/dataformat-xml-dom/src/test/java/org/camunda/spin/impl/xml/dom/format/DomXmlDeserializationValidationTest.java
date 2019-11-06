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
package org.camunda.spin.impl.xml.dom.format;

import org.camunda.spin.DeserializationTypeValidator;
import org.camunda.spin.SpinRuntimeException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class DomXmlDeserializationValidationTest {

  protected DeserializationTypeValidator validator;
  protected static DomXmlDataFormat format;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @BeforeClass
  public static void setUpMocks() {
    format = new DomXmlDataFormat("test");
  }

  @AfterClass
  public static void tearDown() {
    format = null;
  }

  @Test
  public void shouldValidateNothingForPrimitiveClass() {
    // given
    validator = createValidatorMock(true);

    // when
    format.getMapper().validateType(int.class, validator);

    // then
    Mockito.verifyZeroInteractions(validator);
  }

  @Test
  public void shouldValidateBaseTypeOnlyForBaseClass() {
    // given
    validator = createValidatorMock(true);

    // when
    format.getMapper().validateType(String.class, validator);

    // then
    Mockito.verify(validator).validate("java.lang.String");
    Mockito.verifyNoMoreInteractions(validator);
  }

  @Test
  public void shouldValidateBaseTypeOnlyForComplexClass() {
    // given
    validator = createValidatorMock(true);

    // when
    format.getMapper().validateType(Complex.class, validator);

    // then
    Mockito.verify(validator).validate("org.camunda.spin.impl.xml.dom.format.DomXmlDeserializationValidationTest$Complex");
    Mockito.verifyNoMoreInteractions(validator);
  }

  @Test
  public void shouldValidateContentTypeOnlyForArrayClass() {
    // given
    validator = createValidatorMock(true);

    // when
    format.getMapper().validateType(Integer[].class, validator);

    // then
    Mockito.verify(validator).validate("java.lang.Integer");
    Mockito.verifyNoMoreInteractions(validator);
  }

  @Test
  public void shouldFailForSimpleClass() {
    // given
    validator = createValidatorMock(false);

    // then
    thrown.expect(SpinRuntimeException.class);
    thrown.expectMessage("'java.lang.String'");

    // when
    format.getMapper().validateType(String.class, validator);
  }

  @Test
  public void shouldFailForComplexClass() {
    // given
    validator = createValidatorMock(false);

    // then
    thrown.expect(SpinRuntimeException.class);
    thrown.expectMessage("'org.camunda.spin.impl.xml.dom.format.DomXmlDeserializationValidationTest$Complex'");

    // when
    format.getMapper().validateType(Complex.class, validator);
  }

  @Test
  public void shouldFailForArrayClass() {
    // given
    validator = createValidatorMock(false);

    // then
    thrown.expect(SpinRuntimeException.class);
    thrown.expectMessage("'java.lang.Integer'");

    // when
    format.getMapper().validateType(Integer[].class, validator);
  }

  public static class Complex {
    private Nested nested;

    public Nested getNested() {
      return nested;
    }
  }

  public static class Nested {
    private int testInt;

    public int getTestInt() {
      return testInt;
    }
  }

  protected DeserializationTypeValidator createValidatorMock(boolean result) {
    DeserializationTypeValidator newValidator = Mockito.mock(DeserializationTypeValidator.class);
    Mockito.when(newValidator.validate(Mockito.anyString())).thenReturn(result);
    return newValidator;
  }
}
