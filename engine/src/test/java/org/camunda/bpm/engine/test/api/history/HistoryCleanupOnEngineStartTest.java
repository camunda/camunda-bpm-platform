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

package org.camunda.bpm.engine.test.api.history;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandlerConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Svetlana Dorokhova
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoryCleanupOnEngineStartTest extends ResourceProcessEngineTestCase {

  protected static final String ONE_TASK_PROCESS = "oneTaskProcess";

  private HistoryService historyService;

  public HistoryCleanupOnEngineStartTest() {
    super("org/camunda/bpm/engine/test/api/history/historyCleanupConfigurationTest.cfg.xml");
  }

  @Before
  public void init() {
    historyService = processEngine.getHistoryService();
  }

  @Test
  public void testHistoryCleanupJob() {
    Job historyCleanupJob = historyService.findHistoryCleanupJob();
    assertNotNull(historyCleanupJob);
    HistoryCleanupJobHandlerConfiguration configuration = getConfiguration((JobEntity) historyCleanupJob);
    assertEquals(444, configuration.getBatchSize());
    assertEquals(11, configuration.getBatchSizeThreshold());
    assertEquals(DateTimeUtil.getLocalTimeWithoutSecondsFormater().parseDateTime("23:00"), configuration.getBatchWindowStartTime());
    assertEquals(DateTimeUtil.getLocalTimeWithoutSecondsFormater().parseDateTime("01:00"), configuration.getBatchWindowEndTime());

    assertEquals(configuration.getNextRunWithinBatchWindow(ClockUtil.getCurrentTime()), historyCleanupJob.getDuedate());
  }

  private HistoryCleanupJobHandlerConfiguration getConfiguration(JobEntity jobEntity) {
    String jobHandlerConfigurationRaw = jobEntity.getJobHandlerConfigurationRaw();
    return HistoryCleanupJobHandlerConfiguration.fromJson(new JSONObject(jobHandlerConfigurationRaw));
  }

}
