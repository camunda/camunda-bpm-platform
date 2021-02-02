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
package org.camunda.bpm.engine.test.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.util.ClockUtil;
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class MetricsManagerForCleanupTest {

  private static final BpmnModelInstance PROCESS = Bpmn.createExecutableProcess("process")
      .startEvent("start")
      .userTask("userTask1")
      .sequenceFlowId("seq")
      .userTask("userTask2")
      .endEvent("end")
      .done();

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
      configuration.setTaskMetricsEnabled(true));

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ManagementService managementService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    taskService = engineRule.getTaskService();
  }

  @After
  public void clearDatabase() {
    testRule.deleteHistoryCleanupJobs();
    managementService.deleteTaskMetrics(null);
  }

  @Parameterized.Parameter(0)
  public int taskMetricHistoryTTL;

  @Parameterized.Parameter(1)
  public int metric1DaysInThePast;

  @Parameterized.Parameter(2)
  public int metric2DaysInThePast;

  @Parameterized.Parameter(3)
  public int batchSize;

  @Parameterized.Parameter(4)
  public int resultCount;

  @Parameterized.Parameters
  public static Collection<Object[]> scenarios() {
    return Arrays.asList(new Object[][] {
        // all historic batches are old enough to be cleaned up
        { 5, -6, -7, 50, 2 },
        // one batch should be cleaned up
        { 5, -3, -7, 50, 1 },
        // not enough time has passed
        { 5, -3, -4, 50, 0 },
        // batchSize will reduce the result
        { 5, -6, -7, 1, 1 } });
  }

  @Test
  public void testFindHistoricBatchIdsForCleanup() {
    // given
    prepareTaskMetrics();

    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Object>() {
      @Override
      public Object execute(CommandContext commandContext) {
        // when
        List<String> taskMetricIdsForCleanup = commandContext.getMeterLogManager()
            .findTaskMetricsForCleanup(batchSize, taskMetricHistoryTTL, 0, 59);

        // then
        assertThat(taskMetricIdsForCleanup.size()).isEqualTo(resultCount);

        return null;
      }
    });
  }

  private void prepareTaskMetrics() {
    testRule.deploy(PROCESS);
    runtimeService.startProcessInstanceByKey("process");

    String taskId = taskService.createTaskQuery().singleResult().getId();

    ClockUtil.offset(TimeUnit.DAYS.toMillis(metric1DaysInThePast));
    taskService.setAssignee(taskId, "kermit");

    ClockUtil.offset(TimeUnit.DAYS.toMillis(metric2DaysInThePast));
    taskService.setAssignee(taskId, "gonzo");

    ClockUtil.reset();
  }
}
