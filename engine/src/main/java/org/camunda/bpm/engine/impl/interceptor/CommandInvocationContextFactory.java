package org.camunda.bpm.engine.impl.interceptor;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;

public class CommandInvocationContextFactory {
    public CommandInvocationContextFactory() {
    }

    public CommandInvocationContext createCommandInvocationContext(Command< ? > command, ProcessEngineConfigurationImpl configuration) {
        return new CommandInvocationContext(command, configuration);
    }
}
