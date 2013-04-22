package org.camunda.bpm.container.impl.threading.ra.commonj;

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
  
  public void run() {
    delegate.run();    
  }

  public boolean isDaemon() {
    return false;
  }

  public void release() {
  }

}
