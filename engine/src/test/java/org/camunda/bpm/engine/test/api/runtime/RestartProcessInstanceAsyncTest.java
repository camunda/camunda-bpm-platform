package org.camunda.bpm.engine.test.api.runtime;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ClockTestUtil;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * 
 * @author Anna Pazola
 *
 */
public class RestartProcessInstanceAsyncTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  protected BatchRestartHelper helper = new BatchRestartHelper(engineRule);
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected ManagementService managementService;

  protected BpmnModelInstance instance = Bpmn.createExecutableProcess("process").startEvent().sequenceFlowId("seq").userTask("userTask").endEvent().done();
  private int defaultBatchJobsPerSeed;
  private int defaultInvocationsPerBatchJob;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();
  }

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @After
  public void removeAllRunningAndHistoricBatches() {

    for (Batch batch : managementService.createBatchQuery().list()) {
      managementService.deleteBatch(batch.getId(), true);
    }

    // remove history of completed batches
    for (HistoricBatch historicBatch : historyService.createHistoricBatchQuery().list()) {
      historyService.deleteHistoricBatch(historicBatch.getId());
    }
  }
  
  @Before
  public void storeEngineSettings() {
    ProcessEngineConfigurationImpl configuration = engineRule.getProcessEngineConfiguration();
    defaultBatchJobsPerSeed = configuration.getBatchJobsPerSeed();
    defaultInvocationsPerBatchJob = configuration.getInvocationsPerBatchJob();
  }

  @After
  public void restoreEngineSettings() {
    ProcessEngineConfigurationImpl configuration = engineRule.getProcessEngineConfiguration();
    configuration.setBatchJobsPerSeed(defaultBatchJobsPerSeed);
    configuration.setInvocationsPerBatchJob(defaultInvocationsPerBatchJob);
  }

  @Test
  public void createBatchRestart() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process");
    
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    List<String> processInstanceIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());

    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
        .startAfterActivity("user2")
        .processInstanceIds(processInstanceIds)
        .executeAsync();

    assertBatchCreated(batch, 2);
  }

  @Test
  public void restartProcessInstanceWithNullProcessDefinitionId() {
    try {
      runtimeService.restartProcessInstances(null)
      .executeAsync();
      fail("exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("processDefinitionId is null"));
    }
  }
  
  @Test
  public void restartProcessInstanceWithoutProcessInstanceIds() {
    try {
      runtimeService.restartProcessInstances("foo").startAfterActivity("bar").executeAsync();
      fail("exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("processInstanceIds is empty"));
    }
  }

  @Test
  public void restartProcessInstanceWithNullProcessInstanceId() {
    try {
      runtimeService.restartProcessInstances("foo")
      .startAfterActivity("bar")
      .processInstanceIds((String) null)
      .executeAsync();
      fail("exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("processInstanceIds contains null value"));
    }
  }
  
  @Test
  public void restartNotExistingProcessInstance() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance); 
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
        .startBeforeActivity("bar")
        .processInstanceIds("aaa")
        .executeAsync();
    helper.executeSeedJob(batch);
    try {
      helper.executeJobs(batch);
      fail("exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("the historic process instance cannot be found"));
    }
  }

  @Test
  public void shouldRestartProcessInstance() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process");
    Task task1 = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).active().singleResult();
    Task task2 = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).active().singleResult();
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
        .startBeforeActivity("userTask")
        .processInstanceIds(processInstance1.getId(),processInstance2.getId())
        .executeAsync();
    helper.completeBatch(batch);
    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().active().list();
    ProcessInstance restartedProcessInstance = restartedProcessInstances.get(0);
    Task restartedTask = engineRule.getTaskService().createTaskQuery().processInstanceId(restartedProcessInstance.getId()).active().singleResult();
    Assert.assertEquals(task1.getTaskDefinitionKey(), restartedTask.getTaskDefinitionKey());
    
    restartedProcessInstance = restartedProcessInstances.get(1);
    restartedTask = engineRule.getTaskService().createTaskQuery().processInstanceId(restartedProcessInstance.getId()).active().singleResult();
    Assert.assertEquals(task2.getTaskDefinitionKey(), restartedTask.getTaskDefinitionKey());
  }

  @Test
  public void shouldRestartProcessInstanceWithParallelGateway() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess("process1")
        .startEvent()
        .parallelGateway("gateway")
        .userTask("userTask1")
        .endEvent()
        .moveToNode("gateway")
        .userTask("userTask2")
        .endEvent()
        .done();

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process1");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process1");
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
        .startBeforeActivity("userTask1")
        .startBeforeActivity("userTask2")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();
    helper.completeBatch(batch);

    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().active().list();
    for (ProcessInstance restartedProcessInstance : restartedProcessInstances) {
      ActivityInstance updatedTree = runtimeService.getActivityInstance(restartedProcessInstance.getId());
      assertNotNull(updatedTree);
      assertEquals(restartedProcessInstance.getId(), updatedTree.getProcessInstanceId());
      assertThat(updatedTree).hasStructure(
          describeActivityInstanceTree(
              processDefinition.getId())
          .activity("userTask1")
          .activity("userTask2")
          .done());
    }
  }

  @Test
  public void shouldRestartProcessInstanceWithSubProcess() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess("process2")
        .startEvent()
        .subProcess("subProcess")
        .embeddedSubProcess()
        .startEvent()
        .userTask("innerUserTask")
        .endEvent()
        .subProcessDone()
        .endEvent()
        .done();

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process2");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process2");
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
        .startBeforeActivity("subProcess")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();
    helper.completeBatch(batch);

    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().active().list();
    for (ProcessInstance restartedProcessInstance : restartedProcessInstances) {
      ActivityInstance updatedTree = runtimeService.getActivityInstance(restartedProcessInstance.getId());
      assertNotNull(updatedTree);
      assertEquals(restartedProcessInstance.getId(), updatedTree.getProcessInstanceId());
      assertThat(updatedTree).hasStructure(
          describeActivityInstanceTree(
              processDefinition.getId())
          .beginScope("subProcess")
          .activity("innerUserTask")
          .done());
    }
  }
  
  @Test
  public void shouldRestartProcessInstanceWithVariables() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process");
    runtimeService.setVariable(processInstance1.getId(), "foo", "bar");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process");
    runtimeService.setVariable(processInstance2.getId(), "aa", "bb");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
        .startBeforeActivity("userTask")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();
    helper.completeBatch(batch);

    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().active().list();
    ProcessInstance restartedProcessInstance = restartedProcessInstances.get(0);
    VariableInstance variableInstance1 = runtimeService.createVariableInstanceQuery().processInstanceIdIn(restartedProcessInstance.getId()).singleResult();
    assertEquals(variableInstance1.getExecutionId(), restartedProcessInstance.getId());

    restartedProcessInstance = restartedProcessInstances.get(1);
    VariableInstance variableInstance2 = runtimeService.createVariableInstanceQuery().processInstanceIdIn(restartedProcessInstance.getId()).singleResult();
    assertEquals(variableInstance2.getExecutionId(), restartedProcessInstance.getId());
    assertFalse(variableInstance1.getName().equals(variableInstance2.getName()));
    assertFalse(variableInstance1.getValue().equals(variableInstance2.getValue()));
  }
  
  @Test
  public void shouldRestartProcessInstanceUsingHistoricProcessInstanceQuery() {

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process");
    Task task1 = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).active().singleResult();

    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process");
    Task task2 = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).active().singleResult();

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    HistoricProcessInstanceQuery historicProcessInstanceQuery = engineRule.getHistoryService().createHistoricProcessInstanceQuery().processDefinitionId(processDefinition.getId());

    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
        .startBeforeActivity("userTask")
        .historicProcessInstanceQuery(historicProcessInstanceQuery)
        .executeAsync();
    helper.completeBatch(batch);

    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().active().list();
    ProcessInstance restartedProcessInstance = restartedProcessInstances.get(0);
    Task restartedTask = taskService.createTaskQuery().processInstanceId(restartedProcessInstance.getId()).active().singleResult();
    Assert.assertEquals(task1.getTaskDefinitionKey(), restartedTask.getTaskDefinitionKey());

    restartedProcessInstance = restartedProcessInstances.get(1);
    restartedTask = taskService.createTaskQuery().processInstanceId(restartedProcessInstance.getId()).active().singleResult();
    Assert.assertEquals(task2.getTaskDefinitionKey(), restartedTask.getTaskDefinitionKey());
  }
  
  @Test
  public void testBatchCreationWithOverlappingProcessInstanceIdsAndQuery() {
    int processInstanceCount = 2;
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process");
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    HistoricProcessInstanceQuery processInstanceQuery = engineRule.getHistoryService().createHistoricProcessInstanceQuery().processDefinitionId(processDefinition.getId());
    assertEquals(processInstanceCount, processInstanceQuery.count());

    // when
    Batch batch = runtimeService
      .restartProcessInstances(processDefinition.getId())
      .startTransition("seq")
      .processInstanceIds(processInstance1.getId(), processInstance2.getId())
      .historicProcessInstanceQuery(processInstanceQuery)
      .executeAsync();

    // then a batch is created
    assertBatchCreated(batch, processInstanceCount);
  }

  @Test
  public void testMonitorJobPollingForCompletion() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process");
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    Batch batch = runtimeService
        .restartProcessInstances(processDefinition.getId())
        .startTransition("seq")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();

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
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process");
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    Batch batch = runtimeService
        .restartProcessInstances(processDefinition.getId())
        .startTransition("seq")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();

    helper.executeSeedJob(batch);
    helper.executeJobs(batch);

    // when
    helper.executeMonitorJob(batch);

    // then the batch was completed and removed
    assertEquals(0, engineRule.getManagementService().createBatchQuery().count());

    // and the seed jobs was removed
    assertEquals(0, engineRule.getManagementService().createJobQuery().count());
  }

  @Test
  public void testBatchDeletionWithCascade() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process");
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    Batch batch = runtimeService
        .restartProcessInstances(processDefinition.getId())
        .startTransition("seq")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();
    helper.executeSeedJob(batch);

    // when
    engineRule.getManagementService().deleteBatch(batch.getId(), true);

    // then the batch was deleted
    assertEquals(0, engineRule.getManagementService().createBatchQuery().count());

    // and the seed and execution job definition were deleted
    assertEquals(0, engineRule.getManagementService().createJobDefinitionQuery().count());

    // and the seed job and execution jobs were deleted
    assertEquals(0, engineRule.getManagementService().createJobQuery().count());
  }

  @Test
  public void testBatchDeletionWithoutCascade() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process");
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    Batch batch = runtimeService
        .restartProcessInstances(processDefinition.getId())
        .startTransition("seq")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();
    
    helper.executeSeedJob(batch);

    // when
    engineRule.getManagementService().deleteBatch(batch.getId(), false);

    // then the batch was deleted
    assertEquals(0, engineRule.getManagementService().createBatchQuery().count());

    // and the seed and execution job definition were deleted
    assertEquals(0, engineRule.getManagementService().createJobDefinitionQuery().count());

    // and the seed job and execution jobs were deleted
    assertEquals(0, engineRule.getManagementService().createJobQuery().count());
  }

  @Test
  public void testBatchWithFailedSeedJobDeletionWithCascade() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process");
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    Batch batch = runtimeService
        .restartProcessInstances(processDefinition.getId())
        .startTransition("seq")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();

    // create incident
    Job seedJob = helper.getSeedJob(batch);
    engineRule.getManagementService().setJobRetries(seedJob.getId(), 0);

    // when
    engineRule.getManagementService().deleteBatch(batch.getId(), true);

    // then the no historic incidents exists
    long historicIncidents = engineRule.getHistoryService().createHistoricIncidentQuery().count();
    assertEquals(0, historicIncidents);
  }

  @Test
  public void testBatchWithFailedExecutionJobDeletionWithCascade() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process");
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    Batch batch = runtimeService
        .restartProcessInstances(processDefinition.getId())
        .startTransition("seq")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();
    helper.executeSeedJob(batch);

    // create incidents
    List<Job> executionJobs = helper.getExecutionJobs(batch);
    for (Job executionJob : executionJobs) {
      engineRule.getManagementService().setJobRetries(executionJob.getId(), 0);
    }

    // when
    engineRule.getManagementService().deleteBatch(batch.getId(), true);

    // then the no historic incidents exists
    long historicIncidents = engineRule.getHistoryService().createHistoricIncidentQuery().count();
    assertEquals(0, historicIncidents);
  }

  @Test
  public void testBatchWithFailedMonitorJobDeletionWithCascade() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process");
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    Batch batch = runtimeService
        .restartProcessInstances(processDefinition.getId())
        .startTransition("seq")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();
    helper.executeSeedJob(batch);

    // create incident
    Job monitorJob = helper.getMonitorJob(batch);
    engineRule.getManagementService().setJobRetries(monitorJob.getId(), 0);

    // when
    engineRule.getManagementService().deleteBatch(batch.getId(), true);

    // then the no historic incidents exists
    long historicIncidents = engineRule.getHistoryService().createHistoricIncidentQuery().count();
    assertEquals(0, historicIncidents);
  }

  @Test
  public void testJobsExecutionByJobExecutorWithAuthorizationEnabledAndTenant() {
    ProcessEngineConfigurationImpl processEngineConfiguration = engineRule.getProcessEngineConfiguration();

    processEngineConfiguration.setAuthorizationEnabled(true);
    ProcessDefinition processDefinition = testRule.deployForTenantAndGetDefinition("tenantId", instance);

    try {
      ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process");
      ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process");
      List<String> list = Arrays.asList(processInstance1.getId(), processInstance2.getId());
      runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
      runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
      Batch batch = runtimeService
          .restartProcessInstances(processDefinition.getId())
          .startTransition("seq")
          .processInstanceIds(list)
          .executeAsync();
      helper.executeSeedJob(batch);

      testRule.waitForJobExecutorToProcessAllJobs();

      List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition.getId()).list();
      // then all process instances where restarted
      for (ProcessInstance restartedProcessInstance : restartedProcessInstances) {
        ActivityInstance updatedTree = runtimeService.getActivityInstance(restartedProcessInstance.getId());
        assertNotNull(updatedTree);
        assertEquals(restartedProcessInstance.getId(), updatedTree.getProcessInstanceId());

        assertThat(updatedTree).hasStructure(
            describeActivityInstanceTree(
                processDefinition.getId())
            .activity("userTask")
            .done());
      }

    } finally {
      processEngineConfiguration.setAuthorizationEnabled(false);
    }

  }
  
  @Test
  public void restartProcessInstanceWithNotMatchingProcessDefinition() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    BpmnModelInstance instance2 = Bpmn.createExecutableProcess().done();
    ProcessDefinition processDefinition2 = testRule.deployAndGetDefinition(instance2);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    Batch batch = runtimeService.restartProcessInstances(processDefinition2.getId()).startBeforeActivity("userTask").processInstanceIds(processInstance.getId()).executeAsync();
    
    try { 
      helper.completeBatch(batch); 
      fail("exception expected");
    } catch (ProcessEngineException e) {
      Assert.assertThat(e.getMessage(), containsString("Its process definition '" + processDefinition.getId() + "' does not match given process definition '" + processDefinition2.getId() +"'" ));
    }
  }

  protected void assertBatchCreated(Batch batch, int processInstanceCount) {
    assertNotNull(batch);
    assertNotNull(batch.getId());
    assertEquals("instance-restart", batch.getType());
    assertEquals(processInstanceCount, batch.getTotalJobs());
    assertEquals(defaultBatchJobsPerSeed, batch.getBatchJobsPerSeed());
    assertEquals(defaultInvocationsPerBatchJob, batch.getInvocationsPerBatchJob());
  }
}
