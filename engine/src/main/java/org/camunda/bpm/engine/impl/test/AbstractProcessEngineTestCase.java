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

package org.camunda.bpm.engine.impl.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.slf4j.Logger;

import junit.framework.AssertionFailedError;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractProcessEngineTestCase extends PvmTestCase {

  private final static Logger LOG = TestLogger.TEST_LOGGER.getLogger();

  static {
    // this ensures that mybatis uses slf4j logging
    LogFactory.useSlf4jLogging();
  }

  protected ProcessEngine processEngine;

  protected String deploymentId;
  protected Set<String> deploymentIds = new HashSet<String>();

  protected Throwable exception;

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected FormService formService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  protected ManagementService managementService;
  protected AuthorizationService authorizationService;
  protected CaseService caseService;
  protected FilterService filterService;
  protected ExternalTaskService externalTaskService;
  protected DecisionService decisionService;

  protected abstract void initializeProcessEngine();

  // Default: do nothing
  protected void closeDownProcessEngine() {
  }

  @Override
  public void runBare() throws Throwable {
    initializeProcessEngine();
    if (repositoryService==null) {
      initializeServices();
    }

    try {

      boolean hasRequiredHistoryLevel = TestHelper.annotationRequiredHistoryLevelCheck(processEngine, getClass(), getName());
      // ignore test case when current history level is too low
      if (hasRequiredHistoryLevel) {

        deploymentId = TestHelper.annotationDeploymentSetUp(processEngine, getClass(), getName());

        super.runBare();
      }

    }
    catch (AssertionFailedError e) {
      LOG.error("ASSERTION FAILED: " + e, e);
      exception = e;
      throw e;

    }
    catch (Throwable e) {
      LOG.error("EXCEPTION: " + e, e);
      exception = e;
      throw e;

    }
    finally {

      identityService.clearAuthentication();
      processEngineConfiguration.setTenantCheckEnabled(true);

      deleteDeployments();

      deleteHistoryCleanupJobs();

      // only fail if no test failure was recorded
      TestHelper.assertAndEnsureCleanDbAndCache(processEngine, exception == null);
      TestHelper.resetIdGenerator(processEngineConfiguration);
      ClockUtil.reset();

      // Can't do this in the teardown, as the teardown will be called as part
      // of the super.runBare
      closeDownProcessEngine();
      clearServiceReferences();
    }
  }

  private void deleteHistoryCleanupJobs() {
    final List<Job> jobs = historyService.findHistoryCleanupJobs();
    for (final Job job: jobs) {
      processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {
            commandContext.getJobManager().deleteJob((JobEntity) job);
          return null;
        }
      });
    }
  }

  protected void deleteDeployments() {
    if(deploymentId != null) {
      deploymentIds.add(deploymentId);
    }

    for(String deploymentId : deploymentIds) {
      TestHelper.annotationDeploymentTearDown(processEngine, deploymentId, getClass(), getName());
    }

    deploymentId = null;
    deploymentIds.clear();
  }

  protected void initializeServices() {
    processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
    taskService = processEngine.getTaskService();
    formService = processEngine.getFormService();
    historyService = processEngine.getHistoryService();
    identityService = processEngine.getIdentityService();
    managementService = processEngine.getManagementService();
    authorizationService = processEngine.getAuthorizationService();
    caseService = processEngine.getCaseService();
    filterService = processEngine.getFilterService();
    externalTaskService = processEngine.getExternalTaskService();
    decisionService = processEngine.getDecisionService();
  }

  protected void clearServiceReferences() {
    processEngineConfiguration = null;
    repositoryService = null;
    runtimeService = null;
    taskService = null;
    formService = null;
    historyService = null;
    identityService = null;
    managementService = null;
    authorizationService = null;
    caseService = null;
    filterService = null;
    externalTaskService = null;
    decisionService = null;
  }

  public void assertProcessEnded(final String processInstanceId) {
    ProcessInstance processInstance = processEngine
      .getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstanceId)
      .singleResult();

    if (processInstance!=null) {
      throw new AssertionFailedError("Expected finished process instance '"+processInstanceId+"' but it was still in the db");
    }
  }

  public void assertProcessNotEnded(final String processInstanceId) {
    ProcessInstance processInstance = processEngine
      .getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstanceId)
      .singleResult();

    if (processInstance==null) {
      throw new AssertionFailedError("Expected process instance '"+processInstanceId+"' to be still active but it was not in the db");
    }
  }

  public void assertCaseEnded(final String caseInstanceId) {
    CaseInstance caseInstance = processEngine
      .getCaseService()
      .createCaseInstanceQuery()
      .caseInstanceId(caseInstanceId)
      .singleResult();

    if (caseInstance!=null) {
      throw new AssertionFailedError("Expected finished case instance '"+caseInstanceId+"' but it was still in the db");
    }
  }

  @Deprecated
  public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
    waitForJobExecutorToProcessAllJobs(maxMillisToWait);
  }

  public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait) {
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    jobExecutor.start();
    long intervalMillis = 1000;

    int jobExecutorWaitTime = jobExecutor.getWaitTimeInMillis() * 2;
    if(maxMillisToWait < jobExecutorWaitTime) {
      maxMillisToWait = jobExecutorWaitTime;
    }

    try {
      Timer timer = new Timer();
      InterruptTask task = new InterruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean areJobsAvailable = true;
      try {
        while (areJobsAvailable && !task.isTimeLimitExceeded()) {
          Thread.sleep(intervalMillis);
          try {
            areJobsAvailable = areJobsAvailable();
          } catch(Throwable t) {
            // Ignore, possible that exception occurs due to locking/updating of table on MSSQL when
            // isolation level doesn't allow READ of the table
          }
        }
      } catch (InterruptedException e) {
      } finally {
        timer.cancel();
      }
      if (areJobsAvailable) {
        throw new ProcessEngineException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      jobExecutor.shutdown();
    }
  }

  @Deprecated
  public void waitForJobExecutorOnCondition(long maxMillisToWait, long intervalMillis, Callable<Boolean> condition) {
    waitForJobExecutorOnCondition(maxMillisToWait, condition);
  }

  public void waitForJobExecutorOnCondition(long maxMillisToWait, Callable<Boolean> condition) {
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    jobExecutor.start();
    long intervalMillis = 500;

    if(maxMillisToWait < (jobExecutor.getWaitTimeInMillis()*2)) {
      maxMillisToWait = (jobExecutor.getWaitTimeInMillis()*2);
    }

    try {
      Timer timer = new Timer();
      InterruptTask task = new InterruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean conditionIsViolated = true;
      try {
        while (conditionIsViolated && !task.isTimeLimitExceeded()) {
          Thread.sleep(intervalMillis);
          conditionIsViolated = !condition.call();
        }
      } catch (InterruptedException e) {
      } catch (Exception e) {
        throw new ProcessEngineException("Exception while waiting on condition: "+e.getMessage(), e);
      } finally {
        timer.cancel();
      }
      if (conditionIsViolated) {
        throw new ProcessEngineException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      jobExecutor.shutdown();
    }
  }

  /**
   * Execute all available jobs recursively till no more jobs found.
   */
  public void executeAvailableJobs() {
    executeAvailableJobs(0, Integer.MAX_VALUE, true);
  }

  /**
   * Execute all available jobs recursively till no more jobs found or the number of executions is higher than expected.
   *
   * @param expectedExecutions number of expected job executions
   *
   * @throws AssertionFailedError when execute less or more jobs than expected
   *
   * @see #executeAvailableJobs()
   */
  public void executeAvailableJobs(int expectedExecutions){
    executeAvailableJobs(0, expectedExecutions, false);
  }

  private void executeAvailableJobs(int jobsExecuted, int expectedExecutions, boolean ignoreLessExecutions) {
    List<Job> jobs = managementService.createJobQuery().withRetriesLeft().list();

    if (jobs.isEmpty()) {
      assertTrue("executed less jobs than expected. expected <" + expectedExecutions + "> actual <" + jobsExecuted + ">",
          jobsExecuted == expectedExecutions || ignoreLessExecutions);
      return;
    }

    for (Job job : jobs) {
      try {
        managementService.executeJob(job.getId());
        jobsExecuted += 1;
      } catch (Exception e) {}
    }

    assertTrue("executed more jobs than expected. expected <" + expectedExecutions + "> actual <" + jobsExecuted + ">",
        jobsExecuted <= expectedExecutions);

    executeAvailableJobs(jobsExecuted, expectedExecutions, ignoreLessExecutions);
  }

  public boolean areJobsAvailable() {
    List<Job> list = managementService.createJobQuery().list();
    for (Job job : list) {
      if (!job.isSuspended() && job.getRetries() > 0 && (job.getDuedate() == null || ClockUtil.getCurrentTime().after(job.getDuedate()))) {
        return true;
      }
    }
    return false;
  }

  private static class InterruptTask extends TimerTask {
    protected boolean timeLimitExceeded = false;
    protected Thread thread;
    public InterruptTask(Thread thread) {
      this.thread = thread;
    }
    public boolean isTimeLimitExceeded() {
      return timeLimitExceeded;
    }
    @Override
    public void run() {
      timeLimitExceeded = true;
      thread.interrupt();
    }
  }

  @Deprecated
  protected List<ActivityInstance> getInstancesForActivitiyId(ActivityInstance activityInstance, String activityId) {
    return getInstancesForActivityId(activityInstance, activityId);
  }

  protected List<ActivityInstance> getInstancesForActivityId(ActivityInstance activityInstance, String activityId) {
    List<ActivityInstance> result = new ArrayList<ActivityInstance>();
    if(activityInstance.getActivityId().equals(activityId)) {
      result.add(activityInstance);
    }
    for (ActivityInstance childInstance : activityInstance.getChildActivityInstances()) {
      result.addAll(getInstancesForActivityId(childInstance, activityId));
    }
    return result;
  }

  protected void runAsUser(String userId, List<String> groupIds, Runnable r) {
    try {
      identityService.setAuthenticatedUserId(userId);
      processEngineConfiguration.setAuthorizationEnabled(true);

      r.run();

    } finally {
      identityService.setAuthenticatedUserId(null);
      processEngineConfiguration.setAuthorizationEnabled(false);
    }
  }

  protected String deployment(BpmnModelInstance... bpmnModelInstances) {
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();

    return deployment(deploymentBuilder, bpmnModelInstances);
  }

  protected String deployment(String... resources) {
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();

    return deployment(deploymentBuilder, resources);
  }

  protected String deploymentForTenant(String tenantId, BpmnModelInstance... bpmnModelInstances) {
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().tenantId(tenantId);

    return deployment(deploymentBuilder, bpmnModelInstances);
  }

  protected String deploymentForTenant(String tenantId, String... resources) {
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().tenantId(tenantId);

    return deployment(deploymentBuilder, resources);
  }

  protected String deploymentForTenant(String tenantId, String classpathResource, BpmnModelInstance modelInstance) {
    return deployment(repositoryService.createDeployment()
        .tenantId(tenantId)
        .addClasspathResource(classpathResource), modelInstance);
  }

  protected String deployment(DeploymentBuilder deploymentBuilder, BpmnModelInstance... bpmnModelInstances) {
    for (int i = 0; i < bpmnModelInstances.length; i++) {
      BpmnModelInstance bpmnModelInstance = bpmnModelInstances[i];
      deploymentBuilder.addModelInstance("testProcess-"+i+".bpmn", bpmnModelInstance);
    }

    return deploymentWithBuilder(deploymentBuilder);
  }

  protected String deployment(DeploymentBuilder deploymentBuilder, String... resources) {
    for (int i = 0; i < resources.length; i++) {
      deploymentBuilder.addClasspathResource(resources[i]);
    }

    return deploymentWithBuilder(deploymentBuilder);
  }

  protected String deploymentWithBuilder(DeploymentBuilder builder) {
    deploymentId = builder.deploy().getId();
    deploymentIds.add(deploymentId);

    return deploymentId;
  }

}
