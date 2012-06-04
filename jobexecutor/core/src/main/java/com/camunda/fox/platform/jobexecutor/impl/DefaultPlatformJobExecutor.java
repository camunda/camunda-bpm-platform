package com.camunda.fox.platform.jobexecutor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.interceptor.CommandExecutor;

/**
 * {@link PlatformJobExecutor} Implementation using self-managed Threads 
 * 
 * @author Daniel Meyer
 */
public class DefaultPlatformJobExecutor extends PlatformJobExecutor {
  
  private static Logger log = Logger.getLogger(DefaultPlatformJobExecutor.class.getName());

  protected int queueSize = 3;
  protected int corePoolSize = 3;
  private int maxPoolSize = 10;
  
  protected BlockingQueue<Runnable> threadPoolQueue;
  protected ThreadPoolExecutor threadPoolExecutor;
  protected List<Thread> jobAcquisitionThreads;
  
  public void start() { 
    if (threadPoolQueue==null) {
      threadPoolQueue = new ArrayBlockingQueue<Runnable>(queueSize);
    }
    if (threadPoolExecutor==null) {
      threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, threadPoolQueue);      
      threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }
    
    jobAcquisitionThreads = new ArrayList<Thread>();
    
    super.start();
  }
  
  @Override
  public void stop() {
    
     for (Thread jobAcquisitionThread : jobAcquisitionThreads) {
       try {
         jobAcquisitionThread.join();
       }catch (InterruptedException e) {
         log.log(Level.WARNING, "Interrupted while waiting for the job Acquisition thread to terminate", e);
       }      
    }

    // Ask the thread pool to finish and exit
    threadPoolExecutor.shutdown();

    // Waits for 1 minute to finish all currently executing jobs
    try {
      if (!threadPoolExecutor.awaitTermination(60L, TimeUnit.SECONDS)) {
        log.log(Level.WARNING, "Timeout during shutdown of job executor. "
                + "The current running jobs could not end within 60 seconds after shutdown operation.");
      }
    } catch (InterruptedException e) {
      log.log(Level.WARNING, "Interrupted while shutting down the job executor. ", e);
    }
    
    threadPoolExecutor = null;
    jobAcquisitionThreads = null;
    
    super.stop();
  }
  
  public Object scheduleAcquisition(Runnable acquisitionRunnable) {
    Thread acquisitionThread = new Thread(acquisitionRunnable);
    acquisitionThread.start();
    jobAcquisitionThreads.add(acquisitionThread);
    return acquisitionThread;
  }
  
  public void unscheduleAcquisition(Object scheduledAcquisition) {
    if (scheduledAcquisition instanceof Thread) {
      Thread jobAcquisitionThread = (Thread) scheduledAcquisition;
      try {
        jobAcquisitionThread.join();
      }catch (InterruptedException e) {
        log.log(Level.WARNING, "Interrupted while waiting for the job Acquisition thread to terminate", e);
      }      
      jobAcquisitionThreads.remove(jobAcquisitionThread);
    }
  }

  @Override
  public void executeJobs(List<String> jobIds, CommandExecutor commandExecutor) {
      threadPoolExecutor.execute(new PlatformExecuteJobsRunnable(jobIds, commandExecutor));
  }

}
