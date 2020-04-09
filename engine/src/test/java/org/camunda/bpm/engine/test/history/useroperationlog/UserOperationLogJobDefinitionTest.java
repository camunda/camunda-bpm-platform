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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class UserOperationLogJobDefinitionTest extends AbstractUserOperationLogTest {

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/asyncTaskProcess.bpmn20.xml"})
  @Test
  public void testSetOverridingPriority() {
    // For a given deployment
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();

    // given a job definition
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when I set a job priority
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 42);

    // then an op log entry is written
    UserOperationLogEntry userOperationLogEntry = historyService.createUserOperationLogQuery().singleResult();
    assertNotNull(userOperationLogEntry);

    assertEquals(EntityTypes.JOB_DEFINITION, userOperationLogEntry.getEntityType());
    assertEquals(jobDefinition.getId(), userOperationLogEntry.getJobDefinitionId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_SET_PRIORITY,
        userOperationLogEntry.getOperationType());

    assertEquals("overridingPriority", userOperationLogEntry.getProperty());
    assertEquals("42", userOperationLogEntry.getNewValue());
    assertEquals(null, userOperationLogEntry.getOrgValue());

    assertEquals(USER_ID, userOperationLogEntry.getUserId());
    
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, userOperationLogEntry.getCategory());

    assertEquals(jobDefinition.getProcessDefinitionId(), userOperationLogEntry.getProcessDefinitionId());
    assertEquals(jobDefinition.getProcessDefinitionKey(), userOperationLogEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, userOperationLogEntry.getDeploymentId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/asyncTaskProcess.bpmn20.xml"})
  @Test
  public void testOverwriteOverridingPriority() {
    // given a job definition
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // with an overriding priority
    ClockUtil.setCurrentTime(new Date(System.currentTimeMillis()));
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 42);

    // when I overwrite that priority
    ClockUtil.setCurrentTime(new Date(System.currentTimeMillis() + 10000));
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 43);

    // then this is accessible via the op log
    UserOperationLogEntry userOperationLogEntry = historyService.createUserOperationLogQuery()
        .orderByTimestamp().desc().listPage(0, 1).get(0);
    assertNotNull(userOperationLogEntry);

    assertEquals(EntityTypes.JOB_DEFINITION, userOperationLogEntry.getEntityType());
    assertEquals(jobDefinition.getId(), userOperationLogEntry.getJobDefinitionId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_SET_PRIORITY,
        userOperationLogEntry.getOperationType());
    
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, userOperationLogEntry.getCategory());

    assertEquals("overridingPriority", userOperationLogEntry.getProperty());
    assertEquals("43", userOperationLogEntry.getNewValue());
    assertEquals("42", userOperationLogEntry.getOrgValue());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/asyncTaskProcess.bpmn20.xml"})
  @Test
  public void testClearOverridingPriority() {
    // for a given deployment
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();

    // given a job definition
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // with an overriding priority
    ClockUtil.setCurrentTime(new Date(System.currentTimeMillis()));
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 42);

    // when I clear that priority
    ClockUtil.setCurrentTime(new Date(System.currentTimeMillis() + 10000));
    managementService.clearOverridingJobPriorityForJobDefinition(jobDefinition.getId());

    // then this is accessible via the op log
    UserOperationLogEntry userOperationLogEntry = historyService.createUserOperationLogQuery()
        .orderByTimestamp().desc().listPage(0, 1).get(0);
    assertNotNull(userOperationLogEntry);

    assertEquals(EntityTypes.JOB_DEFINITION, userOperationLogEntry.getEntityType());
    assertEquals(jobDefinition.getId(), userOperationLogEntry.getJobDefinitionId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_SET_PRIORITY,
        userOperationLogEntry.getOperationType());

    assertEquals("overridingPriority", userOperationLogEntry.getProperty());
    assertNull(userOperationLogEntry.getNewValue());
    assertEquals("42", userOperationLogEntry.getOrgValue());

    assertEquals(USER_ID, userOperationLogEntry.getUserId());
    
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, userOperationLogEntry.getCategory());

    assertEquals(jobDefinition.getProcessDefinitionId(), userOperationLogEntry.getProcessDefinitionId());
    assertEquals(jobDefinition.getProcessDefinitionKey(), userOperationLogEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, userOperationLogEntry.getDeploymentId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/asyncTaskProcess.bpmn20.xml"})
  @Test
  public void testSetOverridingPriorityCascadeToJobs() {
    // given a job definition and job
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    runtimeService.startProcessInstanceByKey("asyncTaskProcess");
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    Job job = managementService.createJobQuery().singleResult();

    // when I set an overriding priority with cascade=true
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 42, true);

    // then there are three op log entries
    assertEquals(3, historyService.createUserOperationLogQuery().count());

    // (1): One for the process instance start
    UserOperationLogEntry processInstanceStartOpLogEntry = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.PROCESS_INSTANCE).singleResult();
    assertNotNull(processInstanceStartOpLogEntry);

    // (2): One for the job definition priority
    UserOperationLogEntry jobDefOpLogEntry = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.JOB_DEFINITION).singleResult();
    assertNotNull(jobDefOpLogEntry);

    // (3): and another one for the job priorities
    UserOperationLogEntry jobOpLogEntry = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.JOB).singleResult();
    assertNotNull(jobOpLogEntry);

    assertEquals("the two job related entries should be part of the same operation",
        jobDefOpLogEntry.getOperationId(), jobOpLogEntry.getOperationId());

    assertEquals(EntityTypes.JOB, jobOpLogEntry.getEntityType());
    assertNull("id should null because it is a bulk update operation", jobOpLogEntry.getJobId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_SET_PRIORITY, jobOpLogEntry.getOperationType());

    assertEquals("priority", jobOpLogEntry.getProperty());
    assertEquals("42", jobOpLogEntry.getNewValue());
    assertNull("Original Value should be null because it is not known for bulk operations",
        jobOpLogEntry.getOrgValue());

    assertEquals(USER_ID, jobOpLogEntry.getUserId());
    
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, jobOpLogEntry.getCategory());

    // these properties should be there to narrow down the bulk update (like a SQL WHERE clasue)
    assertEquals(job.getJobDefinitionId(), jobOpLogEntry.getJobDefinitionId());
    assertNull("an unspecified set of process instances was affected by the operation",
        jobOpLogEntry.getProcessInstanceId());
    assertEquals(job.getProcessDefinitionId(), jobOpLogEntry.getProcessDefinitionId());
    assertEquals(job.getProcessDefinitionKey(), jobOpLogEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, jobOpLogEntry.getDeploymentId());
  }

}
