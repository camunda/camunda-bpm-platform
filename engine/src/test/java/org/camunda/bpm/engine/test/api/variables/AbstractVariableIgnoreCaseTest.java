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
package org.camunda.bpm.engine.test.api.variables;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.AbstractVariableQueryImpl;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

@SuppressWarnings("unchecked")
public abstract class AbstractVariableIgnoreCaseTest<T extends AbstractVariableQueryImpl, U> {

  protected abstract T createQuery();

  protected abstract void assertThatTwoInstancesAreEqual(U one, U two);

  protected static final String VARIABLE_NAME = "variableName";
  protected static final String VARIABLE_NAME_LC = VARIABLE_NAME.toLowerCase();
  protected static final String VARIABLE_VALUE = "variableValue";
  protected static final String VARIABLE_VALUE_LC = VARIABLE_VALUE.toLowerCase();
  protected static final String VARIABLE_VALUE_LC_LIKE = "%" + VARIABLE_VALUE_LC.substring(2, 10) + "%";
  protected static final String VARIABLE_VALUE_NE = "nonExistent";
  protected static Map<String, Object> VARIABLES = new HashMap<>();
  static {
    VARIABLES.put(VARIABLE_NAME, VARIABLE_VALUE);
  }

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  public U instance;

  protected T queryNameIgnoreCase() {
    return (T) createQuery().matchVariableNamesIgnoreCase();
  }

  protected T queryValueIgnoreCase() {
    return (T) createQuery().matchVariableValuesIgnoreCase();
  }

  protected T queryNameValueIgnoreCase() {
    return (T) queryNameIgnoreCase().matchVariableValuesIgnoreCase();
  }

  protected void assertThatListContainsOnlyExpectedElement(List<U> instances, U instance) {
    assertThat(instances.size()).isEqualTo(1);
    assertThatTwoInstancesAreEqual(instances.get(0), instance);
  }

  @Test
  public void testVariableNameEqualsIgnoreCase() {
    // given
    // when
    List<U> eq = queryNameIgnoreCase().variableValueEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<U> eqNameLC = queryNameIgnoreCase().variableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<U> eqValueLC = queryNameIgnoreCase().variableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<U> eqNameValueLC = queryNameIgnoreCase().variableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();

    // then
    assertThatListContainsOnlyExpectedElement(eq, instance);
    assertThatListContainsOnlyExpectedElement(eqNameLC, instance);
    assertThat(eqValueLC).isEmpty();
    assertThat(eqNameValueLC).isEmpty();
  }

  @Test
  public void testVariableNameNotEqualsIgnoreCase() {
    // given
    // when
    List<U> neq = queryNameIgnoreCase().variableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<U> neqNameLC = queryNameIgnoreCase().variableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<U> neqValueNE = queryNameIgnoreCase().variableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<U> neqNameLCValueNE = queryNameIgnoreCase().variableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThat(neq).isEmpty();
    assertThat(neqNameLC).isEmpty();
    assertThatListContainsOnlyExpectedElement(neqValueNE, instance);
    assertThatListContainsOnlyExpectedElement(neqNameLCValueNE, instance);
  }

  @Test
  public void testVariableValueEqualsIgnoreCase() {
    // given
    // when
    List<U> eq = queryValueIgnoreCase().variableValueEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<U> eqNameLC = queryValueIgnoreCase().variableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<U> eqValueLC = queryValueIgnoreCase().variableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<U> eqNameValueLC = queryValueIgnoreCase().variableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();

    // then
    assertThatListContainsOnlyExpectedElement(eq, instance);
    assertThat(eqNameLC).isEmpty();
    assertThatListContainsOnlyExpectedElement(eqValueLC, instance);
    assertThat(eqNameValueLC).isEmpty();
  }

  @Test
  public void testVariableValueNotEqualsIgnoreCase() {
    // given
    // when
    List<U> neq = queryValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<U> neqNameLC = queryValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<U> neqValueNE = queryValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<U> neqNameLCValueNE = queryValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThat(neq).isEmpty();
    assertThat(neqNameLC).isEmpty();
    assertThatListContainsOnlyExpectedElement(neqValueNE, instance);
    assertThat(neqNameLCValueNE).isEmpty();
  }

  @Test
  public void testVariableValueLikeIgnoreCase() {
    // given
    // when
    List<U> like = queryNameValueIgnoreCase().variableValueLike(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<U> likeValueLC = queryValueIgnoreCase().variableValueLike(VARIABLE_NAME, VARIABLE_VALUE_LC_LIKE).list();

    // then
    assertThatListContainsOnlyExpectedElement(like, instance);
    assertThatListContainsOnlyExpectedElement(likeValueLC, instance);
  }

  @Test
  public void testVariableNameAndValueEqualsIgnoreCase() {
    // given
    // when
    List<U> eq = queryNameValueIgnoreCase().variableValueEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<U> eqNameLC = queryNameValueIgnoreCase().variableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<U> eqValueLC = queryNameValueIgnoreCase().variableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<U> eqValueNE = queryNameValueIgnoreCase().variableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<U> eqNameValueLC = queryNameValueIgnoreCase().variableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();
    List<U> eqNameLCValueNE = queryNameValueIgnoreCase().variableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThatListContainsOnlyExpectedElement(eq, instance);
    assertThatListContainsOnlyExpectedElement(eqNameLC, instance);
    assertThatListContainsOnlyExpectedElement(eqValueLC, instance);
    assertThat(eqValueNE).isEmpty();
    assertThatListContainsOnlyExpectedElement(eqNameValueLC, instance);
    assertThat(eqNameLCValueNE).isEmpty();
  }

  @Test
  public void testVariableNameAndValueNotEqualsIgnoreCase() {
    // given
    // when
    List<U> neq = queryNameValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<U> neqNameLC = queryNameValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<U> neqValueLC = queryNameValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<U> neqValueNE = queryNameValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<U> neqNameValueLC = queryNameValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();
    List<U> neqNameLCValueNE = queryNameValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThat(neq).isEmpty();
    assertThat(neqNameLC).isEmpty();
    assertThat(neqValueLC).isEmpty();
    assertThatListContainsOnlyExpectedElement(neqValueNE, instance);
    assertThat(neqNameValueLC).isEmpty();
    assertThatListContainsOnlyExpectedElement(neqNameLCValueNE, instance);
  }
}
