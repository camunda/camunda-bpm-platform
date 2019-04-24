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
package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.modification.ProcessInstanceModificationInstructionDto;
import org.camunda.bpm.engine.runtime.RestartProcessInstanceBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author Anna Pazola
 *
 */
public class RestartProcessInstanceDto {

  protected List<String> processInstanceIds;
  protected List<ProcessInstanceModificationInstructionDto> instructions;
  protected HistoricProcessInstanceQueryDto historicProcessInstanceQuery;
  protected boolean initialVariables;
  protected boolean skipCustomListeners;
  protected boolean skipIoMappings;
  protected boolean withoutBusinessKey;

  public List<String> getProcessInstanceIds() {
    return processInstanceIds;
  }

  public void setProcessInstanceIds(List<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
  }

  public List<ProcessInstanceModificationInstructionDto> getInstructions() {
    return instructions;
  }

  public void setInstructions(List<ProcessInstanceModificationInstructionDto> instructions) {
    this.instructions = instructions;
  }

  public HistoricProcessInstanceQueryDto getHistoricProcessInstanceQuery() {
    return historicProcessInstanceQuery;
  }

  public void setHistoricProcessInstanceQuery(HistoricProcessInstanceQueryDto historicProcessInstanceQuery) {
    this.historicProcessInstanceQuery = historicProcessInstanceQuery;
  }

  public boolean isInitialVariables() {
    return initialVariables;
  }

  public void setInitialVariables(boolean initialVariables) {
    this.initialVariables = initialVariables;
  }

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

  public boolean isWithoutBusinessKey() {
    return withoutBusinessKey;
  }

  public void setWithoutBusinessKey(boolean withoutBusinessKey) {
    this.withoutBusinessKey = withoutBusinessKey;
  }

  public void applyTo(RestartProcessInstanceBuilder builder, ProcessEngine processEngine, ObjectMapper objectMapper) {
    for (ProcessInstanceModificationInstructionDto instruction : instructions) {

      instruction.applyTo(builder, processEngine, objectMapper);
    }
  }
}
