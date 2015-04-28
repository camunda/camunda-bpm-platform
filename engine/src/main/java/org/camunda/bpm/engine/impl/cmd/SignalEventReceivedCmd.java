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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.impl.persistence.entity.SignalEventSubscriptionEntity;


/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class SignalEventReceivedCmd implements Command<Void> {

  protected final String eventName;
  protected final String executionId;
  protected final Map<String, Object> variables;

  public SignalEventReceivedCmd(String eventName, String executionId, Map<String, Object> variables) {
    this.eventName = eventName;
    this.executionId = executionId;
    this.variables = variables;
  }

  public Void execute(final CommandContext commandContext) {
    final EventSubscriptionManager eventSubscriptionManager = commandContext.getEventSubscriptionManager();
    List<SignalEventSubscriptionEntity> signalEvents = null;

    if(executionId == null) {
      signalEvents =eventSubscriptionManager.findSignalEventSubscriptionsByEventName(eventName);

    } else {

      ExecutionManager executionManager = commandContext.getExecutionManager();
      ExecutionEntity execution = executionManager.findExecutionById(executionId);
      ensureNotNull("Cannot find execution with id '" + executionId + "'", "execution", execution);

      signalEvents = eventSubscriptionManager.findSignalEventSubscriptionsByNameAndExecution(eventName, executionId);
      ensureNotEmpty("Execution '" + executionId + "' has not subscribed to a signal event with name '" + eventName + "'.", signalEvents);
    }

    // check authorization for each fetched signal event
    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    for (SignalEventSubscriptionEntity event : signalEvents) {
      String processInstanceId = event.getProcessInstanceId();
      authorizationManager.checkUpdateProcessInstanceById(processInstanceId);
    }

    HashMap<String, Object> payload = null;
    if(variables != null) {
      payload = new HashMap<String, Object>(variables);
    }

    for (SignalEventSubscriptionEntity signalEventSubscriptionEntity : signalEvents) {
      signalEventSubscriptionEntity.eventReceived(payload, false);
    }

    return null;
  }

}
