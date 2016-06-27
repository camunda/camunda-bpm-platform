package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

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
import org.junit.rules.ExpectedException;
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

  @Rule
  public ExpectedException thrown= ExpectedException.none();

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
       
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the process instance '"
      + processInstanceId +"' because it belongs to no authenticated tenant.");
    engineRule.getRuntimeService().getVariable(processInstanceId, VARIABLE_1);
    
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

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the process instance '"
      + processInstanceId +"' because it belongs to no authenticated tenant.");
    // then
    engineRule.getRuntimeService().getVariableTyped(processInstanceId, VARIABLE_1);
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
       
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the process instance '"
      + processInstanceId +"' because it belongs to no authenticated tenant.");
    engineRule.getRuntimeService().getVariables(processInstanceId).size();
    
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
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process instance '"
      + processInstanceId +"' because it belongs to no authenticated tenant.");
    engineRule.getRuntimeService().setVariable(processInstanceId, "newVariable", "newValue");
    
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
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process instance '"
      + processInstanceId +"' because it belongs to no authenticated tenant.");
    engineRule.getRuntimeService().removeVariable(processInstanceId, VARIABLE_1);
    
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
