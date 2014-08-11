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

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotAllowedException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.rest.dto.runtime.CaseExecutionTriggerDto;
import org.camunda.bpm.engine.rest.dto.runtime.CaseInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.TriggerVariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableNameDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.VariableResource;
import org.camunda.bpm.engine.rest.sub.runtime.CaseInstanceResource;
import org.camunda.bpm.engine.rest.util.DtoUtil;
import org.camunda.bpm.engine.runtime.CaseExecutionCommandBuilder;
import org.camunda.bpm.engine.runtime.CaseInstance;

/**
 *
 * @author Roman Smirnov
 *
 */
public class CaseInstanceResourceImpl implements CaseInstanceResource {

  protected ProcessEngine engine;
  protected String caseInstanceId;

  public CaseInstanceResourceImpl(ProcessEngine engine, String caseInstanceId) {
    this.engine = engine;
    this.caseInstanceId = caseInstanceId;
  }

  public CaseInstanceDto getCaseInstance() {
    CaseService caseService = engine.getCaseService();

    CaseInstance instance = caseService
        .createCaseInstanceQuery()
        .caseInstanceId(caseInstanceId)
        .singleResult();

    if (instance == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Case instance with id " + caseInstanceId + " does not exist.");
    }

    CaseInstanceDto result = CaseInstanceDto.fromCaseInstance(instance);
    return result;
  }

  public void complete(CaseExecutionTriggerDto triggerDto) {
    try {
      CaseService caseService = engine.getCaseService();
      CaseExecutionCommandBuilder commandBuilder = caseService.withCaseExecution(caseInstanceId);

      initializeCommand(commandBuilder, triggerDto, "complete");

      commandBuilder.complete();

    } catch (NotFoundException e) {
      throwInvalidRequestException("complete", Status.NOT_FOUND, e);

    } catch (NotValidException e) {
      throwInvalidRequestException("complete", Status.BAD_REQUEST, e);

    } catch (NotAllowedException e) {
      throwInvalidRequestException("complete", Status.BAD_REQUEST, e);

    } catch (ProcessEngineException e) {
      throwRestException("complete", Status.INTERNAL_SERVER_ERROR, e);

    }
  }

  public void close(CaseExecutionTriggerDto triggerDto) {
    try {
      CaseService caseService = engine.getCaseService();
      CaseExecutionCommandBuilder commandBuilder = caseService.withCaseExecution(caseInstanceId);

      initializeCommand(commandBuilder, triggerDto, "close");

      commandBuilder.close();

    } catch (NotFoundException e) {
      throwInvalidRequestException("close", Status.NOT_FOUND, e);

    } catch (NotValidException e) {
      throwInvalidRequestException("close", Status.BAD_REQUEST, e);

    } catch (NotAllowedException e) {
      throwInvalidRequestException("close", Status.BAD_REQUEST, e);

    } catch (ProcessEngineException e) {
      throwRestException("close", Status.INTERNAL_SERVER_ERROR, e);

    }

  }

  protected void throwInvalidRequestException(String transition, Status status, ProcessEngineException cause) {
    String errorMessage = String.format("Cannot %s case instance %s: %s", transition, caseInstanceId, cause.getMessage());
    throw new InvalidRequestException(status, cause, errorMessage);
  }

  protected void throwRestException(String transition, Status status, ProcessEngineException cause) {
    String errorMessage = String.format("Cannot %s case instance %s: %s", transition, caseInstanceId, cause.getMessage());
    throw new RestException(status, cause, errorMessage);
  }

  protected void initializeCommand(CaseExecutionCommandBuilder commandBuilder, CaseExecutionTriggerDto triggerDto, String transition) {
    Map<String, TriggerVariableValueDto> variables = triggerDto.getVariables();
    if (variables != null && !variables.isEmpty()) {
      initializeCommandWithVariables(commandBuilder, variables, transition);
    }

    List<VariableNameDto> deletions = triggerDto.getDeletions();
    if (deletions != null && !deletions.isEmpty()) {
      initializeCommandWithDeletions(commandBuilder, deletions, transition);
    }
  }

  protected void initializeCommandWithVariables(CaseExecutionCommandBuilder commandBuilder, Map<String, TriggerVariableValueDto> variables, String transition) {
    for(String variableName : variables.keySet()) {
      try {
        TriggerVariableValueDto variableValue = variables.get(variableName);
        Object value = DtoUtil.toType(variableValue.getType(), variableValue.getValue());

        if (variableValue.isLocal()) {
          commandBuilder.setVariableLocal(variableName, value);

        } else {
          commandBuilder.setVariable(variableName, value);
        }

      } catch (NumberFormatException e) {
        String errorMessage = String.format("Cannot %s case instance %s due to number format exception of variable %s: %s", transition, caseInstanceId, variableName, e.getMessage());
        throw new RestException(Status.BAD_REQUEST, e, errorMessage);

      } catch (ParseException e) {
        String errorMessage = String.format("Cannot %s case instance %s due to parse exception of variable %s: %s", transition, variableName, variableName, e.getMessage());
        throw new RestException(Status.BAD_REQUEST, e, errorMessage);

      } catch (IllegalArgumentException e) {
        String errorMessage = String.format("Cannot %s case instance %s because of variable %s: %s", transition, variableName, variableName, e.getMessage());
        throw new RestException(Status.BAD_REQUEST, errorMessage);
      }
    }
  }

  protected void initializeCommandWithDeletions(CaseExecutionCommandBuilder commandBuilder, List<VariableNameDto> deletions, String transition) {
    for (VariableNameDto variableName : deletions) {
      if (variableName.isLocal()) {
        commandBuilder.removeVariableLocal(variableName.getName());
      } else {
        commandBuilder.removeVariable(variableName.getName());
      }
    }
  }

  public VariableResource getVariablesResource() {
    return new CaseExecutionVariablesResource(engine, caseInstanceId);
  }

}
