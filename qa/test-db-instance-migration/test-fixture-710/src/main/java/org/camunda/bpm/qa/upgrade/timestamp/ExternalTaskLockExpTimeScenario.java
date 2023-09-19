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
package org.camunda.bpm.qa.upgrade.timestamp;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;
import org.camunda.bpm.qa.upgrade.Times;

/**
 * @author Nikola Koevski
 */
public class ExternalTaskLockExpTimeScenario extends AbstractTimestampMigrationScenario {

  protected static final String TOPIC_NAME = "externalTaskLockExpTimeTestTopic";
  protected static final String WORKER_ID = "extTaskLockExpTimeWorkerId";
  protected static final long LOCK_TIME = 1000L;

  protected static final String PROCESS_DEFINITION_KEY = "oneExtTaskForLockExpTimeTestProcess";
  protected static final BpmnModelInstance EXT_TASK_MODEL  = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .camundaHistoryTimeToLive(180)
      .startEvent("start")
    .serviceTask("extTask")
      .camundaExternalTask(TOPIC_NAME)
    .endEvent("end")
    .done();

  @DescribesScenario("initExternalTaskLockExpTime")
  @Times(1)
  public static ScenarioSetup initExternalTaskLockExpTime() {
    return new ScenarioSetup() {
      @Override
      public void execute(ProcessEngine processEngine, String scenarioName) {

        ClockUtil.setCurrentTime(TIMESTAMP);

        deployModel(processEngine, PROCESS_DEFINITION_KEY, PROCESS_DEFINITION_KEY, EXT_TASK_MODEL);

        processEngine.getRuntimeService()
          .startProcessInstanceByKey(PROCESS_DEFINITION_KEY, scenarioName);

        processEngine.getExternalTaskService()
          .fetchAndLock(1, WORKER_ID)
          .topic(TOPIC_NAME, LOCK_TIME)
          .execute();

        ClockUtil.reset();
      }
    };
  }
}


