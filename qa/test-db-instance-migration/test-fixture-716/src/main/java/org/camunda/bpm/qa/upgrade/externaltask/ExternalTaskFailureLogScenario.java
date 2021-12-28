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
package org.camunda.bpm.qa.upgrade.externaltask;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

public class ExternalTaskFailureLogScenario {

  @Deployment
  public static String modelDeployment() {
    return "org/camunda/bpm/qa/upgrade/externaltask/oneTaskProcess.bpmn20.xml";
  }

  @DescribesScenario("failedTaskWithRetries")
  public static ScenarioSetup createFailedTaskWithRetries() {
    return (engine, scenarioName) -> {
      RuntimeService runtimeService = engine.getRuntimeService();
      String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess-external-task-716", scenarioName)
          .getId();

      ExternalTaskService externalTaskService = engine.getExternalTaskService();
      ExternalTask task = externalTaskService.createExternalTaskQuery().processInstanceId(processInstanceId).singleResult();

      String workerId = "worker";
      externalTaskService.fetchAndLock(1, workerId).topic("topic", 1000L).execute();

      externalTaskService.handleFailure(task.getId(), workerId, "error message", 2, 0);

      // result: task has failed once (external task log; exception message populated)
      // and has still > 0 retries left
    };
  }
}
