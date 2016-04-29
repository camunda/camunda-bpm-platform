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
public class MultiTenancyProcessInstanceTest extends MultiTenancyCmdTest{

  @Test
  public void deleteProcessInstanceNoUserAndNonTenant() {
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    
    // when
    runtimeService.deleteProcessInstance(processInstanceId, null);

    // then
    disableAuthorization();
    assertProcessEnded(processInstanceId);
    enableAuthorization();
  }

  @Test
  public void deleteProcessInstanceUserAndNonTenant() {
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    setUserContext();
    // when
    runtimeService.deleteProcessInstance(processInstanceId, null);

    // then
    disableAuthorization();
    assertProcessEnded(processInstanceId);
    enableAuthorization();
  }

  @Test
  public void deleteProcessInstanceForUserAndAuthenticatedTenant() {

    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
    .startEvent()
    .userTask("task")
    .endEvent()
    .done();

    disableAuthorization();
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    String processInstanceId = startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey).getId();
 
    enableAuthorization();
    createTenantAuthorization();
    
    runtimeService.deleteProcessInstance(processInstanceId, null);
  }

  @Test
  public void deleteInstanceWithNoUserAndAuthenticatedTenant() {
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
    .startEvent()
    .userTask("task")
    .endEvent()
    .done();

    disableAuthorization();
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    createTenantAuthorization();
    String processInstanceId = startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey).getId();
 
    enableAuthorization();
    
    runtimeService.deleteProcessInstance(processInstanceId, null);
  }

  @Test
  public void deleteInstanceWithUserAndNoAuthenticatedTenant() {

    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
    .startEvent()
    .userTask("task")
    .endEvent()
    .done();

    disableAuthorization();
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);

    String processInstanceId = startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey).getId();
 
    enableAuthorization();
    setUserContext();

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot delete the process instance because it belongs to no authenticated tenant: ");
    runtimeService.deleteProcessInstance(processInstanceId, null);
  }

  @Test
  public void deleteInstanceWithNoUserAndNoAuthenticatedTenant() {

    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
    .startEvent()
    .userTask("task")
    .endEvent()
    .done();

    disableAuthorization();
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);

    String processInstanceId = startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey).getId();
 
    enableAuthorization();
    
    runtimeService.deleteProcessInstance(processInstanceId, null);
  }

  // modify instances
  @Test
  public void modifyProcessInstanceNoUserForNonTenant() {
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    
    // when
    runtimeService
    .createProcessInstanceModification(processInstanceId)
    .cancelAllForActivity("theTask")
    .execute();

    // then
    disableAuthorization();
    assertProcessEnded(processInstanceId);
    enableAuthorization();
  }

  @Test
  public void modifyProcessInstanceWithUserForNonTenant() {
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    setUserContext();
    // when
    runtimeService
    .createProcessInstanceModification(processInstanceId)
    .cancelAllForActivity("theTask")
    .execute();

    // then
    disableAuthorization();
    assertProcessEnded(processInstanceId);
    enableAuthorization();
  }

  @Test
  public void modifyProcessInstanceWithUserAndAuthenticatedTenant() {

    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
    .startEvent()
    .userTask("task")
    .endEvent()
    .done();

    disableAuthorization();
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    String processInstanceId = startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey).getId();
    
    createTenantAuthorization();
    // when
    runtimeService
    .createProcessInstanceModification(processInstanceId)
    .cancelAllForActivity("task")
    .execute();
  }

  @Test
  public void modifyProcessInstanceWithUserAndNoAuthenticatedTenant() {

    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
    .startEvent()
    .userTask("task")
    .endEvent()
    .done();

    disableAuthorization();
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    String processInstanceId = startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey).getId();
 
    setUserContext();
    
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the process instance because it belongs to no authenticated tenant:");
    // when
    runtimeService
    .createProcessInstanceModification(processInstanceId)
    .cancelAllForActivity("task")
    .execute();
  }

  @Test
  public void modifyProcessInstanceWithNoUserAndAuthenticatedTenant() {

    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
    .startEvent()
    .userTask("task")
    .endEvent()
    .done();

    disableAuthorization();
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    String processInstanceId = startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey).getId();
    createTenantAuthorization();
    
    // when
    runtimeService
    .createProcessInstanceModification(processInstanceId)
    .cancelAllForActivity("task")
    .execute();
  }

  @Test
  public void modifyProcessInstanceWithNoUserAndNoAuthenticatedTenant() {

    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
    .startEvent()
    .userTask("task")
    .endEvent()
    .done();

    disableAuthorization();
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    String processInstanceId = startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey).getId();
 
    // when
    runtimeService
    .createProcessInstanceModification(processInstanceId)
    .cancelAllForActivity("task")
    .execute();
  }
}
