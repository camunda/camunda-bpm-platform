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
package org.camunda.bpm.qa.upgrade.scenarios.multiinstance;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;
import org.camunda.bpm.qa.upgrade.Times;

/**
 * @author Thorben Lindhauer
 *
 */
public class MultiInstanceReceiveTaskScenario {

  @Deployment
  public static String deployProcessParallel() {
    return "org/camunda/bpm/qa/upgrade/multiinstance/parallelMultiInstanceReceiveTask.bpmn20.xml";
  }

  @Deployment
  public static String deployProcessSequential() {
    return "org/camunda/bpm/qa/upgrade/multiinstance/sequentialMultiInstanceReceiveTask.bpmn20.xml";
  }

  @DescribesScenario("initParallel")
  @Times(4)
  public static ScenarioSetup instantiateParallel() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        engine
          .getRuntimeService()
          .startProcessInstanceByKey("ParallelMultiInstanceReceiveTask", scenarioName);
      }
    };
  }

  @DescribesScenario("initSequential")
  @Times(4)
  public static ScenarioSetup instantiateSequential() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        engine
          .getRuntimeService()
          .startProcessInstanceByKey("SequentialMultiInstanceReceiveTask", scenarioName);
      }
    };
  }
}
