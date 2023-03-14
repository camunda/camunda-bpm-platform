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
package org.camunda.bpm.integrationtest.jobexecutor;

import static org.junit.Assert.assertEquals;

import java.util.Timer;
import java.util.TimerTask;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.integrationtest.jobexecutor.beans.ManagedJobExecutorBean;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ManagedJobExecutorTest {

  @Deployment
  public static WebArchive createDeployment() {
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
        .addAsWebInfResource("org/camunda/bpm/integrationtest/beans.xml", "beans.xml")
        .addAsLibraries(DeploymentHelper.getEngineCdi())
        .addClass(ManagedJobExecutorTest.class)
        .addClass(ManagedJobExecutorBean.class)
        .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/ManagedJobExecutorTest.testManagedExecutorUsed.bpmn20.xml");

    TestContainer.addContainerSpecificResourcesForNonPa(archive);

    return archive;
  }

  protected ProcessEngine processEngine;
  protected ManagementService managementService;
  protected RuntimeService runtimeService;

  @Before
  public void setUpCdiProcessEngineTestCase() throws Exception {
    processEngine = (ProgrammaticBeanLookup.lookup(ManagedJobExecutorBean.class)).getProcessEngine();
    managementService = processEngine.getManagementService();
    runtimeService = processEngine.getRuntimeService();
  }

  @After
  public void tearDownCdiProcessEngineTestCase() throws Exception {
    processEngine = null;
    managementService = null;
    runtimeService = null;
  }

  @Test
  public void testManagedExecutorUsed() throws InterruptedException {
    org.camunda.bpm.engine.repository.Deployment deployment = processEngine.getRepositoryService().createDeployment()
      .addClasspathResource("org/camunda/bpm/integrationtest/jobexecutor/ManagedJobExecutorTest.testManagedExecutorUsed.bpmn20.xml")
      .deploy();

    try {
      String pid = runtimeService.startProcessInstanceByKey("testBusinessProcessScopedWithJobExecutor").getId();

      assertEquals(1L, managementService.createJobQuery().processInstanceId(pid).count());

      waitForJobExecutorToProcessAllJobs(pid, 5000l, 25l);

      assertEquals(0L, managementService.createJobQuery().processInstanceId(pid).count());

      assertEquals("bar", runtimeService.createVariableInstanceQuery().processInstanceIdIn(pid).variableName("foo").singleResult().getValue());
    } finally {
      processEngine.getRepositoryService().deleteDeployment(deployment.getId(), true);
    }

  }

  protected void waitForJobExecutorToProcessAllJobs(String processInstanceId, long maxMillisToWait, long intervalMillis) {
    JobExecutor jobExecutor = ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).getJobExecutor();
    jobExecutor.start();

    try {
      Timer timer = new Timer();
      InteruptTask task = new InteruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean areJobsAvailable = true;
      try {
        while (areJobsAvailable && !task.isTimeLimitExceeded()) {
          Thread.sleep(intervalMillis);
          areJobsAvailable = areJobsAvailable(processInstanceId);
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

  private static class InteruptTask extends TimerTask {
    protected boolean timeLimitExceeded = false;
    protected Thread thread;
    public InteruptTask(Thread thread) {
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

  protected boolean areJobsAvailable(String processInstanceId) {
    return !managementService
      .createJobQuery()
      .processInstanceId(processInstanceId)
      .executable()
      .list()
      .isEmpty();
  }
}
