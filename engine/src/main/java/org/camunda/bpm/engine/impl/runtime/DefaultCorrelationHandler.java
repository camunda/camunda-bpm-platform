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

package org.camunda.bpm.engine.impl.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.impl.ExecutionQueryImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.runtime.Execution;

/**
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 * @author Michael Scholz
 */
public class DefaultCorrelationHandler implements CorrelationHandler {

  private final static Logger LOGGER = Logger.getLogger(DefaultCorrelationHandler.class.getName());

  public MessageCorrelationResult correlateMessage(CommandContext commandContext, String messageName, CorrelationSet correlationSet) {

    // first try to correlate to execution
    List<MessageCorrelationResult> correlations = correlateMessageToExecutions(commandContext, messageName, correlationSet);

    if(correlations.size() > 1) {
      throw new MismatchingMessageCorrelationException(messageName, correlationSet.getBusinessKey(), correlationSet.getCorrelationKeys(),
          String.valueOf(correlations.size()) + " executions match the correlation keys. Should be one or zero.");
    }
    else if(!correlations.isEmpty()) {
      return correlations.get(0);
    }

    // if unsuccessful, correlate to process definition
    return tryCorrelateMessageToProcessDefinition(commandContext, messageName, correlationSet);
  }

  public List<MessageCorrelationResult> correlateMessages(CommandContext commandContext, String messageName, CorrelationSet correlationSet) {

    List<MessageCorrelationResult> result = new ArrayList<MessageCorrelationResult>();

    // first collect correlations to executions
    result.addAll(correlateMessageToExecutions(commandContext, messageName, correlationSet));

    // now collect a potential correlation to process definition
    MessageCorrelationResult processDefinitionCorrelation = tryCorrelateMessageToProcessDefinition(commandContext, messageName, correlationSet);
    if(processDefinitionCorrelation != null) {
      result.add(processDefinitionCorrelation);
    }

    return result;
  }

  protected List<MessageCorrelationResult> correlateMessageToExecutions(CommandContext commandContext, String messageName,
      CorrelationSet correlationSet) {

    ExecutionQueryImpl query = new ExecutionQueryImpl();

    Map<String, Object> correlationKeys = correlationSet.getCorrelationKeys();
    if (correlationKeys != null) {
      for (Map.Entry<String, Object> correlationKey : correlationKeys.entrySet()) {
        query.processVariableValueEquals(correlationKey.getKey(), correlationKey.getValue());
      }
    }

    String businessKey = correlationSet.getBusinessKey();
    if(businessKey != null) {
      query.processInstanceBusinessKey(businessKey);
    }

    String processInstanceId = correlationSet.getProcessInstanceId();
    if(processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }

    query.messageEventSubscriptionName(messageName);
    List<Execution> matchingExecutions = query.evaluateExpressionsAndExecuteList(commandContext, null);

    List<MessageCorrelationResult> result = new ArrayList<MessageCorrelationResult>(matchingExecutions.size());

    for(Execution matchingExecution: matchingExecutions) {
      result.add(MessageCorrelationResult.matchedExecution((ExecutionEntity) matchingExecution));
    }

    return result;
  }

  protected MessageCorrelationResult tryCorrelateMessageToProcessDefinition(CommandContext commandContext, String messageName, CorrelationSet correlationSet) {

    MessageEventSubscriptionEntity messageEventSubscription = commandContext.getEventSubscriptionManager()
      .findMessageStartEventSubscriptionByName(messageName);
    if(messageEventSubscription == null || messageEventSubscription.getConfiguration() == null) {
      return null;

    } else {
      DeploymentCache deploymentCache = Context
        .getProcessEngineConfiguration()
        .getDeploymentCache();

      String processDefinitionId = messageEventSubscription.getConfiguration();
      ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
      if (processDefinition == null) {
        LOGGER.log(Level.FINE, "Found event subscription with {0} but process definition {1} could not be found.",
            new Object[]{messageEventSubscription, processDefinitionId});
        return null;

      } else {
        return MessageCorrelationResult.matchedProcessDefinition(processDefinition, messageEventSubscription.getActivityId());
      }
    }
  }

}
