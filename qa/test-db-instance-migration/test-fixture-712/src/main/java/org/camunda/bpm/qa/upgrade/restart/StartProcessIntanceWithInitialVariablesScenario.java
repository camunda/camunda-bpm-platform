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
package org.camunda.bpm.qa.upgrade.restart;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

public class StartProcessIntanceWithInitialVariablesScenario {

  @Deployment
  public static String deploy() {
    return "org/camunda/bpm/qa/upgrade/async/oneAsyncTaskProcess.bpmn20.xml";
  }

  @DescribesScenario("startProcessIntanceWithInitialVariablesScenario")
  public static ScenarioSetup createUserOperationLogEntries() {
    return new ScenarioSetup() {
      @Override
      public void execute(ProcessEngine engine, String scenarioName) {
        RuntimeService runtimeService = engine.getRuntimeService();

        String businessKey = "712_ProcessIntanceExecuted";
        runtimeService.startProcessInstanceByKey("asyncBeforeStartProcess_712", businessKey,
            Variables.createVariables()
                .putValue("initial1", "value1"));

        businessKey = "7120_ProcessIntanceWithoutExecute";
        runtimeService.startProcessInstanceByKey("asyncBeforeStartProcess_712", businessKey,
            Variables.createVariables()
            .putValue("initial2", "value1"));

        businessKey = "7120_ProcessIntanceWithoutExecuteAndSetVariables";
        runtimeService.startProcessInstanceByKey("asyncBeforeStartProcess_712", businessKey,
            Variables.createVariables()
            .putValue("initial3", "value1"));
      }
    };
  }

}
