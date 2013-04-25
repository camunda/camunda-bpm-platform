package org.camunda.bpm.container.impl.ejb;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.resource.ResourceException;

import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.container.impl.threading.ra.outbound.JcaExecutorServiceConnection;
import org.camunda.bpm.container.impl.threading.ra.outbound.JcaExecutorServiceConnectionFactory;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;

/**
 * Bean exposing the JCA implementation of the {@link ExecutorService} as Stateless Bean.
 * 
 * @author Daniel Meyer
 *
 */
@Stateless
@Local(ExecutorService.class)
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class ExecutorServiceBean implements ExecutorService {
  
  @Resource(mappedName="eis/JcaExecutorServiceConnectionFactory")
  protected JcaExecutorServiceConnectionFactory executorConnectionFactory;
  
  protected JcaExecutorServiceConnection executorConnection;

  @PostConstruct
  protected void openConnection() {
    try {
      executorConnection = executorConnectionFactory.getConnection();
    } catch (ResourceException e) {
      throw new ProcessEngineException("Could not open connection to executor service connection factory ", e);
    } 
  }
  
  @PreDestroy
  protected void closeConnection() {
    if(executorConnection != null) {
      executorConnection.closeConnection();
    }
  }

  public boolean schedule(Runnable runnable, boolean isLongRunning) {
    return executorConnection.schedule(runnable, isLongRunning);
  }
  
  public Runnable getExecuteJobsRunnable(List<String> jobIds, ProcessEngineImpl processEngine) {
    return executorConnection.getExecuteJobsRunnable(jobIds, processEngine);
  }
  
}
