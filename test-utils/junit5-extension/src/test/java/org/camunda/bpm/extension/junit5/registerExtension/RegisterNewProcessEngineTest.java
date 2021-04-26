package org.camunda.bpm.extension.junit5.registerExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.RegisterExtension;

@TestInstance(Lifecycle.PER_CLASS)
public class RegisterNewProcessEngineTest {
  
  private ProcessEngine testEngine = ProcessEngineConfiguration
      .createStandaloneInMemProcessEngineConfiguration()
      .setProcessEngineName("testEngine")
      .buildProcessEngine();
  
  @RegisterExtension
  ProcessEngineExtension extension = ProcessEngineExtension.builder()
      .useProcessEngine(testEngine)
      .build();
  
  @AfterAll
  public void closeProcessEngine() {
    testEngine.close();
  }
  
  @Test
  @Deployment(resources = "processes/subProcess.bpmn")
  public void testUseProcessEngine() {
    RuntimeService runtimeService = testEngine.getRuntimeService();
    runtimeService.startProcessInstanceByKey("subProcess");
    TaskService taskService = testEngine.getTaskService();
    assertEquals(1, taskService.createTaskQuery().list().size());
  }

}
