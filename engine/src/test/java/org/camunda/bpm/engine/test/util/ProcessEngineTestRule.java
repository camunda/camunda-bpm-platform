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

package org.camunda.bpm.engine.test.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.behavior.CaseControlRuleImpl;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.HistoryLevelFull;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.test.AbstractProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import junit.framework.AssertionFailedError;

public class ProcessEngineTestRule extends TestWatcher {

  public static final String DEFAULT_BPMN_RESOURCE_NAME = "process.bpmn20.xml";

  protected ProcessEngineRule processEngineRule;
  protected ProcessEngine processEngine;

  public ProcessEngineTestRule(ProcessEngineRule processEngineRule) {
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

    assertThat("Process instance with id " + processInstanceId + " is not finished",
        processInstance, is(nullValue()));
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


  public void assertCaseEnded(String caseInstanceId) {
    CaseInstance caseInstance = processEngine
      .getCaseService()
      .createCaseInstanceQuery()
      .caseInstanceId(caseInstanceId)
      .singleResult();

    assertThat("Case instance with id " + caseInstanceId + " is not finished",
        caseInstance, is(nullValue()));
  }

  public DeploymentWithDefinitions deploy(BpmnModelInstance... bpmnModelInstances) {
    return deploy(createDeploymentBuilder(), Arrays.asList(bpmnModelInstances), Collections.<String> emptyList());
  }

  public DeploymentWithDefinitions deploy(String... resources) {
    return deploy(createDeploymentBuilder(), Collections.<BpmnModelInstance> emptyList(), Arrays.asList(resources));
  }

  public DeploymentWithDefinitions deploy(DeploymentBuilder deploymentBuilder) {
    DeploymentWithDefinitions deployment = deploymentBuilder.deployWithResult();

    processEngineRule.manageDeployment(deployment);

    return deployment;
  }

  public Deployment deploy(BpmnModelInstance bpmnModelInstance, String resource) {
    return deploy(createDeploymentBuilder(), Collections.singletonList(bpmnModelInstance), Collections.singletonList(resource));
  }

  public Deployment deployForTenant(String tenantId, BpmnModelInstance... bpmnModelInstances) {
    return deploy(createDeploymentBuilder().tenantId(tenantId), Arrays.asList(bpmnModelInstances), Collections.<String> emptyList());
  }

  public Deployment deployForTenant(String tenantId, String... resources) {
    return deploy(createDeploymentBuilder().tenantId(tenantId), Collections.<BpmnModelInstance> emptyList(), Arrays.asList(resources));
  }

  public Deployment deployForTenant(String tenant, BpmnModelInstance bpmnModelInstance, String resource) {
    return deploy(createDeploymentBuilder().tenantId(tenant), Collections.singletonList(bpmnModelInstance), Collections.singletonList(resource));
  }

  public ProcessDefinition deployAndGetDefinition(BpmnModelInstance bpmnModel) {
    return deployForTenantAndGetDefinition(null, bpmnModel);
  }

  public ProcessDefinition deployForTenantAndGetDefinition(String tenant, BpmnModelInstance bpmnModel) {
    Deployment deployment = deploy(createDeploymentBuilder().tenantId(tenant), Collections.singletonList(bpmnModel), Collections.<String>emptyList());

    return processEngineRule.getRepositoryService()
      .createProcessDefinitionQuery()
      .deploymentId(deployment.getId())
      .singleResult();
  }

  protected DeploymentWithDefinitions deploy(DeploymentBuilder deploymentBuilder, List<BpmnModelInstance> bpmnModelInstances, List<String> resources) {
    int i = 0;
    for (BpmnModelInstance bpmnModelInstance : bpmnModelInstances) {
      deploymentBuilder.addModelInstance(i + "_" + DEFAULT_BPMN_RESOURCE_NAME, bpmnModelInstance);
      i++;
    }

    for (String resource : resources) {
      deploymentBuilder.addClasspathResource(resource);
    }

    return deploy(deploymentBuilder);
  }

  protected DeploymentBuilder createDeploymentBuilder() {
    return processEngine.getRepositoryService().createDeployment();
  }

  public void waitForJobExecutorToProcessAllJobs() {
    waitForJobExecutorToProcessAllJobs(0);
  }

  public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait) {
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
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
        throw new AssertionError("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      jobExecutor.shutdown();
    }
  }

  protected boolean areJobsAvailable() {
    List<Job> list = processEngine.getManagementService().createJobQuery().list();
    for (Job job : list) {
      if (!job.isSuspended() && job.getRetries() > 0 && (job.getDuedate() == null || ClockUtil.getCurrentTime().after(job.getDuedate()))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Execute all available jobs recursively till no more jobs found.
   */
  public void executeAvailableJobs() {
    executeAvailableJobs(0, Integer.MAX_VALUE);
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
    executeAvailableJobs(0, expectedExecutions);
  }

  private void executeAvailableJobs(int jobsExecuted, int expectedExecutions) {
    List<Job> jobs = processEngine.getManagementService().createJobQuery().withRetriesLeft().list();

    if (jobs.isEmpty()) {
      if (expectedExecutions != Integer.MAX_VALUE) {
        assertThat("executed less jobs than expected.", jobsExecuted, is(expectedExecutions));
      }
      return;
    }

    for (Job job : jobs) {
      try {
        processEngine.getManagementService().executeJob(job.getId());
        jobsExecuted += 1;
      } catch (Exception e) {}
    }

    assertThat("executed more jobs than expected.",
        jobsExecuted, lessThanOrEqualTo(expectedExecutions));

    executeAvailableJobs(jobsExecuted, expectedExecutions);
  }

  public void completeTask(String taskKey) {
    TaskService taskService = processEngine.getTaskService();
    Task task = taskService.createTaskQuery().taskDefinitionKey(taskKey).singleResult();
    assertNotNull("Expected a task with key '" + taskKey + "' to exist", task);
    taskService.complete(task.getId());
  }

  public void completeAnyTask(String taskKey) {
    TaskService taskService = processEngine.getTaskService();
    List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey(taskKey).list();
    assertTrue(!tasks.isEmpty());
    taskService.complete(tasks.get(0).getId());
  }

  public void setAnyVariable(String executionId) {
    setVariable(executionId, "any", "any");
  }

  public void setVariable(String executionId, String varName, Object varValue) {
    processEngine.getRuntimeService().setVariable(executionId, varName, varValue);
  }

  public void correlateMessage(String messageName) {
    processEngine.getRuntimeService().createMessageCorrelation(messageName).correlate();
  }

  public void sendSignal(String signalName) {
    processEngine.getRuntimeService().signalEventReceived(signalName);
  }

  public boolean isHistoryLevelNone() {
    HistoryLevel historyLevel = processEngineRule.getProcessEngineConfiguration().getHistoryLevel();
    return HistoryLevel.HISTORY_LEVEL_NONE.equals(historyLevel);
  }

  public boolean isHistoryLevelActivity() {
    HistoryLevel historyLevel = processEngineRule.getProcessEngineConfiguration().getHistoryLevel();
    return HistoryLevel.HISTORY_LEVEL_ACTIVITY.equals(historyLevel);
  }

  public boolean isHistoryLevelAudit() {
    HistoryLevel historyLevel = processEngineRule.getProcessEngineConfiguration().getHistoryLevel();
    return HistoryLevel.HISTORY_LEVEL_AUDIT.equals(historyLevel);
  }

  public boolean isHistoryLevelFull() {
    HistoryLevel historyLevel = processEngineRule.getProcessEngineConfiguration().getHistoryLevel();
    return HistoryLevel.HISTORY_LEVEL_FULL.equals(historyLevel);
  }

  /**
   * Asserts if the provided text is part of some text.
   */
  public void assertTextPresent(String expected, String actual) {
    if ( (actual==null)
      || (actual.indexOf(expected)==-1)
      ) {
      throw new AssertionFailedError("expected presence of ["+expected+"], but was ["+actual+"]");
    }
  }

  /**
   * Asserts if the provided text is part of some text, ignoring any uppercase characters
   */
  public void assertTextPresentIgnoreCase(String expected, String actual) {
    assertTextPresent(expected.toLowerCase(), actual.toLowerCase());
  }

  public Object defaultManualActivation() {
    Expression expression = new FixedValue(true);
    CaseControlRuleImpl caseControlRule = new CaseControlRuleImpl(expression);
    return caseControlRule;
  }

  protected static class InterruptTask extends TimerTask {
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

}
