package com.camunda.fox.platform.jobexecutor.impl.service;

import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.activiti.engine.impl.cmd.ExecuteJobsCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;

import com.camunda.fox.platform.jobexecutor.ra.inflow.JobExecutionHandler;

/**
 * <p>MessageDrivenBean implementation of the {@link JobExecutionHandler} interface</p>
 * 
 * @author Daniel Meyer
 */
@MessageDriven(
  name="JobExecutionHandlerMDB",
  messageListenerInterface=JobExecutionHandler.class
)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class JobExecutionHandlerMDB implements JobExecutionHandler {

  public void executeJob(String job, CommandExecutor commandExecutor) {
    commandExecutor.execute(new ExecuteJobsCmd(job));
  }

}
