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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateUtils;
import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class UserOperationLogJobTest extends AbstractUserOperationLogTest {

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/asyncTaskProcess.bpmn20.xml"})
  @Test
  public void testSetJobPriority() {
    // given a job
    runtimeService.startProcessInstanceByKey("asyncTaskProcess");
    Job job = managementService.createJobQuery().singleResult();

    // when I set a job priority
    managementService.setJobPriority(job.getId(), 42);

    // then an op log entry is written
    UserOperationLogEntry userOperationLogEntry = historyService
            .createUserOperationLogQuery()
            .operationType(UserOperationLogEntry.OPERATION_TYPE_SET_PRIORITY)
            .singleResult();
    assertNotNull(userOperationLogEntry);

    assertEquals(EntityTypes.JOB, userOperationLogEntry.getEntityType());
    assertEquals(job.getId(), userOperationLogEntry.getJobId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_SET_PRIORITY,
        userOperationLogEntry.getOperationType());

    assertEquals("priority", userOperationLogEntry.getProperty());
    assertEquals("42", userOperationLogEntry.getNewValue());
    assertEquals("0", userOperationLogEntry.getOrgValue());

    assertEquals(USER_ID, userOperationLogEntry.getUserId());

    assertEquals(job.getJobDefinitionId(), userOperationLogEntry.getJobDefinitionId());
    assertEquals(job.getProcessInstanceId(), userOperationLogEntry.getProcessInstanceId());
    assertEquals(job.getProcessDefinitionId(), userOperationLogEntry.getProcessDefinitionId());
    assertEquals(job.getProcessDefinitionKey(), userOperationLogEntry.getProcessDefinitionKey());
    assertEquals(job.getDeploymentId(), userOperationLogEntry.getDeploymentId());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, userOperationLogEntry.getCategory());
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/asyncTaskProcess.bpmn20.xml"})
  @Test
  public void testSetRetries() {
    // given a job
    runtimeService.startProcessInstanceByKey("asyncTaskProcess");
    Job job = managementService.createJobQuery().singleResult();

    // when I set the job retries
    managementService.setJobRetries(job.getId(), 4);

    // then an op log entry is written
    UserOperationLogEntry userOperationLogEntry = historyService
            .createUserOperationLogQuery()
            .operationType(UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES)
            .singleResult();
    assertNotNull(userOperationLogEntry);

    assertEquals(EntityTypes.JOB, userOperationLogEntry.getEntityType());
    assertEquals(job.getId(), userOperationLogEntry.getJobId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES,
        userOperationLogEntry.getOperationType());

    assertEquals("retries", userOperationLogEntry.getProperty());
    assertEquals("4", userOperationLogEntry.getNewValue());
    assertEquals("3", userOperationLogEntry.getOrgValue());

    assertEquals(USER_ID, userOperationLogEntry.getUserId());

    assertEquals(job.getJobDefinitionId(), userOperationLogEntry.getJobDefinitionId());
    assertEquals(job.getProcessInstanceId(), userOperationLogEntry.getProcessInstanceId());
    assertEquals(job.getProcessDefinitionId(), userOperationLogEntry.getProcessDefinitionId());
    assertEquals(job.getProcessDefinitionKey(), userOperationLogEntry.getProcessDefinitionKey());
    assertEquals(job.getDeploymentId(), userOperationLogEntry.getDeploymentId());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, userOperationLogEntry.getCategory());
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/asyncTaskProcess.bpmn20.xml"})
  @Test
  public void testSetRetriesByJobDefinitionId() {
    // given a job
    runtimeService.startProcessInstanceByKey("asyncTaskProcess");
    Job job = managementService.createJobQuery().singleResult();

    // when I set the job retries
    managementService.setJobRetriesByJobDefinitionId(job.getJobDefinitionId(), 4);

    // then an op log entry is written
    UserOperationLogEntry userOperationLogEntry = historyService
            .createUserOperationLogQuery()
            .operationType(UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES)
            .singleResult();
    assertNotNull(userOperationLogEntry);

    assertEquals(EntityTypes.JOB, userOperationLogEntry.getEntityType());
    assertNull(userOperationLogEntry.getJobId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES,
        userOperationLogEntry.getOperationType());

    assertEquals("retries", userOperationLogEntry.getProperty());
    assertEquals("4", userOperationLogEntry.getNewValue());
    assertNull(userOperationLogEntry.getOrgValue());

    assertEquals(USER_ID, userOperationLogEntry.getUserId());

    assertEquals(job.getJobDefinitionId(), userOperationLogEntry.getJobDefinitionId());
    assertNull(job.getProcessInstanceId(), userOperationLogEntry.getProcessInstanceId());
    assertEquals(job.getProcessDefinitionId(), userOperationLogEntry.getProcessDefinitionId());
    assertEquals(job.getProcessDefinitionKey(), userOperationLogEntry.getProcessDefinitionKey());
    assertEquals(job.getDeploymentId(), userOperationLogEntry.getDeploymentId());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, userOperationLogEntry.getCategory());
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/asyncTaskProcess.bpmn20.xml"})
  @Test
  public void testSetRetriesAsync() {
    // given a job
    runtimeService.startProcessInstanceByKey("asyncTaskProcess");
    Job job = managementService.createJobQuery().singleResult();

    // when I set the job retries
    Batch batch = managementService.setJobRetriesAsync(Arrays.asList(job.getId()), 4);

    // then three op log entries are written
    UserOperationLogQuery query = historyService
            .createUserOperationLogQuery()
            .operationType(UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES);
    assertEquals(3, query.count());

    // check 'retries' entry
    UserOperationLogEntry userOperationLogEntry = query.property("retries").singleResult();
    assertEquals(EntityTypes.JOB, userOperationLogEntry.getEntityType());
    assertNull(userOperationLogEntry.getJobId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES,
        userOperationLogEntry.getOperationType());

    assertEquals("retries", userOperationLogEntry.getProperty());
    assertEquals("4", userOperationLogEntry.getNewValue());
    assertNull(userOperationLogEntry.getOrgValue());

    assertEquals(USER_ID, userOperationLogEntry.getUserId());

    assertNull(job.getJobDefinitionId(), userOperationLogEntry.getJobDefinitionId());
    assertNull(job.getProcessInstanceId(), userOperationLogEntry.getProcessInstanceId());
    assertNull(job.getProcessDefinitionId(), userOperationLogEntry.getProcessDefinitionId());
    assertNull(job.getProcessDefinitionKey(), userOperationLogEntry.getProcessDefinitionKey());
    assertNull(job.getDeploymentId(), userOperationLogEntry.getDeploymentId());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, userOperationLogEntry.getCategory());

    // check 'nrOfInstances' entry
    userOperationLogEntry = query.property("nrOfInstances").singleResult();
    assertEquals(EntityTypes.JOB, userOperationLogEntry.getEntityType());
    assertNull(userOperationLogEntry.getJobId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES,
        userOperationLogEntry.getOperationType());

    assertEquals("nrOfInstances", userOperationLogEntry.getProperty());
    assertEquals("1", userOperationLogEntry.getNewValue());
    assertNull(userOperationLogEntry.getOrgValue());

    assertEquals(USER_ID, userOperationLogEntry.getUserId());

    assertNull(job.getJobDefinitionId(), userOperationLogEntry.getJobDefinitionId());
    assertNull(job.getProcessInstanceId(), userOperationLogEntry.getProcessInstanceId());
    assertNull(job.getProcessDefinitionId(), userOperationLogEntry.getProcessDefinitionId());
    assertNull(job.getProcessDefinitionKey(), userOperationLogEntry.getProcessDefinitionKey());
    assertNull(job.getDeploymentId(), userOperationLogEntry.getDeploymentId());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, userOperationLogEntry.getCategory());
    
    // check 'async' entry
    userOperationLogEntry = query.property("async").singleResult();
    assertEquals(EntityTypes.JOB, userOperationLogEntry.getEntityType());
    assertNull(userOperationLogEntry.getJobId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES,
        userOperationLogEntry.getOperationType());

    assertEquals("async", userOperationLogEntry.getProperty());
    assertEquals("true", userOperationLogEntry.getNewValue());
    assertNull(userOperationLogEntry.getOrgValue());

    assertEquals(USER_ID, userOperationLogEntry.getUserId());

    assertNull(job.getJobDefinitionId(), userOperationLogEntry.getJobDefinitionId());
    assertNull(job.getProcessInstanceId(), userOperationLogEntry.getProcessInstanceId());
    assertNull(job.getProcessDefinitionId(), userOperationLogEntry.getProcessDefinitionId());
    assertNull(job.getProcessDefinitionKey(), userOperationLogEntry.getProcessDefinitionKey());
    assertNull(job.getDeploymentId(), userOperationLogEntry.getDeploymentId());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, userOperationLogEntry.getCategory());
    
    managementService.deleteBatch(batch.getId(), true);
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/asyncTaskProcess.bpmn20.xml"})
  @Test
  public void testSetRetriesAsyncProcessInstanceId() {
    // given a job
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncTaskProcess");
    Job job = managementService.createJobQuery().singleResult();

    // when I set the job retries
    Batch batch = managementService.setJobRetriesAsync(Arrays.asList(processInstance.getId()), (ProcessInstanceQuery) null, 4);

    // then three op log entries are written
    UserOperationLogQuery query = historyService
            .createUserOperationLogQuery()
            .operationType(UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES);
    assertEquals(3, query.count());

    // check 'retries' entry
    UserOperationLogEntry userOperationLogEntry = query.property("retries").singleResult();
    assertEquals(EntityTypes.JOB, userOperationLogEntry.getEntityType());
    assertNull(userOperationLogEntry.getJobId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES,
        userOperationLogEntry.getOperationType());

    assertEquals("retries", userOperationLogEntry.getProperty());
    assertEquals("4", userOperationLogEntry.getNewValue());
    assertNull(userOperationLogEntry.getOrgValue());

    assertEquals(USER_ID, userOperationLogEntry.getUserId());

    assertNull(job.getJobDefinitionId(), userOperationLogEntry.getJobDefinitionId());
    assertNull(job.getProcessInstanceId(), userOperationLogEntry.getProcessInstanceId());
    assertNull(job.getProcessDefinitionId(), userOperationLogEntry.getProcessDefinitionId());
    assertNull(job.getProcessDefinitionKey(), userOperationLogEntry.getProcessDefinitionKey());
    assertNull(job.getDeploymentId(), userOperationLogEntry.getDeploymentId());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, userOperationLogEntry.getCategory());

    // check 'nrOfInstances' entry
    userOperationLogEntry = query.property("nrOfInstances").singleResult();
    assertEquals(EntityTypes.JOB, userOperationLogEntry.getEntityType());
    assertNull(userOperationLogEntry.getJobId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES,
        userOperationLogEntry.getOperationType());

    assertEquals("nrOfInstances", userOperationLogEntry.getProperty());
    assertEquals("1", userOperationLogEntry.getNewValue());
    assertNull(userOperationLogEntry.getOrgValue());

    assertEquals(USER_ID, userOperationLogEntry.getUserId());

    assertNull(job.getJobDefinitionId(), userOperationLogEntry.getJobDefinitionId());
    assertNull(job.getProcessInstanceId(), userOperationLogEntry.getProcessInstanceId());
    assertNull(job.getProcessDefinitionId(), userOperationLogEntry.getProcessDefinitionId());
    assertNull(job.getProcessDefinitionKey(), userOperationLogEntry.getProcessDefinitionKey());
    assertNull(job.getDeploymentId(), userOperationLogEntry.getDeploymentId());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, userOperationLogEntry.getCategory());
    
    // check 'async' entry
    userOperationLogEntry = query.property("async").singleResult();
    assertEquals(EntityTypes.JOB, userOperationLogEntry.getEntityType());
    assertNull(userOperationLogEntry.getJobId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES,
        userOperationLogEntry.getOperationType());

    assertEquals("async", userOperationLogEntry.getProperty());
    assertEquals("true", userOperationLogEntry.getNewValue());
    assertNull(userOperationLogEntry.getOrgValue());

    assertEquals(USER_ID, userOperationLogEntry.getUserId());

    assertNull(job.getJobDefinitionId(), userOperationLogEntry.getJobDefinitionId());
    assertNull(job.getProcessInstanceId(), userOperationLogEntry.getProcessInstanceId());
    assertNull(job.getProcessDefinitionId(), userOperationLogEntry.getProcessDefinitionId());
    assertNull(job.getProcessDefinitionKey(), userOperationLogEntry.getProcessDefinitionKey());
    assertNull(job.getDeploymentId(), userOperationLogEntry.getDeploymentId());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, userOperationLogEntry.getCategory());
    
    managementService.deleteBatch(batch.getId(), true);
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/asyncTaskProcess.bpmn20.xml"})
  @Test
  public void testSetJobDueDate() {
    // given a job
    runtimeService.startProcessInstanceByKey("asyncTaskProcess");
    Job job = managementService.createJobQuery().singleResult();

    // and set the job due date
    Date newDate = new Date(ClockUtil.getCurrentTime().getTime() + 2 * 1000);
    managementService.setJobDuedate(job.getId(), newDate);

    // then one op log entry is written
    UserOperationLogQuery query = historyService
            .createUserOperationLogQuery()
            .operationType(UserOperationLogEntry.OPERATION_TYPE_SET_DUEDATE);
    assertEquals(1, query.count());

    // assert details
    UserOperationLogEntry entry = query.singleResult();
    assertEquals(job.getId(), entry.getJobId());
    assertEquals(job.getDeploymentId(), entry.getDeploymentId());
    assertEquals(job.getJobDefinitionId(), entry.getJobDefinitionId());
    assertEquals("duedate", entry.getProperty());
    assertNull(entry.getOrgValue());
    assertEquals(newDate, new Date(Long.valueOf(entry.getNewValue())));
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/timer/TimerRecalculationTest.testFinishedJob.bpmn20.xml"})
  @Test
  public void testRecalculateJobDueDate() {
    // given a job
    HashMap<String, Object> variables1 = new HashMap<String, Object>();
    Date duedate = ClockUtil.getCurrentTime();
    variables1.put("dueDate", duedate);

    runtimeService.startProcessInstanceByKey("intermediateTimerEventExample", variables1);
    Job job = managementService.createJobQuery().singleResult();

    // when I recalculate the job due date
    managementService.recalculateJobDuedate(job.getId(), false);

    // then one op log entry is written
    UserOperationLogQuery query = historyService
            .createUserOperationLogQuery()
            .operationType(UserOperationLogEntry.OPERATION_TYPE_RECALC_DUEDATE);
    assertEquals(2, query.count());

    // assert details
    UserOperationLogEntry entry = query.property("duedate").singleResult();
    assertEquals(job.getId(), entry.getJobId());
    assertEquals(job.getDeploymentId(), entry.getDeploymentId());
    assertEquals(job.getJobDefinitionId(), entry.getJobDefinitionId());
    assertEquals("duedate", entry.getProperty());
    assertTrue(DateUtils.truncatedEquals(duedate, new Date(Long.valueOf(entry.getOrgValue())), Calendar.SECOND));
    assertTrue(DateUtils.truncatedEquals(duedate, new Date(Long.valueOf(entry.getNewValue())), Calendar.SECOND));
    
    entry = query.property("creationDateBased").singleResult();
    assertEquals(job.getId(), entry.getJobId());
    assertEquals(job.getDeploymentId(), entry.getDeploymentId());
    assertEquals(job.getJobDefinitionId(), entry.getJobDefinitionId());
    assertEquals("creationDateBased", entry.getProperty());
    assertNull(entry.getOrgValue());
    assertFalse(Boolean.valueOf(entry.getNewValue()));
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/asyncTaskProcess.bpmn20.xml"})
  @Test
  public void testDelete() {
    // given a job
    runtimeService.startProcessInstanceByKey("asyncTaskProcess");
    Job job = managementService.createJobQuery().singleResult();

    // when I delete a job
    managementService.deleteJob(job.getId());

    // then an op log entry is written
    UserOperationLogEntry userOperationLogEntry = historyService
            .createUserOperationLogQuery()
            .operationType(UserOperationLogEntry.OPERATION_TYPE_DELETE)
            .singleResult();
    assertNotNull(userOperationLogEntry);

    assertEquals(EntityTypes.JOB, userOperationLogEntry.getEntityType());
    assertEquals(job.getId(), userOperationLogEntry.getJobId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_DELETE,
        userOperationLogEntry.getOperationType());

    assertNull(userOperationLogEntry.getProperty());
    assertNull(userOperationLogEntry.getNewValue());
    assertNull(userOperationLogEntry.getOrgValue());

    assertEquals(USER_ID, userOperationLogEntry.getUserId());

    assertEquals(job.getJobDefinitionId(), userOperationLogEntry.getJobDefinitionId());
    assertEquals(job.getProcessInstanceId(), userOperationLogEntry.getProcessInstanceId());
    assertEquals(job.getProcessDefinitionId(), userOperationLogEntry.getProcessDefinitionId());
    assertEquals(job.getProcessDefinitionKey(), userOperationLogEntry.getProcessDefinitionKey());
    assertEquals(job.getDeploymentId(), userOperationLogEntry.getDeploymentId());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, userOperationLogEntry.getCategory());
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/asyncTaskProcess.bpmn20.xml"})
  @Test
  public void testExecute() {
    // given a job
    runtimeService.startProcessInstanceByKey("asyncTaskProcess");
    Job job = managementService.createJobQuery().singleResult();

    // when I execute a job manually
    managementService.executeJob(job.getId());

    // then an op log entry is written
    UserOperationLogEntry userOperationLogEntry = historyService
            .createUserOperationLogQuery()
            .operationType(UserOperationLogEntry.OPERATION_TYPE_EXECUTE)
            .singleResult();
    assertNotNull(userOperationLogEntry);

    assertEquals(EntityTypes.JOB, userOperationLogEntry.getEntityType());
    assertEquals(job.getId(), userOperationLogEntry.getJobId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_EXECUTE,
        userOperationLogEntry.getOperationType());

    assertNull(userOperationLogEntry.getProperty());
    assertNull(userOperationLogEntry.getNewValue());
    assertNull(userOperationLogEntry.getOrgValue());

    assertEquals(USER_ID, userOperationLogEntry.getUserId());

    assertEquals(job.getJobDefinitionId(), userOperationLogEntry.getJobDefinitionId());
    assertEquals(job.getProcessInstanceId(), userOperationLogEntry.getProcessInstanceId());
    assertEquals(job.getProcessDefinitionId(), userOperationLogEntry.getProcessDefinitionId());
    assertEquals(job.getProcessDefinitionKey(), userOperationLogEntry.getProcessDefinitionKey());
    assertEquals(job.getDeploymentId(), userOperationLogEntry.getDeploymentId());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, userOperationLogEntry.getCategory());
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/asyncTaskProcess.bpmn20.xml"})
  @Test
  public void testExecuteByJobExecutor() {
    // given a job
    runtimeService.startProcessInstanceByKey("asyncTaskProcess");
    assertEquals(1L, managementService.createJobQuery().count());

    // when a job is executed by the job executor
    testRule.waitForJobExecutorToProcessAllJobs(TimeUnit.MILLISECONDS.convert(5L, TimeUnit.SECONDS));

    // then no op log entry is written
    assertEquals(0L, managementService.createJobQuery().count());
    long logEntriesCount = historyService
            .createUserOperationLogQuery()
            .operationType(UserOperationLogEntry.OPERATION_TYPE_EXECUTE)
            .count();
    assertEquals(0L, logEntriesCount);
  }
}
