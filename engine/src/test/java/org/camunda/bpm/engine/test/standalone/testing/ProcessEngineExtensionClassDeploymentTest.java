package org.camunda.bpm.engine.test.standalone.testing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ProvidedProcessEngineExtension.class)
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
