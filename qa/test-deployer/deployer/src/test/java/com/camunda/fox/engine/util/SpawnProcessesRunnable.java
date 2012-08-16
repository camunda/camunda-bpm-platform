package com.camunda.fox.engine.util;

import java.util.concurrent.atomic.AtomicInteger;
import org.activiti.engine.RuntimeService;

/**
 *
 * @author nico.rehwaldt
 */
class SpawnProcessesRunnable implements Runnable {
  private final int count;
  private final String[] processKeys;
  private final RuntimeService runtimeService;
  private final long[] usedTime;
  private final AtomicInteger spawnedCounter;

  public SpawnProcessesRunnable(int count, String[] processKeys, AtomicInteger spawnedCounter, RuntimeService runtimeService) {
    this.count = count;
    this.processKeys = processKeys;
    this.runtimeService = runtimeService;
    
    this.spawnedCounter = spawnedCounter;
    
    this.usedTime = new long[processKeys.length];
    for (int i = 0; i < usedTime.length; i++) {
      usedTime[i] = 0;
    }
  }

  public void run() {
    int i = 0;
    while (i < count) {
      int processIndex = i % processKeys.length;
      String key = processKeys[processIndex];
      long start = System.currentTimeMillis();
      runtimeService.startProcessInstanceByKey(key);
      spawnedCounter.incrementAndGet();
      usedTime[processIndex] = usedTime[processIndex] + (System.currentTimeMillis() - start);
      i++;
    }
  }

  void printStatistics() {
    for (int j = 0; j < usedTime.length; j++) {
      System.out.println("Took " + usedTime[j] + "ms to spawn instances of " + processKeys[j]);
    }
    System.out.println();
  }
}
