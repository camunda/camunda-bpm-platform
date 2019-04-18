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
package org.camunda.bpm.qa.rolling.update.scenarios.task;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;
import org.camunda.bpm.qa.upgrade.Times;

/**
 * Starts the process with a parallel gateway, service task and user task on the old engine.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ProcessWithParallelGatewayAndServiceTaskScenario {

  public static final String PROCESS_DEF_KEY = "processWithParallelGatewayAndServiceTask";

  public static final String PROCESS_DEF_KEY_2 = "processWithParallelGatewayAndAsyncServiceTask";

  @Deployment
  public static String deploy() {
    return "org/camunda/bpm/qa/rolling/update/processWithParallelGatewayAndServiceTask.bpmn20.xml";
  }

  @Deployment
  public static String deployAsyncServiceTask() {
    return "org/camunda/bpm/qa/rolling/update/processWithParallelGatewayAndAsyncServiceTask.bpmn20.xml";
  }

  @DescribesScenario("init.none")
  @Times(1)
  public static ScenarioSetup startProcess() {
    return new ScenarioSetup() {
      @Override
      public void execute(ProcessEngine engine, String scenarioName) {
        engine.getRuntimeService().startProcessInstanceByKey(PROCESS_DEF_KEY, scenarioName);
      }
    };
  }

  @DescribesScenario("init.async")
  @Times(1)
  public static ScenarioSetup startProcessAsyncServiceTask() {
    return new ScenarioSetup() {
      @Override
      public void execute(ProcessEngine engine, String scenarioName) {
        engine.getRuntimeService().startProcessInstanceByKey(PROCESS_DEF_KEY_2, scenarioName);
      }
    };
  }

  @DescribesScenario("init.async.complete")
  @Times(1)
  public static ScenarioSetup startProcessAsyncServiceTaskCompleteUserTask() {
    return new ScenarioSetup() {
      @Override
      public void execute(ProcessEngine engine, String scenarioName) {
        ProcessInstance procInst = engine.getRuntimeService().startProcessInstanceByKey(PROCESS_DEF_KEY_2, scenarioName);
        Task task = engine.getTaskService().createTaskQuery().processInstanceId(procInst.getId()).singleResult();
        engine.getTaskService().complete(task.getId());
      }
    };
  }
}
