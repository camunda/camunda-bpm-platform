package com.camunda.fox.engine.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class Instances {
  
  public static class InstancesStarter {
    
    private static final AtomicInteger spawnedCounter = new AtomicInteger(0);
    
    private ExecutorService executor;
    private final List<SpawnProcessesRunnable> spawners = new ArrayList();
    
    private final ProcessEngine processEngine;
    
    private final String[] processDefinitionKeys;
    
    private final int count;
    
    private long startTime = -1;
    private long endTime = -1;
    
    private void started() {
      this.startTime = System.currentTimeMillis();
    }
    
    private void finished() {
      this.endTime = System.currentTimeMillis();
    }
    
    public boolean isFinished() {
      return endTime != -1;
    }
    
    public long runtimeMillis() {
      if (endTime == -1 || startTime == -1) {
        return -1;
      }
      
      return endTime - startTime;
    }
    
    private InstancesStarter(String[] processDefinitionKeys, int count, int fork, ProcessEngine processEngine) {
      this.processDefinitionKeys = processDefinitionKeys;
      this.count = count;
      
      this.processEngine = processEngine;
      
      started();
      
      if (fork > 0) {
        forkStart(fork);
      } else {
        start();
      }
    }

    private void forkStart(int threadCount) {
      executor = Executors.newFixedThreadPool(threadCount);
      
      RuntimeService runtimeService = processEngine.getRuntimeService();
      
      for (int i = 0; i < threadCount; i++) {
        SpawnProcessesRunnable runnable = new SpawnProcessesRunnable(count / threadCount, processDefinitionKeys, spawnedCounter, runtimeService);
        executor.submit(runnable);
        
        registerRunnable(runnable);
      }
    }
    
    private void start() {
      RuntimeService runtimeService = processEngine.getRuntimeService();
      
      SpawnProcessesRunnable runnable = new SpawnProcessesRunnable(count, processDefinitionKeys, spawnedCounter, runtimeService);
      registerRunnable(runnable);
      
      runnable.run();
      finished();
    }
    
    private void registerRunnable(SpawnProcessesRunnable runnable) {
      this.spawners.add(runnable);
    }

    public long startedInstances() {
      return this.spawnedCounter.get();
    }
    
    public void awaitAllStarted(int ms, int retries) throws InterruptedException {
      if (executor == null) {
        return;
      }
      
      int retriesLeft = retries + 1;
      
      System.out.print("Waiting for all instances to be started .");
      System.out.flush();
      while (retriesLeft > 0) {
        if (executor.awaitTermination(ms, TimeUnit.MILLISECONDS)) {
          break;
        }
        
        System.out.print(" . ");
        System.out.flush();
        retriesLeft--;
      }
      
      if (retriesLeft == 0) {
        executor.shutdownNow();
        System.out.print(" [Force end of startprocess]");
        System.out.flush();
      }
      
      finished();
      
      System.out.println();
    }
  }
  
  public static class Builder {
    
    private int fork = 0;
    private String[] processDefinitionKeys = null;
    private boolean processDefinitionKeysFromRepository = false;
    private int count;
    
    private Builder() { }
    
    public Builder fork(int fork) {
      if (fork < 0) {
        throw new IllegalArgumentException("Argument for fork must not be negative");
      }
      
      this.fork = fork;
      return this;
    }
    
    public Builder processDefinitionKeys(String ... processDefinitionKeys) {
      if (processDefinitionKeysFromRepository) {
        throw new IllegalArgumentException("Process definition keys from repository is set");
      }
      this.processDefinitionKeys = processDefinitionKeys;
      return this;
    }
    
    public Builder processDefinitionKeysFromRepository() {
      if (processDefinitionKeys != null) {
        throw new IllegalArgumentException("Process definition keys to deploy already set");
      }
      processDefinitionKeysFromRepository = true;
      return this;
    }
    
    public Builder count(int count) {
      this.count = count;
      return this;
    }
    
    public InstancesStarter startOn(ProcessEngine processEngine) {
      String[] keys = processDefinitionKeysFromRepository ? processDefinitionKeysFromEngine(processEngine) : processDefinitionKeys;
      if (keys == null || keys.length == 0) {
        throw new IllegalArgumentException("No process definitions to deploy (processDefinitionKeys is null or empty)");
      }
      
      return new InstancesStarter(processDefinitionKeys, count, fork, processEngine);
    }
    
    private String[] processDefinitionKeysFromEngine(ProcessEngine processEngine) {
      RepositoryService repositoryService = processEngine.getRepositoryService();
      List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
      
      String[] keys = new String[processDefinitions.size()];
      int index = 0;
      
      for (ProcessDefinition d: processDefinitions) {
        keys[index++] = d.getKey();
      }
      
      return keys;
   }
  }
  
  public static Builder processInstanceStarter() {
    return new Builder();
  }
}
