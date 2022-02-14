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
package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
/**
 *
 * @author Deivarayan Azhagappan
 *
 */
public class MultiTenancyExecutionVariableCmdsTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";

  protected static final String VARIABLE_1 = "testVariable1";
  protected static final String VARIABLE_2 = "testVariable2";

  protected static final String VARIABLE_VALUE_1 = "test1";
  protected static final String VARIABLE_VALUE_2 = "test2";

  protected static final String PROCESS_DEFINITION_KEY = "oneTaskProcess";

  protected static final BpmnModelInstance ONE_TASK_PROCESS = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent()
      .userTask()
      .endEvent()
      .done();

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected String processInstanceId;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void init() {

    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, ONE_TASK_PROCESS);

    processInstanceId = engineRule.getRuntimeService()
    .startProcessInstanceByKey(PROCESS_DEFINITION_KEY,
         Variables.createVariables()
         .putValue(VARIABLE_1, VARIABLE_VALUE_1)
         .putValue(VARIABLE_2, VARIABLE_VALUE_2))
    .getId();
  }

  @Test
  public void getExecutionVariableWithAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    // then
    assertEquals(VARIABLE_VALUE_1, engineRule.getRuntimeService().getVariable(processInstanceId, VARIABLE_1));
  }

  @Test
  public void getExecutionVariableWithNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> engineRule.getRuntimeService().getVariable(processInstanceId, VARIABLE_1))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot read the process instance '"
          + processInstanceId +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void getExecutionVariableWithDisabledTenantCheck() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // then
    assertEquals(VARIABLE_VALUE_1, engineRule.getRuntimeService().getVariable(processInstanceId, VARIABLE_1));

  }

  // get typed execution variable
  @Test
  public void getExecutionVariableTypedWithAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    // then
    assertEquals(VARIABLE_VALUE_1, engineRule.getRuntimeService().getVariableTyped(processInstanceId, VARIABLE_1).getValue());
  }

  @Test
  public void getExecutionVariableTypedWithNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> engineRule.getRuntimeService().getVariableTyped(processInstanceId, VARIABLE_1))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot read the process instance '"
          + processInstanceId +"' because it belongs to no authenticated tenant.");
  }

  @Test
  public void getExecutionVariableTypedWithDisabledTenantCheck() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    // if
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // then
    assertEquals(VARIABLE_VALUE_1, engineRule.getRuntimeService().getVariableTyped(processInstanceId, VARIABLE_1).getValue());

  }

  // get execution variables
  @Test
  public void getExecutionVariablesWithAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    // then
    assertEquals(2, engineRule.getRuntimeService().getVariables(processInstanceId).size());
  }

  @Test
  public void getExecutionVariablesWithNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> engineRule.getRuntimeService().getVariables(processInstanceId).size())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot read the process instance '"
          + processInstanceId +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void getExecutionVariablesWithDisabledTenantCheck() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // then
    assertEquals(2, engineRule.getRuntimeService().getVariables(processInstanceId).size());

  }

  // set execution variable
  @Test
  public void setExecutionVariableWithAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    // then
    engineRule.getRuntimeService().setVariable(processInstanceId, "newVariable", "newValue");
    assertEquals(3, engineRule.getRuntimeService().getVariables(processInstanceId).size());
  }

  @Test
  public void setExecutionVariableWithNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> engineRule.getRuntimeService().setVariable(processInstanceId, "newVariable", "newValue"))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot update the process instance '"
          + processInstanceId +"' because it belongs to no authenticated tenant.");
  }

  @Test
  public void setExecutionVariableWithDisabledTenantCheck() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    engineRule.getRuntimeService().setVariable(processInstanceId, "newVariable", "newValue");
    assertEquals(3, engineRule.getRuntimeService().getVariables(processInstanceId).size());
  }

  // remove execution variable
  @Test
  public void removeExecutionVariableWithAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    engineRule.getRuntimeService().removeVariable(processInstanceId, VARIABLE_1);

    // then
    assertEquals(1, engineRule.getRuntimeService().getVariables(processInstanceId).size());
  }

  @Test
  public void removeExecutionVariableWithNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> engineRule.getRuntimeService().removeVariable(processInstanceId, VARIABLE_1))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot update the process instance '"
          + processInstanceId +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void removeExecutionVariableWithDisabledTenantCheck() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    engineRule.getRuntimeService().removeVariable(processInstanceId, VARIABLE_1);

    // then
    assertEquals(1, engineRule.getRuntimeService().getVariables(processInstanceId).size());
  }
}
