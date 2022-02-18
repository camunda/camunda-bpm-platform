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
package org.camunda.bpm.qa.upgrade.scenarios7160.externaltask;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@ScenarioUnderTest("ExternalTaskFailureLogScenario")
@Origin("7.16.0")
public class ExternalTaskFailureLogTest {

  @Rule
  public UpgradeTestRule engineRule = new UpgradeTestRule();

  ExternalTaskService externalTaskService;

  @Before
  public void assignServices() {
    externalTaskService = engineRule.getExternalTaskService();
  }

  @Test
  @ScenarioUnderTest("failedTaskWithRetries.1")
  public void shouldCreateIncidentSuccessfully() {
    // given
    ProcessInstance processInstance = engineRule.processInstance();

    ExternalTask task = externalTaskService.createExternalTaskQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();

    // when
    externalTaskService.setRetries(task.getId(), 0);

    // then
    Incident incident = engineRule.incidentQuery().singleResult();

    // the history configuration is null because the failure log field
    // in the job is not populated in this update case
    assertThat(incident.getHistoryConfiguration()).isNull();

    // the other incident properties are not affected
    assertThat(incident.getIncidentMessage()).isNotNull();

    // and for the historic incident it is the same
    HistoricIncident historicIncident = engineRule.historicIncidentQuery().singleResult();

    assertThat(historicIncident.getHistoryConfiguration()).isNull();
    assertThat(historicIncident.getIncidentMessage()).isNotNull();
  }

}
