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

import org.camunda.bpm.engine.impl.MessageCorrelationBuilderImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.runtime.CorrelationHandler;
import org.camunda.bpm.engine.impl.runtime.CorrelationSet;
import org.camunda.bpm.engine.impl.runtime.MessageCorrelationResultImpl;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureAtLeastOneNotNull;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;

/**
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 * @author Michael Scholz
 */
public class CorrelateAllMessageCmd extends AbstractCorrelateMessageCmd implements Command<List<? extends MessageCorrelationResult>> {

  /**
   * Initialize the command with a builder
   *
   * @param messageCorrelationBuilderImpl
   */
  public CorrelateAllMessageCmd(MessageCorrelationBuilderImpl messageCorrelationBuilderImpl) {
    super(messageCorrelationBuilderImpl);
  }

  public List<? extends MessageCorrelationResult> execute(final CommandContext commandContext) {
    ensureAtLeastOneNotNull(
        "At least one of the following correlation criteria has to be present: " + "messageName, businessKey, correlationKeys, processInstanceId", messageName,
        builder.getBusinessKey(), builder.getCorrelationProcessInstanceVariables(), builder.getProcessInstanceId());

    final CorrelationHandler correlationHandler = Context.getProcessEngineConfiguration().getCorrelationHandler();
    final CorrelationSet correlationSet = new CorrelationSet(builder);
    List<MessageCorrelationResultImpl> correlationResults = commandContext.runWithoutAuthorization(new Callable<List<MessageCorrelationResultImpl>>() {
      public List<MessageCorrelationResultImpl> call() throws Exception {
        return correlationHandler.correlateMessages(commandContext, messageName, correlationSet);
      }
    });

    // check authorization
    for (MessageCorrelationResultImpl correlationResult : correlationResults) {
      checkAuthorization(correlationResult);
    }

    for (MessageCorrelationResultImpl correlationResult : correlationResults) {
      if (MessageCorrelationResultImpl.TYPE_EXECUTION.equals(correlationResult.getResultType())) {
        triggerExecution(commandContext, correlationResult);
      } else {
        instantiateProcess(commandContext, correlationResult);
      }
    }

    return correlationResults;
  }
}
