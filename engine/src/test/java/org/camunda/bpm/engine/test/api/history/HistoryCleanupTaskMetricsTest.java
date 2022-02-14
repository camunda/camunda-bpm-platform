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
package org.camunda.bpm.engine.test.api.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_CLEANUP_STRATEGY_END_TIME_BASED;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.time.DateUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class HistoryCleanupTaskMetricsTest {

  private static final String DEFAULT_TTL_DAYS = "P5D";

  private static final BpmnModelInstance PROCESS = Bpmn.createExecutableProcess("process")
      .startEvent("start")
      .userTask("userTask1")
      .sequenceFlowId("seq")
      .userTask("userTask2")
      .endEvent("end")
      .done();

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
      configuration.setTaskMetricsEnabled(true).setHistoryCleanupDegreeOfParallelism(3));

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected HistoryService historyService;
  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected TaskService taskService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void init() {
    historyService = engineRule.getHistoryService();
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    taskService = engineRule.getTaskService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();

    processEngineConfiguration.setHistoryCleanupStrategy(HISTORY_CLEANUP_STRATEGY_END_TIME_BASED);
  }

  @After
  public void clearDatabase() {
    testRule.deleteHistoryCleanupJobs();
    managementService.deleteTaskMetrics(null);
    managementService.deleteMetrics(null);
  }

  @After
  public void resetConfiguration() {
    processEngineConfiguration.setHistoryCleanupStrategy(HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED);
    processEngineConfiguration.setTaskMetricsTimeToLive(null);
  }

  @Test
  public void shouldCleanupTaskMetrics() {
    // given
    initTaskMetricHistoryTimeToLive(DEFAULT_TTL_DAYS);
    int daysInThePast = -11;

    prepareTaskMetrics(3, daysInThePast);

    // assume
    assertThat(managementService.getUniqueTaskWorkerCount(null, null)).isEqualTo(3L);

    // when
    runHistoryCleanup();

    // then
    assertThat(managementService.getUniqueTaskWorkerCount(null, null)).isZero();
  }

  @Test
  public void shouldProvideCleanupMetricsForTaskMetrics() {
    // given
    initTaskMetricHistoryTimeToLive(DEFAULT_TTL_DAYS);
    int daysInThePast = -11;
    int metricsCount = 5;

    prepareTaskMetrics(metricsCount, daysInThePast);

    // when
    runHistoryCleanup();

    // then
    final long removedMetrics = managementService.createMetricsQuery().name(Metrics.HISTORY_CLEANUP_REMOVED_TASK_METRICS).sum();
    assertThat(removedMetrics).isEqualTo(metricsCount);
  }

  @Test
  public void shouldFailWithInvalidConfiguration() {
    // given
    processEngineConfiguration.setTaskMetricsTimeToLive("PD");

    // when/then
    assertThatThrownBy(() -> processEngineConfiguration.initHistoryCleanup())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid value");
  }

  @Test
  public void shouldFailWithInvalidConfigurationNegativeTTL() {
    // given
    processEngineConfiguration.setTaskMetricsTimeToLive("P-1D");

    // when/then
    assertThatThrownBy(() -> processEngineConfiguration.initHistoryCleanup())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid value");
  }

  private void initTaskMetricHistoryTimeToLive(String days) {
    processEngineConfiguration.setTaskMetricsTimeToLive(days);
    processEngineConfiguration.initHistoryCleanup();
  }

  private void prepareTaskMetrics(int taskMetricsCount, int daysInThePast) {
    Date startDate = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, daysInThePast));

    testRule.deploy(PROCESS);
    runtimeService.startProcessInstanceByKey("process");
    String taskId = taskService.createTaskQuery().singleResult().getId();

    for (int i = 0; i < taskMetricsCount; i++) {
      taskService.setAssignee(taskId, "kermit" + i);
    }

    ClockUtil.reset();
  }

  private void runHistoryCleanup() {
    historyService.cleanUpHistoryAsync(true);
    final List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();
    for (Job historyCleanupJob: historyCleanupJobs) {
      managementService.executeJob(historyCleanupJob.getId());
    }
  }

}
