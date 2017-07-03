/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.interceptor;


import org.camunda.bpm.engine.delegate.ProcessEngineServicesAware;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.context.Context;

/**
 * <p>Interceptor used for opening the {@link CommandContext} and {@link CommandInvocationContext}.</p>
 *
 * <p>Since 7.1, this interceptor will not always open a new command context but instead reuse an existing
 * command context if possible. This is required for supporting process engine public API access from
 * delegation code (see {@link ProcessEngineServicesAware}.). However, for every command, a new
 * command invocation context is created. While a command context holds resources that are
 * shared between multiple commands, such as database sessions, a command invocation context holds
 * resources specific for a single command.</p>
 *
 * <p>The interceptor will check whether an open command context exists. If true, it will reuse the
 * command context. If false, it will open a new one. We will always push the context to the
 * {@link Context} stack. So ins some situations, you will see the same context being pushed to the sack
 * multiple times. The rationale is that the size of  the stack should allow you to determine whether
 * you are currently running an 'inner' command or an 'outer' command as well as your current stack size.
 * Existing code may rely on this behavior.</p>
 *
 * <p>The interceptor can be configured using the property {@link #alwaysOpenNew}.
 * If this property is set to true, we will always open a new context regardless whether there already
 * exists an active context or not. This is required for properly supporting REQUIRES_NEW semantics for
 * commands run through the {@link ProcessEngineConfigurationImpl#getCommandInterceptorsTxRequiresNew()}
 * chain. In that context the 'inner' command must be able to succeed / fail independently from the
 * 'outer' command.</p>
 *
 *
 * @author Tom Baeyens
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 */
public class CommandContextInterceptor extends CommandInterceptor {

  private final static CommandLogger LOG = CommandLogger.CMD_LOGGER;

  protected CommandContextFactory commandContextFactory;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  /** if true, we will always open a new command context */
  protected boolean alwaysOpenNew;

  public CommandContextInterceptor() {
  }

  public CommandContextInterceptor(CommandContextFactory commandContextFactory, ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.commandContextFactory = commandContextFactory;
    this.processEngineConfiguration = processEngineConfiguration;
  }

  public CommandContextInterceptor(CommandContextFactory commandContextFactory, ProcessEngineConfigurationImpl processEngineConfiguration, boolean alwaysOpenNew) {
    this(commandContextFactory, processEngineConfiguration);
    this.alwaysOpenNew = alwaysOpenNew;
  }

  public <T> T execute(Command<T> command) {
    CommandContext context = null;

    if(!alwaysOpenNew) {
      // check whether we can reuse the command context
      CommandContext existingCommandContext = Context.getCommandContext();
      if(existingCommandContext != null && isFromSameEngine(existingCommandContext)) {
        context = existingCommandContext;
      }
    }

    boolean openNew = (context == null);

    CommandInvocationContext commandInvocationContext = new CommandInvocationContext(command);
    Context.setCommandInvocationContext(commandInvocationContext);

    try {
      if(openNew) {
        LOG.debugOpeningNewCommandContext();
        context = commandContextFactory.createCommandContext();

      } else {
        LOG.debugReusingExistingCommandContext();

      }

      Context.setCommandContext(context);
      Context.setProcessEngineConfiguration(processEngineConfiguration);

      // delegate to next interceptor in chain
      return next.execute(command);

    } catch (Throwable t) {
      commandInvocationContext.trySetThrowable(t);

    } finally {
      try {
        if (openNew) {
          LOG.closingCommandContext();
          context.close(commandInvocationContext);
        } else {
          commandInvocationContext.rethrow();
        }
      } finally {
        Context.removeCommandInvocationContext();
        Context.removeCommandContext();
        Context.removeProcessEngineConfiguration();
      }
    }

    return null;
  }

  protected boolean isFromSameEngine(CommandContext existingCommandContext) {
    return processEngineConfiguration == existingCommandContext.getProcessEngineConfiguration();
  }

  public CommandContextFactory getCommandContextFactory() {
    return commandContextFactory;
  }

  public void setCommandContextFactory(CommandContextFactory commandContextFactory) {
    this.commandContextFactory = commandContextFactory;
  }

  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  public void setProcessEngineContext(ProcessEngineConfigurationImpl processEngineContext) {
    this.processEngineConfiguration = processEngineContext;
  }
}
