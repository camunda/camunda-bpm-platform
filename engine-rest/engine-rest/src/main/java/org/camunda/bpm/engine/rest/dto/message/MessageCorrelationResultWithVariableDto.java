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
package org.camunda.bpm.engine.rest.dto.message;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultWithVariables;

public class MessageCorrelationResultWithVariableDto extends MessageCorrelationResultDto {

  private Map<String, VariableValueDto> variables;

  public static MessageCorrelationResultWithVariableDto fromMessageCorrelationResultWithVariables(MessageCorrelationResultWithVariables result) {
    MessageCorrelationResultWithVariableDto dto = new MessageCorrelationResultWithVariableDto();

    if (result != null) {
      dto.setResultType(result.getResultType());
      if (result.getProcessInstance() != null) {
        dto.setProcessInstance(ProcessInstanceDto.fromProcessInstance(result.getProcessInstance()));
      } else if (result.getExecution() != null) {
        dto.setExecution(ExecutionDto.fromExecution(result.getExecution()));
      }

      dto.variables = VariableValueDto.fromMap(result.getVariables(), true);
    }
    return dto;
  }

  public Map<String, VariableValueDto> getVariables() {
    return variables;
  }
}
