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


import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.ProcessEngineServicesAware;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;

/**
 * <p>Interceptor used for opening the command context.</p>
 *
 * <p>Since 7.1, this interceptor will not always open a new command context but instead reuse an existing
 * command context if possible. This is required for supporting process engine public API access from
 * delegation code (see {@link ProcessEngineServicesAware}.)</p>
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
 */
public class CommandContextInterceptor extends CommandInterceptor {

  private final static Logger LOGGER = Logger.getLogger(CommandContextInterceptor.class.getName());

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
    CommandContext context  = Context.getCommandContext();
    boolean openNew = (alwaysOpenNew || context == null);

    try {
      if(openNew) {
        LOGGER.log(Level.FINE, "Opening new command context.");
        context = commandContextFactory.createCommandContext(command);

      } else {
        LOGGER.log(Level.FINE, "Reusing existing command context.");

      }

      Context.setCommandContext(context);
      Context.setProcessEngineConfiguration(processEngineConfiguration);

      // delegate to next interceptor in chain
      return next.execute(command);

    } catch (Exception e) {
      context.exception(e);

    } finally {
      try {
        if (openNew) {
          LOGGER.log(Level.FINE, "Closing command context.");
          context.close();
        }
      } finally {
        Context.removeCommandContext();
        Context.removeProcessEngineConfiguration();
      }
    }

    return null;
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
