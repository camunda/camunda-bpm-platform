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
package org.camunda.bpm.qa.upgrade.useroperationlog;

import java.util.Date;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

/**
 * @author Yana.Vasileva
 *
 */
public class SuspendProcessDefinitionDeleteScenario {

  @Deployment
  public static String deploy() {
    return "org/camunda/bpm/qa/upgrade/useroperationlog/timerBoundaryEventProcess.bpmn20.xml";
  }

  @DescribesScenario("createUserOperationLogEntriesForDelete")
  public static ScenarioSetup createUserOperationLogEntries() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        String processInstanceBusinessKey = "SuspendProcessDefinitionDeleteScenario";
        ProcessInstance processInstance1 = engine.getRuntimeService().startProcessInstanceByKey("timerBoundaryProcess", processInstanceBusinessKey);
        ProcessInstance processInstance2 = engine.getRuntimeService().startProcessInstanceByKey("timerBoundaryProcess", processInstanceBusinessKey);
        ProcessInstance processInstance3 = engine.getRuntimeService().startProcessInstanceByKey("timerBoundaryProcess", processInstanceBusinessKey);
        
        IdentityService identityService = engine.getIdentityService();
        identityService.setAuthentication("jane01", null);

        engine.getProcessEngineConfiguration().setAuthorizationEnabled(false);
        ClockUtil.setCurrentTime(new Date(1549000000000l));
        engine.getRuntimeService().suspendProcessInstanceById(processInstance1.getId());
        ClockUtil.setCurrentTime(new Date(1549100000000l));
        engine.getRuntimeService().suspendProcessInstanceById(processInstance2.getId());
        ClockUtil.setCurrentTime(new Date(1549200000000l));
        engine.getRuntimeService().suspendProcessInstanceById(processInstance3.getId());

        ClockUtil.reset();
        identityService.clearAuthentication();
      }
    };
  }
}
