package com.camunda.fox.platform.impl.service;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * <p>Simple implementation of the fox platform services using {@link FutureTask}.</p>
 * 
 * <p><strong>NOTE</strong>: Do not use this implementation in an environment where 
 * self-management of threads is undesirable. This class is present primarily for 
 * illustrative and testing purposes. In most container environment we want to delegate 
 * the asynchronous processing to container infrastructure.</p> 
 *  
 * @author Daniel Meyer
 */
public class SimplePlatformService extends AbstractPlatformService {

  public Future<ProcessEngineStartOperation> startProcessEngine(final ProcessEngineConfiguration processEngineConfiguration) {
    FutureTask<ProcessEngineStartOperation> task = new FutureTask<ProcessEngineStartOperation>(new Callable<ProcessEngineStartOperation>() {
      public ProcessEngineStartOperation call() throws Exception {        
        return doStartProcessEngine(processEngineConfiguration);
      }
    });
    task.run();
    return task;
  }

  public Future<ProcessEngineStopOperation> stopProcessEngine(final String name) {
    FutureTask<ProcessEngineStopOperation> task = new FutureTask<ProcessEngineService.ProcessEngineStopOperation>(new Callable<ProcessEngineStopOperation>() {
      public ProcessEngineStopOperation call() throws Exception {        
        return doStopProcessEngine(name);
      }
    });
    task.run();
    return task;
  }

  public Future<ProcessArchiveInstallOperation> installProcessArchive(final ProcessArchive processArchive) {
    FutureTask<ProcessArchiveInstallOperation> task = new FutureTask<ProcessArchiveInstallOperation>(new Callable<ProcessArchiveInstallOperation>() {
      public ProcessArchiveInstallOperation call() throws Exception {        
        return doInstallProcessArchive(processArchive);
      }
    });
    task.run();
    return task;
  }

  public Future<ProcessArchiveUninstallOperation> unInstallProcessArchive(final String processArchiveName) {
    FutureTask<ProcessArchiveUninstallOperation> task = new FutureTask<ProcessArchiveUninstallOperation>(new Callable<ProcessArchiveUninstallOperation>() {
      public ProcessArchiveUninstallOperation call() throws Exception {        
        return doUnInstallProcessArchive(processArchiveName);
      }
    });
    task.run();
    return task;
  }

}
