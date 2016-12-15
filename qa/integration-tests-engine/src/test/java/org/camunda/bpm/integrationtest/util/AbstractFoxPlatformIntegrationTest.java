/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.integrationtest.util;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.integrationtest.deployment.callbacks.PurgeDatabaseServlet;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Before;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class AbstractFoxPlatformIntegrationTest {

  private static final String DEFAULT_WAR_NAME = "test.war";
  public static final Set<String> DEPLOYMENT_NAMES = new HashSet<String>();//Collections.synchronizedList(new ArrayList());
  ;

  protected static Logger logger = Logger.getLogger(AbstractFoxPlatformIntegrationTest.class.getName());

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

  public static JavaArchive purgeDatabaseServlet() {
    final JavaArchive purgeJar = ShrinkWrap.create(JavaArchive.class, "purge.jar");
    purgeJar.addClass(PurgeDatabaseServlet.class);
    return purgeJar;
  }


  @ArquillianResource
  private static Deployer deployer;

//  @Deployment(name = "purge")
//  public static WebArchive purgeDeployment() {
//
//    return initWebArchiveDeployment("purge.war");
//  }

  public static WebArchive initWebArchiveDeployment(final String name, String processesXmlPath) {
    WebArchive archive = ShrinkWrap.create(WebArchive.class, name)
      .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
      .addAsLibraries(DeploymentHelper.getEngineCdi())
      .addAsResource(processesXmlPath, "META-INF/processes.xml")
      .addClass(AbstractFoxPlatformIntegrationTest.class)
      .addClass(TestConstants.class)
      .addAsLibraries(purgeDatabaseServlet());

    DEPLOYMENT_NAMES.add(name.replace(".war", ""));

    TestContainer.addContainerSpecificResources(archive);
    return archive;
  }

  public static WebArchive initWebArchiveDeployment(String name) {
    return initWebArchiveDeployment(name, "META-INF/processes.xml");
  }

  public static WebArchive initWebArchiveDeployment() {
    return initWebArchiveDeployment(DEFAULT_WAR_NAME);
  }

  @Before
  public void setupBeforeTest() {
    logger.log(Level.INFO, "Before test - init services");
    processEngineService = BpmPlatform.getProcessEngineService();
    processEngine = processEngineService.getDefaultProcessEngine();
    processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
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

  @AfterClass
  public static void clean() {
    logger.log(Level.INFO, "After test - cleanup");

    if (DEPLOYMENT_NAMES.isEmpty()) {
      DEPLOYMENT_NAMES.add(DEFAULT_WAR_NAME.replace(".war", ""));
    }

    try {
      for (String warName : DEPLOYMENT_NAMES) {
        purgeDatabase(warName);
      }
    } catch (RuntimeException e) {
      throw e;
    } finally {
      DEPLOYMENT_NAMES.clear();
    }
  }

  private static void purgeDatabase(String warName) {
    HttpURLConnection httpURLConnection = null;
    try {
      URL url = new URL("http", "localhost", 38080, "/" + warName + "/purge");
      URLConnection urlConnection = url.openConnection();
      httpURLConnection = (HttpURLConnection) urlConnection;
      httpURLConnection.setRequestMethod("POST");
      httpURLConnection.setDoOutput(true);
      httpURLConnection.connect();

      logger.log(Level.INFO, url.toString());
      int responseCode = httpURLConnection.getResponseCode();

      if (responseCode == 404) {
        throw new IllegalStateException("Not found 404.");
      }
//      if (responseCode >= 400) {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
//        String line;
//        StringBuilder builder = new StringBuilder();
//        while((line = reader.readLine()) != null) {
//          builder.append(line).append("\n");
//        }
//          logger.log(Level.INFO, builder.toString());
//          throw new IllegalStateException("Not clean:\n" + builder.toString());
//      }
    } catch (IOException ioe) {
      logger.log(Level.SEVERE, ioe.getMessage(), ioe);
    } finally {
      if (httpURLConnection != null) {
        httpURLConnection.disconnect();
      }
//      DEPLOYMENT_NAMES.remove(warName);
    }
  }

  public void waitForJobExecutorToProcessAllJobs() {
    waitForJobExecutorToProcessAllJobs(12000);
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
