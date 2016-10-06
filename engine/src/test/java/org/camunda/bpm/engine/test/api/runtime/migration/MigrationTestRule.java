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
package org.camunda.bpm.engine.test.api.runtime.migration;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventSubprocessJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ActivityInstanceAssert.ActivityInstanceAssertThatClause;
import org.camunda.bpm.engine.test.util.ExecutionAssert;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.junit.Assert;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationTestRule extends ProcessEngineTestRule {

  public ProcessInstanceSnapshot snapshotBeforeMigration;
  public ProcessInstanceSnapshot snapshotAfterMigration;

  public MigrationTestRule(ProcessEngineRule processEngineRule) {
    super(processEngineRule);
  }

  public String getSingleExecutionIdForActivity(ActivityInstance activityInstance, String activityId) {
    ActivityInstance singleInstance = getSingleActivityInstance(activityInstance, activityId);

    String[] executionIds = singleInstance.getExecutionIds();
    if (executionIds.length == 1) {
      return executionIds[0];
    }
    else {
      throw new RuntimeException("There is more than one execution assigned to activity instance " + singleInstance.getId());
    }
  }

  public String getSingleExecutionIdForActivityBeforeMigration(String activityId) {
    return getSingleExecutionIdForActivity(snapshotBeforeMigration.getActivityTree(), activityId);
  }

  public String getSingleExecutionIdForActivityAfterMigration(String activityId) {
    return getSingleExecutionIdForActivity(snapshotAfterMigration.getActivityTree(), activityId);
  }

  public ActivityInstance getSingleActivityInstance(ActivityInstance tree, String activityId) {
    ActivityInstance[] activityInstances = tree.getActivityInstances(activityId);
    if (activityInstances.length == 1) {
      return activityInstances[0];
    }
    else {
      throw new RuntimeException("There is not exactly one activity instance for activity " + activityId);
    }
  }

  public ActivityInstance getSingleActivityInstanceBeforeMigration(String activityId) {
    return getSingleActivityInstance(snapshotBeforeMigration.getActivityTree(), activityId);
  }

  public ActivityInstance getSingleActivityInstanceAfterMigration(String activityId) {
    return getSingleActivityInstance(snapshotAfterMigration.getActivityTree(), activityId);
  }

  public ProcessInstanceSnapshot takeFullProcessInstanceSnapshot(ProcessInstance processInstance) {
    return takeProcessInstanceSnapshot(processInstance).full();
  }

  public ProcessInstanceSnapshotBuilder takeProcessInstanceSnapshot(ProcessInstance processInstance) {
    return new ProcessInstanceSnapshotBuilder(processInstance, processEngine);
  }

  public ProcessInstance createProcessInstanceAndMigrate(MigrationPlan migrationPlan) {
    ProcessInstance processInstance = processEngine.getRuntimeService()
      .startProcessInstanceById(migrationPlan.getSourceProcessDefinitionId());

    migrateProcessInstance(migrationPlan, processInstance);
    return processInstance;
  }

  public ProcessInstance createProcessInstanceAndMigrate(MigrationPlan migrationPlan, Map<String, Object> variables) {
    ProcessInstance processInstance = processEngine.getRuntimeService()
        .startProcessInstanceById(migrationPlan.getSourceProcessDefinitionId(), variables);

    migrateProcessInstance(migrationPlan, processInstance);
    return processInstance;
  }

  public void migrateProcessInstance(MigrationPlan migrationPlan, ProcessInstance processInstance) {
    snapshotBeforeMigration = takeFullProcessInstanceSnapshot(processInstance);

    RuntimeService runtimeService = processEngine.getRuntimeService();

    runtimeService
      .newMigration(migrationPlan).processInstanceIds(Collections.singletonList(snapshotBeforeMigration.getProcessInstanceId())).execute();

    // fetch updated process instance
    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

    snapshotAfterMigration = takeFullProcessInstanceSnapshot(processInstance);
  }

  public void triggerTimer() {
    Job job = assertTimerJobExists(snapshotAfterMigration);
    processEngine.getManagementService().executeJob(job.getId());
  }

  public ExecutionAssert assertExecutionTreeAfterMigration() {
    return assertThat(snapshotAfterMigration.getExecutionTree());
  }

  public ActivityInstanceAssertThatClause assertActivityTreeAfterMigration() {
    return assertThat(snapshotAfterMigration.getActivityTree());
  }

  public void assertEventSubscriptionsMigrated(String activityIdBefore, String activityIdAfter, String eventName) {
    List<EventSubscription> eventSubscriptionsBefore = snapshotBeforeMigration.getEventSubscriptionsForActivityIdAndEventName(activityIdAfter, eventName);

    for (EventSubscription eventSubscription : eventSubscriptionsBefore) {
      assertEventSubscriptionMigrated(eventSubscription, activityIdAfter, eventName);
    }
  }

  protected void assertEventSubscriptionMigrated(EventSubscription eventSubscriptionBefore, String activityIdAfter, String eventName) {
    EventSubscription eventSubscriptionAfter = snapshotAfterMigration.getEventSubscriptionById(eventSubscriptionBefore.getId());
    assertNotNull("Expected that an event subscription with id '" + eventSubscriptionBefore.getId() + "' "
        + "exists after migration", eventSubscriptionAfter);

    assertEquals(eventSubscriptionBefore.getEventType(), eventSubscriptionAfter.getEventType());
    assertEquals(activityIdAfter, eventSubscriptionAfter.getActivityId());
    assertEquals(eventName, eventSubscriptionAfter.getEventName());
  }


  public void assertEventSubscriptionMigrated(String activityIdBefore, String activityIdAfter, String eventName) {
    EventSubscription eventSubscriptionBefore = snapshotBeforeMigration.getEventSubscriptionForActivityIdAndEventName(activityIdBefore, eventName);
    assertNotNull("Expected that an event subscription for activity '" + activityIdBefore + "' exists before migration", eventSubscriptionBefore);

    assertEventSubscriptionMigrated(eventSubscriptionBefore, activityIdAfter, eventName);
  }

  public void assertEventSubscriptionMigrated(String activityIdBefore, String eventNameBefore, String activityIdAfter, String eventNameAfter) {
    EventSubscription eventSubscriptionBefore = snapshotBeforeMigration.getEventSubscriptionForActivityIdAndEventName(activityIdBefore, eventNameBefore);
    assertNotNull("Expected that an event subscription for activity '" + activityIdBefore + "' exists before migration", eventSubscriptionBefore);

    assertEventSubscriptionMigrated(eventSubscriptionBefore, activityIdAfter, eventNameAfter);
  }

  public void assertEventSubscriptionRemoved(String activityId, String eventName) {
    EventSubscription eventSubscriptionBefore = snapshotBeforeMigration.getEventSubscriptionForActivityIdAndEventName(activityId, eventName);
    assertNotNull("Expected an event subscription for activity '" + activityId + "' before the migration", eventSubscriptionBefore);

    for (EventSubscription eventSubscription : snapshotAfterMigration.getEventSubscriptions()) {
      if (eventSubscriptionBefore.getId().equals(eventSubscription.getId())) {
        fail("Expected event subscription '" + eventSubscriptionBefore.getId() + "' to be removed after migration");
      }
    }
  }

  public void assertEventSubscriptionCreated(String activityId, String eventName) {
    EventSubscription eventSubscriptionAfter = snapshotAfterMigration.getEventSubscriptionForActivityIdAndEventName(activityId, eventName);
    assertNotNull("Expected an event subscription for activity '" + activityId + "' after the migration", eventSubscriptionAfter);

    for (EventSubscription eventSubscription : snapshotBeforeMigration.getEventSubscriptions()) {
      if (eventSubscriptionAfter.getId().equals(eventSubscription.getId())) {
        fail("Expected event subscription '" + eventSubscriptionAfter.getId() + "' to be created after migration");
      }
    }
  }

  public void assertTimerJob(Job job) {
    assertEquals("Expected job to be a timer job", TimerEntity.TYPE, ((JobEntity) job).getType());
  }

  public Job assertTimerJobExists(ProcessInstanceSnapshot snapshot) {
    List<Job> jobs = snapshot.getJobs();
    assertEquals(1, jobs.size());
    Job job = jobs.get(0);
    assertTimerJob(job);
    return job;
  }

  public void assertJobCreated(String activityId, String handlerType) {
    JobDefinition jobDefinitionAfter = snapshotAfterMigration.getJobDefinitionForActivityIdAndType(activityId, handlerType);
    assertNotNull("Expected that a job definition for activity '" + activityId + "' exists after migration", jobDefinitionAfter);

    Job jobAfter = snapshotAfterMigration.getJobForDefinitionId(jobDefinitionAfter.getId());
    assertNotNull("Expected that a job for activity '" + activityId + "' exists after migration", jobAfter);
    assertTimerJob(jobAfter);
    assertEquals(jobDefinitionAfter.getProcessDefinitionId(), jobAfter.getProcessDefinitionId());
    assertEquals(jobDefinitionAfter.getProcessDefinitionKey(), jobAfter.getProcessDefinitionKey());

    for (Job job : snapshotBeforeMigration.getJobs()) {
      if (jobAfter.getId().equals(job.getId())) {
        fail("Expected job '" + jobAfter.getId() + "' to be created first after migration");
      }
    }
  }

  public void assertJobRemoved(String activityId, String handlerType) {
    JobDefinition jobDefinitionBefore = snapshotBeforeMigration.getJobDefinitionForActivityIdAndType(activityId, handlerType);
    assertNotNull("Expected that a job definition for activity '" + activityId + "' exists before migration", jobDefinitionBefore);

    Job jobBefore = snapshotBeforeMigration.getJobForDefinitionId(jobDefinitionBefore.getId());
    assertNotNull("Expected that a job for activity '" + activityId + "' exists before migration", jobBefore);
    assertTimerJob(jobBefore);

    for (Job job : snapshotAfterMigration.getJobs()) {
      if (jobBefore.getId().equals(job.getId())) {
        fail("Expected job '" + jobBefore.getId() + "' to be removed after migration");
      }
    }
  }

  public void assertJobMigrated(String activityIdBefore, String activityIdAfter, String handlerType) {
    JobDefinition jobDefinitionBefore = snapshotBeforeMigration.getJobDefinitionForActivityIdAndType(activityIdBefore, handlerType);
    assertNotNull("Expected that a job definition for activity '" + activityIdBefore + "' exists before migration", jobDefinitionBefore);

    Job jobBefore = snapshotBeforeMigration.getJobForDefinitionId(jobDefinitionBefore.getId());
    assertNotNull("Expected that a timer job for activity '" + activityIdBefore + "' exists before migration", jobBefore);

    assertJobMigrated(jobBefore, activityIdAfter, jobBefore.getDuedate());
  }

  public void assertJobMigrated(Job jobBefore, String activityIdAfter) {
    assertJobMigrated(jobBefore, activityIdAfter, jobBefore.getDuedate());
  }

  public void assertJobMigrated(Job jobBefore, String activityIdAfter, Date dueDateAfter) {

    Job jobAfter = snapshotAfterMigration.getJobById(jobBefore.getId());
    assertNotNull("Expected that a job with id '" + jobBefore.getId() + "' exists after migration", jobAfter);

    JobDefinition jobDefinitionAfter = snapshotAfterMigration.getJobDefinitionForActivityIdAndType(activityIdAfter, ((JobEntity) jobBefore).getJobHandlerType());
    assertNotNull("Expected that a job definition for activity '" + activityIdAfter + "' exists after migration", jobDefinitionAfter);

    assertEquals(jobBefore.getId(), jobAfter.getId());
    assertEquals("Expected that job is assigned to job definition '" + jobDefinitionAfter.getId() + "' after migration",
        jobDefinitionAfter.getId(), jobAfter.getJobDefinitionId());
    assertEquals("Expected that job is assigned to deployment '" + snapshotAfterMigration.getDeploymentId() + "' after migration",
        snapshotAfterMigration.getDeploymentId(), jobAfter.getDeploymentId());
    assertEquals(dueDateAfter, jobAfter.getDuedate());
    assertEquals(((JobEntity) jobBefore).getType(), ((JobEntity) jobAfter).getType());
    assertEquals(jobBefore.getPriority(), jobAfter.getPriority());
    assertEquals(jobDefinitionAfter.getProcessDefinitionId(), jobAfter.getProcessDefinitionId());
    assertEquals(jobDefinitionAfter.getProcessDefinitionKey(), jobAfter.getProcessDefinitionKey());
  }

  public void assertBoundaryTimerJobCreated(String activityId) {
    assertJobCreated(activityId, TimerExecuteNestedActivityJobHandler.TYPE);
  }

  public void assertBoundaryTimerJobRemoved(String activityId) {
    assertJobRemoved(activityId, TimerExecuteNestedActivityJobHandler.TYPE);
  }

  public void assertBoundaryTimerJobMigrated(String activityIdBefore, String activityIdAfter) {
    assertJobMigrated(activityIdBefore, activityIdAfter, TimerExecuteNestedActivityJobHandler.TYPE);
  }

  public void assertIntermediateTimerJobCreated(String activityId) {
    assertJobCreated(activityId, TimerCatchIntermediateEventJobHandler.TYPE);
  }

  public void assertIntermediateTimerJobRemoved(String activityId) {
    assertJobRemoved(activityId, TimerCatchIntermediateEventJobHandler.TYPE);
  }

  public void assertIntermediateTimerJobMigrated(String activityIdBefore, String activityIdAfter) {
    assertJobMigrated(activityIdBefore, activityIdAfter, TimerCatchIntermediateEventJobHandler.TYPE);
  }

  public void assertEventSubProcessTimerJobCreated(String activityId) {
    assertJobCreated(activityId, TimerStartEventSubprocessJobHandler.TYPE);
  }

  public void assertEventSubProcessTimerJobRemoved(String activityId) {
    assertJobRemoved(activityId, TimerStartEventSubprocessJobHandler.TYPE);
  }

  public void assertVariableMigratedToExecution(VariableInstance variableBefore, String executionId) {
    assertVariableMigratedToExecution(variableBefore, executionId, variableBefore.getActivityInstanceId());
  }

  public void assertVariableMigratedToExecution(VariableInstance variableBefore, String executionId, String activityInstanceId) {
    VariableInstance variableAfter = snapshotAfterMigration.getVariable(variableBefore.getId());

    Assert.assertNotNull("Variable with id " + variableBefore.getId() + " does not exist", variableAfter);

    Assert.assertEquals(activityInstanceId, variableAfter.getActivityInstanceId());
    Assert.assertEquals(variableBefore.getCaseExecutionId(), variableAfter.getCaseExecutionId());
    Assert.assertEquals(variableBefore.getCaseInstanceId(), variableAfter.getCaseInstanceId());
    Assert.assertEquals(variableBefore.getErrorMessage(), variableAfter.getErrorMessage());
    Assert.assertEquals(executionId, variableAfter.getExecutionId());
    Assert.assertEquals(variableBefore.getId(), variableAfter.getId());
    Assert.assertEquals(variableBefore.getName(), variableAfter.getName());
    Assert.assertEquals(variableBefore.getProcessInstanceId(), variableAfter.getProcessInstanceId());
    Assert.assertEquals(variableBefore.getTaskId(), variableAfter.getTaskId());
    Assert.assertEquals(variableBefore.getTenantId(), variableAfter.getTenantId());
    Assert.assertEquals(variableBefore.getTypeName(), variableAfter.getTypeName());
    Assert.assertEquals(variableBefore.getValue(), variableAfter.getValue());
  }

  public void assertSuperExecutionOfCaseInstance(String caseInstanceId, String expectedSuperExecutionId) {
    CaseExecutionEntity calledInstance = (CaseExecutionEntity) processEngine.getCaseService()
        .createCaseInstanceQuery()
        .caseInstanceId(caseInstanceId)
        .singleResult();

    Assert.assertEquals(expectedSuperExecutionId, calledInstance.getSuperExecutionId());
  }

  public void assertSuperExecutionOfProcessInstance(String processInstance, String expectedSuperExecutionId) {
    ExecutionEntity calledInstance = (ExecutionEntity) processEngine.getRuntimeService()
        .createProcessInstanceQuery()
        .processInstanceId(processInstance)
        .singleResult();

    Assert.assertEquals(expectedSuperExecutionId, calledInstance.getSuperExecutionId());
  }

}
