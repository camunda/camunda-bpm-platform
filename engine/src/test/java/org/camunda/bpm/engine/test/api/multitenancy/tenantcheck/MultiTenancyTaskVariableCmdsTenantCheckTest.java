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

  @Rule
  public ExpectedException thrown= ExpectedException.none();

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

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the task '"
      + taskId +"' because it belongs to no authenticated tenant.");
    engineRule.getTaskService().getVariable(taskId, VARIABLE_1);
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
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the task '"
      + taskId +"' because it belongs to no authenticated tenant.");
    engineRule.getTaskService().getVariableTyped(taskId, VARIABLE_1).getValue();
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
       
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the task '"
      + taskId +"' because it belongs to no authenticated tenant.");
    engineRule.getTaskService().getVariables(taskId).size();
    
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

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the task '"
      + taskId +"' because it belongs to no authenticated tenant.");
    engineRule.getTaskService().setVariable(taskId, "newVariable", "newValue");
    
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
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the task '"
      + taskId +"' because it belongs to no authenticated tenant.");

    engineRule.getTaskService().removeVariable(taskId, VARIABLE_1);
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
