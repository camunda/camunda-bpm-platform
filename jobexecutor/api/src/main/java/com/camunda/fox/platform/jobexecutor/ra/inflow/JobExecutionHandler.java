package com.camunda.fox.platform.jobexecutor.ra.inflow;

import org.activiti.engine.impl.interceptor.CommandExecutor;

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
