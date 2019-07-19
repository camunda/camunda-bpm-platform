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

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
public class VariableQueryIgnoreCaseTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();

  public RuntimeService runtimeService;

  private static final String VARIABLE_NAME = "variableName";
  private static final String VARIABLE_NAME_LC = VARIABLE_NAME.toLowerCase();
  private static final String VARIABLE_VALUE = "variableValue";
  private static final String VARIABLE_VALUE_LC = VARIABLE_VALUE.toLowerCase();
  private static final String VARIABLE_VALUE_LC_LIKE = "%" + VARIABLE_VALUE_LC.substring(2, 10) + "%";
  private static final String VARIABLE_VALUE_NE = "nonExistent";
  private static Map<String, Object> VARIABLES = new HashMap<>();

  private ProcessInstance processInstance;
  static {
    VARIABLES.put(VARIABLE_NAME, VARIABLE_VALUE);
  }

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", VARIABLES);
  }

  @Test
  public void testProcessInstanceQueryVariableNameEqualsIgnoreCase() {
    // given
    // when
    List<ProcessInstance> eq = queryNameIgnoreCase().variableValueEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<ProcessInstance> eqNameLC = queryNameIgnoreCase().variableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<ProcessInstance> eqValueLC = queryNameIgnoreCase().variableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<ProcessInstance> eqNameValueLC = queryNameIgnoreCase().variableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();

    // then
    assertThatListContainsOnlyExpectedElement(eq, processInstance);
    assertThatListContainsOnlyExpectedElement(eqNameLC, processInstance);
    assertThat(eqValueLC).isEmpty();
    assertThat(eqNameValueLC).isEmpty();
  }

  @Test
  public void testProcessInstanceQueryVariableNameNotEqualsIgnoreCase() {
    // given
    // when
    List<ProcessInstance> neq = queryNameIgnoreCase().variableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<ProcessInstance> neqNameLC = queryNameIgnoreCase().variableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<ProcessInstance> neqValueNE = queryNameIgnoreCase().variableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<ProcessInstance> neqNameLCValueNE = queryNameIgnoreCase().variableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThat(neq).isEmpty();
    assertThat(neqNameLC).isEmpty();
    assertThatListContainsOnlyExpectedElement(neqValueNE, processInstance);
    assertThatListContainsOnlyExpectedElement(neqNameLCValueNE, processInstance);
  }

  @Test
  public void testProcessInstanceQueryVariableValueEqualsIgnoreCase() {
    // given
    // when
    List<ProcessInstance> eq = queryValueIgnoreCase().variableValueEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<ProcessInstance> eqNameLC = queryValueIgnoreCase().variableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<ProcessInstance> eqValueLC = queryValueIgnoreCase().variableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<ProcessInstance> eqNameValueLC = queryValueIgnoreCase().variableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();

    // then
    assertThatListContainsOnlyExpectedElement(eq, processInstance);
    assertThat(eqNameLC).isEmpty();
    assertThatListContainsOnlyExpectedElement(eqValueLC, processInstance);
    assertThat(eqNameValueLC).isEmpty();
  }

  @Test
  public void testProcessInstanceQueryVariableValueNotEqualsIgnoreCase() {
    // given
    // when
    List<ProcessInstance> neq = queryValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<ProcessInstance> neqNameLC = queryValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<ProcessInstance> neqValueNE = queryValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<ProcessInstance> neqNameLCValueNE = queryValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThat(neq).isEmpty();
    assertThat(neqNameLC).isEmpty();
    assertThatListContainsOnlyExpectedElement(neqValueNE, processInstance);
    assertThat(neqNameLCValueNE).isEmpty();
  }

  @Test
  public void testProcessInstanceQueryVariableValueLikeIgnoreCase() {
    // given
    // when
    List<ProcessInstance> like = queryNameValueIgnoreCase().variableValueLike(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<ProcessInstance> likeValueLC = queryValueIgnoreCase().variableValueLike(VARIABLE_NAME, VARIABLE_VALUE_LC_LIKE).list();

    // then
    assertThatListContainsOnlyExpectedElement(like, processInstance);
    assertThatListContainsOnlyExpectedElement(likeValueLC, processInstance);
  }

  @Test
  public void testProcessInstanceQueryVariableNameAndValueEqualsIgnoreCase() {
    // given
    // when
    List<ProcessInstance> eq = queryNameValueIgnoreCase().variableValueEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<ProcessInstance> eqNameLC = queryNameValueIgnoreCase().variableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<ProcessInstance> eqValueLC = queryNameValueIgnoreCase().variableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<ProcessInstance> eqValueNE = queryNameValueIgnoreCase().variableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<ProcessInstance> eqNameValueLC = queryNameValueIgnoreCase().variableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();
    List<ProcessInstance> eqNameLCValueNE = queryNameValueIgnoreCase().variableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThatListContainsOnlyExpectedElement(eq, processInstance);
    assertThatListContainsOnlyExpectedElement(eqNameLC, processInstance);
    assertThatListContainsOnlyExpectedElement(eqValueLC, processInstance);
    assertThat(eqValueNE).isEmpty();
    assertThatListContainsOnlyExpectedElement(eqNameValueLC, processInstance);
    assertThat(eqNameLCValueNE).isEmpty();
  }

  @Test
  public void testProcessInstanceQueryVariableNameAndValueNotEqualsIgnoreCase() {
    // given
    // when
    List<ProcessInstance> neq = queryNameValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<ProcessInstance> neqNameLC = queryNameValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<ProcessInstance> neqValueLC = queryNameValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<ProcessInstance> neqValueNE = queryNameValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<ProcessInstance> neqNameValueLC = queryNameValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();
    List<ProcessInstance> neqNameLCValueNE = queryNameValueIgnoreCase().variableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThat(neq).isEmpty();
    assertThat(neqNameLC).isEmpty();
    assertThat(neqValueLC).isEmpty();
    assertThatListContainsOnlyExpectedElement(neqValueNE, processInstance);
    assertThat(neqNameValueLC).isEmpty();
    assertThatListContainsOnlyExpectedElement(neqNameLCValueNE, processInstance);
  }

  private void assertThatListContainsOnlyExpectedElement(List<ProcessInstance> instances, ProcessInstance instance) {
    assertThat(instances.size()).isEqualTo(1);
    assertThat(instances.get(0).getId()).isEqualTo(instance.getId());
  }

  private ProcessInstanceQuery processInstanceQuery() {
    return runtimeService.createProcessInstanceQuery();
  }

  private ProcessInstanceQuery queryNameIgnoreCase() {
    return processInstanceQuery().matchVariableNamesIgnoreCase();
  }

  private ProcessInstanceQuery queryValueIgnoreCase() {
    return processInstanceQuery().matchVariableValuesIgnoreCase();
  }

  private ProcessInstanceQuery queryNameValueIgnoreCase() {
    return processInstanceQuery().matchVariableNamesIgnoreCase().matchVariableValuesIgnoreCase();
  }
}
