package org.camunda.bpm.engine.test.standalone.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ProvidedProcessEngineExtension.class)
public class ProcessEngineExtensionJunit5Test {
  
  ProcessEngine engine;
  
  @Test
  @Deployment
  public void extensionUsageExample() {
    RuntimeService runtimeService = engine.getRuntimeService();
    runtimeService.startProcessInstanceByKey("extensionUsage");

    TaskService taskService = engine.getTaskService();
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("My Task", task.getName());

    taskService.complete(task.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  /**
   * The extension should work with tests that have no deployment annotation
   */
  @Test
  public void testWithoutDeploymentAnnotation() {
    assertEquals("aString", "aString");
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void requiredHistoryLevelAudit() {
    
    assertThat(currentHistoryLevel()).isIn(
        ProcessEngineConfiguration.HISTORY_AUDIT, ProcessEngineConfiguration.HISTORY_FULL);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void requiredHistoryLevelActivity() {

    assertThat(currentHistoryLevel()).isIn(
        ProcessEngineConfiguration.HISTORY_ACTIVITY, 
        ProcessEngineConfiguration.HISTORY_AUDIT,
        ProcessEngineConfiguration.HISTORY_FULL);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void requiredHistoryLevelFull() {

    assertThat(currentHistoryLevel()).isEqualTo(ProcessEngineConfiguration.HISTORY_FULL);
  }

  protected String currentHistoryLevel() {
    return engine.getProcessEngineConfiguration().getHistory();
  }

}
