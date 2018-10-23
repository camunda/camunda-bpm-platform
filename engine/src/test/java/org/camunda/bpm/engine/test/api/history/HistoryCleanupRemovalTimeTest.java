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

import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReportResult;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.DefaultHistoryRemovalTimeProvider;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupHandler;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.resources.GetByteArrayCommand;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.time.DateUtils.addDays;
import static org.apache.commons.lang.time.DateUtils.addMinutes;
import static org.apache.commons.lang.time.DateUtils.addSeconds;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_FULL;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_PROCESS_END;
import static org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandlerConfiguration.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * @author Tassilo Weidner
 */
@RequiredHistoryLevel(HISTORY_FULL)
public class HistoryCleanupRemovalTimeTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected FormService formService;
  protected HistoryService historyService;
  protected TaskService taskService;
  protected ManagementService managementService;
  protected RepositoryService repositoryService;
  protected IdentityService identityService;
  protected ExternalTaskService externalTaskService;
  protected DecisionService decisionService;

  protected static ProcessEngineConfigurationImpl engineConfiguration;

  protected Set<String> jobIds;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    formService = engineRule.getFormService();
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
    managementService = engineRule.getManagementService();
    repositoryService = engineRule.getRepositoryService();
    identityService = engineRule.getIdentityService();
    externalTaskService = engineRule.getExternalTaskService();
    decisionService = engineRule.getDecisionService();

    engineConfiguration = engineRule.getProcessEngineConfiguration();

    engineConfiguration
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_PROCESS_END)
      .setHistoryRemovalTimeProvider(new DefaultHistoryRemovalTimeProvider())
      .initHistoryRemovalTime();

    engineConfiguration
      .setHistoryCleanupByRemovalTime(true)
      .setHistoryCleanupBatchSize(HistoryCleanupHandler.MAX_BATCH_SIZE);

    engineConfiguration.setHistoryCleanupBatchWindowStartTime(null);

    engineConfiguration.initHistoryCleanup();

    jobIds = new HashSet<>();
  }

  @After
  public void tearDown() {
    clearMeterLog();

    for (String jobId : jobIds) {
      clearJobLog(jobId);
      clearJob(jobId);
    }
  }

  @AfterClass
  public static void tearDownAfterAll() {
    if (engineConfiguration != null) {
      engineConfiguration
        .setHistoryRemovalTimeProvider(null)
        .setHistoryRemovalTimeStrategy(null)
        .setHistoryCleanupByRemovalTime(false);

      engineConfiguration.setHistoryCleanupBatchWindowStartTime(null);

      engineConfiguration.initHistoryRemovalTime();
      engineConfiguration.initHistoryCleanup();
    }
  }

  protected final String PROCESS_KEY = "process";
  protected final BpmnModelInstance PROCESS = Bpmn.createExecutableProcess(PROCESS_KEY)
    .camundaHistoryTimeToLive(5)
    .startEvent()
      .userTask("userTask").name("userTask")
    .endEvent().done();


  protected final BpmnModelInstance CALLED_PROCESS_INCIDENT = Bpmn.createExecutableProcess(PROCESS_KEY)
    .startEvent()
      .scriptTask()
        .camundaAsyncBefore()
        .scriptFormat("groovy")
        .scriptText("if(execution.getIncidents().size() == 0) throw new RuntimeException(\"I'm supposed to fail!\")")
      .userTask("userTask")
    .endEvent().done();

  protected final String CALLING_PROCESS_KEY = "callingProcess";
  protected final BpmnModelInstance CALLING_PROCESS = Bpmn.createExecutableProcess(CALLING_PROCESS_KEY)
    .camundaHistoryTimeToLive(5)
    .startEvent()
      .callActivity()
        .calledElement(PROCESS_KEY)
    .endEvent().done();

  protected final String CALLING_PROCESS_CALLS_DMN_KEY = "callingProcessCallsDmn";
  protected final BpmnModelInstance CALLING_PROCESS_CALLS_DMN = Bpmn.createExecutableProcess(CALLING_PROCESS_CALLS_DMN_KEY)
    .camundaHistoryTimeToLive(5)
    .startEvent()
      .businessRuleTask()
        .camundaAsyncAfter()
        .camundaDecisionRef("dish-decision")
    .endEvent().done();

  protected final Date END_DATE = new Date(1363608000000L);

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldCleanupDecisionInstance() {
    // given
    testRule.deploy(CALLING_PROCESS_CALLS_DMN);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_CALLS_DMN_KEY,
      Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend"));

    ClockUtil.setCurrentTime(END_DATE);

    String jobId = managementService.createJobQuery().singleResult().getId();

    managementService.executeJob(jobId);

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // assume
    assertThat(historicDecisionInstances.size(), is(3));

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.size(), is(0));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldCleanupDecisionInputInstance() {
    // given
    testRule.deploy(CALLING_PROCESS_CALLS_DMN);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_CALLS_DMN_KEY,
      Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend"));

    ClockUtil.setCurrentTime(END_DATE);

    String jobId = managementService.createJobQuery().singleResult().getId();

    managementService.executeJob(jobId);

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
      .includeInputs()
      .list();

    // assume
    assertThat(historicDecisionInstances.size(), is(3));

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
      .includeInputs()
      .list();

    // then
    assertThat(historicDecisionInstances.size(), is(0));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldCleanupDecisionOutputInstance() {
    // given
    testRule.deploy(CALLING_PROCESS_CALLS_DMN);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_CALLS_DMN_KEY,
      Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend"));

    ClockUtil.setCurrentTime(END_DATE);

    String jobId = managementService.createJobQuery().singleResult().getId();

    managementService.executeJob(jobId);

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
      .includeOutputs()
      .list();

    // assume
    assertThat(historicDecisionInstances.size(), is(3));

    Date removalTime = addDays(END_DATE, 5);
    ClockUtil.setCurrentTime(removalTime);

    // when
    runHistoryCleanup();

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
      .includeOutputs()
      .list();

    // then
    assertThat(historicDecisionInstances.size(), is(0));
  }

  @Test
  public void shouldCleanupProcessInstance() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String taskId = historyService.createHistoricTaskInstanceQuery().singleResult().getId();

    ClockUtil.setCurrentTime(END_DATE);

    taskService.complete(taskId);

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
      .processDefinitionKey(PROCESS_KEY)
      .list();

    // assume
    assertThat(historicProcessInstances.size(), is(1));

    Date removalTime = addDays(END_DATE, 5);
    ClockUtil.setCurrentTime(removalTime);

    // when
    runHistoryCleanup();

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
      .processDefinitionKey(PROCESS_KEY)
      .list();

    // then
    assertThat(historicProcessInstances.size(), is(0));
  }

  @Test
  public void shouldCleanupActivityInstance() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String taskId = historyService.createHistoricTaskInstanceQuery().singleResult().getId();

    ClockUtil.setCurrentTime(END_DATE);

    taskService.complete(taskId);

    List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().list();

    // assume
    assertThat(historicActivityInstances.size(), is(6));

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    historicActivityInstances = historyService.createHistoricActivityInstanceQuery().list();

    // then
    assertThat(historicActivityInstances.size(), is(0));
  }

  @Test
  public void shouldCleanupTaskInstance() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    ClockUtil.setCurrentTime(END_DATE);

    String taskId = historyService.createHistoricTaskInstanceQuery().singleResult().getId();

    taskService.complete(taskId);

    List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().list();

    // assume
    assertThat(historicTaskInstances.size(), is(1));

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    historicTaskInstances = historyService.createHistoricTaskInstanceQuery().list();

    // then
    assertThat(historicTaskInstances.size(), is(0));
  }

  @Test
  public void shouldCleanupVariableInstance() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    runtimeService.setVariable(processInstance.getId(), "aVariableName", Variables.stringValue("anotherVariableValue"));

    ClockUtil.setCurrentTime(END_DATE);

    String taskId = historyService.createHistoricTaskInstanceQuery().singleResult().getId();

    taskService.complete(taskId);

    List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery().list();

    // assume
    assertThat(historicVariableInstances.size(), is(1));

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    historicVariableInstances = historyService.createHistoricVariableInstanceQuery().list();

    // then
    assertThat(historicVariableInstances.size(), is(0));
  }

  @Test
  public void shouldCleanupDetail() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY,
      Variables.createVariables()
        .putValue("aVariableName", Variables.stringValue("aVariableValue")));

    runtimeService.setVariable(processInstance.getId(), "aVariableName", Variables.stringValue("anotherVariableValue"));

    List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery()
      .variableUpdates()
      .list();

    // assume
    assertThat(historicDetails.size(), is(2));

    ClockUtil.setCurrentTime(END_DATE);

    String taskId = historyService.createHistoricTaskInstanceQuery().singleResult().getId();

    taskService.complete(taskId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    historicDetails = historyService.createHistoricDetailQuery()
      .variableUpdates()
      .list();

    // then
    assertThat(historicDetails.size(), is(0));
  }

  @Test
  public void shouldCleanupIncident() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS_INCIDENT);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String jobId = managementService.createJobQuery().singleResult().getId();

    managementService.setJobRetries(jobId, 0);

    try {
      managementService.executeJob(jobId);
    } catch (Exception ignored) { }

    List<HistoricIncident> historicIncidents = historyService.createHistoricIncidentQuery().list();

    // assume
    assertThat(historicIncidents.size(), is(2));

    ClockUtil.setCurrentTime(END_DATE);

    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.complete(taskId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    historicIncidents = historyService.createHistoricIncidentQuery().list();

    // then
    assertThat(historicIncidents.size(), is(0));
  }

  @Test
  public void shouldCleanupExternalTaskLog() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess("calledProcess")
      .startEvent()
        .serviceTask().camundaExternalTask("anExternalTaskTopic")
      .endEvent().done());

    testRule.deploy(Bpmn.createExecutableProcess("callingProcess")
      .camundaHistoryTimeToLive(5)
      .startEvent()
        .callActivity()
          .calledElement("calledProcess")
      .endEvent().done());

    runtimeService.startProcessInstanceByKey("callingProcess");

    LockedExternalTask externalTask = externalTaskService.fetchAndLock(1, "aWorkerId")
      .topic("anExternalTaskTopic", 3000)
      .execute()
      .get(0);

    List<HistoricExternalTaskLog> externalTaskLogs = historyService.createHistoricExternalTaskLogQuery().list();

    // assume
    assertThat(externalTaskLogs.size(), is(1));

    ClockUtil.setCurrentTime(END_DATE);

    externalTaskService.complete(externalTask.getId(), "aWorkerId");

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    externalTaskLogs = historyService.createHistoricExternalTaskLogQuery().list();

    // then
    assertThat(externalTaskLogs.size(), is(0));
  }

  @Test
  public void shouldCleanupJobLog() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_KEY)
      .startEvent().camundaAsyncBefore()
        .userTask("userTask").name("userTask")
      .endEvent().done());

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    ClockUtil.setCurrentTime(END_DATE);

    String jobId = managementService.createJobQuery()
      .singleResult()
      .getId();

    managementService.executeJob(jobId);

    List<HistoricJobLog> jobLogs = historyService.createHistoricJobLogQuery()
      .processDefinitionKey(PROCESS_KEY)
      .list();

    // assume
    assertThat(jobLogs.size(), is(2));

    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.complete(taskId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    jobLogs = historyService.createHistoricJobLogQuery()
      .processDefinitionKey(PROCESS_KEY)
      .list();

    // then
    assertThat(jobLogs.size(), is(0));
  }

  @Test
  public void shouldCleanupUserOperationLog() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_KEY)
      .startEvent().camundaAsyncBefore()
        .userTask("userTask").name("userTask")
      .endEvent().done());

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String jobId = managementService.createJobQuery()
      .singleResult()
      .getId();

    identityService.setAuthenticatedUserId("aUserId");
    managementService.setJobRetries(jobId, 65);
    identityService.clearAuthentication();

    List<UserOperationLogEntry> userOperationLogs = historyService.createUserOperationLogQuery().list();

    // assume
    assertThat(userOperationLogs.size(), is(1));

    managementService.executeJob(jobId);

    ClockUtil.setCurrentTime(END_DATE);

    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.complete(taskId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    userOperationLogs = historyService.createUserOperationLogQuery().list();

    // then
    assertThat(userOperationLogs.size(), is(0));
  }

  @Test
  public void shouldCleanupIdentityLink() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.addCandidateUser(taskId, "aUserId");

    List<HistoricIdentityLinkLog> historicIdentityLinkLogs = historyService.createHistoricIdentityLinkLogQuery().list();

    // assume
    assertThat(historicIdentityLinkLogs.size(), is(1));

    ClockUtil.setCurrentTime(END_DATE);

    taskService.complete(taskId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    historicIdentityLinkLogs = historyService.createHistoricIdentityLinkLogQuery().list();

    // then
    assertThat(historicIdentityLinkLogs.size(), is(0));
  }

  @Test
  public void shouldCleanupComment() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String processInstanceId = runtimeService.createProcessInstanceQuery()
      .activityIdIn("userTask")
      .singleResult()
      .getId();

    taskService.createComment(null, processInstanceId, "aMessage");

    List<Comment> comments = taskService.getProcessInstanceComments(processInstanceId);

    // assume
    assertThat(comments.size(), is(1));

    String taskId = taskService.createTaskQuery().singleResult().getId();

    ClockUtil.setCurrentTime(END_DATE);

    taskService.complete(taskId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    comments = taskService.getProcessInstanceComments(processInstanceId);

    // then
    assertThat(comments.size(), is(0));
  }

  @Test
  public void shouldCleanupAttachment() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String processInstanceId = runtimeService.createProcessInstanceQuery()
      .activityIdIn("userTask")
      .singleResult()
      .getId();

    taskService.createAttachment(null, null, processInstanceId, null, null, "http://camunda.com").getId();

    List<Attachment> attachments = taskService.getProcessInstanceAttachments(processInstanceId);

    // assume
    assertThat(attachments.size(), is(1));

    String taskId = taskService.createTaskQuery().singleResult().getId();

    ClockUtil.setCurrentTime(END_DATE);

    taskService.complete(taskId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    attachments = taskService.getProcessInstanceAttachments(processInstanceId);

    // then
    assertThat(attachments.size(), is(0));
  }

  @Test
  public void shouldCleanupByteArray() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS_INCIDENT);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String jobId = managementService.createJobQuery()
      .singleResult()
      .getId();

    try {
      managementService.executeJob(jobId);
    } catch (Exception ignored) { }

    HistoricJobLogEventEntity jobLog = (HistoricJobLogEventEntity) historyService.createHistoricJobLogQuery()
      .failureLog()
      .singleResult();

    ByteArrayEntity byteArray = findByteArrayById(jobLog.getExceptionByteArrayId());

    // assume
    assertThat(byteArray, notNullValue());

    managementService.setJobRetries(jobId, 0);

    managementService.executeJob(jobId);

    ClockUtil.setCurrentTime(END_DATE);

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    byteArray = findByteArrayById(jobLog.getExceptionByteArrayId());

    // then
    assertThat(byteArray, nullValue());
  }

  @Test
  public void shouldRescheduleCleanupToNow() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    ClockUtil.setCurrentTime(END_DATE);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    engineConfiguration.setHistoryCleanupBatchSize(6);
    engineConfiguration.setHistoryCleanupBatchWindowStartTime("13:00");
    engineConfiguration.initHistoryCleanup();

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    Job job = historyService.findHistoryCleanupJobs().get(0);

    // then
    assertThat(job.getDuedate(), is(ClockUtil.getCurrentTime()));
  }

  @Test
  public void shouldRescheduleCleanupToLater() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    ClockUtil.setCurrentTime(END_DATE);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    engineConfiguration.setHistoryCleanupBatchSize(7);
    engineConfiguration.setHistoryCleanupBatchWindowStartTime("13:00");
    engineConfiguration.initHistoryCleanup();

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    Job job = historyService.findHistoryCleanupJobs().get(0);

    // then
    assertThat(job.getDuedate(), is(addSeconds(ClockUtil.getCurrentTime(), START_DELAY)));
  }

  @Test
  public void shouldDistributeWork() {
    // given
    testRule.deploy(PROCESS);

    for (int i = 0; i < 60; i++) {
      runtimeService.startProcessInstanceByKey(PROCESS_KEY);

      ClockUtil.setCurrentTime(addMinutes(END_DATE, i));

      String taskId = taskService.createTaskQuery().singleResult().getId();
      taskService.complete(taskId);
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 7));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    CleanableHistoricProcessInstanceReportResult reportResults = historyService.createCleanableHistoricProcessInstanceReport().list().get(0);

    // assume
    assertThat(jobs.size(), is(3));
    assertThat(reportResults.getCleanableProcessInstanceCount(), is(60L));

    Job jobOne = jobs.get(0);
    jobIds.add(jobOne.getId());

    // when
    managementService.executeJob(jobOne.getId());

    reportResults = historyService.createCleanableHistoricProcessInstanceReport().list().get(0);

    // then
    assertThat(reportResults.getCleanableProcessInstanceCount(), is(40L));

    Job jobTwo = jobs.get(1);
    jobIds.add(jobTwo.getId());

    // when
    managementService.executeJob(jobTwo.getId());

    reportResults = historyService.createCleanableHistoricProcessInstanceReport().list().get(0);

    // then
    assertThat(reportResults.getCleanableProcessInstanceCount(), is(20L));

    Job jobThree = jobs.get(2);
    jobIds.add(jobThree.getId());

    // when
    managementService.executeJob(jobThree.getId());

    reportResults = historyService.createCleanableHistoricProcessInstanceReport().list().get(0);

    // then
    assertThat(reportResults.getCleanableProcessInstanceCount(), is(0L));
  }

  // helper /////////////////////////////////////////////////////////////////

  protected List<Job> runHistoryCleanup() {
    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();
    for (Job job : jobs) {
      jobIds.add(job.getId());
      managementService.executeJob(job.getId());
    }

    return jobs;
  }



  protected ByteArrayEntity findByteArrayById(String byteArrayId) {
    return engineConfiguration.getCommandExecutorTxRequired()
      .execute(new GetByteArrayCommand(byteArrayId));
  }

  protected void clearJobLog(final String jobId) {
    CommandExecutor commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobId);
        return null;
      }
    });
  }

  protected void clearJob(final String jobId) {
    engineConfiguration.getCommandExecutorTxRequired()
      .execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        JobEntity job = commandContext.getJobManager().findJobById(jobId);
        if (job != null) {
          commandContext.getJobManager().delete(job);
        }
        return null;
      }
    });
  }

  protected void clearMeterLog() {
    engineConfiguration.getCommandExecutorTxRequired()
      .execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getMeterLogManager().deleteAll();

        return null;
      }
    });
  }

}
