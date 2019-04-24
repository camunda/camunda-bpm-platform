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
package org.camunda.bpm.qa.rolling.update.scenarios.cleanup;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;
import org.camunda.bpm.qa.upgrade.Times;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Tassilo Weidner
 */
public class HistoryCleanupScenario {

  static final Date FIXED_DATE = new Date(1363608000000L);

  @Deployment
  public static String deploy() {
    return "org/camunda/bpm/qa/rolling/update/cleanup/oneTaskProcess.bpmn20.xml";
  }

  @DescribesScenario("initHistoryCleanup")
  @Times(1)
  public static ScenarioSetup initHistoryCleanup() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {

        for (int i = 0; i < 60; i++) {
          if (i % 4 == 0) {
            ClockUtil.setCurrentTime(FIXED_DATE);

            engine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess_710", "HistoryCleanupScenario");

            String taskId = engine.getTaskService().createTaskQuery()
              .processInstanceBusinessKey("HistoryCleanupScenario")
              .singleResult()
              .getId();


            ClockUtil.setCurrentTime(addMinutes(FIXED_DATE, i));

            engine.getTaskService().complete(taskId);
          }
        }

        ProcessEngineConfigurationImpl configuration =
          ((ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration());

        configuration.setHistoryCleanupBatchWindowStartTime("13:00");
        configuration.setHistoryCleanupBatchWindowEndTime("14:00");
        configuration.setHistoryCleanupDegreeOfParallelism(3);
        configuration.initHistoryCleanup();

        engine.getHistoryService().cleanUpHistoryAsync();

        List<Job> jobs = engine.getHistoryService().findHistoryCleanupJobs();

        for (int i = 0; i < 4; i++) {
          Job jobOne = jobs.get(0);
          engine.getManagementService().executeJob(jobOne.getId());

          Job jobTwo = jobs.get(1);
          engine.getManagementService().executeJob(jobTwo.getId());

          Job jobThree = jobs.get(2);
          engine.getManagementService().executeJob(jobThree.getId());
        }

        ClockUtil.reset();
      }
    };
  }

  protected static Date addMinutes(Date initialDate, int minutes) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(initialDate);
    calendar.add(Calendar.MINUTE, minutes);
    return calendar.getTime();
  }

}
