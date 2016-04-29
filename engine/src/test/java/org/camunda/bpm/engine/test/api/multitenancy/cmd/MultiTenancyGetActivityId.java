package org.camunda.bpm.engine.test.api.multitenancy.cmd;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;

/**
 * 
 * @author Deivarayan Azhagappan
 *
 */

public class MultiTenancyGetActivityId extends MultiTenancyCmdTest {
  
  @Test
  public void getActiveActivityIdsWithNoUserAndNonTenant() {
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    
    // when
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
    assertEquals(1, activeActivityIds.size());
  }

  @Test
  public void getActiveActivityIdsWithUserAndNonTenant() {
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    setUserContext();
    // when
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
    assertEquals(1, activeActivityIds.size());
  }

  @Test
  public void getActivityInstanceForUserAndAuthenticatedTenant() {

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
    createTenantAuthorization();

    // when
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
    assertEquals(1, activeActivityIds.size());
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
    runtimeService.getActiveActivityIds(processInstanceId);
  }

  @Test
  public void getActivityInstanceForNoUserAndAuthenticatedTenant() {

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
    createTenantAuthorization();

    // when
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
    assertEquals(1, activeActivityIds.size());
  }

  @Test
  public void getActivityInstanceForNoUserAndNoAuthenticatedTenant() {

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
    
    // when
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
    assertEquals(1, activeActivityIds.size());
  }

}
