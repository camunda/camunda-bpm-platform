package org.camunda.bpm.extension.junit5;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ProcessEngineExtension.class)
@Deployment
public class ProcessEngineExtensionClassDeploymentTest {
  
  @Test
  public void testDeploymentOnClassLevel(ProcessEngine processEngine) {
    assertNotNull(processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("testHelperDeploymentTest").singleResult(), 
        "No process deployed with class annotation");
  }
  
  @Test
  @Deployment
  public void testDeploymentOnMethodOverridesClass(ProcessEngine processEngine) {
    assertNotNull(processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("testHelperDeploymentTestOverride").singleResult(), 
        "No process deployed for method");
  }
}
