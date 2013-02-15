package org.camunda.bpm.engine.rest.impl.stub;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.rest.dto.StatisticsResultDto;

public class StubStatisticsBuilder {

  private StatisticsResultDto currentStatisticsEntity;
  private List<StatisticsResultDto> collectedResults;
  
  private StubStatisticsBuilder() {
    currentStatisticsEntity = new StatisticsResultDto();
    collectedResults = new ArrayList<StatisticsResultDto>();
  }
  
  public static StubStatisticsBuilder addResult() {
    return new StubStatisticsBuilder();
  }
  public StubStatisticsBuilder nextResult() {
    collectedResults.add(currentStatisticsEntity);
    currentStatisticsEntity = new StatisticsResultDto();
    return this;
  }
  
  public StubStatisticsBuilder id(String id) {
    currentStatisticsEntity.setId(id);
    return this;
  }
  
  public StubStatisticsBuilder instances(int instances) {
    currentStatisticsEntity.setInstances(instances);
    return this;
  }
  
  public StubStatisticsBuilder failedJobs(int failedJobs) {
    currentStatisticsEntity.setFailedJobs(failedJobs);
    return this;
  }
  
  public List<StatisticsResultDto> build() {
    collectedResults.add(currentStatisticsEntity);
    return collectedResults;
  }
}
