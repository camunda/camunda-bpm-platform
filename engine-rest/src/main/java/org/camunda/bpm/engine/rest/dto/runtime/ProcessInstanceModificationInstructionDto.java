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

import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.runtime.ProcessInstanceActivityInstantiationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Thorben Lindhauer
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes(value = {
    @JsonSubTypes.Type(value = ProcessInstanceModificationInstructionDto.CancellationInstructionDto.class),
    @JsonSubTypes.Type(value = ProcessInstanceModificationInstructionDto.StartBeforeInstructionDto.class),
    @JsonSubTypes.Type(value = ProcessInstanceModificationInstructionDto.StartAfterInstructionDto.class),
    @JsonSubTypes.Type(value = ProcessInstanceModificationInstructionDto.StartTransitionInstructionDto.class)})
public abstract class ProcessInstanceModificationInstructionDto {

  public static final String CANCEL_INSTRUCTION_TYPE = "cancel";
  public static final String START_BEFORE_INSTRUCTION_TYPE = "startBeforeActivity";
  public static final String START_TRANSITION_INSTRUCTION_TYPE = "startTransition";
  public static final String START_AFTER_INSTRUCTION_TYPE = "startAfterActivity";

  protected String type;

  protected Map<String, TriggerVariableValueDto> variables;

  protected String activityId;
  protected String activityInstanceId;
  protected String transitionInstanceId;
  protected String ancestorActivityInstanceId;

  public Map<String, TriggerVariableValueDto> getVariables() {
    return variables;
  }
  public void setVariables(Map<String, TriggerVariableValueDto> variables) {
    this.variables = variables;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public String getActivityId() {
    return activityId;
  }
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }
  public String getActivityInstanceId() {
    return activityInstanceId;
  }
  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }
  public String getTransitionInstanceId() {
    return transitionInstanceId;
  }
  public void setTransitionInstanceId(String transitionInstanceId) {
    this.transitionInstanceId = transitionInstanceId;
  }
  public String getAncestorActivityInstanceId() {
    return ancestorActivityInstanceId;
  }
  public void setAncestorActivityInstanceId(String ancestorActivityInstanceId) {
    this.ancestorActivityInstanceId = ancestorActivityInstanceId;
  }

  public abstract void applyTo(ProcessInstanceModificationBuilder builder, ProcessEngine engine, ObjectMapper mapper);

  protected String buildErrorMessage(String message) {
    return "For instruction type '" + type + "': " +  message;
  }

  protected void applyVariables(ProcessInstanceActivityInstantiationBuilder builder,
      ProcessEngine engine, ObjectMapper mapper) {

    if (variables != null) {
      for (Map.Entry<String, TriggerVariableValueDto> variableValue : variables.entrySet()) {
        TriggerVariableValueDto value = variableValue.getValue();

        if (value.isLocal()) {
          builder.setVariableLocal(variableValue.getKey(), value.toTypedValue(engine, mapper));
        }
        else {
          builder.setVariable(variableValue.getKey(), value.toTypedValue(engine, mapper));

        }
      }
    }
  }

  @JsonTypeName(START_BEFORE_INSTRUCTION_TYPE)
  public static class StartBeforeInstructionDto extends ProcessInstanceModificationInstructionDto {

    @Override
    public void applyTo(ProcessInstanceModificationBuilder builder, ProcessEngine engine, ObjectMapper mapper) {
      if (activityId == null) {
        throw new InvalidRequestException(Status.BAD_REQUEST,
            buildErrorMessage("'activityId' must be set"));
      }

      ProcessInstanceActivityInstantiationBuilder activityBuilder = null;

      if (ancestorActivityInstanceId != null) {
        activityBuilder = builder.startBeforeActivity(activityId, ancestorActivityInstanceId);
      }
      else {
        activityBuilder = builder.startBeforeActivity(activityId);
      }

      applyVariables(activityBuilder, engine, mapper);

    }
  }

  @JsonTypeName(CANCEL_INSTRUCTION_TYPE)
  public static class CancellationInstructionDto extends ProcessInstanceModificationInstructionDto {

    @Override
    public void applyTo(ProcessInstanceModificationBuilder builder, ProcessEngine engine, ObjectMapper mapper) {

      validateParameters();

      if (activityId != null) {
        builder.cancelAllForActivity(activityId);
      }
      else if (activityInstanceId != null) {
        builder.cancelActivityInstance(activityInstanceId);
      }
      else if (transitionInstanceId != null) {
        builder.cancelTransitionInstance(transitionInstanceId);
      }

    }

    protected void validateParameters() {
      // exactly one parameter should be set
      boolean oneParameterSet = false;
      boolean moreThanOneParametersSet = false;

      if (activityId != null) {
        oneParameterSet = true;
      }

      if (activityInstanceId != null) {
        moreThanOneParametersSet |= oneParameterSet;
        oneParameterSet = true;
      }

      if (transitionInstanceId != null) {
        moreThanOneParametersSet |= oneParameterSet;
        oneParameterSet = true;
      }

      if (moreThanOneParametersSet || !oneParameterSet) {
        throw new InvalidRequestException(Status.BAD_REQUEST, buildErrorMessage(
            "exactly one, 'activityId', 'activityInstanceId', or 'transitionInstanceId', is required"));
      }
    }
  }

  @JsonTypeName(START_AFTER_INSTRUCTION_TYPE)
  public static class StartAfterInstructionDto extends ProcessInstanceModificationInstructionDto {

    @Override
    public void applyTo(ProcessInstanceModificationBuilder builder, ProcessEngine engine, ObjectMapper mapper) {
      if (activityId == null) {
        throw new InvalidRequestException(Status.BAD_REQUEST,
            buildErrorMessage("'activityId' must be set"));
      }

      ProcessInstanceActivityInstantiationBuilder activityBuilder = null;

      if (ancestorActivityInstanceId != null) {
        activityBuilder = builder.startAfterActivity(activityId, ancestorActivityInstanceId);
      }
      else {
        activityBuilder = builder.startAfterActivity(activityId);
      }

      applyVariables(activityBuilder, engine, mapper);
    }
  }

  @JsonTypeName(START_TRANSITION_INSTRUCTION_TYPE)
  public static class StartTransitionInstructionDto extends ProcessInstanceModificationInstructionDto {

    @Override
    public void applyTo(ProcessInstanceModificationBuilder builder, ProcessEngine engine, ObjectMapper mapper) {
      if (activityId == null) {
        throw new InvalidRequestException(Status.BAD_REQUEST,
            buildErrorMessage("'activityId' must be set"));
      }

      ProcessInstanceActivityInstantiationBuilder activityBuilder = null;

      if (ancestorActivityInstanceId != null) {
        activityBuilder = builder.startTransition(activityId, ancestorActivityInstanceId);
      }
      else {
        activityBuilder = builder.startTransition(activityId);
      }

      applyVariables(activityBuilder, engine, mapper);
    }
  }
}
