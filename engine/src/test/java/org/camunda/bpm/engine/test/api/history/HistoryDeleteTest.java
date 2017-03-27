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
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInputInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionOutputInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AttachmentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Attachment;
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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Copy of {@link BulkHistoryDeleteTest}, but testing another method: {@link HistoryService#deleteHistoricProcessInstances(List)}.
 * @author Svetlana Dorokhova
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoryDeleteTest {

  protected static final String ONE_TASK_PROCESS = "oneTaskProcess";

  public static final int PROCESS_INSTANCE_COUNT = 5;

  public ProcessEngineRule engineRule = new ProcessEngineRule(true);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  private HistoryService historyService;
  private TaskService taskService;
  private RuntimeService runtimeService;
  private FormService formService;
  private ExternalTaskService externalTaskService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
    formService = engineRule.getFormService();
    externalTaskService = engineRule.getExternalTaskService();
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
    historyService.deleteHistoricProcessInstances(ids);

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
    historyService.deleteHistoricProcessInstances(ids);

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
    historyService.deleteHistoricProcessInstances(ids);

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
  @Ignore("CAM-7566")
  public void FAILING_testCleanupProcessInstanceAttachmentWithContent() {
    //given
    final List<String> ids = prepareHistoricProcesses();

    String processInstanceWithAttachmentId = ids.get(0);
    createProcessInstanceAttachmentWithContent(processInstanceWithAttachmentId);
    //remember contentId
    final String contentId = findAttachmentContentId(taskService.getProcessInstanceAttachments(processInstanceWithAttachmentId));

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstances(ids);

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
    historyService.deleteHistoricProcessInstances(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, taskService.getTaskComments(taskWithCommentId).size());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  @Ignore("CAM-7566")
  public void FAILING_testCleanupProcessInstanceComment() {
    //given
    final List<String> ids = prepareHistoricProcesses();

    String processInstanceWithCommentId = ids.get(0);
    taskService.createComment(null, processInstanceWithCommentId, "Some comment");

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstances(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
    assertEquals(0, taskService.getProcessInstanceComments(processInstanceWithCommentId).size());
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  @Ignore("CAM-7566")
  public void FAILING_testCleanupHistoricVariableInstancesAndHistoricDetails() {
    //given
    final List<String> ids = prepareHistoricProcesses();

    List<Task> taskList = taskService.createTaskQuery().list();

    taskService.setVariables(taskList.get(0).getId(), getVariables());

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstances(ids);

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
    historyService.deleteHistoricProcessInstances(ids);

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
    historyService.deleteHistoricProcessInstances(ids);

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
    historyService.deleteHistoricProcessInstances(ids);

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
    historyService.deleteHistoricProcessInstances(ids);

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
  @Ignore("CAM-7566")
  public void FAILING_testCleanupHistoryDecisionData() {
    //given
    List<String> ids = prepareHistoricProcesses("testProcess", Variables.createVariables().putValue("pojo", new TestPojo("okay", 13.37)));

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //remember input and output ids
    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().includeInputs().includeOutputs().list();
    final List<String> historicDecisionInputIds = collectHistoricDecisionInputIds(historicDecisionInstances);
    final List<String> historicDecisionOutputIds = collectHistoricDecisionOutputIds(historicDecisionInstances);

    //when
    historyService.deleteHistoricProcessInstances(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("testProcess").count());
    assertEquals(0, historyService.createHistoricDecisionInstanceQuery().count());

    //check that decision inputs and outputs were removed
    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        for (String inputId : historicDecisionInputIds) {
          assertNull(commandContext.getDbEntityManager().selectById(HistoricDecisionInputInstanceEntity.class, inputId));
        }
        for (String outputId : historicDecisionOutputIds) {
          assertNull(commandContext.getDbEntityManager().selectById(HistoricDecisionOutputInstanceEntity.class, outputId));
        }
        return null;
      }
    });

  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testCleanupHistoryEmptyProcessIdsException() {
    //given
    final List<String> ids = prepareHistoricProcesses();
    runtimeService.deleteProcessInstances(ids, null, true, true);

    try {
      historyService.deleteHistoricProcessInstances(null);
      fail("Empty process instance ids exception was expected");
    } catch (BadUserRequestException ex) {
    }

    try {
      historyService.deleteHistoricProcessInstances(new ArrayList<String>());
      fail("Empty process instance ids exception was expected");
    } catch (BadUserRequestException ex) {
    }

  }

  private List<String> collectHistoricDecisionInputIds(List<HistoricDecisionInstance> historicDecisionInstances) {
    List<String> result = new ArrayList<String>();
    for (HistoricDecisionInstance historicDecisionInstance : historicDecisionInstances) {
      for (HistoricDecisionInputInstance inputInstanceEntity : historicDecisionInstance.getInputs()) {
        result.add(inputInstanceEntity.getId());
      }
    }
    assertEquals(PROCESS_INSTANCE_COUNT, result.size());
    return result;
  }

  private List<String> collectHistoricDecisionOutputIds(List<HistoricDecisionInstance> historicDecisionInstances) {
    List<String> result = new ArrayList<String>();
    for (HistoricDecisionInstance historicDecisionInstance : historicDecisionInstances) {
      for (HistoricDecisionOutputInstance outputInstanceEntity : historicDecisionInstance.getOutputs()) {
        result.add(outputInstanceEntity.getId());
      }
    }
    assertEquals(PROCESS_INSTANCE_COUNT, result.size());
    return result;
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

}
