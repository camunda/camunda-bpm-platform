package org.camunda.bpm.engine.test.api.multitenancy.cmd;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;

/**
 * 
 * @author Deivarayan Azhagappan
 *
 */
public class MultiTenancyActivityInstanceTest extends MultiTenancyCmdTest{

  @Test
  public void getActivityInstanceNoUserAndNonTenant() {
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    
    // when
    runtimeService.getActivityInstance(processInstanceId);
  }

  @Test
  public void getActivityInstanceUserAndNonTenant() {
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    setUserContext();
    // when
    runtimeService.getActivityInstance(processInstanceId);
  }

  @Test
  public void getActivityInstanceForUserAndAuthenticatedTenant() {

    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
    .startEvent()
    .userTask("task")
    .endEvent()
    .done();

    disableAuthorization();
    setUserContext();
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    createTenantAuthorization();
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
 
    String processInstanceId = startProcessInstanceByKey(multiTenancyProcessDefinitionKey).getId();
 
    enableAuthorization();
    
    runtimeService.getActivityInstance(processInstanceId);
  }

  @Test
  public void getActivityInstanceWithNoUserAndAuthenticatedTenant() {
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
    .startEvent()
    .userTask("task")
    .endEvent()
    .done();

    disableAuthorization();
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    createTenantAuthorization();
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
 
    String processInstanceId = startProcessInstanceByKey(multiTenancyProcessDefinitionKey).getId();
 
    enableAuthorization();
    
    runtimeService.getActivityInstance(processInstanceId);
  }

  @Test
  public void getActivityInstanceForUserAndNoAuthenticatedTenant() {

    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
    .startEvent()
    .userTask("task")
    .endEvent()
    .done();

    disableAuthorization();
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
 
    String processInstanceId = startProcessInstanceByKey(multiTenancyProcessDefinitionKey).getId();
 
    setUserContext();

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the process instance because it belongs to no authenticated tenant:");
    runtimeService.getActivityInstance(processInstanceId);
  }

  
  @Test
  public void getActivityInstanceWithNoUserAndNoAuthenticatedTenant() {

    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
    .startEvent()
    .userTask("task")
    .endEvent()
    .done();

    disableAuthorization();
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
 
    String processInstanceId = startProcessInstanceByKey(multiTenancyProcessDefinitionKey).getId();
 
    enableAuthorization();
    runtimeService.getActivityInstance(processInstanceId);
  }

}
