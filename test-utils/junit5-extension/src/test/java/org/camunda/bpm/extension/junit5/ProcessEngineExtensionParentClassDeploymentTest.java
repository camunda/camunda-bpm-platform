package org.camunda.bpm.extension.junit5;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ProcessEngineExtension.class)
public class ProcessEngineExtensionParentClassDeploymentTest extends ProcessEngineExtensionParentClassDeployment {
  
  @Test
  public void testDeploymentOnParentClassLevel(ProcessEngine processEngine) {
    assertNotNull(processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("testHelperDeploymentTest").singleResult(), 
        "process is not deployed");
  }

}
