package org.camunda.bpm.container.impl.jboss.service;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobsRunnable;
import org.jboss.as.threads.ManagedQueueExecutorService;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jboss.threads.ExecutionTimedOutException;


public class MscExecutorService implements Service<MscExecutorService>, ExecutorService {

  private static Logger log = Logger.getLogger(MscExecutorService.class.getName());
  
  private final InjectedValue<ManagedQueueExecutorService> managedQueueInjector = new InjectedValue<ManagedQueueExecutorService>();
  
  private long lastWarningLogged = System.currentTimeMillis();

  public MscExecutorService getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }

  public void start(StartContext context) throws StartException {
    // nothing to do
  }

  public void stop(StopContext context) {
    // nothing to do    
  }
  
  public Runnable getExecuteJobsRunnable(List<String> jobIds, ProcessEngineImpl processEngine) {
    return new ExecuteJobsRunnable(jobIds, processEngine);
  }
  
  public boolean schedule(Runnable runnable, boolean isLongRunning) {
    
    if(isLongRunning) {
      return scheduleLongRunningWork(runnable);
      
    } else {      
      return scheduleShortRunningWork(runnable);
      
    }

  }

  protected boolean scheduleShortRunningWork(Runnable runnable) {

    ManagedQueueExecutorService managedQueueExecutorService = managedQueueInjector.getValue();
    
    try {
      
      managedQueueExecutorService.executeBlocking(runnable);
      return true;
      
    } catch (InterruptedException e) {
      // the the acquisition thread is interrupted, this probably means the app server is turning the lights off -> ignore          
    } catch (Exception e) {      
      // we must be able to schedule this
      log.log(Level.WARNING,  "Cannot schedule long running work.", e);
    }
    
    return false;
  }

  protected boolean scheduleLongRunningWork(Runnable runnable) {
    
    final ManagedQueueExecutorService managedQueueExecutorService = managedQueueInjector.getValue();

    boolean rejected = false;
    try {
      
      // wait for 2 seconds for the job to be accepted by the pool.
      managedQueueExecutorService.executeBlocking(runnable, 2, TimeUnit.SECONDS);
      
    } catch (InterruptedException e) {
      // the acquisition thread is interrupted, this probably means the app server is turning the lights off -> ignore          
    } catch (ExecutionTimedOutException e) {
      rejected = true;
    } catch (RejectedExecutionException e) {
      rejected = true;
    } catch (Exception e) {
      // if it fails for some other reason, log a warning message
      long now = System.currentTimeMillis();
      // only log every 60 seconds to prevent log flooding
      if((now-lastWarningLogged) >= (60*1000)) {
        log.log(Level.WARNING, "Unexpected Exception while submitting job to executor pool.", e);
      } else {
        log.log(Level.FINE, "Unexpected Exception while submitting job to executor pool.", e);
      }
    }
    
    return !rejected;
    
  }

  public InjectedValue<ManagedQueueExecutorService> getManagedQueueInjector() {
    return managedQueueInjector;
  }
    
}
