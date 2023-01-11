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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.SignalEventReceivedBuilderImpl;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;


/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class SignalEventReceivedCmd implements Command<Void> {

  protected final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected final SignalEventReceivedBuilderImpl builder;

  public SignalEventReceivedCmd(SignalEventReceivedBuilderImpl builder) {
    this.builder = builder;
  }

  @Override
  public Void execute(final CommandContext commandContext) {

    String signalName = builder.getSignalName();
    String executionId = builder.getExecutionId();

    if(executionId == null) {
      sendSignal(commandContext, signalName);

    } else {
      sendSignalToExecution(commandContext, signalName, executionId);
    }
    return null;
  }

  protected void sendSignal(CommandContext commandContext, String signalName) {

    List<EventSubscriptionEntity> signalEventSubscriptions = findSignalEventSubscriptions(commandContext, signalName);

    List<EventSubscriptionEntity> catchSignalEventSubscription = filterIntermediateSubscriptions(signalEventSubscriptions);
    List<EventSubscriptionEntity> startSignalEventSubscriptions = filterStartSubscriptions(signalEventSubscriptions);
    Map<String, ProcessDefinitionEntity> processDefinitions = getProcessDefinitionsOfSubscriptions(startSignalEventSubscriptions);

    checkAuthorizationOfCatchSignals(commandContext, catchSignalEventSubscription);
    checkAuthorizationOfStartSignals(commandContext, startSignalEventSubscriptions, processDefinitions);

    notifyExecutions(catchSignalEventSubscription);
    startProcessInstances(startSignalEventSubscriptions, processDefinitions);
  }

  protected List<EventSubscriptionEntity> findSignalEventSubscriptions(CommandContext commandContext, String signalName) {
    EventSubscriptionManager eventSubscriptionManager = commandContext.getEventSubscriptionManager();

    if (builder.isTenantIdSet()) {
      return eventSubscriptionManager.findSignalEventSubscriptionsByEventNameAndTenantId(signalName, builder.getTenantId());

    } else {
      return eventSubscriptionManager.findSignalEventSubscriptionsByEventName(signalName);
    }
  }

  protected Map<String, ProcessDefinitionEntity> getProcessDefinitionsOfSubscriptions(List<EventSubscriptionEntity> startSignalEventSubscriptions) {
    DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();

    Map<String, ProcessDefinitionEntity> processDefinitions = new HashMap<String, ProcessDefinitionEntity>();

    for (EventSubscriptionEntity eventSubscription : startSignalEventSubscriptions) {

      String processDefinitionId = eventSubscription.getConfiguration();
      ensureNotNull("Configuration of signal start event subscription '" + eventSubscription.getId() + "' contains no process definition id.",
          processDefinitionId);

      ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
      if (processDefinition != null && !processDefinition.isSuspended()) {
        processDefinitions.put(eventSubscription.getId(), processDefinition);
      }
    }

    return processDefinitions;
  }

  protected void sendSignalToExecution(CommandContext commandContext, String signalName, String executionId) {

    ExecutionManager executionManager = commandContext.getExecutionManager();
    ExecutionEntity execution = executionManager.findExecutionById(executionId);
    ensureNotNull("Cannot find execution with id '" + executionId + "'", "execution", execution);

    EventSubscriptionManager eventSubscriptionManager = commandContext.getEventSubscriptionManager();
    List<EventSubscriptionEntity> signalEvents = eventSubscriptionManager.findSignalEventSubscriptionsByNameAndExecution(signalName, executionId);
    ensureNotEmpty(NotFoundException.class, "Execution '" + executionId + "' has not subscribed to a signal event with name '" + signalName + "'.", signalEvents);

    checkAuthorizationOfCatchSignals(commandContext, signalEvents);
    notifyExecutions(signalEvents);
  }

  protected void checkAuthorizationOfCatchSignals(final CommandContext commandContext, List<EventSubscriptionEntity> catchSignalEventSubscription) {
    // check authorization for each fetched signal event
    for (EventSubscriptionEntity event : catchSignalEventSubscription) {
      String processInstanceId = event.getProcessInstanceId();
      for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
        checker.checkUpdateProcessInstanceById(processInstanceId);
      }
    }
  }

  private void checkAuthorizationOfStartSignals(final CommandContext commandContext,
      List<EventSubscriptionEntity> startSignalEventSubscriptions, Map<String, ProcessDefinitionEntity> processDefinitions) {
    // check authorization for process definition
    for (EventSubscriptionEntity signalStartEventSubscription : startSignalEventSubscriptions) {
      ProcessDefinitionEntity processDefinition = processDefinitions.get(signalStartEventSubscription.getId());
      if (processDefinition != null) {

        for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
          checker.checkCreateProcessInstance(processDefinition);
        }

      }
    }
  }

  private void notifyExecutions(List<EventSubscriptionEntity> catchSignalEventSubscription) {

    for (EventSubscriptionEntity signalEventSubscriptionEntity : catchSignalEventSubscription) {
      if (isActiveEventSubscription(signalEventSubscriptionEntity)) {
        signalEventSubscriptionEntity.eventReceived(builder.getVariables(), false);
      }
    }
  }

  private boolean isActiveEventSubscription(EventSubscriptionEntity signalEventSubscriptionEntity) {
    ExecutionEntity execution = signalEventSubscriptionEntity.getExecution();
    return !execution.isEnded() && !execution.isCanceled();
  }

  private void startProcessInstances(List<EventSubscriptionEntity> startSignalEventSubscriptions, Map<String, ProcessDefinitionEntity> processDefinitions) {
    for (EventSubscriptionEntity signalStartEventSubscription : startSignalEventSubscriptions) {
      ProcessDefinitionEntity processDefinition = processDefinitions.get(signalStartEventSubscription.getId());
      if (processDefinition != null) {

        ActivityImpl signalStartEvent = processDefinition.findActivity(signalStartEventSubscription.getActivityId());
        PvmProcessInstance processInstance = processDefinition.createProcessInstanceForInitial(signalStartEvent);
        processInstance.start(builder.getVariables());
      }
    }
  }

  protected List<EventSubscriptionEntity> filterIntermediateSubscriptions(List<EventSubscriptionEntity> subscriptions) {
    List<EventSubscriptionEntity> result = new ArrayList<EventSubscriptionEntity>();

    for (EventSubscriptionEntity subscription : subscriptions) {
      if (subscription.getExecutionId() != null) {
        result.add(subscription);
      }
    }

    return result;
  }

  protected List<EventSubscriptionEntity> filterStartSubscriptions(List<EventSubscriptionEntity> subscriptions) {
    List<EventSubscriptionEntity> result = new ArrayList<EventSubscriptionEntity>();

    for (EventSubscriptionEntity subscription : subscriptions) {
      if (subscription.getExecutionId() == null) {
        result.add(subscription);
      }
    }

    return result;
  }

}
