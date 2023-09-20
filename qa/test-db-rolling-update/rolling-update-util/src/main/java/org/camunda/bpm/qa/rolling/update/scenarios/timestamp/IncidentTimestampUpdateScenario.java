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
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;
import org.camunda.bpm.qa.upgrade.Times;

/**
 * @author Nikola Koevski
 */
public class IncidentTimestampUpdateScenario extends AbstractTimestampUpdateScenario {

  protected static final String PROCESS_DEFINITION_KEY = "oneIncidentTimestampServiceTaskProcess";

  protected static final BpmnModelInstance FAILING_SERVICE_TASK_MODEL = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .camundaHistoryTimeToLive(180)
      .startEvent("start")
      .serviceTask("incidentTimestampTask")
      .camundaAsyncBefore()
      .camundaClass(FailingDelegate.class.getName())
      .endEvent("end")
      .done();

  @DescribesScenario("initIncidentTimestamp")
  @Times(1)
  public static ScenarioSetup initIncidentTimestamp() {
    return new ScenarioSetup() {
      @Override
      public void execute(ProcessEngine processEngine, String scenarioName) {

        ClockUtil.setCurrentTime(TIMESTAMP);

        deployModel(processEngine, PROCESS_DEFINITION_KEY, PROCESS_DEFINITION_KEY, FAILING_SERVICE_TASK_MODEL);

        String processInstanceId = processEngine.getRuntimeService()
          .startProcessInstanceByKey(PROCESS_DEFINITION_KEY, scenarioName)
          .getId();

        causeIncident(processEngine, processInstanceId);

        ClockUtil.reset();
      }
    };
  }

  private static void causeIncident(ProcessEngine processEngine, String processInstanceId) {

    Job job = processEngine.getManagementService()
      .createJobQuery()
      .processInstanceId(processInstanceId)
      .withRetriesLeft()
      .singleResult();

    if (job == null) {
      return;
    }

    try {
      processEngine.getManagementService().executeJob(job.getId());
    } catch (Exception ex) {
      // noop
    }

    causeIncident(processEngine, processInstanceId);
  }

  static class FailingDelegate implements JavaDelegate {

    public static final String EXCEPTION_MESSAGE = "Expected_exception.";

    @Override
    public void execute(DelegateExecution execution) throws Exception {

      Boolean fail = (Boolean) execution.getVariable("fail");

      if (fail == null || fail == true) {
        throw new ProcessEngineException(EXCEPTION_MESSAGE);
      }

    }

  }
}