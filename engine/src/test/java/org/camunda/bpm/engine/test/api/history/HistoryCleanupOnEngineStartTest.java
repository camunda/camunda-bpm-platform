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

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupHelper;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

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

  public ProcessEngineRule engineRule = new ProcessEngineRule(true);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void init() {
    historyService = processEngine.getHistoryService();
  }

  @After
  public void clearDatabase(){
    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        List<Job> jobs = processEngine.getManagementService().createJobQuery().list();
        if (jobs.size() > 0) {
          assertEquals(1, jobs.size());
          String jobId = jobs.get(0).getId();
          commandContext.getJobManager().deleteJob((JobEntity) jobs.get(0));
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobId);
        }

        commandContext.getMeterLogManager().deleteAll();

        return null;
      }
    });

    Collection<Meter> meters = processEngineConfiguration.getMetricsRegistry().getMeters().values();
    for (Meter meter : meters) {
      meter.getAndClear();
    }
    processEngine.getManagementService().deleteMetrics(null);

  }


  @Test
  @Ignore("CAM-7541")
  public void testHistoryCleanupJob() throws ParseException {
    Job historyCleanupJob = historyService.findHistoryCleanupJob();
    assertNotNull(historyCleanupJob);
    Date historyCleanupBatchWindowStartTime = ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration())
        .getHistoryCleanupBatchWindowStartTimeAsDate();
    assertEquals(HistoryCleanupHelper.getNextRunWithinBatchWindow(ClockUtil.getCurrentTime(),
        historyCleanupBatchWindowStartTime), historyCleanupJob.getDuedate());
  }

}
