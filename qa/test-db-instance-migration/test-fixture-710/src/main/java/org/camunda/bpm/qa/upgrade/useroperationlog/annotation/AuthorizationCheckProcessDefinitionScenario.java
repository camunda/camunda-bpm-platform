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
package org.camunda.bpm.qa.upgrade.useroperationlog.annotation;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

public class AuthorizationCheckProcessDefinitionScenario {

  @Deployment
  public static String deploy() {
    return "org/camunda/bpm/qa/upgrade/useroperationlog/annotation/oneTaskProcess.bpmn20.xml";
  }

  @DescribesScenario("prepareAuthorizationCheckProcessDefinition")
  public static ScenarioSetup prepareAuthorizationCheckProcessDefinition() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        String processInstanceId = engine.getRuntimeService()
            .startProcessInstanceByKey("oneTaskProcess_userOpLog_annotation")
            .getId();

        engine.getIdentityService()
            .setAuthentication("demo", null);

        Task task = engine.getTaskService()
            .createTaskQuery()
            .processInstanceId(processInstanceId)
            .singleResult();

        engine.getTaskService()
            .setAssignee(task.getId(), "john");

        engine.getIdentityService()
            .clearAuthentication();
      }
    };
  }
}
