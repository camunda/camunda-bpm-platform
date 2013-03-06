package org.camunda.bpm.engine.test.api.repository;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.Deployment;

/**
 * 
 * @author Daniel Meyer
 */
public class ProcessDefinitionSuspensionTest extends PluggableProcessEngineTestCase {

  @Deployment(resources={"org/camunda/bpm/engine/test/db/processOne.bpmn20.xml"})
  public void testProcessDefinitionActiveByDefault() {
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    
    assertFalse(processDefinition.isSuspended());      
    
  }
    
  @Deployment(resources={"org/camunda/bpm/engine/test/db/processOne.bpmn20.xml"})
  public void testSuspendActivateProcessDefinitionById() {    
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());   
    
    // suspend
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());    
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertTrue(processDefinition.isSuspended());      
    
    // activate
    repositoryService.activateProcessDefinitionById(processDefinition.getId());
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/db/processOne.bpmn20.xml"})
  public void testSuspendActivateProcessDefinitionByKey() {    
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());   
    
    //suspend
    repositoryService.suspendProcessDefinitionByKey(processDefinition.getKey());    
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertTrue(processDefinition.isSuspended());      
    
    //activate
    repositoryService.activateProcessDefinitionByKey(processDefinition.getKey());
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());
  }
  
  @Deployment(resources={"org/camunda/bpm/engine/test/db/processOne.bpmn20.xml"})
  public void testCannotActivateActiveProcessDefinition() {    
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());   
    
    try {
      repositoryService.activateProcessDefinitionById(processDefinition.getId());
      fail("Exception exprected");
    }catch (ProcessEngineException e) {
      // expected
    }
    
  }
  
  @Deployment(resources={"org/camunda/bpm/engine/test/db/processOne.bpmn20.xml"})
  public void testCannotSuspendActiveProcessDefinition() {    
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());   
    
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    
    try {
      repositoryService.suspendProcessDefinitionById(processDefinition.getId());
      fail("Exception exprected");
    }catch (ProcessEngineException e) {
      // expected
    }
  }
  
  @Deployment(resources={
          "org/camunda/bpm/engine/test/db/processOne.bpmn20.xml",
          "org/camunda/bpm/engine/test/db/processTwo.bpmn20.xml"
          })
  public void testQueryForActiveDefinitions() {    
    
    // default = all definitions
    List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
      .list();    
    assertEquals(2, processDefinitionList.size());
    
    assertEquals(2, repositoryService.createProcessDefinitionQuery().active().count());
    
    ProcessDefinition processDefinition = processDefinitionList.get(0);
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());
  }
  
  @Deployment(resources={
          "org/camunda/bpm/engine/test/db/processOne.bpmn20.xml",
          "org/camunda/bpm/engine/test/db/processTwo.bpmn20.xml"
          })
  public void testQueryForSuspendedDefinitions() {    
    
    // default = all definitions
    List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
      .list();    
    assertEquals(2, processDefinitionList.size());
    
    assertEquals(2, repositoryService.createProcessDefinitionQuery().active().count());
    
    ProcessDefinition processDefinition = processDefinitionList.get(0);
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());
  }
  
  

}
