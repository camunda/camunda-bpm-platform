package org.camunda.bpm.engine.test.jobexecutor;

import java.text.DateFormat.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.SequentialJobAcquisitionRunnable;
import org.camunda.bpm.engine.impl.util.ClockUtil;
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
  private List<ProcessEngine> createdProcessEngines = new ArrayList<ProcessEngine>();

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

    Thread.sleep(2000);

    Assert.assertFalse(((SequentialJobAcquisitionRunnable) jobExecutor.getAcquireJobsRunnable()).isJobAdded());

    Assert.assertEquals(0, engine1.getManagementService().createJobQuery().count());
    Assert.assertEquals(0, engine2.getManagementService().createJobQuery().count());
  }


  ////////// helper methods ////////////////////////////


  public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis, JobExecutor jobExecutor, ManagementService managementService, boolean shutdown) {

    try {
      Timer timer = new Timer();
      InteruptTask task = new InteruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean areJobsAvailable = true;
      try {
        while (areJobsAvailable && !task.isTimeLimitExceeded()) {
          Thread.sleep(intervalMillis);
          areJobsAvailable = areJobsAvailable(managementService);
        }
      } catch (InterruptedException e) {
      } finally {
        timer.cancel();
      }
      if (areJobsAvailable) {
        throw new ProcessEngineException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      if (shutdown) {
        jobExecutor.shutdown();
      }
    }
  }

  public boolean areJobsAvailable(ManagementService managementService) {
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
    public void run() {
      timeLimitExceeded = true;
      thread.interrupt();
    }
  }

}
