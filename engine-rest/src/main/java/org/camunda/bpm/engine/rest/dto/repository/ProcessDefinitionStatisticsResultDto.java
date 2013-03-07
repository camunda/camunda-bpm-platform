package org.camunda.bpm.engine.rest.dto.repository;

import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.rest.dto.StatisticsResultDto;

public class ProcessDefinitionStatisticsResultDto extends StatisticsResultDto {

  private ProcessDefinitionDto definition;

  public ProcessDefinitionDto getDefinition() {
    return definition;
  }

  public void setDefinition(ProcessDefinitionDto definition) {
    this.definition = definition;
  }
  
  public static ProcessDefinitionStatisticsResultDto fromProcessDefinitionStatistics(ProcessDefinitionStatistics statistics) {
    ProcessDefinitionStatisticsResultDto dto = new ProcessDefinitionStatisticsResultDto();
    dto.definition = ProcessDefinitionDto.fromProcessDefinition(statistics);
    dto.id = statistics.getId();
    dto.instances = statistics.getInstances();
    dto.failedJobs = statistics.getFailedJobs();
    return dto;
  }
}
