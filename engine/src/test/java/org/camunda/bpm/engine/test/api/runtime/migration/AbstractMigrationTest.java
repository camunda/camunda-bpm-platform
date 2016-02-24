/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.test.api.runtime.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.RuleChain;

public abstract class AbstractMigrationTest {

  public static final String AFTER_BOUNDARY_TASK = "afterBoundary";
  public static final String MESSAGE_NAME = "Message";
  public static final String SIGNAL_NAME = "Signal";
  public static final String TIMER_DATE = "2016-02-11T12:13:14Z";
  public static final String NEW_TIMER_DATE = "2018-02-11T12:13:14Z";
  public static final String ERROR_CODE = "Error";
  public static final String ESCALATION_CODE = "Escalation";

  protected ProcessEngineRule rule = new ProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  protected RuntimeService runtimeService;
  protected TaskService taskService;

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
    taskService = rule.getTaskService();
  }

  protected void completeTask(String taskKey) {
    Task task = taskService.createTaskQuery().taskDefinitionKey(taskKey).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());
  }

  protected void correlateMessage(String messageName) {
    runtimeService.createMessageCorrelation(messageName).correlate();
  }

  protected void sendSignal(String signalName) {
    runtimeService.signalEventReceived(signalName);
  }

  protected void triggerTimer() {
    Job job = assertTimerJobExists(testHelper.snapshotAfterMigration);
    rule.getManagementService().executeJob(job.getId());
  }

  protected void assertEventSubscriptionMigrated(String activityIdBefore, String activityIdAfter, String eventName) {
    EventSubscription eventSubscriptionBefore = testHelper.snapshotBeforeMigration.getEventSubscriptionForActivityIdAndEventName(activityIdBefore, eventName);
    assertNotNull("Expected that an event subscription for activity '" + activityIdBefore + "' exists before migration", eventSubscriptionBefore);
    EventSubscription eventSubscriptionAfter = testHelper.snapshotAfterMigration.getEventSubscriptionForActivityIdAndEventName(activityIdAfter, eventName);
    assertNotNull("Expected that an event subscription for activity '" + activityIdAfter + "' exists after migration", eventSubscriptionAfter);

    assertEquals(eventSubscriptionBefore.getId(), eventSubscriptionAfter.getId());
    assertEquals(eventSubscriptionBefore.getEventType(), eventSubscriptionAfter.getEventType());
  }

  protected void assertEventSubscriptionRemoved(String activityId, String eventName) {
    EventSubscription eventSubscriptionBefore = testHelper.snapshotBeforeMigration.getEventSubscriptionForActivityIdAndEventName(activityId, eventName);
    assertNotNull("Expected an event subscription for activity '" + activityId + "' before the migration", eventSubscriptionBefore);

    for (EventSubscription eventSubscription : testHelper.snapshotAfterMigration.getEventSubscriptions()) {
      if (eventSubscriptionBefore.getId().equals(eventSubscription.getId())) {
        fail("Expected event subscription '" + eventSubscriptionBefore.getId() + "' to be removed after migration");
      }
    }
  }

  protected void assertEventSubscriptionCreated(String activityId, String eventName) {
    EventSubscription eventSubscriptionAfter = testHelper.snapshotAfterMigration.getEventSubscriptionForActivityIdAndEventName(activityId, eventName);
    assertNotNull("Expected an event subscription for activity '" + activityId + "' after the migration", eventSubscriptionAfter);

    for (EventSubscription eventSubscription : testHelper.snapshotBeforeMigration.getEventSubscriptions()) {
      if (eventSubscriptionAfter.getId().equals(eventSubscription.getId())) {
        fail("Expected event subscription '" + eventSubscriptionAfter.getId() + "' to be created after migration");
      }
    }
  }

  protected void assertTimerJobMigrated(String activityIdBefore, String activityIdAfter) {
    JobDefinition jobDefinitionBefore = testHelper.snapshotBeforeMigration.getJobDefinitionForActivityId(activityIdBefore);
    assertNotNull("Expected that a job definition for activity '" + activityIdBefore + "' exists before migration", jobDefinitionBefore);

    Job jobBefore = testHelper.snapshotBeforeMigration.getJobForDefinitionId(jobDefinitionBefore.getId());
    assertNotNull("Expected that a timer job for activity '" + activityIdBefore + "' exists before migration", jobBefore);
    assertTimerJob(jobBefore);

    JobDefinition jobDefinitionAfter = testHelper.snapshotAfterMigration.getJobDefinitionForActivityId(activityIdAfter);
    assertNotNull("Expected that a job definition for activity '" + activityIdAfter + "' exists after migration", jobDefinitionAfter);

    Job jobAfter = testHelper.snapshotAfterMigration.getJobForDefinitionId(jobDefinitionAfter.getId());
    assertNotNull("Expected that a timer job for activity '" + activityIdAfter + "' exists after migration", jobAfter);
    assertTimerJob(jobAfter);

    assertEquals(jobBefore.getId(), jobAfter.getId());
    assertEquals(jobBefore.getDuedate(), jobAfter.getDuedate());
    assertEquals(jobDefinitionAfter.getProcessDefinitionId(), jobAfter.getProcessDefinitionId());
    assertEquals(jobDefinitionAfter.getProcessDefinitionKey(), jobAfter.getProcessDefinitionKey());
  }

  protected void assertTimerJobsRemoved(String... activityIds) {
    for (String activityId : activityIds) {
      assertTimerJobRemoved(activityId);
    }
  }

  protected void assertTimerJobRemoved(String activityId) {
    JobDefinition jobDefinitionBefore = testHelper.snapshotBeforeMigration.getJobDefinitionForActivityId(activityId);
    assertNotNull("Expected that a job definition for activity '" + activityId + "' exists before migration", jobDefinitionBefore);

    Job jobBefore = testHelper.snapshotBeforeMigration.getJobForDefinitionId(jobDefinitionBefore.getId());
    assertNotNull("Expected that a timer job for activity '" + activityId + "' exists before migration", jobBefore);
    assertTimerJob(jobBefore);

    for (Job job : testHelper.snapshotAfterMigration.getJobs()) {
      if (jobBefore.getId().equals(job.getId())) {
        fail("Expected job '" + jobBefore.getId() + "' to be removed after migration");
      }
    }
  }

  protected void assertTimerJobsCreated(String... activityIds) {
    for (String activityId : activityIds) {
      assertTimerJobCreated(activityId);
    }
  }

  protected void assertTimerJobCreated(String activityId) {
    JobDefinition jobDefinitionAfter = testHelper.snapshotAfterMigration.getJobDefinitionForActivityId(activityId);
    assertNotNull("Expected that a job definition for activity '" + activityId + "' exists after migration", jobDefinitionAfter);

    Job jobAfter = testHelper.snapshotAfterMigration.getJobForDefinitionId(jobDefinitionAfter.getId());
    assertNotNull("Expected that a timer job for activity '" + activityId + "' exists after migration", jobAfter);
    assertTimerJob(jobAfter);

    for (Job job : testHelper.snapshotBeforeMigration.getJobs()) {
      if (jobAfter.getId().equals(job.getId())) {
        fail("Expected job '" + jobAfter.getId() + "' to be created first after migration");
      }
    }
  }

  protected Job assertTimerJobExists(ProcessInstanceSnapshot snapshot) {
    List<Job> jobs = snapshot.getJobs();
    assertEquals(1, jobs.size());
    Job job = jobs.get(0);
    assertTimerJob(job);
    return job;
  }

  protected void assertTimerJob(Job job) {
    assertEquals("Expected job to be a timer job", TimerEntity.TYPE, ((JobEntity) job).getType());
  }

}
