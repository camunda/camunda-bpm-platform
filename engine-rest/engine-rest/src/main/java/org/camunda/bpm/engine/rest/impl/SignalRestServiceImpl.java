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
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.SignalRestService;
import org.camunda.bpm.engine.rest.dto.SignalDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.runtime.SignalEventReceivedBuilder;

import javax.ws.rs.core.Response.Status;
import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class SignalRestServiceImpl extends AbstractRestProcessEngineAware implements SignalRestService {

  public SignalRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public void throwSignal(SignalDto dto) {
    String name = dto.getName();
    if (name == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "No signal name given");
    }

    RuntimeService runtimeService = processEngine.getRuntimeService();
    SignalEventReceivedBuilder signal = runtimeService.createSignalEvent(name);

    String executionId = dto.getExecutionId();
    if (executionId != null) {
      signal.executionId(executionId);
    }

    Map<String, VariableValueDto> variablesDto = dto.getVariables();
    if (variablesDto != null) {
      try {
        Map<String, Object> variables = VariableValueDto.toMap(variablesDto, processEngine, objectMapper);
        signal.setVariables(variables);
      } catch (RestException e) {
        throw new InvalidRequestException(e.getStatus(), e, e.getMessage());
      }
    }

    String tenantId = dto.getTenantId();
    if (tenantId != null) {
      signal.tenantId(tenantId);
    }

    boolean isTenantIdSet = dto.isWithoutTenantId();
    if (isTenantIdSet) {
      signal.withoutTenantId();
    }

    try {
      signal.send();
    } catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, e.getMessage());
    } catch (AuthorizationException e) {
      throw new InvalidRequestException(Status.FORBIDDEN, e, e.getMessage());
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, e, e.getMessage());
    }
  }
}
