/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.rest.dto.message;

import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionDto;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class MessageCorrelationResultDto {

  private String resultType;

  //restul type execution
  private ExecutionDto execution;

  //result type process definition
  private String startEventActivityId;
  private ProcessDefinitionDto processDefinition;

  public static MessageCorrelationResultDto fromMessageCorrelationResult(MessageCorrelationResult result) {
    MessageCorrelationResultDto dto = new MessageCorrelationResultDto();
    if (result != null) {
      dto.resultType = result.getResultType();
      dto.startEventActivityId = result.getStartEventActivityId();
      if (result.getProcessDefinition() != null) {
        dto.processDefinition = ProcessDefinitionDto.fromProcessDefinition(result.getProcessDefinition());
      } else if (result.getProcessDefinition() != null) {
        dto.execution = ExecutionDto.fromExecution(result.getExecution());
      }
    }
    return dto;
  }

  public String getResultType() {
    return resultType;
  }

  public ExecutionDto getExecution() {
    return execution;
  }

  public String getStartEventActivityId() {
    return startEventActivityId;
  }

  public ProcessDefinitionDto getProcessDefinition() {
    return processDefinition;
  }

}
