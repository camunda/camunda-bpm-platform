package org.camunda.bpm.engine.test.jobexecutor;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteJobsCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;

public class DeploymentAwareJobExecutorTest extends PluggableProcessEngineTestCase {

  protected ProcessEngine otherProcessEngine = null;

  public void setUp() throws Exception {
    super.setUp();
    processEngineConfiguration.setJobExecutorDeploymentAware(true);
  }

  public void tearDown() throws Exception {
    processEngineConfiguration.setJobExecutorDeploymentAware(false);
    super.tearDown();
  }

  protected void closeDownProcessEngine() {
    super.closeDownProcessEngine();
    if (otherProcessEngine != null) {
      otherProcessEngine.close();
      ProcessEngines.unregister(otherProcessEngine);
      otherProcessEngine = null;
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void testProcessingOfJobsWithMatchingDeployment() {

    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    Set<String> registeredDeployments = managementService.getRegisteredDeployments();
    Assert.assertEquals(1, registeredDeployments.size());
    Assert.assertTrue(registeredDeployments.contains(deploymentId));

    Job executableJob = managementService.createJobQuery().singleResult();

    String otherDeploymentId =
        deployAndInstantiateWithNewEngineConfiguration(
            "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcessVersion2.bpmn20.xml");

    // assert that two jobs have been created, one for each deployment
    List<Job> jobs = managementService.createJobQuery().list();
    Assert.assertEquals(2, jobs.size());
    Set<String> jobDeploymentIds = new HashSet<String>();
    jobDeploymentIds.add(jobs.get(0).getDeploymentId());
    jobDeploymentIds.add(jobs.get(1).getDeploymentId());

    Assert.assertTrue(jobDeploymentIds.contains(deploymentId));
    Assert.assertTrue(jobDeploymentIds.contains(otherDeploymentId));

    // select executable jobs for executor of first engine
    AcquiredJobs acquiredJobs = getExecutableJobs(processEngineConfiguration.getJobExecutor());
    Assert.assertEquals(1, acquiredJobs.size());
    Assert.assertTrue(acquiredJobs.contains(executableJob.getId()));

    repositoryService.deleteDeployment(otherDeploymentId, true);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void testExplicitDeploymentRegistration() {
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    String otherDeploymentId =
        deployAndInstantiateWithNewEngineConfiguration(
            "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcessVersion2.bpmn20.xml");

    processEngine.getManagementService().registerDeploymentForJobExecutor(otherDeploymentId);

    List<Job> jobs = managementService.createJobQuery().list();

    AcquiredJobs acquiredJobs = getExecutableJobs(processEngineConfiguration.getJobExecutor());
    Assert.assertEquals(2, acquiredJobs.size());
    for (Job job : jobs) {
      Assert.assertTrue(acquiredJobs.contains(job.getId()));
    }

    repositoryService.deleteDeployment(otherDeploymentId, true);
  }

  public void testRegistrationOfNonExistingDeployment() {
    String nonExistingDeploymentId = "some non-existing id";

    try {
      processEngine.getManagementService().registerDeploymentForJobExecutor(nonExistingDeploymentId);
      Assert.fail("Registering a non-existing deployment should not succeed");
    } catch (ProcessEngineException e) {
      assertTextPresent("Deployment " + nonExistingDeploymentId + " does not exist", e.getMessage());
      // happy path
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void testDeploymentUnregistrationOnUndeployment() {
    Assert.assertEquals(1, managementService.getRegisteredDeployments().size());

    repositoryService.deleteDeployment(deploymentId, true);

    Assert.assertEquals(0, managementService.getRegisteredDeployments().size());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void testNoUnregistrationOnFailingUndeployment() {
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    try {
      repositoryService.deleteDeployment(deploymentId, false);
      Assert.fail();
    } catch (Exception e) {
      // should still be registered, if not successfully undeployed
      Assert.assertEquals(1, managementService.getRegisteredDeployments().size());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void testExplicitDeploymentUnregistration() {
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    processEngine.getManagementService().unregisterDeploymentForJobExecutor(deploymentId);

    AcquiredJobs acquiredJobs = getExecutableJobs(processEngineConfiguration.getJobExecutor());
    Assert.assertEquals(0, acquiredJobs.size());
  }

  public void testJobsWithoutDeploymentIdAreAlwaysProcessed() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();

    String messageId = commandExecutor.execute(new Command<String>() {
      public String execute(CommandContext commandContext) {
        MessageEntity message = new MessageEntity();
        commandContext.getJobManager().send(message);
        return message.getId();
      }
    });

    AcquiredJobs acquiredJobs = getExecutableJobs(processEngineConfiguration.getJobExecutor());
    Assert.assertEquals(1, acquiredJobs.size());
    Assert.assertTrue(acquiredJobs.contains(messageId));

    commandExecutor.execute(new DeleteJobsCmd(messageId, true));
  }

  private AcquiredJobs getExecutableJobs(JobExecutor jobExecutor) {
    return processEngineConfiguration.getCommandExecutorTxRequired().execute(new AcquireJobsCmd(jobExecutor));
  }

  private String deployAndInstantiateWithNewEngineConfiguration(String resource) {
    // 1. create another process engine
    try {
      otherProcessEngine = ProcessEngineConfiguration
        .createProcessEngineConfigurationFromResource("camunda.cfg.xml")
        .buildProcessEngine();
    } catch (RuntimeException ex) {
      if (ex.getCause() != null && ex.getCause() instanceof FileNotFoundException) {
        otherProcessEngine = ProcessEngineConfiguration
          .createProcessEngineConfigurationFromResource("activiti.cfg.xml")
          .buildProcessEngine();
      } else {
        throw ex;
      }
    }

    // 2. deploy again
    RepositoryService otherRepositoryService = otherProcessEngine.getRepositoryService();

    String deploymentId = otherRepositoryService.createDeployment()
      .addClasspathResource(resource)
      .deploy().getId();

    // 3. start instance (i.e. create job)
    ProcessDefinition newDefinition = otherRepositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
    otherProcessEngine.getRuntimeService().startProcessInstanceById(newDefinition.getId());

    return deploymentId;
  }

  @Deployment(resources="org/camunda/bpm/engine/test/jobexecutor/processWithTimerCatch.bpmn20.xml")
  public void testIntermediateTimerEvent() {


    runtimeService.startProcessInstanceByKey("testProcess");

    Set<String> registeredDeployments = processEngineConfiguration.getRegisteredDeployments();


    Job existingJob = managementService.createJobQuery().singleResult();

    ClockUtil.setCurrentTime(new Date(System.currentTimeMillis() + 61 * 1000));

    List<JobEntity> acquirableJobs = findAcquirableJobs();

    assertEquals(1, acquirableJobs.size());
    assertEquals(existingJob.getId(), acquirableJobs.get(0).getId());

    registeredDeployments.clear();

    acquirableJobs = findAcquirableJobs();

    assertEquals(0, acquirableJobs.size());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/jobexecutor/processWithTimerStart.bpmn20.xml")
  public void testTimerStartEvent() {

    Set<String> registeredDeployments = processEngineConfiguration.getRegisteredDeployments();

    Job existingJob = managementService.createJobQuery().singleResult();

    ClockUtil.setCurrentTime(new Date(System.currentTimeMillis()+1000));

    List<JobEntity> acquirableJobs = findAcquirableJobs();

    assertEquals(1, acquirableJobs.size());
    assertEquals(existingJob.getId(), acquirableJobs.get(0).getId());

    registeredDeployments.clear();

    acquirableJobs = findAcquirableJobs();

    assertEquals(0, acquirableJobs.size());
  }

  protected List<JobEntity> findAcquirableJobs() {
    return processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<List<JobEntity>>() {

      @Override
      public List<JobEntity> execute(CommandContext commandContext) {
        return commandContext
          .getJobManager()
          .findNextJobsToExecute(new Page(0, 100));
      }
    });
  }

}
