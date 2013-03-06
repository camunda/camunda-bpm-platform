package org.camunda.bpm.engine.impl.jobexecutor;

import org.camunda.bpm.engine.impl.cmd.DecrementJobRetriesCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;


public class DefaultFailedJobCommandFactory implements FailedJobCommandFactory {

  public Command<Object> getCommand(String jobId, Throwable exception) {
    return new DecrementJobRetriesCmd(jobId, exception);
  }

}
