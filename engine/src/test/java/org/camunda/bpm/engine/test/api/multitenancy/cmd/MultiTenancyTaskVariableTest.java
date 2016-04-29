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

public class MultiTenancyTaskVariableTest extends MultiTenancyCmdTest {
  
  // get task variables
  @Test
  public void getTaskVariableWithNoUserAndNonTenant() {
    
    Task task = taskService.newTask();
    task.setName("aTask");
    taskService.saveTask(task);
    
    String taskId = task.getId();
    taskService.setVariable(taskId, "testVariable", "test");
    assertEquals("test", taskService.getVariable(taskId, "testVariable"));
    
    taskService.deleteTask(taskId, true);
  }
  
  @Test
  public void getTaskVariableWithUserAndNonTenant() {
    
    disableAuthorization();
    Task task = taskService.newTask();
    task.setName("aTask");
    taskService.saveTask(task);
    
    String taskId = task.getId();
    taskService.setVariable(taskId, "testVariable", "test");
    
    enableAuthorization();
    setUserContext();

    assertEquals("test", taskService.getVariable(taskId, "testVariable"));
    
    taskService.deleteTask(taskId, true);
  }
  
  @Test
  public void getTaskVariableWithUserAndAuthenticatedTenant() {
    
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
    
    taskService.setVariable(task.getId(), "testVariable", "test");
    
    enableAuthorization();
    createTenantAuthorization();

    assertEquals("test", taskService.getVariable(task.getId(), "testVariable"));
  }
  
  @Test
  public void getTaskVariableWithUserAndNoAuthenticatedTenant() {
    
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
    
    taskService.setVariable(task.getId(), "testVariable", "test");
    
    setUserContext();
    
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the task because it belongs to no authenticated tenant:");
    assertEquals("test", taskService.getVariable(task.getId(), "testVariable"));
    
  }
  
  @Test
  public void getTaskVariableWithNoUserAndNoAuthenticatedTenant() {
    
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
    
    taskService.setVariable(task.getId(), "testVariable", "test");
    
    enableAuthorization();
    
    assertEquals("test", taskService.getVariable(task.getId(), "testVariable"));
  }
  
  // get task variable typed
  @Test
  public void getTaskVariableTypedWithNoUserAndNonTenant() {
    Task task = taskService.newTask();
    task.setName("aTask");
    taskService.saveTask(task);
    
    String taskId = task.getId();
    ArrayList<String> serializableValue = getSerializedVariables();
    taskService.setVariable(taskId, "testVariable", objectValue(serializableValue).create());
    
    ObjectValue typedValue = taskService.getVariableTyped(taskId, "testVariable");
    assertEquals(serializableValue, typedValue.getValue());
    
    taskService.deleteTask(taskId, true);
  }
  
  @Test
  public void getTaskVariableTypedWithUserAndNonTenant() {
    
    Task task = taskService.newTask();
    task.setName("aTask");
    taskService.saveTask(task);
    
    String taskId = task.getId();
    ArrayList<String> serializableValue = getSerializedVariables();
    taskService.setVariable(taskId, "testVariable", objectValue(serializableValue).create());

    setUserContext();
    ObjectValue typedValue = taskService.getVariableTyped(taskId, "testVariable");
    assertEquals(serializableValue, typedValue.getValue());
    
    taskService.deleteTask(taskId, true);
  }
  
  @Test
  public void getTaskVariableTypedWithUserAndAuthenticatedTenant() {
    
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
    
    ArrayList<String> serializableValue = getSerializedVariables();
    taskService.setVariable(task.getId(), "testVariable", objectValue(serializableValue).create());

    createTenantAuthorization();

    ObjectValue typedValue = taskService.getVariableTyped(task.getId(), "testVariable");
    assertEquals(serializableValue, typedValue.getValue());
  }
  
  @Test
  public void getTaskVariableTypedWithUserAndNoAuthenticatedTenant() {
    
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
    
    ArrayList<String> serializableValue = getSerializedVariables();
    taskService.setVariable(task.getId(), "testVariable", objectValue(serializableValue).create());
    
    enableAuthorization();
    setUserContext();
    
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the task because it belongs to no authenticated tenant:");
    ObjectValue typedValue = taskService.getVariableTyped(task.getId(), "testVariable");
    assertEquals(serializableValue, typedValue.getValue());
  }
  
  @Test
  public void getTaskVariableTypedWithNoUserAndNoAuthenticatedTenant() {
    
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
    
    ArrayList<String> serializableValue = getSerializedVariables();
    taskService.setVariable(task.getId(), "testVariable", objectValue(serializableValue).create());
    
    ObjectValue typedValue = taskService.getVariableTyped(task.getId(), "testVariable");
    assertEquals(serializableValue, typedValue.getValue());
  }
  
  // get task variables
  @Test
  public void getTaskVariablesWithNoUserAndNonTenant() {
    
    Task task = taskService.newTask();
    task.setName("aTask");
    taskService.saveTask(task);
    
    String taskId = task.getId();
    taskService.setVariable(taskId, "testVariable1", "test");
    taskService.setVariable(taskId, "testVariable2", "test");
    
    Map<String, Object> variables = taskService.getVariables(taskId);
    assertEquals(2, variables.size());
    
    taskService.deleteTask(taskId, true);
  }
  
  @Test
  public void getTaskVariablesWithUserAndNonTenant() {
    
    disableAuthorization();
    Task task = taskService.newTask();
    task.setName("aTask");
    taskService.saveTask(task);
    
    String taskId = task.getId();
    taskService.setVariable(taskId, "testVariable1", "test");
    taskService.setVariable(taskId, "testVariable2", "test");
    
    setUserContext();
    
    Map<String, Object> variables = taskService.getVariables(taskId);
    assertEquals(2, variables.size());
    
    taskService.deleteTask(taskId, true);
  }
  
  @Test
  public void getTaskVariablesWithUserAndAuthenticatedTenant() {
    
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
    
    taskService.setVariable(task.getId(), "testVariable1", "test");
    taskService.setVariable(task.getId(), "testVariable2", "test");
    
    enableAuthorization();
    createTenantAuthorization();

    Map<String, Object> variables = taskService.getVariables(task.getId());
    assertEquals(2, variables.size());
  }
  
  @Test
  public void getTaskVariablesWithUserAndNoAuthenticatedTenant() {
    
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
    
    taskService.setVariable(task.getId(), "testVariable1", "test");
    taskService.setVariable(task.getId(), "testVariable2", "test");
    
    setUserContext();
    
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the task because it belongs to no authenticated tenant:");
    taskService.getVariables(task.getId());
    
  }
  
  @Test
  public void getTaskVariablesWithNoUserAndNoAuthenticatedTenant() {
    
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
    
    taskService.setVariable(task.getId(), "testVariable1", "test");
    taskService.setVariable(task.getId(), "testVariable2", "test");
    
    enableAuthorization();
    
    Map<String, Object> variables = taskService.getVariables(task.getId());
    assertEquals(2, variables.size());
  }
  
  // set variable test
  @Test
  public void setTaskVariableWithNoUserAndNonTenant() {
    
    Task task = taskService.newTask();
    task.setName("aTask");
    taskService.saveTask(task);
    
    String taskId = task.getId();
    taskService.setVariable(taskId, "testVariable", "test");
    taskService.deleteTask(taskId, true);
  }
  
  @Test
  public void setTaskVariableWithUserAndNonTenant() {
    
    disableAuthorization();
    Task task = taskService.newTask();
    task.setName("aTask");
    taskService.saveTask(task);
    
    String taskId = task.getId();
    setUserContext();
    taskService.setVariable(taskId, "testVariable", "test");
    
    taskService.deleteTask(taskId, true);
  }
  
  @Test
  public void setTaskVariableWithUserAndAuthenticatedTenant() {
    
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess(multiTenancyProcessDefinitionKey)
      .startEvent()
      .userTask("task")
      .endEvent()
      .done();
    
    disableAuthorization();
    // deploy tenants
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    
    startProcessInstanceForTenant(TENANT_ONE, multiTenancyProcessDefinitionKey);
    
    createTenantAuthorization();

    Task task = taskService.createTaskQuery().singleResult();
    enableAuthorization();
    taskService.setVariable(task.getId(), "testVariable", "test");
  }
  
  @Test
  public void setTaskVariableWithUserAndNoAuthenticatedTenant() {
    
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
    thrown.expectMessage("Cannot update the task because it belongs to no authenticated tenant:");
    taskService.setVariable(task.getId(), "testVariable", "test");
  }
  
  @Test
  public void setTaskVariableWithNoUserAndNoAuthenticatedTenant() {
    
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
    taskService.setVariable(task.getId(), "testVariable", "test");
  }
  
  // remove variable test
  @Test
  public void removeTaskVariableWithNoUserAndNonTenant() {
    
    Task task = taskService.newTask();
    task.setName("aTask");
    taskService.saveTask(task);
    
    String taskId = task.getId();
    taskService.setVariable(taskId, "testVariable", "test");
    
    taskService.removeVariable(taskId, "testVariable");
    taskService.deleteTask(taskId, true);
  }
  
  @Test
  public void removeTaskVariableWithUserAndNonTenant() {
    
    setUserContext();
    disableAuthorization();
    Task task = taskService.newTask();
    task.setName("aTask");
    taskService.saveTask(task);
    
    String taskId = task.getId();
    enableAuthorization();
    taskService.setVariable(taskId, "testVariable", "test");
    
    taskService.removeVariable(taskId, "testVariable");
    
    taskService.deleteTask(taskId, true);
  }
  
  @Test
  public void removeTaskVariableWithUserAndAuthenticatedTenant() {
    
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
    taskService.setVariable(task.getId(), "testVariable", "test");

    createTenantAuthorization();
    taskService.removeVariable(task.getId(), "testVariable");
  }
  
  @Test
  public void removeTaskVariableWithUserAndNoAuthenticatedTenant() {
    
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
    taskService.setVariable(task.getId(), "testVariable", "test");

    enableAuthorization();
    setUserContext();
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the task because it belongs to no authenticated tenant:");
    taskService.removeVariable(task.getId(), "testVariable");
  }
  
  @Test
  public void removeTaskVariableWithNoUserAndNoAuthenticatedTenant() {
    
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
    taskService.setVariable(task.getId(), "testVariable", "test");
    
    enableAuthorization();
    taskService.removeVariable(task.getId(), "testVariable");
  }
  
  public ArrayList<String> getSerializedVariables() {
    
    ArrayList<String> serializableValue = new ArrayList<String>();
    serializableValue.add("1");
    serializableValue.add("2");
    return serializableValue;
  }
  
}
