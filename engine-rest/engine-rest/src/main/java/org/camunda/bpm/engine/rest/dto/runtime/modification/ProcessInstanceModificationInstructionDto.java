/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.dto.runtime.modification;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.runtime.TriggerVariableValueDto;
import org.camunda.bpm.engine.runtime.ActivityInstantiationBuilder;
import org.camunda.bpm.engine.runtime.InstantiationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Thorben Lindhauer
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes(value = {
    @JsonSubTypes.Type(value = CancellationInstructionDto.class),
    @JsonSubTypes.Type(value = StartBeforeInstructionDto.class),
    @JsonSubTypes.Type(value = StartAfterInstructionDto.class),
    @JsonSubTypes.Type(value = StartTransitionInstructionDto.class)})
public abstract class ProcessInstanceModificationInstructionDto {

  public static final String CANCEL_INSTRUCTION_TYPE = "cancel";
  public static final String START_BEFORE_INSTRUCTION_TYPE = "startBeforeActivity";
  public static final String START_TRANSITION_INSTRUCTION_TYPE = "startTransition";
  public static final String START_AFTER_INSTRUCTION_TYPE = "startAfterActivity";

  protected String type;

  protected Map<String, TriggerVariableValueDto> variables;

  protected String activityId;
  protected String transitionId;
  protected String activityInstanceId;
  protected String transitionInstanceId;
  protected String ancestorActivityInstanceId;
  protected boolean cancelCurrentActiveActivityInstances;

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
  public String getTransitionId() {
    return transitionId;
  }
  public void setTransitionId(String transitionId) {
    this.transitionId = transitionId;
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
  public boolean isCancelCurrentActiveActivityInstances() {
    return cancelCurrentActiveActivityInstances;
  }
  public void setCancelCurrentActiveActivityInstances(boolean cancelCurrentActiveActivityInstances) {
    this.cancelCurrentActiveActivityInstances = cancelCurrentActiveActivityInstances;
  }

  public abstract void applyTo(ProcessInstanceModificationBuilder builder, ProcessEngine engine, ObjectMapper mapper);

  public abstract void applyTo(InstantiationBuilder<?> builder, ProcessEngine engine, ObjectMapper mapper);

  protected String buildErrorMessage(String message) {
    return "For instruction type '" + type + "': " +  message;
  }

  protected void applyVariables(ActivityInstantiationBuilder<?> builder,
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

}
