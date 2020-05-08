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
package org.camunda.bpm.engine.rest.dto;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.modification.ProcessInstanceModificationInstructionDto;
import org.camunda.bpm.engine.runtime.ModificationBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ModificationDto {

  protected List<ProcessInstanceModificationInstructionDto> instructions;
  protected List<String> processInstanceIds;
  protected ProcessInstanceQueryDto processInstanceQuery;
  protected String processDefinitionId;
  protected boolean skipIoMappings;
  protected boolean skipCustomListeners;
  protected String annotation;

  public List<ProcessInstanceModificationInstructionDto> getInstructions() {
    return instructions;
  }

  public void setInstructions(List<ProcessInstanceModificationInstructionDto> instructions) {
    this.instructions = instructions;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public List<String> getProcessInstanceIds() {
    return processInstanceIds;
  }

  public void setProcessInstanceIds(List<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
  }

  public ProcessInstanceQueryDto getProcessInstanceQuery() {
    return processInstanceQuery;
  }

  public void setProcessInstanceQuery(ProcessInstanceQueryDto processInstanceQuery) {
    this.processInstanceQuery = processInstanceQuery;
  }

  public boolean isSkipIoMappings() {
    return skipIoMappings;
  }

  public void setSkipIoMappings(boolean skipIoMappings) {
    this.skipIoMappings = skipIoMappings;
  }

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public void setSkipCustomListeners(boolean skipCustomListeners) {
    this.skipCustomListeners = skipCustomListeners;
  }

  public void applyTo(ModificationBuilder builder, ProcessEngine processEngine, ObjectMapper objectMapper) {
    for (ProcessInstanceModificationInstructionDto instruction : instructions) {

      instruction.applyTo(builder, processEngine, objectMapper);
    }

  }

  public String getAnnotation() {
    return annotation;
  }

  public void setAnnotation(String annotation) {
    this.annotation = annotation;
  }

}
