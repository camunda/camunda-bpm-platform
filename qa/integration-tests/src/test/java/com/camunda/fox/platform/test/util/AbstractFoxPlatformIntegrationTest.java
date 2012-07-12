/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.platform.test.util;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.runtime.Job;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;

import com.camunda.fox.platform.api.ProcessArchiveService;
import com.camunda.fox.platform.api.ProcessEngineService;

public abstract class AbstractFoxPlatformIntegrationTest {
  
  public final static String PROCESS_ARCHIVE_SERVICE_NAME =
          "java:global/" +
          "camunda-fox-platform/" +
          "process-engine/" +
          "PlatformService!com.camunda.fox.platform.api.ProcessArchiveService";
  
  public final static String PROCESS_ENGINE_SERVICE_NAME =
          "java:global/" +
          "camunda-fox-platform/" +
          "process-engine/" +
          "PlatformService!com.camunda.fox.platform.api.ProcessEngineService";

  protected ProcessEngineService processEngineService;
  protected ProcessArchiveService processArchiveService;
  protected ProcessEngine processEngine;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected FormService formService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  protected ManagementService managementService;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  
  public static WebArchive initWebArchiveDeployment(String name) {
    return ShrinkWrap.create(WebArchive.class, name)
              .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
              .addAsLibraries(DeploymentHelper.getFoxPlatformClient())
              .addAsResource("META-INF/processes.xml", "META-INF/processes.xml")
              .addClass(AbstractFoxPlatformIntegrationTest.class);    
  }
  
  public static WebArchive initWebArchiveDeployment() {
    return initWebArchiveDeployment("test.war");
  }
  
  public static ProcessEngineService getProcessEngineService() {
    try {
      return InitialContext.doLookup(PROCESS_ENGINE_SERVICE_NAME);
    } catch (NamingException e) {
      Assert.fail("Exception while looking up process engine service: "+e.getMessage());
      e.printStackTrace();
      return null;
    }
  }
  
  public static ProcessArchiveService getProcessArchiveService() {
    try {
      return InitialContext.doLookup(PROCESS_ARCHIVE_SERVICE_NAME);
    } catch (NamingException e) {
      Assert.fail("Exception while looking up process archive service: "+e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  @Before
  public void setupBeforeTest() {
    processEngineService = getProcessEngineService();
    processArchiveService = getProcessArchiveService();
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
  }

  public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    jobExecutor.start();

    try {
      Timer timer = new Timer();
      InteruptTask task = new InteruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean areJobsAvailable = true;
      try {
        while (areJobsAvailable && !task.isTimeLimitExceeded()) {
          Thread.sleep(intervalMillis);
          areJobsAvailable = areJobsAvailable();
        }
      } catch (InterruptedException e) {
      } finally {
        timer.cancel();
      }
      if (areJobsAvailable) {
        throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      jobExecutor.shutdown();
    }
  }

  public boolean areJobsAvailable() {
    List<Job> list = managementService.createJobQuery().executable().list();
    return !list.isEmpty();
  }

  private static class InteruptTask extends TimerTask {

    protected boolean timeLimitExceeded = false;
    protected Thread thread;

    public InteruptTask(Thread thread) {
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
