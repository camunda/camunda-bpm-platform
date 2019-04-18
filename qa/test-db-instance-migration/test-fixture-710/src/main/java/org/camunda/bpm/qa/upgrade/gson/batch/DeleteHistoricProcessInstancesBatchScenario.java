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
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tassilo Weidner
 */
public class DeleteHistoricProcessInstancesBatchScenario {

  @Deployment
  public static String deploy() {
    return "org/camunda/bpm/qa/upgrade/gson/oneTaskProcess.bpmn20.xml";
  }

  @DescribesScenario("initDeleteHistoricProcessInstancesBatch")
  public static ScenarioSetup initDeleteHistoricProcessInstancesBatch() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {

        for (int i = 0; i < 10; i++) {
          engine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess_710", "DeleteHistoricProcessInstancesBatchScenario");

          String taskId = engine.getTaskService().createTaskQuery()
            .processDefinitionKey("oneTaskProcess_710")
            .processInstanceBusinessKey("DeleteHistoricProcessInstancesBatchScenario")
            .singleResult()
            .getId();

          engine.getTaskService().complete(taskId);
        }

        List<String> processInstanceIds = new ArrayList<>();

        List<HistoricProcessInstance> processInstances = engine.getHistoryService().createHistoricProcessInstanceQuery()
          .processDefinitionKey("oneTaskProcess_710")
          .processInstanceBusinessKey("DeleteHistoricProcessInstancesBatchScenario")
          .list();

        for (HistoricProcessInstance processInstance : processInstances) {
          processInstanceIds.add(processInstance.getId());
        }

        engine.getHistoryService().deleteHistoricProcessInstancesAsync(processInstanceIds, null);
      }
    };
  }
}
