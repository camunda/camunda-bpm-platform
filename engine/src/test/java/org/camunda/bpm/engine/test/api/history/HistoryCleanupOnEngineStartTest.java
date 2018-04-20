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
import java.util.Date;
import java.util.List;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupHelper;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author Svetlana Dorokhova
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
public class HistoryCleanupOnEngineStartTest {

  protected static final String ONE_TASK_PROCESS = "oneTaskProcess";

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    @Override
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      configuration.setHistoryCleanupBatchWindowStartTime("23:00");
      configuration.setHistoryCleanupBatchWindowEndTime("01:00");
      return configuration;
    }
  };

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  private HistoryService historyService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(testRule);

  @Before
  public void init() {
    historyService = engineRule.getProcessEngine().getHistoryService();
  }

  @After
  public void clearDatabase(){
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl)engineRule.getProcessEngine().getProcessEngineConfiguration();
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        List<Job> jobs = engineRule.getProcessEngine().getManagementService().createJobQuery().list();
        if (jobs.size() > 0) {
          assertEquals(1, jobs.size());
          String jobId = jobs.get(0).getId();
          commandContext.getJobManager().deleteJob((JobEntity) jobs.get(0));
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobId);
        }

        return null;
      }
    });
  }


  @Test
  public void testHistoryCleanupJob() throws ParseException {
    final List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();
    assertFalse(historyCleanupJobs.isEmpty());
    Date historyCleanupBatchWindowStartTime = ((ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration())
        .getHistoryCleanupBatchWindowStartTimeAsDate();
    Date historyCleanupBatchWindowEndTime = ((ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration())
        .getHistoryCleanupBatchWindowEndTimeAsDate();
    for (Job historyCleanupJob : historyCleanupJobs) {
      assertEquals(HistoryCleanupHelper.getCurrentOrNextBatchWindowStartTime(ClockUtil.getCurrentTime(), historyCleanupBatchWindowStartTime,
        HistoryCleanupHelper.addDays(historyCleanupBatchWindowEndTime, 1)), historyCleanupJob.getDuedate());
    }
  }

}
