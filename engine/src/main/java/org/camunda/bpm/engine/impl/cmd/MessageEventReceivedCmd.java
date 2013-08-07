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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.event.MessageEventHandler;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;


/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class MessageEventReceivedCmd implements Command<Void>, Serializable {
  
  private static final long serialVersionUID = 1L;
  
  protected final String executionId;
  protected final Map<String, Object> processVariables;
  protected final String messageName;

  public MessageEventReceivedCmd(String messageName, String executionId, Map<String, Object> processVariables) {
    this.executionId = executionId;
    this.messageName = messageName;
    this.processVariables = processVariables;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    if(executionId == null) {
      throw new ProcessEngineException("executionId is null");
    }

    if(messageName == null) {
      throw new ProcessEngineException("messageName cannot be null");
    }
    
    List<EventSubscriptionEntity> eventSubscriptions = commandContext.getEventSubscriptionManager()
      .findEventSubscriptionsByNameAndExecution(MessageEventHandler.EVENT_HANDLER_TYPE, messageName, executionId);
    
    if(eventSubscriptions.isEmpty()) {
      throw new ProcessEngineException("Execution with id '"+executionId+"' does not have a subscription to a message event with name '"+messageName+"'");
    }
    
    // there can be only one:
    EventSubscriptionEntity eventSubscriptionEntity = eventSubscriptions.get(0);
    
    HashMap<String, Object> payload = null;
    if(processVariables != null) {
      payload = new HashMap<String, Object>(processVariables);
    }
    
    eventSubscriptionEntity.eventReceived(payload, false);
    
    return null;
  }

  
}
