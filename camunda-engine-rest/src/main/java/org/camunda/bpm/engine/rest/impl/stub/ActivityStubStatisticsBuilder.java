package org.camunda.bpm.engine.rest.impl.stub;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.rest.dto.StatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ActivityStatisticsResultDto;

public class ActivityStubStatisticsBuilder {
  
  private ActivityStatisticsResultDto currentStatisticsEntity;
  private List<StatisticsResultDto> collectedResults;
  
  private ActivityStubStatisticsBuilder() {
    currentStatisticsEntity = new ActivityStatisticsResultDto();
    collectedResults = new ArrayList<StatisticsResultDto>();
  }
  
  public static ActivityStubStatisticsBuilder addResult() {
    return new ActivityStubStatisticsBuilder();
  }
  public ActivityStubStatisticsBuilder nextResult() {
    collectedResults.add(currentStatisticsEntity);
    currentStatisticsEntity = new ActivityStatisticsResultDto();
    return this;
  }
  
  public ActivityStubStatisticsBuilder id(String id) {
    currentStatisticsEntity.setId(id);
    return this;
  }
  
  public ActivityStubStatisticsBuilder instances(int instances) {
    currentStatisticsEntity.setInstances(instances);
    return this;
  }
  
  public ActivityStubStatisticsBuilder failedJobs(int failedJobs) {
    currentStatisticsEntity.setFailedJobs(failedJobs);
    return this;
  }
  
  public ActivityStubStatisticsBuilder activityName(String name) {
    currentStatisticsEntity.setName(name);
    return this;
  }
  
  public List<StatisticsResultDto> build() {
    collectedResults.add(currentStatisticsEntity);
    return collectedResults;
  }
  
}
