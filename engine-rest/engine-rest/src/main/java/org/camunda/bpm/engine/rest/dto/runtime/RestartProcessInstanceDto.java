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
