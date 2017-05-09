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

}
