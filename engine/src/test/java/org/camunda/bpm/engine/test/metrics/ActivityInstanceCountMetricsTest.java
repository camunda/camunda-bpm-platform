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
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.Bpmn;

/**
 * @author Daniel Meyer
 *
 */
public class ActivityInstanceCountMetricsTest extends PluggableProcessEngineTestCase {

  public void testBpmnActivityInstances() {
    deployment(Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .manualTask()
      .endEvent()
    .done());

    // given
    // that no activity instances have been executed
    assertEquals(0l, managementService.createMetricsQuery()
      .name(Metrics.ACTIVTY_INSTANCE_END)
      .sum());

    // if
    // a process instance is started
    runtimeService.startProcessInstanceByKey("testProcess");

    // then
    // the increased count is immediately visible
    assertEquals(3l, managementService.createMetricsQuery()
        .name(Metrics.ACTIVTY_INSTANCE_END)
        .sum());

    // and force the db metrics reporter to report
    processEngineConfiguration.getDbMetricsReporter().reportNow();

    // still 3
    assertEquals(3l, managementService.createMetricsQuery()
        .name(Metrics.ACTIVTY_INSTANCE_END)
        .sum());

    managementService.deleteMetrics(null);
  }

  public void testStandaloneTask() {

    // given
    // that no activity instances have been executed
    assertEquals(0l, managementService.createMetricsQuery()
      .name(Metrics.ACTIVTY_INSTANCE_END)
      .sum());

    // if
    // I complete a standalone task
    Task task = taskService.newTask();
    taskService.saveTask(task);
    taskService.complete(task.getId());

    // then
    // the increased count is immediately visible
    assertEquals(1l, managementService.createMetricsQuery()
        .name(Metrics.ACTIVTY_INSTANCE_END)
        .sum());

    // and force the db metrics reporter to report
    processEngineConfiguration.getDbMetricsReporter().reportNow();

    // still 1
    assertEquals(1l, managementService.createMetricsQuery()
        .name(Metrics.ACTIVTY_INSTANCE_END)
        .sum());

    managementService.deleteMetrics(null);

    // clean up
    HistoricTaskInstance hti = historyService.createHistoricTaskInstanceQuery().singleResult();
    historyService.deleteHistoricTaskInstance(hti.getId());

    List<UserOperationLogEntry> uoles = historyService.createUserOperationLogQuery().list();
    for (UserOperationLogEntry uole : uoles) {
      historyService.deleteUserOperationLogEntry(uole.getId());
    }

  }

}
