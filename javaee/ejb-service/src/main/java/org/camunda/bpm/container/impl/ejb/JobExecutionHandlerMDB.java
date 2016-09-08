package org.camunda.bpm.container.impl.ejb;

import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.camunda.bpm.container.impl.threading.ra.inflow.JobExecutionHandler;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobHelper;


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
    ExecuteJobHelper.executeJob(job, commandExecutor);
  }

}
