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

import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.MessageRestService;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.camunda.bpm.engine.rest.dto.message.MessageCorrelationResultDto;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;

public class MessageRestServiceImpl extends AbstractRestProcessEngineAware implements MessageRestService {

  public MessageRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public Response deliverMessage(CorrelationMessageDto messageDto) {
    if (messageDto.getMessageName() == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "No message name supplied");
    }
    if (messageDto.getTenantId() != null && messageDto.isWithoutTenantId()) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Parameter 'tenantId' cannot be used together with parameter 'withoutTenantId'.");
    }

    List<MessageCorrelationResultDto> resultDtos = new ArrayList<MessageCorrelationResultDto>();
    try {
      MessageCorrelationBuilder correlation = createMessageCorrelationBuilder(messageDto);
      if (!messageDto.isAll()) {
        MessageCorrelationResult result = correlation.correlateWithResult();
        resultDtos.add(MessageCorrelationResultDto.fromMessageCorrelationResult(result));
      } else {
        List<MessageCorrelationResult> results = correlation.correlateAllWithResult();
        for (MessageCorrelationResult result : results) {
          resultDtos.add(MessageCorrelationResultDto.fromMessageCorrelationResult(result));
        }
      }
    } catch (RestException e) {
      String errorMessage = String.format("Cannot deliver message: %s", e.getMessage());
      throw new InvalidRequestException(e.getStatus(), e, errorMessage);

    } catch (MismatchingMessageCorrelationException e) {
      throw new RestException(Status.BAD_REQUEST, e);
    }
    return createResponse(resultDtos, messageDto);
  }


  protected Response createResponse(List<MessageCorrelationResultDto> resultDtos, CorrelationMessageDto messageDto) {
    Response.ResponseBuilder response = Response.noContent();
    if (messageDto.isResultEnabled()) {
      response = Response.ok(resultDtos, MediaType.APPLICATION_JSON);
    }
    return response.build();
  }

  protected MessageCorrelationBuilder createMessageCorrelationBuilder(CorrelationMessageDto messageDto) {
    RuntimeService runtimeService = processEngine.getRuntimeService();

    ObjectMapper objectMapper = getObjectMapper();
    Map<String, Object> correlationKeys = VariableValueDto.toMap(messageDto.getCorrelationKeys(), processEngine, objectMapper);
    Map<String, Object> localCorrelationKeys = VariableValueDto.toMap(messageDto.getLocalCorrelationKeys(), processEngine, objectMapper);
    Map<String, Object> processVariables = VariableValueDto.toMap(messageDto.getProcessVariables(), processEngine, objectMapper);

    MessageCorrelationBuilder builder = runtimeService
        .createMessageCorrelation(messageDto.getMessageName());

    if (processVariables != null) {
      builder.setVariables(processVariables);
    }
    if (messageDto.getBusinessKey() != null) {
      builder.processInstanceBusinessKey(messageDto.getBusinessKey());
    }

    if (correlationKeys != null && !correlationKeys.isEmpty()) {
      for (Entry<String, Object> correlationKey  : correlationKeys.entrySet()) {
        String name = correlationKey.getKey();
        Object value = correlationKey.getValue();
        builder.processInstanceVariableEquals(name, value);
      }
    }

    if (localCorrelationKeys != null && !localCorrelationKeys.isEmpty()) {
      for (Entry<String, Object> correlationKey  : localCorrelationKeys.entrySet()) {
        String name = correlationKey.getKey();
        Object value = correlationKey.getValue();
        builder.localVariableEquals(name, value);
      }
    }

    if (messageDto.getTenantId() != null) {
      builder.tenantId(messageDto.getTenantId());

    } else if (messageDto.isWithoutTenantId()) {
      builder.withoutTenantId();
    }

    String processInstanceId = messageDto.getProcessInstanceId();
    if (processInstanceId != null) {
      builder.processInstanceId(processInstanceId);
    }

    return builder;
  }

}
