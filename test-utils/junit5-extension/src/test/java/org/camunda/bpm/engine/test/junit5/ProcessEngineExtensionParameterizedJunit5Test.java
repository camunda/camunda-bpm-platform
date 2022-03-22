/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.junit5;

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
  @Deployment(resources = "org/camunda/bpm/engine/test/junit5/ProcessEngineExtensionParameterizedJunit5Test.extensionUsageExample.bpmn20.xml")
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
