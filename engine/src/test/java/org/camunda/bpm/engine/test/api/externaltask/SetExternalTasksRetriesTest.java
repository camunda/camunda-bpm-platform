package org.camunda.bpm.engine.test.api.externaltask;

import java.util.ArrayList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
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

  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  protected ManagementService managementService;
  protected ExternalTaskService externalTaskService;

  protected List<String> processInstanceIds;

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    externalTaskService = engineRule.getExternalTaskService();
    managementService = engineRule.getManagementService();
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
    Batch batch = managementService.createBatchQuery().singleResult();
    if (batch != null) {
      managementService.deleteBatch(
          batch.getId(), true);
    }

    HistoricBatch historicBatch = engineRule.getHistoryService().createHistoricBatchQuery().singleResult();
    if (historicBatch != null) {
      engineRule.getHistoryService().deleteHistoricBatch(
          historicBatch.getId());
    }
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

  public void executeSeedAndBatchJobs(Batch batch) {
    Job job = engineRule.getManagementService().createJobQuery().jobDefinitionId(batch.getSeedJobDefinitionId()).singleResult();
    // seed job
    managementService.executeJob(job.getId());

    for (Job pending : managementService.createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).list()) {
      managementService.executeJob(pending.getId());
    }
  }
}
