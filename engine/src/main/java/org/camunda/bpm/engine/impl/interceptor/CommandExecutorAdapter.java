package org.camunda.bpm.engine.impl.interceptor;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;

/**
 * Adapter that hides the execution of a camunda {@link Command}, works with {@link org.camunda.bpm.engine.impl.interceptor.CommandContext} when called inside
 * a camunda {@link org.camunda.bpm.engine.delegate.JavaDelegate} or Listener but uses {@link ProcessEngineConfigurationImpl#getCommandExecutorTxRequired()}
 * to get a new context when called elsewhere.
 */
@FunctionalInterface
public interface CommandExecutorAdapter {

    static CommandExecutorAdapter create(CommandContext commandContext) {
        return new CommandExecutorAdapter() {
            @Override
            public <T> T execute(Command<T> command) {
                return command.execute(commandContext);
            }
        };
    }

    static CommandExecutorAdapter create(final ProcessEngineConfigurationImpl configuration) {
        return new CommandExecutorAdapter() {
            @Override
            public <T> T execute(Command<T> command) {
                if (Context.getCommandContext() == null) {
                    return configuration.getCommandExecutorTxRequired().execute(command);
                } else {
                    return create(Context.getCommandContext()).execute(command);
                }
            }
        };
    }

    <T> T execute(Command<T> command);
}
