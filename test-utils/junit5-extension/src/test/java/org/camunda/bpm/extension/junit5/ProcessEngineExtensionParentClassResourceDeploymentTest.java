package org.camunda.bpm.extension.junit5;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ProcessEngineExtension.class)
public class ProcessEngineExtensionParentClassResourceDeploymentTest extends ProcessEngineExtensionParentClass {
  
  ProcessEngine processEngine;

  @Test
  public void testSuperClassResourcesDeployment() {
    List<ProcessDefinition> processDefinitions = processEngine.getRepositoryService().createProcessDefinitionQuery().list();
    Assertions.assertThat(processDefinitions).hasSize(2);
  }
}
