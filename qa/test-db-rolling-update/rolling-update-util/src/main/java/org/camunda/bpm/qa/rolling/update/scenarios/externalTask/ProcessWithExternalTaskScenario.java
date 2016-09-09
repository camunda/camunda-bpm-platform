/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.qa.rolling.update.scenarios.externalTask;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.qa.rolling.update.TestFixture;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;
import org.camunda.bpm.qa.upgrade.Times;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ProcessWithExternalTaskScenario {

  public static final String PROCESS_DEF_KEY = "processWithExternalTask";
  public static final long LOCK_TIME = 5 * 60 * 1000;

  @Deployment
  public static BpmnModelInstance deploy() {
    return Bpmn.createExecutableProcess(PROCESS_DEF_KEY)
                .startEvent()
                .serviceTask("externalTask")
                  .camundaType("external")
                  .camundaTopic(TestFixture.currentFixtureTag)
                .endEvent()
                .done();
  }

  @DescribesScenario("init")
  @Times(1)
  public static ScenarioSetup startProcess() {
    return new ScenarioSetup() {

      @Override
      public void execute(ProcessEngine engine, String scenarioName) {
        engine.getRuntimeService().startProcessInstanceByKey(PROCESS_DEF_KEY, scenarioName);
      }
    };
  }


  @DescribesScenario("init.fetch")
  @Times(1)
  public static ScenarioSetup startProcessWithFetch() {
    return new ScenarioSetup() {

      @Override
      public void execute(ProcessEngine engine, String scenarioName) {
        engine.getRuntimeService().startProcessInstanceByKey(PROCESS_DEF_KEY, scenarioName);
        engine.getExternalTaskService().fetchAndLock(1, scenarioName)
                                       .topic(TestFixture.currentFixtureTag, LOCK_TIME)
                                       .execute();
      }
    };
  }
}
