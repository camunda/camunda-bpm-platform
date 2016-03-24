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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventSubprocessJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ActivityInstanceAssert.ActivityInstanceAssertThatClause;
import org.camunda.bpm.engine.test.util.ExecutionAssert;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assert;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationTestRule extends TestWatcher {

  public static final String DEFAULT_BPMN_RESOURCE_NAME = "process.bpmn20.xml";

  protected ProcessEngineRule processEngineRule;
  protected ProcessEngine processEngine;

  public ProcessInstanceSnapshot snapshotBeforeMigration;
  public ProcessInstanceSnapshot snapshotAfterMigration;

  public MigrationTestRule(ProcessEngineRule processEngineRule) {
    this.processEngineRule = processEngineRule;
  }

  @Override
  protected void starting(Description description) {
    this.processEngine = processEngineRule.getProcessEngine();
  }

  @Override
  protected void finished(Description description) {
    this.processEngine = null;
  }

  public void assertProcessEnded(String processInstanceId) {
    ProcessInstance processInstance = processEngine
      .getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstanceId)
      .singleResult();

    if (processInstance!=null) {
      Assert.fail("Process instance with id " + processInstanceId + " is not finished");
    }
  }

  public ProcessDefinition findProcessDefinition(String key, int version) {
    return processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(key)
        .processDefinitionVersion(version)
        .singleResult();
  }

  public ProcessDefinition deploy(String name, BpmnModelInstance bpmnModel) {
    Deployment deployment = processEngine.getRepositoryService()
      .createDeployment()
      .addModelInstance(name, bpmnModel)
      .deploy();

    processEngineRule.manageDeployment(deployment);

    return processEngineRule.getRepositoryService()
      .createProcessDefinitionQuery()
      .deploymentId(deployment.getId())
      .singleResult();
  }

  /**
   * Deploys a bpmn model instance and returns its corresponding process definition object
   */
  public ProcessDefinition deploy(BpmnModelInstance bpmnModel) {
    return deploy(DEFAULT_BPMN_RESOURCE_NAME, bpmnModel);
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

  public void migrateProcessInstance(MigrationPlan migrationPlan, ProcessInstance processInstance) {
    snapshotBeforeMigration = takeFullProcessInstanceSnapshot(processInstance);

    processEngine.getRuntimeService()
      .newMigration(migrationPlan).processInstanceIds(Collections.singletonList(snapshotBeforeMigration.getProcessInstanceId())).execute();

    snapshotAfterMigration = takeFullProcessInstanceSnapshot(processInstance);
  }

  public void completeTask(String taskKey) {
    TaskService taskService = processEngine.getTaskService();
    Task task = taskService.createTaskQuery().taskDefinitionKey(taskKey).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());
  }

  public void completeAnyTask(String taskKey) {
    TaskService taskService = processEngine.getTaskService();
    List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey(taskKey).list();
    assertTrue(!tasks.isEmpty());
    taskService.complete(tasks.get(0).getId());
  }

  public void correlateMessage(String messageName) {
    processEngine.getRuntimeService().createMessageCorrelation(messageName).correlate();
  }

  public void sendSignal(String signalName) {
    processEngine.getRuntimeService().signalEventReceived(signalName);
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

  public void assertEventSubscriptionMigrated(String activityIdBefore, String activityIdAfter, String eventName) {
    EventSubscription eventSubscriptionBefore = snapshotBeforeMigration.getEventSubscriptionForActivityIdAndEventName(activityIdBefore, eventName);
    assertNotNull("Expected that an event subscription for activity '" + activityIdBefore + "' exists before migration", eventSubscriptionBefore);
    EventSubscription eventSubscriptionAfter = snapshotAfterMigration.getEventSubscriptionForActivityIdAndEventName(activityIdAfter, eventName);
    assertNotNull("Expected that an event subscription for activity '" + activityIdAfter + "' exists after migration", eventSubscriptionAfter);

    assertEquals(eventSubscriptionBefore.getId(), eventSubscriptionAfter.getId());
    assertEquals(eventSubscriptionBefore.getEventType(), eventSubscriptionAfter.getEventType());
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

    assertJobMigrated(jobBefore, activityIdAfter);
  }

  public void assertJobMigrated(Job jobBefore, String activityIdAfter) {

    Job jobAfter = snapshotAfterMigration.getJobById(jobBefore.getId());
    assertNotNull("Expected that a job with id '" + jobBefore.getId() + "' exists after migration", jobAfter);

    JobDefinition jobDefinitionAfter = snapshotAfterMigration.getJobDefinitionForActivityIdAndType(activityIdAfter, ((JobEntity) jobBefore).getJobHandlerType());
    assertNotNull("Expected that a job definition for activity '" + activityIdAfter + "' exists after migration", jobDefinitionAfter);

    assertEquals(jobBefore.getId(), jobAfter.getId());
    assertEquals("Expected that job is assigned to job definition '" + jobDefinitionAfter.getId() + "' after migration",
        jobDefinitionAfter.getId(), jobAfter.getJobDefinitionId());
    assertEquals(jobBefore.getDuedate(), jobAfter.getDuedate());
    assertEquals(((JobEntity) jobBefore).getType(), ((JobEntity) jobAfter).getType());
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


}
