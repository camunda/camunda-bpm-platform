package org.camunda.bpm.container.impl.jobexecutor.ra.execution.commonj;

import commonj.work.Work;


public class CommonjAcquireJobsRunnableAdapter implements Work {

  protected final Runnable runnable; 
  
  public CommonjAcquireJobsRunnableAdapter(Runnable runnable) {
    this.runnable = runnable;
  }
  
  @Override
  public void run() {
    runnable.run();
  }

  @Override
  public boolean isDaemon() {
    return true;
  }

  @Override
  public void release() {
    // unsupported
  }

}
