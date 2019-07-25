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

import java.util.List;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionQueryImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CaseExecutionQueryVariableIgnoreCaseTest extends AbstractVariableIgnoreCaseTest<CaseExecutionQueryImpl, CaseExecution> {

  CaseService caseService;
  RepositoryService repositoryService;

  @Before
  public void init() {
    caseService = engineRule.getCaseService();
    repositoryService = engineRule.getRepositoryService();

    repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn").deploy();
    instance = caseService.withCaseDefinitionByKey("oneTaskCase").setVariables(VARIABLES).businessKey("oneTaskCase").create();
  }

  @After
  public void tearDown() {
    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  @Override
  protected CaseExecutionQueryImpl createQuery() {
    return (CaseExecutionQueryImpl) caseService.createCaseExecutionQuery();
  }

  @Override
  protected void assertThatTwoInstancesAreEqual(CaseExecution one, CaseExecution two) {
    assertThat(one.getId()).isEqualTo(two.getId());
  }

  public void assertThatListContainsOnlyExpectedElements(List<CaseExecution> instances, CaseExecution instance) {
    // normally we would only get one result. here we also get the corresponding CaseInstance
    assertThat(instances.size()).isEqualTo(2);
    assertThat(instances.get(0).getCaseInstanceId()).isEqualTo(instance.getCaseInstanceId());
    assertThat(instances.get(1).getCaseInstanceId()).isEqualTo(instance.getCaseInstanceId());
  }

  @Test
  public void testCaseInstanceVariableNameEqualsIgnoreCase() {
    // given
    // when
    List<CaseExecution> eq = queryNameIgnoreCase().caseInstanceVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<CaseExecution> eqNameLC = queryNameIgnoreCase().caseInstanceVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<CaseExecution> eqValueLC = queryNameIgnoreCase().caseInstanceVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<CaseExecution> eqNameValueLC = queryNameIgnoreCase().caseInstanceVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();

    // then
    assertThatListContainsOnlyExpectedElements(eq, instance);
    assertThatListContainsOnlyExpectedElements(eqNameLC, instance);
    assertThat(eqValueLC).isEmpty();
    assertThat(eqNameValueLC).isEmpty();
  }

  @Test
  public void testCaseInstanceVariableNameNotEqualsIgnoreCase() {
    // given
    // when
    List<CaseExecution> neq = queryNameIgnoreCase().caseInstanceVariableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<CaseExecution> neqNameLC = queryNameIgnoreCase().caseInstanceVariableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<CaseExecution> neqValueNE = queryNameIgnoreCase().caseInstanceVariableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<CaseExecution> neqNameLCValueNE = queryNameIgnoreCase().caseInstanceVariableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThat(neq).isEmpty();
    assertThat(neqNameLC).isEmpty();
    assertThatListContainsOnlyExpectedElements(neqValueNE, instance);
    assertThatListContainsOnlyExpectedElements(neqNameLCValueNE, instance);
  }

  @Test
  public void testCaseInstanceVariableValueEqualsIgnoreCase() {
    // given
    // when
    List<CaseExecution> eq = queryValueIgnoreCase().caseInstanceVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<CaseExecution> eqNameLC = queryValueIgnoreCase().caseInstanceVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<CaseExecution> eqValueLC = queryValueIgnoreCase().caseInstanceVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<CaseExecution> eqNameValueLC = queryValueIgnoreCase().caseInstanceVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();

    // then
    assertThatListContainsOnlyExpectedElements(eq, instance);
    assertThat(eqNameLC).isEmpty();
    assertThatListContainsOnlyExpectedElements(eqValueLC, instance);
    assertThat(eqNameValueLC).isEmpty();
  }

  @Test
  public void testCaseInstanceVariableValueNotEqualsIgnoreCase() {
    // given
    // when
    List<CaseExecution> neq = queryValueIgnoreCase().caseInstanceVariableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<CaseExecution> neqNameLC = queryValueIgnoreCase().caseInstanceVariableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<CaseExecution> neqValueNE = queryValueIgnoreCase().caseInstanceVariableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<CaseExecution> neqNameLCValueNE = queryValueIgnoreCase().caseInstanceVariableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThat(neq).isEmpty();
    assertThat(neqNameLC).isEmpty();
    assertThatListContainsOnlyExpectedElements(neqValueNE, instance);
    assertThat(neqNameLCValueNE).isEmpty();
  }

  @Test
  public void testCaseInstanceVariableNameAndValueEqualsIgnoreCase() {
    // given
    // when
    List<CaseExecution> eq = queryNameValueIgnoreCase().caseInstanceVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<CaseExecution> eqNameLC = queryNameValueIgnoreCase().caseInstanceVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<CaseExecution> eqValueLC = queryNameValueIgnoreCase().caseInstanceVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<CaseExecution> eqValueNE = queryNameValueIgnoreCase().caseInstanceVariableValueEquals(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<CaseExecution> eqNameValueLC = queryNameValueIgnoreCase().caseInstanceVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();
    List<CaseExecution> eqNameLCValueNE = queryNameValueIgnoreCase().caseInstanceVariableValueEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThatListContainsOnlyExpectedElements(eq, instance);
    assertThatListContainsOnlyExpectedElements(eqNameLC, instance);
    assertThatListContainsOnlyExpectedElements(eqValueLC, instance);
    assertThat(eqValueNE).isEmpty();
    assertThatListContainsOnlyExpectedElements(eqNameValueLC, instance);
    assertThat(eqNameLCValueNE).isEmpty();
  }

  @Test
  public void testCaseInstanceVariableNameAndValueNotEqualsIgnoreCase() {
    // given
    // when
    List<CaseExecution> neq = queryNameValueIgnoreCase().caseInstanceVariableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE).list();
    List<CaseExecution> neqNameLC = queryNameValueIgnoreCase().caseInstanceVariableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE).list();
    List<CaseExecution> neqValueLC = queryNameValueIgnoreCase().caseInstanceVariableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE_LC).list();
    List<CaseExecution> neqValueNE = queryNameValueIgnoreCase().caseInstanceVariableValueNotEquals(VARIABLE_NAME, VARIABLE_VALUE_NE).list();
    List<CaseExecution> neqNameValueLC = queryNameValueIgnoreCase().caseInstanceVariableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_LC).list();
    List<CaseExecution> neqNameLCValueNE = queryNameValueIgnoreCase().caseInstanceVariableValueNotEquals(VARIABLE_NAME_LC, VARIABLE_VALUE_NE).list();

    // then
    assertThat(neq).isEmpty();
    assertThat(neqNameLC).isEmpty();
    assertThat(neqValueLC).isEmpty();
    assertThatListContainsOnlyExpectedElements(neqValueNE, instance);
    assertThat(neqNameValueLC).isEmpty();
    assertThatListContainsOnlyExpectedElements(neqNameLCValueNE, instance);
  }
}
