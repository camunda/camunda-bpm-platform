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
package org.camunda.bpm.engine.test.api.history.removaltime.cleanup;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.apache.commons.lang3.time.DateUtils.addSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandlerConfiguration.START_DELAY;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Tassilo Weidner
 */
public class HistoryCleanupSchedulerBatchesTest extends AbstractHistoryCleanupSchedulerTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
      configure(configuration, HistoryEventTypes.BATCH_START, HistoryEventTypes.BATCH_END));
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Before
  public void init() {
    engineConfiguration = engineRule.getProcessEngineConfiguration();
    initEngineConfiguration(engineConfiguration);

    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();

    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
  }

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected TaskService taskService;

  protected final String PROCESS_KEY = "process";
  protected final BpmnModelInstance PROCESS = Bpmn.createExecutableProcess(PROCESS_KEY)
    .camundaHistoryTimeToLive(5)
    .startEvent()
      .userTask()
    .endEvent().done();

  protected final String CALLING_PROCESS_KEY = "callingProcess";
  protected final BpmnModelInstance CALLING_PROCESS = Bpmn.createExecutableProcess(CALLING_PROCESS_KEY)
    .startEvent()
      .callActivity()
        .calledElement(PROCESS_KEY)
          .multiInstance()
            .cardinality("5")
          .multiInstanceDone()
    .endEvent().done();

  protected final Date END_DATE = new Date(1363608000000L);

  @Test
  public void shouldScheduleToNow() {
    // given
    engineConfiguration.setBatchOperationHistoryTimeToLive("P5D");
    engineConfiguration.setHistoryCleanupBatchSize(5);
    engineConfiguration.initHistoryCleanup();

    testRule.deploy(PROCESS);

    testRule.deploy(CALLING_PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY).getId();

    for (int i = 0; i < 5; i++) {
      String processInstanceId = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY).list().get(0).getId();
      runtimeService.deleteProcessInstancesAsync(Collections.singletonList(processInstanceId), "aDeleteReason");

      ClockUtil.setCurrentTime(END_DATE);

      String jobId = managementService.createJobQuery().singleResult().getId();
      managementService.executeJob(jobId);
      jobIds.add(jobId);

      List<Job> jobs = managementService.createJobQuery().list();
      for (Job job : jobs) {
        managementService.executeJob(job.getId());
        jobIds.add(job.getId());
      }
    }

    Date removalTime = addDays(END_DATE, 5);
    ClockUtil.setCurrentTime(removalTime);

    // when
    runHistoryCleanup();

    Job job = historyService.findHistoryCleanupJobs().get(0);

    // then
    assertThat(job.getDuedate()).isEqualTo(removalTime);
  }

  @Test
  public void shouldScheduleToLater() {
    // given
    engineConfiguration.setBatchOperationHistoryTimeToLive("P5D");
    engineConfiguration.setHistoryCleanupBatchSize(6);
    engineConfiguration.initHistoryCleanup();

    testRule.deploy(PROCESS);

    testRule.deploy(CALLING_PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY).getId();

    for (int i = 0; i < 5; i++) {
        String processInstanceId = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY).list().get(0).getId();
      runtimeService.deleteProcessInstancesAsync(Collections.singletonList(processInstanceId), "aDeleteReason");

      ClockUtil.setCurrentTime(END_DATE);

      String jobId = managementService.createJobQuery().singleResult().getId();
      managementService.executeJob(jobId);
      jobIds.add(jobId);

      List<Job> jobs = managementService.createJobQuery().list();
      for (Job job : jobs) {
        managementService.executeJob(job.getId());
        jobIds.add(job.getId());
      }
    }

    Date removalTime = addDays(END_DATE, 5);
    ClockUtil.setCurrentTime(removalTime);

    // when
    runHistoryCleanup();

    Job job = historyService.findHistoryCleanupJobs().get(0);

    // then
    assertThat(job.getDuedate()).isEqualTo(addSeconds(removalTime, START_DELAY));
  }

}
