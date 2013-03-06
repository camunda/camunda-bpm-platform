package org.camunda.bpm.engine.impl.jobexecutor;

import org.camunda.bpm.engine.impl.cmd.FoxJobRetryCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;



public class FoxFailedJobCommandFactory implements FailedJobCommandFactory {

  public Command<Object> getCommand(String jobId, Throwable exception) {
    return new FoxJobRetryCmd(jobId, exception);
  }

}
