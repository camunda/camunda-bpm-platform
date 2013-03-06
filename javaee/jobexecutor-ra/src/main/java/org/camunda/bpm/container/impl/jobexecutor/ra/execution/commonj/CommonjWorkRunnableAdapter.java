package org.camunda.bpm.container.impl.jobexecutor.ra.execution.commonj;

import commonj.work.Work;

/**
 * An adapter for wrapping a Runnable as a CommonJ {@link Work} instance.
 * 
 * @author Daniel Meyer
 * 
 */
public class CommonjWorkRunnableAdapter implements Work {

  private final Runnable delegate;
  
  public CommonjWorkRunnableAdapter(Runnable delegate) {
    this.delegate = delegate;
  }
  
  @Override
  public void run() {
    delegate.run();    
  }

  @Override
  public boolean isDaemon() {
    return false;
  }

  @Override
  public void release() {
  }

}
