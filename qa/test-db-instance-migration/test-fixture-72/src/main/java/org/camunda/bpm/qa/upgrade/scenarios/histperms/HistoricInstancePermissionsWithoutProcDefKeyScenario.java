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
package org.camunda.bpm.qa.upgrade.scenarios.histperms;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

public class HistoricInstancePermissionsWithoutProcDefKeyScenario {

  @Deployment
  public static String deploy() {
    return "org/camunda/bpm/qa/upgrade/oneTaskProcess.bpmn20.xml";
  }

  @DescribesScenario("createProcInstancesAndOpLogs")
  public static ScenarioSetup createProcInstancesAndOpLogs() {
    return (engine, scenarioName) -> {
      IdentityService identityService = engine.getIdentityService();
      for (int i = 0; i < 5; i++) {
        String processInstanceId = engine.getRuntimeService()
            .startProcessInstanceByKey("oneTaskProcess",
                "HistPermsWithoutProcDefKeyScenarioBusinessKey" + i)
            .getId();

        identityService.setAuthentication("mary02", null);

        TaskService taskService = engine.getTaskService();

        String taskId = taskService.createTaskQuery()
            .processInstanceId(processInstanceId)
            .singleResult()
            .getId();

        taskService.setAssignee(taskId, "john");

        identityService.clearAuthentication();
      }
    };
  }
}
