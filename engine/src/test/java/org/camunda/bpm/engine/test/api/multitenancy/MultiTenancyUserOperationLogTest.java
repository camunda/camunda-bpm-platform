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
package org.camunda.bpm.engine.test.api.multitenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_FULL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.batch.BatchMigrationHelper;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
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

@RequiredHistoryLevel(HISTORY_FULL)
public class MultiTenancyUserOperationLogTest {

  protected static final String USER_ID = "aUserId";
  protected static final String USER_WITHOUT_TENANT = "aUserId1";

  protected static final String TENANT_ONE = "tenant1";

  protected static final String PROCESS_NAME = "process";
  protected static final String TASK_ID = "aTaskId";
  protected static final String AN_ANNOTATION = "anAnnotation";

  // normalize timestamps for databases which do not provide millisecond precision.
  protected Date today = new Date((ClockUtil.getCurrentTime().getTime() / 1000) * 1000);
  protected Date tomorrow = new Date(((ClockUtil.getCurrentTime().getTime() + 86400000) / 1000) * 1000);
  protected Date yesterday = new Date(((ClockUtil.getCurrentTime().getTime() - 86400000) / 1000) * 1000);
  protected Task userTask;


  protected static final BpmnModelInstance MODEL = Bpmn.createExecutableProcess(PROCESS_NAME)
      .startEvent().userTask(TASK_ID).done();
  protected static final BpmnModelInstance MODEL_JOB = Bpmn.createExecutableProcess(PROCESS_NAME)
      .startEvent().userTask(TASK_ID).camundaAsyncBefore().done();

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  protected BatchMigrationHelper batchHelper = new BatchMigrationHelper(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfiguration configuration;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected IdentityService identityService;
  protected ManagementService managementService;
  protected boolean isDefaultTenantCheckEnabled;

  @Before
  public void init() {
    configuration = engineRule.getProcessEngineConfiguration();
    taskService = engineRule.getTaskService();
    historyService = engineRule.getHistoryService();
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();
    identityService = engineRule.getIdentityService();
    managementService = engineRule.getManagementService();
    isDefaultTenantCheckEnabled = configuration.isTenantCheckEnabled();
    configuration.setTenantCheckEnabled(false);
  }

  @After
  public void tearDown() {
    configuration.setTenantCheckEnabled(isDefaultTenantCheckEnabled);
  }

  @Test
  public void shouldLogUserOperationsWithTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, MODEL);
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));

    runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.complete(taskId);

    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery().entityType(EntityTypes.TASK).singleResult();

    historyService.setAnnotationForOperationLogById(singleResult.getOperationId(), AN_ANNOTATION);

    // when
    historyService.clearAnnotationForOperationLogById(singleResult.getOperationId());
    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.OPERATION_LOG)
        .list();

    // then
    assertThat(list.size()).isEqualTo(2);
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(TENANT_ONE);
   }
  }

  @Test
  public void shouldLogIncidentOperationsWithTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, MODEL);
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    Incident incident = runtimeService.createIncident("foo", processInstance.getId(), TASK_ID, "bar");


    // when
    runtimeService.setAnnotationForIncidentById(incident.getId(), AN_ANNOTATION);
    runtimeService.clearAnnotationForIncidentById(incident.getId());
    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.INCIDENT)
        .list();

    // then
     assertThat(list.size()).isEqualTo(2);
     for (UserOperationLogEntry userOperationLogEntry : list) {
       assertThat(userOperationLogEntry.getTenantId()).isEqualTo(TENANT_ONE);
    }
  }

  @Test
  public void shouldLogIdentityLinkOperationsWithTenant() {
    //given
    testRule.deployForTenant(TENANT_ONE, MODEL);
    // create a process with a userTask and work with it
    runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    String processTaskId = taskService.createTaskQuery().singleResult().getId();

    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));

    // when create and remove some links
    taskService.addCandidateUser(processTaskId, "they");
    taskService.deleteCandidateUser(processTaskId, "they");
    taskService.addCandidateGroup(processTaskId, "we");
    taskService.deleteCandidateGroup(processTaskId, "we");

    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery().list();

    // then
    assertThat(list.size()).isEqualTo(4);
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getEntityType()).isEqualTo(EntityTypes.IDENTITY_LINK);
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(TENANT_ONE);
    }
  }

  @Test
  public void shouldLogAttachmentOperationsWithTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, MODEL);
    // create a process with a userTask and work with it
    ProcessInstance process = runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    String processTaskId = taskService.createTaskQuery().singleResult().getId();

    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));

    // when add and delete an attachment
    Attachment attachment = taskService.createAttachment("image/ico", processTaskId, process.getId(), "favicon.ico", "favicon", "http://camunda.com/favicon.ico");
    taskService.deleteAttachment(attachment.getId());

    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery().list();

    // then
    assertThat(list.size()).isEqualTo(2);
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getEntityType()).isEqualTo(EntityTypes.ATTACHMENT);
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(TENANT_ONE);
    }
  }


  @Test
  public void shouldLogTaskOperationsWithTenant() {
    //given
    testRule.deployForTenant(TENANT_ONE, MODEL);
    // create a process with a userTask and work with it
    runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    String processTaskId = taskService.createTaskQuery().singleResult().getId();

    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));

    // assign and reassign the userTask
    ClockUtil.setCurrentTime(today);
    taskService.setOwner(processTaskId, "icke");
    taskService.claim(processTaskId, "icke");
    taskService.setAssignee(processTaskId, "er");

    // change priority of task
    taskService.setPriority(processTaskId, 10);

    // complete the userTask to finish the process
    taskService.complete(processTaskId);

    // when
    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery().list();

    // then
    assertThat(list.size()).isEqualTo(5);
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getEntityType()).isEqualTo(EntityTypes.TASK);
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(TENANT_ONE);
    }
  }

  @Test
  public void shouldLogStandaloneTaskOperationsWithTenant() {
    // given
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    // create a standalone userTask
    userTask = taskService.newTask();
    userTask.setName("to do");
    userTask.setTenantId(TENANT_ONE);
    taskService.saveTask(userTask);

    // change some properties manually to create an update event
    ClockUtil.setCurrentTime(tomorrow);
    userTask.setDescription("desc");
    userTask.setOwner("icke");
    userTask.setAssignee("er");
    userTask.setDueDate(new Date());
    taskService.saveTask(userTask);

    // complete the userTask
    taskService.complete(userTask.getId());
    historyService.deleteHistoricTaskInstance(userTask.getId());// 2 log entries

    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.TASK).list();

    // then
    assertThat(list.size()).isEqualTo(8);
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(TENANT_ONE);
    }
  }

  @Test
  public void shouldLogJobDefinitionOperationWithTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, MODEL_JOB);
    runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when set a job priority
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 42);

    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.JOB_DEFINITION)
        .operationType(UserOperationLogEntry.OPERATION_TYPE_SET_PRIORITY)
        .singleResult();

    // then
    assertThat(singleResult.getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void shouldLogJobOperationWithTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, MODEL_JOB);
    runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    Job job = managementService.createJobQuery().singleResult();

    // I set a job priority
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    managementService.setJobRetries(job.getId(), 4);

    // when
    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.JOB)
        .operationType(UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES)
        .singleResult();

    // then
    assertThat(singleResult.getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void shouldLogProcessInstanceOperationsWithTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, MODEL);
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    ProcessInstance process = runtimeService.startProcessInstanceByKey(PROCESS_NAME);

    // when
    runtimeService.suspendProcessInstanceById(process.getId());
    runtimeService.activateProcessInstanceById(process.getId());

    runtimeService.deleteProcessInstance(process.getId(), "a delete reason");

    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.PROCESS_INSTANCE)
        .list();

    // then
    assertThat(list.size()).isEqualTo(4);
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(TENANT_ONE);
    }
  }

  @Test
  public void shouldLogProcessDefinitionOperationsWithTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, MODEL);
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().singleResult();

    // when
    repositoryService.updateProcessDefinitionHistoryTimeToLive(definition.getId(), 5);
    repositoryService.deleteProcessDefinition(definition.getId());
    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.PROCESS_DEFINITION)
        .list();

    // then
    assertThat(list.size()).isEqualTo(2);
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(TENANT_ONE);
    }
  }

  @Test
  public void shouldLogVariableOperationsWithTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, MODEL);
    ProcessInstance process = runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));

    // when
    runtimeService.setVariable(process.getId(), "myVariable", 10);


    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE)
        .entityType(EntityTypes.VARIABLE)
        .singleResult();

    // then
      assertThat(singleResult.getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void shouldLogDeployOperationWithTenant() {
    // given
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    String deploymentId = testRule.deployForTenant(TENANT_ONE, "org/camunda/bpm/engine/test/api/externaltask/externalTaskPriorityExpression.bpmn20.xml").getId();

    // when
    repositoryService.deleteDeployment(deploymentId);

    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.DEPLOYMENT)
        .list();

    // then
    assertThat(list.size()).isEqualTo(2);
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(TENANT_ONE);
    }
  }
  @Test
  public void shouldLogBatchOperationWithTenant() {
    // given
    Batch batch = batchHelper.migrateProcessInstanceAsync(
        testRule.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS),
        testRule.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS));

    // when
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    managementService.suspendBatchById(batch.getId());
    managementService.deleteBatch(batch.getId(), true);
    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.BATCH)
        .list();

    // then
    assertThat(list.size()).isEqualTo(2);
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(TENANT_ONE);
    }
  }

  @Test
  public void shouldLogExternalTaskOperationWithTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, "org/camunda/bpm/engine/test/api/externaltask/externalTaskPriorityExpression.bpmn20.xml");
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    runtimeService.startProcessInstanceByKey("twoExternalTaskWithPriorityProcess", Collections.<String, Object>singletonMap("priority", 14));
    ExternalTaskService externalTaskService = engineRule.getExternalTaskService();
    ExternalTask externalTask = externalTaskService.createExternalTaskQuery().priorityHigherThanOrEquals(1).singleResult();

    // when
    externalTaskService.setPriority(externalTask.getId(), 78L);
    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_SET_PRIORITY)
        .entityType(EntityTypes.EXTERNAL_TASK)
        .singleResult();

    // then
    assertThat(singleResult.getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void shouldLogDecisionDefinitionOperationWithTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, "org/camunda/bpm/engine/test/api/dmn/Example.dmn");
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

    // when
    repositoryService.updateDecisionDefinitionHistoryTimeToLive(decisionDefinition.getId(), 6);
    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_UPDATE_HISTORY_TIME_TO_LIVE)
        .entityType(EntityTypes.DECISION_DEFINITION)
        .list();

    // then
    assertThat(list.size()).isEqualTo(3); // 3 properties
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(TENANT_ONE);
    }
  }

  @Test
  public void shouldLogDecisionInstanceOperationWithTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, "org/camunda/bpm/engine/test/api/dmn/Example.dmn");
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    engineRule.getDecisionService().evaluateDecisionByKey("decision")
    .variables(
        Variables.createVariables()
        .putValue("status", "silver")
        .putValue("sum", 723)
    ).evaluate();
    String instanceId =  historyService.createHistoricDecisionInstanceQuery().singleResult().getId();

    // when
    historyService.deleteHistoricDecisionInstanceByInstanceId(instanceId);
    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.DECISION_INSTANCE)
        .list();

    // then
    assertThat(list.size()).isEqualTo(2); // 2 properties
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(TENANT_ONE);
    }
  }

  @Test
  public void shouldLogDecisionInstancesOperationWithoutTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, "org/camunda/bpm/engine/test/api/dmn/Example.dmn");
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    engineRule.getDecisionService().evaluateDecisionByKey("decision")
      .variables(
        Variables.createVariables()
        .putValue("status", "silver")
        .putValue("sum", 723)
        ).evaluate();
    HistoricDecisionInstanceQuery historicDecisionInstanceQuery = historyService.createHistoricDecisionInstanceQuery();

    // when
    Batch batch = historyService.setRemovalTimeToHistoricDecisionInstances()
      .clearedRemovalTime()
      .byQuery(historicDecisionInstanceQuery)
      .executeAsync();
    managementService.deleteBatch(batch.getId(), true);
    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.DECISION_INSTANCE)
        .list();

    // then
    assertThat(list.size()).isEqualTo(5); // 5 properties
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(null);
    }
  }

  @Test
  public void shouldLogCaseDefinitionOperationWithTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn");
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    CaseDefinition caseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();

    // when
    repositoryService.updateCaseDefinitionHistoryTimeToLive(caseDefinition.getId(), 6);
    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_UPDATE_HISTORY_TIME_TO_LIVE)
        .entityType(EntityTypes.CASE_DEFINITION)
        .list();

    // then
    assertThat(list.size()).isEqualTo(2); // 2 properties
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(TENANT_ONE);
    }
  }

  @Test
  public void shouldLogCaseInstanceOperationWithTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn");
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    CaseDefinition caseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();
    CaseService caseService = engineRule.getCaseService();
    String caseInstanceId = caseService.withCaseDefinition(caseDefinition.getId()).create().getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId).complete();

    caseService
      .withCaseExecution(caseInstanceId)
      .close();

    // when
    historyService.deleteHistoricCaseInstance(caseInstanceId);
    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY)
        .entityType(EntityTypes.CASE_INSTANCE)
        .singleResult();

    // then
    assertThat(singleResult.getTenantId()).isEqualTo(TENANT_ONE);
  }


  @Test
  public void shouldLogMetricsOperationWithoutTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, MODEL);
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    runtimeService.startProcessInstanceByKey(PROCESS_NAME);

    // when
    managementService.deleteTaskMetrics(null);
    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_DELETE)
        .entityType(EntityTypes.TASK_METRICS)
        .singleResult();

    // then
    assertThat(singleResult.getTenantId()).isEqualTo(null);
  }


  @Test
  public void shouldLogTaskMetricsOperationWithoutTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, MODEL);
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    runtimeService.startProcessInstanceByKey(PROCESS_NAME);

    // when
    managementService.deleteMetrics(null);
    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_DELETE)
        .entityType(EntityTypes.METRICS)
        .singleResult();

    // then
    assertThat(singleResult.getTenantId()).isEqualTo(null);
  }

  @Test
  public void shouldLogFilterOperationsWithoutTenant() {
    // given
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    FilterService filterService = engineRule.getFilterService();
    Filter filter = filterService.newTaskFilter()
        .setName("name")
        .setOwner("owner")
        .setQuery(taskService.createTaskQuery())
        .setProperties(new HashMap<String, Object>());

    // when
    filterService.saveFilter(filter);
    filter.setName(filter.getName() + "_new");
    filterService.saveFilter(filter);
    filterService.deleteFilter(filter.getId());
    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.FILTER)
        .list();

    // then
    assertThat(list.size()).isEqualTo(3);
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(null);
    }
  }

  @Test
  public void shouldLogUserOperationsWithoutTenant() {
    // given
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));

    // when
    User newUser = identityService.newUser("test");
    identityService.saveUser(newUser);
    newUser.setEmail("test@mail.com");
    identityService.saveUser(newUser);
    identityService.deleteUser(newUser.getId());

    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.USER)
        .list();

    // then
    assertThat(list.size()).isEqualTo(3);
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(null);
    }
  }

  @Test
  public void shouldLogGroupOperationsWithoutTenant() {
    // given
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));

    // when
    Group newGroup = identityService.newGroup("test");
    identityService.saveGroup(newGroup);
    newGroup.setName("testName");
    identityService.saveGroup(newGroup);
    identityService.deleteGroup(newGroup.getId());

    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.GROUP)
        .list();

    // then
    assertThat(list.size()).isEqualTo(3);
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(null);
    }
  }

  @Test
  public void shouldLogTenantOperationsWithoutTenant() {
    // given
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));

    // when
    Tenant newTenant = identityService.newTenant("test");
    identityService.saveTenant(newTenant);
    newTenant.setName("testName");
    identityService.saveTenant(newTenant);
    identityService.deleteTenant(newTenant.getId());


    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.TENANT)
        .list();

    // then
    assertThat(list.size()).isEqualTo(3);
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo("test");
    }
  }

  @Test
  public void shouldLogGroupMemebershipOperationsWithoutTenant() {
    // given
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));

    // when
    identityService.saveUser(identityService.newUser("testUser"));
    identityService.saveGroup(identityService.newGroup("testGroup"));
    identityService.createMembership("testUser", "testGroup");
    identityService.deleteMembership("testUser", "testGroup");

    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.GROUP_MEMBERSHIP)
        .list();

    // then
    assertThat(list.size()).isEqualTo(4); // 2 properties per log
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(null);
    }

    // finish
    identityService.deleteUser("testUser");
    identityService.deleteGroup("testGroup");
  }

  @Test
  public void shouldLogTenantMemebershipOperationsWithoutTenant() {
    // given
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));

    // when
    identityService.saveUser(identityService.newUser("testUser"));
    identityService.saveTenant(identityService.newTenant("testTenant"));
    identityService.createTenantUserMembership("testTenant", "testUser");
    identityService.deleteTenantUserMembership("testTenant", "testUser");

    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.TENANT_MEMBERSHIP)
        .list();

    // then
    assertThat(list.size()).isEqualTo(4); // 2 properties per log
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo("testTenant");
    }

    // finish
    identityService.deleteUser("testUser");
    identityService.deleteTenant("testTenant");
  }

  @Test
  public void shouldLogAuthorizationOperationsWithoutTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, MODEL);
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));
    String processInstanceId = runtimeService.startProcessInstanceByKey(PROCESS_NAME).getId();

    // when
    AuthorizationService authorizationService = engineRule.getAuthorizationService();
    Authorization authorization =
        authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);

    authorization.setUserId("myUserId");
    authorization.setResource(Resources.HISTORIC_PROCESS_INSTANCE);


    authorization.setResourceId(processInstanceId);
    authorizationService.saveAuthorization(authorization);
    authorizationService.deleteAuthorization(authorization.getId());

    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.AUTHORIZATION)
        .list();

    // then
    assertThat(list.size()).isEqualTo(12); // 6 properties per log
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(null);
    }
  }

  @Test
  public void shouldLogPropertyOperationsWithoutTenant() {
    // given
    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));

    // when
    managementService.setProperty("testProperty", "testValue");
    managementService.deleteProperty("testProperty");

    List<UserOperationLogEntry> list = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.PROPERTY)
        .list();

    // then
    assertThat(list.size()).isEqualTo(2);
    for (UserOperationLogEntry userOperationLogEntry : list) {
      assertThat(userOperationLogEntry.getTenantId()).isEqualTo(null);
    }
  }
}
