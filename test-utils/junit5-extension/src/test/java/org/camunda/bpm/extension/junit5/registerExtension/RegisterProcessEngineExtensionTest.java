package org.camunda.bpm.extension.junit5.registerExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class RegisterProcessEngineExtensionTest {
  
  @RegisterExtension
  ProcessEngineExtension extension = ProcessEngineExtension.builder().build();
  
  @Test
  @Deployment
  public void registeredExtensionUsageExample() {
    RuntimeService runtimeService = extension.getProcessEngine()
        .getRuntimeService();
    runtimeService.startProcessInstanceByKey("registeredExtensionUsage");
    
    TaskService taskService = extension
        .getProcessEngine()
        .getTaskService();
    
    Task task = taskService
        .createTaskQuery()
        .singleResult();
    assertEquals("Test something", task.getName());
    taskService.complete(task.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().list().size());
  }

}
