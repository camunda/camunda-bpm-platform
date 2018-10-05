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

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.AttachmentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.bpmn.async.FailingDelegate;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * @author Tassilo Weidner
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricRootProcessInstanceTest {

  protected final String CALLED_PROCESS_KEY = "calledProcess";
  protected final BpmnModelInstance CALLED_PROCESS = Bpmn.createExecutableProcess(CALLED_PROCESS_KEY)
    .startEvent()
      .userTask("userTask").name("userTask")
      .serviceTask()
        .camundaAsyncBefore()
        .camundaClass(FailingDelegate.class.getName())
    .endEvent().done();

  protected final String CALLING_PROCESS_KEY = "callingProcess";
  protected final BpmnModelInstance CALLING_PROCESS = Bpmn.createExecutableProcess(CALLING_PROCESS_KEY)
    .startEvent()
      .callActivity()
        .calledElement(CALLED_PROCESS_KEY)
    .endEvent().done();

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
  }

  @Test
  public void shouldResolveHistoricActivityInstance() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery()
      .activityId("userTask")
      .singleResult();

    // assume
    assertThat(historicActivityInstance, notNullValue());

    // then
    assertThat(historicActivityInstance.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldResolveHistoricTaskInstance() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
      .taskName("userTask")
      .singleResult();

    // assume
    assertThat(historicTaskInstance, notNullValue());

    // then
    assertThat(historicTaskInstance.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldNotResolveHistoricTaskInstance() {
    // given
    Task task = taskService.newTask();

    // when
    taskService.saveTask(task);

    HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();

    // assume
    assertThat(historicTaskInstance, notNullValue());

    // then
    assertThat(historicTaskInstance.getRootProcessInstanceId(), nullValue());

    // cleanup
    taskService.deleteTask(task.getId(), true);
  }


  @Test
  public void shouldResolveHistoricVariableInstance() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY,
      Variables.createVariables()
        .putValue("aVariableName", Variables.stringValue("aVariableValue")));

    HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();

    // assume
    assertThat(historicVariableInstance, notNullValue());

    // then
    assertThat(historicVariableInstance.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldResolveHistoricDetailByVariableInstanceUpdate() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY,
      Variables.createVariables()
        .putValue("aVariableName", Variables.stringValue("aVariableValue")));

    // when
    runtimeService.setVariable(processInstance.getId(), "aVariableName", Variables.stringValue("anotherVariableValue"));

    List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery()
      .variableUpdates()
      .list();

    // assume
    assertThat(historicDetails.size(), is(2));

    // then
    assertThat(historicDetails.get(0).getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
    assertThat(historicDetails.get(1).getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldResolveHistoricDetailByFormProperty() {
    // given
    testRule.deploy(CALLING_PROCESS);

    DeploymentWithDefinitions deployment = testRule.deploy(CALLED_PROCESS);

    String processDefinitionId = deployment.getDeployedProcessDefinitions().get(0).getId();
    Map<String, Object> properties = new HashMap<>();
    properties.put("aFormProperty", "aFormPropertyValue");

    // when
    ProcessInstance processInstance = formService.submitStartForm(processDefinitionId, properties);

    HistoricDetail historicDetail = historyService.createHistoricDetailQuery().formFields().singleResult();

    // assume
    assertThat(historicDetail, notNullValue());

    // then
    assertThat(historicDetail.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldResolveIncident() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);
    taskService.complete(taskService.createTaskQuery().singleResult().getId());

    String jobId = managementService.createJobQuery()
      .singleResult()
      .getId();

    managementService.setJobRetries(jobId, 0);

    try {
      // when
      managementService.executeJob(jobId);
    } catch (Exception ignored) { }

    List<HistoricIncident> historicIncidents = historyService.createHistoricIncidentQuery().list();

    // assume
    assertThat(historicIncidents.size(), is(2));

    // then
    assertThat(historicIncidents.get(0).getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
    assertThat(historicIncidents.get(1).getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldNotResolveStandaloneIncident() {
    // given
    testRule.deploy(CALLED_PROCESS);

    repositoryService.suspendProcessDefinitionByKey(CALLED_PROCESS_KEY, true, new Date());

    String jobId = managementService.createJobQuery()
      .singleResult()
      .getId();

    managementService.setJobRetries(jobId, 0);

    try {
      // when
      managementService.executeJob(jobId);
    } catch (Exception ignored) { }

    HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().singleResult();

    // assume
    assertThat(historicIncident, notNullValue());

    // then
    assertThat(historicIncident.getRootProcessInstanceId(), nullValue());

    // cleanup
    clearJobLog(jobId);
    clearHistoricIncident(historicIncident);
  }

  @Test
  public void shouldResolveExternalTaskLog() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess("calledProcess")
      .startEvent()
        .serviceTask().camundaExternalTask("anExternalTaskTopic")
      .endEvent().done());

    testRule.deploy(Bpmn.createExecutableProcess("callingProcess")
      .startEvent()
        .callActivity()
          .calledElement("calledProcess")
      .endEvent().done());

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callingProcess");

    HistoricExternalTaskLog ExternalTaskLog = historyService.createHistoricExternalTaskLogQuery().singleResult();

    // assume
    assertThat(ExternalTaskLog, notNullValue());

    // then
    assertThat(ExternalTaskLog.getRootProcessInstanceId(), is(processInstance.getRootProcessInstanceId()));
  }

  @Test
  public void shouldResolveJobLog() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);
    taskService.complete(taskService.createTaskQuery().singleResult().getId());

    String jobId = managementService.createJobQuery()
      .singleResult()
      .getId();

    try {
      // when
      managementService.executeJob(jobId);
    } catch (Exception ignored) { }

    List<HistoricJobLog> jobLog = historyService.createHistoricJobLogQuery().list();

    // assume
    assertThat(jobLog.size(), is(2));

    // then
    assertThat(jobLog.get(0).getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
    assertThat(jobLog.get(1).getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldNotResolveJobLog() {
    // given
    testRule.deploy(CALLED_PROCESS);

    repositoryService.suspendProcessDefinitionByKey(CALLED_PROCESS_KEY, true, new Date());

    // when
    HistoricJobLog jobLog = historyService.createHistoricJobLogQuery().singleResult();

    // assume
    assertThat(jobLog, notNullValue());

    // then
    assertThat(jobLog.getRootProcessInstanceId(), nullValue());

    // cleanup
    managementService.deleteJob(jobLog.getJobId());
    clearJobLog(jobLog.getJobId());
  }

  @Test
  public void shouldResolveUserOperationLog_SetJobRetries() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);
    taskService.complete(taskService.createTaskQuery().singleResult().getId());

    String jobId = managementService.createJobQuery()
      .singleResult()
      .getId();

    // when
    identityService.setAuthenticatedUserId("aUserId");
    managementService.setJobRetries(jobId, 65);
    identityService.clearAuthentication();

    UserOperationLogEntry userOperationLog = historyService.createUserOperationLogQuery().singleResult();

    // assume
    assertThat(userOperationLog, notNullValue());

    // then
    assertThat(userOperationLog.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldResolveUserOperationLog_SetExternalTaskRetries() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess("calledProcess")
      .startEvent()
        .serviceTask().camundaExternalTask("anExternalTaskTopic")
      .endEvent().done());

    testRule.deploy(Bpmn.createExecutableProcess("callingProcess")
      .startEvent()
        .callActivity()
          .calledElement("calledProcess")
      .endEvent().done());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callingProcess");

    // when
    identityService.setAuthenticatedUserId("aUserId");
    externalTaskService.setRetries(externalTaskService.createExternalTaskQuery().singleResult().getId(), 65);
    identityService.clearAuthentication();

    UserOperationLogEntry userOperationLog = historyService.createUserOperationLogQuery().singleResult();

    // assume
    assertThat(userOperationLog, notNullValue());

    // then
    assertThat(userOperationLog.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldResolveUserOperationLog_ClaimTask() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    // when
    identityService.setAuthenticatedUserId("aUserId");
    taskService.claim(taskService.createTaskQuery().singleResult().getId(), "aUserId");
    identityService.clearAuthentication();

    UserOperationLogEntry userOperationLog = historyService.createUserOperationLogQuery().singleResult();

    // assume
    assertThat(userOperationLog, notNullValue());

    // then
    assertThat(userOperationLog.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldResolveUserOperationLog_CreateAttachment() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    // when
    identityService.setAuthenticatedUserId("aUserId");
    taskService.createAttachment(null, null, runtimeService.createProcessInstanceQuery().activityIdIn("userTask").singleResult().getId(), null, null, "http://camunda.com");
    identityService.clearAuthentication();

    UserOperationLogEntry userOperationLog = historyService.createUserOperationLogQuery().singleResult();

    // assume
    assertThat(userOperationLog, notNullValue());

    // then
    assertThat(userOperationLog.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldResolveIdentityLink_AddCandidateUser() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    // when
    taskService.addCandidateUser(taskService.createTaskQuery().singleResult().getId(), "aUserId");

    HistoricIdentityLinkLog historicIdentityLinkLog = historyService.createHistoricIdentityLinkLogQuery().singleResult();

    // assume
    assertThat(historicIdentityLinkLog, notNullValue());

    // then
    assertThat(historicIdentityLinkLog.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldNotResolveIdentityLink_AddCandidateUser() {
    // given
    Task aTask = taskService.newTask();
    taskService.saveTask(aTask);

    // when
    taskService.addCandidateUser(aTask.getId(), "aUserId");

    HistoricIdentityLinkLog historicIdentityLinkLog = historyService.createHistoricIdentityLinkLogQuery().singleResult();

    // assume
    assertThat(historicIdentityLinkLog, notNullValue());

    // then
    assertThat(historicIdentityLinkLog.getRootProcessInstanceId(), nullValue());

    // cleanup
    taskService.complete(aTask.getId());
    clearHistoricTaskInst(aTask.getId());
  }

  @Test
  public void shouldResolveCommentByProcessInstanceId() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String processInstanceId = runtimeService.createProcessInstanceQuery()
      .activityIdIn("userTask")
      .singleResult()
      .getId();

    // when
    taskService.createComment(null, processInstanceId, "aMessage");

    Comment comment = taskService.getProcessInstanceComments(processInstanceId).get(0);

    // assume
    assertThat(comment, notNullValue());

    // then
    assertThat(comment.getRootProcessInstanceId(), is(processInstance.getRootProcessInstanceId()));
  }

  @Test
  public void shouldResolveCommentByTaskId() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.createComment(taskId, null, "aMessage");

    Comment comment = taskService.getTaskComments(taskId).get(0);

    // assume
    assertThat(comment, notNullValue());

    // then
    assertThat(comment.getRootProcessInstanceId(), is(processInstance.getRootProcessInstanceId()));
  }

  @Test
  public void shouldNotResolveCommentByWrongTaskIdAndProcessInstanceId() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String processInstanceId = runtimeService.createProcessInstanceQuery()
      .activityIdIn("userTask")
      .singleResult()
      .getId();

    // when
    taskService.createComment("aNonExistentTaskId", processInstanceId, "aMessage");

    Comment comment = taskService.getProcessInstanceComments(processInstanceId).get(0);

    // assume
    assertThat(comment, notNullValue());

    // then
    assertThat(comment.getRootProcessInstanceId(), nullValue());
  }

  @Test
  public void shouldResolveCommentByTaskIdAndWrongProcessInstanceId() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.createComment(taskId, "aNonExistentProcessInstanceId", "aMessage");

    Comment comment = taskService.getTaskComments(taskId).get(0);

    // assume
    assertThat(comment, notNullValue());

    // then
    assertThat(comment.getRootProcessInstanceId(), is(processInstance.getRootProcessInstanceId()));
  }

  @Test
  public void shouldNotResolveCommentByWrongProcessInstanceId() {
    // given

    // when
    taskService.createComment(null, "aNonExistentProcessInstanceId", "aMessage");

    Comment comment = taskService.getProcessInstanceComments("aNonExistentProcessInstanceId").get(0);

    // assume
    assertThat(comment, notNullValue());

    // then
    assertThat(comment.getRootProcessInstanceId(), nullValue());

    // cleanup
    clearCommentByProcessInstanceId("aNonExistentProcessInstanceId");
  }

  @Test
  public void shouldNotResolveCommentByWrongTaskId() {
    // given

    // when
    taskService.createComment("aNonExistentTaskId", null, "aMessage");

    Comment comment = taskService.getTaskComments("aNonExistentTaskId").get(0);

    // assume
    assertThat(comment, notNullValue());

    // then
    assertThat(comment.getRootProcessInstanceId(), nullValue());

    // cleanup
    clearCommentByTaskId("aNonExistentTaskId");
  }

  @Test
  public void shouldResolveAttachmentByProcessInstanceId() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String processInstanceId = runtimeService.createProcessInstanceQuery()
      .activityIdIn("userTask")
      .singleResult()
      .getId();

    // when
    String attachmentId = taskService.createAttachment(null, null, processInstanceId, null, null, "http://camunda.com").getId();

    Attachment attachment = taskService.getAttachment(attachmentId);

    // assume
    assertThat(attachment, notNullValue());

    // then
    assertThat(attachment.getRootProcessInstanceId(), is(processInstance.getRootProcessInstanceId()));
  }

  @Test
  public void shouldResolveAttachmentByTaskId() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    String attachmentId = taskService.createAttachment(null, taskId, null, null, null, "http://camunda.com").getId();

    Attachment attachment = taskService.getAttachment(attachmentId);

    // assume
    assertThat(attachment, notNullValue());

    // then
    assertThat(attachment.getRootProcessInstanceId(), is(processInstance.getRootProcessInstanceId()));
  }

  @Test
  public void shouldNotResolveAttachmentByWrongTaskIdAndProcessInstanceId() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String processInstanceId = runtimeService.createProcessInstanceQuery()
      .activityIdIn("userTask")
      .singleResult()
      .getId();

    // when
    String attachmentId = taskService.createAttachment(null, "aWrongTaskId", processInstanceId, null, null, "http://camunda.com").getId();

    Attachment attachment = taskService.getAttachment(attachmentId);

    // assume
    assertThat(attachment, notNullValue());

    // then
    assertThat(attachment.getRootProcessInstanceId(), nullValue());
  }

  @Test
  public void shouldResolveAttachmentByTaskIdAndWrongProcessInstanceId() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String taskId = taskService.createTaskQuery()
      .singleResult()
      .getId();

    // when
    String attachmentId = taskService.createAttachment(null, taskId, "aWrongProcessInstanceId", null, null, "http://camunda.com").getId();

    Attachment attachment = taskService.getAttachment(attachmentId);

    // assume
    assertThat(attachment, notNullValue());

    // then
    assertThat(attachment.getRootProcessInstanceId(), is(processInstance.getRootProcessInstanceId()));
  }

  @Test
  public void shouldNotResolveAttachmentByWrongTaskId() {
    // given

    // when
    String attachmentId = taskService.createAttachment(null, "aWrongTaskId", null, null, null, "http://camunda.com").getId();

    Attachment attachment = taskService.getAttachment(attachmentId);

    // assume
    assertThat(attachment, notNullValue());

    // then
    assertThat(attachment.getRootProcessInstanceId(), nullValue());

    // cleanup
    clearAttachment(attachment);
  }

  protected void clearAttachment(final Attachment attachment) {
    CommandExecutor commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getAttachmentManager().delete((AttachmentEntity) attachment);
        return null;
      }
    });
  }

  protected void clearCommentByTaskId(final String taskId) {
    CommandExecutor commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getCommentManager().deleteCommentsByTaskId(taskId);
        return null;
      }
    });
  }

  protected void clearCommentByProcessInstanceId(final String processInstanceId) {
    CommandExecutor commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getCommentManager().deleteCommentsByProcessInstanceIds(Collections.singletonList(processInstanceId));
        return null;
      }
    });
  }

  protected void clearHistoricTaskInst(final String taskId) {
    CommandExecutor commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricTaskInstanceManager().deleteHistoricTaskInstanceById(taskId);
        commandContext.getHistoricIdentityLinkManager().deleteHistoricIdentityLinksLogByTaskId(taskId);
        return null;
      }
    });
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

  protected void clearHistoricIncident(final HistoricIncident historicIncident) {
    CommandExecutor commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricIncidentManager().delete((HistoricIncidentEntity) historicIncident);
        return null;
      }
    });
  }

}
