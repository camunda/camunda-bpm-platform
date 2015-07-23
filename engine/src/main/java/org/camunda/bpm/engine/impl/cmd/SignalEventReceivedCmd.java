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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;


/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class SignalEventReceivedCmd implements Command<Void> {

  private final static Logger LOGGER = Logger.getLogger(SignalEventReceivedCmd.class.getName());

  protected final String eventName;
  protected final String executionId;
  protected final Map<String, Object> variables;

  public SignalEventReceivedCmd(String eventName, String executionId, Map<String, Object> variables) {
    this.eventName = eventName;
    this.executionId = executionId;
    this.variables = variables;
  }

  @Override
  public Void execute(final CommandContext commandContext) {
    final EventSubscriptionManager eventSubscriptionManager = commandContext.getEventSubscriptionManager();
    final AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();

    if(executionId == null) {

      List<SignalEventSubscriptionEntity> signalEventSubscriptions = eventSubscriptionManager.findSignalEventSubscriptionsByEventName(eventName);

      List<SignalEventSubscriptionEntity> catchSignalEventSubscription = filterIntermediateSubscriptions(signalEventSubscriptions);
      List<SignalEventSubscriptionEntity> startSignalEventSubscriptions = filterStartSubscriptions(signalEventSubscriptions);
      Map<String, ProcessDefinitionEntity> processDefinitions = getProcessDefinitionsOfSubscriptions(startSignalEventSubscriptions);

      checkAuthorizationOfCatchSignals(authorizationManager, catchSignalEventSubscription);
      checkAuthorizationOfStartSignals(authorizationManager, startSignalEventSubscriptions, processDefinitions);

      notifyExecutions(catchSignalEventSubscription);
      startProcessInstances(startSignalEventSubscriptions, processDefinitions);

    } else {

      ExecutionManager executionManager = commandContext.getExecutionManager();
      ExecutionEntity execution = executionManager.findExecutionById(executionId);
      ensureNotNull("Cannot find execution with id '" + executionId + "'", "execution", execution);

      List<SignalEventSubscriptionEntity> signalEvents = eventSubscriptionManager.findSignalEventSubscriptionsByNameAndExecution(eventName, executionId);
      ensureNotEmpty("Execution '" + executionId + "' has not subscribed to a signal event with name '" + eventName + "'.", signalEvents);

      checkAuthorizationOfCatchSignals(authorizationManager, signalEvents);
      notifyExecutions(signalEvents);
    }

    return null;
  }

  protected Map<String, ProcessDefinitionEntity> getProcessDefinitionsOfSubscriptions(List<SignalEventSubscriptionEntity> startSignalEventSubscriptions) {
    DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();

    Map<String, ProcessDefinitionEntity> processDefinitions = new HashMap<String, ProcessDefinitionEntity>();

    for (SignalEventSubscriptionEntity eventSubscription : startSignalEventSubscriptions) {

      String processDefinitionId = eventSubscription.getConfiguration();
      ensureNotNull("Configuration of signal start event subscription '" + eventSubscription.getId() + "' contains no process definition id.",
          processDefinitionId);

      ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
      if (processDefinition == null || processDefinition.isSuspended()) {
        // ignore event subscription
        LOGGER.log(Level.FINE, "Found event subscription with {0} but process definition {1} could not be found.",
            new Object[] { eventSubscription, processDefinitionId });
      } else {
        processDefinitions.put(eventSubscription.getId(), processDefinition);
      }
    }

    return processDefinitions;
  }

  protected void checkAuthorizationOfCatchSignals(final AuthorizationManager authorizationManager, List<SignalEventSubscriptionEntity> catchSignalEventSubscription) {
    // check authorization for each fetched signal event
    for (SignalEventSubscriptionEntity event : catchSignalEventSubscription) {
      String processInstanceId = event.getProcessInstanceId();
      authorizationManager.checkUpdateProcessInstanceById(processInstanceId);
    }
  }

  private void checkAuthorizationOfStartSignals(final AuthorizationManager authorizationManager,
      List<SignalEventSubscriptionEntity> startSignalEventSubscriptions, Map<String, ProcessDefinitionEntity> processDefinitions) {
    // check authorization for process definition
    for (SignalEventSubscriptionEntity signalStartEventSubscription : startSignalEventSubscriptions) {
      ProcessDefinitionEntity processDefinition = processDefinitions.get(signalStartEventSubscription.getId());
      if (processDefinition != null) {
        authorizationManager.checkCreateProcessInstance(processDefinition);
      }
    }
  }

  private void notifyExecutions(List<SignalEventSubscriptionEntity> catchSignalEventSubscription) {
    HashMap<String, Object> payload = null;
    if (variables != null) {
      payload = new HashMap<String, Object>(variables);
    }

    for (SignalEventSubscriptionEntity signalEventSubscriptionEntity : catchSignalEventSubscription) {
      if (isActiveEventSubscription(signalEventSubscriptionEntity)) {
        signalEventSubscriptionEntity.eventReceived(payload, false);
      }
    }
  }

  private boolean isActiveEventSubscription(SignalEventSubscriptionEntity signalEventSubscriptionEntity) {
    ExecutionEntity execution = signalEventSubscriptionEntity.getExecution();
    return !execution.isEnded() && !execution.isCanceled();
  }

  private void startProcessInstances(List<SignalEventSubscriptionEntity> startSignalEventSubscriptions, Map<String, ProcessDefinitionEntity> processDefinitions) {
    for (SignalEventSubscriptionEntity signalStartEventSubscription : startSignalEventSubscriptions) {
      ProcessDefinitionEntity processDefinition = processDefinitions.get(signalStartEventSubscription.getId());
      if (processDefinition != null) {

        ActivityImpl signalStartEvent = processDefinition.findActivity(signalStartEventSubscription.getActivityId());
        PvmProcessInstance processInstance = processDefinition.createProcessInstanceForInitial(signalStartEvent);
        processInstance.start(variables);
      }
    }
  }

  protected List<SignalEventSubscriptionEntity> filterIntermediateSubscriptions(List<SignalEventSubscriptionEntity> subscriptions) {
    List<SignalEventSubscriptionEntity> result = new ArrayList<SignalEventSubscriptionEntity>();

    for (SignalEventSubscriptionEntity subscription : subscriptions) {
      if (subscription.getExecutionId() != null) {
        result.add(subscription);
      }
    }

    return result;
  }

  protected List<SignalEventSubscriptionEntity> filterStartSubscriptions(List<SignalEventSubscriptionEntity> subscriptions) {
    List<SignalEventSubscriptionEntity> result = new ArrayList<SignalEventSubscriptionEntity>();

    for (SignalEventSubscriptionEntity subscription : subscriptions) {
      if (subscription.getExecutionId() == null) {
        result.add(subscription);
      }
    }

    return result;
  }

}
