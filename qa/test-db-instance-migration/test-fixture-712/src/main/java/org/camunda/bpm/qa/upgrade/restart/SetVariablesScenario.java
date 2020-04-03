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

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

public class SetVariablesScenario {

  @Deployment
  public static String deploy() {
    return "org/camunda/bpm/qa/upgrade/async/oneAsyncTaskProcess.bpmn20.xml";
  }

  @DescribesScenario("setVariablesScenario")
  public static ScenarioSetup createUserOperationLogEntries() {
    return new ScenarioSetup() {
      @Override
      public void execute(ProcessEngine engine, String scenarioName) {
        RuntimeService runtimeService = engine.getRuntimeService();
        ProcessInstance processInstanceWithInitialVariables = runtimeService.createProcessInstanceQuery()
            .processDefinitionKey("asyncBeforeStartProcess_712")
            .processInstanceBusinessKey("712_ProcessIntanceExecuted")
            .singleResult();
        ManagementService managementService = engine.getManagementService();
        Job firstJob = managementService.createJobQuery()
            .processDefinitionKey("asyncBeforeStartProcess_712")
            .processInstanceId(processInstanceWithInitialVariables.getId())
            .singleResult();
        try {
          managementService.executeJob(firstJob.getId());
        } catch (Exception e) {
          // ignore
        }

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
            .processDefinitionKey("asyncBeforeStartProcess_712")
            .processInstanceBusinessKey("7120_ProcessIntanceWithoutExecute")
            .singleResult();
        runtimeService.setVariable(processInstance.getId(), "foo", "value");
        runtimeService.setVariableLocal(processInstance.getId(), "local", "foo1");
      }
    };
  }

}
