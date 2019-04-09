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
package org.camunda.bpm.qa.upgrade.scenarios.eventsubprocess;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ExtendsScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;
import org.camunda.bpm.qa.upgrade.Times;

/**
 * Provokes misbehaving error propagation that none of the other cases triggers
 *
 * @author Thorben Lindhauer
 */
public class TwoLevelNestedNonInterruptingEventSubprocessScenario {


  @Deployment
  public static String deployProcess() {
    return "org/camunda/bpm/qa/upgrade/eventsubprocess/twoLevelNestedNonInterruptingMessageEventSubprocess.bpmn20.xml";
  }

  @DescribesScenario("initLevel1")
  @Times(7)
  public static ScenarioSetup initLevelOneEventSubProcess() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        engine
          .getRuntimeService()
          .startProcessInstanceByKey("NestedNonInterruptingMessageEventSubprocessScenarioNestedSubprocess",
              scenarioName);

        engine.getRuntimeService()
          .createMessageCorrelation("OuterEventSubProcessMessage")
          .processInstanceBusinessKey(scenarioName)
          .correlate();
      }
    };
  }

  @DescribesScenario("initLevel1.initLevel2")
  @ExtendsScenario("initLevel1")
  @Times(7)
  public static ScenarioSetup initNestedSubProcessEnterSubprocess() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        engine.getRuntimeService()
          .createMessageCorrelation("InnerEventSubProcessMessage")
          .processInstanceBusinessKey(scenarioName)
          .correlate();
      }
    };
  }
}
