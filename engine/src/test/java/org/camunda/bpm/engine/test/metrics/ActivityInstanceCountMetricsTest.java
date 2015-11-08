/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.metrics;

import java.util.List;

import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.model.bpmn.Bpmn;

/**
 * @author Daniel Meyer
 *
 */
public class ActivityInstanceCountMetricsTest extends AbstractMetricsTest {

  public void testBpmnActivityInstances() {
    deployment(Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .manualTask()
      .endEvent()
    .done());

    // given
    // that no activity instances have been executed
    assertEquals(0l, managementService.createMetricsQuery()
      .name(Metrics.ACTIVTY_INSTANCE_START)
      .sum());

    // if
    // a process instance is started
    runtimeService.startProcessInstanceByKey("testProcess");

    // then
    // the increased count is immediately visible
    assertEquals(3l, managementService.createMetricsQuery()
        .name(Metrics.ACTIVTY_INSTANCE_START)
        .sum());

    // and force the db metrics reporter to report
    processEngineConfiguration.getDbMetricsReporter().reportNow();

    // still 3
    assertEquals(3l, managementService.createMetricsQuery()
        .name(Metrics.ACTIVTY_INSTANCE_START)
        .sum());
  }

  public void testStandaloneTask() {

    // given
    // that no activity instances have been executed
    assertEquals(0l, managementService.createMetricsQuery()
      .name(Metrics.ACTIVTY_INSTANCE_START)
      .sum());

    // if
    // I complete a standalone task
    Task task = taskService.newTask();
    taskService.saveTask(task);

    // then
    // the increased count is immediately visible
    assertEquals(1l, managementService.createMetricsQuery()
        .name(Metrics.ACTIVTY_INSTANCE_START)
        .sum());

    // and force the db metrics reporter to report
    processEngineConfiguration.getDbMetricsReporter().reportNow();

    // still 1
    assertEquals(1l, managementService.createMetricsQuery()
        .name(Metrics.ACTIVTY_INSTANCE_START)
        .sum());

    taskService.deleteTask(task.getId());

    // clean up
    HistoricTaskInstance hti = historyService.createHistoricTaskInstanceQuery().singleResult();
    if(hti!=null) {
      historyService.deleteHistoricTaskInstance(hti.getId());
    }
  }

  @Deployment
  public void testCmmnActivitiyInstances() {
    // given
    // that no activity instances have been executed
    assertEquals(0l, managementService.createMetricsQuery()
      .name(Metrics.ACTIVTY_INSTANCE_START)
      .sum());

    caseService.createCaseInstanceByKey("case");

    assertEquals(0l, managementService.createMetricsQuery()
        .name(Metrics.ACTIVTY_INSTANCE_START)
        .sum());

    // start PI_HumanTask_1 and PI_Milestone_1
    List<CaseExecution> list = caseService.createCaseExecutionQuery().enabled().list();
    for (CaseExecution caseExecution : list) {
      caseService.withCaseExecution(caseExecution.getId())
        .manualStart();
    }

    assertEquals(2l, managementService.createMetricsQuery()
        .name(Metrics.ACTIVTY_INSTANCE_START)
        .sum());

    // and force the db metrics reporter to report
    processEngineConfiguration.getDbMetricsReporter().reportNow();

    // still 2
    assertEquals(2l, managementService.createMetricsQuery()
        .name(Metrics.ACTIVTY_INSTANCE_START)
        .sum());

    // trigger the milestone
    CaseExecution taskExecution = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    caseService.completeCaseExecution(taskExecution.getId());

    // milestone is counted
    assertEquals(3l, managementService.createMetricsQuery()
        .name(Metrics.ACTIVTY_INSTANCE_START)
        .sum());

  }

}
