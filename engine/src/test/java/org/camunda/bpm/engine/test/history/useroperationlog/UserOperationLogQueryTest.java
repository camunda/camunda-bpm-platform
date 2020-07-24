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
package org.camunda.bpm.engine.test.history.useroperationlog;

import static org.camunda.bpm.engine.EntityTypes.JOB;
import static org.camunda.bpm.engine.EntityTypes.JOB_DEFINITION;
import static org.camunda.bpm.engine.EntityTypes.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.EntityTypes.PROCESS_INSTANCE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.ENTITY_TYPE_ATTACHMENT;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.ENTITY_TYPE_IDENTITY_LINK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.ENTITY_TYPE_TASK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ACTIVATE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ACTIVATE_JOB;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ACTIVATE_JOB_DEFINITION;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ADD_ATTACHMENT;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ADD_GROUP_LINK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ADD_USER_LINK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_CREATE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE_ATTACHMENT;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE_GROUP_LINK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE_USER_LINK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SET_PRIORITY;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SUSPEND;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SUSPEND_JOB;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SUSPEND_JOB_DEFINITION;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_UPDATE;
import static org.camunda.bpm.engine.impl.cmd.AbstractSetBatchStateCmd.SUSPENSION_STATE_PROPERTY;
import static org.camunda.bpm.engine.impl.cmd.AbstractSetProcessDefinitionStateCmd.INCLUDE_PROCESS_INSTANCES_PROPERTY;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.ASSIGNEE;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.OWNER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.TimerActivateJobDefinitionHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerSuspendProcessDefinitionHandler;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.After;
import org.junit.Test;
/**
 * @author Danny Gräf
 */
public class UserOperationLogQueryTest extends AbstractUserOperationLogTest {

  protected static final String ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml";
  protected static final String ONE_TASK_CASE = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";
  protected static final String ONE_EXTERNAL_TASK_PROCESS = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml";

  private ProcessInstance process;
  private Task userTask;
  private Execution execution;
  private String processTaskId;

  // normalize timestamps for databases which do not provide millisecond presision.
  private Date today = new Date((ClockUtil.getCurrentTime().getTime() / 1000) * 1000);
  private Date tomorrow = new Date(((ClockUtil.getCurrentTime().getTime() + 86400000) / 1000) * 1000);
  private Date yesterday = new Date(((ClockUtil.getCurrentTime().getTime() - 86400000) / 1000) * 1000);

  @After
  public void tearDown() throws Exception {
    if (userTask != null) {
      historyService.deleteHistoricTaskInstance(userTask.getId());
    }
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQuery() {
    createLogEntries();

    // expect: all entries can be fetched
    assertEquals(18, query().count());

    // entity type
    assertEquals(11, query().entityType(EntityTypes.TASK).count());
    assertEquals(4, query().entityType(EntityTypes.IDENTITY_LINK).count());
    assertEquals(2, query().entityType(EntityTypes.ATTACHMENT).count());
    assertEquals(1, query().entityType(EntityTypes.PROCESS_INSTANCE).count());
    assertEquals(0, query().entityType("unknown entity type").count());

    // operation type
    assertEquals(2, query().operationType(OPERATION_TYPE_CREATE).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_SET_PRIORITY).count());
    assertEquals(4, query().operationType(OPERATION_TYPE_UPDATE).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_ADD_USER_LINK).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_DELETE_USER_LINK).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_ADD_GROUP_LINK).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_DELETE_GROUP_LINK).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_ADD_ATTACHMENT).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_DELETE_ATTACHMENT).count());
    
    // category
    assertEquals(17, query().categoryIn(UserOperationLogEntry.CATEGORY_TASK_WORKER).count());
    assertEquals(1, query().categoryIn(UserOperationLogEntry.CATEGORY_OPERATOR).count());// start process instance

    // process and execution reference
    assertEquals(12, query().processDefinitionId(process.getProcessDefinitionId()).count());
    assertEquals(12, query().processInstanceId(process.getId()).count());
    assertEquals(11, query().executionId(execution.getId()).count());

    // task reference
    assertEquals(11, query().taskId(processTaskId).count());
    assertEquals(6, query().taskId(userTask.getId()).count());

    // user reference
    assertEquals(11, query().userId("icke").count()); // not includes the create operation called by the process
    assertEquals(6, query().userId("er").count());

    // operation ID
    UserOperationLogQuery updates = query().operationType(OPERATION_TYPE_UPDATE);
    String updateOperationId = updates.list().get(0).getOperationId();
    assertEquals(updates.count(), query().operationId(updateOperationId).count());

    // changed properties
    assertEquals(3, query().property(ASSIGNEE).count());
    assertEquals(2, query().property(OWNER).count());

    // ascending order results by time
    List<UserOperationLogEntry> ascLog = query().orderByTimestamp().asc().list();
    for (int i = 0; i < 5; i++) {
      assertTrue(yesterday.getTime()<=ascLog.get(i).getTimestamp().getTime());
    }
    for (int i = 5; i < 13; i++) {
      assertTrue(today.getTime()<=ascLog.get(i).getTimestamp().getTime());
    }
    for (int i = 13; i < 18; i++) {
      assertTrue(tomorrow.getTime()<=ascLog.get(i).getTimestamp().getTime());
    }

    // descending order results by time
    List<UserOperationLogEntry> descLog = query().orderByTimestamp().desc().list();
    for (int i = 0; i < 4; i++) {
      assertTrue(tomorrow.getTime()<=descLog.get(i).getTimestamp().getTime());
    }
    for (int i = 4; i < 11; i++) {
      assertTrue(today.getTime()<=descLog.get(i).getTimestamp().getTime());
    }
    for (int i = 11; i < 18; i++) {
      assertTrue(yesterday.getTime()<=descLog.get(i).getTimestamp().getTime());
    }

    // filter by time, created yesterday
    assertEquals(5, query().beforeTimestamp(today).count());
    // filter by time, created today and before
    assertEquals(13, query().beforeTimestamp(tomorrow).count());
    // filter by time, created today and later
    assertEquals(13, query().afterTimestamp(yesterday).count());
    // filter by time, created tomorrow
    assertEquals(5, query().afterTimestamp(today).count());
    assertEquals(0, query().afterTimestamp(today).beforeTimestamp(yesterday).count());
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryWithBackwardCompatibility() {
    createLogEntries();

    // expect: all entries can be fetched
    assertEquals(18, query().count());

    // entity type
    assertEquals(11, query().entityType(ENTITY_TYPE_TASK).count());
    assertEquals(4, query().entityType(ENTITY_TYPE_IDENTITY_LINK).count());
    assertEquals(2, query().entityType(ENTITY_TYPE_ATTACHMENT).count());
    assertEquals(0, query().entityType("unknown entity type").count());
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryProcessInstanceOperationsById() {
    // given
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.suspendProcessInstanceById(process.getId());
    runtimeService.activateProcessInstanceById(process.getId());

    runtimeService.deleteProcessInstance(process.getId(), "a delete reason");

    // then
    assertEquals(4, query().entityType(PROCESS_INSTANCE).count());

    UserOperationLogEntry deleteEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processInstanceId(process.getId())
        .operationType(OPERATION_TYPE_DELETE)
        .singleResult();

    assertNotNull(deleteEntry);
    assertEquals(process.getId(), deleteEntry.getProcessInstanceId());
    assertNotNull(deleteEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", deleteEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, deleteEntry.getDeploymentId());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, deleteEntry.getCategory());

    UserOperationLogEntry suspendEntry = query()
      .entityType(PROCESS_INSTANCE)
      .processInstanceId(process.getId())
      .operationType(OPERATION_TYPE_SUSPEND)
      .singleResult();

    assertNotNull(suspendEntry);
    assertEquals(process.getId(), suspendEntry.getProcessInstanceId());
    assertNotNull(suspendEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", suspendEntry.getProcessDefinitionKey());

    assertEquals("suspensionState", suspendEntry.getProperty());
    assertEquals("suspended", suspendEntry.getNewValue());
    assertNull(suspendEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, suspendEntry.getCategory());

    UserOperationLogEntry activateEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processInstanceId(process.getId())
        .operationType(OPERATION_TYPE_ACTIVATE)
        .singleResult();

    assertNotNull(activateEntry);
    assertEquals(process.getId(), activateEntry.getProcessInstanceId());
    assertNotNull(activateEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", activateEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, activateEntry.getDeploymentId());

    assertEquals("suspensionState", activateEntry.getProperty());
    assertEquals("active", activateEntry.getNewValue());
    assertNull(activateEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, activateEntry.getCategory());
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryProcessInstanceOperationsByProcessDefinitionId() {
    // given
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.suspendProcessInstanceByProcessDefinitionId(process.getProcessDefinitionId());
    runtimeService.activateProcessInstanceByProcessDefinitionId(process.getProcessDefinitionId());

    // then
    assertEquals(3, query().entityType(PROCESS_INSTANCE).count());

    UserOperationLogEntry suspendEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processDefinitionId(process.getProcessDefinitionId())
        .operationType(OPERATION_TYPE_SUSPEND)
        .singleResult();

    assertNotNull(suspendEntry);
    assertEquals(process.getProcessDefinitionId(), suspendEntry.getProcessDefinitionId());
    assertNull(suspendEntry.getProcessInstanceId());
    assertEquals("oneTaskProcess", suspendEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, suspendEntry.getDeploymentId());

    assertEquals("suspensionState", suspendEntry.getProperty());
    assertEquals("suspended", suspendEntry.getNewValue());
    assertNull(suspendEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, suspendEntry.getCategory());

    UserOperationLogEntry activateEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processDefinitionId(process.getProcessDefinitionId())
        .operationType(OPERATION_TYPE_ACTIVATE)
        .singleResult();

    assertNotNull(activateEntry);
    assertNull(activateEntry.getProcessInstanceId());
    assertEquals("oneTaskProcess", activateEntry.getProcessDefinitionKey());
    assertEquals(process.getProcessDefinitionId(), activateEntry.getProcessDefinitionId());
    assertEquals(deploymentId, activateEntry.getDeploymentId());

    assertEquals("suspensionState", activateEntry.getProperty());
    assertEquals("active", activateEntry.getNewValue());
    assertNull(activateEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, activateEntry.getCategory());
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryProcessInstanceOperationsByProcessDefinitionKey() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.suspendProcessInstanceByProcessDefinitionKey("oneTaskProcess");
    runtimeService.activateProcessInstanceByProcessDefinitionKey("oneTaskProcess");

    // then
    assertEquals(3, query().entityType(PROCESS_INSTANCE).count());

    UserOperationLogEntry suspendEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processDefinitionKey("oneTaskProcess")
        .operationType(OPERATION_TYPE_SUSPEND)
        .singleResult();

    assertNotNull(suspendEntry);
    assertNull(suspendEntry.getProcessInstanceId());
    assertNull(suspendEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", suspendEntry.getProcessDefinitionKey());
    assertNull(suspendEntry.getDeploymentId());

    assertEquals("suspensionState", suspendEntry.getProperty());
    assertEquals("suspended", suspendEntry.getNewValue());
    assertNull(suspendEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, suspendEntry.getCategory());

    UserOperationLogEntry activateEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processDefinitionKey("oneTaskProcess")
        .operationType(OPERATION_TYPE_ACTIVATE)
        .singleResult();

    assertNotNull(activateEntry);
    assertNull(activateEntry.getProcessInstanceId());
    assertNull(activateEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", activateEntry.getProcessDefinitionKey());
    assertNull(activateEntry.getDeploymentId());

    assertEquals("suspensionState", activateEntry.getProperty());
    assertEquals("active", activateEntry.getNewValue());
    assertNull(activateEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, activateEntry.getCategory());
  }

  /**
   * CAM-1930: add assertions for additional op log entries here
   */
  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryProcessDefinitionOperationsById() {
    // given
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    repositoryService.suspendProcessDefinitionById(process.getProcessDefinitionId(), true, null);
    repositoryService.activateProcessDefinitionById(process.getProcessDefinitionId(), true, null);

    // then
    assertEquals(2, query().entityType(PROCESS_DEFINITION).property(SUSPENSION_STATE_PROPERTY).count());

    // Process Definition Suspension
    UserOperationLogEntry suspendDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionId(process.getProcessDefinitionId())
      .operationType(OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION)
      .property(SUSPENSION_STATE_PROPERTY)
      .singleResult();

    assertNotNull(suspendDefinitionEntry);
    assertEquals(process.getProcessDefinitionId(), suspendDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", suspendDefinitionEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, suspendDefinitionEntry.getDeploymentId());

    assertEquals(SUSPENSION_STATE_PROPERTY, suspendDefinitionEntry.getProperty());
    assertEquals("suspended", suspendDefinitionEntry.getNewValue());
    assertNull(suspendDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, suspendDefinitionEntry.getCategory());

    UserOperationLogEntry activateDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionId(process.getProcessDefinitionId())
      .operationType(OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION)
      .property(SUSPENSION_STATE_PROPERTY)
      .singleResult();

    assertNotNull(activateDefinitionEntry);
    assertEquals(process.getProcessDefinitionId(), activateDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", activateDefinitionEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, activateDefinitionEntry.getDeploymentId());

    assertEquals(SUSPENSION_STATE_PROPERTY, activateDefinitionEntry.getProperty());
    assertEquals("active", activateDefinitionEntry.getNewValue());
    assertNull(activateDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, activateDefinitionEntry.getCategory());

  }

  /**
   * CAM-1930: add assertions for additional op log entries here
   */
  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryProcessDefinitionOperationsByKey() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    repositoryService.suspendProcessDefinitionByKey("oneTaskProcess", true, null);
    repositoryService.activateProcessDefinitionByKey("oneTaskProcess", true, null);

    // then
    assertEquals(2, query().entityType(PROCESS_DEFINITION).property(SUSPENSION_STATE_PROPERTY).count());

    UserOperationLogEntry suspendDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionKey("oneTaskProcess")
      .operationType(OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION)
      .property(SUSPENSION_STATE_PROPERTY)
      .singleResult();

    assertNotNull(suspendDefinitionEntry);
    assertNull(suspendDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", suspendDefinitionEntry.getProcessDefinitionKey());
    assertNull(suspendDefinitionEntry.getDeploymentId());

    assertEquals(SUSPENSION_STATE_PROPERTY, suspendDefinitionEntry.getProperty());
    assertEquals("suspended", suspendDefinitionEntry.getNewValue());
    assertNull(suspendDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, suspendDefinitionEntry.getCategory());

    UserOperationLogEntry activateDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionKey("oneTaskProcess")
      .operationType(OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION)
      .property(SUSPENSION_STATE_PROPERTY)
      .singleResult();

    assertNotNull(activateDefinitionEntry);
    assertNull(activateDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", activateDefinitionEntry.getProcessDefinitionKey());
    assertNull(activateDefinitionEntry.getDeploymentId());

    assertEquals(SUSPENSION_STATE_PROPERTY, activateDefinitionEntry.getProperty());
    assertEquals("active", activateDefinitionEntry.getNewValue());
    assertNull(activateDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, activateDefinitionEntry.getCategory());
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryProcessDefinitionOperationsById_createsTwoLogEntries() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    repositoryService.suspendProcessDefinitionById(process.getProcessDefinitionId(), true, null);
    repositoryService.activateProcessDefinitionById(process.getProcessDefinitionId(), true, null);

    // then
    assertEquals(2, query().entityType(PROCESS_DEFINITION).operationType(OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION).count());
    assertEquals(2, query().entityType(PROCESS_DEFINITION).operationType(OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION).count());

    // Process Definition Suspension
    Set<String> actualProperties = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionId(process.getProcessDefinitionId())
      .operationType(OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION)
      .list()
      .stream()
      .map(UserOperationLogEntry::getProperty)
      .collect(Collectors.toSet());

    assertEquals(
      actualProperties,
      new HashSet<>(Arrays.asList(INCLUDE_PROCESS_INSTANCES_PROPERTY, SUSPENSION_STATE_PROPERTY))
    );

    // Process Definition activation
    actualProperties = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionId(process.getProcessDefinitionId())
      .operationType(OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION)
      .list()
      .stream()
      .map(UserOperationLogEntry::getProperty)
      .collect(Collectors.toSet());

    assertEquals(
      actualProperties,
      new HashSet<>(Arrays.asList(INCLUDE_PROCESS_INSTANCES_PROPERTY, SUSPENSION_STATE_PROPERTY))
    );
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryProcessDefinitionOperationsById_includeProcessInstancesEntries() {
    // given
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    repositoryService.suspendProcessDefinitionById(process.getProcessDefinitionId(), true, null);
    repositoryService.activateProcessDefinitionById(process.getProcessDefinitionId(), true, null);

    // then
    assertEquals(2, query().entityType(PROCESS_DEFINITION).property(INCLUDE_PROCESS_INSTANCES_PROPERTY).count());

    // Process Definition Suspension
    UserOperationLogEntry suspendDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionId(process.getProcessDefinitionId())
      .operationType(OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION)
      .property(INCLUDE_PROCESS_INSTANCES_PROPERTY)
      .singleResult();

    assertNotNull(suspendDefinitionEntry);
    assertEquals(process.getProcessDefinitionId(), suspendDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", suspendDefinitionEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, suspendDefinitionEntry.getDeploymentId());

    assertEquals(INCLUDE_PROCESS_INSTANCES_PROPERTY, suspendDefinitionEntry.getProperty());
    assertEquals("true", suspendDefinitionEntry.getNewValue());
    assertNull(suspendDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, suspendDefinitionEntry.getCategory());

    // Process Definition Activation
    UserOperationLogEntry activeDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionId(process.getProcessDefinitionId())
      .operationType(OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION)
      .property(INCLUDE_PROCESS_INSTANCES_PROPERTY)
      .singleResult();

    assertNotNull(activeDefinitionEntry);
    assertEquals(process.getProcessDefinitionId(), activeDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", activeDefinitionEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, activeDefinitionEntry.getDeploymentId());

    assertEquals(INCLUDE_PROCESS_INSTANCES_PROPERTY, activeDefinitionEntry.getProperty());
    assertEquals("true", activeDefinitionEntry.getNewValue());
    assertNull(activeDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, activeDefinitionEntry.getCategory());
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryProcessDefinitionOperationsById_excludeProcessInstancesEntries() {
    // given
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    repositoryService.suspendProcessDefinitionById(process.getProcessDefinitionId(), false, null);
    repositoryService.activateProcessDefinitionById(process.getProcessDefinitionId(), true, null);

    // then
    assertEquals(2, query().entityType(PROCESS_DEFINITION).property(INCLUDE_PROCESS_INSTANCES_PROPERTY).count());

    // Process Definition Suspension
    UserOperationLogEntry suspendDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionId(process.getProcessDefinitionId())
      .operationType(OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION)
      .property(INCLUDE_PROCESS_INSTANCES_PROPERTY)
      .singleResult();

    assertNotNull(suspendDefinitionEntry);
    assertEquals(process.getProcessDefinitionId(), suspendDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", suspendDefinitionEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, suspendDefinitionEntry.getDeploymentId());

    assertEquals(INCLUDE_PROCESS_INSTANCES_PROPERTY, suspendDefinitionEntry.getProperty());
    assertEquals("false", suspendDefinitionEntry.getNewValue());
    assertNull(suspendDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, suspendDefinitionEntry.getCategory());

    // Process Definition Activation
    UserOperationLogEntry activeDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionId(process.getProcessDefinitionId())
      .operationType(OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION)
      .property(INCLUDE_PROCESS_INSTANCES_PROPERTY)
      .singleResult();

    assertNotNull(activeDefinitionEntry);
    assertEquals(process.getProcessDefinitionId(), activeDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", activeDefinitionEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, activeDefinitionEntry.getDeploymentId());

    assertEquals(INCLUDE_PROCESS_INSTANCES_PROPERTY, activeDefinitionEntry.getProperty());
    assertEquals("true", activeDefinitionEntry.getNewValue());
    assertNull(activeDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, activeDefinitionEntry.getCategory());
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryProcessDefinitionOperationsByKey_createsTwoLogEntries() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    repositoryService.suspendProcessDefinitionByKey("oneTaskProcess", true, null);
    repositoryService.activateProcessDefinitionByKey("oneTaskProcess", true, null);

    // then
    assertEquals(2, query().entityType(PROCESS_DEFINITION).operationType(OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION).count());
    assertEquals(2, query().entityType(PROCESS_DEFINITION).operationType(OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION).count());

    // Process Definition Suspension
    Set<String> actualProperties = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionKey("oneTaskProcess")
      .operationType(OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION)
      .list()
      .stream()
      .map(UserOperationLogEntry::getProperty)
      .collect(Collectors.toSet());

    assertEquals(
      actualProperties,
      new HashSet<>(Arrays.asList(INCLUDE_PROCESS_INSTANCES_PROPERTY, SUSPENSION_STATE_PROPERTY))
    );

    // Process Definition activation
    actualProperties = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionKey("oneTaskProcess")
      .operationType(OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION)
      .list()
      .stream()
      .map(UserOperationLogEntry::getProperty)
      .collect(Collectors.toSet());

    assertEquals(
      actualProperties,
      new HashSet<>(Arrays.asList(INCLUDE_PROCESS_INSTANCES_PROPERTY, SUSPENSION_STATE_PROPERTY))
    );
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryProcessDefinitionOperationsByKey_includeProcessInstancesEntries() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    repositoryService.suspendProcessDefinitionByKey("oneTaskProcess", true, null);
    repositoryService.activateProcessDefinitionByKey("oneTaskProcess", true, null);

    // then
    assertEquals(2, query().entityType(PROCESS_DEFINITION).property(INCLUDE_PROCESS_INSTANCES_PROPERTY).count());

    // Process Definition Suspension
    UserOperationLogEntry suspendDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionKey("oneTaskProcess")
      .operationType(OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION)
      .property(INCLUDE_PROCESS_INSTANCES_PROPERTY)
      .singleResult();

    assertNotNull(suspendDefinitionEntry);
    assertNull(suspendDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", suspendDefinitionEntry.getProcessDefinitionKey());
    assertNull(suspendDefinitionEntry.getDeploymentId());

    assertEquals(INCLUDE_PROCESS_INSTANCES_PROPERTY, suspendDefinitionEntry.getProperty());
    assertEquals("true", suspendDefinitionEntry.getNewValue());
    assertNull(suspendDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, suspendDefinitionEntry.getCategory());

    // Process Definition Activation
    UserOperationLogEntry activeDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionKey("oneTaskProcess")
      .operationType(OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION)
      .property(INCLUDE_PROCESS_INSTANCES_PROPERTY)
      .singleResult();

    assertNotNull(activeDefinitionEntry);
    assertNull(activeDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", activeDefinitionEntry.getProcessDefinitionKey());
    assertNull(activeDefinitionEntry.getDeploymentId());

    assertEquals(INCLUDE_PROCESS_INSTANCES_PROPERTY, activeDefinitionEntry.getProperty());
    assertEquals("true", activeDefinitionEntry.getNewValue());
    assertNull(activeDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, activeDefinitionEntry.getCategory());
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryProcessDefinitionOperationsByKey_excludeProcessInstancesEntries() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    repositoryService.suspendProcessDefinitionByKey("oneTaskProcess", false, null);
    repositoryService.activateProcessDefinitionByKey("oneTaskProcess", false, null);

    // then
    assertEquals(2, query().entityType(PROCESS_DEFINITION).property(INCLUDE_PROCESS_INSTANCES_PROPERTY).count());

    // Process Definition Suspension
    UserOperationLogEntry suspendDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionKey("oneTaskProcess")
      .operationType(OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION)
      .property(INCLUDE_PROCESS_INSTANCES_PROPERTY)
      .singleResult();

    assertNotNull(suspendDefinitionEntry);
    assertNull(suspendDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", suspendDefinitionEntry.getProcessDefinitionKey());
    assertNull(suspendDefinitionEntry.getDeploymentId());

    assertEquals(INCLUDE_PROCESS_INSTANCES_PROPERTY, suspendDefinitionEntry.getProperty());
    assertEquals("false", suspendDefinitionEntry.getNewValue());
    assertNull(suspendDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, suspendDefinitionEntry.getCategory());

    // Process Definition Activation
    UserOperationLogEntry activeDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionKey("oneTaskProcess")
      .operationType(OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION)
      .property(INCLUDE_PROCESS_INSTANCES_PROPERTY)
      .singleResult();

    assertNotNull(activeDefinitionEntry);
    assertNull(activeDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", activeDefinitionEntry.getProcessDefinitionKey());
    assertNull(activeDefinitionEntry.getDeploymentId());

    assertEquals(INCLUDE_PROCESS_INSTANCES_PROPERTY, activeDefinitionEntry.getProperty());
    assertEquals("false", activeDefinitionEntry.getNewValue());
    assertNull(activeDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, activeDefinitionEntry.getCategory());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  @Test
  public void testQueryJobOperations() {
    // given
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    process = runtimeService.startProcessInstanceByKey("process");

    // when
    managementService.suspendJobDefinitionByProcessDefinitionId(process.getProcessDefinitionId());
    managementService.activateJobDefinitionByProcessDefinitionId(process.getProcessDefinitionId());
    managementService.suspendJobByProcessInstanceId(process.getId());
    managementService.activateJobByProcessInstanceId(process.getId());

    // then
    assertEquals(2, query().entityType(JOB_DEFINITION).count());
    assertEquals(2, query().entityType(JOB).count());

    // active job definition
    UserOperationLogEntry activeJobDefinitionEntry = query()
      .entityType(JOB_DEFINITION)
      .processDefinitionId(process.getProcessDefinitionId())
      .operationType(OPERATION_TYPE_ACTIVATE_JOB_DEFINITION)
      .singleResult();

    assertNotNull(activeJobDefinitionEntry);
    assertEquals(process.getProcessDefinitionId(), activeJobDefinitionEntry.getProcessDefinitionId());
    assertEquals(deploymentId, activeJobDefinitionEntry.getDeploymentId());

    assertEquals("suspensionState", activeJobDefinitionEntry.getProperty());
    assertEquals("active", activeJobDefinitionEntry.getNewValue());
    assertNull(activeJobDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, activeJobDefinitionEntry.getCategory());

    // active job
    UserOperationLogEntry activateJobIdEntry = query()
      .entityType(JOB)
      .processInstanceId(process.getProcessInstanceId())
      .operationType(OPERATION_TYPE_ACTIVATE_JOB)
      .singleResult();

    assertNotNull(activateJobIdEntry);
    assertEquals(process.getProcessInstanceId(), activateJobIdEntry.getProcessInstanceId());
    assertEquals(deploymentId, activateJobIdEntry.getDeploymentId());

    assertEquals("suspensionState", activateJobIdEntry.getProperty());
    assertEquals("active", activateJobIdEntry.getNewValue());
    assertNull(activateJobIdEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, activateJobIdEntry.getCategory());

    // suspended job definition
    UserOperationLogEntry suspendJobDefinitionEntry = query()
      .entityType(JOB_DEFINITION)
      .processDefinitionId(process.getProcessDefinitionId())
      .operationType(OPERATION_TYPE_SUSPEND_JOB_DEFINITION)
      .singleResult();

    assertNotNull(suspendJobDefinitionEntry);
    assertEquals(process.getProcessDefinitionId(), suspendJobDefinitionEntry.getProcessDefinitionId());
    assertEquals(deploymentId, suspendJobDefinitionEntry.getDeploymentId());

    assertEquals("suspensionState", suspendJobDefinitionEntry.getProperty());
    assertEquals("suspended", suspendJobDefinitionEntry.getNewValue());
    assertNull(suspendJobDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, suspendJobDefinitionEntry.getCategory());

    // suspended job
    UserOperationLogEntry suspendedJobEntry = query()
      .entityType(JOB)
      .processInstanceId(process.getProcessInstanceId())
      .operationType(OPERATION_TYPE_SUSPEND_JOB)
      .singleResult();

    assertNotNull(suspendedJobEntry);
    assertEquals(process.getProcessInstanceId(), suspendedJobEntry.getProcessInstanceId());
    assertEquals(deploymentId, suspendedJobEntry.getDeploymentId());

    assertEquals("suspensionState", suspendedJobEntry.getProperty());
    assertEquals("suspended", suspendedJobEntry.getNewValue());
    assertNull(suspendedJobEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, suspendedJobEntry.getCategory());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedServiceTask.bpmn20.xml" })
  @Test
  public void testQueryJobRetryOperationsById() {
    // given
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    process = runtimeService.startProcessInstanceByKey("failedServiceTask");
    Job job = managementService.createJobQuery().processInstanceId(process.getProcessInstanceId()).singleResult();

    managementService.setJobRetries(job.getId(), 10);

    // then
    assertEquals(1, query().entityType(JOB).operationType(OPERATION_TYPE_SET_JOB_RETRIES).count());

    UserOperationLogEntry jobRetryEntry = query()
      .entityType(JOB)
      .jobId(job.getId())
      .operationType(OPERATION_TYPE_SET_JOB_RETRIES)
      .singleResult();

    assertNotNull(jobRetryEntry);
    assertEquals(job.getId(), jobRetryEntry.getJobId());

    assertEquals("3", jobRetryEntry.getOrgValue());
    assertEquals("10", jobRetryEntry.getNewValue());
    assertEquals("retries", jobRetryEntry.getProperty());
    assertEquals(job.getJobDefinitionId(), jobRetryEntry.getJobDefinitionId());
    assertEquals(job.getProcessInstanceId(), jobRetryEntry.getProcessInstanceId());
    assertEquals(job.getProcessDefinitionKey(), jobRetryEntry.getProcessDefinitionKey());
    assertEquals(job.getProcessDefinitionId(), jobRetryEntry.getProcessDefinitionId());
    assertEquals(deploymentId, jobRetryEntry.getDeploymentId());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, jobRetryEntry.getCategory());
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryJobDefinitionOperationWithDelayedJobDefinition() {
    // given
    // a running process instance
    ProcessInstance process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // with a process definition id
    String processDefinitionId = process.getProcessDefinitionId();

    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionId(processDefinitionId, true);

    // one week from now
    ClockUtil.setCurrentTime(today);
    long oneWeekFromStartTime = today.getTime() + (7 * 24 * 60 * 60 * 1000);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionId(processDefinitionId, false, new Date(oneWeekFromStartTime));

    // then
    // there is a user log entry for the activation
    Long jobDefinitionEntryCount = query()
      .entityType(JOB_DEFINITION)
      .operationType(UserOperationLogEntry.OPERATION_TYPE_ACTIVATE_JOB_DEFINITION)
      .processDefinitionId(processDefinitionId)
      .category(UserOperationLogEntry.CATEGORY_OPERATOR)
      .count();

    assertEquals(1, jobDefinitionEntryCount.longValue());

    // there exists a job for the delayed activation execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedActivationJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedActivationJob);

    // execute job
    managementService.executeJob(delayedActivationJob.getId());

    jobDefinitionEntryCount = query()
      .entityType(JOB_DEFINITION)
      .operationType(UserOperationLogEntry.OPERATION_TYPE_ACTIVATE_JOB_DEFINITION)
      .processDefinitionId(processDefinitionId)
      .category(UserOperationLogEntry.CATEGORY_OPERATOR)
      .count();

    assertEquals(1, jobDefinitionEntryCount.longValue());

    // Clean up db
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerActivateJobDefinitionHandler.TYPE);
        return null;
      }
    });
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  @Test
  public void testQueryProcessDefinitionOperationWithDelayedProcessDefinition() {
    // given
    ClockUtil.setCurrentTime(today);
    final long hourInMs = 60 * 60 * 1000;

    String key = "oneFailingServiceTaskProcess";

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey(key, params);

    // when
    // the process definition will be suspended
    repositoryService.suspendProcessDefinitionByKey(key, false, new Date(today.getTime() + (2 * hourInMs)));

    // then
    // there exists a timer job to suspend the process definition delayed
    Job timerToSuspendProcessDefinition = managementService.createJobQuery().timers().singleResult();
    assertNotNull(timerToSuspendProcessDefinition);

    // there is a user log entry for the activation
    Long processDefinitionEntryCount = query()
      .entityType(PROCESS_DEFINITION)
      .operationType(UserOperationLogEntry.OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION)
      .processDefinitionKey(key)
      .category(UserOperationLogEntry.CATEGORY_OPERATOR)
      .property(SUSPENSION_STATE_PROPERTY)
      .count();

    assertEquals(1, processDefinitionEntryCount.longValue());

    // when
    // execute job
    managementService.executeJob(timerToSuspendProcessDefinition.getId());

    // then
    // there is a user log entry for the activation
    processDefinitionEntryCount = query()
      .entityType(PROCESS_DEFINITION)
      .operationType(UserOperationLogEntry.OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION)
      .processDefinitionKey(key)
      .category(UserOperationLogEntry.CATEGORY_OPERATOR)
      .property(SUSPENSION_STATE_PROPERTY)
      .count();

    assertEquals(1, processDefinitionEntryCount.longValue());

    // clean up op log
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerSuspendProcessDefinitionHandler.TYPE);
        return null;
      }
    });
  }

  // ----- PROCESS INSTANCE MODIFICATION -----

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryProcessInstanceModificationOperation() {
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().singleResult();

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("theTask")
      .execute();

    UserOperationLogQuery logQuery = query()
      .entityType(EntityTypes.PROCESS_INSTANCE)
      .operationType(UserOperationLogEntry.OPERATION_TYPE_MODIFY_PROCESS_INSTANCE);

    assertEquals(1, logQuery.count());
    UserOperationLogEntry logEntry = logQuery.singleResult();

    assertEquals(processInstanceId, logEntry.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), logEntry.getProcessDefinitionId());
    assertEquals(definition.getKey(), logEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, logEntry.getDeploymentId());
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_MODIFY_PROCESS_INSTANCE, logEntry.getOperationType());
    assertEquals(EntityTypes.PROCESS_INSTANCE, logEntry.getEntityType());
    assertNull(logEntry.getProperty());
    assertNull(logEntry.getOrgValue());
    assertNull(logEntry.getNewValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, logEntry.getCategory());
  }

  // ----- ADD VARIABLES -----

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryAddExecutionVariableOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.setVariable(process.getId(), "testVariable1", "THIS IS TESTVARIABLE!!!");

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE, UserOperationLogEntry.CATEGORY_OPERATOR);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryAddExecutionVariablesMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.setVariables(process.getId(), createMapForVariableAddition());

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE, UserOperationLogEntry.CATEGORY_OPERATOR);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryAddExecutionVariablesSingleAndMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.setVariable(process.getId(), "testVariable3", "foo");
    runtimeService.setVariables(process.getId(), createMapForVariableAddition());
    runtimeService.setVariable(process.getId(), "testVariable4", "bar");

    // then
    verifyVariableOperationAsserts(3, UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE, UserOperationLogEntry.CATEGORY_OPERATOR);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryAddTaskVariableOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setVariable(processTaskId, "testVariable1", "THIS IS TESTVARIABLE!!!");

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE, UserOperationLogEntry.CATEGORY_TASK_WORKER);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryAddTaskVariablesMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setVariables(processTaskId, createMapForVariableAddition());

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE, UserOperationLogEntry.CATEGORY_TASK_WORKER);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryAddTaskVariablesSingleAndMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setVariable(processTaskId, "testVariable3", "foo");
    taskService.setVariables(processTaskId, createMapForVariableAddition());
    taskService.setVariable(processTaskId, "testVariable4", "bar");

    // then
    verifyVariableOperationAsserts(3, UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE, UserOperationLogEntry.CATEGORY_TASK_WORKER);
  }

  // ----- PATCH VARIABLES -----

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryPatchExecutionVariablesOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    ((RuntimeServiceImpl) runtimeService)
      .updateVariables(process.getId(), createMapForVariableAddition(), createCollectionForVariableDeletion());

    // then
   verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_MODIFY_VARIABLE, UserOperationLogEntry.CATEGORY_OPERATOR);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryPatchTaskVariablesOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    ((TaskServiceImpl) taskService)
      .updateVariablesLocal(processTaskId, createMapForVariableAddition(), createCollectionForVariableDeletion());

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_MODIFY_VARIABLE, UserOperationLogEntry.CATEGORY_TASK_WORKER);
  }

  // ----- REMOVE VARIABLES -----

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryRemoveExecutionVariableOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.removeVariable(process.getId(), "testVariable1");

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_REMOVE_VARIABLE, UserOperationLogEntry.CATEGORY_OPERATOR);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryRemoveExecutionVariablesMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.removeVariables(process.getId(), createCollectionForVariableDeletion());

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_REMOVE_VARIABLE, UserOperationLogEntry.CATEGORY_OPERATOR);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryRemoveExecutionVariablesSingleAndMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.removeVariable(process.getId(), "testVariable1");
    runtimeService.removeVariables(process.getId(), createCollectionForVariableDeletion());
    runtimeService.removeVariable(process.getId(), "testVariable2");

    // then
    verifyVariableOperationAsserts(3, UserOperationLogEntry.OPERATION_TYPE_REMOVE_VARIABLE, UserOperationLogEntry.CATEGORY_OPERATOR);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryRemoveTaskVariableOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.removeVariable(processTaskId, "testVariable1");

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_REMOVE_VARIABLE, UserOperationLogEntry.CATEGORY_TASK_WORKER);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryRemoveTaskVariablesMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.removeVariables(processTaskId, createCollectionForVariableDeletion());

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_REMOVE_VARIABLE, UserOperationLogEntry.CATEGORY_TASK_WORKER);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryRemoveTaskVariablesSingleAndMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.removeVariable(processTaskId, "testVariable3");
    taskService.removeVariables(processTaskId, createCollectionForVariableDeletion());
    taskService.removeVariable(processTaskId, "testVariable4");

    // then
    verifyVariableOperationAsserts(3, UserOperationLogEntry.OPERATION_TYPE_REMOVE_VARIABLE, UserOperationLogEntry.CATEGORY_TASK_WORKER);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryByEntityTypes() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setAssignee(processTaskId, "foo");
    taskService.setVariable(processTaskId, "foo", "bar");

    // then
    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .entityTypeIn(EntityTypes.TASK, EntityTypes.VARIABLE);

    verifyQueryResults(query, 2);
  }

  @Test
  public void testQueryByInvalidEntityTypes() {
    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .entityTypeIn("foo");

    verifyQueryResults(query, 0);

    try {
      query.entityTypeIn((String[]) null);
      fail();
    } catch (ProcessEngineException e) {
      // expected
    }

    try {
      query.entityTypeIn(EntityTypes.TASK, null, EntityTypes.VARIABLE);
      fail();
    } catch (ProcessEngineException e) {
      // expected
    }
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryByCategories() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setAssignee(processTaskId, "foo");
    taskService.setVariable(processTaskId, "foo", "bar");

    // then
    UserOperationLogQuery query = historyService
      .createUserOperationLogQuery()
      .categoryIn(UserOperationLogEntry.CATEGORY_TASK_WORKER, UserOperationLogEntry.CATEGORY_OPERATOR);

    verifyQueryResults(query, 3);

    // and
    query = historyService
        .createUserOperationLogQuery()
        .categoryIn(UserOperationLogEntry.CATEGORY_TASK_WORKER);

    verifyQueryResults(query, 2);

    // and
    query = historyService
      .createUserOperationLogQuery()
      .category(UserOperationLogEntry.CATEGORY_OPERATOR);

    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidCategories() {
    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .categoryIn("foo");

    verifyQueryResults(query, 0);

    query = historyService
        .createUserOperationLogQuery()
        .category("foo");

    verifyQueryResults(query, 0);

    try {
      query.category(null);
      fail();
    } catch (ProcessEngineException e) {
      // expected
    }

    try {
      query.categoryIn((String[]) null);
      fail();
    } catch (ProcessEngineException e) {
      // expected
    }

    try {
      query.categoryIn(UserOperationLogEntry.CATEGORY_ADMIN, null, UserOperationLogEntry.CATEGORY_TASK_WORKER);
      fail();
    } catch (ProcessEngineException e) {
      // expected
    }
  }

  // ----- DELETE VARIABLE HISTORY -----

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryDeleteVariableHistoryOperationOnRunningInstance() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariable(process.getId(), "testVariable", "test");
    runtimeService.setVariable(process.getId(), "testVariable", "test2");
    String variableInstanceId = historyService.createHistoricVariableInstanceQuery().singleResult().getId();

    // when
    historyService.deleteHistoricVariableInstance(variableInstanceId);

    // then
    verifyHistoricVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY);
    verifySingleVariableOperationPropertyChange("name", "testVariable", UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryDeleteVariableHistoryOperationOnHistoricInstance() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariable(process.getId(), "testVariable", "test");
    runtimeService.deleteProcessInstance(process.getId(), "none");
    String variableInstanceId = historyService.createHistoricVariableInstanceQuery().singleResult().getId();

    // when
    historyService.deleteHistoricVariableInstance(variableInstanceId);

    // then
    verifyHistoricVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY);
    verifySingleVariableOperationPropertyChange("name", "testVariable", UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryDeleteVariableHistoryOperationOnTaskOfRunningInstance() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();
    taskService.setVariable(processTaskId, "testVariable", "test");
    taskService.setVariable(processTaskId, "testVariable", "test2");
    String variableInstanceId = historyService.createHistoricVariableInstanceQuery().singleResult().getId();

    // when
    historyService.deleteHistoricVariableInstance(variableInstanceId);

    // then
    verifyHistoricVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY);
    verifySingleVariableOperationPropertyChange("name", "testVariable", UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryDeleteVariableHistoryOperationOnTaskOfHistoricInstance() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();
    taskService.setVariable(processTaskId, "testVariable", "test");
    runtimeService.deleteProcessInstance(process.getId(), "none");
    String variableInstanceId = historyService.createHistoricVariableInstanceQuery().singleResult().getId();

    // when
    historyService.deleteHistoricVariableInstance(variableInstanceId);

    // then
    verifyHistoricVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY);
    verifySingleVariableOperationPropertyChange("name", "testVariable", UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryDeleteVariableHistoryOperationOnCase() {
    // given
    CaseInstance caseInstance = caseService.createCaseInstanceByKey("oneTaskCase");
    caseService.setVariable(caseInstance.getId(), "myVariable", 1);
    caseService.setVariable(caseInstance.getId(), "myVariable", 2);
    caseService.setVariable(caseInstance.getId(), "myVariable", 3);
    HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();

    // when
    historyService.deleteHistoricVariableInstance(variableInstance.getId());

    // then
    verfiySingleCaseVariableOperationAsserts(caseInstance);
    verifySingleVariableOperationPropertyChange("name", "myVariable", UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testQueryDeleteVariableHistoryOperationOnTaskOfCase() {
    // given
    CaseInstance caseInstance = caseService.createCaseInstanceByKey("oneTaskCase");
    processTaskId = taskService.createTaskQuery().singleResult().getId();
    taskService.setVariable(processTaskId, "myVariable", "1");
    taskService.setVariable(processTaskId, "myVariable", "2");
    taskService.setVariable(processTaskId, "myVariable", "3");
    HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();

    // when
    historyService.deleteHistoricVariableInstance(variableInstance.getId());

    verfiySingleCaseVariableOperationAsserts(caseInstance);
    verifySingleVariableOperationPropertyChange("name", "myVariable", UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY);
  }

  @Test
  public void testQueryDeleteVariableHistoryOperationOnStandaloneTask() {
    // given
    Task task = taskService.newTask();
    taskService.saveTask(task);
    taskService.setVariable(task.getId(), "testVariable", "testValue");
    taskService.setVariable(task.getId(), "testVariable", "testValue2");
    HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();

    // when
    historyService.deleteHistoricVariableInstance(variableInstance.getId());

    // then
    String operationType = UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY;
    UserOperationLogQuery logQuery = query().entityType(EntityTypes.VARIABLE).operationType(operationType);
    assertEquals(1, logQuery.count());

    UserOperationLogEntry logEntry = logQuery.singleResult();
    assertEquals(task.getId(), logEntry.getTaskId());
    assertNull(logEntry.getDeploymentId());
    verifySingleVariableOperationPropertyChange("name", "testVariable", UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY);

    taskService.deleteTask(task.getId(), true);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryDeleteVariablesHistoryOperationOnRunningInstance() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariable(process.getId(), "testVariable", "test");
    runtimeService.setVariable(process.getId(), "testVariable", "test2");
    runtimeService.setVariable(process.getId(), "testVariable2", "test");
    runtimeService.setVariable(process.getId(), "testVariable2", "test2");
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().count());

    // when
    historyService.deleteHistoricVariableInstancesByProcessInstanceId(process.getId());

    // then
    verifyHistoricVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryDeleteVariablesHistoryOperationOnHistoryInstance() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariable(process.getId(), "testVariable", "test");
    runtimeService.setVariable(process.getId(), "testVariable2", "test");
    runtimeService.deleteProcessInstance(process.getId(), "none");
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().count());

    // when
    historyService.deleteHistoricVariableInstancesByProcessInstanceId(process.getId());

    // then
    verifyHistoricVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryDeleteVariableAndVariablesHistoryOperationOnRunningInstance() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariable(process.getId(), "testVariable", "test");
    runtimeService.setVariable(process.getId(), "testVariable", "test2");
    runtimeService.setVariable(process.getId(), "testVariable2", "test");
    runtimeService.setVariable(process.getId(), "testVariable2", "test2");
    runtimeService.setVariable(process.getId(), "testVariable3", "test");
    runtimeService.setVariable(process.getId(), "testVariable3", "test2");
    String variableInstanceId = historyService.createHistoricVariableInstanceQuery().variableName("testVariable").singleResult().getId();

    // when
    historyService.deleteHistoricVariableInstance(variableInstanceId);
    historyService.deleteHistoricVariableInstancesByProcessInstanceId(process.getId());

    // then
    verifyHistoricVariableOperationAsserts(2, UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  @Test
  public void testQueryDeleteVariableAndVariablesHistoryOperationOnHistoryInstance() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariable(process.getId(), "testVariable", "test");
    runtimeService.setVariable(process.getId(), "testVariable2", "test");
    runtimeService.setVariable(process.getId(), "testVariable3", "test");
    runtimeService.deleteProcessInstance(process.getId(), "none");
    String variableInstanceId = historyService.createHistoricVariableInstanceQuery().variableName("testVariable").singleResult().getId();

    // when
    historyService.deleteHistoricVariableInstance(variableInstanceId);
    historyService.deleteHistoricVariableInstancesByProcessInstanceId(process.getId());

    // then
    verifyHistoricVariableOperationAsserts(2, UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY);
  }

  // --------------- CMMN --------------------

  @Deployment(resources={ONE_TASK_CASE})
  @Test
  public void testQueryByCaseDefinitionId() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    Task task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);

    // when
    taskService.setAssignee(task.getId(), "demo");

    // then

    UserOperationLogQuery query = historyService
      .createUserOperationLogQuery()
      .caseDefinitionId(caseDefinitionId)
      .category(UserOperationLogEntry.CATEGORY_TASK_WORKER);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={ONE_TASK_CASE})
  @Test
  public void testQueryByCaseInstanceId() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    Task task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);

    // when
    taskService.setAssignee(task.getId(), "demo");

    // then

    UserOperationLogQuery query = historyService
      .createUserOperationLogQuery()
      .caseInstanceId(caseInstanceId)
      .category(UserOperationLogEntry.CATEGORY_TASK_WORKER);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={ONE_TASK_CASE})
  @Test
  public void testQueryByCaseExecutionId() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    Task task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);

    // when
    taskService.setAssignee(task.getId(), "demo");

    // then

    UserOperationLogQuery query = historyService
      .createUserOperationLogQuery()
      .caseExecutionId(caseExecutionId)
      .category(UserOperationLogEntry.CATEGORY_TASK_WORKER);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={ONE_EXTERNAL_TASK_PROCESS})
  @Test
  public void testQueryByExternalTaskId() {
    // given:
    // an active process instance
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    ExternalTask task = externalTaskService.createExternalTaskQuery().singleResult();
    assertNotNull(task);

    // when
    externalTaskService.setRetries(task.getId(), 5);

    // then
    UserOperationLogQuery query = historyService
      .createUserOperationLogQuery()
      .externalTaskId(task.getId());

    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByDeploymentId() {
    // given
    String deploymentId = repositoryService
        .createDeployment()
        .addClasspathResource(ONE_TASK_PROCESS)
        .deploy()
        .getId();

    // when
    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .deploymentId(deploymentId)
        .category(UserOperationLogEntry.CATEGORY_OPERATOR);

    // then
    verifyQueryResults(query, 1);

    repositoryService.deleteDeployment(deploymentId, true);
  }

  @Test
  public void testQueryByInvalidDeploymentId() {
    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .deploymentId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.deploymentId(null);
      fail();
    } catch (ProcessEngineException e) {
      // expected
    }
  }

  private void verifyQueryResults(UserOperationLogQuery query, int countExpected) {
    assertEquals(countExpected, query.list().size());
    assertEquals(countExpected, query.count());

    if (countExpected == 1) {
      assertNotNull(query.singleResult());
    } else if (countExpected > 1){
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertNull(query.singleResult());
    }
  }

  private void verifySingleResultFails(UserOperationLogQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {}
  }

  private Map<String, Object> createMapForVariableAddition() {
    Map<String, Object> variables =  new HashMap<String, Object>();
    variables.put("testVariable1", "THIS IS TESTVARIABLE!!!");
    variables.put("testVariable2", "OVER 9000!");

    return variables;
  }

  private Collection<String> createCollectionForVariableDeletion() {
    Collection<String> variables = new ArrayList<String>();
    variables.add("testVariable3");
    variables.add("testVariable4");

    return variables;
  }

  private void verifyVariableOperationAsserts(int countAssertValue, String operationType, String category) {
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    UserOperationLogQuery logQuery = query().entityType(EntityTypes.VARIABLE).operationType(operationType);
    assertEquals(countAssertValue, logQuery.count());

    if(countAssertValue > 1) {
      List<UserOperationLogEntry> logEntryList = logQuery.list();

      for (UserOperationLogEntry logEntry : logEntryList) {
        assertEquals(process.getProcessDefinitionId(), logEntry.getProcessDefinitionId());
        assertEquals(process.getProcessInstanceId(), logEntry.getProcessInstanceId());
        assertEquals(deploymentId, logEntry.getDeploymentId());
        assertEquals(category, logEntry.getCategory());
      }
    } else {
      UserOperationLogEntry logEntry = logQuery.singleResult();
      assertEquals(process.getProcessDefinitionId(), logEntry.getProcessDefinitionId());
      assertEquals(process.getProcessInstanceId(), logEntry.getProcessInstanceId());
      assertEquals(deploymentId, logEntry.getDeploymentId());
      assertEquals(category, logEntry.getCategory());
    }
  }

  private void verifyHistoricVariableOperationAsserts(int countAssertValue, String operationType) {
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    UserOperationLogQuery logQuery = query().entityType(EntityTypes.VARIABLE).operationType(operationType);
    assertEquals(countAssertValue, logQuery.count());

    if(countAssertValue > 1) {
      List<UserOperationLogEntry> logEntryList = logQuery.list();

      for (UserOperationLogEntry logEntry : logEntryList) {
        assertEquals(process.getProcessDefinitionId(), logEntry.getProcessDefinitionId());
        assertEquals(process.getProcessInstanceId(), logEntry.getProcessInstanceId());
        assertEquals(deploymentId, logEntry.getDeploymentId());
        assertNull(logEntry.getTaskId());
        assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, logEntry.getCategory());
      }
    } else {
      UserOperationLogEntry logEntry = logQuery.singleResult();
      assertEquals(process.getProcessDefinitionId(), logEntry.getProcessDefinitionId());
      assertEquals(process.getProcessInstanceId(), logEntry.getProcessInstanceId());
      assertEquals(deploymentId, logEntry.getDeploymentId());
      assertNull(logEntry.getTaskId());
      assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, logEntry.getCategory());
    }
  }

  private void verifySingleVariableOperationPropertyChange(String property, String newValue, String operationType) {
    UserOperationLogQuery logQuery = query().entityType(EntityTypes.VARIABLE).operationType(operationType);
    assertEquals(1, logQuery.count());
    UserOperationLogEntry logEntry = logQuery.singleResult();
    assertEquals(property, logEntry.getProperty());
    assertEquals(newValue, logEntry.getNewValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, logEntry.getCategory());
  }

  private void verfiySingleCaseVariableOperationAsserts(CaseInstance caseInstance) {
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    String operationType = UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY;
    UserOperationLogQuery logQuery = query().entityType(EntityTypes.VARIABLE).operationType(operationType);
    assertEquals(1, logQuery.count());

    UserOperationLogEntry logEntry = logQuery.singleResult();
    assertEquals(caseInstance.getCaseDefinitionId(), logEntry.getCaseDefinitionId());
    assertEquals(caseInstance.getCaseInstanceId(), logEntry.getCaseInstanceId());
    assertEquals(deploymentId, logEntry.getDeploymentId());
    assertNull(logEntry.getTaskId());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, logEntry.getCategory());
  }

  private UserOperationLogQuery query() {
    return historyService.createUserOperationLogQuery();
  }

  /**
   * start process and operate on userTask to create some log entries for the query tests
   */
  private void createLogEntries() {
    ClockUtil.setCurrentTime(yesterday);

    // create a process with a userTask and work with it
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    execution = processEngine.getRuntimeService().createExecutionQuery().processInstanceId(process.getId()).singleResult();
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // user "icke" works on the process userTask
    identityService.setAuthenticatedUserId("icke");

    // create and remove some links
    taskService.addCandidateUser(processTaskId, "er");
    taskService.deleteCandidateUser(processTaskId, "er");
    taskService.addCandidateGroup(processTaskId, "wir");
    taskService.deleteCandidateGroup(processTaskId, "wir");

    // assign and reassign the userTask
    ClockUtil.setCurrentTime(today);
    taskService.setOwner(processTaskId, "icke");
    taskService.claim(processTaskId, "icke");
    taskService.setAssignee(processTaskId, "er");

    // change priority of task
    taskService.setPriority(processTaskId, 10);

    // add and delete an attachment
    Attachment attachment = taskService.createAttachment("image/ico", processTaskId, process.getId(), "favicon.ico", "favicon", "http://camunda.com/favicon.ico");
    taskService.deleteAttachment(attachment.getId());

    // complete the userTask to finish the process
    taskService.complete(processTaskId);
    testRule.assertProcessEnded(process.getId());

    // user "er" works on the process userTask
    identityService.setAuthenticatedUserId("er");

    // create a standalone userTask
    userTask = taskService.newTask();
    userTask.setName("to do");
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
  }

}
