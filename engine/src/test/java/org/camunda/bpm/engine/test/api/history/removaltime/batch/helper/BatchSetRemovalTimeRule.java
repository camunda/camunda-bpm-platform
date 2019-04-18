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
package org.camunda.bpm.engine.test.api.history.removaltime.batch.helper;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.resources.GetByteArrayCommand;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.builder.StartEventBuilder;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class BatchSetRemovalTimeRule extends TestWatcher {

  protected ProcessEngineRule engineRule;
  protected ProcessEngineTestRule engineTestRule;

  public final Date CURRENT_DATE = new Date(1363608000000L);
  public final Date REMOVAL_TIME = new Date(1363609000000L);

  protected String batchId;

  public BatchSetRemovalTimeRule(ProcessEngineRule engineRule, ProcessEngineTestRule engineTestRule) {
    this.engineRule = engineRule;
    this.engineTestRule = engineTestRule;
  }

  protected void starting(Description description) {
    ClockUtil.setCurrentTime(CURRENT_DATE);

    super.starting(description);
  }

  protected void finished(Description description) {
    getProcessEngineConfiguration()
      .setHistoryRemovalTimeProvider(null)
      .setHistoryRemovalTimeStrategy(null)
      .initHistoryRemovalTime();

    getProcessEngineConfiguration().setBatchOperationHistoryTimeToLive(null);
    getProcessEngineConfiguration().setBatchOperationsForHistoryCleanup(null);

    getProcessEngineConfiguration().initHistoryCleanup();

    getProcessEngineConfiguration().setInvocationsPerBatchJob(1);

    getProcessEngineConfiguration().setDmnEnabled(true);

    ClockUtil.reset();

    if (batchId != null) {
      String historicBatchId = engineRule.getHistoryService().createHistoricBatchQuery()
        .batchId(batchId)
        .singleResult()
        .getId();

      engineRule.getHistoryService().deleteHistoricBatch(historicBatchId);
    }

    super.finished(description);
  }

  // helper ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public TestProcessBuilder process() {
    return new TestProcessBuilder();
  }

  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return engineRule.getProcessEngineConfiguration();
  }

  public void updateHistoryTimeToLive(int ttl) {
    String processDefinitionId = engineRule.getRepositoryService()
      .createProcessDefinitionQuery()
      .singleResult()
      .getId();

    engineRule.getRepositoryService().updateProcessDefinitionHistoryTimeToLive(processDefinitionId, ttl);
  }

  public class TestProcessBuilder {

    ProcessBuilder builder = Bpmn.createExecutableProcess("process");

    StartEventBuilder startEventBuilder = builder.startEvent();

    public TestProcessBuilder ttl(Integer ttl) {
      builder.camundaHistoryTimeToLive(ttl);

      return this;
    }

    public TestProcessBuilder async() {
      startEventBuilder.camundaAsyncBefore();

      return this;
    }

    public TestProcessBuilder ruleTask(String ref) {
      startEventBuilder
        .businessRuleTask()
        .camundaDecisionRef(ref);

      return this;
    }

    public TestProcessBuilder userTask() {
      startEventBuilder
        .userTask("userTask")
        .name("userTask")
        .camundaAssignee("anAssignee");

      return this;
    }

    public TestProcessBuilder scriptTask() {
      startEventBuilder
        .scriptTask()
        .scriptFormat("groovy")
        .scriptText("throw new RuntimeException()");

      return this;
    }

    public TestProcessBuilder externalTask() {
      startEventBuilder
        .serviceTask()
        .camundaExternalTask("aTopicName");

      return this;
    }

    public TestProcessBuilder serviceTask() {
      startEventBuilder
        .serviceTask()
        .camundaExpression("${true}");

      return this;
    }

    public TestProcessBuilder deploy() {
      BpmnModelInstance process = startEventBuilder.endEvent().done();

      engineTestRule.deploy(process);

      return this;
    }

    public String start() {
      return startWithVariables(null);
    }

    public String startWithVariables(Map<String, Object> variables) {
      return engineRule.getRuntimeService().startProcessInstanceByKey("process", variables).getId();
    }
  }

  public void syncExec(Batch batch) {
    batchId = batch.getId();

    String seedJobDefinitionId = batch.getSeedJobDefinitionId();

    String jobId = engineRule.getManagementService().createJobQuery()
      .jobDefinitionId(seedJobDefinitionId)
      .singleResult()
      .getId();

    engineRule.getManagementService().executeJob(jobId);

    String batchJobDefinitionId = batch.getBatchJobDefinitionId();

    List<Job> jobs = engineRule.getManagementService().createJobQuery()
      .jobDefinitionId(batchJobDefinitionId)
      .list();

    for (Job job : jobs) {
      engineRule.getManagementService().executeJob(job.getId());
    }

    String monitorJobDefinitionId = batch.getMonitorJobDefinitionId();

    jobId = engineRule.getManagementService().createJobQuery()
      .jobDefinitionId(monitorJobDefinitionId)
      .singleResult()
      .getId();

    engineRule.getManagementService().executeJob(jobId);
  }

  public Date addDays(Date date, int amount) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(Calendar.DATE, amount);
    return c.getTime();
  }

  public ByteArrayEntity findByteArrayById(String byteArrayId) {
    CommandExecutor commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    return commandExecutor.execute(new GetByteArrayCommand(byteArrayId));
  }

}
