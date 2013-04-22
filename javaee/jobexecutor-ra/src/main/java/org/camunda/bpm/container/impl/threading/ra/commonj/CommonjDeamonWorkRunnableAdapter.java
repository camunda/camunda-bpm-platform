package org.camunda.bpm.container.impl.threading.ra.commonj;

import commonj.work.Work;

/**
 * <p>Long running {@link Runnable} adapter</p>
 * 
 * @author Daniel Meyer
 *
 */
public class CommonjDeamonWorkRunnableAdapter implements Work {

  protected final Runnable runnable; 
  
  public CommonjDeamonWorkRunnableAdapter(Runnable runnable) {
    this.runnable = runnable;
  }
  
  public void run() {
    runnable.run();
  }

  public boolean isDaemon() {
    return true;
  }

  public void release() {
    // unsupported
  }

}
