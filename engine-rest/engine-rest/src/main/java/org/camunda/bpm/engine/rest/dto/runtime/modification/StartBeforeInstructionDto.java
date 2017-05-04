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
package org.camunda.bpm.engine.rest.dto.runtime.modification;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.runtime.InstantiationBuilder;
import org.camunda.bpm.engine.runtime.ModificationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationInstantiationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Thorben Lindhauer
 *
 */
@JsonTypeName(ProcessInstanceModificationInstructionDto.START_BEFORE_INSTRUCTION_TYPE)
public class StartBeforeInstructionDto extends ProcessInstanceModificationInstructionDto {

  @Override
  public void applyTo(ProcessInstanceModificationBuilder builder, ProcessEngine engine, ObjectMapper mapper) {
    checkValidity();

    ProcessInstanceModificationInstantiationBuilder activityBuilder = null;

    if (ancestorActivityInstanceId != null) {
      activityBuilder = builder.startBeforeActivity(activityId, ancestorActivityInstanceId);
    }
    else {
      activityBuilder = builder.startBeforeActivity(activityId);
    }

    applyVariables(activityBuilder, engine, mapper);

  }

  @Override
  public void applyTo(InstantiationBuilder<?> builder, ProcessEngine engine, ObjectMapper mapper) {
    checkValidity();

    builder.startBeforeActivity(activityId);

    if (builder instanceof ProcessInstantiationBuilder) {
      applyVariables((ProcessInstantiationBuilder) builder, engine, mapper);
    }
  }

  protected void checkValidity() {
    if (activityId == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST,
          buildErrorMessage("'activityId' must be set"));
    }
  }


}