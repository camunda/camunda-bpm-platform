/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.cmd;


import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureAtLeastOneNotNull;

import java.util.List;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.impl.MessageCorrelationBuilderImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.runtime.CorrelationHandler;
import org.camunda.bpm.engine.impl.runtime.CorrelationHandlerResult;
import org.camunda.bpm.engine.impl.runtime.CorrelationSet;
import org.camunda.bpm.engine.impl.runtime.MessageCorrelationResultImpl;

/**
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 * @author Michael Scholz
 * @author Christopher Zell
 */
public class CorrelateMessageCmd extends AbstractCorrelateMessageCmd implements Command<MessageCorrelationResultImpl> {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected boolean startMessageOnly;

  /**
   * Initialize the command with a builder
   *
   * @param messageCorrelationBuilderImpl
   */
  public CorrelateMessageCmd(MessageCorrelationBuilderImpl messageCorrelationBuilderImpl, boolean collectVariables, boolean deserializeVariableValues, boolean startMessageOnly) {
    super(messageCorrelationBuilderImpl, collectVariables, deserializeVariableValues);
    this.startMessageOnly = startMessageOnly;
  }

  public MessageCorrelationResultImpl execute(final CommandContext commandContext) {
    ensureAtLeastOneNotNull(
        "At least one of the following correlation criteria has to be present: " + "messageName, businessKey, correlationKeys, processInstanceId", messageName,
        builder.getBusinessKey(), builder.getCorrelationProcessInstanceVariables(), builder.getProcessInstanceId());

    final CorrelationHandler correlationHandler = Context.getProcessEngineConfiguration().getCorrelationHandler();
    final CorrelationSet correlationSet = new CorrelationSet(builder);

    CorrelationHandlerResult correlationResult = null;
    if (startMessageOnly) {
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
        correlationResult = correlationResults.get(0);
      }
    } else {
      correlationResult = commandContext.runWithoutAuthorization(new Callable<CorrelationHandlerResult>() {
        public CorrelationHandlerResult call() throws Exception {
          return correlationHandler.correlateMessage(commandContext, messageName, correlationSet);
        }
      });

      if (correlationResult == null) {
        throw new MismatchingMessageCorrelationException(messageName, "No process definition or execution matches the parameters");
      }
    }

    // check authorization
    checkAuthorization(correlationResult);

    return createMessageCorrelationResult(commandContext, correlationResult);
  }
}
