package org.camunda.bpm.engine.test.api.externaltask;

import java.util.ArrayList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class SetExternalTasksRetriesTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testHelper);

  private static String PROCESS_DEFINITION_KEY = "oneExternalTaskProcess";
  private static String PROCESS_DEFINITION_KEY_2 = "twoExternalTaskWithPriorityProcess";

  private int defaultBatchJobsPerSeed;
  private int defaultInvocationsPerBatchJob;
  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  protected ManagementService managementService;
  protected ExternalTaskService externalTaskService;
  protected HistoryService historyService;

  protected List<String> processInstanceIds;

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    externalTaskService = engineRule.getExternalTaskService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
  }

  @Before
  public void deployTestProcesses() throws Exception {
    org.camunda.bpm.engine.repository.Deployment deployment = engineRule.getRepositoryService().createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
      .addClasspathResource("org/camunda/bpm/engine/test/api/externaltask/externalTaskPriorityExpression.bpmn20.xml")
      .deploy();

    engineRule.manageDeployment(deployment);

    RuntimeService runtimeService = engineRule.getRuntimeService();
    processInstanceIds = new ArrayList<String>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, i + "").getId());
    }
    processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY_2).getId());
  }

  @After
  public void cleanBatch() {
    List<Batch> batches = managementService.createBatchQuery().list();
    if (batches.size() > 0) {
      for (Batch batch : batches) {
        managementService.deleteBatch(batch.getId(), true);
      }
    }

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();
    if (historicBatch != null) {
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
  public void shouldSetExternalTaskRetriesSync() {

    List<ExternalTask> externalTasks = externalTaskService.createExternalTaskQuery().list();
    ArrayList<String> externalTaskIds = new ArrayList<String>();
    for (ExternalTask task : externalTasks) {
      externalTaskIds.add(task.getId());
    }
    // when
    externalTaskService.setRetries(externalTaskIds, 10);

    // then
    externalTasks = externalTaskService.createExternalTaskQuery().list();
    for (ExternalTask task : externalTasks) {
     Assert.assertEquals(10, (int) task.getRetries());
    }
  }

  @Test
  public void shouldFailForNonExistingExternalTaskIdSync() {

    List<ExternalTask> externalTasks = externalTaskService.createExternalTaskQuery().list();
    ArrayList<String> externalTaskIds = new ArrayList<String>();
    for (ExternalTask task : externalTasks) {
      externalTaskIds.add(task.getId());
    }

    externalTaskIds.add("nonExistingExternalTaskId");

    try {
      externalTaskService.setRetries(externalTaskIds, 10);
      fail("exception expected");
    } catch (NotFoundException e) {
      Assert.assertThat(e.getMessage(), containsString("Cannot find external task with id nonExistingExternalTaskId"));
    }
  }

  @Test
  public void shouldFailForNullExternalTaskIdSync() {

    List<ExternalTask> externalTasks = externalTaskService.createExternalTaskQuery().list();
    ArrayList<String> externalTaskIds = new ArrayList<String>();
    for (ExternalTask task : externalTasks) {
      externalTaskIds.add(task.getId());
    }

    externalTaskIds.add(null);

    try {
      externalTaskService.setRetries(externalTaskIds, 10);
      fail("exception expected");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), containsString("External task id cannot be null"));
    }
  }

  @Test
  public void shouldFailForNullExternalTaskIdsSync() {
    try {
      externalTaskService.setRetries((List<String>) null, 10);
      fail("exception expected");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), containsString("externalTaskIds is empty"));
    }
  }

  @Test
  public void shouldFailForNonExistingExternalTaskIdAsync() {

    List<ExternalTask> externalTasks = externalTaskService.createExternalTaskQuery().list();
    ArrayList<String> externalTaskIds = new ArrayList<String>();
    for (ExternalTask task : externalTasks) {
      externalTaskIds.add(task.getId());
    }

    externalTaskIds.add("nonExistingExternalTaskId");
    Batch batch = externalTaskService.setRetriesAsync(externalTaskIds, null, 10);

    try {
      executeSeedAndBatchJobs(batch);
      fail("exception expected");
    } catch (NotFoundException e) {
      Assert.assertThat(e.getMessage(), containsString("Cannot find external task with id nonExistingExternalTaskId"));
    }
  }

  @Test
  public void shouldFailForNullExternalTaskIdAsync() {

    List<ExternalTask> externalTasks = externalTaskService.createExternalTaskQuery().list();
    ArrayList<String> externalTaskIds = new ArrayList<String>();
    for (ExternalTask task : externalTasks) {
      externalTaskIds.add(task.getId());
    }

    externalTaskIds.add(null);
    Batch batch = null;

    try {
      batch = externalTaskService.setRetriesAsync(externalTaskIds, null, 10);
      executeSeedAndBatchJobs(batch);
      fail("exception expected");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), containsString("External task id cannot be null"));
    }
  }

  @Test
  public void shouldFailForNullExternalTaskIdsAsync() {
    try {
      externalTaskService.setRetriesAsync((List<String>) null, null, 10);
      fail("exception expected");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), containsString("externalTaskIds is empty"));
    }
  }

  @Test
  public void shouldFailForNegativeRetriesSync() {

    List<String> externalTaskIds = Arrays.asList("externalTaskId");

    try {
      externalTaskService.setRetries(externalTaskIds, -10);
      fail("exception expected");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), containsString("The number of retries cannot be negative"));
    }
  }

  @Test
  public void shouldFailForNegativeRetriesAsync() {

    List<String> externalTaskIds = Arrays.asList("externalTaskId");

    try {
      Batch batch = externalTaskService.setRetriesAsync(externalTaskIds, null, -10);
      executeSeedAndBatchJobs(batch);
      fail("exception expected");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), containsString("The number of retries cannot be negative"));
    }
  }

  @Test
  public void shouldSetExternalTaskRetriesWithQueryAsync() {

    ExternalTaskQuery externalTaskQuery = engineRule.getExternalTaskService().createExternalTaskQuery();

    // when
    Batch batch = externalTaskService.setRetriesAsync(null, externalTaskQuery, 5);

    // then
    executeSeedAndBatchJobs(batch);

    for (ExternalTask task : externalTaskQuery.list()) {
      Assert.assertEquals(5, (int) task.getRetries());
    }
  }

  @Test
  public void shouldSetExternalTaskRetriesWithListAsync() {

    List<ExternalTask> externalTasks = externalTaskService.createExternalTaskQuery().list();
    ArrayList<String> externalTaskIds = new ArrayList<String>();
    for (ExternalTask task : externalTasks) {
      externalTaskIds.add(task.getId());
    }
    // when
    Batch batch = externalTaskService.setRetriesAsync(externalTaskIds, null, 5);

    // then
    executeSeedAndBatchJobs(batch);

    externalTasks = externalTaskService.createExternalTaskQuery().list();
    for (ExternalTask task : externalTasks) {
      Assert.assertEquals(5, (int) task.getRetries());
    }
  }

  @Test
  public void shouldSetExternalTaskRetriesWithListAndQueryAsync() {

    ExternalTaskQuery externalTaskQuery = externalTaskService.createExternalTaskQuery();
    List<ExternalTask> externalTasks = externalTaskQuery.list();

    ArrayList<String> externalTaskIds = new ArrayList<String>();
    for (ExternalTask task : externalTasks) {
      externalTaskIds.add(task.getId());
    }
    // when
    Batch batch = externalTaskService.setRetriesAsync(externalTaskIds, externalTaskQuery, 5);

    // then
    executeSeedAndBatchJobs(batch);

    externalTasks = externalTaskService.createExternalTaskQuery().list();
    for (ExternalTask task : externalTasks) {
      Assert.assertEquals(5, (int) task.getRetries());
    }
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldSetExternalTaskRetriesWithLargeList() {
    // given
    engineRule.getProcessEngineConfiguration().setBatchJobsPerSeed(1010);
    List<String> processIds = startProcessInstance(PROCESS_DEFINITION_KEY, 1100);

    HistoricProcessInstanceQuery processInstanceQuery = historyService.createHistoricProcessInstanceQuery();

    // when
    Batch batch = externalTaskService.updateRetries()
        .historicProcessInstanceQuery(processInstanceQuery)
        .setAsync(3);

    createAndExecuteSeedJobs(batch.getSeedJobDefinitionId(), 2);
    executeBatchJobs(batch);

    // then no error is thrown
    assertHistoricBatchExists();

    // cleanup
    if (!testHelper.isHistoryLevelNone()) {
      batch = historyService.deleteHistoricProcessInstancesAsync(processIds, null);
      createAndExecuteSeedJobs(batch.getSeedJobDefinitionId(), 2);
      executeBatchJobs(batch);
    }
  }

  @Test
  public void shouldSetExternalTaskRetriesWithDifferentListAndQueryAsync() {
    // given
    ExternalTaskQuery externalTaskQuery = externalTaskService.createExternalTaskQuery().processInstanceId(processInstanceIds.get(0));
    List<ExternalTask> externalTasks = externalTaskService.createExternalTaskQuery().processInstanceId(processInstanceIds.get(processInstanceIds.size()-1)).list();
    ArrayList<String> externalTaskIds = new ArrayList<String>();
    for (ExternalTask task : externalTasks) {
      externalTaskIds.add(task.getId());
    }

    // when
    Batch batch = externalTaskService.setRetriesAsync(externalTaskIds, externalTaskQuery, 8);
    executeSeedAndBatchJobs(batch);

    // then
    ExternalTask task = externalTaskService.createExternalTaskQuery().processInstanceId(processInstanceIds.get(0)).singleResult();
    Assert.assertEquals(8, (int) task.getRetries());
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().processInstanceId(processInstanceIds.get(processInstanceIds.size()-1)).list();
    for (ExternalTask t : tasks) {
      Assert.assertEquals(8, (int) t.getRetries());
    }
  }

  @Test
  public void shouldUpdateRetriesByExternalTaskIds() {
    // given
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().list();
    List<String> externalTaskIds = Arrays.asList(
        tasks.get(0).getId(),
        tasks.get(1).getId(),
        tasks.get(2).getId(),
        tasks.get(3).getId(),
        tasks.get(4).getId(),
        tasks.get(5).getId());

    // when
    Batch batch = externalTaskService.updateRetries().externalTaskIds(externalTaskIds).setAsync(5);
    executeSeedAndBatchJobs(batch);

    // then
    tasks = externalTaskService.createExternalTaskQuery().list();
    assertEquals(6, tasks.size());

    for (ExternalTask task : tasks) {
      assertEquals(5, (int) task.getRetries());
    }
  }

  @Test
  public void shouldUpdateRetriesByExternalTaskIdArray() {
    // given
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().list();
    List<String> externalTaskIds = Arrays.asList(
        tasks.get(0).getId(),
        tasks.get(1).getId(),
        tasks.get(2).getId(),
        tasks.get(3).getId(),
        tasks.get(4).getId(),
        tasks.get(5).getId());

    // when
    Batch batch = externalTaskService.updateRetries().externalTaskIds(externalTaskIds.toArray(new String[externalTaskIds.size()])).setAsync(5);
    executeSeedAndBatchJobs(batch);

    // then
    tasks = externalTaskService.createExternalTaskQuery().list();
    assertEquals(6, tasks.size());

    for (ExternalTask task : tasks) {
      assertEquals(5, (int) task.getRetries());
    }
  }

  @Test
  public void shouldUpdateRetriesByProcessInstanceIds() {
    // when
    Batch batch = externalTaskService.updateRetries().processInstanceIds(processInstanceIds).setAsync(5);
    executeSeedAndBatchJobs(batch);

    // then
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().list();
    assertEquals(6, tasks.size());

    for (ExternalTask task : tasks) {
      assertEquals(5, (int) task.getRetries());
    }
  }

  @Test
  public void shouldUpdateRetriesByProcessInstanceIdArray() {
    // given

    // when
    Batch batch = externalTaskService.updateRetries().processInstanceIds(processInstanceIds.toArray(new String[processInstanceIds.size()])).setAsync(5);
    executeSeedAndBatchJobs(batch);

    // then
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().list();
    assertEquals(6, tasks.size());

    for (ExternalTask task : tasks) {
      assertEquals(5, (int) task.getRetries());
    }
  }

  @Test
  public void shouldUpdateRetriesByExternalTaskQuery() {
    // given
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();

    // when
    Batch batch = externalTaskService.updateRetries().externalTaskQuery(query).setAsync(5);
    executeSeedAndBatchJobs(batch);

    // then
    List<ExternalTask> tasks = query.list();
    assertEquals(6, tasks.size());

    for (ExternalTask task : tasks) {
      assertEquals(5, (int) task.getRetries());
    }
  }

  @Test
  public void shouldUpdateRetriesByProcessInstanceQuery() {
    // given
    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();

    // when
    Batch batch = externalTaskService.updateRetries().processInstanceQuery(processInstanceQuery).setAsync(5);
    executeSeedAndBatchJobs(batch);

    // then
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().list();
    assertEquals(6, tasks.size());

    for (ExternalTask task : tasks) {
      assertEquals(5, (int) task.getRetries());
    }
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void shouldUpdateRetriesByHistoricProcessInstanceQuery() {
    // given
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    Batch batch = externalTaskService.updateRetries().historicProcessInstanceQuery(query).setAsync(5);
    executeSeedAndBatchJobs(batch);

    // then
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().list();
    assertEquals(6, tasks.size());

    for (ExternalTask task : tasks) {
      assertEquals(5, (int) task.getRetries());
    }
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void shouldUpdateRetriesByAllParameters() {
    // given
    ExternalTask externalTask = externalTaskService
        .createExternalTaskQuery()
        .processInstanceId(processInstanceIds.get(0))
        .singleResult();

    ExternalTaskQuery externalTaskQuery = externalTaskService
        .createExternalTaskQuery()
        .processInstanceId(processInstanceIds.get(1));

    ProcessInstanceQuery processInstanceQuery = runtimeService
        .createProcessInstanceQuery()
        .processInstanceId(processInstanceIds.get(2));


    HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceIds.get(3));

    // when
    Batch batch = externalTaskService.updateRetries()
      .externalTaskIds(externalTask.getId())
      .externalTaskQuery(externalTaskQuery)
      .processInstanceQuery(processInstanceQuery)
      .historicProcessInstanceQuery(historicProcessInstanceQuery)
      .processInstanceIds(processInstanceIds.get(4))
      .setAsync(5);
    executeSeedAndBatchJobs(batch);

    // then
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().list();
    assertEquals(6, tasks.size());

    for (ExternalTask task : tasks) {
      assertEquals(Integer.valueOf(5), task.getRetries());
    }
  }

  public void executeSeedAndBatchJobs(Batch batch) {
    Job job = engineRule.getManagementService().createJobQuery().jobDefinitionId(batch.getSeedJobDefinitionId()).singleResult();
    // seed job
    managementService.executeJob(job.getId());

    for (Job pending : managementService.createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).list()) {
      managementService.executeJob(pending.getId());
    }
  }

  protected void assertHistoricBatchExists() {
    if (testHelper.isHistoryLevelFull()) {
      assertEquals(1, historyService.createHistoricBatchQuery().count());
    }
  }

  protected void createAndExecuteSeedJobs(String seedJobDefinitionId, int expectedSeedJobsCount) {
    for (int i = 0; i <= expectedSeedJobsCount; i++) {
      Job seedJob = managementService.createJobQuery().jobDefinitionId(seedJobDefinitionId).singleResult();
      if (i != expectedSeedJobsCount) {
        assertNotNull(seedJob);
        managementService.executeJob(seedJob.getId());
      } else {
        //the last seed job should not trigger another seed job
        assertNull(seedJob);
      }
    }
  }

  /**
   * Execute all batch jobs of batch once and collect exceptions during job execution.
   *
   * @param batch the batch for which the batch jobs should be executed
   * @return the catched exceptions of the batch job executions, is empty if non where thrown
   */
  protected List<Exception> executeBatchJobs(Batch batch) {
    String batchJobDefinitionId = batch.getBatchJobDefinitionId();
    List<Job> batchJobs = managementService.createJobQuery().jobDefinitionId(batchJobDefinitionId).list();
    assertFalse(batchJobs.isEmpty());

    List<Exception> catchedExceptions = new ArrayList<Exception>();

    for (Job batchJob : batchJobs) {
      try {
        managementService.executeJob(batchJob.getId());
      } catch (Exception e) {
        catchedExceptions.add(e);
      }
    }

    return catchedExceptions;
  }

  protected void startTestProcesses() {
    RuntimeService runtimeService = engineRule.getRuntimeService();
    for (int i = 4; i < 1000; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, i + "").getId());
    }

  }

  protected List<String> startProcessInstance(String key, int instances) {
    List<String> ids = new ArrayList<String>();
    for (int i = 0; i < instances; i++) {
      ids.add(runtimeService.startProcessInstanceByKey(key, String.valueOf(i)).getId());
    }
    processInstanceIds.addAll(ids);
    return ids;
  }

}
