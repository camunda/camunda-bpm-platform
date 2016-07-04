package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class MultiTenancyJobCmdsTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";

  protected static final String PROCESS_DEFINITION_KEY = "exceptionInJobExecution";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected ProcessInstance processInstance;

  protected ManagementService managementService;

  protected IdentityService identityService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected static final BpmnModelInstance BPMN_PROCESS = Bpmn.createExecutableProcess("exceptionInJobExecution")
    .startEvent()
     .userTask("aUserTask")
       .boundaryEvent("timerEvent")
         .timerWithDuration("PT4H")
           .serviceTask()
           .camundaExpression("${failing}")
    .endEvent()
    .done();

  BpmnModelInstance BPMN_NO_FAIL_PROCESS = Bpmn.createExecutableProcess("noFail")
    .startEvent()
     .userTask("aUserTask")
       .boundaryEvent("timerEvent")
         .timerWithDuration("PT4H")
    .endEvent()
    .done();

  @Rule
  public ExpectedException thrown= ExpectedException.none();

  @Before
  public void init() {

    managementService = engineRule.getManagementService();

    identityService = engineRule.getIdentityService();

    testRule.deployForTenant(TENANT_ONE, BPMN_PROCESS);
    testRule.deployForTenant(TENANT_ONE, BPMN_NO_FAIL_PROCESS);

    processInstance = engineRule.getRuntimeService()
      .startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
  }

  // set jobRetries
  @Test
  public void testSetJobRetriesWithAuthenticatedTenant() {
    
    Job timerJob = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .singleResult();

    identityService.setAuthentication("aUserId", null,  Arrays.asList(TENANT_ONE));
    managementService.setJobRetries(timerJob.getId(), 5);

    assertEquals(5, managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .singleResult()
      .getRetries());
  }

  @Test
  public void testSetJobRetriesWithNoAuthenticatedTenant() {
    
    Job timerJob = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .singleResult();

    identityService.setAuthentication("aUserId", null);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the job '"+ timerJob.getId() 
      +"' because it belongs to no authenticated tenant.");
    managementService.setJobRetries(timerJob.getId(), 5);

  }

  @Test
  public void testSetJobRetriesWithDisabledTenantCheck() {
    
    Job timerJob = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .singleResult();

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    managementService.setJobRetries(timerJob.getId(), 5);

    // then
    assertEquals(5, managementService.createJobQuery()
    .processInstanceId(processInstance.getId())
    .singleResult()
    .getRetries());
    
  }

  // set Jobretries based on job definition
  @Test
  public void testSetJobRetriesDefinitionWithAuthenticatedTenant() {

    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().list().get(0);
    
    String jobId = selectJobByProcessInstanceId(processInstance.getId()).getId();

    managementService.setJobRetries(jobId, 0);

    identityService.setAuthentication("aUserId", null,  Arrays.asList(TENANT_ONE));
    // sets the retries for failed jobs - That's the reason why job retries are made 0 in the above step
    managementService.setJobRetriesByJobDefinitionId(jobDefinition.getId(), 1);

    // then
    assertEquals(1, selectJobByProcessInstanceId(processInstance.getId())
      .getRetries());
    
  }

  @Test
  public void testSetJobRetriesDefinitionWithNoAuthenticatedTenant() {

    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().list().get(0);
    
    String jobId = selectJobByProcessInstanceId(processInstance.getId()).getId();
   
    managementService.setJobRetries(jobId, 0);
    identityService.setAuthentication("aUserId", null);

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process definition '"
      + jobDefinition.getProcessDefinitionId() + "' because it belongs to no authenticated tenant.");
    // when
    managementService.setJobRetriesByJobDefinitionId(jobDefinition.getId(), 1);
  }

  @Test
  public void testSetJobRetriesDefinitionWithDisabledTenantCheck() {

    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().list().get(0);

    String jobId = selectJobByProcessInstanceId(processInstance.getId()).getId();

    managementService.setJobRetries(jobId, 0);
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
  
    managementService.setJobRetriesByJobDefinitionId(jobDefinition.getId(), 1);
    // then
    assertEquals(1, selectJobByProcessInstanceId(processInstance.getId()).getRetries());
  
  }

  // set JobDueDate
  @Test
  public void testSetJobDueDateWithAuthenticatedTenant() {
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    
    assertEquals(0, managementService.createJobQuery().duedateLowerThan(new Date()).count());

    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.DATE, -3);
    
    identityService.setAuthentication("aUserId", null,  Arrays.asList(TENANT_ONE));
    managementService.setJobDuedate(timerJob.getId(), cal.getTime());

    // then
    assertEquals(1, managementService.createJobQuery()
      .duedateLowerThan(new Date()).count());
  }

  @Test
  public void testSetJobDueDateWithNoAuthenticatedTenant() {
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    
    identityService.setAuthentication("aUserId", null);

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the job '" + timerJob.getId() +"' because it belongs to no authenticated tenant.");
    // when
    managementService.setJobDuedate(timerJob.getId(), new Date());

  }

  @Test
  public void testSetJobDueDateWithDisabledTenantCheck() {
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.DATE, -3);
    
    managementService.setJobDuedate(timerJob.getId(), cal.getTime());
    // then
    assertEquals(1, managementService.createJobQuery()
      .duedateLowerThan(new Date()).count());

  }

  // set jobPriority test cases
  @Test
  public void testSetJobPriorityWithAuthenticatedTenant() {
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    
    identityService.setAuthentication("aUserId", null,  Arrays.asList(TENANT_ONE));
    managementService.setJobPriority(timerJob.getId(), 5);

    // then
    assertEquals(1, managementService.createJobQuery().priorityHigherThanOrEquals(5).count());
  }

  @Test
  public void testSetJobPriorityWithNoAuthenticatedTenant() {
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    
    identityService.setAuthentication("aUserId", null);

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the job '"+ timerJob.getId() + "' because it belongs to no authenticated tenant.");

    // when
    managementService.setJobPriority(timerJob.getId(), 5);
  }

  @Test
  public void testSetJobPriorityWithDisabledTenantCheck() {
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    managementService.setJobPriority(timerJob.getId(), 5);
    // then
    assertEquals(1, managementService.createJobQuery().priorityHigherThanOrEquals(5).count());
  }

  // setOverridingJobPriorityForJobDefinition without cascade
  @Test
  public void testSetOverridingJobPriorityWithAuthenticatedTenant() {
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().list().get(0);
    identityService.setAuthentication("aUserId", null,  Arrays.asList(TENANT_ONE));
    
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 1701);

    // then
    assertThat(managementService.createJobDefinitionQuery()
      .jobDefinitionId(jobDefinition.getId()).singleResult()
      .getOverridingJobPriority(), is(1701L));
  }

  @Test
  public void testSetOverridingJobPriorityWithNoAuthenticatedTenant() {
    JobDefinition jobDefinition = managementService
      .createJobDefinitionQuery()
      .list().get(0);
   
    identityService.setAuthentication("aUserId", null);

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process definition '"
      + jobDefinition.getProcessDefinitionId() +"' because it belongs to no authenticated tenant.");
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 1701);

  }

  @Test
  public void testSetOverridingJobPriorityWithDisabledTenantCheck() {
    JobDefinition jobDefinition = managementService
      .createJobDefinitionQuery()
      .list().get(0);
   
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 1701);
    // then
    assertThat(managementService.createJobDefinitionQuery()
      .jobDefinitionId(jobDefinition.getId()).singleResult()
      .getOverridingJobPriority(), is(1701L));
  }

  // setOverridingJobPriority with cascade
  @Test
  public void testSetOverridingJobPriorityWithCascadeAndAuthenticatedTenant() {
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().list().get(0);
    identityService.setAuthentication("aUserId", null,  Arrays.asList(TENANT_ONE));
    
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 1701, true);

    // then
    assertThat(managementService.createJobDefinitionQuery()
      .jobDefinitionId(jobDefinition.getId()).singleResult()
      .getOverridingJobPriority(), is(1701L));
  }

  @Test
  public void testSetOverridingJobPriorityWithCascadeAndNoAuthenticatedTenant() {
    JobDefinition jobDefinition = managementService
      .createJobDefinitionQuery()
      .list().get(0);

    identityService.setAuthentication("aUserId", null);

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process definition '"
      + jobDefinition.getProcessDefinitionId() +"' because it belongs to no authenticated tenant.");

    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 1701, true);

  }

  @Test
  public void testSetOverridingJobPriorityWithCascadeAndDisabledTenantCheck() {
    JobDefinition jobDefinition = managementService
      .createJobDefinitionQuery()
      .list().get(0);
   
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 1701, true);
    // then
    assertThat(managementService.createJobDefinitionQuery()
      .jobDefinitionId(jobDefinition.getId()).singleResult()
      .getOverridingJobPriority(), is(1701L));
  }

  // clearOverridingJobPriorityForJobDefinition
  @Test
  public void testClearOverridingJobPriorityWithAuthenticatedTenant() {
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().list().get(0);
    
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 1701);

    assertThat(managementService.createJobDefinitionQuery()
      .jobDefinitionId(jobDefinition.getId()).singleResult()
      .getOverridingJobPriority(), is(1701L));

    identityService.setAuthentication("aUserId", null,  Arrays.asList(TENANT_ONE));
    managementService.clearOverridingJobPriorityForJobDefinition(jobDefinition.getId());

    // then
    assertThat(managementService.createJobDefinitionQuery()
    .jobDefinitionId(jobDefinition.getId()).singleResult()
    .getOverridingJobPriority(), nullValue());

  }

  @Test
  public void testClearOverridingJobPriorityWithNoAuthenticatedTenant() {
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().list().get(0);
    
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 1701);

    identityService.setAuthentication("aUserId", null);

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the process definition '" 
      + jobDefinition.getProcessDefinitionId() +"' because it belongs to no authenticated tenant.");

    // when
    managementService.clearOverridingJobPriorityForJobDefinition(jobDefinition.getId());

  }

  @Test
  public void testClearOverridingJobPriorityWithDisabledTenantCheck() {
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().list().get(0);
    
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 1701);

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
  
    // then
    managementService.clearOverridingJobPriorityForJobDefinition(jobDefinition.getId());
    // then
    assertThat(managementService.createJobDefinitionQuery()
      .jobDefinitionId(jobDefinition.getId()).singleResult()
      .getOverridingJobPriority(), nullValue());
  }

  // getJobExceptionStackTrace
  @Test
  public void testGetJobExceptionStackTraceWithAuthenticatedTenant() {
 
    String processInstanceId = engineRule.getRuntimeService()
      .startProcessInstanceByKey(PROCESS_DEFINITION_KEY)
      .getId();

    testRule.executeAvailableJobs();

    String timerJobId = managementService.createJobQuery()
      .processInstanceId(processInstanceId)
      .singleResult()
      .getId();
    
    identityService.setAuthentication("aUserId", null,  Arrays.asList(TENANT_ONE));
    assertThat(managementService.getJobExceptionStacktrace(timerJobId), notNullValue());
  }

  @Test
  public void testGetJobExceptionStackTraceWithNoAuthenticatedTenant() {
 
    String processInstanceId = engineRule.getRuntimeService()
      .startProcessInstanceByKey(PROCESS_DEFINITION_KEY)
      .getId();

    testRule.executeAvailableJobs();

    String timerJobId = managementService.createJobQuery()
      .processInstanceId(processInstanceId)
      .singleResult()
      .getId();
    
    identityService.setAuthentication("aUserId", null);

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the job '" + timerJobId 
      +"' because it belongs to no authenticated tenant.");

    // when
    managementService.getJobExceptionStacktrace(timerJobId);
  }

  @Test
  public void testGetJobExceptionStackTraceWithDisabledTenantCheck() {
 
    String processInstanceId = engineRule.getRuntimeService()
      .startProcessInstanceByKey(PROCESS_DEFINITION_KEY)
      .getId();

    testRule.executeAvailableJobs();
    
    String timerJobId = managementService.createJobQuery()
      .processInstanceId(processInstanceId)
      .singleResult()
      .getId();
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // when
    managementService.getJobExceptionStacktrace(timerJobId);
    assertThat(managementService.getJobExceptionStacktrace(timerJobId), notNullValue());
  }

  // deleteJobs
  @Test
  public void testDeleteJobWithAuthenticatedTenant() {
    String timerJobId = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .singleResult()
      .getId();
    
    identityService.setAuthentication("aUserId", null,  Arrays.asList(TENANT_ONE));
    managementService.deleteJob(timerJobId);

    // then
    assertEquals(0, managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .count());
  }

  @Test
  public void testDeleteJobWithNoAuthenticatedTenant() {
    String timerJobId = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .singleResult()
      .getId();
    
    identityService.setAuthentication("aUserId", null);

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the job '" + timerJobId 
      +"' because it belongs to no authenticated tenant.");

    // when
    managementService.deleteJob(timerJobId);
  }

  @Test
  public void testDeleteJobWithDisabledTenantCheck() {
    String timerJobId = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .singleResult()
      .getId();
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    managementService.deleteJob(timerJobId);

    // then
    assertEquals(0, managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .count());
  }

  //executeJobs
  @Test
  public void testExecuteJobWithAuthenticatedTenant() {

    String noFailProcessInstanceId = engineRule.getRuntimeService()
      .startProcessInstanceByKey("noFail")
      .getId(); 

    TaskQuery taskQuery = engineRule.getTaskService()
      .createTaskQuery()
      .processInstanceId(noFailProcessInstanceId);
  
    assertEquals(1, taskQuery.list().size());

    String timerJobId = managementService.createJobQuery()
      .processInstanceId(noFailProcessInstanceId)
      .singleResult()
      .getId();
    
    identityService.setAuthentication("aUserId", null,  Arrays.asList(TENANT_ONE));
    managementService.executeJob(timerJobId);

    // then
    assertEquals(0, taskQuery.list().size());
  }

  @Test
  public void testExecuteJobWithNoAuthenticatedTenant() {

    String noFailProcessInstanceId = engineRule.getRuntimeService()
      .startProcessInstanceByKey("noFail")
      .getId(); 

    String timerJobId = managementService.createJobQuery()
      .processInstanceId(noFailProcessInstanceId)
      .singleResult()
      .getId();
    
    identityService.setAuthentication("aUserId", null);

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the job '" + timerJobId 
      +"' because it belongs to no authenticated tenant.");
    managementService.executeJob(timerJobId);

  }

  @Test
  public void testExecuteJobWithDisabledTenantCheck() {

    String noFailProcessInstanceId = engineRule.getRuntimeService()
      .startProcessInstanceByKey("noFail")
      .getId(); 

    String timerJobId = managementService.createJobQuery()
      .processInstanceId(noFailProcessInstanceId)
      .singleResult()
      .getId();
    
    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    managementService.executeJob(timerJobId);

    TaskQuery taskQuery = engineRule.getTaskService()
      .createTaskQuery()
      .processInstanceId(noFailProcessInstanceId);

    // then
    assertEquals(0, taskQuery.list().size());
  }

  protected Job selectJobByProcessInstanceId(String processInstanceId) {
    Job job = managementService
        .createJobQuery()
        .processInstanceId(processInstanceId)
        .singleResult();
    return job;
  }
}
