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
package org.camunda.bpm.qa.upgrade.pvm;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

public class AsyncJoinScenario {

  @Deployment
  public static String modelParallelDeployment() {
    return "org/camunda/bpm/qa/upgrade/pvm/asyncParallelGateway.bpmn20.xml";
  }
  
  @Deployment
  public static String modelInclusiveDeployment() {
    return "org/camunda/bpm/qa/upgrade/pvm/asyncInclusiveGateway.bpmn20.xml";
  }

  @DescribesScenario("asyncJoinParallel")
  public static ScenarioSetup createJobsForParallelJoin() {
    return (engine, scenarioName) -> {
      engine.getRuntimeService().startProcessInstanceByKey("async-join-parallel-716", scenarioName);

      // result: two jobs have been created, one for each concurrent execution
      // reaching the parallel join gateway
    };
  }
  
  @DescribesScenario("asyncJoinInclusive")
  public static ScenarioSetup createJobsForInclusiveJoin() {
    return (engine, scenarioName) -> {
      engine.getRuntimeService().startProcessInstanceByKey("async-join-inclusive-716", scenarioName);

      // result: two jobs have been created, one for each concurrent execution
      // reaching the inclusive join gateway
    };
  }
}
