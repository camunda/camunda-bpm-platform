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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class JobExecutionLoggingTest {

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule().watch("org.camunda.bpm.engine.jobexecutor", Level.DEBUG);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule).around(loggingRule);

  private RuntimeService runtimeService;
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml"})
  public void shouldLogJobsQueuedForExecution() {
    // replace job executor with one that has custom threadpool executor settings
    JobExecutionLoggingTest.TestJobExecutor testJobExecutor = new JobExecutionLoggingTest.TestJobExecutor();
    testJobExecutor.setMaxJobsPerAcquisition(10);
    processEngineConfiguration.setJobExecutor(testJobExecutor);
    testJobExecutor.registerProcessEngine(processEngineConfiguration.getProcessEngine());

    // given five jobs
    for (int i = 0; i < 5; i++) {
      runtimeService.startProcessInstanceByKey("simpleAsyncProcess");
    }

    // when executing the jobs
    processEngineConfiguration.getJobExecutor().start();
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    processEngineConfiguration.getJobExecutor().shutdown();

    // look for filled queue logs
    List<ILoggingEvent> filteredLogList = loggingRule.getFilteredLog("Jobs currently in queue to be executed for the process engine 'default': 2 (out of the max queue size : 2)");

    // then 3 instances of filled queue logs will be available as there is 2 additional threads possible to reach max-pool-size
    assertThat(filteredLogList.size()).isGreaterThan(0);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml"})
  public void shouldLogJobsInExecution() {
    // replace job executor with one that has custom threadpool executor settings
    JobExecutionLoggingTest.TestJobExecutor testJobExecutor = new JobExecutionLoggingTest.TestJobExecutor();
    testJobExecutor.setMaxJobsPerAcquisition(10);
    processEngineConfiguration.setJobExecutor(testJobExecutor);
    testJobExecutor.registerProcessEngine(processEngineConfiguration.getProcessEngine());

    // given five jobs
    for (int i = 0; i < 5; i++) {
      runtimeService.startProcessInstanceByKey("simpleAsyncProcess");
    }

    // when executing the jobs
    processEngineConfiguration.getJobExecutor().start();
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    processEngineConfiguration.getJobExecutor().shutdown();

    // look for 3 jobs in execution
    List<ILoggingEvent> filteredLogList = loggingRule.getFilteredLog("Jobs currently in execution for the process engine 'default' : 3");

    // then one count of 'three jobs in execution' will be available as 2 out of 5 jobs should be queued.
    assertThat(filteredLogList.size()).isEqualTo(1);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml"})
  public void shouldLogAvailableJobExecutionThreads() {
    // replace job executor with one that has custom threadpool executor settings
    JobExecutionLoggingTest.TestJobExecutor testJobExecutor = new JobExecutionLoggingTest.TestJobExecutor();
    testJobExecutor.setMaxJobsPerAcquisition(10);
    processEngineConfiguration.setJobExecutor(testJobExecutor);
    testJobExecutor.registerProcessEngine(processEngineConfiguration.getProcessEngine());

    // given five jobs
    for (int i = 0; i < 5; i++) {
      runtimeService.startProcessInstanceByKey("simpleAsyncProcess");
    }

    // when executing the jobs
    processEngineConfiguration.getJobExecutor().start();
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    processEngineConfiguration.getJobExecutor().shutdown();

    // look for 2 count of available job execution threads
    List<ILoggingEvent> filteredLogList = loggingRule.getFilteredLog("Available job execution threads for the process engine 'default' : 2");

    // then 2 threads will be available for 3 number of times because of queuing action post the core-pool-size limit is met
    assertThat(filteredLogList.size()).isEqualTo(3);
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/jobexecutor/delegateThrowsException.bpmn20.xml" })
  public void shouldLogJobExecutionRejections() {
    // given three jobs
    for (int i = 0; i < 3; i++) {
      runtimeService.startProcessInstanceByKey("testProcess");
    }

    // replace job executor with one that rejects all jobs
    JobExecutionLoggingTest.RejectionJobExecutor rejectionExecutor = new JobExecutionLoggingTest.RejectionJobExecutor();
    processEngineConfiguration.setJobExecutor(rejectionExecutor);
    rejectionExecutor.registerProcessEngine(processEngineConfiguration.getProcessEngine());

    // when executing the jobs
    processEngineConfiguration.getJobExecutor().start();
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    processEngineConfiguration.getJobExecutor().shutdown();

    // look for job execution rejection job count with one job
    List<ILoggingEvent> filteredLogList = loggingRule.getFilteredLog("Jobs execution rejections for the process engine 'default' : 1");

    // then there exist job execution rejection logs with job count as one
    assertThat(filteredLogList.size()).isGreaterThan(0);
  }

  public static class TestJobExecutor extends DefaultJobExecutor {

    protected int queueSize = 2;
    protected int corePoolSize = 1;
    protected int maxPoolSize = 3;
    protected BlockingQueue<Runnable> threadPoolQueue;
    public TestJobExecutor() {
      threadPoolQueue = new ArrayBlockingQueue<>(queueSize);
      threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, threadPoolQueue);
      threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
    }
  }

  public static class RejectionJobExecutor extends TestJobExecutor {
    public RejectionJobExecutor() {
      threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, threadPoolQueue) {
        @Override
        public void execute(Runnable command) {
          throw new RejectedExecutionException();
        }
      };
      rejectedJobsHandler = new CallerRunsRejectedJobsHandler();
    }
  }

}
