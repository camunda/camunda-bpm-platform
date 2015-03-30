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
package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.runtime.ProcessInstanceActivityInstantiationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessInstanceModificationDto {

  public static String CANCEL_INSTRUCTION_TYPE = "cancel";
  public static String START_BEFORE_INSTRUCTION_TYPE = "startBeforeActivity";
  public static String START_TRANSITION_INSTRUCTION_TYPE = "startTransition";
  public static String START_AFTER_INSTRUCTION_TYPE = "startAfterActivity";

  protected boolean skipCustomListeners = false;
  protected boolean skipIoMappings = false;
  protected List<ProcessInstanceModificationInstructionDto> instructions;

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public void setSkipCustomListeners(boolean skipCustomListeners) {
    this.skipCustomListeners = skipCustomListeners;
  }

  public boolean isSkipIoMappings() {
    return skipIoMappings;
  }

  public void setSkipIoMappings(boolean skipIoMappings) {
    this.skipIoMappings = skipIoMappings;
  }

  public List<ProcessInstanceModificationInstructionDto> getInstructions() {
    return instructions;
  }

  public void setInstructions(List<ProcessInstanceModificationInstructionDto> instructions) {
    this.instructions = instructions;
  }

  public void applyTo(ProcessInstanceModificationBuilder builder, ProcessEngine engine, ObjectMapper objectMapper) {
    for (ProcessInstanceModificationInstructionDto instruction : instructions) {
      String type = instruction.getType();

      if (CANCEL_INSTRUCTION_TYPE.equals(type)) {
        applyCancellation(instruction, builder);
      }
      else if (START_BEFORE_INSTRUCTION_TYPE.equals(type)) {
        applyStartBefore(instruction, builder, engine, objectMapper);
      }
      else if (START_AFTER_INSTRUCTION_TYPE.equals(type)) {
        applyStartAfter(instruction, builder, engine, objectMapper);
      }
      else if (START_TRANSITION_INSTRUCTION_TYPE.equals(type)) {
        applyStartTransition(instruction, builder, engine, objectMapper);
      }
      else {
        throw new InvalidRequestException(Status.BAD_REQUEST, "Unrecognized instruction type " + type);
      }
    }
  }

  protected void applyCancellation(ProcessInstanceModificationInstructionDto instruction,
      ProcessInstanceModificationBuilder builder) {
    String activityId = instruction.getActivityId();
    String activityInstanceId = instruction.getActivityInstanceId();

    if (activityId == null && activityInstanceId == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST,
          buildErrorMessage(CANCEL_INSTRUCTION_TYPE, "either 'activityId' or 'activityInstanceId' is required"));
    }
    if (activityId != null && activityInstanceId != null) {
      throw new InvalidRequestException(Status.BAD_REQUEST,
          buildErrorMessage(CANCEL_INSTRUCTION_TYPE , "only one, 'activityId' or 'activityInstanceId', can be set"));
    }

    if (activityId != null) {
      builder.cancelAllForActivity(activityId);
    }
    else if (activityInstanceId != null) {
      builder.cancelActivityInstance(activityInstanceId);
    }
  }

  protected void applyStartBefore(ProcessInstanceModificationInstructionDto instruction,
      ProcessInstanceModificationBuilder builder, ProcessEngine engine, ObjectMapper objectMapper) {
    Map<String, TriggerVariableValueDto> variables = instruction.getVariables();
    String activityId = instruction.getActivityId();
    String ancestorInstanceId = instruction.getAncestorActivityInstanceId();

    if (instruction.getActivityId() == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST,
          buildErrorMessage(START_BEFORE_INSTRUCTION_TYPE, "'activityId' must be set"));
    }

    ProcessInstanceActivityInstantiationBuilder activityBuilder = null;

    if (instruction.getAncestorActivityInstanceId() != null) {
      activityBuilder = builder.startBeforeActivity(activityId, ancestorInstanceId);
    }
    else {
      activityBuilder = builder.startBeforeActivity(activityId);
    }

    if (variables != null) {
      for (Map.Entry<String, TriggerVariableValueDto> variableValue : variables.entrySet()) {
        TriggerVariableValueDto value = variableValue.getValue();

        if (value.isLocal()) {
          activityBuilder.setVariableLocal(variableValue.getKey(), value.toTypedValue(engine, objectMapper));
        }
        else {
          activityBuilder.setVariable(variableValue.getKey(), value.toTypedValue(engine, objectMapper));

        }
      }
    }
  }

  protected void applyStartAfter(ProcessInstanceModificationInstructionDto instruction,
      ProcessInstanceModificationBuilder builder, ProcessEngine engine, ObjectMapper objectMapper) {
    Map<String, TriggerVariableValueDto> variables = instruction.getVariables();
    String activityId = instruction.getActivityId();
    String ancestorInstanceId = instruction.getAncestorActivityInstanceId();

    if (instruction.getActivityId() == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST,
          buildErrorMessage(START_AFTER_INSTRUCTION_TYPE, "'activityId' must be set"));
    }

    ProcessInstanceActivityInstantiationBuilder activityBuilder = null;

    if (instruction.getAncestorActivityInstanceId() != null) {
      activityBuilder = builder.startAfterActivity(activityId, ancestorInstanceId);
    }
    else {
      activityBuilder = builder.startAfterActivity(activityId);
    }

    if (variables != null) {
      for (Map.Entry<String, TriggerVariableValueDto> variableValue : variables.entrySet()) {
        TriggerVariableValueDto value = variableValue.getValue();

        if (value.isLocal()) {
          activityBuilder.setVariableLocal(variableValue.getKey(), value.toTypedValue(engine, objectMapper));
        }
        else {
          activityBuilder.setVariable(variableValue.getKey(), value.toTypedValue(engine, objectMapper));

        }
      }
    }
  }

  protected void applyStartTransition(ProcessInstanceModificationInstructionDto instruction,
      ProcessInstanceModificationBuilder builder, ProcessEngine engine, ObjectMapper objectMapper) {
    Map<String, TriggerVariableValueDto> variables = instruction.getVariables();
    String activityId = instruction.getActivityId();
    String ancestorInstanceId = instruction.getAncestorActivityInstanceId();

    if (instruction.getActivityId() == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST,
          buildErrorMessage(START_TRANSITION_INSTRUCTION_TYPE, "'activityId' must be set"));
    }

    ProcessInstanceActivityInstantiationBuilder activityBuilder = null;

    if (instruction.getAncestorActivityInstanceId() != null) {
      activityBuilder = builder.startTransition(activityId, ancestorInstanceId);
    }
    else {
      activityBuilder = builder.startTransition(activityId);
    }

    if (variables != null) {
      for (Map.Entry<String, TriggerVariableValueDto> variableValue : variables.entrySet()) {
        TriggerVariableValueDto value = variableValue.getValue();

        if (value.isLocal()) {
          activityBuilder.setVariableLocal(variableValue.getKey(), value.toTypedValue(engine, objectMapper));
        }
        else {
          activityBuilder.setVariable(variableValue.getKey(), value.toTypedValue(engine, objectMapper));

        }
      }
    }
  }

  protected String buildErrorMessage(String instructionType, String message) {
    return "For instruction type '" + instructionType + "': " +  message;
  }
}
