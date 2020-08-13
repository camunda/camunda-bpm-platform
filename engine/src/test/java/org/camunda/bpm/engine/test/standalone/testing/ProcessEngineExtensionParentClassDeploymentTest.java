package org.camunda.bpm.engine.test.standalone.testing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ProvidedProcessEngineExtension.class)
public class ProcessEngineExtensionParentClassDeploymentTest extends ProcessEngineExtensionParentClassDeployment {
  
  @Test
  public void testDeploymentOnParentClassLevel(ProcessEngine processEngine) {
    assertNotNull(processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("testHelperDeploymentTest").singleResult(), 
        "process is not deployed");
  }

}
