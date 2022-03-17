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
import org.junit.jupiter.api.extension.RegisterExtension;

public class RegisterNewProcessEngineTest {
  
  private static ProcessEngine testEngine = ProcessEngineConfiguration
      .createStandaloneInMemProcessEngineConfiguration()
      .setProcessEngineName("testEngine")
      // Use a new database to resolve the conflict with existing 
      // in-memory-database. The tables will be removed after the test.
      .setJdbcUrl("jdbc:h2:mem:camunda-test")
      .buildProcessEngine();
  
  @RegisterExtension
  ProcessEngineExtension extension = ProcessEngineExtension.builder()
      .useProcessEngine(testEngine)
      .build();
  
  @AfterAll
  public static void closeProcessEngine() {
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
