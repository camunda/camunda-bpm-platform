package org.camunda.bpm.extension.junit5;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ProcessEngineExtension.class)
@Deployment(resources = {"processes/superProcess.bpmn", "processes/subProcess.bpmn"})
public class ProcessEngineExtensionResourcesDeploymentTest {

  ProcessEngine processEngine;
  
  @Test
  @Deployment(resources = {
      "processes/superProcess.bpmn", 
      "processes/subProcess.bpmn"
      })
  public void testDeployTwoDiagrams() {
    List<ProcessDefinition> processDefinitions = processEngine.getRepositoryService().createProcessDefinitionQuery().list();
    Assertions.assertThat(processDefinitions).hasSize(2);
  }
  
  @Test
  public void testDeployTwoDiagramsFromClassLevel() {
    List<ProcessDefinition> processDefinitions = processEngine.getRepositoryService().createProcessDefinitionQuery().list();
    Assertions.assertThat(processDefinitions).hasSize(2);
  }
}
