package org.camunda.bpm.engine.test.api.multitenancy.cmd;

import static org.camunda.bpm.engine.variable.Variables.objectValue;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;
/**
 * 
 * @author Deivarayan Azhagappan
 *
 */
public class MultiTenancyExecutionVariableTest extends MultiTenancyCmdTest {
  
  // get execution variables
  @Test
  public void getExecutionVariableWithNoUserAndNonTenant() {

    // given
    startProcessInstanceByKey(PROCESS_KEY).getId();
    Task task = taskService.createTaskQuery().singleResult();
    runtimeService.setVariable(task.getExecutionId(), "testVariable", "test");
    
    //then
    assertEquals("test", runtimeService.getVariable(task.getExecutionId(), "testVariable"));

  }

  @Test
  public void getExecutionVariableWithUserAndNonTenant() {

    // given
    disableAuthorization();
    
    startProcessInstanceByKey(PROCESS_KEY).getId();
    Task task = taskService.createTaskQuery().singleResult();
    
    runtimeService.setVariable(task.getExecutionId(), "testVariable", "test");
    
    enableAuthorization();
    setUserContext();
    // then
    assertEquals("test", runtimeService.getVariable(task.getExecutionId(), "testVariable"));
    
  }
  
  @Test
  public void getExecutionVariableWithUserAndAuthenticatedTenant() {
    
    // given
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
      .startEvent()
      .userTask("task")
      .endEvent()
      .done();
    
    disableAuthorization();
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
    
    Task task = taskService.createTaskQuery().singleResult();
    
    // if
    runtimeService.setVariable(task.getExecutionId(), "testVariable", "test");
    enableAuthorization();
    createTenantAuthorization();
    
    // then
    assertEquals("test", runtimeService.getVariable(task.getExecutionId(), "testVariable"));
  }
  
  @Test
  public void getExecutionVariableWithUserAndNoAuthenticatedTenant() {
    
    // given
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
      .startEvent()
      .userTask("task")
      .endEvent()
      .done();
    
    disableAuthorization();
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
    
    Task task = taskService.createTaskQuery().singleResult();
    
    // if
    runtimeService.setVariable(task.getExecutionId(), "testVariable", "test");
    
    enableAuthorization();
    setUserContext();
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the process instance because it belongs to no authenticated tenant:");
    assertEquals("test", runtimeService.getVariable(task.getExecutionId(), "testVariable"));
    
  }
  
  @Test
  public void getExecutionVariableWithNoUserAndNoAuthenticatedTenant() {
    
    // given
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
      .startEvent()
      .userTask("task")
      .endEvent()
      .done();
    
    disableAuthorization();
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
    
    Task task = taskService.createTaskQuery().singleResult();
    
    // if
    runtimeService.setVariable(task.getExecutionId(), "testVariable", "test");
    
    enableAuthorization();
    
    // then
    assertEquals("test", runtimeService.getVariable(task.getExecutionId(), "testVariable"));
  }

  // get execution variable typed
  @Test
  public void getExecutionVariableTypedWithNoUserAndNonTenant() {
    
    // given
    startProcessInstanceByKey(PROCESS_KEY).getId();
    Task task = taskService.createTaskQuery().singleResult();
    
    // if
    ArrayList<String> serializableValue = getSerializedVariables();
    runtimeService.setVariable(task.getExecutionId(), "testVariable", objectValue(serializableValue).create());
    
    // then
    ObjectValue typedValue = runtimeService.getVariableTyped(task.getExecutionId(), "testVariable");
    assertEquals(serializableValue, typedValue.getValue());
  }
  
  @Test
  public void getExecutionVariableTypedWithUserAndNonTenant() {
    
    // given
    startProcessInstanceByKey(PROCESS_KEY).getId();
    Task task = taskService.createTaskQuery().singleResult();
    
    // if
    ArrayList<String> serializableValue = getSerializedVariables();
    runtimeService.setVariable(task.getExecutionId(), "testVariable", objectValue(serializableValue).create());

    setUserContext();
    // then
    ObjectValue typedValue = runtimeService.getVariableTyped(task.getExecutionId(), "testVariable");
    assertEquals(serializableValue, typedValue.getValue());

  }
  
  @Test
  public void getExecutionVariableTypedWithUserAndAuthenticatedTenant() {
    
    // given
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
      .startEvent()
      .userTask("task")
      .endEvent()
      .done();
    
    disableAuthorization();
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
    Task task = taskService.createTaskQuery().singleResult();
    
    // if
    ArrayList<String> serializableValue = getSerializedVariables();
    runtimeService.setVariable(task.getExecutionId(), "testVariable", objectValue(serializableValue).create());
    
    createTenantAuthorization();
    // then
    ObjectValue typedValue = runtimeService.getVariableTyped(task.getExecutionId(), "testVariable");
    assertEquals(serializableValue, typedValue.getValue());
  }
  
  @Test
  public void getExecutionVariableTypedWithUserAndNoAuthenticatedTenant() {
    
    // given
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
      .startEvent()
      .userTask("task")
      .endEvent()
      .done();
    
    disableAuthorization();
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
    
    Task task = taskService.createTaskQuery().singleResult();
 
    // if
    ArrayList<String> serializableValue = getSerializedVariables();
    runtimeService.setVariable(task.getExecutionId(), "testVariable", objectValue(serializableValue).create());
    
    setUserContext();

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the process instance because it belongs to no authenticated tenant:");
    ObjectValue typedValue = runtimeService.getVariableTyped(task.getExecutionId(), "testVariable");
    assertEquals(serializableValue, typedValue.getValue());
  }
  
  @Test
  public void getExecutionVariableTypedWithNoUserAndNoAuthenticatedTenant() {
    // given
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
      .startEvent()
      .userTask("task")
      .endEvent()
      .done();
    
    disableAuthorization();
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
    
    Task task = taskService.createTaskQuery().singleResult();

    // if
    ArrayList<String> serializableValue = getSerializedVariables();
    runtimeService.setVariable(task.getExecutionId(), "testVariable", objectValue(serializableValue).create());

    // then
    ObjectValue typedValue = runtimeService.getVariableTyped(task.getExecutionId(), "testVariable");
    assertEquals(serializableValue, typedValue.getValue());
  }

  // get execution variables
  @Test
  public void getExecutionVariablesWithNoUserAndNonTenant() {

    // given
    startProcessInstanceByKey(PROCESS_KEY).getId();
    Task task = taskService.createTaskQuery().singleResult();
    
    // if
    runtimeService.setVariable(task.getExecutionId(), "testVariable1", "test");
    runtimeService.setVariable(task.getExecutionId(), "testVariable2", "test");
    
    // then
    Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
    assertEquals(2, variables.size());
    
    taskService.deleteTask(task.getExecutionId(), true);
  }
  
  @Test
  public void getExecutionVariablesWithUserAndNonTenant() {
    
    // given
    disableAuthorization();
    startProcessInstanceByKey(PROCESS_KEY).getId();
    Task task = taskService.createTaskQuery().singleResult();
    
    // if
    runtimeService.setVariable(task.getExecutionId(), "testVariable1", "test");
    runtimeService.setVariable(task.getExecutionId(), "testVariable2", "test");
    
    enableAuthorization();
   
    setUserContext();
    // then
    Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
    assertEquals(2, variables.size());
    
    taskService.deleteTask(task.getExecutionId(), true);
  }
  
  @Test
  public void getExecutionVariablesWithUserAndAuthenticatedTenant() {
    
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
      .startEvent()
      .userTask("task")
      .endEvent()
      .done();
    
    disableAuthorization();
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
    
    Task task = taskService.createTaskQuery().singleResult();
    
    runtimeService.setVariable(task.getExecutionId(), "testVariable1", "test");
    runtimeService.setVariable(task.getExecutionId(), "testVariable2", "test");
    
    createTenantAuthorization();

    Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
    assertEquals(2, variables.size());
  }
  
  @Test
  public void getExecutionVariablesWithUserAndNoAuthenticatedTenant() {
    
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
      .startEvent()
      .userTask("task")
      .endEvent()
      .done();
    
    disableAuthorization();
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
    
    Task task = taskService.createTaskQuery().singleResult();
    
    runtimeService.setVariable(task.getExecutionId(), "testVariable1", "test");
    runtimeService.setVariable(task.getExecutionId(), "testVariable2", "test");
    
    enableAuthorization();
    setUserContext();

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the process instance because it belongs to no authenticated tenant:");
    runtimeService.getVariables(task.getExecutionId());
    
  }
  
  @Test
  public void getExecutionVariablesWithNoUserAndNoAuthenticatedTenant() {
    
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
      .startEvent()
      .userTask("task")
      .endEvent()
      .done();
    
    disableAuthorization();
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
    
    Task task = taskService.createTaskQuery().singleResult();
    
    runtimeService.setVariable(task.getExecutionId(), "testVariable1", "test");
    runtimeService.setVariable(task.getExecutionId(), "testVariable2", "test");
    
    enableAuthorization();
    
    Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
    assertEquals(2, variables.size());
  }

  // set variable test
  @Test
  public void setExecutionVariableWithNoUserAndNonTenant() {
    
    startProcessInstanceByKey(PROCESS_KEY).getId();
    Task task = taskService.createTaskQuery().singleResult();
    
    runtimeService.setVariable(task.getExecutionId(), "testVariable", "test");
  }
  
  @Test
  public void setExecutionVariableWithUserAndNonTenant() {
    
    setUserContext();
    disableAuthorization();
    
    startProcessInstanceByKey(PROCESS_KEY).getId();
    Task task = taskService.createTaskQuery().singleResult();
    
    enableAuthorization();
    runtimeService.setVariable(task.getExecutionId(), "testVariable", "test");
    
  }
  
  @Test
  public void setExecutionVariableWithUserAndAuthenticatedTenant() {
    
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
    
    Task task = taskService.createTaskQuery().singleResult();
    enableAuthorization();
    runtimeService.setVariable(task.getExecutionId(), "testVariable", "test");
  }
  
  @Test
  public void setExecutionVariableWithUserAndNoAuthenticatedTenant() {
    
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
      .startEvent()
      .userTask("task")
      .endEvent()
      .done();
    
    disableAuthorization();
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
    Task task = taskService.createTaskQuery().singleResult();
    
    setUserContext();
    
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process instance because it belongs to no authenticated tenant:");
    runtimeService.setVariable(task.getExecutionId(), "testVariable", "test");
  }
  
  @Test
  public void setExecutionVariableWithNoUserAndNoAuthenticatedTenant() {
    
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
      .startEvent()
      .userTask("task")
      .endEvent()
      .done();
    
    disableAuthorization();
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
    
    Task task = taskService.createTaskQuery().singleResult();
    enableAuthorization();
    runtimeService.setVariable(task.getExecutionId(), "testVariable", "test");
  }

  // remove variable test
  @Test
  public void removeExecutionVariableWithNoUserAndNonTenant() {
    
    startProcessInstanceByKey(PROCESS_KEY).getId();
    Task task = taskService.createTaskQuery().singleResult();
    
    runtimeService.setVariable(task.getExecutionId(), "testVariable", "test");
    
    runtimeService.removeVariable(task.getExecutionId(), "testVariable");
  }
  
  @Test
  public void removeExecutionVariableWithUserAndNonTenant() {
    
    startProcessInstanceByKey(PROCESS_KEY).getId();
    Task task = taskService.createTaskQuery().singleResult();

    enableAuthorization();
    runtimeService.setVariable(task.getExecutionId(), "testVariable", "test");
   
    setUserContext();
    runtimeService.removeVariable(task.getExecutionId(), "testVariable");
  }
  
  @Test
  public void removeExecutionVariableWithUserAndAuthenticatedTenant() {
    
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
      .startEvent()
      .userTask("task")
      .endEvent()
      .done();
    
    disableAuthorization();
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
    Task task = taskService.createTaskQuery().singleResult();

    enableAuthorization();
    createTenantAuthorization();
    runtimeService.setVariable(task.getExecutionId(), "testVariable", "test");
    
    runtimeService.removeVariable(task.getExecutionId(), "testVariable");
  }
  
  @Test
  public void removeExecutionVariableWithUserAndNoAuthenticatedTenant() {
    
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
      .startEvent()
      .userTask("task")
      .endEvent()
      .done();
    
    disableAuthorization();
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
    
    Task task = taskService.createTaskQuery().singleResult();
    runtimeService.setVariable(task.getExecutionId(), "testVariable", "test");
    
    enableAuthorization();
    setUserContext();

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process instance because it belongs to no authenticated tenant:");
    runtimeService.removeVariable(task.getExecutionId(), "testVariable");
  }
  
  @Test
  public void removeExecutionVariableWithNoUserAndNoAuthenticatedTenant() {
    
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
      .startEvent()
      .userTask("task")
      .endEvent()
      .done();
    
    disableAuthorization();
    
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
    
    Task task = taskService.createTaskQuery().singleResult();
    runtimeService.setVariable(task.getExecutionId(), "testVariable", "test");
    
    enableAuthorization();
    runtimeService.removeVariable(task.getExecutionId(), "testVariable");
  }

  public ArrayList<String> getSerializedVariables() {
    
    ArrayList<String> serializableValue = new ArrayList<String>();
    serializableValue.add("1");
    serializableValue.add("2");
    return serializableValue;
  }
}
