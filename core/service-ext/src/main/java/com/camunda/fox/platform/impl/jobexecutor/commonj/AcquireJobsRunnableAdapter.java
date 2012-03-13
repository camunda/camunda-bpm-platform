package com.camunda.fox.platform.impl.jobexecutor.commonj;

import org.activiti.engine.impl.jobexecutor.AcquireJobsRunnable;

import commonj.work.Work;

/**
 * 
 * @author Daniel Meyer
 */
public class AcquireJobsRunnableAdapter implements Work {
  
  protected final AcquireJobsRunnable acquireJobsRunnable;

  public AcquireJobsRunnableAdapter(AcquireJobsRunnable acquireJobsRunnable) {
    this.acquireJobsRunnable = acquireJobsRunnable;
  }

  @Override
  public void run() {
    acquireJobsRunnable.run();
  }

  @Override
  public void release() {
    acquireJobsRunnable.stop();
  }

  @Override
  public boolean isDaemon() {
    return true; // this is long running
  }

}
