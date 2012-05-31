package com.camunda.fox.platform.jobexecutorimpl.test;

import java.text.DateFormat.Field;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import junit.framework.Assert;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.util.ClockUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.camunda.fox.platform.jobexecutor.impl.DefaultPlatformJobExecutor;
import com.camunda.fox.platform.jobexecutor.impl.acquisition.SequentialJobAcquisitionRunnable;
import com.camunda.fox.platform.jobexecutor.impl.util.JobAcquisitionConfigurationBean;
import com.camunda.fox.platform.jobexecutor.spi.JobAcquisitionStrategy;

/**
 * 
 * @author Daniel Meyer
 * 
 */
@RunWith(Parameterized.class)
public class TestPlatformJobExecutorFunctional {
  
  @Parameters
  public static List<String[]> strategies() {
    return Arrays.asList(new String[][]{
      {JobAcquisitionStrategy.SEQUENTIAL},
//      {JobAcquisitionStrategy.SIMULTANEOUS}
    });    
  }

    
  protected DefaultPlatformJobExecutor platformJobExecutor;

  public TestPlatformJobExecutorFunctional(String strategy) {
    platformJobExecutor = new DefaultPlatformJobExecutor();    
    
    JobAcquisitionConfigurationBean acquisitionConfiguration = new JobAcquisitionConfigurationBean();
    acquisitionConfiguration.setAcquisitionName("default");
    acquisitionConfiguration.setJobAcquisitionStrategy(strategy);
    acquisitionConfiguration.setLockOwner(UUID.randomUUID().toString());
    acquisitionConfiguration.setLockTimeInMillis(5*60*1000);
    acquisitionConfiguration.setMaxJobsPerAcquisition(3);
    acquisitionConfiguration.setWaitTimeInMillis(30);
    
    // start the platform job executor
    platformJobExecutor.start();
    platformJobExecutor.startJobAcquisition(acquisitionConfiguration);
  }
  
  @After
  public void stopPlatformJobexecutor() {
    platformJobExecutor.stop();
  }
  
  @Test
  public void testExecuteJobsForSingleEngine() {
    // configure and build a process engine
    StandaloneProcessEngineConfiguration standaloneProcessEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    standaloneProcessEngineConfiguration.setJobExecutorActivate(false);
    ProcessEngine engine = standaloneProcessEngineConfiguration.buildProcessEngine();
    
    // register the process engine with the platform job executor
    JobExecutor jobExecutor = platformJobExecutor.registerProcessEngine(standaloneProcessEngineConfiguration, "default");
    standaloneProcessEngineConfiguration.setJobExecutor(jobExecutor);
        
    engine.getRepositoryService().createDeployment()
      .addClasspathResource("IntermediateTimerEventTest.testCatchingTimerEvent.bpmn20.xml")
      .deploy();
    
    jobExecutor.shutdown();
    
    engine.getRuntimeService()
      .startProcessInstanceByKey("intermediateTimerEventExample");
    
    Assert.assertEquals(1, engine.getManagementService().createJobQuery().count());
    
    try {
      Calendar calendar = Calendar.getInstance();
      calendar.add(Field.DAY_OF_YEAR.getCalendarField(), 6);
      ClockUtil.setCurrentTime(calendar.getTime());
      jobExecutor.start();
      waitForJobExecutorToProcessAllJobs(10000, 100, jobExecutor, engine.getManagementService(), true);
      
      Assert.assertEquals(0, engine.getManagementService().createJobQuery().count());
      
    }finally {
      ClockUtil.reset();
      engine.close();
    }
    
  }
  
  @Test
  public void testExecuteJobsForTwoEnginesSameAcquisition() {
    // configure and build a process engine
    StandaloneProcessEngineConfiguration engineConfiguration1 = new StandaloneInMemProcessEngineConfiguration();
    engineConfiguration1.setProcessEngineName("engine1");
    engineConfiguration1.setJdbcUrl("jdbc:h2:mem:activiti1");
    engineConfiguration1.setJobExecutorActivate(false);
    ProcessEngine engine1 = engineConfiguration1.buildProcessEngine();
    
    // and a second one
    StandaloneProcessEngineConfiguration engineConfiguration2 = new StandaloneInMemProcessEngineConfiguration();
    engineConfiguration2.setProcessEngineName("engine2");
    engineConfiguration2.setJdbcUrl("jdbc:h2:mem:activiti2");
    engineConfiguration2.setJobExecutorActivate(false);
    ProcessEngine engine2 = engineConfiguration2.buildProcessEngine();
    
    // register the first process engine with the platform job executor
    JobExecutor jobExecutor1 = platformJobExecutor.registerProcessEngine(engineConfiguration1, "default");
    engineConfiguration1.setJobExecutor(jobExecutor1);
            
    // register the second process engine with the platform job executor
    JobExecutor jobExecutor2 = platformJobExecutor.registerProcessEngine(engineConfiguration2, "default");
    engineConfiguration2.setJobExecutor(jobExecutor2);
        
    // stop the acquisition
    platformJobExecutor.getJobAcquisitionByName("default").shutdown();
    
    // deploy the processes
    
    engine1.getRepositoryService().createDeployment()
      .addClasspathResource("IntermediateTimerEventTest.testCatchingTimerEvent.bpmn20.xml")
      .deploy();
    
    engine2.getRepositoryService().createDeployment()
     .addClasspathResource("IntermediateTimerEventTest.testCatchingTimerEvent.bpmn20.xml")
     .deploy();
    
    // start one instance for each engine:
        
    engine1.getRuntimeService().startProcessInstanceByKey("intermediateTimerEventExample");
    engine2.getRuntimeService().startProcessInstanceByKey("intermediateTimerEventExample");
    
    Assert.assertEquals(1, engine1.getManagementService().createJobQuery().count());
    Assert.assertEquals(1, engine2.getManagementService().createJobQuery().count());
    
    try {
      Calendar calendar = Calendar.getInstance();
      calendar.add(Field.DAY_OF_YEAR.getCalendarField(), 6);
      ClockUtil.setCurrentTime(calendar.getTime());
      
      // assert task completed for the first engine
      jobExecutor1.start();
      waitForJobExecutorToProcessAllJobs(10000, 100, jobExecutor1, engine1.getManagementService(), true);
      
      // assert task completed for the second engine
      jobExecutor2.start();
      waitForJobExecutorToProcessAllJobs(10000, 100, jobExecutor2, engine2.getManagementService(), true);
      
      Assert.assertEquals(0, engine1.getManagementService().createJobQuery().count());
      Assert.assertEquals(0, engine2.getManagementService().createJobQuery().count());
      
    }finally {
      ClockUtil.reset();
      engine1.close();
      engine2.close();
    }    
  }
  
  
  @Test
  public void testJobAddedGuardForTwoEnginesSameAcquisition() throws InterruptedException {
    // configure and build a process engine
    StandaloneProcessEngineConfiguration engineConfiguration1 = new StandaloneInMemProcessEngineConfiguration();
    engineConfiguration1.setProcessEngineName("engine1");
    engineConfiguration1.setJdbcUrl("jdbc:h2:mem:activiti1");
    engineConfiguration1.setJobExecutorActivate(false);
    ProcessEngine engine1 = engineConfiguration1.buildProcessEngine();
    
    // and a second one
    StandaloneProcessEngineConfiguration engineConfiguration2 = new StandaloneInMemProcessEngineConfiguration();
    engineConfiguration2.setProcessEngineName("engine2");
    engineConfiguration2.setJdbcUrl("jdbc:h2:mem:activiti2");
    engineConfiguration2.setJobExecutorActivate(false);
    ProcessEngine engine2 = engineConfiguration2.buildProcessEngine();
    
    // register the first process engine with the platform job executor
    JobExecutor jobExecutor1 = platformJobExecutor.registerProcessEngine(engineConfiguration1, "default");
    engineConfiguration1.setJobExecutor(jobExecutor1);
            
    // register the second process engine with the platform job executor
    JobExecutor jobExecutor2 = platformJobExecutor.registerProcessEngine(engineConfiguration2, "default");
    engineConfiguration2.setJobExecutor(jobExecutor2);
        
    // deploy the processes
    
    engine1.getRepositoryService().createDeployment()
      .addClasspathResource("IntermediateTimerEventTest.testCatchingTimerEvent.bpmn20.xml")
      .deploy();
    
    engine2.getRepositoryService().createDeployment()
     .addClasspathResource("IntermediateTimerEventTest.testCatchingTimerEvent.bpmn20.xml")
     .deploy();
    
    try {
      // start one instance for each engine:
      
      engine1.getRuntimeService().startProcessInstanceByKey("intermediateTimerEventExample");
      engine2.getRuntimeService().startProcessInstanceByKey("intermediateTimerEventExample");
      
      Calendar calendar = Calendar.getInstance();
      calendar.add(Field.DAY_OF_YEAR.getCalendarField(), 6);
      ClockUtil.setCurrentTime(calendar.getTime());
      
      Assert.assertEquals(1, engine1.getManagementService().createJobQuery().count());
      Assert.assertEquals(1, engine2.getManagementService().createJobQuery().count());
          
      // assert task completed for the first engine
      jobExecutor1.start();
      waitForJobExecutorToProcessAllJobs(10000, 100, jobExecutor1, engine1.getManagementService(), false);
      
      // assert task completed for the second engine
      jobExecutor2.start();
      waitForJobExecutorToProcessAllJobs(10000, 100, jobExecutor2, engine2.getManagementService(), false);
      
      Thread.sleep(2000);
      
      Assert.assertFalse(((SequentialJobAcquisitionRunnable) platformJobExecutor.getJobAcquisitionByName("default").getAcquireJobsRunnable()).isJobAdded());
      
      Assert.assertEquals(0, engine1.getManagementService().createJobQuery().count());
      Assert.assertEquals(0, engine2.getManagementService().createJobQuery().count());
      
      
    }finally {
      jobExecutor1.shutdown();
      
      ClockUtil.reset();
      engine1.close();
      engine2.close();
    }    
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
        throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
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
