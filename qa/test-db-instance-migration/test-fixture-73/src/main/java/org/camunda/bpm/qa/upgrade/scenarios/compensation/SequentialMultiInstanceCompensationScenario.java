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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ExtendsScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;
import org.camunda.bpm.qa.upgrade.Times;

/**
 * @author Thorben Lindhauer
 *
 */
public class SequentialMultiInstanceCompensationScenario {

  @Deployment
  public static String deploySingleActivityHandler() {
    return "org/camunda/bpm/qa/upgrade/compensation/sequentialMultiInstanceCompensationSingleActivityHandlerProcess.bpmn20.xml";
  }

  @DescribesScenario("singleActivityHandler.multiInstancePartial")
  @Times(3)
  public static ScenarioSetup singleActivityHandlerMultiInstancePartial() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        engine
          .getRuntimeService()
          .startProcessInstanceByKey("SequentialMultiInstanceCompensationSingleActivityHandlerScenario", scenarioName);

        // complete two out of three MI tasks
        Task miTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();
        engine.getTaskService().complete(miTask.getId());

        miTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();
        engine.getTaskService().complete(miTask.getId());
      }
    };
  }

  @DescribesScenario("singleActivityHandler.beforeCompensate")
  @Times(3)
  public static ScenarioSetup singleActivityHandlerBeforeCompensate() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        engine
          .getRuntimeService()
          .startProcessInstanceByKey("SequentialMultiInstanceCompensationSingleActivityHandlerScenario", scenarioName);

        // complete all mi tasks
        Task miTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();
        engine.getTaskService().complete(miTask.getId());

        miTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();
        engine.getTaskService().complete(miTask.getId());

        miTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();
        engine.getTaskService().complete(miTask.getId());
      }
    };
  }

  @DescribesScenario("singleActivityHandler.beforeCompensate.throwCompensate")
  @ExtendsScenario("singleActivityHandler.beforeCompensate")
  @Times(3)
  public static ScenarioSetup singleActivityHandlerThrowCompensate() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        Task beforeCompensateTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();

        engine.getTaskService().complete(beforeCompensateTask.getId());
      }
    };
  }

  @Deployment
  public static String deployDefaultHandler() {
    return "org/camunda/bpm/qa/upgrade/compensation/sequentialMultiInstanceCompensationDefaultHandlerProcess.bpmn20.xml";
  }

  @DescribesScenario("defaultHandler.multiInstancePartial")
  @Times(3)
  public static ScenarioSetup defaultHandlerMultiInstancePartial() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        engine
          .getRuntimeService()
          .startProcessInstanceByKey("SequentialMultiInstanceCompensationDefaultHandlerScenario", scenarioName);

        // complete two out of three MI tasks
        Task miTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();
        engine.getTaskService().complete(miTask.getId());

        miTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();
        engine.getTaskService().complete(miTask.getId());
      }
    };
  }

  @DescribesScenario("defaultHandler.beforeCompensate")
  @Times(3)
  public static ScenarioSetup defaultHandlerBeforeCompensate() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        engine
          .getRuntimeService()
          .startProcessInstanceByKey("SequentialMultiInstanceCompensationDefaultHandlerScenario", scenarioName);

        // complete all mi tasks
        Task miTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();
        engine.getTaskService().complete(miTask.getId());

        miTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();
        engine.getTaskService().complete(miTask.getId());

        miTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();
        engine.getTaskService().complete(miTask.getId());
      }
    };
  }

  @DescribesScenario("defaultHandler.beforeCompensate.throwCompensate")
  @ExtendsScenario("defaultHandler.beforeCompensate")
  @Times(3)
  public static ScenarioSetup defaultHandlerThrowCompensate() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        Task beforeCompensateTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();

        engine.getTaskService().complete(beforeCompensateTask.getId());
      }
    };
  }

  @Deployment
  public static String deploySubProcessHandler() {
    return "org/camunda/bpm/qa/upgrade/compensation/sequentialMultiInstanceCompensationSubProcessHandlerProcess.bpmn20.xml";
  }

  @DescribesScenario("subProcessHandler.multiInstancePartial")
  @Times(3)
  public static ScenarioSetup subProcessHandlerMultiInstancePartial() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        engine
          .getRuntimeService()
          .startProcessInstanceByKey("SequentialMultiInstanceCompensationSubProcessHandlerScenario", scenarioName);

        // complete two out of three MI tasks
        Task miTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();
        engine.getTaskService().complete(miTask.getId());

        miTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();
        engine.getTaskService().complete(miTask.getId());
      }
    };
  }

  @DescribesScenario("subProcessHandler.beforeCompensate")
  @Times(3)
  public static ScenarioSetup subProcessHandlerBeforeCompensate() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        engine
          .getRuntimeService()
          .startProcessInstanceByKey("SequentialMultiInstanceCompensationSubProcessHandlerScenario", scenarioName);

        // complete all mi tasks
        Task miTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();
        engine.getTaskService().complete(miTask.getId());

        miTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();
        engine.getTaskService().complete(miTask.getId());

        miTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();
        engine.getTaskService().complete(miTask.getId());
      }
    };
  }

  @DescribesScenario("subProcessHandler.beforeCompensate.throwCompensate")
  @ExtendsScenario("subProcessHandler.beforeCompensate")
  @Times(3)
  public static ScenarioSetup subProcessHandlerThrowCompensate() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        Task beforeCompensateTask = engine.getTaskService().createTaskQuery()
            .processInstanceBusinessKey(scenarioName).singleResult();

        engine.getTaskService().complete(beforeCompensateTask.getId());
      }
    };
  }
}
