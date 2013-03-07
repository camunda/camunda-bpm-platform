package org.camunda.bpm.engine.rest.dto.repository;

import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.rest.dto.StatisticsResultDto;

public class ActivityStatisticsResultDto extends StatisticsResultDto {

  public static ActivityStatisticsResultDto fromActivityStatistics(ActivityStatistics statistics) {
    ActivityStatisticsResultDto dto = new ActivityStatisticsResultDto();
    dto.id = statistics.getId();
    dto.instances = statistics.getInstances();
    dto.failedJobs = statistics.getFailedJobs();
    return dto;
  }
  
}
