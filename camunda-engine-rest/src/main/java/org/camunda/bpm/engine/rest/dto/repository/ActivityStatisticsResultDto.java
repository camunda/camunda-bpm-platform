package org.camunda.bpm.engine.rest.dto.repository;

import org.camunda.bpm.engine.rest.dto.StatisticsResultDto;

public class ActivityStatisticsResultDto extends StatisticsResultDto {

  private String name;
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
}
