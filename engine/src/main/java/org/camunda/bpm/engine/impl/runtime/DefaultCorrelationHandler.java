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
package org.camunda.bpm.engine.impl.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.ExecutionQueryImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.runtime.Execution;

/**
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 * @author Michael Scholz
 */
public class DefaultCorrelationHandler implements CorrelationHandler {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  public CorrelationHandlerResult correlateMessage(CommandContext commandContext, String messageName, CorrelationSet correlationSet) {

    // first try to correlate to execution
    List<CorrelationHandlerResult> correlations = correlateMessageToExecutions(commandContext, messageName, correlationSet);

    if (correlations.size() > 1) {
      throw LOG.exceptionCorrelateMessageToSingleExecution(messageName, correlations.size(), correlationSet);
    } else if (correlations.size() == 1) {
      return correlations.get(0);
    } else if (correlationSet.isExecutionsOnly()) {
      // no correlation to an execution found
      return null;
    }

    // then try to correlate to process definition
    correlations = correlateStartMessages(commandContext, messageName, correlationSet);

    if (correlations.size() > 1) {
      throw LOG.exceptionCorrelateMessageToSingleProcessDefinition(messageName, correlations.size(), correlationSet);

    } else if (correlations.size() == 1) {
      return correlations.get(0);

    } else {
      return null;
    }
  }

  public List<CorrelationHandlerResult> correlateMessages(CommandContext commandContext, String messageName, CorrelationSet correlationSet) {
    List<CorrelationHandlerResult> results = new ArrayList<>();

    // first collect correlations to executions
    results.addAll(correlateMessageToExecutions(commandContext, messageName, correlationSet));
    // now collect correlations to process definition, if enabled
    if (!correlationSet.isExecutionsOnly()) {
      results.addAll(correlateStartMessages(commandContext, messageName, correlationSet));
    }

    return results;
  }

  protected List<CorrelationHandlerResult> correlateMessageToExecutions(CommandContext commandContext, String messageName, CorrelationSet correlationSet) {

    ExecutionQueryImpl query = new ExecutionQueryImpl();

    Map<String, Object> correlationKeys = correlationSet.getCorrelationKeys();
    if (correlationKeys != null) {
      for (Map.Entry<String, Object> correlationKey : correlationKeys.entrySet()) {
        query.processVariableValueEquals(correlationKey.getKey(), correlationKey.getValue());
      }
    }

    Map<String, Object> localCorrelationKeys = correlationSet.getLocalCorrelationKeys();
    if (localCorrelationKeys != null) {
      for (Map.Entry<String, Object> correlationKey : localCorrelationKeys.entrySet()) {
        query.variableValueEquals(correlationKey.getKey(), correlationKey.getValue());
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

    List<CorrelationHandlerResult> result = new ArrayList<>(matchingExecutions.size());

    for (Execution matchingExecution : matchingExecutions) {
      CorrelationHandlerResult correlationResult = CorrelationHandlerResult.matchedExecution((ExecutionEntity) matchingExecution);
      if (!commandContext.getDbEntityManager().isDeleted(correlationResult.getExecutionEntity())) {
        result.add(correlationResult);
      }
    }

    return result;
  }

  @Override
  public List<CorrelationHandlerResult> correlateStartMessages(CommandContext commandContext, String messageName, CorrelationSet correlationSet) {
    if (messageName == null) {
      // ignore empty message name
      return Collections.emptyList();
    }

    if (correlationSet.getProcessDefinitionId() == null) {
      return correlateStartMessageByEventSubscription(commandContext, messageName, correlationSet);

    } else {
      CorrelationHandlerResult correlationResult = correlateStartMessageByProcessDefinitionId(commandContext, messageName, correlationSet.getProcessDefinitionId());
      if (correlationResult != null) {
        return Collections.singletonList(correlationResult);
      } else {
        return Collections.emptyList();
      }
    }
  }

  protected List<CorrelationHandlerResult> correlateStartMessageByEventSubscription(CommandContext commandContext, String messageName, CorrelationSet correlationSet) {
    List<CorrelationHandlerResult> results = new ArrayList<>();
    DeploymentCache deploymentCache = commandContext.getProcessEngineConfiguration().getDeploymentCache();

    List<EventSubscriptionEntity> messageEventSubscriptions = findMessageStartEventSubscriptions(commandContext, messageName, correlationSet);
    for (EventSubscriptionEntity messageEventSubscription : messageEventSubscriptions) {

      if (messageEventSubscription.getConfiguration() != null) {
        String processDefinitionId = messageEventSubscription.getConfiguration();
        ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
        // only an active process definition will be returned
        if (processDefinition != null && !processDefinition.isSuspended()) {
          CorrelationHandlerResult result = CorrelationHandlerResult.matchedProcessDefinition(processDefinition, messageEventSubscription.getActivityId());
          results.add(result);

        } else {
          LOG.couldNotFindProcessDefinitionForEventSubscription(messageEventSubscription, processDefinitionId);
        }
      }
    }
    return results;
  }

  protected List<EventSubscriptionEntity> findMessageStartEventSubscriptions(CommandContext commandContext, String messageName, CorrelationSet correlationSet) {
    EventSubscriptionManager eventSubscriptionManager = commandContext.getEventSubscriptionManager();

    if (correlationSet.isTenantIdSet) {
      EventSubscriptionEntity eventSubscription = eventSubscriptionManager.findMessageStartEventSubscriptionByNameAndTenantId(messageName, correlationSet.getTenantId());
      if (eventSubscription != null) {
        return Collections.singletonList(eventSubscription);
      } else {
        return Collections.emptyList();
      }

    } else {
      return eventSubscriptionManager.findMessageStartEventSubscriptionByName(messageName);
    }
  }

  protected CorrelationHandlerResult correlateStartMessageByProcessDefinitionId(CommandContext commandContext, String messageName, String processDefinitionId) {
    DeploymentCache deploymentCache = commandContext.getProcessEngineConfiguration().getDeploymentCache();
    ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
    // only an active process definition will be returned
    if (processDefinition != null && !processDefinition.isSuspended()) {

      String startActivityId = findStartActivityIdByMessage(processDefinition, messageName);
      if (startActivityId != null) {
        return CorrelationHandlerResult.matchedProcessDefinition(processDefinition, startActivityId);
      }
    }
    return null;
  }

  protected String findStartActivityIdByMessage(ProcessDefinitionEntity processDefinition, String messageName) {
    for (EventSubscriptionDeclaration declaration : EventSubscriptionDeclaration.getDeclarationsForScope(processDefinition).values()) {
      if (isMessageStartEventWithName(declaration, messageName)) {
        return declaration.getActivityId();
      }
    }
    return null;
  }

  protected boolean isMessageStartEventWithName(EventSubscriptionDeclaration declaration, String messageName) {
    return EventType.MESSAGE.name().equals(declaration.getEventType()) && declaration.isStartEvent()
        && messageName.equals(declaration.getUnresolvedEventName());
  }

}
