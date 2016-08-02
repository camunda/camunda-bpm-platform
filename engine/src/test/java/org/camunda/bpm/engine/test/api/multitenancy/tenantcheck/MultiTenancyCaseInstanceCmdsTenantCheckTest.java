/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Map;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstanceBuilder;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

/**
 * @author kristin.polenz
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class MultiTenancyCaseInstanceCmdsTenantCheckTest {

  protected static final String VARIABLE_NAME = "myVar";
  protected static final String VARIABLE_VALUE = "myValue";

  protected static final String TENANT_ONE = "tenant1";

  protected static final String CMMN_MODEL = "org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn";

  protected static final String ACTIVITY_ID = "PI_HumanTask_1";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown= ExpectedException.none();

  protected IdentityService identityService;
  protected CaseService caseService;
  protected HistoryService historyService;
  protected ProcessEngineConfiguration processEngineConfiguration;

  protected String caseInstanceId;
  protected String caseExecutionId;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    identityService = engineRule.getIdentityService();
    caseService = engineRule.getCaseService();
    historyService = engineRule.getHistoryService();

    testRule.deployForTenant(TENANT_ONE, CMMN_MODEL);

    caseInstanceId = createCaseInstance(null);

    caseExecutionId = getCaseExecution().getId();
  }

  @Test
  public void manuallyStartCaseExecutionNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the case execution");

    caseService.manuallyStartCaseExecution(caseExecutionId);
  }

  @Test
  public void manuallyStartCaseExecutionWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    caseService.manuallyStartCaseExecution(caseExecutionId);

    identityService.clearAuthentication();

    CaseExecution caseExecution = getCaseExecution();

    assertThat(caseExecution.isActive(), is(true));
  }

  @Test
  public void manuallyStartCaseExecutionDisabledTenantCheck() {
    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    caseService.manuallyStartCaseExecution(caseExecutionId);

    identityService.clearAuthentication();

    CaseExecution caseExecution = getCaseExecution();

    assertThat(caseExecution.isActive(), is(true));
  }

  @Test
  public void disableCaseExecutionNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the case execution");

    caseService.disableCaseExecution(caseExecutionId);
  }

  @Test
  public void disableCaseExecutionWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    caseService.disableCaseExecution(caseExecutionId);

    identityService.clearAuthentication();

    HistoricCaseActivityInstance historicCaseActivityInstance = getHistoricCaseActivityInstance();

    assertThat(historicCaseActivityInstance, notNullValue());
    assertThat(historicCaseActivityInstance.isDisabled(), is(true));
  }

  @Test
  public void disableCaseExecutionDisabledTenantCheck() {
    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    caseService.disableCaseExecution(caseExecutionId);

    identityService.clearAuthentication();

    HistoricCaseActivityInstance historicCaseActivityInstance = getHistoricCaseActivityInstance();

    assertThat(historicCaseActivityInstance, notNullValue());
    assertThat(historicCaseActivityInstance.isDisabled(), is(true));
  }

  @Test
  public void reenableCaseExecutionNoAuthenticatedTenants() {
    caseService.disableCaseExecution(caseExecutionId);

    identityService.setAuthentication("user", null, null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the case execution");

    caseService.reenableCaseExecution(caseExecutionId);
  }

  @Test
  public void reenableCaseExecutionWithAuthenticatedTenant() {
    caseService.disableCaseExecution(caseExecutionId);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    caseService.reenableCaseExecution(caseExecutionId);

    identityService.clearAuthentication();

    CaseExecution caseExecution = getCaseExecution();

    assertThat(caseExecution.isEnabled(), is(true));
  }

  @Test
  public void reenableCaseExecutionDisabledTenantCheck() {
    caseService.disableCaseExecution(caseExecutionId);

    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    caseService.reenableCaseExecution(caseExecutionId);

    identityService.clearAuthentication();

    CaseExecution caseExecution = getCaseExecution();

    assertThat(caseExecution.isEnabled(), is(true));
  }

  @Test
  public void completeCaseExecutionNoAuthenticatedTenants() {
    caseService.manuallyStartCaseExecution(caseExecutionId);

    identityService.setAuthentication("user", null, null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the case execution");

    caseService.completeCaseExecution(caseExecutionId);
  }

  @Test
  public void completeCaseExecutionWithAuthenticatedTenant() {
    caseService.manuallyStartCaseExecution(caseExecutionId);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    caseService.completeCaseExecution(caseExecutionId);

    identityService.clearAuthentication();

    HistoricCaseActivityInstance historicCaseActivityInstance = getHistoricCaseActivityInstance();

    assertThat(historicCaseActivityInstance, notNullValue());
    assertThat(historicCaseActivityInstance.isCompleted(), is(true));
  }

  @Test
  public void completeCaseExecutionDisabledTenantCheck() {
    caseService.manuallyStartCaseExecution(caseExecutionId);

    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    caseService.completeCaseExecution(caseExecutionId);

    identityService.clearAuthentication();

    HistoricCaseActivityInstance historicCaseActivityInstance = getHistoricCaseActivityInstance();

    assertThat(historicCaseActivityInstance, notNullValue());
    assertThat(historicCaseActivityInstance.isCompleted(), is(true));
  }

  @Test
  public void closeCaseInstanceNoAuthenticatedTenants() {
    caseService.completeCaseExecution(caseInstanceId);

    identityService.setAuthentication("user", null, null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the case execution");

    caseService.closeCaseInstance(caseInstanceId);
  }

  @Test
  public void closeCaseInstanceWithAuthenticatedTenant() {
    caseService.completeCaseExecution(caseInstanceId);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    caseService.closeCaseInstance(caseInstanceId);

    identityService.clearAuthentication();

    HistoricCaseInstance historicCaseInstance = getHistoricCaseInstance();

    assertThat(historicCaseInstance, notNullValue());
    assertThat(historicCaseInstance.isClosed(), is(true));
  }

  @Test
  public void closeCaseInstanceDisabledTenantCheck() {
    caseService.completeCaseExecution(caseInstanceId);

    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    caseService.closeCaseInstance(caseInstanceId);

    identityService.clearAuthentication();

    HistoricCaseInstance historicCaseInstance = getHistoricCaseInstance();

    assertThat(historicCaseInstance, notNullValue());
    assertThat(historicCaseInstance.isClosed(), is(true));
  }

  @Test
  public void terminateCaseInstanceNoAuthenticatedTenants() {
    
    identityService.setAuthentication("user", null, null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the case execution");

    caseService.terminateCaseExecution(caseInstanceId);
  }

  @Test
  public void terminateCaseExecutionWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    caseService.terminateCaseExecution(caseInstanceId);

    HistoricCaseInstance historicCaseInstance = getHistoricCaseInstance();

    assertThat(historicCaseInstance, notNullValue());
    assertThat(historicCaseInstance.isTerminated(), is(true));

  }

  @Test
  public void terminateCaseExecutionDisabledTenantCheck() {
    
    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    caseService.terminateCaseExecution(caseInstanceId);
    
    HistoricCaseInstance historicCaseInstance = getHistoricCaseInstance();

    assertThat(historicCaseInstance, notNullValue());
    assertThat(historicCaseInstance.isTerminated(), is(true));
  }
  
  @Test
  public void getVariablesNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the case execution");

    caseService.getVariables(caseExecutionId);
  }

  @Test
  public void getVariablesWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    Map<String, Object> variables = caseService.getVariables(caseExecutionId);

    assertThat(variables, notNullValue());
    assertThat(variables.keySet(), hasItem(VARIABLE_NAME));
  }

  @Test
  public void getVariablesDisabledTenantCheck() {
    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    Map<String, Object> variables = caseService.getVariables(caseExecutionId);

    assertThat(variables, notNullValue());
    assertThat(variables.keySet(), hasItem(VARIABLE_NAME));
  }

  @Test
  public void getVariableNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the case execution");

    caseService.getVariable(caseExecutionId, VARIABLE_NAME);
  }

  @Test
  public void getVariableWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    String variableValue = (String) caseService.getVariable(caseExecutionId, VARIABLE_NAME);

    assertThat(variableValue, is(VARIABLE_VALUE));
  }

  @Test
  public void getVariableDisabledTenantCheck() {
    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    String variableValue = (String) caseService.getVariable(caseExecutionId, VARIABLE_NAME);

    assertThat(variableValue, is(VARIABLE_VALUE));
  }

  @Test
  public void getVariableTypedNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the case execution");

    caseService.getVariableTyped(caseExecutionId, VARIABLE_NAME);
  }

  @Test
  public void getVariableTypedWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    StringValue variable = caseService.getVariableTyped(caseExecutionId, VARIABLE_NAME);

    assertThat(variable.getValue(), is(VARIABLE_VALUE));
  }

  @Test
  public void getVariableTypedDisabledTenantCheck() {
    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    StringValue variable = caseService.getVariableTyped(caseExecutionId, VARIABLE_NAME);

    assertThat(variable.getValue(), is(VARIABLE_VALUE));
  }

  @Test
  public void removeVariablesNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the case execution");

    caseService.removeVariable(caseExecutionId, VARIABLE_NAME);
  }

  @Test
  public void removeVariablesWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    caseService.removeVariable(caseExecutionId, VARIABLE_NAME);

    identityService.clearAuthentication();

    Map<String, Object> variables = caseService.getVariables(caseExecutionId);
    assertThat(variables.isEmpty(), is(true));
  }

  @Test
  public void removeVariablesDisabledTenantCheck() {
    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    caseService.removeVariable(caseExecutionId, VARIABLE_NAME);

    identityService.clearAuthentication();

    Map<String, Object> variables = caseService.getVariables(caseExecutionId);
    assertThat(variables.isEmpty(), is(true));
  }

  @Test
  public void setVariableNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the case execution");

    caseService.setVariable(caseExecutionId, "newVar", "newValue");
  }

  @Test
  public void setVariableWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    caseService.setVariable(caseExecutionId, "newVar", "newValue");

    identityService.clearAuthentication();

    Map<String, Object> variables = caseService.getVariables(caseExecutionId);
    assertThat(variables, notNullValue());
    assertThat(variables.keySet(), hasItems(VARIABLE_NAME, "newVar"));
  }

  @Test
  public void setVariableDisabledTenantCheck() {
    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    caseService.setVariable(caseExecutionId, "newVar", "newValue");

    identityService.clearAuthentication();

    Map<String, Object> variables = caseService.getVariables(caseExecutionId);
    assertThat(variables, notNullValue());
    assertThat(variables.keySet(), hasItems(VARIABLE_NAME, "newVar"));
  }

  protected String createCaseInstance(String tenantId) {
    VariableMap variables = Variables.putValue(VARIABLE_NAME, VARIABLE_VALUE);
    CaseInstanceBuilder builder = caseService.withCaseDefinitionByKey("twoTaskCase").setVariables(variables);
    if (tenantId == null) {
      return builder.create().getId();
    } else {
      return builder.caseDefinitionTenantId(tenantId).create().getId();
    }
  }

  protected CaseExecution getCaseExecution() {
    return caseService.createCaseExecutionQuery().activityId(ACTIVITY_ID).singleResult();
  }

  protected HistoricCaseActivityInstance getHistoricCaseActivityInstance() {
    return historyService.createHistoricCaseActivityInstanceQuery().caseActivityId(ACTIVITY_ID).singleResult();
  }

  protected HistoricCaseInstance getHistoricCaseInstance() {
    return historyService.createHistoricCaseInstanceQuery().caseInstanceId(caseInstanceId).singleResult();
  }

}
