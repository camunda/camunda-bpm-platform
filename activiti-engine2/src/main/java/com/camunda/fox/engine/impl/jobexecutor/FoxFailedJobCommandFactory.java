package com.camunda.fox.engine.impl.jobexecutor;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.jobexecutor.FailedJobCommandFactory;

import com.camunda.fox.engine.impl.cmd.FoxJobRetryCmd;


public class FoxFailedJobCommandFactory implements FailedJobCommandFactory {

  public Command<Object> getCommand(String jobId, Throwable exception) {
    return new FoxJobRetryCmd(jobId, exception);
  }

}
