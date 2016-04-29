package org.camunda.bpm.engine.test.api.multitenancy.cmd;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.Test;

/**
 * 
 * @author Deivarayan Azhagappan
 *
 */

public class MultiTenancyFormVariables extends MultiTenancyCmdTest{

  // start form variables
  @Test
  public void testGetStartFormVariablesWithNoUserAndNoTenants() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().list().get(1);

    VariableMap variables = formService.getStartFormVariables(processDefinition.getId());
    assertEquals(4, variables.size());
  }

  @Test
  public void testGetStartFormVariablesWithUserAndNoTenants() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().list().get(1);

    setUserContext();
    
    VariableMap variables = formService.getStartFormVariables(processDefinition.getId());
    assertEquals(4, variables.size());

  }

  @Test
  public void testGetStartFormVariablesWithUserAndAuthenticatedTenant() {

    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, START_FORM_RESOURCE);
    ProcessInstance instance = startProcessInstanceForTenant(TENANT_ONE, "testProcess");
    
    createTenantAuthorization();
    VariableMap variables = formService.getStartFormVariables(instance.getProcessDefinitionId());
    assertEquals(4, variables.size());

  }

  @Test
  public void testGetStartFormVariablesWithUserAndNoAuthenticatedTenant() {

    disableAuthorization();
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, START_FORM_RESOURCE);
    ProcessInstance instance = startProcessInstanceForTenant(TENANT_ONE, "testProcess");
    
    setUserContext();
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the process definition because it belongs to no authenticated tenant:");
    VariableMap variables = formService.getStartFormVariables(instance.getProcessDefinitionId());
    assertEquals(4, variables.size());

  }

  @Test
  public void testGetStartFormVariablesWithNoUserAndNoAuthenticatedTenant() {

    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, START_FORM_RESOURCE);
    ProcessInstance instance = startProcessInstanceForTenant(TENANT_ONE, "testProcess");
    
    VariableMap variables = formService.getStartFormVariables(instance.getProcessDefinitionId());
    assertEquals(4, variables.size());

  }

  // task form variables
  @Test
  public void testGetTaskFormVariablesWithNoUserAndNoTenants() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().list().get(1);

    startProcessInstanceByKey(processDefinition.getKey(),getTaskFormVariables());
    
    Task task = taskService.createTaskQuery().singleResult();
    
    VariableMap variables = formService.getTaskFormVariables(task.getId());
    assertEquals(4, variables.size());
  }

  @Test
  public void testGetTaskFormVariablesWithUserAndNoTenants() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().list().get(1);

    startProcessInstanceByKey(processDefinition.getKey(), getTaskFormVariables());
    
    Task task = taskService.createTaskQuery().singleResult();
    
    setUserContext();
    VariableMap variables = formService.getTaskFormVariables(task.getId());
    assertEquals(4, variables.size());

  }

  @Test
  public void testGetTaskFormVariablesWithUserAndAuthenticatedTenant() {

    testRule.deployForTenant(TENANT_ONE, START_FORM_RESOURCE);
    startProcessInstanceForTenant(TENANT_ONE, getTaskFormVariables(), "testProcess");
    
    Task task = taskService.createTaskQuery().singleResult();
    
    createTenantAuthorization();
    VariableMap variables = formService.getTaskFormVariables(task.getId());
    assertEquals(4, variables.size());

  }

  @Test
  public void testGetTaskFormVariablesWithUserAndNoAuthenticatedTenant() {

    testRule.deployForTenant(TENANT_ONE, START_FORM_RESOURCE);
    
    startProcessInstanceForTenant(TENANT_ONE, getTaskFormVariables(), "testProcess");
    
    Task task = taskService.createTaskQuery().singleResult();
    
    setUserContext();
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the task because it belongs to no authenticated tenant:");
    formService.getTaskFormVariables(task.getId());
  }

  @Test
  public void testGetTaskFormVariablesWithNoUserAndNoAuthenticatedTenant() {

    repositoryService.createProcessDefinitionQuery().list().get(1);

    testRule.deployForTenant(TENANT_ONE, START_FORM_RESOURCE);
    
    startProcessInstanceForTenant(TENANT_ONE, getTaskFormVariables(), "testProcess");
    
    Task task = taskService.createTaskQuery().singleResult();
    formService.getTaskFormVariables(task.getId());
  }

  public Map<String, Object> getTaskFormVariables() {
    
    Map<String, Object> processVars = new HashMap<String, Object>();
    processVars.put("someString", "initialValue");
    processVars.put("initialBooleanVariable", true);
    processVars.put("initialLongVariable", 1l); 
    processVars.put("serializable", Arrays.asList("a", "b", "c"));
    
    return processVars;
  }
 
}
