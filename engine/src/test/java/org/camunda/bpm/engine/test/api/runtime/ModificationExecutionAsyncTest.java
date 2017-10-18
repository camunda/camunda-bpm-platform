package org.camunda.bpm.engine.test.api.runtime;

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.batch.BatchSeedJobHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.bpmn.multiinstance.DelegateEvent;
import org.camunda.bpm.engine.test.bpmn.multiinstance.DelegateExecutionListener;
import org.camunda.bpm.engine.test.util.ClockTestUtil;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class ModificationExecutionAsyncTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(rule);
  protected BatchModificationHelper helper = new BatchModificationHelper(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testRule);

  protected RuntimeService runtimeService;
  protected BpmnModelInstance instance;
  private int defaultBatchJobsPerSeed;
  private int defaultInvocationsPerBatchJob;
  protected static final Date START_DATE = new Date(1457326800000L);

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
  }

  @Before
  public void setClock() {
    ClockUtil.setCurrentTime(START_DATE);
  }

  @Before
  public void createBpmnModelInstance() {
    this.instance = Bpmn.createExecutableProcess("process1")
        .startEvent("start")
        .userTask("user1")
        .sequenceFlowId("seq")
        .userTask("user2")
        .endEvent("end")
        .done();
  }

  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  @Before
  public void storeEngineSettings() {
    ProcessEngineConfigurationImpl configuration = rule.getProcessEngineConfiguration();
    defaultBatchJobsPerSeed = configuration.getBatchJobsPerSeed();
    defaultInvocationsPerBatchJob = configuration.getInvocationsPerBatchJob();
  }

  @After
  public void restoreEngineSettings() {
    ProcessEngineConfigurationImpl configuration = rule.getProcessEngineConfiguration();
    configuration.setBatchJobsPerSeed(defaultBatchJobsPerSeed);
    configuration.setInvocationsPerBatchJob(defaultInvocationsPerBatchJob);
  }

  @After
  public void removeInstanceIds() {
    helper.currentProcessInstances = new ArrayList<String>();
  }

  @After
  public void removeBatches() {
    helper.removeAllRunningAndHistoricBatches();
  }

  @Test
  public void createBatchModification() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    List<String> processInstanceIds = helper.startInstances("process1", 2);

    Batch batch = runtimeService.createModification(processDefinition.getId()).startAfterActivity("user2").processInstanceIds(processInstanceIds).executeAsync();

    assertBatchCreated(batch, 2);
  }

  @Test
  public void createModificationWithNullProcessInstanceIdsListAsync() {

    try {
      runtimeService.createModification("processDefinitionId").startAfterActivity("user1").processInstanceIds((List<String>) null).executeAsync();
      fail("Should not succeed");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("Process instance ids is empty"));
    }
  }

  @Test
  public void createModificationWithNullProcessDefinitionId() {
    try {
      runtimeService.createModification(null).cancelAllForActivity("activityId").processInstanceIds(Arrays.asList("20", "1--0")).executeAsync();
      fail("Should not succed");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("processDefinitionId is null"));
    }
  }


  @Test
  public void createModificationUsingProcessInstanceIdsListWithNullValueAsync() {

    try {
      runtimeService.createModification("processDefinitionId").startAfterActivity("user1").processInstanceIds(Arrays.asList("foo", null, "bar")).executeAsync();
      fail("Should not succeed");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("Process instance ids contains null value"));
    }
  }

  @Test
  public void createModificationWithEmptyProcessInstanceIdsListAsync() {
    try {
      runtimeService.createModification("processDefinitionId").startAfterActivity("user1").processInstanceIds(Collections.<String> emptyList()).executeAsync();
      fail("Should not succeed");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("Process instance ids is empty"));
    }
  }

  @Test
  public void createModificationWithNullProcessInstanceIdsArrayAsync() {

    try {
      runtimeService.createModification("processDefinitionId").startAfterActivity("user1").processInstanceIds((String[]) null).executeAsync();
      fail("Should not be able to modify");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.containsString("Process instance ids is empty"));
    }
  }

  @Test
  public void createModificationUsingProcessInstanceIdsArrayWithNullValueAsync() {

    try {
      runtimeService.createModification("processDefinitionId").cancelAllForActivity("user1").processInstanceIds("foo", null, "bar").executeAsync();
      fail("Should not be able to modify");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("Process instance ids contains null value"));
    }
  }

  @Test
  public void testNullProcessInstanceQueryAsync() {

    try {
      runtimeService.createModification("processDefinitionId").startAfterActivity("user1").processInstanceQuery(null).executeAsync();
      fail("Should not succeed");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("Process instance ids is empty"));
    }
  }

  @Test
  public void createModificationWithNonExistingProcessDefinitionId() {
    DeploymentWithDefinitions deployment = testRule.deploy(instance);
    deployment.getDeployedProcessDefinitions().get(0);

    List<String> processInstanceIds = helper.startInstances("process1", 2);
    try {
      runtimeService.createModification("foo").cancelAllForActivity("activityId").processInstanceIds(processInstanceIds).executeAsync();
      fail("Should not succed");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("processDefinition is null"));
    }
  }

  @Test
  public void createSeedJob() {
    // when
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startAfterAsync("process1", 3, "user1", processDefinition.getId());

    // then there exists a seed job definition with the batch id as
    // configuration
    JobDefinition seedJobDefinition = helper.getSeedJobDefinition(batch);
    assertNotNull(seedJobDefinition);
    assertEquals(batch.getId(), seedJobDefinition.getJobConfiguration());
    assertEquals(BatchSeedJobHandler.TYPE, seedJobDefinition.getJobType());

    // and there exists a modification job definition
    JobDefinition modificationJobDefinition = helper.getExecutionJobDefinition(batch);
    assertNotNull(modificationJobDefinition);
    assertEquals(Batch.TYPE_PROCESS_INSTANCE_MODIFICATION, modificationJobDefinition.getJobType());

    // and a seed job with no relation to a process or execution etc.
    Job seedJob = helper.getSeedJob(batch);
    assertNotNull(seedJob);
    assertEquals(seedJobDefinition.getId(), seedJob.getJobDefinitionId());
    assertNull(seedJob.getDuedate());
    assertNull(seedJob.getDeploymentId());
    assertNull(seedJob.getProcessDefinitionId());
    assertNull(seedJob.getProcessDefinitionKey());
    assertNull(seedJob.getProcessInstanceId());
    assertNull(seedJob.getExecutionId());

    // but no modification jobs where created
    List<Job> modificationJobs = helper.getExecutionJobs(batch);
    assertEquals(0, modificationJobs.size());
  }

  @Test
  public void createModificationJobs() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    rule.getProcessEngineConfiguration().setBatchJobsPerSeed(10);
    Batch batch = helper.startAfterAsync("process1", 20, "user1", processDefinition.getId());
    JobDefinition seedJobDefinition = helper.getSeedJobDefinition(batch);
    JobDefinition modificationJobDefinition = helper.getExecutionJobDefinition(batch);;

    helper.executeSeedJob(batch);

    List<Job> modificationJobs = helper.getJobsForDefinition(modificationJobDefinition);
    assertEquals(10, modificationJobs.size());

    for (Job modificationJob : modificationJobs) {
      assertEquals(modificationJobDefinition.getId(), modificationJob.getJobDefinitionId());
      assertNull(modificationJob.getDuedate());
      assertNull(modificationJob.getProcessDefinitionId());
      assertNull(modificationJob.getProcessDefinitionKey());
      assertNull(modificationJob.getProcessInstanceId());
      assertNull(modificationJob.getExecutionId());
    }

    // and the seed job still exists
    Job seedJob = helper.getJobForDefinition(seedJobDefinition);
    assertNotNull(seedJob);
  }

  @Test
  public void createMonitorJob() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startAfterAsync("process1", 10, "user1", processDefinition.getId());

    // when
    helper.executeSeedJob(batch);

    // then the seed job definition still exists but the seed job is removed
    JobDefinition seedJobDefinition = helper.getSeedJobDefinition(batch);
    assertNotNull(seedJobDefinition);

    Job seedJob = helper.getSeedJob(batch);
    assertNull(seedJob);

    // and a monitor job definition and job exists
    JobDefinition monitorJobDefinition = helper.getMonitorJobDefinition(batch);
    assertNotNull(monitorJobDefinition);

    Job monitorJob = helper.getMonitorJob(batch);
    assertNotNull(monitorJob);
  }

  @Test
  public void executeModificationJobsForStartAfter() {
    DeploymentWithDefinitions deployment = testRule.deploy(instance);
    ProcessDefinition processDefinition = deployment.getDeployedProcessDefinitions().get(0);

    Batch batch = helper.startAfterAsync("process1", 10, "user1", processDefinition.getId());
    helper.executeSeedJob(batch);
    List<Job> modificationJobs = helper.getExecutionJobs(batch);

    // when
    for (Job modificationJob : modificationJobs) {
      helper.executeJob(modificationJob);
    }

    // then all process instances where modified
    for (String processInstanceId : helper.currentProcessInstances) {
      ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
      assertNotNull(updatedTree);
      assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

      assertThat(updatedTree).hasStructure(
          describeActivityInstanceTree(
              processDefinition.getId())
          .activity("user1")
          .activity("user2")
          .done());
    }

    // and the no modification jobs exist
    assertEquals(0, helper.getExecutionJobs(batch).size());

    // but a monitor job exists
    assertNotNull(helper.getMonitorJob(batch));
  }

  @Test
  public void executeModificationJobsForStartBefore() {
    DeploymentWithDefinitions deployment = testRule.deploy(instance);
    ProcessDefinition processDefinition = deployment.getDeployedProcessDefinitions().get(0);

    Batch batch = helper.startBeforeAsync("process1", 10, "user2", processDefinition.getId());
    helper.executeSeedJob(batch);
    List<Job> modificationJobs = helper.getExecutionJobs(batch);

    // when
    for (Job modificationJob : modificationJobs) {
      helper.executeJob(modificationJob);
    }

    // then all process instances where modified
    for (String processInstanceId : helper.currentProcessInstances) {
      ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
      assertNotNull(updatedTree);
      assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

      assertThat(updatedTree).hasStructure(
          describeActivityInstanceTree(
              processDefinition.getId())
          .activity("user1")
          .activity("user2")
          .done());
    }

    // and the no modification jobs exist
    assertEquals(0, helper.getExecutionJobs(batch).size());

    // but a monitor job exists
    assertNotNull(helper.getMonitorJob(batch));
  }

  @Test
  public void executeModificationJobsForStartTransition() {
    DeploymentWithDefinitions deployment = testRule.deploy(instance);
    ProcessDefinition processDefinition = deployment.getDeployedProcessDefinitions().get(0);

    Batch batch = helper.startTransitionAsync("process1", 10, "seq", processDefinition.getId());
    helper.executeSeedJob(batch);
    List<Job> modificationJobs = helper.getExecutionJobs(batch);

    // when
    for (Job modificationJob : modificationJobs) {
      helper.executeJob(modificationJob);
    }

    // then all process instances where modified
    for (String processInstanceId : helper.currentProcessInstances) {
      ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
      assertNotNull(updatedTree);
      assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

      assertThat(updatedTree).hasStructure(
          describeActivityInstanceTree(
              processDefinition.getId())
          .activity("user1")
          .activity("user2")
          .done());
    }

    // and the no modification jobs exist
    assertEquals(0, helper.getExecutionJobs(batch).size());

    // but a monitor job exists
    assertNotNull(helper.getMonitorJob(batch));
  }

  @Test
  public void executeModificationJobsForCancelAll() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.cancelAllAsync("process1", 10, "user1", processDefinition.getId());
    helper.executeSeedJob(batch);
    List<Job> modificationJobs = helper.getExecutionJobs(batch);

    // when
    for (Job modificationJob : modificationJobs) {
      helper.executeJob(modificationJob);
    }

    // then all process instances where modified
    for (String processInstanceId : helper.currentProcessInstances) {
      ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
      assertNull(updatedTree);
    }

    // and the no modification jobs exist
    assertEquals(0, helper.getExecutionJobs(batch).size());

    // but a monitor job exists
    assertNotNull(helper.getMonitorJob(batch));
  }

  @Test
  public void executeModificationJobsForStartAfterAndCancelAll() {
    DeploymentWithDefinitions deployment = testRule.deploy(instance);
    ProcessDefinition processDefinition = deployment.getDeployedProcessDefinitions().get(0);
    List<String> instances = helper.startInstances("process1", 10);

    Batch batch = runtimeService
        .createModification(processDefinition.getId())
        .startAfterActivity("user1")
        .cancelAllForActivity("user1")
        .processInstanceIds(instances)
        .executeAsync();

    helper.executeSeedJob(batch);
    List<Job> modificationJobs = helper.getExecutionJobs(batch);

    // when
    for (Job modificationJob : modificationJobs) {
      helper.executeJob(modificationJob);
    }

    // then all process instances where modified
    for (String processInstanceId : helper.currentProcessInstances) {
      ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
      assertNotNull(updatedTree);
      assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

      assertThat(updatedTree).hasStructure(
          describeActivityInstanceTree(
              processDefinition.getId())
          .activity("user2")
          .done());
    }

    // and the no modification jobs exist
    assertEquals(0, helper.getExecutionJobs(batch).size());

    // but a monitor job exists
    assertNotNull(helper.getMonitorJob(batch));
  }

  @Test
  public void executeModificationJobsForStartBeforeAndCancelAll() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    List<String> instances = helper.startInstances("process1", 10);

    Batch batch = runtimeService
        .createModification(processDefinition.getId())
        .startBeforeActivity("user1")
        .cancelAllForActivity("user1")
        .processInstanceIds(instances)
        .executeAsync();

    helper.executeSeedJob(batch);
    List<Job> modificationJobs = helper.getExecutionJobs(batch);

    // when
    for (Job modificationJob : modificationJobs) {
      helper.executeJob(modificationJob);
    }

    // then all process instances where modified
    for (String processInstanceId : helper.currentProcessInstances) {
      ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
      assertNull(updatedTree);
    }

    // and the no modification jobs exist
    assertEquals(0, helper.getExecutionJobs(batch).size());

    // but a monitor job exists
    assertNotNull(helper.getMonitorJob(batch));
  }

  @Test
  public void executeModificationJobsForStartTransitionAndCancelAll() {
    DeploymentWithDefinitions deployment = testRule.deploy(instance);
    ProcessDefinition processDefinition = deployment.getDeployedProcessDefinitions().get(0);

    List<String> instances = helper.startInstances("process1", 10);

    Batch batch = runtimeService
        .createModification(processDefinition.getId())
        .startTransition("seq")
        .cancelAllForActivity("user1")
        .processInstanceIds(instances)
        .executeAsync();

    helper.executeSeedJob(batch);
    List<Job> modificationJobs = helper.getExecutionJobs(batch);

    // when
    for (Job modificationJob : modificationJobs) {
      helper.executeJob(modificationJob);
    }

    // then all process instances where modified
    for (String processInstanceId : helper.currentProcessInstances) {
      ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
      assertNotNull(updatedTree);
      assertThat(updatedTree).hasStructure(
          describeActivityInstanceTree(
              processDefinition.getId())
          .activity("user2")
          .done());
    }

    // and the no modification jobs exist
    assertEquals(0, helper.getExecutionJobs(batch).size());

    // but a monitor job exists
    assertNotNull(helper.getMonitorJob(batch));
  }

  @Test
  public void executeModificationJobsForProcessInstancesWithDifferentStates() {

    DeploymentWithDefinitions deployment = testRule.deploy(instance);
    ProcessDefinition processDefinition = deployment.getDeployedProcessDefinitions().get(0);

    List<String> processInstanceIds = helper.startInstances("process1", 1);
    Task task = rule.getTaskService().createTaskQuery().singleResult();
    rule.getTaskService().complete(task.getId());

    List<String> anotherProcessInstanceIds = helper.startInstances("process1", 1);
    processInstanceIds.addAll(anotherProcessInstanceIds);

    Batch batch = runtimeService.createModification(processDefinition.getId()).startBeforeActivity("user2").processInstanceIds(processInstanceIds).executeAsync();

    helper.executeSeedJob(batch);
    List<Job> modificationJobs = helper.getExecutionJobs(batch);

    // when
    for (Job modificationJob : modificationJobs) {
      helper.executeJob(modificationJob);
    }

    // then all process instances where modified
    ActivityInstance updatedTree = null;
    String processInstanceId = processInstanceIds.get(0);
    updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(processDefinition.getId()).activity("user2").activity("user2").done());

    processInstanceId = processInstanceIds.get(1);
    updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(processDefinition.getId()).activity("user1").activity("user2").done());

    // and the no modification jobs exist
    assertEquals(0, helper.getExecutionJobs(batch).size());

    // but a monitor job exists
    assertNotNull(helper.getMonitorJob(batch));
  }

  @Test
  public void testMonitorJobPollingForCompletion() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startAfterAsync("process1", 3, "user1", processDefinition.getId());

    // when the seed job creates the monitor job
    Date createDate = ClockTestUtil.setClockToDateWithoutMilliseconds();
    helper.executeSeedJob(batch);

    // then the monitor job has a no due date set
    Job monitorJob = helper.getMonitorJob(batch);
    assertNotNull(monitorJob);
    assertNull(monitorJob.getDuedate());

    // when the monitor job is executed
    helper.executeMonitorJob(batch);

    // then the monitor job has a due date of the default batch poll time
    monitorJob = helper.getMonitorJob(batch);
    Date dueDate = helper.addSeconds(createDate, 30);
    assertEquals(dueDate, monitorJob.getDuedate());
  }

  @Test
  public void testMonitorJobRemovesBatchAfterCompletion() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startBeforeAsync("process1", 10, "user2", processDefinition.getId());
    helper.executeSeedJob(batch);
    helper.executeJobs(batch);

    // when
    helper.executeMonitorJob(batch);

    // then the batch was completed and removed
    assertEquals(0, rule.getManagementService().createBatchQuery().count());

    // and the seed jobs was removed
    assertEquals(0, rule.getManagementService().createJobQuery().count());
  }

  @Test
  public void testBatchDeletionWithCascade() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startTransitionAsync("process1", 10, "seq", processDefinition.getId());
    helper.executeSeedJob(batch);

    // when
    rule.getManagementService().deleteBatch(batch.getId(), true);

    // then the batch was deleted
    assertEquals(0, rule.getManagementService().createBatchQuery().count());

    // and the seed and modification job definition were deleted
    assertEquals(0, rule.getManagementService().createJobDefinitionQuery().count());

    // and the seed job and modification jobs were deleted
    assertEquals(0, rule.getManagementService().createJobQuery().count());
  }

  @Test
  public void testBatchDeletionWithoutCascade() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startBeforeAsync("process1", 10, "user2", processDefinition.getId());
    helper.executeSeedJob(batch);

    // when
    rule.getManagementService().deleteBatch(batch.getId(), false);

    // then the batch was deleted
    assertEquals(0, rule.getManagementService().createBatchQuery().count());

    // and the seed and modification job definition were deleted
    assertEquals(0, rule.getManagementService().createJobDefinitionQuery().count());

    // and the seed job and modification jobs were deleted
    assertEquals(0, rule.getManagementService().createJobQuery().count());
  }

  @Test
  public void testBatchWithFailedSeedJobDeletionWithCascade() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.cancelAllAsync("process1", 2, "user1", processDefinition.getId());

    // create incident
    Job seedJob = helper.getSeedJob(batch);
    rule.getManagementService().setJobRetries(seedJob.getId(), 0);

    // when
    rule.getManagementService().deleteBatch(batch.getId(), true);

    // then the no historic incidents exists
    long historicIncidents = rule.getHistoryService().createHistoricIncidentQuery().count();
    assertEquals(0, historicIncidents);
  }

  @Test
  public void testBatchWithFailedModificationJobDeletionWithCascade() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startAfterAsync("process1", 2, "user1", processDefinition.getId());
    helper.executeSeedJob(batch);

    // create incidents
    List<Job> modificationJobs = helper.getExecutionJobs(batch);
    for (Job modificationJob : modificationJobs) {
      rule.getManagementService().setJobRetries(modificationJob.getId(), 0);
    }

    // when
    rule.getManagementService().deleteBatch(batch.getId(), true);

    // then the no historic incidents exists
    long historicIncidents = rule.getHistoryService().createHistoricIncidentQuery().count();
    assertEquals(0, historicIncidents);
  }

  @Test
  public void testBatchWithFailedMonitorJobDeletionWithCascade() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startBeforeAsync("process1", 2, "user2", processDefinition.getId());
    helper.executeSeedJob(batch);

    // create incident
    Job monitorJob = helper.getMonitorJob(batch);
    rule.getManagementService().setJobRetries(monitorJob.getId(), 0);

    // when
    rule.getManagementService().deleteBatch(batch.getId(), true);

    // then the no historic incidents exists
    long historicIncidents = rule.getHistoryService().createHistoricIncidentQuery().count();
    assertEquals(0, historicIncidents);
  }

  @Test
  public void testModificationJobsExecutionByJobExecutorWithAuthorizationEnabledAndTenant() {
    ProcessEngineConfigurationImpl processEngineConfiguration = rule.getProcessEngineConfiguration();

    processEngineConfiguration.setAuthorizationEnabled(true);
    ProcessDefinition processDefinition = testRule.deployForTenantAndGetDefinition("tenantId", instance);

    try {
      Batch batch = helper.startAfterAsync("process1", 10, "user1", processDefinition.getId());
      helper.executeSeedJob(batch);

      testRule.waitForJobExecutorToProcessAllJobs();

      // then all process instances where modified
      for (String processInstanceId : helper.currentProcessInstances) {
        ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
        assertNotNull(updatedTree);
        assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

        assertThat(updatedTree).hasStructure(
            describeActivityInstanceTree(
                processDefinition.getId())
            .activity("user1")
            .activity("user2")
            .done());
      }

    } finally {
      processEngineConfiguration.setAuthorizationEnabled(false);
    }

  }

  @Test
  public void testBatchExecutionFailureWithMissingProcessInstance() {
    DeploymentWithDefinitions deployment = testRule.deploy(instance);
    ProcessDefinition processDefinition = deployment.getDeployedProcessDefinitions().get(0);
    Batch batch = helper.startAfterAsync("process1", 2, "user1", processDefinition.getId());
    helper.executeSeedJob(batch);

    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    String deletedProcessInstanceId = processInstances.get(0).getId();

    // when
    runtimeService.deleteProcessInstance(deletedProcessInstanceId, "test");
    helper.executeJobs(batch);

    // then the remaining process instance was modified
    for (String processInstanceId : helper.currentProcessInstances) {
      if (processInstanceId.equals(helper.currentProcessInstances.get(0))) {
        ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
        assertNull(updatedTree);
        continue;
      }

      ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
      assertNotNull(updatedTree);
      assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

      assertThat(updatedTree).hasStructure(
          describeActivityInstanceTree(
              processDefinition.getId())
          .activity("user1")
          .activity("user2")
          .done());
    }

    // and one batch job failed and has 2 retries left
    List<Job> modificationJobs = helper.getExecutionJobs(batch);
    assertEquals(1, modificationJobs.size());

    Job failedJob = modificationJobs.get(0);
    assertEquals(2, failedJob.getRetries());
    assertThat(failedJob.getExceptionMessage(), startsWith("ENGINE-13036"));
    assertThat(failedJob.getExceptionMessage(), containsString("Process instance '" + deletedProcessInstanceId + "' cannot be modified"));
  }

  @Test
  public void testBatchCreationWithProcessInstanceQuery() {
    int processInstanceCount = 15;
    DeploymentWithDefinitions deployment = testRule.deploy(instance);
    ProcessDefinition processDefinition = deployment.getDeployedProcessDefinitions().get(0);
    helper.startInstances("process1", 15);

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition.getId());
    assertEquals(processInstanceCount, processInstanceQuery.count());

    // when
    Batch batch = runtimeService
      .createModification(processDefinition.getId())
      .startAfterActivity("user1")
      .processInstanceQuery(processInstanceQuery)
      .executeAsync();

    // then a batch is created
    assertBatchCreated(batch, processInstanceCount);
  }

  @Test
  public void testBatchCreationWithOverlappingProcessInstanceIdsAndQuery() {
    int processInstanceCount = 15;
    DeploymentWithDefinitions deployment = testRule.deploy(instance);
    ProcessDefinition processDefinition = deployment.getDeployedProcessDefinitions().get(0);
    List<String> processInstanceIds = helper.startInstances("process1", 15);

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition.getId());
    assertEquals(processInstanceCount, processInstanceQuery.count());

    // when
    Batch batch = runtimeService
      .createModification(processDefinition.getId())
      .startTransition("seq")
      .processInstanceIds(processInstanceIds)
      .processInstanceQuery(processInstanceQuery)
      .executeAsync();

    // then a batch is created
    assertBatchCreated(batch, processInstanceCount);
  }

  @Test
  public void testListenerInvocation() {
    // given
    DelegateEvent.clearEvents();
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(modify(instance)
        .activityBuilder("user2")
        .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, DelegateExecutionListener.class.getName())
        .done()
      );

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    Batch batch = runtimeService
      .createModification(processDefinition.getId())
      .startBeforeActivity("user2")
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .executeAsync();

    helper.executeSeedJob(batch);

    // when
    helper.executeJobs(batch);

    // then
    List<DelegateEvent> recordedEvents = DelegateEvent.getEvents();
    assertEquals(1, recordedEvents.size());

    DelegateEvent event = recordedEvents.get(0);
    assertEquals(processDefinition.getId(), event.getProcessDefinitionId());
    assertEquals("user2", event.getCurrentActivityId());

    DelegateEvent.clearEvents();
  }

  @Test
  public void testSkipListenerInvocationF() {
    // given
    DelegateEvent.clearEvents();
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(modify(instance)
        .activityBuilder("user2")
        .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, DelegateExecutionListener.class.getName())
        .done());

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    Batch batch = runtimeService
      .createModification(processDefinition.getId())
      .cancelAllForActivity("user2")
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .skipCustomListeners()
      .executeAsync();

    helper.executeSeedJob(batch);

    // when
    helper.executeJobs(batch);

    // then
    assertEquals(0, DelegateEvent.getEvents().size());
  }

  @Test
  public void testIoMappingInvocation() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(modify(instance)
      .activityBuilder("user1")
      .camundaInputParameter("foo", "bar")
      .done()
    );

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    Batch batch = runtimeService
      .createModification(processDefinition.getId())
      .startAfterActivity("user2")
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .executeAsync();

    helper.executeSeedJob(batch);

    // when
    helper.executeJobs(batch);

    // then
    VariableInstance inputVariable = runtimeService.createVariableInstanceQuery().singleResult();
    Assert.assertNotNull(inputVariable);
    assertEquals("foo", inputVariable.getName());
    assertEquals("bar", inputVariable.getValue());

    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstance.getId());
    assertEquals(activityInstance.getActivityInstances("user1")[0].getId(), inputVariable.getActivityInstanceId());
  }

  @Test
  public void testSkipIoMappingInvocation() {
    // given

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(modify(instance)
        .activityBuilder("user2")
        .camundaInputParameter("foo", "bar")
        .done());


    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    Batch batch = runtimeService
      .createModification(processDefinition.getId())
      .startBeforeActivity("user2")
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .skipIoMappings()
      .executeAsync();

    helper.executeSeedJob(batch);

    // when
    helper.executeJobs(batch);

    // then
    assertEquals(0, runtimeService.createVariableInstanceQuery().count());
  }

  @Test
  public void testCancelWithoutFlag() {
    // given
    this.instance = Bpmn.createExecutableProcess("process1")
        .startEvent("start")
        .serviceTask("ser").camundaExpression("${true}")
        .userTask("user")
        .endEvent("end")
        .done();

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);

    List<String> processInstanceIds = helper.startInstances("process1", 1);

    // when
    Batch batch = runtimeService.createModification(processDefinition.getId())
      .startBeforeActivity("ser")
      .cancelAllForActivity("user")
      .processInstanceIds(processInstanceIds)
      .executeAsync();

    helper.executeSeedJob(batch);
    helper.executeJobs(batch);

    // then
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }

  @Test
  public void testCancelWithoutFlag2() {
    // given
    this.instance = Bpmn.createExecutableProcess("process1")
        .startEvent("start")
        .serviceTask("ser").camundaExpression("${true}")
        .userTask("user")
        .endEvent("end")
        .done();

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);

    List<String> processInstanceIds = helper.startInstances("process1", 1);

    // when
    Batch batch = runtimeService.createModification(processDefinition.getId())
      .startBeforeActivity("ser")
      .cancelAllForActivity("user", false)
      .processInstanceIds(processInstanceIds)
      .executeAsync();

    helper.executeSeedJob(batch);
    helper.executeJobs(batch);

    // then
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }

  @Test
  public void testCancelWithFlag() {
    // given
    this.instance = Bpmn.createExecutableProcess("process1")
        .startEvent("start")
        .serviceTask("ser").camundaExpression("${true}")
        .userTask("user")
        .endEvent("end")
        .done();

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);

    List<String> processInstanceIds = helper.startInstances("process1", 1);

    // when
    Batch batch = runtimeService.createModification(processDefinition.getId())
      .startBeforeActivity("ser")
      .cancelAllForActivity("user", true)
      .processInstanceIds(processInstanceIds)
      .executeAsync();

    helper.executeSeedJob(batch);
    helper.executeJobs(batch);

    // then
    ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().singleResult();
    assertNotNull(execution);
    assertEquals("user", execution.getActivityId());
  }

  @Test
  public void testCancelWithFlagForManyInstances() {
    // given
    this.instance = Bpmn.createExecutableProcess("process1")
        .startEvent("start")
        .serviceTask("ser").camundaExpression("${true}")
        .userTask("user")
        .endEvent("end")
        .done();

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);

    List<String> processInstanceIds = helper.startInstances("process1", 10);

    // when
    Batch batch = runtimeService.createModification(processDefinition.getId())
      .startBeforeActivity("ser")
      .cancelAllForActivity("user", true)
      .processInstanceIds(processInstanceIds)
      .executeAsync();

    helper.executeSeedJob(batch);
    helper.executeJobs(batch);

    // then
    for (String processInstanceId : processInstanceIds) {
      Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).singleResult();
      assertNotNull(execution);
      assertEquals("user", ((ExecutionEntity) execution).getActivityId());
    }
  }

  protected void assertBatchCreated(Batch batch, int processInstanceCount) {
    assertNotNull(batch);
    assertNotNull(batch.getId());
    assertEquals("instance-modification", batch.getType());
    assertEquals(processInstanceCount, batch.getTotalJobs());
    assertEquals(defaultBatchJobsPerSeed, batch.getBatchJobsPerSeed());
    assertEquals(defaultInvocationsPerBatchJob, batch.getInvocationsPerBatchJob());
  }

}
