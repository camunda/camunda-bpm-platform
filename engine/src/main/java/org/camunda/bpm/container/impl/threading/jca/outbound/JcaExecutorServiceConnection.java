package org.camunda.bpm.container.impl.threading.jca.outbound;

import org.camunda.bpm.container.ExecutorService;

/**
 * 
 * @author Daniel Meyer
 *
 */
public interface JcaExecutorServiceConnection extends ExecutorService {
  
  public void closeConnection();
  
}
