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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.impl.ExecutionQueryImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.event.MessageEventHandler;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionManager;
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

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  public MessageCorrelationResult correlateMessage(CommandContext commandContext, String messageName, CorrelationSet correlationSet) {

    // first try to correlate to execution
    List<MessageCorrelationResult> correlations = correlateMessageToExecutions(commandContext, messageName, correlationSet);

    if (correlations.size() > 1) {
      throw new MismatchingMessageCorrelationException(messageName, correlationSet.getBusinessKey(), correlationSet.getCorrelationKeys(),
          String.valueOf(correlations.size()) + " executions match the correlation keys. Should be one or zero.");
    } else if (!correlations.isEmpty()) {
      return correlations.get(0);
    }

    // if unsuccessful, correlate to process definition
    return correlateStartMessage(commandContext, messageName, correlationSet);
  }

  public List<MessageCorrelationResult> correlateMessages(CommandContext commandContext, String messageName, CorrelationSet correlationSet) {

    List<MessageCorrelationResult> result = new ArrayList<MessageCorrelationResult>();

    // first collect correlations to executions
    result.addAll(correlateMessageToExecutions(commandContext, messageName, correlationSet));

    // now collect a potential correlation to process definition
    MessageCorrelationResult processDefinitionCorrelation = correlateStartMessage(commandContext, messageName, correlationSet);
    if (processDefinitionCorrelation != null) {
      result.add(processDefinitionCorrelation);
    }

    ensureCorrelateMessageOnlyForOneTenant(result, messageName);

    return result;
  }

  protected List<MessageCorrelationResult> correlateMessageToExecutions(CommandContext commandContext, String messageName, CorrelationSet correlationSet) {

    ExecutionQueryImpl query = new ExecutionQueryImpl();

    Map<String, Object> correlationKeys = correlationSet.getCorrelationKeys();
    if (correlationKeys != null) {
      for (Map.Entry<String, Object> correlationKey : correlationKeys.entrySet()) {
        query.processVariableValueEquals(correlationKey.getKey(), correlationKey.getValue());
      }
    }

    String businessKey = correlationSet.getBusinessKey();
    if (businessKey != null) {
      query.processInstanceBusinessKey(businessKey);
    }

    String processInstanceId = correlationSet.getProcessInstanceId();
    if (processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }

    if (messageName != null) {
      query.messageEventSubscriptionName(messageName);
    } else {
      query.messageEventSubscription();
    }

    if (correlationSet.isTenantIdSet) {
      String tenantId = correlationSet.getTenantId();
      if (tenantId != null) {
        query.tenantIdIn(tenantId);
      } else {
        query.withoutTenantId();
      }
    }

    // restrict to active executions
    query.active();

    List<Execution> matchingExecutions = query.evaluateExpressionsAndExecuteList(commandContext, null);

    List<MessageCorrelationResult> result = new ArrayList<MessageCorrelationResult>(matchingExecutions.size());

    for (Execution matchingExecution : matchingExecutions) {
      MessageCorrelationResult correlationResult = MessageCorrelationResult.matchedExecution((ExecutionEntity) matchingExecution);
      result.add(correlationResult);
    }

    return result;
  }

  @Override
  public MessageCorrelationResult correlateStartMessage(CommandContext commandContext, String messageName, CorrelationSet correlationSet) {
    if (messageName == null) {
      return null;
    }

    if (correlationSet.getProcessDefinitionId() == null) {
      return correlateStartMessageByEventSubscription(commandContext, messageName, correlationSet);

    } else {
      return correlateStartMessageByProcessDefinitionId(commandContext, messageName, correlationSet.getProcessDefinitionId());
    }
  }

  protected MessageCorrelationResult correlateStartMessageByEventSubscription(CommandContext commandContext, String messageName, CorrelationSet correlationSet) {
    MessageEventSubscriptionEntity messageEventSubscription = findMessageStartEventSubscription(commandContext, messageName, correlationSet);

    if (messageEventSubscription != null && messageEventSubscription.getConfiguration() != null) {
      DeploymentCache deploymentCache = commandContext.getProcessEngineConfiguration().getDeploymentCache();

      String processDefinitionId = messageEventSubscription.getConfiguration();
      ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
      // only an active process definition will be returned
      if (processDefinition != null && !processDefinition.isSuspended()) {
        return MessageCorrelationResult.matchedProcessDefinition(processDefinition, messageEventSubscription.getActivityId());

      } else {
        LOG.couldNotFindProcessDefinitionForEventSubscription(messageEventSubscription, processDefinitionId);
      }
    }
    return null;
  }

  protected MessageEventSubscriptionEntity findMessageStartEventSubscription(CommandContext commandContext, String messageName, CorrelationSet correlationSet) {
    EventSubscriptionManager eventSubscriptionManager = commandContext.getEventSubscriptionManager();

    if (correlationSet.isTenantIdSet) {
      return eventSubscriptionManager.findMessageStartEventSubscriptionByNameAndTenantId(messageName, correlationSet.getTenantId());

    } else {
      List<MessageEventSubscriptionEntity> eventSubscriptions = eventSubscriptionManager.findMessageStartEventSubscriptionByName(messageName);
      if (eventSubscriptions.isEmpty()) {
        return null;

      } else if (eventSubscriptions.size() == 1) {
        return eventSubscriptions.iterator().next();

      } else {
        throw LOG.multipleTenantsForMessageNameException(messageName);
      }
    }
  }

  protected MessageCorrelationResult correlateStartMessageByProcessDefinitionId(CommandContext commandContext, String messageName, String processDefinitionId) {
    DeploymentCache deploymentCache = commandContext.getProcessEngineConfiguration().getDeploymentCache();
    ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
    // only an active process definition will be returned
    if (processDefinition != null && !processDefinition.isSuspended()) {

      String startActivityId = findStartActivityIdByMessage(processDefinition, messageName);
      if (startActivityId != null) {
        return MessageCorrelationResult.matchedProcessDefinition(processDefinition, startActivityId);
      }
    }
    return null;
  }

  protected String findStartActivityIdByMessage(ProcessDefinitionEntity processDefinition, String messageName) {
    for (EventSubscriptionDeclaration declaration : EventSubscriptionDeclaration.getDeclarationsForScope(processDefinition)) {
      if (isMessageStartEventWithName(declaration, messageName)) {
        return declaration.getActivityId();
      }
    }
    return null;
  }

  protected boolean isMessageStartEventWithName(EventSubscriptionDeclaration declaration, String messageName) {
    return MessageEventHandler.EVENT_HANDLER_TYPE.equals(declaration.getEventType()) && declaration.isStartEvent()
        && messageName.equals(declaration.getEventName());
  }

  protected void ensureCorrelateMessageOnlyForOneTenant(List<MessageCorrelationResult> result, String messageName) {
    Set<String> tenantIds = new HashSet<String>();

    for (MessageCorrelationResult correlationResult : result) {
      if (MessageCorrelationResult.TYPE_EXECUTION.equals(correlationResult.getResultType())) {
        tenantIds.add(correlationResult.getExecutionEntity().getTenantId());
      } else {
        tenantIds.add(correlationResult.getProcessDefinitionEntity().getTenantId());
      }
    }
    if (tenantIds.size() > 1) {
      throw LOG.exceptionCorrelateMessageToMultipleTenants(messageName);
    }
  }

}
