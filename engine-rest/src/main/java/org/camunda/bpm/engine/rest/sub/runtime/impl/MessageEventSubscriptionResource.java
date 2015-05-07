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
package org.camunda.bpm.engine.rest.sub.runtime.impl;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.EventSubscriptionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionTriggerDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.runtime.EventSubscriptionResource;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.variable.VariableMap;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageEventSubscriptionResource implements EventSubscriptionResource {

  protected static final String MESSAGE_EVENT_TYPE = "message";

  protected ProcessEngine engine;
  protected String executionId;
  protected String messageName;

  protected ObjectMapper objectMapper;

  public MessageEventSubscriptionResource(ProcessEngine engine, String executionId, String messageName, ObjectMapper objectMapper) {
    this.engine = engine;
    this.executionId = executionId;
    this.messageName = messageName;
    this.objectMapper = objectMapper;
  }

  @Override
  public EventSubscriptionDto getEventSubscription() {
    RuntimeService runtimeService = engine.getRuntimeService();
    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery()
        .executionId(executionId).eventName(messageName).eventType(MESSAGE_EVENT_TYPE).singleResult();

    if (eventSubscription == null) {
      String errorMessage = String.format("Message event subscription for execution %s named %s does not exist", executionId, messageName);
      throw new InvalidRequestException(Status.NOT_FOUND, errorMessage);
    }

    return EventSubscriptionDto.fromEventSubscription(eventSubscription);
  }

  @Override
  public void triggerEvent(ExecutionTriggerDto triggerDto) {
    RuntimeService runtimeService = engine.getRuntimeService();


    try {
      VariableMap variables = VariableValueDto.toMap(triggerDto.getVariables(), engine, objectMapper);
      runtimeService.messageEventReceived(messageName, executionId, variables);

    } catch (AuthorizationException e) {
      throw e;
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, String.format("Cannot trigger message %s for execution %s: %s",
        messageName, executionId, e.getMessage()));

    } catch (RestException e) {
      String errorMessage = String.format("Cannot trigger message %s for execution %s: %s", messageName, executionId, e.getMessage());
      throw new InvalidRequestException(e.getStatus(), e, errorMessage);

    }

  }

}
