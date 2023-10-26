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

package org.camunda.bpm.engine.test.api.externaltask;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class ExternalTaskQueryByCreationDateTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(testHelper);

  protected ProcessEngine engine;

  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected HistoryService historyService;
  protected TaskService taskService;
  protected CaseService caseService;

  @Before
  public void initServices() {
    engine = engineRule.getProcessEngine();
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
    caseService = engineRule.getCaseService();

    // given
    deployProcessesWithExternalTasks();
  }

  @Test
  public void shouldHaveNonNullCreationDate() {
    // when
    runtimeService.startProcessInstanceByKey("process1");

    // then
    var result = engineRule.getExternalTaskService()
        .createExternalTaskQuery()
        .list();

    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).getCreationDate()).isNotNull();
  }

  @Test
  public void shouldProduceEventWithCreationDateValue() {
    // when
    runtimeService.startProcessInstanceByKey("process1");

    var extTask = engineRule.getExternalTaskService()
        .createExternalTaskQuery()
        .singleResult();

    var result = historyService.createHistoricExternalTaskLogQuery().list();

    assertThat(result.size()).isEqualTo(1);

    var historyEventTimestamp = result.get(0).getTimestamp();

    // then
    assertThat(extTask.getCreationDate()).isEqualTo(historyEventTimestamp);
  }

  @Test
  public void shouldReturnTasksInDescOrder() {
    // when
    runtimeService.startProcessInstanceByKey("process1");
    runtimeService.startProcessInstanceByKey("process2");

    var result = engineRule.getExternalTaskService()
        .createExternalTaskQuery()
        .orderByCreationDate().desc()
        .list();

    assertThat(result.size()).isEqualTo(2);

    var extTask1 = result.get(0);
    var extTask2 = result.get(1);

    // then
    assertThat(extTask2.getCreationDate())
        .isBefore(extTask1.getCreationDate());
  }

  @Test
  public void shouldReturnTasksInAScOrder() {
    // when
    runtimeService.startProcessInstanceByKey("process1");
    runtimeService.startProcessInstanceByKey("process2");

    var result = engineRule.getExternalTaskService()
        .createExternalTaskQuery()
        .orderByCreationDate().asc()
        .list();

    assertThat(result.size()).isEqualTo(2);

    var extTask1 = result.get(0);
    var extTask2 = result.get(1);

    // then
    assertThat(extTask1.getCreationDate())
        .isBefore(extTask2.getCreationDate());
  }

  private void deployProcessesWithExternalTasks() {
    var processWithExtTask = Bpmn.createExecutableProcess("process1")
        .startEvent()
        .serviceTask().camundaExternalTask("external-task-topic1")
        .endEvent()
        .done();

    var process2WithExtTask = Bpmn.createExecutableProcess("process2")
        .startEvent()
        .serviceTask().camundaExternalTask("external-task-topic2")
        .endEvent()
        .done();

    testHelper.deploy(processWithExtTask);
    testHelper.deploy(process2WithExtTask);
  }
}
