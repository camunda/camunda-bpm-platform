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
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNumberOfElements;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionManager;


/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class MessageEventReceivedCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected final String executionId;
  protected final Map<String, Object> processVariables;
  protected final String messageName;
  protected boolean exclusive = false;

  public MessageEventReceivedCmd(String messageName, String executionId, Map<String, Object> processVariables) {
    this.executionId = executionId;
    this.messageName = messageName;
    this.processVariables = processVariables;
  }

  public MessageEventReceivedCmd(String messageName, String executionId, Map<String, Object> processVariables, boolean exclusive) {
    this(messageName, executionId, processVariables);
    this.exclusive = exclusive;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    ensureNotNull("executionId", executionId);

    EventSubscriptionManager eventSubscriptionManager = commandContext.getEventSubscriptionManager();
    List<EventSubscriptionEntity> eventSubscriptions = null;
    if (messageName != null) {
      eventSubscriptions = eventSubscriptionManager.findEventSubscriptionsByNameAndExecution(
              EventType.MESSAGE.name(), messageName, executionId, exclusive);
    } else {
      eventSubscriptions = eventSubscriptionManager.findEventSubscriptionsByExecutionAndType(
          executionId, EventType.MESSAGE.name(), exclusive);
    }

    ensureNotEmpty("Execution with id '" + executionId + "' does not have a subscription to a message event with name '" + messageName + "'", "eventSubscriptions", eventSubscriptions);
    ensureNumberOfElements("More than one matching message subscription found for execution " + executionId, "eventSubscriptions", eventSubscriptions, 1);

    // there can be only one:
    EventSubscriptionEntity eventSubscriptionEntity = eventSubscriptions.get(0);

    // check authorization
    String processInstanceId = eventSubscriptionEntity.getProcessInstanceId();
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkUpdateProcessInstanceById(processInstanceId);
    }

    eventSubscriptionEntity.eventReceived(processVariables, false);

    return null;
  }


}
