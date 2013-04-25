package org.camunda.bpm.container.impl.threading.ra.outbound;

import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;


/**
 * 
 * @author Daniel Meyer
 * 
 */
public class JcaExecutorServiceConnectionImpl implements JcaExecutorServiceConnection {

  protected JcaExecutorServiceManagedConnection mc;
  protected JcaExecutorServiceManagedConnectionFactory mcf;
  
  public JcaExecutorServiceConnectionImpl() {
  }
  
  public JcaExecutorServiceConnectionImpl(JcaExecutorServiceManagedConnection mc, JcaExecutorServiceManagedConnectionFactory mcf) {
    this.mc = mc;
    this.mcf = mcf;
  }

  public void closeConnection() {
    mc.closeHandle(this);
  }

  public boolean schedule(Runnable runnable, boolean isLongRunning) {
    return mc.schedule(runnable, isLongRunning);
  }

  public Runnable getExecuteJobsRunnable(List<String> jobIds, ProcessEngineImpl processEngine) {
    return mc.getExecuteJobsRunnable(jobIds, processEngine);
  }
 


}
