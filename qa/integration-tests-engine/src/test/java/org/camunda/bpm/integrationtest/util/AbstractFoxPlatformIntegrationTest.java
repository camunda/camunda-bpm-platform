/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.integrationtest.util;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;


public abstract class AbstractFoxPlatformIntegrationTest {

  protected static final long JOBS_WAIT_TIMEOUT_MS = 20_000L;

  protected Logger logger = Logger.getLogger(AbstractFoxPlatformIntegrationTest.class.getName());

  protected ProcessEngineService processEngineService;
//  protected ProcessArchiveService processArchiveService;
  protected ProcessEngine processEngine;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected FormService formService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  protected ManagementService managementService;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected CaseService caseService;
  protected DecisionService decisionService;

  public static WebArchive initWebArchiveDeployment(String name, String processesXmlPath) {
    WebArchive archive = ShrinkWrap.create(WebArchive.class, name)
              .addAsWebInfResource("org/camunda/bpm/integrationtest/beans.xml", "beans.xml")
              .addAsLibraries(DeploymentHelper.getEngineCdi())
              .addAsResource(processesXmlPath, "META-INF/processes.xml")
              .addClass(AbstractFoxPlatformIntegrationTest.class)
              .addClass(TestConstants.class);

    TestContainer.addContainerSpecificResources(archive);

    return archive;
  }
  public static WebArchive initWebArchiveDeployment(String name) {
    return initWebArchiveDeployment(name, "META-INF/processes.xml");
  }

  public static WebArchive initWebArchiveDeployment() {
    return initWebArchiveDeployment("test.war");
  }



  @Before
  public void setupBeforeTest() {
    processEngineService = BpmPlatform.getProcessEngineService();
    processEngine = processEngineService.getDefaultProcessEngine();
    processEngineConfiguration = ((ProcessEngineImpl)processEngine).getProcessEngineConfiguration();
    processEngineConfiguration.getJobExecutor().shutdown(); // make sure the job executor is down
    formService = processEngine.getFormService();
    historyService = processEngine.getHistoryService();
    identityService = processEngine.getIdentityService();
    managementService = processEngine.getManagementService();
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
    taskService = processEngine.getTaskService();
    caseService = processEngine.getCaseService();
    decisionService = processEngine.getDecisionService();
  }

  public void waitForJobExecutorToProcessAllJobs() {
    waitForJobExecutorToProcessAllJobs(JOBS_WAIT_TIMEOUT_MS);
  }

  public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait) {

    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    waitForJobExecutorToProcessAllJobs(jobExecutor, maxMillisToWait);
  }

  public void waitForJobExecutorToProcessAllJobs(JobExecutor jobExecutor, long maxMillisToWait) {

    int checkInterval = 1000;

    jobExecutor.start();

    try {
      Timer timer = new Timer();
      InterruptTask task = new InterruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean areJobsAvailable = true;
      try {
        while (areJobsAvailable && !task.isTimeLimitExceeded()) {
          Thread.sleep(checkInterval);
          areJobsAvailable = areJobsAvailable();
        }
      } catch (InterruptedException e) {
      } finally {
        timer.cancel();
      }
      if (areJobsAvailable) {
        throw new RuntimeException("time limit of " + maxMillisToWait + " was exceeded (still " + numberOfJobsAvailable() + " jobs available)");
      }

    } finally {
      jobExecutor.shutdown();
    }
  }

  public boolean areJobsAvailable() {
    List<Job> list = managementService.createJobQuery().list();
    for (Job job : list) {
      if (isJobAvailable(job)) {
        return true;
      }
    }
    return false;
  }

  public boolean isJobAvailable(Job job) {
    return job.getRetries() > 0 && (job.getDuedate() == null || ClockUtil.getCurrentTime().after(job.getDuedate()));
  }

  public int numberOfJobsAvailable() {
    int numberOfJobs = 0;
    List<Job> jobs = managementService.createJobQuery().list();
    for (Job job : jobs) {
      if (isJobAvailable(job)) {
        numberOfJobs++;
      }
    }
    return numberOfJobs;
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
    public void run() {
      timeLimitExceeded = true;
      thread.interrupt();
    }
  }

}
