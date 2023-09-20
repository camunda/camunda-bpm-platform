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
package org.camunda.bpm.qa.rolling.update.scenarios.timestamp;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;
import org.camunda.bpm.qa.upgrade.Times;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Nikola Koevski
 */
public class JobTimestampsUpdateScenario extends AbstractTimestampUpdateScenario {

  protected static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
  protected static final Date LOCK_EXP_TIME = new Date(TIME + 300_000L);
  protected static final String PROCESS_DEFINITION_KEY = "jobTimestampsUpdateTestProcess";

  protected static final BpmnModelInstance SINGLE_JOB_MODEL = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .camundaHistoryTimeToLive(180)
      .startEvent("start")
      .intermediateCatchEvent("catch")
      .timerWithDate(SDF.format(TIMESTAMP))
      .endEvent("end")
      .done();

  @DescribesScenario("initJobTimestamps")
  @Times(1)
  public static ScenarioSetup initJobTimestamps() {
    return new ScenarioSetup() {
      @Override
      public void execute(final ProcessEngine processEngine, String scenarioName) {

        ClockUtil.setCurrentTime(TIMESTAMP);

        deployModel(processEngine, PROCESS_DEFINITION_KEY, PROCESS_DEFINITION_KEY, SINGLE_JOB_MODEL);

        final String processInstanceId = processEngine.getRuntimeService()
          .startProcessInstanceByKey(PROCESS_DEFINITION_KEY, scenarioName)
          .getId();

        ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration())
          .getCommandExecutorTxRequired()
          .execute(new Command<Void>() {
            @Override
            public Void execute(CommandContext commandContext) {

              JobEntity job = (JobEntity) processEngine.getManagementService()
                .createJobQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

              job.setLockExpirationTime(LOCK_EXP_TIME);

              commandContext.getJobManager()
                .updateJob(job);

              return null;
            }
          });

        ClockUtil.reset();
      }
    };
  }
}