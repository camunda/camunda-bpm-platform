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
package org.camunda.bpm.engine.rest.sub.impl;

import static org.mockito.Mockito.times;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.runtime.DeserializationTypeValidator;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class VariableDeserializationTypeValidationTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  protected AbstractVariablesResource variablesResourceSpy;
  protected DeserializationTypeValidator validator;

  @Before
  public void setUpMocks() {
    validator = Mockito.mock(DeserializationTypeValidator.class);

    ProcessEngineConfiguration configurationMock = Mockito.mock(ProcessEngineConfiguration.class);
    Mockito.when(configurationMock.isDeserializationTypeValidationEnabled()).thenReturn(true);
    Mockito.when(configurationMock.getDeserializationTypeValidator()).thenReturn(validator);

    variablesResourceSpy = createVariablesResourceSpy();
    Mockito.when(variablesResourceSpy.getProcessEngineConfiguration()).thenReturn(configurationMock);
  }

  @Test
  public void shouldValidateNothingForPrimitiveClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructType(int.class);
    setValidatorMockResult(true);

    // when
    variablesResourceSpy.validateType(type);

    // then
    Mockito.verifyNoInteractions(validator);
  }

  @Test
  public void shouldValidateBaseTypeOnlyForSimpleClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructType(String.class);
    setValidatorMockResult(true);

    // when
    variablesResourceSpy.validateType(type);

    // then
    Mockito.verify(validator).validate("java.lang.String");
    Mockito.verifyNoMoreInteractions(validator);
  }

  @Test
  public void shouldValidateBaseTypeOnlyForComplexClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructType(Complex.class);
    setValidatorMockResult(true);

    // when
    variablesResourceSpy.validateType(type);

    // then
    Mockito.verify(validator).validate("org.camunda.bpm.engine.rest.sub.impl.VariableDeserializationTypeValidationTest$Complex");
    Mockito.verifyNoMoreInteractions(validator);
  }

  @Test
  public void shouldValidateContentTypeOnlyForArrayClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructType(Integer[].class);
    setValidatorMockResult(true);

    // when
    variablesResourceSpy.validateType(type);

    // then
    Mockito.verify(validator).validate("java.lang.Integer");
    Mockito.verifyNoMoreInteractions(validator);
  }

  @Test
  public void shouldValidateCollectionAndContentTypeForCollectionClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructFromCanonical("java.util.ArrayList<java.lang.String>");
    setValidatorMockResult(true);

    // when
    variablesResourceSpy.validateType(type);

    // then
    Mockito.verify(validator).validate("java.util.ArrayList");
    Mockito.verify(validator).validate("java.lang.String");
    Mockito.verifyNoMoreInteractions(validator);
  }

  @Test
  public void shouldValidateCollectionAndContentTypeForNestedCollectionClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructFromCanonical("java.util.ArrayList<java.util.ArrayList<java.lang.String>>");
    setValidatorMockResult(true);

    // when
    variablesResourceSpy.validateType(type);

    // then
    Mockito.verify(validator, times(2)).validate("java.util.ArrayList");
    Mockito.verify(validator).validate("java.lang.String");
    Mockito.verifyNoMoreInteractions(validator);
  }

  @Test
  public void shouldValidateMapAndKeyAndContentTypeForMapClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructFromCanonical("java.util.HashMap<java.lang.String, java.lang.Integer>");
    setValidatorMockResult(true);

    // when
    variablesResourceSpy.validateType(type);

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
    setValidatorMockResult(false);

    // then
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("[java.lang.String]");

    // when
    variablesResourceSpy.validateType(type);
  }

  @Test
  public void shouldFailForComplexClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructType(Complex.class);
    setValidatorMockResult(false);

    // then
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("[org.camunda.bpm.engine.rest.sub.impl.VariableDeserializationTypeValidationTest$Complex]");

    // when
    variablesResourceSpy.validateType(type);
  }

  @Test
  public void shouldFailForArrayClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructType(Integer[].class);
    setValidatorMockResult(false);

    // then
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("[java.lang.Integer]");

    // when
    variablesResourceSpy.validateType(type);
  }

  @Test
  public void shouldFailForCollectionClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructFromCanonical("java.util.ArrayList<java.lang.String>");
    setValidatorMockResult(false);

    // then
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("[java.util.ArrayList, java.lang.String]");

    // when
    variablesResourceSpy.validateType(type);
  }

  @Test
  public void shouldFailForMapClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructFromCanonical("java.util.HashMap<java.lang.String, java.lang.Integer>");
    setValidatorMockResult(false);

    // then
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("[java.util.HashMap, java.lang.String, java.lang.Integer]");

    // when
    variablesResourceSpy.validateType(type);
  }

  @Test
  public void shouldFailOnceForMapClass() {
    // given
    JavaType type = TypeFactory.defaultInstance().constructFromCanonical("java.util.HashMap<java.lang.String, java.lang.String>");
    setValidatorMockResult(false);

    // then
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("[java.util.HashMap, java.lang.String]");

    // when
    variablesResourceSpy.validateType(type);
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

  protected void setValidatorMockResult(boolean result) {
    Mockito.when(validator.validate(Mockito.anyString())).thenReturn(result);
  }

  protected AbstractVariablesResource createVariablesResourceSpy() {
    return Mockito.spy(new AbstractVariablesResource(Mockito.mock(ProcessEngine.class), "test", Mockito.mock(ObjectMapper.class)) {

      @Override
      protected void updateVariableEntities(VariableMap variables, List<String> deletions) {
      }

      @Override
      protected void setVariableEntity(String variableKey, TypedValue variableValue) {
      }

      @Override
      protected void removeVariableEntity(String variableKey) {
      }

      @Override
      protected TypedValue getVariableEntity(String variableKey, boolean deserializeValue) {
        return null;
      }

      @Override
      protected VariableMap getVariableEntities(boolean deserializeValues) {
        return null;
      }

      @Override
      protected String getResourceTypeName() {
        return null;
      }
    });
  }

}
