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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandlerConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoryCleanupDegreeOfParallelismTest {

  private static final String PROCESS_ENGINE_NAME = "historyCleanupJobsEngine";

  protected ProcessEngineConfigurationImpl configuration;

  protected HistoryService historyService;
  protected ManagementService managementService;

  @Before
  public void init() {
    configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
        .createStandaloneInMemProcessEngineConfiguration()
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName());
    configuration.setHistoryCleanupDegreeOfParallelism(3);
    configuration.setMetricsEnabled(false);
    configuration.setProcessEngineName(PROCESS_ENGINE_NAME);

    configuration.buildProcessEngine();

    historyService = configuration.getHistoryService();
    managementService = configuration.getManagementService();

    configuration.getRepositoryService()
        .createDeployment()
        .name("historyCleanupDeployment")
        .addClasspathResource("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
        .addClasspathResource("org/camunda/bpm/engine/test/api/dmn/Example.dmn")
        .addClasspathResource("org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithHistoryTimeToLive.cmmn")
        .deploy();
  }

  @After
  public void clearDatabase() {
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

        List<HistoricIncident> historicIncidents = historyService.createHistoricIncidentQuery().list();
        for (HistoricIncident historicIncident : historicIncidents) {
          commandContext.getDbEntityManager().delete((HistoricIncidentEntity) historicIncident);
        }

        commandContext.getMeterLogManager().deleteAll();

        return null;
      }
    });

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance historicProcessInstance: historicProcessInstances) {
      historyService.deleteHistoricProcessInstance(historicProcessInstance.getId());
    }

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();
    for (HistoricDecisionInstance historicDecisionInstance: historicDecisionInstances) {
      historyService.deleteHistoricDecisionInstanceByInstanceId(historicDecisionInstance.getId());
    }

    List<HistoricCaseInstance> historicCaseInstances = historyService.createHistoricCaseInstanceQuery().list();
    for (HistoricCaseInstance historicCaseInstance: historicCaseInstances) {
      historyService.deleteHistoricCaseInstance(historicCaseInstance.getId());
    }

    ProcessEngineImpl processEngine = configuration.getProcessEngine();
    processEngine.close();
    ProcessEngines.unregister(processEngine);

    // assume
    assertEquals(0, ProcessEngines.getProcessEngines().size());
    assertNull(ProcessEngines.getProcessEngines().get(PROCESS_ENGINE_NAME));
  }

  @Test
  public void testReconfigureDegreeOfParallelism() {
    // when
    historyService.cleanUpHistoryAsync(true);
    // then
    assertEquals(3, managementService.createJobQuery().list().size());
    for (Job historyJob : managementService.createJobQuery().list()) {
      final int minuteTo = getHistoryCleanupJobHandlerConfiguration(historyJob).getMinuteTo();
      final int minuteFrom = getHistoryCleanupJobHandlerConfiguration(historyJob).getMinuteFrom();
      if (minuteFrom == 0) {
        assertEquals(19, minuteTo);
      } else if (minuteFrom == 20) {
        assertEquals(39, minuteTo);
      } else {
        assertEquals(40, minuteFrom);
        assertEquals(59, minuteTo);
      }
    }

    ProcessEngineImpl processEngine = configuration.getProcessEngine();
    processEngine.close();
    ProcessEngines.unregister(processEngine);

    // assume
    assertEquals(0, ProcessEngines.getProcessEngines().size());
    assertNull(ProcessEngines.getProcessEngines().get(PROCESS_ENGINE_NAME));

    // given
    configuration.setHistoryCleanupDegreeOfParallelism(1);
    configuration.setProcessEngineName(PROCESS_ENGINE_NAME);
    configuration.buildProcessEngine();

    // when
    historyService.cleanUpHistoryAsync(true);
    // then
    assertEquals(1, managementService.createJobQuery().list().size());
    Job job = managementService.createJobQuery().singleResult();
    assertEquals(0, getHistoryCleanupJobHandlerConfiguration(job).getMinuteFrom());
    assertEquals(59, getHistoryCleanupJobHandlerConfiguration(job).getMinuteTo());

    processEngine = configuration.getProcessEngine();
    processEngine.close();
    ProcessEngines.unregister(processEngine);

    // assume
    assertEquals(0, ProcessEngines.getProcessEngines().size());
    assertNull(ProcessEngines.getProcessEngines().get(PROCESS_ENGINE_NAME));

    // given
    configuration.setHistoryCleanupDegreeOfParallelism(2);
    configuration.setProcessEngineName(PROCESS_ENGINE_NAME);
    configuration.buildProcessEngine();

    // when
    historyService.cleanUpHistoryAsync(true);
    // then
    assertEquals(2, managementService.createJobQuery().list().size());
    for (Job historyJob : managementService.createJobQuery().list()) {
      final int minuteTo = getHistoryCleanupJobHandlerConfiguration(historyJob).getMinuteTo();
      final int minuteFrom = getHistoryCleanupJobHandlerConfiguration(historyJob).getMinuteFrom();
      if (minuteFrom == 0) {
        assertEquals(29, minuteTo);
      } else {
        assertEquals(30, minuteFrom);
        assertEquals(59, minuteTo);
      }
    }
  }


  protected HistoryCleanupJobHandlerConfiguration getHistoryCleanupJobHandlerConfiguration(Job job) {
    return HistoryCleanupJobHandlerConfiguration
          .fromJson(new JSONObject(((JobEntity) job).getJobHandlerConfigurationRaw()));
  }
}
