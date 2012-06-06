package com.camunda.fox.platform.subsystem.impl.platform;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;

import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.jboss.as.threads.ManagedQueueExecutorService;
import org.jboss.as.threads.ThreadsServices;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceListener;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.jobexecutor.impl.PlatformExecuteJobsRunnable;
import com.camunda.fox.platform.jobexecutor.impl.PlatformJobExecutor;


public class ContainerJobExecutorService extends PlatformJobExecutor implements Service<ContainerJobExecutorService> {

  private static Logger log = Logger.getLogger(ContainerJobExecutorService.class.getName());
  
  private final InjectedValue<ManagedQueueExecutorService> managedQueueInjector = new InjectedValue<ManagedQueueExecutorService>();
  private final InjectedValue<ProcessEngineControllerService> processEngineControllerInjector = new InjectedValue<ProcessEngineControllerService>();
  
  public static ServiceController<?> addService(final ServiceTarget target, final ServiceListener<Object> listeners, String jobExecutorThreadPoolName) {
    final ContainerJobExecutorService service = new ContainerJobExecutorService();
    return target.addService(ContainerJobExecutorService.getServiceName(), service)
        .addDependency(ThreadsServices.EXECUTOR.append(jobExecutorThreadPoolName), ManagedQueueExecutorService.class, service.getManagedQueueInjector())
        .addListener(listeners)
        .setInitialMode(Mode.ACTIVE)
        .install();
  }
  
  /**
   * empty constructor
   */
  public ContainerJobExecutorService() {
  }
  
  @Override
  public ContainerJobExecutorService getValue() throws IllegalStateException, IllegalArgumentException {
    return null;
  }

  @Override
  public void start(StartContext context) throws StartException {
    log.info("Starting Container Job Executor Service");
    start();
  }

  @Override
  public void stop(StopContext context) {
    log.info("Stopping Container Job Executor Service");
    stop();
  }
  
  public static ServiceName getServiceName() {
    return ServiceName.of("foxPlatform", "containerJobExecutorService");
  }

  @Override
  public void executeJobs(List<String> jobIds, CommandExecutor commandExecutor) {
    ManagedQueueExecutorService managedQueueExecutorService = managedQueueInjector.getValue();
    // TODO: check rejectedExecutionException policy, see DefaultPlatformJobExecutor.class
    try {
      managedQueueExecutorService.execute(new PlatformExecuteJobsRunnable(jobIds, commandExecutor));
    } catch (RejectedExecutionException e) {
      // if the queue is exhausted, execute the jobs in the caller thread
      new PlatformExecuteJobsRunnable(jobIds, commandExecutor).run();
    }
  }

  @Override
  public Object scheduleAcquisition(Runnable acquisitionRunnable) {
    ManagedQueueExecutorService managedQueueExecutorService = managedQueueInjector.getValue();
    try {
      managedQueueExecutorService.execute(acquisitionRunnable);
    } catch (Exception e) {
      throw new FoxPlatformException("Can not schedule acquisition.", e);
    }
    return null;
  }

  @Override
  public void unscheduleAcquisition(Object scheduledAcquisition) {
  }

  public InjectedValue<ManagedQueueExecutorService> getManagedQueueInjector() {
    return managedQueueInjector;
  }
  
  public InjectedValue<ProcessEngineControllerService> getProcessEngineControllerInjector() {
    return processEngineControllerInjector;
  }

}
