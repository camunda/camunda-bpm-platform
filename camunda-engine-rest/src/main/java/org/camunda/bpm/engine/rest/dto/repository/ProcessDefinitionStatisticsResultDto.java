package org.camunda.bpm.engine.rest.dto.repository;

import org.camunda.bpm.engine.rest.dto.StatisticsResultDto;

public class ProcessDefinitionStatisticsResultDto extends StatisticsResultDto {

  private ProcessDefinitionDto definition;

  public ProcessDefinitionDto getDefinition() {
    return definition;
  }

  public void setDefinition(ProcessDefinitionDto definition) {
    this.definition = definition;
  }
}
