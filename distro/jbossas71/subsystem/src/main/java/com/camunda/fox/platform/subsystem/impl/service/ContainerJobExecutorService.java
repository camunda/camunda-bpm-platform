package com.camunda.fox.platform.subsystem.impl.service;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.jboss.as.threads.ManagedQueueExecutorService;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jboss.threads.ExecutionTimedOutException;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.jobexecutor.impl.PlatformJobExecutor;
import com.camunda.fox.platform.subsystem.impl.service.execution.ContainerExecuteJobsRunnable;

public class ContainerJobExecutorService extends PlatformJobExecutor implements Service<ContainerJobExecutorService> {

  private static Logger log = Logger.getLogger(ContainerJobExecutorService.class.getName());
  
  private final InjectedValue<ManagedQueueExecutorService> managedQueueInjector = new InjectedValue<ManagedQueueExecutorService>();
  
  private long lastWarningLogged = System.currentTimeMillis();

  public ContainerJobExecutorService getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }

  public void start(StartContext context) throws StartException {
    log.info("Starting Container Job Executor Service");
    // start the job executor
    start();
  }

  public void stop(StopContext context) {
    log.info("Stopping Container Job Executor Service");
    // stop the job executor
    stop();
  }
  
  public static ServiceName getServiceName() {
    return ServiceName.of("foxPlatform", "containerJobExecutorService");
  }

  public void executeJobs(List<String> jobIds, CommandExecutor commandExecutor) {
    final ManagedQueueExecutorService managedQueueExecutorService = managedQueueInjector.getValue();

    boolean rejected = false;
    try {
      
      // wait for 2 seconds for the job to be accepted by the pool.
      managedQueueExecutorService.executeBlocking(new ContainerExecuteJobsRunnable(jobIds, commandExecutor), 2, TimeUnit.SECONDS);
      
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
    
    if(rejected) {
      // if the job is rejected, execute in the caller thread (Acquisition Thread)
      // TODO: check rejectedExecutionException policy, see DefaultPlatformJobExecutor.class
      new ContainerExecuteJobsRunnable(jobIds, commandExecutor).run();
    }
  }

  public Object scheduleAcquisition(Runnable acquisitionRunnable) {
    
    ManagedQueueExecutorService managedQueueExecutorService = managedQueueInjector.getValue();
    
    try {
      
      managedQueueExecutorService.executeBlocking(acquisitionRunnable);
      
    }catch (InterruptedException e) {
      // the the acquisition thread is interrupted, this probably means the app server is turning the lights off -> ignore          
    } catch (Exception e) {      
      // we must be able to schedule this
      throw new FoxPlatformException("Can not schedule acquisition.", e);
    }
    
    return null;
  }

  public void unscheduleAcquisition(Object scheduledAcquisition) {
    // nothing to do here (the runnable simply terminates)
  }

  public InjectedValue<ManagedQueueExecutorService> getManagedQueueInjector() {
    return managedQueueInjector;
  }
    
}
