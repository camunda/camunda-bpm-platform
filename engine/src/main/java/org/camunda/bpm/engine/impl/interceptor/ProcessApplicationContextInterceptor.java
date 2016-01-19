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

import java.util.concurrent.Callable;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.impl.ProcessApplicationContextImpl;
import org.camunda.bpm.application.impl.ProcessApplicationIdentifier;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.context.Context;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessApplicationContextInterceptor extends CommandInterceptor {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  public ProcessApplicationContextInterceptor(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
  }

  @Override
  public <T> T execute(final Command<T> command) {
    ProcessApplicationIdentifier processApplicationIdentifier = ProcessApplicationContextImpl.get();

    if (processApplicationIdentifier != null) {
      // clear the identifier so this interceptor does not apply to nested commands
      ProcessApplicationContextImpl.clear();

      try {
        ProcessApplicationReference reference = getPaReference(processApplicationIdentifier);
        return Context.executeWithinProcessApplication(new Callable<T>() {

          @Override
          public T call() throws Exception {
            return next.execute(command);
          }
        },
        reference);

      }
      finally {
        // restore the identifier for subsequent commands
        ProcessApplicationContextImpl.set(processApplicationIdentifier);
      }
    }
    else {
      return next.execute(command);
    }
  }

  protected ProcessApplicationReference getPaReference(ProcessApplicationIdentifier processApplicationIdentifier) {
    if (processApplicationIdentifier.getReference() != null) {
      return processApplicationIdentifier.getReference();
    }
    else if (processApplicationIdentifier.getProcessApplication() != null) {
      return processApplicationIdentifier.getProcessApplication().getReference();
    }
    else if (processApplicationIdentifier.getName() != null) {
       RuntimeContainerDelegate runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get();
       ProcessApplicationReference reference = runtimeContainerDelegate.getDeployedProcessApplication(processApplicationIdentifier.getName());

       if (reference == null) {
         throw LOG.paWithNameNotRegistered(processApplicationIdentifier.getName());
       }
       else {
         return reference;
       }
    }
    else {
      throw LOG.cannotReolvePa(processApplicationIdentifier);
    }
  }

}
