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
package org.camunda.bpm.qa.upgrade.scenarios.compensation;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;
import org.camunda.bpm.qa.upgrade.Times;

/**
 * @author Thorben Lindhauer
 *
 */
public class NestedMultiInstanceCompensationScenario {

  @Deployment
  public static String deployThrowInnerProcess() {
    return "org/camunda/bpm/qa/upgrade/compensation/nestedMultiInstanceCompensationThrowInnerProcess.bpmn20.xml";
  }

  @Deployment
  public static String deployThrowOuterProcess() {
    return "org/camunda/bpm/qa/upgrade/compensation/nestedMultiInstanceCompensationThrowOuterProcess.bpmn20.xml";
  }

  @DescribesScenario("init.throwInner")
  @Times(3)
  public static ScenarioSetup instantitiateThrowCompensateInSubprocess() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        engine.getRuntimeService()
          .startProcessInstanceByKey("NestedMultiInstanceCompensationThrowInnerScenario", scenarioName);

        // throw compensation within the mi subprocess
        List<Task> subProcessTasks = engine
            .getTaskService()
            .createTaskQuery()
            .processInstanceBusinessKey(scenarioName)
            .list();

        for (Task subProcessTask : subProcessTasks) {
          engine.getTaskService().complete(subProcessTask.getId());
        }
      }
    };
  }

  @DescribesScenario("init.throwOuter")
  @Times(3)
  public static ScenarioSetup instantitiateThrowCompensateAfterSubprocess() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        engine.getRuntimeService()
          .startProcessInstanceByKey("NestedMultiInstanceCompensationThrowOuterScenario", scenarioName);

        // throw compensation after the mi subprocess has ended
        List<Task> subProcessTasks = engine
            .getTaskService()
            .createTaskQuery()
            .processInstanceBusinessKey(scenarioName)
            .list();

        for (Task subProcessTask : subProcessTasks) {
          engine.getTaskService().complete(subProcessTask.getId());
        }
      }
    };
  }
}
