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

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInputInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionOutputInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AttachmentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.dmn.businessruletask.TestPojo;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Svetlana Dorokhova
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class BulkHistoryDeleteTest {

  protected static final String ONE_TASK_PROCESS = "oneTaskProcess";

  public static final int PROCESS_INSTANCE_COUNT = 5;

  public ProcessEngineRule engineRule = new ProcessEngineRule(true);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  private HistoryService historyService;
  private TaskService taskService;
  private RuntimeService runtimeService;
  private FormService formService;
  private ExternalTaskService externalTaskService;
  private CaseService caseService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
    formService = engineRule.getFormService();
    externalTaskService = engineRule.getExternalTaskService();
    caseService = engineRule.getCaseService();
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupHistoryTaskIdentityLink() {
    //given
    final List<String> ids = prepareHistoricProcesses();
    List<Task> taskList = taskService.createTaskQuery().list();
    taskService.addUserIdentityLink(taskList.get(0).getId(), "someUser", IdentityLinkType.ASSIGNEE);

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, historyService.createHistoricIdentityLinkLogQuery().count());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupHistoryActivityInstances() {
    //given
    final List<String> ids = prepareHistoricProcesses();

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().count());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupTaskAttachmentWithContent() {
    //given
    final List<String> ids = prepareHistoricProcesses();

    List<Task> taskList = taskService.createTaskQuery().list();

    String taskWithAttachmentId = taskList.get(0).getId();
    createTaskAttachmentWithContent(taskWithAttachmentId);
    //remember contentId
    final String contentId = findAttachmentContentId(taskService.getTaskAttachments(taskWithAttachmentId));

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, taskService.getTaskAttachments(taskWithAttachmentId).size());
    //check that attachment content was removed
    verifyByteArraysWereRemoved(contentId);
  }

  private String findAttachmentContentId(List<Attachment> attachments) {
    assertEquals(1, attachments.size());
    return ((AttachmentEntity) attachments.get(0)).getContentId();
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupProcessInstanceAttachmentWithContent() {
    //given
    final List<String> ids = prepareHistoricProcesses();

    String processInstanceWithAttachmentId = ids.get(0);
    createProcessInstanceAttachmentWithContent(processInstanceWithAttachmentId);
    //remember contentId
    final String contentId = findAttachmentContentId(taskService.getProcessInstanceAttachments(processInstanceWithAttachmentId));

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, taskService.getProcessInstanceAttachments(processInstanceWithAttachmentId).size());
    //check that attachment content was removed
    verifyByteArraysWereRemoved(contentId);
  }

  private void createProcessInstanceAttachmentWithContent(String processInstanceId) {
    taskService
        .createAttachment("web page", null, processInstanceId, "weatherforcast", "temperatures and more", new ByteArrayInputStream("someContent".getBytes()));

    List<Attachment> taskAttachments = taskService.getProcessInstanceAttachments(processInstanceId);
    assertEquals(1, taskAttachments.size());
    assertNotNull(taskService.getAttachmentContent(taskAttachments.get(0).getId()));
  }

  private void createTaskAttachmentWithContent(String taskId) {
    taskService.createAttachment("web page", taskId, null, "weatherforcast", "temperatures and more", new ByteArrayInputStream("someContent".getBytes()));

    List<Attachment> taskAttachments = taskService.getTaskAttachments(taskId);
    assertEquals(1, taskAttachments.size());
    assertNotNull(taskService.getAttachmentContent(taskAttachments.get(0).getId()));
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupTaskComment() {
    //given
    final List<String> ids = prepareHistoricProcesses();

    List<Task> taskList = taskService.createTaskQuery().list();

    String taskWithCommentId = taskList.get(2).getId();
    taskService.createComment(taskWithCommentId, null, "Some comment");

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, taskService.getTaskComments(taskWithCommentId).size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupProcessInstanceComment() {
    //given
    final List<String> ids = prepareHistoricProcesses();

    String processInstanceWithCommentId = ids.get(0);
    taskService.createComment(null, processInstanceWithCommentId, "Some comment");

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, taskService.getProcessInstanceComments(processInstanceWithCommentId).size());
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupHistoricVariableInstancesAndHistoricDetails() {
    //given
    final List<String> ids = prepareHistoricProcesses();

    List<Task> taskList = taskService.createTaskQuery().list();

    taskService.setVariables(taskList.get(0).getId(), getVariables());

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, historyService.createHistoricDetailQuery().count());
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().count());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupHistoryTaskForm() {
    //given
    final List<String> ids = prepareHistoricProcesses();

    List<Task> taskList = taskService.createTaskQuery().list();

    formService.submitTaskForm(taskList.get(0).getId(), getVariables());

    for (ProcessInstance processInstance : runtimeService.createProcessInstanceQuery().list()) {
      runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), null);
    }

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, historyService.createHistoricDetailQuery().count());
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().count());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  public void testCleanupHistoricExternalTaskLog() {
    //given
    final List<String> ids = prepareHistoricProcesses("oneExternalTaskProcess");

    String workerId = "aWrokerId";
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(1, workerId).topic("externalTaskTopic", 10000L).execute();

    externalTaskService.handleFailure(tasks.get(0).getId(), workerId, "errorMessage", "exceptionStackTrace", 5, 3000L);

    //remember errorDetailsByteArrayId
    final String errorDetailsByteArrayId = findErrorDetailsByteArrayId("errorMessage");

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, historyService.createHistoricExternalTaskLogQuery().count());
    //check that ByteArray was removed
    verifyByteArraysWereRemoved(errorDetailsByteArrayId);
  }

  private String findErrorDetailsByteArrayId(String errorMessage) {
    final List<HistoricExternalTaskLog> historicExternalTaskLogs = historyService.createHistoricExternalTaskLogQuery().errorMessage(errorMessage).list();
    assertEquals(1, historicExternalTaskLogs.size());

    return ((HistoricExternalTaskLogEntity) historicExternalTaskLogs.get(0)).getErrorDetailsByteArrayId();
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn" })
  public void testCleanupHistoricIncidents() {
    //given
    List<String> ids = prepareHistoricProcesses("failingProcess");

    testRule.executeAvailableJobs();

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("failingProcess").count());
    assertEquals(0, historyService.createHistoricIncidentQuery().count());

  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn" })
  public void testCleanupHistoricJobLogs() {
    //given
    List<String> ids = prepareHistoricProcesses("failingProcess", null, 1);

    testRule.executeAvailableJobs();

    runtimeService.deleteProcessInstances(ids, null, true, true);

    List<String> byteArrayIds = findExceptionByteArrayIds();

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("failingProcess").count());
    assertEquals(0, historyService.createHistoricJobLogQuery().count());

    verifyByteArraysWereRemoved(byteArrayIds.toArray(new String[] {}));
  }

  private List<String> findExceptionByteArrayIds() {
    List<String> exceptionByteArrayIds = new ArrayList<String>();
    List<HistoricJobLog> historicJobLogs = historyService.createHistoricJobLogQuery().list();
    for (HistoricJobLog historicJobLog : historicJobLogs) {
      HistoricJobLogEventEntity historicJobLogEventEntity = (HistoricJobLogEventEntity) historicJobLog;
      if (historicJobLogEventEntity.getExceptionByteArrayId() != null) {
        exceptionByteArrayIds.add(historicJobLogEventEntity.getExceptionByteArrayId());
      }
    }
    return exceptionByteArrayIds;
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRef.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml" })
  public void testCleanupHistoryDecisionData() {
    //given
    List<String> ids = prepareHistoricProcesses("testProcess", Variables.createVariables().putValue("pojo", new TestPojo("okay", 13.37)));

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //remember input and output ids
    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().includeInputs().includeOutputs().list();
    final List<String> inputIds = new ArrayList<String>();
    final List<String> inputByteArrayIds = new ArrayList<String>();
    collectHistoricDecisionInputIds(historicDecisionInstances, inputIds, inputByteArrayIds);

    final List<String> outputIds = new ArrayList<String>();
    final List<String> outputByteArrayIds = new ArrayList<String>();
    collectHistoricDecisionOutputIds(historicDecisionInstances, outputIds, outputByteArrayIds);

    //when
    historyService.deleteHistoricDecisionInstancesBulk(extractIds(historicDecisionInstances));

    //then
    assertEquals(0, historyService.createHistoricDecisionInstanceQuery().count());

    //check that decision inputs and outputs were removed
    assertDataDeleted(inputIds, inputByteArrayIds, outputIds, outputByteArrayIds);

  }

  void assertDataDeleted(final List<String> inputIds, final List<String> inputByteArrayIds, final List<String> outputIds,
    final List<String> outputByteArrayIds) {
    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        for (String inputId : inputIds) {
          assertNull(commandContext.getDbEntityManager().selectById(HistoricDecisionInputInstanceEntity.class, inputId));
        }
        for (String inputByteArrayId : inputByteArrayIds) {
          assertNull(commandContext.getDbEntityManager().selectById(ByteArrayEntity.class, inputByteArrayId));
        }
        for (String outputId : outputIds) {
          assertNull(commandContext.getDbEntityManager().selectById(HistoricDecisionOutputInstanceEntity.class, outputId));
        }
        for (String outputByteArrayId : outputByteArrayIds) {
          assertNull(commandContext.getDbEntityManager().selectById(ByteArrayEntity.class, outputByteArrayId));
        }
        return null;
      }
    });
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml" })
  public void testCleanupHistoryStandaloneDecisionData() {
    //given
    for (int i = 0; i < 5; i++) {
      engineRule.getDecisionService().evaluateDecisionByKey("testDecision").variables(Variables.createVariables().putValue("pojo", new TestPojo("okay", 13.37))).evaluate();
    }

    //remember input and output ids
    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().includeInputs().includeOutputs().list();
    final List<String> inputIds = new ArrayList<String>();
    final List<String> inputByteArrayIds = new ArrayList<String>();
    collectHistoricDecisionInputIds(historicDecisionInstances, inputIds, inputByteArrayIds);

    final List<String> outputIds = new ArrayList<String>();
    final List<String> outputByteArrayIds = new ArrayList<String>();
    collectHistoricDecisionOutputIds(historicDecisionInstances, outputIds, outputByteArrayIds);

    List<String> decisionInstanceIds = extractIds(historicDecisionInstances);

    //when
    historyService.deleteHistoricDecisionInstancesBulk(decisionInstanceIds);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("testProcess").count());
    assertEquals(0, historyService.createHistoricDecisionInstanceQuery().count());

    //check that decision inputs and outputs were removed
    assertDataDeleted(inputIds, inputByteArrayIds, outputIds, outputByteArrayIds);

  }

  private List<String> extractIds(List<HistoricDecisionInstance> historicDecisionInstances) {
    List<String> decisionInstanceIds = new ArrayList<String>();
    for (HistoricDecisionInstance historicDecisionInstance: historicDecisionInstances) {
      decisionInstanceIds.add(historicDecisionInstance.getId());
    }
    return decisionInstanceIds;
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupHistoryEmptyProcessIdsException() {
    //given
    final List<String> ids = prepareHistoricProcesses();
    runtimeService.deleteProcessInstances(ids, null, true, true);

    try {
      historyService.deleteHistoricProcessInstancesBulk(null);
      fail("Empty process instance ids exception was expected");
    } catch (BadUserRequestException ex) {
    }

    try {
      historyService.deleteHistoricProcessInstancesBulk(new ArrayList<String>());
      fail("Empty process instance ids exception was expected");
    } catch (BadUserRequestException ex) {
    }

  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupHistoryProcessesNotFinishedException() {
    //given
    final List<String> ids = prepareHistoricProcesses();
    runtimeService.deleteProcessInstances(ids.subList(1, ids.size()), null, true, true);

    try {
      historyService.deleteHistoricProcessInstancesBulk(ids);
      fail("Not all processes are finished exception was expected");
    } catch (BadUserRequestException ex) {
    }

  }

  private void collectHistoricDecisionInputIds(List<HistoricDecisionInstance> historicDecisionInstances, List<String> historicDecisionInputIds, List<String> inputByteArrayIds) {
    for (HistoricDecisionInstance historicDecisionInstance : historicDecisionInstances) {
      for (HistoricDecisionInputInstance inputInstanceEntity : historicDecisionInstance.getInputs()) {
        historicDecisionInputIds.add(inputInstanceEntity.getId());
        final String byteArrayValueId = ((HistoricDecisionInputInstanceEntity) inputInstanceEntity).getByteArrayValueId();
        if (byteArrayValueId != null) {
          inputByteArrayIds.add(byteArrayValueId);
        }
      }
    }
    assertEquals(PROCESS_INSTANCE_COUNT, historicDecisionInputIds.size());
  }

  private void collectHistoricDecisionOutputIds(List<HistoricDecisionInstance> historicDecisionInstances, List<String> historicDecisionOutputIds, List<String> outputByteArrayId) {
    for (HistoricDecisionInstance historicDecisionInstance : historicDecisionInstances) {
      for (HistoricDecisionOutputInstance outputInstanceEntity : historicDecisionInstance.getOutputs()) {
        historicDecisionOutputIds.add(outputInstanceEntity.getId());
        final String byteArrayValueId = ((HistoricDecisionOutputInstanceEntity) outputInstanceEntity).getByteArrayValueId();
        if (byteArrayValueId != null) {
          outputByteArrayId.add(byteArrayValueId);
        }
      }
    }
    assertEquals(PROCESS_INSTANCE_COUNT, historicDecisionOutputIds.size());
  }

  private List<String> prepareHistoricProcesses() {
    return prepareHistoricProcesses(ONE_TASK_PROCESS);
  }

  private List<String> prepareHistoricProcesses(String businessKey) {
    return prepareHistoricProcesses(businessKey, null);
  }

  private List<String> prepareHistoricProcesses(String businessKey, VariableMap variables) {
    return prepareHistoricProcesses(businessKey, variables, PROCESS_INSTANCE_COUNT);
  }

  private List<String> prepareHistoricProcesses(String businessKey, VariableMap variables, Integer processInstanceCount) {
    List<String> processInstanceIds = new ArrayList<String>();

    for (int i = 0; i < processInstanceCount; i++) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(businessKey, variables);
      processInstanceIds.add(processInstance.getId());
    }

    return processInstanceIds;
  }

  private void verifyByteArraysWereRemoved(final String... errorDetailsByteArrayIds) {
    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        for (String errorDetailsByteArrayId : errorDetailsByteArrayIds) {
          assertNull(commandContext.getDbEntityManager().selectOne("selectByteArray", errorDetailsByteArrayId));
        }
        return null;
      }
    });
  }

  private VariableMap getVariables() {
    return Variables.createVariables()
        .putValue("aVariableName", "aVariableValue")
        .putValue("pojoVariableName", new TestPojo("someValue", 111.));
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testCleanupHistoryCaseInstance() {
    // given
    // create case instances
    int instanceCount = 10;
    List<String> caseInstanceIds = prepareHistoricCaseInstance(instanceCount);

    // assume
    List<HistoricCaseInstance> caseInstanceList = historyService.createHistoricCaseInstanceQuery().list();
    assertEquals(instanceCount, caseInstanceList.size());

    // when
    historyService.deleteHistoricCaseInstancesBulk(caseInstanceIds);

    // then
    assertEquals(0, historyService.createHistoricCaseInstanceQuery().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().count());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testCleanupHistoryCaseActivityInstance() {
    // given
    // create case instance
    String caseInstanceId = caseService.createCaseInstanceByKey("oneTaskCase").getId();
    terminateAndCloseCaseInstance(caseInstanceId, null);

    // assume
    List<HistoricCaseActivityInstance> activityInstances = historyService.createHistoricCaseActivityInstanceQuery().list();
    assertEquals(1, activityInstances.size());

    // when
    historyService.deleteHistoricCaseInstancesBulk(Arrays.asList(caseInstanceId));

    // then
    activityInstances = historyService.createHistoricCaseActivityInstanceQuery().list();
    assertEquals(0, activityInstances.size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testCleanupHistoryCaseInstanceTask() {
    // given
    // create case instance
    String caseInstanceId = caseService.createCaseInstanceByKey("oneTaskCase").getId();
    terminateAndCloseCaseInstance(caseInstanceId, null);

    // assume
    List<HistoricTaskInstance> taskInstances = historyService.createHistoricTaskInstanceQuery().list();
    assertEquals(1, taskInstances.size());

    // when
    historyService.deleteHistoricCaseInstancesBulk(Arrays.asList(caseInstanceId));

    // then
    taskInstances = historyService.createHistoricTaskInstanceQuery().list();
    assertEquals(0, taskInstances.size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testCleanupHistoryCaseInstanceTaskComment() {
    // given
    // create case instance
    String caseInstanceId = caseService.createCaseInstanceByKey("oneTaskCase").getId();

    Task task = taskService.createTaskQuery().singleResult();
    taskService.createComment(task.getId(), null, "This is a comment...");

    // assume
    List<Comment> comments = taskService.getTaskComments(task.getId());
    assertEquals(1, comments.size());
    terminateAndCloseCaseInstance(caseInstanceId, null);

    // when
    historyService.deleteHistoricCaseInstancesBulk(Arrays.asList(caseInstanceId));

    // then
    comments = taskService.getTaskComments(task.getId());
    assertEquals(0, comments.size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testCleanupHistoryCaseInstanceTaskDetails() {
    // given
    // create case instance
    CaseInstance caseInstance = caseService.createCaseInstanceByKey("oneTaskCase");

    Task task = taskService.createTaskQuery().singleResult();

    taskService.setVariable(task.getId(), "boo", new TestPojo("foo", 123.0));
    taskService.setVariable(task.getId(), "goo", 9);
    taskService.setVariable(task.getId(), "boo", new TestPojo("foo", 321.0));


    // assume
    List<HistoricDetail> detailsList = historyService.createHistoricDetailQuery().list();
    assertEquals(3, detailsList.size());
    terminateAndCloseCaseInstance(caseInstance.getId(), taskService.getVariables(task.getId()));

    // when
    historyService.deleteHistoricCaseInstancesBulk(Arrays.asList(caseInstance.getId()));

    // then
    detailsList = historyService.createHistoricDetailQuery().list();
    assertEquals(0, detailsList.size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testCleanupHistoryCaseInstanceTaskIdentityLink() {
    // given
    // create case instance
    String caseInstanceId = caseService.createCaseInstanceByKey("oneTaskCase").getId();

    Task task = taskService.createTaskQuery().singleResult();

    // assume
    taskService.addGroupIdentityLink(task.getId(), "accounting", IdentityLinkType.CANDIDATE);
    int identityLinksForTask = taskService.getIdentityLinksForTask(task.getId()).size();
    assertEquals(1, identityLinksForTask);
    terminateAndCloseCaseInstance(caseInstanceId, null);

    // when
    historyService.deleteHistoricCaseInstancesBulk(Arrays.asList(caseInstanceId));

    // then
    List<HistoricIdentityLinkLog> historicIdentityLinkLog = historyService.createHistoricIdentityLinkLogQuery().list();
    assertEquals(0, historicIdentityLinkLog.size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testCleanupHistoryCaseInstanceTaskAttachmentByteArray() {
    // given
    // create case instance
    CaseInstance caseInstance = caseService.createCaseInstanceByKey("oneTaskCase");

    Task task = taskService.createTaskQuery().singleResult();
    String taskId = task.getId();
    taskService.createAttachment("foo", taskId, null, "something", null, new ByteArrayInputStream("someContent".getBytes()));

    // assume
    List<Attachment> attachments = taskService.getTaskAttachments(taskId);
    assertEquals(1, attachments.size());
    String contentId = findAttachmentContentId(attachments);
    terminateAndCloseCaseInstance(caseInstance.getId(), null);

    // when
    historyService.deleteHistoricCaseInstancesBulk(Arrays.asList(caseInstance.getId()));

    // then
    attachments = taskService.getTaskAttachments(taskId);
    assertEquals(0, attachments.size());
    verifyByteArraysWereRemoved(contentId);
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testCleanupHistoryCaseInstanceTaskAttachmentUrl() {
    // given
    // create case instance
    String caseInstanceId = caseService.createCaseInstanceByKey("oneTaskCase").getId();

    Task task = taskService.createTaskQuery().singleResult();
    taskService.createAttachment("foo", task.getId(), null, "something", null, "http://camunda.org");

    // assume
    List<Attachment> attachments = taskService.getTaskAttachments(task.getId());
    assertEquals(1, attachments.size());
    terminateAndCloseCaseInstance(caseInstanceId, null);

    // when
    historyService.deleteHistoricCaseInstancesBulk(Arrays.asList(caseInstanceId));

    // then
    attachments = taskService.getTaskAttachments(task.getId());
    assertEquals(0, attachments.size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testCleanupHistoryCaseInstanceVariables() {
    // given
    // create case instances
    List<String> caseInstanceIds = new ArrayList<String>();
    int instanceCount = 10;
    for (int i = 0; i < instanceCount; i++) {
      VariableMap variables = Variables.createVariables();
      CaseInstance caseInstance = caseService.createCaseInstanceByKey("oneTaskCase", variables.putValue("name" + i, "theValue"));
      caseInstanceIds.add(caseInstance.getId());
      terminateAndCloseCaseInstance(caseInstance.getId(), variables);
    }
    // assume
    List<HistoricVariableInstance> variablesInstances = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals(instanceCount, variablesInstances.size());

    // when
    historyService.deleteHistoricCaseInstancesBulk(caseInstanceIds);

    // then
    variablesInstances = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals(0, variablesInstances.size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testCleanupHistoryCaseInstanceComplexVariable() {
    // given
    // create case instances
    VariableMap variables = Variables.createVariables();
    CaseInstance caseInstance = caseService.createCaseInstanceByKey("oneTaskCase", variables.putValue("pojo", new TestPojo("okay", 13.37)));

    caseService.setVariable(caseInstance.getId(), "pojo", "theValue");

    // assume
    List<HistoricVariableInstance> variablesInstances = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals(1, variablesInstances.size());
    List<HistoricDetail> detailsList = historyService.createHistoricDetailQuery().list();
    assertEquals(2, detailsList.size());
    terminateAndCloseCaseInstance(caseInstance.getId(), variables);

    // when
    historyService.deleteHistoricCaseInstancesBulk(Arrays.asList(caseInstance.getId()));

    // then
    variablesInstances = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals(0, variablesInstances.size());
    detailsList = historyService.createHistoricDetailQuery().list();
    assertEquals(0, detailsList.size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testCleanupHistoryCaseInstanceDetails() {
    // given
    // create case instances
    String variableNameCase1 = "varName1";
    CaseInstance caseInstance1 = caseService.createCaseInstanceByKey("oneTaskCase", Variables.createVariables().putValue(variableNameCase1, "value1"));
    CaseInstance caseInstance2 = caseService.createCaseInstanceByKey("oneTaskCase", Variables.createVariables().putValue("varName2", "value2"));

    caseService.setVariable(caseInstance1.getId(), variableNameCase1, "theValue");

    // assume
    List<HistoricDetail> detailsList = historyService.createHistoricDetailQuery().list();
    assertEquals(3, detailsList.size());
    caseService.terminateCaseExecution(caseInstance1.getId(), caseService.getVariables(caseInstance1.getId()));
    caseService.terminateCaseExecution(caseInstance2.getId(), caseService.getVariables(caseInstance2.getId()));
    caseService.closeCaseInstance(caseInstance1.getId());
    caseService.closeCaseInstance(caseInstance2.getId());

    // when
    historyService.deleteHistoricCaseInstancesBulk(Arrays.asList(caseInstance1.getId(), caseInstance2.getId()));

    // then
    detailsList = historyService.createHistoricDetailQuery().list();
    assertEquals(0, detailsList.size());
  }

  private List<String> prepareHistoricCaseInstance(int instanceCount) {
    List<String> caseInstanceIds = new ArrayList<String>();
    for (int i = 0; i < instanceCount; i++) {
      CaseInstance caseInstance = caseService.createCaseInstanceByKey("oneTaskCase");
      String caseInstanceId = caseInstance.getId();
      caseInstanceIds.add(caseInstanceId);
      terminateAndCloseCaseInstance(caseInstanceId, null);
    }
    return caseInstanceIds;
  }

  private void terminateAndCloseCaseInstance(String caseInstanceId, Map<String, Object> variables) {
    if (variables==null) {
      caseService.terminateCaseExecution(caseInstanceId, variables);
    }else {
      caseService.terminateCaseExecution(caseInstanceId);
    }
    caseService.closeCaseInstance(caseInstanceId);
  }
}
