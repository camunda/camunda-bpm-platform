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

public class MultiTenancyTaskVariableCmdsTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";

  protected static final String VARIABLE_1 = "testVariable1";
  protected static final String VARIABLE_2 = "testVariable2";

  protected static final String VARIABLE_VALUE_1 = "test1";
  protected static final String VARIABLE_VALUE_2 = "test2";

  protected static final String PROCESS_DEFINITION_KEY = "oneTaskProcess";

  protected static final BpmnModelInstance ONE_TASK_PROCESS = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
    .startEvent()
    .userTask("task")
    .endEvent()
    .done();

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected String taskId;

  @Before
  public void init() {

    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, ONE_TASK_PROCESS);

    engineRule.getRuntimeService()
    .startProcessInstanceByKey(PROCESS_DEFINITION_KEY,
         Variables.createVariables()
         .putValue(VARIABLE_1, VARIABLE_VALUE_1)
         .putValue(VARIABLE_2, VARIABLE_VALUE_2))
    .getId();

    taskId = engineRule.getTaskService().createTaskQuery().singleResult().getId();

  }

  // get task variable
  @Test
  public void getTaskVariableWithAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    assertEquals(VARIABLE_VALUE_1, engineRule.getTaskService().getVariable(taskId, VARIABLE_1));
  }

  @Test
  public void getTaskVariableWithNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> engineRule.getTaskService().getVariable(taskId, VARIABLE_1))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot read the task '"
          + taskId +"' because it belongs to no authenticated tenant.");

  }


  @Test
  public void getTaskVariableWithDisabledTenantCheck() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // then
    assertEquals(VARIABLE_VALUE_1, engineRule.getTaskService().getVariable(taskId, VARIABLE_1));
  }

  // get task variable typed
  @Test
  public void getTaskVariableTypedWithAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    // then
    assertEquals(VARIABLE_VALUE_1, engineRule.getTaskService().getVariableTyped(taskId, VARIABLE_1).getValue());

  }

  @Test
  public void getTaskVariableTypedWithNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> engineRule.getTaskService().getVariableTyped(taskId, VARIABLE_1).getValue())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot read the task '"
          + taskId +"' because it belongs to no authenticated tenant.");
  }

  @Test
  public void getTaskVariableTypedWithDisableTenantCheck() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // then
    assertEquals(VARIABLE_VALUE_1, engineRule.getTaskService().getVariableTyped(taskId, VARIABLE_1).getValue());
  }

  // get task variables
  @Test
  public void getTaskVariablesWithAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    // then
    assertEquals(2, engineRule.getTaskService().getVariables(taskId).size());
  }

  @Test
  public void getTaskVariablesWithNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> engineRule.getTaskService().getVariables(taskId).size())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot read the task '"
          + taskId +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void getTaskVariablesWithDisabledTenantCheck() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    assertEquals(2, engineRule.getTaskService().getVariables(taskId).size());
  }


  // set variable test
  @Test
  public void setTaskVariableWithAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    engineRule.getTaskService().setVariable(taskId, "newVariable", "newValue");

    assertEquals(3, engineRule.getTaskService().getVariables(taskId).size());
  }

  @Test
  public void setTaskVariableWithNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> engineRule.getTaskService().setVariable(taskId, "newVariable", "newValue"))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot update the task '"
          + taskId +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void setTaskVariableWithDisabledTenantCheck() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    engineRule.getTaskService().setVariable(taskId, "newVariable", "newValue");
    assertEquals(3, engineRule.getTaskService().getVariables(taskId).size());

  }

  // remove variable test
  @Test
  public void removeTaskVariableWithAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    engineRule.getTaskService().removeVariable(taskId, VARIABLE_1);
    // then
    assertEquals(1, engineRule.getTaskService().getVariables(taskId).size());
  }

  @Test
  public void removeTaskVariablesWithNoAuthenticatedTenant() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> engineRule.getTaskService().removeVariable(taskId, VARIABLE_1))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot update the task '"
          + taskId +"' because it belongs to no authenticated tenant.");
  }

  @Test
  public void removeTaskVariablesWithDisabledTenantCheck() {

    engineRule.getIdentityService().setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // then
    engineRule.getTaskService().removeVariable(taskId, VARIABLE_1);
    assertEquals(1, engineRule.getTaskService().getVariables(taskId).size());
  }

}
