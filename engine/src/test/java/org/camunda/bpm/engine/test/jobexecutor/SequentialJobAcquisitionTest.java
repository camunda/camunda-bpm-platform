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

import java.text.DateFormat.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.ThreadPoolJobExecutor;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Daniel Meyer
 *
 */
public class SequentialJobAcquisitionTest {

  private static final String RESOURCE_BASE = SequentialJobAcquisitionTest.class.getPackage().getName().replace(".", "/");
  private static final String PROCESS_RESOURCE = RESOURCE_BASE + "/IntermediateTimerEventTest.testCatchingTimerEvent.bpmn20.xml";

  private JobExecutor jobExecutor = new DefaultJobExecutor();
  private List<ProcessEngine> createdProcessEngines = new ArrayList<>();

  @After
  public void stopJobExecutor() {
    jobExecutor.shutdown();
  }

  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  @After
  public void closeProcessEngines() {
    Iterator<ProcessEngine> iterator = createdProcessEngines.iterator();
    while (iterator.hasNext()) {
      ProcessEngine processEngine = iterator.next();
      processEngine.close();
      ProcessEngines.unregister(processEngine);
      iterator.remove();
    }
  }

  @Test
  public void testExecuteJobsForSingleEngine() {
    // configure and build a process engine
    StandaloneProcessEngineConfiguration standaloneProcessEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    standaloneProcessEngineConfiguration.setProcessEngineName(getClass().getName() + "-engine1");
    standaloneProcessEngineConfiguration.setJdbcUrl("jdbc:h2:mem:jobexecutor-test-engine");
    standaloneProcessEngineConfiguration.setJobExecutorActivate(false);
    standaloneProcessEngineConfiguration.setJobExecutor(jobExecutor);
    standaloneProcessEngineConfiguration.setDbMetricsReporterActivate(false);

    ProcessEngine engine = standaloneProcessEngineConfiguration.buildProcessEngine();

    createdProcessEngines.add(engine);

    engine.getRepositoryService().createDeployment()
      .addClasspathResource(PROCESS_RESOURCE)
      .deploy();

    jobExecutor.shutdown();

    engine.getRuntimeService()
      .startProcessInstanceByKey("intermediateTimerEventExample");

    Assert.assertEquals(1, engine.getManagementService().createJobQuery().count());

    Calendar calendar = Calendar.getInstance();
    calendar.add(Field.DAY_OF_YEAR.getCalendarField(), 6);
    ClockUtil.setCurrentTime(calendar.getTime());
    jobExecutor.start();
    waitForJobExecutorToProcessAllJobs(10000, 100, jobExecutor, engine.getManagementService(), true);

    Assert.assertEquals(0, engine.getManagementService().createJobQuery().count());
  }

  @Test
  public void testExecuteJobsForTwoEnginesSameAcquisition() {
    // configure and build a process engine
    StandaloneProcessEngineConfiguration engineConfiguration1 = new StandaloneInMemProcessEngineConfiguration();
    engineConfiguration1.setProcessEngineName(getClass().getName() + "-engine1");
    engineConfiguration1.setJdbcUrl("jdbc:h2:mem:activiti1");
    engineConfiguration1.setJobExecutorActivate(false);
    engineConfiguration1.setJobExecutor(jobExecutor);
    engineConfiguration1.setDbMetricsReporterActivate(false);

    ProcessEngine engine1 = engineConfiguration1.buildProcessEngine();
    createdProcessEngines.add(engine1);

    // and a second one
    StandaloneProcessEngineConfiguration engineConfiguration2 = new StandaloneInMemProcessEngineConfiguration();
    engineConfiguration2.setProcessEngineName(getClass().getName() + "engine2");
    engineConfiguration2.setJdbcUrl("jdbc:h2:mem:activiti2");
    engineConfiguration2.setJobExecutorActivate(false);
    engineConfiguration2.setJobExecutor(jobExecutor);
    engineConfiguration2.setDbMetricsReporterActivate(false);
    engineConfiguration2.setEnforceHistoryTimeToLive(false);

    ProcessEngine engine2 = engineConfiguration2.buildProcessEngine();

    createdProcessEngines.add(engine2);

    // stop the acquisition
    jobExecutor.shutdown();

    // deploy the processes

    engine1.getRepositoryService().createDeployment()
      .addClasspathResource(PROCESS_RESOURCE)
      .deploy();

    engine2.getRepositoryService().createDeployment()
     .addClasspathResource(PROCESS_RESOURCE)
     .deploy();

    // start one instance for each engine:

    engine1.getRuntimeService().startProcessInstanceByKey("intermediateTimerEventExample");
    engine2.getRuntimeService().startProcessInstanceByKey("intermediateTimerEventExample");

    Assert.assertEquals(1, engine1.getManagementService().createJobQuery().count());
    Assert.assertEquals(1, engine2.getManagementService().createJobQuery().count());

    Calendar calendar = Calendar.getInstance();
    calendar.add(Field.DAY_OF_YEAR.getCalendarField(), 6);
    ClockUtil.setCurrentTime(calendar.getTime());

    jobExecutor.start();
    // assert task completed for the first engine
    waitForJobExecutorToProcessAllJobs(10000, 100, jobExecutor, engine1.getManagementService(), true);

    jobExecutor.start();
    // assert task completed for the second engine
    waitForJobExecutorToProcessAllJobs(10000, 100, jobExecutor, engine2.getManagementService(), true);

    Assert.assertEquals(0, engine1.getManagementService().createJobQuery().count());
    Assert.assertEquals(0, engine2.getManagementService().createJobQuery().count());
  }

  @Test
  public void testJobAddedGuardForTwoEnginesSameAcquisition() throws InterruptedException {
    // configure and build a process engine
    StandaloneProcessEngineConfiguration engineConfiguration1 = new StandaloneInMemProcessEngineConfiguration();
    engineConfiguration1.setProcessEngineName(getClass().getName() + "-engine1");
    engineConfiguration1.setJdbcUrl("jdbc:h2:mem:activiti1");
    engineConfiguration1.setJobExecutorActivate(false);
    engineConfiguration1.setJobExecutor(jobExecutor);
    engineConfiguration1.setDbMetricsReporterActivate(false);

    ProcessEngine engine1 = engineConfiguration1.buildProcessEngine();
    createdProcessEngines.add(engine1);

    // and a second one
    StandaloneProcessEngineConfiguration engineConfiguration2 = new StandaloneInMemProcessEngineConfiguration();
    engineConfiguration2.setProcessEngineName(getClass().getName() + "engine2");
    engineConfiguration2.setJdbcUrl("jdbc:h2:mem:activiti2");
    engineConfiguration2.setJobExecutorActivate(false);
    engineConfiguration2.setJobExecutor(jobExecutor);
    engineConfiguration2.setDbMetricsReporterActivate(false);

    ProcessEngine engine2 = engineConfiguration2.buildProcessEngine();
    createdProcessEngines.add(engine2);

    // stop the acquisition
    jobExecutor.shutdown();

    // deploy the processes

    engine1.getRepositoryService().createDeployment()
      .addClasspathResource(PROCESS_RESOURCE)
      .deploy();

    engine2.getRepositoryService().createDeployment()
     .addClasspathResource(PROCESS_RESOURCE)
     .deploy();

    // start one instance for each engine:

    engine1.getRuntimeService().startProcessInstanceByKey("intermediateTimerEventExample");
    engine2.getRuntimeService().startProcessInstanceByKey("intermediateTimerEventExample");

    Calendar calendar = Calendar.getInstance();
    calendar.add(Field.DAY_OF_YEAR.getCalendarField(), 6);
    ClockUtil.setCurrentTime(calendar.getTime());

    Assert.assertEquals(1, engine1.getManagementService().createJobQuery().count());
    Assert.assertEquals(1, engine2.getManagementService().createJobQuery().count());

    // assert task completed for the first engine
    jobExecutor.start();
    waitForJobExecutorToProcessAllJobs(10000, 100, jobExecutor, engine1.getManagementService(), false);

    // assert task completed for the second engine
    jobExecutor.start();
    waitForJobExecutorToProcessAllJobs(10000, 100, jobExecutor, engine2.getManagementService(), false);

    waitForJobExecutionRunnablesToFinish(10000, 100, jobExecutor);

    Thread.sleep(2000);

    Assert.assertFalse(jobExecutor.getAcquireJobsRunnable().isJobAdded());

    Assert.assertEquals(0, engine1.getManagementService().createJobQuery().count());
    Assert.assertEquals(0, engine2.getManagementService().createJobQuery().count());
  }

  ////////// helper methods ////////////////////////////


  protected void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis, JobExecutor jobExecutor,
      ManagementService managementService, boolean shutdown) {
    try {
      waitForCondition(maxMillisToWait, intervalMillis, () -> !areJobsAvailable(managementService));
    } finally {
      if (shutdown) {
        jobExecutor.shutdown();
      }
    }
  }

  protected void waitForJobExecutionRunnablesToFinish(long maxMillisToWait, long intervalMillis, JobExecutor jobExecutor) {
    waitForCondition(maxMillisToWait, intervalMillis,
        () -> ((ThreadPoolJobExecutor) jobExecutor).getThreadPoolExecutor().getActiveCount() == 0);
  }

  protected void waitForCondition(long maxMillisToWait, long intervalMillis, Supplier<Boolean> conditionSupplier) {
    boolean conditionFulfilled = false;
    Timer timer = new Timer();
    InteruptTask task = new InteruptTask(Thread.currentThread());
    timer.schedule(task, maxMillisToWait);
    try {
      while (!conditionFulfilled && !task.isTimeLimitExceeded()) {
        Thread.sleep(intervalMillis);
        conditionFulfilled = conditionSupplier.get();
      }
    } catch (InterruptedException e) {
    } finally {
      timer.cancel();
    }
    if (!conditionFulfilled) {
      throw new ProcessEngineException("time limit of " + maxMillisToWait + " was exceeded");
    }
  }

  protected boolean areJobsAvailable(ManagementService managementService) {
    return !managementService
      .createJobQuery()
      .executable()
      .list()
      .isEmpty();
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

  protected List<String> assertProcessInstanceJobs(ProcessEngine engine, ProcessInstance pi, int nJobs, String pdKey) {
    var jobs = engine.getManagementService().createJobQuery()
        .rootProcessInstanceId(pi.getId())
        .list();

    assertThat(jobs).hasSize(nJobs);

    jobs.forEach(job -> {
      assertThat(job.getProcessDefinitionKey()).isEqualTo(pdKey);
      assertThat(job.getRootProcessInstanceId()).isEqualTo(pi.getId());
    });

    return jobs.stream()
        .map(Job::getId)
        .collect(Collectors.toList());
  }

  public static class AssertJobExecutor extends DefaultJobExecutor {

    final List<List<String>> jobGroups = new ArrayList<>();

    @SafeVarargs
    public final void assertJobGroup(List<String>... jobIds) {
      assertThat(jobGroups).containsExactlyInAnyOrder(jobIds);
    }

    @Override
    public void executeJobs(List<String> jobIds, ProcessEngineImpl processEngine) {
      super.executeJobs(jobIds, processEngine);

      System.out.println("jobIds = " + jobIds);
      jobGroups.add(jobIds);
    }
  }

  @Test
  public void testJobAcquisitionWithCallActivitySubProcesses() {
    // configure job executor
    var jobExecutor = new AssertJobExecutor();
    jobExecutor.setMaxJobsPerAcquisition(10);

    // configure and build a process engine
    var engineConfig = new StandaloneInMemProcessEngineConfiguration()
        .setProcessEngineName(getClass().getName() + "-engine")
        .setJdbcUrl("jdbc:h2:mem:activiti1")
        .setJobExecutorActivate(false)
        .setJobExecutor(jobExecutor)
        .setDbMetricsReporterActivate(false)
        .setEnableExclusivenessAcrossProcessInstances(true); // enable exclusiveness

    ProcessEngine engine = engineConfig.buildProcessEngine();
    createdProcessEngines.add(engine);
    jobExecutor.registerProcessEngine((ProcessEngineImpl) engine);

    // given
    var subModel = Bpmn.createExecutableProcess("subProcess")
        .startEvent()
          .scriptTask("scriptTask")
            .camundaAsyncBefore()
            .camundaExclusive(true)
            .scriptFormat("javascript")
            .scriptText("console.log(execution.getJobs())")
        .endEvent()
        .done();

    var rootModel = Bpmn.createExecutableProcess("rootProcess")
        .startEvent()
          .callActivity("callActivity")
            .calledElement("subProcess")
            .multiInstance()
              .parallel()
              .cardinality("2")
            .multiInstanceDone()
        .endEvent()
        .done();

    var deployment = engine.getRepositoryService()
        .createDeployment()
        .addModelInstance("subProcess.bpmn", subModel)
        .addModelInstance("rootProcess.bpmn", rootModel)
        .deploy();

    // when
    var pi1 = engine.getRuntimeService().startProcessInstanceByKey("rootProcess");
    var pi2 = engine.getRuntimeService().startProcessInstanceByKey("rootProcess");

    // then 4 jobs are created (2 for each root process due to cardinality)
    assertThat(engine.getManagementService().createJobQuery().list()).hasSize(4);

    var pi1Jobs = assertProcessInstanceJobs(engine, pi1, 2, "subProcess");
    var pi2Jobs = assertProcessInstanceJobs(engine, pi2, 2, "subProcess");

    // when
    jobExecutor.start();
    waitForJobExecutorToProcessAllJobs(10000, 100, jobExecutor, engine.getManagementService(), true);

    // then
    jobExecutor.assertJobGroup(pi1Jobs, pi2Jobs);

    // cleanup
    engine.getRepositoryService().deleteDeployment(deployment.getId(), true);
  }

  @Test
  public void testJobAcquisitionWithMultiHierarchySubProcesses() {
    // configure job executor
    var jobExecutor = new AssertJobExecutor();
    jobExecutor.setMaxJobsPerAcquisition(10);

    // configure and build a process engine
    var engineConfig = new StandaloneInMemProcessEngineConfiguration()
        .setProcessEngineName(getClass().getName() + "-engine")
        .setJdbcUrl("jdbc:h2:mem:activiti1")
        .setJobExecutorActivate(false)
        .setJobExecutor(jobExecutor)
        .setDbMetricsReporterActivate(false)
        .setEnableExclusivenessAcrossProcessInstances(true); // enable exclusiveness

    ProcessEngine engine = engineConfig.buildProcessEngine();
    createdProcessEngines.add(engine);

    // given
    var subSubModel = Bpmn.createExecutableProcess("subSubProcess")
        .startEvent()
          .scriptTask("scriptTask")
            .camundaAsyncBefore()
            .camundaExclusive(true)
            .scriptFormat("javascript")
            .scriptText("console.log(execution.getJobs())")
        .endEvent()
        .done();

    var subModel = Bpmn.createExecutableProcess("subProcess")
        .startEvent()
          .callActivity("callActivity")
            .calledElement("subSubProcess")
            .multiInstance()
              .parallel()
              .cardinality("2")
            .multiInstanceDone()
        .endEvent()
        .done();

    var rootModel = Bpmn.createExecutableProcess("rootProcess")
        .startEvent()
          .callActivity("callActivity")
            .calledElement("subProcess")
            .multiInstance()
              .parallel()
              .cardinality("2")
            .multiInstanceDone()
        .endEvent()
        .done();

    var deployment = engine.getRepositoryService()
        .createDeployment()
        .addModelInstance("subSubProcess.bpmn", subSubModel)
        .addModelInstance("subProcess.bpmn", subModel)
        .addModelInstance("rootProcess.bpmn", rootModel)
        .deploy();

    // when
    var pi1 = engine.getRuntimeService().startProcessInstanceByKey("rootProcess");
    var pi2 = engine.getRuntimeService().startProcessInstanceByKey("rootProcess");

    // then
    // 4 jobs of subSubProcess are waiting with the rootProcess's id as rootProcessInstanceId
    assertThat(engine.getManagementService().createJobQuery().list()).hasSize(8);

    var pi1Jobs = assertProcessInstanceJobs(engine, pi1, 4, "subSubProcess");
    var pi2Jobs = assertProcessInstanceJobs(engine, pi2, 4, "subSubProcess");

    jobExecutor.start();
    waitForJobExecutorToProcessAllJobs(10000, 100, jobExecutor, engine.getManagementService(), true);

    // then
    jobExecutor.assertJobGroup(pi1Jobs, pi2Jobs);

    // cleanup
    engine.getRepositoryService().deleteDeployment(deployment.getId(), true);
  }

}
