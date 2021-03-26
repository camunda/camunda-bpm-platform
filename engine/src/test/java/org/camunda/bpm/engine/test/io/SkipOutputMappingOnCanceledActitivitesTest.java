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
package org.camunda.bpm.engine.test.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.After;
import org.junit.Test;

public class SkipOutputMappingOnCanceledActitivitesTest extends PluggableProcessEngineTest {

  protected static final String WORKER_ID = "aWorkerId";
  protected static final long LOCK_TIME = 10000L;
  protected static final String TOPIC_NAME = "externalTaskTopic";

  @After
  public void tearDown() {
    processEngineConfiguration.setSkipOutputMappingOnCanceledActivities(false);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/io/SkipOutputMappingOnCanceledActitivitesTest.oneExternalTaskWithOutputMappingAndCatchingErrorBoundaryEvent.bpmn")
  public void shouldSkipOutputMappingOnBpmnErrorAtExternalTask() {
    // given a process with one external task which has output mapping configured
    processEngineConfiguration.setSkipOutputMappingOnCanceledActivities(true);
    runtimeService.startProcessInstanceByKey("externalTaskProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertThat(externalTasks).hasSize(1);
    externalTaskService.handleBpmnError(externalTasks.get(0).getId(), WORKER_ID, "errorCode", null);

    // then
    // expect no mapping failure
    // error was caught
    // output mapping is skipped
    Task userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
  }


  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldSkipOutputMappingOnBpmnErrorAtExternalTaskWithUncaughtError() {
    // given a process with one external task which has output mapping configured
    processEngineConfiguration.setSkipOutputMappingOnCanceledActivities(true);
    testRule.deploy(Bpmn.createExecutableProcess("process")
        .startEvent()
        .serviceTask("service")
          .camundaExternalTask(TOPIC_NAME)
          .camundaOutputParameter("foo", "bar")
          .camundaErrorEventDefinition().expression("${true}").error("501", "intentionally").errorEventDefinitionDone()
        .userTask()
        .endEvent("regular")
        .done());
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process");

    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertThat(externalTasks).hasSize(1);

    // when an uncaught BPMN error is thrown
//    externalTaskService.handleFailure(externalTasks.get(0).getId(), WORKER_ID, null, 0, 0);
    externalTaskService.complete(externalTasks.get(0).getId(), WORKER_ID);

    // then the process instance ended
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0L);
    HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(instance.getId()).singleResult();
    assertThat(historicInstance).isNotNull();
    assertThat(historicInstance.getState()).isEqualTo(HistoricProcessInstance.STATE_COMPLETED);
  }

  @Test
  public void shouldSkipOutputMappingOnBpmnErrorAtExternalTasWithUncaughtErrorAsyncAfter() {
    // given a process with one async-after external task which has output mapping configured
    processEngineConfiguration.setSkipOutputMappingOnCanceledActivities(true);
    testRule.deploy(Bpmn.createExecutableProcess("process")
        .startEvent()
        .serviceTask("service")
          .camundaExternalTask(TOPIC_NAME)
          .camundaOutputParameter("foo", "bar")
          .camundaAsyncAfter()
          .camundaExclusive(true)
          .camundaErrorEventDefinition().expression("${true}").error("501", "intentionally").errorEventDefinitionDone()
        .userTask()
        .endEvent("regular")
        .done());
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process");

    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertThat(externalTasks).hasSize(1);

//    externalTaskService.handleFailure(externalTasks.get(0).getId(), WORKER_ID, null, 0, 0);
    externalTaskService.complete(externalTasks.get(0).getId(), WORKER_ID);
    assertThat(managementService.createJobQuery().count()).isEqualTo(1L);

    // when an uncaught BPMN error is thrown
    testRule.executeAvailableJobs();

    // then no incident is created and the instance has ended
    assertThat(runtimeService.createIncidentQuery().count()).isEqualTo(0L);
    HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(instance.getId()).singleResult();
    assertThat(historicInstance).isNotNull();
    assertThat(historicInstance.getState()).isEqualTo(HistoricProcessInstance.STATE_COMPLETED);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/io/SkipOutputMappingOnCanceledActitivitesTest.oneExternalTaskWithOutputMappingAndCatchingErrorBoundaryEvent.bpmn")
  public void shouldNotSkipOutputMappingOnBpmnErrorAtExternalTask() {
    // given a process with one external task which has output mapping configured
    processEngineConfiguration.setSkipOutputMappingOnCanceledActivities(false);
    runtimeService.startProcessInstanceByKey("externalTaskProcess");

    // when/then
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertThat(externalTasks).hasSize(1);
    assertThatThrownBy(() -> externalTaskService.handleBpmnError(externalTasks.get(0).getId(), WORKER_ID, "errorCode", null))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Propagation of bpmn error errorCode failed.");
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/io/SkipOutputMappingOnCanceledActitivitesTest.oneSubprocessWithOutputMappingAndCatchingErrorBoundaryEvent.bpmn")
  public void shouldSkipOutputMappingOnBpmnErrorInSubprocess() {
    // given a process with one external task which has output mapping configured
    processEngineConfiguration.setSkipOutputMappingOnCanceledActivities(true);
    runtimeService.startProcessInstanceByKey("subProcess");

    // when
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getName()).isEqualTo("userTask in Subprocess");
    taskService.handleBpmnError(task.getId(), "errorCode");

    // then
    // expect no mapping failure
    // error was caught
    // output mapping is skipped
    Task userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getName()).isEqualTo("userTask");
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/io/SkipOutputMappingOnCanceledActitivitesTest.oneSubprocessWithOutputMappingAndCatchingErrorBoundaryEvent.bpmn")
  public void shouldNotSkipOutputMappingOnBpmnErrorInSubprocess() {
    // given a process with one external task which has output mapping configured
    processEngineConfiguration.setSkipOutputMappingOnCanceledActivities(false);
    runtimeService.startProcessInstanceByKey("subProcess");

    // when/then
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getName()).isEqualTo("userTask in Subprocess");

    assertThatThrownBy(() -> taskService.handleBpmnError(task.getId(), "errorCode"))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Propagation of bpmn error errorCode failed.");
  }
}
