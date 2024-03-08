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
package org.camunda.bpm.engine.test.jobexecutor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.CallerRunsRejectedJobsHandler;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobExecutor;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class JobExecutionLoggingTest {

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule().watch(
      "org.camunda.bpm.engine.jobexecutor", Level.DEBUG);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule).around(loggingRule);

  protected RuntimeService runtimeService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/jobexecutor/SimpleAsyncDelayProcess.bpmn20.xml" })
  public void shouldLogJobsQueuedForExecution() {
    // Replace job executor with one that has custom threadpool executor settings
    JobExecutionLoggingTest.TestJobExecutor testJobExecutor = new JobExecutionLoggingTest.TestJobExecutor();
    testJobExecutor.setMaxJobsPerAcquisition(10);
    processEngineConfiguration.setJobExecutor(testJobExecutor);
    testJobExecutor.registerProcessEngine(processEngineConfiguration.getProcessEngine());

    // Given five jobs
    for (int i = 0; i < 5; i++) {
      runtimeService.startProcessInstanceByKey("simpleAsyncDelayProcess");
    }

    // When executing the jobs
    processEngineConfiguration.getJobExecutor().start();
    testRule.waitForJobExecutorToProcessAllJobs(7000L);
    processEngineConfiguration.getJobExecutor().shutdown();

    // Look for filled queue logs
    List<ILoggingEvent> filteredLogList = loggingRule.getFilteredLog("Jobs currently in queue to be executed for the "
        + "process engine '" + processEngineConfiguration.getProcessEngineName() + "' : 2 (out of the max queue size "
        + ": " + testJobExecutor.queueSize + ")");

    // Then minimum one instance of filled queue log will be available.
    assertThat(filteredLogList.size()).isGreaterThanOrEqualTo(1);
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/jobexecutor/SimpleAsyncDelayProcess.bpmn20.xml" })
  public void shouldLogJobsInExecution() {
    // Replace job executor with one that has custom threadpool executor settings
    JobExecutionLoggingTest.TestJobExecutor testJobExecutor = new JobExecutionLoggingTest.TestJobExecutor();
    processEngineConfiguration.setJobExecutor(testJobExecutor);
    testJobExecutor.registerProcessEngine(processEngineConfiguration.getProcessEngine());

    // Given one job
    runtimeService.startProcessInstanceByKey("simpleAsyncDelayProcess");

    // When executing the jobs
    processEngineConfiguration.getJobExecutor().start();
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    processEngineConfiguration.getJobExecutor().shutdown();

    // Look for jobs in execution
    List<ILoggingEvent> filteredLogList = loggingRule.getFilteredLog(
        "Jobs currently in execution for the process engine '" + processEngineConfiguration.getProcessEngineName()
            + "' : 1");

    // Since the execution will be happening for a while the check is made for more than one occurrence of the log.
    assertThat(filteredLogList.size()).isGreaterThanOrEqualTo(1);
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/jobexecutor/SimpleAsyncDelayProcess.bpmn20.xml" })
  public void shouldLogAvailableJobExecutionThreads() {
    // Replace job executor with one that has custom threadpool executor settings
    JobExecutionLoggingTest.TestJobExecutor testJobExecutor = new JobExecutionLoggingTest.TestJobExecutor();
    processEngineConfiguration.setJobExecutor(testJobExecutor);
    testJobExecutor.registerProcessEngine(processEngineConfiguration.getProcessEngine());

    // Given one job
    runtimeService.startProcessInstanceByKey("simpleAsyncDelayProcess");

    // When executing the jobs
    processEngineConfiguration.getJobExecutor().start();
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    processEngineConfiguration.getJobExecutor().shutdown();

    // Look for available job execution threads logs
    List<ILoggingEvent> filteredLogList = loggingRule.getFilteredLog(
        "Available job execution threads for the process engine '" + processEngineConfiguration.getProcessEngineName()
            + "' : 2");

    // Since the execution will be happening for a while the check is made for more than one occurrence of the log.
    assertThat(filteredLogList.size()).isGreaterThanOrEqualTo(1);
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/jobexecutor/delegateThrowsException.bpmn20.xml" })
  public void shouldLogJobExecutionRejections() {
    // Given three jobs
    for (int i = 0; i < 3; i++) {
      runtimeService.startProcessInstanceByKey("testProcess");
    }

    // Replace job executor with one that rejects all jobs
    JobExecutionLoggingTest.RejectionJobExecutor rejectionExecutor = new JobExecutionLoggingTest.RejectionJobExecutor();
    processEngineConfiguration.setJobExecutor(rejectionExecutor);
    rejectionExecutor.registerProcessEngine(processEngineConfiguration.getProcessEngine());

    // When executing the jobs
    processEngineConfiguration.getJobExecutor().start();
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    processEngineConfiguration.getJobExecutor().shutdown();

    // Look for job execution rejection job count with one job
    List<ILoggingEvent> filteredLogList = loggingRule.getFilteredLog(
        "Jobs execution rejections for the process engine '" + processEngineConfiguration.getProcessEngineName()
            + "' : ");

    // Minimum occurrences of the log is three
    assertThat(filteredLogList.size()).isGreaterThanOrEqualTo(3);
  }

  public static class TestJobExecutor extends DefaultJobExecutor {

    protected int queueSize = 2;
    protected int corePoolSize = 1;
    protected int maxPoolSize = 3;
    protected BlockingQueue<Runnable> threadPoolQueue;

    public TestJobExecutor() {
      threadPoolQueue = new ArrayBlockingQueue<>(queueSize);
      threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS,
          threadPoolQueue);
      threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
    }
  }

  public static class RejectionJobExecutor extends TestJobExecutor {
    public RejectionJobExecutor() {
      threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS,
          threadPoolQueue) {
        @Override
        public void execute(Runnable command) {
          throw new RejectedExecutionException();
        }
      };
      rejectedJobsHandler = new CallerRunsRejectedJobsHandler();
    }
  }

}