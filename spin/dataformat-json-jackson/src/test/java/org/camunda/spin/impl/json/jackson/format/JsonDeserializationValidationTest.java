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
package org.camunda.spin.impl.json.jackson.format;

import static org.mockito.Mockito.times;

import org.camunda.spin.DeserializationTypeValidator;
import org.camunda.spin.SpinRuntimeException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JsonDeserializationValidationTest {

  protected DeserializationTypeValidator validator;
  protected static JacksonJsonDataFormat format;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @BeforeClass
  public static void setUpMocks() {
    format = new JacksonJsonDataFormat("test");
  }

  @AfterClass
  public static void tearDown() {
    format = null;
  }

  @Test
  public void shouldValidateNothingForPrimitiveClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructType(int.class);
    validator = createValidatorMock(true);

    // when
    format.getMapper().validateType(type, validator);

    // then
    Mockito.verifyZeroInteractions(validator);
  }

  @Test
  public void shouldValidateBaseTypeOnlyForBaseClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructType(String.class);
    validator = createValidatorMock(true);

    // when
    format.getMapper().validateType(type, validator);

    // then
    Mockito.verify(validator).validate("java.lang.String");
    Mockito.verifyNoMoreInteractions(validator);
  }

  @Test
  public void shouldValidateBaseTypeOnlyForComplexClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructType(Complex.class);
    validator = createValidatorMock(true);

    // when
    format.getMapper().validateType(type, validator);

    // then
    Mockito.verify(validator).validate("org.camunda.spin.impl.json.jackson.format.JsonDeserializationValidationTest$Complex");
    Mockito.verifyNoMoreInteractions(validator);
  }

  @Test
  public void shouldValidateContentTypeOnlyForArrayClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructType(Integer[].class);
    validator = createValidatorMock(true);

    // when
    format.getMapper().validateType(type, validator);

    // then
    Mockito.verify(validator).validate("java.lang.Integer");
    Mockito.verifyNoMoreInteractions(validator);
  }

  @Test
  public void shouldValidateCollectionAndContentTypeForCollectionClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructFromCanonical("java.util.ArrayList<java.lang.String>");
    validator = createValidatorMock(true);

    // when
    format.getMapper().validateType(type, validator);

    // then
    Mockito.verify(validator).validate("java.util.ArrayList");
    Mockito.verify(validator).validate("java.lang.String");
    Mockito.verifyNoMoreInteractions(validator);
  }

  @Test
  public void shouldValidateCollectionAndContentTypeForNestedCollectionClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructFromCanonical("java.util.ArrayList<java.util.ArrayList<java.lang.String>>");
    validator = createValidatorMock(true);

    // when
    format.getMapper().validateType(type, validator);

    // then
    Mockito.verify(validator, times(2)).validate("java.util.ArrayList");
    Mockito.verify(validator).validate("java.lang.String");
    Mockito.verifyNoMoreInteractions(validator);
  }

  @Test
  public void shouldValidateMapAndKeyAndContentTypeForMapClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructFromCanonical("java.util.HashMap<java.lang.String, java.lang.Integer>");
    validator = createValidatorMock(true);

    // when
    format.getMapper().validateType(type, validator);

    // then
    Mockito.verify(validator).validate("java.util.HashMap");
    Mockito.verify(validator).validate("java.lang.String");
    Mockito.verify(validator).validate("java.lang.Integer");
    Mockito.verifyNoMoreInteractions(validator);
  }

  @Test
  public void shouldFailForSimpleClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructType(String.class);
    validator = createValidatorMock(false);

    // then
    thrown.expect(SpinRuntimeException.class);
    thrown.expectMessage("[java.lang.String]");

    // when
    format.getMapper().validateType(type, validator);
  }

  @Test
  public void shouldFailForComplexClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructType(Complex.class);
    validator = createValidatorMock(false);

    // then
    thrown.expect(SpinRuntimeException.class);
    thrown.expectMessage("[org.camunda.spin.impl.json.jackson.format.JsonDeserializationValidationTest$Complex]");

    // when
    format.getMapper().validateType(type, validator);
  }

  @Test
  public void shouldFailForArrayClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructType(Integer[].class);
    validator = createValidatorMock(false);

    // then
    thrown.expect(SpinRuntimeException.class);
    thrown.expectMessage("[java.lang.Integer]");

    // when
    format.getMapper().validateType(type, validator);
  }

  @Test
  public void shouldFailForCollectionClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructFromCanonical("java.util.ArrayList<java.lang.String>");
    validator = createValidatorMock(false);

    // then
    thrown.expect(SpinRuntimeException.class);
    thrown.expectMessage("[java.util.ArrayList, java.lang.String]");

    // when
    format.getMapper().validateType(type, validator);
  }

  @Test
  public void shouldFailForMapClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructFromCanonical("java.util.HashMap<java.lang.String, java.lang.Integer>");
    validator = createValidatorMock(false);

    // then
    thrown.expect(SpinRuntimeException.class);
    thrown.expectMessage("[java.util.HashMap, java.lang.String, java.lang.Integer]");

    // when
    format.getMapper().validateType(type, validator);
  }

  @Test
  public void shouldFailOnceForMapClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructFromCanonical("java.util.HashMap<java.lang.String, java.lang.String>");
    validator = createValidatorMock(false);

    // then
    thrown.expect(SpinRuntimeException.class);
    thrown.expectMessage("[java.util.HashMap, java.lang.String]");

    // when
    format.getMapper().validateType(type, validator);
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
