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

package org.camunda.bpm.engine.impl.cmd;


import java.util.List;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.impl.MessageCorrelationBuilderImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.runtime.CorrelationHandler;
import org.camunda.bpm.engine.impl.runtime.CorrelationSet;
import org.camunda.bpm.engine.impl.runtime.CorrelationHandlerResult;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

public class CorrelateStartMessageCmd extends AbstractCorrelateMessageCmd implements Command<ProcessInstance> {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  public CorrelateStartMessageCmd(MessageCorrelationBuilderImpl messageCorrelationBuilderImpl) {
    super(messageCorrelationBuilderImpl);
  }

  public ProcessInstance execute(final CommandContext commandContext) {
    ensureNotNull("messageName", messageName);

    final CorrelationHandler correlationHandler = Context.getProcessEngineConfiguration().getCorrelationHandler();
    final CorrelationSet correlationSet = new CorrelationSet(builder);

    List<CorrelationHandlerResult> correlationResults = commandContext.runWithoutAuthorization(new Callable<List<CorrelationHandlerResult>>() {
      public List<CorrelationHandlerResult> call() throws Exception {
        return correlationHandler.correlateStartMessages(commandContext, messageName, correlationSet);
      }
    });

    if (correlationResults.isEmpty()) {
      throw new MismatchingMessageCorrelationException(messageName, "No process definition matches the parameters");

    } else if (correlationResults.size() > 1) {
      throw LOG.exceptionCorrelateMessageToSingleProcessDefinition(messageName, correlationResults.size(), correlationSet);

    } else {
      CorrelationHandlerResult correlationResult = correlationResults.get(0);

      checkAuthorization(correlationResult);

      ProcessInstance processInstance = instantiateProcess(commandContext, correlationResult);
      return processInstance;
    }
  }
}
