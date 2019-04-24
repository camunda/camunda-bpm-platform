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

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.runtime.InstantiationBuilder;
import org.camunda.bpm.engine.runtime.ModificationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Thorben Lindhauer
 *
 */
@JsonTypeName(ProcessInstanceModificationInstructionDto.CANCEL_INSTRUCTION_TYPE)
public class CancellationInstructionDto extends ProcessInstanceModificationInstructionDto {

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

  @Override
  public void applyTo(InstantiationBuilder<?> builder, ProcessEngine engine, ObjectMapper mapper) {
    // cannot be applied to instantiation

    if (builder instanceof ModificationBuilder) {
      if (activityId == null) {
        throw new InvalidRequestException(Status.BAD_REQUEST, buildErrorMessage("'activityId' must be set"));
      }
      if (cancelCurrentActiveActivityInstances) {
        ((ModificationBuilder) builder).cancelAllForActivity(activityId, true);
      } else {
        ((ModificationBuilder) builder).cancelAllForActivity(activityId);
      }
    }
  }


}