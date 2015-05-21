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

import java.util.Collection;
import java.util.Date;

import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.model.bpmn.Bpmn;

/**
 * @author Daniel Meyer
 *
 */
public class MetricsTest extends PluggableProcessEngineTestCase {

  @Override
  protected void setUp() throws Exception {
    Collection<Meter> meters = processEngineConfiguration.getMetricsRegistry().getMeters().values();
    for (Meter meter : meters) {
      meter.getAndClear();
    }
    managementService.deleteMetrics(null);
  }

  public void testDeleteMetrics() {
    deployment(Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .manualTask()
      .endEvent()
    .done());

    // given
    runtimeService.startProcessInstanceByKey("testProcess");
    processEngineConfiguration.getDbMetricsReporter().reportNow();

    // a count of three
    assertEquals(3l, managementService.createMetricsQuery()
        .sum());

    // if
    // we delete with timestamp "null"
    managementService.deleteMetrics(null);

    // then
    // all entries are deleted
    assertEquals(0l, managementService.createMetricsQuery()
        .name(Metrics.ACTIVTY_INSTANCE_START)
        .sum());
  }

  public void testDeleteMetricsWithTimestamp() {
    deployment(Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .manualTask()
      .endEvent()
    .done());

    // given
    runtimeService.startProcessInstanceByKey("testProcess");
    processEngineConfiguration.getDbMetricsReporter().reportNow();

    // a count of three
    assertEquals(3l, managementService.createMetricsQuery()
        .sum());

    // if
    // we delete with timestamp older or equal to the timestamp of the log entry
    managementService.deleteMetrics(ClockUtil.getCurrentTime());

    // then
    // all entries are deleted
    assertEquals(0l, managementService.createMetricsQuery()
        .name(Metrics.ACTIVTY_INSTANCE_START)
        .sum());
  }

  public void testDeleteMetricsWithTimestampBefore() {
    deployment(Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .manualTask()
      .endEvent()
    .done());

    // given
    runtimeService.startProcessInstanceByKey("testProcess");
    processEngineConfiguration.getDbMetricsReporter().reportNow();

    // a count of three
    assertEquals(3l, managementService.createMetricsQuery()
        .sum());

    // if
    // we delete with timestamp before the timestamp of the log entry
    managementService.deleteMetrics(new Date(ClockUtil.getCurrentTime().getTime() - 10000));

    // then
    // the entires are NOT deleted
    assertEquals(3l, managementService.createMetricsQuery()
        .name(Metrics.ACTIVTY_INSTANCE_START)
        .sum());

    // cleanup
    managementService.deleteMetrics(null);
  }

  public void testQuery() {
    deployment(Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .manualTask()
      .endEvent()
    .done());

    // given
    runtimeService.startProcessInstanceByKey("testProcess");
    processEngineConfiguration.getDbMetricsReporter().reportNow();

    // then (query assertions)
    assertEquals(0l, managementService.createMetricsQuery().name("UNKNOWN").sum());
    assertEquals(3l, managementService.createMetricsQuery().name(Metrics.ACTIVTY_INSTANCE_START).sum());

    assertEquals(3l, managementService.createMetricsQuery().sum());
    assertEquals(3l, managementService.createMetricsQuery().startDate(new Date(1000)).sum());
    assertEquals(3l, managementService.createMetricsQuery().startDate(new Date(1000)).endDate(ClockUtil.getCurrentTime()).sum());
    assertEquals(0l, managementService.createMetricsQuery().startDate(new Date(ClockUtil.getCurrentTime().getTime() + 1000l)).sum());
    assertEquals(0l, managementService.createMetricsQuery().startDate(new Date(ClockUtil.getCurrentTime().getTime() + 1000l)).endDate(ClockUtil.getCurrentTime()).sum());

    // given
    runtimeService.startProcessInstanceByKey("testProcess");
    processEngineConfiguration.getDbMetricsReporter().reportNow();

    // then (query assertions)
    assertEquals(6l, managementService.createMetricsQuery().sum());
    assertEquals(6l, managementService.createMetricsQuery().startDate(new Date(1000)).sum());
    assertEquals(6l, managementService.createMetricsQuery().startDate(new Date(1000)).endDate(ClockUtil.getCurrentTime()).sum());
    assertEquals(0l, managementService.createMetricsQuery().startDate(new Date(ClockUtil.getCurrentTime().getTime() + 1000l)).sum());
    assertEquals(0l, managementService.createMetricsQuery().startDate(new Date(ClockUtil.getCurrentTime().getTime() + 1000l)).endDate(ClockUtil.getCurrentTime()).sum());

    // cleanup
    managementService.deleteMetrics(null);
  }

}
