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
package org.camunda.bpm.qa.upgrade;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.qa.upgrade.batch.deploymentaware.DeploymentAwareBatchesScenario;
import org.camunda.bpm.qa.upgrade.customretries.FailingIntermediateBoundaryTimerJobScenario;
import org.camunda.bpm.qa.upgrade.restart.StartProcessIntanceWithInitialVariablesScenario;
import org.camunda.bpm.qa.upgrade.restart.SetVariablesScenario;

/**
 * @author Nikola Koevski
 */
public class TestFixture {

  public static final String ENGINE_VERSION = "7.12.0";

  public TestFixture(ProcessEngine processEngine) {
  }

  public static void main(String[] args) {
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("camunda.cfg.xml");
    ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();

    // register test scenarios
    ScenarioRunner runner = new ScenarioRunner(processEngine, ENGINE_VERSION);

    runner.setupScenarios(FailingIntermediateBoundaryTimerJobScenario.class);
    // batch deployment-awareness
    runner.setupScenarios(DeploymentAwareBatchesScenario.class);

    runner.setupScenarios(StartProcessIntanceWithInitialVariablesScenario.class);
    runner.setupScenarios(SetVariablesScenario.class);

    processEngine.close();
  }
}
