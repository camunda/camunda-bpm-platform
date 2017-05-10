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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class BulkHistoryDeleteCmmnDisabledTest {

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      configuration.setCmmnEnabled(false);
      return configuration;
    }
  };

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(testRule);

  private RuntimeService runtimeService;
  private HistoryService historyService;

  @Before
  public void createProcessEngine() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();

  }

  @After
  public void clearDatabase() {
    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        List<Job> jobs = engineRule.getManagementService().createJobQuery().list();
        if (jobs.size() > 0) {
          assertEquals(1, jobs.size());
          String jobId = jobs.get(0).getId();
          commandContext.getJobManager().deleteJob((JobEntity) jobs.get(0));
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobId);
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
    for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
      historyService.deleteHistoricProcessInstance(historicProcessInstance.getId());
    }

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();
    for (HistoricDecisionInstance historicDecisionInstance : historicDecisionInstances) {
      historyService.deleteHistoricDecisionInstanceByInstanceId(historicDecisionInstance.getId());
    }

    clearMetrics();

  }

  protected void clearMetrics() {
    Collection<Meter> meters = engineRule.getProcessEngineConfiguration().getMetricsRegistry().getMeters().values();
    for (Meter meter : meters) {
      meter.getAndClear();
    }
    engineRule.getManagementService().deleteMetrics(null);
  }

  @Test
  public void bulkHistoryProcessDeleteWithDisabledCmmn() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess("someProcess").startEvent().userTask("userTask").endEvent().done();
    testRule.deploy(model);
    List<String> ids = prepareHistoricProcesses("someProcess");
    runtimeService.deleteProcessInstances(ids, null, true, true);

    // when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    // then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("someProcess").count());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/dmn/Example.dmn" })
  public void bulkHistoryDecisionDeleteWithDisabledCmmn() {
    // given
    prepareDMNData(5);

    ClockUtil.setCurrentTime(new Date());
    // when
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    engineRule.getManagementService().executeJob(jobId);

    // then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(0, historyService.createHistoricDecisionInstanceQuery().count());
  }

  private void prepareDMNData(int instanceCount) {
    Date oldCurrentTime = ClockUtil.getCurrentTime();
    List<DecisionDefinition> decisionDefinitions = engineRule.getRepositoryService().createDecisionDefinitionQuery().decisionDefinitionKey("decision").list();
    assertEquals(1, decisionDefinitions.size());
    engineRule.getRepositoryService().updateDecisionDefinitionHistoryTimeToLive(decisionDefinitions.get(0).getId(), 5);

    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), -6));
    for (int i = 0; i < instanceCount; i++) {
      engineRule.getDecisionService().evaluateDecisionByKey("decision").variables(Variables.createVariables().putValue("status", "silver").putValue("sum", 723))
          .evaluate();
    }
    ClockUtil.setCurrentTime(oldCurrentTime);
  }

  private List<String> prepareHistoricProcesses(String businessKey) {
    List<String> processInstanceIds = new ArrayList<String>();

    for (int i = 0; i < 5; i++) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(businessKey);
      processInstanceIds.add(processInstance.getId());
    }

    return processInstanceIds;
  }
}
