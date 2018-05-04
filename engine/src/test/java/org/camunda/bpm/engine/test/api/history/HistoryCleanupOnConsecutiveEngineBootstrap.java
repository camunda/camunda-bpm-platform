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
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandlerConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

/**
 * @author Nikola Koevski
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoryCleanupOnConsecutiveEngineBootstrap {

  private static final String ENGINE_NAME = "engineWithHistoryCleanupBatchWindow";

  @Test
  public void testConsecutiveEngineBootstrapHistoryCleanupJobReconfiguration() {

    // given
    // create history cleanup job
    ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/batchwindow.camunda.cfg.xml")
      .buildProcessEngine()
      .close();

    // when
    // suspend history cleanup job
    ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/no-batchwindow.camunda.cfg.xml")
      .buildProcessEngine()
      .close();

    // then
    // reconfigure history cleanup job
    ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/batchwindow.camunda.cfg.xml");
    processEngineConfiguration.setProcessEngineName(ENGINE_NAME);
    ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();

    assertNotNull(ProcessEngines.getProcessEngine(ENGINE_NAME));

    closeProcessEngine(processEngine);
  }

  @Test
  public void testDecreaseNumberOfHistoryCleanupJobs() {
    // given
    // create history cleanup job
    ProcessEngine engine = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/history-cleanup-parallelism-default.camunda.cfg.xml")
      .buildProcessEngine();

    // assume
    ManagementService managementService = engine.getManagementService();
    assertEquals(4, managementService.createJobQuery().list().size());

    engine.close();

    // when
    engine = ProcessEngineConfiguration
    .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/history-cleanup-parallelism-less.camunda.cfg.xml")
      .buildProcessEngine();

    // then
    // reconfigure history cleanup job
    managementService = engine.getManagementService();
    assertEquals(1, managementService.createJobQuery().list().size());

    Job job = managementService.createJobQuery().singleResult();
    assertEquals(0, getHistoryCleanupJobHandlerConfiguration(job).getMinuteFrom());
    assertEquals(59, getHistoryCleanupJobHandlerConfiguration(job).getMinuteTo());

    closeProcessEngine(engine);
  }

  @Test
  public void testIncreaseNumberOfHistoryCleanupJobs() {
    // given
    // create history cleanup job
    ProcessEngine engine = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/history-cleanup-parallelism-default.camunda.cfg.xml")
      .buildProcessEngine();

    // assume
    ManagementService managementService = engine.getManagementService();
    assertEquals(4, managementService.createJobQuery().count());

    engine.close();

    // when
    engine = ProcessEngineConfiguration
    .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/history-cleanup-parallelism-more.camunda.cfg.xml")
      .buildProcessEngine();

    // then
    // reconfigure history cleanup job
    managementService = engine.getManagementService();
    List<Job> jobs = managementService.createJobQuery().list();
    assertEquals(8, jobs.size());

    for (Job job : jobs) {
      int minuteTo = getHistoryCleanupJobHandlerConfiguration(job).getMinuteTo();
      int minuteFrom = getHistoryCleanupJobHandlerConfiguration(job).getMinuteFrom();

      if (minuteFrom == 0) {
        assertEquals(6, minuteTo);
      }
      else if (minuteFrom == 7) {
        assertEquals(13, minuteTo);
      }
      else if (minuteFrom == 14) {
        assertEquals(20, minuteTo);
      }
      else if (minuteFrom == 21) {
        assertEquals(27, minuteTo);
      }
      else if (minuteFrom == 28) {
        assertEquals(34, minuteTo);
      }
      else if (minuteFrom == 35) {
        assertEquals(41, minuteTo);
      }
      else if (minuteFrom == 42) {
        assertEquals(48, minuteTo);
      }
      else if (minuteFrom == 49) {
        assertEquals(59, minuteTo);
      }
      else {
        fail("unexpected minute from " + minuteFrom);
      }
    }

    closeProcessEngine(engine);
  }

  protected HistoryCleanupJobHandlerConfiguration getHistoryCleanupJobHandlerConfiguration(Job job) {
    return HistoryCleanupJobHandlerConfiguration
          .fromJson(new JSONObject(((JobEntity) job).getJobHandlerConfigurationRaw()));
  }

  protected void closeProcessEngine(ProcessEngine processEngine) {
    ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    final HistoryService historyService = processEngine.getHistoryService();
    configuration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        List<Job> jobs = historyService.findHistoryCleanupJobs();
        for (Job job: jobs) {
          commandContext.getJobManager().deleteJob((JobEntity) job);
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(job.getId());
        }

        //cleanup "detached" historic job logs
        final List<HistoricJobLog> list = historyService.createHistoricJobLogQuery().list();
        for (HistoricJobLog jobLog: list) {
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobLog.getJobId());
        }

        commandContext.getMeterLogManager().deleteAll();

        return null;
      }
    });

    processEngine.close();
  }

}
