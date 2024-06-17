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

import org.camunda.bpm.engine.rest.dto.runtime.ExecutionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class MessageCorrelationResultDto {

  private MessageCorrelationResultType resultType;

  //restul type execution
  private ExecutionDto execution;

  //result type process definition
  private ProcessInstanceDto processInstance;

  public static MessageCorrelationResultDto fromMessageCorrelationResult(MessageCorrelationResult result) {
    MessageCorrelationResultDto dto = new MessageCorrelationResultDto();
    return setExecutionAndProcessInstance(dto, result);
  }

  public static MessageCorrelationResultDto setExecutionAndProcessInstance(MessageCorrelationResultDto dto, MessageCorrelationResult result) {
    if (result != null) {
      dto.setResultType(result.getResultType());
      if (result.getResultType() == MessageCorrelationResultType.Execution && result.getExecution() != null) {
        dto.setExecution(ExecutionDto.fromExecution(result.getExecution()));
      } else if (result.getResultType() == MessageCorrelationResultType.ProcessDefinition && result.getProcessInstance() != null) {
        dto.setProcessInstance(ProcessInstanceDto.fromProcessInstance(result.getProcessInstance()));
      }
    }
    return dto;
  }

  public MessageCorrelationResultType getResultType() {
    return resultType;
  }

  public void setResultType(MessageCorrelationResultType resultType) {
    this.resultType = resultType;
  }

  public ExecutionDto getExecution() {
    return execution;
  }

  public void setExecution(ExecutionDto execution) {
    this.execution = execution;
  }

  public ProcessInstanceDto getProcessInstance() {
    return processInstance;
  }

  public void setProcessInstance(ProcessInstanceDto processInstance) {
    this.processInstance = processInstance;
  }

}
