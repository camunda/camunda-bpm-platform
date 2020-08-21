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
package org.camunda.bpm.engine.test.api.mgmt.telemetry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.telemetry.CommandCounter;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class TelemetryRegistryCounterTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule);

  protected ProcessEngineConfigurationImpl configuration;
  protected RuntimeService runtimeService;
  protected TaskService taskService;

  protected ProcessEngine processEngineInMem;

  @Before
  public void init() {
    configuration = engineRule.getProcessEngineConfiguration();
    runtimeService = configuration.getRuntimeService();
    taskService = configuration.getTaskService();
  }

  @After
  public void tearDown() {
    configuration.setTelemetryRegistry(new TelemetryRegistry());

    if (processEngineInMem != null) {
      ProcessEngines.unregister(processEngineInMem);
      processEngineInMem.close();
    }
  }

  @Test
  public void shouldCountCommandsFromEngineStart() {
    // when
    processEngineInMem =  new StandaloneInMemProcessEngineConfiguration()
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName())
        .buildProcessEngine();

    // then
    TelemetryRegistry telemetryRegistry = processEngineInMem.getProcessEngineConfiguration().getTelemetryRegistry();
    Map<String, CommandCounter> entries = telemetryRegistry.getCommands();
    assertThat(entries.size()).isEqualTo(6);
    assertThat(entries.keySet()).containsExactlyInAnyOrder(
        "SchemaOperationsProcessEngineBuild", 
        "HistoryLevelSetupCommand", 
        "GetTableMetaDataCmd",
        "HistoryCleanupCmd", 
        "IsTelemetryEnabledCmd",
        "BootstrapEngineCommand");
    for (String commandName : entries.keySet()) {
      assertThat(entries.get(commandName).get()).isEqualTo(1);
    }
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldCountAfterCleaning() {
    // given
    clearCommandCounts();
    Map<String, CommandCounter> entries = configuration.getTelemetryRegistry().getCommands();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
    runtimeService.setVariable(processInstanceId, "foo", "bar");
    Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    taskService.complete(task.getId());

    // then
    assertThat(entries.size()).isEqualTo(4);
    String [] expectedExcutedCommands = {"StartProcessInstanceCmd",
                                         "SetExecutionVariablesCmd",
                                         "TaskQueryImpl",
                                         "CompleteTaskCmd"};
    assertThat(entries.keySet()).contains(expectedExcutedCommands);
    for (String commandName : expectedExcutedCommands) {
      assertThat(entries.get(commandName).get()).isEqualTo(1);
    }
  }

  protected void clearCommandCounts() {
    TelemetryRegistry telemetryRegistry = configuration.getTelemetryRegistry();
    Map<String, CommandCounter> entries = telemetryRegistry.getCommands();
    entries.clear();
  }

}
