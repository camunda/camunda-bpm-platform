package org.camunda.bpm.engine.test.api.runtime;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

/**
 * 
 * @author Daniel Meyer
 */
public class ProcessInstanceSuspensionTest extends PluggableProcessEngineTestCase {

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testProcessInstanceActiveByDefault() {
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());      
    
  }
    
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testSuspendActivateProcessInstance() {    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());      
    
    //suspend
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertTrue(processInstance.isSuspended());      
    
    //activate
    runtimeService.activateProcessInstanceById(processInstance.getId());
    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());
  }
  
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testCannotActivateActiveProcessInstance() {    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());      
        
    try {
      //activate
      runtimeService.activateProcessInstanceById(processInstance.getId());
      fail("Expected activiti exception");
    }catch (ProcessEngineException e) {
     // expected
    }
   
  }
  
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testCannotSuspendSuspendedProcessInstance() {    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());      
    
    runtimeService.suspendProcessInstanceById(processInstance.getId());
        
    try {
      runtimeService.suspendProcessInstanceById(processInstance.getId());
      fail("Expected activiti exception");
    }catch (ProcessEngineException e) {
     // expected
    }
   
  }
  
  @Deployment(resources={
          "org/camunda/bpm/engine/test/api/runtime/superProcessWithMultipleNestedSubProcess.bpmn20.xml",
          "org/camunda/bpm/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
          "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml"
          })
  public void testQueryForActiveAndSuspendedProcessInstances() {    
    runtimeService.startProcessInstanceByKey("nestedSubProcessQueryTest");
    
    assertEquals(5, runtimeService.createProcessInstanceQuery().count());
    assertEquals(5, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().suspended().count());
    
    ProcessInstance piToSuspend = runtimeService.createProcessInstanceQuery()
            .processDefinitionKey("nestedSubProcessQueryTest")
            .singleResult();
    runtimeService.suspendProcessInstanceById(piToSuspend.getId());
    
    assertEquals(5, runtimeService.createProcessInstanceQuery().count());
    assertEquals(4, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().suspended().count());
    
    assertEquals(piToSuspend.getId(), runtimeService.createProcessInstanceQuery().suspended().singleResult().getId());
  }

}
