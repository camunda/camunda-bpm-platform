package org.camunda.bpm.engine.test.jobexecutor;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

public class FailingCmd implements Command<Void> {
    @Override
    public Void execute(CommandContext commandContext) {
      throw new ProcessEngineException("Expected Exception");
    }
  }