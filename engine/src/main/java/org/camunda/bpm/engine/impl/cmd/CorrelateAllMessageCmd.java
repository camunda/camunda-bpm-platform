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
import java.util.Map;

import org.camunda.bpm.engine.impl.MessageCorrelationBuilderImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.runtime.CorrelationHandler;
import org.camunda.bpm.engine.impl.runtime.CorrelationSet;
import org.camunda.bpm.engine.impl.runtime.MessageCorrelationResult;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureAtLeastOneNotNull;

/**
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 * @author Michael Scholz
 */
public class CorrelateAllMessageCmd extends AbstractCorrelateMessageCmd {

  public CorrelateAllMessageCmd(String messageName, String businessKey,
      Map<String, Object> correlationKeys, Map<String, Object> processVariables) {
    super(messageName, businessKey, correlationKeys, processVariables);
  }

  /**
   * Initialize the command with a builder
   *
   * @param messageCorrelationBuilderImpl
   */
  public CorrelateAllMessageCmd(MessageCorrelationBuilderImpl messageCorrelationBuilderImpl) {
    super(messageCorrelationBuilderImpl);
  }

  public Void execute(CommandContext commandContext) {
    ensureAtLeastOneNotNull("At least one of the following correlation criteria has to be present: "
        + "messageName, businessKey, correlationKeys, processInstanceId", messageName, businessKey, correlationKeys, processInstanceId);

    CorrelationHandler correlationHandler = Context
      .getProcessEngineConfiguration()
      .getCorrelationHandler();

    CorrelationSet correlationSet = new CorrelationSet(businessKey, processInstanceId, correlationKeys);
    List<MessageCorrelationResult> correlationResults = correlationHandler
      .correlateMessages(commandContext, messageName, correlationSet);

    for (MessageCorrelationResult correlationResult : correlationResults) {
      if (MessageCorrelationResult.TYPE_EXECUTION.equals(correlationResult.getResultType())) {
        triggerExecution(commandContext, correlationResult);
      } else {
        instantiateProcess(commandContext, correlationResult);
      }
    }

    return null;
  }
}
