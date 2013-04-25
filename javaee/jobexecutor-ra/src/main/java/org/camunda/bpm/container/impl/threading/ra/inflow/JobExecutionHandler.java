package org.camunda.bpm.container.impl.threading.ra.inflow;

import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 * Interface to be implemented by a MessageDriven bean handling the execution of
 * a job.
 * 
 * @author Daniel Meyer
 * 
 */
public interface JobExecutionHandler {

  public void executeJob(String job, CommandExecutor commandExecutor);

}
