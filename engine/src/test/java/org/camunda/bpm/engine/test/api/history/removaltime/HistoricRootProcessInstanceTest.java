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
package org.camunda.bpm.engine.test.api.history.removaltime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInputInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionOutputInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity;
import org.camunda.bpm.engine.impl.persistence.entity.AttachmentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.async.FailingDelegate;
import org.camunda.bpm.engine.test.dmn.businessruletask.TestPojo;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;

/**
 * @author Tassilo Weidner
 */
public class HistoricRootProcessInstanceTest extends AbstractRemovalTimeTest {

  protected final String CALLED_PROCESS_KEY = "calledProcess";
  protected final BpmnModelInstance CALLED_PROCESS = Bpmn.createExecutableProcess(CALLED_PROCESS_KEY)
    .startEvent()
      .userTask("userTask")
        .name("userTask")
        .camundaAssignee("foo")
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

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldResolveHistoricDecisionInstance() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess(CALLING_PROCESS_KEY)
    .startEvent()
      .businessRuleTask()
        .camundaDecisionRef("dish-decision")
    .endEvent().done());

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY,
      Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend"));

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // assume
    assertThat(historicDecisionInstances.size(), is(3));

    // then
    assertThat(historicDecisionInstances.get(0).getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
    assertThat(historicDecisionInstances.get(1).getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
    assertThat(historicDecisionInstances.get(2).getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldResolveHistoricDecisionInputInstance() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess(CALLING_PROCESS_KEY)
    .startEvent()
      .businessRuleTask()
        .camundaDecisionRef("dish-decision")
    .endEvent().done());

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY,
      Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend"));

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeInputs()
      .singleResult();

    // assume
    assertThat(historicDecisionInstance, notNullValue());

    List<HistoricDecisionInputInstance> historicDecisionInputInstances = historicDecisionInstance.getInputs();

    // then
    assertThat(historicDecisionInputInstances.get(0).getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
    assertThat(historicDecisionInputInstances.get(1).getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldNotResolveHistoricDecisionInputInstance() {
    // given

    // when
    decisionService.evaluateDecisionTableByKey("dish-decision", Variables.createVariables()
      .putValue("temperature", 32)
      .putValue("dayType", "Weekend"));

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeInputs()
      .singleResult();

    // assume
    assertThat(historicDecisionInstance, notNullValue());

    List<HistoricDecisionInputInstance> historicDecisionInputInstances = historicDecisionInstance.getInputs();

    // then
    assertThat(historicDecisionInputInstances.get(0).getRootProcessInstanceId(), nullValue());
    assertThat(historicDecisionInputInstances.get(1).getRootProcessInstanceId(), nullValue());
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldResolveHistoricDecisionOutputInstance() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess(CALLING_PROCESS_KEY)
    .startEvent()
      .businessRuleTask()
        .camundaDecisionRef("dish-decision")
    .endEvent().done());

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY,
      Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend"));

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeOutputs()
      .singleResult();

    // assume
    assertThat(historicDecisionInstance, notNullValue());

    List<HistoricDecisionOutputInstance> historicDecisionOutputInstances = historicDecisionInstance.getOutputs();

    // then
    assertThat(historicDecisionOutputInstances.get(0).getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldNotResolveHistoricDecisionOutputInstance() {
    // given

    // when
    decisionService.evaluateDecisionTableByKey("dish-decision", Variables.createVariables()
      .putValue("temperature", 32)
      .putValue("dayType", "Weekend"));

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeOutputs()
      .singleResult();

    // assume
    assertThat(historicDecisionInstance, notNullValue());

    List<HistoricDecisionOutputInstance> historicDecisionOutputInstances = historicDecisionInstance.getOutputs();

    // then
    assertThat(historicDecisionOutputInstances.get(0).getRootProcessInstanceId(), nullValue());
  }

  @Test
  public void shouldResolveHistoricProcessInstance() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
      .activeActivityIdIn("userTask")
      .singleResult();

    // assume
    assertThat(historicProcessInstance, notNullValue());

    // then
    assertThat(historicProcessInstance.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
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

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    identityService.setAuthenticatedUserId("aUserId");
    taskService.setAssignee(taskId, "aUserId");
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

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.addCandidateUser(taskId, "aUserId");

    HistoricIdentityLinkLog historicIdentityLinkLog =
        historyService.createHistoricIdentityLinkLogQuery()
            .userId("aUserId")
            .singleResult();

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

  @Test
  public void shouldResolveByteArray_CreateAttachmentByTask() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    AttachmentEntity attachment = (AttachmentEntity) taskService.createAttachment(null, taskId, null, null, null, new ByteArrayInputStream("hello world".getBytes()));

    ByteArrayEntity byteArray = findByteArrayById(attachment.getContentId());

    // assume
    assertThat(byteArray, notNullValue());

    // then
    assertThat(byteArray.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldResolveByteArray_CreateAttachmentByProcessInstance() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String calledProcessInstanceId = runtimeService.createProcessInstanceQuery()
      .activityIdIn("userTask")
      .singleResult()
      .getId();

    // when
    AttachmentEntity attachment = (AttachmentEntity) taskService.createAttachment(null, null, calledProcessInstanceId, null, null, new ByteArrayInputStream("hello world".getBytes()));

    ByteArrayEntity byteArray = findByteArrayById(attachment.getContentId());

    // assume
    assertThat(byteArray, notNullValue());

    // then
    assertThat(byteArray.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldResolveByteArray_SetVariable() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    // when
    runtimeService.setVariable(processInstance.getId(), "aVariableName", new ByteArrayInputStream("hello world".getBytes()));

    HistoricVariableInstanceEntity historicVariableInstance = (HistoricVariableInstanceEntity) historyService.createHistoricVariableInstanceQuery().singleResult();

    ByteArrayEntity byteArray = findByteArrayById(historicVariableInstance.getByteArrayId());

    // assume
    assertThat(byteArray, notNullValue());

    // then
    assertThat(byteArray.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldResolveByteArray_UpdateVariable() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY,
      Variables.createVariables()
        .putValue("aVariableName", Variables.stringValue("aVariableValue")));

    // when
    runtimeService.setVariable(processInstance.getId(), "aVariableName", new ByteArrayInputStream("hello world".getBytes()));

    HistoricDetailVariableInstanceUpdateEntity historicDetails = (HistoricDetailVariableInstanceUpdateEntity) historyService.createHistoricDetailQuery()
      .variableUpdates()
      .variableTypeIn("Bytes")
      .singleResult();

    // assume
    ByteArrayEntity byteArray = findByteArrayById(historicDetails.getByteArrayValueId());

    // then
    assertThat(byteArray.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldResolveByteArray_JobLog() {
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

    HistoricJobLogEventEntity jobLog = (HistoricJobLogEventEntity) historyService.createHistoricJobLogQuery()
      .jobExceptionMessage("I'm supposed to fail!")
      .singleResult();

    // assume
    assertThat(jobLog, notNullValue());

    ByteArrayEntity byteArray = findByteArrayById(jobLog.getExceptionByteArrayId());

    // then
    assertThat(byteArray.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldResolveByteArray_ExternalTaskLog() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess("calledProcess")
      .startEvent()
        .serviceTask().camundaExternalTask("aTopicName")
      .endEvent().done());

    testRule.deploy(Bpmn.createExecutableProcess("callingProcess")
      .startEvent()
        .callActivity()
          .calledElement("calledProcess")
      .endEvent().done());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callingProcess");

    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, "aWorkerId")
      .topic("aTopicName", Integer.MAX_VALUE)
      .execute();

    // when
    externalTaskService.handleFailure(tasks.get(0).getId(), "aWorkerId", null, "errorDetails", 5, 3000L);

    HistoricExternalTaskLogEntity externalTaskLog = (HistoricExternalTaskLogEntity) historyService.createHistoricExternalTaskLogQuery()
      .failureLog()
      .singleResult();

    // assume
    assertThat(externalTaskLog, notNullValue());

    ByteArrayEntity byteArrayEntity = findByteArrayById(externalTaskLog.getErrorDetailsByteArrayId());

    // then
    assertThat(byteArrayEntity.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml"
  })
  public void shouldResolveByteArray_DecisionInput() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess(CALLING_PROCESS_KEY)
      .startEvent()
        .businessRuleTask().camundaDecisionRef("testDecision")
      .endEvent().done());

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY,
      Variables.createVariables()
        .putValue("pojo", new TestPojo("okay", 13.37)));

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeInputs()
      .singleResult();

    // assume
    assertThat(historicDecisionInstance, notNullValue());

    HistoricDecisionInputInstanceEntity historicDecisionInputInstanceEntity = (HistoricDecisionInputInstanceEntity) historicDecisionInstance.getInputs().get(0);

    ByteArrayEntity byteArrayEntity = findByteArrayById(historicDecisionInputInstanceEntity.getByteArrayValueId());

    // then
    assertThat(byteArrayEntity.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml"
  })
  public void shouldResolveByteArray_DecisionOutput() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess(CALLING_PROCESS_KEY)
      .startEvent()
        .businessRuleTask().camundaDecisionRef("testDecision")
      .endEvent().done());

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY,
      Variables.createVariables()
        .putValue("pojo", new TestPojo("okay", 13.37)));

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeOutputs()
      .singleResult();

    // assume
    assertThat(historicDecisionInstance, notNullValue());

    HistoricDecisionOutputInstanceEntity historicDecisionOutputInstanceEntity = (HistoricDecisionOutputInstanceEntity) historicDecisionInstance.getOutputs().get(0);

    ByteArrayEntity byteArrayEntity = findByteArrayById(historicDecisionOutputInstanceEntity.getByteArrayValueId());

    // then
    assertThat(byteArrayEntity.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  @Deployment
  public void shouldResolveByteArray_DecisionOutputLiteralExpression() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess(CALLING_PROCESS_KEY)
      .startEvent()
        .businessRuleTask().camundaDecisionRef("testDecision")
      .endEvent().done());

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY,
      Variables.createVariables()
        .putValue("pojo", new TestPojo("okay", 13.37)));

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeOutputs()
      .singleResult();

    // assume
    assertThat(historicDecisionInstance, notNullValue());

    HistoricDecisionOutputInstanceEntity historicDecisionOutputInstanceEntity = (HistoricDecisionOutputInstanceEntity) historicDecisionInstance.getOutputs().get(0);

    ByteArrayEntity byteArrayEntity = findByteArrayById(historicDecisionOutputInstanceEntity.getByteArrayValueId());

    // then
    assertThat(byteArrayEntity.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));
  }

  @Test
  public void shouldResolveAuthorization() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS);

    // when
    enabledAuth();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);
    disableAuth();

    Authorization authorization = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_TASK)
        .singleResult();

    // assume
    assertThat(authorization, notNullValue());

    // then
    assertThat(authorization.getRootProcessInstanceId(), is(processInstance.getProcessInstanceId()));

    // clear
    clearAuthorization();
  }

}
