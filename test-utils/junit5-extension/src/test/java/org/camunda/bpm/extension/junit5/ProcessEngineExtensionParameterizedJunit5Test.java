package org.camunda.bpm.extension.junit5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(ProcessEngineExtension.class)
public class ProcessEngineExtensionParameterizedJunit5Test {
  
  ProcessEngine engine;
  
  @ParameterizedTest
  @ValueSource(ints = {1, 2})
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

  @ParameterizedTest
  @ValueSource(strings = {"A", "B"})
  @Deployment(resources = "org/camunda/bpm/extension/junit5/ProcessEngineExtensionParameterizedJunit5Test.extensionUsageExample.bpmn20.xml")
  public void extensionUsageExampleWithNamedAnnotation(String value) {
    Map<String,Object> variables = new HashMap<>();
    variables.put("key", value);
    RuntimeService runtimeService = engine.getRuntimeService();
    runtimeService.startProcessInstanceByKey("extensionUsage", variables);

    TaskService taskService = engine.getTaskService();
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("My Task", task.getName());

    taskService.complete(task.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    
    HistoryService historyService = engine.getHistoryService();
    HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();
    assertThat(variableInstance.getValue()).isEqualTo(value);
  }

  /**
   * The rule should work with tests that have no deployment annotation
   */
  @ParameterizedTest
  @EmptySource
  public void testWithoutDeploymentAnnotation(String argument) {
    assertEquals("aString", "aString");
  }

}
