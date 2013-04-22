package org.camunda.bpm.container.impl.threading.ra;

import javax.resource.spi.work.Work;

/**
 * An adapter for wrapping a Runnable as a JCA {@link Work} instance.
 * 
 * @author Daniel Meyer
 * 
 */
public class JcaWorkRunnableAdapter implements Work {

  private final Runnable runnable;

  public JcaWorkRunnableAdapter(Runnable runnable) {
    this.runnable = runnable;
  }

  public void run() {
    runnable.run();
  }

  public void release() {
    // nothing to do here
  }

}
