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
package org.camunda.bpm.engine.rest.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.MessageRestService;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;

import javax.ws.rs.core.Response.Status;
import java.util.Map;
import java.util.Map.Entry;

public class MessageRestServiceImpl extends AbstractRestProcessEngineAware implements MessageRestService {

  public MessageRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public void deliverMessage(CorrelationMessageDto messageDto) {

    if (messageDto.getMessageName() == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "No message name supplied");
    }

    RuntimeService runtimeService = processEngine.getRuntimeService();

    try {
      ObjectMapper objectMapper = getObjectMapper();
      Map<String, Object> correlationKeys = VariableValueDto.toMap(messageDto.getCorrelationKeys(), processEngine, objectMapper);
      Map<String, Object> processVariables = VariableValueDto.toMap(messageDto.getProcessVariables(), processEngine, objectMapper);

      MessageCorrelationBuilder correlation = runtimeService
          .createMessageCorrelation(messageDto.getMessageName())
          .setVariables(processVariables)
          .processInstanceBusinessKey(messageDto.getBusinessKey());

      if (correlationKeys != null && !correlationKeys.isEmpty()) {
        for (Entry<String, Object> correlationKey  : correlationKeys.entrySet()) {
          String name = correlationKey.getKey();
          Object value = correlationKey.getValue();
          correlation.processInstanceVariableEquals(name, value);
        }
      }

      if (!messageDto.isAll()) {
        correlation.correlate();
      } else {
        correlation.correlateAll();
      }

    } catch (RestException e) {
      String errorMessage = String.format("Cannot deliver message: %s", e.getMessage());
      throw new InvalidRequestException(e.getStatus(), e, errorMessage);

    } catch (MismatchingMessageCorrelationException e) {
      throw new RestException(Status.BAD_REQUEST, e);

    }

  }

}
