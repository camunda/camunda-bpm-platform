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
package org.camunda.bpm.qa.upgrade.gson.batch;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tassilo Weidner
 */
public class RestartProcessInstanceBatchScenario {

  @Deployment
  public static String deploy() {
    return "org/camunda/bpm/qa/upgrade/gson/oneTaskProcessRestart.bpmn20.xml";
  }

  @DescribesScenario("initRestartProcessInstanceBatch")
  public static ScenarioSetup initRestartProcessInstanceBatch() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {

        List<String> processInstanceIds = new ArrayList<>();
        String processDefinitionId = null;
        for (int i = 0; i < 10; i++) {
          ProcessInstance processInstance = engine.getRuntimeService()
            .startProcessInstanceByKey("oneTaskProcessRestart_710", "RestartProcessInstanceBatchScenario");

          processDefinitionId = processInstance.getProcessDefinitionId();

          processInstanceIds.add(processInstance.getId());

          String taskId = engine.getTaskService().createTaskQuery()
            .processDefinitionKey("oneTaskProcessRestart_710")
            .processInstanceBusinessKey("RestartProcessInstanceBatchScenario")
            .singleResult()
            .getId();

          engine.getTaskService().complete(taskId);
        }

        Batch batch = engine.getRuntimeService().restartProcessInstances(processDefinitionId)
          .startBeforeActivity("theTask")
          .processInstanceIds(processInstanceIds)
          .skipCustomListeners()
          .skipIoMappings()
          .withoutBusinessKey()
          .executeAsync();
        engine.getManagementService().setProperty("RestartProcessInstanceBatchScenario.batchId", batch.getId());
      }
    };
  }
}
