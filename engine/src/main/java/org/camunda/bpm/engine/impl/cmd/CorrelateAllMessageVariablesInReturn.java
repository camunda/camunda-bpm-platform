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


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.impl.MessageCorrelationBuilderImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.runtime.CorrelationHandler;
import org.camunda.bpm.engine.impl.runtime.CorrelationSet;
import org.camunda.bpm.engine.impl.runtime.CorrelationHandlerResult;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultWithVariables;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureAtLeastOneNotNull;

public class CorrelateAllMessageVariablesInReturn extends AbstractCorrelateMessageCmd implements Command<List<MessageCorrelationResultWithVariables>> {

  /**
   * Initialize the command with a builder
   *
   * @param messageCorrelationBuilderImpl
   */
  public CorrelateAllMessageVariablesInReturn(MessageCorrelationBuilderImpl messageCorrelationBuilderImpl) {
    super(messageCorrelationBuilderImpl);
  }

  public List<MessageCorrelationResultWithVariables> execute(final CommandContext commandContext) {
    ensureAtLeastOneNotNull(
        "At least one of the following correlation criteria has to be present: " + "messageName, businessKey, correlationKeys, processInstanceId", messageName,
        builder.getBusinessKey(), builder.getCorrelationProcessInstanceVariables(), builder.getProcessInstanceId());

    final CorrelationHandler correlationHandler = Context.getProcessEngineConfiguration().getCorrelationHandler();
    final CorrelationSet correlationSet = new CorrelationSet(builder);
    List<CorrelationHandlerResult> correlationResults = commandContext.runWithoutAuthorization(new Callable<List<CorrelationHandlerResult>>() {
      public List<CorrelationHandlerResult> call() throws Exception {
        return correlationHandler.correlateMessages(commandContext, messageName, correlationSet);
      }
    });

    // check authorization
    for (CorrelationHandlerResult correlationResult : correlationResults) {
      checkAuthorization(correlationResult);
    }

    List<MessageCorrelationResultWithVariables> results = new ArrayList<MessageCorrelationResultWithVariables>();
    for (CorrelationHandlerResult correlationResult : correlationResults) {
      results.add(createMessageCorrelationResultWithVariables(commandContext, correlationResult));
    }

    return results;
  }
}
